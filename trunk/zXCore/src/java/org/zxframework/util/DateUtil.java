/*
 * Created on Mar 29, 2004
 * $Id: DateUtil.java,v 1.1.2.5 2006/07/17 16:15:22 mike Exp $
 */
package org.zxframework.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Special Date Utils similiar to the vb functions.
 * This can be expanded to allow for a special date api for newbie java developers :).
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public  class DateUtil {
    
	//------------------------ Constants
	
    /** <code>MILLI_DIFF</code> - Used for milliseconds diff */
    public static final int MILLI_DIFF = 0;
    /** <code>SEC_DIFF</code> - Used for seconds diff */
    public static final int SEC_DIFF = 1;
    /** <code>MIN_DIFF</code> - Used for minutes diff */
    public static final int MIN_DIFF = 2;
    /** <code>HOUR_DIFF</code> - Used for hours diff */
    public static final int HOUR_DIFF = 3;
    /** <code>DAY_DIFF</code> - Used for day diff */
    public static final int DAY_DIFF = 4;
    /** <code>MONTH_DIFF</code> - Used for month diff */
    public static final int MONTH_DIFF = 5;
    /** <code>YEAR_DIFF</code> - Used for year diff */
    public static final int YEAR_DIFF = 6;
    /** <code>MAX_VALUE</code> - The largest value for the Date diff type. */
    public static final int MAX_VALUE = 7;
    
    /**
     * Number of milliseconds in a standard second.
     */
    public static final int MILLIS_IN_SECOND = 1000;
    /**
     * Number of milliseconds in a standard minute.
     */
    public static final int MILLIS_IN_MINUTE = 60 * 1000;
    /**
     * Number of milliseconds in a standard hour.
     */
    public static final int MILLIS_IN_HOUR = 60 * 60 * 1000;
    /**
     * Number of milliseconds in a standard day.
     */
    public static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    
    /**
     * Standard dateFormats. 
     */
    private static final DateFormat[] ARR_DATEFORMATS = {new SimpleDateFormat("ddMMMyyy"), new SimpleDateFormat("dd/MM/yyyy")};
    
    //------------------------ Public methods
    
    /**
     * This emulates the VB DateDiff function. 
     * 
     * @param pintInterval The type of diff you want. It supports SECS/MILLI/DAYS/MONTHS and YEAR_DIFF.
     * @param pdat1 The date to substract.
     * @param pdat2 The date to substract from.
     * @see DateUtil#datediff(int, Date, Date, boolean)
     * @return Returns a Long value specifying the number of time intervals between two Date values.
     */
    public static long datediff(int pintInterval, Date pdat1, Date pdat2) {
        return datediff(pintInterval, pdat1, pdat2, true);
    }
    
    /**
     * This emulates the VB DateDiff function. 
     * 
     * <pre>
     * The interval of the difference returned is determined by the pintInterval value.
     * 
     * MILLI_DIFF - Returns the difference of 2 dates in milliseconds.
     * SEC_DIFF - Returns the difference of 2 dates in seconds.
     * DAY_DIFF - Returns the difference of 2 dates in whole days.
     * MONTH_DIFF - Returns the difference of 2 dates in whole months.
     * YEAR_DIFF - Returns the difference of 2 dates in whole years.
     * 
     * Usage :
     * DateUtil.datediff(DateUtil.DAY_DIFF, date1, date2);
     * 
     * http://msdn.microsoft.com/library/default.asp?url=/library/en-us/vblr7/html/vafctDateDiff.asp 
     * <pre>
     * 
     * @param pintInterval The type of diff you want. It supports SECS/MILLI/DAYS/MONTHS and YEAR_DIFF.
     * @param pdat1 The date to substract.
     * @param pdat2 The date to substract from.
     * @param blnWholeDates Whether to return differences of whole values.
     * @return Returns the number of day/months or years between to values. It the second parameter is greater a minus value is returned.
     */
    public static long datediff(int pintInterval, Date pdat1, Date pdat2, boolean blnWholeDates) {
        long datediff = 0;
        
        /**
         * Must be the same date
         */
        if (pdat1 == pdat2) {
            return datediff;
        }
        
        if (pdat1 == null) {
        	pdat1 = new Date();
        }
        if (pdat2 == null) {
        	pdat2 = new Date();
        }
        
        Calendar calValue1;
        Calendar calValue2;
        
        switch (pintInterval) {
            case MILLI_DIFF:
                datediff = pdat2.getTime() - pdat1.getTime();
                break;
                
            case SEC_DIFF:
                datediff = (pdat2.getTime() - pdat1.getTime()) / MILLIS_IN_SECOND;
                break;

            case MIN_DIFF:
                datediff = (pdat2.getTime() - pdat1.getTime()) / MILLIS_IN_MINUTE;
                break;
                
            case HOUR_DIFF:
                datediff = (pdat2.getTime() - pdat1.getTime()) / MILLIS_IN_HOUR;
                break;
                
            case DAY_DIFF:
                datediff = (pdat2.getTime() - pdat1.getTime()) / MILLIS_IN_DAY;
                break;
                
            case MONTH_DIFF:
    	        calValue1 = Calendar.getInstance();
    	        calValue1.setLenient(false);
    	        calValue1.setTimeInMillis(pdat1.getTime());
    	        calValue2 = Calendar.getInstance();
    	        calValue2.setLenient(false);
    	        calValue2.setTimeInMillis(pdat2.getTime());
    	        
                if (calValue1.after(calValue2)) {
                    // Whether to return difference in whole months :
                    if (blnWholeDates) {
                        calValue1.add(Calendar.MONTH, -1);
                    }
                    
    				while (calValue1.after(calValue2) ) {
    					datediff--;
    				    calValue1.add(Calendar.MONTH, -1);
    				}
    				
    				if (calValue1.equals(calValue2)) {
    				    datediff--;
    				} else if (!blnWholeDates && calValue2.get(Calendar.MONTH) > calValue1.get(Calendar.MONTH)) {
    				    // Started on the same month
    					datediff++;
    				}
    				
                } else {
                    if (blnWholeDates) {
                        calValue1.add(Calendar.MONTH, 1);
                    }
    				while (calValue1.before(calValue2) ) {
    					datediff++;
    					calValue1.add(Calendar.MONTH, 1);
    				}
    				
    				if (!blnWholeDates && calValue2.get(Calendar.MONTH) < calValue1.get(Calendar.MONTH)) {
    				    // Started on the same month
    					datediff--;
    				} else if (calValue1.equals(calValue2)) {
    				    datediff++;
    				}
                }
                
                break;
                
            case YEAR_DIFF:
    	        calValue1 = Calendar.getInstance();
    	        calValue1.setLenient(false);
    	        calValue1.setTimeInMillis(pdat1.getTime());
    	        calValue2 = Calendar.getInstance();
    	        calValue2.setLenient(false);
    	        calValue2.setTimeInMillis(pdat2.getTime());
    	        
                if (blnWholeDates) {
                    datediff = datediff(MONTH_DIFF, pdat1, pdat2) / 12;
                } else {
                    datediff = calValue2.get(Calendar.YEAR) - calValue1.get(Calendar.YEAR);
                }
                
                break;
                
            default:
            	/**
            	 * Unhandled date diff.
            	 */
                datediff = 0;
                break;
        }
        
        return datediff;
    }    
    
    /**
     * Tests to see if the date is valid. It will also test against
     * standard dateformats like : 31Dec2004 and 31/12/2004.
     * @param dateFormatter The Date Format to use.
     * @param pstrValue The value validation is being performed on.
     * 
     * @return Returns true if it is a valid date.
     */
    public static boolean isValid(DateFormat dateFormatter, String pstrValue) {
    	/**
    	 * Try the specified dateFormat first.
    	 */
        if (isDateValid(new DateFormat[]{dateFormatter}, pstrValue)) {
        	return true;
        }
        
    	/**
    	 * Try standard date formats.
    	 */
    	return isDateValid(ARR_DATEFORMATS, pstrValue);
    }
    
    /**
     * Tests to see if the date is valid. It will also test against
     * standard dateformats like : 31Dec2004 and 31/12/2004.
     * @param parrDateFormats A collection of dateFormats to test.
     * @param pstrValue The value validation is being performed on.
     *
     * @return Returns true if it is a valid date.
     */
    public static boolean isValid(DateFormat[] parrDateFormats, String pstrValue) {
    	/**
    	 * Try the specified date Formats first.
    	 */
    	if (isDateValid(parrDateFormats, pstrValue)) {
    		return true;
    	}
    	
    	/**
    	 * Try standard date formats.
    	 */
    	return isDateValid(ARR_DATEFORMATS, pstrValue);
    }
    
    /**
     * Try to parse a sting as a date. Also
     * tries to use the standard date formats when parsing.
     * 
     * @param pobjDateFormats The dateFormat to use.
     * @param pstrValue The string you want to test.
     * @return Returns date if successfull otherwise null.
     */
    public static Date parse(DateFormat pobjDateFormats, String pstrValue) {
    	return parse(new DateFormat[]{pobjDateFormats}, pstrValue);
    }
    
    /**
     * Try to parse a sting as a date. Also
     * tries to use the standard date formats when parsing.
     * 
     * @param parrDateFormats A collection of dateformats to test.
     * @param pstrValue The string you want to test.
     * @return Returns date if successfull otherwise null.
     */
    public static Date parse(DateFormat[] parrDateFormats, String pstrValue) {
    	Date parse = parseDate(parrDateFormats, pstrValue);
    	
    	if (parse == null) {
        	/**
        	 * Try to use the standard date formats next.
        	 */
    		parse = parseDate(ARR_DATEFORMATS, pstrValue);
    	}
    	
    	return parse;
    }
    
    //------------------------ Private methods
    
    /**
     * @param parrDateFormats A collection of dateformats to use for testing.
     * @param pstrValue The string you want to test.
     * @return Returns true if a valid date.
     */
    private static boolean isDateValid(DateFormat[] parrDateFormats, String pstrValue) {
    	return parseDate(parrDateFormats, pstrValue) != null;
    }
    
    /**
     * Tried to parse a sting as a date.
     * 
     * @param parrDateFormats A collection of dateformats to use.
     * @param pstrValue The string you want to parse.
     * @return Returns date if successfull otherwise null.
     */
    private static Date parseDate(DateFormat[] parrDateFormats, String pstrValue) {
    	Date parseDate = null;
    	
    	DateFormat dateFormat;
    	
    	for (int i = 0; i < parrDateFormats.length; i++) {
    		dateFormat = parrDateFormats[i];
    		dateFormat.setLenient(true);
    		
    		try {
    			parseDate = dateFormat.parse(pstrValue);
    			return parseDate;
    			
    		} catch(Exception e1) {
    			/** Ignore exception and carry on to try the next format. */
    		}
		}
    	
    	return parseDate;
    }
    
    /**
     * Get the real "day of the week" within a year.
     * 
     * @param pintFirstDayOfWeek The first day of the week (Calendar.MONDAY etc..).
     * @param pdat The current date to check.
     * @return Returns the real day of the week ie : Monday is the first day of the week.
     */
    public static int getRealDayofWeek(int pintFirstDayOfWeek, Date pdat) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(pdat.getTime());
    	cal.setFirstDayOfWeek(pintFirstDayOfWeek);
    	
    	if (pintFirstDayOfWeek == Calendar.SUNDAY) {
    		/**
    		 * DAY_OF_WEEK is measure from SUNDAY which is 1.
    		 * So we just return the value.
    		 */
    		return cal.get(Calendar.DAY_OF_WEEK);
    	}
    	
		int getRealDayofWeek = 1;
		int intCurrentYear = cal.get(Calendar.YEAR);
		while (true) {
			if (cal.get(Calendar.DAY_OF_WEEK) == pintFirstDayOfWeek) {
				return getRealDayofWeek;
			}
			
			cal.add(Calendar.DATE, -1);
			
			/**
			 * Within this year.
			 */
			if (intCurrentYear > cal.get(Calendar.YEAR)) {
				return getRealDayofWeek;
			}
			
			getRealDayofWeek++;
		}
    }
}