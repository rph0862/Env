/*
 *  emxCompany.java
 *
 * Copyright (c) 1992-2017 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * Date           |    Change By    | Tag to be searched |       Details
   ===========================================================================================================
   04/05/2016     |  Ajit           | 04/05/2016         |    To Show  Business Unit PE only based on login Person
*
 */
import matrix.db.*;
import java.lang.*;

//04/05/2016   |  Ajit |    To Show  Business Unit PE only based on login Person -- Start
import java.util.HashMap;
import java.util.Map;
import matrix.util.StringList;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
//04/05/2016   |  Ajit |    To Show  Business Unit PE only based on login Person -- End
//Code added for B3 Action- Start
import java.util.Iterator;
import java.util.Iterator;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
//Code added for B3 Action- end

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCompany_mxJPO extends emxCompanyBase_mxJPO
{

	//Code Added for B3 Action- Start
	String ATTRIBUTE_WMS_CATEGORY_LIST = PropertyUtil.getSchemaProperty("attribute_WMSCategoryList");
	String ATTRIBUTE_WMS_MAXIMUM_CONTRACT_VALUE = PropertyUtil.getSchemaProperty("attribute_WMSMaximumContractValue");
	String ATTRIBUTE_WMS_TURNOVER = PropertyUtil.getSchemaProperty("attribute_WMSTurnover");
	
	//Code Added for B3 Action- End
	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since AEF Rossini
	 * @grade 0
	 */
	public emxCompany_mxJPO (Context context, String[] args)
		throws Exception
	{
	  super(context, args);
	}
	
	//04/05/2016   |  Ajit |    To Show  Business Unit PE only based on login Person -- Start

	/**
	* Gets the Business Units for the Company.
	*
	* @param context The Matrix Context.
	* @param selectStmts The list of selects.
	* @return maplist of BusinessUnits
	* @throws FrameworkException If the operation fails.
	* @since Common 10.0.0.0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBusinessUnits (Context context,String[] args) throws Exception
	{
		String strPref_BU        = PropertyUtil.getAdminProperty(context,"Person",context.getUser(), "preference_BusinessUnit");          	 
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");
		MapList mapList = new MapList();
		MapList mlBUList = new MapList();
		try {
			Company companyObj = (Company)newInstance(context,TYPE_COMPANY);
			companyObj.setId(objectId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(companyObj.SELECT_ID);
			selectStmts.addElement(companyObj.SELECT_NAME);
			selectStmts.addElement(Organization.SELECT_WEB_SITE);
			mapList = companyObj.getDivisions(context, selectStmts);
			if(null!=mapList && !mapList.isEmpty()){
				for(int div=0;div<mapList.size();div++){
					Map mapBU = (Map)mapList.get(div);
					String strBUName=(String)mapBU.get(companyObj.SELECT_NAME);
					if(strBUName.equalsIgnoreCase(strPref_BU)){
						mlBUList.add(mapBU);
					}
				}
			}
			if(null !=mlBUList && mlBUList.size()>0){
				mapList=mlBUList;
			}
		}
		catch (FrameworkException Ex) {
			throw Ex;
		}
		return mapList;
	}
	//04/05/2016   |  Ajit |    To Show  Business Unit PE only based on login Person -- End
	
	//Code Added for B3 Action- start
	/**
	* This method will be invoked on post editing the company details.
	* This method will check for uniqueness of the fields and also updates schema property cache.
	* If host company name is changed system need to update the schema property 'role_CompanyName'
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap companyEditPostProcess(Context context, String[] args) throws Exception
	{
		HashMap actionMap = validateUniqueness(context, args);
		//<Fix for 374428>
		/**
		 * Reload PropertyUtil schemaProperty Cache
		 * If the Company Name is changed and it has corresponding role attached.
		 * we need to update this in schema property 'role_CompanyName'
		 */
		if("continue".equals(actionMap.get("Action"))){
			HashMap hashMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) hashMap.get("paramMap");
			String companyID = (String)paramMap.get("objectId");
			String attrOrganizationName = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_OrganizationName);
			String companyNameFieldName = paramMap.get("Name1") != null ? "Name1" : "Name2";

			String  sCompNewName =  (String) paramMap.get(companyNameFieldName);
			boolean isCompanyNameChanged = false;
			MapList fields = (MapList) ((HashMap) hashMap.get("formMap")).get("fields");
			for (Iterator iter = fields.iterator(); iter.hasNext();) {
				Map field = (Map) iter.next();
				if(companyNameFieldName.equals(field.get("name"))) {
					StringList values =  (StringList) field.get("field_value");
					if(values != null && values.size() == 1) {
						isCompanyNameChanged = !values.get(0).equals(sCompNewName);
					}
					break;
					
				}
			}
				DomainObject obj = newInstance(context, companyID);
				calculateWMSMaximumContractValue(context, obj);
			try {
				if (isCompanyNameChanged && hasAdminUserRole(context, sCompNewName)){
					try {
						obj.setAttributeValue(context, attrOrganizationName, sCompNewName);
						//ContextUtil.pushContext(context);
						PropertyUtil.cacheSymbolicNames(context);
					} finally {
						//ContextUtil.popContext(context);
					}
				}
			} catch (Exception e) {
				//No need to update the cache.
				e.printStackTrace();
			}
		}else{
			if(UIUtil.isNotNullAndNotEmpty((String)actionMap.get("Message"))){
				throw new FrameworkException((String)actionMap.get("Message"));
			}
		}
		return actionMap;
		//</Fix for 374428>
	}
	
	private boolean hasAdminUserRole(Context context, String compNewName) throws Exception {
		//This query will return empty string if there is no role with this name or the role is not Project or Org type.
		String strResult = MqlUtil.mqlCommand(context, "list role $1 where $2", true, compNewName, "isanorg");
		return compNewName.equals(strResult);
	}
	public void calculateWMSMaximumContractValue(Context context,DomainObject obj) throws FrameworkException 
	{
		try {
		StringList strListAmbSelects = new StringList(2);
		strListAmbSelects.add("attribute["+ATTRIBUTE_WMS_TURNOVER+"]");//Sequence atribute
		Map mCompanynfo = obj.getInfo(context, strListAmbSelects);
		String strWMSTURNOVER=(String)mCompanynfo.get("attribute["+ATTRIBUTE_WMS_TURNOVER+"]");
		float fWMSTurnover = Float.parseFloat(strWMSTURNOVER);
		float fWMSMaximumContractValue=0.0f;
		if(fWMSTurnover>=300){
			fWMSMaximumContractValue=fWMSTurnover/3;
		}
		if(fWMSTurnover>=100 && fWMSTurnover<300){
			fWMSMaximumContractValue=100;
		}
		if(fWMSTurnover<100){
			fWMSMaximumContractValue=fWMSTurnover;
		}
		obj.setAttributeValue(context, ATTRIBUTE_WMS_MAXIMUM_CONTRACT_VALUE, String.valueOf(fWMSMaximumContractValue));
		}catch (Exception e) {
			e.printStackTrace();// TODO: handle exception
			throw e;
		}
	}
	
	/**
	 * Added Method displayCategoryListRanges to display the ranges for attribute WMSCategoryList
	 * @param context - current context
	 * @param String String[] args - holds ObjID
	 * @throws Exception - the exception
	 */
 	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public HashMap displayCategoryListRanges(Context context, String args[]) throws Exception
	{
		HashMap CategoryListMap = new HashMap();
		StringList slCategoryList = new StringList();
		String strCategoryList = null;
		
		String objectID = null;
		String strCatList = null;
		DomainObject doCompID = null;
		try 
		{
			strCategoryList =  EnoviaResourceBundle.getProperty(context,"WMS.CategoryList.Ranges");
			slCategoryList = FrameworkUtil.split(strCategoryList, ",");
			CategoryListMap.put("field_choices", slCategoryList);
			CategoryListMap.put("field_display_choices", slCategoryList); 
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			throw ex;
		}
		return CategoryListMap;
	}
	
	 /**
	 * Added Method updateCategoryList to update the ranges for attribute WMSCategoryList
	 * @param context - current context
	 * @param String String[] args - holds ObjID
	 * @throws Exception - the exception
	 */
 	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public void updateCategoryList(Context context,String[] args) throws Exception
	{

		String objectID = null;
		String strCategory = null;
		DomainObject doObjectID = null;
		StringBuilder sbCategoryList = new StringBuilder();
		
		try {
			Map programMap = (Map)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			objectID = (String)paramMap.get("objectId");
			doObjectID = DomainObject.newInstance(context,objectID);
			String[] newAttrValue = (String[]) paramMap.get("New Values");
			
			for(int j=0; j < newAttrValue.length; j++){
				strCategory = newAttrValue[j];
				sbCategoryList.append(strCategory);
				if(j<newAttrValue.length-1)
				sbCategoryList.append("\n");
			}
			doObjectID.setAttributeValue(context, ATTRIBUTE_WMS_CATEGORY_LIST, sbCategoryList.toString());
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
	}
	
	/**
	 * displaySupplierCategoryList Method - display category list of Supplier
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlFindSupplierList MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String displaySupplierCategoryList(Context context,String[] args) throws Exception
	{
		String strObjID = null;
		DomainObject doCompID = null;
		String strCatList = null;
		String strCategoryList = null;
		StringList slSelectedCatList = new StringList();
		StringList slCategoryList = new StringList();
		StringBuffer strPAC = new StringBuffer();
		
		try
		{
			HashMap inputMap =  (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)inputMap.get("paramMap");
			
			strObjID = (String) paramMap.get("objectId");
			
			HashMap requestMap = (HashMap) inputMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");

			doCompID = DomainObject.newInstance(context,strObjID);
			strCatList = doCompID.getAttributeValue(context, ATTRIBUTE_WMS_CATEGORY_LIST);
			slSelectedCatList = FrameworkUtil.split(strCatList, ",");
			
			strCategoryList =  EnoviaResourceBundle.getProperty(context,"WMS.CategoryList.Ranges");
			slCategoryList = FrameworkUtil.split(strCategoryList, ",");

			if("view".equals(strMode) || strMode==null || strMode=="") {
				//strPAC.append("<text>"+strCatList+"</text>");
				strPAC.append(strCatList);
			}
			/*else if("edit".equals(strMode)) {
				
				strPAC.append("<select name='categoryList' multiple='multiple'>");
				for (int c = 0; c<slCategoryList.size(); c++) { 
					if(slSelectedCatList.contains(slCategoryList.get(c))){
						strPAC.append ("<option selected >"+slCategoryList.get(c)+"</option>");
					} else {
						strPAC.append("<option >"+slCategoryList.get(c)+"</option>");
					}
				}
				strPAC.append("</select>");
				
			}*/
		}
		catch(Exception exception) 
		{
			exception.printStackTrace();
			throw exception;
		}
		return strPAC.toString();
	}
	//Code Added for B3 Action- end
}
