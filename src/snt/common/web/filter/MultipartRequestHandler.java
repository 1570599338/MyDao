/**
 * 
 */
package snt.common.web.filter;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import snt.common.web.util.FileUtil;
import snt.common.web.util.WebUtils;

/**
 * 处理请求中的上传文件等资源的Handler
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class MultipartRequestHandler {
    protected static Log log = LogFactory.getLog(MultipartRequestHandler.class);
    public static final String ATTRIBUTE_MAX_LENGTH_EXCEEDED = "upload.MaxLengthExceeded";
    
    private static long maxFileSize = FileUtil.convertSizeToBytes(WebUtils.getModuleProperty("upload.maxFileSize"), 20*1024*1024);
	private static String repositoryFilePath = WebUtils.getModuleProperty("upload.repository");
	private static int thresholdFileSize = (int)FileUtil.convertSizeToBytes(WebUtils.getModuleProperty("upload.thresholdFileSize"), 250*1024);
	
    /**
     * The combined text and file request parameters.
     */
    private Map<String, String[]> elementsText;

    /**
     * The file request parameters.
     */
    private Map<String, FileItem> elementsFile;

    /**
     * Parses the input stream and partitions the parsed items into a set of
     * form fields and a set of file items. In the process, the parsed items
     * are translated from Commons FileUpload <code>FileItem</code> instances
     * to Struts <code>FormFile</code> instances.
     *
     * @param request The multipart request to be processed.
     *
     * @throws ServletException if an unrecoverable error occurs.
     */
    public void handleRequest(HttpServletRequestDecorator request)
            throws ServletException {
    	DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		//设置缓存目录
		diskFileItemFactory.setRepository(new File(repositoryFilePath));
		//设置允许在内存中存储的数据，单位：字节
		diskFileItemFactory.setSizeThreshold(thresholdFileSize);
		//初始化FILEUPLOAD组件的ServletFileUpload类
		ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        // The following line is to support an "EncodingFilter"
        // see http://nagoya.apache.org/bugzilla/show_bug.cgi?id=23255
        upload.setHeaderEncoding(request.getCharacterEncoding());
        // Set the maximum size before a FileUploadException will be thrown.
        upload.setSizeMax(maxFileSize);

        // Create the hash tables to be populated.
        elementsText = new HashMap<String, String[]>();
        elementsFile = new HashMap<String, FileItem>();

        // Parse the request into file items.
        List items = null;
        try {
            items = upload.parseRequest((HttpServletRequest)request.getRequest());
        } catch (FileUploadBase.SizeLimitExceededException e) {
            // Special handling for uploads that are too big.
            request.setAttribute(ATTRIBUTE_MAX_LENGTH_EXCEEDED, Boolean.TRUE);
            return;
        } catch (FileUploadException e) {
            log.error("Failed to parse multipart request", e);
            throw new ServletException(e);
        }

        // Partition the items into form fields and files.
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {
                addTextParameter(request, item);
            } else {
                addFileParameter(item);
            }
        }
        request.setParameters(elementsText);
        request.setFileItem(elementsFile);
    }

    /**
     * Cleans up when a problem occurs during request processing.
     */
    public void rollback() {
        Iterator iter = elementsFile.values().iterator();

        while (iter.hasNext()) {
        	FileItem formFile = (FileItem) iter.next();

            formFile.delete();
        }
    }

    /**
     * Cleans up at the end of a request.
     */
    public void finish() {
        rollback();
    }    

    /**
     * Adds a regular text parameter to the set of text parameters for this
     * request and also to the list of all parameters. Handles the case of
     * multiple values for the same parameter by using an array for the
     * parameter value.
     *
     * @param request The request in which the parameter was specified.
     * @param item    The file item for the parameter to add.
     */
    protected void addTextParameter(HttpServletRequest request, FileItem item) {
        String name = item.getFieldName();
        String value = null;
        boolean haveValue = false;
        String encoding = request.getCharacterEncoding();

        if (encoding != null) {
            try {
                value = item.getString(encoding);
                haveValue = true;
            } catch (Exception e) {
                // Handled below, since haveValue is false.
            }
        }
        if (!haveValue) {
            try {
                 value = item.getString("ISO-8859-1");
            } catch (java.io.UnsupportedEncodingException uee) {
                 value = item.getString();
            }
            haveValue = true;
        }

        String[] oldArray = (String[]) elementsText.get(name);
        String[] newArray;

        if (oldArray != null) {
            newArray = new String[oldArray.length + 1];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            newArray[oldArray.length] = value;
        } else {
            newArray = new String[] { value };
        }

        elementsText.put(name, newArray);
    }


    /**
     * Adds a file parameter to the set of file parameters for this request
     * and also to the list of all parameters.
     *
     * @param item    The file item for the parameter to add.
     */
    protected void addFileParameter(FileItem item) {
        elementsFile.put(item.getFieldName(), item);
        elementsText.put(item.getFieldName(), new String[]{item.getName()});
    }
}
