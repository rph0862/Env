
/*
 *  PackageBase
 *
 *
 * (c) Dassault Systemes, 1993 - 2017.  All rights reserved
 *
 *
 *  static const char RCSID[] = $Id: /ENOSourcingCentral/CNext/Modules/ENOSourcingCentral/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1.1.1 Thu Nov 13 08:27:30 2008 GMT  Experimental$
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.sourcing.Calculator;
import com.matrixone.apps.sourcing.RTSQuotation;
import com.matrixone.apps.sourcing.RequestToSupplier;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.MatrixException;
import matrix.util.StringList;

/**
 * The <code>${CLASSNAME}</code> class contains Package related utilites
 * The methods of this class are used to create packages, list all the packages based on selected filter,
 * list the attachments for the packages,list the PackageRFQ
 */

public class TDRCapital_mxJPO extends emxDomainObject_mxJPO
{
	public String RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT = TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT;
	public String RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT = TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT;
	public String RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE = TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE;
	public String RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER = TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER;
	public String RELATIONSHIP_TDR_RFQ_LOCATION = TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_LOCATION;

	public String ATTRIBUTE_TDR_VISIBILITY = TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY;
	public String ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER = TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER;
	public String ATTRIBUTE_MSIL_RINGI_NUMBER = TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_NUMBER;
	public String ATTRIBUTE_TDR_BUDGET = TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET;

	public String VAULT_E_SERVICE_PRODUCTION = "eService Production";
	public Properties  _classCurrencyConfig = new Properties();

	/**
	 * Constructs a <code>${CLASSNAME}</code> Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 */
	public TDRCapital_mxJPO (Context context, String[] args)
			throws Exception
	{
		super(context, args);
		Page page= new Page("TDRCapitalConfiguration");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
	}

	/**
	 * Default method to be executed when no method is specified
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @return int 0 for success and non-zero for failure
	 * @throws Exception if the operation fails
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
	{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}


	/**
	 * Method returns MapList of departments connected to RFQ Template
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDepartments(Context context, String[] args)throws Exception{
		MapList mlDeptList = new MapList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlDeptList = doObject.getRelatedObjects(context,RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT,DomainObject.TYPE_DEPARTMENT,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return mlDeptList;
	}

	/**
	 * Method returns StringList of departments connected to RFQ Template. This is used for excluding the Departments from search result while adding
	 * department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedDepartments(Context context, String[] args)throws Exception{
		StringList slExcludeDeptList = new StringList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				MapList mlDeptList = doObject.getRelatedObjects(context,RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT,DomainObject.TYPE_DEPARTMENT,slObjectSelect,null,false,true,(short)1,null,null);

				if(mlDeptList != null && !mlDeptList.isEmpty()) {
					slExcludeDeptList = TDRUtil_mxJPO.toStringList(context, DomainObject.SELECT_ID, mlDeptList);
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slExcludeDeptList;
	}

	/**
	 * Method used to update TDRAttributeVisibility attribute on Line Item Template object. 
	 * If Attribute is not available, Add interface and update the value.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateTDRAttributeVisibility(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			String strOldValue = (String)paramMap.get("Old Value");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				doObject.setAttributeValue(context, ATTRIBUTE_TDR_VISIBILITY, strNewValue);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	/**
	 * Method returns StringList of RFQ Templates which are having departments same as context user.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeRFQTemplatesOfSameDepartment(Context context, String[] args)throws Exception{
		StringList slIncludeRFQTemplateList = new StringList();
		try {
			StringList slIncludeDeptList = new StringList();
			String strContextPerson = context.getUser();
			String strWhere = "from["+DomainRelationship.RELATIONSHIP_MEMBER+"].to.name=='"+strContextPerson+"'";
			StringList slDepartmentSelect = new StringList();
			slDepartmentSelect.add(DomainObject.SELECT_ID);
			MapList mlDepartmentList =  DomainObject.findObjects(context,DomainObject.TYPE_DEPARTMENT,VAULT_E_SERVICE_PRODUCTION,strWhere,slDepartmentSelect);

			if(mlDepartmentList.isEmpty() == false) {
				slIncludeDeptList = TDRUtil_mxJPO.toStringList(context, DomainConstants.SELECT_ID, mlDepartmentList);
			}
			DomainObject doDept = null;
			for(String strDeptId: slIncludeDeptList) {
				doDept = new DomainObject(strDeptId);
				slIncludeRFQTemplateList.addAll(doDept.getInfoList(context, "to["+RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT+"].from.id"));
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slIncludeRFQTemplateList;
	}


	/**
	 * Method used to connect/update relationship between RFQ and Department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateSCDepartment(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String) paramMap.get("objectId");
			String strNewValue = (String) paramMap.get("New Value");
			String strOldValue = (String) paramMap.get("Old value");
			if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				String strRelId = doRFQ.getInfo(context, "to[" + RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].id");
				if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
					DomainRelationship.disconnect(context, strRelId);
				} 
				if (UIUtil.isNotNullAndNotEmpty(strNewValue)) {
					DomainRelationship.connect(context, new DomainObject(strNewValue),
							RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT, doRFQ);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	/**
	 * Method used to get Concerned SC Department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	public String getConcernedSCDepartment(Context context, String[] args)throws Exception{
		String strReturnValue = DomainConstants.EMPTY_STRING;
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId) ) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				strReturnValue = doRFQ.getInfo(context, "to["+RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.name");
				if(UIUtil.isNullOrEmpty(strReturnValue)) {
					strReturnValue = DomainConstants.EMPTY_STRING;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strReturnValue;
	}

	/**
	 * Method used to get list of Department under Business Unit mentioned in Page. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map getListOfSCDepartments(Context context, String[] args)throws Exception{
		Map returnMap = new HashMap();
		try {
			StringList slOptions = new StringList();
			StringList slDisplayOptions = new StringList();

			slOptions.add(DomainConstants.EMPTY_STRING);
			slDisplayOptions.add(DomainConstants.EMPTY_STRING);

			//			Properties _classCurrencyConfig = new Properties();
			//			Page page= new Page("TDRCapitalConfiguration");
			//			_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
			String strAccessKey = "TDRCapital.ConcernedSCDept.BusinessUnit";
			String strBusinessUnitName = _classCurrencyConfig.getProperty(strAccessKey);
			StringList slSelect = new StringList(DomainObject.SELECT_ID);
			slSelect.add(DomainObject.SELECT_NAME);
			slSelect.add(DomainObject.SELECT_TYPE);
			MapList mlBUList = DomainObject.findObjects(context, DomainObject.TYPE_BUSINESS_UNIT, strBusinessUnitName, DomainConstants.QUERY_WILDCARD, "*", VAULT_E_SERVICE_PRODUCTION, "", false, slSelect);
			if(mlBUList.isEmpty() == false) {
				Map tempMap = (HashMap)mlBUList.get(0);
				String strBUId = (String)tempMap.get(DomainObject.SELECT_ID);
				if(UIUtil.isNotNullAndNotEmpty(strBUId)) {
					DomainObject doBU = new DomainObject(strBUId);
					StringBuffer sbRelPattern = new StringBuffer();
					sbRelPattern.append(DomainRelationship.RELATIONSHIP_COMPANY_DEPARTMENT);
					sbRelPattern.append(",");
					sbRelPattern.append(DomainRelationship.RELATIONSHIP_DIVISION);
					StringBuffer sbTypePattern = new StringBuffer();
					sbTypePattern.append(DomainObject.TYPE_DEPARTMENT);
					sbTypePattern.append(",");
					sbTypePattern.append(DomainObject.TYPE_BUSINESS_UNIT);

					MapList mlDeptList = doBU.getRelatedObjects(context,sbRelPattern.toString(),sbTypePattern.toString(),slSelect,null,false,true,(short)0,null,null);

					String strType = DomainConstants.EMPTY_STRING;
					for(int i=0;i<mlDeptList.size();i++) {
						tempMap = (Map)mlDeptList.get(i);
						strType = (String)tempMap.get(DomainObject.SELECT_TYPE);
						if(DomainObject.TYPE_DEPARTMENT.equals(strType)) {
							slOptions.add((String)tempMap.get(DomainObject.SELECT_ID));
							slDisplayOptions.add((String)tempMap.get(DomainObject.SELECT_NAME));
						}						
					}
				}
			}

			returnMap.put("field_choices", slOptions);
			returnMap.put("field_display_choices", slDisplayOptions);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return returnMap;

	}

	/**
	 * Method used to connect/update relationship between RFQ and Route Template. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void connectApprovalRouteTemplate(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId) && UIUtil.isNotNullAndNotEmpty(strNewValue)) {
				DomainRelationship.connect(context, new DomainObject(strNewValue), RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE, new DomainObject(strObjectId));
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	/**
	 * Method used to get Concerned Route Template. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	public String getApprovalRouteTemplate(Context context, String[] args)throws Exception{
		String strReturnValue = DomainConstants.EMPTY_STRING;
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId) ) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				strReturnValue = doRFQ.getInfo(context, "to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.name");
				if(UIUtil.isNullOrEmpty(strReturnValue)) {
					strReturnValue = DomainConstants.EMPTY_STRING;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strReturnValue;
	}

	/**
	 * Method returns StringList of Route Templates connected to RFQ
	 * department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedRouteTemplate(Context context, String[] args)throws Exception{
		StringList slExcludeRouteTemplateList = new StringList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				slExcludeRouteTemplateList = doObject.getInfoList(context, "to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.id");
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slExcludeRouteTemplateList;
	}


	/**
	 * Method returns int value based on Route Templates connected to RFQ
	 * If connected, return 0 else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int checkApprovalTemplateOnRFQ(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			String strMessage = DomainObject.EMPTY_STRING;
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slSelect =  new StringList();
				slSelect.add(DomainObject.SELECT_CURRENT);
				slSelect.add("to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.id");
				slSelect.add("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.id");
				Map mapInfo = (Map)doObject.getInfo(context, slSelect);
				String strCurrent = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
				String strInitialApprovalTemplate = (String)mapInfo.get("to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.id");
				String strFinalApprovalTemplate = (String)mapInfo.get("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.id");
				if(strCurrent.equals(DomainObject.STATE_STARTED) &&UIUtil.isNullOrEmpty(strInitialApprovalTemplate)) {
					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.AssignApprovalTemplate");
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
					iReturn = 1;
				}
				if(strCurrent.equals(DomainObject.STATE_RESPONSE_COMPLETE) && UIUtil.isNullOrEmpty(strFinalApprovalTemplate)){
					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.AssignFinalApprovalTemplate");
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
					iReturn = 1;
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return iReturn;
	}


	/**
	 * Method returns int value based on Commercial Quote Opener assigned.
	 * Return 0 if assigned else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int checkCheckForCommercialQuoteOpener(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				String strQuoteOpener = doObject.getInfo(context, "to["+RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.id");

				if(UIUtil.isNullOrEmpty(strQuoteOpener)) {
					String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.AssignQuoteOpener");
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
					iReturn = 1;
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return iReturn;
	}

	/**
	 * Method returns int value. This will create route from route template while promoting RFQ from Started to Initial Review State
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int createRouteFromTemplate(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			String strCurrent = (String)args[3];
			Route newRouteObj = null;
			String strApprvalTemplate =  DomainObject.EMPTY_STRING;
			String strMessage =  DomainObject.EMPTY_STRING;
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_DESCRIPTION);
				slObjectSelect.add(DomainObject.SELECT_CURRENT);
				slObjectSelect.add(DomainObject.SELECT_POLICY);
				slObjectSelect.add("to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.id");
				slObjectSelect.add("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.id");

				Map mRFQInfo = doObject.getInfo(context,slObjectSelect);
				if(strCurrent.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){					
					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.AssignApprovalTemplate");
					strApprvalTemplate = (String)mRFQInfo.get("to["+RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE+"].from.id");
				}else if(strCurrent.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){		
					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.AssignFinalApprovalTemplate");
					strApprvalTemplate = (String)mRFQInfo.get("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.id");
				}

				if(UIUtil.isNullOrEmpty(strApprvalTemplate)) {
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
					iReturn = 1;
				}else if(UIUtil.isNotNullAndNotEmpty(strApprvalTemplate)){
					DomainObject doRouteTemplate = new DomainObject(strApprvalTemplate);

					String strAttributeRouteCompletionAction = PropertyUtil.getSchemaProperty(context, "attribute_RouteCompletionAction");

					String strAttributeRouteBasePurpose = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
					String SELECT_ATTR_ROUTE_BASE_PURPOSE = "attribute[" +  PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose")  + "]";

					String strAttributeRouteBasePolicy = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePolicy");
					String strAttributeRouteBaseState = PropertyUtil.getSchemaProperty(context, "attribute_RouteBaseState");
					String strAttributeAutoStopRejection = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" );
					String SELECT_ATTR_AUTO_STOP_REJECTION = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" ) + "]";

					Map routeAttributeMap = new HashMap();		

					Map objectRouteAttributeMap = new HashMap();	

					slObjectSelect.add(SELECT_ATTR_AUTO_STOP_REJECTION);
					slObjectSelect.add(SELECT_ATTR_ROUTE_BASE_PURPOSE);
					Map mRouteTemplateInfo = doRouteTemplate.getInfo(context, slObjectSelect);


					routeAttributeMap.put(strAttributeRouteCompletionAction, "Promote Connected Object");
					routeAttributeMap.put(strAttributeAutoStopRejection, mRouteTemplateInfo.get(SELECT_ATTR_AUTO_STOP_REJECTION));
					routeAttributeMap.put(strAttributeRouteBasePurpose,mRouteTemplateInfo.get(SELECT_ATTR_ROUTE_BASE_PURPOSE));

					// attributes to be set on relationship Object Route
					objectRouteAttributeMap.put(strAttributeRouteBasePolicy,FrameworkUtil.getAliasForAdmin(context, "Policy", args[2], false));
					objectRouteAttributeMap.put(strAttributeRouteBaseState, FrameworkUtil.reverseLookupStateName(context, args[2], args[3]));
					objectRouteAttributeMap.put(strAttributeRouteBasePurpose, "Standard");
					String strRouteTemplateDescription = (String)mRouteTemplateInfo.get(DomainObject.SELECT_DESCRIPTION);
					String strRFQPolicyName = (String)mRFQInfo.get(DomainObject.SELECT_POLICY);
					String strRFQStateName = (String)mRouteTemplateInfo.get(DomainObject.SELECT_CURRENT);
					newRouteObj = Route.createAndStartRouteFromTemplateAndReviewers(context, 
							strApprvalTemplate, 
							strRouteTemplateDescription, 
							DomainConstants.EMPTY_STRING, 
							strObjectId,
							strRFQPolicyName, 
							strRFQStateName,
							routeAttributeMap, 
							objectRouteAttributeMap, 
							new HashMap(),
							true);
					newRouteObj.promote(context);
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}

	/**
	 * Method returns StringList containing Preferred Commercial Quote Opener user
	 * department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeDefaultCommercialQuoteOpener(Context context, String[] args)throws Exception{
		StringList slIncludePersonList = new StringList();
		try {
			String strPersonName = context.getUser();
			String strPreferredCommercialQO = MqlUtil.mqlCommand(context, "print person '"+strPersonName+"' select property[preference_CommercialQuoteOpener].value dump", false);

			String strRolesKey = "TDRCapital.CommercialQuoteOpener.Roles";
			String strCommercialQuoyteOpenerRoles = _classCurrencyConfig.getProperty(strRolesKey);

			String strAccessKey = "TDRCapital.ConcernedSCDept.BusinessUnit";
			String strBusinessUnitName = _classCurrencyConfig.getProperty(strAccessKey);
			StringList slSelect = new StringList(DomainObject.SELECT_ID);
			slSelect.add(DomainObject.SELECT_NAME);
			slSelect.add(DomainObject.SELECT_TYPE);
			MapList mlBUList = DomainObject.findObjects(context, DomainObject.TYPE_BUSINESS_UNIT, strBusinessUnitName, DomainConstants.QUERY_WILDCARD, "*", VAULT_E_SERVICE_PRODUCTION, "", false, slSelect);

			StringBuilder sbPersonWhere = new StringBuilder();
			if(UIUtil.isNotNullAndNotEmpty(strCommercialQuoyteOpenerRoles)) {
				StringList slRoleList = FrameworkUtil.splitString(strCommercialQuoyteOpenerRoles, ",");

				for(String strRole: slRoleList) {
					if(sbPersonWhere.length() == 0) {
						sbPersonWhere.append("assignment["+strRole+"]==true");
					}else {
						sbPersonWhere.append(" || assignment["+strRole+"]==true");
					}
				}				
			}
			StringList slPersonSelect = new StringList(DomainObject.SELECT_ID);
			MapList mlPersonList = new MapList();

			if(mlBUList.isEmpty() == false) {
				Map tempMap = (HashMap)mlBUList.get(0);
				String strBUId = (String)tempMap.get(DomainObject.SELECT_ID);
				if(UIUtil.isNotNullAndNotEmpty(strBUId)) {
					DomainObject doBU = new DomainObject(strBUId);
					mlBUList = doBU.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_DIVISION+","+DomainRelationship.RELATIONSHIP_MEMBER,DomainObject.TYPE_BUSINESS_UNIT+","+DomainObject.TYPE_PERSON,slSelect,null,false,true,(short)0,null,null);
				}
			}			
			String strQuery = "list person * where \""+sbPersonWhere.toString()+"\" select name dump";
			String strPerson = MqlUtil.mqlCommand(context, strQuery);
			StringList slPersonList = FrameworkUtil.split(strPerson, "\n");
			Map tempMap = null;
			if(slPersonList.size()>0) {
				for(int i=0;i<mlBUList.size();i++) {
					tempMap = (Map)mlBUList.get(i);
					strPerson = (String)tempMap.get(DomainObject.SELECT_ID);
					if(slPersonList.contains((String)tempMap.get(DomainObject.SELECT_NAME)) && slIncludePersonList.contains(strPerson) == false){
						slIncludePersonList.add((String)tempMap.get(DomainObject.SELECT_ID));
					}
				}
			}

			if(UIUtil.isNotNullAndNotEmpty(strPreferredCommercialQO)){
				slIncludePersonList.remove(strPreferredCommercialQO);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slIncludePersonList;
	}



	/**
	 * Method used to get Commercial Quote Opener 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	public String getCommercialQuoteOpener(Context context, String[] args)throws Exception{
		String strReturnValue = DomainConstants.EMPTY_STRING;
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId) ) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				strReturnValue = doRFQ.getInfo(context, "to["+RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.name");
				if(UIUtil.isNullOrEmpty(strReturnValue)) {
					strReturnValue = DomainConstants.EMPTY_STRING;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strReturnValue;
	}


	/**
	 * Method returns int value. This will update TDRAGSequence attribute on Default Line Item Attribute Group relationship while copying Line Item Template from RFQ Template
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int updateTDRAGSequenctAttributeOnDefLineItemGroup(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strFromObjectId = (String)args[0];
			String strToObjectId = (String)args[1];
			String strRelId = (String)args[2];
			String strSequenceValue = "0";
			String strRFQTemplateID = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",strFromObjectId,"from["+DomainRelationship.RELATIONSHIP_RTS_TEMPLATE+"].to.id");
			if(UIUtil.isNotNullAndNotEmpty(strRFQTemplateID)) {
				DomainObject doRFQTemplate = new DomainObject(strRFQTemplateID);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainConstants.SELECT_ID);
				
				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);
				slRelSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"].value");
				
				MapList mlLineItemTemplates = doRFQTemplate.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_RFQ_LINE_ITEM_TEMPLATE,DomainObject.TYPE_LINE_ITEM_TEMPLATE,slObjectSelect,slRelSelect,false,true,(short)0,null,null);
				Map tempMap = null;
				for(int i=0;i<mlLineItemTemplates.size();i++) {
					tempMap = (Map)mlLineItemTemplates.get(i);
					if(tempMap.containsValue(strToObjectId)) {
						strSequenceValue = (String) tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"].value");
						if(UIUtil.isNotNullAndNotEmpty(strRelId) && UIUtil.isNotNullAndNotEmpty(strSequenceValue)) {
							DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER, strSequenceValue);
						}
					}
				}
				
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}


	/**
	 * Method returns int value. This will update TDRAGSequence attribute on Default Line Item Attribute Group relationship while copying Line Item Template from RFQ Template
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int checkForBudgetValue(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add("attribute["+ATTRIBUTE_TDR_BUDGET+"].value");
				slObjectSelect.add("from["+RELATIONSHIP_RTS_TEMPLATE+"].to.attribute["+ATTRIBUTE_COMPANY_BUYER_ATTRIBUTES+"].value");
				Map mRFQInfo = (Map)doObject.getInfo(context, slObjectSelect);

				String strBudgetValue = (String)mRFQInfo.get("attribute["+ATTRIBUTE_TDR_BUDGET+"].value");
				String strBuyerHeaderAttributes = (String)mRFQInfo.get("from["+RELATIONSHIP_RTS_TEMPLATE+"].to.attribute["+ATTRIBUTE_COMPANY_BUYER_ATTRIBUTES+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strBuyerHeaderAttributes) && strBuyerHeaderAttributes.contains("attribute_TDRBudget")) {
					float fValue = 0;
					if(UIUtil.isNullOrEmpty(strBudgetValue) || Float.valueOf(strBudgetValue) == fValue) {
						String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.ValidBudget");
						emxContextUtil_mxJPO.mqlNotice(context, strMessage);
						iReturn = 1;
					}
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}

	/**
	 * Method returns int value. This method will get the status of Ringi Number with help of external query.
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int checkStatusByRingiNumber(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			String strRingiType = (String)args[1];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);

				StringList slObjectSelect = new StringList();
				slObjectSelect.add("attribute["+ATTRIBUTE_MSIL_RINGI_NUMBER+"].value");
				slObjectSelect.add("from["+RELATIONSHIP_RTS_TEMPLATE+"].to.attribute["+ATTRIBUTE_COMPANY_BUYER_ATTRIBUTES+"].value");
				Map mRFQInfo = (Map)doObject.getInfo(context, slObjectSelect);

				String strRingiNumber = (String)mRFQInfo.get("attribute["+ATTRIBUTE_MSIL_RINGI_NUMBER+"].value");
				String strBuyerHeaderAttributes = (String)mRFQInfo.get("from["+RELATIONSHIP_RTS_TEMPLATE+"].to.attribute["+ATTRIBUTE_COMPANY_BUYER_ATTRIBUTES+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strBuyerHeaderAttributes) && strBuyerHeaderAttributes.contains("attribute_MSILRFQRingiNumber")) {
					if(UIUtil.isNullOrEmpty(strRingiNumber)) {
						String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Alert.ValidMSILRingiNumber");
						emxContextUtil_mxJPO.mqlNotice(context, strMessage);
						iReturn = 1;
					}else {
						//Code to get Status of RFQ by Ringi Number
						TDRUtil_mxJPO tdrUtil = new TDRUtil_mxJPO(context, args);
						MapList mlRingiDetails = tdrUtil.getRingiDetailsByRingiNo(context, strRingiNumber);
						String strMessage  = "";
						if(mlRingiDetails != null && mlRingiDetails.isEmpty() == false) {
							Map ringiDetailMap = (HashMap)mlRingiDetails.get(0);
							
							String strStatus = (String)ringiDetailMap.get("RNGI_STATUS");
							String strLevel = (String)ringiDetailMap.get("RNGI_LEVEL");
							String strAppDate = (String)ringiDetailMap.get("RNGI_APP_REJ_ON");
							
							strAppDate = strAppDate.substring(0, strAppDate.indexOf("."));
							
							String strMatrixDateFormat = eMatrixDateFormat.getEMatrixDateFormat();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date dAppDate = sdf.parse(strAppDate);
							strAppDate = dAppDate.toString();
							
							SimpleDateFormat sdf1 = new SimpleDateFormat(strMatrixDateFormat);
							String strDate = sdf1.format(dAppDate);
							
							if("A".equals(strStatus)) {
								strMessage = "Success";
							}else {
								strMessage = "Failure";
							}
							
							if("A".equalsIgnoreCase(strStatus)) {
								strStatus = "Approved";
							}
							if("R".equalsIgnoreCase(strStatus)) {
								strStatus = "Rejected";
							}
							if("I".equalsIgnoreCase(strStatus)) {
								strStatus = "Initiated";
							}
							if("C".equalsIgnoreCase(strStatus)) {
								strStatus = "Cancelled";
							}
							
							if(UIUtil.isNullOrEmpty(strAppDate)) {
								strAppDate = "";
							}

							HashMap hmAttributeMap = new HashMap();
							
							if(UIUtil.isNotNullAndNotEmpty(strRingiType) && "CommercialRingi".equals(strRingiType)) {
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_LEVEL, strLevel);
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_APPROVED_DATE, strDate);
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_STATUS, strStatus);
							}else {
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_LEVEL, strLevel);
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_APPROVED_DATE, strDate);
								hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_STATUS, strStatus);
							}
							if(hmAttributeMap.isEmpty()==false) {
								doObject.setAttributeValues(context, hmAttributeMap);
							}
													
						}
						if(UIUtil.isNotNullAndNotEmpty(strMessage) && "Success".equalsIgnoreCase(strMessage) == true) {
							iReturn = 0;
						}else {
							emxContextUtil_mxJPO.mqlNotice(context, EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Alert.RingiNumberNotApproved"));
							iReturn = 1;
						}
					}
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}


	/**
	 * Method returns MapList of locations connected to RFQ
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRFQLocations(Context context, String[] args)throws Exception{
		MapList mlLocationList = new MapList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlLocationList = doObject.getRelatedObjects(context,RELATIONSHIP_TDR_RFQ_LOCATION,DomainObject.TYPE_LOCATION,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return mlLocationList;
	}


	/**
	 * Method returns StringList of locations connected to RFQ. This is used for excluding the Locations from search result while adding
	 * department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedLocations(Context context, String[] args)throws Exception{
		StringList slExcludeDeptList = new StringList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				MapList mlDeptList = doObject.getRelatedObjects(context,RELATIONSHIP_TDR_RFQ_LOCATION,DomainObject.TYPE_LOCATION,slObjectSelect,null,false,true,(short)1,null,null);

				if(mlDeptList != null && !mlDeptList.isEmpty()) {
					slExcludeDeptList = TDRUtil_mxJPO.toStringList(context, DomainObject.SELECT_ID, mlDeptList);
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slExcludeDeptList;
	}

	/**
	 * Method returns int value. This method will get Send email notifications to SC Department lead
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int sendNotificationToSCDeptLead(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add("to["+RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.id");
				slObjectSelect.add(DomainObject.SELECT_NAME);
				Map mObjectInfo = doObject.getInfo(context, slObjectSelect);
				String strSCDeptId = (String)doObject.getInfo(context, "to["+RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.id");
				if(UIUtil.isNotNullAndNotEmpty(strSCDeptId)) {
					String strSCDeptLeadId = TDRUtil_mxJPO.getSCDepartmentLead(context, strSCDeptId);
					if(UIUtil.isNotNullAndNotEmpty(strSCDeptLeadId)) {
						DomainObject doPerson = new DomainObject(strSCDeptLeadId);
						String strEmail = doPerson.getAttributeValue(context, DomainConstants.ATTRIBUTE_EMAIL_ADDRESS);
						String message = "RFQ '" +(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"' Reviewed and Sent";
						StringList ccList = new StringList("");
						StringList bccList= new StringList("");
						StringList toList= new StringList(context.getUser());
						StringList objectIdList= new StringList(strObjectId);
						String subject = "RFQ '" +(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"' Reviewed and Sent";
						String notifyType = "both";
						StringList replyTo = new StringList("");
						String fromAgent = context.getUser();
						String messageHTML = "";
						emxNotificationUtil_mxJPO.sendJavaMail(context, toList, null, null, subject, message, messageHTML, fromAgent, null, objectIdList, notifyType);
					}
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return 0;
	}


	/**
	 * Method returns int value. This method will get Send email notifications to SC Department lead
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int sendNotificationToSuppliers(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add("from["+TDRConstants_mxJPO.RELATIONSHIP_RFQ_SUPPLIER+"].to.id");
				slObjectSelect.add(DomainObject.SELECT_NAME);
				Map mObjectInfo = doObject.getInfo(context, slObjectSelect);
				String strSupplierId = (String)doObject.getInfo(context, "from["+TDRConstants_mxJPO.RELATIONSHIP_RFQ_SUPPLIER+"].to.id");
				if(UIUtil.isNotNullAndNotEmpty(strSupplierId)) {
					DomainObject doSupplier = new  DomainObject(strSupplierId);
					StringList slSelect = new StringList();
					slSelect.add(DomainObject.SELECT_ID);
					slSelect.add(DomainObject.SELECT_NAME);
					slSelect.add("attribute["+DomainObject.ATTRIBUTE_EMAIL_ADDRESS+"].value");
					String strWhere = DomainObject.SELECT_CURRENT +"=="+DomainConstants.STATE_PERSON_ACTIVE;
					MapList mlMemberList = doSupplier.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_MEMBER,DomainObject.TYPE_PERSON,slSelect,null,false,true,(short)1,strWhere,null);
					StringList toList= new StringList();
					Map tempMap = null;
					String strEmail = DomainConstants.EMPTY_STRING;
					for(int i=0;i<mlMemberList.size();i++) {
						tempMap = (Map)mlMemberList.get(i);
						strEmail = (String)tempMap.get("attribute["+DomainObject.ATTRIBUTE_EMAIL_ADDRESS+"].value");
						if(UIUtil.isNotNullAndNotEmpty(strEmail)) {
							toList.add((String)tempMap.get(DomainObject.SELECT_NAME));
						}
					}					
					String message = "RFQ '" +(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"' Reviewed and Sent";
					StringList ccList = new StringList("");
					StringList bccList= new StringList("");
					StringList objectIdList= new StringList(strObjectId);
					String subject = "RFQ '" +(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"' Reviewed and Sent";
					String notifyType = "both";
					StringList replyTo = new StringList("");
					String fromAgent = context.getUser();
					String messageHTML = "";
					emxNotificationUtil_mxJPO.sendJavaMail(context, toList, null, null, subject, message, messageHTML, fromAgent, null, objectIdList, notifyType);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return 0;
	}


	/**
	 * showInternalDocumentCommand: Returns true or false based on channel
	 * in the search page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request data.
	 * @throws MatrixException if operations on DomainObject fail.
	 */
	public boolean showInternalDocumentCommand(Context context, String[] args) throws Exception
	{
		boolean showCommand = false;
		try
		{
			Map programMap    = (Map)JPO.unpackArgs(args);
			String strPortalCmdName    = (String) programMap.get("portalCmdName");
			if(UIUtil.isNotNullAndNotEmpty(strPortalCmdName) && "TDRInternalDocument".equalsIgnoreCase(strPortalCmdName))
			{
				showCommand = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return showCommand;
	}
	/**
	 * showExternalDocumentCommand: Returns true or false based on channel
	 * in the search page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request data.
	 * @throws MatrixException if operations on DomainObject fail.
	 */
	public boolean showExternalDocumentCommand(Context context, String[] args) throws Exception
	{
		boolean showCommand = false;
		try
		{
			Map programMap    = (Map)JPO.unpackArgs(args);
			String strPortalCmdName    = (String) programMap.get("portalCmdName");
			if(UIUtil.isNotNullAndNotEmpty(strPortalCmdName) && "TDRExternalDocument".equalsIgnoreCase(strPortalCmdName))
			{
				showCommand = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return showCommand;
	}

	/**
	 * Gets all the attachments associated for the Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getInternalDocumentAttachments(Context context, String[] args)
			throws Exception
	{
		MapList mlAttachementList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				StringBuffer sbBusWhere = new StringBuffer(DomainObject.SELECT_TYPE+"!="+TDRConstants_mxJPO.TYPE_TDR_EXTERNAL_RFQ_DOCUMENT);
				String strExternalDocTypes = MqlUtil.mqlCommand(context, "print type $1 select derivative dump $2",TDRConstants_mxJPO.TYPE_TDR_EXTERNAL_RFQ_DOCUMENT,"|");
				StringList slExternalDocTypes = FrameworkUtil.split(strExternalDocTypes, "|");
				for(Object object : slExternalDocTypes){
					sbBusWhere.append("&& "+DomainObject.SELECT_TYPE+"!="+(String)object);
				}

				String strTypePattern  = TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT+","+DomainConstants.TYPE_DOCUMENT;
				mlAttachementList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,strTypePattern,slObjectSelect,slRelSelect,false,true,(short)1,sbBusWhere.toString(),null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAttachementList;
		}
	}

	/**
	 * Gets all the attachments associated for the Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExternalDocumentAttachments(Context context, String[] args)
			throws Exception
	{
		MapList mlAttachementList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			DomainObject doObject = DomainObject.newInstance(context);
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				doObject.setId(strObjectId);
				if(doObject.isKindOf(context, DomainObject.TYPE_RTS_QUOTATION)){
					strObjectId = (String)doObject.getInfo(context, "to["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].from.id");
					doObject.setId(strObjectId);
				}
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlAttachementList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,TDRConstants_mxJPO.TYPE_TDR_EXTERNAL_RFQ_DOCUMENT,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAttachementList;
		}
	}


	/**
	 * Gets all the RFQ Template Quotation Documents
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRFQTemplateQuoationDocuments(Context context, String[] args)
			throws Exception
	{
		MapList mlQuotationDocumentList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlQuotationDocumentList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS,TDRConstants_mxJPO.TYPE_TDR_QUOTATION_DOCUMENT_TYPE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlQuotationDocumentList;
		}
	}

	/**
	 * Gets all the Quotation Documents type not connected with RFQ Template/RFQ
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllQuotationDocumentTypes(Context context, String[] args)
			throws Exception
	{
		MapList mlQuotationDocumentList = new MapList();
		MapList mlAllQuotationDocType = new MapList();
		try
		{
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			mlAllQuotationDocType = DomainObject.findObjects(context, TDRConstants_mxJPO.TYPE_TDR_QUOTATION_DOCUMENT_TYPE, VAULT_E_SERVICE_PRODUCTION,null,slObjectSelect);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);		

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlQuotationDocumentList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS,TDRConstants_mxJPO.TYPE_TDR_QUOTATION_DOCUMENT_TYPE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
				StringList slQuotationDocList = TDRUtil_mxJPO.toStringList(context, DomainObject.SELECT_ID, mlQuotationDocumentList);

				String strConnectedQuoteId = DomainConstants.EMPTY_STRING;
				String strQuoteId = DomainConstants.EMPTY_STRING;
				Map tempMap = null;
				for(int i=0;i<slQuotationDocList.size();i++) {
					strConnectedQuoteId = (String)slQuotationDocList.get(i);

					for(int j=0;j<mlAllQuotationDocType.size();j++) {
						tempMap = (Map)mlAllQuotationDocType.get(j);
						strQuoteId = (String)tempMap.get(DomainObject.SELECT_ID);
						if(UIUtil.isNotNullAndNotEmpty(strConnectedQuoteId) && UIUtil.isNotNullAndNotEmpty(strQuoteId) && strQuoteId.equals(strConnectedQuoteId)) {
							mlAllQuotationDocType.remove(j);
						}
					}

				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAllQuotationDocType;
		}
	}


	/**
	 * Method returns int value. this method will connect all default Quotation document type to RFQ fro RFQ template
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int connectMandatoryQuotationTypes(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strFromObjectId = (String)args[0];
			String strToObjectId = (String)args[1];
			if(UIUtil.isNotNullAndNotEmpty(strFromObjectId) && UIUtil.isNotNullAndNotEmpty(strToObjectId)) {
				DomainObject doRFQ = new DomainObject(strFromObjectId);
				DomainObject doRFQTemplate = new DomainObject(strToObjectId);
				StringList slMadatoryQuotationTypes = doRFQTemplate.getInfoList(context, "from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS+"].to.id");
				DomainObject doMandQuoteType = null;
				for(String strQuoteType: slMadatoryQuotationTypes) {
					DomainRelationship.connect(context, doRFQ, TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS, new DomainObject(strQuoteType));
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}


	/**
	 * Method returns int value. this method will disconnect all default Quotation document type to RFQ fro RFQ template
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int disconnectMandatoryQuotationTypes(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strFromObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strFromObjectId)) {
				DomainObject doRFQ = new DomainObject(strFromObjectId);
				StringList slMadatoryQuotationRels = doRFQ.getInfoList(context, "from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS+"].id");
				for(String strQuoteRel: slMadatoryQuotationRels) {
					DomainRelationship.disconnect(context, strQuoteRel);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}


	/**
	 * Method returns int value. This method will updated TDRRoundType attribute based on the assignment of person
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int updateRoundType(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				Vector vAssignments = PersonUtil.getAssignments(context);
				String strRoundTypeValue = DomainConstants.EMPTY_STRING;
				if(vAssignments.isEmpty() == false && vAssignments.contains(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER)) {
					strRoundTypeValue = "Commercial";
				}else if(vAssignments.isEmpty() == false && vAssignments.contains(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER)) {
					strRoundTypeValue = "Technical";
				}else if(vAssignments.isEmpty() == false && vAssignments.contains(TDRConstants_mxJPO.ROLE_RM_BUYER)) {
					strRoundTypeValue = "RM";
				}else if(vAssignments.isEmpty() == false && vAssignments.contains(TDRConstants_mxJPO.ROLE_CIVIL_BUYER)){
					strRoundTypeValue = "Civil";
				}
				if(UIUtil.isNotNullAndNotEmpty(strRoundTypeValue))
					doRFQ.setAttributeValue(context,TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE, strRoundTypeValue);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}
	/**
	 * showTechnicalDocumentCommand: Returns true or false based on channel
	 * in the search page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request data.
	 * @throws MatrixException if operations on DomainObject fail.
	 */
	public boolean showTechnicalDocumentCommand(Context context, String[] args) throws Exception
	{
		boolean showCommand = false;
		try
		{
			Map programMap    = (Map)JPO.unpackArgs(args);
			String strPortalCmdName    = (String) programMap.get("portalCmdName");
			if(UIUtil.isNotNullAndNotEmpty(strPortalCmdName) && "TDRTechnicalDocument".equalsIgnoreCase(strPortalCmdName))
			{
				showCommand = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return showCommand;
	}
	/**
	 * showCommercialDocumentCommand: Returns true or false based on channel
	 * in the search page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request data.
	 * @throws MatrixException if operations on DomainObject fail.
	 */
	public boolean showCommercialDocumentCommand(Context context, String[] args) throws Exception
	{
		boolean showCommand = false;
		try
		{
			Map programMap    = (Map)JPO.unpackArgs(args);
			String strPortalCmdName    = (String) programMap.get("portalCmdName");
			if(UIUtil.isNotNullAndNotEmpty(strPortalCmdName) && "TDRCommercialDocument".equalsIgnoreCase(strPortalCmdName))
			{
				showCommand = true;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return showCommand;
	}

	/**
	 * Gets all the attachments associated for the Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTechnicalDocumentAttachments(Context context, String[] args)
			throws Exception
	{
		MapList mlAttachementList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlAttachementList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,TDRConstants_mxJPO.TYPE_TDR_TECHNICAL_DOCUMENT,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAttachementList;
		}
	}

	/**
	 * Gets all the attachments associated for the Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCommercialDocumentAttachments(Context context, String[] args)
			throws Exception
	{
		MapList mlAttachementList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlAttachementList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,TDRConstants_mxJPO.TYPE_TDR_COMMERCIAL_DOCUMENT,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAttachementList;
		}
	}


	/* Gets RFQ Quotation Name
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>StringList</code>contains all the names
	 * @throws Exception if the operation fails
	 */
	public StringList getRFQQuotationName(Context context, String[] args)throws Exception{
		StringList slNameList = new StringList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			Map tempMap = null;
			if(mlObjectList != null && mlObjectList.isEmpty() == false) {
				String strName = EMPTY_STRING;
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strName = (String)tempMap.get(DomainObject.SELECT_NAME);
					if(UIUtil.isNotNullAndNotEmpty(strName)) {
						strName = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), strName);
						slNameList.add(strName);
					}
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slNameList;

	}

	/**
	 * Method returns int value. This method check that Supplier has connected document of type which is connected to RFQ.
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int checkSupplierConnectedRFQDocument(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				String strMessage = DomainObject.EMPTY_STRING;
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slQuotationDocumentTypes = (StringList)doObject.getInfoList(context, "to["+DomainConstants.RELATIONSHIP_RTS_QUOTATION+"].from.from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS+"].to.name");
				if(null != slQuotationDocumentTypes && slQuotationDocumentTypes.size()>0){					
					String strQuotationDocumentTypePattern = FrameworkUtil.join( slQuotationDocumentTypes, ",");
					StringList slObjectSelect = new StringList(DomainObject.SELECT_TYPE);
					MapList mlQuotationDocuments = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,strQuotationDocumentTypePattern,slObjectSelect,null,false,true,(short)1,null,null);					
					int iSize = mlQuotationDocuments.size();
					Map mQuotationDocument = null;
					StringList slDocumentTypes = TDRUtil_mxJPO.toStringList(context, DomainObject.SELECT_TYPE, mlQuotationDocuments);		
					String strDocList = "";
					String strDocName = "";
					for(Object object : slQuotationDocumentTypes){
						if(!slDocumentTypes.contains((String)object)){
							strDocName = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), (String)object);
							if(strDocList.length()==0) {
								strDocList = strDocName;
							}else {
								strDocList = strDocList + ", "+strDocName;
							}
						}
					}
					
					if(UIUtil.isNotNullAndNotEmpty(strDocList) && strDocList.length() >0) {
						strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.RFQQuotationDocument");
						strMessage = strMessage + "\n"+strDocList;
						emxContextUtil_mxJPO.mqlNotice(context, strMessage);
						iReturn = 1;
						return iReturn;
					}
				}
				//				else{
				//					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.SupplierConnectedRFQDocument");
				//					${CLASS:emxContextUtil}.mqlNotice(context, strMessage);
				//					iReturn = 1;
				//				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return iReturn;
	}

	/***
	 * Edit Access Function :
	 * Only for Technical Buyer whoever has edit access on RFQ and RFQ is in Response Complete State and RFQ is latest revision.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries:
	 * ObjectId - The object Id of the context.
	 * @return StringList 
	 * @throws Exception if the operation fails
	 */
	public StringList hasAccessEditTechReviewColumn(Context context, String[] args) throws Exception
	{
		StringList slReturnList = new StringList();
		String strReturnStatus = "false";
		try {
			String strContextUser = context.getUser();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			Map requestMap = (Map) programMap.get("requestMap");
			String strParentOID = (String) requestMap.get("parentOID");
			boolean bAccess = hasTecnicalBuyerAccessOnRFQ(context, strParentOID);
			StringList slObjectSelect = new StringList();
			slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value");
			slObjectSelect.add(DomainConstants.SELECT_OWNER);
			slObjectSelect.add("attribute["+DomainConstants.ATTRIBUTE_CO_OWNER+"].value");
			if (bAccess){
				DomainObject doObject = new DomainObject(strParentOID);
				Map mRFQInfo = (Map)doObject.getInfo(context, slObjectSelect);
				String strTechReviewStatus = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value");
				String strOwner = (String)mRFQInfo.get(DomainConstants.SELECT_OWNER);
				String strCoOwner = (String)mRFQInfo.get("attribute["+DomainConstants.ATTRIBUTE_CO_OWNER+"].value");
				if(!"Complete".equalsIgnoreCase(strTechReviewStatus) && ((strContextUser.equals(strOwner) || (UIUtil.isNotNullAndNotEmpty(strCoOwner) && strCoOwner.contains(strContextUser)))))
					strReturnStatus = "true";
			}
			else{				
				strReturnStatus = "false";
			}
			int iSize = objList.size();
			for (int iCount = 0; iCount < iSize; iCount++) {
				slReturnList.add(strReturnStatus);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slReturnList;
	}
	public boolean showSendCommercialNegotiationCommand(Context context, String[] args) throws Exception {
		boolean showCommand = false;
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strParentOID = (String) programMap.get("parentOID");
			showCommand = hasTecnicalBuyerAccessOnRFQ(context, strParentOID);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return showCommand;
	}

	private boolean hasTecnicalBuyerAccessOnRFQ(Context context, String strRFQId) throws Exception {
		boolean bAccess = false;
		try {
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainConstants.SELECT_LAST_ID);
			slObjectSelect.add(DomainConstants.SELECT_CURRENT);
			slObjectSelect.add(DomainConstants.SELECT_OWNER);
			String strContextUser = context.getUser();
			if (UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				DomainObject doRFQ = DomainObject.newInstance(context, strRFQId);
				Map mRFQ = (Map) doRFQ.getInfo(context, slObjectSelect);
				String strCurrent = (String) mRFQ.get(DomainConstants.SELECT_CURRENT);
				String strLastId = (String) mRFQ.get(DomainConstants.SELECT_LAST_ID);
				String strOwner = (String) mRFQ.get(DomainConstants.SELECT_OWNER);
				if (RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE.equalsIgnoreCase(strCurrent) && (strContextUser.equals(strOwner) || context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER)) && strLastId.equalsIgnoreCase(strRFQId))
					bAccess = true;
				else
					bAccess = false;
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return bAccess;
	}

	/**
	 * Method returns Maplist containing Pending RFQs
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getPeningRFQForOpenQuoteDashboard(Context context, String[] args)throws Exception{
		MapList mlPendingRFQList = new MapList();
		try {
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE+"]");

			String strWhere = "current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"' && attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value == '' && attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value == 'Complete'";
			mlPendingRFQList =  DomainObject.findObjects(context,DomainObject.TYPE_REQUEST_TO_SUPPLIER,VAULT_E_SERVICE_PRODUCTION,strWhere,slObjectSelect);

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mlPendingRFQList;
	} 

	/**
	 * Method returns Maplist containing Open RFQs
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getOpenRFQForOpenQuoteDashboard(Context context, String[] args)throws Exception{
		MapList mlOpenRFQList = new MapList();
		try {
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			String strPersonID = PersonUtil.getPersonObjectID(context);

			String strWhere = "current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"' && attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value == 'Complete' && to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.id=="+strPersonID;
			mlOpenRFQList =  DomainObject.findObjects(context,DomainObject.TYPE_REQUEST_TO_SUPPLIER,VAULT_E_SERVICE_PRODUCTION,strWhere,slObjectSelect);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mlOpenRFQList;
	}



	/**
	 * Method returns Maplist containing Department RFQs
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRFQForDepartmentDashboard(Context context, String[] args)throws Exception{
		MapList mlDeptRFQList = new MapList();
		try {
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			slObjectSelect.add(DomainObject.SELECT_OWNER);
			slObjectSelect.add(DomainObject.SELECT_CURRENT);
			slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value");
			slObjectSelect.add("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "].value");
			String strPersonID = PersonUtil.getPersonObjectID(context);

			StringBuffer sbWhere = new StringBuffer();
//			sbWhere.append("owner != '"+context.getUser()+"'");
//			sbWhere.append(" && current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"'");
			sbWhere.append("current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"'");
			sbWhere.append(" && attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"] == 'Complete'");
			sbWhere.append(" && from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"] == false");
			mlDeptRFQList =  DomainObject.findObjects(context,DomainObject.TYPE_REQUEST_TO_SUPPLIER,VAULT_E_SERVICE_PRODUCTION,sbWhere.toString(),slObjectSelect);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mlDeptRFQList;
	}

	public String sendCommercialNegotiation(Context context, String[] args) throws Exception {
		String strReturnStatus = DomainConstants.EMPTY_STRING;
		boolean bHasTechReviewResultEmpty = false;
		String strCompleteNotice = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",context.getLocale(), "TDR.Command.Notice.SendCommercialNegotiation");
		String strTechReviewStatusBlankNotice = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",context.getLocale(), "TDR.Command.Notice.TechReviewStatusBlank");
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectOID = (String) programMap.get("objectId");

			MapList mlRFQQuotationList = new MapList();
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainConstants.SELECT_LAST_ID);
			slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");

			if (UIUtil.isNotNullAndNotEmpty(strObjectOID)) {
				String strTechReviewStatus = DomainConstants.EMPTY_STRING;
				DomainObject doRFQ = DomainObject.newInstance(context, strObjectOID);
				DomainObject doQuotation = DomainObject.newInstance(context);
				strTechReviewStatus = (String)doRFQ.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
				mlRFQQuotationList = doRFQ.getRelatedObjects(context, DomainConstants.RELATIONSHIP_RTS_QUOTATION,
						DomainObject.TYPE_RTS_QUOTATION, slObjectSelect, null, false, true, (short) 1, null, null);
				int iSize = mlRFQQuotationList.size();
				Map tempMap = null;
				String strRFQQuotationId = null;
				String strTechReviewResult = null;
				for (int iCount = 0; iCount < iSize; iCount++) {
					tempMap = (Map) mlRFQQuotationList.get(iCount);
					strRFQQuotationId = (String) tempMap.get(DomainConstants.SELECT_LAST_ID);
					strTechReviewResult = (String) tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");
					if ("".equalsIgnoreCase(strTechReviewResult)) {
						if(context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER)){
							doQuotation.setId(strRFQQuotationId);
							ContextUtil.pushContext(context);
							doQuotation.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT, "OK");
							ContextUtil.popContext(context);
						}else{
							bHasTechReviewResultEmpty = true;
							break;
						}
					}
				}
				if (bHasTechReviewResultEmpty) {
					strReturnStatus = strTechReviewStatusBlankNotice;
				}else{					
					if (!"Complete".equalsIgnoreCase(strTechReviewStatus)) {
						SimpleDateFormat sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), context.getLocale());
						Date todayDate = new Date();
						String strTodayDate = (String)sdf1.format(todayDate);
						Map hmAttributeList = new HashMap();
						hmAttributeList.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS, "Complete");
						hmAttributeList.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE, strTodayDate);
						ContextUtil.pushContext(context);
						doRFQ.setAttributeValues(context, hmAttributeList);
						ContextUtil.popContext(context);
						String strRevisionIds = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",strObjectOID,"revisions.id","|");
						StringList slRevisionsId = FrameworkUtil.split(strRevisionIds, "|");
						slRevisionsId.remove(strObjectOID);						
						for(int i=0;i<slRevisionsId.size();i++) {
							doRFQ.setId((String)slRevisionsId.get(i));
							strTechReviewStatus = (String)doRFQ.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
							if (!"Complete".equalsIgnoreCase(strTechReviewStatus)) {
								doRFQ.setAttributeValues(context, hmAttributeList);
							}
						}
					}else{
						strReturnStatus = strCompleteNotice;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strReturnStatus;
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList updateDynamicAttributes(Context context, String[] args) throws Exception {
		MapList fieldMapList = new MapList();
		try {
			fieldMapList = JPO.invoke(context, "SourcingRFQ", null, "updateDynamicAttributes", args, MapList.class);
			Map tempMap = null;
			Map settingMap = null;
			int iSize = fieldMapList.size();
			for (int iCount = 0; iCount < iSize; iCount++) {
				tempMap = (Map) fieldMapList.get(iCount);
				settingMap = (Map) tempMap.get("settings");
				if (settingMap != null) {
					if(TDRConstants_mxJPO.ATTRIBUTE_TDR_PO_NUMBER.equals((String)tempMap.get("name"))){						
						settingMap.put("Editable", "true");
						settingMap.put("Field Type", "attribute");
					}
					else{						
						settingMap.put("Editable", "false");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fieldMapList;
	}

	public String openPendingQuotation(Context context, String[] args) throws Exception {
		StringBuffer sbReturn = new StringBuffer();
		try {
			String strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.OpenCommercialRFQPending.Subject");
			String strSelectedObject = "";
			String strObjectId = "";
			DomainObject doObject = DomainObject.newInstance(context);
			StringList slObjectIds = new StringList();
			StringList slObjectListIds = new StringList();
			String strRFQName = DomainObject.EMPTY_STRING;
			for(int iCount=0; iCount<args.length;iCount++){
				strSelectedObject = (String)args[iCount];
				slObjectIds = FrameworkUtil.split(strSelectedObject,"|");
				strObjectId = (String)slObjectIds.get(0);
				if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
					slObjectListIds.add(strObjectId);
					doObject.setId(strObjectId);
					SimpleDateFormat sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), context.getLocale());
					Date todayDate = new Date();
					String strTodayDate = (String)sdf1.format(todayDate);
					Map mapAttribute = new HashMap();
					mapAttribute.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS, "Complete");
					mapAttribute.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE, strTodayDate);
					doObject.setAttributeValues(context, mapAttribute);
					strRFQName = (String)doObject.getInfo(context, DomainObject.SELECT_NAME);
					sbReturn.append(strRFQName);
					if(iCount != (args.length-1)){
						sbReturn.append(", ");
					}
				}
			}	
			if( slObjectListIds.size() > 0){	    		
				TDRUtil_mxJPO.sendNotificationTo(context,slObjectListIds, strSubject);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return sbReturn.toString();
	}

	public Vector getConcernedSCDepartmentColumnValue(Context context , String[] args) throws Exception
	{
		Vector vcReturn =new Vector();
		Map programMap                 = (Map)JPO.unpackArgs(args);
		Map paramList                  = (Map)programMap.get("paramList");
		MapList objectList             = (MapList) programMap.get("objectList");
		int iobjectListSize            = objectList.size();
		String strObjectId = "";
		HashMap paramMap = new HashMap();
		HashMap argMap = new HashMap();
		for (int i=0; i < iobjectListSize; i++)
		{
			Map map     = (Map) objectList.get(i);
			strObjectId = (String)map.get(DomainConstants.SELECT_ID);			
			paramMap.put("objectId",strObjectId);
			argMap.put("paramMap", paramMap);
			String[] methodargs = JPO.packArgs(argMap);
			vcReturn.add((String)getConcernedSCDepartment(context, methodargs));
		}
		return vcReturn;
	}

	public int takeOwnership(Context context, String[] args) throws Exception {
		int iReturnStatus = 0;
		try {
			String strSelectedObject = "";
			String strObjectId = "";
			String strOwner = "";
			String strRelId = "";
			HashMap hmOwnerMap = new HashMap();
			DomainObject doObject = DomainObject.newInstance(context);
			StringList slObjectIds = new StringList();
			StringList slObjectListIds = new StringList();
			com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
			String personId = person.getId(context);
			StringList toList= new StringList();
			StringList slMembersList = new StringList();
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			slObjectSelect.add(DomainObject.SELECT_OWNER);
			slObjectSelect.add("from[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER + "].id");
			Map mRFQInfo = null;
			for(int iCount=0; iCount<args.length;iCount++){
				strSelectedObject = (String)args[iCount];
				slObjectIds = FrameworkUtil.split(strSelectedObject,"|");
				strObjectId = (String)slObjectIds.get(0);
				if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
					doObject.setId(strObjectId);
					mRFQInfo = (Map)doObject.getInfo(context, slObjectSelect);	
					strOwner = (String)mRFQInfo.get(DomainObject.SELECT_OWNER);	   
					strRelId = (String)mRFQInfo.get("from[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER + "].id");

					ContextUtil.pushContext(context);
					doObject.changeOwner(context, person.getName());
					doObject.setAttributeValue(context, DomainObject.ATTRIBUTE_CO_OWNER, DomainConstants.EMPTY_STRING);
					ContextUtil.popContext(context);

					if(UIUtil.isNotNullAndNotEmpty(strRelId)){
						DomainRelationship.modifyTo(context, strRelId, new DomainObject(personId));
					}else{	    				
						DomainRelationship.connect(context,strObjectId,TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER,personId,true);
					}	
					strOwner = person.getName();
					slMembersList = TDRUtil_mxJPO.getPeopleFromMemberList(context, strObjectId);
					mRFQInfo.put("members", slMembersList);
					toList.add(strOwner);
					toList.addAll(slMembersList);
					HashSet<String> hsToList = new HashSet<String>(toList);
					for (String toMembers : toList) {						
						if(hmOwnerMap.containsKey(toMembers))
						{
							MapList mapListComp = (MapList) hmOwnerMap.get(toMembers);
							mapListComp.add(mRFQInfo);
							hmOwnerMap.put(toMembers, mapListComp);
						}else{
							MapList temp = new MapList();
							temp.add(mRFQInfo);
							hmOwnerMap.put(toMembers, temp);
						}
					}
				}
			}

			String strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.TakeOwnership.Subject");
			String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.TakeOwnership.Message");
			strSubject = context.getUser()+ " " +strSubject;
			strMessage = context.getUser()+ " " +strMessage;
			Iterator itrOwnerMap = hmOwnerMap.keySet().iterator();
			while(itrOwnerMap.hasNext()) {
				String keyOwnerName = (String) itrOwnerMap.next();
				MapList mlObjectList = (MapList)hmOwnerMap.get(keyOwnerName);
				StringList slObjectList = new StringList();			
				String strToEmail = PersonUtil.getEmail(context, keyOwnerName);

				StringBuffer sbHTMLBody = new StringBuffer();
				sbHTMLBody.append("<html>");
				sbHTMLBody.append(
						"<head><style>.datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden;}.datagrid table td, .datagrid table th { padding: 3px 10px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; color:#FFFFFF; font-size: 13px; font-weight: bold; border-left: 1px solid #0070A8; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #00557F; border-left: 1px solid #E1EEF4;font-size: 12px;font-weight: normal; }.datagrid table tbody .alt td { background: #E1EEf4; color: #00557F; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }</style></head>");
				sbHTMLBody.append("<body>");
				sbHTMLBody.append("<div class='datagrid'>");
				sbHTMLBody.append("<b>Dear User , <b> <br><br>");
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append(strMessage);
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append("<center>");
				sbHTMLBody.append("<table width='100%' border='1'>");
				sbHTMLBody.append("<thead>");
				sbHTMLBody.append(
						"<tr align='center'><th width='5%'><b>S.No</b></th><th width='10%'><b>RFQ Name</b></th><th width='15%'><b>New Owner</b></th></tr>");
				sbHTMLBody.append("</thead>");
				sbHTMLBody.append("<tbody>");

				int iSizeRFQList = mlObjectList.size();
				Map mapRFQ = null;
				for (int iCount = 0; iCount < iSizeRFQList;) {
					mapRFQ = (Map) mlObjectList.get(iCount);
					iCount++;
					String sRFQId = (String) mapRFQ.get(DomainObject.SELECT_ID);
					slObjectList.add(sRFQId);
					String sName = (String) mapRFQ.get(DomainObject.SELECT_NAME);
					String sOwner = (String) mapRFQ.get(DomainObject.SELECT_OWNER);
					sbHTMLBody.append("<tr><th width='5%'>" + iCount + "</th><th width='10%'>" + sName
							+ "</th><th width='15%'>" + sOwner + "</th></tr>");
				}
				//MailUtil.sendMessage(context,new StringList(keyOwnerName),null,null,strSubject,strMessage,slObjectList);
				String fromAgent = context.getUser();
				String notifyType = "both";
				emxNotificationUtil_mxJPO.sendJavaMail(context, new StringList(keyOwnerName), null, null, strSubject, strMessage, sbHTMLBody.toString(), fromAgent, null, slObjectList, notifyType);
			}	    	
		} catch (Exception ex) {
			iReturnStatus = 0;
			ex.printStackTrace();
			throw ex;
		}
		return iReturnStatus;
	}

	/**
	 * Method returns int value. This method will send email notifications to all SC Department members and RFQ owner
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int sendNotificationToAllSCDeptMembers(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strRFQId = args[1];
			String strDepartmentId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)){		
				String strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.OpenCommercialRFQPending.Subject");
				TDRUtil_mxJPO.sendNotificationTo(context, new StringList(strRFQId), strSubject);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return 0;
	}

	/**
	 * Method for Cron Job to send reminders if open Commercial RFQ is pending for certain duration.
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public MapList cronJobPeningRFQForOpenQuote(Context context, String[] args)throws Exception{
		MapList mlPendingRFQList = new MapList();
		try {
			StringList slPendingCommercialRFQ = new StringList();
			String strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.PendingRFQ.Subject");
			String strDuration = _classCurrencyConfig.getProperty("TDR.CronJOB.OpenCommercialRFQPending.Duration");		
			strSubject = strSubject +" "+strDuration+" day(s)";
			int iDuration = Integer.parseInt(strDuration);
			SimpleDateFormat sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), context.getLocale());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -iDuration);

			String strTodayDate = (String)sdf1.format(cal.getTime());  
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append("current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"'");
			sbWhere.append(" && ");
			sbWhere.append("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value == ''");
			sbWhere.append(" && ");
			sbWhere.append("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value == 'Complete'");
			sbWhere.append(" && ");
			sbWhere.append("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE+"].value >= '"+strTodayDate+"'");

			mlPendingRFQList =  DomainObject.findObjects(context,DomainObject.TYPE_REQUEST_TO_SUPPLIER,VAULT_E_SERVICE_PRODUCTION,sbWhere.toString(),slObjectSelect);
			int iSize = mlPendingRFQList.size();

			if(iSize>0){
				Map mapTemp = null;
				String strTechReviewCompletionDate = "";
				for(int iCount = 0; iCount < iSize ; iCount++){
					mapTemp = (Map)mlPendingRFQList.get(iCount);
					slPendingCommercialRFQ.add((String)mapTemp.get(DomainConstants.SELECT_ID));
				}
				if(slPendingCommercialRFQ != null && slPendingCommercialRFQ.size()>0){
					System.out.println("Sending mail.......");	
					TDRUtil_mxJPO.sendNotificationTo(context, slPendingCommercialRFQ, strSubject);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mlPendingRFQList;
	} 


	/**
	 * Method returns int value. This method will Updated Round type attribute to Commercial on new revision
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int updateTDRAttributesOnRound(Context context, String[] args)throws Exception{
		try {
			String strRFQId = args[0];
			RequestToSupplier doRFQ = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
			if (UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				doRFQ.setId(strRFQId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_LAST_ID);
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
				slObjectSelect
				.add("from[" + DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE + "].to." + DomainObject.SELECT_ID);
				Map mapRFQ = (Map)doRFQ.getInfo(context,slObjectSelect);
				String strTechReviewStatus = (String)mapRFQ.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
				String strRevisedRFQId = (String)mapRFQ.get(DomainObject.SELECT_LAST_ID);

				if(UIUtil.isNotNullAndNotEmpty(strTechReviewStatus) && "Complete".equals(strTechReviewStatus)){	
					String strWhereCondition = "attribute[" + TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+ "] == NG";
					MapList mlQuotationList = doRFQ.getRelatedObjects(context, DomainObject.RELATIONSHIP_RTS_QUOTATION,DomainObject.TYPE_RTS_QUOTATION, slObjectSelect, null, false, true, (short) 1,strWhereCondition, null);
					if (mlQuotationList != null && mlQuotationList.size() > 0) {
						doRFQ.setId(strRevisedRFQId);
						StringList slSupplierCompanyIds = (StringList)TDRUtil_mxJPO.toStringList(context, "from[" + DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE + "].to." + DomainObject.SELECT_ID, mlQuotationList);
						int iSize = slSupplierCompanyIds.size();
						for (int iCount = 0; iCount < iSize; iCount++) {
							String strWhere = DomainObject.SELECT_ID + "=='" + (String)slSupplierCompanyIds.get(iCount) + "'";
							MapList mlSuplierList = doRFQ.getRelatedObjects(context, DomainObject.RELATIONSHIP_RTS_SUPPLIER,
									DomainObject.TYPE_COMPANY, null, new StringList(DomainRelationship.SELECT_ID), false,
									true, (short) 1, strWhere, null);
							if(mlSuplierList != null && mlSuplierList.size()>0){								
								StringList slSupplierRFQRelIds = (StringList)TDRUtil_mxJPO.toStringList(context, DomainRelationship.SELECT_ID, mlSuplierList);
								for(Object object : slSupplierRFQRelIds) {
									doRFQ.removeSupplier(context,(String)object);
								}
							}
						}
					}
				}

				doRFQ.setId(strRevisedRFQId);
				doRFQ.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE, "Commercial");
			}	 
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return 0;
	}

	/***
	 * Edit Access Function :
	 * If TechReviwstatus attribute value is complete, column will be non-editable or editable.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries:
	 * ObjectId - The object Id of the context.
	 * @return StringList 
	 * @throws Exception if the operation fails
	 */
	public StringList hasAccessEditSCDepartmentColumn(Context context, String[] args) throws Exception
	{
		StringList slReturnList = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			int iSize = objList.size();
			Map mObjList = null;
			String strOwner = null;
			String strCoOwner = null;
			String strCurrent = null;
			String strTechReviewStatus = null;
			for (int iCount = 0; iCount < iSize; iCount++) {
				mObjList = (Map) objList.get(iCount);
				strOwner = (String) mObjList.get(DomainObject.SELECT_OWNER);
				strCurrent = (String) mObjList.get(DomainObject.SELECT_CURRENT);
				strCoOwner = (String) mObjList.get("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "].value");
				strTechReviewStatus = (String) mObjList.get("attribute[" + TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS + "].value");
				if (strCurrent.equals(DomainObject.STATE_RESPONSE_COMPLETE) && !"Complete".equals(strTechReviewStatus) && (strOwner.equals((String)context.getUser()) || strCoOwner.equals((String)context.getUser())))
					slReturnList.add("true");
				else
					slReturnList.add("false");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slReturnList;
	}
	/**
	 * Check access on both RFQ Template and RFQ.
	 * Checks if the RFQ is in editable mode
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds packed HashMap containing objectId
	 * @return boolean indicating editability of RFQ
	 * @throws Exception if the operation fails
	 */
	public boolean showQuotationDocumentMenuCommand (Context context, String[] args) throws Exception
	{
		boolean showCmd=false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String)programMap.get("objectId");
		DomainObject domObject = DomainObject.newInstance(context);
		if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
			domObject.setId(strObjectId);
			if(domObject.isKindOf(context, DomainConstants.TYPE_RTS_TEMPLATE)){
				String strCurrent = (String)domObject.getInfo(context, DomainObject.SELECT_CURRENT);
				if("Inactive".equalsIgnoreCase(strCurrent)){
					showCmd=true;
				}
			}else if(domObject.isKindOf(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER)){
				showCmd=new SourcingLineItem_mxJPO(context, args).showLineItemMenuCommand(context, args);
			}
		}
		return showCmd;
	}


	/**
	 * Method returns int value. This method will Updated Round type attribute to Commercial on new revision
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int updateTechReviewResultsOnRound(Context context, String[] args)throws Exception{
		try {
			String strRFQId = args[0];
			RequestToSupplier doRFQ = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
			RTSQuotation rtsquotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
			if (UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				doRFQ.setId(strRFQId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainConstants.SELECT_ID);
				slObjectSelect.add("previous.id");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT_REMARKS+"].value");

				MapList mlQuotationList =doRFQ.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_RTS_QUOTATION,DomainObject.TYPE_RTS_QUOTATION,slObjectSelect,null,false,true,(short)1,null,null);

				String strPreviousId = (String)doRFQ.getInfo(context, "previous.id");				
				RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
				if(UIUtil.isNullOrEmpty(strPreviousId)) {
					strPreviousId = strRFQId;
				}
				rts.setId(strPreviousId);
				String strTechReviewStatus = (String)rts.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"].value");

				MapList mlQuotationListOfPrev =rts.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_RTS_QUOTATION,DomainObject.TYPE_RTS_QUOTATION,slObjectSelect,null,false,true,(short)1,null,null);
				String strTechReviewResult = DomainConstants.EMPTY_STRING;
				String strTechReviewResultRemark = DomainConstants.EMPTY_STRING;
				String strRFQQuoteId = DomainConstants.EMPTY_STRING;
				Map hmAttributeList = new HashMap();
				if(UIUtil.isNotNullAndNotEmpty(strTechReviewStatus) && "Complete".equals(strTechReviewStatus)) {
					for(int i=0;i<mlQuotationListOfPrev.size();i++) {
						Map mTempMap = (Map)mlQuotationListOfPrev.get(i);
						strRFQQuoteId = (String)mTempMap.get(DomainObject.SELECT_ID);
						strTechReviewResult = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");
						strTechReviewResultRemark = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT_REMARKS+"].value");
						for(int j=0;j<mlQuotationList.size();j++) {							
							Map mTempMap1 = (Map)mlQuotationList.get(j);
							if(strRFQQuoteId.equals((String)mTempMap1.get("previous.id"))){
								hmAttributeList = new HashMap();
								hmAttributeList.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT,strTechReviewResult);
								hmAttributeList.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT_REMARKS, strTechReviewResultRemark);
								rtsquotation.setId((String)mTempMap1.get(DomainObject.SELECT_ID));
								rtsquotation.setAttributeValues(context, hmAttributeList);
							}
						}
					}

				}else {
					String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Trigger.Notice.ResetTechReviewResult");
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
				}
			}	 
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return 0;
	}


	/**
	 * Method returns MapList of Member Lists  connected to RFQ
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRFQMemberList(Context context, String[] args)throws Exception{
		MapList mlMemberList = new MapList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);

				mlMemberList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_MEMBER_LIST,DomainObject.TYPE_MEMBER_LIST,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return mlMemberList;
	}

	/**
	 * Method returns StringList of MemberList connected to RFQ. This is used for excluding the MemberList from search result while adding
	 * MemberList. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedMemberList(Context context, String[] args)throws Exception{
		StringList slExcludeMemberList = new StringList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_TYPE);

				MapList mlDeptList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_MEMBER_LIST,DomainObject.TYPE_MEMBER_LIST,slObjectSelect,null,false,true,(short)1,null,null);

				if(mlDeptList != null && !mlDeptList.isEmpty()) {
					slExcludeMemberList = TDRUtil_mxJPO.toStringList(context, DomainObject.SELECT_ID, mlDeptList);
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slExcludeMemberList;
	}


	/**
	 * Method returns int value. This method will get Send email notifications to RFQ owner and co owner on Acknowledgement of Quotation
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public int sendNotificationOnQuotationAcknowledge(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strObjectId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				DomainObject.MULTI_VALUE_LIST.add("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
				StringList slObjectSelect = new StringList();
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ACKNOWLEDGE+"].value");
				slObjectSelect.add("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.id");
				slObjectSelect.add("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.owner");
				slObjectSelect.add("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");

				Map mObjectInfo = (Map)doObject.getInfo(context, slObjectSelect);
				String strAttrAcknowledge = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ACKNOWLEDGE+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strAttrAcknowledge) && "Yes".equalsIgnoreCase(strAttrAcknowledge)) {
					String strRFQId = (String)mObjectInfo.get("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.id");
					String strOwner = (String)mObjectInfo.get("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.owner");
					StringList slCoOwners = (StringList)mObjectInfo.get("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
					StringList slRFQMembersList = TDRUtil_mxJPO.getPeopleFromMemberList(context, strRFQId);
					StringList slToList = new StringList();
					slToList.add(strOwner);
					slToList.addAll(slCoOwners);
					slToList.addAll(slRFQMembersList);
					slToList.remove("");
					//Code for Sending Email to RFQ Owner, Co Owner and Member List
					String strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Quotation.Acknowledged.Subject");
					String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Quotation.Acknowledged.Message");
					MailUtil.sendMessage(context, slToList, null, null, strSubject,strMessage, new StringList(strObjectId));
					String fromAgent = context.getUser();
					String notifyType = "both";
					emxNotificationUtil_mxJPO.sendJavaMail(context, slToList, null, null, strSubject, strMessage, strMessage, fromAgent, null, new StringList(strObjectId), notifyType);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove("to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
		}

		return 0;
	}


	public boolean getAccessIfAcknowledged(Context context, String[] args)throws Exception{
		boolean bHasAccess = true;
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				String strAttrAcknowledge = (String)doObject.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_ACKNOWLEDGE);
				if(UIUtil.isNullOrEmpty(strAttrAcknowledge) || "No".equalsIgnoreCase(strAttrAcknowledge)) {
					bHasAccess = false;
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}	
		return bHasAccess;
	}

	/**
	 * Method for Cron Job to send reminders to Owner and Co-Owner of RFQ/Quotation which has Due date in number of offset days.
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public MapList cronJobDueDate(Context context, String[] args)throws Exception{
		MapList mlDueDateList = new MapList();
		String strType = args[0];
		System.out.println("strType  :::"+strType);
		String strSelectSupplier = DomainObject.EMPTY_STRING;
		String strSelectDueDate = DomainObject.EMPTY_STRING;
		String strSubject = DomainObject.EMPTY_STRING;
		String strMessage = DomainObject.EMPTY_STRING;
		try {
			if(strType.equals("RFQ")){
				strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.DueDateOffset.Subject");
				strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.DueDateOffset.Message");
				strSelectSupplier = "from[" + DomainObject.RELATIONSHIP_RTS_SUPPLIER+ "].to.name";
				strSelectDueDate = "attribute["+DomainConstants.ATTRIBUTE_QUOTE_REQUESTED_BY_DATE+"]";
			}else if(strType.equals("RFQ Quotation")){
				strSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Quotation.DueDateOffset.Subject");
				strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Quotation.DueDateOffset.Message");
				strSelectSupplier = "from[" + DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE+ "].to.name";
				strSelectDueDate = "to["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+DomainConstants.ATTRIBUTE_QUOTE_REQUESTED_BY_DATE+"]";
			}
			StringList slPendingCommercialRFQ = new StringList();
			String strDueDateOffset = _classCurrencyConfig.getProperty("TDR.CronJOB.DueDateOffset");		
			int iDueDateOffset = Integer.parseInt(strDueDateOffset);

			SimpleDateFormat sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), context.getLocale());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, iDueDateOffset);

			DomainObject.MULTI_VALUE_LIST.add(strSelectSupplier);
			DomainObject.MULTI_VALUE_LIST.add("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
			String strTodayDate = (String)sdf1.format(cal.getTime());  
			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			slObjectSelect.add(DomainObject.SELECT_OWNER);
			slObjectSelect.add(DomainObject.SELECT_DESCRIPTION);
			slObjectSelect.add(strSelectDueDate);
			slObjectSelect.add("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
			slObjectSelect.add(strSelectSupplier);

			strTodayDate = FrameworkUtil.split(strTodayDate, " ").get(0);
			if(strTodayDate.startsWith("0")){
				strTodayDate = strTodayDate.substring(1, strTodayDate.length());
			}

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(strSelectDueDate+" ~~ '*"+strTodayDate+"*'");
			mlDueDateList =  DomainObject.findObjects(context,strType,VAULT_E_SERVICE_PRODUCTION,sbWhere.toString(),slObjectSelect);
			int iSize = mlDueDateList.size();

			if(iSize>0){
				HashMap hmToList = new HashMap();
				String strObjectId = DomainObject.EMPTY_STRING;
				String strDueDate = DomainObject.EMPTY_STRING;
				String strOwner = DomainObject.EMPTY_STRING;
				StringList slCoOwners = null;
				StringList slMembersList = null;
				StringList slSuppliers = null;
				StringList toList = null;
				Map mapTemp = null;
				String strTechReviewCompletionDate = "";
				for(int iCount = 0; iCount < iSize ; iCount++){
					mapTemp = (Map)mlDueDateList.get(iCount);
					strObjectId = (String)mapTemp.get(DomainObject.SELECT_ID);
					strOwner = (String)mapTemp.get(DomainObject.SELECT_OWNER);
					slCoOwners = (StringList)mapTemp.get("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
					strDueDate = (String)mapTemp.get(strSelectDueDate);
					slSuppliers = (StringList)mapTemp.get(strSelectSupplier);
					slMembersList = TDRUtil_mxJPO.getPeopleFromMemberList(context, strObjectId);
					if(slSuppliers != null && slSuppliers.size()>0){
						mapTemp.put("suppliers", FrameworkUtil.join(slSuppliers, ","));
					}
					if(slMembersList != null && slMembersList.size()>0){
						mapTemp.put("members", slMembersList);
					}
					mapTemp.put("dueDate", strDueDate);
					toList = new StringList(strOwner);
					toList.addAll(slCoOwners);
					toList.addAll(slMembersList);
					HashSet<String> hsToList = new HashSet<String>(toList);
					for (String toMembers : toList) {
						if(UIUtil.isNotNullAndNotEmpty(toMembers)){							
							if (hmToList.containsKey(toMembers)) {
								MapList mapListComp = (MapList) hmToList.get(toMembers);
								mapListComp.add(mapTemp);
								hmToList.put(toMembers, mapListComp);
							} else {
								MapList temp = new MapList();
								temp.add(mapTemp);
								hmToList.put(toMembers, temp);
							}
						}
					}
				}
				TDRUtil_mxJPO.sendDueDateNotification(context, hmToList, strSubject+" "+strTodayDate, strMessage+" "+strTodayDate);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove(strSelectSupplier);
			DomainObject.MULTI_VALUE_LIST.remove("attribute[" + DomainObject.ATTRIBUTE_CO_OWNER + "]");
		}
		return mlDueDateList;
	}

	/**
	 * Method for Cron Job to automatically return quotations.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public MapList cronJobAutoReturnQuoteOnDueDatePass(Context context, String[] args)throws Exception{
		MapList mlReturnList = new MapList();

		try {
			String strDueDateOffset = _classCurrencyConfig.getProperty("TDR.CronJOB.AutoReturnQuoteOnDueDatePass");		
			int iDueDateOffset = Integer.parseInt(strDueDateOffset);

			SimpleDateFormat sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), context.getLocale());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -iDueDateOffset);
			String strTodayDate = (String)sdf1.format(cal.getTime());  

			DomainObject.MULTI_VALUE_LIST.add("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_ID);
			DomainObject.MULTI_VALUE_LIST.add("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_CURRENT);

			StringList slObjectSelect = new StringList();
			slObjectSelect.add(DomainObject.SELECT_ID);
			slObjectSelect.add(DomainObject.SELECT_NAME);
			slObjectSelect.add(DomainObject.SELECT_LAST_ID);
			slObjectSelect.add("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_ID);
			slObjectSelect.add("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_CURRENT);

			strTodayDate = FrameworkUtil.split(strTodayDate, " ").get(0);
			if(strTodayDate.startsWith("0")){
				strTodayDate = strTodayDate.substring(1, strTodayDate.length());
			}

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(DomainObject.SELECT_CURRENT+"=="+DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT);
			sbWhere.append(" && "+DomainObject.SELECT_ID+"=="+DomainObject.SELECT_LAST_ID);
			sbWhere.append(" && attribute["+DomainConstants.ATTRIBUTE_QUOTE_REQUESTED_BY_DATE+"] ~~ '*"+strTodayDate+"*'");
			mlReturnList =  DomainObject.findObjects(context,DomainObject.TYPE_RFQ,VAULT_E_SERVICE_PRODUCTION,sbWhere.toString(),slObjectSelect);
			int iSize = mlReturnList.size();			
			if(iSize>0){
				HashMap hmAttrMap = new HashMap();
				hmAttrMap.put(DomainObject.ATTRIBUTE_COMMENTS, "Regret - Due Date Passed");
				StringList slQuoatationIds = null;
				StringList slQuoatationCurrents = null;
				Map mapTemp = null;
				DomainObject doObject = DomainObject.newInstance(context);
				for(int iCount = 0; iCount < iSize ; iCount++){
					mapTemp = (Map)mlReturnList.get(iCount);
					slQuoatationIds = (StringList)mapTemp.get("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_ID);
					slQuoatationCurrents = (StringList)mapTemp.get("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_CURRENT);
					int jSize =  slQuoatationCurrents.size();
					for(int jCount = 0; jCount < jSize; jCount++){
						if(DomainObject.STATE_RTS_QUOTATION_OPEN.equals((String)slQuoatationCurrents.get(jCount))){
							doObject.setId((String)slQuoatationIds.get(jCount));
							doObject.setAttributeValues(context, hmAttrMap);
							MqlUtil.mqlCommand(context, "trigger off;", true);
							doObject.setState(context, DomainConstants.STATE_RTS_QUOTATION_RETURNED);
							MqlUtil.mqlCommand(context, "trigger on;", true);
						}
					}
					doObject.setId((String)mapTemp.get(DomainObject.SELECT_ID));
					doObject.setState(context, DomainConstants.STATE_RESPONSE_COMPLETE);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_ID);
			DomainObject.MULTI_VALUE_LIST.remove("from[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].to."+DomainObject.SELECT_CURRENT);
		}
		return mlReturnList;
	}
	public String getRFQSummaryDocument(Context context, String[] args)
			throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		String strObjectId = (String)requestMap.get("objectId");
		StringBuffer strBuf = new StringBuffer(1256); 
		try {
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doRFQ = new  DomainObject(strObjectId);
				String strAccessKey = "TDR.RFQSummary.DocumentName";
				String strRFQSummaryName = _classCurrencyConfig.getProperty(strAccessKey);
				String strName = doRFQ.getInfo(context,DomainObject.SELECT_NAME) + " "+strRFQSummaryName;
				String strRFQSummaryId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",strObjectId,"from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"|to.name=='"+strName+"'].to.id");
				if(UIUtil.isNotNullAndNotEmpty(strRFQSummaryId)) {
					strBuf.append("<a href='javascript:callCheckout(\"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, strRFQSummaryId));
					strBuf.append("\",\"download\", \"\", \"\",\"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, strObjectId));
					strBuf.append("\"");
					strBuf.append(")'>");
					strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
					strBuf.append("Download");
					strBuf.append("\" title=\"");
					strBuf.append("Download");
					strBuf.append("\"></img></a>&#160;");
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strBuf.toString();

	}


	public boolean displayRFQSummaryLink(Context context,String[] args)throws Exception{

		boolean bDisplaySummary = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String)programMap.get("objectId");
		if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
			DomainObject doRFQ = new  DomainObject(strObjectId);
			String strAccessKey = "TDR.RFQSummary.DocumentName";
			String strRFQSummaryName = _classCurrencyConfig.getProperty(strAccessKey);
			String strName = doRFQ.getInfo(context,DomainObject.SELECT_NAME) + " "+strRFQSummaryName;
			String isSummaryAvailable = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",strObjectId,"from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"|to.name=='"+strName+"']");
			if(UIUtil.isNotNullAndNotEmpty(isSummaryAvailable) && "true".equalsIgnoreCase(isSummaryAvailable)) {
				bDisplaySummary = true;
			}
		}

		return bDisplaySummary;

	}

	/***
	 * This method is to check whether SC Department field is editable or not.
	 * If TechReviewstatus attribute value is complete, field will be non-editable or editable.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries:
	 * ObjectId - The object Id of the context.
	 * @return boolean 
	 * @throws Exception if the operation fails
	 */
	public boolean hasAccessEditSCDepartmentFeild(Context context, String[] args) throws Exception
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
			String strObjectId = (String)programMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				StringList slSelects = new StringList();
				slSelects.add(DomainObject.SELECT_CURRENT);
				slSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
				DomainObject doRFQ = new DomainObject(strObjectId);
				Map mObjectInfo = (Map)doRFQ.getInfo(context, slSelects);
				String strCurrent = (String)mObjectInfo.get(DomainObject.SELECT_CURRENT);
				String strTechReviewStatus = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
				if(strCurrent.equals(DomainObject.STATE_RESPONSE_COMPLETE) && UIUtil.isNullOrEmpty(strTechReviewStatus)){
					settingsMap.put("Editable", "true");
				} else {
					settingsMap.put("Editable", "false");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return true;
	}

	/**
	 * Method returns range values for QuotationResponseReview in RFQ Create Page
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public StringList getQuotationResponseReview(Context context, String[] args) throws Exception {
		StringList slReturnRanges=new StringList();
		try{
			StringList slQuotationResponseReview = FrameworkUtil.getRanges(context, DomainObject.ATTRIBUTE_QUOTATION_RESPONSE_REVIEW);
			int iSize =slQuotationResponseReview.size();
			for (int iCount=0;iCount<iSize; iCount++)
			{
				if("Sealed".equals((String)slQuotationResponseReview.get(iCount))){
					slReturnRanges.add((String)slQuotationResponseReview.get(iCount));
				}
			}
		}catch (Exception Ex) {
			throw Ex;
		}
		return slReturnRanges;
	}

	/**
	 * Trigger method to connect RFQ with Commercial Quote Opener.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public void connectCommercialQuoteOpener(Context context, String[] args) throws Exception {
		try{
			String strRFQObjectId = args[0];
			//        	String strCommercialQuoteOpener = _classCurrencyConfig.getProperty("TDR.RFQ.CommercialQuoteOpener");
			//        	if(UIUtil.isNotNullAndNotEmpty(strCommercialQuoteOpener)){
			//        		String mqlResult = MqlUtil.mqlCommand(context, "list person '"+strCommercialQuoteOpener+"'", false);
			//        		if(UIUtil.isNotNullAndNotEmpty(mqlResult)){        			
			//        			String strCommercialQuoteOpenerId = PersonUtil.getPersonObjectID(context, strCommercialQuoteOpener);
			//        			DomainRelationship.connect(context, new DomainObject(strCommercialQuoteOpenerId), ${CLASS:TDRConstants}.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER, new DomainObject(strRFQObjectId));
			//        		}
			//        	}

			String strCommercialQuoteOpener = MqlUtil.mqlCommand(context, "print person $1 select $2 dump",context.getUser(),"property[preference_CommercialQuoteOpener].value");
			if(UIUtil.isNotNullAndNotEmpty(strCommercialQuoteOpener)){
				DomainRelationship.connect(context, new DomainObject(strCommercialQuoteOpener), TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER, new DomainObject(strRFQObjectId));
			}else{
				String message = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.PreferredQuoteOpenerNotSet.Message");
				throw new FrameworkException(message);
			}
		}catch (Exception Ex) {
			throw Ex;
		}
	}

	/***
	 * This method is to check whether context user has access to view Internal Document or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries:
	 * ObjectId - The object Id of the context.
	 * @return boolean 
	 * @throws Exception if the operation fails
	 */
	public boolean hasAccessInternalDocument(Context context, String[] args) throws Exception
	{
		boolean bAccess = false;
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			Vector assignments = PersonUtil.getAssignments(context);
			String strContextUser = context.getUser();
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slSelect =  new StringList();
				slSelect.add(DomainObject.SELECT_OWNER);
				slSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value");
				Map mapInfo = (Map)doObject.getInfo(context, slSelect);
				String strOwner = (String)mapInfo.get(DomainObject.SELECT_OWNER);
				String strTechReviewStatus = (String)mapInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value");
				if(strOwner.equals(strContextUser) || assignments.contains(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER) || assignments.contains(TDRConstants_mxJPO.ROLE_MSIL_DPM)){
					bAccess = true;
				}else if((assignments.contains(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER) || assignments.contains(TDRConstants_mxJPO.ROLE_MSIL_DDVM)) && "Complete".equals(strTechReviewStatus)){
					bAccess = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return bAccess;
	}

	/***
	 * This method is to check whether context user has access to view Technical Document or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries:
	 * ObjectId - The object Id of the context.
	 * @return boolean 
	 * @throws Exception if the operation fails
	 */
	public boolean hasAccessTechnicalDocument(Context context, String[] args) throws Exception
	{
		boolean bAccess = false;
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			Vector assignments = PersonUtil.getAssignments(context);
			String strContextUser = context.getUser();
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slSelect =  new StringList();
				slSelect.add(DomainObject.SELECT_OWNER);
				slSelect.add("to["+DomainObject.TYPE_RTS_QUOTATION+"].from["+DomainObject.TYPE_RFQ+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value");
				Map mapInfo = (Map)doObject.getInfo(context, slSelect);
				String strOwner = (String)mapInfo.get(DomainObject.SELECT_OWNER);
				String strQuoteOpenStatus = (String)mapInfo.get("to["+DomainObject.TYPE_RTS_QUOTATION+"].from["+DomainObject.TYPE_RFQ+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"].value");
//				if(strOwner.equals(strContextUser) || assignments.contains(DomainObject.ROLE_SUPPLIER_REPRESENTATIVE) || assignments.contains(TDRConstants_mxJPO.ROLE_RM_BUYER)|| assignments.contains(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER) || assignments.contains(TDRConstants_mxJPO.ROLE_MSIL_DPM)){
//					bAccess = true;
//				}else if((assignments.contains(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER) || assignments.contains(TDRConstants_mxJPO.ROLE_MSIL_DDVM)) && "Complete".equals(strQuoteOpenStatus)){
//					bAccess = true;
//				}
				bAccess = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return bAccess;
	}

	/**
	 * Method used to get Concerned Final Approval Route Template. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	public String getFinalApprovalRouteTemplate(Context context, String[] args)throws Exception{
		String strReturnValue = DomainConstants.EMPTY_STRING;
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId) ) {
				DomainObject doRFQ = new DomainObject(strObjectId);
				strReturnValue = doRFQ.getInfo(context, "to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.name");
				if(UIUtil.isNullOrEmpty(strReturnValue)) {
					strReturnValue = DomainConstants.EMPTY_STRING;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strReturnValue;
	}

	/**
	 * Method returns StringList of Route Templates connected to RFQ
	 * department. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedFinalRouteTemplate(Context context, String[] args)throws Exception{
		StringList slExcludeRouteTemplateList = new StringList();
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				slExcludeRouteTemplateList = doObject.getInfoList(context, "to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].from.id");
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}   	

		return slExcludeRouteTemplateList;
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int notifySuppliers(Context context, String[] args) throws Exception {

		String objectId = args[0];
		RequestToSupplier RTS = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
		RTSQuotation rtsQuotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
		//If sendNotificationByLoggedInUser = true, send mail with logged in user's name and not by "User Agent" name
		String sendNotificationByLoggedInUser = EnoviaResourceBundle.getProperty(context,"emxSourcing.sendNotificationByLoggedInUser");//MULTITENANT UPDATE
		String oldAgentName = MailUtil.getAgentName(context);
		String strUserAgent = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
		String strContextUser = context.getUser();
		if("true".equalsIgnoreCase(sendNotificationByLoggedInUser) && !strUserAgent.equals(strContextUser) )
		{
			MailUtil.setAgentName(context, strContextUser);
		}
		try {
			String message = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.RFQ.NotifySupplier.Message");
			String subject   = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "emxSourcing.RTS.CompletionNotificationSubject");
			String awardSubject   = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "emxSourcing.RTS.AwardNotificationSubject");

			RTS.setId(objectId);
			StringList selectList = new StringList(3);
			selectList.add(RTS.SELECT_NAME);
			selectList.add(RTS.SELECT_CURRENT);
			selectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_NOTIFY_AWARDED_SUPPLIER+"]");
			Map m = RTS.getInfo(context,selectList);

			subject = (String)m.get(RTS.SELECT_NAME) + " " + subject;

			StringList busSelects = rtsQuotation.getBusSelectList(3);
			busSelects.add(rtsQuotation.SELECT_ID);
			busSelects.add(rtsQuotation.SELECT_AWARD_STATUS);
			busSelects.add(RTS.SELECT_NAME);

			MapList rtsQuotationIdList = RTS.getRTSQuotations(context, busSelects, null, false);

			String notifyOption = (String)m.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_NOTIFY_AWARDED_SUPPLIER+"]");
			String showAward = "yes";
			if(notifyOption != null && notifyOption.equals("No"))
			{
				showAward = "no";
			}

			//set the awarded attribute on the RTS quotation based on whether the
			//awards should be shown or hidden
			for (int i=0; i<rtsQuotationIdList.size(); i++){
				Map quotation = (Map) rtsQuotationIdList.get(i);
				String quotationId = (String)quotation.get(rtsQuotation.SELECT_ID);
				String awardStatus = (String)quotation.get(rtsQuotation.SELECT_AWARD_STATUS);

				//check if the quotation is awarded
				if(awardStatus.equals("Awarded") || awardStatus.equals("Awarded - Supplier Hidden"))
				{
					rtsQuotation.setId(quotationId);
					String relationshipId = rtsQuotation.getInfo(context,rtsQuotation.SELECT_RTS_QUOTATION_REL_ID);
					if(showAward.equals("yes")) {
						//Set the awardstatus on the RTS Quotation relationship
						RTS.setRTSQuotationAwardStatus(context, relationshipId, "Awarded");
						// New Code Added to notify only depending on notify option -sc
						if (notifyOption.equals("Yes")) {
							rtsQuotation.notifyOwner(context, awardSubject, message);
						}
					} else {
						//Set the awardstatus on the RTS Quotation relationship
						RTS.setRTSQuotationAwardStatus(context, relationshipId, "Awarded - Supplier Hidden");
					}
					// New Code Added to notify only depending on notify option -sc
				} else if (showAward.equals("yes") && notifyOption.equals("Yes")) {
					rtsQuotation.setId(quotationId);
					rtsQuotation.notifyOwner(context, subject, message);
				}
			}
		} catch (Exception ex ){
			ex.printStackTrace();
		}
		return 0;
	}

	/**
	 * Method used to connect/update relationship between RFQ and Route Template for final approval. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void connectFinalApprovalRouteTemplate(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){	
				DomainObject doObject = new DomainObject(strObjectId);
				String strRFQInitialApprovalTemplateRelId = (String)doObject.getInfo(context,"to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE+"].id");
				if(UIUtil.isNotNullAndNotEmpty(strRFQInitialApprovalTemplateRelId)){
					DomainRelationship.disconnect(context, strRFQInitialApprovalTemplateRelId);
				}
				if(UIUtil.isNotNullAndNotEmpty(strNewValue)) {
					DomainRelationship.connect(context, new DomainObject(strNewValue), TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE, new DomainObject(strObjectId));
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	public boolean checkAccessForUnitPrice(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");

		if(context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER) || context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER)) {
			String strRFQId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				DomainObject doObject = new DomainObject(strRFQId);
				String strOpenQuoteStatus = (String)doObject.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
				if(UIUtil.isNotNullAndNotEmpty(strOpenQuoteStatus) && "Complete".equals(strOpenQuoteStatus)) {
					bHasAccess = true;
				}
			}
		}
		return bHasAccess;
	}
	/**
	 * Trigger method to connect RFQ with Technical Buyer when create RFQ if context user is Technical Buyer.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	public void connectTechnicalBuyer(Context context, String[] args) throws Exception {
		try{
			String strRFQObjectId = args[0];
			String strContextPersonId = (String)PersonUtil.getPersonObjectID(context);
			if(context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER) && UIUtil.isNotNullAndNotEmpty(strContextPersonId)){
				DomainRelationship.connect(context, new DomainObject(strRFQObjectId), TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_TECHNICAL_BUYER, new DomainObject(strContextPersonId));
			}
		}catch (Exception Ex) {
			throw Ex;
		}
	}
	/**
	 * Method returns int value. This method will updated TDRRoundType attribute based on the assignment of person
	 * Return 0 if success else 1.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateRateFormulaOnRFQCreationFromTemplate(Context context, String[] args)throws Exception{
		try {
			String strFromObjectId = (String)args[0];
			String strToObjectId = (String)args[1];
			if(UIUtil.isNotNullAndNotEmpty(strToObjectId) && UIUtil.isNotNullAndNotEmpty(strFromObjectId)) {
				DomainObject doRFQ = new DomainObject(strFromObjectId);
				DomainObject doRFQTemplate = new DomainObject(strToObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add("attribute["+DomainConstants.ATTRIBUTE_EXTEND_UNIT_PRICE_FORMULA+"].value");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_FORMULA+"].value");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_FORMULA+"].value");
				slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");

				Map mTemplateInfo = (Map)doRFQTemplate.getInfo(context, slObjectSelect);

				if(mTemplateInfo != null && mTemplateInfo.isEmpty() == false) {
					if("Yes".equalsIgnoreCase((String)mTemplateInfo.get("attribute["+DomainConstants.ATTRIBUTE_EXTEND_UNIT_PRICE_FORMULA+"].value"))){
						HashMap rfqTemplateAttributes = new HashMap();
						//Added for MSIL Capital = Start
						rfqTemplateAttributes.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_FORMULA,(String)mTemplateInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_FORMULA+"].value"));
						rfqTemplateAttributes.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA,(String)mTemplateInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value"));
						rfqTemplateAttributes.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_FORMULA,(String)mTemplateInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_FORMULA+"].value"));
						rfqTemplateAttributes.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA,(String)mTemplateInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value"));

						doRFQ.setAttributeValues(context, rfqTemplateAttributes);
					}
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	public Vector getRBIRate(Context context, String[] args)throws Exception{
		Vector vec=new Vector();
		try {
			StringBuilder finalVal = null;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String strRFQId = (String)paramList.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				DomainObject doRFQ = new DomainObject(strRFQId);
				StringList slRFQSelect = new StringList();
				slRFQSelect.add("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				Map mRFQInfo = doRFQ.getInfo(context, slRFQSelect);

				String strRBIFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				String strCurrency = (String)mRFQInfo.get("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				StringList localStringList1 = new StringList();
				localStringList1.add("id");

				StringList localStringList2 = new StringList(1);
				localStringList2.add("id[connection]");
				String upFormulaMsg= EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",context.getLocale(), "TDR.Expression.RBIFormulaDisplay");
				Map tempMap = null;
				String strQuoteId = "";
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strQuoteId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",(String)tempMap.get("id[connection]"),"to.id");
					double d = Calculator.CalculateExpression(context, strQuoteId, (String)tempMap.get("id"), strRBIFormula, null);
					if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
						d = 0.0D;
					}

					finalVal = new StringBuilder();
					finalVal.append(String.valueOf(d)).append("\n").append(strCurrency);
					vec.addElement(finalVal.toString());
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return vec;


	}


	public Vector getCBDSRate(Context context, String[] args)throws Exception{
		Vector vec=new Vector();
		try {
			StringBuilder finalVal = null;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String strRFQId = (String)paramList.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {

				DomainObject doRFQ = new DomainObject(strRFQId);
				StringList slRFQSelect = new StringList();
				slRFQSelect.add("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
				Map mRFQInfo = doRFQ.getInfo(context, slRFQSelect);

				String strCBDSFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
				String strCurrency = (String)mRFQInfo.get("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");

				String upFormulaMsg= EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",context.getLocale(), "TDR.Expression.RBIFormulaDisplay");
				Map tempMap = null;
				String strQuoteId = "";
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strQuoteId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",(String)tempMap.get("id[connection]"),"to.id");
					double d = Calculator.CalculateExpression(context, strQuoteId, (String)tempMap.get("id"), strCBDSFormula, null);
					if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
						d = 0.0D;
					}

					finalVal = new StringBuilder();
					finalVal.append(String.valueOf(d)).append("\n").append(strCurrency);
					vec.addElement(finalVal.toString());
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return vec;


	}



	public Vector getRFQSummaryLink(Context context, String[] args)throws Exception{
		Vector vec=new Vector();
		try {
			StringBuilder finalVal = null;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			Map tempMap = null;
			String strObjectId = "";
			DomainObject doRFQ = new DomainObject();
			StringBuffer strBuf = null;
			for(int i=0;i<mlObjectList.size();i++) {
				strBuf = new StringBuffer();
				tempMap = (Map)mlObjectList.get(i);
				strObjectId = (String)tempMap.get("id");
				doRFQ.setId(strObjectId);
				String strAccessKey = "TDR.RFQSummary.DocumentName";
				String strRFQSummaryName = _classCurrencyConfig.getProperty(strAccessKey);
				String strName = doRFQ.getInfo(context,DomainObject.SELECT_NAME) + " "+strRFQSummaryName;
				String strRFQSummaryId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",strObjectId,"from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"|to.name=='"+strName+"'].to.id");
				if(UIUtil.isNotNullAndNotEmpty(strRFQSummaryId)) {
					strBuf .append("<a href='javascript:callCheckout(\"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, strRFQSummaryId));
					strBuf.append("\",\"download\", \"\", \"\",\"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, ""));
					strBuf.append("\", \"");
					strBuf.append(XSSUtil.encodeForJavaScript(context, strObjectId));
					strBuf.append("\"");
					strBuf.append(")'>");
					strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
					strBuf.append("Download");
					strBuf.append("\" title=\"");
					strBuf.append("Download");
					strBuf.append("\"></img></a>&#160;");
				}		
				vec.addElement(strBuf.toString());
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return vec;


	}



	public boolean checkAccessForRequiredQty(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strRFQQuoteId = (String)programMap.get("parentOID");
		if(UIUtil.isNotNullAndNotEmpty(strRFQQuoteId)) {
			DomainObject doObject = new DomainObject(strRFQQuoteId);
			String strRoundType = "";
			
			if(doObject.isKindOf(context, DomainObject.TYPE_REQUEST_TO_SUPPLIER)) {
				strRoundType = (String)doObject.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}else if(doObject.isKindOf(context, DomainObject.TYPE_RTS_QUOTATION)) {
				strRoundType = (String)doObject.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}
			
			if("Technical".equalsIgnoreCase(strRoundType) || "Commercial".equalsIgnoreCase(strRoundType)) {
				
				
				bHasAccess = true;
			}else if("RM".equalsIgnoreCase(strRoundType)) {
				bHasAccess = false;
			}else {
				bHasAccess = true;
			}
		}
		return bHasAccess;
	}

	public boolean checkAccessForRequiredUoM(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strRFQQuoteId = (String)programMap.get("parentOID");
		if(UIUtil.isNotNullAndNotEmpty(strRFQQuoteId)) {
			DomainObject doObject = new DomainObject(strRFQQuoteId);
			String strRoundType = "";
			
			if(doObject.isKindOf(context, DomainObject.TYPE_REQUEST_TO_SUPPLIER)) {
				strRoundType = (String)doObject.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}else if(doObject.isKindOf(context, DomainObject.TYPE_RTS_QUOTATION)) {
				strRoundType = (String)doObject.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}
			
			if("Technical".equalsIgnoreCase(strRoundType) || "Commercial".equalsIgnoreCase(strRoundType)) {
				bHasAccess = true;
			}else if("RM".equalsIgnoreCase(strRoundType)) {
				bHasAccess = false;
			}else {
				bHasAccess = true;
			}
		}
		return bHasAccess;
	}

	public boolean checkAccessForRequiredCurrency(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strRFQQuoteId = (String)programMap.get("parentOID");
		if(UIUtil.isNotNullAndNotEmpty(strRFQQuoteId)) {
			DomainObject doObject = new DomainObject(strRFQQuoteId);
			String strRoundType = "";			
			if(doObject.isKindOf(context, DomainObject.TYPE_REQUEST_TO_SUPPLIER)) {
				strRoundType = (String)doObject.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}else if(doObject.isKindOf(context, DomainObject.TYPE_RTS_QUOTATION)) {
				strRoundType = (String)doObject.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}
			if("Technical".equalsIgnoreCase(strRoundType) || "Commercial".equalsIgnoreCase(strRoundType)) {
				bHasAccess = false;
			}else if("RM".equalsIgnoreCase(strRoundType)) {
				bHasAccess = false;
			}else {
				bHasAccess = true;
			}
		}
		return bHasAccess;
	}
	
	public boolean checkAccessForQuotedQty(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strRFQQuoteId = (String)programMap.get("parentOID");
		if(UIUtil.isNotNullAndNotEmpty(strRFQQuoteId)) {
			DomainObject doObject = new DomainObject(strRFQQuoteId);
			String strRoundType = "";			
			if(doObject.isKindOf(context, DomainObject.TYPE_REQUEST_TO_SUPPLIER)) {
				strRoundType = (String)doObject.getInfo(context, "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}else if(doObject.isKindOf(context, DomainObject.TYPE_RTS_QUOTATION)) {
				strRoundType = (String)doObject.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
			}
			if("Technical".equalsIgnoreCase(strRoundType) || "Commercial".equalsIgnoreCase(strRoundType)) {
				bHasAccess = true;
			}else if("RM".equalsIgnoreCase(strRoundType)) {
				bHasAccess = false;
			}else {
				bHasAccess = false;
			}
		}
		return bHasAccess;
	}
	
	public String getRateForCompare(Context context, String[] args)throws Exception{
		System.out.println("called");
		String strReturn = DomainObject.EMPTY_STRING;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String strRFQId = (String)programMap.get("objectId");
		String strQuoteId = (String)programMap.get("quotationId");
		String strlineItemId = (String)programMap.get("lineItemId");
		String strPrice = (String)programMap.get("price");
		String strCBDSFormula = DomainObject.EMPTY_STRING;
		String strRBIFormula = DomainObject.EMPTY_STRING;
		double d = 0.0;
		if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {
			DomainObject doRFQ = new DomainObject(strRFQId);
			StringList slRFQSelect = new StringList();
			slRFQSelect.add("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
			slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
			slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
			Map mRFQInfo = doRFQ.getInfo(context, slRFQSelect);
			strCBDSFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
			strRBIFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
			if("Quoted CBDS Price".equalsIgnoreCase(strPrice)){
				d = Calculator.CalculateExpression(context, strQuoteId, strlineItemId, strCBDSFormula, null);
			}
			if("Quoted RBI Price".equalsIgnoreCase(strPrice)){
				d = Calculator.CalculateExpression(context, strQuoteId, strlineItemId, strRBIFormula, null);
			}
			if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
				d = 0.0D;
			}
			strReturn = String.valueOf(d);
		}
		return strReturn;
	}
	
	
	/**
	 * Method used to update TDREditableInResponseComplete attribute on Line Item Template object. 
	 * If Attribute is not available, Add interface and update the value.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no value
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateTDREditableInResponseComplete(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			String strOldValue = (String)paramMap.get("Old Value");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				doObject.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_EDITABLE_IN_RESPONSE_COMPLETE, strNewValue);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}
	
	public boolean hasAccessForInternalDocument(Context context, String[] args) throws Exception
	{
		boolean bAccess = false;
		try {
			String strObjectId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
		    	HashMap paramMap = new HashMap();
		    	paramMap.put("objectId", strObjectId);
		    	String[] methodargs = JPO.packArgs(paramMap);
		    	bAccess = hasAccessInternalDocument(context, methodargs);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return bAccess;
	}

	public boolean hasAccessForTechnicalDocument(Context context, String[] args) throws Exception
	{
		boolean bAccess = false;
		try {
			String strObjectId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
		    	HashMap paramMap = new HashMap();
		    	paramMap.put("objectId", strObjectId);
		    	String[] methodargs = JPO.packArgs(paramMap);
		    	bAccess = hasAccessTechnicalDocument(context, methodargs);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return bAccess;
	}
	
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateRBIFormulaOnRFQEdit(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			
			System.out.println("paramMap--------"+paramMap);

			String strObjectId = (String)paramMap.get("objectId");
			String strRBIFormula = (String)paramMap.get("New Value");			
			String strRBICalFormula = (String)paramMap.get("New OID");
			
			Map hmAttributeMap = new HashMap();
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				if(null != strRBIFormula && null != strRBICalFormula) {
					hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_FORMULA, strRBIFormula);
					hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA, strRBICalFormula);
					doObject.setAttributeValues(context, hmAttributeMap);
				}				
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateCBDSFormulaOnRFQEdit(Context context, String[] args)throws Exception{
		try {
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strObjectId = (String)paramMap.get("objectId");
			String strCBDSFormula = (String)paramMap.get("New Value");			
			String strCBDSCalFormula = (String)paramMap.get("New OID");
			
			Map hmAttributeMap = new HashMap();
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				if(null != strCBDSFormula && null != strCBDSCalFormula) {
					hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_FORMULA, strCBDSFormula);
					hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA, strCBDSCalFormula);
					doObject.setAttributeValues(context, hmAttributeMap);
				}				
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	
	/**
	 * Gets all the attachments associated for the Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing ObjectId and parameter list.
	 * @return a list of <code>MapList</code>contains all the associated attachment objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getReferenceDocumentsOfQuotation(Context context, String[] args)
			throws Exception
	{
		MapList mlAttachementList = new MapList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_TYPE);
				
				
				boolean bIsSupplier = context.isAssigned(DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE);

				StringList slRelSelect = new StringList();
				slRelSelect.add(DomainRelationship.SELECT_ID);
				ContextUtil.pushContext(context);
				mlAttachementList = doObject.getRelatedObjects(context,TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT,TDRConstants_mxJPO.TYPE_TDR_REFERENCE_DOCUMENT_TYPE+","+TDRConstants_mxJPO.TYPE_TDR_PO_DOCUMENT_TYPE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
				ContextUtil.popContext(context);
				Map tempMap = null;
				String strType = "";
				 for(int i=0;i<mlAttachementList.size();i++) {
			        	tempMap = (Map)mlAttachementList.get(i);
			        	strType = (String)tempMap.get(DomainObject.SELECT_TYPE);
			        	if(TDRConstants_mxJPO.TYPE_TDR_PO_DOCUMENT_TYPE.equals(strType) && bIsSupplier) {
			        		tempMap.put("disableSelection", "true");
			        	}else if(TDRConstants_mxJPO.TYPE_TDR_REFERENCE_DOCUMENT_TYPE.equals(strType) && bIsSupplier==false){
			        		tempMap.put("disableSelection", "true");
			        	}
			        }
				
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return mlAttachementList;
		}
	}

	public void updateTechReviewResult(Context context,String[] args)throws Exception{
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String strObjectId = (String)paramMap.get("objectId");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObj = new DomainObject(strObjectId);
				if(UIUtil.isNotNullAndNotEmpty(strNewValue)) {
					ContextUtil.pushContext(context);
					doObj.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT, strNewValue);
					ContextUtil.popContext(context);
				}
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}		
	}
	
	
	public void updateTechReviewResultRemark(Context context,String[] args)throws Exception{
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String strObjectId = (String)paramMap.get("objectId");
			String strNewValue = (String)paramMap.get("New Value");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObj = new DomainObject(strObjectId);
				if(UIUtil.isNotNullAndNotEmpty(strNewValue)) {
					ContextUtil.pushContext(context);
					doObj.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT_REMARKS, strNewValue);
					ContextUtil.popContext(context);
				}
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}		
	}
	
	
	public boolean checkAccessForBuyer(Context context, String[] args)throws Exception{
		boolean bHasAccess = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");

		if(context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER)) {
			String strRFQId = (String)programMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				DomainObject doObject = new DomainObject(strRFQId);
				String strOpenQuoteStatus = (String)doObject.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
				if(UIUtil.isNotNullAndNotEmpty(strOpenQuoteStatus) && "Complete".equals(strOpenQuoteStatus)) {
					bHasAccess = true;
}
			}
		}else if(context.isAssigned(DomainConstants.ROLE_SUPPLIER_REPRESENTATIVE)){
			bHasAccess = true;
		}
		return bHasAccess;
	}


	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateAwardDate(Context context, String[] args)throws Exception{
		int iReturn = 0;
		try {
			String strRelId = (String)args[0];
			if(UIUtil.isNotNullAndNotEmpty(strRelId)) {
				DomainRelationship drRel = new DomainRelationship(strRelId);
				Date dtToday = new Date();

				String strMatrixDateFormat = eMatrixDateFormat.getEMatrixDateFormat();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String strAppDate = dtToday.toString();

				SimpleDateFormat sdf1 = new SimpleDateFormat(strMatrixDateFormat);
				String strDate = sdf1.format(dtToday);				
				drRel.setAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE, strDate);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


	public boolean showRFQSupplierLineItemSummary(matrix.db.Context context, String[] args) throws Exception
	{
		boolean showCmd=false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);

		DomainObject requestToSupplierObj = new DomainObject();
		String rtsId = (String)programMap.get("objectId");
		requestToSupplierObj.setId(rtsId);


		StringList selectList = new StringList(1);
		selectList.add(RequestToSupplier.SELECT_CURRENT);

		Map m = requestToSupplierObj.getInfo(context,selectList);

		String hasPending = (String)m.get(RequestToSupplier.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
		String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);

		if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
				currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE) ||
				currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) )
		{
			String strOpenQuoteStatus = (String)requestToSupplierObj.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
			if(UIUtil.isNotNullAndNotEmpty(strOpenQuoteStatus) && "Complete".equals(strOpenQuoteStatus)) {
				showCmd = true;
			}else {
				showCmd = false;
			}
		}else if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW) ||
				currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_STARTED)){
			showCmd = true;
		}else {
			showCmd = false;
		}
		return showCmd;
	}    


	public Vector getTotalCostOfLineItem(Context context, String[] args)throws Exception{

		Vector vec=new Vector();
		try {
			StringBuilder finalVal = null;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String strRFQId = (String)paramList.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strRFQId)) {
				DomainObject doRFQ = new DomainObject(strRFQId);
				StringList slRFQSelect = new StringList();
				slRFQSelect.add("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				Map mRFQInfo = doRFQ.getInfo(context, slRFQSelect);

				String strRBIFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				String strCurrency = (String)mRFQInfo.get("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				Map tempMap = null;
				String strQuoteId = "";
				String strQty = "";
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strQuoteId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",(String)tempMap.get("id[connection]"),"to.id");
					strQty = (String)tempMap.get("quantity");
					double d = Calculator.CalculateExpression(context, strQuoteId, (String)tempMap.get("id"), strRBIFormula, null);
					if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
						d = 0.0D;
					}

					d = d*Double.valueOf(strQty);
					
					finalVal = new StringBuilder();
					finalVal.append(String.valueOf(d)).append("\n").append(strCurrency);
					vec.addElement(finalVal.toString());
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return vec;
	}



}




