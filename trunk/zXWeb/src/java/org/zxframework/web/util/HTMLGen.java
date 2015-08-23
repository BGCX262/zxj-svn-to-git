/*
 * Created on June 20, 2005
 * $Id: HTMLGen.java,v 1.1.2.2 2005/06/21 11:05:36 mike Exp $ 
 */
package org.zxframework.web.util;

import org.zxframework.util.StringUtil;

/**
 * Html Generator.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class HTMLGen {
	
	//------------------------ Members
	
    /** A new line character. **/
    public static final char NL = '\n';
    
    /** Represents a html space. **/
    protected static final char[] NBSP = "&nbsp;".toCharArray();
	StringBuffer buff;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor
	 */
	public HTMLGen() {
		buff = new StringBuffer(5000);
	}
	
	/**
	 * @param size
	 */
	public HTMLGen(int size) {
		buff = new StringBuffer(size);
	}
	
	//------------------------ Delegate methods
	
	/**
	 * @param obj Object to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(java.lang.Object)
	 */
	public synchronized HTMLGen append(Object obj) {
		buff.append(obj);
		return this;
	}

	/**
	 * @param str String to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public synchronized HTMLGen append(String str) {
		buff.append(str);
		return this;
	}

	/**
	 * @param sb StringBuffer to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(java.lang.StringBuffer)
	 */
	public synchronized HTMLGen append(StringBuffer sb) {
		buff.append(sb);
		return this;
	}

	/**
	 * @param b Boolean to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(boolean)
	 */
	public synchronized HTMLGen append(boolean b) {
		buff.append(b);
		return this;
	}

	/**
	 * @param c The char to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(char)
	 */
	public synchronized HTMLGen append(char c) {
		buff.append(c);
		return this;
	}

	/**
	 * @param str The char[] to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(char[])
	 */
	public synchronized HTMLGen append(char[] str) {
		buff.append(str);
		return this;
	}

	/**
	 * @param d The double to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(double)
	 */
	public synchronized HTMLGen append(double d) {
		buff.append(d);
		return this;
	}

	/**
	 * @param f The float to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(float)
	 */
	public synchronized HTMLGen append(float f) {
		buff.append(f);
		return this;
	}

	/**
	 * @param i The int to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(int)
	 */
	public synchronized HTMLGen append(int i) {
		buff.append(i);
		return this;
	}

	/**
	 * @param l The long to append
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.StringBuffer#append(long)
	 */
	public synchronized HTMLGen append(long l) {
		buff.append(l);
		return this;
	}
	
	/**
	 * @return Returns a handle to HTMLGen
	 * @see java.lang.Object#toString()
	 * @see java.lang.StringBuffer#toString()
	 */
	public String toString() {
		//System.out.println("HTML SIZE : "  + buff.toString().length());
		return buff.toString();
	}
	
	//------------------------ Helper methods
	
    /**
	 * Construct attribute that can be added to a tag.
	 * 
	 * @param pstrName The name of the attribute 
	 * @param pintValue The value of the attribute.
	 * @return Returns handle to HTMLGen.
	 */
	public HTMLGen appendAttr(String pstrName, int pintValue) {
		buff.append(pstrName).append("=\"").append(pintValue).append("\" ");
		return this;
	}

	/**
	 * Construct attribute that can be added to a tag.
	 *
	 *<pre>
	 *
	 * Example : 
	 * <input pstrName="pstrValue"/>
	 * 
	 * NOTE : Note removed tracing for performance.
	 *</pre>
	 *
	 * @param pstrName The name of the attribute 
	 * @param pstrValue  The value of the attribute.
	 * @return Returns The returns a XML/HTML attribute.
	 */
	public HTMLGen appendAttr(String pstrName, String pstrValue) {
		if (StringUtil.len(pstrValue) > 0) {
			/**
			 *  BD6NOV01 Less cautious in favour of small performance gain
			 *  I probably live to regret this one....
			 * 
			 * 'attr = pstrName & "=" & quote(pstrValue) & " "
			 */
			buff.append(pstrName).append("=\"").append(pstrValue).append("\" ");
		}

		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param obj The object to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(Object obj) {
		buff.append(obj).append(NL);
		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param str The string to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(String str) {
		buff.append(str).append(NL);
		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param sb The stringbuffer to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(StringBuffer sb) {
		buff.append(sb).append(NL);
		return this;
	}

	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param b The boolean to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(boolean b) {
		buff.append(b).append(NL);
		return this;
	}

	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param c The char to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(char c) {
		buff.append(c).append(NL);
		return this;
	}

	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param str The char[] to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(char[] str) {
		buff.append(str).append(NL);
		return this;
	}

	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param d The double to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(double d) {
		buff.append(d).append(NL);
		return this;
	}

	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param f The float to append.
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(float f) {
		buff.append(f).append(NL);
		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param i The int to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(int i) {
		buff.append(i).append(NL);
		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @param l The long to append
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL(long l) {
		buff.append(l).append(NL);
		return this;
	}
	
	/**
	 * Appends to HTML with a new line at the end.
	 * 
	 * @return Returns a handle to HTMLGen
	 */
	public synchronized HTMLGen appendNL() {
		buff.append(NL);
		return this;
	}
}