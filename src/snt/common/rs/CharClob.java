// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2004-12-6 11:46:34
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) definits fieldsfirst ansi
// Source File Name:   ByteClob.java

package snt.common.rs;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * 内存结果集中采用的Clob实现
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-9-5
 */
public class CharClob implements Clob, Serializable {
    private static final long serialVersionUID = 1111L;
    char buf[] = null;

    public CharClob(char[] buf) {
        this.buf = buf;
    }
    
    public CharClob(Clob clob) throws SQLException {
        this(clob == null ? null : clob.getCharacterStream());
      }

      public CharClob(Reader in) throws SQLException {
        CharArrayWriter baos = null;
        try {
          if (in == null) {
            return;
          }
          baos = new CharArrayWriter();
          char[] charBuf = new char[1024];
          int len = -1;
          while ((len = in.read(charBuf)) != -1) {
            baos.write(charBuf, 0, len);
          }
          this.buf = baos.toCharArray();
        } catch (IOException e) {
          throw new SQLException("��ȡ��Ϣ����", e);
        } finally {
          if (in != null)
            try {
              in.close();
            }
            catch (IOException e1) {
            }
          if (baos != null)
            baos.close();
        }
      }

    

   /* public CharClob(Clob clob) throws SQLException {
        this(clob == null ? null : clob.getCharacterStream());
      }*/
    
   /* public CharClob(Clob clob) {
        java.io.Reader in = null;
		java.io.CharArrayWriter baos = null;
		try {
			if (clob == null){
				return;
			}
			in = clob.getCharacterStream();
			baos = new java.io.CharArrayWriter();
			char[] charBuf = new char[1024];
			int len = -1;
			while ((len = in.read(charBuf)) != -1) {
				baos.write(charBuf, 0, len);
			}
			buf = baos.toCharArray();
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
			    baos.close();
			}
		}
    }*/

    public long length() throws SQLException {
        return buf==null?0:(long) buf.length;
    }

    public String getSubString(long pos, int length) throws SQLException {
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
            return new String(buf, (int)pos, length);
        }
    }

    public Reader getCharacterStream() throws SQLException {
        return buf==null?null:new CharArrayReader(buf);
    }

    public InputStream getAsciiStream() throws SQLException {
        return buf==null?null:new ByteArrayInputStream(new String(buf).getBytes());
    }

    public long position(String searchstr, long start) throws SQLException {
        throw new UnsupportedOperationException(
                "Method position() not yet implemented.");
    }

    public long position(Clob searchstr, long start) throws SQLException {
        throw new UnsupportedOperationException(
                "Method position() not yet implemented.");
    }

    public int setString(long pos, String str) throws SQLException {
        throw new UnsupportedOperationException(
                "Method setString() not yet implemented.");
    }

    public int setString(long pos, String str, int offset, int len)
            throws SQLException {
        throw new UnsupportedOperationException(
                "Method setString() not yet implemented.");
    }

    public OutputStream setAsciiStream(long pos) throws SQLException {
        throw new UnsupportedOperationException(
                "Method setAsciiStream() not yet implemented.");
    }

    public Writer setCharacterStream(long pos) throws SQLException {
        throw new UnsupportedOperationException(
                "Method setCharacterStream() not yet implemented.");
    }

    public void truncate(long len) throws SQLException {
        throw new UnsupportedOperationException(
                "Method truncate() not yet implemented.");
    }

	public void free() throws SQLException {
		 this.buf = null;
		
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		 return this.buf == null ? null : new CharArrayReader(this.buf, (int)(pos - 1L), (int)length);
	}
}
