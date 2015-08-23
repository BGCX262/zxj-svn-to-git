/*
 * Created on Mar 12, 2004
 * $Id: DrctrFHDefault.java,v 1.1.2.7 2006/07/17 16:38:03 mike Exp $
 */
package org.zxframework.director;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.property.Property;
import org.zxframework.util.StringUtil;

/**
 * The default framework director function handler.
 * 
 * <pre>
 * 
 * Change    : BD2JUN03
 *  Why       : Added #eval director
 * 
 *  Change    : DGS06OCT2003
 *  Why       : Change to #viewonlygroup to fix log error when trying to use non-existent 2nd arg.
 * 
 *  Change    : BD20OCT03
 *  Why       : Added #fkAttrName director
 *
 *  Change    : BD22FEB04
 *  Why       : Added #msg director
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DrctrFHDefault extends DrctrFH {

    //------------------------ Members  
    
    //------------------------ Constructors 
    
    /**
     * Default constructor.
     */
    public DrctrFHDefault() {
        super();
    }
    
    //------------------------ Public Methods 
    
    //------------------------ Directors
    
    /**
     * #qs - Get entry from quick context.
     * 
     * <pre>
     * 
     * Get entry from quick context (#qs is legacy)
     * 
     * Only when we are not talking about a #qsn.*... in which case it
     * will be dealt with by the pageflow director FH handler
     * </pre>
     */
    public class qs implements Director {  
        /** Discribes me*/
    	public static final String describe = "Get entry from quick context";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException{
            String exec; 
            String strArg = pobjFunc.getRemainder();	
            if (strArg.charAt(0) != '*')  {
                exec = getZx().getQuickContext().getEntry(strArg);
            } else {
                throw new ZXException("Failed to : Legacy use of qs use qsn instead.");
            }
            return exec;
        }        
    }
    
    /**
     * #qc - Get entry from quick context .
     */
    public class qc extends qs {
    	// Implementation is in qs.
    }
    
    /**
     * #maincontext - Take <code>argument</code> from main session context.
     */
    public class maincontext implements Director {  
    	/** Discribes me*/
    	public static final String describe = "Take <argument> from main session context";
    	
        /** @see Director#exec(DirectorFunction) */
         public String exec(DirectorFunction pobjFunc) throws ZXException{
             String exec; 
             exec = getZx().getSession().getFromContext(pobjFunc.getRemainder());
             return exec;
         }        
    }    
    
    /**
     * #eval - Take <argument> and evaluate as a director.
     */
    public class eval implements Director {  
    	/** Discribes me*/
    	public static final String describe = "Take <argument> and evaluate as a director";
        
        /** @see Director#exec(DirectorFunction) */
         public String exec(DirectorFunction pobjFunc) throws ZXException{
             String exec; 
             exec = getZx().resolveDirector(pobjFunc.getRemainder());
             return exec;
         }        
    }
    
    /**
     * #entityname - Name / alias of a names business object.
     * 
     *  <pre>
     * 
     * - Name of current entity
     * - Name or alias of an arbitrary entity from the entity collection
     * </pre>
     */
    public class entityname implements Director {  
        /** Discribes me*/
        public static final String describe = "Name / alias of a names business object";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            ZXBO objBO = getZx().getBOContext().getBO(pobjFunc.getRemainder());
            if (objBO != null) {
                if (StringUtil.len(objBO.getDescriptor().getAlias()) > 0) {
                    exec = objBO.getDescriptor().getAlias();
                } else {
                    exec = objBO.getDescriptor().getName();
                }
            } else {
                throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
            }
            return exec;
        }        
    }    
    
    /**
     * #entitylabel - Label of an entity descriptor.
     */
    public class entitylabel implements Director {  
    	/** Discribes me*/
    	public static final String describe = "Label of an entity descriptor";
    	
        /** @see Director#exec(DirectorFunction) */
         public String exec(DirectorFunction pobjFunc) throws ZXException{
             String exec; 
             ZXBO objBO = getZx().getBOContext().getCreateBO(pobjFunc.getRemainder());
             if (objBO != null) {
                 exec = objBO.getDescriptor().getLabel().getLabel();
             } else {
                 throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
             }
             return exec;
         }        
    }     
    
    /**
     * #entityvalue - Get value of an attribute of an / the BO.
     * 
     * <pre>
     * 
     * DGS09JUN2003: Can be formatted value e.g. option list label
     * </pre>
     */
    public class entityvalue implements Director {  
    	/** Discribes me*/
    	public static final String describe = "Get value of an attribute of an / the BO";
    	
        /** @see Director#exec(DirectorFunction) */
         public String exec(DirectorFunction pobjFunc) throws ZXException{
             String exec = null; 
             String strKey = "";
             
             if (pobjFunc.numArgs() > 1) {
                 strKey = pobjFunc.getArg(0);
             }
             
             ZXBO objBO = getZx().getBOContext().getBO(strKey);
             if(objBO != null) {
                 if (pobjFunc.numArgs() == 1) {
                     strKey = pobjFunc.getRemainder();
                 } else {
                     strKey = pobjFunc.getArg(1);
                 }
                 
                 Property objProperty = objBO.getValue(strKey);
                 if(objProperty != null) {
                     /**
                      * DGS09JUN2003: Can be formatted value e.g. option list label
                      */
                     if (this instanceof attrformattedvalue) {
                         exec = objProperty.formattedValue(true);
                     } else {
                         exec = objProperty.getStringValue();
                     }
                     
                 } else {
                     throw new ZXException("Unable to find property, BO " + objBO.getDescriptor().getName() + "." + strKey);
                 }
                 
             } else {
                 throw new ZXException("BO not found in BO context : " + strKey);
             }
             
             return exec;
         }
    }       
    
    /**
     * #attrvalue - Get value of an attribute of an / the BO .
     */
    public class attrvalue extends entityvalue {
    	// Implementation is in entityvalue.
    }
    
    /**
     * #attrformattedvalue - Get value of an attribute of an / the BO .
     */
    public class attrformattedvalue extends entityvalue {
    	// Implementation is in entityvalue.
    }
    
    /**
     * #pref - Added user preference support.
     */
    public class pref implements Director {  
    	/** Discribes me*/
    	public static final String describe = "Added user preference support";
    
        /** @see Director#exec(DirectorFunction) */
         public String exec(DirectorFunction pobjFunc) throws ZXException{
             String exec; 
             exec = getZx().getUserProfile().getPreference(pobjFunc.getRemainder());
             return exec;
         }        
    } 
    
    /**
     * #labelvalue - Value of label attribute group of a named BO.
     * 
     * <pre>
     * 
     * Optionally escaped for use in Javascript
     * </pre>
     */
    public class labelvalue implements Director {
        /** Discribes me*/
        public static final String describe = "Value of label attribute group of a named BO";
        /** Is a jslabelvalue */
        protected boolean jsvalue = false;
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            ZXBO objBO = getZx().getBOContext().getBO(pobjFunc.getRemainder());
            if (objBO != null) {
                exec = objBO.formattedString("label");
                if (jsvalue) exec = StringUtil.replaceAll(exec,'\'', ' ');
                
            } else {
                throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
            }
            return exec;
        }
    }
    
    /**
     * #jslabelvalue - Value of label attribute group of a named BO.
     */
    public class jslabelvalue extends labelvalue {
        /** Public constructor **/
        public jslabelvalue() {
            jsvalue = true;
        }
    }
    
    /**
     * #pk - The PK value of a named entity.
     */
    public class pk implements Director {
        /** Discribes me*/
        public static final String describe = "The PK value of a named entity";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            ZXBO objBO = getZx().getBOContext().getBO(pobjFunc.getRemainder());
            if (objBO != null) {
                exec = objBO.getPKValue().getStringValue();
            } else {
                throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
            }
            return exec;
        }
    }
    
    /**
     * #pkname - The PK name of current entity.
     */
    public class pkname implements Director {
        /** Discribes me*/
        public static final String describe = "The PK name of current entity";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
                ZXBO objBO = getZx().getBOContext().getBO(pobjFunc.getRemainder());
                if (objBO != null) {
                    exec = objBO.getDescriptor().getPrimaryKey();
                } else {
                    throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
                }
                return exec;
        }
    }
    
    /**
     * #fkattrname - Gives the foriegn key between 2 BOs.
     * 
     * <pre>
     * 
     * The name of the attribute that links from to to
     * #fkAttrName.from.to
     * from and to can either be references to entries in BO context
     * or entity names
     * </pre>
     */
    public class fkattrname implements Director {
        /** Discribes me*/
        public static final String describe = "Gives the foriegn key between 2 BOs";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            
            if (pobjFunc.numArgs() != 2) {
                throw new ZXException("Director requires two parameters (#fkAttrName.from.to)");
            }
            
            ZXBO objFromBO = getZx().getBOContext().getCreateBO(pobjFunc.getArg(0));
            if (objFromBO == null) {
                throw new ZXException("'From' BO not found in context and unable to create (" + pobjFunc.getArg(0) + ")");
            }
            
            ZXBO objToBO = getZx().getBOContext().getCreateBO(pobjFunc.getArg(1));
            if (objToBO == null) {
                throw new ZXException("'To' BO not found in context and unable to create (" + pobjFunc.getArg(1) + ")");
            }
            
            Attribute objAttr = objFromBO.getFKAttr(objToBO);
            if (objAttr == null) {
                throw new ZXException("No FK attr found from '" + pobjFunc.getArg(0) + "' to '" + pobjFunc.getArg(1) + "'");
            }
            exec = objAttr.getName();
            return exec;
        }
    }
    
    /**
     * #inqs - Does an entry exists in the query string.
     * 
     * <pre>
     * 
     * Designed for use in active and therefor return T or F
     * </pre>
     */
    public class inqs implements Director {
        /** Discribes me*/
        public static final String describe = "Does an entry exists in the query string";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;

            if (StringUtil.len( getZx().getQuickContext().getEntry(pobjFunc.getRemainder()) ) > 0) {
                exec = "T";
            } else {
                exec = "F";
            }
            return exec;
        }
    }
    
    /**
     * #notinqs - Does an entry not exist in the query string.
     */
    public class notinqs implements Director {
        /** Discribes me*/
        public static final String describe = "Does an entry not exist in the query string.";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            if (StringUtil.len( getZx().getQuickContext().getEntry(pobjFunc.getRemainder()) ) > 0) {
                exec = "F";
            } else {
                exec = "T";
            }
            return exec;
        }
    }
    
    /**
     * #notviewonly - Not in view only.
     * 
     * <pre>
     * 
     * Designed to be used in active field so we can easily disable
     * submit / create / delete buttons
     * </pre>
     */
    public class notviewonly implements Director {
        /** Discribes me*/
        public static final String describe = "Not in view only";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            
            if(StringUtil.len(getZx().getQuickContext().getEntry("-vo")) > 0) {
                exec = "F";
            } else {
                exec = "T";
            }
            return exec;
        }
    }
    
    /**
     * #viewonlygroup - In view group only.
     * 
     * <pre>
     * 
     * Designed to be used in lock attribute group so we can simply
     * say #viewonlygroup.* in the lock attribute group to make clear
     * that this group is not editable
     * 
     * Use optional 2nd parameter (e.g. #viewonlygroup.*¬label to use
     * an attribute group in case you do have a lock
     * 
     * DGS06OCT2003: Was assuming arg(2) always present if -vo has no length,
     * but in fact it is optional and often not there - if not, default to
     * to the empty string i.e. if no lock, don't lock anything. Just to make it clear,
     * the first argument is used when it is viewonly, the second is used when it is
     * not viewonly.
     * </pre>
     */
    public class viewonlygroup implements Director {
        /** Discribes me*/
        public static final String describe = "In view group only";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            if(StringUtil.len(getZx().getQuickContext().getEntry("-vo")) > 0) {
                exec = pobjFunc.getArg(0);
            } else {
                if (pobjFunc.getArgs().size() > 1) {
                    exec = pobjFunc.getArg(1);
                } else {
                    exec = "";
                }
                
            }
            return exec;
        }
    }
    
    /**
     * #notnull - Is the named attribute of the BO null yes / no.
     */
    public class notnull implements Director {
        /** Discribes me*/
        public static final String describe = "Is the named attribute of the BO null yes / no";
        protected boolean isNull = false;
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            String strKey;
            
            if (pobjFunc.numArgs() > 1) {
                strKey = pobjFunc.getArg(0);
            } else {
                strKey = "";
            }
            
            ZXBO objBO = getZx().getBOContext().getBO(strKey);
            if (objBO != null) {
                if (pobjFunc.numArgs() == 1) {
                    strKey = pobjFunc.getRemainder();
                } else {
                    strKey = pobjFunc.getArg(1);
                }
                
                Property objProperty = objBO.getValue(strKey);
                if (objProperty != null) {
                    if (objProperty.isNull) {
                        exec = isNull?"T":"F";
                    } else {
                        exec = isNull?"F":"T";
                    }
                    
                } else {
                    throw new ZXException("Unable to find property, BO " + objBO.getDescriptor().getName() + "." + strKey);
                }
                
            } else {
                throw new ZXException("BO not found in BO context : " + strKey);
            }
            
            return exec;
        }
    }
    
    /**
     * #null - Is the named attribute of the BO null yes / no.
     */
    public class Null extends notnull {
        /** Default constructor */
        public Null( ) {
            isNull = true;
        }
    }
    
    /**
     * #ingroup - Check whether the user is in given group.
     * 
     * <pre>
     * 
     * #ingroup.groupname - Check whether the user is in given group
     * </pre>
     */
    public class ingroup implements Director {
        /** Discribes me*/
        public static final String describe = "Check whether the user is in given group";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            if(getZx().getUserProfile().isUserInGroup(pobjFunc.getRemainder())) {
                exec = "T";
            } else {
                exec = "F";
            }
            return exec;
        }
    }
    
    /**
     * #haslock.entity - Check whether the current subsession has a lock.
     */
    public class havelock implements Director {
        /** Discribes me*/
        public static final String describe = "Check whether the current subsession has a lock";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            
            ZXBO objBO = getZx().getBOContext().getBO(pobjFunc.getRemainder());
            if (objBO != null) {
                if (getZx().getBOLock().haveLock(objBO.getDescriptor().getName(), 
                        								  objBO.getPKValue(), 
                        								  getZx().getQuickContext().getEntry("-ss"))) {
                    exec = "T";
                } else {
                    exec = "F";
                }
                
            } else {
                throw new ZXException("BO not found in BO context : " + pobjFunc.getRemainder());
            }
            
            return exec;
        }
    }
    
    /**
     * #uniqueid - Unique identifier.
     */
    public class uniqueid implements Director {
        /** Discribes me*/
        public static final String describe = "Unique identifier";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            exec = new Date().getTime() + "" + Math.random();
            return exec;
        }
    }
    
    /**
     * #expr - An expression.
     * @see org.zxframework.expression.Expression
     */
    public class expr implements Director {
        /** Discribes me*/
        public static final String describe = "Expression";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            Property objProperty = getZx().getExpressionHandler().eval(pobjFunc.getRemainder());
            if (objProperty != null) {
                exec = objProperty.getStringValue();
            }
            return exec;
        }
    }

    /**
     * #date - Take current date in format <argument> (see VB Format function).
     */
    public class date implements Director {
        /** Discribes me*/
        public static final String describe = "Take current date in format <argument> (see VB Format function)";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            if (StringUtil.len(pobjFunc.getRemainder()) == 0) {
                DateFormat df = getZx().getTimestampFormat();
                exec = df.format(new Date());
            } else {
                DateFormat df = new SimpleDateFormat(pobjFunc.getRemainder());
                exec = df.format(new Date());
            }
            return exec;
        }
    }
    
    /**
     * #time - Take current time in format <argument> (see VB Format function).
     */
    public class time implements Director {
        /** Discribes me*/
        public static final String describe = "Take current time in format <argument> (see VB Format function)";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            if (StringUtil.len(pobjFunc.getRemainder()) == 0) {
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                exec = df.format(new Date());
            } else {
                DateFormat df = new SimpleDateFormat(pobjFunc.getRemainder());
                exec = df.format(new Date());
            }
            return exec;
        }
    }
    
    /**
     * #user - ID of current user.
     */
    public class user implements Director {
        /** Discribes me*/
        public static final String describe = "ID of current user";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            exec = getZx().getUserProfile().getPKValue().formattedValue(true);
            return exec;
        }
    }
    
    /**
     * #whoami -  ID of current user .
     */
    public class whoami extends user {
    	// Implementation is in user.
    }
    
    /**
     * #language - Language of the current logged in user.
     */
    public class language implements Director {
        /** Discribes me*/
        public static final String describe = "Language of current user";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            return getZx().getLanguage();
        }
    }
    
    /**
     * #True - T should be interpreted by boolan attributes as true.
     */
    public class True implements Director {
        /** Discribes me*/
        public static final String describe = "T should be interpreted by boolan attributes as true";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            return "T";
        }
    }
    
    /**
     * #False - F should be interpreted by boolan attributes as false.
     */
    public class False implements Director {
        /** Discribes me*/
        public static final String describe = "F should be interpreted by boolan attributes as false";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) {
            return "F";
        }
    }
    
    /**
     * #msg - Get message.
     * 
     * <pre>
     * 
     * can have up to 5 parameters, but does actually support more ;).
     * 
     * #msg.messageId
     * #msg.messageId.parm1
     * #msg.messageId.parm1.parm2
     * #msg.messageId.parm1.parm2..parm5
     * </pre>
     */
    public class msg implements Director {
        /** Discribes me*/
        public static final String describe = "Get message";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            
            // Check the number of arguments.
            int intNumArgs = pobjFunc.numArgs();
            if (intNumArgs > 6 && intNumArgs < 1) {
                throw new ZXException("To many of to little arguments");
            }
            
            if (intNumArgs == 1) {
                exec = getZx().getMsg().getMsg(pobjFunc.getArg(1));
            } else {
                // Populate an array of string arguements :
                String[] args = new String[intNumArgs];
                for (int i = 1; i < args.length; i++) {
                    args[i] = (String)pobjFunc.getArgs().get(i);
                }
                exec = getZx().getMsg().getMsg(pobjFunc.getArg(1), args);                
            }
            
            return exec;
        }
    }
}