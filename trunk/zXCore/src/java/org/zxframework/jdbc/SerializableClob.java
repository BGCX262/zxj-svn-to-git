//$Id: SerializableClob.java,v 1.1.2.2 2006/07/17 16:18:27 mike Exp $
package org.zxframework.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author Gavin King
 */
public class SerializableClob implements Serializable, Clob {

	private transient final Clob clob;
	
	/**
	 * @param blob The clob to wrap.
	 */
	public SerializableClob(Clob blob) {
		this.clob = blob;
	}

	/**
	 * @return Returns the wrapped clob.
	 */
	public Clob getWrappedClob() {
		if ( clob==null ) {
			throw new IllegalStateException("Clobs may not be accessed after serialization");
		}
		
		return clob;
	}
	
	/**
	 * @see java.sql.Clob#length()
	 */
	public long length() throws SQLException {
		return getWrappedClob().length();
	}

	/**
	 * @see java.sql.Clob#getSubString(long, int)
	 */
	public String getSubString(long pos, int length) throws SQLException {
		return getWrappedClob().getSubString(pos, length);
	}

	/**
	 * @see java.sql.Clob#getCharacterStream()
	 */
	public Reader getCharacterStream() throws SQLException {
		return getWrappedClob().getCharacterStream();
	}

	/**
	 * @see java.sql.Clob#getAsciiStream()
	 */
	public InputStream getAsciiStream() throws SQLException {
		return getWrappedClob().getAsciiStream();
	}

	/**
	 * @see java.sql.Clob#position(java.lang.String, long)
	 */
	public long position(String searchstr, long start) throws SQLException {
		return getWrappedClob().position(searchstr, start);
	}

	/**
	 * @see java.sql.Clob#position(java.sql.Clob, long)
	 */
	public long position(Clob searchstr, long start) throws SQLException {
		return getWrappedClob().position(searchstr, start);
	}

	/**
	 * @see java.sql.Clob#setString(long, java.lang.String)
	 */
	public int setString(long pos, String str) throws SQLException {
		return getWrappedClob().setString(pos, str);
	}

	/**
	 * @see java.sql.Clob#setString(long, java.lang.String, int, int)
	 */
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		return getWrappedClob().setString(pos, str, offset, len);
	}

	/**
	 * @see java.sql.Clob#setAsciiStream(long)
	 */
	public OutputStream setAsciiStream(long pos) throws SQLException {
		return getWrappedClob().setAsciiStream(pos);
	}

	/**
	 * @see java.sql.Clob#setCharacterStream(long)
	 */
	public Writer setCharacterStream(long pos) throws SQLException {
		return getWrappedClob().setCharacterStream(pos);
	}

	/**
	 * @see java.sql.Clob#truncate(long)
	 */
	public void truncate(long len) throws SQLException {
		getWrappedClob().truncate(len);
	}
}