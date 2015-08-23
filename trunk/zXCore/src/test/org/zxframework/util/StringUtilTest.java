/*
 * Created on Aug 30, 2004
 * $Id: StringUtilTest.java,v 1.1.2.9 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.util.StringUtil} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class StringUtilTest extends TestCase {

    /**
     * @param name The name of the test
     */
    public StringUtilTest(String name) {
        super(name);
    }

    /**
     * @param args Program parameters.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(StringUtilTest.class);
        suite.setName("StringUtil Tests");
        return suite;
    }

    //----------------------------------------------- Construstor testing
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new StringUtil());
        Constructor[] cons = StringUtil.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(StringUtil.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringUtil.class.getModifiers()));
    }
    
    /**
     * Test the isNull method
     */
    public void testIsNull() {
        assertEquals(true, StringUtil.isEmpty(null));
        assertEquals(true, StringUtil.isEmpty(""));
        assertEquals(false, StringUtil.isEmpty(" "));
    }
    
    /**
     * Test the isEmptyTrim method
     */
    public void testIsNullTrim() {
        assertEquals(true, StringUtil.isEmptyTrim(null));
        assertEquals(true, StringUtil.isEmptyTrim(""));
        assertEquals(true, StringUtil.isEmptyTrim(" "));
        assertEquals(true, StringUtil.isEmptyTrim("   "));
        assertEquals(false, StringUtil.isEmptyTrim("  d "));
    }
    
    /**
     * Test the len method
     */
    public void testLen() {
        
        String len = null;
        assertEquals(0, StringUtil.len(len));
        assertEquals(0, StringUtil.len(""));
        assertEquals(1, StringUtil.len(" "));
        assertEquals(4, StringUtil.len("test"));
        StringBuffer len2 = null;
        assertEquals(0, StringUtil.len(len2));
        assertEquals(4, StringUtil.len(new StringBuffer("test")));
        char[] len3 = null;
        assertEquals(0, StringUtil.len(len3));
        len3 = new char[4]; 
        assertEquals(4, StringUtil.len(len3));
        
    }
    
    /**
     * Test the equalsAnyOf method
     */
    public void testEqualsAnyOf() {
        
        assertEquals(false, StringUtil.equalsAnyOf(new StringTokenizer("y,yes,t,true",","), ""));
        assertEquals(false, StringUtil.equalsAnyOf(new StringTokenizer("y,yes,t,true",","), null));
        assertEquals(true, StringUtil.equalsAnyOf(new StringTokenizer("y,yes,t,true",","), "yes"));
        assertEquals(false, StringUtil.equalsAnyOf(new String []{"y","yes","t","true"} , ""));
        assertEquals(false, StringUtil.equalsAnyOf(new String []{"y","yes","t","true"} , null));
        assertEquals(true, StringUtil.equalsAnyOf(new String []{"y","yes","t","true"} , "yes"));
        
    }
    
    /**
     * Test the containsAny method
     */
    public void testContainsAny() {
        
        assertEquals(false, StringUtil.containsAny(null, null));
        assertEquals(true, StringUtil.containsAny(" ", " "));
        assertEquals(true, StringUtil.containsAny("abs", "a"));
        assertEquals(false, StringUtil.containsAny("abs", "z"));
        assertEquals(false, StringUtil.containsAny("abs", "zqwr"));
        assertEquals(true, StringUtil.containsAny("ab£s", "zq£wr"));
        
    }
    
    /**
     * Tests the isAlphaNumeric method
     */
    public void testIsAlphaNumeric() {
        
        assertEquals(true, StringUtil.isAlphaNumeric('a'));
        assertEquals(true, StringUtil.isAlphaNumeric('A'));
        assertEquals(true, StringUtil.isAlphaNumeric('1'));
        assertEquals(false, StringUtil.isAlphaNumeric(' '));
        assertEquals(false, StringUtil.isAlphaNumeric('£'));
        
    }
    
    /**
     * Tests the isAlpha method
     */
    public void testIsAlpha() {
        
        assertEquals(true, StringUtil.isAlpha('a'));
        assertEquals(true, StringUtil.isAlpha('A'));
        assertEquals(false, StringUtil.isAlpha('1'));
        assertEquals(false, StringUtil.isAlpha(' '));
        assertEquals(false, StringUtil.isAlpha('£'));
        
    }
    
    /**
     * Tests the isDouble method
     */
    public void testIsDouble() {
        
        assertEquals(false, StringUtil.isDouble(null));
        assertEquals(false, StringUtil.isDouble(""));
        assertEquals(false, StringUtil.isDouble("0.1ff"));
        assertEquals(true, StringUtil.isDouble("11"));
        assertEquals(true, StringUtil.isDouble("0.1"));
        assertEquals(true, StringUtil.isDouble("0.1f"));
        
    }
    
    /**
     * Tests the isNumeric method
     */
    public void testIsNumeric() {
        
        assertEquals(false, StringUtil.isNumeric(null));
        assertEquals(false, StringUtil.isNumeric(""));
        assertEquals(false, StringUtil.isNumeric(' '));
        assertEquals(false, StringUtil.isNumeric("0.1ff"));
        assertEquals(false, StringUtil.isNumeric("0.1"));
        assertEquals(false, StringUtil.isNumeric("0.1f"));
        assertEquals(false, StringUtil.isNumeric("f"));
        assertEquals(true, StringUtil.isNumeric("11"));
        assertEquals(true, StringUtil.isNumeric("-11"));
        assertEquals(false, StringUtil.isNumeric("1-1"));
        assertEquals(true, StringUtil.isNumeric('2'));
        
    }
    
    /**
     * Test the soundex method.
     */
    public void testSoundex() {
        
        assertEquals(null, StringUtil.soundex(null));
        assertEquals(null, StringUtil.soundex(""));
        assertEquals(null, StringUtil.soundex(" "));
        assertEquals("FN", StringUtil.soundex("Phone"));
        assertEquals("FN", StringUtil.soundex("Fone"));
        assertEquals(StringUtil.soundex("color"), StringUtil.soundex("colour"));
        assertEquals(StringUtil.soundex("Van"), StringUtil.soundex("Fan"));
        
    }
    
    /**
     * Test the makeClassName method.
     */
    public void testMakeClassName() {
        
        assertEquals(null, StringUtil.makeClassName(null));
        assertEquals("", StringUtil.makeClassName(""));
        assertEquals("Class", StringUtil.makeClassName("class"));
        assertEquals("False", StringUtil.makeClassName("false"));
        assertEquals("True", StringUtil.makeClassName("true"));
        assertEquals("test", StringUtil.makeClassName("test"));
        
    }
    
    /**
     * Test the booleanValue method.
     */
    public void testBooleanValue() {
        
        assertEquals(false, StringUtil.booleanValue(null));
        assertEquals(false, StringUtil.booleanValue(""));
        assertEquals(true, StringUtil.booleanValue("True"));
        assertEquals(true, StringUtil.booleanValue("yes"));
        assertEquals(true, StringUtil.booleanValue("zyes"));
        assertEquals(true, StringUtil.booleanValue("1"));
        assertEquals(false, StringUtil.booleanValue("false"));
        assertEquals(false, StringUtil.booleanValue("no"));
        
    }
    
    /**
     * Testing the ltrim method.
     */
    public void testLTrim() {
        
        assertEquals(null, StringUtil.ltrim(null));
        assertEquals("", StringUtil.ltrim(""));
        assertEquals("", StringUtil.ltrim(" "));
        assertEquals("test", StringUtil.ltrim(" test"));
        assertEquals("test ", StringUtil.ltrim("test "));
        assertEquals("test ", StringUtil.ltrim(" test "));
        
    }
    
    /**
     * Testing the rtrim method.
     */
    public void testRTrim() {
        
        assertEquals(null, StringUtil.rtrim(null));
        assertEquals("", StringUtil.rtrim(""));
        assertEquals("", StringUtil.rtrim(" "));
        assertEquals(" test", StringUtil.rtrim(" test"));
        assertEquals("test", StringUtil.rtrim("test "));
        assertEquals(" test", StringUtil.rtrim(" test "));
        
    }
    
    /**
     * Testing the padString method.
     */
    public void testPadString() {
        
        assertEquals("", StringUtil.padString(0, null));
        assertEquals("", StringUtil.padString(2, ""));
        assertEquals("", StringUtil.padString(0, "t"));
        assertEquals("t", StringUtil.padString(1, "t"));
        assertEquals("ttt", StringUtil.padString(3, "t"));
        assertEquals("abab", StringUtil.padString(2, "ab"));
        assertEquals("a ba b", StringUtil.padString(2, "a b"));
        assertEquals("  ", StringUtil.padString(2, ' '));
        assertEquals("ss", StringUtil.padString(2, 's'));
        assertEquals("s", StringUtil.padString(1, 's'));
        assertEquals("", StringUtil.padString(0, 's'));
        
        assertEquals("00000", StringUtil.padString(0 + "", '0', 5, false));
        assertEquals("00001", StringUtil.padString(1 + "", '0', 5, false));
        assertEquals("00123", StringUtil.padString(123 + "", '0', 5, false));
        
    }
    
    /**
     * Testing the right method.
     */
    public void testLeft() {
        
        assertEquals("", StringUtil.left(null, ' ', 0));
        assertEquals("", StringUtil.left("", ' ', 0));
        assertEquals("", StringUtil.left("test", ' ', 0));
        assertEquals(" ", StringUtil.left(null, ' ', 1));
        assertEquals("tes", StringUtil.left("test", 's', 3));
        assertEquals("testss", StringUtil.left("test", 's', 6));
        
    }
    
    /**
     * Testing the right method.
     */
    public void testRight() {
        
        assertEquals("", StringUtil.right(null, ' ', 0));
        assertEquals("", StringUtil.right("", ' ', 0));
        assertEquals("", StringUtil.right("test", ' ', 0));
        assertEquals(" ", StringUtil.right(null, ' ', 1));
        assertEquals("est", StringUtil.right("test", 's', 3));
        assertEquals("sstest", StringUtil.right("test", 's', 6));
        assertEquals("00123", StringUtil.right(123 + "", '0', 5));
        
    }
    
    /**
     * Testing the toLowerCase method.
     */
    public void testToLowerCase() {
        assertEquals(' ', StringUtil.toLowerCase(' '));
        assertEquals('a', StringUtil.toLowerCase('A'));
        assertEquals('z', StringUtil.toLowerCase('z'));
        assertEquals(null, StringUtil.toLowerCase(null));
        assertEquals("", StringUtil.toLowerCase(""));
        assertEquals(" ", StringUtil.toLowerCase(" "));
        assertEquals("a", StringUtil.toLowerCase("A"));
        assertEquals("aza", StringUtil.toLowerCase("AzA"));
        assertEquals("z", StringUtil.toLowerCase("z"));
    }
    
    /**
     * Testing the toUpperCase method.
     */
    public void testToUpperCase() {
        assertEquals(' ', StringUtil.toUpperCase(' '));
        assertEquals('A', StringUtil.toUpperCase('A'));
        assertEquals('Z', StringUtil.toUpperCase('z'));
        assertEquals(null, StringUtil.toUpperCase(null));
        assertEquals("", StringUtil.toUpperCase(""));
        assertEquals(" ", StringUtil.toUpperCase(" "));
        assertEquals("A", StringUtil.toUpperCase("A"));
        assertEquals("AZA", StringUtil.toUpperCase("azA"));
        assertEquals("Z", StringUtil.toUpperCase("z"));
    }
    
    /**
     * Test the stripChars method.
     */
    public void testStripChars() {
        assertEquals("", StringUtil.stripChars(' ', null));
        assertEquals("d", StringUtil.stripChars(' ', "d"));
        assertEquals("daz", StringUtil.stripChars(' ', " d a z "));
        assertEquals("dzd", StringUtil.stripChars('a', "adzda"));
        assertEquals("", StringUtil.stripChars(" ", null));
        assertEquals("", StringUtil.stripChars(" ", ""));
        assertEquals("", StringUtil.stripChars("", ""));
        assertEquals("d", StringUtil.stripChars(" ", "d"));
        assertEquals("daz", StringUtil.stripChars(" ", " d a z "));
        assertEquals("dzd", StringUtil.stripChars("a", "adzda"));
        assertEquals("ZdA", StringUtil.stripChars("az", "aZzdAa"));
    }
    
    /**
     * Test replaceAll methods.
     */
    public void testReplaceAll() {
        assertEquals(null, StringUtil.replaceAll(null, null, null));
        assertEquals("", StringUtil.replaceAll("", null, null));
        
        // str, chr, str
        assertEquals("aasfff", StringUtil.replaceAll("aasz", 'z', "fff"));
        assertEquals("a<s<", StringUtil.replaceAll("azsz", 'z', "<"));
        assertEquals("hello", StringUtil.replaceAll("hell$", '$', "o"));
        assertEquals("Test\\#", StringUtil.replaceAll("Test#", '#', "\\#"));
        assertEquals("Te\\#st", StringUtil.replaceAll("Te#st", '#', "\\#"));
        assertEquals("19191919", StringUtil.replaceAll("10101010", '0', "9"));
        
        // str, str, str
        assertEquals("aasfff", StringUtil.replaceAll("aasz", "z", "fff"));
        assertEquals("a<s<", StringUtil.replaceAll("azsz", "z", "<"));
        assertEquals("hello", StringUtil.replaceAll("hell$", "$", "o"));
        assertEquals("19191919", StringUtil.replaceAll("10101010", "0", "9"));
        
        // str, chr, chr
        assertEquals("hello", StringUtil.replaceAll("hell$", '$', 'o'));
        assertEquals("19191919", StringUtil.replaceAll("10101010", '0', '9'));
        assertEquals("a<s<", StringUtil.replaceAll("azsz", 'z', '<'));
        
        // Probably should only use stripChars.
        assertEquals("1111", StringUtil.removeAll("100010101", '0'));
    }
    
    /**
     * Test inStr method.
     */
    public void testInStr() {
        assertEquals(false, StringUtil.inStr(null, 'd') );
        assertEquals(false, StringUtil.inStr("", 'd') );
        assertEquals(false, StringUtil.inStr("D", 'd') );
        assertEquals(true, StringUtil.inStr("asda", 'd') );
    }
    
    /**
     * Test the checkSum method
     */
    public void testCheckSum() {
        assertEquals(null, StringUtil.checkSum(null));
        assertEquals("##377", StringUtil.checkSum("hello"));
        assertEquals("##164", StringUtil.checkSum("377"));
        assertEquals("#2334", StringUtil.checkSum("thisisalongverylongmanitislong"));
    }
    
    /**
     * Tests the reverse method
     */
    public void testReverse() {
        assertEquals(null, StringUtil.reverse(null));
        assertEquals("", StringUtil.reverse(""));
        assertEquals("tset", StringUtil.reverse("test"));
    }
    
    /**
     * Tests the getLine method.
     */
    public void testGetline() {
        
        assertEquals("", StringUtil.getLine(null, 0));
        assertEquals("", StringUtil.getLine("",0));
        
        assertEquals("hello", StringUtil.getLine("hello",0));
        
        String strTest = "Hello\nWorld";
        assertEquals(strTest, StringUtil.getLine(strTest,0));
        assertEquals("Hello", StringUtil.getLine(strTest,1));
        assertEquals("World", StringUtil.getLine(strTest,2));
        assertEquals("Hello", StringUtil.getLine(strTest,-2));
        assertEquals("World", StringUtil.getLine(strTest,-1));

        strTest = "Hello\rWorld\nAgain";
        assertEquals(strTest, StringUtil.getLine(strTest,0));
        assertEquals("Hello", StringUtil.getLine(strTest,1));
        assertEquals("Again", StringUtil.getLine(strTest,3));
        assertEquals("World", StringUtil.getLine(strTest,-2));
        assertEquals("Again", StringUtil.getLine(strTest,-1));
    }
    
    /**
     * Test the summarise functions.
     */
    public void testSummarise() {
         String [] str = StringUtil.summarize("hello how are you all doing today? This is the cool api for. Loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong sad sd sd sd sd sd sd sd sd sd sd a a a a a a a a a a a a a a a", " ", 15);
         for (int i = 0; i < str.length; i++) {
            String string = str[i];
            System.out.println(string);
        }
    }
    
    /**
     * Tests the getWord function.
     */
    public void testGetWord() {
        assertEquals("", StringUtil.getWord("", 0, " "));
        assertEquals("", StringUtil.getWord("", 2, " "));
        assertEquals("", StringUtil.getWord("", -1, " "));
        assertEquals("", StringUtil.getWord(null, -1, " "));
        assertEquals("", StringUtil.getWord("yesasdasdtest", 2, " "));
        assertEquals("", StringUtil.getWord("yesasdasdtest", -2, " "));
        
        assertEquals("yes", StringUtil.getWord("yes", 1, " "));
        assertEquals("yes", StringUtil.getWord("yes test", 1, " "));
        assertEquals("test", StringUtil.getWord("yes test", 2, " "));
        assertEquals("four", StringUtil.getWord("yes test one four you", 4, " "));
        
        assertEquals("test", StringUtil.getWord("yes test", -1, " "));
        assertEquals("five", StringUtil.getWord("one two three four five", -1, " "));
        assertEquals("four", StringUtil.getWord("one two three four five", -2, " "));
        assertEquals("test", StringUtil.getWord("This is a test :)?", -2, " "));
        
        assertEquals("test", StringUtil.getWord("ThisFisFaFtestfcan", -2, "F"));
        assertEquals("hello", StringUtil.getWord("hello\nworld", -2, "\n"));
        assertEquals("", StringUtil.getWord("hello\nworld", -2, "\t"));
    }
    
    /**
     * Test replaceAll method.
     */
    public void testEncodezXString() {
        
        assertEquals(null, StringUtil.encodezXString(null));
        assertEquals("hello", StringUtil.encodezXString("hello"));
        assertEquals("Test\\#", StringUtil.encodezXString("Test#"));
        assertEquals("Te\\#st", StringUtil.encodezXString("Te#st"));
        
    }
    
    /**
     * Test split method.
     */
    public void testSplit() {
        String splitMe = "one,two,three";
        
        assertEquals(true, ArrayUtil.isEquals(splitMe.split(","), 
                                              StringUtil.split(",", splitMe)));
        
        assertEquals(false, 
                     ArrayUtil.isEquals(splitMe.split(","), 
                                        StringUtil.split(",", splitMe, true)
                                        )
                     );
        
        // NOTE : This will fail as we match on the first element.
//        assertEquals(true, 
//                     ArrayUtil.isEquals(splitMe.split("o"), 
//                                        StringUtil.split("o", splitMe)
//                                        )
//                     );
        
        String splitMe2 = "{one; two;  nine; P_ods+ }";
        assertEquals(false, 
                     ArrayUtil.isEquals(splitMe2.split(";"), 
                                        StringUtil.split(";", splitMe2, true)
                                       )
                     );
    }
    
    /**
     * Test the vb version of the hash thing.
     */
    public void testHash() {
    	System.out.println(StringUtil.hash("hello"));
    }
}