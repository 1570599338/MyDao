// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2004-12-6 11:46:28
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) definits fieldsfirst ansi
// Source File Name:   ByteBlob.java

package snt.common.rs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * 内存结果集中使用的Blob实现
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-9-5
 */
public class ByteBlob implements Blob, Serializable {
    private static final long serialVersionUID = 1111L;
    byte buf[] = null;

    public ByteBlob(byte buf[]) {
        this.buf = buf;
    }

    public ByteBlob(Blob blob) throws SQLException{
        this(blob==null?null:blob.getBinaryStream());
    }

    public ByteBlob(InputStream in){
        java.io.ByteArrayOutputStream baos = null;
		try {
			if (in == null){
				return;
			}
			baos = new java.io.ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			buffer = baos.toByteArray();
			buf = buffer;
		}
		catch(Exception e){
		    e.printStackTrace();
		}
		finally {
			if (in != null){
				try {
                    in.close();
                }
                catch (IOException e1) {
                }
			}
			if (baos != null){
				try {
                    baos.close();
                }
                catch (IOException e1) {
                }
			}
		}
    }

    public long length() throws SQLException {
        return buf==null?0:(long) buf.length;
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        if(buf == null){
            return null;
        }
        else{
            if (--pos < 0) {//注意，参数pos以1为基
                pos = 0;
            }
            if(length+pos>buf.length){
                length = buf.length-(int)pos;
            }
            byte[] newBytes = new byte[length];
            System.arraycopy(buf, (int)pos, newBytes,0, length);
            return newBytes;
        }
    }

    public InputStream getBinaryStream() throws SQLException {
        return buf==null?null:new ByteArrayInputStream(buf);
    }

    public long position(byte pattern[], long start) throws SQLException {
        throw new UnsupportedOperationException(
                "Method position() not yet implemented.");
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw new UnsupportedOperationException(
                "Method position() not yet implemented.");
    }

    public int setBytes(long pos, byte bytes[]) throws SQLException {
        throw new UnsupportedOperationException(
                "Method setBytes() not yet implemented.");
    }

    public int setBytes(long pos, byte bytes[], int offset, int len)
            throws SQLException {
        throw new UnsupportedOperationException(
                "Method setBytes() not yet implemented.");
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new UnsupportedOperationException(
                "Method setBinaryStream() not yet implemented.");
    }

    public void truncate(long len) throws SQLException {
        throw new UnsupportedOperationException(
                "Method truncate() not yet implemented.");
    }

	public void free() throws SQLException {
	    this.buf = null;
	}

	 public InputStream getBinaryStream(long pos, long length)throws SQLException{
	    return this.buf == null ? null : new ByteArrayInputStream(this.buf, (int)(pos - 1L), (int)length);
	  }
}
