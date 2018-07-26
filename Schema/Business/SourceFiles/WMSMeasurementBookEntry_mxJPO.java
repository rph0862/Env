/** Name of the JPO    : WMSMeasurementBookEntry
 ** Developed by    : Matrixone 
 ** Client            : WMS
 ** Description        : The purpose of this JPO is to create a Measurement Book Entry
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.common.util.ImageManagerImageInfo;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

import matrix.db.Context;
import matrix.db.File;
import matrix.db.FileItr;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
/**
 * The purpose of this JPO is to create a Measurement Book Entry.
 * @author CHiPS
 * @version R418 - Copyright (c) 1993-2016 Dassault Systems.
 */
public class WMSMeasurementBookEntry_mxJPO extends WMSConstants_mxJPO
{
	 
    /**
     * Create a new ${CLASS:WMSMeasurementBookEntry} object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since R418
     */

    public WMSMeasurementBookEntry_mxJPO (Context context, String[] args)
        throws Exception
    {
       super(context,args);
    }

    /**
     * Method to get the connected Objects on expansion
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps from the command or form or table
     * @return mapListConnectedObject MapList containing the connected objects
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getConnectedObjects(Context context, String[] args) throws Exception{
        MapList mapListObjects = new MapList();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strObjectOID = (String)programMap.get("objectId");
            if(UIUtil.isNotNullAndNotEmpty(strObjectOID))
            {
                DomainObject domObj = DomainObject.newInstance(context, strObjectOID);
                //mapListObjects = getConnectedObjects(context, domObj,strObjectOID);
                mapListObjects = getConnectedObjects(context, domObj,strObjectOID);
                // @Added to filter data by ATTRIBUTE_CHIPS_MBE_ITEM_TYPE Values Normal, Payment and Rebate - End
            }
            mapListObjects.sort("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"].value", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        return mapListObjects;
    }

    /**
     * Method to get the connected Objects on expansion
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domObj context domain Object
     * @param strObjectOID String value containing the context object ID
     * @return mapListConnectedObject MapList containing the connected objects
     * @throws FrameworkException if the operation fails
     * @author CHiPS
     * @since 418
     */    
    private MapList getConnectedObjects(Context context, DomainObject domObj,String strObjectOID)
            throws Exception {
        try
        {
            MapList mapListObjects = new MapList();
            //String strExpandType = getExpandType(context, domObj);
            String strType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
            StringList strListBusSelects     = new StringList(3);
            strListBusSelects.add(DomainConstants.SELECT_ID);
            strListBusSelects .add(DomainConstants.SELECT_NAME);
            strListBusSelects .add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
            strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"].value");
            strListBusSelects.add("attribute["+ATTRIBUTE_WMS_ITEM_RATE_ESCALATION+"].value");
            strListBusSelects.add("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
            StringList strListRelSelects     = new StringList(1);
            strListRelSelects.add(DomainRelationship.SELECT_ID);
            Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
            patternType.addPattern(TYPE_WMS_SEGMENT);
            Pattern patternRel = new Pattern(RELATIONSHIP_BILL_OF_QUANTITY);        

            if(TYPE_WMS_WORK_ORDER.equals(strType))
            {
                mapListObjects = getWorkOrderSegment(context, strObjectOID);
                MapList mapListItems = getWorkOrderItems(context, strObjectOID);
                mapListObjects.addAll(mapListItems);
                insertKeyValue(mapListObjects, DomainConstants.SELECT_LEVEL, "1");
            }
            else
            {
                mapListObjects = domObj.getRelatedObjects(context, // matrix context
                		                                    RELATIONSHIP_BILL_OF_QUANTITY,                                     // relationship pattern
															patternType.getPattern(),    
															strListBusSelects, // object selects
															strListRelSelects, // relationship selects
															false, // to direction
															true, // from direction
															(short) 1, // recursion level
															DomainConstants.EMPTY_STRING, // object where clause
															DomainConstants.EMPTY_STRING, // relationship where clause
															0);
            }
            return mapListObjects;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }
    /**
     * Function to get the connected Segments to the Work Order
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strWorkOrderOID String value containing the project object ID
     * @return mapListSegments MapList containing the Work Orders with name and ID
     * @throws FrameworkException if the operation fails
     * @author CHiPS
     * @since 418
     */
    private MapList getWorkOrderSegment(Context context, String strWorkOrderOID)
            throws FrameworkException {
        try
        {
            StringList objectSelects     = new StringList(3);
            objectSelects .add(DomainConstants.SELECT_ID);
            objectSelects .add(DomainConstants.SELECT_NAME);
            objectSelects .add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
            objectSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"].value");
            
            DomainObject domObjWorkOrder  = DomainObject.newInstance(context, strWorkOrderOID);

            Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_BOOK);
            patternType.addPattern(TYPE_WMS_SEGMENT);
            MapList mapListSegments = domObjWorkOrder.getRelatedObjects(context,
            	                                                   	   RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
																		patternType.getPattern(),                                    // object pattern
																		false,                                                        // to direction
																		true,                                                       // from direction
																		(short)2,                                                      // recursion level
																		objectSelects,                                                 // object selects
																		null,                                                         // relationship selects
																		DomainConstants.EMPTY_STRING,                                // object where clause
																		DomainConstants.EMPTY_STRING,                                // relationship where clause
																		(short)0,                                                      // No expand limit
																		DomainConstants.EMPTY_STRING,                                // postRelPattern
																		TYPE_WMS_SEGMENT,                                                // postTypePattern
																		null);                                                      // postPatterns

            return mapListSegments;
        }
        catch(FrameworkException frameworkException)
        {
            throw frameworkException;
        }
    }
    /**
     * Function to get the connected Items to the Work Order directly
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strWorkOrderOID String value containing the project object ID
     * @return mapListItems MapList containing the items with name and ID
     * @throws FrameworkException if the operation fails
     * @author CHiPS
     * @since 418
     */
    public MapList getWorkOrderItems(Context context, String strWorkOrderOID)
            throws FrameworkException {
        try
        {
            StringList objectSelects     = new StringList(3);
            objectSelects .add(DomainConstants.SELECT_ID);
            objectSelects .add(DomainConstants.SELECT_NAME);
            objectSelects .add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
            objectSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"].value");
            
            DomainObject domObjWorkOrder  = DomainObject.newInstance(context, strWorkOrderOID);

            Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_BOOK);
            patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
            MapList mapListItems = domObjWorkOrder.getRelatedObjects(context,
            		                                                    RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
																		patternType.getPattern(),                                    // object pattern
																		false,                                                        // to direction
																		true,                                                       // from direction
																		(short)2,                                                      // recursion level
																		objectSelects,                                                 // object selects
																		null,                                                         // relationship selects
																		DomainConstants.EMPTY_STRING,                                // object where clause
																		DomainConstants.EMPTY_STRING,                                // relationship where clause
																		(short)0,                                                      // No expand limit
																		DomainConstants.EMPTY_STRING,                                // postRelPattern
																		TYPE_WMS_MEASUREMENT_TASK,                                                // postTypePattern
																		null);                                                      // postPatterns

            return mapListItems;
        }
        catch(FrameworkException frameworkException)
        {
            throw frameworkException;
        }
    }
    /**
     * Method to insert value for the specific key in all map of the map list
     *
     * @param mapListConnectedMBEs MapList containing the connected objects
     * @param strKey String value containing the key
     * @param strValue String value containing the value
     * @author CHiPS
     * @since 418
     */    
    public static void insertKeyValue(MapList mapListConnectedMBEs, String strKey,
            String strValue) {
        Iterator<Map<String,String>> iterator = mapListConnectedMBEs.iterator();
        Map<String,String> mapData;
        while(iterator.hasNext())
        {
            mapData = iterator.next();
            mapData.put(strKey, strValue);
        }
    }
	
    // MBE - Start
    /** 
     * Method MBE Entry AutoName as Title
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps for the table
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public HashMap setTitleMBE(Context context, String[] args) throws Exception {
             HashMap returnMap              = new HashMap();    
        try {
            HashMap programMap              = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap               = (HashMap)programMap.get("paramMap");
            String strMBEOID             = (String) paramMap.get("objectId");
            
            if(UIUtil.isNotNullAndNotEmpty(strMBEOID))
            {
                StringList strListMBEInfo = new StringList(2);
                String strNameSelect = DomainConstants.SELECT_NAME;
                String strTitleNameSelect = "attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]";
                strListMBEInfo.add(strNameSelect);
                strListMBEInfo.add(strTitleNameSelect);
                DomainObject domObjMBE = DomainObject.newInstance(context,strMBEOID);
                Map<String, String> mapAMB = domObjMBE.getInfo(context, strListMBEInfo);
                String strName =  DomainConstants.EMPTY_STRING;   
                String strTitleName =  DomainConstants.EMPTY_STRING;
                strName = mapAMB.get(strNameSelect);
                strTitleName = mapAMB.get(strTitleNameSelect);
                
                if(UIUtil.isNullOrEmpty(strTitleName))
                {
                    domObjMBE.setAttributeValue(context,ATTRIBUTE_WMS_WORK_ORDER_TITLE,strName);
                }
            }
						
		/*	//Check if current date is in between 25th-current month to 5th-next month-start
			Locale strLocale = context.getLocale();
			String strMsg = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.MBE.CheckDate.Alert");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			int curMonth = cal.get(cal.MONTH)+1;
			int curYear = (cal.get(cal.YEAR));
			Date CurrentMonthFirstLimit = sdf.parse(curYear+"-"+curMonth+"-5");
			Date CurrentMonthSecondLimit = sdf.parse(curYear+"-"+curMonth+"-25");
			if(today.after(CurrentMonthSecondLimit) && today.before(CurrentMonthFirstLimit)) {
				returnMap.put("Action","Stop");
				returnMap.put("Message",strMsg);
				return returnMap;
			}*/
			//Check if current date is in between 25th-current month to 5th-next month-end
			
             returnMap.put("Message","Successfully created Measurement Entry ");
             returnMap.put("Action","success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }
/** 
     * Method AMBE AutoName as Title
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps for the table
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public HashMap setTitle(Context context, String[] args) throws Exception {
             HashMap returnMap              = new HashMap();    
        try {
            HashMap programMap              = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap               = (HashMap)programMap.get("paramMap");
            HashMap requestMap           = (HashMap)programMap.get("requestMap");
            //String strAbsMBEOID             = (String) paramMap.get("objectId");
            Locale strLocale = context.getLocale();
            StringList strListBusSelects     = new StringList(1);
            strListBusSelects.add(DomainConstants.SELECT_CURRENT);
            strListBusSelects.add(DomainConstants.SELECT_ID);
            String strWorkOrderOID        = (String) programMap.get("WorkOrder");
            String strDescription        = (String) programMap.get("Description");
            String strUser	= context.getUser();
            if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))//&&UIUtil.isNotNullAndNotEmpty(strAbsMBEOID))
            {
				//ContextUtil.pushContext(context);
				// DomainObject domObjAbsMBE  = DomainObject.newInstance(context, strAbsMBEOID);
				//Check if current date is in between 25th-current month to 5th-next month-start
				String strMsg = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.AMBE.CheckDate.Alert");
				
				String strMinDateNumber = EnoviaResourceBundle.getProperty(context,"WMS.AMBE.BlockCreate.MinDateNumber");
				String strMaxDateNumber = EnoviaResourceBundle.getProperty(context,"WMS.AMBE.BlockCreate.MaxDateNumber");
				if(strMinDateNumber==null || strMinDateNumber.isEmpty()) 
					strMinDateNumber="5";
				if(strMaxDateNumber==null || strMaxDateNumber.isEmpty()) 
					strMaxDateNumber="25";
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				LocalDate ldToday=new LocalDate();
				int iDay = ldToday.getDayOfMonth();
		 	 	if(iDay<=Integer.parseInt(strMinDateNumber) || iDay >=Integer.parseInt(strMaxDateNumber)) {
					returnMap.put("Action","Stop");
					returnMap.put("ErrorMessage",strMsg);
					return returnMap;
				}
            	WMSSupplierDevelopmentPlan_mxJPO wmsSDP=new WMSSupplierDevelopmentPlan_mxJPO(context, args);
            	Map mArgs=new HashMap();
            	mArgs.put("WORKORDEROiD", strWorkOrderOID);
            	strMsg = wmsSDP.triggerAreMandatoryDocumentsUploaded(context,JPO.packArgs(mArgs));
            	if(!strMsg.isEmpty()) {
            		returnMap.put("Action","Stop");
                    returnMap.put("ErrorMessage",strMsg);
                    return returnMap;
            	}
                DomainObject domObjWorkOrder = DomainObject.newInstance(context,strWorkOrderOID);
                String strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.AMBE.Statecheck.Alert");
                //Collect Only ABMEObjs which are in state Create,Submitted and Approved
                MapList mapListAMBEs = domObjWorkOrder.getRelatedObjects(context, // matrix context
                        WMSConstants_mxJPO.RELATIONSHIP_WORKORDER_ABSTRACT_MBE, // relationship pattern
                        WMSConstants_mxJPO.TYPE_ABSTRACT_MBE, // type pattern
                        strListBusSelects, // object selects
                        null, // relationship selects
                        false, // to direction
                        true, // from direction
                        (short) 1, // recursion level
                        DomainConstants.EMPTY_STRING, // object where clause
                        DomainConstants.EMPTY_STRING, // relationship where clause
                        0);
                Iterator<Map<String,String>> iterator = mapListAMBEs.iterator();
                Map<String,String> mapData;
                while(iterator.hasNext())
                {
                    mapData = iterator.next();
                    String strState  = mapData.get(DomainConstants.SELECT_CURRENT);
                    String objId  = mapData.get(DomainConstants.SELECT_ID);
                    if(!(STATE_PLAN.equals(strState)|| STATE_APPROVED.equals(strState)))
                     {
                         returnMap.put("Action","Stop");
                         returnMap.put("ErrorMessage",strMessage);
                         return returnMap;
                     }
					
                }
				//Check if current date is in between 25th-current month to 5th-next month-start
				/*String strMsg = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.AMBE.CheckDate.Alert");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date today = new Date();
				Calendar cal = Calendar.getInstance();
				int curMonth = cal.get(cal.MONTH)+1;
				int curYear = (cal.get(cal.YEAR));
				Date CurrentMonthFirstLimit = sdf.parse(curYear+"-"+curMonth+"-5");
				Date CurrentMonthSecondLimit = sdf.parse(curYear+"-"+curMonth+"-25");
				if(today.after(CurrentMonthSecondLimit) && today.before(CurrentMonthFirstLimit)) {
					returnMap.put("Action","Stop");
					returnMap.put("ErrorMessage",strMsg);
					return returnMap;
				}*/
				//Check if current date is in between 25th-current month to 5th-next month-end
				
				String strMMOid  = FrameworkUtil.autoName(context, "type_WMSAbstractMeasurementBookEntry", "policy_WMSAbstractMeasurementBookEntry");
				DomainObject domObjAbsMBE  = DomainObject.newInstance(context, strMMOid);
				domObjAbsMBE.setDescription(context,strDescription);
				domObjAbsMBE.setOwner(context, strUser);
				returnMap.put("id",strMMOid);
				DomainRelationship domRel = DomainRelationship.connect(context, domObjWorkOrder, WMSConstants_mxJPO.RELATIONSHIP_WORKORDER_ABSTRACT_MBE, domObjAbsMBE);
				int intSequence = mapListAMBEs.size()+1;
				NumberFormat numberFormat = new DecimalFormat("0000");
				String strSequence = numberFormat.format(intSequence);
				//TODO use constant entry
				String strAgreementNumber = domObjWorkOrder.getAttributeValue(context, "WMSPONumber");
				//ContextUtil.popContext(context);
				String strPrefix =  EnoviaResourceBundle.getProperty(context, "WMS.Prefix.AbstractMBE");
				String strName = strAgreementNumber.concat("-").concat(strPrefix).concat("-").concat(strSequence);
				domObjAbsMBE.setName(context,strName);

				//String strName = domObjAbsMBE.getInfo(context,DomainConstants.SELECT_NAME);
				Map<String,String> hashMapAttributes = new HashMap<String,String>(2);
				hashMapAttributes.put(DomainConstants.ATTRIBUTE_TITLE, strName);
				hashMapAttributes.put(ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER, String.valueOf(intSequence));
				domObjAbsMBE.setAttributeValues(context,hashMapAttributes);

			}
        } catch (Exception ex) {
            throw ex;
        }
        returnMap.put("Action","Success");
        
        return returnMap;
    }
    /**
     * Function to get the Work Order Field Map
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps from the Web Form
     * @return mapField Map<String,Object> containing the keys RangeValues and RangeDisplayValues
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    public HashMap<String,Object> getProjectWorkOrder(Context context, String[] args) throws Exception
    {
        try
        {
            Map<String,Object> mapProgram = (Map<String,Object>)JPO.unpackArgs(args);
            Map requestMap = (HashMap)mapProgram.get("requestMap");
            String strPersonId = PersonUtil.getPersonObjectID(context);
            HashMap<String,Object> mapField         = new HashMap<String,Object>(3);
            if(UIUtil.isNotNullAndNotEmpty(strPersonId))
            {
                StringList slCustomerActualValList = new StringList();
                StringList slCustomerDisplayValList = new StringList();
                String sType = (String) requestMap.get("TypeActual");
                String strWherCond = "("+DomainConstants.SELECT_CURRENT+"==Active || "+DomainConstants.SELECT_CURRENT+"==Complete)";
                StringList objectSelects     = new StringList(3);
                objectSelects .add(DomainConstants.SELECT_ID);
                objectSelects .add(DomainConstants.SELECT_NAME);
                objectSelects .add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"].value");                
                DomainObject personObject  = DomainObject.newInstance(context,strPersonId);
                ContextUtil.pushContext(context);
                MapList mapListWorkOrders = personObject.getRelatedObjects(context, // matrix context
																			RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE, // relationship pattern
																			TYPE_WMS_WORK_ORDER, // type pattern
																			objectSelects, // object selects
																			null, // relationship selects
																			false, // to direction
																			true, // from direction
																			(short) 1, // recursion level
																			strWherCond, // object where clause "current==Active"
																			DomainConstants.EMPTY_STRING, // relationship where clause
																			0);
                ContextUtil.popContext(context);
                Iterator<Map<String,String>> iterator = mapListWorkOrders.iterator();
                while(iterator.hasNext())
                {
                    Map<String,String> mapWorkOrder = iterator.next();
                    slCustomerActualValList.add(mapWorkOrder.get(DomainConstants.SELECT_ID));  
                    slCustomerDisplayValList.add(mapWorkOrder.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"].value"));
                }
                slCustomerActualValList.add(DomainConstants.EMPTY_STRING);
                slCustomerDisplayValList.add(DomainConstants.EMPTY_STRING);
                mapField.put("field_choices", slCustomerActualValList);
                mapField.put("field_display_choices", slCustomerDisplayValList);
            }
            return mapField;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw new Exception(exception);
        }

    }
    /**
     * Method to connect the Work Order and MBE
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps from the command
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public void connectWorkOrder(Context context,String[]args)throws Exception
    {
        try
        {
			Map programMap                 = JPO.unpackArgs(args);
			HashMap paramMap             = (HashMap) programMap.get("paramMap");
			String strMBEOID             = (String) paramMap.get("objectId");
			String strWorkOrderOID             = (String) paramMap.get("New OID");
			if(UIUtil.isNullOrEmpty(strWorkOrderOID))
			{
				strWorkOrderOID = (String) paramMap.get("New Value");
			}
			if(UIUtil.isNotNullAndNotEmpty(strMBEOID)&& UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
			{
				DomainObject domObjWO  = DomainObject.newInstance(context, strWorkOrderOID);
				DomainObject domObjMBE  = DomainObject.newInstance(context, strMBEOID);
				//DomainRelationship domRel = DomainRelationship.connect(context, domObjWO, WMSDomainConstant.RELATIONSHIP_WORKORDER_MBE, domObjMBE);
				DomainRelationship domRel = DomainRelationship.connect(context, domObjWO, RELATIONSHIP_WMS_WORK_ORDER_MBE, domObjMBE);
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
    }
	// MBE - End
    
    
    public Map reloadMBEType(Context context, String[] args) throws Exception
    {
        Map argMap = (Map)JPO.unpackArgs(args);
        Map fieldValues = (Map)argMap.get("fieldValues");
        String sWorkOrderOid = (String) fieldValues.get("WorkOrder");
        String sWOCurrentState = "";
        if(UIUtil.isNotNullAndNotEmpty(sWorkOrderOid))
        {
        	ContextUtil.pushContext(context);
            DomainObject doWorkOrder = DomainObject.newInstance(context, sWorkOrderOid);
            sWOCurrentState = doWorkOrder.getInfo(context, "current");
            ContextUtil.popContext(context);
        }
        String sMBEType = "Running";
        if ("Complete".equals(sWOCurrentState)) {
            sMBEType = "Final";
        }
        Map fieldMap = new HashMap();
        fieldMap.put("SelectedValues", sMBEType);
        fieldMap.put("SelectedDisplayValues", sMBEType);
        return fieldMap;
    }
    
    /**
	 * Method to get the connected Activities under the MBE
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMBEActivities (Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListConnectedTasks = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			String strMBCurrentState = DomainConstants.EMPTY_STRING;
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjMBE= DomainObject.newInstance(context, strObjectId);
				mapListConnectedTasks = getMBEActivities(context, domObjMBE);
				strMBCurrentState = domObjMBE.getInfo(context, DomainConstants.SELECT_CURRENT);
            }
			insertKeyValue(mapListConnectedTasks, "MBCurrent", strMBCurrentState);
				
            return mapListConnectedTasks;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }
	
	
	/**
     * Function to get the Tasks connected to the selected MBE
     * DON'T MODIFY
     * @param context the eMatrix <code>Context</code> object
     * @param domObjMBE DomainObject instance of selected Work Order 
     * @return mapListTasks MapList containing the MBEs connected to Work Order with ID
     * @throws FrameworkException if the operation fails
     * @author CHiPS
     * @since 418
     */
    private MapList getMBEActivities(Context context, DomainObject domObjMBE)
            throws FrameworkException {
        try
        {
            SelectList selListBusSelects     = new SelectList(12);
            selListBusSelects.add(DomainConstants.SELECT_ID);
            selListBusSelects.add(DomainConstants.SELECT_TYPE);
            selListBusSelects.add(DomainConstants.SELECT_DESCRIPTION);
            selListBusSelects.add("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
            selListBusSelects.add(DomainConstants.SELECT_REVISION);
            selListBusSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_BOQ_SERIAL_NUMBER+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_COST+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
            selListBusSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]"); 
            SelectList selListRelSelects     = new SelectList(2);
            selListRelSelects.add(DomainRelationship.SELECT_ID);
            selListRelSelects.add("attribute["+DomainConstants.ATTRIBUTE_QUANTITY+"]");
            selListRelSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]");

            MapList mapListTasks = domObjMBE.getRelatedObjects(context, // matrix context
            		RELATIONSHIP_WMS_MBE_ACTIVITIES, // relationship pattern
                    TYPE_WMS_MEASUREMENT_TASK, // type pattern
                    selListBusSelects, // object selects
                    selListRelSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    DomainConstants.EMPTY_STRING, // object where clause
                    DomainConstants.EMPTY_STRING, // relationship where clause
                    0);
            /*            Iterator<Map<String,String>> iterator = mapListTasks.iterator();
            while(iterator.hasNext())
            {
                Map<String,String> mapData = iterator.next();
                String strSORConnection = mapData.get("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
            }*/
            return mapListTasks;
        }
        catch(FrameworkException frameworkException)
        {
            throw frameworkException;
        }
    }

    /**
  	 * Method to get the connected Activities/Payment under the MBE
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command
       * @return mapListConnectedTasks MapList containing the Task IDs
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */
      @com.matrixone.apps.framework.ui.ProgramCallable
    	public MapList getMBEPreviewBillActivities (Context context, String[] args) throws Exception 
      {
          try
          {
              MapList mapListConnectedTasks = new MapList();
  			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
  			String strMBCurrentState = DomainConstants.EMPTY_STRING;
              if(UIUtil.isNotNullAndNotEmpty(strObjectId))
              {
                  DomainObject domObjMBE= DomainObject.newInstance(context, strObjectId);
                  mapListConnectedTasks = getMBEActivities(context, domObjMBE);
  				MapList mapListPaidPaymentScheduleItems = WMSPaymentSchedule_mxJPO.getPaidPaymentScheduleItems(context, domObjMBE);
                  mapListConnectedTasks.addAll(mapListPaidPaymentScheduleItems);
  				strMBCurrentState = domObjMBE.getInfo(context, DomainConstants.SELECT_CURRENT);
  			}
  			insertKeyValue(mapListConnectedTasks, "MBCurrent", strMBCurrentState);
  			return mapListConnectedTasks;
  		}
  		catch(Exception exception)
  		{
  			exception.printStackTrace();
  			throw exception;
  		}
  	}
	
      /**
       * Method to get the connected Items under the Work Order from MBE add existing 
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command
       * @return mapListConnectedTasks MapList containing the Task IDs
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */
      @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
      public StringList getRelatedActivitiesForAbstractMBE (Context context, String[] args) throws Exception 
      {
          try
          {
              StringList strListActivitiesOIDs = new StringList();
  			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
              String strRel = DomainConstants.EMPTY_STRING;
              if(UIUtil.isNotNullAndNotEmpty(strObjectId))
              {
                  StringList strListBusSelects     = new StringList(1);
                  strListBusSelects.add(DomainConstants.SELECT_ID);
                  strListBusSelects.add("to[CHiPSAbstractMBEActivities]");

                  DomainObject domObjMBE= DomainObject.newInstance(context, strObjectId);
                  if(domObjMBE.isKindOf(context, TYPE_WMS_MEASUREMENT_BOOK_ENTRY)){
                      strRel= RELATIONSHIP_WMS_WORK_ORDER_MBE;
                  }
                  else if(domObjMBE.isKindOf(context, TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY)){
                      strRel=RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE;
                  }
                  MapList mapListConnectedActivities = getMBEActivities(context, domObjMBE);
  				StringList strListConnectdActivities = WMSUtil_mxJPO.convertToStringList(mapListConnectedActivities, DomainConstants.SELECT_ID);
                  String strWorkOrderOID = domObjMBE.getInfo(context, "to["+strRel+"].from.id");
                  if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
                  {
                      String strMBOID = getMeasurementBookOID(context,strListBusSelects, strWorkOrderOID);
                      if(UIUtil.isNotNullAndNotEmpty(strMBOID))
                      {
                          StringList strListOBJ = getRelatedActivitiesForAbstractMBE(context,
                                  strListBusSelects, strMBOID);
                          strListActivitiesOIDs.addAll(strListOBJ);
                          strListActivitiesOIDs.removeAll(strListConnectdActivities);
                      }
                  }
              }
              return strListActivitiesOIDs;
          }
          catch(Exception exception)
          {
              exception.printStackTrace();
              throw exception;
          }
      }
      
      
      /**
       * Function to get the Tasks connected to the selected Measurement Book
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strMBOID String value containing the Measurement Book OID 
       * @param strListBusSelects StringList containing the bus selects
       * @return strListOBJ StringList  containing the Measurement Activities OIDs
       * @throws FrameworkException if the operation fails
       * @author CHiPS
       * @since 418
       */
      private StringList getRelatedActivitiesForAbstractMBE(Context context,
              StringList strListBusSelects, String strMBOID)
                      throws FrameworkException {
          try
          {
              DomainObject domObjMB= DomainObject.newInstance(context, strMBOID);

              Pattern patternType = new Pattern(TYPE_WMS_SEGMENT);
              patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
              MapList mapListActivities = domObjMB.getRelatedObjects(context,
                      RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
                      patternType.getPattern(),                                    // object pattern
                      false,                                                        // to direction
                      true,                                                       // from direction
                      (short)0,                                                      // recursion level
                      strListBusSelects,                                                 // object selects
                      null,                                                         // relationship selects
                      null,                                // object where clause
                      DomainConstants.EMPTY_STRING,                                // relationship where clause
                      (short)0,                                                      // No expand limit
                      DomainConstants.EMPTY_STRING,                                // postRelPattern
                      TYPE_WMS_MEASUREMENT_TASK,                                                // postTypePattern
                      null);                                                      // postPatterns

              Iterator<Hashtable<String, String>> Itr = mapListActivities.iterator();
              while (Itr.hasNext()) {
                  Hashtable<java.lang.String, java.lang.String> hashMap = (Hashtable<java.lang.String, java.lang.String>) Itr.next();
                  String strIsConnected = hashMap.get("to[CHiPSAbstractMBEActivities]");
                  if(strIsConnected.equalsIgnoreCase("true"))
                      Itr.remove();
              }

  			StringList strListOBJ = WMSUtil_mxJPO.convertToStringList(mapListActivities, DomainConstants.SELECT_ID);
              return strListOBJ;
          }
          catch(FrameworkException frameworkException)
          {
              throw frameworkException;
          }
      }

      
      
      /**
       * Function to get the Tasks connected to the selected Work Order
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strWorkOrderOID String value containing the Work Order OID 
       * @param strListBusSelects StringList containing the bus selects
       * @return strMBOID String value containing the Measurement Book OID
       * @throws FrameworkException if the operation fails
       * @author CHiPS
       * @since 418
       */
      private String getMeasurementBookOID(Context context,
              StringList strListBusSelects, String strWorkOrderOID)
                      throws FrameworkException 
      {
          try
          {
              
              DomainObject domObjWO= DomainObject.newInstance(context, strWorkOrderOID);
              String strMBOID = domObjWO.getInfo(context, "from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.id");

              return strMBOID;
          }
          catch(FrameworkException frameworkException)
          {
              throw frameworkException;
          }
        }

      /**
       * Method to get the connected Objects on expansion of Items for Copy Measurements
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command or form or table
       * @return mapListConnectedObject MapList containing the connected objects
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */    
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getConnectedMeasurementObjects(Context context, String[] args) throws Exception
      {
          MapList mapListObjects = new MapList();
          try 
          {
              
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              String strObjectOID = (String)programMap.get("objectId");
              String strParentObjectOId = (String) programMap.get("parentOID");
              if(UIUtil.isNotNullAndNotEmpty(strObjectOID))
              {
                  StringList strListBusSelects     = new StringList(2);
                  strListBusSelects.add(DomainConstants.SELECT_ID);
                  strListBusSelects .add(DomainConstants.SELECT_LEVEL);
                  
                  SelectList selListRelSelects = new SelectList(1);
                  selListRelSelects.add(DomainRelationship.SELECT_ID);
                  String strWhere = DomainConstants.EMPTY_STRING;
                  if(strParentObjectOId.equals(strObjectOID))
                  {
                      strWhere ="("+ DomainConstants.SELECT_ID+"!="+strParentObjectOId +")&&(id==last.id)";
                  }
                  else
                  {
                  strWhere  = "(id==last.id)";
                  }
                  
                  DomainObject domObjTask= DomainObject.newInstance(context, strObjectOID);
                  mapListObjects = domObjTask.getRelatedObjects(context, // matrix context
                		  RELATIONSHIP_WMS_MBE_ACTIVITIES, // relationship pattern
                          TYPE_WMS_MEASUREMENT_BOOK_ENTRY, // type pattern
                          strListBusSelects, // object selects
                          selListRelSelects, // relationship selects
                          true, // to direction
                          false, // from direction
                          (short) 1, // recursion level
                          strWhere, // object where clause
                          DomainConstants.EMPTY_STRING, // relationship where clause
                          0);
                  //mapListObjects.add(0, mapListObjects.size());
              }
          }
          
          catch(Exception exception)
          {
              exception.printStackTrace();
              throw exception;
          }
          return mapListObjects;
  }
      /**
	 * Function to get the Abstarct MBEs connected to the selected Work Order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjWO DomainObject instance of selected Work Order 
	 * @return mapListMBEs MapList containing the Abstract MBEs connected to Work Order with ID
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public MapList getWorkOrderAbstractMBEs(Context context, DomainObject domObjWO)
			throws FrameworkException {
		boolean isContextPushed = false;
		try
		{
			StringList strListBusSelects     = new StringList(1);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			StringList strListRelSelects     = new StringList(2);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			strListBusSelects.add("attribute[atttibute_SequenceOrder].value");
            String strUser = context.getUser();
            //ContextUtil.pushContext(context);
			MapList membersList = WMSMeasurementBookItem_mxJPO.getWorkOrderAssignees(context,domObjWO);
		    StringList strListMembers     = new StringList();
			if(membersList.size()>0)
			{
			    Iterator<Map<String,String>> iterator  = membersList.iterator();
                Map<String,String> mapData;
				   
					while(iterator.hasNext())
					{
						mapData =  iterator.next();
						String strWOMemberName = mapData.get(DomainConstants.SELECT_NAME);
						strListMembers.add(strWOMemberName);
					}
			}		
            if(strListMembers.contains(strUser))
			{	
					isContextPushed = true;
					ContextUtil.pushContext(context);
			}		

			MapList mapListMBEs = domObjWO.getRelatedObjects(context, // matrix context
					WMSConstants_mxJPO.RELATIONSHIP_WORKORDER_ABSTRACT_MBE, // relationship pattern
					WMSConstants_mxJPO.TYPE_ABSTRACT_MBE, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			//ContextUtil.popContext(context);
			return mapListMBEs;
		}
		catch(FrameworkException frameworkException)
		{
			throw frameworkException;
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
       * Method to get the connected Active Work Orders from the context user
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command or form or table
       * @return mapListWorkOrders MapList containing the Work Order data
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getActiveWorkOrders (Context context, String[] args) throws Exception 
      {
      	try
      	{

      		String strPersonId = PersonUtil.getPersonObjectID(context,context.getUser());
      		String strWherCond = DomainConstants.SELECT_CURRENT+"==Active";
      		MapList mapListWorkOrders = getContextUserWorkOrders(context, strPersonId, strWherCond);
      		return mapListWorkOrders;
      	}
      	catch(Exception exception)
      	{
      		exception.printStackTrace();
      		throw exception;
      	}
      }
      
      
      /**
       * Method to get the connected Work Orders from the context user
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strPersonId context user BusOID
       * @param strWherCond String value containing the where cause
       * @return mapListWorkOrders MapList containing the Work Order data
       * @throws FrameworkException if the operation fails
       * @author CHiPS
       * @since 418
       */
      private MapList getContextUserWorkOrders(Context context, String strPersonId, String strWherCond)
      		throws FrameworkException {
      	try
      	{
      		MapList mapListWorkOrders = new MapList();
      		if(UIUtil.isNotNullAndNotEmpty(strPersonId))
      		{
      			DomainObject domObjPerson = DomainObject.newInstance(context, strPersonId);
      			StringList strListBusSelects     = new StringList(1);
      			strListBusSelects.add(DomainConstants.SELECT_ID);
      			StringList strListRelSelects     = new StringList(1);
      			strListRelSelects.add(DomainRelationship.SELECT_ID);
      			mapListWorkOrders = domObjPerson.getRelatedObjects(context, // matrix context
      					RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE, // relationship pattern
      					TYPE_WMS_WORK_ORDER, // type pattern
      					strListBusSelects, // object selects
      					strListRelSelects, // relationship selects
      					false, // to direction
      					true, // from direction
      					(short) 1, // recursion level
      					strWherCond, // object where clause "current==Active"
      					DomainConstants.EMPTY_STRING, // relationship where clause
      					0);   
      		}
      		return mapListWorkOrders;
      	}
      	catch(FrameworkException frameworkException)
      	{
      		frameworkException.printStackTrace();
      		throw frameworkException;
      	}
      }
      
      /**
       * Method to get the connected completed Work Orders from the context user
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command or form or table
       * @return mapListWorkOrders MapList containing the Work Order data
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getCompleteWorkOrders (Context context, String[] args) throws Exception 
      {
      	try
      	{

      		String strPersonId = PersonUtil.getPersonObjectID(context,context.getUser());
      		String strWherCond = DomainConstants.SELECT_CURRENT+"==Complete";
      		MapList mapListWorkOrders = getContextUserWorkOrders(context, strPersonId, strWherCond);
      		return mapListWorkOrders;
      	}
      	catch(Exception exception)
      	{
      		exception.printStackTrace();
      		throw exception;
      	}
      }
      /**
	 * Method to get the connected Abstract MBEs under the Work Order
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mapListConnectedMBEs MapList containing the Abstract MBEs IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWorkOrderAbstractMBEs (Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListConnectedMBEs = new MapList();
			ContextUtil.pushContext(context);
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjWO = DomainObject.newInstance(context, strObjectId);
				mapListConnectedMBEs = getWorkOrderAbstractMBEs(context, domObjWO);
			}
			ContextUtil.popContext(context);
			return mapListConnectedMBEs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
    /**
      * Method to get the connected all Work Orders from the context user
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command or form or table
      * @return mapListWorkOrders MapList containing the Work Order data
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getAllWorkOrders (Context context, String[] args) throws Exception 
     {
     	try
     	{

     		String strPersonId = PersonUtil.getPersonObjectID(context,context.getUser());
     		String strWherCond = DomainConstants.EMPTY_STRING;
     		MapList mapListWorkOrders = getContextUserWorkOrders(context, strPersonId, strWherCond);
     		return mapListWorkOrders;
     	}
     	catch(Exception exception)
     	{
     		exception.printStackTrace();
     		throw exception;
     	}
     }
     
     
     @com.matrixone.apps.framework.ui.ProgramCallable
     public  MapList getAllMyWIPMBE(Context context, String []  args)    throws Exception {
         MapList mlReturnList     =     new MapList();
         try
         {
             String strVault = context.getVault().getName();
             StringList strListBusInfo = new StringList(2);
             strListBusInfo.add(DomainConstants.SELECT_ID);
             strListBusInfo.add(DomainConstants.SELECT_CURRENT);
             String strWhere =    DomainConstants.SELECT_OWNER+"=='"+context.getUser()+"' && current==Create";
             mlReturnList =  DomainObject.findObjects(
                                             context,
                                             TYPE_WMS_MEASUREMENT_BOOK_ENTRY,
                                             DomainConstants.QUERY_WILDCARD,
                                             DomainConstants.QUERY_WILDCARD,
                                             DomainConstants.QUERY_WILDCARD,
                                             strVault,
                                             strWhere,               // where expression
                                             DomainConstants.EMPTY_STRING,
                                             false,
                                             strListBusInfo, // object selects
                                             (short) 0);       // limit
                 //enableSelection(mapListContractorAbsMBEs);
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
         return mlReturnList;
     }
	 
     @com.matrixone.apps.framework.ui.ProgramCallable
     public  MapList getAllMyMBEForApproval(Context context, String [] args)    throws Exception {
         MapList mlReturnList     =     new MapList();
         try
         {
             String strVault = context.getVault().getName();
             
             
             StringList strListBusInfo = new StringList(2);
             strListBusInfo.add(DomainConstants.SELECT_ID);
             strListBusInfo.add(DomainConstants.SELECT_CURRENT);
            // strListBusInfo.add("attribute["+ATTRIBUTE_WMS_CONTRACTOR_REVIEW+"]");
             
             StringList slRelSelect     =    new StringList();
             slRelSelect.add(DomainRelationship.SELECT_ID);
             
             String strWhere =    "current==Review";
             String strRelWhere     =     DomainConstants.EMPTY_STRING;
             com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
             
             
             Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

             Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
             patternType.addPattern(DomainConstants.TYPE_ROUTE);    
             patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);  
             
             strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MEASUREMENT_BOOK_ENTRY+" && current==Review)";


             mlReturnList         = contextPerson.getRelatedObjects(context,
                                                             //patternRel.getPattern(),  // relationship pattern
                                                             patternRel.getPattern(),
                                                             patternType.getPattern(),  // object pattern
                                                             true,                                                        // to direction
                                                             true,                                                       // from direction
                                                             (short)0,                                                      // recursion level
                                                             strListBusInfo,                                                 // object selects
                                                             slRelSelect,                                                         // relationship selects
                                                             strWhere,                                // object where clause
                                                             strRelWhere,                                // relationship where clause
                                                             (short)0,                                                      // No expand limit
                                                             DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                             TYPE_WMS_MEASUREMENT_BOOK_ENTRY,                                                // postTypePattern
                                                             null);
             
             
             String strOwnWhere =    DomainConstants.SELECT_OWNER+"=='"+context.getUser()+"' && current==Review";
             MapList mlOwnMBList =  DomainObject.findObjects(
                                             context,
                                             TYPE_WMS_MEASUREMENT_BOOK_ENTRY,
                                             DomainConstants.QUERY_WILDCARD,
                                             DomainConstants.QUERY_WILDCARD,
                                             DomainConstants.QUERY_WILDCARD,
                                             strVault,
                                             strOwnWhere,               // where expression
                                             DomainConstants.EMPTY_STRING,
                                             false,
                                             strListBusInfo, // object selects
                                             (short) 0);
             
             
             mlReturnList.addAll(mlOwnMBList);
           insertKeyValue(mlReturnList, DomainConstants.SELECT_LEVEL, "1");  
     
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
         return mlReturnList;
     }
     /**
	 * Method to get the connected Abstract MBEs under the PMCMyDesk
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mapListConnectedMBEs MapList containing the Abstract MBEs IDs
	 * @throws Exception if the operation fails
	 * @author WMS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public  MapList getAllMyWIPAbstractMBE(Context context, String []  args)    throws Exception {
        MapList mlReturnList     =     new MapList();
        try
        {
            String strVault = context.getVault().getName();
            StringList strListBusInfo = new StringList(2);
            strListBusInfo.add(DomainConstants.SELECT_ID);
            strListBusInfo.add(DomainConstants.SELECT_CURRENT);
            String strWhere =    DomainConstants.SELECT_OWNER+"=='"+context.getUser()+"' && current==Create";
            //String strWhere =   "current==Create";
            ContextUtil.pushContext(context);
            mlReturnList =  DomainObject.findObjects(
                                            context,
                                            WMSConstants_mxJPO.TYPE_ABSTRACT_MBE,
                                            DomainConstants.QUERY_WILDCARD,
                                            DomainConstants.QUERY_WILDCARD,
                                            DomainConstants.QUERY_WILDCARD,
                                            strVault,
                                            strWhere,               // where expression
                                            DomainConstants.EMPTY_STRING,
                                            false,
                                            strListBusInfo, // object selects
                                            (short) 0);       // limit
            ContextUtil.popContext(context);
                //enableSelection(mapListContractorAbsMBEs);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        return mlReturnList;
    }
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public  MapList getAllMyAbstractMBEForApproval(Context context, String [] args)    throws Exception {
        MapList mlReturnList     =     new MapList();
        try
        {
            String strVault = context.getVault().getName();
            
            
            StringList strListBusInfo = new StringList(2);
            strListBusInfo.add(DomainConstants.SELECT_ID);
            strListBusInfo.add(DomainConstants.SELECT_CURRENT);
            //strListBusInfo.add("attribute["+ATTRIBUTE_CONTRECTOR_REVIEW+"]");
            
            StringList slRelSelect     =    new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            
            String strWhere =    "current==Review";
            String strRelWhere     =     DomainConstants.EMPTY_STRING;
            com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
            
            
            Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
            patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

            Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
            patternType.addPattern(DomainConstants.TYPE_ROUTE);    
            patternType.addPattern(WMSConstants_mxJPO.TYPE_ABSTRACT_MBE);  
            
            strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+WMSConstants_mxJPO.TYPE_ABSTRACT_MBE+"&& current==Review)";


            mlReturnList         = contextPerson.getRelatedObjects(context,
                                                            //patternRel.getPattern(),  // relationship pattern
                                                            patternRel.getPattern(),
                                                            patternType.getPattern(),  // object pattern
                                                            true,                                                        // to direction
                                                            true,                                                       // from direction
                                                            (short)0,                                                      // recursion level
                                                            strListBusInfo,                                                 // object selects
                                                            slRelSelect,                                                         // relationship selects
                                                            strWhere,                                // object where clause
                                                            strRelWhere,                                // relationship where clause
                                                            (short)0,                                                      // No expand limit
                                                            DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                            WMSConstants_mxJPO.TYPE_ABSTRACT_MBE,                                                // postTypePattern
                                                            null);
            
            
            String strOwnWhere         =    DomainConstants.SELECT_OWNER+"=='"+context.getUser()+"' && current==Review";
            MapList mlOwnAbsMBList  =  DomainObject.findObjects(
                                            context,
                                            WMSConstants_mxJPO.TYPE_ABSTRACT_MBE,
                                            DomainConstants.QUERY_WILDCARD,
                                            DomainConstants.QUERY_WILDCARD,
                                            DomainConstants.QUERY_WILDCARD,
                                            strVault,
                                            strOwnWhere,               // where expression
                                            DomainConstants.EMPTY_STRING,
                                            false,
                                            strListBusInfo, // object selects
                                            (short) 0);
            
            
            mlReturnList.addAll(mlOwnAbsMBList);
            insertKeyValue(mlReturnList, DomainConstants.SELECT_LEVEL, "1");            

        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        return mlReturnList;
    }
	 @com.matrixone.apps.framework.ui.ProgramCallable
     public  MapList getAllMyMBE(Context context, String [] args)    throws Exception {
         MapList mlReturnList     =     new MapList();
         try
         {
             String strVault = context.getVault().getName();
             
             
             StringList strListBusInfo = new StringList(2);
             strListBusInfo.add(DomainConstants.SELECT_ID);
             strListBusInfo.add(DomainConstants.SELECT_CURRENT);
             
             StringList slRelSelect     =    new StringList();
             slRelSelect.add(DomainRelationship.SELECT_ID);
             
             String strWhere     = DomainConstants.EMPTY_STRING;
             String strRelWhere     = DomainConstants.EMPTY_STRING;
             com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
             
             
             Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

             Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
             patternType.addPattern(DomainConstants.TYPE_ROUTE);    
             patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);  
             
             strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Complete )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MEASUREMENT_BOOK_ENTRY+" && current==Submitted)";
             String strWhereOnwer =    DomainConstants.SELECT_OWNER+"=='"+context.getUser()+"' && current==Submitted";

             mlReturnList                     = contextPerson.getRelatedObjects(context,
                                                             //patternRel.getPattern(),  // relationship pattern
                                                             patternRel.getPattern(),
                                                             patternType.getPattern(),  // object pattern
                                                             true,                                                        // to direction
                                                             true,                                                       // from direction
                                                             (short)0,                                                      // recursion level
                                                             strListBusInfo,                                                 // object selects
                                                             slRelSelect,                                                         // relationship selects
                                                             strWhere,                                // object where clause
                                                             strRelWhere,                                // relationship where clause
                                                             (short)0,                                                      // No expand limit
                                                             DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                             TYPE_WMS_MEASUREMENT_BOOK_ENTRY,                                                // postTypePattern
                                                             null);
             
             
             MapList mlOwnerMBE                =  DomainObject.findObjects(context,
                                                                         TYPE_WMS_MEASUREMENT_BOOK_ENTRY,
                                                                         DomainConstants.QUERY_WILDCARD,
                                                                         DomainConstants.QUERY_WILDCARD,
                                                                         DomainConstants.QUERY_WILDCARD,
                                                                         strVault,
                                                                         strWhereOnwer,               // where expression
                                                                         DomainConstants.EMPTY_STRING,
                                                                         false,
                                                                         strListBusInfo, // object selects
                                                                         (short) 0); 
             
             mlReturnList.addAll(mlOwnerMBE);
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
         insertKeyValue(mlReturnList, DomainConstants.SELECT_LEVEL, "1");
         return mlReturnList;
     }
     
     /**
      * Method to get the disconnected Tasks under the MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return String containing the message
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public String removeSelectedActivities (Context context, String[] args) throws Exception 
     {
         try
         {
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");

             String strMBEID = (String) programMap.get("objectId");
             String strItemOID = emxTableRowId[0].split("[|]")[1];
             StringList strListInfo =new StringList();
             strListInfo.add(DomainConstants.SELECT_TYPE);
             DomainObject domItemId= DomainObject.newInstance(context,strItemOID);
             Map<String,String> mapInfo = domItemId.getInfo(context,strListInfo);
             String strType =  mapInfo.get(DomainConstants.SELECT_TYPE);

             if(emxTableRowId !=null && emxTableRowId.length>0)
             {
                 if(strType.equals(TYPE_WMS_MEASUREMENTS)){
                      ArrayList<String> arrayListRelOIDs = getSelectedActivities(context,emxTableRowId,strMBEID);
 					WMSUtil_mxJPO.disconnect(context, arrayListRelOIDs);
                     return "Selected objects sucessfully removed";

                 }
                 //else if(strType.equalsIgnoreCase(CHiPSDomainConstant.TYPE_ITEM)){

                 ArrayList<String> arrayListRelOIDs = getSelectedActivities(context,
                         emxTableRowId);
                 WMSUtil_mxJPO.disconnect(context, arrayListRelOIDs);
                 return "Selected objects sucessfully removed";

                 //}
             }                    
             return "Selected objects sucessfully removed";
         }catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     /**
      * Method to set Quantity in Current MBE And to select Measurement for Remove operation
      * Also Check Rollup of Quantity in Current MBE after removing the Measurement
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return mapListConnectedTasks MapList containing the Task IDs
      * @return strMBEID Object id OF MBE
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */

     private ArrayList<String> getSelectedActivities(Context context,
             String[] emxTableRowId,String strMBEID) throws  FrameworkException ,MatrixException {
         try
         {
        	
             ArrayList<String> arrrayListSelectedRelOIDS = new ArrayList<String>();
             ArrayList<String> arrayListRelOIDs = getRelationshipOIDs(emxTableRowId);

             DomainObject domMBEID = DomainObject.newInstance(context,strMBEID);
             for(int y=0;y<emxTableRowId.length;y++){
                 double doubleFinalQuantity =0.0f;
                 String strItemOid = emxTableRowId[y].split("[|]")[2];
                 String mqlQuery = "Print bus "+domMBEID.getId(context)+" select from["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"|to.id=="+strItemOid+"].id dump";
                 String strMBEItemRelId =MqlUtil.mqlCommand(context, mqlQuery);
                 DomainRelationship domrelID= DomainRelationship.newInstance(context,strMBEItemRelId);
                 String strFinalQuantity = domrelID.getAttributeValue(context,ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY);
                 double doubleItemFinalQuantity = Double.valueOf(strFinalQuantity);
                 DomainRelationship domrelAM=null;
                 String strMesurementQuantity =null;
                 domrelAM= DomainRelationship.newInstance(context,arrayListRelOIDs.get(y));
                 strMesurementQuantity = domrelAM.getAttributeValue(context,ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY);
                 doubleFinalQuantity = doubleFinalQuantity+(Double.valueOf(strMesurementQuantity));
                 doubleItemFinalQuantity = doubleItemFinalQuantity-doubleFinalQuantity;
                 domrelID.setAttributeValue(context, ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY, String.valueOf(doubleItemFinalQuantity));
             }
             emxTableRowId = ProgramCentralUtil.parseTableRowId(context, emxTableRowId);
             StringList strListInfo = getSelectedActivitiesInfo();
             MapList mapListInfo = DomainObject.getInfo(context, emxTableRowId, strListInfo);
             Iterator iterator = mapListInfo.iterator();
             int intCounter = 0;
             while(iterator.hasNext())
             {
                 Map<String,String> mapInfo = (Map<String,String>)iterator.next();
                 String strType = mapInfo.get(DomainConstants.SELECT_TYPE);
                 String strRelOID = mapInfo.get("relationship["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"].id");

                 if(TYPE_WMS_MEASUREMENTS.equals(strType))
                 {
                     arrrayListSelectedRelOIDS.add(arrayListRelOIDs.get(intCounter));
                 }

                 if(TYPE_WMS_MEASUREMENT_TASK.equals(strType))
                 {
                     arrrayListSelectedRelOIDS.add(arrayListRelOIDs.get(intCounter));
                 }

                 intCounter++;
             }
             return arrrayListSelectedRelOIDS;
         }
         catch(FrameworkException frameworkException)
         {
             frameworkException.printStackTrace();
             throw frameworkException;
         }


     }
     
     /**
      * Method to get the disconnected Tasks under the MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return mapListConnectedTasks MapList containing the Task IDs
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */

     private ArrayList<String> getSelectedActivities(Context context,
             String[] emxTableRowId) throws  FrameworkException ,MatrixException {
         try
         {
             ArrayList<String> arrrayListSelectedRelOIDS = new ArrayList<String>();
             ArrayList<String> arrayListRelOIDs = getRelationshipOIDs(emxTableRowId);
             emxTableRowId = ProgramCentralUtil.parseTableRowId(context, emxTableRowId);
             StringList strListInfo = getSelectedActivitiesInfo();
             MapList mapListInfo = DomainObject.getInfo(context, emxTableRowId, strListInfo);
             Iterator iterator = mapListInfo.iterator();
             int intCounter = 0;
             //while(iterator.hasNext())
             int intSize = mapListInfo.size();
             for(int i=0 ; i<intSize;i++ )
             {
                 Map<String,String> mapInfo = (Map<String,String>)iterator.next();
                 String strType = mapInfo.get(DomainConstants.SELECT_TYPE);
                 //String strRelOID = mapInfo.get(CHiPSConstants.relationship+CHiPSDomainConstant.RELATIONSHIP_MBE_TASKS+CHiPSConstants.idClause);
 				if(TYPE_WMS_MEASUREMENT_TASK.equals(strType)|| TYPE_WMS_PAYMENT_ITEM.equals(strType))
                 {
                     arrrayListSelectedRelOIDS.add(arrayListRelOIDs.get(intCounter));
                 }
                 intCounter++;
             }
             return arrrayListSelectedRelOIDS;
         }
         catch(FrameworkException frameworkException)
         {
             frameworkException.printStackTrace();
             throw frameworkException;
         }
         catch(MatrixException matrixException)
         {
             matrixException.printStackTrace();
             throw matrixException;
         }

     }
     
     
     private StringList getSelectedActivitiesInfo() {
         StringList strListInfo = new StringList(3);
         strListInfo.add(DomainConstants.SELECT_TYPE);
         strListInfo.add(DomainConstants.SELECT_ID);
         strListInfo.add("relationship["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"].id");
         return strListInfo;
     }
     
     private ArrayList<String> getRelationshipOIDs(String[] emxTableRowId) {
         ArrayList<String> arrayListRelOIDs = new ArrayList<String>();
         int intSize = emxTableRowId.length;
         for (int i = 0; i < intSize; i++)
         {
             String strRelOID = emxTableRowId[i].split("[|]")[0];

             arrayListRelOIDs.add(strRelOID);
         }
         return arrayListRelOIDs;
     }
     
     
     

     public  Boolean isTaskOpenForCotextUser(Context context, String [] args)    throws Exception {
         Boolean bReturn = new Boolean(Boolean.FALSE);
         try
         {
             HashMap programMap          = (HashMap)JPO.unpackArgs(args);
             
             String strMbId     = (String)programMap.get("objectId");
             
             String strVault = context.getVault().getName();
             
             
             StringList strListBusInfo = new StringList(2);
             strListBusInfo.add(DomainConstants.SELECT_ID);
             strListBusInfo.add(DomainConstants.SELECT_CURRENT);
             
             StringList slRelSelect     =    new StringList();
             slRelSelect.add(DomainRelationship.SELECT_ID);
             
             String strWhere     = DomainConstants.EMPTY_STRING;
             String strRelWhere     = DomainConstants.EMPTY_STRING;
             com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
             
             
             Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
             patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

             Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
             patternType.addPattern(DomainConstants.TYPE_ROUTE);    
             patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);  
             patternType.addPattern(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY);              
             
             DomainObject domObj = DomainObject.newInstance(context,strMbId);
             String strObjectType= domObj.getInfo(context,DomainConstants.SELECT_TYPE);
             if(strObjectType.equals(TYPE_WMS_MEASUREMENT_BOOK_ENTRY))
             {
             strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MEASUREMENT_BOOK_ENTRY+"&& current==Review && id=="+strMbId+")";
             }
             else
             {
             strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY+"&& current==Review && id=="+strMbId+")";
             }

             
             MapList mlMBEList                     = contextPerson.getRelatedObjects(context,
                                                                                     //patternRel.getPattern(),  // relationship pattern
                                                                                     patternRel.getPattern(),
                                                                                     patternType.getPattern(),  // object pattern
                                                                                     true,                                                        // to direction
                                                                                     true,                                                       // from direction
                                                                                     (short)0,                                                      // recursion level
                                                                                     strListBusInfo,                                                 // object selects
                                                                                     slRelSelect,                                                         // relationship selects
                                                                                     strWhere,                                // object where clause
                                                                                     strRelWhere,                                // relationship where clause
                                                                                     (short)0,                                                      // No expand limit
                                                                                     DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                                                     TYPE_WMS_MEASUREMENT_BOOK_ENTRY,                                                // postTypePattern
                                                                                     null);
            
             if(mlMBEList.size()>0)
             {
                 bReturn=    Boolean.TRUE;
             }   
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
         return bReturn;
     }
     
     
     public Boolean isMBEditable (Context context, String[] args) throws Exception 
     {Boolean bReturn = false ;
     try {
      	HashMap programMap 			= (HashMap)JPO.unpackArgs(args);
         String sMeasurementOid 		= (String)programMap.get("objectId");
         String strVendorComment 	= "";
         String strState	= "";
     	StringList slSelect = new StringList(2);
     	slSelect.add(DomainConstants.SELECT_CURRENT);
     	if(UIUtil.isNotNullAndNotEmpty(sMeasurementOid))
     	{
     		DomainObject domMeasurement = DomainObject.newInstance(context,sMeasurementOid);
     		Map map = (Map)domMeasurement.getInfo(context,slSelect);
     		
     		if(map!=null&&!map.isEmpty())
     		{
     			  if("".equals(strVendorComment))
     			{
     				
     				bReturn = true ;
     			}
     		}
     	}
     } catch (Exception e) {
     }
     return bReturn;
     }
     
     
     
    
     
     /**
      * Method to get the connected Measurements of a activity under
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return mapListConnectedTasks MapList containing the Task IDs
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getActivityMeasurements (Context context, String[] args) throws Exception 
     {
         try
         {
             MapList mapListConnectedMeasurements = new MapList();
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             HashMap paramMap = (HashMap)programMap.get("paramMap");
             String strObjectId = (String) programMap.get("objectId");
             String strRelOID  = (String)programMap.get("relId");
             if(UIUtil.isNotNullAndNotEmpty(strRelOID)&& UIUtil.isNotNullAndNotEmpty(strObjectId))
             {
                 DomainObject domObj = DomainObject.newInstance(context, strObjectId);
              if(domObj.isKindOf(context, TYPE_WMS_MEASUREMENT_TASK) )  // we dont need this ||domObj.isKindOf(context, TYPE_CHIPS_PAYMENT_ITEM))
                 {
                     StringList strListInfoSelects=new StringList(2);
                     strListInfoSelects.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].id");
                     strListInfoSelects.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");

                     DomainObject.MULTI_VALUE_LIST.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].id");
                     DomainObject.MULTI_VALUE_LIST.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");
                     String[] strEBOMList={strRelOID};
                     MapList mapListInfo = DomainRelationship.getInfo(context, strEBOMList, strListInfoSelects);
                     DomainObject.MULTI_VALUE_LIST.remove("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].id");
                     DomainObject.MULTI_VALUE_LIST.remove("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");
                     mapListConnectedMeasurements = getActivityMeasurements(context,mapListInfo);
                 }
 
             }
             return mapListConnectedMeasurements;
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     
     
     
     private MapList getActivityMeasurements(Context context,
             MapList mapListInfo) throws FrameworkException {
         try
         {
             MapList mapListConnectedMeasurements = new MapList();
             StringList strListRelOIDs = new StringList();
             StringList strListObjOIDs = new StringList();
             Iterator iterator = mapListInfo.iterator();
             while(iterator.hasNext())
             {
                 Map<String,Object> mapInfo = (Map<String,Object>)iterator.next();
                 Object objectRelOIDs = (Object) mapInfo.get("tomid[WMSActivityMeasurements].id");
                 Object objectObjOIDs = (Object) mapInfo.get("tomid[WMSActivityMeasurements].from.id");

                 //check whether the dependency list has one or many ids
                 if (objectRelOIDs instanceof String){
                     strListRelOIDs.add((String)objectRelOIDs);
                     strListObjOIDs.add((String)objectObjOIDs);
                 } else if (objectRelOIDs instanceof StringList) {
                     strListRelOIDs = (StringList) objectRelOIDs;
                     strListObjOIDs = (StringList) objectObjOIDs;
                 }
             }

             int intSize = strListObjOIDs.size();
             MapList mapListMeasurementInfo = new MapList(intSize);
             if(intSize>0)
             {
                 ArrayList<String> arrayListOIDS = new ArrayList<String>(strListObjOIDs);
                 String [] strMeasurementOIds = arrayListOIDS.toArray(new String[intSize]);
                 StringList strListInfo = new StringList(12);
                 strListInfo.add(DomainConstants.SELECT_ID);
                 strListInfo.add(DomainConstants.SELECT_TYPE);
                 strListInfo.add(DomainConstants.SELECT_CURRENT);
                 strListInfo.add(DomainConstants.SELECT_REVISION);
                 strListInfo.add(DomainConstants.SELECT_DESCRIPTION);
                 strListInfo.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                 strListInfo.add("attribute["+ATTRIBUTE_WMS_IS_DEDUCTION+"]");
 				
                 mapListMeasurementInfo = DomainObject.getInfo(context, strMeasurementOIds, strListInfo);
             }
             Iterator<Map<String,String>> iteratorMeasurement = mapListMeasurementInfo.iterator();
             String isDuduction = "no";
             while(iteratorMeasurement.hasNext())
             {
                 Map<String,String> mapMeasurement = iteratorMeasurement.next();
                 String strMeasurementOID= mapMeasurement.get(DomainConstants.SELECT_ID);
                 isDuduction=mapMeasurement.get("attribute["+ATTRIBUTE_WMS_IS_DEDUCTION+"]");
                 if(isDuduction.equalsIgnoreCase("Yes"))
                	 mapMeasurement.put("styleRows", "ResourcePlanningRedBackGroundColor");
                 int intIndex = strListObjOIDs.indexOf(strMeasurementOID);
                 if(intIndex>=0)
                 {
                     String strExpandRelOID = (String)strListRelOIDs.get(intIndex);
                     mapMeasurement.put(DomainConstants.SELECT_LEVEL, "1");
                     mapMeasurement.put(DomainRelationship.SELECT_ID, strExpandRelOID);
                     mapListConnectedMeasurements.add(mapMeasurement);
                 }
             }
             return mapListConnectedMeasurements;
         }
         catch(FrameworkException frameworkException)
         {
             frameworkException.printStackTrace();
             throw frameworkException;
         }
     }
     
     /**
      * Method to MBE title while connecting the Work Order and MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed MBE and Work Order Id
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     public void updateMBETitle(Context context,String[]args)throws Exception
     {
         try
         {
             String strMBEOID = args[0];
             String  strWorkOrderOID = args[1];
             String  strRelID = args[2];
             boolean isContextPushed = false;
             if(UIUtil.isNotNullAndNotEmpty(strMBEOID)&& UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
             {
                 DomainObject domObjWO  = DomainObject.newInstance(context, strWorkOrderOID);
                 DomainObject domObjMBE  = DomainObject.newInstance(context, strMBEOID);
                 int intSequence = getNewMBESequence(context, domObjWO,RELATIONSHIP_WMS_WORK_ORDER_MBE);
                 intSequence--;
                 NumberFormat numberFormat = new DecimalFormat("0000");
                 String strSequence = numberFormat.format(intSequence);
                 //TODO use constant entry
                 String strAgreementNumber = domObjWO.getAttributeValue(context, ATTRIBUTE_WMS_PO_NUMBER);
                 String strPrefix =  EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Prefix.MBE");
                 String strName = strAgreementNumber.concat("-").concat(strPrefix).concat("-").concat(strSequence);
                 domObjMBE.setName(context,strName);
                 DomainRelationship domRel = DomainRelationship.newInstance(context, strRelID);
                 try
                 {
                     ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                     domRel.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER, String.valueOf(intSequence));
                     isContextPushed = true;
                 }
                 catch (Exception exception)
                 {
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
         }
         catch(Exception e)
         {
             e.printStackTrace();
             throw e;
         }
     }    

     
     /**
      * Method to get the connected Items under the Work Order from MBE add existing 
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return mapListConnectedTasks MapList containing the Task IDs
      * @throws Exception if the operation fails
      * @author WMS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
     public StringList getRelatedActivities (Context context, String[] args) throws Exception 
     {
         try
         {
             StringList strListActivitiesOIDs = new StringList();
 			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
             String strRel = DomainConstants.EMPTY_STRING;
             String strRelSeg = DomainConstants.EMPTY_STRING;
             if(UIUtil.isNotNullAndNotEmpty(strObjectId))
             {
                 StringList strListBusSelects     = new StringList(1);
                 strListBusSelects.add(DomainConstants.SELECT_ID);

                 DomainObject domObjMBE= DomainObject.newInstance(context, strObjectId);
                 String strType = domObjMBE.getInfo(context, DomainConstants.SELECT_TYPE);
                 if( TYPE_WMS_MEASUREMENT_BOOK_ENTRY.equals(strType))
                 {
                     strRel= RELATIONSHIP_WMS_WORK_ORDER_MBE;
                     strRelSeg = RELATIONSHIP_WMS_SEGMENT_MBE;
                 }
                 else if( TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY.equals(strType))
                 {
                     strRel= RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE;
                 }
                 String strWorkOrderOID = domObjMBE.getInfo(context, "relationship["+strRel+"].from.id");

                 MapList mapListConnectedActivities = getMBEActivities(context, domObjMBE);
 				StringList strListConnectdActivities = WMSUtil_mxJPO.convertToStringList(mapListConnectedActivities, DomainConstants.SELECT_ID);

                 if(UIUtil.isNotNullAndNotEmpty(strRelSeg))
                 {
                     String strSegmentOID = domObjMBE.getInfo(context,"relationship["+strRelSeg+"].from.id");
                     if(UIUtil.isNotNullAndNotEmpty(strSegmentOID))
                     {
 						StringList strListOBJ = getRelatedActivities(context,
 								strListBusSelects, strSegmentOID);
                         strListActivitiesOIDs.addAll(strListOBJ);
                     }
                     else
                     {
                         if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
                         {
                             String strMBOID = getMeasurementBookOID(context,strListBusSelects, strWorkOrderOID);
                             if(UIUtil.isNotNullAndNotEmpty(strMBOID))
                             {
                                 StringList strListOBJ = getRelatedActivities(context,
                                 strListBusSelects, strMBOID);
                                 strListActivitiesOIDs.addAll(strListOBJ);

                             }
                         }
                     }
                     
                 }
                 else
                 {
                     if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
                     {
                         String strMBOID = getMeasurementBookOID(context,strListBusSelects, strWorkOrderOID);
                         if(UIUtil.isNotNullAndNotEmpty(strMBOID))
                         {
                             StringList strListOBJ = getRelatedActivities(context,
                             strListBusSelects, strMBOID);
                             strListActivitiesOIDs.addAll(strListOBJ);
                         }
                     }
                 }
                 strListActivitiesOIDs.removeAll(strListConnectdActivities);
             }
             return strListActivitiesOIDs;
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     /**
      * Function to get the Tasks connected to the selected Measurement Book
      *
      * @param context the eMatrix <code>Context</code> object
      * @param strMBOID String value containing the Measurement Book OID 
      * @param strListBusSelects StringList containing the bus selects
      * @return strListOBJ StringList  containing the Measurement Activities OIDs
      * @throws FrameworkException if the operation fails
      * @author CHiPS
      * @since 418
      */
     private StringList getRelatedActivities(Context context,
             StringList strListBusSelects, String strMBOID)
                     throws FrameworkException {
         try
         {
             DomainObject domObjMB= DomainObject.newInstance(context, strMBOID);

             Pattern patternType = new Pattern(TYPE_WMS_SEGMENT);
             patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
             MapList mapListActivities = domObjMB.getRelatedObjects(context,
                     RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
                     patternType.getPattern(),                                    // object pattern
                     false,                                                        // to direction
                     true,                                                       // from direction
                     (short)0,                                                      // recursion level
                     strListBusSelects,                                                 // object selects
                     null,                                                         // relationship selects
                     DomainConstants.EMPTY_STRING,                                // object where clause
                     DomainConstants.EMPTY_STRING,                                // relationship where clause
                     (short)0,                                                      // No expand limit
                     DomainConstants.EMPTY_STRING,                                // postRelPattern
                     TYPE_WMS_MEASUREMENT_TASK,                                                // postTypePattern
                     null);                                                      // postPatterns
 			StringList strListOBJ = WMSUtil_mxJPO.convertToStringList(mapListActivities, DomainConstants.SELECT_ID);
             return strListOBJ;
         }
         catch(FrameworkException frameworkException)
         {
             throw frameworkException;
         }
     }
    
     /**
      * Function to new Sequence number of the newly created MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param domObjWO DomainObject instance of selected Work Order 
      * @return intSequence Integer containing the sequence of the newly created MBE in that project
      * @throws FrameworkException if the operation fails
      * @author CHiPS
      * @since 418
      */
     private int getNewMBESequence(Context context, DomainObject domObjWO,String strRelationship)
             throws FrameworkException {
         try
         {
             int intSequence = 0;
             MapList mapListConnectedMBEs = new MapList();
             String strWhere  = CommonDocument.SELECT_REVISION+"==last";
             try
             {
                         ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                 mapListConnectedMBEs = getConnectedMBEs(context, domObjWO,strRelationship,strWhere);
             }
             catch(Exception exception)
             {
             }
             finally
             {
                  ContextUtil.popContext(context);
             }
             
             if(mapListConnectedMBEs!=null )
             {
                 intSequence = mapListConnectedMBEs.size();
             }
             intSequence++;
             return intSequence;
         }
         catch(FrameworkException frameworkException)
         {
             frameworkException.printStackTrace();
             throw frameworkException;
         }
     }
     
     /**
      * Function to get the MBEs connected to the selected Work Order
      *
      * @param context the eMatrix <code>Context</code> object
      * @param domObj DomainObject instance of selected Object
      * @param strRelationship string value containing the relation with which the Object is connected
      * @return mapListMBEs MapList containing the MBEs connected to Work Order with ID
      * @throws FrameworkException if the operation fails
      * @author CHiPS
      * @since 418
      */
     private MapList getConnectedMBEs(Context context, DomainObject domObj,String strRelationship,String strWhere)
             throws FrameworkException {
         MapList mapListMBEs  = new MapList();
         try
         {
             StringList strListBusSelects     = new StringList(6);
             strListBusSelects.add(DomainConstants.SELECT_ID);
             strListBusSelects.add(DomainConstants.SELECT_REVISION);
             strListBusSelects.add(DomainConstants.SELECT_NAME);
             strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ITEM_TYPE+"]");
            // strListBusSelects.add"attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_FINALIZED+"]");
             //strListBusSelects.add("relationship["+RELATIONSHIP_EE_REVIEW_MBE+"].to.id");
             StringList strListRelSelects     = new StringList(2);
             strListRelSelects.add(DomainRelationship.SELECT_ID);
             strListRelSelects.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
                 mapListMBEs = domObj.getRelatedObjects(context, // matrix context
                         strRelationship, // relationship pattern
                          TYPE_WMS_MEASUREMENT_BOOK_ENTRY, // type pattern
                         strListBusSelects, // object selects
                         strListRelSelects, // relationship selects
                         false, // to direction
                         true, // from direction
                         (short) 1, // recursion level
                         strWhere, // object where clause
                         DomainConstants.EMPTY_STRING, // relationship where clause
                         0);
         }
         catch(Exception Exception)
         {
             throw Exception;
         }
         return mapListMBEs;
     }
     
   /**
    * Trigger method on WMSWorkOrder Create promote action which promotes all its child  
    *   
    *   
    *   
    * @param context
    * @param args
    * @throws Exception
    */
     
     public void promoteChildTasks(Context context, String[] args) throws Exception {
         try {
             String strBOQID = args[0];
             String strType  = args[1];
             String strWherCond =DomainConstants.SELECT_CURRENT+"==Create";
             StringList strListBusSelects     = new StringList(1);
             strListBusSelects.add(DomainConstants.SELECT_ID);
                         if (UIUtil.isNotNullAndNotEmpty(strBOQID)) {
                     
                 if(TYPE_WMS_WORK_ORDER.equals(strType))
                 {
                     DomainObject domObjWorkOrder = DomainObject.newInstance(context,strBOQID);
                     String strMBOID = getMeasurementBookOID(context,strListBusSelects, strBOQID);
                     if(UIUtil.isNotNullAndNotEmpty(strMBOID))
                     {
                     DomainObject domObjMBO = DomainObject.newInstance(context,strMBOID);
                         domObjMBO.promote(context);
                     Pattern patternType = new Pattern(TYPE_WMS_SEGMENT);
                     patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
                     MapList mapListBOQItems = domObjMBO.getRelatedObjects(context,
                     RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
                     patternType.getPattern(),                                    // object pattern
                     false,                                                        // to direction
                     true,                                                       // from direction
                                 (short)0,                                                      // recursion level
                     strListBusSelects,                                                 // object selects
                     null,                                                         // relationship selects
                     strWherCond,                                // object where clause
                     DomainConstants.EMPTY_STRING,                                // relationship where clause
                     (short)0,                                                      // No expand limit
                     RELATIONSHIP_BILL_OF_QUANTITY,                                // postRelPattern
                     patternType.getPattern(),                                                // postTypePattern
                     null);   
                     Iterator<Map<String,String>> ChildIterator = mapListBOQItems.iterator();
                     Map<String,String> childmapData;
                     DomainObject domChildObjItem  = DomainObject.newInstance(context);
                             
                         while(ChildIterator.hasNext())
                         {
                             childmapData = ChildIterator.next();
                             String strChildOID = childmapData.get(DomainConstants.SELECT_ID);
                             String strChildType = childmapData.get(DomainConstants.SELECT_TYPE);                             
                             domChildObjItem.setId(strChildOID);
                             domChildObjItem.promote(context);
                         }
                     }
                 }
             }//End If
         }//End Try
         catch (Exception exception) 
         {
             exception.printStackTrace();
         }
     }
     /**
      * Table WMSMBEActivities : column ImageDropZone
      * 
      * 
      * 
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     
     public Vector getImageHolder(Context context, String[] args) throws Exception {
         
         Vector returnVector = new Vector();
         try
         {
             com.matrixone.apps.common.util.ImageManagerImageInfo info = new ImageManagerImageInfo();
             
             Map programMap        = (Map)JPO.unpackArgs(args);
             Map paramList      = (HashMap) programMap.get("paramList");
             MapList objectList = (MapList)programMap.get("objectList");
             int intSize = objectList.size();
             
             String strHtml        = new String();
             Map dataMap         = null;
             String strObjectId    = DomainConstants.EMPTY_STRING;
             String strRowId    =    DomainConstants.EMPTY_STRING;
             String strType     = DomainConstants.EMPTY_STRING;
             for (int i = 0; i < intSize ; i++) {
                 dataMap            = (Map) objectList.get(i);
                 strObjectId     = (String) dataMap.get("id");
                 strRowId     = (String) dataMap.get("id[level]");
                 strType    =    (String) dataMap.get("type");
                 if(TYPE_WMS_MEASUREMENTS.equals(strType))
                 {
                     strHtml="<div ><form id='imageUpload"+strObjectId+"' action='../common/emxExtendedPageHeaderFileUploadImage.jsp?objectId="+strObjectId+"' method='post'>   <div style='height:30px' id='divDropImages"+strObjectId+"' class='dropArea' ondrop='ImageDropColumn(event, &quot;imageUpload"+strObjectId+"&quot;, &quot;divDropImages"+strObjectId+"&quot; , &quot;"+strRowId+"&quot; )' ondragover='ImageDragHover(event, &quot;divDropImages"+strObjectId+"&quot;)' ondragleave='ImageDragHover(event, &quot;divDropImages"+strObjectId+"&quot;)'> Drop<br></br>images</div></form></div>";
                 }
                 else
                 {
                     strHtml="";
                 }
                 returnVector.add(strHtml);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         return returnVector;
     }
     
     
     
     
     /**
      * Method to add measurement to the Item in the MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return Map<String,String> containing the newly created measurements and relOIDs 
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public Map<String,String> addMeasurements (Context context, String[] args) throws Exception 
     {
         try
         {
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
             Map<String,String> mapObject = new HashMap<String, String>();

             if(emxTableRowId !=null && emxTableRowId.length>0)
             {
                 ArrayList<String> arrayListRelOIDs = getSelectedActivities(context, emxTableRowId);
                
                 
                 String strItemOID = emxTableRowId[0];
                 String strItemMBERelOID = DomainConstants.EMPTY_STRING;
                 if(!arrayListRelOIDs.isEmpty())
                 {
                     strItemMBERelOID = arrayListRelOIDs.get(0);
                 }
                 if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strItemMBERelOID))
                 {
                     DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
                     StringList strListInfoSelects = new StringList(3);
                     strListInfoSelects.add(DomainConstants.SELECT_TYPE);
                     strListInfoSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
                     strListInfoSelects.add("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
                     Map<String,String> mapInfo = domObjItem.getInfo(context, strListInfoSelects);
                     String strName = mapInfo.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"].value");
                     String strType = mapInfo.get(DomainConstants.SELECT_TYPE);
                     String strSORConnection = mapInfo.get("relationship["+RELATIONSHIP_WMS_TASK_SOR+"]");
                     if(UIUtil.isNullOrEmpty(strSORConnection))
                     {
                         strSORConnection = "false";
                     }
                     if(TYPE_WMS_MEASUREMENT_TASK.equals(strType) ) // we do not need this  ||TYPE_CHIPS_PAYMENT_ITEM.equals(strType))
                     {
                         int intSequence = getMeasurementSequence(context, strItemMBERelOID);
                         int intMeasurementToAdd =0;
                     if(TYPE_WMS_MEASUREMENT_TASK.equals(strType))
                     {
                             intMeasurementToAdd = getNumberOfMeasurements(programMap);
                         }
                         else
                         {
                             intMeasurementToAdd=1;
                         }
                         StringBuffer strBuffer = new StringBuffer();
                         strBuffer.append("<mxRoot>");
                         strBuffer.append("<action><![CDATA[add]]></action>");
                         for(int i=0;i<intMeasurementToAdd;i++)
                         {
                             String strMeasurementOID = FrameworkUtil.autoName(context,
                                     "type_WMSMeasurements",
                                     "policy_WMSMeasurements");
                             //TODO Use MQl Arguments
                             String strMQLCommand = "add connection WMSActivityMeasurements torel "+strItemMBERelOID+" from "+strMeasurementOID;
                             MqlUtil.mqlCommand(context, strMQLCommand);
                             DomainObject domObjMeasurement = DomainObject.newInstance(context, strMeasurementOID);
                             String strMTitle = strName+" - "+intSequence;
                             domObjMeasurement.setAttributeValue(context,"Title",strMTitle);
                             String strRelOID = domObjMeasurement.getInfo(context, "relationship["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].id");

                             strBuffer.append("<data status=\"committed\">");
                             strBuffer.append("<item oid=\""+strMeasurementOID+"\" relId=\""+strRelOID+"\" pid=\""+strItemOID+"\"  direction=\"\" />");
                             strBuffer.append("</data>");
                             intSequence++;
                         }

                         Locale strLocale = context.getLocale();
                         String strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.MBE.CREATEMEASUREMENT.Alert ");
                         strBuffer.append("</mxRoot>");
                         mapObject.put("selectedOID", strItemOID);
                         mapObject.put("rows",strBuffer.toString());
                         mapObject.put("message",strMessage);
                         mapObject.put("SOR_TYPE","SORItem");
                     }
                 }
             }
             return mapObject;
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     
     
     private int getMeasurementSequence(Context context, String strItemMBERelOID) throws FrameworkException {
         StringList strListInfoSelectsRel=new StringList(2);
         strListInfoSelectsRel.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");

         DomainObject.MULTI_VALUE_LIST.add("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");
         String[] strEBOMList={strItemMBERelOID};
         MapList mapListConnectionInfo = DomainRelationship.getInfo(context, strEBOMList, strListInfoSelectsRel);
         DomainObject.MULTI_VALUE_LIST.remove("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");
         StringList strListObjOIDs = new StringList();
         Iterator iteratorTemp = mapListConnectionInfo.iterator();
         StringList strlTitle=new StringList();
         while(iteratorTemp.hasNext())
         {
             Map<String,Object> mapInfoMeasureMent = (Map<String,Object>)iteratorTemp.next();
             Object objectRelOIDs = (Object) mapInfoMeasureMent.get("tomid["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].from.id");
             //check whether the dependency list has one or many ids
             if (objectRelOIDs instanceof String){
                 strListObjOIDs.add((String)objectRelOIDs);
             } else if (objectRelOIDs instanceof StringList) {
                 strListObjOIDs = (StringList) objectRelOIDs;
             }
         }
         int intSequence = strListObjOIDs.size();
         intSequence++;
         return intSequence;
     }
     private int getNumberOfMeasurements(HashMap programMap) {
         String strMeasurementToAdd = (String)programMap.get("WMSMeasurementsToBeAdded");
         int intMeasurementToAdd = 0;
         if(UIUtil.isNotNullAndNotEmpty(strMeasurementToAdd))
         {
             try
             {
                 intMeasurementToAdd = Integer.parseInt(strMeasurementToAdd);
             }catch(NumberFormatException numberFormatException)
             {
                 intMeasurementToAdd = 1;
             }
         }
         return intMeasurementToAdd;
     }
     
     
     	@com.matrixone.apps.framework.ui.ProgramCallable
	public  MapList getAllMyAbstractMBE(Context context, String [] args)    throws Exception {
        MapList mlReturnList     =     new MapList();
        try
        {
            String strVault = context.getVault().getName();
            
            
            StringList strListBusInfo = new StringList(2);
            strListBusInfo.add(DomainConstants.SELECT_ID);
            strListBusInfo.add(DomainConstants.SELECT_CURRENT);
            
            StringList slRelSelect     =    new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            
            String strWhere     = DomainConstants.EMPTY_STRING;
            String strRelWhere     = DomainConstants.EMPTY_STRING;
            com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
            
            
            Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
            patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

            Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
            patternType.addPattern(DomainConstants.TYPE_ROUTE);    
            patternType.addPattern(WMSConstants_mxJPO.TYPE_ABSTRACT_MBE);  
            
            strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+")||("+DomainConstants.SELECT_TYPE+"=="+WMSConstants_mxJPO.TYPE_ABSTRACT_MBE+"&& current==Approved)";


            mlReturnList                     = contextPerson.getRelatedObjects(context,
                                                            //patternRel.getPattern(),  // relationship pattern
                                                            patternRel.getPattern(),
                                                            patternType.getPattern(),  // object pattern
                                                            true,                                                        // to direction
                                                            true,                                                       // from direction
                                                            (short)0,                                                      // recursion level
                                                            strListBusInfo,                                                 // object selects
                                                            slRelSelect,                                                         // relationship selects
                                                            strWhere,                                // object where clause
                                                            strRelWhere,                                // relationship where clause
                                                            (short)0,                                                      // No expand limit
                                                            DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                            WMSConstants_mxJPO.TYPE_ABSTRACT_MBE,                                                // postTypePattern
                                                            null);
            
            
            MapList mlOwnerMBE                =  DomainObject.findObjects(context,
                                                                        WMSConstants_mxJPO.TYPE_ABSTRACT_MBE,
                                                                        DomainConstants.QUERY_WILDCARD,
                                                                        DomainConstants.QUERY_WILDCARD,
                                                                        DomainConstants.QUERY_WILDCARD,
                                                                        strVault,
                                                                        "current==Approved || current==Paid",               // where expression
                                                                        DomainConstants.EMPTY_STRING,
                                                                        false,
                                                                        strListBusInfo, // object selects
                                                                        (short) 0); 
            
            mlReturnList.addAll(mlOwnerMBE);
      insertKeyValue(mlReturnList, DomainConstants.SELECT_LEVEL, "1");
            
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        
        return mlReturnList;
    }
     
     public Vector displayLocation(Context context, String[] args) throws Exception {
         
         Vector returnVector = new Vector();
         try
         {
             Map programMap        = (Map)JPO.unpackArgs(args);
             Map paramList      = (HashMap) programMap.get("paramList");
             MapList objectList = (MapList)programMap.get("objectList");
             int intSize = objectList.size();
             
             String strHtml        = new String();
             Map dataMap         = null;
             String strObjectId    = DomainConstants.EMPTY_STRING;
             String strRowId    =    DomainConstants.EMPTY_STRING;
             String strType     = DomainConstants.EMPTY_STRING;
             String sObjectName     = DomainConstants.EMPTY_STRING;
             for (int i = 0; i < intSize ; i++) {
                 dataMap            = (Map) objectList.get(i);
                 
                 strObjectId     = (String) dataMap.get("id");
                 strRowId     = (String) dataMap.get("id[level]");
                 strType    =    (String) dataMap.get("type");
                 sObjectName = (String)dataMap.get("attribute[Title]");
                 
                 StringList sl = new StringList("attribute["+ATTRIBUTE_WMS_MEASUREMENT_LOCATION+"]");
                 sl.add("attribute["+ATTRIBUTE_WMS_MEASUREMENT_ADDRESS+"].value");
                 Map attList = new DomainObject(strObjectId).getInfo(context, sl);
             
                  StringBuilder strUrl = new StringBuilder();
                 String strGPSLocation = (String)attList.get("attribute["+ATTRIBUTE_WMS_MEASUREMENT_LOCATION+"]");
                 if(TYPE_WMS_MEASUREMENTS.equals(strType) && UIUtil.isNotNullAndNotEmpty(strGPSLocation))
                 {
                     String strLink = "../wms/wmsMeasurementGSPlocator.jsp?GPSLocation="+strGPSLocation;
                     strUrl.append("<a href=\"javascript:showModalDialog('"+strLink+"','600','400','false');\" >");            
                     strUrl.append("<img border='0' title='map' src='../common/images/map.png' height='25px' name='map' id='map' alt='map' />");
                     strUrl.append("</a>");
                 }
                 else
                 {
                     strUrl.append(attList.get("attribute["+ATTRIBUTE_WMS_MEASUREMENT_ADDRESS+"]"));
                 }
                 returnVector.add(strUrl.toString());
                 
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         return returnVector;
     }
    /**Method to connect Approval Template with MBE 
     * 
     * @param context
     * @param args
     * @throws Exception
     */
     
     public void updateMBEApprovalTemplate(Context context,String[] args) throws Exception
     {
    	try { 
    		String strExistRelId ="";
    	    Map mInput  = JPO.unpackArgs(args);
    	    Map paramMap = (Map)mInput.get("paramMap");
    	    String strNewAprTemplate =(String) paramMap.get("New Value");
    	    String strMBEOid  = (String)paramMap.get("objectId");
    	    DomainObject dom=DomainObject.newInstance(context,strMBEOid);
    	    String strType = dom.getInfo(context, DomainConstants.SELECT_TYPE);
    	    String strRel=RELATIONSHIP_WMS_AMBE_APPROVAL_TEMPLATE;
            if(strType.equals(TYPE_WMS_MEASUREMENT_BOOK_ENTRY)) {
            	strRel =RELATIONSHIP_WMS_MBE_APPROVAL_TEMPLATE;
            }else if(strType.equals(TYPE_WMS_MATERIAL_BILL)) {
             	strRel = RELATIONSHIP_WMS_MB_APPROVAL_TEMPLATE;
            }
            strExistRelId = dom.getInfo(context, "from["+strRel+"].id");
    	    if(strExistRelId!=null && !strExistRelId.isEmpty()) {
    	    	DomainRelationship.disconnect(context, strExistRelId);
    	    }
    	    if(strNewAprTemplate!=null && !strNewAprTemplate.isEmpty()) {
    	     		DomainRelationship.connect(context, dom, new RelationshipType(strRel), new DomainObject(strNewAprTemplate));
              }
    	 
    	 
      	}catch(Exception e) {
    		e.printStackTrace();
    	}}
     
     
     
     /**
      * Method to get the connected MBEs under the Work Order
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command or form or table
      * @return mapListConnectedMBEs MapList containing the MBEs IDs
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getWorkOrderMBEs (Context context, String[] args) throws Exception 
     {
         try
         {
             MapList mapListConnectedMBEs = new MapList();
 			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
             if(UIUtil.isNotNullAndNotEmpty(strObjectId))
             {
                 DomainObject domObjWO = DomainObject.newInstance(context, strObjectId);
                // mapListConnectedMBEs = getConnectedMBEs(context, domObjWO,CHiPSDomainConstant.RELATIONSHIP_WORKORDER_MBE,DomainConstants.EMPTY_STRING);
                 mapListConnectedMBEs = getConnectedMBEs(context, domObjWO,RELATIONSHIP_WMS_WORK_ORDER_MBE,"current.access[read]==TRUE");
                 mapListConnectedMBEs.addSortKey(DomainConstants.SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
                 mapListConnectedMBEs.addSortKey(DomainObject.SELECT_REVISION, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
                 mapListConnectedMBEs.sort();
               /*  Map<String,String> mapData;
                 Iterator<Map<String,String>> iterator = mapListConnectedMBEs.iterator();
                 while(iterator.hasNext())
                 {
                     mapData =  (Map<String,String>)iterator.next();
                     String strOID =  mapData.get(DomainConstants.SELECT_ID);
                     String strMBEFinalized = mapData.get("relationship["+RELATIONSHIP_EE_REVIEW_MBE+"].to.id");
                     if(strOID.equalsIgnoreCase(strMBEFinalized))
                     {
                         mapData.put("styleRows","ResourcePlanningGreenBackGroundColor");
                     }
                 }*/
             }
             return mapListConnectedMBEs;
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     /**Create Promote Check Trigger on Policy WMSMeasurementBookEntry 
      * 
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     
     public int triggerIsApprovalTemplateConnected(Context context, String[] args) throws Exception{
    	 try {
    		 String strExistRel ="";
    		  String strMEBOid = args[0];
    		  DomainObject domMBE = DomainObject.newInstance(context, strMEBOid);	
    		  String strType = domMBE.getInfo(context, DomainConstants.SELECT_TYPE);
    		  String strRel=RELATIONSHIP_WMS_AMBE_APPROVAL_TEMPLATE;
    		  if(strType.equals(TYPE_WMS_MATERIAL_BILL)) {
    			  strRel=RELATIONSHIP_WMS_MB_APPROVAL_TEMPLATE;
    		  }
    		  if(strType.equals(TYPE_WMS_MEASUREMENT_BOOK_ENTRY)) {
    			  strRel = RELATIONSHIP_WMS_MBE_APPROVAL_TEMPLATE;
    		  }
    		  strExistRel = domMBE.getInfo(context, "from["+strRel+"]");
    		  if(strExistRel.equalsIgnoreCase("FALSE")) {
    			  Locale strLocale = context.getLocale();
                  String strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.alert.NoApprovalTemplate");
                  emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                  return 1;
    		  }
     	    }catch(Exception e) {
    		 e.printStackTrace();
    	 }
    	 
    	 
         return 0; 
      }
     
     
     
     /**
      * update function
      * updates Measurement Title
      * @param context
      * @param args new title for Measurement and object id of measurement
      *    objectId - Measurement id
      *    New Value -Title
      * @throws Exception if the operation fails
      */
     public void updateMeasurementTitle (Context context, String[] args) throws Exception 
     {
         try {
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             HashMap paramMap = (HashMap)programMap.get("paramMap");
             String sMeasurementOid = (String)paramMap.get("objectId");
             String sTitle = (String)paramMap.get("New Value");
             DomainObject doMeasurement = DomainObject.newInstance(context, sMeasurementOid);
             doMeasurement.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, sTitle);
         }
         catch (Exception e) {
             throw e;
         }
     }
     
     
     public int triggerAutoCreateApprovalRoute(Context context,String[] args) throws Exception
     {
      try {	  
    	   String strApprovalTemplOid ="";
    	   String strMBEOid = args[0];
    	   String strPurpose=args[1];
    	   String strBasePolicy=args[2];
    	   String strBaseState=args[3];
    	   String strRelWhere ="attribute["+Route.ATTRIBUTE_ROUTE_STATUS+"]==\"Stopped\"";
    	   DomainObject domMBE= DomainObject.newInstance(context, strMBEOid);
    	   String strRel=RELATIONSHIP_WMS_AMBE_APPROVAL_TEMPLATE;
    	   String strType = domMBE.getInfo(context, DomainConstants.SELECT_TYPE);
           if(strType.equals(TYPE_WMS_MATERIAL_BILL)) {
        	   strRel=RELATIONSHIP_WMS_MB_APPROVAL_TEMPLATE;
           }if(strType.equals(TYPE_WMS_MEASUREMENT_BOOK_ENTRY)) {
        	  strRel=RELATIONSHIP_WMS_MBE_APPROVAL_TEMPLATE;
           }
           strApprovalTemplOid = domMBE.getInfo(context, "from["+strRel+"].to.id");
    	   Map mRouteAttrib= new HashMap();
    	   Map reviewerInfo= new HashMap();
    	   mRouteAttrib.put("Route Completion Action", "Promote Connected Object");
    	   MapList mlExtRoutes = WMSUtil_mxJPO.checkForExistingRouteObject(context, domMBE, strRelWhere);
    	   if(mlExtRoutes.size()>0) {
    		   WMSUtil_mxJPO.restartExistingRoute(context,mlExtRoutes);
    	   }else {
    		     
    		     Map objectRouteAttributeMap=new HashMap(); 
    		    objectRouteAttributeMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY,FrameworkUtil.getAliasForAdmin(context, "Policy", strBasePolicy,false ));;
				objectRouteAttributeMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE,"state_Review");
				objectRouteAttributeMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strPurpose);
				//  ContextUtil.pushContext(context, "TestPM",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
    		    Route.createAndStartRouteFromTemplateAndReviewers(context,
    				   strApprovalTemplOid,
    				    "Measurement Review",
    				     context.getUser() , 
    				    strMBEOid,
    				    strBasePolicy,
    				    strBaseState, 
    				    mRouteAttrib,
    				    objectRouteAttributeMap, 
    				    reviewerInfo,
    				    true);
    		   
    		  
    	   }
    	 
         }catch(Exception e) {
        	//  ContextUtil.popContext(context);
    	  e.printStackTrace();
      } 
      return 0;
     } 
     /**Method to show MeasurebookEntry dashbord page  check if user is Supplier representative or  part of CIVIL dept 
      *  WMSMyApprovalMBE 
      *   WMSALLApprovalMBE
      * @param contet
      * @param args
      * @return
      * @throws Exception
      */
     
     public boolean showMBEDashboardCommnad(Context contet,String[] args) throws Exception{
    	 
    	
      try { 
    	    if(!PersonUtil.hasAssignment(context, DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE)) {
    	    	
    	    	 MSILAccessRights_mxJPO msilAccess=new MSILAccessRights_mxJPO(context,args);
    	         if(!msilAccess.hasAccessForOOTBCommand(context, "WMSCIVIL.DepartmentDashboard.ENAccess")) {
    	        	 return true;
    	         }else {
    	        	 return false;
    	         }
    	      }
    	    return true;
    	  
      	}catch(Exception e) {
    		
    		e.printStackTrace();
    	}
       
       return false;
     }
     
     
     public Map calculateArea(Context context,String strShapeName,String strDim1,String strDim2,String strDim3,String strDim4){
    		Map attributeMap = new HashMap();
    		double dbArea = 0.0;
    		switch(strShapeName)
    		{
    			case "Line":
    				dbArea = WMSUtil_mxJPO.convertToDouble(strDim2);
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Circle":
    				dbArea = 3.14*WMSUtil_mxJPO.convertToDouble(strDim1)*WMSUtil_mxJPO.convertToDouble(strDim1);
    				attributeMap.put("CHiPSDimension1", strDim1);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Square":
    				dbArea = WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim2);
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Rectangle":
    				dbArea = WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim3);
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("CHiPSDimension3", strDim3);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Trapezoid":
    				dbArea = ((WMSUtil_mxJPO.convertToDouble(strDim2)+WMSUtil_mxJPO.convertToDouble(strDim3))/2)*WMSUtil_mxJPO.convertToDouble(strDim4);
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("CHiPSDimension3", strDim3);
    				attributeMap.put("CHiPSDimension4", strDim4);
    				attributeMap.put("Area", dbArea);
    				break;		
    			case "Hexagon":
    				//3root(3)/2)*Side*Side
    				dbArea = 3*Math.sqrt(3)/2*(WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim2));
    				attributeMap.put("CHiPSDimension2", strDim2);	
    				attributeMap.put("Area", dbArea);
    				break;
    			
    			case "HexagonalPrism":
    				dbArea = (3*Math.sqrt(3)/2)*(WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim2))*(WMSUtil_mxJPO.convertToDouble(strDim4)*WMSUtil_mxJPO.convertToDouble(strDim4));
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("CHiPSDimension4", strDim4);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Cube":
    				dbArea = WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim2);
    				attributeMap.put("CHiPSDimension2", strDim2);	
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Cuboid":
    				dbArea = WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim3)+WMSUtil_mxJPO.convertToDouble(strDim4);
    				attributeMap.put("CHiPSDimension2", strDim2);
    				attributeMap.put("CHiPSDimension3", strDim3);
    				attributeMap.put("CHiPSDimension4", strDim4);
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Trapezoidal":
    				dbArea = ((WMSUtil_mxJPO.convertToDouble(strDim2)/2)*(WMSUtil_mxJPO.convertToDouble(strDim3)))*WMSUtil_mxJPO.convertToDouble(strDim4);					
    				attributeMap.put("CHiPSDimension3", strDim3);//a,b
    				attributeMap.put("CHiPSDimension4", strDim4);//h
    				attributeMap.put("CHiPSDimension2", strDim2);//l
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Cylinder":
    				//2*3.14*Radius*Height+2*3.14*Radius*Radius
    				dbArea = 3.14*(WMSUtil_mxJPO.convertToDouble(strDim1)*WMSUtil_mxJPO.convertToDouble(strDim1))*WMSUtil_mxJPO.convertToDouble(strDim4);
    				attributeMap.put("CHiPSDimension1", strDim1);
    				attributeMap.put("CHiPSDimension4", strDim4);	
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Cone":
    				//3.14*Radius(Radius+root(Height*Height+Radius*Radius)
    				dbArea = 3.14*WMSUtil_mxJPO.convertToDouble(strDim1)*WMSUtil_mxJPO.convertToDouble(strDim2);
    				attributeMap.put("CHiPSDimension1", strDim1);
    				attributeMap.put("CHiPSDimension2", strDim2);	
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Pyramid":
    				//Length*Width+Length*root((Width*2)*(Width*2)+Height*Height)+Width*root(Lenght/2+Height)
    				dbArea = (WMSUtil_mxJPO.convertToDouble(strDim2)*WMSUtil_mxJPO.convertToDouble(strDim3)*WMSUtil_mxJPO.convertToDouble(strDim4))/3;
    				attributeMap.put("CHiPSDimension2", strDim2);//l
    				attributeMap.put("CHiPSDimension3", strDim3);//w
    				attributeMap.put("CHiPSDimension4", strDim4);//h
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Sphere":
    				dbArea = 4/3*3.14*(WMSUtil_mxJPO.convertToDouble(strDim1)*WMSUtil_mxJPO.convertToDouble(strDim1));
    				attributeMap.put("CHiPSDimension1", strDim1);	
    				attributeMap.put("Area", dbArea);
    				break;
    			case "Hemisphere":
    				dbArea = 2/3*3.14*(WMSUtil_mxJPO.convertToDouble(strDim1)*WMSUtil_mxJPO.convertToDouble(strDim1));
    				attributeMap.put("CHiPSDimension1", strDim1);	
    				attributeMap.put("Area", dbArea);
    				break;
    		}
    		return attributeMap;
    		

    	}
     /**  Trigger method on Review Promote of Policy WMSMeasurementBookEntry 
      * 
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     
     public int triggerUpdateMBESubmittedQuantity(Context context,String[] args) throws Exception {
     	 try {
    	      String strMBEOid = args[0];
     		  DomainObject domMBE=DomainObject.newInstance(context, strMBEOid);
     		  SelectList selListBusSelects = new SelectList(4);
              selListBusSelects.add(DomainConstants.SELECT_ID);
              selListBusSelects.add(DomainConstants.SELECT_TYPE);
                selListBusSelects.add("to[" + RELATIONSHIP_WMS_MBE_ACTIVITIES 
                      + "|from.current==Create && from.id!=" + strMBEOid + "].id");
                 selListBusSelects.add(
                     "attribute[" + ATTRIBUTE_WMS_MBE_QUANTITY + "]");
                 selListBusSelects.add(
                         "attribute[" + ATTRIBUTE_WMS_MBE_QUANTITY + "]");
               MapList mapListTasks = domMBE.getRelatedObjects(context, // matrix
                      // context
            		  RELATIONSHIP_WMS_MBE_ACTIVITIES, // relationship
                      // pattern
                      TYPE_WMS_MEASUREMENT_TASK, // type pattern
                      selListBusSelects, // object selects
                      null, // relationship selects
                      false, // to direction
                      true, // from direction
                      (short) 1, // recursion level
                      DomainConstants.EMPTY_STRING, // object where clause
                      DomainConstants.EMPTY_STRING, // relationship where
                      // clause
                      0);

              // float itemQty = 0;
              Iterator iterator = mapListTasks.iterator();
              String strMBEQty = "";
              StringList strListObjOIDs = new StringList(10, 2);
              String strTemp = "";
              String strRelId = "";

              while (iterator.hasNext()) {
                  strListObjOIDs.clear();
                  Map<String, String> mapActivity = (Map<String, String>) iterator.next();
                  strMBEQty = (String) mapActivity.get(
                         "attribute[" + ATTRIBUTE_WMS_MBE_QUANTITY + "]");
                  Object objectMBEOIDs = (Object) mapActivity
                          .get("to[" + RELATIONSHIP_WMS_MBE_ACTIVITIES + "].id");

                  // check whether the MBE list has one or many ids
                  if (objectMBEOIDs instanceof String) {
                      strListObjOIDs.add((String) objectMBEOIDs);
                  } else if (objectMBEOIDs instanceof StringList) {
                      strListObjOIDs.addAll((StringList) objectMBEOIDs);
                  }

                  if (!strListObjOIDs.isEmpty()) {
                      StringItr strItr = new StringItr(strListObjOIDs);
                      while (strItr.next()) {
                          strRelId = (String) strItr.value();
                          DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_QTY_SUBMITTED_TILL_DATE,
                                  strMBEQty);
                          DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_ABS_MBE_OID,
                        		  strMBEOid);
                      }
                  }
              }
    	 
    	 
    	   }catch(Exception e) {
    		 
    		e.printStackTrace(); 
    	 }
    	 return 0;
     }
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getFiles(Context context,String[] args) throws Exception{
      
    	 MapList mlFiles=new MapList();
    	try {
    	 
    
    		 HashMap programMap         = (HashMap) JPO.unpackArgs(args);
             String  masterObjectId     = (String) programMap.get("objectId");
             DomainObject masterObject  = DomainObject.newInstance(context, masterObjectId);
            // DocumentUtil.checkout(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)
             FileList  _fileList =  masterObject.getFiles(context);
             FileItr  fileItr = new FileItr(_fileList);
             Iterator<File>  itr  =   fileItr.iterator();
             String _fileId = "";
             String _fileName ="";
             String _fileFormat ="";
            
              while(itr.hasNext()) {
            	  Map m =new HashMap();
                  File file = itr.next();
             	 _fileId = file.getId();
              	 _fileName = file.getName();
            	 _fileFormat = file.getFormat();
            	 
                 m.put(DomainConstants.SELECT_ID, _fileId);
                 m.put("FileName", _fileName);
                 m.put("objReadAccess", "TRUE");
                 m.put("FileFormat", _fileFormat);
             	 
             	 System.out.println(_fileName);
             	 System.out.println(_fileId);
             	 System.out.println(_fileFormat);
             	mlFiles.add(m) ;
              }
    	 
    	 
    	 
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    	}
    	return mlFiles;
     }
     
     public Vector getFileColumn(Context context,String[] args) throws Exception
     {
    	Vector colVector = new Vector();
	     try { 
	     	 
	    	 HashMap programMap         = (HashMap) JPO.unpackArgs(args);
	    	 HashMap columnMap = (HashMap) programMap.get("columnMap");
	    	 Map settings = (Map) columnMap.get("settings");
	    	 String sColumnKey = (String) settings.get("ColumnName");
             String  masterObjectId     = (String) programMap.get("objectId");
	    	 MapList objectList  = (MapList) programMap.get("objectList");
	    	 Iterator<Map> itr = objectList.iterator();
	    	 while(itr.hasNext()) {
	    		 Map m = itr.next();
	    		 colVector.add((String)m.get(sColumnKey));
	    	 }
	    	 
	    }catch(Exception e) {
	    	e.printStackTrace();
	    } 
	  return colVector;
     }
     
     public Vector getDownloadLink(Context context,String[] args) throws Exception
     {
    	Vector colVector = new Vector();
	     try { 
	     	 
	    	 String downloadURL="../wms/wmsDownloadFileProcess.jsp?action=download";
	    	 String strDownLoadTip="Download";
	    	 String _fileId = "";
             String _fileName ="";
             String _fileFormat ="";
	    	 HashMap programMap         = (HashMap) JPO.unpackArgs(args);
	    	  HashMap paramList = (HashMap) programMap.get("paramList");
	    	// Map settings = (Map) columnMap.get("settings");
	    	// String sColumnKey = (String) settings.get("ColumnName");
             String  strMBEId     = (String) paramList.get("objectId");
	    	 MapList objectList  = (MapList) programMap.get("objectList");
	    	
	    	 Iterator<Map> itr = objectList.iterator();
	    	 while(itr.hasNext()) {
	    		 Map m = itr.next();
	    		 _fileName = (String) m.get("FileName");
	    		 _fileFormat = (String) m.get("FileFormat");
	    		 _fileId = (String) m.get("id");
	    		  downloadURL=downloadURL+"&amp;fileName="+_fileName+"&amp;fileFormat="+_fileFormat+"&amp;fileId="+_fileId+"&amp;objectId="+strMBEId;
	    		  StringBuilder sb=new StringBuilder();
	    		 sb.append("<a href=\"" + downloadURL + "\"  target=\"hiddenFrame\"   >");
	 	    	 sb.append("<img border=\"0\" src=\"../common/images/iconActionDownload.gif\" alt=\""
	 			+ strDownLoadTip + "\" title=\"" + strDownLoadTip + "\"></img></a>&#160;");
	    		
	    		 colVector.add(sb.toString());
	    	 }
	    	 
	    }catch(Exception e) {
	    	e.printStackTrace();
	    } 
	  return colVector;
     }
     
     
     /**
      * Method will Add MBE Quantity from Type to AtySubmitedTillDate in
      * MBEAvtivities Relationip
      * 
      * @param context the eMatrix <code>Context</code> object
      * @throws Exception
      *             if the operation fails
      * @author CHiPS
      * @since 418
      */
     public void updateQtySubmittedTillDate(Context context, String[] args) throws Exception {
         boolean isContextPushed = false;
         try {
             
             String strToObjItemID = args[0];
             String strRELID = args[1];
             DomainObject domObjToObject = DomainObject.newInstance(context, strToObjItemID);
             String strMBEQuantity = domObjToObject.getAttributeValue(context,ATTRIBUTE_WMS_MBE_QUANTITY);
             DomainRelationship.setAttributeValue(context, strRELID, ATTRIBUTE_WMS_QTY_SUBMITTED_TILL_DATE, strMBEQuantity);
         } catch (Exception exception) {
                   
             exception.printStackTrace();
             throw exception;
         } 
     }

     /** 
 	 * Method will update MBE Quantity
 	 * 
 	 * @param context the eMatrix <code>Context</code> object
 	 * @param args with program arguments
 	 *             args[0]- MBE OID
 	 * @throws Exception if the operation fails
 	 * @author WMS
 	 * @since 418
 	 */
     public void updateMBEQuantity(Context context, String[] args) throws Exception {
         boolean isContextPushed = false;
         try {
             String strMBEOID = args[0];
             if (UIUtil.isNotNullAndNotEmpty(strMBEOID)) {
                 DomainObject domObjMBE = DomainObject.newInstance(context, strMBEOID);
                 DomainObject domObjMBItem = DomainObject.newInstance(context);
                 MapList mapListConnectedActivities = getMBEActivities(context, domObjMBE);    
 				//MapList mapListPaidPaymentScheduleItems = ${CLASS:CHiPSPaymentScheduleJPO}.getPaidPaymentScheduleItems(context, domObjMBE);
                 //mapListConnectedActivities.addAll(mapListPaidPaymentScheduleItems);                //Get whether the MBE is Running or not and also if its first revision or not
                 StringList slObjSelects = new StringList(3);
                // slObjSelects.add("attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"]");
                 slObjSelects.add("previous");
                 slObjSelects.add("previous.id");
                 Map mObjInfo = domObjMBE.getInfo(context, slObjSelects);
               //  String strPrevious = (String)mObjInfo.get("previous");
                 String strMBType = DomainConstants.EMPTY_STRING; //(String)mObjInfo.get("attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"]");
               /*  if(UIUtil.isNullOrEmpty(strMBType))
                 {
                     strMBType = DomainConstants.EMPTY_STRING;
                 }
                 //if MBE is running MBE and not first revision then call another method
                 if ("Running".equals(strMBType) && strPrevious.length() > 0) {
                     String strPreviousId = (String)mObjInfo.get("previous.id");
                     updateRunningMBEQuantity(context, strPreviousId, mapListConnectedActivities);
                     //domObjMBItem.setAttributeValues(context, attrMap);
                 } else {*/
                     Map attrMap = new HashMap();
                     float itemQty = 0;                
                     Iterator iterator = mapListConnectedActivities.iterator();
                     while (iterator.hasNext()) {
                         Map<String,String> mapActivity = (Map<String,String>) iterator.next();
                         String strMBItemId = mapActivity.get(DomainObject.SELECT_ID);
                         String strObjType = mapActivity.get(DomainObject.SELECT_TYPE);
                         domObjMBItem.setId(strMBItemId);
                         String strItemMBEQunatity = mapActivity.get("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]");
                         String strTotalQunatity = mapActivity.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
                         itemQty = 0;
                        if (UIUtil.isNullOrEmpty(strItemMBEQunatity)) {
                             strItemMBEQunatity = "0";
                         }
                         if (UIUtil.isNullOrEmpty(strTotalQunatity)) {
                             strTotalQunatity = "0";
                         }
                         itemQty = itemQty + Float.parseFloat(strItemMBEQunatity);
                         itemQty = itemQty + Float.parseFloat(strTotalQunatity);
                         attrMap.put(ATTRIBUTE_WMS_MBE_QUANTITY, String.valueOf(itemQty));
                         if(TYPE_WMS_MEASUREMENT_TASK.equals(strObjType))
                         {
                           //  attrMap.put(CHiPSDomainConstant.ATTRIBUTE_PREVIOUS_MBE_QUANTITY,strItemMBEQunatity);
                         }
                         ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                         isContextPushed = true;
                         domObjMBItem.setAttributeValues(context,attrMap);                        
                     }
                 }
            // }
         }
         catch (Exception exception) 
         {
             exception.printStackTrace();
         }
         finally
         {
             if (isContextPushed)
             {
                 ContextUtil.popContext(context);
             }
         }
    
     }
    
     /** Trigger method on MBE Create state promote which checks for Contract Value 
      * 
      * 
      * 
      * @param context
      * @param args
      * @return
      */
     
     
     public  int triggerContractValueLimitCheck(Context context ,String[] args) {
        	 try {
                 String strMBEOid = args[0];
        		 String strMBEName=args[1];
        		 SelectList selListBusSelects     = new SelectList(7);
                 selListBusSelects.add(DomainConstants.SELECT_ID);
                 selListBusSelects.add(DomainConstants.SELECT_TYPE);
                 selListBusSelects.add(DomainConstants.SELECT_NAME);
                 
                 // selListBusSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
                 selListBusSelects.add("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]");
                 selListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_COST+"]");
                 selListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
                 selListBusSelects.add("attribute["+ATTRIBUTE_WMS_UNIT_OF_MEASURE+"]");
                 selListBusSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                // selListBusSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]"); 
                 SelectList selListRelSelects     = new SelectList(2);
                 selListRelSelects.add(DomainRelationship.SELECT_ID);
                 selListRelSelects.add("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]");
                 selListRelSelects.add("attribute["+ATTRIBUTE_WMS_QTY_SUBMITTED_TILL_DATE+"]");
                 selListRelSelects.add("from.id");
                 selListRelSelects.add("from.current");
        		 
        		 DomainObject domMBE= DomainObject.newInstance(context, strMBEOid);
        	 	// MapList mlCurrentBOQs  = getMBEActivities(context,domMBE);
        		 StringList slSelects = new StringList();
        		 slSelects.add("to["+RELATIONSHIP_WMS_WORK_ORDER_MBE+"].from.id");
        		 slSelects.add("to["+RELATIONSHIP_WMS_WORK_ORDER_MBE+"].from.attribute["+ATTRIBUTE_WMS_VALUE_OF_CONTRACT+"]");
        		 Map mWOInfo = domMBE.getInfo(context, slSelects);
        		 
        	 	 String strWOId = (String)mWOInfo.get("to["+RELATIONSHIP_WMS_WORK_ORDER_MBE+"].from.id");
        	 	 double dWOContractValue = Double.parseDouble((String)mWOInfo.get("to["+RELATIONSHIP_WMS_WORK_ORDER_MBE+"].from.attribute["+ATTRIBUTE_WMS_VALUE_OF_CONTRACT+"]"));
        		 DomainObject domWO=DomainObject.newInstance(context, strWOId);
        		 Pattern typePattern = new Pattern(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);
        		 typePattern.addPattern(TYPE_WMS_MEASUREMENT_TASK);
        		 
        		 Pattern relPattern = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_MBE);
        		 relPattern.addPattern(RELATIONSHIP_WMS_MBE_ACTIVITIES);
        		 MapList mapListItems = domWO.getRelatedObjects(context,
        				    relPattern.getPattern(),                         // relationship pattern
                            typePattern.getPattern(),                                    // object pattern
							false,                                                        // to direction
							true,                                                       // from direction
							(short)2,                                                      // recursion level
							selListBusSelects,                                                 // object selects
							selListRelSelects,                                                         // relationship selects
							DomainConstants.EMPTY_STRING,                                // object where clause
							DomainConstants.EMPTY_STRING,                                // relationship where clause
							(short)0,                                                      // No expand limit
							DomainConstants.EMPTY_STRING,                                // postRelPattern
							TYPE_WMS_MEASUREMENT_TASK,                                                // postTypePattern
							null);          
        		
        		 Iterator<Map> itr = mapListItems.iterator();
        		 String strMTOid = DomainConstants.EMPTY_STRING;
        		 double dQtySubmittedtd= 0.0;
        		 double dRunningQty= 0.0;
        		 double dRate= 0.0;
        		 Map<String,Map> mMTDetails= new HashMap<String,Map>();
        		 Map<String,Map> mCurrentMT = new HashMap<String,Map>();
        		 double dTotalQtySubmittedtd= 0.0;
        		 String strTitle=DomainConstants.EMPTY_STRING;
        		 String strMBEId=DomainConstants.EMPTY_STRING;
        		 String strMBEState=DomainConstants.EMPTY_STRING;
        		 Map mMBEDetails = new HashMap();
        		 String strUOM = DomainConstants.EMPTY_STRING;
        		 while(itr.hasNext()) {
        			 dQtySubmittedtd=0;
        			 dRunningQty=0;
        			 dRate=0;
        			 Map m = itr.next();
        			 Map mDetails = new HashMap();
        			 Map mMBETaskDetails = new HashMap();
        			 strMTOid= (String)m.get(DomainConstants.SELECT_ID);
        			 strMBEId= (String)m.get("from.id");
        			 strUOM=(String)m.get("attribute["+ATTRIBUTE_WMS_UNIT_OF_MEASURE+"]");
        			 strMBEState= (String)m.get("from.current");
        			 dQtySubmittedtd=Double.parseDouble((String)m.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]"));
        			 
        			 if(strMBEOid.equalsIgnoreCase(strMBEId)) {
        				 mMBETaskDetails.put("MBESTATE", strMBEState);
        				 mMBETaskDetails.put("MBEQTY", (String) m.get("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]"));
        				 mMBEDetails.put(strMTOid, mMBETaskDetails);
        				 mCurrentMT.put(strMBEOid, mMBEDetails);
        			 }
        	  			 dRate= Double.parseDouble((String)m.get("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]"));
	        			 strTitle=(String)m.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
	        			 if(mMTDetails.containsKey(strMTOid)) {
	        				 mDetails = mMTDetails.get(strMTOid);
	        			     dRunningQty= Double.parseDouble((String)mDetails.get("TotalRunning"));
	        			 }
	        			 if(!strMBEState.equalsIgnoreCase("Submitted")) 
       				 		  dRunningQty=dRunningQty+Double.parseDouble( (String) m.get("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]"));
	        		 
        			 
        		  	   mDetails.put(("TotalSubmitted"), String.valueOf(dQtySubmittedtd));
        		       mDetails.put(("TotalRunning"), String.valueOf(dRunningQty));
        			   mDetails.put(("Rate"), String.valueOf(dRate));
        			   mDetails.put(("name"),strTitle);
        			   mDetails.put(("UoM"),strUOM);
        			   mMTDetails.put(strMTOid, mDetails);
        			 
        		 }
        		
        		Set<String> set =  mMTDetails.keySet();
        		Iterator<String> itrSet = set.iterator();
        		String strKey=DomainConstants.EMPTY_STRING;
        		Map m =new HashMap();
        		double dTotalContractValue=0;
        		 StringBuilder sb=new StringBuilder();
            	 sb.append("=====================================================================================================================\n");
            	 sb.append("=                                                                                                                    =\n");
            	 sb.append("=   Title   UoM      Rate   Since Previous Bill    Submitted Till Date   Total Runnning Cost  Total Submitted Cost  =\n");
            	 sb.append("=                                                                                                                    =\n");
            	 sb.append("======================================================================================================================\n");
            	double dMBEQty=0;
            	Map mMTData = (Map)mCurrentMT.get(strMBEOid);
        		while(itrSet.hasNext()) {
        			dMBEQty=0;
        			strKey=itrSet.next();
        			m = mMTDetails.get(strKey);
        			dQtySubmittedtd = Double.parseDouble((String) m.get("TotalSubmitted"));
        			dRunningQty= Double.parseDouble((String) m.get("TotalRunning"));
        			dRate= Double.parseDouble((String) m.get("Rate"));
        			strUOM=(String)m.get("UoM");
        			strTitle=(String) m.get("name");
       			    dTotalContractValue=dTotalContractValue+((dQtySubmittedtd+dRunningQty)*dRate);
        	 		/*if(dRunningQty==0) {
        	 			dRunningQty=dQtySubmittedtd;
        	 			dQtySubmittedtd=0;
        	 		}*/
        	 		 if(mMTData.containsKey(strKey)) {
	        	 	 		 Map mDataDetails  =(Map) mMTData.get(strKey);
	        	 	 		 dMBEQty =Double.parseDouble((String) mDataDetails.get("MBEQTY")); 
	        	 	 		 String strState = (String) mDataDetails.get("MBESTATE");
	        	 	 		 if(strState.equalsIgnoreCase("Review") && dQtySubmittedtd==0)  {dQtySubmittedtd=dMBEQty;dMBEQty=0 ;}// if this is 1st MBE and submitted running should e moved as 
	        	 	 		 sb.append("= ").append(strTitle).append("     ")
	        	 	 		.append(strUOM).append("       ")
	        	 	 	 	.append(dRate).append("        ")
	        	 	 		.append(dMBEQty).append("             ")
	        	 	 		.append(dQtySubmittedtd).append("               ")
	        	 	 		.append(dMBEQty*dRate).append("                  ")
	            	 		.append(dQtySubmittedtd*dRate).append("                               =\n");
        			 }
         	 		
        		 }
        		DecimalFormat df2 = new DecimalFormat(".##");
        		
                if(dTotalContractValue>dWOContractValue)
        		{
        		 String strAlert = 	MessageUtil.getMessage(context, null, "WMS.alert.ContractValueBeyondDefinedContractValue",
        					new String[] {df2.format(dTotalContractValue),String.valueOf(dWOContractValue)}, null, context.getLocale(),
        					"wmsStringResource");	
        		 emxContextUtil_mxJPO.mqlNotice(context,strAlert);
        		  return 1;
        		}
        		
          		String strFilePath  = context.createWorkspace();
          	    String strFileName= strMBEName+".txt";
        		java.io.File file = new java.io.File(strFilePath, strFileName);
        	    FileUtils.writeStringToFile(file, sb.toString());
        	    domMBE.checkinFile(context, true, true, "",  DomainConstants.FORMAT_GENERIC , strFileName, strFilePath);
        	    file.delete();
        	  
        	 }catch(Exception e) {
        		 e.printStackTrace();
        	 }
        
         
         return 0;
         
    
        }
     
     
     /**
      * Method to get the disconnected Tasks under the MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return String containing the message
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public String removeDeductionss (Context context, String[] args) throws Exception 
     {
         try
         {
             
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
                       
             if(emxTableRowId !=null && emxTableRowId.length>0)
             {
                 
                 ArrayList<String> arrayListRelOIDs = new ArrayList<String>();
                 for(int i=0;i<emxTableRowId.length;i++)
                 {
                   StringTokenizer st = new StringTokenizer(emxTableRowId[i], "|");
                   String sObjId = st.nextToken();
                   arrayListRelOIDs.add(sObjId);          
                  
                         while (st.hasMoreTokens()) 
                         {
                            sObjId = st.nextToken();
                         }      
                 }        
                   
 				 WMSUtil_mxJPO.disconnect(context, arrayListRelOIDs);
                 return "Selected objects sucessfully removed";

             }                    
             return "Selected objects sucessfully removed";
         }catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     
     /**
      * Method to get the connected MBE under the Item(Submitted)
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args Packed program and request maps from the command
      * @return mapListMBEs MapList containing the MBE IDs
      * @throws Exception if the operation fails
      * @author WMS
      * @since 418
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getActivitySubmittedMBEs (Context context, String[] args) throws Exception 
     {
         try
         {
             MapList mapListMBEs = new MapList();
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             String strObjectId = (String) programMap.get("objectId");
             String strMBEOID             = (String) programMap.get("mbeOID");
             if(UIUtil.isNotNullAndNotEmpty(strObjectId))
             {
                 DomainObject domObjMBE= DomainObject.newInstance(context, strObjectId);
                 MapList mapListMBEsTemp = getActivityMBEs(context, domObjMBE);
                 //TODO convert to Constant entries
 				mapListMBEs = WMSUtil_mxJPO.getSubMapList(mapListMBEsTemp, DomainConstants.SELECT_CURRENT, "Submitted");
                 insertKeyValue(mapListMBEs, DomainConstants.SELECT_LEVEL, "1");
             }
             return mapListMBEs;
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
     }
     /**
      * Function to get the Tasks connected to the selected MBE
      *
      * @param context the eMatrix <code>Context</code> object
      * @param domObjItem DomainObject instance of selected Item
      * @return mapListTasks MapList containing the MBEs connected to Work Order with ID
      * @throws FrameworkException if the operation fails
      * @author WMS
      * @since 418
      */
     private MapList getActivityMBEs(Context context, DomainObject domObjItem)
             throws FrameworkException {
         try
         {
             SelectList selListBusSelects     = new SelectList(1);
             selListBusSelects.add(DomainConstants.SELECT_ID);
             selListBusSelects.add(DomainConstants.SELECT_CURRENT);
             SelectList selListRelSelects     = new SelectList(1);
             selListRelSelects.add(DomainRelationship.SELECT_ID);

             MapList mapListMBEs = domObjItem.getRelatedObjects(context, // matrix context
            		 RELATIONSHIP_WMS_MBE_ACTIVITIES, // relationship pattern
            		 TYPE_WMS_MEASUREMENT_BOOK_ENTRY, // type pattern
                     selListBusSelects, // object selects
                     selListRelSelects, // relationship selects
                     true, // to direction
                     false, // from direction
                     (short) 1, // recursion level
                     DomainConstants.EMPTY_STRING, // object where clause
                     DomainConstants.EMPTY_STRING, // relationship where clause
                     0);
             return mapListMBEs;
         }
         catch(FrameworkException frameworkException)
         {
             throw frameworkException;
         }
     }
	/** 
     * Method will connect AbstractMBE/MBE and RouteTemplate, When we have only one AMBE/MBE Route template on work order
     * 
     * @param context the eMatrix <code>Context</code> object    
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    public void connectAMBERouteTemplate (Context context, String args[]) throws Exception {
		Map mObjectDetail = null;
		MapList mlConnectedRouteTemplate = null;
		String strTypeSelected = null;
		String strRelWhereClause = null;
		String strRTId = null;
		
		try{
			
			String strWOId = args[0];
			String strAMBEId = args[1];
			String strRange = args[2];
			String strRelationshipName = args[3];
			
			StringList slBusSelects = new StringList(1);
			slBusSelects.add(DomainConstants.SELECT_ID);
			 
			strRelWhereClause = "attribute["+ATTRIBUTE_WMS_APPROVAL_TEMPLATE_PURPOSE+"] =='"+strRange+"'";
			if(UIUtil.isNotNullAndNotEmpty(strWOId)&& UIUtil.isNotNullAndNotEmpty(strAMBEId)) {
				
				DomainObject domWO = DomainObject.newInstance(context,strWOId);
				mlConnectedRouteTemplate = domWO.getRelatedObjects(context, // matrix context
																	RELATIONSHIP_WMS_WORK_ORDER_APPROVAL_TEMPLATE, // relationship pattern
																	DomainConstants.TYPE_ROUTE_TEMPLATE, // type pattern
																	slBusSelects, // object selects
																	null, // relationship selects
																	false, // to direction
																	true, // from direction
																	(short) 0, // recursion level
																	DomainConstants.EMPTY_STRING, // object where clause
																	strRelWhereClause, // relationship where clause
																	0);
				if(mlConnectedRouteTemplate.size() == 1){
					mObjectDetail = (Map)mlConnectedRouteTemplate.get(0);
					strRTId = (String)mObjectDetail.get(DomainConstants.SELECT_ID);
					
					DomainRelationship.connect(context, strAMBEId, PropertyUtil.getSchemaProperty(strRelationshipName), strRTId, true);
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;            
		}
	}
	//Code added for B3 - Start
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
    	
	
	    /**
  	 * Method to get the connected AbstractMBE to show Abstract Bill number/Date 
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed program and request maps from the command
       * @return mapListConnectedTasks MapList containing the Task IDs
       * @throws Exception if the operation fails
       * @author CHiPS
       * @since 418
       */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAbstractMBE(Context context,String[] args) throws Exception
	{
		MapList mlConnectedAMBE = new MapList();
		MapList mlFinalResult = new MapList();
		StringList slUniqueId = new StringList();
		Map mapObject = null;
		String strMBEID = null;
		String strMBEIDs = null;
		String strMBEIDFinal = null;
		DomainObject doMBEID = null;
		
		StringBuffer sbTypeSelect = new StringBuffer(10);
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_TASK);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY);

		StringBuffer sbRelSelect = new StringBuffer(10);
		sbRelSelect.append(RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS);
		sbRelSelect.append(",");
		sbRelSelect.append(RELATIONSHIP_WMS_MBE_ACTIVITIES);
		
		StringList slBusSelect = new StringList(1);
		slBusSelect.addElement(DomainConstants.SELECT_ID);
		
		matrix.util.Pattern filterTypePattern = new matrix.util.Pattern(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY);
		matrix.util.Pattern filterRelPattern  = new matrix.util.Pattern(RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS);
		
		try
		{
			HashMap programMap   = (HashMap) JPO.unpackArgs(args);
			strMBEID = (String) programMap.get("objectId");
			doMBEID = DomainObject.newInstance(context,strMBEID);
			
			mlConnectedAMBE = doMBEID.getRelatedObjects(context,						//context
														sbRelSelect.toString(),			//relationshipPattern
														sbTypeSelect.toString(),		//typePattern 
														slBusSelect,					//objectSelects 
														null,							//relationshipSelects 
														true,							//getTo 
														true,							//getFrom 
														(short)0,						//recurseToLevel 
														null,							//objectWhere 
														null,							//relationshipWhere 
														filterTypePattern,				//TYPE_WMS_WORK_ORDER includeType
														filterRelPattern,				//RELATIONSHIP_BILL_OF_QUANTITY includeRelationship 
														null);							//includeMap
			
			if((null!=mlConnectedAMBE)&&(mlConnectedAMBE.size()>0)){
				Iterator itr = mlConnectedAMBE.iterator();
				while(itr.hasNext()) {
					mapObject = (Map)itr.next();
					strMBEIDs = (String)mapObject.get(DomainConstants.SELECT_ID);
					if(!slUniqueId.contains(strMBEIDs)){
						slUniqueId.add(strMBEIDs);
						Map mFinalResult = new HashMap();
						mFinalResult.put("id", strMBEIDs);
						mlFinalResult.add(mFinalResult);
					}
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return mlFinalResult;
	}
		//Code added for B3 - end
}