/*
 * Created on Jan 13, 2004 by michael
 * $Id: zXType.java,v 1.1.2.27 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.enum.Enum;
import org.zxframework.util.EnumConverter;

/**
 * Mimic the enum in VB.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class zXType {
    
    /**
     * Mimic the dataType enum.
     */
	public static final class dataType extends Enum {
	    
	    /** <code>pos</code> -  The position of the enum. */
	    public int pos;
	    
		/** <code>dtAutomatic</code> - Used to generated keys. */
		public static final dataType dtAutomatic = new dataType("automatic");
		/** <code>dtString</code> - String dataType. */
		public static final dataType dtString = new dataType("string");
		/** <code>dtLong</code> - Long dataType. */
		public static final dataType dtLong = new dataType("long");
		/** <code>dtDouble</code> - Double dataType */
		public static final dataType dtDouble = new dataType("double");
		/** <code>dtDate</code> - Date dataType. */
		public static final dataType dtDate = new dataType("date");
		/** <code>dtBoolean</code> - Boolean dataType. */
		public static final dataType dtBoolean = new dataType("boolean");
		/**<code>dtTime</code> - Time dataType. */
		public static final dataType dtTime = new dataType("time");
		/** <code>dtTimestamp</code> - Timestamp dataType. */
		public static final dataType dtTimestamp = new dataType("timestamp");
		/** <code>dtExpression</code> - Expression dataType. */
		public static final dataType dtExpression = new dataType("expression");
		
		static  {
		    dtAutomatic.pos = 0;
		    dtString.pos = 1;
		    dtLong.pos = 2;
		    dtDouble.pos = 3;
		    dtDate.pos = 4;
		    dtBoolean.pos = 5;
		    dtTime.pos = 6;
		    dtTimestamp.pos = 7;
		    dtExpression.pos = 8;
		}
        
	    private dataType(String dataType) { super(dataType); }
	    
	    /**
	     * @param pstrDataType The name of the enum.
	     * @return Returns the datatype
	     */
	    public static dataType getEnum(String pstrDataType) {
	        return (dataType) getEnum(dataType.class, zxVar(pstrDataType));
	    }
	    
	    /**
	     * @param pintDataType The pos of the enum.
	     * @return Returns the datatype
	     */
	    public static dataType getEnum(int pintDataType) {
	        dataType getEnum;
	        
	        switch (pintDataType) {
                case 0:
                    getEnum = dtAutomatic;
                    break;
                case 1:
                    getEnum = dtString;
                    break;
                case 2:
                    getEnum = dtLong;
                    break;
                case 3:
                    getEnum = dtDouble;
                    break;
                case 4:
                    getEnum = dtDate;
                    break;
                case 5:
                    getEnum = dtBoolean;
                    break;
                case 6:
                    getEnum = dtTime;
                    break;
                case 7:
                    getEnum = dtTimestamp;
                    break;   
                case 8:
                    getEnum = dtExpression;
                    break;
                    
                default:
                    getEnum = dtAutomatic;
                    break;
            }
	        
	        return getEnum;
	    }
        
        /**
         * @param pintPos The position of the datatype.
         * @return Returns true if the datatype is a date.
         */
        public static boolean isDate(int pintPos) {
            switch (pintPos) {
            case 4: // dtDate
                return true;
            case 6: // dtTime
                return true;
            case 7: // dtTimestamp
                return true;
            default:
                return false;
            }
        }
        
        /**
         * @param pintPos The position of the datatype.
         * @return Returns true if the datatype is a number.
         */
        public static boolean isNumber(int pintPos) {
            switch (pintPos) {
            
            case 0: // dtAutomatic
                return true;
            case 2: // dtLong
                return true;
            case 3: // dtDouble
                return true;
                
            default:
                return false;
            }
        }
	}
	
	/**
	 * Mimic the enum for the rc.
	 */
	public static final class rc extends Enum {
	    
	    /** <code>pos</code> -  The position of the enum. */
	    public int pos;
	    
		/** <code>rcOK</code> - OK return code. */
		public static final rc rcOK = new rc("ok");
		/**<code>rcWarning</code> - Warning return code. */
		public static final rc rcWarning = new rc("warning");
		/**<code>rcError</code> - Error return code.*/
		public static final rc rcError = new rc("error");
		
		static {
		    rcOK.pos = 0;
		    rcWarning.pos = 1;
		    rcError.pos = 2;
		}
		
        private rc(String rc) { super(rc); }
        
	    /**
	     * @param pstrRC The name of the enum
	     * @return Returns the return code 
	     */
	    public static rc getEnum(String pstrRC) {
	        return (rc) getEnum(rc.class, zxVar(pstrRC));
	    }
	}

	/**
	 * Mimic the enum for the runMode.
	 * 
	 * <pre>
	 * 
	 * The runlevels are : 
	 * prod - For production
	 * test - For testing
	 * devl - For development
	 * edit - For running inside of the ide or editor.
	 * systrace - For tracing.
	 * </pre>
	 * 
	 */
	public static final class runMode extends Enum {
	    
        /** The current enum position. */
        public int pos;
        
		/** 
		 * <code>rmProduction</code> - Production run mode. 
		 * Postion : 0
		 * */
		public static final runMode rmProduction = new runMode("prod");
		/** 
		 * <code>rmTest</code> - Test run mode. 
		 * Postion : 1
		 **/
		public static final runMode rmTest = new runMode("test");
		/**
		 * <code>rmDevelopment</code> - Development run mode. 
		 * Postion : 2
		 **/
		public static final runMode rmDevelopment = new runMode("devl");
		/**
		 * <code>rmEditor</code> - Editor run mode -  running inside of the repository editor. 
		 * Postion : 3 
		 **/
		public static final runMode rmEditor = new runMode("edit");
		/**
		 * <code>rmSystemTrace</code> - Tracing run mode. 
		 * Postion : 4 
		 **/
		public static final runMode rmSystemTrace = new runMode("systrace");
        /**
         * <code>rmDevelCache</code> - Development run mode plus caching. 
         * Postion : 5 
         **/
        public static final runMode rmDevelCache = new runMode("devlcache");
	    
		private runMode(String runMode) { super(runMode); }
        
        static {
            rmProduction.pos = 0;
            rmTest.pos = 1;
            rmDevelopment.pos = 2;
            rmEditor.pos = 3;
            rmSystemTrace.pos = 4;
            rmDevelCache.pos = 5;
        }
        
	    /**
	     * @param runMode The name of the enum.
	     * @return Returns the runmode
	     */
	    public static runMode getEnum(String runMode) {
	        return (runMode) getEnum(runMode.class, zxVar(runMode));
	    }
	}
	
	/**
	 * Mimic the enum for persistStatus
	 */
	public static final class persistStatus extends Enum {
	    
	    /** <code>psClean</code> -  */
	    public static final persistStatus psClean = new persistStatus("clean");
	    /** <code>psDirty</code> -  */
	    public static final persistStatus psDirty = new persistStatus("dirty");
	    /** <code>psNew</code> -  */
	    public static final persistStatus psNew = new persistStatus("new");
	    /** <code>psDeleted</code> -  */
	    public static final persistStatus psDeleted = new persistStatus("deleted");
		
        private persistStatus(String persistStatus) { super(persistStatus); }

	    /**
	     * @param persistStatus The name of the enum.
	     * @return Returns the persistStatus
	     */
	    public static persistStatus getEnum(String persistStatus) {
	        return (persistStatus) getEnum(persistStatus.class, zxVar(persistStatus));
	    }
	}
	
	/**
	 * Mimic the enum for sessionSouce
	 */
	public static final class sessionSouce extends Enum {
	    
	    /** <code>ssHttpSessionssHttpSession</code> -  */
	    public static final sessionSouce ssHttpSession = new sessionSouce("httpsession");
	    /** <code>ssDB</code> -  */
	    public static final sessionSouce ssDB = new sessionSouce("db");
	    /** <code>ssAlternativeDB</code> -  */
	    public static final sessionSouce ssAlternativeDB = new sessionSouce("alternativedb");
	    /** <code>ssFile</code> -  */
	    public static final sessionSouce ssFile = new sessionSouce("file");
		
        private sessionSouce(String sessionSouce) { super(sessionSouce); }

	    /**
	     * @param sessionSouce The name of the enum.
	     * @return Returns the sessionSouce
	     */
	    public static sessionSouce getEnum(String sessionSouce) {
	        return (sessionSouce) getEnum(sessionSouce.class, sessionSouce);
	    }
	}

	/**
	 * Mimic the enum for textCase.
	 */
	public static final class textCase extends Enum {
		
	    /** <code>pos</code> -  The position of the enum. */
	    public int pos;
	    
	    /** <code>tcMixed</code> - Mixed case */
	    public static final textCase tcMixed = new textCase("mixed");
	    /** <code>tcLower</code> - Lower case the property value. */
	    public static final textCase tcLower = new textCase("lower");
	    /** <code>tcUpper</code> - Upper case the property value. */
	    public static final textCase tcUpper = new textCase("upper");
	    /** <code>tcCapital</code> - Capitalise the property value. */
	    public static final textCase tcCapital = new textCase("capitalized");
	    /** <code>tcInsensitive</code> - The property should use search insensitive searches. */
	    public static final textCase tcInsensitive = new textCase("insensitive");
	    
	    static {
	    	tcMixed.pos = 0;
	    	tcLower.pos = 1;
	    	tcUpper.pos = 2;
	    	tcCapital.pos = 3;
	    	tcInsensitive.pos = 4;
	    }
	    
	    private textCase(String textCase) { super(textCase); }
        
	    /**
	     * @param textCase The name of the enum.
	     * @return Returns the textCase
	     */
	    public static textCase getEnum(String textCase) {
	    	return (textCase) getEnum(textCase.class, zxVar(textCase));
	    }
	    
	    /**
	     * @param textCase The pos of the enum.
	     * @return Returns the textCase
	     */
	    public static textCase getEnum(int textCase) {
	        textCase getEnum;
	        switch (textCase) {
                case 0:
                    getEnum = tcMixed;
                    break;
                case 1:
                    getEnum = tcLower;
                    break;
                case 2:
                    getEnum = tcUpper;
                    break;
                case 3:
                    getEnum = tcCapital;
                    break;
                case 4:
                    getEnum = tcInsensitive;
                    break;
                default:
                    getEnum = tcMixed;
                    break;
            }
	        return getEnum;
	    }
	}
	
	/**
	 * Mimic the enum for searchMethod
	 */
	public static final class searchMethod extends Enum {

	    /** The position of the enum. **/
	    public int pos;	
	    
	    /**<code>smSimple</code> - */
	    public static final searchMethod smSimple = new searchMethod("simple");
	    /** <code>smRange</code> -  */
	    public static final searchMethod smRange = new searchMethod("range");
		
	    static {
	        smSimple.pos = 0;
	        smRange.pos = 1;
	    }
	    
        private searchMethod(String searchMethod) { super(searchMethod); }
        
	    /**
	     * @param searchMethod The name of the enum.
	     * @return Returns the searchMethod
	     */
	    public static searchMethod getEnum(String searchMethod) {
	        return (searchMethod) getEnum(searchMethod.class, zxVar(searchMethod));
	    }   
	    
	    /**
	     * @param searchMethod The pos of the enum.
	     * @return Returns the searchMethod
	     */
	    public static searchMethod getEnum(int searchMethod) {
            if (searchMethod == 0) {
	            return smSimple;
	        }
            
            return smRange;
	    }
	}
	
	/**
	 * Mimic the enum for entitySize
	 */
	public static final class entitySize extends Enum {
	    
	    /** Position of the enum. **/
	    public int pos;
	    
	    /** <code>esSmall</code> -  */
	    public static final entitySize esSmall = new entitySize("small");
	    /** <code>esMedium</code> -  */
	    public static final entitySize esMedium = new entitySize("medium");
	    /** <code>esLarge</code> - */
	    public static final entitySize esLarge = new entitySize("large");
		
	    static {
	        esSmall.pos = 0;
	        esMedium.pos = 1;
	        esLarge.pos = 2;
	    }
	    
        private entitySize(String entitySize) { super(entitySize); }
        
	    /**
	     * @param entitySize The name of the enum.
	     * @return Returns the entitySize
	     */
	    public static entitySize getEnum(String entitySize) {
	        return (entitySize) getEnum(entitySize.class, zxVar(entitySize));
	    }
	    
	    /**
	     * @param entitySize The pos of the enum.
	     * @return Returns the entitySize
	     */
	    public static entitySize getEnum(int entitySize) {
	        entitySize getEnum;
	        switch (entitySize) {
                case 0:
                    getEnum = esSmall;
                    break;
                case 1:
                    getEnum = esMedium;
                    break;
                case 2:
                    getEnum = esLarge;
                    break;

                default:
                    getEnum = esSmall;
                    break;
            }
	        return getEnum;
	    }
	}
	
	/**
	 * Mimic the enum for persistAction - The order of the enum should not be modified. 
	 * This class is slightly richer with functionality than the rest as it is used for the pre and post persist actions.
	 */
	public static final class persistAction extends Enum {
	    
	    /** <code>pos</code> - The position of the enum*/
	    public int pos;
	    
	    /** <code>paProcess</code> -  During a generic processing action (pos=0). */
	    public static final persistAction paProcess = new persistAction("process");
	    /** <code>paInsert</code> - The insert persist action (pos=1).  */
	    public static final persistAction paInsert = new persistAction("insert");
	    /** <code>paDelete</code> - The delete persist action (pos=2). */
	    public static final persistAction paDelete = new persistAction("delete");
	    /** <code>paUpdate</code> - The update persist action (pos=3). */
	    public static final persistAction paUpdate = new persistAction("update");
	    /**<code>paReset</code> - The reset persist action (pos=4). */
	    public static final persistAction paReset = new persistAction("reset");
	    /** <code>paSetAutomatics</code> -  The setAutomatics persist action (pos=5). */
	    public static final persistAction paSetAutomatics = new persistAction("setautomics");
	    /** <code>paBO2BO</code> -  The ZXBO 2 ZXBO cloning action (pos=6). */
	    public static final persistAction paBO2BO = new persistAction("bo2bo");
	    /** <code>paRs2obj</code> -  Copy the resultset to business object action (pos=7). */
	    public static final persistAction paRs2obj = new persistAction("rs2obj");
	    /** <code>paView</code> - The view action (pos=8). */
	    public static final persistAction paView = new persistAction("view");
	    /** <code>paSearchForm</code> - Showing the search form action (pos=9).  */
	    public static final persistAction paSearchForm = new persistAction("searchform");
	    /** <code>paEditForm</code> - Showing a pageflow edit form action (pos=11). */
	    public static final persistAction paEditForm = new persistAction("editform");
	    /** <code>paListRow</code> - Listing a row element action (pos=13). */
	    public static final persistAction paListRow = new persistAction("listrow");
	    /** <code>paProcessEditForm</code> -  Processing a edit form action (pos=15). */
	    public static final persistAction paProcessEditForm = new persistAction("processeditform");
	    /** <code>paProcessSearchForm</code> -  Processing a search form action (pos=17). */
	    public static final persistAction paProcessSearchForm = new persistAction("processsearchform");
	    /**<code>paProcessMultiListForm</code> -  Processing a multilistform action (pos=18). */
	    public static final persistAction paProcessMultiListForm = new persistAction("multilistform");
	    /** <code>paProcessEditFormError</code> -  Process an error on a edit form (pos=19). */
	    public static final persistAction paProcessEditFormError = new persistAction("editformerror");
        /** <code>paDocBuilderAction</code> -  Doc bulder action (pos=20). */
        public static final persistAction paDocBuilderAction = new persistAction("docbuilderaction");
        /** <code>paQueryBuilderAction</code> -  Query bulder action (pos=21). */
        public static final persistAction paQueryBuilderAction = new persistAction("querybuilderaction");
        
	    static {
	        paProcess.pos = 0;
	        paInsert.pos = 1;
	        paDelete.pos = 2;
	        paUpdate.pos = 3;
	        paReset.pos = 4;
	        paSetAutomatics.pos = 5;
	        paBO2BO.pos = 6;
	        paRs2obj.pos = 7;
	        paView.pos = 8;
            paSearchForm.pos = 9;
            // 10
            paEditForm.pos = 11;
            // 12
            paListRow.pos = 13;
            // 14
            paProcessEditForm.pos = 15;
            // 16
            paProcessSearchForm.pos = 17;
            paProcessMultiListForm.pos = 18;
            paProcessEditFormError.pos = 19;
            paDocBuilderAction.pos = 20;
            paQueryBuilderAction.pos = 21;
            // paHighestValue.pos  = 22;
	    }
	    
        private persistAction(String persistAction) { super(persistAction); }
        
	    /**
	     * @param persistAction The name of the enum.
	     * @return Returns the persistAction
	     */
	    public static persistAction getEnum(String persistAction) {
	        return (persistAction) getEnum( persistAction.class, zxVar(persistAction));
	    }        
	    
	    /**
	     * This is useful for the pre and postpersist action scripting. 
	     * 
	     * @return Returns a iterator for the enum
	     */
	    public static Iterator iterator() {
	        return Enum.getEnumList(persistAction.class).iterator();
	    }
	    
	    /**
	     * @return Returns the number of enums in this class.
	     */
	    public static int getHighestValue() {
	        //return Enum.getEnumList(persistAction.class).size();
	        return 22;
	    }
	}
	
	/**
	 * Mimic the enum for databaseType
	 */
	public static final class databaseType extends Enum{

	    /** <code>dbAny</code> - Universally supported by all databases */
	    public static final databaseType dbAny = new databaseType("any");
	    /** <code>dbAccess</code> - For the dreaded Access. */
	    public static final databaseType dbAccess = new databaseType("access");
	    /** <code>dbOracle</code> - For Oracle database type. */
	    public static final databaseType dbOracle = new databaseType("oracle");
	    /** <code>dbSQLServer</code> - For SQL. */
	    public static final databaseType dbSQLServer = new databaseType("sql-server");
	    /** <code>dbDB2</code> - DB. But this might be difference from AS400 db2. */
	    public static final databaseType dbDB2 = new databaseType("db2");
	    /** <code>dbDB2</code> - DB. But this might be difference from AS400 db2. */
	    public static final databaseType dbAS400 = new databaseType("as400");
	    /** <code>dbMysql</code> - Mysql, used in develepment. */
	    public static final databaseType dbMysql = new databaseType("mysql");
	    /** <code>dbHsql</code> - HSQL, used in develepment. */
	    public static final databaseType dbHsql = new databaseType("hsql");

        private databaseType(String databaseType) {  super(databaseType); }

	    /**
	     * @param databaseType The name of the enum.
	     * @return Returns the databaseType
	     */
	    public static databaseType getEnum(String databaseType) {
	        return (databaseType) getEnum(databaseType.class, zxVar(databaseType));
	    }        
	}

	/**
	 * Mimic the enum for logLevel
	 */
	public static final class logLevel extends Enum {
	    
	    /** Useful for finding the loglevel. **/
	    public int pos;
	    
	    /** <code>llInfo</code> -  */
	    public static final logLevel llInfo = new logLevel("info");
	    /** <code>llWarning</code> -  */
	    public static final logLevel llWarning = new logLevel("warning");
	    /** <code>llError</code> - */
	    public static final logLevel llError = new logLevel("error");

	    static {
	        llInfo.pos = 0;
	        llWarning.pos = 1;
	        llError.pos = 2;
	    }
	    
        private logLevel(String logLevel) { super(logLevel); }

	    /**
	     * @param logLevel The name of the enum.
	     * @return Returns the logLevel
	     */
	    public static logLevel getEnum(String logLevel) {
	        return (logLevel) getEnum(logLevel.class, zxVar(logLevel));
	    }        
	}
	
	/**
	 * Mimic the enum for sqlObjectName
	 */
	public static final class sqlObjectName extends Enum {
	    
        /** The pos of the enum **/
        public int pos;
        
	    /** <code>sonName</code> -  The name of the table or column. (Default) */
	    public static final sqlObjectName sonName = new sqlObjectName("name");
	    /** <code>sonAlias</code> -  The alias of the column or table. */
	    public static final sqlObjectName sonAlias = new sqlObjectName("alias");
	    /** <code>sonClause</code> - The clause name of the column or table. */
	    public static final sqlObjectName sonClause = new sqlObjectName("clause");
	    /** <code>sonPureName</code> - Give the pure sql name  */
	    public static final sqlObjectName sonPureName = new sqlObjectName("purname");
	    /** <code>sonRSName</code> -  The result name of the column or table */
	    public static final sqlObjectName sonRSName = new sqlObjectName("rsname");

        private sqlObjectName(String sqlObjectName) { super(sqlObjectName); }
        
        static {
           sonName.pos = 0;
           sonAlias.pos = 1;
           sonClause.pos = 2;
           sonPureName.pos = 3;
           sonRSName.pos = 4;
        }
        
	    /**
	     * @param sqlObjectName The name of the enum.
	     * @return Returns the sqlObjectName
	     */
	    public static sqlObjectName getEnum(String sqlObjectName) {
	        return (sqlObjectName) getEnum(sqlObjectName.class, zxVar(sqlObjectName));
	    }		
	}
	
	/**
	 * Mimic the enum for webFormType
	 */
	public static final class webFormType extends Enum {
	    
	    /** <code>wftEdit</code> -  */
	    public static final webFormType wftEdit = new webFormType("edit");
	    /** <code>wftSearch</code> -  */
	    public static final webFormType wftSearch = new webFormType("search");
	    /** <code>wftMenu</code> -  */
	    public static final webFormType wftMenu = new webFormType("menu");
	    /** <code>wftList</code> - */
	    public static final webFormType wftList = new webFormType("list");
	    /** <code>wftNull</code> - */
	    public static final webFormType wftNull = new webFormType("null");
	    /** <code>wftGrid</code> -  */
	    public static final webFormType wftGrid = new webFormType("grid");
		
        private webFormType(String webFormType) { super(webFormType); }

	    /**
	     * @param webFormType The name of the enum.
	     * @return Returns the webFormType
	     */
	    public static webFormType getEnum(String webFormType) {
	        return (webFormType) getEnum(webFormType.class, zxVar(webFormType));
	    }		        
	}
	
	/**
	 * Mimic the enum for BOCompare
	 */
	public static final class BOCompare extends Enum {
	    
	    /** <code>bocError</code> -  */
	    public static final BOCompare bocError = new BOCompare("error");
	    /** <code>bocLt</code> - */
	    public static final BOCompare bocLt = new BOCompare("lt");
	    /** <code>bocEq</code> -  */
	    public static final BOCompare bocEq = new BOCompare("eq");
	    /** <code>bocGt</code> - */
	    public static final BOCompare bocGt = new BOCompare("gt");
		
        private BOCompare(String BOCompare) { super(BOCompare); }
        
	    /**
	     * @param BOCompare The name of the enum.
	     * @return Returns the BOCompare
	     */
	    public static BOCompare getEnum(String BOCompare) {
	        return (BOCompare) getEnum(BOCompare.class, zxVar(BOCompare));
	    }		        
	}

	/**
	 * Mimic the enum for pageflowDebugMode
	 */
	public static final class pageflowDebugMode extends Enum {
	    
	    /** <code>pdmOn</code> -  */
	    public static final pageflowDebugMode pdmOn = new pageflowDebugMode("on");		
	    /** <code>pdmOff</code> -  */
	    public static final pageflowDebugMode pdmOff = new pageflowDebugMode("off");		
	    /** <code>pdmInherit</code> -  */
	    public static final pageflowDebugMode pdmInherit = new pageflowDebugMode("inherit");		
	    /** <code>pdmAllErrors</code> -  */
	    public static final pageflowDebugMode pdmAllErrors = new pageflowDebugMode("allerrors");		
		
        private pageflowDebugMode(String pageflowDebugMode) { super(pageflowDebugMode); }

	    /**
	     * @param pageflowDebugMode The name of the enum.
	     * @return Returns the pageflowDebugMode
	     */
	    public static pageflowDebugMode getEnum(String pageflowDebugMode) {
	        return (pageflowDebugMode) getEnum(pageflowDebugMode.class, zxVar(pageflowDebugMode));
	    }	        
	}
	
	/**
	 * Mimic the enum for pageflowUrlType
	 */
	public static final class pageflowUrlType extends Enum {
	    
	    /** <code>putFixed</code> -   Just return the url */
	    public static final pageflowUrlType putFixed = new pageflowUrlType("fixed");		
	    /** <code>putAction</code> - */
	    public static final pageflowUrlType putAction = new pageflowUrlType("action");		
	    /** <code>putRelative</code> -  Just append the url to the base url (but first construct this one) **/
	    public static final pageflowUrlType putRelative = new pageflowUrlType("relative");		
	    /** <code>putPopup</code> - Construct a call to the generated popup function **/
	    public static final pageflowUrlType putPopup = new pageflowUrlType("popup");		

        private pageflowUrlType(String pageflowUrlType) { super(pageflowUrlType);}

	    /**
	     * @param pageflowUrlType The name of the enum.
	     * @return Returns the pageflowUrlType
	     */
	    public static pageflowUrlType getEnum(String pageflowUrlType) {
	        return (pageflowUrlType) getEnum(pageflowUrlType.class, zxVar(pageflowUrlType));
	    }		
	}
	
	/**
	 * Mimic the enum for pageflowFrameHandling
	 */
	public static final class pageflowFrameHandling extends Enum {
	    
	    /** <code>pfhMax1</code> - */
	    public static final pageflowFrameHandling pfhMax1 = new pageflowFrameHandling("sizemax1");		
	    /** <code>pfhMax2</code> -  */
	    public static final pageflowFrameHandling pfhMax2 = new pageflowFrameHandling("sizemax2");		
	    /** <code>pfhMax3</code> -  */
	    public static final pageflowFrameHandling pfhMax3 = new pageflowFrameHandling("sizemax3");		
	    /** <code>pfhMax4</code> -  */
	    public static final pageflowFrameHandling pfhMax4 = new pageflowFrameHandling("sizemax4");		
	    /** <code>pfhMax5</code> -  */
	    public static final pageflowFrameHandling pfhMax5 = new pageflowFrameHandling("sizemax5");		
	    /** <code>pfhBigger1</code> -  */
	    public static final pageflowFrameHandling pfhBigger1 = new pageflowFrameHandling("sizebigger1");		
	    /** <code>pfhBigger2</code> -  */
	    public static final pageflowFrameHandling pfhBigger2 = new pageflowFrameHandling("sizebigger2");		
	    /** <code>pfhBigger3</code> -  */
	    public static final pageflowFrameHandling pfhBigger3 = new pageflowFrameHandling("sizebigger3");		
	    /** <code>pfhBigger4</code> - */
	    public static final pageflowFrameHandling pfhBigger4 = new pageflowFrameHandling("sizebigger4");		
	    /** <code>pfhBigger5</code> -  */
	    public static final pageflowFrameHandling pfhBigger5 = new pageflowFrameHandling("sizebigger5");		
	    /** <code>pfhBlank1</code> -  */
	    public static final pageflowFrameHandling pfhBlank1 = new pageflowFrameHandling("blank1");		
	    /** <code>pfhBlank2</code> -  */
	    public static final pageflowFrameHandling pfhBlank2 = new pageflowFrameHandling("blank2");		
	    /** <code>pfhBlank3</code> -  */
	    public static final pageflowFrameHandling pfhBlank3 = new pageflowFrameHandling("blank3");		
	    /** <code>pfhBlank4</code> -  */
	    public static final pageflowFrameHandling pfhBlank4 = new pageflowFrameHandling("blank4");		
	    /** <code>pfhBlank5</code> -  */
	    public static final pageflowFrameHandling pfhBlank5 = new pageflowFrameHandling("blank5");		
	    /** <code>pfhEqual</code> -  */
	    public static final pageflowFrameHandling pfhEqual = new pageflowFrameHandling("sizeequal");		
	    /** <code>pfhClose</code> - */
	    public static final pageflowFrameHandling pfhClose = new pageflowFrameHandling("close");		
	    /** <code>pfhNone</code> -  */
	    public static final pageflowFrameHandling pfhNone = new pageflowFrameHandling("none");		
		
        private pageflowFrameHandling(String pageflowFrameHandling) { super(pageflowFrameHandling); }

	    /**
	     * @param pageflowFrameHandling The name of the enum.
	     * @return Returns the pageflowFrameHandling
	     */
	    public static pageflowFrameHandling getEnum(String pageflowFrameHandling) {
	        return (pageflowFrameHandling) getEnum(pageflowFrameHandling.class, zxVar(pageflowFrameHandling));
	    }		
	}
	
	/**
	 * Mimic the enum for pageflowQueryType
	 */
	public static final class pageflowQueryType extends Enum {
	    
	    /** <code>pqtSearchForm</code> -  */
	    public static final pageflowQueryType pqtSearchForm = new pageflowQueryType("searchform");	
	    /** <code>pqtAssociatedWith</code> -  */
	    public static final pageflowQueryType pqtAssociatedWith = new pageflowQueryType("associatedwith");	
	    /** <code>pqtNotAssociatedWith</code> -  */
	    public static final pageflowQueryType pqtNotAssociatedWith = new pageflowQueryType("notassociatedwith");
	    /** <code>pqtAll</code> -  */
	    public static final pageflowQueryType pqtAll = new pageflowQueryType("all");	
	    /** <code>pqtReservedForFuture1</code> -  */
	    public static final pageflowQueryType pqtReservedForFuture1 = new pageflowQueryType("reservedforfuture1");	
	    /** <code>pqtReservedForFuture2</code> -  */
	    public static final pageflowQueryType pqtReservedForFuture2 = new pageflowQueryType("reservedforfuture2");	
	    /** <code>pqtQueryDef</code> -  */
	    public static final pageflowQueryType pqtQueryDef = new pageflowQueryType("querydef");	
	    /** <code>pqtQs</code> -  */
	    public static final pageflowQueryType pqtQs = new pageflowQueryType("qs");	
		
        private pageflowQueryType(String pageflowQueryType) { super(pageflowQueryType); }

	    /**
	     * @param pageflowQueryType The name of the enum.
	     * @return Returns the pageflowQueryType
	     */
	    public static pageflowQueryType getEnum(String pageflowQueryType) {
	        return (pageflowQueryType) getEnum(pageflowQueryType.class, zxVar(pageflowQueryType));
	    }		
	}
	
	/**
	 * Mimic the enum for pageflowActionType
	 */	
	public static final class pageflowActionType extends Enum {
	    
	    /** <code>patSearchForm</code> -  */
	    public static final pageflowActionType patSearchForm = new pageflowActionType("searchform");	
	    /** <code>patQuery</code> -  */
	    public static final pageflowActionType patQuery = new pageflowActionType("query");	
	    /** <code>patEditForm</code> -  */
	    public static final pageflowActionType patEditForm = new pageflowActionType("editform");	
	    /** <code>patListForm</code> -  */
	    public static final pageflowActionType patListForm = new pageflowActionType("listform");	
	    /** <code>patCreateUpdate</code> -  */
	    public static final pageflowActionType patCreateUpdate = new pageflowActionType("createupdate");	
	    /** <code>patDBAction</code> -  */
	    public static final pageflowActionType patDBAction = new pageflowActionType("dbaction");	
	    /** <code>patNull</code> - */
	    public static final pageflowActionType patNull = new pageflowActionType("null");	
	    /** <code>patRefine</code> -  */
	    public static final pageflowActionType patRefine = new pageflowActionType("refine");	
	    /** <code>patLoopOver</code> -  */
	    public static final pageflowActionType patLoopOver = new pageflowActionType("loopover");	
	    /** <code>patTreeForm</code> -  */
	    public static final pageflowActionType patTreeForm = new pageflowActionType("treeform");	
	    /** <code>patStdPopup</code> -  */
	    public static final pageflowActionType patStdPopup = new pageflowActionType("stdpopup");	
	    /** <code>patRecTree</code> -  */
	    public static final pageflowActionType patRecTree = new pageflowActionType("rectree");	
	    /** <code>patGridEditForm</code> - */
	    public static final pageflowActionType patGridEditForm = new pageflowActionType("grideditform");	
	    /** <code>patGridCreateUpdate</code> -  */
	    public static final pageflowActionType patGridCreateUpdate = new pageflowActionType("gridcreateupdate");	
	    /** <code>patMatrixCreateUpdate</code> -  */
	    public static final pageflowActionType patMatrixCreateUpdate = new pageflowActionType("matrixcreateupdate");	
	    /** <code>patMatrixEditForm</code> -  */
	    public static final pageflowActionType patMatrixEditForm = new pageflowActionType("matrixeditform");	
	    /** <code>patCalendar</code> -  The new calendar component. */
	    public static final pageflowActionType patCalendar = new pageflowActionType("calendar");	
	    /** <code>patLayout</code> -  */
	    public static final pageflowActionType patLayout = new pageflowActionType("layout");	
		
        private pageflowActionType(String pageflowActionType) { super(pageflowActionType); }

	    /**
	     * @param pageflowActionType The name of the enum.
	     * @return Returns the pageflowActionType
	     */
	    public static pageflowActionType getEnum(String pageflowActionType) {
	        return (pageflowActionType) getEnum(pageflowActionType.class, zxVar(pageflowActionType));
	    }		
	}
	
	/**
	 * Mimic the enum for pageflowRefType
	 */
	public static final class pageflowRefType extends Enum {
	    
	    /** <code>prtButton</code> -  */
	    public static final pageflowRefType prtButton = new pageflowRefType("button");	
	    /** <code>prtRef</code> -  */
	    public static final pageflowRefType prtRef = new pageflowRefType("ref");	
	    /** <code>prtAction</code> -  */
	    public static final pageflowRefType prtAction = new pageflowRefType("action");	
	    /** <code>prtFieldRef</code> -  */
	    public static final pageflowRefType prtFieldRef = new pageflowRefType("fieldref");	
		
        private pageflowRefType(String pageflowRefType) { super(pageflowRefType); }

	    /**
	     * @param pageflowRefType The name of the enum.
	     * @return Returns the pageflowRefType
	     */
	    public static pageflowRefType getEnum(String pageflowRefType) {
	        return (pageflowRefType) getEnum(pageflowRefType.class, zxVar(pageflowRefType));
	    }	
	}
	
	/**
	 * Mimic the enum for pageflowSubActionType
	 */
	public static final class pageflowSubActionType extends Enum {
	    
	    /** <code>psatEdit</code> -  */
	    public static final pageflowSubActionType psatEdit = new pageflowSubActionType("edit");	
	    /** <code>psatInsert</code> -  */
	    public static final pageflowSubActionType psatInsert = new pageflowSubActionType("insert");	
	    /** <code>psatCopy</code> -  */
	    public static final pageflowSubActionType psatCopy = new pageflowSubActionType("copy");	
	    /** <code>psatError</code> -  */
	    public static final pageflowSubActionType psatError = new pageflowSubActionType("error");	
	    /** <code>psatEditNoDB</code> - */
	    public static final pageflowSubActionType psatEditNoDB = new pageflowSubActionType("editnodb");	
	    /** <code>psatEditNoDBUpdate</code> -  */
	    public static final pageflowSubActionType psatEditNoDBUpdate = new pageflowSubActionType("editnodbupdate");	

        private pageflowSubActionType(String pageflowSubActionType) { super(pageflowSubActionType); }

	    /**
	     * @param pageflowSubActionType The name of the enum.
	     * @return Returns the pageflowSubActionType
	     */
	    public static pageflowSubActionType getEnum(String pageflowSubActionType) {
	        return (pageflowSubActionType) getEnum(pageflowSubActionType.class, zxVar(pageflowSubActionType));
	    }
	}
	
	/**
	 * Mimic the enum for compareOperand.
	 * 
	 * <pre>
	 * 
	 * Need to standardize the naming of this enum as it is used in a number of places, like web forms
	 * </pre>
	 */	
	public static final class compareOperand extends Enum {
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
	    /** 
	     * <code>coEQ</code> - Equals. 
	     * Symbol : "=". 
	     * Position : 0
	     *  
	     **/
	    public static final compareOperand coEQ = new compareOperand("eq");	
	    /** 
	     * <code>coNE</code> - Not Equals.
	     * Symbol : "!=" 
	     * Position :1 
	     **/
	    public static final compareOperand coNE = new compareOperand("ne");	
	    /** 
	     * <code>coGT</code> - Greater than. 
	     * Symbol : ">"
	     * Position : 2 
	     **/
	    public static final compareOperand coGT = new compareOperand("gt");	
	    /** 
	     * <code>coGE</code> - Greater than or Equals. 
	     * Symbol : ">="
	     * Position : 3 
	     **/
	    public static final compareOperand coGE = new compareOperand("ge");	
	    /** 
	     * <code>coLT</code> -  Less Than. 
	     * Symbol : "<"
	     * Position :4 
	     **/
	    public static final compareOperand coLT = new compareOperand("lt");	
	    /** 
	     * <code>coLE</code> -  Less Than or Equals. 
	     * Symbol : "<="
	     * Position : 5 
	     **/
	    public static final compareOperand coLE = new compareOperand("le");	
	    /** 
	     * <code>coSW</code> - Starts with. 
	     * Symbol : "%**"
	     * Position : 6 
	     **/
	    public static final compareOperand coSW = new compareOperand("sw");	
	    /** 
	     * <code>coCNT</code> - Whether it contains a value. 
	     * Symbol : "%**%"
	     * Position : 7 
	     **/
	    public static final compareOperand coCNT = new compareOperand("contains"); // cnt ? 	
	    /** 
	     * <code>coBTWN</code> - Between 2 values. 
	     * Position :8 
	     **/
	    public static final compareOperand coBTWN = new compareOperand("btwn");	
        /** 
         * <code>coBTWN</code> - Between 2 values.
         * Position :9 
         **/
        public static final compareOperand coNSW = new compareOperand("nsw"); 
        /** 
         * <code>coBTWN</code> - Between 2 values.
         * Position : 10 
         **/
        public static final compareOperand coNCNT = new compareOperand("ncnt"); 
	    
        private compareOperand(String compareOperand) { super(compareOperand); }
        
        static {
            coEQ.pos = 0;
            coNE.pos = 1;
            coGT.pos = 2;
            coGE.pos = 3;
            coLT.pos = 4;
            coLE.pos = 5;
            coSW.pos = 6;
            coCNT.pos = 7;
            coBTWN.pos = 8;
            coNSW.pos = 9;
            coNCNT.pos = 10;
        }
        
	    /**
	     * @param compareOperand The name of the enum.
	     * @return Returns the compareOperand
	     */
	    public static compareOperand getEnum(String compareOperand) {
	    	compareOperand = zxVar(compareOperand);
	    	
	    	/**
	    	 * Handle cnt in the parsing :
	    	 */
	    	if (compareOperand.equalsIgnoreCase("cnt")) {
	    	    compareOperand = "contains";
	    	}
	    	
	        return (compareOperand) getEnum(compareOperand.class, compareOperand);
	    }
        
        /**
         * @return Returns a string representation of the operand.
         */
        public String getOperator() {
            switch (this.pos) {
            case 0: // coEQ
                return " = ";
            case 1: // coNE
                return " <> ";
            case 2: // coGT
                return " > ";
            case 3: // coGE
                return " >= ";
            case 4: // coLT
                return " < ";
            case 5: // coLE
                return " <= ";
            case 6: // coSW
                return " % ";
            case 7: // coCNT
                return " %% ";
            // 8 - coBTWN
            case 9: // coNSW
                return " !% ";
            case 10: // coNCNT
                return " !%% ";
            default:
                return " = ";
            }
        }
        
        /**
         * @return Returns a verbose string representation of the operand.
         */
        public String getAsVerbose() {
            switch (this.pos) {
            case 0: // coEQ
                return " = ";
            case 1: // coNE
                return " <> ";
            case 2: // coGT
                return " > ";
            case 3: // coGE
                return " >= ";
            case 4: // coLT
                return " < ";
            case 5: // coLE
                return " <= ";
            case 6: // coSW
                return " starts-with ";
            case 7: // coCNT
                return " contains ";
            case 8: // coBTWN
                return " between ";
            case 9: // coNSW
                return " does-not-start-with ";
            case 10: // coNCNT
                return " does-not-contain ";
            default:
                return " = ";
            }
        }
	}	
	
	/**
	 * Mimic the enum for pageflowDBActionType
	 */	
	public static final class pageflowDBActionType extends Enum {		
	    
	    /** <code>pdaInsert</code> -  */
	    public static final pageflowDBActionType pdaInsert = new pageflowDBActionType("insert");
	    /** <code>pdaDelete</code> -  */
	    public static final pageflowDBActionType pdaDelete = new pageflowDBActionType("delete");
	    /** <code>pdaUpdate</code> -  */
	    public static final pageflowDBActionType pdaUpdate = new pageflowDBActionType("update");
	
        private pageflowDBActionType(String pageflowDBActionType) { super(pageflowDBActionType); }

	    /**
	     * @param pageflowDBActionType The name of the enum.
	     * @return Returns the pageflowDBActionType
	     */
	    public static pageflowDBActionType getEnum(String pageflowDBActionType) {
	        return (pageflowDBActionType) getEnum(pageflowDBActionType.class, zxVar(pageflowDBActionType));
	    }
	}	
	
	/**
	 * Mimic the enum for pageflowTxBehaviour
	 */	
	public static final class pageflowTxBehaviour extends Enum {
	    
	    /** <code>ptbSafe</code> -  */
	    public static final pageflowTxBehaviour ptbSafe = new pageflowTxBehaviour("safe");
	    /** <code>ptbTry</code> -  */
	    public static final pageflowTxBehaviour ptbTry = new pageflowTxBehaviour("try");
		
        private pageflowTxBehaviour(String pageflowTxBehaviour) { super(pageflowTxBehaviour); }

	    /**
	     * @param pageflowTxBehaviour The name of the enum.
	     * @return Returns the pageFlowTxBehaviour
	     */
	    public static pageflowTxBehaviour getEnum(String pageflowTxBehaviour) {
	        return (pageflowTxBehaviour) getEnum(pageflowTxBehaviour.class, zxVar(pageflowTxBehaviour));
	    }
	}
	
	/**
	 * Mimic the enum for pageflowTreeType
	 */	
	public static final class pageflowTreeType extends Enum {	
	    
		/** <code>pttNormal</code> -  */
		public static final pageflowTreeType pttNormal = new pageflowTreeType("normal");
		/** <code>pttPivot</code> -  */
		public static final pageflowTreeType pttPivot = new pageflowTreeType("pivot");
		
        private pageflowTreeType(String pageflowTreeType) { super(pageflowTreeType); }
        
        /**
         * @param pageflowTreeType The name of the enum.
         * @return Returns the pageflowTreeType
         */
        public static pageflowTreeType getEnum(String pageflowTreeType) {
	        return (pageflowTreeType) getEnum(pageflowTreeType.class, zxVar(pageflowTreeType));
        }   	
	}	

	/**
	 * Mimic the enum for exprTokenType
	 */	
	public static final class exprTokenType extends Enum {
		
		/** Position of the enum. **/
		public int pos;
		
		/** <code>ettUnknown</code> - Unknown token type (0). */
		public static final exprTokenType ettUnknown = new exprTokenType("unknown");
		/** <code>ettDouble</code> - Double token type (1). */
		public static final exprTokenType ettDouble = new exprTokenType("double");
		/** <code>ettInteger</code> - Integar token type (2). */
		public static final exprTokenType ettInteger = new exprTokenType("integer");
		/** <code>ettDate</code> - Date token type (3). */
		public static final exprTokenType ettDate = new exprTokenType("date");
		/** <code>ettContext</code> - Context token (4). */
		public static final exprTokenType ettContext = new exprTokenType("context");
		/** <code>ettString</code> - String token (5). */
		public static final exprTokenType ettString = new exprTokenType("string");
		/** <code>ettId</code> - Id token type (6). */
		public static final exprTokenType ettId = new exprTokenType("id");
		/** <code>ettExternalId</code> - External id token type (7). */
		public static final exprTokenType ettExternalId = new exprTokenType("externalid");
		/** <code>ettStartParmList</code> - Start of parameter list (8). */
		public static final exprTokenType ettStartParmList = new exprTokenType("startparmlist");
		/** <code>ettEndParmList</code> - End of parameter list (9). */
		public static final exprTokenType ettEndParmList = new exprTokenType("endparmlist");
		/** <code>ettNextParm</code> -  Next parameter (10) */
		public static final exprTokenType ettNextParm = new exprTokenType("nextparm");
		/** <code>ettComment</code> - Parsing a comment (11). */
		public static final exprTokenType ettComment = new exprTokenType("comment");

		static {
			ettUnknown.pos = 0;
			ettDouble.pos = 1;
			ettInteger.pos = 2;
			ettDate.pos = 3;
			ettContext.pos = 4;
			ettString.pos = 5;
			ettId.pos = 6;
			ettExternalId.pos = 7;
			ettStartParmList.pos = 8;
			ettEndParmList.pos = 9;
			ettNextParm.pos = 10;
			ettComment.pos = 11;
		}
		
        private exprTokenType(String exprTokenType) { super(exprTokenType); }
        
        /**
         * @param exprTokenType The name of the enum.
         * @return Returns the exprTokenType
         */
        public static exprTokenType getEnum(String exprTokenType) {
	        return (exprTokenType) getEnum(exprTokenType.class, zxVar(exprTokenType));
        }        
	}	

	/**
	 * Mimic the enum for exprParseState
	 */	
	public static final class exprParseState extends Enum {
	    
		/** Position of the enum. **/
		public int pos;
		
		/** <code>epsStart</code> - Start of an expression (0). */
		public static final exprParseState epsStart = new exprParseState("start");
		/** <code>epsId</code> - Parsing a id (1). */
		public static final exprParseState epsId = new exprParseState("id");
		/** <code>epsExternalId</code> -  Parsing an external id (2). */
		public static final exprParseState epsExternalId = new exprParseState("externalid");
		/** <code>epsInteger</code> - Parsing a integer (3). */
		public static final exprParseState epsInteger = new exprParseState("integer");
		/** <code>epsDouble</code> - Parsing a double (4). */
		public static final exprParseState epsDouble = new exprParseState("double");
		/** <code>epsDate</code> - Parsing a date (5). */
		public static final exprParseState epsDate = new exprParseState("date");
		/**<code>epsDateClose</code> - Parsing the end of a date (6). */
		public static final exprParseState epsDateClose = new exprParseState("dateclose");
		/** <code>epsContext</code> -  Parsing a context (7). */
		public static final exprParseState epsContext = new exprParseState("context");
		/** <code>epsContextClose</code> -  End of a context (8). */
		public static final exprParseState epsContextClose = new exprParseState("contextclose");
		/**<code>epsString</code> - Parsing a string (9). */
		public static final exprParseState epsString = new exprParseState("string");
		/** <code>epsStringClose</code> - End of a string (10). */
		public static final exprParseState epsStringClose = new exprParseState("stringclose");
		/** <code>epsStringEscape</code> - Parsing a escape character (11). */
		public static final exprParseState epsStringEscape = new exprParseState("stringescape");
		/**<code>epsDone</code> - Finished parsing (12). */
		public static final exprParseState epsDone = new exprParseState("done");
		/** <code>epsError</code> - Error in parsing (13). */
		public static final exprParseState epsError = new exprParseState("error");
		/** <code>epsComment</code> - Parsing a comment (14). */
		public static final exprParseState epsComment = new exprParseState("comment");
		/** <code>epsCommentClose</code> - End of comment (15). */
		public static final exprParseState epsCommentClose = new exprParseState("commentclose");
		
		static {
			epsStart.pos = 0;
			epsId.pos = 1;
			epsExternalId.pos = 2;
			epsInteger.pos = 3;
			epsDouble.pos = 4;
			epsDate.pos = 5;
			epsDateClose.pos = 6;
			epsContext.pos = 7;
			epsContextClose.pos = 8;
			epsString.pos = 9;
			epsStringClose.pos = 10;
			epsStringEscape.pos = 11;
			epsDone.pos = 12;
			epsError.pos = 13;
			epsComment.pos = 14;
			epsCommentClose.pos = 15;
		}
		
        private exprParseState(String exprParseState) { super(exprParseState); }
        
        /**
         * @param exprParseState The name of the enum.
         * @return Returns the exprParseState
         */
        public static exprParseState getEnum(String exprParseState) {
	        return (exprParseState) getEnum(exprParseState.class, zxVar(exprParseState));
        }            
	}

	/**
	 * Mimic the enum for exprPurpose
	 */	
	public static final class exprPurpose extends Enum {
	    
		/** <code>epEval</code> -  */
		public static final exprPurpose epEval = new exprPurpose("eval");
		/** <code>epAPI</code> -  */
		public static final exprPurpose epAPI = new exprPurpose("api");
		/** <code>epDescribe</code> -  */
		public static final exprPurpose epDescribe = new exprPurpose("describe");
		/** <code>epConstruct</code> -  */
		public static final exprPurpose epConstruct = new exprPurpose("construct");
		
        private exprPurpose(String exprPurpose) { super(exprPurpose);}
        
        /**
         * @param exprPurpose The name of the enum.
         * @return Returns the exprPurpose
         */
        public static exprPurpose getEnum(String exprPurpose) {
	        return (exprPurpose) getEnum(exprPurpose.class, zxVar(exprPurpose));
        }            
	}

	/**
	 * Mimic the enum for controlLabelSize
	 */	
	public static final class controlLabelSize extends Enum {
	    
		/** <code>clsRegular</code> -  */
		public static final controlLabelSize clsRegular = new controlLabelSize("regular");
		/** <code>clsSmall</code> -  */
		public static final controlLabelSize clsSmall = new controlLabelSize("small");
		/** <code>clsLarge</code> -  */
		public static final controlLabelSize clsLarge = new controlLabelSize("large");
		
        private controlLabelSize(String controlLabelSize) { super(controlLabelSize); }
        
        /**
         * @param controlLabelSize The name of the enum.
         * @return Returns the controlLabelSize
         */
        public static controlLabelSize getEnum(String controlLabelSize) {
	        return (controlLabelSize) getEnum(controlLabelSize.class, zxVar(controlLabelSize));
        }        
	}

	/**
	 * Mimic the enum for windowClose
	 */	
	public static final class windowClose extends Enum {
	    
		/** <code>wcOk</code> -  */
		public static final windowClose wcOk = new windowClose("ok");
		/** <code>wcCancel</code> - */
		public static final windowClose wcCancel = new windowClose("cancel");
		/** <code>wcClose</code> -  */
		public static final windowClose wcClose = new windowClose("close");
		/** <code>wcError</code> - */
		public static final windowClose wcError = new windowClose("error");
		
        private windowClose(String windowClose) { super(windowClose);}
        
        /**
         * @param windowClose The name of the enum.
         * @return Returns the windowClose
         */
        public static windowClose getEnum(String windowClose) {
	        return (windowClose) getEnum(windowClose.class, zxVar(windowClose));
        }        
	}

	/**
	 * Mimic the enum for layoutSchema
	 */	
	public static final class layoutSchema extends Enum {
	    
		/** <code>lsTop</code> -  */
		public static final layoutSchema lsTop = new layoutSchema("top");
		/** <code>lsBottom</code> -  */
		public static final layoutSchema lsBottom = new layoutSchema("bottom");
		/** <code>lsLeft</code> - */
		public static final layoutSchema lsLeft = new layoutSchema("left");
		/** <code>lsRight</code> - */
		public static final layoutSchema lsRight = new layoutSchema("right");
		/** <code>lsTopBottom</code> - */
		public static final layoutSchema lsTopBottom = new layoutSchema("topbottom");
		/** <code>lsLeftRight</code> -  */
		public static final layoutSchema lsLeftRight = new layoutSchema("leftright");
		/** <code>lsFixed</code> -  */
		public static final layoutSchema lsFixed = new layoutSchema("fixed");
		
        private layoutSchema(String layoutSchema) { super(layoutSchema); }
        
        /**
         * @param layoutSchema The name of the enum.
         * @return Returns the layoutSchema
         */
        public static layoutSchema getEnum(String layoutSchema) {
	        return (layoutSchema) getEnum(layoutSchema.class, zxVar(layoutSchema));
        }        
	}

	/**
	 * Mimic the enum for wordSection
	 */	
	public static class wordSection extends Enum {
	    
		/** <code>wsPage</code> -  */
		public static final wordSection wsPage = new wordSection("page");
		/** <code>wsFooter</code> -  */
		public static final wordSection wsFooter = new wordSection("footer");
		/**<code>wsHeader</code> - */
		public static final wordSection wsHeader = new wordSection("header");
		
        private wordSection(String wordSection) { super(wordSection); }
        
        /**
         * @param wordSection The name of the enum.
         * @return Returns the wordSection
         */
        public static wordSection getEnum(String wordSection) {
	        return (wordSection) getEnum(wordSection.class, zxVar(wordSection));
        }
	}
	
	/**
	 * Mimic the enum for deleteRule
	 */	
	public static final class deleteRule extends Enum{
	    
	    /** Position of the enum **/
	    public int pos;
	    
		/** <code>drAllowed</code> - You are allowed to delete this BO (0). */
		public static final deleteRule drAllowed = new deleteRule("allowed");
		/** <code>drNotAllowed</code> - You are not allowed to delete this BO (1). */
		public static final deleteRule drNotAllowed = new deleteRule("notallowed");
		/** <code>drCascade</code> - Whether to delete all of the related objects (2). */
		public static final deleteRule drCascade = new deleteRule("cascade");
		/** <code>drNoAssociates</code> - Only delete if there is no associates. (3). */
		public static final deleteRule drNoAssociates = new deleteRule("noassociates");
		/** <code>drPerRelation</code> - New deleterule. (4). */
		public static final deleteRule drPerRelation = new deleteRule("perrelation");
		
		static {
		    drAllowed.pos = 0;
		    drNotAllowed.pos = 1;
		    drCascade.pos = 2;
		    drNoAssociates.pos = 3;
		    drPerRelation.pos = 4;
		}
		
        private deleteRule(String deleteRule) { super(deleteRule); }
        
        /**
         * @param deleteRule The name of the enum.
         * @return Returns the deleteRule
         */
        public static deleteRule getEnum(String deleteRule) {
	        return (deleteRule) getEnum(deleteRule.class, zxVar(deleteRule));
        }
        
        /**
         * @param deleteRule The pos of the enum.
         * @return Returns the deleteRule
         */
        public static deleteRule getEnum(int deleteRule) {
            deleteRule getEnum;
            
            switch (deleteRule) {
                case 0:
                    getEnum = drAllowed;
                    break;
                case 1:
                    getEnum = drNotAllowed;
                    break;
                case 2:
                    getEnum = drCascade;
                    break;
                case 3:
                    getEnum = drNoAssociates;
                    break; 
                case 4:
                    getEnum = drPerRelation;
                    break;
                default:
                    getEnum = drAllowed;
                    break;
            }
            return getEnum;
        }
	}
	
	/**
	 * Mimic the enum for drctrNextToken.
	 * 
	 * This represents the type of the next token of a director.
	 */	
	public static final class drctrNextToken extends Enum{
	    
		/** <code>dntError</code> - Error in the resolving of the next element. */
		public static final drctrNextToken dntError = new drctrNextToken("error");
		/** <code>dntEndOfString</code> - End of the directors. */
		public static final drctrNextToken dntEndOfString = new drctrNextToken("endofstring");
		/** <code>dntFunction</code> - The next element is a function */
		public static final drctrNextToken dntFunction = new drctrNextToken("function");
		/** <code>dntSubDirector</code> - The next element is a sub director */
		public static final drctrNextToken dntSubDirector = new drctrNextToken("subdirector");
		/** <code>dntUnSpecified</code> - Unspecified director.*/
		public static final drctrNextToken dntUnSpecified = new drctrNextToken("unspecified");		
		
        private drctrNextToken(String drctrNextToken) { super(drctrNextToken); }
        
        /**
         * @param drctrNextToken The name of the enum.
         * @return Returns the drctrNextToken
         */
        public static drctrNextToken getEnum(String drctrNextToken) {
	        return (drctrNextToken) getEnum(drctrNextToken.class, zxVar(drctrNextToken));
        }
	}	
	
	/**
	 * Mimic the enum for queryDefType.
	 * 
	 * This represents the type of the next token of a director.
	 */	
	public static final class queryDefType extends Enum{
	    
		/** <code>qdtColumn</code> */
		public static final queryDefType qdtColumn = new queryDefType("column");
		/** <code>qdtDelete</code> */
		public static final queryDefType qdtDelete = new queryDefType("delete");
		/** <code>qdtDelete</code> */
		public static final queryDefType qdtInsert = new queryDefType("insert");
		/** <code>qdtUpdate</code> */
		public static final queryDefType qdtUpdate = new queryDefType("update");
		/** <code>qdtLiteral</code> */
		public static final queryDefType qdtLiteral = new queryDefType("literal");
		/** <code>qdtOrderBy</code> */
		public static final queryDefType qdtOrderBy = new queryDefType("orderby");
		/** <code>qdtSelect</code> */
		public static final queryDefType qdtSelect = new queryDefType("select");
		/** <code>qdtTable</code> */
		public static final queryDefType qdtTable = new queryDefType("table");
		/** <code>qdtWhereCondition</code> */
		public static final queryDefType qdtWhereClause = new queryDefType("whereclause");
		/** <code>qdtWhereCondition</code> */
		public static final queryDefType qdtWhereCondition = new queryDefType("wherecondition");
		/** <code>qdtValue</code> */
		public static final queryDefType qdtValue = new queryDefType("value");
		
        private queryDefType(String queryDefType) { super(queryDefType); }
        
        /**
         * @param queryDefType The name of the enum.
         * @return Returns the queryDefType
         */
        public static queryDefType getEnum(String queryDefType) {
	        return (queryDefType) getEnum(queryDefType.class, zxVar(queryDefType));
        }
	}		
	
	/**
	 * Mimic the enum for queryDefPageFlowScope.
	 */	
	public static final class queryDefPageFlowScope extends Enum{
	    
		/** <code>qdpfsWhere</code> */
		public static final queryDefPageFlowScope qdpfsSelect = new queryDefPageFlowScope("select");
		/** <code>qdpfsWhere</code> */
		public static final queryDefPageFlowScope qdpfsWhere = new queryDefPageFlowScope("where");
		/** <code>qdpfsOrderBy</code> */
		public static final queryDefPageFlowScope qdpfsOrderBy = new queryDefPageFlowScope("orderby");
		/** <code>qdpfsNotApplicable</code> */
		public static final queryDefPageFlowScope qdpfsNotApplicable = new queryDefPageFlowScope("notapplicable");
		
        private queryDefPageFlowScope(String queryDefPageFlowScope) {  super(queryDefPageFlowScope); }
        
        /**
         * @param queryDefPageFlowScope The name of the enum.
         * @return Returns the queryDefPageFlowScope
         */
        public static queryDefPageFlowScope getEnum(String queryDefPageFlowScope) {
	        return (queryDefPageFlowScope) getEnum(queryDefPageFlowScope.class, zxVar(queryDefPageFlowScope));
        }
	}
	
	/**
	 * Mimic the enum for processMonitorStatus.
	 */	
	public static final class processMonitorStatus extends Enum {
	    
	    /** <code>pos</code> -  The position of the enum. */
	    public int pos;
	    
		/** <code>pmsAutomatic</code> - 0 */
		public static final processMonitorStatus pmsAutomatic = new processMonitorStatus("automatic");
		/** <code>pmsRun</code> - 1 */
		public static final processMonitorStatus pmsRun = new processMonitorStatus("run");
		/** <code>pmsPause</code> - 2 */
		public static final processMonitorStatus pmsPause = new processMonitorStatus("pause");
		/** <code>pmsStop</code> - 3 */
		public static final processMonitorStatus pmsStop = new processMonitorStatus("stop");
		/** <code>pmsStart</code> - 4 */
		public static final processMonitorStatus pmsStart = new processMonitorStatus("start");
		/** <code>pmsKill</code> - 5 */
		public static final processMonitorStatus pmsKill = new processMonitorStatus("kill");
		
        private processMonitorStatus(String processMonitorStatus) { super(processMonitorStatus); }
        
	    static {
	    	pmsAutomatic.pos = 0;
	    	pmsRun.pos = 1;
	    	pmsPause.pos = 2;
	    	pmsStop.pos = 3;
	    	pmsStart.pos = 4;
	    	pmsKill.pos = 5;
	    }
	    
        /**
         * @param processMonitorStatus The name of the enum.
         * @return Returns the processMonitorStatus
         */
        public static processMonitorStatus getEnum(String processMonitorStatus) {
	        return (processMonitorStatus) getEnum(processMonitorStatus.class, zxVar(processMonitorStatus));
        }
        
	    /**
	     * @param pintProcessMonitorStatus The pos of the enum.
	     * @return Returns the datatype
	     */
	    public static processMonitorStatus getEnum(int pintProcessMonitorStatus) {
	    	switch (pintProcessMonitorStatus) {
			case 0:
				return pmsAutomatic;
			case 1:
				return pmsRun;
			case 2:
				return pmsPause;
			case 3:
				return pmsStop;
			case 4:
				return pmsStart;
			case 5:
				return pmsKill;	
				
			default:
				return pmsAutomatic;
			}
	    }
	}
	
	/**
	 * Mimic the enum for pageflowEditFormType.
	 */	
	public static final class pageflowEditFormType extends Enum{
	    
		/** <code>pfeftNormal</code> */
		public static final pageflowEditFormType pfeftNormal = new pageflowEditFormType("normal");
		/** <code>pfeftColumns</code> */
		public static final pageflowEditFormType pfeftColumns = new pageflowEditFormType("columns");
		/** <code>pfeftTabs</code> */
		public static final pageflowEditFormType pfeftTabs = new pageflowEditFormType("tabs");
		
        private pageflowEditFormType(String pageflowEditFormType) { super(pageflowEditFormType); }
        
        /**
         * @param pageflowEditFormType The name of the enum.
         * @return Returns the pageflowEditFormType
         */
        public static pageflowEditFormType getEnum(String pageflowEditFormType) {
	        return (pageflowEditFormType) getEnum(pageflowEditFormType.class, zxVar(pageflowEditFormType));
        }
	}
	
	/**
	 * Mimic the enum for pageflowDependencyOperator.
	 * 
	 * An exception to the rule.
	 */	
	public static final class pageflowDependencyOperator extends Enum{
	    
		/** <code>pdoEQ</code> */
		public static final pageflowDependencyOperator pdoEQ = new pageflowDependencyOperator("eq");
		/** <code>pdoNE</code> */
		public static final pageflowDependencyOperator pdoNE = new pageflowDependencyOperator("ne");
		/** <code>pdoGT</code> */
		public static final pageflowDependencyOperator pdoGT = new pageflowDependencyOperator("gt");
		/** <code>pdoGE</code> */
		public static final pageflowDependencyOperator pdoGE = new pageflowDependencyOperator("ge");
		/** <code>pdoLT</code> */
		public static final pageflowDependencyOperator pdoLT = new pageflowDependencyOperator("lt");
		/** <code>pdoLE</code> */
		public static final pageflowDependencyOperator pdoLE = new pageflowDependencyOperator("le");
		/** <code>pdoNull</code> */
		public static final pageflowDependencyOperator pdoNull = new pageflowDependencyOperator("null");
		/** <code>pdoNotNull</code> */
		public static final pageflowDependencyOperator pdoNotNull = new pageflowDependencyOperator("notnull");
		
        private pageflowDependencyOperator(String pageflowDependencyOperator) { super(pageflowDependencyOperator); }
        
        /**
         * @param pageflowDependencyOperator The name of the enum.
         * @return Returns the pageflowEditFormType
         */
        public static pageflowDependencyOperator getEnum(String pageflowDependencyOperator) {
	        return (pageflowDependencyOperator) getEnum(pageflowDependencyOperator.class, zxVar(pageflowDependencyOperator));
        }
        
        /**
         * @param pageflowDependencyOperator The name of the enum.
         * @return Returns the pageflowEditFormType
         */
        public static pageflowDependencyOperator getEnum(int pageflowDependencyOperator) {
            pageflowDependencyOperator getEnum;
            switch (pageflowDependencyOperator) {
                case 0:
                    getEnum = pdoEQ;
                    break;
                case 1:
                    getEnum = pdoNE;
                    break;
                case 2:
                    getEnum = pdoGT;
                    break;
                case 3:
                    getEnum = pdoGE;
                    break;
                case 4:
                    getEnum = pdoLT;
                    break;
                case 5:
                    getEnum = pdoLE;
                    break;
                case 6:
                    getEnum = pdoNull;
                    break;
                case 7:
                    getEnum = pdoNotNull;
                    break;
                default:
                    getEnum = pdoEQ;
                    break;
            }
            
            return getEnum;
        }
	}
	
	/**
	 * Mimic the enum for pageflowDependencyType.
	 */	
	public static final class pageflowDependencyType extends Enum{
	    
		/** <code>pdtDisable</code> - */
		public static final pageflowDependencyType pdtDisable = new pageflowDependencyType("disable");
		/** <code>pdtDisableBlank</code> - */
		public static final pageflowDependencyType pdtDisableBlank = new pageflowDependencyType("disableblank");
		/** <code>pdtSet</code> - */
		public static final pageflowDependencyType pdtSet = new pageflowDependencyType("set");
		/** <code>pdtBound</code> - */
		public static final pageflowDependencyType pdtBound = new pageflowDependencyType("bound");
		/** <code>pdtRestrict</code> - */
		public static final pageflowDependencyType pdtRestrict = new pageflowDependencyType("restrict");
		
        private pageflowDependencyType(String pageflowDependencyType) { super(pageflowDependencyType); }
        
        /**
         * @param pageflowDependencyType The name of the enum.
         * @return Returns the pageflowDependencyType
         */
        public static pageflowDependencyType getEnum(String pageflowDependencyType) {
	        return (pageflowDependencyType) getEnum(pageflowDependencyType.class, zxVar(pageflowDependencyType));
        }
	}
	
	/**
	 * Mimic the enum for pageflowQueryExprOperator.
	 */	
	public static final class pageflowQueryExprOperator extends Enum{
	    
        /** Position of the enum **/
        public int pos;
        
		/** <code>pqeoEQ</code> - 0 */
		public static final pageflowQueryExprOperator pqeoEQ = new pageflowQueryExprOperator("eq");
		/** <code>pqeoNE</code> - 1 */
		public static final pageflowQueryExprOperator pqeoNE = new pageflowQueryExprOperator("ne");
		/** <code>pqeoGT</code> - 2 */
		public static final pageflowQueryExprOperator pqeoGT = new pageflowQueryExprOperator("gt");
		/** <code>pqeoGE</code> - 3 */
		public static final pageflowQueryExprOperator pqeoGE = new pageflowQueryExprOperator("ge");
		/** <code>pqeoLT</code> - 4 */
		public static final pageflowQueryExprOperator pqeoLT = new pageflowQueryExprOperator("lt");
		/** <code>pqeoLE</code> - 5 */
		public static final pageflowQueryExprOperator pqeoLE = new pageflowQueryExprOperator("le");
		/** <code>pqeoSW</code> - 6 */
		public static final pageflowQueryExprOperator pqeoSW = new pageflowQueryExprOperator("sw");
		/** <code>pqeoCNT</code> - 7 */
		public static final pageflowQueryExprOperator pqeoCNT = new pageflowQueryExprOperator("cnt");
		/** <code>pqeoAND</code> - 8 */
		public static final pageflowQueryExprOperator pqeoAND = new pageflowQueryExprOperator("and");
		/** <code>pqeoOR</code> - 9 */
		public static final pageflowQueryExprOperator pqeoOR = new pageflowQueryExprOperator("or");
		/** <code>pqeoNULL</code> - 10 */
		public static final pageflowQueryExprOperator pqeoNULL = new pageflowQueryExprOperator("isnull");
		/** <code>pqeoNOTNULL</code> - 11 */
		public static final pageflowQueryExprOperator pqeoNOTNULL = new pageflowQueryExprOperator("notnull");
        
        static {
            pqeoEQ.pos = 0;
            pqeoNE.pos = 1;
            pqeoGT.pos = 2;
            pqeoGE.pos = 3;
            pqeoLT.pos = 4;
            pqeoLE.pos = 5;
            pqeoSW.pos = 6;
            pqeoCNT.pos = 7;
            pqeoAND.pos = 8;
            pqeoOR.pos = 9;
            pqeoNULL.pos = 10;
            pqeoNOTNULL.pos = 11;
        }
        
        private pageflowQueryExprOperator(String pageflowQueryExprOperator) { super(pageflowQueryExprOperator); }
        
        /**
         * @param pageflowQueryExprOperator The name of the enum.
         * @return Returns the pageflowDependencyType
         */
        public static pageflowQueryExprOperator getEnum(String pageflowQueryExprOperator) {
	        return (pageflowQueryExprOperator) getEnum(pageflowQueryExprOperator.class, zxVar(pageflowQueryExprOperator));
        }
        
        /**
         * @param pageflowQueryExprOperator
         * @return Returns a stirng usable for sql statements.
         */
        public static String getStringValue(pageflowQueryExprOperator pageflowQueryExprOperator) {
            String getStringValue = "";
            int intpageflowQueryExprOperator = pageflowQueryExprOperator.pos;
            
            if (intpageflowQueryExprOperator == pqeoEQ.pos) {
                getStringValue = "=";
            } else if (intpageflowQueryExprOperator == pqeoNE.pos){
                getStringValue = "<>";
            } else if (intpageflowQueryExprOperator == pqeoGT.pos){
                getStringValue = ">";
            } else if (intpageflowQueryExprOperator == pqeoGE.pos){
                getStringValue = ">=";
            } else if (intpageflowQueryExprOperator == pqeoLT.pos){
                getStringValue = "<";
            } else if (intpageflowQueryExprOperator == pqeoLE.pos){
                getStringValue = "<=";
            } else if (intpageflowQueryExprOperator == pqeoCNT.pos){
                getStringValue = "LIKE";
            } else if (intpageflowQueryExprOperator == pqeoSW.pos){
                getStringValue = "LIKE";
            } else if (intpageflowQueryExprOperator == pqeoOR.pos){
                getStringValue = "OR";
            } else if (intpageflowQueryExprOperator == pqeoAND.pos){
                getStringValue = "AND";
            } else if (intpageflowQueryExprOperator == pqeoNULL.pos){
                getStringValue = "IS NULL";
            } else if (intpageflowQueryExprOperator == pqeoNOTNULL.pos){
                getStringValue = "IS NOT NULL";
            }
            return getStringValue;
        }
	}
	
	/**
	 * Mimic the enum for pageflowQueryExprMode.
	 */	
	public static final class pageflowQueryExprMode extends Enum{
	    
		/** <code>pqemRun</code> - */
		public static final pageflowQueryExprMode pqemRun = new pageflowQueryExprMode("run");
		/** <code>pqemDesign</code> - */
		public static final pageflowQueryExprMode pqemDesign = new pageflowQueryExprMode("design");
		/** <code>pqemXML</code> - */
		public static final pageflowQueryExprMode pqemXML = new pageflowQueryExprMode("xml");
		
        private pageflowQueryExprMode(String pageflowQueryExprMode) { super(pageflowQueryExprMode); }
        
        /**
         * @param pageflowQueryExprMode The name of the enum.
         * @return Returns the pageflowDependencyType
         */
        public static pageflowQueryExprMode getEnum(String pageflowQueryExprMode) {
	        return (pageflowQueryExprMode) getEnum(pageflowQueryExprMode.class, zxVar(pageflowQueryExprMode));
        }
	}

	/**
	 * Mimic the enum for docBuilderEngineType.
	 */	
	public static final class docBuilderEngineType extends Enum{
	    
		/** <code>pqemRun</code> - */
		public static final docBuilderEngineType dbetWord9 = new docBuilderEngineType("word9");
		
        private docBuilderEngineType(String docBuilderEngineType) { super(docBuilderEngineType); }
        
        /**
         * @param docBuilderEngineType The name of the enum.
         * @return Returns the docBuilderEngineType
         */
        public static docBuilderEngineType getEnum(String docBuilderEngineType) {
	        return (docBuilderEngineType) getEnum(docBuilderEngineType.class, zxVar(docBuilderEngineType));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderEngineType.
	 */	
	public static final class docBuilderActionType extends Enum{
        
        /** <code>pos</code> - The position of the enum. */
        public int pos;
		
		/** 
		 * <code>dbatNull</code> - Null docbuilder action.
		 * Position : 0 
		 **/
		public static final docBuilderActionType dbatNull = new docBuilderActionType("null");
		
		/** 
		 * <code>dbatQuery</code> - Query doc builder action.
		 *  Position : 1
		 **/
		public static final docBuilderActionType dbatQuery = new docBuilderActionType("query");
		
		/** 
		 * <code>dbatBOMerge</code> - BO Merge doc builder action.
		 * Position : 2 
		 **/
		public static final docBuilderActionType dbatBOMerge = new docBuilderActionType("bomerge");
		
		/** 
		 * <code>dbatMerge</code> - Merge doc builder action.
		 * Position : 3 
		 **/
		public static final docBuilderActionType dbatMerge = new docBuilderActionType("merge");
		
		/** 
		 * <code>dbatBO2Table</code> - BO2Table doc builder action.
		 * Position : 4
		 **/
		public static final docBuilderActionType dbatBO2Table = new docBuilderActionType("bo2table");
		
		/** 
		 * <code>dbatBO2Grid</code> - BO2Grid doc builder action.
		 * Position : 5
		 **/
		public static final docBuilderActionType dbatBO2Grid = new docBuilderActionType("bo2grid");
		
		/** 
		 * <code>dbatAddObject</code> - Add object doc builder action.
		 * Position : 6 
		 **/
		public static final docBuilderActionType dbatAddObject = new docBuilderActionType("addobject");
		
		/** 
		 * <code>dbatLoopOver</code> - Loopover doc builder action.
		 *  Position : 7
		 **/
		public static final docBuilderActionType dbatLoopOver = new docBuilderActionType("loopover");
		
		/** 
		 * <code>dbatEmbed</code> - Embed doc builder action
		 * Position : 8
		 **/
		public static final docBuilderActionType dbatEmbed = new docBuilderActionType("embed");
		
		/** 
		 * <code>dbatGridMerge</code> - Grid merge doc builder action
		 * Position : 9
		 **/
		public static final docBuilderActionType dbatGridMerge = new docBuilderActionType("gridmerge");
		
		static {
			dbatNull.pos = 0;
			dbatQuery.pos = 1;
			dbatBOMerge.pos = 2;
			dbatMerge.pos = 3;
			dbatBO2Table.pos = 4;
			dbatBO2Grid.pos = 5;
			dbatAddObject.pos = 6;
			dbatLoopOver.pos = 7;
			dbatEmbed.pos = 8;
			dbatGridMerge.pos = 9;
		}
		
        private docBuilderActionType(String docBuilderActionType) { super(docBuilderActionType); }
        
        /**
         * @param docBuilderActionType The name of the enum.
         * @return Returns the docBuilderActionType
         */
        public static docBuilderActionType getEnum(String docBuilderActionType) {
	        return (docBuilderActionType) getEnum(docBuilderActionType.class, zxVar(docBuilderActionType));
        }
	}
	
	/**
	 * Mimic the enum for docBuilderEntityRefMethod.
	 */	
	public static final class docBuilderEntityRefMethod extends Enum{
	    
		/** <code>dbermPK</code> - */
		public static final docBuilderEntityRefMethod dbermPK = new docBuilderEntityRefMethod("pk");
		/** <code>dbermPKGroup</code> - */
		public static final docBuilderEntityRefMethod dbermPKGroup = new docBuilderEntityRefMethod("pkgroup");
		/** <code>dbermContext</code> - */
		public static final docBuilderEntityRefMethod dbermContext = new docBuilderEntityRefMethod("context");
		
        private docBuilderEntityRefMethod(String docBuilderEntityRefMethod) { super(docBuilderEntityRefMethod); }
        
        /**
         * @param docBuilderEntityRefMethod The name of the enum.
         * @return Returns the docBuilderEntityRefMethod
         */
        public static docBuilderEntityRefMethod getEnum(String docBuilderEntityRefMethod) {
	        return (docBuilderEntityRefMethod) getEnum(docBuilderEntityRefMethod.class, zxVar(docBuilderEntityRefMethod));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderObjectrefMethod.
	 */	
	public static final class docBuilderObjectrefMethod extends Enum{
	    
		/** <code>dbormLastUsed</code> - */
		public static final docBuilderObjectrefMethod dbormLastUsed = new docBuilderObjectrefMethod("lastused");
		/** <code>dbormByNumber</code> - */
		public static final docBuilderObjectrefMethod dbormByNumber = new docBuilderObjectrefMethod("bynumber");
		/** <code>dbormByName</code> - */
		public static final docBuilderObjectrefMethod dbormByName = new docBuilderObjectrefMethod("byname");
		/** <code>dbormLast</code> - */
		public static final docBuilderObjectrefMethod dbormLast = new docBuilderObjectrefMethod("last");
		
        private docBuilderObjectrefMethod(String docBuilderObjectrefMethod) { super(docBuilderObjectrefMethod); }
        
        /**
         * @param docBuilderObjectrefMethod The name of the enum.
         * @return Returns the docBuilderObjectrefMethod
         */
        public static docBuilderObjectrefMethod getEnum(String docBuilderObjectrefMethod) {
	        return (docBuilderObjectrefMethod) getEnum(docBuilderObjectrefMethod.class, zxVar(docBuilderObjectrefMethod));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderPagePart.
	 */	
	public static final class docBuilderPagePart extends Enum{
	    
		/** <code>dbppHeader</code> - */
		public static final docBuilderPagePart dbppHeader = new docBuilderPagePart("header");
		/** <code>dbppBody</code> - */
		public static final docBuilderPagePart dbppBody = new docBuilderPagePart("body");
		/** <code>dbppFooter</code> - */
		public static final docBuilderPagePart dbppFooter = new docBuilderPagePart("footer");
		
        private docBuilderPagePart(String docBuilderPagePart) { super(docBuilderPagePart); }
        
        /**
         * @param docBuilderPagePart The name of the enum.
         * @return Returns the docBuilderPagePart
         */
        public static docBuilderPagePart getEnum(String docBuilderPagePart) {
	        return (docBuilderPagePart) getEnum(docBuilderPagePart.class, zxVar(docBuilderPagePart));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderQueryType.
	 */	
	public static final class docBuilderQueryType extends Enum{
	    
		/** <code>dbqtAll</code> - */
		public static final docBuilderQueryType dbqtAll = new docBuilderQueryType("all");
		/** <code>dbqtQueryDef</code> - */
		public static final docBuilderQueryType dbqtQueryDef = new docBuilderQueryType("querydef");
		/** <code>dbqtAssociatedWith</code> - */
		public static final docBuilderQueryType dbqtAssociatedWith = new docBuilderQueryType("associatedwith");
		/** <code>dbqtNotAssociatedWith</code> - */
		public static final docBuilderQueryType dbqtNotAssociatedWith = new docBuilderQueryType("notassociatedwith");
		
        private docBuilderQueryType(String docBuilderQueryType) { super(docBuilderQueryType); }
        
        /**
         * @param docBuilderQueryType The name of the enum.
         * @return Returns the docBuilderQueryType
         */
        public static docBuilderQueryType getEnum(String docBuilderQueryType) {
	        return (docBuilderQueryType) getEnum(docBuilderQueryType.class, zxVar(docBuilderQueryType));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderGridType.
	 */	
	public static final class docBuilderGridType extends Enum{
	    
		/** <code>dbgtBespoke</code> - */
		public static final docBuilderGridType dbgtBespoke = new docBuilderGridType("bespoke");
		/** <code>dbgtStandardBO</code> - */
		public static final docBuilderGridType dbgtStandardBO = new docBuilderGridType("standardbo");
		
        private docBuilderGridType(String docBuilderGridType) { super(docBuilderGridType); }
        
        /**
         * @param docBuilderGridType The name of the enum.
         * @return Returns the docBuilderGridType
         */
        public static docBuilderGridType getEnum(String docBuilderGridType) {
	        return (docBuilderGridType) getEnum(docBuilderGridType.class, zxVar(docBuilderGridType));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderDisposition.
	 */	
	public static final class docBuilderDisposition extends Enum{
	    
		/** <code>dbpbNone</code> - */
		public static final docBuilderDisposition dbdpAfter = new docBuilderDisposition("after");
		/** <code>dbpbBefore</code> - */
		public static final docBuilderDisposition dbdpBefore = new docBuilderDisposition("before");
		/** <code>dbpbAfter</code> - */
		public static final docBuilderDisposition dbdpReplace = new docBuilderDisposition("replace");
		
        private docBuilderDisposition(String docBuilderDisposition) { super(docBuilderDisposition); }
        
        /**
         * @param docBuilderDisposition The name of the enum.
         * @return Returns the docBuilderDisposition
         */
        public static docBuilderDisposition getEnum(String docBuilderDisposition) {
	        return (docBuilderDisposition) getEnum(docBuilderDisposition.class, zxVar(docBuilderDisposition));
        }
	}	
	
	/**
	 * Mimic the enum for docBuilderPageBreak.
	 */	
	public static final class docBuilderPageBreak extends Enum{
	    
		/** <code>dbpbNone</code> - */
		public static final docBuilderPageBreak dbpbNone = new docBuilderPageBreak("none");
		/** <code>dbpbBefore</code> - */
		public static final docBuilderPageBreak dbpbBefore = new docBuilderPageBreak("before");
		/** <code>dbpbAfter</code> - */
		public static final docBuilderPageBreak dbpbAfter = new docBuilderPageBreak("after");
		
        private docBuilderPageBreak(String docBuilderPageBreak) { super(docBuilderPageBreak); }
        
        /**
         * @param docBuilderPageBreak The name of the enum.
         * @return Returns the docBuilderPageBreak
         */
        public static docBuilderPageBreak getEnum(String docBuilderPageBreak) {
	        return (docBuilderPageBreak) getEnum(docBuilderPageBreak.class, zxVar(docBuilderPageBreak));
        }
	}
	
	/**
	 * Mimic the enum for treeOpenMode.
	 */	
	public static final class treeOpenMode extends Enum{
	    
		/** <code>tomClosed</code> - */
		public static final treeOpenMode tomClosed = new treeOpenMode("closed");
		/** <code>tomOpenPath</code> - */
		public static final treeOpenMode tomOpenPath = new treeOpenMode("openpath");
		/** <code>tomOpenAllChildren</code> - */
		public static final treeOpenMode tomOpenAllChildren = new treeOpenMode("openallchildren");
		/** <code>tomOpenLevel</code> - */
		public static final treeOpenMode tomOpenLevel = new treeOpenMode("openlevel");
		
        private treeOpenMode(String treeOpenMode) { super(treeOpenMode); }
        
        /**
         * @param treeOpenMode The name of the enum.
         * @return Returns the treeOpenMode
         */
        public static treeOpenMode getEnum(String treeOpenMode) {
	        return (treeOpenMode) getEnum(treeOpenMode.class, zxVar(treeOpenMode));
        }
	}	
	
	/**
	 * Mimic the enum for pageflowQueryWhereClause.
	 */	
	public static final class pageflowQueryWhereClause extends Enum{
	    
		/** <code>pqwcStandard</code> - */
		public static final pageflowQueryWhereClause pqwcStandard = new pageflowQueryWhereClause("standard");
		/** <code>pqwcAnd</code> - */
		public static final pageflowQueryWhereClause pqwcAnd = new pageflowQueryWhereClause("and");
		/** <code>pqwcOr</code> - */
		public static final pageflowQueryWhereClause pqwcOr = new pageflowQueryWhereClause("or");
		/** <code>pqwcReplace</code> - */
		public static final pageflowQueryWhereClause pqwcReplace = new pageflowQueryWhereClause("replace");
		
        private pageflowQueryWhereClause(String pageflowQueryWhereClause) { super(pageflowQueryWhereClause); }
        
        /**
         * @param pageflowQueryWhereClause The name of the enum.
         * @return Returns the pageflowQueryWhereClause
         */
        public static pageflowQueryWhereClause getEnum(String pageflowQueryWhereClause) {
	        return (pageflowQueryWhereClause) getEnum(pageflowQueryWhereClause.class, zxVar(pageflowQueryWhereClause));
        }
	}		
	
	/**
	 * Mimic the enum for eaTiming.
	 */	
	public static final class eaTiming extends Enum{
	    
		/** <code>eatPre</code> - */
		public static final eaTiming eatPre = new eaTiming("pre");
		/** <code>eatPost</code> - */
		public static final eaTiming eatPost = new eaTiming("post");
		
        private eaTiming(String eaTiming) { super(eaTiming); }
        
        /**
         * @param eaTiming The name of the enum.
         * @return Returns the eaTiming
         */
        public static eaTiming getEnum(String eaTiming) {
	        return (eaTiming) getEnum(eaTiming.class, zxVar(eaTiming));
        }
	}		
	
	/**
	 * Mimic the enum for eaGroupBehaviour.
	 */	
	public static final class eaGroupBehaviour extends Enum{
	    
		/** <code>eagbAll</code> - */
		public static final eaGroupBehaviour eagbAll = new eaGroupBehaviour("all");
		/** <code>eagbSome</code> - */
		public static final eaGroupBehaviour eagbSome = new eaGroupBehaviour("some");
		/** <code>eagbNone</code> - */
		public static final eaGroupBehaviour eagbNone = new eaGroupBehaviour("none");		
		/** <code>eagbOnly</code> - */
		public static final eaGroupBehaviour eagbOnly = new eaGroupBehaviour("only");
		
        private eaGroupBehaviour(String eaGroupBehaviour) { super(eaGroupBehaviour); }
        
        /**
         * @param eaGroupBehaviour The name of the enum.
         * @return Returns the eaTiming
         */
        public static eaGroupBehaviour getEnum(String eaGroupBehaviour) {
	        return (eaGroupBehaviour) getEnum(eaGroupBehaviour.class, zxVar(eaGroupBehaviour));
        }
	}	
	
	/**
	 * Mimic the enum for eaIDBehaviour.
	 */	
	public static final class eaIDBehaviour extends Enum{
	    
		/** <code>eaibMatch</code> - */
		public static final eaIDBehaviour eaibMatch = new eaIDBehaviour("match");
		/** <code>eaibContains</code> - */
		public static final eaIDBehaviour eaibContains = new eaIDBehaviour("contains");
		/** <code>eaibStartsWith</code> - */
		public static final eaIDBehaviour eaibStartsWith = new eaIDBehaviour("startswith");
		/** <code>eaibContains</code> - */
		public static final eaIDBehaviour eaibDoesNotContain = new eaIDBehaviour("doesnotcontain");
		/** <code>eaibContains</code> - */
		public static final eaIDBehaviour eaibRegExp = new eaIDBehaviour("regexp");
		
        private eaIDBehaviour(String eaIDBehaviour) { super(eaIDBehaviour); }
        
        /**
         * @param eaIDBehaviour The name of the enum.
         * @return Returns the eaIDBehaviour
         */
        public static eaIDBehaviour getEnum(String eaIDBehaviour) {
	        return (eaIDBehaviour) getEnum(eaIDBehaviour.class, zxVar(eaIDBehaviour));
        }
	}	
	
	/**
	 * Mimic the enum for eaContinuation.
	 */	
	public static final class eaContinuation extends Enum{
	    
		/** <code>eacContinue</code> - */
		public static final eaContinuation eacContinue = new eaContinuation("continue");
		/** <code>eacStopWhenFired</code> - */
		public static final eaContinuation eacStopWhenFired = new eaContinuation("stopwhenfired");
		/** <code>eacStopWhenNotFired</code> - */
		public static final eaContinuation eacStopWhenNotFired = new eaContinuation("stopwhennotfired");
		
        private eaContinuation(String eaContinuation) { super(eaContinuation); }
        
        /**
         * @param eaContinuation The name of the enum.
         * @return Returns the eaContinuation
         */
        public static eaContinuation getEnum(String eaContinuation) {
	        return (eaContinuation) getEnum(eaContinuation.class, zxVar(eaContinuation));
        }
	}

	
	/**
	 * Mimic the enum for pageflowCellMode.
	 */	
	public static final class pageflowCellMode extends Enum{
	    
		/** 
		 * <code>pcmList</code> -List mode.
		 * Postion : 0 
		 **/
		public static final pageflowCellMode pcmList = new pageflowCellMode("list");
		/** 
		 * <code>pcmCategory </code> - Category mode. 
		 * Postion : 1
		 **/
		public static final pageflowCellMode pcmCategory  = new pageflowCellMode("category");
		/** 
		 * <code>pcmPopup </code> - Popup mode. 
		 * Postion : 2
		 **/
		public static final pageflowCellMode pcmPopup = new pageflowCellMode("popup");
		
        private pageflowCellMode(String pageflowCellMode) { super(pageflowCellMode); }
        
        /**
         * @param pageflowCellMode The name of the enum.
         * @return Returns the pageflowCellMode
         */
        public static pageflowCellMode getEnum(String pageflowCellMode) {
	        return (pageflowCellMode) getEnum(pageflowCellMode.class, pageflowCellMode);
        }
	}	
	
	/**
	 * Mimic the enum for displayOrientation.
	 */	
	public static final class displayOrientation extends Enum{
	    
		/** <code>doVertical</code> - Vertical mode (0). */
		public static final displayOrientation doVertical = new displayOrientation("vertical");
		/** <code>doHorizontal </code> - Horizontal mode (1). */
		public static final displayOrientation doHorizontal = new displayOrientation("horizontal");
		
        private displayOrientation(String displayOrientation) { super(displayOrientation); }
        
        /**
         * @param displayOrientation The name of the enum.
         * @return Returns the displayOrientation
         */
        public static displayOrientation getEnum(String displayOrientation) {
	        return (displayOrientation) getEnum(displayOrientation.class, displayOrientation);
        }
	}
	
	/**
	 * Mimic the enum for pageflowLayout.
	 * This is used to determince the layout of cells.
	 */	
	public static final class pageflowLayout extends Enum{
	    
		/** 
		 * <code>plOne</code> - One cell only. 
		 * Postion : 0 
		 **/
		public static final pageflowLayout plOne = new pageflowLayout("one");
		/** 
		 * <code>plTwo</code> - Two cells next to each other. 
		 * Postion : 1
		 **/
		public static final pageflowLayout plTwo = new pageflowLayout("two");
		/** 
		 * <code>plThree</code> - Two cells one underneath the other. 
		 * Postion : 2 
		 **/
		public static final pageflowLayout plThree = new pageflowLayout("three");
		/** 
		 * <code>plFour</code> - Three cells. One long one and two next to it. 
		 * Postion : 3 
		 **/
		public static final pageflowLayout plFour = new pageflowLayout("four");
		/** 
		 * <code>plFive</code> - Three cells. Two cells and then one long one next to it.
		 * Postion : 4 
		 **/
		public static final pageflowLayout plFive = new pageflowLayout("five");
		/** 
		 * <code>plSix</code> - Four cells iin a box. 
		 * Postion : 5 
		 **/
		public static final pageflowLayout plSix = new pageflowLayout("six");
		/** 
		 * <code>plTab</code> - Tabbed layout like a editform with tabs. 
		 * Postion : 6 
		 **/
		public static final pageflowLayout plTab = new pageflowLayout("tab");
		/** 
		 * <code>plTabv</code> - Tabbed layout like a editform with tabs but vertical. 
		 * Postion : 7 
		 * */
		public static final pageflowLayout plTabv = new pageflowLayout("tabv");
		/** 
		 * <code>plTabv</code> - A flexible layout that allows the user to define a template file. 
		 * Postion : 8
		 **/
		public static final pageflowLayout plTemplate = new pageflowLayout("template");
		
        private pageflowLayout(String pageflowLayout) { super(pageflowLayout); }
        
        /**
         * @param pageflowLayout The name of the enum.
         * @return Returns the pageflowLayout
         */
        public static pageflowLayout getEnum(String pageflowLayout) {
	        return (pageflowLayout) getEnum(pageflowLayout.class, pageflowLayout);
        }
	}	
    
    //------------------------------------------- New Datasource specific enums
    
    /**
     * Mimic the enum for dsType.
     * This is used to determine the type of datasource used.
     */ 
    public static final class dsType extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>dstPrimary</code> - This is a primary datasource which has to be a database. 
         * Postion :  0 
         **/
        public static final dsType dstPrimary = new dsType("primary");
        /** 
         * <code>dstAlternative</code> - An alternative datasource. 
         * Postion : 1
         **/
        public static final dsType dstAlternative  = new dsType("alternative");
        /** 
         * <code>dstChannel</code> - A channel datasource. 
         * Postion : 2 
         * */
        public static final dsType dstChannel = new dsType("channel");
        
        private dsType(String dsType) { super(dsType); }
        
        static {
            dstPrimary.pos = 0;
            dstAlternative.pos = 1;
            dstChannel.pos = 2;
        }
        
        /**
         * @param dsType The name of the enum.
         * @return Returns the dsType
         */
        public static dsType getEnum(String dsType) {
            return (dsType)getEnum(dsType.class, dsType);
        }
    }    
    
    /**
     * Mimic the enum for dsRSType.
     * This is used to determine the result type used.
     */ 
    public static final class dsRSType extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** <code>dsrstStream</code> - Stream resultset. Neither data nor rs available, can only moveNext. 0 */
        public static final dsRSType dsrstStream = new dsRSType("stream");
        /** <code>dsrstCollection</code> - A collection resultset. Data contains result set. 1*/
        public static final dsRSType dsrstCollection  = new dsRSType("collection");
        /** <code>dsrstRS</code> - Database resultset. Result set available as rs. 2 */
        public static final dsRSType dsrstRS = new dsRSType("rs");
        
        private dsRSType(String dsRSType) { super(dsRSType); }
        
        static {
            dsrstStream.pos = 0;
            dsrstCollection.pos = 1;
            dsrstRS.pos = 2;
        }
        
        /**
         * @param dsRSType The name of the enum.
         * @return Returns the dsType
         */
        public static dsRSType getEnum(String dsRSType) {
            return (dsRSType) getEnum(dsRSType.class, dsRSType);
        }
    } 
    
    /**
     * Mimic the enum for dsTxSupport.
     * This is used to determine transaction support.
     */ 
    public static final class dsTxSupport extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** <code>dstxsNone</code> - No support for transactions. Has no tx support. 0 */
        public static final dsTxSupport dstxsNone = new dsTxSupport("none");
        /** <code>dstxsLocal</code> - Localised support for transactions. Has local tx support (ie not hand-shaked). 1*/
        public static final dsTxSupport dstxsLocal  = new dsTxSupport("local");
        /** <code>dstxsGlobal</code> - Global and nested transaction support. Piggy-back rides on global transaction monitor. 2 */
        public static final dsTxSupport dstxsGlobal = new dsTxSupport("global");
        
        private dsTxSupport(String dsTxSupport) { super(dsTxSupport); }
        
        static {
            dstxsNone.pos = 0;
            dstxsLocal.pos = 1;
            dstxsGlobal.pos = 2;
        }
        
        /**
         * @param dsTxSupport The name of the enum.
         * @return Returns the dsType
         */
        public static dsTxSupport getEnum(String dsTxSupport) {
            return (dsTxSupport) getEnum(dsTxSupport.class, dsTxSupport);
        }
    } 
    
    /**
     * Mimic the enum for dsWhereConditionOperator.
     * This is used to determine where clauses operator.
     */ 
    public static final class dsWhereConditionOperator extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>dswcoNone</code> - No operator. 
         * Position : 0 
         **/
        public static final dsWhereConditionOperator dswcoNone = new dsWhereConditionOperator("none");
        /** 
         * <code>dswcoAnd</code> - And operator. 
         * Position : 1
         **/
        public static final dsWhereConditionOperator dswcoAnd  = new dsWhereConditionOperator("and");
        /** 
         * <code>dswcoOr</code> - Or operator. 
         * Position : 2 
         **/
        public static final dsWhereConditionOperator dswcoOr = new dsWhereConditionOperator("or");
        
        private dsWhereConditionOperator(String dsWhereConditionOperator) { super(dsWhereConditionOperator); }
        
        static {
            dswcoNone.pos = 0;
            dswcoAnd.pos = 1;
            dswcoOr.pos = 2;
        }
        
        /**
         * @param dsWhereConditionOperator The name of the enum.
         * @return Returns the dsType
         */
        public static dsWhereConditionOperator getEnum(String dsWhereConditionOperator) {
            return (dsWhereConditionOperator) getEnum(dsWhereConditionOperator.class, dsWhereConditionOperator);
        }
    } 
    
    /**
     * Mimic the enum for dsWhereConditionType.
     * This is used to determine the type of where clause.
     */ 
    public static final class dsWhereConditionType extends Enum{
        
        /** <code>pos</code> - The position of the enum. */
        public int pos;
        
        /** 
         * <code>dswctDefault</code> - Default where condition type. 
         * Position : 0 
         **/
        public static final dsWhereConditionType dswctDefault = new dsWhereConditionType("default");
        /** 
         * <code>dswctAttr</code> - Attr where condition type. 
         * Position : 1
         **/
        public static final dsWhereConditionType dswctAttr  = new dsWhereConditionType("attr");
        /** 
         * <code>dswctBOContextAttr</code> - BOContextAttr where condition type. 
         * Position : 2 
         **/
        public static final dsWhereConditionType dswctBOContextAttr = new dsWhereConditionType("bocontextattr");
        /** 
         * <code>dswctString</code> - String where condition type. 
         * Position : 3 
         **/
        public static final dsWhereConditionType dswctString = new dsWhereConditionType("string");
        /** 
         * <code>dswctNumber</code> - Number where condition type. 
         * Position : 4 
         **/
        public static final dsWhereConditionType dswctNumber = new dsWhereConditionType("number");
        /** 
         * <code>dswctDate</code> - Date where condition type. 
         * Position : 5 
         **/
        public static final dsWhereConditionType dswctDate = new dsWhereConditionType("date");
        /** 
         * <code>dswctSpecial</code> - Special where condition type. 
         * Position : 6 
         **/
        public static final dsWhereConditionType dswctSpecial = new dsWhereConditionType("special");
        /** 
         * <code>dswctOperand</code> - Operand where condition type. 
         * Position : 7 
         **/
        public static final dsWhereConditionType dswctOperand = new dsWhereConditionType("operand");
        /** 
         * <code>dswctNesting</code> - Nesting where condition type. 
         * Position : 8 
         **/
        public static final dsWhereConditionType dswctNesting = new dsWhereConditionType("nesting");
        /** 
         * <code>dswctOperator</code> - Operator where condition type. 
         * Position : 9 
         **/
        public static final dsWhereConditionType dswctOperator = new dsWhereConditionType("operator");
        
        private dsWhereConditionType(String dsWhereConditionType) { super(dsWhereConditionType); }
        
        static {
            dswctDefault.pos = 0;
            dswctAttr.pos = 1;
            dswctBOContextAttr.pos = 2;
            dswctString.pos = 3;
            dswctNumber.pos = 4;
            dswctDate.pos = 5;
            dswctSpecial.pos = 6;
            dswctOperand.pos = 7;
            dswctNesting.pos = 8;
            dswctOperator.pos = 9;
        }
        
        /**
         * @param dsWhereConditionType The name of the enum.
         * @return Returns the dsType
         */
        public static dsWhereConditionType getEnum(String dsWhereConditionType) {
            return (dsWhereConditionType)getEnum(dsWhereConditionType.class, dsWhereConditionType);
        }
    }
    
    /**
     * Mimic the enum for dsState.
     * This is used to determine the state of a datasource.
     */ 
    public static final class dsState extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>dssUnused </code> - Unused datasource. 
         * Position : 0 
         **/
        public static final dsState dssUnused = new dsState("unused");
        /** 
         * <code>dssActive </code> - The datasource is active. 
         * Position : 1
         **/
        public static final dsState dssActive   = new dsState("active");
        /**
         * <code>dssClosed</code> - The datasource is closed. 
         * Position : 2 
         **/
        public static final dsState dssClosed = new dsState("closed");
        /** 
         * <code>dssError</code> - The datasource has a error. 
         * Position : 3 
         **/
        public static final dsState dssError = new dsState("error");
        
        private dsState(String dsState) { super(dsState); }
        
        static {
            dssUnused.pos = 0;
            dssActive.pos = 1;
            dssClosed.pos = 2;
            dssError.pos = 3;
        }
        
        /**
         * @param dsState The name of the enum.
         * @return Returns the dsType
         */
        public static dsState getEnum(String dsState) {
            return (dsState) getEnum(dsState.class, dsState);
        }
    }  
    
    /**
     * Mimic the enum for dsSearchSupport.
     * This is used to determine the search support of a datasource.
     */ 
    public static final class dsSearchSupport extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>dsssSimple</code> - Simple search support. 
         * Can only search using =. 
         * Position : 0 
         **/
        public static final dsSearchSupport dsssSimple = new dsSearchSupport("simple");
        /** 
         * <code>dsssBasic</code> - Basic search support. 
         * Can only search using =  and <>. 
         * Position : 1
         **/
        public static final dsSearchSupport dsssBasic   = new dsSearchSupport("basic");
        /** 
         * <code>dsssStandard</code> - Standard search support. 
         * Can search using = , <>, >, >=, < and <=. 
         * Position : 2 
         **/
        public static final dsSearchSupport dsssStandard = new dsSearchSupport("standard");
        /** 
         * <code>dsssFull</code> - Full search support. 
         * Full search capabilities (RDBMS). 
         * Position : 3 
         **/
        public static final dsSearchSupport dsssFull = new dsSearchSupport("full");
        
        private dsSearchSupport(String dsSearchSupport) { super(dsSearchSupport); }
        
        static {
            dsssSimple.pos = 0;
            dsssBasic.pos = 1;
            dsssStandard.pos = 2;
            dsssFull.pos = 3;
        }
        
        /**
         * @param dsSearchSupport The name of the enum.
         * @return Returns the dsType
         */
        public static dsSearchSupport getEnum(String dsSearchSupport) {
            return (dsSearchSupport)getEnum(dsSearchSupport.class, dsSearchSupport);
        }
    }     
    
    /**
     * Mimic the enum for propertyPersistStatus.
     */ 
    public static final class propertyPersistStatus extends Enum {
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>ppsSet</code> - Attribute was set.
         * Position : 0 
         **/
        public static final propertyPersistStatus ppsSet  = new propertyPersistStatus("set");
        /** 
         * <code>ppsNew</code> - Newly created property
         * Position : 1 
         **/
        public static final propertyPersistStatus ppsNew = new propertyPersistStatus("new");
        /** 
         * <code>ppsReset</code> - Reset the value of the propety.
         * Position :  2
         **/
        public static final propertyPersistStatus ppsReset   = new propertyPersistStatus("reset");
        /** 
         * <code>ppsLoaded</code> - Loaded property.
         * Position : 3 
         **/
        public static final propertyPersistStatus ppsLoaded = new propertyPersistStatus("loaded");
        
        private propertyPersistStatus(String propertyPersistStatus) { super(propertyPersistStatus); }
        
        static {
        	ppsSet.pos = 0;
        	ppsNew .pos = 1;
        	ppsReset.pos = 2;
        	ppsLoaded.pos = 3;
        }
        
        /**
         * @param propertyPersistStatus The name of the enum.
         * @return Returns the dsType
         */
        public static propertyPersistStatus getEnum(String propertyPersistStatus) {
            return (propertyPersistStatus)getEnum(propertyPersistStatus.class, propertyPersistStatus);
        }
    }
    
    /**
     * Mimic the enum for dsOrderSupport.
     * This is used to determine the order support of a datasource.
     */ 
    public static final class dsOrderSupport extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>dsosNone</code> - No order support. Cannot sort at all. 
         * Position : 0 
         **/
        public static final dsOrderSupport dsosNone  = new dsOrderSupport("none");
        /** 
         * <code>dsosSimple</code> - Simple order support. Can order on single attribute in ASC/DESC order. 
         * Position : 1 
         **/
        public static final dsOrderSupport dsosSimple = new dsOrderSupport("simple");
        /** 
         * <code>dsosBasic</code> - Basic order support. Can order on single attribute in ASC/DESC order. 
         * Position :  2
         **/
        public static final dsOrderSupport dsosBasic   = new dsOrderSupport("basic");
        /** 
         * <code>dsosFull</code> - Full order support. Can order on many groups in mixed ASC/ESC order. 
         * Position : 3 
         **/
        public static final dsOrderSupport dsosFull = new dsOrderSupport("full");
        
        private dsOrderSupport(String dsOrderSupport) { super(dsOrderSupport); }
        
        static {
            dsosNone.pos = 0;
            dsosSimple .pos = 1;
            dsosBasic.pos = 2;
            dsosFull.pos = 3;
        }
        
        /**
         * @param dsOrderSupport The name of the enum.
         * @return Returns the dsType
         */
        public static dsOrderSupport getEnum(String dsOrderSupport) {
            return (dsOrderSupport)getEnum(dsOrderSupport.class, dsOrderSupport);
        }
    }
    
    /**
     * Mimic the enum for pageflowBOContextIdentificationMethod.
     * This is used to determine the business object context indentification method.
     */ 
    public static final class pageflowBOContextIdentificationMethod extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>pbocimByPK</code> - By primary key. 
         * Position : 0 
         **/
        public static final pageflowBOContextIdentificationMethod pbocimByPK   = new pageflowBOContextIdentificationMethod("bypk");
        /** 
         * <code>pbocimByWhereGroup</code> - By where group. 
         * Position : 1 
         **/
        public static final pageflowBOContextIdentificationMethod pbocimByWhereGroup = new pageflowBOContextIdentificationMethod("bywheregroup");
        /** 
         * <code>pbocimByContextName</code> - By context name. 
         * Position : 2
         **/
        public static final pageflowBOContextIdentificationMethod pbocimByContextName   = new pageflowBOContextIdentificationMethod("bycontextname");
        /** 
         * <code>pbocimCreateOnly</code> - Create only. 
         * Position : 3
         **/
        public static final pageflowBOContextIdentificationMethod pbocimCreateOnly   = new pageflowBOContextIdentificationMethod("createonly");
        
        private pageflowBOContextIdentificationMethod(String pageflowBOContextIdentificationMethod) { super(pageflowBOContextIdentificationMethod); }
        
        static {
            pbocimByPK.pos = 0;
            pbocimByWhereGroup .pos = 1;
            pbocimByContextName.pos = 2;
            pbocimCreateOnly.pos = 3;
        }
        
        /**
         * @param pageflowBOContextIdentificationMethod The name of the enum.
         * @return Returns the dsType
         */
        public static pageflowBOContextIdentificationMethod getEnum(String pageflowBOContextIdentificationMethod) {
        	if (pageflowBOContextIdentificationMethod == null) return null;
            return (pageflowBOContextIdentificationMethod)
            	    getEnum(pageflowBOContextIdentificationMethod.class, 
            	    		pageflowBOContextIdentificationMethod.toLowerCase());
        }
    }
    
    /**
     * Mimic the enum for webFKBehaviour.
     * 
     * The behaviour for the Foriegn Key.
     */ 
    public static final class webFKBehaviour extends Enum{
        
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>wfkbOnClick</code> - Trigger search onClick of the search icon.
         * Position : 0 
         **/
        public static final webFKBehaviour wfkbOnClick   = new webFKBehaviour("onclick");
        
        /** 
         * <code>wfkbOnKeyDown</code> - Trigger search on key down.
         * Position : 1 
         **/
        public static final webFKBehaviour wfkbOnKeyDown = new webFKBehaviour("onkeydown");
        
        /** 
         * <code>wfkbOnBlur</code> - Trigger search onBlur.
         * Position : 2
         **/
        public static final webFKBehaviour wfkbOnBlur   = new webFKBehaviour("onblur");
        
        private webFKBehaviour(String webFKBehaviour) { super(webFKBehaviour); }
        
        static {
        	wfkbOnClick.pos = 0;
        	wfkbOnKeyDown .pos = 1;
        	wfkbOnBlur.pos = 2;
        }
        
        /**
         * @param webFKBehaviour The name of the enum.
         * @return Returns the webFKBehaviour enum.
         */
        public static webFKBehaviour getEnum(String webFKBehaviour) {
            return (webFKBehaviour)getEnum(webFKBehaviour.class, webFKBehaviour);
        }
    }
    
    /**
     * Mimic the enum for pageflowParameterBagEntryType.
     * 
     * The type of a parameters bags value.
     */ 
    public static final class pageflowParameterBagEntryType extends Enum{
    	
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>ppbetUrl</code> - The parameterbag value will be a PFUrl.
         * Position : 0 
         **/
        public static final pageflowParameterBagEntryType ppbetUrl   = new pageflowParameterBagEntryType("url");
        
        /** 
         * <code>ppbetRef</code> - The parameterbag value will be a PFRef.
         * Position : 1 
         **/
        public static final pageflowParameterBagEntryType ppbetRef = new pageflowParameterBagEntryType("ref");
        
        /** 
         * <code>ppbetEntities</code> - The parameterbag value will be a collection of entities.
         * Position : 2
         **/
        public static final pageflowParameterBagEntryType ppbetEntities   = new pageflowParameterBagEntryType("entities");
        
        /** 
         * <code>ppbetEntity</code> - The parameterbag value will be entity.
         * Position : 3
         **/
        public static final pageflowParameterBagEntryType ppbetEntity   = new pageflowParameterBagEntryType("entity");
        
        /** 
         * <code>ppbetLabel</code> - The parameterbag value will be a Label Collection.
         * Position : 4
         **/
        public static final pageflowParameterBagEntryType ppbetLabel   = new pageflowParameterBagEntryType("label");
        
        /** 
         * <code>ppbetComponent</code> - The parameterbag value will be a PFComponent.
         * Position : 5
         **/
        public static final pageflowParameterBagEntryType ppbetComponent   = new pageflowParameterBagEntryType("component");
        
        /** 
         * <code>ppbetString</code> - The parameterbag value will be a string.
         * Position : 6
         **/
        public static final pageflowParameterBagEntryType ppbetString   = new pageflowParameterBagEntryType("string");
        
        private pageflowParameterBagEntryType(String pageflowParameterBagEntryType) { super(pageflowParameterBagEntryType); }
        
        static {
        	ppbetUrl.pos = 0;
        	ppbetRef.pos = 1;
        	ppbetEntities.pos = 2;
        	ppbetEntity.pos = 3;
        	ppbetLabel.pos = 4;
        	ppbetComponent.pos = 5;
        	ppbetString.pos = 6;
        }
        
        /**
         * @param pageflowParameterBagEntryType The name of the enum.
         * @return Returns the pageflowParameterBagEntryType enum.
         */
        public static pageflowParameterBagEntryType getEnum(String pageflowParameterBagEntryType) {
            return (pageflowParameterBagEntryType)getEnum(pageflowParameterBagEntryType.class, pageflowParameterBagEntryType);
        }
    }
    
    /**
     * Mimic the enum for pageflowElementAlign.
     * 
     * Pageflow element alignment
     */ 
    public static final class pageflowElementAlign extends Enum{
    	
        /** <code>pos</code> -  The position of the enum. */
        public int pos;
        
        /** 
         * <code>peaBottom</code> - Pageflow element bottom alignment.
         * Position : 0
         **/
        public static final pageflowElementAlign peaBottom   = new pageflowElementAlign("bottom");
        
        /** 
         * <code>peaTop</code> - Pageflow element top alignment.
         * Position : 1 
         **/
        public static final pageflowElementAlign peaTop   = new pageflowElementAlign("top");
        
        /** 
         * <code>peaRight</code> - Pageflow element right alignment.
         * Position : 2
         **/
        public static final pageflowElementAlign peaRight   = new pageflowElementAlign("right");
        
        /** 
         * <code>peaLeft</code> - Pageflow element left alignment.
         * Position : 3 
         **/
        public static final pageflowElementAlign peaLeft = new pageflowElementAlign("left");
        
        private pageflowElementAlign(String pageflowElementAlign) { super(pageflowElementAlign); }
        
        static {
        	peaBottom.pos = 0;
        	peaTop.pos = 1;
        	peaRight.pos = 2;        	
        	peaLeft.pos = 3;
        }
        
        /**
         * @param pageflowElementAlign The name of the enum.
         * @return Returns the pageflowParameterBagEntryType enum.
         */
        public static pageflowElementAlign getEnum(String pageflowElementAlign) {
        	/**
        	 * Default to bottom
        	 */
        	if (pageflowElementAlign == null) {
        		return peaBottom;
        	}
        	
            return (pageflowElementAlign)getEnum(pageflowElementAlign.class, pageflowElementAlign.toLowerCase());
        }
    }
    
	//---------------------- Static helper methods menthods
	
    /**
     * Handle zx variables read from xml files.
     * 
     * <pre>
     * 
     * NOTE : This will fail with zip=ip
     * or z ? error !
     * </pre>
     * 
     * @param zxVar The String that could contain a legacy name like "zFixed" becomes "fixed"
     * @return Returns a lowserCase string.
     */
    protected static String zxVar(String zxVar) {
        zxVar = zxVar.toLowerCase();
        if (zxVar.charAt(0) == 'z') {
            zxVar = zxVar.substring(1);
        }
        return zxVar;
    }
    
    /**
     * @param penm The enum
     * @return Returns the string value of this enum.
     */
    public static String valueOf(Enum penm) {
    	if (penm != null) {
    		return penm.getName();
    	}
    	
		return null;
    }
    
    /**
     * @param penm The enum
     * @param penmDefault The default to fall back on
     * @return Returns the string value of this enum.
     */
    public static String valueOf(Enum penm, Enum penmDefault) {
    	if (penm != null) {
    		return penm.getName();
    	}
    	
		return penmDefault.getName();
    }
    
    /**
     * Does type conversion. Does not seem to work.
     */
    public static void registerConverter() {
    	ConvertUtils.register(new EnumConverter(), Enum.class);
    }
}