/**
 * Title:       MSILAccessRights
 * Description: This JPO contains all the code related to Access Rights
 * Company:     MSIL
 * Date:        4/18/2011
 * @author      Tarun Gupta
 * @version     1.0
 *
 * ----------------------------------------------------------------
 * Modification History:
 * Date      |    Modified By    | Description (with Build Name, if any) 
 * 4/25/2011 |    Tarun G        | Added a method for Colu,mn Edit Access Function.
 * 5/19/2011 |    Tarun G        | Added logging using log4j and added try catch
 * 7/8/2013  |     Vinit         | Modified to Show TPL difference list to MT14 as well..
 * 11/10/2013| Sarita Kumari     | Added for Form MSILSendRevisedSourcingCostForApproval Checker Text Validation for CR Revised Sourcing Target Cost Approval ID -SK-11/10/2013
 * 17-Oct-2014 | Naveena         | added for CR 65075 - extend due date  command is visible only if Quotation is Auto regret - Opne requote Task
 * 1/8/2016    | Nitika          | to fix VCP option on right click of RFQ-ENGG-13187
 */
import matrix.db.MatrixClassLoader;
import matrix.db.Page;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.StringList;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
//import msil.enovia.util.MSILConstants; 
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.domain.DomainConstants;
import org.apache.log4j.Logger;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
//Added by Sarita  for Revised Sourcing Target Cost Approval on 11/10/2013 ID -SK-11/10/2013
import com.matrixone.apps.domain.util.PersonUtil;
//import msil.enovia.util.MSILOrganization;
import com.matrixone.apps.common.Company;
//Ended by Sarita ID -SK-11/10/2013
/**
 * The <code>MSILAccessRights</code> class contains methods for executing JPO operations related
 * to management of access rights on various functions in MSIL
 * @author Tarun
 * @Build 1
 **
 */
public class MSILAccessRights_mxJPO extends emxDomainObject_mxJPO
{

	public static final String MSIL_ACCESS_RIGHTS_PAGE = "MSILAccessRights";
	protected static Properties _classAccessRights;
	
	/********************************* STRING RESOURCES FIELDS /*********************************
    /** A string constant with the value "emxComponentsStringResource". */
	protected static String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";
	static Logger log = Logger.getLogger("MSILAccessRights");
	static String strKey="";
	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds no arguments.   
	 * @throws Exception if the operation fails.
	 */
	public MSILAccessRights_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		// load the Part Access right values from the Matrix Page object
		MatrixClassLoader mcl   =   new MatrixClassLoader(ClassLoader.getSystemClassLoader());
		_classAccessRights      =   new Properties();
		try {
			log.info("User using this class is : " + context.getUser());
			_classAccessRights.load(mcl.getResourceAsStream(MSIL_ACCESS_RIGHTS_PAGE));
		} catch (Exception e) {
			log.error("Got exception:  " + e.toString());
			// if the Page object cannot be loaded the properties object is just an empty hashmap
		}
	}
	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @return        an integer status code (0 = success)
	 * @throws        Exception when problems occurred in the Common Components
	 * @since         Phase 2
	 **
	 */
	public int mxMain(Context context, String[] args) throws Exception
	{
		if (!context.isConnected()) {
			i18nNow i18nnow = new i18nNow();
			String strContentLabel = i18nnow.GetString(RESOURCE_BUNDLE_COMPONENTS_STR,
					context.getSession().getLanguage(),
					"emxComponents.Error.UnsupportedClient");
			throw  new Exception(strContentLabel);
		}
		return  0;

	}
	/**
	 * Checks if the user has the access on the specified UI3 component based on passed setting
	 * This method checks access type based on access file
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *            0 - HashMap containing one String entry for key "objectId"
	 *                Hashmap has the function name as well for which access need to be checked.
	 * @return        boolean if user can have access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 * @author        Sunil Dhingra
	 **/
	public Boolean getFileBasedAccess(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strValidAccess       =   (String)hmFieldSettings.get("MSILFileBasedAccessKey");
			strValidAccess = _classAccessRights.getProperty(strValidAccess);
			StringList slValidAccess = FrameworkUtil.split(strValidAccess, ",");
			DomainObject doLoggedInPerson = PersonUtil.getPersonObject(context); 
			Map mPersonAttrs = doLoggedInPerson.getAttributeMap(context);
			String strAccessType = (String) mPersonAttrs.get("AccessType");
			if(slValidAccess.contains(strAccessType)){
				bHasAccess = true;
				// check whether the employee is DPM and above, such an employee can see the links while under DPM
				// employee can see links only is his dept is added to Project, all such employees have Engineer role assigned
				if(strAccessType!=null && (strAccessType.equals("1") || strAccessType.equals("2") || strAccessType.equals("3") || strAccessType.equals("4") || strAccessType.equals("5") || strAccessType.equals("11") || strAccessType.equals("13") || strAccessType.equals("14")) ){
					String objectId = (String) hmParamMap.get("objectId");
					String strWorksForDept = (String) mPersonAttrs.get("WorksForDept");
					bHasAccess = getFileBasedAccessWRTDept(context, objectId, strWorksForDept);
				}

				// Provide
				if(bHasAccess) {
					// if traditional check has been defined, execute it
					String strTraditionalAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
					if(strTraditionalAccess!=null){
						bHasAccess                  =   hasAccessForFunction(context, strTraditionalAccess);
					}else{
						bHasAccess                  =   true;
					}
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasFunctionAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(bHasAccess);
	}
	private boolean getFileBasedAccessWRTDept(Context context, String objectId, String strWorksForDept) throws Exception
	{
		boolean bHasAccess = false;
		if(PersonUtil.hasAssignment(context, PropertyUtil.getSchemaProperty("group_MSILEngineer"))
//				&& !PersonUtil.hasAssignment(context, MSILConstants.ROLE_SMCAdvisor)
				&& !PersonUtil.hasAssignment(context, "SQA Admin")
		){
			if(objectId!=null){
				DomainObject doObject = DomainObject.newInstance(context, objectId);
				String strType = doObject.getInfo(context, DomainConstants.SELECT_TYPE);
				if(strType.equals(DomainConstants.TYPE_PROJECT_SPACE)){
					StringList slProjectDepts = doObject.getInfoList(context, "from[MSILConstants.RELATIONSHIP_PROJECT_TO_DEPT].to.name");
					if(slProjectDepts.contains(strWorksForDept)){
						bHasAccess                  =   true;
					}
				}else{
					bHasAccess                  =   true;	
				}
			}
		}else{
			bHasAccess                  =   true;
		}
		return  bHasAccess;
	}
	public Boolean getFileBasedAccessWRTDept(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			DomainObject doLoggedInPerson = PersonUtil.getPersonObject(context); 
			Map mPersonAttrs = doLoggedInPerson.getAttributeMap(context);
			String strAccessType = (String) mPersonAttrs.get("AccessType");
			// check whether the employee is DPM and above, such an employee can see the links while under DPM
			// employee can see links only is his dept is added to Project, all such employees have Engineer role assigned

			if(strAccessType!=null && (strAccessType.equals("1") || strAccessType.equals("2") || strAccessType.equals("3") || strAccessType.equals("4") || strAccessType.equals("5") || strAccessType.equals("11") || strAccessType.equals("13") || strAccessType.equals("14")) ){
				String objectId = (String) hmParamMap.get("objectId");
				String strWorksForDept = (String) mPersonAttrs.get("WorksForDept");
				bHasAccess = getFileBasedAccessWRTDept(context, objectId, strWorksForDept);
			}else if (strAccessType!=null && strAccessType.equals("999"))
			{
				bHasAccess = false;
			}else{
				bHasAccess = true;
			}
			if(bHasAccess) {
				// if traditional check has been defined, execute it
				HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
				String strTraditionalAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
				if(strTraditionalAccess!=null){
					bHasAccess                  =   hasAccessForFunction(context, strTraditionalAccess);
				}else{
					bHasAccess                  =   true;
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(bHasAccess);
	}
	/**
	 * Checks if the user has the access on the specified UI3 component based on passed setting
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *            0 - HashMap containing one String entry for key "objectId"
	 *                Hashmap has the function name as well for which access need to be checked.
	 * @return        boolean if user can have access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 **/
	public Boolean hasFunctionAccess(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			//Get the value from page object for the passed key value.
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strValidAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
			bHasAccess                  =   hasAccessForFunction(context, strValidAccess);
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasFunctionAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(bHasAccess);
	}
	public static String getStrVaule(Context context, String strAccessKey) throws Exception
	{
		String strPageContent = MqlUtil.mqlCommand(context, "print page MSILAccessRights select content dump");
		
		byte[] bytes 			= strPageContent.getBytes("UTF-8");
		InputStream input 		= new ByteArrayInputStream(bytes);
		Properties properties	= new Properties();
		properties.load(input);
		
		if(properties.keySet() != null)
		{
			Iterator keyTypeSymbolicNames  = properties.keySet().iterator();
			String strSelect = DomainConstants.EMPTY_STRING;
			while(keyTypeSymbolicNames.hasNext())
			{
				String keyType = (String)keyTypeSymbolicNames.next();
				if(keyType.startsWith(strAccessKey)){
					strKey = properties.getProperty(keyType);
				}
			}
		}
		return strKey;
	}
	/**
	 * Checks if the user has the access on the specified UI3 component based on passed setting
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param String  holds the key for Function Access
	 * @return        boolean if user can have access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 **/
	public static boolean hasAccessForFunction(Context context, String strAccessKey) throws Exception
	{
		boolean bHasAccess          =   false;
		//Get the value from page object for the passed key value.
		if(null!=_classAccessRights){
			String strRoleList          =   _classAccessRights.getProperty(strAccessKey);

			strRoleList                 =   strRoleList.trim();
			//get context user
			String strContextUser       =   context.getUser();

			//get all assignment and ancestors for context user
			String strMQLCommand        =   "print person '"+ strContextUser + "' select assignment.ancestor dump";
			MQLCommand mcGetAssign      =   new MQLCommand();
			mcGetAssign.open(context);
			mcGetAssign.executeCommand(context,strMQLCommand);
			String strMQLCommandResult  =   mcGetAssign.getResult();
			strMQLCommandResult         =   strMQLCommandResult.trim();
			mcGetAssign.close(context);
			StringList slUserRoleList   =   FrameworkUtil.split(strMQLCommandResult,",");

			//Check if the user has any of the assignment based on the passed key value
			StringList slAccessList     =   FrameworkUtil.split(strRoleList,",");

			int iAccessListSize         =   slAccessList.size();
			String strAccessName        =   "";
			for (int k = 0; k < iAccessListSize; k++) {
				strAccessName = (String) slAccessList.elementAt(k);
				if (slUserRoleList.contains(strAccessName))
				{
					bHasAccess = true;
					break;
				}
			}
		}
		return  bHasAccess;
	}
	/**
	 * This method will check if logged in user has access to cells in column based on Access Key.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 * @return        StringList containing the booleans for each cell of the column
	 * @throws        Exception if the operation fails
	 * @since   Build 1
	 **/
	public StringList checkDept(Context context, String[] args) throws Exception
	{
		StringList slIsEditable         = new StringList(10,2);
		try
		{
			HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
			HashMap hmColumnMap         =   (HashMap) programMap.get("columnMap");
			HashMap hmFieldSettings     =   (HashMap) hmColumnMap.get("settings");
			String strValidAccess       =   (String) hmFieldSettings.get("MSILAccessKey");

			//Call the MSILAccessRights program to check if user has the access for passed key or not.
			Boolean bAccess             = new Boolean(hasAccessForFunction(context, strValidAccess));
			MapList objectList          = (MapList) programMap.get("objectList");
			int iobjectList             = objectList.size();
			String strENPerson = "";				
			//Pass the returned value from Access rights program as setting for Column Edit Access
			for(int m = 0; m < iobjectList; m++)
			{
				//slIsEditable.addElement(bAccess);
				/*
				strENPerson = (String)((Map)objectList.get(m)).get("attribute[" + MSILConstants.ATTRIBUTE_EN_PERSON_INCHARGE +"]");
				if (strENPerson == null || "null".equals(strENPerson) || "".equals(strENPerson))
				{
					slIsEditable.addElement(false);
				}
				else
				{
					slIsEditable.addElement(bAccess);
				}*/
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get edit access for table columns. Please contact your system administrator");
			log.error("Got exception in method checkDept:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get edit access for table columns. \\nPlease contact your system administrator.");
		}
		return slIsEditable;
	}
	/**
	 * Checks if the user has no access on the specified UI3 component based on passed setting
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *            0 - HashMap containing one String entry for key "objectId"
	 *                Hashmap has the function name as well for which access need to be checked.
	 * @return        boolean false if user have no access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 **/
	public Boolean hasNoAccess(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);

			//Get the value from page object for the passed key value.
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strValidAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
			bHasAccess          =   hasAccessForFunction(context, strValidAccess);
		}catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasNoAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(!bHasAccess);
	}
	/**
	 * This method returns whether user has View Access on the column or not
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 * @throws        Exception if the operation fails
	 * @since   Build 1
	 * @ Shiv Singh
	 **/
	public Boolean getColumnAccess(Context context, String[] args) throws Exception
	{
		HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
		HashMap hmFieldSettings     =   (HashMap) programMap.get("SETTINGS");
		String strValidAccess       =   (String) hmFieldSettings.get("MSILAccessKey");
		//Call the MSILAccessRights program to check if user has the access for passed key or not.
		Boolean bAccess             = new Boolean(hasAccessForFunction(context, strValidAccess));
		return bAccess;
	}
	/**
	 * This method is used to return the boolean value to check whether the user belongs to the given department or not
	 * @param context The Matrix Context object
	 * @param args The string args paramater
	 * @return Boolean
	 * @throws Exception if operation fails
	 * @author Sunny Asija
	 */
	public Boolean getDepartmentAccess(Context context, String[] args) throws Exception
	{
		HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
		//Get the value from page object for the passed key value.
		String strValidAccess       =   (String) hmParamMap.get("MSILAccessKey");
		boolean bHasAccess          =   hasAccessForFunction(context, strValidAccess);
		return  new Boolean(bHasAccess);
	}
	/**
	 * This method will return the value of key present in the page file
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 * @throws        Exception if the operation fails
	 * @since   Build 1
	 **/
	public String getKeyValue(Context context, String[] args) throws Exception
	{
		HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
		String strValidKey          =   (String) programMap.get("key");
		String strValue             =   _classAccessRights.getProperty(strValidKey);
		if(null==strValue)
			strValue                =   "";
		return strValue;
	}

	/**
	 * Show TPL Family Difference List to MT 14 People.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *            0 - HashMap containing one String entry for key "objectId"
	 *                Hashmap has the function name as well for which access need to be checked.
	 * @return        boolean if user can have access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 * @author        Sunil Dhingra
	 * @modified by   Vinit - to show TPL Difference list to MT 14 people.
	 **/
	public Boolean showTPLDifferenceList(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strValidAccess       =   (String)hmFieldSettings.get("MSILFileBasedAccessKey");
			strValidAccess = _classAccessRights.getProperty(strValidAccess);
			StringList slValidAccess = FrameworkUtil.split(strValidAccess, ",");
			DomainObject doLoggedInPerson = PersonUtil.getPersonObject(context); 
			Map mPersonAttrs = doLoggedInPerson.getAttributeMap(context);
			String strAccessType = (String) mPersonAttrs.get("AccessType");
			if(slValidAccess.contains(strAccessType)){
				bHasAccess = true;
				// check whether the employee is DPM and above, such an employee can see the links while under DPM
				// employee can see links only is his dept is added to Project, all such employees have Engineer role assigned
				if(strAccessType!=null && (strAccessType.equals("1") || strAccessType.equals("2") || strAccessType.equals("3") || strAccessType.equals("4") || strAccessType.equals("5") || strAccessType.equals("11") || strAccessType.equals("13") || strAccessType.equals("14")) ){
					String objectId = (String) hmParamMap.get("objectId");
					String strWorksForDept = (String) mPersonAttrs.get("WorksForDept");
					bHasAccess = getFileBasedAccessWRTDept(context, objectId, strWorksForDept);
				}

				// Provide
				if(bHasAccess) {
					// if traditional check has been defined, execute it
					String strTraditionalAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
					if(strTraditionalAccess!=null){
						bHasAccess                  =   hasAccessForFunction(context, strTraditionalAccess);
					}else{
						bHasAccess                  =   true;
					}
				}
			}

			// Added by Vinit - Starts 7/8/2013
			// show TPL difference list to MT14 as well..

			//MSIL.BuyerAdminDepartment.Name
			String strBuyerAdmin = FrameworkProperties.getProperty("MSIL.BuyerAdminDepartment.Name");

			String strWorksForDept = (String)(String) mPersonAttrs.get("WorksForDept");
			if(null!=strWorksForDept && null!=strBuyerAdmin && strBuyerAdmin.length()>0 && strWorksForDept.contains(strBuyerAdmin))
			{
				bHasAccess = true;
			}
			// Added by Vinit - Ends 7/8/2013
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasFunctionAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(bHasAccess);
	}
	//Added by Sarita on 11/10/2013 for revised Sourcing Target Cost Approval ID -SK-11/10/2013.
	/**
   @ Created by Sarita used for form "MSILRevisedsourcingCostApproversForm"
   @ Checker field only accessible to EN8C Owner
   @ Sarita Kumari
   @ Date 11/10/2013
	 **/
	public Boolean getEN8CCheckerFieldAccess(Context context, String[] args) throws Exception
	{
		boolean check                      =  false;
		try{
			HashMap hmProgramMap       = (HashMap)JPO.unpackArgs(args);
			String strObjectId                = (String)hmProgramMap.get("objectId");
			if (null!=strObjectId && !"".equalsIgnoreCase(strObjectId) && !"null".equalsIgnoreCase(strObjectId)) 
			{
				String strContextUser      =   context.getUser().toString();
				DomainObject doObject      =    DomainObject.newInstance(context, strObjectId);
				String strType             = doObject.getInfo(context, "type");
				String strEN8COwner = "";
				if(null != strType && strType.equalsIgnoreCase("RRFQ"))
				{
					strEN8COwner = doObject.getInfo(context, "attribute[EN8C Owner]");
					if(!strContextUser.equals("") && strContextUser != null && strContextUser.equalsIgnoreCase(strEN8COwner))
					{
						check                         = true;
					}
				}
				else if(null != strType && strType.equalsIgnoreCase("RRFQ Package"))
				{
					StringList slEN8COwner = doObject.getInfoList(context, "from[RRFQ Package To RRFQ].to.attribute[EN8C Owner]");
					if(slEN8COwner !=null && slEN8COwner.size() > 0 && slEN8COwner.contains(strContextUser))
					{
						check                         = true;
					}
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return new Boolean(check);
	}//Ended by Sarita on 11/10/2013 for Updated Revised Cost Checker field  ID -SK-11/10/2013
	//Added by Sarita on 11/10/2013 for revised Sourcing Target Cost Approval ID -SK-11/10/2013.
	/**
    @ Created by Sarita used for form "MSILRevisedsourcingCostApproversForm"
    @ DPM Field Hide on Form if DPM is initiator for Revised sourcing Target Cost Initiator
    @ Date 11/10/2013
	 **/
	public Boolean getEN8CDPMFieldAccess(Context context, String[] args) throws Exception
	{
		boolean check                      =  true;
		try{
			String strContextUser       =   context.getUser().toString();
			String strENCCDPM       = "";//MSILOrganization.getEN8CDeptHead(context);
			if(null != strContextUser && !"".equalsIgnoreCase(strContextUser) && strContextUser.equalsIgnoreCase(strENCCDPM))
			{
				check                         = false;
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return new Boolean(check);
	}//Ended by Sarita on 11/10/2013 ID -SK-11/10/2013.
	//Added by Sarita on 11/10/2013 for revised Sourcing Target Cost Approval ID -SK-11/10/2013.
	/**
    @ Created by Sarita used for access to EN-CCDPM Update LOCLOIDashboard "
    @ DPM able to View LOCLOIDashboard
    @ Date 11/10/2013
	 **/
	public Boolean getEngineerAndEN8CDPMAccess(Context context, String[] args) throws Exception
	{
		boolean check                      = false;
		try{
			String strContextUser       =   context.getUser().toString();
			String strEngineerGroup                           = PropertyUtil.getSchemaProperty(context,"group_MSILEngineer");
			String strENCCDPM       = "";//MSILOrganization.getEN8CDeptHead(context);
			matrix.db.Person pUtil               = new matrix.db.Person(strContextUser);
			boolean bCheckEngineerGroup          = pUtil.isAssigned(context, strEngineerGroup);
			if((strContextUser != null && !"".equalsIgnoreCase(strContextUser) && strContextUser.equalsIgnoreCase(strENCCDPM)) || bCheckEngineerGroup)
			{
				check                         = true;
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return new Boolean(check);
	}//Ended by Sarita on 11/10/2013 ID -SK-11/10/2013.
	//Added By Naveena - for open requote task - extend due date - CR-  65075 starts
	public Boolean getQuotewiseAccess(Context context, String[] args) throws Exception
	{
		boolean check                      =  false;
		try{
			HashMap hmProgramMap       = (HashMap)JPO.unpackArgs(args);
			String strObjectId                = (String)hmProgramMap.get("objectId");
			if (null!=strObjectId && !"".equalsIgnoreCase(strObjectId) && !"null".equalsIgnoreCase(strObjectId)) 
			{
				DomainObject doObject      =   DomainObject.newInstance(context, strObjectId);
				String strType             =   doObject.getInfo(context, "type");
				if(null != strType && strType.equalsIgnoreCase("RFQ"))
				{
					String strWhere = "current == 'Returned' && revision==last && name !~~ \"MyQuote*\" && name !~~ \"BestQuote*\" && attribute[MSILConstants.ATTRIBUTE_QUOTATION_STATUS] == 'Regret Quote' && attribute[Comments] == 'Regret - Due Date Passed' ";
					StringList sListSelect = new StringList();
					sListSelect.add(DomainConstants.SELECT_ID);
					sListSelect.add(DomainConstants.SELECT_NAME);
					MapList list = doObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_RTS_QUOTATION, DomainConstants.TYPE_RTS_QUOTATION, false, true,(short) 1, sListSelect, null, strWhere,null,"*",null, null);
					int iListSize = list.size();
					if(list != null && iListSize >0)
					{
						check   = true;
					}

				}

			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return new Boolean(check);
	}
	//Added By Naveena - for open requote task - extend due date - CR-  65075 - Ends
	// For LOC lOI Table to be visible in LOCLOI dashboard
	public Boolean hasFunctionLinkAccess(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		boolean bTrue               =   false;
		boolean breturn             =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			//Get the value from page object for the passed key value.
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strRFQId = (String)hmParamMap.get("strRowIds");

			String strValidAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
			HashMap hMap;
			String[] methodargs;
			hMap          = new HashMap();
			hMap.put("RFQId",strRFQId);
			methodargs    = JPO.packArgs(hMap);
			bHasAccess                  =   hasAccessForFunction(context, strValidAccess);
			//breturn                     =   isTogenerateLOCLOI(context,methodargs);
			//if(bHasAccess && breturn){
			if(bHasAccess){
				bTrue= true;
			}
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasFunctionAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  new Boolean(bTrue);
	}
	/**
	 * This method is used to provide parent role based on Context User's Vault by reading from Properties Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getSCParentRoleBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strParentRole ="";
		try{
			String sContextUserVault = context.getVault().toString();
			if(null !=sContextUserVault)			
				sContextUserVault = sContextUserVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String sSupplyChainParentRoleKey ="MSILSourcing."+sContextUserVault+".SupplyChainParentRole";
			strParentRole = i18nNow.getI18nString(sSupplyChainParentRoleKey,"emxSourcingStringResource",context.getSession().getLanguage());
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getSCParentRoleBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strParentRole;	 
	}
	/**
	 * This method is used to provide parent role based on Context User's Vault by reading from Properties Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getCostingDeptNameBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strResponsibleCostingDept ="";
		try{
			String strProductionVault = context.getVault().toString();          
			if(null !=strProductionVault)   
				strProductionVault = strProductionVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String sResponsibleEN8CDeptKey ="MSILSourcing."+strProductionVault+".CostingDepartment";
			strResponsibleCostingDept = i18nNow.getI18nString(sResponsibleEN8CDeptKey,"emxSourcingStringResource",context.getSession().getLanguage());    
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getCostingDeptNameBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strResponsibleCostingDept;	 
	}
	/**
	 * This method is used to provide parent role based on Context User's Vault by reading from Properties Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getENGGParentRoleBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strENGGParentRole ="";
		try{
			String sContextUserVault = context.getVault().toString();
			if(null !=sContextUserVault)                                       
				sContextUserVault = sContextUserVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String sENGGParentRoleKey ="MSILSourcing."+sContextUserVault+".ENGGParentRole";
			strENGGParentRole = i18nNow.getI18nString(sENGGParentRoleKey,"emxSourcingStringResource",context.getSession().getLanguage());
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getENGGParentRoleBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strENGGParentRole;	 
	}
	/**
	 * This method is used to provide all Supply Chain Departments based on Context User's Vault by reading from Property Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getSCDeptsBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strAllMTDepartment ="";
		try{
			String sContextUserVault = context.getVault().toString();
			if(null !=sContextUserVault)                                       
				sContextUserVault = sContextUserVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String strAllMTDepartmentKey ="MSIL.MT"+sContextUserVault+".AvailableMTDepartment";
			strAllMTDepartment           =   FrameworkProperties.getProperty(context,strAllMTDepartmentKey);
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getENGGParentRoleBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strAllMTDepartment;
	}
	/**
	 * This method is used to check for Part Replacement access given only to ENGG users
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Nitika
	 */
	public boolean getENGGAccessForPartReplacement(Context context, String[] args) throws Exception
	{
		HashMap hmParamMap              =   (HashMap)JPO.unpackArgs(args);
		//Updated by Nitika for SMIPL
		boolean hasENAccess = hasAccessForFunction(context,"MSILMasters.Access.Users");
		if(hasENAccess)
		{
			String strCostDept= getCostingDeptNameBasedOnContextUserVault(context,args);
			if(PersonUtil.hasAssignment(context,strCostDept))
			{
				hasENAccess=false;
			}
		}
		return hasENAccess;
	}
	/**
	 * This method is used to provide ENGG Directorate based on Context User's Vault by reading from Property Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getENGGDirectorateBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strENGGDirectorate ="";
		try{
			String sContextUserVault = context.getVault().toString();
			if(null !=sContextUserVault)                                       
				sContextUserVault = sContextUserVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String strENGGDirectorateKey ="MSIL.EngineeringDirectorate"+sContextUserVault+".Name";
			strENGGDirectorate           =   FrameworkProperties.getProperty(context,strENGGDirectorateKey);
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getENGGParentRoleBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strENGGDirectorate;
	}
	/**
	 * This method is used to provide ENGG Directorate based on Context User's Vault by reading from Property Key
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static String  getSCDirectorateBasedOnContextUserVault(Context context,String[] args) throws Exception
	{ 
		String strSCDirectorate ="";
		try{
			String sContextUserVault = context.getVault().toString();
			if(null !=sContextUserVault)                                       
				sContextUserVault = sContextUserVault.replaceAll(" ",""); // removing space if the vault name is having Space in between
			String strSCDirectorateKey ="MSIL.SupplyChainDirectorate"+sContextUserVault+".Name";
			strSCDirectorate           =   FrameworkProperties.getProperty(context,strSCDirectorateKey);
		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : getENGGParentRoleBasedOnContextUserVault() : "+ex.getMessage());
		}
		return strSCDirectorate;
	}

	/**
	 * This method is used to Check Context User is Associated To MSIL or not
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Ajit
	 */
	public static boolean  isContextUserAssociatedToMSIL(Context context,String[] args) throws Exception
	{ 
		boolean isContextUserAssociatedToMSIL =true;
		try{
			String sContextUserVault = context.getVault().toString();
			String strVaultWithoutPDP               =   FrameworkProperties.getProperty(context, "MSIL.VaultWithoutPDP.Name");
			if(strVaultWithoutPDP.contains(sContextUserVault)){
				isContextUserAssociatedToMSIL = false;
			}

		}catch (Exception ex)
		{
			log.error("Got Exception in MSILAccessRights : isContextUserAssociatedToMSIL() : "+ex.getMessage());
		}
		return isContextUserAssociatedToMSIL;
	}
	/**
	 * This method is used to fix VCP option on right click of RFQ-ENGG-13187
	 * @param context            : The Matrix Context object
	 * @return String
	 * @throws Exception if operation fails
	 * @author Nitika
	 */
	public Boolean hasVCPAccessOnRMB(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			//Get the value from page object for the passed key value.
			HashMap hmFieldSettings     =   (HashMap) hmParamMap.get("SETTINGS");
			String strValidAccess       =   (String)hmFieldSettings.get("MSILAccessKey");
			bHasAccess                  =   hasAccessForFunction(context, strValidAccess);
			if(bHasAccess)
			{
				String objectId = (String) hmParamMap.get("objectId");
				if(null!=objectId && !objectId.equals(""))
				{
					DomainObject doObject = DomainObject.newInstance(context, objectId);
					String strType = doObject.getInfo(context, DomainConstants.SELECT_TYPE);
					if(null!=strType && strType.equalsIgnoreCase("RFQ"))
						bHasAccess=false;
				}

			}
			
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			
		}
		return  new Boolean(bHasAccess);
	}/**
	 * Checks if the user has no access on the specified UI3 component based on passed setting
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *            0 - HashMap containing one String entry for key "objectId"
	 *                Hashmap has the function name as well for which access need to be checked.
	 * @return        boolean false if user have no access
	 * @throws        Exception if the operation fails
	 * @since         Build1
	 **/
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSectionManager(Context context, String[] args) throws Exception
	{
		boolean bHasAccess          =   false;
		MapList mlPerson=new MapList();
		 MapList mlPerson1 = new MapList();
		try{
			
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			//String objectId = (String) hmParamMap.get("objectId");
			String RELATIONSHIP_WMS_REPORTING_MANAGER= PropertyUtil.getSchemaProperty(context, "relationship_WMSReportingManager");
	       	String ATTRIBUTE_WMS_DEFAULT_ROLE= PropertyUtil.getSchemaProperty(context, "attribute_WMSDefaultRole");
	       	com.matrixone.apps.program.ProjectSpace project =
	                    (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
	                            DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
	           String strProjectId = (String) hmParamMap.get("objectId");
	           DomainObject domProject = DomainObject.newInstance(context, strProjectId);
	           String contextUser = context.getUser();
	           String userID      = PersonUtil.getPersonObjectID(context, contextUser);
	           ArrayList<String>arrayListIds = new ArrayList<String>();
	           if(UIUtil.isNotNullAndNotEmpty(userID))
	           {
	               StringList strListBusSelects = new StringList(DomainConstants.SELECT_ID);
	               strListBusSelects.add(DomainConstants.SELECT_NAME);
	               strListBusSelects.add("attribute["+ATTRIBUTE_WMS_DEFAULT_ROLE+"]");
	               DomainObject domContextUser = DomainObject.newInstance(context,userID);
	               mlPerson= domContextUser.getRelatedObjects(context, // matrix context
	               	RELATIONSHIP_WMS_REPORTING_MANAGER, // relationship pattern
	                      DomainConstants.TYPE_PERSON, // type pattern
	                       strListBusSelects, // object selects
	                       DomainConstants.EMPTY_STRINGLIST, // relationship selects
	                       false, // to direction
	                       true, // from direction
	                       (short) 0, // recursion level
	                       //DomainConstants.EMPTY_STRING, // object where clause
						   "attribute[WMSDefaultRole].value == SectionManager",
	                       DomainConstants.EMPTY_STRING, // relationship where clause
	                       0);
	               //Iterator itr = mlPerson.iterator();
	              
	               for ( int i=0; i<mlPerson.size(); i++ ) {
	                   Map projectMap = (Map) mlPerson.get( i );
	                   //Map projectMap = (Map) itr.next();

	                   // Get ProjectSpace business object id
	                   String personName = (String) projectMap.get(SELECT_NAME);
	                   String mqlString = "print person $1 select $2 dump $3";
	                  	List<String> queryParameterList = new ArrayList<String>();
	                  	queryParameterList.add(personName);
	                  	queryParameterList.add("product.derivative.derivative");
	                  	queryParameterList.add("|");

	                  	String[] queryParameterArray = new String[queryParameterList.size()];
	                  	queryParameterList.toArray(queryParameterArray);

	                  	String productNameList = MqlUtil.mqlCommand(context, true, true, mqlString, true,queryParameterArray);
	                  	StringList assignProductList = FrameworkUtil.split(productNameList, "|");
	                 	if(assignProductList.contains(ProgramCentralConstants.PRG_LICENSE_ARRAY[0]))
	                  	{
	                  		mlPerson1.add(projectMap);
	                  	}
	               }
	           }  
		}catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			log.error("Got exception in method hasNoAccess:  " + ex.toString());
			MqlUtil.mqlCommand(context, "notice Error has occured while trying to get access for your functions. \\nPlease contact your system administrator.");
		}
		return  mlPerson1;
	}
	public String isSectionManagerORPICConnected(Context context, String[] args) throws Exception
	{
		String StrValue          =   EMPTY_STRING;
		StringList assignProductList =new StringList();
		try
		{
			HashMap hmParamMap          =   (HashMap)JPO.unpackArgs(args);
			String objectId = (String) hmParamMap.get("objectId");
			String role = (String) hmParamMap.get("role");
			String mqlString = "print bus $1 select $2 dump $3";
            	List<String> queryParameterList = new ArrayList<String>();
            	queryParameterList.add(objectId);
            	queryParameterList.add("from[Member].attribute[Project Role].value");
            	queryParameterList.add("|");

            	String[] queryParameterArray = new String[queryParameterList.size()];
            	queryParameterList.toArray(queryParameterArray);

            	String productNameList = MqlUtil.mqlCommand(context, true, true, mqlString, true,queryParameterArray);
            	assignProductList = FrameworkUtil.split(productNameList, "|");
            	for(int i=0;i<assignProductList.size();i++) {
					if(assignProductList.get(i).equals(role))
					{
						//StrValue=(String)assignProductList.get(i);
						StrValue="true";
		
					}
            	}
			
		}
		catch (Exception ex)
		{
			log.error("Error displayed to user: Error has occured while trying to get access for your functions. Please contact your system administrator");
			
		}
		return  StrValue;
	}
	/**
	* Method gets called from Project-Member page-Add PIC
	* return list of person reporting to context person if context person is project owner
	* if not a owner gets its reporting manager and bring all its subordinates ..
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	 


	 	@com.matrixone.apps.framework.ui.ProgramCallable
	   public MapList  getApplicablePersonInCharge(Context context, String[] args) throws Exception {
	   	MapList mlPeople =new MapList();
	   	MapList mlnewPeople=new MapList();
	       try {
	       	Map mInputMap = (Map) JPO.unpackArgs(args);
	       	String RELATIONSHIP_WMS_REPORTING_MANAGER= PropertyUtil.getSchemaProperty(context, "relationship_WMSReportingManager");
	       	String ATTRIBUTE_WMS_DEFAULT_ROLE= PropertyUtil.getSchemaProperty(context, "attribute_WMSDefaultRole");
	       	com.matrixone.apps.program.ProjectSpace project =
	                    (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
	                            DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
	           String strProjectId = (String)mInputMap.get("objectId");
	           project.setId(strProjectId);
	           String contextUser = context.getUser();
	           
	           String userID      = PersonUtil.getPersonObjectID(context, contextUser);
	           ArrayList<String>arrayListIds = new ArrayList<String>();
	           DomainObject domContextUser = DomainObject.newInstance(context,userID);
	           String strProjectOwner =  project.getInfo(context,DomainConstants.SELECT_OWNER);
	           if(!strProjectOwner.equals(contextUser)){  // if not owner gets is reporting person 
	            userID = domContextUser.getInfo(context, "from["+WMSConstants_mxJPO.RELATIONSHIP_WMS_REPORTING_MANAGER+"].to.id");
	            domContextUser.setId(userID);
	           }
	           
	           if(UIUtil.isNotNullAndNotEmpty(userID))
	           {
	               StringList strListBusSelects = new StringList(DomainConstants.SELECT_ID);
	               strListBusSelects.add(DomainConstants.SELECT_NAME);
	               strListBusSelects.add("attribute["+WMSConstants_mxJPO.ATTRIBUTE_WMS_DEFAULT_ROLE+"]");
	                mlPeople = domContextUser.getRelatedObjects(context, // matrix context
	               	WMSConstants_mxJPO.RELATIONSHIP_WMS_REPORTING_MANAGER, // relationship pattern
	                      DomainConstants.TYPE_PERSON, // type pattern
	                       strListBusSelects, // object selects
	                       DomainConstants.EMPTY_STRINGLIST, // relationship selects
	                       false, // to direction
	                       true, // from direction
	                       (short) 0, // recursion level
	                       "attribute[WMSDefaultRole].value == ProjectInCharge",
	                       "", 0);
	                  for ( int i=0; i<mlPeople.size(); i++ ) {
		                   Map projectMap = (Map) mlPeople.get( i );
		                   // Get ProjectSpace business object id
		                   String personName = (String) projectMap.get(SELECT_NAME);
		                   String mqlString = "print person $1 select $2 dump $3";
		                  	List<String> queryParameterList = new ArrayList<String>();
		                  	queryParameterList.add(personName);
		                  	queryParameterList.add("product.derivative.derivative");
		                  	queryParameterList.add("|");

		                  	String[] queryParameterArray = new String[queryParameterList.size()];
		                  	queryParameterList.toArray(queryParameterArray);

		                  	String productNameList = MqlUtil.mqlCommand(context, true, true, mqlString, true,queryParameterArray);
		                  	StringList assignProductList = FrameworkUtil.split(productNameList, "|");
		                 	if(assignProductList.contains(ProgramCentralConstants.PRG_LICENSE_ARRAY[0]))
		                  	{
		                 		mlnewPeople.add(projectMap);
		                  	}
		               }
	           }
	       }catch (Exception ex) {
			throw ex;// TODO: handle exception
	       }
	       return mlnewPeople;
	   }
	 	 /**
	   	  * This method returns true if logged in user is SectionManager of poject.
	   	  * @param context - The ENOVIA <code>Context</code> object.
	   	  * @param args
	   	  * @return true/false
	   	  * @throws Exception
	   	  */
	 public Boolean hasPICCommandAccess(Context context, String[] args) throws Exception{
		 
		 boolean hasAccess = false;
   	     boolean isCivil = isPersonFromCivil(context, args);
	    if(isCivil) {
   			Map inputMap   = (Map) JPO.unpackArgs(args);
   		    String strobjId = (String) inputMap.get("objectId");
		    com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
           String strProjectId = (String)inputMap.get("objectId");
           project.setId(strProjectId);
           String strProjectOwner =  project.getInfo(context,DomainConstants.SELECT_OWNER);
   		   String contextUser = context.getUser();
   		   if(strProjectOwner.equals(contextUser)) {
    			hasAccess=true;
   		    }
			String userID      = PersonUtil.getPersonObjectID(context, contextUser);
        	String strMQLCommand        =   "print bus '"+ userID + "' select to[Member|from.id=='"+strProjectId+"'].attribute[Project Role] dump";
			MQLCommand mcGetAssign      =   new MQLCommand();
			mcGetAssign.open(context);
			mcGetAssign.executeCommand(context,strMQLCommand);
			String strMQLCommandResult  =   mcGetAssign.getResult();
			strMQLCommandResult         =   strMQLCommandResult.trim();
	    	if(strMQLCommandResult.equalsIgnoreCase("SectionManager"))
         	{
        		  
        		hasAccess=true;
         	}
   		 }
		 return hasAccess;
	 }
	/**
	* This method returns false if logged in user is not a MSIL user.
	* @param context - The ENOVIA <code>Context</code> object.
	* @param args
	* @return true/false
	* @throws Exception
	*/
	public Boolean hasAccessForHistoryCommand(Context context, String[] args) throws Exception
	{
		boolean check = true;
		try{
			Company company = com.matrixone.apps.common.Person.getPerson(context).getCompany(context);
			String strCompany = company.getName();
			if(null != strCompany && !"".equalsIgnoreCase(strCompany) && !strCompany.equalsIgnoreCase("MSIL"))
			{
				check = false;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new Boolean(check);
	}
	/**Method checks is person is from CIVIL Dept.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	
	 public boolean isPersonFromCivil(Context context,String[] args) throws Exception
	   {
		 boolean bReturn =false;
		 try {
			 Map m = new HashMap();
			 m.put("MSILAccessKey", "WMSCIVIL.DepartmentDashboard.ENAccess");
			 Map mColumn = new HashMap();
	 	     mColumn.put("SETTINGS", m);
	 	      bReturn =isCivilUser(context,JPO.packArgs(mColumn));
	 		 }catch(Exception e) {
				 e.printStackTrace();
			 }
		 return bReturn;
		 
	 }
	
	
	
	
	/**
	* This method returns false if logged in user is not a Civil user.
	* @param context - The ENOVIA <code>Context</code> object.
	* @param args
	* @return true/false
	* @throws Exception
	*/
	 public boolean isCivilUser(Context context, String args[]) throws Exception
		{
		 HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
			HashMap hmColumnMap         =   (HashMap) programMap.get("SETTINGS");
			String strAccessKey       =   (String) hmColumnMap.get("MSILAccessKey");
			boolean bHasAccess          =   false;
			try {
				Properties _classCurrencyConfig  =   new Properties();
				Page page= new Page("MSILAccessRights");
				_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
				String strRoleList = _classCurrencyConfig.getProperty(strAccessKey);
				strRoleList                 =   strRoleList.trim();
				//get context user
				String strContextUser       =   context.getUser();
			 	//get all assignment and ancestors for context user
				String strMQLCommand        =   "print bus Person '"+ strContextUser + "' - select to[Member|from.type=='Department' || from.type=='Business Unit'].from.name dump";
				MQLCommand mcGetAssign      =   new MQLCommand();
				mcGetAssign.open(context);
				String strMQLCommandResult = MqlUtil.mqlCommand(context, strMQLCommand);
				strMQLCommandResult         =   strMQLCommandResult.trim();
				mcGetAssign.close(context);
				//Check if the user has any of the assignment based on the passed key value
				StringList slAccessList     =   FrameworkUtil.split(strRoleList,",");
				
				int iAccessListSize         =   slAccessList.size();
				String strAccessName        =   "";
				for (int k = 0; k < iAccessListSize; k++) {
					strAccessName = (String) slAccessList.elementAt(k);
					if (strMQLCommandResult.contains(strAccessName))
					{
						bHasAccess = true;
						break;
					}
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			return  bHasAccess;
		}
	 /**
		* This method returns false if logged in user is not a Civil user.
		* @param context - The ENOVIA <code>Context</code> object.
		* @param args
		* @return true/false
		* @throws Exception
		*/
		 public boolean checkDepartmentAccess(Context context, String args[]) throws Exception
			{
			 HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
				HashMap hmColumnMap         =   (HashMap) programMap.get("SETTINGS");
				String strAccessKey       =   (String) hmColumnMap.get("MSILAccessKey");
				boolean bHasAccess          =   true;
				try {
					Properties _classCurrencyConfig  =   new Properties();
					Page page= new Page("MSILAccessRights");
					_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
					String strRoleList = _classCurrencyConfig.getProperty(strAccessKey);
					strRoleList                 =   strRoleList.trim();
					//get context user
					String strContextUser       =   context.getUser();
					//get all assignment and ancestors for context user
					String strMQLCommand        =   "print bus Person '"+ strContextUser + "' - select to[Member|from.type=='Department'||from.type=='Business Unit'].from.name dump";
					MQLCommand mcGetAssign      =   new MQLCommand();
					mcGetAssign.open(context);
					String strMQLCommandResult = MqlUtil.mqlCommand(context, strMQLCommand);
					strMQLCommandResult         =   strMQLCommandResult.trim();
					mcGetAssign.close(context);
					//Check if the user has any of the assignment based on the passed key value
					StringList slAccessList     =   FrameworkUtil.split(strRoleList,",");
					
					int iAccessListSize         =   slAccessList.size();
					String strAccessName        =   "";
					for (int k = 0; k < iAccessListSize; k++) {
						strAccessName = (String) slAccessList.elementAt(k);
						if (strMQLCommandResult.contains(strAccessName))
						{
							bHasAccess = false;
							break;
						}
					}
				}catch (Exception e) {
					// TODO: handle exception
				}
				return  bHasAccess;
			}
			/**
			 * Checks if the user has the access on the specified UI3 component based on passed setting
			 *
			 * @param context the eMatrix <code>Context</code> object
			 * @param String  holds the key for Function Access
			 * @return        boolean if user can have access
			 * @throws        Exception if the operation fails
			 * @since         Build1
			 **/
			public static boolean hasAccessForOOTBCommand(Context context, String strAccessKey) throws Exception
			{
				boolean bHasAccess          =   true;
				if(strAccessKey.equalsIgnoreCase("WMSCIVIL.DepartmentDashboard.ENAccess"))
				{
					bHasAccess=false;
					
				}
				//String languageString = context.getSession().getLanguage();
				//Get the value from page object for the passed key value.
				//strKey = getStrVaule(context, strAccessKey);
				//if(null!=_classAccessRights){
				try {
					Properties _classCurrencyConfig  =   new Properties();
					Page page= new Page("MSILAccessRights");
					_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
					String strRoleList = _classCurrencyConfig.getProperty(strAccessKey);
					//String strRoleList          =   _classAccessRights.getProperty(strAccessKey);
					//String strRoleList = EnoviaResourceBundle.getProperty(context, "MSILAccessRights", strKey,languageString);//context,strAccessKey);
					strRoleList                 =   strRoleList.trim();
					//get context user
					String strContextUser       =   context.getUser();
					//get all assignment and ancestors for context user
					String strMQLCommand        =   "temp query bus Person '"+ strContextUser + "' * select to[Member].from.name dump";
					//String strMQLCommand        =   "print person '"+ strContextUser + "' select assignment.ancestor assignment.child dump";
					MQLCommand mcGetAssign      =   new MQLCommand();
					mcGetAssign.open(context);
					//mcGetAssign.executeCommand(context,strMQLCommand);
					String strMQLCommandResult = MqlUtil.mqlCommand(context, strMQLCommand);
					strMQLCommandResult         =   strMQLCommandResult.trim();
				 	mcGetAssign.close(context);
					StringList slUserRoleList   =   FrameworkUtil.split(strMQLCommandResult,",");

					//Check if the user has any of the assignment based on the passed key value
					StringList slAccessList     =   FrameworkUtil.split(strRoleList,",");
					
					int iAccessListSize         =   slAccessList.size();
					String strAccessName        =   "";
					for (int k = 0; k < iAccessListSize; k++) {
						strAccessName = (String) slAccessList.elementAt(k);
					 	if (strMQLCommandResult.contains(strAccessName))
						{
							bHasAccess = false;
							break;
						}else{
							bHasAccess = true;
						}
					}
				}catch (Exception e) {
					// TODO: handle exception
				}
				return  bHasAccess;
			}
			
	/**
	* This method returns false if logged in user is not a PE user.
	* @param context - The ENOVIA <code>Context</code> object.
	* @param args
	* @return true/false
	* @throws Exception
	*/
			public static boolean isPEUser(Context context, String args[]) throws Exception
			{
				boolean bHasAccess=false;
				HashMap programMap          =   (HashMap) JPO.unpackArgs(args);
				HashMap hmColumnMap         =   (HashMap) programMap.get("SETTINGS");
				String strAccessKey       =   (String) hmColumnMap.get("MSILAccessKey");
				
			try {
				Properties _classCurrencyConfig  =   new Properties();
				Page page= new Page("MSILAccessRights");
				_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
				String strRoleList = _classCurrencyConfig.getProperty(strAccessKey);
				strRoleList                 =   strRoleList.trim();
				//get context user
				String strContextUser       =   context.getUser();
				//get all assignment and ancestors for context user
				String strMQLCommand        =   "print bus Person '"+ strContextUser + "' - select to[Member|from.type=='Department'||from.type=='Business Unit'].from.name dump";
				MQLCommand mcGetAssign      =   new MQLCommand();
				mcGetAssign.open(context);
				String strMQLCommandResult = MqlUtil.mqlCommand(context, strMQLCommand);
				strMQLCommandResult         =   strMQLCommandResult.trim();
				mcGetAssign.close(context);
				//Check if the user has any of the assignment based on the passed key value
				StringList slAccessList     =   FrameworkUtil.split(strRoleList,",");
				
				int iAccessListSize         =   slAccessList.size();
				String strAccessName        =   "";
				for (int k = 0; k < iAccessListSize; k++) {
					strAccessName = (String) slAccessList.elementAt(k);
					if (strMQLCommandResult.contains(strAccessName))
					{
						bHasAccess = true;
						break;
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		return bHasAccess;				
	}
}
