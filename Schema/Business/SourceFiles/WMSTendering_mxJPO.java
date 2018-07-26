/** Name of the JPO : WMSTendering
 ** Client          : MSIL
 ** Description     : 
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.ContextUtil;

public class WMSTendering_mxJPO extends WMSConstants_mxJPO
{
	public WMSTendering_mxJPO(Context context, String[] args) {
		super(context, args);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * getSORForLineItem Method to get the connected Objects on expansion
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlSOR MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSORForLineItem(Context context, String args[]) throws Exception {
		MapList mlSOR=new MapList();
		try {
			StringList objectSelects= new StringList();
			objectSelects.addElement(DomainConstants.SELECT_ID);
			
			mlSOR = DomainObject.findObjects(context,
												TYPE_WMS_SOR,				// type filter
												"*",					// vault filter
												"current==Active",		// where clause
												objectSelects);
			
		}catch (Exception e) {
			e.printStackTrace();
			throw e;// TODO: handle exception
		}
		
		return mlSOR;
	}
	
	/**
	 * copySORRateForLineItem Method to get the connected Objects on expansion
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlWorkOrder MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList copySORRateForLineItem(Context context, String args[]) throws Exception {
		MapList mlSOR=new MapList();
		MapList mlWorkOrder=new MapList();
		StringList slFinalList = new StringList();
		String RELATIONSHIP_LINEITEM = PropertyUtil.getSchemaProperty("relationship_LineItem"); 
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strobjId = (String) programMap.get("objectId");
			DomainObject domObj = DomainObject.newInstance(context, strobjId);

			StringList slLineItem = domObj.getInfoList(context, "from["+RELATIONSHIP_LINEITEM+"].to.to["+RELATIONSHIP_WMS_SOR_TO_LINEITEM+"].from.id");

			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement("from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id");
			
			mlWorkOrder = DomainObject.findObjects(context,
													"WMSWorkOrder",		// type filter
													"*",				//vault filter
													"current==Active",	// where clause
													objectSelects);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;// TODO: handle exception
		}
		return mlWorkOrder;
	}

	/**
	 * expandConnectedObjectsForCopyRate Method to get the connected Objects on expansion
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return getConnectedObjects MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandConnectedObjectsForCopyRate(Context context, String[] args) throws Exception{
		MapList mapListObjects = new MapList();
		try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String strObjectOID = (String)programMap.get("objectId");
				String strSelectedLID = (String)programMap.get("parentId");
				
				programMap.put("objectId",strObjectOID);
				programMap.put("parentId",strObjectOID);
				programMap.put("CopyAll","CopyAll");
				return getConnectedObjects(context, JPO.packArgs(programMap));
		}catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * getConnectedObjects Method to get the connected Objects on expansion
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mapListObjects MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getConnectedObjects(Context context, String[] args) throws Exception{
		MapList mapListObjects = new MapList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectOID = (String)programMap.get("objectId");
			String strSelectedLID = (String)programMap.get("parentId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectOID))
			{
				
				DomainObject domObjLineItm = DomainObject.newInstance(context, strSelectedLID);
				String strSORId = (String)domObjLineItm.getInfo(context, "to["+RELATIONSHIP_WMS_SOR_TO_LINEITEM+"].from.id");
				DomainObject domObj = DomainObject.newInstance(context, strObjectOID);
				
				Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_BOOK);
				patternType.addPattern(TYPE_WMS_SEGMENT);
				patternType.addPattern(TYPE_WMS_MEASUREMENT_TASK);
				patternType.addPattern(TYPE_WMS_WORK_ORDER);
 
				Pattern patternRel = new Pattern(RELATIONSHIP_BILL_OF_QUANTITY );
				
				StringList strBusSelect = new StringList(2);
				strBusSelect.add(DomainConstants.SELECT_ID);
				strBusSelect.add(DomainConstants.SELECT_TYPE);
				strBusSelect.add("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id");

				StringList strListRelSelects = new StringList(1);
				strListRelSelects.add(DomainRelationship.SELECT_ID);

				Map postMatch=new HashMap();
				if(!programMap.containsKey("CopyAll")){
					postMatch.put("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id", strSORId);
				}

				MapList  mlMTs = domObj.getRelatedObjects(context,										// matrix context
																patternRel.getPattern(),				// relationship pattern
																patternType.getPattern(),				// object pattern
																true,									// to direction
																true,									// from direction
																(short)4,								// recursion level
																strBusSelect,							// object selects
																strListRelSelects,						// relationship selects
																DomainConstants.EMPTY_STRING,			// object where clause
																DomainConstants.EMPTY_STRING,			// relationship where clause
																(short)0,								// No expand limit
																DomainConstants.EMPTY_STRING,			// postRelPattern
																TYPE_WMS_MEASUREMENT_TASK,				// postTypePattern
																postMatch);
				for(int i = 0; i < mlMTs.size();i++) {
					Map mapSOR = (Map) mlMTs.get(i);
					mapSOR.put("disableSelection", "true");
					mapSOR.put("hasChildren", "false");
					mapListObjects.add(mapSOR);
				}
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
	 * copySORRateForOneLineItem Method to get the connected Objects on expansion
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlReturn MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList copySORRateForOneLineItem(Context context,String[] args ) throws Exception
	{
		MapList mlReturn=new MapList();
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strobjId = (String) programMap.get("objectId");
			DomainObject domObj = DomainObject.newInstance(context, strobjId);
			
			StringList strBusSelect = new StringList(2);
			strBusSelect.add(DomainConstants.SELECT_ID);
			strBusSelect.add(DomainConstants.SELECT_TYPE);
			strBusSelect.add(DomainConstants.SELECT_CURRENT);
			
			StringList strListRelSelects = new StringList(1);
			strListRelSelects.add(DomainRelationship.SELECT_ID);
			
			String strSORId = (String)domObj.getInfo(context, "to["+RELATIONSHIP_WMS_SOR_TO_LINEITEM+"].from.id");
			DomainObject domSOR = DomainObject.newInstance(context,strSORId);
			
			Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
			patternType.addPattern(TYPE_WMS_SEGMENT);
			patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK);
			patternType.addPattern(TYPE_WMS_WORK_ORDER);
			
			Pattern patternRel = new Pattern(RELATIONSHIP_WMS_TASK_SOR);
			patternRel.addPattern(RELATIONSHIP_BILL_OF_QUANTITY);
			
			Map postMatch=new HashMap();
			postMatch.put("current","Active");
			mlReturn =  domSOR.getRelatedObjects(context,										// matrix context
													patternRel.getPattern(),					// relationship pattern
													patternType.getPattern(),					// object pattern
													true,										// to direction
													true,										// from direction
													(short)4,									// recursion level
													strBusSelect,								// object selects
													strListRelSelects,							// relationship selects
													DomainConstants.EMPTY_STRING,				// object where clause
													DomainConstants.EMPTY_STRING,				// relationship where clause
													(short)0,									// No expand limit
													DomainConstants.EMPTY_STRING,				// postRelPattern
													TYPE_WMS_WORK_ORDER,						// postTypePattern
													postMatch);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return mlReturn;
	}
	/**
	 * getDepartmentList Method to get the connected Objects on expansion
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlDepartments MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDepartmentList (Context context,String strOwnerID) throws Exception {
		MapList mlDepartments = new MapList();
		try{
			StringList slDepartmentSelect = new StringList(2);
			slDepartmentSelect.addElement(DomainConstants.SELECT_NAME);
			
			String srtPersonID = PersonUtil.getPersonObjectID(context, strOwnerID);
			DomainObject doPersonID = DomainObject.newInstance(context, srtPersonID);
			
			mlDepartments = doPersonID.getRelatedObjects(
											context, 							//the context for this request
											DomainConstants.RELATIONSHIP_MEMBER,//pattern to match relationships
											DomainConstants.TYPE_DEPARTMENT,	//pattern to match types
											slDepartmentSelect, 				// list of select statement pertaining to Business Obejcts.
											null,								//list of select statement pertaining to Relationships.
											true,								//get To relationships
											false, 								//get From relationships
											(short)1,							//the number of levels to expand, 0 equals expand all.
											null,								//where clause to apply to objects, can be empty ""
											null);								//where clause to apply to relationship, can be empty ""
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return mlDepartments;
	}
	/**
	 * isRFQOwnerFromCivilDepartment Method to get the connected Objects on expansion
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlConnectedDepartment MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean isRFQOwnerFromCivilDepartment(Context context,String[] args) throws Exception
	{
		boolean bReturn = false;
		String RELATIONSHIP_RTSQUOTATION = PropertyUtil.getSchemaProperty("relationship_RTSQuotation");
		Map mapDept = null;
		String strDeptName = null;
		MapList mlConnectedDepartment = new MapList();
		
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strobjId = (String) programMap.get("objectId");
			HashMap mSettings = (HashMap) programMap.get("SETTINGS");
			String strKey = (String) mSettings.get("key");
			DomainObject domObj = DomainObject.newInstance(context, strobjId);
			String strRFQId = (String)domObj.getInfo(context, "to["+RELATIONSHIP_RTSQUOTATION+"].from.id");
			DomainObject doRFQId = DomainObject.newInstance(context,strRFQId);
			String strOwnerID = doRFQId.getInfo(context,DomainConstants.SELECT_OWNER);
			
			ContextUtil.pushContext(context, strOwnerID, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); //366577
			MSILAccessRights_mxJPO msilAccess=new MSILAccessRights_mxJPO(context,args);
		 	boolean isCIVIL = msilAccess.isPersonFromCivil(context, args);
			if ( isCIVIL && (strKey.equals("isCivil"))){
					bReturn = true;
			} else if (!isCIVIL && strKey.equals("isNotCivil")){
					bReturn = true;
			} 
			ContextUtil.popContext(context);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return bReturn;
	}
}