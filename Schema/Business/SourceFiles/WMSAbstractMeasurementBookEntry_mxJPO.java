/** Name of the JPO    : CHiPSAbstractMeasurementBookEntryBase
 ** Developed by    : Matrixone 
 ** Client            : CHiPS
 ** Description        : The purpose of this JPO is to create a Abstract Measurement Book Entry and its functionalities
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.time.DateUtils;

import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import matrix.db.BusinessInterface;
import matrix.db.BusinessInterfaceList;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Vault;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;
/**
 * The purpose of this JPO is to create a Abstract Measurement Book Entry and its functionalities
 * @author CHiPS
 * @version R418 - Copyright (c) 1993-2016 Dassault Systems.
 */
public class WMSAbstractMeasurementBookEntry_mxJPO extends WMSConstants_mxJPO
{
	public WMSAbstractMeasurementBookEntry_mxJPO(Context context, String[] args) {
		super(context, args);
		// TODO Auto-generated constructor stub
	}

	

    /**
	 * Method to get the connected Tasks under the MBEs which are connected to Abtract MBE
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getUniqueMBEActivities (Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mlAllConnectedTasks = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjAbstractMBE= DomainObject.newInstance(context, strObjectId);
				mlAllConnectedTasks = getUniqueMBEActivities(context, domObjAbstractMBE);
				WMSMeasurementBookEntry_mxJPO.insertKeyValue(mlAllConnectedTasks, "disableSelection", "false");
			}
			return mlAllConnectedTasks;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/** 
	 * Method will get all Unique Tasks which are connected to Abstract MBE connected MBEs
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbstractMBE DomainObject instance of Abstract MBE
	 * @return mapListUniqueTasks MapList containing the Activities connected to Abstract MBE through MBE
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getUniqueMBEActivities(Context context,
			DomainObject domObjAbstractMBE) throws FrameworkException {
		try
		{
			StringList strListBusSelects     = getUniqueMBEActivitiesBusSelects();
			StringList strListRelSelects     = getUniqueMBEActivitiesRelSelects();
			Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
			//patternType.addPattern(TYPE_CHIPS_PAYMENT_ITEM);

			MapList mapListItems = domObjAbstractMBE.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS, // relationship pattern
					patternType.getPattern(), // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			return mapListItems;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	
	/** 
	 * Method will get StringList
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjMBE DomainObject instance of selected Work Order 
	 * @return mapListTasks MapList containing the Activities connected to Abstract MBE through MBE
	 * @author CHiPS
	 * @since 418
	 */
	private SelectList getUniqueMBEActivitiesBusSelects() {
		SelectList strListBusSelects     = new SelectList(4);
		strListBusSelects.add(DomainConstants.SELECT_TYPE);
		strListBusSelects.add(DomainConstants.SELECT_ID);
		strListBusSelects.add("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"].value");
		strListBusSelects.add("relationship["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"].from.name");
		//strListBusSelects.add("relationship["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"].attribute["+ATTRIBUTE_INCLUDE_IN_BILL+"].value");
		strListBusSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
		strListBusSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"].value");
		 strListBusSelects.add("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].from.id");
		//strListBusSelects.add("attribute["+ATTRIBUTE_CHIPS_PERCENTAGE_OF_WEIGHTAGE+"]");
		//strListBusSelects.add("from[CHiPSCostScheduleToMeasurementTask].to.id");
		//strListBusSelects.add("to[CHiPSTaskBOQ].from.id");

		return strListBusSelects;
	}
	
	private StringList getUniqueMBEActivitiesRelSelects() {
		StringList strListRelSelects     = new StringList(8);
		strListRelSelects.add(DomainRelationship.SELECT_ID);
		//strListRelSelects.add("attribute["+ATTRIBUTE_ABSMBE_ITEM_RATE+"].value");
		//strListRelSelects.add("attribute["+ATTRIBUTE_ITEM_TOTAL_DEDUCTION+"].value");
		//strListRelSelects.add("attribute["+ATTRIBUTE_ITEM_WITHHELD_CAUSE+"].value");
		//strListRelSelects.add("attribute["+ATTRIBUTE_ABSMBE_ITEM_TOTAL_COST+"].value");
		strListRelSelects.add("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
		strListRelSelects.add("attribute["+ATTRIBUTE_WMS_PAYABLE_QUANTITY+"].value");
		strListRelSelects.add("attribute["+ATTRIBUTE_WMS_WITHHELD_CAUSE+"].value");
		strListRelSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"].value");

		return strListRelSelects;
	}
	/** 
	 * It gives quanity of item that can be added to Abs MBE
	 * 
	 * @param context the eMatrix <code>Context</code> object 
	 * @param args
	 * @return Response values list
	 * @throws Exception if operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public Vector<String> getAbsMBEAddParticularsItemQuantity(Context context, String[] args)
			throws Exception
	{
		try{
			Map<String,Object> programMap =   (Map<String,Object>)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			int intSize = objectList.size();
			Vector<String> vecResponse = new Vector<String>(intSize);
			Iterator<Map<String,String>> iterator  = objectList.iterator();
			Map<String,String> mapData ;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strReleasedQuantity = mapData.get("FinalQuanity");
				vecResponse.add(strReleasedQuantity);
			}
			return vecResponse;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * This method is to get Measurement Task For Select AbstractMBE
	 * @param context - the eMatrix <code>Context</code> object
	 * @param String array args containing the programMap
	 * @return - MapList
	 * @throws Exception when problems occurred
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMeasurementTask(Context context, String[] args) throws Exception
	{
		MapList mlreturnList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strAbstractMBEId    = (String) programMap.get("mbeOID");
			if(UIUtil.isNotNullAndNotEmpty(strAbstractMBEId))
			{
				String sCHiPSMBEItemType = (String) programMap.get("MBEItemType");
				if(UIUtil.isNullOrEmpty(sCHiPSMBEItemType))
				{
					sCHiPSMBEItemType = "Normal";
				}
				DomainObject domObjAbsMBE = new DomainObject(strAbstractMBEId);
				StringList strListConnectedTasks = getAbsMBEConnectedActivities(context, domObjAbsMBE);
				String strWhere = "("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MEASUREMENT_BOOK+")||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_SEGMENT+")||(("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MEASUREMENT_TASK+")&&(attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"].value<attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"].value))";
				MapList mapListItems =  getAbsMBEParticularsMBEs(context,domObjAbsMBE, strWhere); 
				Map<String,String> mapData;
				Iterator<Map<String,String>> iterator = mapListItems.iterator();
				while(iterator.hasNext())
				{
					mapData = iterator.next();
					String strOID = mapData.get(DomainConstants.SELECT_ID);
					if(!strListConnectedTasks.contains(strOID))// && sMBEItemType.equalsIgnoreCase(sCHiPSMBEItemType))
					{
						String strItemPaidQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
						String strItemSubmittedQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
						double doubleItemPaidQuantity =WMSUtil_mxJPO.convertToDouble(strItemPaidQuantity);
						double doubleItemSubmittedQuantity = WMSUtil_mxJPO.convertToDouble(strItemSubmittedQuantity);
						mapData.put("FinalQuanity", String.valueOf((doubleItemSubmittedQuantity-doubleItemPaidQuantity)));
						mlreturnList.add(mapData);
					}

				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return mlreturnList;
	}
	/** 
	 * Method will get all Unique Tasks which are connected to Abstract MBE connected MBEs
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbstractMBE DomainObject instance of Abstract MBE
	 * @return mapListUniqueTasks MapList containing the Activities connected to Abstract MBE through MBE
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private StringList getAbsMBEConnectedActivities(Context context,
			DomainObject domObjAbstractMBE) throws FrameworkException {
		try
		{
			SelectList strListBusSelects     = new SelectList(1);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			StringList strListRelSelects     = new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
			MapList mapListItems = domObjAbstractMBE.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS, // relationship pattern
					patternType.getPattern(), // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			StringList strListAbsMBEConnectedTasks = WMSUtil_mxJPO.convertToStringList(mapListItems, DomainConstants.SELECT_ID);
			return strListAbsMBEConnectedTasks;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/** 
	 * Method will roll up the the respective attribute value on the Items connected to the item
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args with program arguments
	 *             args[0]- Activity OID
	 *          args[1]- Attribute Old value
	 *            args[2]- Attribute New value
	 *             args[3]- Rel OID between Abstract MBE and Activity
	 *          args[4]- Abstract MBE OID
	 *            args[5]- Attribute Name
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getAbsMBEParticularsMBEs(Context context,
			DomainObject domObjAbsMBE, String strWhere)
					throws FrameworkException {
		try
		{
			String strWOOID = domObjAbsMBE.getInfo(context, "relationship["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
			MapList mapListItems = new MapList();
			if(UIUtil.isNotNullAndNotEmpty(strWOOID))
			{
				DomainObject domObjWO = DomainObject.newInstance(context, strWOOID);
				domObjWO.setId(strWOOID);
				StringList strListBusSelects=new StringList(4);
				strListBusSelects.add(DomainConstants.SELECT_ID);
				strListBusSelects.add(DomainConstants.SELECT_NAME);
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
				StringList strListRelSelects=new StringList(3);
				strListRelSelects.add(DomainRelationship.SELECT_ID);
				Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_BOOK);
				patternType.addPattern(TYPE_WMS_SEGMENT);
				patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
				mapListItems = domObjWO.getRelatedObjects(context,
						RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
						patternType.getPattern(),                                    // object pattern
						true,                                                        // to direction
						true,                                                       // from direction
						(short)0,                                                      // recursion level
						strListBusSelects,                                                 // object selects
						strListRelSelects,                                                         // relationship selects
						strWhere,                                // object where clause
						DomainConstants.EMPTY_STRING,                                // relationship where clause
						(short)0,                                                      // No expand limit
						DomainConstants.EMPTY_STRING,                                // postRelPattern
						TYPE_WMS_MEASUREMENT_TASK,                                                // postTypePattern
						null);
				WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListItems, DomainConstants.SELECT_LEVEL,"1");

			}
			return mapListItems;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/**
	 * Method add the Item to AbstractMBE
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map<String,String> addAbstractMBEItem(Context context, String[] args) throws Exception 
	{
		try
		{
			Map<String,String> mapResult = new HashMap<String,String>();
			mapResult.put(DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING);
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
			String strAbstractMBEOID = (String) programMap.get("objectId");
			int intSize = emxTableRowId.length;
			if(emxTableRowId !=null && intSize>0&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID))
			{
				DomainObject domObjAbstractMBE  = DomainObject.newInstance(context, strAbstractMBEOID);
				String strSequenceValue = domObjAbstractMBE.getAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER);
				String strAbsTotalValue = domObjAbstractMBE.getAttributeValue(context, "WMSTotalCost");
				double doubleAbsMBETotalAmount = WMSUtil_mxJPO.convertToDouble(strAbsTotalValue);
				ArrayList<String> arrayListItemOIDs = new ArrayList<String>(intSize);
				ArrayList<String> arrayListMBEOIDs = new ArrayList<String>(intSize);
				StringList strListItemMBERelOIDs = new StringList();
				Map<String,String> mapQuanity = new HashMap<String,String>(intSize);
				Map<String,String> mapData ;
				MapList mapListItemInfo = getSelectedItemInfo(context, emxTableRowId);
				HashMap<String,String> hashMapAttributeMap = new HashMap<String,String>(2);
			//	hashMapAttributeMap.put(ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER,strSequenceValue);
				hashMapAttributeMap.put(ATTRIBUTE_WMS_ABS_MBE_OID,strAbstractMBEOID);
				for (int i = 0; i < intSize; i++)
				{
					String[] emxTableRowIdData = emxTableRowId[i].split("[|]");
					String strItemOID = emxTableRowIdData[1];
					if(UIUtil.isNotNullAndNotEmpty(strItemOID))
					{
						DomainObject domObjItem  = DomainObject.newInstance(context, strItemOID);
						Map<String,String> mapItemData = WMSUtil_mxJPO.getMap(mapListItemInfo, DomainConstants.SELECT_ID, strItemOID);
						String strItemPaidQuantity = mapItemData.get("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
						String strItemSubmittedQuantity = mapItemData.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
						double doubleItemPaidQuantity =WMSUtil_mxJPO.convertToDouble(strItemPaidQuantity);
						double doubleItemSubmittedQuantity = WMSUtil_mxJPO.convertToDouble(strItemSubmittedQuantity);
						double doubleCurrentBillQuantity = doubleItemSubmittedQuantity-doubleItemPaidQuantity;
						StringList strListCurrentItemMBERelOIDs = getItemConnectedMBERelOIDs(context,domObjItem);
						strListItemMBERelOIDs.addAll(strListCurrentItemMBERelOIDs);
						mapQuanity.put(strItemOID,String.valueOf(doubleCurrentBillQuantity));
						mapQuanity.put(strItemOID+"_Rate",mapItemData.get("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]"));
						double doubleItemCost = connectAbstractMBEItems(context, domObjAbstractMBE, strItemOID, mapQuanity);
						doubleAbsMBETotalAmount+=doubleItemCost;
					}
				}
				setAbsMBEOIDOnItemsMBE(context, hashMapAttributeMap,strListItemMBERelOIDs);
				//${CLASS:CHiPSUtil}.connect(context,domObjAbstractMBE , CHiPSDomainConstant.RELATIONSHIP_ABSTRACT_MBE, true, arrayListMBEOIDs);
				domObjAbstractMBE.setAttributeValue(context, "WMSTotalCost", String.valueOf(doubleAbsMBETotalAmount));
				Locale strLocale = context.getLocale();
				String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.CONNECTIONMBE.Alert");			
				mapResult.put("Message", strMessage);
			}
			return mapResult;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	private MapList getSelectedItemInfo(Context context, String[] emxTableRowId) throws FrameworkException {
		StringList strListItemInfo = new StringList(4);
		strListItemInfo.add(DomainConstants.SELECT_ID);
		strListItemInfo.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
		strListItemInfo.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
		strListItemInfo.add("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]");
		int intSizeTemp = emxTableRowId.length;
		String[] strItemOIDs = new String[intSizeTemp];
		for (int i = 0; i < intSizeTemp; i++)
		{
			String[] emxTableRowIdData = emxTableRowId[i].split("[|]");
			String strItemOID = emxTableRowIdData[1];
			if(UIUtil.isNotNullAndNotEmpty(strItemOID))
			{
				strItemOIDs[i] = strItemOID;
			}
		}
		MapList mapListItemInfo = DomainObject.getInfo(context, strItemOIDs, strListItemInfo);
		return mapListItemInfo;
	}
	private StringList getItemConnectedMBERelOIDs(Context context,
			DomainObject domObjItem) throws FrameworkException {
		try
		{
			String strBusWhere = "current==Submitted";
			String strRelWhere = "(attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value==\"\")";
			MapList mapListMBEs = getItemConnectedMBEs(context,domObjItem,strRelWhere,strBusWhere);
			MapList mapListFilteredMBEs = filterMBEByRevision(mapListMBEs);
			StringList strListRelOIDS = WMSUtil_mxJPO.convertToStringList(mapListFilteredMBEs, DomainRelationship.SELECT_ID);
			return strListRelOIDS;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private MapList filterMBEByRevision(MapList mapListMBEs) 
	{
		MapList mapListFinalMBEs = new MapList();//CHiPSUtil_mxJPO.getSubMapList(mapListMBEs, "attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"].value", "Final");
		mapListMBEs.removeAll(mapListFinalMBEs);
		Map<String, String> mapLatestMBE = new HashMap<String, String>();
		Map<String, String> mapData;
		mapListMBEs.addSortKey(DomainConstants.SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
		mapListMBEs.addSortKey(DomainObject.SELECT_REVISION, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
		mapListMBEs.sortStructure();
		double doublePreviousBilledQuantity = 0d;
		double doubleCurrentBilledQuantity = 0d;
		String strRunningMBEName = DomainConstants.EMPTY_STRING;
		while(mapListMBEs.size()>0)
		{
			ListIterator<Map<String,String>> iterator = mapListMBEs.listIterator();
			mapData = iterator.next();
			String strMBEName = mapData.get(DomainConstants.SELECT_NAME);
			MapList mapListTemp = WMSUtil_mxJPO.getSubMapList(mapListMBEs, DomainConstants.SELECT_NAME, strMBEName);
			if(mapListTemp.size()>0)
			{
				mapLatestMBE = (Map<String, String>) mapListTemp.get(0);
				String strCurrentQuanity = mapLatestMBE.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
				doubleCurrentBilledQuantity =  WMSUtil_mxJPO.convertToDouble(strCurrentQuanity);
				//Iterate through he above MapList and identify if any of the revision is connected to context AbsMBE
				ListIterator<Map<String,String>> iteratorTemp = mapListTemp.listIterator();
				MapList mapListRunningMBEs = new MapList();
				while(iteratorTemp.hasNext())
				{
					mapData= iteratorTemp.next();
					String strPreviousBilledMBE = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
					if(UIUtil.isNotNullAndNotEmpty(strPreviousBilledMBE))
					{
						String strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
						doublePreviousBilledQuantity = WMSUtil_mxJPO.convertToDouble(strQuantity);
						break;
					}
				}
				mapLatestMBE.put("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value", String.valueOf((doubleCurrentBilledQuantity-doublePreviousBilledQuantity)));
				doubleCurrentBilledQuantity = 0d;
				doublePreviousBilledQuantity = 0d;
				mapListFinalMBEs.add(mapLatestMBE);
				mapListMBEs.removeAll(mapListTemp);
			}
		}
		return mapListFinalMBEs;
	}
	/**
	 * Method connects the Items and abstract MBE. Also It calculates cost as per the Quantity and rate provided in info map
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbstractMBE DomainObject instance of the new Abstract MBE
	 * @param strItemOID String value containing items OID
	 * @param mapItemInfo Map<String,String> contains the ItemOID , ItemOID_Rate as key
	 * @return doubleAbsMBEItemCost double value containing the Item cost
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private double connectAbstractMBEItems(Context context,
			DomainObject domObjAbstractMBE,
			String strItemOID,
			Map<String, String> mapItemInfo) throws FrameworkException {
		try
		{
			double doubleAbsMBEItemCost = 0d;
			DomainObject domObjItem = DomainObject.newInstance(context);
			Map<String,String> mapAttributesValue = new HashMap<String, String>(7);
			mapAttributesValue.put("WMSAbsMBEItemCost", "0");
			mapAttributesValue.put("WMSMBEActivityQuantity", "0");
			domObjItem.setId(strItemOID);
			DomainRelationship domRel = DomainRelationship.connect(context,domObjAbstractMBE , "WMSAbstractMBEActivities",domObjItem );
			String strRate = mapItemInfo.get((strItemOID+"_Rate"));
			double doubleRate = WMSUtil_mxJPO.convertToDouble(strRate);
			double doubleCurrentBillQuantity = WMSUtil_mxJPO.convertToDouble(mapItemInfo.get(strItemOID));
			doubleAbsMBEItemCost = (doubleRate*doubleCurrentBillQuantity);
			mapAttributesValue.put("WMSAbsMBEItemCost", String.valueOf(doubleAbsMBEItemCost));
			mapAttributesValue.put("WMSAMBItemTotalCost",String.valueOf(doubleAbsMBEItemCost));
			mapAttributesValue.put("WMSMBEActivityQuantity", String.valueOf(doubleCurrentBillQuantity));
			mapAttributesValue.put("WMSAbstractMBEItemPayableQuantity", String.valueOf(doubleCurrentBillQuantity));
			mapAttributesValue.put("WMSDeductionRate", String.valueOf(doubleRate));
			//updateItemPaidQuanity(context,domObjItem, doubleCurrentBillQuantity,domObjAbstractMBE.getInfo(context, DomainConstants.SELECT_ID));
			domRel.setAttributeValues(context, mapAttributesValue);
			return doubleAbsMBEItemCost;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private void setAbsMBEOIDOnItemsMBE(Context context, HashMap attributeMap,
			StringList strListItemMBERelOIDs)
					throws FrameworkException {
		boolean isContextPushed = true;
		try
		{
			//ContextUtil.pushContext(context, "person_UserAgent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			ContextUtil.pushContext(context);
			Iterator<String> iterator = strListItemMBERelOIDs.iterator();
			String strRelOID = DomainConstants.EMPTY_STRING;
			while(iterator.hasNext())
			{
				strRelOID = iterator.next();
				DomainRelationship.setAttributeValues(context, strRelOID, attributeMap);
			}
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
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
	
	private MapList getItemConnectedMBEs(Context context,
			DomainObject domObjItem,String strRelWhere,String strBusWhere) throws FrameworkException {
		try
		{
			if(UIUtil.isNullOrEmpty(strBusWhere))
			{
				strBusWhere = DomainConstants.EMPTY_STRING;
			}
			if(UIUtil.isNullOrEmpty(strRelWhere))
			{
				strRelWhere = DomainConstants.EMPTY_STRING;
			}
			StringList strListBusSelects = getItemConnectedMBEBusSelects();
			StringList strListRelSelects = getItemConnectedMBERelSelects();
			MapList mapListMBEs = domObjItem.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_MBE_ACTIVITIES, // relationship pattern
					TYPE_WMS_MEASUREMENT_BOOK_ENTRY, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					true, // to direction
					false, // from direction
					(short) 1, // recursion level
					strBusWhere, // object where clause
					strRelWhere, // relationship where clause
					0);
			return mapListMBEs;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private StringList getItemConnectedMBEBusSelects() {
		StringList strListBusSelects = new StringList(5);
		strListBusSelects.add(DomainConstants.SELECT_ID);
		strListBusSelects.add(DomainConstants.SELECT_REVISION);
		strListBusSelects.add(DomainConstants.SELECT_NAME);
		strListBusSelects.add("previous.id");
		//strListBusSelects.add("attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"].value");
		return strListBusSelects;
	}
	private StringList getItemConnectedMBERelSelects() {
		StringList strListRelSelects = new StringList(3);
		strListRelSelects.add(DomainRelationship.SELECT_ID);
		strListRelSelects.add("attribute[WMSMBEActivityQuantity].value");
		strListRelSelects.add("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
		return strListRelSelects;
	}
	/**
	 * Method to remove  the Item to AbstractMBE
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map<String,String> removeAbstractMBEItem(Context context, String[] args) throws Exception 
	{
		StringList slErrorLog = new StringList();
		try
		{
			Map<String,String> mapResult = new HashMap<String,String>();
			mapResult.put(DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING);
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
			String strAbstractMBEOID = (String) programMap.get("objectId");
			int intSize = emxTableRowId.length;
			if(emxTableRowId !=null && intSize>0&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID))
			{
				ArrayList<String> arrayListRelIDs 		= new ArrayList<String>(intSize);
				DomainObject domObjAbstractMBE  		= DomainObject.newInstance(context, strAbstractMBEOID);
				String strAbsTotalValue = domObjAbstractMBE.getAttributeValue(context, "WMSTotalCost");
				double doubleAbsMBETotalAmount = WMSUtil_mxJPO.convertToDouble(strAbsTotalValue);
				Map<String,String> mapQuanity = new HashMap<String,String>(intSize);
				Map<String,String> mapData ;

				MapList mapListAbsMBEConnectedObjects 	= getAbsMBEConnectedObjects(context, domObjAbstractMBE);
				Map<String, String> mapAbsMBERelData 	= getAbsMBERelData(mapListAbsMBEConnectedObjects);
				DomainObject domItemTask=DomainObject.newInstance(context);
				
				boolean bRemove =false;
				for (int i = 0; i < intSize; i++)
				{
					String[] emxTableRowIdData = emxTableRowId[i].split("[|]");
					String strItemOID = emxTableRowIdData[1];
					if(UIUtil.isNotNullAndNotEmpty(strItemOID))
					{
						//domItemTask.setId(strItemOID);
						//bRemove =checkIfStockEntries(context,domItemTask,slErrorLog);
					//if(true) {
						deleteTechnicalDeductions(context, strItemOID, strAbstractMBEOID);
						WMSMaterial_mxJPO objMaterial        =  new WMSMaterial_mxJPO(context, args); 
                        Map  removeMCMap                       = objMaterial.removeConnectedMC(context ,strItemOID , strAbstractMBEOID);
                         Map<String,String> mapItemData = WMSUtil_mxJPO.getMap(mapListAbsMBEConnectedObjects, DomainConstants.SELECT_ID, strItemOID);
						StringList stsrListItemMBERelOIDs = getAbsMBESingleMBEOID(context,mapListAbsMBEConnectedObjects, strAbstractMBEOID, strItemOID);
						//DomainRelationship.setAttributeValue(context, strRelOID, ATTRIBUTE_ABS_MBE_OID, DomainConstants.EMPTY_STRING);
						HashMap<String,String> hashMapAttributeMap = new HashMap<String,String>(2);
						hashMapAttributeMap.put(ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER,"0");
						hashMapAttributeMap.put(ATTRIBUTE_WMS_ABS_MBE_OID,DomainConstants.EMPTY_STRING);
						setAbsMBEOIDOnItemsMBE(context, hashMapAttributeMap, stsrListItemMBERelOIDs);
						String strItemRelOID = mapAbsMBERelData.get(strItemOID);
						arrayListRelIDs.add(strItemRelOID);
						String strCurrentBillQuantity = mapItemData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"]");
						double doubleCurrentBillQuantity =WMSUtil_mxJPO.convertToDouble(strCurrentBillQuantity);
						//updateItemPaidQuanity(context, domObjItem, (doubleCurrentBillQuantity*-1), strAbstractMBEOID);
						mapQuanity.put(strItemOID,String.valueOf(doubleCurrentBillQuantity));
						mapQuanity.put(strItemOID+"_Rate",mapItemData.get("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]"));
						double doubleItemCost = updateTotalCostOfMBEItems(context,  strItemOID, mapQuanity);
						doubleAbsMBETotalAmount-=doubleItemCost;
					 
					}

				}
				WMSUtil_mxJPO.disconnect(context, arrayListRelIDs);
				domObjAbstractMBE.setAttributeValue(context, "WMSTotalCost", String.valueOf(doubleAbsMBETotalAmount));
			}
			//if(!slErrorLog.isEmpty())
			//	 emxContextUtil_mxJPO.mqlNotice(context,"Below list of particulars cannot be deleted as material consuption is reported ,please remove consumed material and then try  \n"+slErrorLog.toString());
			//	mapResult.put("Message", "Below list of particulars cannot be deleted as material consuption is reported ,please remove consumed material and then try  \n"+slErrorLog.toString());
			return mapResult;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 *  checking if is any connected consumption 
	 * 
	 * @param context
	 * @param domItemTask
	 * @param slErrorLog
	 * @return
	 * @throws Exception
	 */
	
	public boolean checkIfStockEntries(Context context,DomainObject domItemTask,StringList slErrorLog) throws Exception
	{
		StringList slInfo = new StringList(2);
		slInfo.add("attribute[Title]");
		slInfo.add("from["+RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION+"]");
	    Map  m=  domItemTask.getInfo(context, slInfo);
	    String strConsumption=(String)m.get("from["+RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION+"]");
	    if(strConsumption.equalsIgnoreCase("TRUE")) {
	    	slErrorLog.add((String)m.get("attribute[Title]"));
	    	return false;
	    }
		
		return true;
	}
	
	private MapList getAbsMBEConnectedObjects(Context context,
			DomainObject domObjAbstractMBE) throws FrameworkException {
		try
		{
			StringList strListBusSelects=new StringList(4);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add("attribute["+ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE+"]");
			strListBusSelects.add("attribute["+ATTRIBUTE_WMS_SOR_RATE+"]");
			strListBusSelects.add("attribute[WMSTotalCost]");
			
			StringList strListRelSelects=new StringList(5);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			strListRelSelects.add("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
			strListRelSelects.add("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"]");
			strListRelSelects.add("from.id");
			strListRelSelects.add("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"]");

			Pattern patternRel = new Pattern(RELATIONSHIP_WORKORDER_ABSTRACT_MBE);;
			patternRel.addPattern("WMSAbstractMBEActivities");
			Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
			patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK_ENTRY);

			MapList mapListAbsMBEConnectedObjects = domObjAbstractMBE.getRelatedObjects(context, // matrix context
					patternRel.getPattern(), // relationship pattern
					patternType.getPattern(), // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);

			return mapListAbsMBEConnectedObjects;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private Map<String, String> getAbsMBERelData(
			MapList mapListAbsMBEConnectedObjects) {
		Map<String,String> mapAbsMBERelData = new HashMap<String, String>(mapListAbsMBEConnectedObjects.size());
		Map<String,String> mapData;
		Iterator<Map<String,String>> iterator = mapListAbsMBEConnectedObjects.iterator();
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			mapAbsMBERelData.put(mapData.get(DomainConstants.SELECT_ID), mapData.get(DomainRelationship.SELECT_ID));
		}
		return mapAbsMBERelData;
	}
	private StringList getAbsMBESingleMBEOID(Context context,
			MapList mapListItemsData, String strAbstractMBEOID,
			String strItemOID) throws FrameworkException {
		StringList strListItemConnectedMBEs = new StringList();
		String strMBEOID = DomainConstants.EMPTY_STRING;
		Map<String,String> mapData ;
		Map<String,StringList> mapItemRelInfo = new HashMap<String, StringList>();
		Iterator<Map<String,String>> iterator = mapListItemsData.iterator();
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			String strAbsMBEOIDValue = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
			String strItemOIDTemp =  mapData.get(DomainConstants.SELECT_ID);
			strMBEOID =  mapData.get("from.id");
			if(strAbstractMBEOID.equals(strAbsMBEOIDValue)&&strItemOIDTemp.equals(strItemOID))
			{
				String strRelOID = mapData.get(DomainRelationship.SELECT_ID);
				strListItemConnectedMBEs.add(strRelOID);
			}
		}
		return strListItemConnectedMBEs;
	}
	/**
	 * Method used to substract cost of removed items from Abs MBE
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbstractMBE DomainObject instance of the new Abstract MBE
	 * @param strItemOID String value containing items OID
	 * @param mapItemInfo Map<String,String> contains the ItemOID , ItemOID_Rate as key
	 * @return doubleAbsMBEItemCost double value containing the Item cost
	 * @author CHiPS
	 * @since 418
	 */
	private double updateTotalCostOfMBEItems(Context context,String strItemOID,Map<String, String> mapItemInfo) 
		{
			double doubleAbsMBEItemCost = 0d;
			String strRate = mapItemInfo.get((strItemOID+"_Rate"));
			double doubleRate = WMSUtil_mxJPO.convertToDouble(strRate);
			double doubleQuatity = WMSUtil_mxJPO.convertToDouble(mapItemInfo.get(strItemOID));
			doubleAbsMBEItemCost = (doubleRate*doubleQuatity);
			return doubleAbsMBEItemCost;
		}
	/**
	 * It gives quantity of MBE
	 * 
	 * @param context the eMatrix <code>Context</code> object 
	 * @param args
	 * @return Response values list
	 * @throws Exception if operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public Vector<String> displayQuantity(Context context, String[] args)throws Exception
	{
		try
		{
			Map<String,Object> programMap =   (Map<String,Object>)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			int intSize = objectList.size();
			Vector<String> vecResponse = new Vector<String>(intSize);
			Iterator<Map<String,String>> iterator  = objectList.iterator();
			Map<String,String> mapData;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strAbsMBEQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
				vecResponse.add(strAbsMBEQuantity);
			}
			return vecResponse;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * It selects the checkbox if there is a abstract MBE already connected
	 * 
	 * @param context the eMatrix <code>Context</code> object 
	 * @param args
	 * @return Response values list with true or false
	 * @throws Exception if operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public Vector<String> autoSelect(Context context, String[] args)
			throws Exception
	{
		try{
			Map<String,Object> programMap =   (Map<String,Object>)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			int intSize = objectList.size();
			Vector<String> vecResponse = new Vector<String>(intSize);
			Iterator<Map<String,String>> iterator  = objectList.iterator();
			Map<String,String> mapData ;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strAbsMBEOID = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
				if(UIUtil.isNullOrEmpty(strAbsMBEOID))
				{
					vecResponse.add(String.valueOf(true));
				}
				else
				{
					vecResponse.add(String.valueOf(false));
				}
			}
			return vecResponse;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * Method to get the Item connected MBEs WRT to Abstract MBE
	 * @mx_used On Abstract MBE paticular table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListMBEs MapList containing the MBE IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAbsMBEItemConnectedMBEs(Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListFilterMBEs = new MapList();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			String strItemOID = (String) programMap.get("objectId");
			String strRelID =(String) programMap.get("relId");
			if(strItemOID.equals(strAbstractMBEOID) && UIUtil.isNotNullAndNotEmpty(strRelID))
			{
				strAbstractMBEOID = getAbsMBEOIDFromRelOID(context,
						strRelID);
			}
			if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID)&&!strItemOID.equals(strAbstractMBEOID))
			{
				DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
				String strBusWhere = "current==Submitted";
				String strRelWhere = "(attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value==\""+strAbstractMBEOID+"\")";
				MapList mapListAbstractMBEConnectedMBEs =  getItemConnectedMBEs(context,domObjItem,strRelWhere,strBusWhere);
				mapListFilterMBEs = getAbsMBEItemConnectedMBEs(context,strAbstractMBEOID, mapListAbstractMBEConnectedMBEs);
				//mapListFilterMBEs = filterMBEs(mapListMBEs);
				if(mapListFilterMBEs.size()>0)
				{
					WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListFilterMBEs, DomainRelationship.SELECT_ID, strAbstractMBEOID);
				}
			}
			return mapListFilterMBEs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	private String getAbsMBEOIDFromRelOID(Context context,
			String strRelID)
					throws FrameworkException 
	{
		try
		{
			String[] strRelIDs = {strRelID};
			String strAbstractMBEOID = DomainConstants.EMPTY_STRING;
			SelectList selListRelInfo = new SelectList(2);
			selListRelInfo.add("to.id");
			selListRelInfo.add("from.id");
			MapList mapListRelInfo = DomainRelationship.getInfo(context, strRelIDs, selListRelInfo);
			Iterator<Map<String,String>> iteratorRelInfo = mapListRelInfo.iterator();
			while(iteratorRelInfo.hasNext())
			{
				Map<String,String> mapRelInfo = iteratorRelInfo.next();
				strAbstractMBEOID = mapRelInfo.get("from.id");
			}
			return strAbstractMBEOID;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private MapList getAbsMBEItemConnectedMBEs(Context context,
			String strAbstractMBEOID,MapList mapListAbstractMBEConnectedMBEs)
	{
		MapList mapListFilterMBEs;

		mapListFilterMBEs =  getItemFinalMBEs( strAbstractMBEOID,mapListAbstractMBEConnectedMBEs);
		mapListAbstractMBEConnectedMBEs.removeAll(mapListFilterMBEs);
		if(mapListAbstractMBEConnectedMBEs!=null && mapListAbstractMBEConnectedMBEs.size()>0)
		{
			mapListAbstractMBEConnectedMBEs.addSortKey(DomainObject.SELECT_REVISION, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
			mapListAbstractMBEConnectedMBEs.sort();

			Map<String,String> mapData;
			while(mapListAbstractMBEConnectedMBEs.size()>0)
			{
				Map<String,String> mapPivotMap = new HashMap<String, String>();
				Iterator<Map<String,String>> iterator = mapListAbstractMBEConnectedMBEs.iterator();
				mapData= iterator.next();
				String strName = mapData.get(DomainConstants.SELECT_NAME);
				MapList mapListTemp = WMSUtil_mxJPO.getSubMapList(mapListAbstractMBEConnectedMBEs, DomainConstants.SELECT_NAME, strName);
				ListIterator<Map<String,String>> iteratorTemp = mapListTemp.listIterator();
				boolean booleanPivotElement = false;
				String strAbsMBEOID = DomainConstants.EMPTY_STRING;
				String strQuantity = DomainConstants.EMPTY_STRING;
				double doubleQuantity = 0d;
				double doublePivotQuantity = 0d;
				while(iteratorTemp.hasNext())
				{
					mapData= iteratorTemp.next();
					strAbsMBEOID = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
					if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOID)&& strAbstractMBEOID.equals(strAbsMBEOID))
					{
						mapPivotMap.putAll(mapData);
						booleanPivotElement = true;
						strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
						doublePivotQuantity = WMSUtil_mxJPO.convertToDouble(strQuantity);
						strAbsMBEOID = DomainConstants.EMPTY_STRING;
					}
					if(booleanPivotElement)
					{
						if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOID))
						{
							strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
							doubleQuantity = WMSUtil_mxJPO.convertToDouble(strQuantity);
						}
					}
				}
				booleanPivotElement = false;
				mapListAbstractMBEConnectedMBEs.removeAll(mapListTemp);
				if(mapPivotMap!=null && mapPivotMap.size()>0)
				{
					mapPivotMap.put("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value",String.valueOf(doublePivotQuantity-doubleQuantity));
					mapListFilterMBEs.add(mapPivotMap);
					doubleQuantity = 0d;
					doublePivotQuantity = 0d;
				}
			}
		}
		return mapListFilterMBEs;
	}
	private MapList getItemFinalMBEs( String strAbstractMBEOID,
			MapList mapListItemConnectedMBEs) {
		MapList mapListFilterMBEs = new MapList();
		Iterator<Map<String,String>> iteratorMBEs = mapListItemConnectedMBEs.iterator();
		Map<String,String> mapData;
		while(iteratorMBEs.hasNext())
		{
			mapData = iteratorMBEs.next();
			String strAbsMBEOID = mapData.get( "attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
			if(strAbstractMBEOID.equals(strAbsMBEOID))
			{
				mapListFilterMBEs.add(mapData);
			}
		}
		return mapListFilterMBEs;
	}
	/**
	 * Method to get the MBEs connected to Item which are also not connected to any Abstract MBE
	 * @mx_used On Abstract MBE particular table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListMBEs MapList containing the MBE IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAbsMBEItemNotConnectedMBEs(Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListFilterMBEs = new MapList();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			String strItemOID = (String) programMap.get("objectId");
			String strRelID =(String) programMap.get("relId");
			if(strItemOID.equals(strAbstractMBEOID) && UIUtil.isNotNullAndNotEmpty(strRelID))
			{
				strAbstractMBEOID = getAbsMBEOIDFromRelOID(context,
						strRelID);
			}
			if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID)&&!strItemOID.equals(strAbstractMBEOID))
			{
				DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
				String strBusWhere = "current==Submitted";
				MapList mapListMBEs =  getItemConnectedMBEs(context,domObjItem,DomainConstants.EMPTY_STRING,strBusWhere);
				mapListFilterMBEs = getMBEWithDisableSelectionFlag(mapListMBEs,strAbstractMBEOID);
				WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListFilterMBEs, DomainRelationship.SELECT_ID, strAbstractMBEOID);
			}
			return mapListFilterMBEs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	private MapList getMBEWithDisableSelectionFlag(MapList mapListMBEs,String strAbstractMBEOID) 
	{
		Map<String,String> mapData ;
		MapList mapListFinalMBEs = getItemFinalMBEs(mapListMBEs);
		mapListMBEs.removeAll(mapListFinalMBEs);
		WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListFinalMBEs,  "disableSelection", "false");
		//mapListMBEs.addSortKey("attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"].value", ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
		mapListMBEs.addSortKey(DomainConstants.SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
		mapListMBEs.addSortKey(DomainObject.SELECT_REVISION, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
		mapListMBEs.sort();
		while(mapListMBEs.size()>0)
		{
			ListIterator<Map<String,String>> iterator = mapListMBEs.listIterator();
			mapData= iterator.next();
			String strName = mapData.get(DomainConstants.SELECT_NAME);
			//Get a separate MapList based on Running MBE name.MapList size can be from 1 to N
			MapList mapListTemp = WMSUtil_mxJPO.getSubMapList(mapListMBEs, DomainConstants.SELECT_NAME, strName);
			//Iterate through he above MapList and identify if any of the revision is connected to context AbsMBE
			ListIterator<Map<String,String>> iteratorTemp = mapListTemp.listIterator();
			MapList mapListRunningMBEs = new MapList();
			boolean bConsiderEntries = true;
			while(iteratorTemp.hasNext())
			{
				mapData= iteratorTemp.next();
				String strAbsMBEOIDTemp = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOIDTemp)&& strAbstractMBEOID.equals(strAbsMBEOIDTemp))
				{
					bConsiderEntries = false;
					break;
				}
				else
				{
					mapListRunningMBEs.add(mapData);
				}
			}
			//If No MBE is connected to context AbsMBE , no disable selection
			if(bConsiderEntries)
			{
				//if any of these MBEs is connected to a different Abstract MBE or not connected to any , display only latest versions with delta separated
				double doublePreviousBilledQuantity = 0d;
				iteratorTemp = mapListTemp.listIterator();
				MapList mapListLatestRunningMBEs = new MapList();
				while(iteratorTemp.hasNext())
				{
					mapData= iteratorTemp.next();
					String strPreviousBilledMBE = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
					if(UIUtil.isNotNullAndNotEmpty(strPreviousBilledMBE))
					{
						String strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
						doublePreviousBilledQuantity = WMSUtil_mxJPO.convertToDouble(strQuantity);
						break;
					}
					else
					{
						mapListLatestRunningMBEs.add(mapData);
					}
				}
				iteratorTemp = mapListLatestRunningMBEs.listIterator();
				while(iteratorTemp.hasNext())
				{
					mapData= iteratorTemp.next();
					String strCurrentQuanity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
					double doubleCurrentBilledQuantity =  WMSUtil_mxJPO.convertToDouble(strCurrentQuanity);
					mapData.put("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value",String.valueOf((doubleCurrentBilledQuantity-doublePreviousBilledQuantity)));
				}
				mapListFinalMBEs.addAll(mapListLatestRunningMBEs);
			}
			else
			{
				WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListRunningMBEs,   "disableSelection", "true");
				mapListFinalMBEs.addAll(mapListRunningMBEs);
			}
			mapListMBEs.removeAll(mapListTemp);
		}
		return mapListFinalMBEs;
	}
	private MapList getItemFinalMBEs(MapList mapListMBEs) {
		Map<String, String> mapData;
		MapList mapListFinalMBEs =new MapList();// ${CLASS:WMSUtil}.getSubMapList(mapListMBEs, "attribute["+CHiPSDomainConstant.ATTRIBUTE_MBE_TYPE+"].value", "Final");
		ListIterator<Map<String,String>> iteratorFinal = mapListMBEs.listIterator();
		while(iteratorFinal.hasNext())
		{
			mapData= iteratorFinal.next();
			String strAbsMBEOIDTemp = mapData.get("attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value");
			if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOIDTemp)==false)
			{
				mapListFinalMBEs.add(mapData);
			}
		}
		return mapListFinalMBEs;
	}
	/**
	 * Method remove the MBE's from AbstractMBE and deduce the Quantity
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map<String,String> removeExistingMBE(Context context, String[] args) throws Exception 
	{
		try
		{
			Map<String,String> mapResult = new HashMap<String,String>();
			mapResult.put(DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING);
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
			String strItemOID = (String) programMap.get("objectId");
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			int intSize = emxTableRowId.length;
			
			// Pre-UAT issue tempearary issue work around
			if(emxTableRowId !=null && emxTableRowId.length>0)
			{
				ArrayList<String> arrayListMBEOIDs = new ArrayList<String>();
				for (int i = 0; i < intSize; i++)
				{
					String[] emxTableRowIdData = emxTableRowId[i].split("[|]");
					arrayListMBEOIDs.add(emxTableRowIdData[1]);
				}
				/*
				boolean bRemove=true;
				DomainObject domItem = DomainObject.newInstance(context);
				for(int i =0;i<arrayListMBEOIDsToRemove.size();i++) {
				   domItem.setId( (String) arrayListMBEOIDsToRemove.get(i)); 
				   bRemove =checkIfStockEntries(context,domItem,slErrorLog);
				   if(bRemove) {
					   arrayListMBEOIDs.add(arrayListMBEOIDsToRemove.get(i));
				   }
					
				}
				
				 if(slErrorLog.isEmpty()) {
			        	mapResult.put("Message2", slErrorLog.toString());
					}*/
				
				if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID)&&!strItemOID.equals(strAbstractMBEOID))
				{
					//Get the Item Rate
					DomainObject domObjAbstractMBE         = DomainObject.newInstance(context, strAbstractMBEOID);
					DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
					String strBusWhere = "current==Submitted";

					MapList mapListAbstractMBEConnectedMBEs =  getItemConnectedMBEs(context,domObjItem,DomainConstants.EMPTY_STRING,strBusWhere);
					MapList mapListFilterMBEs = getAbsMBEItemConnectedMBEs(context,strAbstractMBEOID, mapListAbstractMBEConnectedMBEs);

					if(intSize == mapListFilterMBEs.size())
					{
						Locale strLocale = context.getLocale();
						String strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", strLocale, "WMS.MBEs.CannotRemoveMBE.Alert");			
						mapResult.put("Message", strMessage);
						//mapResult.put(CHiPSConstants.Message, "Cannot Connect Abstract MBE with the selected Data");
						return mapResult;
					}
					else
					{
						double doubleRemovalQuantity = getRemovalQuantity(context, arrayListMBEOIDs, mapListFilterMBEs);
						if(doubleRemovalQuantity!=0)
						{
							 deleteTechnicalDeductions(context, strItemOID, strAbstractMBEOID);
							WMSMaterial_mxJPO objMaterial        =  new WMSMaterial_mxJPO(context, args); 
		                     Map  removeMCMap                       = objMaterial.removeConnectedMC(context ,strItemOID , strAbstractMBEOID);
		                     //String strRelWhere = "(attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value==\"\")";
							String strItemRate = domObjItem.getAttributeValue(context, ATTRIBUTE_WMS_SOR_RATE);
							double doubleRate = WMSUtil_mxJPO.convertToDouble(strItemRate);
							//double doubleRate = updateItemPaidQuanity(context,domObjItem, (doubleRemovalQuantity*-1),strAbstractMBEOID);

							String strAbsTotalValue = domObjAbstractMBE.getAttributeValue(context, ATTRIBUTE_WMS_TOTAL_COST);
							double doubleAbsMBETotalAmount = WMSUtil_mxJPO.convertToDouble(strAbsTotalValue);
							double doubleItemCost = doubleRemovalQuantity*doubleRate;
							doubleAbsMBETotalAmount-=doubleItemCost;
							domObjAbstractMBE.setAttributeValue(context, ATTRIBUTE_WMS_TOTAL_COST, String.valueOf(doubleAbsMBETotalAmount));
		                    
							updateAbsMBEItemRelationshipAttributes(context,strItemOID, domObjAbstractMBE,(doubleRemovalQuantity*-1), doubleRate);
							Locale strLocale = context.getLocale();
							//String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.CONNECTIONMBE.Alert");			
							mapResult.put("Message", "Selected MBEs have been removed successfully");
							return mapResult;
						}
						else
						{
							Locale strLocale = context.getLocale();
							//String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NoChangeinQunatity.Alert");			
							mapResult.put("Message", "No change in Quantity");
							// mapResult.put(CHiPSConstants.Message, "Connection of Abstract MBE with the selected Data Sucuessfully completed");
							return mapResult;
						}

					}
				}
				else
				{
					Locale strLocale = context.getLocale();
					String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NOTCONNECTIONMBE.Alert");			
					mapResult.put("Message", strMessage);
					//mapResult.put(CHiPSConstants.Message, "Cannot Connect Abstract MBE with the selected Data");
					return mapResult;
				}
			}
			else
			{
				Locale strLocale = context.getLocale();
				String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NOTCONNECTIONMBE.Alert");			
				mapResult.put("Message", strMessage);
				//mapResult.put(CHiPSConstants.Message, "Cannot Connect Abstract MBE with the selected Data");
				return mapResult;
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	
	
	/**
	 * Method to get the Item connected technical reductions WRT to Abstract MBE and delete the technical deductions
	 * @param context the eMatrix <code>Context</code> object
	 * @param strAbstractMBEOID String value containing the Abstract MBE OID
	 * @param strItemOID String value containing the Item OID
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private void deleteTechnicalDeductions(Context context, String strItemOID, String strAbstractMBEOID)
			throws  Exception {
		try
		{
			MapList mapListTechnicalDeductions = getItemConnectedTechnicalReductions(context, strAbstractMBEOID, strItemOID);
			StringList strListTechnicalDeductionsOIDs =WMSUtil_mxJPO.convertToStringList(mapListTechnicalDeductions, DomainConstants.SELECT_ID);
			ArrayList<String> arrayListOIDs = new ArrayList<String>(strListTechnicalDeductionsOIDs.size());
			arrayListOIDs.addAll(strListTechnicalDeductionsOIDs);
		   DomainObject.deleteObjects(context, arrayListOIDs.toArray(new String[arrayListOIDs.size()]));
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	
	
	private double getRemovalQuantity(Context context,
			ArrayList<String> arrayListMBEOIDs, MapList mapListFilterMBEs)
					throws FrameworkException {
		try
		{
			Map<String, String> mapData;
			Iterator<Map<String,String>> iterator = mapListFilterMBEs.iterator();
			double doubleRemovalQuantity =0d;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strMBEID = mapData.get(DomainConstants.SELECT_ID);
				if(arrayListMBEOIDs.contains(strMBEID))
				{
					String strMBEItemRelID= mapData.get(DomainRelationship.SELECT_ID);
					String strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
					doubleRemovalQuantity+= WMSUtil_mxJPO.convertToDouble(strQuantity);
					HashMap<String,String> hashMapAttributes     = new HashMap<String, String>();
					hashMapAttributes.put(ATTRIBUTE_WMS_ABS_MBE_OID, DomainConstants.EMPTY_STRING);
					//hashMapAttributes.put(ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER,DomainConstants.EMPTY_STRING);
		            ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try
					{
					DomainRelationship.setAttributeValues(context, strMBEItemRelID, hashMapAttributes);
				}
					catch(Exception exception)
					
					{
						
					}
					finally
					{
						ContextUtil.popContext(context);
					}
				}
			}
			return doubleRemovalQuantity;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private void updateAbsMBEItemRelationshipAttributes(Context context,
			String strItemOID, DomainObject domObjAbstractMBE,
			double doubleRemovalQuantity, double doubleRate)
					throws FrameworkException {
		try
		{
			MapList mapListAbsMBEConnectedObjects 	= getAbsMBEConnectedObjects(context, domObjAbstractMBE);
			Map<String, String> mapAbsMBERelData 	= getAbsMBEItemRelData(mapListAbsMBEConnectedObjects,strItemOID);
			String strAbsMBEItemRelID = mapAbsMBERelData.get(DomainRelationship.SELECT_ID);
			String strBillQuanity = mapAbsMBERelData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"]");
			double doubleBillQuantity = WMSUtil_mxJPO.convertToDouble(strBillQuanity);
			double doubleFinalQunaity = doubleBillQuantity+doubleRemovalQuantity;
			HashMap<String,String> hashMapAttributes     = new HashMap<String, String>();
			hashMapAttributes.put(ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY, String.valueOf(doubleFinalQunaity));
			hashMapAttributes.put(ATTRIBUTE_WMS_ABS_MBE_ITEM_COST,String.valueOf(doubleFinalQunaity*doubleRate));
			DomainRelationship.setAttributeValues(context, strAbsMBEItemRelID, hashMapAttributes);
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	private Map<String, String> getAbsMBEItemRelData(
			MapList mapListAbsMBEConnectedObjects,String strItemOID) {
		Map<String,String> mapAbsMBERelData = new HashMap<String, String>();
		Map<String,String> mapData;
		Iterator<Map<String,String>> iterator = mapListAbsMBEConnectedObjects.iterator();
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			String strId= mapData.get(DomainConstants.SELECT_ID);
			if(strItemOID.equals(strId))
			{
				mapAbsMBERelData.putAll(mapData);
				break;
			}

		}
		return mapAbsMBERelData;
	}
	private int getUniqueMBECount(Context context,
			ArrayList<String> arrayListMBEOIDs) throws FrameworkException {
		StringList strListUnique = new StringList();
		StringList strListInfo = new StringList(DomainConstants.SELECT_NAME);
		String [] strMBEIds = arrayListMBEOIDs.toArray(new String[arrayListMBEOIDs.size()]);
		MapList mapListMBEInfo = DomainObject.getInfo(context, strMBEIds, strListInfo);
		Iterator<Map<String,String>> iterator = mapListMBEInfo.iterator();
		Map<String,String> mapData ;
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			String strName = mapData.get(DomainConstants.SELECT_NAME);
			if(!strListUnique.contains(strName))
			{
				strListUnique.add(strName);
			}
		}
		int intSelectedObjectSize = strListUnique.size();
		return intSelectedObjectSize;
	}
	/**
	 * Method add the MBE's and to AbstractMBE
	 * @mxUsed Used as toolbar command from the link on the since previous bill column under Bill->Partiiculars
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTasks MapList containing the Task IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map<String,String> addExistingMBE(Context context, String[] args) throws Exception 
	{
		try
		{
			Map<String,String> mapResult = new HashMap<String,String>();
			mapResult.put(DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING);
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
			String strItemOID = (String) programMap.get("objectId");
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			int intSize = emxTableRowId.length;
			if(emxTableRowId !=null && emxTableRowId.length>0)
			{
				ArrayList<String> arrayListMBEOIDs = new ArrayList<String>();

				for (int i = 0; i < intSize; i++)
				{
					String[] emxTableRowIdData = emxTableRowId[i].split("[|]");
					arrayListMBEOIDs.add(emxTableRowIdData[1]);
				}
				int intSelectedObjectSize = getUniqueMBECount(context,arrayListMBEOIDs);
				if(intSelectedObjectSize!=intSize)
				{
					Locale strLocale = context.getLocale();
					String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.alert.AddingMBE.SameRunningMBE");			
					mapResult.put("Message", strMessage);
					//mapResult.put(CHiPSConstants.Message, "Cannot Connect Abstract MBE with the selected Data");
					return mapResult;
				}
				else if(arrayListMBEOIDs.size()>0)
				{
					DomainObject domObjAbstractMBE 			= DomainObject.newInstance(context, strAbstractMBEOID);
					DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
					String strBusWhere = "current==Submitted";
					String strRelWhere = "(attribute["+ATTRIBUTE_WMS_ABS_MBE_OID+"].value==\"\")";
					String strItemRate = domObjItem.getAttributeValue(context, ATTRIBUTE_WMS_SOR_RATE);
					double doubleRate = WMSUtil_mxJPO.convertToDouble(strItemRate);
					MapList mapListMBEs 					= getItemConnectedMBEs(context,domObjItem,strRelWhere,strBusWhere);
					MapList mapListFilterMBEs 				= getMBEWithDisableSelectionFlag(mapListMBEs,strAbstractMBEOID);
					double doubleAdditionalQuantity = getAdditionalQuantity(context, arrayListMBEOIDs, mapListFilterMBEs,domObjAbstractMBE);
					if(doubleAdditionalQuantity!=0)
					{
						//deleteTechnicalDeductions(context, strItemOID, strAbstractMBEOID);
						//${CLASS:CHiPSMaterial} objMaterial        =  new ${CLASS:CHiPSMaterial}(context, args); 
	                   // Map  removeMCMap                       = objMaterial.removeConnectedMC(context ,strItemOID , strAbstractMBEOID );
						
						String strAbsTotalValue = domObjAbstractMBE.getAttributeValue(context, ATTRIBUTE_WMS_TOTAL_COST);
						double doubleAbsMBETotalAmount = WMSUtil_mxJPO.convertToDouble(strAbsTotalValue);
						double doubleItemCost = doubleAdditionalQuantity*doubleRate;
						doubleAbsMBETotalAmount+=doubleItemCost;
						domObjAbstractMBE.setAttributeValue(context, ATTRIBUTE_WMS_TOTAL_COST, String.valueOf(doubleAbsMBETotalAmount));
						
						updateAbsMBEItemRelationshipAttributes(context,strItemOID, domObjAbstractMBE,doubleAdditionalQuantity, doubleRate);
						Locale strLocale = context.getLocale();
						String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.CONNECTIONMBE.CheckAlert");			
						mapResult.put("Message", strMessage);
						// mapResult.put(CHiPSConstants.Message, "Connection of Abstract MBE with the selected Data Sucuessfully completed");
						return mapResult;
					}
					else
					{
						Locale strLocale = context.getLocale();
						String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NoChangeinQunatity.Alert");			
						mapResult.put("Message", strMessage);
						return mapResult;
					}
				}
				else
				{
					Locale strLocale = context.getLocale();
					String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NOTCONNECTIONMBE.CheckAlert");			
					mapResult.put("Message", strMessage);
					return mapResult;
				}
			}
			else
			{
				Locale strLocale = context.getLocale();
				String strMessage = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource", strLocale, "emxProgramCentral.MBEs.NOTCONNECTIONMBE.CheckAlert");			
				mapResult.put("Message", strMessage);
				// mapResult.put(CHiPSConstants.Message, "Cannot Connect Abstract MBE with the selected Data");
				return mapResult;
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	private double getAdditionalQuantity(Context context,
			ArrayList<String> arrayListMBEOIDs, MapList mapListFilterMBEs,DomainObject domOBjAbsMBE)
					throws FrameworkException {
		try
		{
			StringList strListAbsMBEInfo = new StringList(2);
			strListAbsMBEInfo.add(DomainConstants.SELECT_ID);
			strListAbsMBEInfo.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
			Map<String,String> mapAbsMBEData = domOBjAbsMBE.getInfo(context, strListAbsMBEInfo);
			String strAbsMBEOID = mapAbsMBEData.get(DomainConstants.SELECT_ID);
			String strSequenceOrder = mapAbsMBEData.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
			Map<String, String> mapData;
			Iterator<Map<String,String>> iterator = mapListFilterMBEs.iterator();
			double doubleRemovalQuantity =0d;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strMBEID = mapData.get(DomainConstants.SELECT_ID);
				if(arrayListMBEOIDs.contains(strMBEID))
				{
					String strMBEItemRelID= mapData.get(DomainRelationship.SELECT_ID);
					String strQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY+"].value");
					doubleRemovalQuantity+= WMSUtil_mxJPO.convertToDouble(strQuantity);
					HashMap<String,String> hashMapAttributes     = new HashMap<String, String>();
					hashMapAttributes.put(ATTRIBUTE_WMS_ABS_MBE_OID, strAbsMBEOID);
					hashMapAttributes.put(ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER,strSequenceOrder);
					
	            ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try
					{
					DomainRelationship.setAttributeValues(context, strMBEItemRelID, hashMapAttributes);
				}
					catch(Exception exception)
					
					{
						
					}
					finally
					{
						ContextUtil.popContext(context);
					}
				}
			}
			return doubleRemovalQuantity;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/**
     * This method is used to show Technical Deduction in current Bill on particulars
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @returns Vector containing column data.
     * @throws Exception if the operation fails
     */
    public Vector getTDInCurrentBill(Context context, String[] args) throws Exception {
    	try {
    		 
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map paramList      = (HashMap) programMap.get("paramList");
			
			MapList objectList = (MapList) programMap.get("objectList");
			String strAbsId = (String)paramList.get("parentOID");
			StringList strListBusSelects = new StringList();
			strListBusSelects.add("attribute[WMSTechnicalDeductionAmount].value");
			strListBusSelects.add("to[WMSItemTechnicalDeduction].from.id");
			Map<String, String> mapTechTask = new HashMap<>();
			int intSize = objectList.size();
			Vector vecResponse = new Vector(intSize);
			if(UIUtil.isNotNullAndNotEmpty(strAbsId)) {
				DomainObject domAbs = DomainObject.newInstance(context, strAbsId);
				MapList mlTech = domAbs.getRelatedObjects(context, // matrix context
		                "WMSAbstractMBETechnicalDeduction", // relationship pattern
		                "WMSTechnicalDeduction", // type pattern
		                strListBusSelects, // object selects
		                new StringList(DomainRelationship.SELECT_ID), // relationship selects
		                false, // to direction
		                true, // from direction
		                (short) 1, // recursion level
		                DomainConstants.EMPTY_STRING, // object where clause
		                DomainConstants.EMPTY_STRING, // relationship where clause
		                0);
				if(mlTech.size()>0) {
					String strDeductionAmount="";
					float fstrQty=0.0F;
					float fstrTotal=0.0F;
					Iterator<Map<String, String>> iteratorMap = mlTech.iterator();
					Map<String, String> mapObjectData = new HashMap<String, String>();
					while (iteratorMap.hasNext()) {
						mapObjectData = iteratorMap.next();
						strDeductionAmount = mapObjectData.get("attribute[WMSTechnicalDeductionAmount].value");
						fstrQty = Float.valueOf(strDeductionAmount);
						fstrTotal+=fstrQty;
						strDeductionAmount=String.valueOf(fstrTotal);
						String strTaskId = mapObjectData.get("to[WMSItemTechnicalDeduction].from.id");
						if(UIUtil.isNotNullAndNotEmpty(strDeductionAmount)) {
							strDeductionAmount = "-"+strDeductionAmount;
							mapTechTask.put(strTaskId, strDeductionAmount);
						}
					}
				}
			}
			
			Iterator<Map<String, String>> objectListIterator = objectList.iterator();
			Map<String, String> mapObjecListData = new HashMap<String, String>();
			while(objectListIterator.hasNext()) {
				mapObjecListData = objectListIterator.next();
				String strTaskId = mapObjecListData.get("id");
				String strMapDeductionAmount = mapTechTask.get(strTaskId);
				if(UIUtil.isNotNullAndNotEmpty(strMapDeductionAmount)) {
					vecResponse.add(strMapDeductionAmount);
				}else {
					vecResponse.add("0.0");
				}
			}
			 return vecResponse;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    	
    }
    /**
     * This method is used to show Technical Deduction Release in current Bill on particulars
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @returns Vector containing column data.
     * @throws Exception if the operation fails
     */
    public Vector getTDReleaseInCurrentBill(Context context, String[] args) throws Exception {
    	try {
   		 
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map paramList      = (HashMap) programMap.get("paramList");
			
			MapList objectList = (MapList) programMap.get("objectList");
			String strAbsId = (String)paramList.get("parentOID");
			StringList strListBusSelects = new StringList();
			//strListBusSelects.add("attribute[CHiPSTechnicalDeductionAmount].value");
			strListBusSelects.add("to[WMSItemTechnicalDeduction].from.id");
			StringList strListRelSelects = new StringList();
			strListRelSelects.add("attribute[WMSTechnicalDeductionReleaseCurrentBill].value");
			Map<String, String> mapTechTask = new HashMap<>();
			int intSize = objectList.size();
			Vector vecResponse = new Vector(intSize);
			if(UIUtil.isNotNullAndNotEmpty(strAbsId)) {
				DomainObject domAbs = DomainObject.newInstance(context, strAbsId);
				MapList mlTech = domAbs.getRelatedObjects(context, // matrix context
		                "WMSAbstractMBETechnicalDeductionRelease", // relationship pattern
		                "WMSTechnicalDeduction", // type pattern
		                strListBusSelects, // object selects
		                strListRelSelects, // relationship selects
		                false, // to direction
		                true, // from direction
		                (short) 1, // recursion level
		                DomainConstants.EMPTY_STRING, // object where clause
		                DomainConstants.EMPTY_STRING, // relationship where clause
		                0);
				if(mlTech.size()>0) {
					Iterator<Map<String, String>> iteratorMap = mlTech.iterator();
					Map<String, String> mapObjectData = new HashMap<String, String>();
					while (iteratorMap.hasNext()) {
						mapObjectData = iteratorMap.next();
						String strDeductionReleaseAmount = mapObjectData.get("attribute[WMSTechnicalDeductionReleaseCurrentBill].value");
						String strTaskId = mapObjectData.get("to[WMSItemTechnicalDeduction].from.id");
						if(UIUtil.isNotNullAndNotEmpty(strDeductionReleaseAmount)) {
							mapTechTask.put(strTaskId, strDeductionReleaseAmount);
						}
					}
				}
			}
			
			Iterator<Map<String, String>> objectListIterator = objectList.iterator();
			Map<String, String> mapObjecListData = new HashMap<String, String>();
			while(objectListIterator.hasNext()) {
				mapObjecListData = objectListIterator.next();
				String strTaskId = mapObjecListData.get("id");
				String strMapDeductionReleaseAmount = mapTechTask.get(strTaskId);
				if(UIUtil.isNotNullAndNotEmpty(strMapDeductionReleaseAmount)) {
					
					vecResponse.add(strMapDeductionReleaseAmount);
				}else {
					vecResponse.add("0.0");
				}
			}
			 return vecResponse;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    /**
	 * Method to get the Item connected technical reductions WRT to Abstract MBE
	 * @param context the eMatrix <code>Context</code> object
	 * @param strAbstractMBEOID String value containing the Abstract MBE OID
	 * @param strItemOID String value containing the Item OID
	 * @return mapListMBEs MapList containing the MBE IDs
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getItemConnectedTechnicalReductions(Context context, String strAbstractMBEOID, String strItemOID)
			throws FrameworkException {
		try
		{
			MapList mapListFilterMBEs;
			String strBusWhere = "relationship["+RELATIONSHIP_WMS_ABSMBE_TECHNICALDEDUCTION+"].from.id=="+strAbstractMBEOID;
			DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
			StringList strListBusSelects=new StringList(1);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add("attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_QUANTITY+"]");
			StringList strListRelSelects=new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			mapListFilterMBEs = domObjItem.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ITEM_TECHNICAL_DEDUCTION, // relationship pattern
					TYPE_WMS_TECHNICAL_DEDUCTION, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					strBusWhere, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			return mapListFilterMBEs;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/**
	 * Method to get the check if the context user has access for the Reductions
	 * @mx_used On CHiPSTechnicalDeductionToolbar under technical reductions table toolbar
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return booleanAccess boolean containing if access is there or not
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean isAbstractMBEEditable(Context context, String[] args) throws Exception 
	{
		try
		{
			boolean booleanAccess =false;
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			String strItemOID = (String) programMap.get("objectId");
			String strRelID =(String) programMap.get("relId");
			if(strItemOID.equals(strAbstractMBEOID) && UIUtil.isNotNullAndNotEmpty(strRelID))
			{
				strAbstractMBEOID = getAbsMBEOIDFromRelOID(context,
						strRelID);
			}
			if(UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID))
			{
				DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbstractMBEOID);
				StringList strListSelects = new StringList(2);
				strListSelects.add(DomainConstants.SELECT_CURRENT);
				strListSelects.add("relationship["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
				Map<String,String> mapAbsMBEInfo = domObjAbsMBE.getInfo(context, strListSelects);
				String strWorkOrderID =  mapAbsMBEInfo.get("relationship["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
				String strAbsMBEState = mapAbsMBEInfo.get(DomainConstants.SELECT_CURRENT);
				boolean booleanRelatedUser = checkContextUserWOVisibility(context, strWorkOrderID);
				if(booleanRelatedUser && !(STATE_APPROVED.equals(strAbsMBEState)|| STATE_PLAN.equals(strAbsMBEState)))
				{
					booleanAccess = true;
				}
			}
			return booleanAccess;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * checks if the context user is member of the W/O
	 * @param context the eMatrix <code>Context</code> object
	 * @param strWorkOrderID String containing the WO OID
	 * @return booleanRelatedUser boolean value false if context user is not member of W/O
	 * @author CHiPS
	 * @since 418
	 **/
	private boolean checkContextUserWOVisibility(Context context, String strWorkOrderID) throws FrameworkException {
		boolean booleanRelatedUser = false;
		try
		{
			String strContextUser = context.getUser();
			ContextUtil.pushContext(context);
			MapList mapListMembers = WMSMeasurementBookItem_mxJPO.getWorkOrderAssignees(context, strWorkOrderID);
			ListIterator<Map<String,String>> membersItr = mapListMembers.listIterator();
			Map<String,String> memberMap;
			while(membersItr.hasNext())
			{
				memberMap = membersItr.next();
				String strRoleUser = (String)memberMap.get(Person.SELECT_NAME);
				if(strContextUser.equals(strRoleUser))
				{
					booleanRelatedUser = true;
					break;
				}
			}
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
		}
		finally
		{
			ContextUtil.popContext(context);
		}
		return booleanRelatedUser;
	}
	/**
	 * Method to restrict the visibility of add/edit/remove buttons of technical deduction if ABS MBE is not in create state when context user is Sub-Engineer 
	 * @mx_used On CHiPSTechnicalDeductionToolbar under technical reductions table toolbar
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return booleanAccess boolean containing if access is there or not
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean toMakeTechnicalDeductionToolbarActionsVisible(Context context, String[] args) throws Exception 
	{
		try
		{
			boolean booleanAccess =false;
			String strContextUser = context.getUser();
			String userContextUserID           = PersonUtil.getPersonObjectID(context, strContextUser);
            
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			
			/*DomainObject domObjContextUser = DomainObject.newInstance(context, userContextUserID);
			DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbstractMBEOID);
			*/
			StringList strListSelects = new StringList(2);
			strListSelects.add(DomainConstants.SELECT_CURRENT);
			//strListSelects.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
			
			
			String[] objectIds = {userContextUserID,strAbstractMBEOID};
			MapList membersList = DomainObject.getInfo(context, objectIds,strListSelects);
			
			String strAbsMBEStateMap = DomainConstants.EMPTY_STRING;
			String strAbsMBEState = DomainConstants.EMPTY_STRING;
			String strContextUserRoleMap = DomainConstants.EMPTY_STRING;
			String strContextUserRole = DomainConstants.EMPTY_STRING;
			
			if(membersList.size()>0)
			{
				Iterator<Map<String,String>> iterator  = membersList.iterator();
				Map<String,String> mapData;
			
				while(iterator.hasNext())
				{
					mapData =  iterator.next();
					strAbsMBEStateMap = mapData.get(DomainConstants.SELECT_CURRENT);
					//strContextUserRoleMap = mapData.get("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
					
					//if("Sub Engineer".equals(strContextUserRoleMap)){
					//	strContextUserRole = strContextUserRoleMap;
					//}
					if("Create".equals(strAbsMBEStateMap)){
						strAbsMBEState = strAbsMBEStateMap;
					}
					
				}
			}
			if("Create".equals(strAbsMBEState))//&&"Sub Engineer".equals(strContextUserRole))
			{
				booleanAccess = true;
			}
			else
			{
				booleanAccess = false;
			}
			return booleanAccess;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/** 
	 * Method will create technical deduction
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps for the table
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */

	public Map<String,String> createTechnicalDeduction(Context context, String[] args) throws Exception 
	{
		Map<String,String> mapResult = new HashMap(2);
		mapResult.put("Result", DomainConstants.EMPTY_STRING);
		mapResult.put("Message", DomainConstants.EMPTY_STRING);
		try 
		{
			HashMap programMap      = (HashMap) JPO.unpackArgs(args);
			String strItemOID = (String)programMap.get("objectId");
			String strAbsMBEOID = (String)programMap.get("parentOID");
			String strRelOID = (String)programMap.get("relId");
			if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strAbsMBEOID)&&UIUtil.isNotNullAndNotEmpty(strRelOID))
			{
				MapList mapListTechnicalDeductions = getItemConnectedTechnicalReductions(context, strAbsMBEOID, strItemOID, strRelOID);
				String strTechnicalDeductionOID  = FrameworkUtil.autoName(context,
						"type_WMSTechnicalDeduction",
						"policy_WMSTechnicalDeduction");
				if(UIUtil.isNotNullAndNotEmpty(strTechnicalDeductionOID))
				{
					DomainObject domObjTechnicalDeduction = DomainObject.newInstance(context, strTechnicalDeductionOID);
					DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbsMBEOID);
					DomainObject domObjItem = DomainObject.newInstance(context, strItemOID);
					String strBilledQuanity = DomainRelationship.getAttributeValue(context, strRelOID, ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY);
					if(UIUtil.isNullOrEmpty(strBilledQuanity))
					{
						strBilledQuanity="0.00";
					}
					double doubleRemainigQty = getTechnicalDeductionsQuantity(context, strBilledQuanity,
							mapListTechnicalDeductions);
					if(doubleRemainigQty>0)
					{
						setTechnicalDeductionAttribute(context, domObjTechnicalDeduction, domObjItem, doubleRemainigQty,strBilledQuanity);
						try
						{
						ContextUtil.pushContext(context);//, CHiPSDomainConstant.PERSON_USER_AGENT,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

						DomainRelationship.connect(context, domObjAbsMBE, RELATIONSHIP_WMS_ABSMBE_TECHNICALDEDUCTION, domObjTechnicalDeduction);
						}
						finally
						{
							ContextUtil.popContext(context);
						}
						String strNewRelOID = connectItemAndTechnicalDeduction(context, domObjItem, domObjTechnicalDeduction);
						if(UIUtil.isNotNullAndNotEmpty(strNewRelOID))
						{
							StringBuffer strBuffer = new StringBuffer();
							strBuffer.append("<mxRoot>");
							strBuffer.append("<action><![CDATA[add]]></action>");
							strBuffer.append("<data status=\"committed\">");
							strBuffer.append("<item oid=\""+strTechnicalDeductionOID+"\" relId=\""+strNewRelOID+"\"   direction=\"\" />");
							strBuffer.append("</data>");
							strBuffer.append("</mxRoot>");
							mapResult.put("Result",strBuffer.toString());
						}
						else
						{
							mapResult.put("Message", "An error occured while connecting technical deduction and Item");;
						}
					}
					else
					{
						String strTechnicalDeductionQuantityWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.ExecedingBillQuantity");
						mapResult.put("Message", strTechnicalDeductionQuantityWarning);
					}
				}
				else
				{
					mapResult.put("Message",  "An error occured while creating technical deduction");
				}
			}
			else
			{
				mapResult.put("Message",  "Couldn't process data");
			}

		} catch (Exception exception) {
			exception.printStackTrace();
			mapResult.put("Message", "An error occured while creating technical deduction");;
		}
		return mapResult;
	}
	/**
	 * Method to get the Item connected technical reductions WRT to Abstract MBE
	 * @mx_used On Abstract MBE particular technical reduction table table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListMBEs MapList containing the MBE IDs
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getItemConnectedTechnicalReductions(Context context, String[] args) throws Exception 
	{
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strAbstractMBEOID = (String) programMap.get("parentOID");
			String strItemOID = (String) programMap.get("objectId");
			String strRelID =(String) programMap.get("relId");
			if(strItemOID.equals(strAbstractMBEOID) && UIUtil.isNotNullAndNotEmpty(strRelID))
			{
				strAbstractMBEOID = getAbsMBEOIDFromRelOID(context,
						strRelID);
			}
			MapList mapListFilterMBEs = getItemConnectedTechnicalReductions(context, strAbstractMBEOID, strItemOID,
					strRelID);
			return mapListFilterMBEs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/**
	 * Method to get the Item connected technical reductions WRT to Abstract MBE
	 * @param context the eMatrix <code>Context</code> object
	 * @param strAbstractMBEOID String value containing the Abstract MBE OID
	 * @param strItemOID String value containing the Item OID
	 * @param strRelID String value containing the relationship ID between Item and Abstract MBE
	 * @return mapListMBEs MapList containing the MBE IDs
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getItemConnectedTechnicalReductions(Context context, String strAbstractMBEOID, String strItemOID,
			String strRelID) throws FrameworkException {
		try
		{
			MapList mapListFilterMBEs = new MapList();
			if(UIUtil.isNotNullAndNotEmpty(strItemOID)&& UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID)&&!strItemOID.equals(strAbstractMBEOID))
			{
				String strBilledQuanity = DomainRelationship.getAttributeValue(context, strRelID, ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY);

				mapListFilterMBEs = getItemConnectedTechnicalReductions(context, strAbstractMBEOID, strItemOID);
				WMSMeasurementBookEntry_mxJPO.insertKeyValue(mapListFilterMBEs, ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY, strBilledQuanity);
			}
			return mapListFilterMBEs;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/** 
	 * Method will overallDeduction quantity
	 * @param context the eMatrix <code>Context</code> object
	 * @param strBilledQuanity String value containing the Quantity billed against the item
	 * @param mapListTechnicalDeductions MapList containing Technical Deductions
	 * @return doubleTDQuantity double value containing the overallDeduction quantity
	 * @author CHiPS
	 * @since 418
	 */
	private double getTechnicalDeductionsQuantity(Context context, String strBilledQuanity, MapList mapListTechnicalDeductions)
	{
		double doubleBilledQuantity = WMSUtil_mxJPO.convertToDouble(strBilledQuanity);
		double doubleTDQuantity = 0d;
		Map<String,String> mapData ;
		Iterator<Map<String,String>> iterator = mapListTechnicalDeductions.iterator();
		while(iterator.hasNext())
		{
			mapData = iterator.next();
			String strTechnicalDeductionQuantity = mapData.get("attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_QUANTITY+"]");

			doubleTDQuantity+=WMSUtil_mxJPO.convertToDouble(strTechnicalDeductionQuantity);
		}
		double doubleRemainigQty =  doubleBilledQuantity-doubleTDQuantity;
		return doubleRemainigQty;
	}
	/** 
	 * Method set the attributes on technical deduction
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjItem String DomainObject instance of Item
	 * @param domObjTechnicalDeduction DomainObject instance of newly created technical deduction
	 * @param doubleRemainigQty double value containing the Quantity
	 * @param strBilledQuanity String value containing the billed quantity
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private void setTechnicalDeductionAttribute(Context context, DomainObject domObjTechnicalDeduction,
			DomainObject domObjItem, double doubleRemainigQty,String strBilledQuanity) throws FrameworkException {
		try
		{
			Map<String,String> hashMapAttributes = new HashMap<String,String>(5);
			hashMapAttributes.put(ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_TYPE,"PayableLater");
			hashMapAttributes.put(ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_BILL_AMOUNT,strBilledQuanity);
			if(doubleRemainigQty>0)
			{
				String strRate = domObjItem.getAttributeValue(context, ATTRIBUTE_WMS_SOR_RATE);
				double doubleRate = WMSUtil_mxJPO.convertToDouble(strRate);
				double doubleAmount = doubleRate*doubleRemainigQty;
				//hashMapAttributes.put(ATTRIBUTE_TECHNICAL_DEDUCTION_AMOUNT, new BigDecimal(doubleAmount).toPlainString());
				hashMapAttributes.put(ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_QUANTITY,Double.toString(doubleRemainigQty));
				hashMapAttributes.put(ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RATE,Double.toString(doubleRate));
			}
			domObjTechnicalDeduction.setAttributeValues(context, hashMapAttributes);
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	/** 
	 * Method will connect Item and Technical Deduction
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjItem DomainObject instance of Item
	 * @param domObjTechnicalDeduction DomainObject instance of newly created technical deduction
	 * @return strRelOID String value containing the rel ID between Item and Transaction
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private String connectItemAndTechnicalDeduction(Context context, DomainObject domObjItem, DomainObject domObjTechnicalDeduction)
			throws  Exception {
		try
		{
			String strRelOID = DomainConstants.EMPTY_STRING;

			DomainRelationship domObjRel = DomainRelationship.connect(context, domObjItem, RELATIONSHIP_WMS_ITEM_TECHNICAL_DEDUCTION, domObjTechnicalDeduction);
			StringList strListRelSelects=new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			Map<String,Object> mapRelData = domObjRel.getRelationshipData(context, strListRelSelects);
			if(mapRelData!=null && !mapRelData.isEmpty())
			{
				StringList strListRelID = (StringList)mapRelData.get(DomainRelationship.SELECT_ID);
				if(strListRelID!=null && strListRelID.size() > 0)
				{
					strRelOID = (String)strListRelID.get(0);
				}
			}
			return strRelOID;
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	/***************** Technical Deduduction starts *****************************************************************************************************/
	
	
	/**
	 * Method to get the connected technical deduction under the items which are connected to Abstract MBE
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTDs MapList containing the connected technical deductions
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAbstractMBETechnicalDeductions (Context context, String[] args) throws Exception 
	{

		try
		{
			MapList mapListConnectedTDs = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjAbsMBE = DomainObject.newInstance(context, strObjectId);
				//Map<String, String> mapBillAmount = getAbsMBEConnectedItemsBillAmount(context, domObjAbsMBE);
				mapListConnectedTDs = getAbstractMBETechnicalDeductions(context, domObjAbsMBE);
				//putTDConnectedItemsBillAmount(mapListConnectedTDs, mapBillAmount);
			}
			return mapListConnectedTDs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}

	}
	
	
	/**
	 * Method to get the Technical Deduction connected to Items WRT to abstract
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbsMBE DomainObject instance of context Abstract MBE
	 * @return mapListTDs MapList containing the Technical Deduction
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getAbstractMBETechnicalDeductions(Context context, DomainObject domObjAbsMBE)
			throws FrameworkException {
		try
		{
			StringList strListBusSelects     = new StringList(2);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add("relationship["+RELATIONSHIP_WMS_ITEM_TECHNICAL_DEDUCTION+"].from.id");
			StringList strListRelSelects     = new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			MapList mapListTDs = domObjAbsMBE.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ABSMBE_TECHNICALDEDUCTION, // relationship pattern
					TYPE_WMS_TECHNICAL_DEDUCTION, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			return mapListTDs;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	
	
	/**
	 * Method to get the Technical Deduction released against the context bill
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedReleasedTDs MapList containing the released technical deductions against the bill
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAbstractMBEReleasedTechnicalDeductions (Context context, String[] args) throws Exception 
	{
		try
		{
			MapList mapListConnectedReleasedTDs = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjAbsMBE = DomainObject.newInstance(context, strObjectId);
				mapListConnectedReleasedTDs = getAbstractMBEReleasedTechnicalDeductions(context, domObjAbsMBE);
			}
			return mapListConnectedReleasedTDs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	
	/**
	 * Method to get the Technical Deduction released against the context bill
	 * @param context the eMatrix <code>Context</code> object
	 * @param domObjAbsMBE DomainObject instance of context Abstract MBE
	 * @return mapListReleasedTDs MapList containing the released Technical Deduction
	 * @throws FrameworkException if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	private MapList getAbstractMBEReleasedTechnicalDeductions(Context context, DomainObject domObjAbsMBE)
			throws FrameworkException {
		try
		{
			StringList strListBusSelects     = new StringList(2);
			strListBusSelects.add(DomainConstants.SELECT_ID);
			strListBusSelects.add("to["+RELATIONSHIP_WMS_ITEM_TECHNICAL_DEDUCTION+"].from.id");
			strListBusSelects.add("attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT+"]");
			StringList strListRelSelects     = new StringList(2);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			strListRelSelects.add("attribute["+ATTRIBUTE_WMS_CURRENT_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT+"]");
			MapList mapListReleasedTDs = domObjAbsMBE.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ABSMBE_TECHNICAL_DEDUCTION_RELEASE, // relationship pattern
					TYPE_WMS_TECHNICAL_DEDUCTION, // type pattern
					strListBusSelects, // object selects
					strListRelSelects, // relationship selects
					false, // to direction
					true, // from direction
					(short) 1, // recursion level
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
			return mapListReleasedTDs;
		}
		catch(FrameworkException frameworkException)
		{
			frameworkException.printStackTrace();
			throw frameworkException;
		}
	}
	
	/**
	 * Method to get the technical deduction which are eligible for release in the current bill
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command
	 * @return mapListConnectedTDs MapList containing the connected technical deductions
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getEligibleTechnicalDeductions (Context context, String[] args) throws Exception 
	{

		try
		{
			MapList mapListConnectedTDs = new MapList();
			String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObjAbstractMBE = DomainObject.newInstance(context, strObjectId);

				String strWorkOrderOID	 	= domObjAbstractMBE.getInfo(context, "relationship["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id"); 
				if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID))
				{
					MapList mapListCUrrentBuillTDs = getAbstractMBETechnicalDeductions(context, domObjAbstractMBE);
					StringList strListCurrentBillTDs = WMSUtil_mxJPO.convertToStringList(mapListCUrrentBuillTDs, DomainConstants.SELECT_ID);

					String strWhere = "(attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_TYPE+"].value==\"PayableLater\")&&(attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_AMOUNT+"] > attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT+"])";
					MapList mapListTDs = getWorkOrderConnectedTechnicalDeductions(context, strWorkOrderOID, strWhere);
					mapListConnectedTDs = getEligibleTechnicalDeductions(strListCurrentBillTDs, mapListTDs);

				}
			}
			return mapListConnectedTDs;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
 



/** 
 * Method will get all deductions connected to the WO
 * @param context the eMatrix <code>Context</code> object
 * @param strWorkOrderOID String value containing the work order connected to the Abstract MBE
 * @param strWhere String value containing the BUS filter for technical deductions
 * @return mapListTDs MapList containing the Technical deductions connected to Abstract MBE through MBE
 * @throws FrameworkException if the operation fails
 * @author CHiPS
 * @since 418
 */
private MapList getWorkOrderConnectedTechnicalDeductions(Context context, String strWorkOrderOID, String strWhere)
		throws FrameworkException {
	try
	{
		DomainObject domObjWO = DomainObject.newInstance(context, strWorkOrderOID);
		StringList strListBusSelects     = new StringList(1);
		strListBusSelects.add(DomainConstants.SELECT_ID);
		StringList strListRelSelects     = new StringList(1);
		strListRelSelects.add(DomainRelationship.SELECT_ID);
		MapList mapListTDs = domObjWO.getRelatedObjects(context, // matrix context
				RELATIONSHIP_WMS_WO_TECHNICAL_DEDUCTION, // relationship pattern
				TYPE_WMS_TECHNICAL_DEDUCTION, // type pattern
				strListBusSelects, // object selects
				strListRelSelects, // relationship selects
				false, // to direction
				true, // from direction
				(short) 1, // recursion level
				strWhere, // object where clause
				DomainConstants.EMPTY_STRING, // relationship where clause
				0);
		return mapListTDs;
	}
	catch(FrameworkException frameworkException)
	{
		frameworkException.printStackTrace();
		throw frameworkException;
	}
}

/** 
 * Method will get all technical deductions eligible for release
 * @param strListCurrentBillTDs StringList containing the current bill technical deductions.These shouldn't be displayed
 * @param mapListWOTDs MapList<Map<String,String>> containing the technical deduction which are connected to the WO of current bill
 * @return mapListTDs MapList containing the Technical deductions eligible for release
 * @throws FrameworkException if the operation fails
 * @author CHiPS
 * @since 418
 */
private MapList getEligibleTechnicalDeductions(StringList strListCurrentBillTDs, MapList mapListWOTDs) {
	MapList mapListConnectedTDs = new MapList();
	Iterator<Map<String,String>> iterator = mapListWOTDs.iterator();
	Map<String,String> mapData;
	while(iterator.hasNext())
	{
		mapData = (Map<String,String>)iterator.next();
		String strOID = mapData.get(DomainConstants.SELECT_ID);
		if(!strListCurrentBillTDs.contains(strOID))
		{
			mapListConnectedTDs.add(mapData);
		}
	}
	return mapListConnectedTDs;
}


/** 
 * Method will sets the release amount on technical deduction as till date reduction on relationship
 * 
 * @param context the eMatrix <code>Context</code> object    
 * @throws Exception if the operation fails
 * @author CHiPS
 * @since 418
 */
public void setTillDateTechnicalDeductionReleaseAmount(Context context, String[] args) throws Exception 
{
    try 
    {
        String strTDOID = args[0];
        String srrRELOID = args[1];
        if(UIUtil.isNotNullAndNotEmpty(strTDOID)&&UIUtil.isNotNullAndNotEmpty(srrRELOID))
        {
        	DomainObject domObjTD = DomainObject.newInstance(context, strTDOID);
        	String strValue = domObjTD.getAttributeValue(context, ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT);
        	if(UIUtil.isNullOrEmpty(strValue))
        	{
        		strValue = "0.0";
        	}
        	Map<String,String> mapAttributeMap = new HashMap<String,String>(2);
        	mapAttributeMap.put(ATTRIBUTE_WMS_PREVIOUS_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT, strValue);
        	mapAttributeMap.put(ATTRIBUTE_WMS_CURRENT_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT, strValue);
        	DomainRelationship.setAttributeValues(context, srrRELOID, mapAttributeMap);
        }
    }
    catch(Exception exception)
    {
    	System.out.println("An Exception while connected Technical deduction to Current Bill as release");
        exception.printStackTrace();
        throw exception;            
    }
}

/**  Trigger on ABSMBE Policy state Create Action , send notification to Sector Manager/Project in Charged 
 * 
 * @param context
 * @param args
 * @throws Exception
 */

public void sendAMBApprovalNotification(Context context,String[] args) throws Exception
{
	try {
			String strABSMBDOid = args[0];
			MailUtil.setTreeMenuName(context, "type_WMSAbstractMeasurementBookEntry");
			DomainObject domABSMBE= DomainObject.newInstance(context, strABSMBDOid);
			StringList slInfo=new StringList();
			slInfo.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.id");
			slInfo.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
		 	Map mInfo = domABSMBE.getInfo(context, slInfo);
			String strProjectId  =(String) mInfo.get("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.id");
			String strABSMBETitle=(String) mInfo.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
			
			ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,
			DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
			project.setId(strProjectId);
			StringList busSelects = new StringList(2);
			busSelects.add(DomainConstants.SELECT_NAME);
			StringList relSelects = new StringList(2);
			relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
			 
		    StringBuilder sbRelWhere = new StringBuilder();
		    sbRelWhere.append( "attribute[").append(DomainConstants.ATTRIBUTE_PROJECT_ROLE).append("]=='ProjectInCharge'").append(" || ");
		    sbRelWhere.append( "attribute[").append(DomainConstants.ATTRIBUTE_PROJECT_ROLE).append("]=='SectionManager'");
		  	MapList membersList = project.getMembers(context, busSelects, relSelects, null, sbRelWhere.toString());
			StringList slToPersonList = new StringList();
			Iterator<Map> itr = membersList.iterator();
			while(itr.hasNext()) {
				slToPersonList.add((String)((Map) itr.next()).get(DomainConstants.SELECT_NAME));
		 	}
			
			if(slToPersonList.size()>0) {
				  StringBuilder sbSubject = new StringBuilder();
		          sbSubject.append("Measurement Bill Book : ").append(strABSMBETitle).append(" has been submitted");
				  StringBuilder sbMessage = new StringBuilder();
			      sbMessage.append(" Measurement Bill Book  ");
		          sbMessage.append(strABSMBETitle);
				  sbMessage.append( "has been submitted :-  ");
				  StringList slObjectIdList = new StringList(1);
		          slObjectIdList.addElement(strABSMBDOid);
		          MailUtil.sendMessage(context,slToPersonList,null,null,sbSubject.toString(),sbMessage.toString(),slObjectIdList);
				
			}
		 
	}catch(Exception e) {
		e.printStackTrace();
	}






 }
/** 
 * Method will connect technical deduction to Work Order
 * @param context the eMatrix <code>Context</code> object
 * @param args Packed program and request maps for the table
 * @throws Exception if the operation fails
 * @author CHiPS
 * @since 418
 */
public void connectTechnicalDeduction(Context context, String[] args) throws Exception 
{
	try
	{
		String strAbsMBEOID = args[0];
		if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOID))
		{
			DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbsMBEOID);
			MapList mapListMBEs = getAbstractMBETechnicalDeductions(context, domObjAbsMBE);
			StringList strListTechnicalDeductionsOIDs = WMSUtil_mxJPO.convertToStringList(mapListMBEs, DomainConstants.SELECT_ID);
			String sWorkOrderOid = domObjAbsMBE.getInfo(context, "to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
			DomainObject domObjWO = DomainObject.newInstance(context, sWorkOrderOid);
			ArrayList<String> arrayListOIDs = new ArrayList<String>(strListTechnicalDeductionsOIDs.size());
			arrayListOIDs.addAll(strListTechnicalDeductionsOIDs);
			try
			{
				ContextUtil.pushContext(context);//, CHiPSDomainConstant.PERSON_USER_AGENT,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				WMSUtil_mxJPO.connect(context, domObjWO, RELATIONSHIP_WMS_WO_TECHNICAL_DEDUCTION, true, arrayListOIDs);
			}
			finally
			{
				ContextUtil.popContext(context);
			}
		}
	}
	catch(Exception exception)
	{
		exception.printStackTrace();
		throw exception;
	}
}
/**
 * Method add the Item to AbstractMBE
 * @param context the eMatrix <code>Context</code> object
 * @param args Packed program and request maps from the command
 * @return mapListConnectedTasks MapList containing the Task IDs
 * @throws Exception if the operation fails
 * @author CHiPS
 * @since 418
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public Map<String, String> addTechnicalReductionRelease(Context context, String[] args) throws Exception {
	Map<String, String> mapResult = new HashMap<String, String>();
	try 
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
		String strAbstractMBEOID = (String) programMap.get("parentOID");
		if( UIUtil.isNotNullAndNotEmpty(strAbstractMBEOID))
		{
			Map<String,String> mParsedRowId ;
			ArrayList<String> arrayListOIDs = new ArrayList();
			int intLength = emxTableRowId.length;
			for (int i = 0; i < intLength; i++) 
			{
				mParsedRowId = ProgramCentralUtil.parseTableRowId(context,emxTableRowId[i]);
				String strSelectedOid = (String) mParsedRowId.get("objectId");
				arrayListOIDs.add(strSelectedOid);
			}
			DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbstractMBEOID);
			WMSUtil_mxJPO.connect(context, domObjAbsMBE, RELATIONSHIP_WMS_ABSMBE_TECHNICAL_DEDUCTION_RELEASE, true, arrayListOIDs);
		}
		return mapResult;
	} catch (Exception exception) {
		System.out.println("Exception in adding a technical deduction as a release in particulars under Abstract MBE");
		exception.printStackTrace();
		throw exception;
	}
}
/** 
 * Method will update the technical release amount on the released technical deductions in the current bill
 * @param context the eMatrix <code>Context</code> object
 * @param args Packed program and request maps for the table
 * @throws Exception if the operation fails
 * @author CHiPS
 * @since 418
 */
public void updateTechnicalDeductionReleaseAmount(Context context, String[] args) throws Exception 
{
	try
	{
		String strAbsMBEOID = args[0];
		if(UIUtil.isNotNullAndNotEmpty(strAbsMBEOID))
		{
			DomainObject domObjAbsMBE = DomainObject.newInstance(context, strAbsMBEOID);
			MapList mapListMBEs = getAbstractMBEReleasedTechnicalDeductions(context, domObjAbsMBE);
			Iterator<Map<String,String>> iterator = mapListMBEs.iterator();
			Map<String,String> mapData ;
			while(iterator.hasNext())
			{
				mapData = iterator.next();
				String strTDOID = mapData.get(DomainConstants.SELECT_ID);
				String strReleasedAmount = mapData.get("attribute["+ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT+"]");
				String strCurrentBillReleasedAmount = mapData.get("attribute["+ATTRIBUTE_WMS_CURRENT_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT+"]");
				double doubleReleasedAmount = WMSUtil_mxJPO.convertToDouble(strReleasedAmount);
				double doubleCurrentBillReleasedAmount = WMSUtil_mxJPO.convertToDouble(strCurrentBillReleasedAmount);
				doubleReleasedAmount+=doubleCurrentBillReleasedAmount;
				DomainObject domObjTD = DomainObject.newInstance(context, strTDOID);
				//TO HANDLE BIG CALCULATION : EXPONENTIAL ISSUE
				domObjTD.setAttributeValue(context, ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT, new BigDecimal(doubleReleasedAmount).toPlainString());
			}
		}
	}
	catch(Exception exception)
	{
		exception.printStackTrace();
		throw exception;
	}
}
/**
 * Function to get Items Abstract MBE(Submitted)
 *
 * @param context the eMatrix <code>Context</code> object
 * @param domObjItem DomainObject instance of selected Item
 * @return mapListTasks MapList containing the MBEs connected to Work Order with ID
 * @author WMS
 * @throws Exception 
 * @since 418
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getSubmittedOnAbstractMBEs(Context context, String args[]) throws Exception {    		
	MapList mapListMBEs = new MapList();
	try
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strItemID             = (String) programMap.get("objectID");

		if(strItemID==null ||  "".equals(strItemID)|| "null".equals(strItemID))
		{
			strItemID             = (String) programMap.get("objectId");
		}

		if(strItemID !=null &&  !"".equals(strItemID) && !"null".equals(strItemID))
		{
			String strwhere = "("+DomainConstants.SELECT_CURRENT+"==Approved"+")||("+DomainConstants.SELECT_CURRENT+"==Paid)";
			SelectList selListBusSelects     = new SelectList(1);
			selListBusSelects.add(DomainConstants.SELECT_ID);
			SelectList selListRelSelects     = new SelectList(1);
			selListRelSelects.add(DomainRelationship.SELECT_ID);				
			DomainObject domObjItem 		= DomainObject.newInstance(context,strItemID);
			mapListMBEs 					= domObjItem.getRelatedObjects(context, // matrix context
					RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS, // relationship pattern
					TYPE_ABSTRACT_MBE, // type pattern
					selListBusSelects, // object selects
					selListRelSelects, // relationship selects
					true, // to direction
					false, // from direction
					(short) 1, // recursion level
					strwhere, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);
		}			
	}
	catch(Exception exception)
	{
		exception.printStackTrace();
		throw exception;
	}		
	return mapListMBEs;
}
/**
 *  Method gets call from AMB Particualr Quantity column power view show command in Create state only
 * 
 * @param context
 * @param args
 * @return
 */

public boolean showInCreate(Context context,String[] args) {
	try {
		
		 Map input = JPO.unpackArgs(args);
		 String strRelId= (String)input.get("relId");
		 String strObjId=DomainConstants.EMPTY_STRING;
		 if (strRelId != null) {
				DomainRelationship domrelPCBId = new DomainRelationship(
						strRelId);
				StringList sList = new StringList();
				sList.add(DomainRelationship.SELECT_FROM_ID);
				String[] arr = new String[1];
				arr[0] = strRelId;
				MapList objMapList = DomainRelationship.getInfo(
						context, arr, sList);
				// To get the PC Id
				for (int j = 0; j < objMapList.size(); j++) {
					Map objFLMap = (Map) objMapList.get(j);
					strObjId = (String) objFLMap
							.get(DomainRelationship.SELECT_FROM_ID);
				}
 	      DomainObject domAMB= DomainObject.newInstance(context, strObjId);
		 String strCurrent = domAMB.getInfo(context, DomainConstants.SELECT_CURRENT);
		   if(strCurrent.equalsIgnoreCase("Create")) 
			   return true;
		 }
	   }catch(Exception e) {
		e.printStackTrace();
	}
	
	
	return false;

   }

/******************************************************************* Added for B3 Abstract Bill document***********************************************************************/

/** Method to get connected Abstract Bills documents 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
@ProgramCallable
public MapList getAbstractBillsDocuments(Context context,String[] args) throws Exception{
	MapList mlBillDocuments = new MapList();
		
	try {
		  Map inputMap = JPO.unpackArgs(args);
		  String strABsBillId = (String)inputMap.get("objectId");	
		  DomainObject domBills= DomainObject.newInstance(context,strABsBillId);
		  StringList slObjectSelects = new StringList();
		  slObjectSelects.add("format.file.name");
		  slObjectSelects.add(DomainConstants.SELECT_ID);
		  slObjectSelects.add(DomainConstants.SELECT_NAME);
		  StringList slRelSelects = new StringList();
		  mlBillDocuments = domBills.getRelatedObjects(context,
				  RELATIONSHIP_WMS_ABSTRACT_BILL_DOCUMENTS,
				  TYPE_DELIVERABLE2,
				  slObjectSelects,
				  slRelSelects,
				  false,// get to 
				  true, // get from 
				  (short) 1, // recursion level
				  DomainConstants.EMPTY_STRING, // object where clause
				  DomainConstants.EMPTY_STRING, // relationship where clause
				  0);
	
		}catch(Exception e) {
			e.printStackTrace();
        }	
	
	
	return mlBillDocuments;
	
  }

/**
 *  Trigger which creates 
 * 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public int triggerAutoCreateAbstractBillsDocuments(Context context,String[] args) throws Exception
{
  try {
	  
       String strAbsId = args[0];
	   String strDocLists  = EnoviaResourceBundle.getProperty(context, "WMS.AbstractBills.ContractorDocuments.List");
	   DomainObject domAbsBill= DomainObject.newInstance(context, strAbsId);
	   DomainObject domDummy= DomainObject.newInstance(context);
	   String[] arrDocs = strDocLists.split(",");
	   String strAutorevision=DomainObject.EMPTY_STRING;
	   for(String strName:arrDocs) {
  	    strAutorevision  = domDummy.getUniqueName("10");
  	    DomainRelationship domWOSDP = domDummy.createAndConnect(context, 
																	TYPE_DELIVERABLE2, 
																	strName, 
																	strAutorevision,
																	DomainConstants.POLICY_DELIVERABLE, context.getVault().getName(),
																	RELATIONSHIP_WMS_ABSTRACT_BILL_DOCUMENTS,
																	domAbsBill,
																	true);
  	   BusinessObject busObject =  domWOSDP.getTo();
  	   busObject.promote(context); // move to  Active state .. so document can be checkedIn
	  
	   }
      }catch(Exception e) {
	  
	  e.printStackTrace();
  }	

return 0;
}
/** Column tells whether document is uploaded or not 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public Vector isDocumentUploaded(Context context,String[] args) throws Exception {
 
	try {
		 Map programMap= JPO.unpackArgs(args);
		 MapList objectList = (MapList)programMap.get("objectList");
			int intSize = objectList.size();
			Vector<String> vecResponse = new Vector<String>(intSize);
			Iterator<Map<String,String>> iterator  = objectList.iterator();
			Map<String,String> mapData ;
			String strIsFileUploaded= DomainConstants.EMPTY_STRING;
			while(iterator.hasNext()) {
				Map m= iterator.next();
				strIsFileUploaded=(String)m.get("format.file.name");
				if(strIsFileUploaded!=null  && !strIsFileUploaded.isEmpty()) {
					vecResponse.add("Yes");
				}else {
					vecResponse.add("No");
				}
			}
			  triggerSendDocumentListNotification(context,args);
		 return vecResponse;
		
    	}catch(Exception e) {
		 e.printStackTrace();
		  throw e;
	  }
    }


/** Trigger method which sends email to Approver listing document against which document is uploaded /Non Uploaded
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public int triggerSendDocumentListNotification(Context context, String[] args) throws Exception{
	
 try {
	  String strAbsId = "44816.63426.20144.44658" ;//args[0];
	   DomainObject domAbs = DomainObject.newInstance(context,strAbsId);
	   Map mPackArgs= new HashMap();
	   mPackArgs.put("objectId", strAbsId);
	   StringList slSelect= new StringList(3);
	   slSelect.add(DomainConstants.SELECT_NAME);
	   slSelect.add("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
	   slSelect.add("from["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].to.attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
	   MapList mlDocs =  getAbstractBillsDocuments(context,JPO.packArgs(mPackArgs));
	   Map mInfo = domAbs.getInfo(context, slSelect);
	   String stRoute=(String)mInfo.get("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
	   String strWorkOrder= (String)mInfo.get("from["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].to.attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
	   String strBill = (String)mInfo.get(DomainConstants.SELECT_NAME); 
	   Route route = new Route(stRoute);
	   StringList slMember = new StringList(route.getRouteMembers(context));
	   Iterator<Map> itr = mlDocs.iterator();
	   StringList slNonUploadedList = new StringList();
	   StringList slUploadedList = new StringList();
	   String strIsFileUploaded=DomainConstants.EMPTY_STRING;
	   String strDocumentName=DomainConstants.EMPTY_STRING;
	   while(itr.hasNext()) {
		  Map m = itr.next(); 
			strIsFileUploaded=(String)m.get("format.file.name");
			strDocumentName  =(String)m.get(DomainConstants.SELECT_NAME);
			if(strIsFileUploaded!=null  && !strIsFileUploaded.isEmpty()) {
				slUploadedList.add(strDocumentName);
			}else {
				slNonUploadedList.add(strDocumentName);
			}
 	      }
	   
	     StringBuilder sbMsgBody= new StringBuilder();
	     String subject= "Document uploaded/not uploaded list for Bill : "+strBill+"  WorkOrder :"+strWorkOrder;
	     sbMsgBody.append("Uploaded Document list <br>");
	      for(int i=0;i<slUploadedList.size();i++) {
	    	  sbMsgBody.append(i+1).append("] ").append(slUploadedList.get(i));
	      }
 	      sbMsgBody.append("\n\n Uploaded Document list <br>");
	      for(int i=0;i<slNonUploadedList.size();i++) {
	    	  sbMsgBody.append(i+1).append("] ").append(slNonUploadedList.get(i));
	      }
	     // System.out.println( " sbMsgBody "+sbMsgBody.toString());
	      emxNotificationUtil_mxJPO.sendJavaMail(context, slMember, null, null, subject, null, sbMsgBody.toString(), context.getUser(), null, null, "both");
 	    
       }catch(Exception e) {
	    e.printStackTrace();
	 throw e;
	 
 }
	
	
	return 0;
 }


	public static StringList getAllDepartmentMembers(Context context, String strDepartmetId)throws Exception{
		StringList slAllDepartmentMembers = new StringList();
		try {
			if(UIUtil.isNotNullAndNotEmpty(strDepartmetId)) {
				StringList slSelect = new StringList(DomainObject.SELECT_ID);
				slSelect.add(DomainObject.SELECT_NAME);
				slSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList(DomainRelationship.SELECT_ID);
				DomainObject doDepartment = new DomainObject(strDepartmetId);
				MapList mlMemberList = doDepartment.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_MEMBER,DomainObject.TYPE_PERSON,slSelect,slRelSelect,false,true,(short)0,null,null);
				int iSize = mlMemberList.size();
				Map mapTemp = null;
				for (int iCount=0;iCount<iSize; iCount++)
				{
					mapTemp=(Map)mlMemberList.get(iCount);
					slAllDepartmentMembers.add((String)mapTemp.get(DomainConstants.SELECT_NAME));
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slAllDepartmentMembers;
	}
	//Code added for B3 Action-  start
	public void triggerSendMailNotificationOnThreadCreation(Context context, String args[]) {
		String strprojectId = args[0];
		String strThreadid = args[1];
		StringList slObjectSelect = new StringList();
		slObjectSelect.add(DomainObject.SELECT_ID);
		slObjectSelect.add(DomainObject.SELECT_NAME);
		slObjectSelect.add("to[Thread].from.owner");
		slObjectSelect.add("to[Thread].from.id");
		slObjectSelect.add("to[Thread].from.name");
		slObjectSelect.add("from[Message].to.id");
		HashMap hmToList = new HashMap();
		StringList objectIdLists = new StringList();
		DomainObject doObject;
		try {
			doObject = new DomainObject(strThreadid);
			Map mObjectInfo = (Map) doObject.getInfo(context, slObjectSelect);
			String strProjectOwner = (String) mObjectInfo.get("to[Thread].from.owner");
			String strProjectName = (String) mObjectInfo.get("to[Thread].from.name");
			objectIdLists.add((String)mObjectInfo.get("from[Message].to.id"));
			BusinessObject personObject = new BusinessObject("Person",strProjectOwner,"-","eService Production");
			String personId = personObject.getObjectId(context);
			DomainObject domPerson = new DomainObject(personId);
			// Do get related of person who are all reportees
			String strSubject=EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(), "WMS.ThreadCreationNotification.Subject");
			StringBuffer sbSubject = new StringBuffer();
			sbSubject.append(strSubject);
			sbSubject.append(" : ");
			sbSubject.append(strProjectName);
			String strMessage=EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(), "WMS.ThreadCreationNotification.Message");
			StringBuffer sbMessage = new StringBuffer();
			sbMessage.append(strMessage);
			sbMessage.append(" : ");
			sbMessage.append(strProjectName);
			StringList slAllDepartmentMembers = new StringList();
			MapList mapListItems = domPerson.getRelatedObjects(context, // matrix context
														"WMSReportingManager", // relationship pattern
														"Person", // type pattern
														slObjectSelect, // object selects
														null, // relationship selects
														false, // to direction
														true, // from direction
														(short) 1, // recursion level
														DomainConstants.EMPTY_STRING, // object where clause
														DomainConstants.EMPTY_STRING, // relationship where clause
														0);
		
			int iSize = mapListItems.size();
			Map mapTemp = null;
			for (int iCount=0;iCount<iSize; iCount++)
			{
				mapTemp=(Map)mapListItems.get(iCount);
				slAllDepartmentMembers.add((String)mapTemp.get(DomainConstants.SELECT_NAME));
			}
			StringList toList = new StringList(strProjectOwner);
			toList.addAll(slAllDepartmentMembers);
			HashSet<String> hsToList = new HashSet<String>(toList);
			for (String toMembers : hsToList) {
				if (hmToList.containsKey(toMembers)) {
					MapList mapListComp = (MapList) hmToList.get(toMembers);
					mapListComp.add(mObjectInfo);
					hmToList.put(toMembers, mapListComp);
				} else {
					MapList temp = new MapList();
					temp.add(mObjectInfo);
					hmToList.put(toMembers, temp);
				}
			}
			Iterator<Map.Entry<String, MapList>> itr = hmToList.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, MapList> entry = itr.next();
				String strKeyName = entry.getKey();
				MapList mlRFQs = entry.getValue();
				String strToEmail = PersonUtil.getEmail(context, strKeyName);
				String fromAgent = context.getUser();
				String notifyType = "IconMail";
				//${CLASS:emxNotificationUtil}.sendJavaMail(context, new StringList(strKeyName), null, null, "Discussion Thread Created", sbMessage.toString(), null, fromAgent, null, strMessageId, notifyType);
				emxNotificationUtil_mxJPO.sendJavaMail(context, new StringList(strKeyName), null, null, sbSubject.toString(), sbMessage.toString(), "", fromAgent, null, new StringList(strThreadid), notifyType);
				//MailUtil.sendMessage(context,new StringList(strKeyName),null,null,"Discussion Thread Created",strMessage,objectIdLists);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateCheckoutDateTime(Context context, String[] args) throws Exception {
		String strDocid = args[0];
		try{
			StringList styleSheet=new StringList();
			DomainObject domDoc = new DomainObject(strDocid);
			String docName = domDoc.getName(context);
			String docRev = domDoc.getRevision(context);
			BusinessInterface busIntDoc = new BusinessInterface("WMSDocumentExtension", new Vault("eService Administration"));
			BusinessObject busDoc = new BusinessObject("Document",docName,docRev,"eService Production");
			BusinessInterfaceList busIntList = busDoc.getBusinessInterfaces(context);
			if(!busIntList.contains(busIntDoc)){
				busDoc.addBusinessInterface(context, busIntDoc);
			}
			java.util.Date curDate = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getInputDateFormat(), context.getLocale());
			String todayDate = sdf.format(curDate);
			Person person = new Person(context.getUser());
			String strPersonId=Person.getPerson(context).getId();
			DomainObject domPerson = new DomainObject(strPersonId);
			String strDefaultRole=domPerson.getInfo(context, "attribute[WMSDefaultRole]");
			if(strDefaultRole.equals("SectionManager")) {
				domDoc.setAttributeValue(context, "WMSCheckoutDateTime", todayDate);
			}
		}catch (Exception e) {
			e.printStackTrace();// TODO: handle exception
			throw e;
		}
	}
	
	public StringList getStyleForCheckout(Context context,String args[]) throws Exception{
		HashMap programMap=JPO.unpackArgs(args);
		MapList objectList=(MapList)programMap.get("objectList");
		StringList styleSheet=new StringList(objectList.size());
		try{
			String[] ids = new String[objectList.size()];
			for(int count=0;count<objectList.size();count++) {
				Map info = (Map)objectList.get(count);
				ids[count] = (String)info.get("id");
			}
			MapList objectVsalues = DomainObject.getInfo(context, ids, new StringList("attribute[WMSCheckoutDateTime]"));
			for(int count=0;count<objectVsalues.size();count++) {
				Map info = (Map)objectVsalues.get(count);
				String checkOutDate = (String)info.get("attribute[WMSCheckoutDateTime]");
				if(UIUtil.isNotNullAndNotEmpty(checkOutDate)) {
					styleSheet.add("doco-cell-background-green");
				}else {
					styleSheet.add("");
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return styleSheet;
	}
	//Code added for B3 Action-  start

}