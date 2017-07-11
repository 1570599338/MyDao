/**
 * 
 */
package snt.common.dao.base;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.orm.ObjectRetrievalFailureException;

import snt.common.business.AutoAssemble;

/**
 * 把结果集转换为pojo列表的工具
 * 结果集中的每一条记录会被转换为一个pojo实例
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class PojoResultSetExtractor implements ResultSetExtractor {
	private Map<String, String> col2PropMap;
	private Map<Integer, PropertyDescriptor> colIndex2PropDescMap;
	private Class pojoClass;
	private PropertyDescriptor verPropDesc;
	private boolean clob2String = true;
	private boolean blob2Obj = false;
	
	public PojoResultSetExtractor(Class cls) {
		this(cls, null);
	}
	
	@SuppressWarnings("unchecked")
	public PojoResultSetExtractor(Class cls, Map<String, String> col2PropMap){
		this.pojoClass = cls;
		if(col2PropMap != null){
			this.col2PropMap = new HashMap<String, String>();
			for (String key : col2PropMap.keySet()) {//不区分大小写
				String value = col2PropMap.get(key);
				this.col2PropMap.put(key==null?null:key.toUpperCase(), value==null?null:value.toUpperCase());
			}
		}		
		else if(cls.getAnnotation(AutoAssemble.class) != null){
			List<String[]> propFldMap = AutoAssembleConfig.getPropFldMap(cls);
			verPropDesc = AutoAssembleConfig.getVerPropDesc(cls);
			this.col2PropMap = new HashMap<String, String>();
			for (String[] propFld : propFldMap) {
				this.col2PropMap.put(propFld[1].toUpperCase(), propFld[0].toUpperCase());
			}
		}
	}
	
	public PojoResultSetExtractor(Class cls, Map<String, String> col2PropMap, boolean clob2String, boolean blob2Obj){
		this(cls, col2PropMap);
		this.clob2String = clob2String;
		this.blob2Obj = blob2Obj;
	}

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	@SuppressWarnings("unchecked")
	public List extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		PropertyDescriptor[] propDescs = BeanUtils.getPropertyDescriptors(pojoClass);
		ResultSetMetaData rsMetaData = rs.getMetaData();
		buildCol2PropMap(rsMetaData, propDescs);
		List objList = new ArrayList(20);
		try {
			while (rs.next()) {
				Object pojo = pojoClass.newInstance();
				for (Integer colIndex : colIndex2PropDescMap.keySet()) {
					Object value = getValue(rs, colIndex, rsMetaData.getColumnType(colIndex));					
					if(value != null){
						PropertyDescriptor propertyDescriptor = colIndex2PropDescMap.get(colIndex);						propertyDescriptor.getWriteMethod().invoke(pojo, new Object[]{value});
					}
				}
				if(verPropDesc != null){
					verPropDesc.getWriteMethod().invoke(pojo, new Object[]{1});
				}
				objList.add(pojo);
			}
		} catch (Throwable e) {
			throw new ObjectRetrievalFailureException("拼装POJO对象出错！", e);
		}
		colIndex2PropDescMap = null;
		return objList;
	}
	
	private void buildCol2PropMap(ResultSetMetaData rsMetaData, PropertyDescriptor[] propDescs) throws SQLException{
		boolean useDefaultMapping = (col2PropMap == null);
		colIndex2PropDescMap = new HashMap<Integer, PropertyDescriptor>();
		
		Map<String, PropertyDescriptor> propDescMap = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor descriptor : propDescs) {
			propDescMap.put(descriptor.getName().toUpperCase(), descriptor);
		}
		int columnCount = rsMetaData.getColumnCount();
		for (int col = 0; col < columnCount; col++) {
			String colName = rsMetaData.getColumnName(col+1).toUpperCase();
			String propName = useDefaultMapping?colName:col2PropMap.get(colName);
			if (propName != null && propDescMap.containsKey(propName)) {
				PropertyDescriptor propDesc = propDescMap.get(propName);
				if(propDesc.getWriteMethod() != null){
					colIndex2PropDescMap.put(col+1, propDescMap.get(propName));
				}
			}
		}
	}
	
	private Object getValue(ResultSet rs, int columnIndex, int columnType) throws SQLException{
		Object obj = null;
		switch (columnType) {
		case Types.BLOB:{
			Blob blob = rs.getBlob(columnIndex);
			obj = blob==null?null:
				(blob2Obj?getObjectFromInputStream(blob.getBinaryStream()):blob);
			break;
		}			
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:{
			obj = blob2Obj?getObjectFromInputStream(rs.getBinaryStream(columnIndex))
					:rs.getObject(columnIndex);
			break;
		}
		case Types.CLOB:{
			Clob clob = rs.getClob(columnIndex);
			if (clob2String) {
				obj = clob==null?null:clob.getSubString(1, (int)clob.length());
			}
			else{
				obj = clob;
			}
			break;
		}
		default:{
			obj = rs.getObject(columnIndex);
			break;
		}
		}
		return obj;
	}
	
	private Object getObjectFromInputStream(InputStream in){
		if (in == null){
			return null;
		}
		java.io.ByteArrayOutputStream baos = null;
		ObjectInputStream objIn = null;
		try {			
			baos = new java.io.ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			buffer = baos.toByteArray();
			objIn = new ObjectInputStream(new ByteArrayInputStream(buffer));
			return objIn.readObject();
		}
		catch(IOException e){
		    throw new LobRetrievalFailureException("将Blob类型字段里的对象转换为Java POJO出错", e);
		}
		catch(Exception e){
			throw new LobRetrievalFailureException("将Blob类型字段里的对象转换为Java POJO出错");
		}
		finally {
			if (in != null){
				try {
                    in.close();
                }
                catch (IOException e1) {
                	throw new LobRetrievalFailureException("将Blob类型字段里的对象转换为Java POJO出错", e1);
                }
			}
			if (baos != null){
				try {
                    baos.close();
                }
                catch (IOException e1) {
                	throw new LobRetrievalFailureException("将Blob类型字段里的对象转换为Java POJO出错", e1);
                }
			}
			if (objIn != null) {
				try {
					objIn.close();
				} catch (IOException e1) {
					throw new LobRetrievalFailureException("将Blob类型字段里的对象转换为Java POJO出错", e1);
				}
			}
		}
	}
}
