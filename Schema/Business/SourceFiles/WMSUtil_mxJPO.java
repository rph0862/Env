/** Name of the JPO    : WMSUtil
 ** Developed by    :  
 ** Client            : WMS
 ** Description        : The purpose of this JPO is to use utilities created for code.
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/

 
 
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

 
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;
/**
 * The purpose of this JPO is to use utilities created
 * @author 
 * @version R418 - Copyright (c) 1993-2016 Dassault Systems.
 */
public class WMSUtil_mxJPO extends WMSConstants_mxJPO 
{
	 


    /**
     * Constructor.
     * @param context the eMatrix Context object
     * @param strArguments holds the following input arguments[0]-id of the business object
     * @throws Exception if the operation fails    
     * @since 418
     */
    public WMSUtil_mxJPO (Context context, String[] args) throws Exception
    {
         super(context,args);
    }
    /**
     * Main entry point.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     * @since 418
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }


    /**
     * Method to get Map  based on Key and Value
     * 
     * @param mapListObjects Maplist containing the Object data  
     * @param strKey String value containing the Key to identify the data
     * @param strValue String value to filter data
     * @return mapListSubSet MapList containing the Data based on Key and Value
     * @author WMS
     * @since R418 ///////////////USED
     */
    public static Map<String,String> getMap(MapList mapListObjects, String strKey,
            String strValue) {
        Iterator<Map<String,String>> iteratorMBEs = mapListObjects.iterator();
        Map<String,String> mapData;
        while(iteratorMBEs.hasNext())
        {
        	mapData = iteratorMBEs.next();
            String strKeyValue = mapData.get(strKey);
            if(strValue.equalsIgnoreCase(strKeyValue))
            {
            	return mapData;
            }
        }
        return new HashMap<String,String>();
    }

        /**
     * Method to get the context object OID from the args.
     *
     * @param args Packed program and request maps from the command or form or table
     * @return a string containing the object ID
     * @author WMS
     * @throws Exception if the operation fails 
     * @since 418
     */
    public static String getContextObjectOIDFromArgs(String[] args) throws Exception {
        try
        {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get("objectId");
            if(UIUtil.isNullOrEmpty(strObjectId)) {
                strObjectId = (String) programMap.get("mbeOID");
            }
            return strObjectId;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

    /**
     * method get the basic bus info for the general class
     *
     * @return selListBusSelects SelectList containing the bus selects : type name revision policy vault
     * @author WMS
     * @since 418
     */
    public static SelectList getSubClassBusSelect() {
        SelectList selListBusSelects     = new SelectList(6);
        selListBusSelects.add(DomainConstants.SELECT_ID);
        selListBusSelects.add(DomainConstants.SELECT_POLICY);
        selListBusSelects.add(DomainConstants.SELECT_DESCRIPTION);
        selListBusSelects.add(DomainConstants.SELECT_VAULT);
        selListBusSelects.add(DomainConstants.SELECT_TYPE);
        selListBusSelects.add(DomainConstants.SELECT_NAME);
        return selListBusSelects;
    }

    /**
     * This method checks whether the given value is numeric or not
     *
     * @param String holds value to validate for numeric
     *
     * @return boolean holds true if value is numeric
     *                       false if value is not numeric
     * @throws Exception if the operation fails.
     */
    public static boolean isNumeric(String sValue)
    {
        try
        {
            Double.parseDouble(sValue);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }


/**
     * Access function for formType of Work Order to make it editable
     * @param context The Matrix Context object
     * @param args holds the arguments:     
     * @throws Exception if the operation fails
     */ 
    public boolean isCreateModeFormType(Context context, String[] args) throws Exception 
    {
        try
        {
            boolean bAccess	= false;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strMode = (String) programMap.get("mode");
            if(UIUtil.isNotNullAndNotEmpty(strMode))
            {
                if("create".equals(strMode)){ 
                    bAccess= false;
                }
                else
                {
                    bAccess= true;
                }
            }
            return bAccess;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }


    public boolean isCreateModeHideField(Context context, String[] args) throws Exception 
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strMode = (String) programMap.get("mode");
            if(UIUtil.isNotNullAndNotEmpty(strMode))
            {
                if("create".equals(strMode)|| "edit".equals(strMode)){ 
                    return false;
                }
                else
                {
                    return true;
                }
                
            }
            return false;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

    /**
     * Access function for formType of Work Order to make it editable
     * @param context The Matrix Context object
     * @param args holds the arguments:     
     * @throws Exception if the operation fails
     */ 
    public boolean isCreateModeShowField(Context context, String[] args) throws Exception 
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strMode = (String) programMap.get("mode");
                       
            if(UIUtil.isNotNullAndNotEmpty(strMode))
            {
                if("create".equals(strMode)){ 
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return false;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

    /**
    * Access function for Project Description on Work Order Properties
    * @param context The Matrix Context object
    * @param args holds the arguments:     
    * @throws Exception if the operation fails
    */ 
   public boolean isCreateModeForProjectNameFields(Context context, String[] args) throws Exception 
   {
       try
       {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           String strMode = (String) programMap.get("mode");
           if(UIUtil.isNotNullAndNotEmpty(strMode))
           {
               if("view".equals(strMode)){ 
                   return true;
               }
               else
               {
                   return false;
               }
               
           }
           return false;
       }
       catch(Exception exception)
       {
           exception.printStackTrace();
           throw exception;
       }
   }


    /**
     * Method will convert a string to Integer
     *
     * @param strValue String containing the real value
     * @return doubleValue double value of String
     * @author WMS
     * @since 418
     */
    public static int convertToInteger(String strValue) {
        int intValue = 0;
        try
        {

            if(UIUtil.isNullOrEmpty(strValue))
            {
                strValue = "-1";
            }
            intValue=  Integer.parseInt(strValue);
        }
        catch(Exception exception)
        {
            intValue = -1;
        }
        return intValue;
    }


    /**
     * Returns ematrix Date display format for Forms
     * @author WMS
     * @throws Exception 
     * @since 418
     **/
    public String getEmatrixDateFormat(Context context, String[] args) throws Exception
    {
        String strFormattedDate = "";
        try{                
                HashMap programMap          = (HashMap)JPO.unpackArgs(args);
                String strDateForFormat = (String)programMap.get("Date");
                
                Calendar calendarDate = Calendar.getInstance();
                long lDateForFormat=Long.parseLong(strDateForFormat);
                calendarDate.setTimeInMillis(lDateForFormat);
                SimpleDateFormat simpleDateFormatMatrix  = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),context.getLocale());
                strFormattedDate = simpleDateFormatMatrix.format(calendarDate.getTime());
                
                Date dStartDate=simpleDateFormatMatrix.parse(strFormattedDate);				
                DateFormat dateFormat = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),context.getLocale());
                strFormattedDate = dateFormat.format(dStartDate);
                
        }
        catch(Exception exception) 
        {
            exception.printStackTrace();
            throw exception;
        }
        return strFormattedDate;
    }

    public boolean isEditModeShowField(Context context, String[] args) throws Exception 
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strMode = (String) programMap.get("mode");
            if(UIUtil.isNotNullAndNotEmpty(strMode))
            {
                if("edit".equals(strMode)){ 
                    return true;
                }
                else
                {
                    return false;
                }
                
            }
            return false;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }
    
    
    /**
     * Gets the Departments for the context user
     * @param context The Matrix Context.
     * @param args
     * @return maplist of Departments
     * @throws Exception If the operation fails.
     * @author WMS
     * @since 418
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public static MapList getContextUserDepartment (Context context) throws Exception
     {
    	 MapList mapListDepartment = new MapList();
    	 StringList strListBusSelects = new StringList(2);
    	 strListBusSelects.addElement(DomainConstants.SELECT_ID);
    	 strListBusSelects.addElement(DomainConstants.SELECT_NAME);
    	 StringList strListRelSelects = new StringList(1);
    	 strListRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
    	 try 
    	 {
    		 String strPersonId = PersonUtil.getPersonObjectID(context);
    		 Person person =(Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
    		 person.setId(strPersonId);
    		 mapListDepartment = person.getRelatedObjects(context, // matrix context
    				 DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
    				 DomainConstants.TYPE_DEPARTMENT, // type pattern
    				 strListBusSelects, // object selects
    				 strListRelSelects, // relationship selects
    				 true, // to direction
    				 false, // from direction
    				 (short) 1, // recursion level
    				 DomainConstants.EMPTY_STRING, // object where clause
    				 DomainConstants.EMPTY_STRING, // relationship where clause
    				 0);
    	 }
    	 catch (Exception exception) 
    	 {
    		 exception.printStackTrace();
    		 throw exception;
    	 }
    	 return mapListDepartment;
     }

     /**
     * Connects an object with many others using the given relationship type. 
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param givenObject the object to connect to the list passed in
     * @param relationshipType The relationship type used for the connection
     * @param isFrom this is a from connection of the given object (true) or to the given object (false)
     * @param arrayListClonedTCIds is array list containing the businessobject ids to connect to given object
     * @return a map of related ids as keys and connection ids as values
     * @throws FrameworkException - if the operation fails
     * @author WMS
     * @since R418
     */
     public static Map connect(Context context, DomainObject givenObject,
	     String relationshipType, boolean isFrom,
	     ArrayList<String> arrayListClonedTCIds) throws FrameworkException {
	     try
		     {
		     if(arrayListClonedTCIds.size()>0)
		     {
		     int intSize = arrayListClonedTCIds.size();
		     String [] strTCIds = arrayListClonedTCIds.toArray(new String[intSize]);
		     return DomainRelationship.connect(context, givenObject,relationshipType, isFrom, strTCIds);
		     }
		     }
		     catch(FrameworkException frameworkException)
		     {
		     frameworkException.printStackTrace();
		     throw frameworkException;
		     }
	     return new HashMap();
     }
    
     /** 
      * Method will generates a StringList of value for a particular key from each map of MapList 
      * @param mapListObject holds the MapList of Objects
      * @param strKey holds the Key value
      * @return strListValues list of value from MapList for the given Key 
      * @author WMS
      * @since 418
      */
     public static StringList convertToStringList(MapList mapListObject, String strKey) {
         StringList strListValues = new StringList(mapListObject.size());
         Iterator iterator = mapListObject.iterator();
         while(iterator.hasNext())
         {
             Map<String,String> mapObject = (Map<String,String>)iterator.next();
             strListValues.add(mapObject.get(strKey));
         }
         return strListValues;
     }
	/** 
     * Method to check the User Company
     * @
     * @author WMS
     * @since 418
     */
	public boolean isEmployee(Context context, String[] args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Boolean isSupplierRepresentative = context.isAssigned(DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE );
		boolean bAccess = false;
		try
		{       
				if(isSupplierRepresentative){
					bAccess=false;
				}
				else
				{
					bAccess=true ;
				}
		}catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return bAccess;
	}
	
	
	/** 
     * Method to check the if context user is supplier representative
     * @
     * @author WMS
     * @since 418
     */
	public boolean isSupplierRepresentative(Context context,String[] args) throws Exception{
		return !(isEmployee(context,args));
	
	}
	
	
	/**
	   * Display drop Icon.
	   * @param context - The eMatrix <code>Context</code> object.
	   * @param args - The args holds information about object.
	   * @return list of drop icon.
	   * @throws Exception if operation fails.
	   */
	  public StringList columnDropZone(Context context, String[] args) throws Exception 
	  {
	    StringList dropIconList = new StringList();
	    emxGenericColumns_mxJPO genericColumn = new emxGenericColumns_mxJPO(context,args);
	    Vector columnIconList = genericColumn.columnDropZone(context, args);
	    
	    Map programMap         = (HashMap) JPO.unpackArgs(args);
	    MapList objectList  = (MapList)programMap.get("objectList");
	    
	    if(objectList != null && objectList.size()>0){
	        
	    String[] objectIdArray = new String[objectList.size()];
	    for (int i=0; i<objectList.size(); i++) {
	            Map objectMap = (Map) objectList.get(i);
	            String objectId = (String)objectMap.get(DomainObject.SELECT_ID);
	            objectIdArray[i] = objectId;
	        }
	    
	        StringList dropAccesList = new StringList(3);
	        dropAccesList.addElement("modify");
	        dropAccesList.addElement("fromconnect");
	        dropAccesList.addElement("toconnect");
	        
	        StringList busSelect = new StringList(3);
	        busSelect.addElement("current.access");
	        busSelect.addElement(DomainObject.SELECT_TYPE);
	    
	    MapList objectInfoList = DomainObject.getInfo(context, objectIdArray, busSelect);
	    Iterator itrObj = objectInfoList.iterator();
	    Iterator itrIcon = columnIconList.iterator();
	    while(itrObj.hasNext()&&itrIcon.hasNext()){
	            Map objectMap = (Map)itrObj.next();
	            String dropIcon = (String)itrIcon.next();
	        
	            String access = (String)objectMap.get("current.access");
	            String strObjeType = (String)objectMap.get(DomainObject.SELECT_TYPE);
	            
	            boolean showDropIcon = false;
	            StringList currentAccessList = new StringList();
	            if(ProgramCentralUtil.isNotNullString(access)&& !access.equalsIgnoreCase("all") && TYPE_WMS_MEASUREMENTS.equals(strObjeType)){
	                currentAccessList = FrameworkUtil.split(access, ProgramCentralConstants.COMMA);
	                
	                if(currentAccessList.containsAll(dropAccesList)){
	                    showDropIcon = true;
	                }
	            }else if(access.equalsIgnoreCase("all") && TYPE_WMS_MEASUREMENTS.equals(strObjeType)){
	                showDropIcon = true;
	            }
	        
	            if(showDropIcon){
	                dropIconList.addElement(dropIcon);
	              }else{
	                  dropIconList.addElement(DomainObject.EMPTY_STRING);
	              }
	    }
	    }
	    return dropIconList;
	    
	  }
	 
	  /**
	     * disconnects an arraylist of relationship IDs
	     * 
	     * @param context the eMatrix <code>Context</code> object 
	     * @param arrayListClonedTCIds is array list containing Classified Item relationship IDs
	     * @throws FrameworkException - if the operation fails
	     * @author CHiPS
	     * @since R418
	     */
	    public static void disconnect(Context context,
	            ArrayList<String> arrayListRelationshipIDs)
	                    throws FrameworkException {
	        try
	        {
	            int intSize = arrayListRelationshipIDs.size();
	            if(intSize>0)
	            {
	                String [] strReqRelIds = arrayListRelationshipIDs.toArray(new String[intSize]);

	                DomainRelationship.disconnect(context,strReqRelIds);
	            }
	        }
	        catch(FrameworkException frameworkException)
	        {
	            frameworkException.printStackTrace();
	            throw frameworkException;
	        }
	    }
	    
	    
	    
	    /**
	     * Method will convert a string to double
	     *
	     * @param strValue String containing the real value
	     * @return doubleValue double value of String
	     * @author CHiPS
	     * @since 418
	     */
	    public static double convertToDouble(String strValue) {
	        double doubleValue = 0d;
	        try
	        {
	        	 if(UIUtil.isNullOrEmpty(strValue))
	             {
	                 strValue = "0.0";
	             }
	            
	             int iLangthAfterdecimal     =     0;
	             try
	             {
	                   iLangthAfterdecimal    =     strValue.substring(strValue.indexOf(".")+1, strValue.length()).length();
	             }
	             catch(Exception ex)
	             {
	                
	             }

	             doubleValue=  Double.parseDouble(strValue);
	             BigDecimal bigdecimal = new BigDecimal(doubleValue);
	             if(iLangthAfterdecimal == 2 || iLangthAfterdecimal == 3)
	             {
	                  bigdecimal = bigdecimal.setScale(iLangthAfterdecimal , BigDecimal.ROUND_HALF_EVEN);
	                  String strbigdecimal = bigdecimal.toPlainString();
	                  doubleValue=  Double.parseDouble(strbigdecimal);
	             }
	             else
	             {
	                 if(iLangthAfterdecimal > 3)
	                 {
	                     bigdecimal = bigdecimal.setScale(3 , BigDecimal.ROUND_HALF_EVEN);
	                     String strbigdecimal = bigdecimal.toPlainString();
	                     doubleValue=  Double.parseDouble(strbigdecimal);
	                 }                 
	             }            
	        	
	        }
	        catch(Exception exception)
	        {
	            doubleValue = 0d;
	        }
	        return doubleValue;
	    }
	    
	    
	    
	    /** 
	     * Method to check route exist on particular state
	     * @param context the eMatrix <code>Context</code> object
	     * @param domObj context object
	     * @param strRelWhere where condition to check on particular state
	     * @throws Exception if the operation fails
	     * @return MapList list of route objects exists
	     * @author WMS
	     * @since 418
	     */
	    public static MapList checkForExistingRouteObject(Context context,DomainObject domObj,
	            String strRelWhere) throws Exception {
	        MapList mlRoutes=new MapList();

	        try {
	            StringList busSelects = new StringList(4);
	            busSelects.add(DomainConstants.SELECT_ID);
	            busSelects.add(DomainConstants.SELECT_OWNER);
	            busSelects.add(DomainConstants.SELECT_CURRENT);
	            busSelects.add("attribute["+Route.ATTRIBUTE_ROUTE_STATUS+"]");
	            mlRoutes = domObj.getRelatedObjects(context,
	                    DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
	                    DomainConstants.TYPE_ROUTE, busSelects, null, false, true,
	                    (short) 1,strRelWhere, DomainConstants.EMPTY_STRING, 0);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return mlRoutes;
	    }
	    
	    /** 
	     * Method to restart the existing route 
	     * @param context the eMatrix <code>Context</code> object
	     * @param routeMapList list of existing route objects
	     * @throws Exception if the operation fails
	     * @author WMS
	     * @since 418
	     */
	    public static void restartExistingRoute(Context context, MapList routeMapList) throws Exception {
	        try {
	            Iterator iterator = routeMapList.iterator();
	            Route objRoute = (Route) DomainObject.newInstance(context,
	                    Route.TYPE_ROUTE);
	            while (iterator.hasNext()) {

	                Map<String, String> mRoute = (Map<String, String>) iterator
	                        .next();
	                String sOIDRoute = (String) mRoute
	                        .get(DomainConstants.SELECT_ID);
	                objRoute.setId(sOIDRoute);
	                objRoute.resume(context);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    
	    /** 
	     * Method will generates a StringList from object 
	     * @param mapListObject holds the MapList of Objects
	     * @param strKey holds the Key value
	     * @return strListValues list of value from MapList for the given Key 
	     * @author CHiPS
	     * @since 418
	     */
	    public static StringList convertToStringList(Object obj) {
	        StringList strListValues = new StringList();
	        if(obj instanceof StringList)
	        {
	      	  strListValues = (StringList)obj;
	        }
	        else
	        {
	      	  if(UIUtil.isNotNullAndNotEmpty((String)obj))
	      	  {
	      		  strListValues.add((String)obj);
	      	  }
	        }
	        
	        return strListValues;
	    }
	    /**
	     * Method to get Map List based on Key and Value
	     * 
	     * @param mapListObjects Maplist containing the Object data  
	     * @param strKey String value containing the Key to identify the data
	     * @param strValue String value to filter data
	     * @return mapListSubSet MapList containing the Data based on Key and Value
	     * @author CHiPS
	     * @since R418
	     */
	    public static MapList getSubMapList(MapList mapListObjects, String strKey,
	            String strValue) {
	        MapList mapListSubSet = new MapList(mapListObjects.size());
	        Iterator<Map<String,String>> iteratorMBEs = mapListObjects.iterator();
	        Map<String,String> mapData;
	        while(iteratorMBEs.hasNext())
	        {
	        	mapData = iteratorMBEs.next();
	            String strKeyValue = mapData.get(strKey);
	            if(strValue.equalsIgnoreCase(strKeyValue))
	            {
	                mapListSubSet.add(mapData);
	            }
	        }
	        return mapListSubSet;
	    }
	    
	    /**
	     * Method will set rate value to two decimal points 
	     *
	     * @param strRelId String containing relationship id
	     * @param strValue String containing new value
	     * @param strAttributeName String containing attribute name
	     * @return int flag for trigger
	     * @author CHiPS
	     * @since 418
	     */
	    public static int convertToDoubleRateForRel(Context context ,  String [] args) {
	        double doubleValue = 0.00d;
	        try
	        {
	        	String strRelId 	 = args[0];
	        	String strAttributeNewVlaue = args[1];
	        	String strAttributeName  = args[2];
	        	if(UIUtil.isNullOrEmpty(strAttributeNewVlaue))
	            {
	        		strAttributeNewVlaue = "0.00";
	            }
	        	int iLangthAfterdecimal    =     strAttributeNewVlaue.substring(strAttributeNewVlaue.indexOf(".")+1, strAttributeNewVlaue.length()).length();
	        	if(iLangthAfterdecimal > 2){ 
	            doubleValue=  Double.parseDouble(strAttributeNewVlaue);
	            BigDecimal bigdecimal = new BigDecimal(doubleValue);
	            bigdecimal = bigdecimal.setScale(2  , BigDecimal.ROUND_HALF_EVEN);
	            String strbigdecimal = bigdecimal.toPlainString();
	            doubleValue=  Double.parseDouble(strbigdecimal);
	            strAttributeNewVlaue = String.valueOf(doubleValue);
	        	}
	        	if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	            DomainRelationship.setAttributeValue(context, strRelId, strAttributeName, strAttributeNewVlaue);
	        	}
	        }
	        catch(Exception exception)
	        {
	            exception.printStackTrace();
	        }
	         return 0;
	    }
	    
	    
	    /**
	     * Method will set Quantity value to three decimal points 
	     *
	     * @param strObjId String containing object id
	     * @param strValue String containing new value
	     * @param strAttributeName String containing attribute name
	     * @return int flag for trigger
	     * @author CHiPS
	     * @since 418
	     */
	    public static int convertToDoubleQuantity(Context context ,  String [] args) {
	        double doubleValue = 0.000d;
	        try
	        {
	        	String strObjectIds 	 = args[0];
	        	String strAttributeNewVlaue = args[1];
	        	String strAttributeName  = args[2];
	        	if(UIUtil.isNullOrEmpty(strAttributeNewVlaue))
	            {
	        		strAttributeNewVlaue = "0.000";
	            }
	        	int iLangthAfterdecimal    =     strAttributeNewVlaue.substring(strAttributeNewVlaue.indexOf(".")+1, strAttributeNewVlaue.length()).length();
	        	if(iLangthAfterdecimal > 3){
	            doubleValue=  Double.parseDouble(strAttributeNewVlaue);
	            BigDecimal bigdecimal = new BigDecimal(doubleValue);
	            bigdecimal = bigdecimal.setScale(3  , BigDecimal.ROUND_HALF_EVEN);
	            String strbigdecimal = bigdecimal.toPlainString();
	            doubleValue=  Double.parseDouble(strbigdecimal);
	            strAttributeNewVlaue = String.valueOf(doubleValue);
	        	}
	        	if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	        		DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	        		domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	        	}
	        }
	        catch(Exception exception)
	        {
	        	exception.printStackTrace();
	        }
	         return 0;
	    }
	    
	    /**
	     * Method will set Rate value to two decimal points 
	     *
	     * @param strObjId String containing object id
	     * @param strValue String containing new value
	     * @param strAttributeName String containing attribute name
	     * @return int flag for trigger
	     * @author CHiPS
	     * @since 418
	     */
	    public static int convertToDoubleRate(Context context ,  String [] args) {
	        double doubleValue = 0.00d;
	        try
	        {
	        	String strObjectIds 	 	= args[0];
	        	String strAttributeNewVlaue = args[1];
	        	String strAttributeName  	= args[2];
	        	if(UIUtil.isNullOrEmpty(strAttributeNewVlaue))
	            {
	        		strAttributeNewVlaue = "0.00";
	            }
	        	int iLangthAfterdecimal    =     strAttributeNewVlaue.substring(strAttributeNewVlaue.indexOf(".")+1, strAttributeNewVlaue.length()).length();
	        	if(iLangthAfterdecimal > 2){            
	            doubleValue			  = Double.parseDouble(strAttributeNewVlaue);
	            BigDecimal bigdecimal = new BigDecimal(doubleValue);
	            bigdecimal 			  = bigdecimal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	            String strbigdecimal  = bigdecimal.toPlainString();
	            doubleValue			  = Double.parseDouble(strbigdecimal);
	            strAttributeNewVlaue = String.valueOf(doubleValue);
	        	}
	        	if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	        		DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	        		domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	        	}
	        }
	        catch(Exception exception)
	        {
	            doubleValue = 0d;
	        }
	         return 0;
	    }
	    
	    
	    /**
	     * Method will set Quantity value to three decimal points 
	     *
	     * @param strObjId String containing object id/Rel id
	     * @param strValue String containing new value
	     * @param strAttributeName String containing attribute name
	     * @return int flag for trigger
	     * @author CHiPS
	     * @since 418
	     * */
	    public static int convertToDoubleObjRelQty(Context context ,  String [] args) {
	    	double doubleValue = 0.000d;
	    	try
	    	{/*
	    		String strObjectIds 	 = args[0];
	    		String strAttributeNewVlaue = args[1];
	    		String strAttributeName  = args[2];
	    		String strRelId  = args[3];
	    		if(UIUtil.isNullOrEmpty(strAttributeNewVlaue))
	    		{
	    			strAttributeNewVlaue = "0.000";
	    		}
	    		int iLangthAfterdecimal    =     strAttributeNewVlaue.substring(strAttributeNewVlaue.indexOf(".")+1, strAttributeNewVlaue.length()).length();
	    		if(iLangthAfterdecimal > 3){
	    			doubleValue=  Double.parseDouble(strAttributeNewVlaue);
	    			BigDecimal bigdecimal = new BigDecimal(doubleValue);
	    			bigdecimal = bigdecimal.setScale(3  , BigDecimal.ROUND_HALF_EVEN);
	    			String strbigdecimal = bigdecimal.toPlainString();
	    			doubleValue=  Double.parseDouble(strbigdecimal);
	    			strAttributeNewVlaue = String.valueOf(doubleValue);
	    			if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	    				DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	    				//domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	    			}
	    			else
	    			{
	    				if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	    					DomainRelationship.setAttributeValue(context, strRelId, strAttributeName, strAttributeNewVlaue);
	    				}

	    			}
	    		}
	    		else{
	    			if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	    				DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	    				domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	    			}
	    			else
	    			{
	    				if(UIUtil.isNotNullAndNotEmpty(args[3])){
	    					DomainRelationship.setAttributeValue(context, strRelId, strAttributeName, strAttributeNewVlaue);
	    				}

	    			}
	    		}

	    	*/}
	    	catch(Exception exception)
	    	{
	    		exception.printStackTrace();
	    	}
	    	return 0;
	    }
	  
	    
	    /**
	     * Method will set Rate value to two decimal points 
	     *
	     * @param strObjId String containing object id/Rel id
	     * @param strValue String containing new value
	     * @param strAttributeName String containing attribute name
	     * @return int flag for trigger
	     * @author CHiPS
	     * @since 418
	     */
	    public static int convertToDoubleObjRelRate(Context context ,  String [] args) {
	        
	        double doubleValue = 0.00d;
	    	try
	    	{
	    		String strObjectIds 	 	= args[0];
	    		String strAttributeNewVlaue = args[1];
	    		String strAttributeName  	= args[2];
	    		String strRelId = args[3];
	    		if(UIUtil.isNullOrEmpty(strAttributeNewVlaue))
	    		{
	    			strAttributeNewVlaue = "0.00";
	    		}
	    		int iLangthAfterdecimal    =     strAttributeNewVlaue.substring(strAttributeNewVlaue.indexOf(".")+1, strAttributeNewVlaue.length()).length();
	    		if(iLangthAfterdecimal > 2){

	    			doubleValue			  = Double.parseDouble(strAttributeNewVlaue);
	    			BigDecimal bigdecimal = new BigDecimal(doubleValue);
	    			bigdecimal 			  = bigdecimal.setScale(2  , BigDecimal.ROUND_HALF_EVEN);
	    			String strbigdecimal  = bigdecimal.toPlainString();
	    			doubleValue			  = Double.parseDouble(strbigdecimal);
	    			strAttributeNewVlaue = String.valueOf(doubleValue);
	    			if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	    				DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	    				domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	    			}
	    			else
	    			{
	    				if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	    					DomainRelationship.setAttributeValue(context, strRelId, strAttributeName, strAttributeNewVlaue);
	    				}

	    			}
	    		}
	    		else
	    		{
	    			if(UIUtil.isNotNullAndNotEmpty(strObjectIds)){
	    				DomainObject domObj = DomainObject.newInstance(context ,  strObjectIds);
	    				domObj.setAttributeValue(context, strAttributeName, strAttributeNewVlaue);
	    			}
	    			else
	    			{
	    				if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	    					DomainRelationship.setAttributeValue(context, strRelId, strAttributeName, strAttributeNewVlaue);
	    				}

	    			}
	    		}
	    	}
	    	catch(Exception exception)
	    	{
	    		exception.printStackTrace();
	    	}
	    	return 0;
	    }
/**
 *  Method will allow person with role Supplier Representative  to see objects only
 *  if he is owner or he is from same company
 *  Applicable to Policy
 *  1.WMSMeasurementBookEntry
 *  2.WMSMaterial
 *  3.WMEAbstractMeasurmentBookEntry
 * 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
 public boolean isSupplierOfSameCompany(Context context,String[] args) throws Exception{
 		 try {
			 
 				 String strObjectID = args[0];
	 			 String strContextUser = context.getUser();
	 			 DomainObject domContextObj= DomainObject.newInstance(context, strObjectID);
	 			 matrix.db.User user = domContextObj.getOwner(context);
	 		     String strComapnyId = PersonUtil.getUserCompanyId(context);
	 			 if(strContextUser.equalsIgnoreCase(user.getName())) {
	 				 return true;
	 			 }else {
	 				     String strRel = RELATIONSHIP_WMS_WO_MATERIAL_BILL;
	 				     String strType = domContextObj.getInfo(context, DomainConstants.SELECT_TYPE);
	 				     if(strType.equalsIgnoreCase(TYPE_WMS_MEASUREMENT_BOOK_ENTRY)) {
	 				    	strRel=RELATIONSHIP_WMS_WORK_ORDER_MBE;
	 				     }else if(strType.equalsIgnoreCase(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY)) {
	 				    	strRel=RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE;
	 				     }
	 				     
	 				    String strContractorCmpny= domContextObj.getInfo(context, "to["+strRel+"].from.to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+ "].from.name");
	 				    DomainObject domUserCompany = DomainObject.newInstance(context, strComapnyId);
	 				    String strUSerCompany = domUserCompany.getInfo(context, DomainConstants.SELECT_NAME);
	 				    if(strContractorCmpny.equalsIgnoreCase(strUSerCompany)) {
	 				    	   	 return true;
	 	 			     }
	 			 }
	 			  
			 
		    }catch(Exception e) {
			 e.printStackTrace();
		 }
 		 return false;
	 }   
	    
	    
}

