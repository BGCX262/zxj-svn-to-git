//$Id: ReaderInputStream.java,v 1.1.2.1 2005/07/21 11:40:28 mike Exp $
package org.zxframework.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Exposes a <tt>Reader</tt> as an <tt>InputStream</tt>
 * @author Gavin King
 */
public class ReaderInputStream extends InputStream {
	
	private Reader reader;
	
	/**
	 * @param reader Reader to wrap.
	 */
	public ReaderInputStream(Reader reader) {
		this.reader = reader;
	}
	
	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return reader.read();
	}
}