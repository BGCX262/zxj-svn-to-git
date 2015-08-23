/*
 * Created on Mar 4, 2004
 * $Id: StringUtil.java,v 1.1.2.17 2006/07/17 16:39:56 mike Exp $
 */
package org.zxframework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// JDK 1.4 specific.
import java.util.regex.Pattern;

// Used for Soundex Support.
import org.apache.commons.codec.language.DoubleMetaphone;

// Used for emulating enums in java.
import org.apache.commons.lang.enum.Enum;

/**
 * Is a String utility class, alot of these are to do with the diferences between java
 * and vb. <p/>
 * 
 * So this class will try to emulate VB functions like isNull and Len etc..
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class StringUtil {
    
    /** List of illegal class names. **/
    private static final String[] ILLEGAL_CLASSNAMES = { "true","int","false","char","boolean","if", "class", "null"};
    
    /** List of values that can be interpritted as the true boolean value. **/
    private static final String[] TRUE_VALUES = {"true","yes","t", "y","1","zyes","on","true()","-1"};
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    /** Handy final constant for dots. */
    public static final char DOT = '.';
    
    /**
     * Returns if the string is empty or null.
     * 
     * <pre>
     * 
     * NOTE : This is like the VB function <code>isNull()</code>
     * NOTE : Use StringUtil#len when you really need performance.
     * </pre> 
     * 
     * @param s The string you want to check.
     * @return Returns true if the string is empty or null 
     */
    public static boolean isEmpty(String s) {
        return(s!=null&&s.length()>0)?false:true;
    }
    
    /**
     * Returns if the string is empty or null even after trimming.
     * 
     * @param s The string you want to check.
     * @return Returns true if the string is empty or null 
     */
    public static boolean isEmptyTrim(String s) {
        return(s!=null&&s.trim().length()>0)?false:true;
    }    
    
    /**
     * Get the length of a String and return 0 if it is null.
     * 
     * <pre>
     * 
     * NOTE : This is like the VB function <code>Len()</code>.
     * </pre>
     * 
     * @param pstrValue The string to check the length of. If the string is null it will return 0
     * @return Returns the number of characters.
     */
    public static int len(String pstrValue) {
        if (pstrValue != null) {
            return pstrValue.length();
        }
        return 0;
    }

	/**
	 * Get the length of a StringBuffer and return 0 if it is null.
	 * 
	 * <pre>
	 * 
	 * NOTE : This is like the VB function <code>Len() </code> but for StringBuffers
	 * </pre>
	 * 
	 * @param pstrValue
	 *                 The StringBuffer to check the length of. If the StringBuffer is
	 *                 null it will return 0
	 * @return Returns the number of characters.
	 */
    public static int len(StringBuffer pstrValue) {
        if (pstrValue != null) {
            return pstrValue.length();
        }
        return 0;
    }    
    
	/**
	 * Get the length of a char[] and return 0 if it is null.
	 * 
	 * <pre>
	 * 
	 * NOTE : This is like the VB function <code>Len() </code> but for StringBuffers
	 * </pre>
	 * 
	 * @param parrValue
	 *                 The char[] to check the length of. If the char[] is
	 *                 null it will return 0
	 * @return Returns the number of characters.
	 */
    public static int len(char[] parrValue) {
        if (parrValue != null) {
            return parrValue.length;
        }
        return 0;
    }
    
    /**
     * Checks to see if a string contains any one of the String tokens if so it will return true.
     * 
     * <pre>
     * 
     * NOTE : This is case insensitve and not formant.
     * 
     * Usage : 
     * StringUtil.equalsAnyOf(new StringTokenizer("y,yes,t,true",","), "yes");
     * </pre>
     * 
     * @param pobjTokens The tokens you are looking
     * @param pstrValue The String to looking in 
     * @return Returns a true if there are at least one of the tokens in the string.
     */
    public static boolean equalsAnyOf(StringTokenizer pobjTokens, String pstrValue) {
        if (len(pstrValue) == 0) return false;
        while (pobjTokens.hasMoreTokens()) {
            if ( pstrValue.equals(pobjTokens.nextToken()) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks to see if a string equals any one of the String tokens if so it will return true.
     * 
     * <pre>
     * 
     * Example : if (containsAny(new String{"y","yes","1"'}, pstrValue)) return true;
     * </pre>
     * 
     * @param parrTokens An array of tokens to look for.
     * @param pstrValue The String to check.
     * @return Returns trues if it matchs on one of them.
     */
    public static boolean equalsAnyOf(String[] parrTokens, String pstrValue) {
        if (len(pstrValue) == 0) return false;
//        pstrValue = pstrValue.intern();
        for (int i = 0; i < parrTokens.length; i++) {
            if (parrTokens[i].equalsIgnoreCase(pstrValue)) {
//            if (parrTokens[i].intern() == pstrValue) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks to see if a string equals any one of the String tokens if so it will return true.
     * 
     * <pre>
     * 
     * Example : if (containsAny(new char[]{'y','1'}, pstrValue)) return true;
     * </pre>
     * 
     * @param parrTokens An array of tokens to look for.
     * @param pstrValue The String to check.
     * @return Returns trues if it matchs on one of them.
     */
    public static boolean equalsAnyOf(char[] parrTokens, char pstrValue) {
        for (int i = 0; i < parrTokens.length; i++) {
            if (parrTokens[i] == pstrValue) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks whether a string contains any of the set tokens.
     * 
     * <pre>
     * 
     * NOTE : This is was originally in zXInclude#zX.bas#containsAny
     * </pre>
     * 
     * @param pstrT Tokens to look for.
     * @param pstrV The value to check. If this is null or empty it will always return false.
     * @return Returns true if there is at least one hit.
     */
    public static boolean containsAny(String pstrT, String pstrV) {
    	
        int intV = len(pstrV);
        if (intV ==0) return false;
    	int intT = len(pstrT);
    	
    	if (intV < intT) {
			char[] t = pstrT.toCharArray();
			char[] v = pstrV.toCharArray();
			for (int i = 0; i < intV; i++) {
			    for (int j = 0; j < intT; j++) {
			        if ( v[i] == t[j]) {
			            return true;
			        }
			    }
			}
			//for (int i = 0; i < intV; i++) {
			//    for (int j = 0; j < intT; j++) {
			//        if ( pstrV.charAt(i) == pstrT.charAt(j)) return true;
			//    }
			//}
		} else {
			for (int i = 0; i < intT; i++) {
			    for (int j = 0; j < intV; j++) {
			        if ( pstrT.charAt(i) == pstrV.charAt(j)) return true;
			    }
			}
		}
        return false;
    }
    
    /**
     * Converts a Unicode character (char) into its position (int).
     * 
     * @param c The char to get the posiotion of
     * @return Returns the position of the char
     */
     private static int pos(char c) {
         return c;
     } 
     
     /**
      *Is a given character in ( [0-9] | [a-z] | [A-Z] )?
      *
      * @param c The char to check
      * @return Returns true if it is a alpha numeric char
      */
     public static  boolean isAlphaNumeric(char c) {
         int pos = pos(c);
         return (pos('0')<=pos && pos<=pos('9')) || (pos('a')<=pos && pos<=pos('z')) || (pos('A')<=pos && pos<=pos('Z'));
     }
     
     /**
      *Is a given character in ([a-z] | [A-Z] )?
      *
      * @param c Check to check.
      * @return Return whether the character is a letter
      */
     public static  boolean isAlpha(char c) {
         int pos = pos(c);
         return (pos('a')<=pos && pos<=pos('z')) || (pos('A')<=pos && pos<=pos('Z'));
     }     
     
     /**
      *Is a given character in ( [0-9] )?
      *
      * @param c Char to check
      * @return Returns whether the character is numberic
      */
     public static boolean isNumeric(char c) {
         int pos = pos(c);
         return (pos('0')<=pos && pos<=pos('9'));
     }    
     
     /**
      * This returns a Soundex function.
      * <pre>
      * This relies on the Apache Commons Codec library.
      * </pre>
      * 
     * @param pstrValue The string to soundex.
     * @return Returns the soundex of the string
     */
    public static String soundex(String pstrValue) {
        return new DoubleMetaphone().encode(pstrValue);
        //return new Soundex().encode(pstrValue);
     }

    /**
     * Convert the string to a classname acceptable in java.
     * 
     * @param func The DirectorFunction or Expression Function.
     * @return Returns a safe java classname
     */
    public static String makeClassName(String func){
        
        if (len(func) == 0) return func;
        
        // Strip up any director or expression stuff
        if (func.charAt(0) == '#') {
            func = func.substring(1);
        }
        if (equalsAnyOf(ILLEGAL_CLASSNAMES, func)) {
            // Make safe ie : True/Int/False/Char/Boolean etc..
            func = capitalize(func);
        } else {
            func = toLowerCase(func);
        }
        
        return func;
    }    
    
    /**
     * Trys to get a boolean value from a string, if it can't is will default to false.
     * 
     * @param pstrValue The string that you want the boolean of.
     * @return Returns the boolean of a String. Default is false.
     */
    public static boolean booleanValue(String pstrValue) {
        if ( len(pstrValue) == 0 || !equalsAnyOf(TRUE_VALUES, pstrValue) ) {
            return false;
        }
        
        return true;     
    }
    
    /**
     * Left trim.
     * 
     * @param pstrValue The string to left trim
     * @return Returns a left trimmed string
     */
    public static String ltrim(String pstrValue) {
        if (StringUtil.len(pstrValue) > 0) {
            if (pstrValue.charAt(0) == ' ') {
                if (StringUtil.len(pstrValue.trim()) == 0) {
                    return "";
                }
                
                int pos = 1;
                while (pstrValue.charAt(pos) == ' ') {
                    pos++;
                }
                
                return pstrValue.substring(pos);
            }
            return pstrValue;
        }
        return pstrValue;
        
    }
    
    /**
     * Right trim.
     * 
     * @param pstrValue The value to right trim
     * @return Returns a right trimmed string
     */
    public static String rtrim(String pstrValue) {
        if (StringUtil.len(pstrValue) > 0) {
            if (pstrValue.charAt(pstrValue.length() -1) == ' ') {
            	
                if (StringUtil.len(pstrValue.trim()) == 0) {
                    return "";
                }
                
                int pos = pstrValue.length() - 1;
                while (pstrValue.charAt(pos) == ' ') {
                    pos--;
                }
                
                return pstrValue.substring(0, pos + 1);
                
            } // Is there something to trim.
            
            return pstrValue;
        }
        
        return pstrValue;
    }
    
    /**
     * Creates a padded string.
     * 
     * @param pintNum Number of times to pad out. If pintNum is 0 a empty string will be returned.
     * @param pstrValue The string to padd it out with.
     * @return Returns a padded string from pstrValue.
     */
    public static String padString(int pintNum, String pstrValue) {
        if (pintNum == 1)  {
            return pstrValue;
        } else if (pintNum == 0) {
            return "";
        }
        int length = len(pstrValue);
        if (length == 0)   {
            return "";
        } else if (length == 1){
            return padString(pintNum, pstrValue.charAt(0));
        }
        
        int lStr = pstrValue.length();
        char padString[] = new char[pintNum * lStr];
        int t = 0;
        for (int i = 0; i < pintNum; i++) {
            for (int j = 0; j < lStr; j++) {
                padString[t] = pstrValue.charAt(j);
                t++;
            }	
        }
        return new String(padString);
    }
    
    /**
     * Creates a padded string.
     * 
     * @param pintNum The number of times to pad the string. If pintNum is 0 will return a empty string.
     * @param c The char to use to pad the string out with.
     * @return Returns a padded string.
     */
    public static String padString(int pintNum, char c) {
        if (pintNum == 0) return "";
        char padString[] = new char[pintNum];
        for (int i = 0; i < pintNum; i++) {
            padString[i] = c;
        }
        return new String(padString);
    }
    
    /**
     * Pads out a string to the required length, this is padded from the right end of the string.
     * 
     * @param str The string to pad.
     * @param c The element to pad the string with
     * @param i The size of the end result.
     * @return Returns a right padded string.
     */
    public static String right(String str, char c, int i) {
        return padString(str, c, i, false);
    }
    
    /**
     * Pads out a string to the required length, this is padded from the left end of the string.
     * 
     * @param str The string to pad.
     * @param c The element to pad the string with
     * @param i The size of the end result.
     * @return Returns a left padded string.
     */
    public static String left(String str, char c, int i) {
        return padString(str, c, i, true);
    }
    
    /**
     * padString - Pads out a string to the required length, this can be padded
     * from the beginning or the end.
     * 
     * @param str String that you want to pad out.
     * @param padding The padding character
     * @param length The number of paddings
     * @param isRight Whether to start padding from the begining of the string of
     *            				the end. true is like vb right() and false like vb left()
     * @return Returns a padded out string.
     */
    public static String padString(String str, char padding, int length, boolean isRight) {
        char result[] = new char[length];
        int strLength =  len(str);
        if (strLength > length) {
            if (!isRight) {
                str = str.substring(strLength - length);
            } else {
                str = str.substring(0, length);
            }
            return str;
        }
        
        int i = 0;
        if (isRight) {
            for (; i < strLength; i++) {
                result[i] = str.charAt(i);
            }
        } else {
            // We will be adding "str" to the end
            length = length - strLength;
        }
        
        for (; i < length; i++) {
            result[i] = padding;
        }
        
        if (!isRight) {
            int k =0;
            for (; i < length + strLength; i++) {
                result[i] = str.charAt(k);
                k++;
            }
        }
        return new String(result);
    }
    
    /**
     * Converts is char parameter to a lowercase letter.
     * 
     * @param c The char to be converted
     * @return Returns a lowercase char
    */
    public static char toLowerCase(char c) {
        if ((c >= 'A') && (c <= 'Z')) return (char)(c + 32);
        return c;
    }
    
    /**
     * Converts is char parameter to an uppercase letter.
     * 
     * @param c The char to be converted
     * @return Returns an uppercase char
     */
    public static char toUpperCase(char c) {
        if ( (c >= 'a') && (c <= 'z') ) return (char)(c - 32);
        return c;
    }
    
    /**
     * Capitalizes the first letter in the string s
     * 
     * @param str the string to be capitalized
     * @return Returns a string with its first letter capitalized
     */
    public static String capitalize(String str) {
        int length = len(str);
        if (length == 0) return str;
        
        char[] result = new char[length];
		result[0] = toUpperCase(str.charAt(0));
		for (int k = 1; k < length; k++) { 
		    result[k] = toLowerCase(str.charAt(k));
		}
		return new String(result);
    }
    
    /**
     * Converts its parameter to uppercase
     * @param str The string to be converted to uppercase
     * @return Returns a string giving the uppercase of s
     */
    public static String toUpperCase(String str) {
        int length = len(str);
        if (length == 0) return str;
		char[] result = new char[length];
		for (int k = 0; k < length; k++) { 
			result[k] = toUpperCase(str.charAt(k));
		}
		return new String(result);
    }
    
    /**
     * Converts its parameter to uppercase
     * @param str The string to be converted to uppercase
     * @return Returns a string giving the uppercase of s
     */
    public static String toLowerCase(String str) {
        int length = len(str);
        if (length == 0) return str;
		char[] result = new char[length];
		for (int k = 0; k < length; k++) { 
			result[k] = toLowerCase(str.charAt(k));
		}
		return new String(result);
    }
    
    /**
     * @param pchrStrip The char you want to strip
     * @param pstrWhat The string you want to clean up.
     * @return Returns a strip string.
     */
    public static String stripChars(char pchrStrip, String pstrWhat) {
        int intWhat = len(pstrWhat);
        StringBuffer stripChars = new StringBuffer(intWhat);
        char c;
        for (int i = 0; i < intWhat; i++) {
            c = pstrWhat.charAt(i);
            if (c != pchrStrip) {
                stripChars.append(c);
            }
        }
        return stripChars.toString();
    }
    
    /**
     * @param pstrStrip The chars you want to strip eg : "_.%$" etc..
     * @param pstrWhat The string you want to clean up.
     * @return Returns a strip string.
     */
    public static String stripChars(String pstrStrip, String pstrWhat) {
        StringBuffer stripChars = new StringBuffer();
        char c;
        int intStrip = len(pstrStrip);
        int intWhat = len(pstrWhat);
        for (int i = 0; i < intWhat; i++) {
            c = pstrWhat.charAt(i);
            boolean doesMatch = false;
            for (int j = 0; j < intStrip; j++) {
                if (c == pstrStrip.charAt(j)) {
                    doesMatch = true;
                    break;
                }
            }
            if (!doesMatch) {
                stripChars.append(c);
            }
        }
        return stripChars.toString();
    }
    
    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *)         = null
     * StringUtils.replace("", *, *)           = ""
     * StringUtils.replace("abaa", null, null) = "abaa"
     * StringUtils.replace("abaa", null, null) = "abaa"
     * StringUtils.replace("abaa", "a", null)  = "abaa"
     * StringUtils.replace("abaa", "a", "")    = "b"
     * StringUtils.replace("abaa", "a", "z")   = "zbzz"
     * </pre>
     * 
     * @param pstrText  text to search and replace in, may be null
     * @param pstrRepl  the String to search for, may be null
     * @param pstrWith  the String to replace with, may be null
     * @return Returns the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replaceAll(String pstrText, String pstrRepl, String pstrWith) {
        if (len(pstrText) == 0 || len(pstrRepl) == 0 || pstrWith == null) {
            return pstrText;
        }
        
        int start = 0;
        int end = 0;
        
        StringBuffer buf = new StringBuffer(len(pstrText));
        while ((end = pstrText.indexOf(pstrRepl, start)) != -1) {
            buf.append(pstrText.substring(start, end)).append(pstrWith);
            start = end + pstrRepl.length();
        }
        
        buf.append(pstrText.substring(start));
        return buf.toString();
    }
    
    /**
     * @see StringUtil#replaceAll(String, String, String)
     * @param pstrText  text to search and replace in, may be null
     * @param pchrRepl The char to search for.
     * @param pstrWith  the String to replace with, may be null
     * @return Returns the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replaceAll(String pstrText, char pchrRepl, String pstrWith) {
        if (len(pstrText) == 0 || pstrWith == null) {
            return pstrText;
        }
        
        char c;
        StringBuffer buf = new StringBuffer(len(pstrText));
        char[] arrText = pstrText.toCharArray();
        for (int i = 0; i < arrText.length; i++) {
            c = arrText[i];
            if (c == pchrRepl) {
                buf.append(pstrWith);
            } else {
                buf.append(c);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * @see StringUtil#replaceAll(String, String, String)
     * @param pstrText  text to search and replace in, may be null
     * @param pstrRepl  the String to search for, may be null
     * @param pcharWith  the String to replace with, may be null
     * @return Returns the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replaceAll(String pstrText, char pstrRepl, char pcharWith) {
        if (len(pstrText) == 0) {
            return pstrText;
        }
        StringBuffer buf = new StringBuffer(len(pstrText));
        char c;
        char[] arrText = pstrText.toCharArray();
        for (int i = 0; i < arrText.length; i++) {
            c = arrText[i];
            
            if (c == pstrRepl) {
                buf.append(pcharWith);
            } else {
                buf.append(c);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * @param pstrText  text to search and replace in, may be null
     * @param pstrRepl  the String to search for and remove
     * @return Returns the text with any replacements processed, <code>null</code> if null String input
     */
    public static String removeAll(String pstrText, char pstrRepl) {
        if (len(pstrText) == 0) {
            return pstrText;
        }
        
        StringBuffer buf = new StringBuffer();
        char c;
        char[] arrText = pstrText.toCharArray();
        for (int i = 0; i < arrText.length; i++) {
            c = arrText[i];
            if (c != pstrRepl) {
                buf.append(c);
            }
        }
        
        return buf.toString();
    }


    /**
     * <p>Checks if the String contains only unicode digits.
     * A decimal point is not a unicode digit and returns false.</p>
     *
     * <p><code>null</code> will return <code>false</code>.
     * An empty String ("") will return <code>true</code>.</p>
     * 
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = true
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("-123")  = true
     * StringUtils.isNumeric("-1-23")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if only contains digits, and is non-null
     */
    public static boolean isNumeric(String str) {
        int sz = len(str);
        if (sz == 0) return false;
        
        /**
         * Allow for -100 but not 100-100
         */
        if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != '-') {
            return false;
        }
        
        /**
         * Skip the first char
         */
        for (int i = 1; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if the value can safely be converted to a double primitive.
     *
     * @param str The value validation is being performed on.
     * @return Returns true if it is a valid double.
     */
    public static boolean isDouble(String str) {
        if (len(str) == 0) return false;
        try {
            Double.valueOf(str);
            return true;
        } catch(Exception e) {
            return false; 
        }
    }
    
    /**
     * @param str The string to do the search on.
     * @param c The char to look for
     * @return Returns if there is at least one instace of c
     */
    public static boolean inStr(String str, char c) {
        boolean inStr = false;
        int length = len(str);
        if (length >0) {
            for (int i = 0; i < length; i++) {
                if (c == str.charAt(i)) {
                    return true;
                }
            }
        }
        return inStr;
    }
    
    /**
     * Translate string into checksum that is shorter 
     * in length. 
     * 
     * <pre>
     * 
     * This is used to handle generated 
     *  table / columnnames that are too long
     *   for the database to handle (e.g. Oracle will
     *  only handle ids up to 20 positions)
     *  </pre> 
     * 
     * @param pstrStr String to generate checksum for
     * @return Returns a checkSum of pstrStr
     */
    public static String checkSum(String pstrStr) {
        if (pstrStr == null) return pstrStr;
        
    	String checkSum = null;
        int lngCheckSum = 0;
        char[] arrStr = pstrStr.toCharArray();
        char c;
        for (int i = 0; i < arrStr.length; i++) {
            c = arrStr[i];
            lngCheckSum = lngCheckSum +pos(toUpperCase(c)) + 1;
        }
        checkSum = right(String.valueOf(lngCheckSum), '#', 5);
        return checkSum;
    }
    
    /**
     * @param bln The boolean you want to get a string versoin of.
     * @return Returns the string form of a boolean
     */
    public static String valueOf(boolean bln) {
        return bln?TRUE:FALSE;
    }
    
    /**
     * <p>Reverses a String as per {@link StringBuffer#reverse()}.</p>
     *
     * <p><A code>null</code> String returns <code>null</code>.</p>
     * 
     * <pre>
     * StringUtils.reverse(null)  = null
     * StringUtils.reverse("")    = ""
     * StringUtils.reverse("bat") = "tab"
     * </pre>
     * 
     * @param str  the String to reverse, may be null
     * @return the reversed String, <code>null</code> if null String input
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuffer(str).reverse().toString();
    }
    
    /**
     * Get a line from an optional multi-line string. System will work out the line
     * separator (\n or \r). Negative line numbers indicate getting from end.
     * 
     * @param pstr The string to get line from.
     * @param pintLineNo The line number ot get (1 based, negative means from end)
     * @return Return the string in line number x from start (lineNo > 0) or from end (lineNo < 0)
     */
    public static String getLine(String pstr, int pintLineNo) {
        
        if (len(pstr) ==0)  return "";
        
        /**
         * Line 0 is always the full string, no line extraction
         */
        if (pintLineNo == 0) return pstr;
        
        /**
         * Replace all instances of CrLf with Lf to cover both CrLf and Lf
         */
        pstr = pstr.replace('\r','\n');
        
        String[] arrStr = split("\n", pstr);
        int intLines = arrStr.length;
        
        pintLineNo = pintLineNo<0?intLines+(pintLineNo +1):pintLineNo;
        
        /**
         * return empty string if out of bounds
         */
        if (pintLineNo > intLines || pintLineNo < 0) {
            return "";
        }
        
        return arrStr[pintLineNo-1];
    }
    /**
     * Get the nth word number from start (wordNo > 0) or from end (wordNo < 0).
     * 
     * @param pstr The string to get nth word from from
     * @param pintWordNo The word to get (1 based, negative means from end).
     * @return nth word number from start (wordNo > 0) or from end (wordNo < 0)
     */
    public static String getWord(String pstr, int pintWordNo) {
        return getWord(pstr,pintWordNo, " ");
    }
    
    /**
     * Get the nth word number from start (wordNo > 0) or from end (wordNo < 0).
     * 
     * @param pstr The string to get nth word from from
     * @param pintWordNo The word to get (1 based, negative means from end).
     * @param pstrSep string of separating characters. Optional, default is " ".
     * @return nth word number from start (wordNo > 0) or from end (wordNo < 0)
     */
    public static String getWord(String pstr, int pintWordNo, String pstrSep) {
        /** Handle nulls **/
        if (len(pstr) == 0) return "";
        if (pstrSep == null) pstrSep = " ";
        
        try {
            String[] arrStr = Pattern.compile(pstrSep, Pattern.CASE_INSENSITIVE).split(pstr);
            if (arrStr != null) {
                /** Calculate the pos in the array to select. **/
                int intStr = arrStr.length;
                if (pintWordNo < 0) {
                    pintWordNo = intStr + pintWordNo;
                } else {
                    pintWordNo--;
                }
                
                /** Position should be within  range of the array. **/
                if (pintWordNo > -1 && pintWordNo < intStr) {
                    return arrStr[pintWordNo];
                }
            }
        } catch (Exception e) { 
        	throw new RuntimeException(e);
        }
        
        return "";
    }
    
    /**
     * @param pstr The string to summary neatly.
     * @param sep The sepator.
     * @param max The maximum size of the string.
     * @return Returns a summary of a string.
     */
    public static String summarise(String pstr, String sep, int max) {
        int len = len(pstr);
        if (len == 0) return pstr;
        if (len < max) return pstr;
        if (len > max) pstr = pstr.substring(0, max); // Trim to size.
        
        String summarise;
        int indexOf = pstr.indexOf(sep);
        if (indexOf != -1) {
            pstr = reverse(pstr);
            indexOf = pstr.indexOf(sep);
            pstr = pstr.substring(indexOf+sep.length());
            summarise = reverse(pstr);
        } else {
            if (len > 3) {
                pstr = pstr.substring(0, len-3) + "...";   
            }
            summarise = pstr;
        }
        return summarise;
    }
    
    /**
     * @param pstr The string you want to summarise in to bits.
     * @param sep The seperate to used.
     * @param max The maximum size of each bit.
     * @return Returns an array of a summaries strings.
     */
    public static String[] summarize(String pstr, String sep, int max) {
       int len = len(pstr);
       if (len == 0) return new String[0];
       else if (len < max) return new String[]{pstr};
       
       List summarise = new ArrayList();
       int pos = 0;
       String strTemp =pstr.substring(pos, max);
       while (pos < len) {
           strTemp = summarise(strTemp, sep, max);
           pos = pos + strTemp.length() + sep.length(); // next pos
           summarise.add(strTemp);
           
           if (max + pos > len) {
               strTemp = pstr.substring(pos);
               summarise.add(strTemp);
               pos = len;
           } else {
               strTemp = pstr.substring(pos, pos + max);
           }
       }
       return (String[]) summarise.toArray(new String[summarise.size()]);
    }
    
    /** 
     * We only want to see the class name, without the package. 
     * @param c The class you want to class name for.
     * @return Returns a class name without the package
     */
    public static String stripPackageName(Class c) {
      return c.getName().replaceAll(".*\\.", "").replace('$', '.');
    }
    
    /**
     * Get the ascii value of a char.
     * 
     * @param c The char to get the ascii value of.
     * @return Returns the ascii value of a char
     */
    public static int ascii(char c) {
        return c;
    }
    
    /**
     * Get the ascii value of a string.
     * 
     * @param s The string to get the ascii value of.
     * @return Returns the ascii value of a string
     */
    public static int ascii(String s) {
        if (len(s) == 0) return 0;
        int ascii = 0;
        char c[] = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            ascii = ascii + c[i];
        }
        return ascii; 
    }
    
    /**
     * See if the string looks like a subsession variable.
     * 
     * <pre>
     * 
     * This looks like a string with 9 numbers in a row.
     * NOTE: In future just use a regular expression.
     * </pre>
     * 
     * @param pstr The string to Check.
     * @return Return true if it looks like a SubSession.
     */
    public static boolean isSubSessionData(String pstr) {
        int len = len(pstr);
        if (len < 9) return false;
        int j=0;
        char[] arrStr = pstr.toCharArray();
        for (int i = 0; i < arrStr.length; i++) {
            if (Character.isDigit(arrStr[i])) {
                j++;
                if (j ==9) return true;
                
            } else {
                j = 0;
            }
        }
        return false;
    }
    
    /**
     * Replace special characters in typical zX strings
     * @param str String to encode
     * @return Returns the encoded string
     */
    public static String encodezXString(String str) {
        if (len(str) == 0) return str;
        return replaceAll(str, '#', "\\#");
    }
    
    /**
     * Split a string base on the specified seperator.
     * 
     * NOTE : This is similar to String.split however it does not support Regular expressions etc..
     * 
     * @param seperators The seperator to look for.
     * @param list The string to build the array from.
     * @return Returns an array of Strings from a string based on a seperator without the seperator.
     */
    public static String[] split(String seperators, String list) {
        return split(seperators, list, false);
    }
    
    /**
     * Split a string base on the specified seperator.
     * 
     * @param seperators The seperator to look for.
     * @param list The string to build from.
     * @param include Whether to include the seperator.
     * @return Returns an array of Strings from a string based on a seperator.
     */
    public static String[] split(String seperators, String list, boolean include) {
        StringTokenizer tokens = new StringTokenizer(list, seperators, include);
        
        String[] result = new String[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            result[i++] = tokens.nextToken();
        }
        
        return result;
    }
    
    /**
     * @param prefix  The prefix
     * @param name The name.
     * @return Returns "prefix.name" format.
     */
    public static String qualify(String prefix, String name) {
        if ( name == null || prefix == null ) {
            throw new NullPointerException();
        }
        
        return new StringBuffer(prefix.length() + name.length() + 1)
               .append(prefix)
               .append('.')
               .append(name)
               .toString();
    }
    
	//------------------------ Constants
    
	private static final String OFFSETS = ")IJKLMNO(&*$AFGHP678QRS234BE59TUVCDWXYZ01";
	
    /**
     * @param pstrPassWord
     * @return Returns a hashed string
     */
    public static String hash(String pstrPassWord) {
    	if (pstrPassWord == null) return "";
    	
    	int dbl = 0;
    	char arrChars[] = pstrPassWord.toUpperCase().toCharArray();
    	
    	for (int j = 0; j < arrChars.length; j++) {
    		// dbl + InStr(OFFSETS, arrChars[j]) ^ j 
    		dbl = dbl + (OFFSETS.indexOf(arrChars[j]) ^ j);
		}
    	
    	// format(dbl, "000000000000000")
        return StringUtil.padString(dbl + "", '0', 15, true);
    }
    
}