/** Name of the JPO    : WMSMeasurementBookItem
 ** Developed by    : Matrixone 
 ** Client            : WMS
 ** Description        : The purpose of this JPO is to create a Measurement book items like segment , work order , items , non project items and its functionalities 
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/
// WO Assignees - Start
import java.io.StringReader;
import java.util.ArrayList;
// WO Assignees - End
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

// WO Assignees - Start
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.joda.time.Days;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
// WO Assignees - End

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import matrix.util.Pattern;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIUtil;



// MBE - Start
import com.matrixone.apps.common.Person;
import com.matrixone.apps.program.ProgramCentralConstants;
// MBE - End
/**
 * The purpose of this JPO is to create a Measurement book items like segment , work order , items , non project items and its functionalities 
 * @author  DS
 * @version R418 - Copyright (c) 1993-2016 Dassault Systems.
 */
public class WMSMeasurementBookItem_mxJPO extends WMSConstants_mxJPO   
{
	 
    /**
     * Create a new ${CLASS:MarketingFeature} object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since R418
     */

    public WMSMeasurementBookItem_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

	
    /**
     * get list of all BOQ items
     * Used for BOQ table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args 
     * @returns MapList - all BOQ items
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 417
     */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getBOQItems(Context context,String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String sWMSMBEItemType = (String) programMap.get("MBEItemType");
		if (sWMSMBEItemType == null || "".equals(sWMSMBEItemType)) {
			sWMSMBEItemType = "Normal";
		}
        MapList mapListObjects = new MapList();
        StringList strListBusSelects     = new StringList(3);
        strListBusSelects.add(DomainConstants.SELECT_ID);
		strListBusSelects.add("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
        strListBusSelects.add("attribute["+"WMSItemRateEscalation"+"].value");
	strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"].value");

		StringList strListRelSelects = new StringList(1);
        strListRelSelects.add(DomainRelationship.SELECT_ID);
        Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
        patternType.addPattern(TYPE_WMS_SEGMENT);
        patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK);
        Pattern patternRel = new Pattern(RELATIONSHIP_BILL_OF_QUANTITY);     
        String strObjId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
        if(UIUtil.isNotNullAndNotEmpty(strObjId)){
            DomainObject domObj = DomainObject.newInstance(context, strObjId);
           
            mapListObjects = domObj.getRelatedObjects(context,
            	                     	                    RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
															patternType.getPattern(),                                    // object pattern
															false,                                                        // to direction
															true,                                                       // from direction
															(short)2,                                                      // recursion level
															strListBusSelects,                                                 // object selects
															null,                                                         // relationship selects
															DomainConstants.EMPTY_STRING,                                // object where clause
															DomainConstants.EMPTY_STRING,                                // relationship where clause
															(short)0,                                                      // No expand limit
															DomainConstants.EMPTY_STRING,                                // postRelPattern
															TYPE_WMS_MEASUREMENT_TASK+","+TYPE_WMS_SEGMENT, // postTypePattern
															null); 
        }
        return mapListObjects;
    }

	/**
	 * Check whether column is having Edit Access or not depending on sate of object, Checking For SOR State For NIT
	 * @param context The Matrix Context object
	 * @param  
	 * @param args holds the following input arguments:
	 *     1) ObjectList : List of objects in table
	 *     2) ProgramMap : Contains all info about table columns        
	 * @throws Exception if the operation fails
	 */   
	public StringList checkBOQNITEdit(Context context,String args[]) throws Exception
	{
		StringList strListAccess = new StringList();;
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator<Map<String,String>> objectListIterator = objectList.iterator();
			
			HashMap requestMap = (HashMap)programMap.get("requestMap");
            String strWorkOrderOID = (String)requestMap.get("objectId");
			DomainObject domWOObj = DomainObject.newInstance(context, strWorkOrderOID);
			String strContextUser = context.getUser();
            StringList strListInfo = new StringList(4);
			strListInfo.add(DomainConstants.SELECT_OWNER);
		    strListInfo.add(DomainConstants.SELECT_CURRENT);
			Map<String,String> mapInfo = domWOObj.getInfo(context, strListInfo);
		    String strWOOwner 	= mapInfo.get(DomainConstants.SELECT_OWNER);
		    String strWOCurrent = mapInfo.get(DomainConstants.SELECT_CURRENT);
			String strCommandName = (String)requestMap.get("portalCmdName");
			Map<String,String> mapData ;
			while (objectListIterator.hasNext()) 
			{
				mapData = objectListIterator.next();
				if(strWOOwner.equals(strContextUser))
				{
					if("Create".equals(strWOCurrent)||(("WMSPaymentMBE".equalsIgnoreCase(strCommandName) || "WMSRebateMBE".equalsIgnoreCase(strCommandName)) && "Active".equals(strWOCurrent)))
					{
						strListAccess.add(String.valueOf(true));
					}
					else
					{
						strListAccess.add(String.valueOf(false));
					}
			    }
				else{
					strListAccess.add(String.valueOf(false));
			    }
			}

		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return strListAccess;

	}
    /**
     * Method is used to show Rate Escalation on BOQ table
     * @param context The Matrix Context object
     * @param args holds the arguments:     
     * @throws Exception if the operation fails
     */ 
	@com.matrixone.apps.framework.ui.ColJPOCallable
    public Vector getRateEscalation(Context context, String[] args) throws Exception
    {
        try{
            Map programMap                  = (Map)JPO.unpackArgs(args);
            MapList objectList              = (MapList)programMap.get("objectList");
            Iterator iterator               = objectList.iterator();
            Vector returnList               = new Vector();
            Map<String,String> mapData      = null;
            String strValue                 = DomainConstants.EMPTY_STRING;
            String strItemRateEscalation    = DomainConstants.EMPTY_STRING;
            String strQuantity              = DomainConstants.EMPTY_STRING;
            String strObjectId              = DomainConstants.EMPTY_STRING;
			String strBOQType             = DomainConstants.EMPTY_STRING;
            DomainObject domObj = DomainObject.newInstance(context);

            while(iterator.hasNext())
            {
                mapData     = (Map<String,String>) iterator.next();
                
                strQuantity = (String) mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_RATE_ESCALATION+"]");
                if(UIUtil.isNullOrEmpty(strQuantity))
                {
					if(TYPE_WMS_SEGMENT.equals(strBOQType))
					{
						returnList.add(DomainConstants.EMPTY_STRING);
					}
					else
					{
						strObjectId = (String)mapData.get("id");
						domObj.setId(strObjectId);
						strValue    = "true".equalsIgnoreCase(strValue)?"Yes" : "No";
						returnList.add(strValue);
					}
                }
                else if(DomainConstants.EMPTY_STRING.equals(strQuantity))
                {
                    returnList.add(DomainConstants.EMPTY_STRING);
                }
                else
                {
                    if("true".equalsIgnoreCase(strQuantity))
                    {
                        returnList.add("Yes");
                    }
                    else
                    {
                        returnList.add("No");
                    }
                }
            }
            return returnList;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

	/**
     * Method is used to update Rate Escalation on BOQ table
     * @param context The Matrix Context object
     * @param args holds the arguments:     
     * @throws Exception if the operation fails
	 */ 
	@com.matrixone.apps.framework.ui.CellUpdateJPOCallable
	public void updateRateEscalation(Context context, String[] args) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		String newEscaltionValue = (String) paramMap.get("New Value");
		if(UIUtil.isNotNullAndNotEmpty(objectId) && UIUtil.isNotNullAndNotEmpty(newEscaltionValue))
		{
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			domObj.setAttributeValue(context, ATTRIBUTE_WMS_ITEM_RATE_ESCALATION, newEscaltionValue);
		}
	}
	/**
	 * Check whether column is having Edit Access or not depending on sate of object, Checking For SOR State
	 * @param context The Matrix Context object
	 * @param  
	 * @param args holds the following input arguments:
	 *     1) ObjectList : List of objects in table
	 *     2) ProgramMap : Contains all info about table columns        
	 * @throws Exception if the operation fails
	 */    
	public StringList checkForSegmentType(Context context,String args[]) throws Exception
	{
		StringList strListAccess = new StringList();;
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator<Map<String,String>> objectListIterator = objectList.iterator();
            HashMap requestMap           = (HashMap)programMap.get("requestMap");
            String strWorkOrderOID     = (String)requestMap.get("objectId");
            
			DomainObject domWOObj = DomainObject.newInstance(context, strWorkOrderOID);
            String strContextUser = context.getUser();
			
			
		    StringList strListInfo = new StringList(4);
		    strListInfo.add(DomainConstants.SELECT_OWNER);
		    strListInfo.add(DomainConstants.SELECT_CURRENT);
			
			Map<String,String> mapInfo = domWOObj.getInfo(context, strListInfo);
		    String strWOOwner 	= mapInfo.get(DomainConstants.SELECT_OWNER);
		    String strWOCurrent = mapInfo.get(DomainConstants.SELECT_CURRENT);
			boolean booleanWOChecks = false;
			if( strWOOwner.equals(strContextUser) && "Create".equals(strWOCurrent) )
			{
				booleanWOChecks = true;
			}
			while (objectListIterator.hasNext()) {
               
				Map<String,String> mapData = objectListIterator.next();

				String strBOQType = mapData.get(DomainConstants.SELECT_TYPE);
				
				if(booleanWOChecks)
				{
      			   if(TYPE_WMS_SEGMENT.equals(strBOQType))
				   {
					strListAccess.add(String.valueOf(false));
				   }
				   else
				   {
					strListAccess.add(String.valueOf(true));
				   }   
				}
				else
				{
					strListAccess.add(String.valueOf(false));
				}
			}

		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return strListAccess;

	}
	/** RELATIONSHIP_BOQ
	  * Method will add measurement book to the work order
	  * 
	  * @param context the eMatrix <code>Context</code> object
	  * @param args with program arguments
	  *             args[0]- Workorder OID
	  * @throws Exception if the operation fails
	  * @author CHiPS
	  * @since 418
	  */
	 public void addMeasurementBook(Context context, String[] args) throws Exception {
		 try {
			 String strWOOID = args[0];
			 if(UIUtil.isNotNullAndNotEmpty(strWOOID))
			 {
				 String strMBOID = FrameworkUtil.autoName(context,
						 "type_WMSMeasurementBook",
						 "policy_WMSMeasurementItem");
				 if(UIUtil.isNotNullAndNotEmpty(strMBOID))
				 {
					StringList strListInfoSelects = new StringList(3);
					strListInfoSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
					strListInfoSelects.add(DomainConstants.SELECT_DESCRIPTION);
					strListInfoSelects.add(DomainConstants.SELECT_NAME);
					DomainObject domObjWO = DomainObject.newInstance(context, strWOOID);
					DomainObject domObjMB = DomainObject.newInstance(context, strMBOID);
					Map<String,String> mapWOInfo =  domObjWO.getInfo(context,  strListInfoSelects);
					String strName = mapWOInfo.get(DomainConstants.SELECT_NAME);
					domObjWO.setName(context,strName);
					domObjMB.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, mapWOInfo.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value"));
					domObjMB.setDescription(context, mapWOInfo.get(DomainConstants.SELECT_DESCRIPTION));
					DomainRelationship.connect(context, strWOOID, RELATIONSHIP_BILL_OF_QUANTITY, strMBOID, true);
				 }
			 }
		 }
		 catch(Exception exception)
		 {
			 exception.printStackTrace();
			 throw exception;
		 }
	 }

	// WO Assignees - Start
    /**
	 * Method to get the connected Assignees to the work order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mapListConnectedMBEs MapList containing the Person data
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWorkOrderAssignees (Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListAssignees = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjWO = DomainObject.newInstance(context, strObjectId);
                String strWorkOrderOwner = domObjWO.getInfo(context, DomainConstants.SELECT_OWNER);
                mapListAssignees = getWorkOrderAssignees(context, domObjWO);
                Iterator iterator   = mapListAssignees.iterator();
                while (iterator.hasNext()) {
                    Map mapAssigneInfo = (Map) iterator.next();
                    String strAssignee = (String) mapAssigneInfo.get(DomainConstants.SELECT_NAME);
                    String strRelAttr = (String) mapAssigneInfo.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_ROLE+"]");
                    if (strWorkOrderOwner.equalsIgnoreCase(strAssignee)) {
                        mapAssigneInfo.put("disableSelection", "true");
                        mapAssigneInfo.put("RowEditable", "readonly");
                    }
					if(strRelAttr.equalsIgnoreCase(DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE)){
                        mapAssigneInfo.put("disableSelection", "true");
                        mapAssigneInfo.put("RowEditable", "readonly");
                    }
                }
			}
			return mapListAssignees;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * Method to get the connected Assignees to the work order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjWO domain object instance of work order
	 * @return mapListConnectedMBEs MapList containing the Person data
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public static MapList getWorkOrderAssignees(Context context, DomainObject domObjWO) throws FrameworkException
	{
		try
		{
			StringList strListBusSelects     = new StringList(1);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add(DomainConstants.SELECT_NAME);
			strListBusSelects.add("attribute[Project Role]");
			StringList strListRelSelects = new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			strListRelSelects.add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_ROLE+"]");
			
             MapList mapListAssignees = domObjWO.getRelatedObjects(context, // matrix context
            		                                                 RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE, // relationship pattern
																	DomainConstants.TYPE_PERSON, // type pattern
																	strListBusSelects, // object selects
																	strListRelSelects, // relationship selects
																	true, // to direction
																	false, // from direction
																	(short) 1, // recursion level
																	DomainConstants.EMPTY_STRING, // object where clause
																	DomainConstants.EMPTY_STRING, // relationship where clause
																	0);
			return mapListAssignees;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}

	/**
	 * Method to get the connected Assignees to the work order
	 *and show display role
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjWO domain object instance of work order
	 * @return mapListConnectedMBEs MapList containing the Person data
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ColJPOCallable
	public Vector getDisplayProjectRoleSearchPage (Context context, String[] args) throws Exception{
		Map projectRoleMap = new HashMap();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String strPersonId = DomainConstants.EMPTY_STRING;
		Map<String,String> mapDataObj;
		Iterator<Map<String,String>> iteratorObjList = objectList.iterator();
		String[] PersonIdArray = new String[objectList.size()];
		int i=0;
		while(iteratorObjList.hasNext())
		{
			
			mapDataObj = iteratorObjList.next();
			strPersonId = mapDataObj.get("id");
			PersonIdArray[i] = strPersonId;
			i++;
			
		}
		StringList slBusSelect = new StringList();
		slBusSelect.add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_ROLE+"]");
		slBusSelect.add(DomainConstants.SELECT_NAME);
		MapList mlPersonRoleData = DomainObject.getInfo(context, PersonIdArray, slBusSelect);
		///////////Read Project role XML here/////
		String strDepartmentName = DomainConstants.EMPTY_STRING;
		MapList mapListDepartment = WMSUtil_mxJPO.getContextUserDepartment(context);
		Iterator<Map<String,String>> iterator = mapListDepartment.iterator();
		Map<String,String> mapData;
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			strDepartmentName = mapData.get(DomainConstants.SELECT_NAME);
		}
		String MQLResult = MqlUtil.mqlCommand(context, "print page $1 select content dump", "ProjectDepartmentRole");
		DocumentBuilder db      = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is          = new InputSource();
		is.setCharacterStream(new StringReader(MQLResult));
		org.w3c.dom.Document doc    = db.parse(is);
		NodeList department  = doc.getElementsByTagName("department");
		for (int ii = 0; ii < department.getLength(); ii++) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) department.item(i);
			String strDeptName = element.getAttribute("id");
			if(strDeptName.equalsIgnoreCase(strDepartmentName)) {
				NodeList role = element.getElementsByTagName("Role");
				for(int j= 0;j<role.getLength();j++) {
					org.w3c.dom.Element roleElement = (org.w3c.dom.Element) role.item(j);
					String strSchemaRole = roleElement.getAttribute("schemaRole");
					Iterator projectInfoListIterator = mlPersonRoleData.iterator();
					while(projectInfoListIterator.hasNext()){
						Map<String,String> projectInfoMap = (Map)projectInfoListIterator.next();
						String strActualRole = projectInfoMap.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_ROLE+"]");
						if(strSchemaRole.equals(strActualRole)) {
							projectRoleMap.put(projectInfoMap.get(DomainConstants.SELECT_NAME),roleElement.getAttribute("displayRole"));
						}
					}
				}
			}
		}
		///////////////////////////////////
		Vector columnValues = new Vector(mlPersonRoleData.size());
		Iterator mlPersonRoleDataIterator = mlPersonRoleData.iterator();
		while(mlPersonRoleDataIterator.hasNext()){
			Map<String,String> objectMap = (Map)mlPersonRoleDataIterator.next();
        	String name = (String)objectMap.get(DomainConstants.SELECT_NAME);
        	columnValues.addElement(projectRoleMap.get(name));
		}
		return columnValues;
		
		
	}
    /** 
     * Method will copy Project Member to Work Order
     * 
     * @param context the eMatrix <code>Context</code> object    
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    public void connectWOMembers(Context context, String args[]) throws Exception {
    	try {
    		String strProjectId = args[0];
    		String  strWOId = args[1];
    		ArrayList<String>arrayListIds = new ArrayList<String>();
    		StringList strListBusSelects = new StringList(DomainConstants.SELECT_ID);
    		if(UIUtil.isNotNullAndNotEmpty(strWOId)&& UIUtil.isNotNullAndNotEmpty(strProjectId)) {
				DomainObject domWO = DomainObject.newInstance(context,strWOId);
				DomainObject domProject = DomainObject.newInstance(context, strProjectId);
				StringList slRelSels = new StringList();
				slRelSels.add("attribute["+MemberRelationship.ATTRIBUTE_PROJECT_ROLE+"]");
				Map<String,String> mPeron=new HashMap<String,String>();
				MapList mapListObjects = domProject.getRelatedObjects(context, // matrix context
																		DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
																		DomainConstants.TYPE_PERSON, // type pattern
																		strListBusSelects, // object selects
																		slRelSels, // relationship selects
																		false, // to direction
																		true, // from direction
																		(short) 0, // recursion level
																		DomainConstants.EMPTY_STRING, // object where clause
																		DomainConstants.EMPTY_STRING, // relationship where clause
																		0);
				//WMS Code added to add Contractor Supplier Representative as WorkOrder Memeber
				 String strWOContractor = domWO.getInfo(context, "to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.id");
				 
				 Organization organizationObj = (Organization) DomainObject.newInstance(context,strWOContractor);
                 StringList orgObjSelects = new StringList(1);
                 orgObjSelects.add(DomainConstants.SELECT_NAME);
                 orgObjSelects.add(DomainConstants.SELECT_ID);
				 MapList personList = (MapList)organizationObj.getMemberPersons(context,orgObjSelects,null, DomainConstants.RELATIONSHIP_MEMBER,DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE);
	         	Map mapObj = new HashMap();
				String strObjId = "";
				String[] personId=new String[mapListObjects.size()+personList.size()];
				String strProjectRole=DomainConstants.EMPTY_STRING;
				 int cnt=0;
				for(int i=0;i<mapListObjects.size();i++)
				{
					mapObj = (Map)mapListObjects.get(i);
				 
						strObjId = (String)mapObj.get(DomainConstants.SELECT_ID);
						strProjectRole= (String)mapObj.get("attribute["+MemberRelationship.ATTRIBUTE_PROJECT_ROLE+"]");
						mPeron.put(strObjId, strProjectRole);
						personId[cnt]=strObjId;
					 	arrayListIds.add(strObjId);
					 	cnt++;
						 
					 
				}
			 
				//get contractor person dont know number of Supplier Representative ..
				 
		     	 for(int i=0;i<personList.size();i++)
                 {
                     Map objectMap =  (Map)personList.get(i);
                     strObjId=(String)objectMap.get(DomainConstants.SELECT_ID);
                     arrayListIds.add(strObjId);
                     mPeron.put(strObjId, DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE);
					 personId[cnt]=strObjId;
					 cnt++;
                  }
		     	 
		     	Map<String,String> mRelData = DomainRelationship.connect(context, domWO, new RelationshipType(RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE), false, personId);
		     	DomainRelationship domWOAssignee=null;
                for(int i=0;i<personId.length;i++) {
                	domWOAssignee=DomainRelationship.newInstance(context, (String) mRelData.get(personId[i]));
                	domWOAssignee.setAttributeValue(context, ATTRIBUTE_WMS_WORK_ORDER_ROLE, (String) mPeron.get(personId[i]));
                	
                }
		     	 
		     	
				// Map<String, String> mRelIds = ${CLASS:WMSUtil}.connect(context, domWO, RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE, false, arrayListIds);
    		}
    	}
    	catch(Exception exception)
    	{
    		System.out.println("exception   "+exception.getMessage());
    		exception.printStackTrace();
    		throw exception;            
    	}
    }
    /**
	 * Trigger Program To update the values for Project Role on Relationship
	 * @ args - Method arguments 
	 * arg1 - To side object id
	 * @param args2 - Relationship id
	 * @return 
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateProjectRoleAttributes(Context context,String[]args) throws Exception
	{
		boolean isContextPushed = false;
		try {
			String strObjectID = args[0];
			String strWOId = args[1];
			String strRELID = args[2];
			
			if(UIUtil.isNotNullAndNotEmpty(strWOId) && UIUtil.isNotNullAndNotEmpty(strObjectID))
			{
				DomainObject domObjToObject = DomainObject.newInstance(context, strObjectID);
				String strProjectRole = (String)domObjToObject.getInfo(context, "attribute["+ATTRIBUTE_WMS_DEFAULT_ROLE+"]");
				DomainObject domWO = DomainObject.newInstance(context,strWOId);
				String strProjectId = domWO.getInfo(context, "to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.id");
				if(UIUtil.isNotNullAndNotEmpty(strProjectId)) {
					String sWhere = "id=="+strObjectID;
					StringList strListBusSelects = null;
					StringList slRelSels = new StringList(1);
					slRelSels.add("attribute["+MemberRelationship.ATTRIBUTE_PROJECT_ROLE+"]");
					DomainObject domProject = DomainObject.newInstance(context, strProjectId);
					MapList mapListObjects = domProject.getRelatedObjects(context, // matrix context
																			DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
																			DomainConstants.TYPE_PERSON, // type pattern
																			strListBusSelects, // object selects
																			slRelSels, // relationship selects
																			false, // to direction
																			true, // from direction
																			(short) 0, // recursion level
																			sWhere, // object where clause
																			DomainConstants.EMPTY_STRING, // relationship where clause
																			0);
					if (mapListObjects.size() > 0) {
						Map objMap = (Map)mapListObjects.get(0);
						strProjectRole = (String)objMap.get("attribute["+MemberRelationship.ATTRIBUTE_PROJECT_ROLE+"]");
					}
				}
				DomainRelationship drRecRel = new DomainRelationship(strRELID);
				ContextUtil.pushContext(context);
				isContextPushed = true;
				drRecRel.setAttributeValue(context, ATTRIBUTE_WMS_WORK_ORDER_ROLE, strProjectRole);
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		finally
		{
			if (isContextPushed)
			{
				ContextUtil.popContext(context);
			}
		}
	}

    /**
     * Include oid program
     * shows only Subordinate of context user and Contractors of Work Order    
     * @param context
     * @return StringList containing information of payment items to show for connection
     * @throws Exception if the operation fails
     */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getMemberForWO(Context context, String[] args) throws Exception 
    {
    	StringList slIncludeMemeber = new StringList();
        try {            
            StringList slObjectSelects = new StringList(1);
            slObjectSelects.add(DomainConstants.SELECT_ID);
            String  strOwnerId = PersonUtil.getPersonObjectID(context, context.getUser());
            DomainObject domWOOwner = DomainObject.newInstance(context,strOwnerId);
            String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
            DomainObject domWO = DomainObject.newInstance(context,strObjectId);
            MapList mapListObjects = domWOOwner.getRelatedObjects(context, // matrix context
																	RELATIONSHIP_REPORTING_MANAGER, // relationship pattern
																	DomainConstants.TYPE_PERSON, // type pattern
																	slObjectSelects, // object selects
																	DomainConstants.EMPTY_STRINGLIST, // relationship selects
																	false, // to direction
																	true, // from direction
																	(short) 0, // recursion level
																	DomainConstants.EMPTY_STRING, // object where clause
																	DomainConstants.EMPTY_STRING, // relationship where clause
																	0);
            Map mapObj = new HashMap();
            String strObjId = DomainConstants.EMPTY_STRING;
            for(int i=0;i<mapListObjects.size();i++)
            {
                mapObj = (Map)mapListObjects.get(i);
                if(mapObj != null && !mapObj.isEmpty())
                {
                    strObjId = (String)mapObj.get(DomainConstants.SELECT_ID);
                    if(UIUtil.isNotNullAndNotEmpty(strObjId))
                    {
                    	slIncludeMemeber.add(strObjId);
                    }
                }
            }
            StringList slContractor = domWO.getInfoList(context,"to[Responsible Organization].from.id");
            String strMemberId = DomainConstants.EMPTY_STRING;
            if(slContractor!=null&&!slContractor.isEmpty())
            {
            	for(int i=0;i<slContractor.size();i++)
            	{
            		MapList mlOrgMember = getWorkOrderContractorMember(context,(String)slContractor.get(i));
            		if(mlOrgMember.size()==1)
                    {
                    	Map map = (Map)mlOrgMember.get(0);
                    	if(map != null && !map.isEmpty())
                    	{
                    		strMemberId = (String)map.get(DomainConstants.SELECT_ID);
                    		slIncludeMemeber.add(strMemberId);
                    	}                	
                    } 
            	}
            }
			String strProjectID 	= (String) domWO.getInfo(context, "to[" + RELATIONSHIP_WMS_PORJECT_WORK_ORDER + "].from.id");
			if (UIUtil.isNotNullAndNotEmpty(strProjectID)) {
				DomainObject domProject 		= DomainObject.newInstance(context, strProjectID);
				MapList mapProjectMemberList 	= getProjectConnectMembers(context, domProject);
				Iterator<Map<String, String>> iterator = mapProjectMemberList.iterator();
				String strProjectMemberId 		= DomainConstants.EMPTY_STRING;
				Map<String, String> mapMemberData;
				while (iterator.hasNext()) {
					mapMemberData 		= iterator.next();
					strProjectMemberId 	= mapMemberData.get(DomainConstants.SELECT_ID);
					if (!slIncludeMemeber.contains(strProjectMemberId)) {
						slIncludeMemeber.add(strProjectMemberId);
					}
				}
			}
        }
        catch (Exception e) {
            throw e;
        }
        return slIncludeMemeber;
    }    
    
	/**
	 * Method to get the connected Assignees to the work order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return strListAssignees StringList containing the Person IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getExcludeWorkOrderAssignees (Context context, String[] args) throws Exception 
	{
		try
		{
			StringList strListAssignees = new StringList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				MapList mapListAssignees = new MapList();
				DomainObject domObjWO = DomainObject.newInstance(context, strObjectId);
				mapListAssignees = getWorkOrderAssignees(context, domObjWO);
				strListAssignees = WMSUtil_mxJPO.convertToStringList(mapListAssignees, DomainObject.SELECT_ID);
			}
			return strListAssignees;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}

    /** 
     * Method will search contractor organization memebers of Work Order
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param strOrganization - Contractor id
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    public MapList getWorkOrderContractorMember(Context context, String strOrganization) throws Exception {
    	MapList mlWOMember = new MapList();
        try {
           StringList slObjectSelects = new StringList(1);
                slObjectSelects.add(DomainConstants.SELECT_ID);
                StringList slRelSelects = new StringList(1);
                slRelSelects.add(DomainRelationship.SELECT_ID);
                DomainObject domOrgnization = DomainObject.newInstance(context,strOrganization);
                mlWOMember = domOrgnization.getRelatedObjects(context, // matrix context
                		DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
                       DomainConstants.TYPE_PERSON, // type pattern
                       slObjectSelects, // object selects
                       slRelSelects, // relationship selects
                        false, // to direction
                        true, // from direction
                        (short) 0, // recursion level
                        DomainConstants.EMPTY_STRING, // object where clause
                        DomainConstants.EMPTY_STRING, // relationship where clause
                        0);
        }
        catch(Exception exception)
        {
            System.out.println("exception   "+exception.getMessage());
            exception.printStackTrace();
            throw exception;            
        }
        
        return mlWOMember;
    }

	/**
	 * Method to get the connected members to Project Space
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param domProjectObj domain object instance of work order
	 * @return mapListConnectedMBEs MapList containing the Person data
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public static MapList getProjectConnectMembers(Context context, DomainObject domProjectObj) throws FrameworkException {
		try
		{
			StringList strListBusSelects     = new StringList(1);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add(DomainConstants.SELECT_NAME);
			strListBusSelects.add("attribute[Project Role]");
			StringList strListRelSelects     = new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
            MapList mapListAssignees = domProjectObj.getRelatedObjects(context, // matrix context
            		DomainConstants.RELATIONSHIP_MEMBER, // relationship pattern
					DomainConstants.TYPE_PERSON, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
                    false, // to direction
                    true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			return mapListAssignees;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}

	// WO Assignees - End
	
	// MBE - Start
	
	/**
	 * Used in Policy access filters to check if the context user is a contractor for respective work order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the ObjectId
	 *
	 * @return boolean - true (if function that is getting called provides access),
	 *                   false(if function that is getting called does not provide access)
	 *                   if function is not there return the  default value true
	 * @throws Exception if the operation fails
	 */
	public String isContextUserContractor (Context context, String[] args)throws Exception
	{
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
		boolean isContextPushed = true;
		Boolean booleanReturnValue = new Boolean(false);
		try
		{
			String strObjectOID = (String) args[0];
			String strContextUser = context.getUser();
			ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			if(UIUtil.isNotNullAndNotEmpty(strObjectOID))
			{
				MapList mapListAssignees = getWorkOrderAssignees(context, strObjectOID);
				booleanReturnValue = checkForProjectRole(context, mapListAssignees, strContextUser, "Contractor");
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		finally
		{
			if (isContextPushed)
			{
				ContextUtil.popContext(context);
			}

		}
		return booleanReturnValue.toString();
	}
	
    /**
	 * Method to get the connected Assignees to the work order from the context Abstract MBE OID
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param strAbsMBEOID String containing the context abstrac MBE OID
	 * @return mapListAssignees MapList containing the Person data
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
    public static MapList getWorkOrderAssignees(Context context, String strAbsMBEOID) throws FrameworkException {
		try
		{
			StringList strListBusInfo = new StringList(2);
			strListBusInfo.add(DomainConstants.SELECT_ID);
            strListBusInfo.add(Person.SELECT_NAME);
			StringList strListRelInfo = new StringList(2);
			strListRelInfo.add(DomainRelationship.SELECT_ID);
			strListRelInfo.add("attribute["+ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE+"].value");
			DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbsMBEOID);
			Pattern patternType = new Pattern(TYPE_WMS_WORK_ORDER);
			patternType.addPattern(DomainConstants.TYPE_PERSON);
			Pattern patternRel = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_MBE);
			patternRel.addPattern(RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE);
			MapList mapListAssignees = domObjAbsMBE.getRelatedObjects(context,
																		patternRel.getPattern(),			 			// relationship pattern
																		patternType.getPattern(),									// object pattern
																		true,														// to direction
																		true,                               						// from direction
																		(short)2,								  					// recursion level
																		strListBusInfo,                         						// object selects
																		strListRelInfo,                         								// relationship selects
																		null,								// object where clause
																		DomainConstants.EMPTY_STRING,								// relationship where clause
																		(short)0,								  					// No expand limit
																		RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE,                        		// postRelPattern
																		(DomainConstants.TYPE_PERSON),												// postTypePattern
																		null);                              						// postPatterns
			return mapListAssignees;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/**
	 * Checks if the list of context user is a assignee of Work Order and assigned with contractor role
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param mapListAssignees MapList containing the Work Order assignees
	 * @param strContextUser String containing the context user
	 * @return boolean - true (context user is a assignee of Work Order and assigned with contractor role),
	 *                   false(context user is neither  a assignee of Work Order nor assigned with contractor role)
	 *                   if function is not there return the  default value false
	 */
	private Boolean checkForProjectRole(Context context,  MapList mapListAssignees,String strContextUser,String strProjectRole) {
		Boolean booleanReturnValue = new Boolean(false);
		StringList strListRoles = FrameworkUtil.split(strProjectRole, ",");
		Map<String,String> mapData;
		Iterator<Map<String,String>> iterator = mapListAssignees.iterator();
        String strUser = DomainConstants.EMPTY_STRING;
        String strRole = DomainConstants.EMPTY_STRING;
		while(iterator.hasNext())
		{
			mapData = iterator.next();
            strUser = mapData.get(Person.SELECT_NAME);
            strRole = mapData.get("attribute["+ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE+"].value");
            if(strUser.equals(strContextUser)&&strListRoles.contains(strRole))
			{
				booleanReturnValue = new Boolean(true);
				break;
			}
		}
		return booleanReturnValue;
	}
	// MBE - End
	
	
	
	/**
	 * Check whether column is having Edit Access or not depending on sate of object, Checking For SOR State
	 * @param context The Matrix Context object
	 * @param  
	 * @param args holds the following input arguments:
	 *     1) ObjectList : List of objects in table
	 *     2) ProgramMap : Contains all info about table columns        
	 * @throws Exception if the operation fails
	 */    
	public StringList checkBOQRateEdit(Context context,String args[]) throws Exception
	{
		StringList strListAccess = new StringList();;
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator<Map<String,String>> objectListIterator = objectList.iterator();
			
			HashMap requestMap           = (HashMap)programMap.get("requestMap");
            String strWorkOrderOID     = (String)requestMap.get("objectId");
			
			DomainObject domWOObj = DomainObject.newInstance(context, strWorkOrderOID);
            String strContextUser = context.getUser();
			
		    StringList strListInfo = new StringList(4);
		    strListInfo.add(DomainConstants.SELECT_OWNER);
		  //  strListInfo.add("attribute["+CHiPSDomainConstant.ATTRIBUTE_CHIPS_FORM_TYPE+"]");
		    strListInfo.add(DomainConstants.SELECT_CURRENT);

		    Map<String,String> mapInfo = domWOObj.getInfo(context, strListInfo);
		    String strWOOwner 	= mapInfo.get(DomainConstants.SELECT_OWNER);
		    String strWOCurrent = mapInfo.get(DomainConstants.SELECT_CURRENT);
			///String strFormType  = mapInfo.get("attribute["+CHiPSDomainConstant.ATTRIBUTE_CHIPS_FORM_TYPE+"]");
			boolean booleanWOChecks = false;
			if( strWOOwner.equals(strContextUser) && "Create".equals(strWOCurrent) )
			{
				booleanWOChecks = true;
			}
			Map<String,String> mapData ;
			while (objectListIterator.hasNext()) 
			{
               	mapData = objectListIterator.next();				    				
				String strSORConnected =   mapData.get("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");   				
				if(booleanWOChecks)
				{
					 
						if("False".equalsIgnoreCase(strSORConnected))
						{
							strListAccess.add(String.valueOf(true));
						}
						else
						{
							strListAccess.add(String.valueOf(false));
						}
					}
				}
				 
			

		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return strListAccess;

	}
	
	
	public Vector getItemType(Context context, String[] args) throws Exception 
	{
        try {
            Vector vColumnValues = new Vector();
            Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			String[] strObjOIDs = getObjectOIDS(objectList);
			StringList slSels = new StringList("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"]");
			MapList mlInfo = DomainObject.getInfo(context, strObjOIDs, slSels);
			Iterator<Map<String,String>> iterator  = mlInfo.iterator();
			Map<String,String> objMap;
			while(iterator.hasNext())
			{
				objMap = (Map<String,String>) iterator.next();
				String sItemType = objMap.get("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"]");
				if("Payment".equals(sItemType) || "Rebate".equals(sItemType)){
					vColumnValues.add(sItemType);
				}
				else {
					vColumnValues.add("");
				}
			}
            return vColumnValues;
        }
        catch (Exception e) {
            throw new Exception("Error while getting up to date amount:"+e.getMessage());
        }
	}
	
	 /**
     * Method to get the object OIDs as array from the MapList
     *
     * @param objectList MapList containing the Object List info
     * @return strObjOIDs String[] containing the Object OIDs
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    private String[] getObjectOIDS(MapList objectList) {
        Iterator<Map<String,String>> iterator  = objectList.iterator();
        ArrayList<String> arrayListOIDs = new ArrayList<String>(objectList.size());
        Map<String,String> mapData;
        while(iterator.hasNext())
        {
            mapData =  iterator.next();
            String strObjOID = mapData.get(DomainConstants.SELECT_ID);
            arrayListOIDs.add(strObjOID);
        }
        int intSize = arrayListOIDs.size();
        String [] strObjOIDs = arrayListOIDs.toArray(new String[intSize]);
        return strObjOIDs;
    }
    
    
    public StringList showColorForPaymentAndRebateItems(Context context, String[] args)  throws Exception 
	{
		try 
		{
			StringList slOutput = new StringList();
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				Map mapObjectInfo = (Map) itrTableRows.next();
				String sObjectid = (String)mapObjectInfo.get("id");
				DomainObject doItem = DomainObject.newInstance(context, sObjectid);
				String sItemType = doItem.getAttributeValue(context, ATTRIBUTE_WMS_MBE_ITEM_TYPE);
				if("Payment".equals(sItemType) || "Rebate".equals(sItemType)) {
					slOutput.addElement("RowBackGroundColor");
				}
				else {
					slOutput.addElement("");
				}
			}
			return slOutput;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}
	
    public boolean showItemType(Context context, String[] args) throws Exception 
    {/*
        try
        {
			boolean showItemTypeColumn = false;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sObjectId = (String) programMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
			DomainObject doMeasurementBookItem = DomainObject.newInstance(context,sObjectId);
			StringList slObjectSelects = new StringList();
			slObjectSelects.add("to["+CHiPSDomainConstant.RELATIONSHIP_WORKORDER_MBE+"].from.attribute["+CHiPSDomainConstant.ATTRIBUTE_CHIPS_FORM_TYPE+"]");
			slObjectSelects.add("to["+CHiPSDomainConstant.RELATIONSHIP_WORKORDER_MBE+"].from.id");
			Map mMBEObjData = doMeasurementBookItem.getInfo(context, slObjectSelects);
			String sWorkOrderFormType = (String) mMBEObjData.get("to["+CHiPSDomainConstant.RELATIONSHIP_WORKORDER_MBE+"].from.attribute["+CHiPSDomainConstant.ATTRIBUTE_CHIPS_FORM_TYPE+"]");
			if ("FormF".equals(sWorkOrderFormType)) {
				${CLASS:WMSMeasurementBookEntry} jpo = new ${CLASS:WMSMeasurementBookEntry}(context, null);
				StringList slActivitiesToAdd = jpo.getRelatedActivities(context, args);
				for (int i=0,j=slActivitiesToAdd.size(); i<j; i++) {
					String sItemOid = (String)slActivitiesToAdd.get(i);
					DomainObject doItem = DomainObject.newInstance(context, sItemOid);
					//TODO call the DB in getRelatedActivities
					String sItemType = doItem.getAttributeValue(context, CHiPSDomainConstant_ATTRIBUTE_CHIPS_MBE_ITEM_TYPE);
					if("Payment".equals(sItemType) || "Rebate".equals(sItemType)){
						showItemTypeColumn = true;
						break;
					}
				}
			}
			}
            return showItemTypeColumn;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }  */
        return true;
    }
  
    /**
     * Check whether column is having Edit Access or not depending on type of object, Checking For type CHiPSMeasurements
     * @param context The Matrix Context object
     * @param  
     * @param args holds the following input arguments:
     *     1) ObjectList : List of objects in table
     *     2) ProgramMap : Contains all info about table columns        
     * @throws Exception if the operation fails
     */    
    public StringList checkForMeasurementType(Context context,String args[]) throws Exception
    {
        StringList strListAccess = new StringList();;
        try
        {
            Map programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Iterator<Map<String,String>> objectListIterator = objectList.iterator();

            while (objectListIterator.hasNext()) {

                Map<String,String> mapData = objectListIterator.next();
                String strType = mapData.get(DomainConstants.SELECT_TYPE);
                if(TYPE_WMS_MEASUREMENTS.equals(strType))
                    strListAccess.add(String.valueOf(true));
                else
                    strListAccess.add(String.valueOf(false));
            }

        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        return strListAccess;

    }
    //Code added for B3 - start
    /**
     * Returns Revised BOQ object for Work Order.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds ParamMap
     * @return        MapList
     * @throws        Exception if the operation fails
     **/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRevisedBOQs(Context context, String [] args) throws Exception
    {
    	MapList mlRevisedInfo = new MapList();
    	try 
    	{
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    		String strWorkOrderId = (String)programMap.get("objectId");
    		if(UIUtil.isNotNullAndNotEmpty(strWorkOrderId))
    		{
    			StringList objSelects = new StringList(1);
    			objSelects.addElement(DomainConstants.SELECT_ID);
    			objSelects.addElement(DomainConstants.SELECT_REVISION);
    			DomainObject doWorkOrder = DomainObject.newInstance(context,strWorkOrderId);				
    			mlRevisedInfo = doWorkOrder.getRelatedObjects(context, 
    														  RELATIONSHIP_WMS_REVISED_BOQ, 
    														  TYPE_WMS_REVISED_BOQ, 
    														  objSelects,
    														  null,
    														  false,
    														  true,
    														  (short)1,
    														  "",
    														  "");	
    			mlRevisedInfo.sortStructure(DomainConstants.SELECT_REVISION, "ascending", "Integer");
    		}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	return mlRevisedInfo;
    }
	//Code added for B3 - end
}
