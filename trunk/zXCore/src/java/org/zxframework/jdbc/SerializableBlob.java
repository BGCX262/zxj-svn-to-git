//$Id: SerializableBlob.java,v 1.1.2.2 2006/07/17 16:18:27 mike Exp $
package org.zxframework.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author Gavin King
 */
public class SerializableBlob implements Serializable, Blob {
	
	private transient final Blob blob;
	
	/**
	 * @param blob The blob to wrap.
	 */
	public SerializableBlob(Blob blob) {
		this.blob = blob;
	}

	/**
	 * @return The wrapped blob.
	 */
	public Blob getWrappedBlob() {
		if ( blob==null ) {
			throw new IllegalStateException("Blobs may not be accessed after serialization");
		}
		
		return blob;
	}
	
	/**
	 * @see java.sql.Blob#length()
	 */
	public long length() throws SQLException {
		return getWrappedBlob().length();
	}

	/**
	 * @see java.sql.Blob#getBytes(long, int)
	 */
	public byte[] getBytes(long pos, int length) throws SQLException {
		return getWrappedBlob().getBytes(pos, length);
	}

	/**
	 * @see java.sql.Blob#getBinaryStream()
	 */
	public InputStream getBinaryStream() throws SQLException {
		return getWrappedBlob().getBinaryStream();
	}

	/**
	 * @see java.sql.Blob#position(byte[], long)
	 */
	public long position(byte[] pattern, long start) throws SQLException {
		return getWrappedBlob().position(pattern, start);
	}

	/**
	 * @see java.sql.Blob#position(java.sql.Blob, long)
	 */
	public long position(Blob pattern, long start) throws SQLException {
		return getWrappedBlob().position(pattern, start);
	}

	/**
	 * @see java.sql.Blob#setBytes(long, byte[])
	 */
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		return getWrappedBlob().setBytes(pos, bytes);
	}

	/**
	 * @see java.sql.Blob#setBytes(long, byte[], int, int)
	 */
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		return getWrappedBlob().setBytes(pos, bytes, offset, len);
	}

	/**
	 * @see java.sql.Blob#setBinaryStream(long)
	 */
	public OutputStream setBinaryStream(long pos) throws SQLException {
		return getWrappedBlob().setBinaryStream(pos);
	}

	/**
	 * @see java.sql.Blob#truncate(long)
	 */
	public void truncate(long len) throws SQLException {
		getWrappedBlob().truncate(len);
	}
}