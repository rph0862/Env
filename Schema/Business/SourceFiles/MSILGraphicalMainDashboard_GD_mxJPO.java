/* ${CLASSNAME}.java

Author : Intelizign



Change History:

Date           |    Change By    | Tag to be searched |       Details

===========================================================================================================

30-Oct-2015    |    Intelizign   |      -             |     Initial Release
20-Jul-2016    |    Dheeraj Garg |                    | WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project
01-Aug-2016    |    Dheeraj Garg |                    | Hinkai/SQR table no hyperlink on Open, Closed points
03-Aug-2016    |    Dheeraj Garg |                    | Blank Dashboard for DPM/DVM, if no Active project.
05-Aug-2016    |    Dheeraj Garg |                    | Slip days are different in WBS page and Dashboard.
09-Aug-2016    |    Dheeraj Garg |                    | Blank ECN graph.
10-Aug-2016    |    Dheeraj Garg |                    | Add name of user instead of id.
11-Aug-2016    |    Dheeraj Garg |                    | Order of the dept list should be in particular order.
12-Aug-2016    |    Dheeraj Garg |                    | Week Wise Report access to be given to NPE DPM & NPE Incharge.
05-Sep-2016    |    Dheeraj Garg |                    | Order of the dept list should be in particular order - License Graph.
13-Sep-2016    |    Dheeraj Garg |                    | Graphical Dashboard option is not present on ï¿½DPO NPEï¿½ Enovia login.
29-Jun-2018    |    Vinit        |   29-Jun-2018      | Slip Days to be calculated based on estimated start date and percent complete
03-Jul-2018    |    Ajit         |   27/06/2018       | Graphical Dashboard to show not active projects also
 */



import java.io.File;

import java.io.FileOutputStream;

import java.text.SimpleDateFormat;

import java.util.HashMap;

import java.util.HashSet;

import java.util.Iterator;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.Set;

import java.util.StringTokenizer;

import java.util.Vector;



import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;

import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;



import org.w3c.dom.Attr;

import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.NamedNodeMap;



import matrix.db.BusinessObject;

import matrix.db.Context;

import matrix.db.FileItr;

import matrix.db.FileList;

import matrix.db.JPO;

import matrix.db.MQLCommand;

import matrix.util.LicenseUtil;

import matrix.util.Pattern;

import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;

import com.matrixone.apps.domain.DomainObject;

import com.matrixone.apps.domain.DomainRelationship;

import com.matrixone.apps.domain.util.ContextUtil;

import com.matrixone.apps.domain.util.FrameworkException;

import com.matrixone.apps.domain.util.FrameworkProperties;

import com.matrixone.apps.domain.util.FrameworkUtil;

import com.matrixone.apps.domain.util.MapList;

import com.matrixone.apps.domain.util.MqlUtil;

import com.matrixone.apps.domain.util.PersonUtil;

import com.matrixone.apps.domain.util.PropertyUtil;

import com.matrixone.apps.domain.util.StringUtil;

import com.matrixone.apps.domain.util.XSSUtil;

import com.matrixone.apps.domain.util.eMatrixDateFormat;

import com.matrixone.apps.framework.ui.UICache;

import com.matrixone.apps.framework.ui.UINavigatorUtil;

import com.matrixone.apps.framework.ui.UITable;



import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.Date;

import com.matrixone.apps.program.ProgramCentralConstants;

import com.matrixone.apps.program.ProjectSpace;

import com.matrixone.jdom.Attribute;



import org.w3c.dom.NodeList;

import org.w3c.dom.Node;



public class MSILGraphicalMainDashboard_GD_mxJPO implements MSILConstants_mxJPO

{

    com.matrixone.apps.program.ProjectSpace projectSpace = null;

    com.matrixone.apps.program.Task task = null;



    public MSILGraphicalMainDashboard_GD_mxJPO (Context context, String[] args) throws Exception

    {

        projectSpace = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);

        task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

    }

    

    /**

     * This method will be used to redirect the page from msilIntermediateDashboard.jsp.

     * 1. If the user is DVM PE OR NPE DPM

     * 2. If the user is not DVM PE & not NPE DPM but belongs to NPE department and owner of any active master project 

     * the command Graphical Dashboard will be visible only if the user is an owner of any Project

     * @param context

     * @param args

     * @return

     * @throws Exception

     */

    public Boolean hasAccessForNPEDashboard(Context context, String[] args) throws Exception

    {

        boolean bHasAccess=false;

        boolean bNPEMember=false;

        try{

            com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);                

            String strContextUser = context.getUser().trim();

            String strContextUserId = person.getPerson(context, strContextUser).getId();

            DomainObject doPerson = DomainObject.newInstance(context, strContextUserId);

            //${CLASS:MSILGraphicalDashboard} progGraphicalDashboard = new ${CLASS:MSILGraphicalDashboard}(context, args);

            Map mpUserInfo=getUserList(context, doPerson);

            if(null !=mpUserInfo && !mpUserInfo.isEmpty()){

                String strConnectedAsLead=(String)mpUserInfo.get("ConnectedAsLead");

                if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strConnectedAsLead) && (PROJECT_MANAGEMENT_DIRECTORATE.equals(strConnectedAsLead) || PROJECT_CREATE_DEPARTMENT.equals(strConnectedAsLead))){

                    bHasAccess=true;

                }else{//If user is not DVM PE and not DPM NPE but a NPE member.

                    String strNPEMember=doPerson.getInfo(context,"to["+DomainConstants.RELATIONSHIP_MEMBER+"].from["+DomainConstants.TYPE_DEPARTMENT+"].name");

                    if(null != strNPEMember && PROJECT_CREATE_DEPARTMENT.equals(strNPEMember)){

                        bHasAccess=true;

                    }

                }

            }

        } catch(Exception ex){

            throw ex;

        }

        return bHasAccess;

    }



    /*

     * Get All users from the system and prepare map with following data

     * 1) LeadResponsibility - type of the object to which user is connected with relationship Lead Responsibility

     * 2) ConnectedAsLead - name of the object to which user is connected with relationship Lead Responsibility

     * 3) Department - List of the departments that come under this user

     * 4) UserRole - MSIL Project Role set on Lead Responsibility relationship

     * 5) PersonId - Object Id of person

     * 6) PersonName - Name of person

     */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public Map getUserList(Context context, DomainObject personDO) throws Exception

    {

        MapList userList = new MapList();

        Map userMap = new HashMap();

        try

        {

            Pattern typePattern = new Pattern(DomainConstants.TYPE_DEPARTMENT);

            typePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);



            Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_DIVISION);

            relPattern.addPattern(DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT);



            StringList objectSelectsList = new StringList();

            objectSelectsList.addElement(DomainObject.SELECT_ID);

            objectSelectsList.addElement(DomainObject.SELECT_TYPE);

            objectSelectsList.addElement(DomainObject.SELECT_NAME);



            StringList objectList = new StringList();

            objectList.addElement(DomainObject.SELECT_ID);

            objectList.addElement(DomainObject.SELECT_NAME);



            String strWhereclause = "current==Active";



            objectList.add("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.id");

            objectList.add("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.type");

            objectList.add("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.name");

            objectList.add("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ROLE + "]");

            objectList.add("to[Member|(from.type==Department || from.type=='Business Unit')].from.id");

            objectList.add("to[Member|(from.type==Department || from.type=='Business Unit')].from.name");

            objectList.add("to[Member|(from.type==Department || from.type=='Business Unit')].from.type");



            Map mpPerson = personDO.getInfo(context, objectList);



            boolean bIsHierarchyPerson = false;

            String strPersonId = (String) mpPerson.get(DomainObject.SELECT_ID);

            String strPersonName = (String) mpPerson.get(DomainObject.SELECT_NAME);



            // Fetch Assignment of user

            String strMQLCommand                =   "print person '"+ strPersonName + "' select assignment dump";

            MQLCommand mcGetAssign              =   new MQLCommand();

            mcGetAssign.open(context);

            mcGetAssign.executeCommand(context,strMQLCommand);

            String strMQLCommandResult          =   mcGetAssign.getResult();

            strMQLCommandResult                 =   strMQLCommandResult.trim();

            mcGetAssign.close(context);

            StringList slUserRoles              =   FrameworkUtil.split(strMQLCommandResult, ",");



            if(mpPerson.containsKey("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.id"))

            {

                String strLeadResponsibility = (String)mpPerson.get("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.type");

                String strBUDeptName = (String)mpPerson.get("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.name");

                String strBUDeptId = (String)mpPerson.get("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.id");

                String strBUDeptLeadRole = (String)mpPerson.get("to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ROLE + "]");

                if(null != strLeadResponsibility && strLeadResponsibility.length() > 0 && DomainConstants.TYPE_BUSINESS_UNIT.equalsIgnoreCase(strLeadResponsibility))

                {

                    bIsHierarchyPerson = true;

                    userMap.put("LeadResponsibility", strLeadResponsibility);

                    userMap.put("ConnectedAsLead", strBUDeptName);



                    StringList slBUDeptLeadRole = FrameworkUtil.split(strBUDeptLeadRole, "~");

                    int nBUDeptLeadRoleListSize = 0;

                    if(null != slBUDeptLeadRole)

                        nBUDeptLeadRoleListSize = slBUDeptLeadRole.size();

                    for(int nBUDeptLeadRoleCount = 0; nBUDeptLeadRoleCount < nBUDeptLeadRoleListSize; nBUDeptLeadRoleCount++)

                    {

                        String strRole = (String)slBUDeptLeadRole.get(nBUDeptLeadRoleCount);

                        if(null != strRole && strRole.length() > 0 && strRole.contains("MSIL"))

                        {

                            strRole = PropertyUtil.getSchemaProperty(strRole);

                            userMap.put("UserRole", strBUDeptLeadRole);

                            break;

                        }

                    }



                    StringList slConnectedDepts = new StringList();

                    DomainObject buDeptDO = DomainObject.newInstance(context, strBUDeptId);

                    // fetch all departments of system

                    MapList connectedDeptList = buDeptDO.getRelatedObjects(context,

                            relPattern.getPattern(),

                            typePattern.getPattern(),

                            objectSelectsList,

                            null,

                            false,

                            true,

                            (short)0,

                            "",

                            "");



                    int nConnectedDeptListSize = 0;

                    if(null != connectedDeptList)

                        nConnectedDeptListSize = connectedDeptList.size();



                    for(int nDeptCount = 0; nDeptCount < nConnectedDeptListSize; nDeptCount++)

                    {

                        Map deptMap = (Map)connectedDeptList.get(nDeptCount);

                        String strType = (String)deptMap.get(DomainConstants.SELECT_TYPE);

                        if(DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strType))

                        {

                            String strDeptName = (String)deptMap.get(DomainConstants.SELECT_NAME);

                            slConnectedDepts.addElement(strDeptName);

                        }

                    }

                    userMap.put("Department", slConnectedDepts);



                }

                else if(null != strLeadResponsibility && strLeadResponsibility.length() > 0 && DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strLeadResponsibility))

                {

                    bIsHierarchyPerson = true;

                    userMap.put("LeadResponsibility", strLeadResponsibility);

                    userMap.put("ConnectedAsLead", strBUDeptName);

                    StringList slBUDeptLeadRole = FrameworkUtil.split(strBUDeptLeadRole, "~");

                    int nBUDeptLeadRoleListSize = 0;

                    if(null != slBUDeptLeadRole)

                        nBUDeptLeadRoleListSize = slBUDeptLeadRole.size();

                    for(int nBUDeptLeadRoleCount = 0; nBUDeptLeadRoleCount < nBUDeptLeadRoleListSize; nBUDeptLeadRoleCount++)

                    {

                        String strRole = (String)slBUDeptLeadRole.get(nBUDeptLeadRoleCount);

                        if(null != strRole && strRole.length() > 0 && strRole.contains("MSIL"))

                        {

                            strRole = PropertyUtil.getSchemaProperty(strRole);

                            userMap.put("UserRole", strRole);

                            break;

                        }

                    }



                    // IF USER IS NPE DPM

                    if(null != strBUDeptName && strBUDeptName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strBUDeptName))

                    {

                        strBUDeptId = (String)(FrameworkUtil.split(MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 select $4 dump $5",DomainConstants.TYPE_BUSINESS_UNIT,PROJECT_MANAGEMENT_DIRECTORATE,DomainConstants.QUERY_WILDCARD,DomainConstants.SELECT_ID,"|"),"|")).get(3);

                        StringList slConnectedDepts = new StringList();

                        DomainObject buDeptDO = DomainObject.newInstance(context, strBUDeptId);

                        // fetch all departments of system

                        MapList connectedDeptList = buDeptDO.getRelatedObjects(context,

                                relPattern.getPattern(),

                                typePattern.getPattern(),

                                objectSelectsList,

                                null,

                                false,

                                true,

                                (short)0,

                                "",

                                "");



                        int nConnectedDeptListSize = 0;

                        if(null != connectedDeptList)

                            nConnectedDeptListSize = connectedDeptList.size();



                        for(int nDeptCount = 0; nDeptCount < nConnectedDeptListSize; nDeptCount++)

                        {

                            Map deptMap = (Map)connectedDeptList.get(nDeptCount);

                            String strType = (String)deptMap.get(DomainConstants.SELECT_TYPE);

                            if(DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strType))

                            {

                                String strDeptName = (String)deptMap.get(DomainConstants.SELECT_NAME);

                                slConnectedDepts.addElement(strDeptName);

                            }

                        }

                        userMap.put("Department", slConnectedDepts);

                    }

                    else

                        userMap.put("Department", new StringList(strBUDeptName));

                }

            }

            else if(mpPerson.containsKey("to[Member].from.type") && DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase((String)mpPerson.get("to[Member].from.type"))) // FOR BELOW DPMs

            {

                if(slUserRoles.contains(DomainConstants.ROLE_PROJECT_LEAD))

                {

                    bIsHierarchyPerson = true;

                    String strDepartmentName = (String)mpPerson.get("to[Member].from.name");



                    userMap.put("LeadResponsibility", DomainConstants.ROLE_PROJECT_LEAD);

                    userMap.put("ConnectedAsLead", "");

                    userMap.put("Department", new StringList(strDepartmentName));

                    userMap.put("UserRole", DomainConstants.ROLE_PROJECT_LEAD);

                }

            }

            if(bIsHierarchyPerson)

            {

                userMap.put("PersonId", strPersonId);

                userMap.put("PersonName", strPersonName);

            }

        }

        catch (Exception ex)

        {

            throw new FrameworkException(ex);

        }

        return userMap;

    }



    /*

     * GET LIST OF PROJECTS FOR THE LOGGED IN USER

     */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getLoggedInPersonProjectList(Context context, Map userMap) throws Exception

    {

    	 MapList projectsList = new MapList();

         try

         {

             // Project selects

             StringList objectSelects = new StringList(6);

             objectSelects.add(DomainConstants.SELECT_ID);

             objectSelects.addElement(DomainConstants.SELECT_NAME);

             objectSelects.addElement(DomainConstants.SELECT_OWNER);

             objectSelects.addElement("to[Project Access List].from.id");

             objectSelects.addElement("attribute[Percent Complete]");

  

             // PREPARE WHERE CLAUSE

             StringBuffer sbWhereClause = new StringBuffer(500);

  

             if(null != userMap && userMap.containsKey("PersonId"))

             {

                 boolean bOwnerWhereClause = false;

                 boolean bProjectDepartmentWhereClause = false;

                 String strPersonId = (String) userMap.get("PersonId");

                 String strPersonName = (String) userMap.get("PersonName");

  

                 // FETCH DESIGNATION OF THE USER

                 String strLeadResponsibility = (String)userMap.get("LeadResponsibility");

                 // FETCH CONNECTED BU & DEPT

                 String strBUDeptName = (String)userMap.get("ConnectedAsLead");

  

                 StringList slDeptNameList = (StringList)userMap.get("Department");

  

                 if(null == strBUDeptName || strBUDeptName.length() <= 0)

                     strBUDeptName = "";

  

                 if(null != strLeadResponsibility && strLeadResponsibility.length() > 0)

                 {

                     // IF-ELSE FOR BU/DIVISION HEAD

                     if(DomainConstants.TYPE_BUSINESS_UNIT.equalsIgnoreCase(strLeadResponsibility) && (null != strBUDeptName && strBUDeptName.length() > 0 && PROJECT_MANAGEMENT_DIRECTORATE.equalsIgnoreCase(strBUDeptName)))

                     {

                         bProjectDepartmentWhereClause = true;

                     }

                     else if(DomainConstants.TYPE_BUSINESS_UNIT.equalsIgnoreCase(strLeadResponsibility) && (null != strBUDeptName && strBUDeptName.length() > 0 && !PROJECT_MANAGEMENT_DIRECTORATE.equalsIgnoreCase(strBUDeptName)))

                     {

                         bProjectDepartmentWhereClause = true;

                     }

                     // IF-ELSE FOR DEPARTMENT HEAD

                     else if(DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strLeadResponsibility) && (null != strBUDeptName && strBUDeptName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strBUDeptName)))

                     {

                         bProjectDepartmentWhereClause = true;

                     }

                     else if(DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strLeadResponsibility) && (null != strBUDeptName && strBUDeptName.length() > 0 && !PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strBUDeptName)))

                     {

                         bProjectDepartmentWhereClause = true;

                     }

                     // IF-ELSE FOR PROJECT LEAD

                     else if(DomainConstants.ROLE_PROJECT_LEAD.equalsIgnoreCase(strLeadResponsibility))

                     {

                         if(null != slDeptNameList && slDeptNameList.size() > 0)

                         {

                             String strDepartmentName = (String)slDeptNameList.get(0);

  

                             if(null != strDepartmentName && strDepartmentName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentName))

                             {

                                 bOwnerWhereClause = true;

                             }

                             else if(null != strDepartmentName && strDepartmentName.length() > 0 && !PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentName))

                             {

                                 bOwnerWhereClause = true;

                             }

                         }

                     }

                 }

  

  sbWhereClause.append("(");

                 if(bProjectDepartmentWhereClause)

                 {

                     if(null != slDeptNameList && slDeptNameList.size() > 0)

                     {

                         int nDeptListSize = slDeptNameList.size();

  

                         //sbWhereClause.append("(");

                         for(int nCount = 0; nCount < nDeptListSize; nCount++)

                         {

                             String strDeptName = (String)slDeptNameList.get(nCount);

                             if(sbWhereClause.length() <= 0)

                             {

                                 sbWhereClause.append("(attribute[");

                                 sbWhereClause.append(ATTRIBUTE_MSIL_PROJECT_DEPARTMENT);

                                 sbWhereClause.append("]");

                                 sbWhereClause.append("==");

                                 sbWhereClause.append("'");

                                 sbWhereClause.append(strDeptName);

                                 sbWhereClause.append("')");

                             }

                             else

                             {

                                 sbWhereClause.append("(attribute[");

                                 sbWhereClause.append(ATTRIBUTE_MSIL_PROJECT_DEPARTMENT);

                                 sbWhereClause.append("]");

                                 sbWhereClause.append("==");

                                 sbWhereClause.append("'");

                                 sbWhereClause.append(strDeptName);

                                 sbWhereClause.append("')");

                                 //if(nCount == nDeptListSize-1)

                                //     sbWhereClause.append(" || ");

                             }

                            // sbWhereClause.append(") && ");

							if(nCount != nDeptListSize-1)

								sbWhereClause.append(" || ");

                         }

                     }                   

                 }

                 else if(bOwnerWhereClause)

                 {

                     if(sbWhereClause.length() <= 0)

                     {

                         sbWhereClause.append("(owner");

                         sbWhereClause.append("==");

                         //START : Graphical Dashboard

                         sbWhereClause.append("'");

                         sbWhereClause.append(strPersonName);

                         sbWhereClause.append("'");

                         //END : Graphical Dashboard

                         //sbWhereClause.append(")");

                     }

                     else

                     {

                         sbWhereClause.append("(owner");

                         sbWhereClause.append("==");

                         //START : Graphical Dashboard

                         sbWhereClause.append("'");

                         sbWhereClause.append(strPersonName);

                         sbWhereClause.append("'");

                         //END : Graphical Dashboard

                         //sbWhereClause.append(")");

                     }

                     //sbWhereClause.append(") && ");

					 sbWhereClause.append(")");

                 }

                 //sbWhereClause.append(") && current==Active");

				 // Commented and Modified by Ajit -- 27/06/2018 -- To Show All projects as part of  Graphical Dashboard except Complete or Hold or Cancel Projects -- Start
                                                                
                 //sbWhereClause.append(") && current==Active");
                 sbWhereClause.append(") && !(current==Complete || current==Hold || current==Cancel)");

                 // Commented and Modified by Ajit -- 27/06/2018 -- To Show All projects as part of  Graphical Dashboard except Complete or Hold or Cancel Projects -- End


                 System.out.println("sbWhereClause................."+sbWhereClause);

                 StringList busSelects =  new StringList(7);

                 busSelects.addElement(projectSpace.SELECT_ID);

                 busSelects.addElement(projectSpace.SELECT_NAME);

                 // Graphical Dashboard , Add Project Department selectable

                 busSelects.addElement("attribute["+ATTRIBUTE_MSIL_PROJECT_DEPARTMENT+"]");

                 busSelects.addElement(projectSpace.SELECT_TYPE);

                 busSelects.addElement(projectSpace.SELECT_CURRENT);

                 busSelects.addElement(projectSpace.SELECT_OWNER);

                 busSelects.addElement("attribute[Percent Complete]");

                 projectsList = DomainObject.findObjects(context,

                         DomainConstants.TYPE_PROJECT_SPACE,

                         VAULT_ESERVICE_PRODUCTION,

                         sbWhereClause.toString(),

                         busSelects);	

                 

             }

 			

         }

         catch (Exception ex)

         {

             throw ex;

         }

         finally

         {

             return projectsList;

         }

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public Map getMasterProjectsList(Context context, MapList mlProjectsList, Map userMap) throws Exception

    {

        MapList mlMasterProjectsList = new MapList();

        Map masterProjectMap = new HashMap();

        try

        {



            boolean bIsNPEUser = false;



            if(null != userMap && userMap.containsKey("PersonId"))

            {

                // FETCH DESIGNATION OF THE USER

                String strLeadResponsibility = (String)userMap.get("LeadResponsibility");

                StringList slDeptNameList = (StringList)userMap.get("Department");



                if(null != strLeadResponsibility && strLeadResponsibility.length() > 0 && DomainConstants.ROLE_PROJECT_LEAD.equalsIgnoreCase(strLeadResponsibility))

                {

                    if(null != slDeptNameList && slDeptNameList.size() == 1)

                    {

                        String strDepartmentName = (String)slDeptNameList.get(0);



                        if(null != strDepartmentName && strDepartmentName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentName))

                        {                                

                            bIsNPEUser = true;                                

                        }

                    }

                }

            }                



            if(!bIsNPEUser)

            {

                if(null != mlProjectsList && mlProjectsList.size() > 0)

                {

                    int nProjectsCount = mlProjectsList.size();

                    for(int nProjectCount = 0; nProjectCount < nProjectsCount; nProjectCount++)

                    {

                        String strMasterProjectId = "";

                        String strMasterProjectType = "";

                        String strMasterProjectName = "";

                        String strMasterProjectPercentComplete = "";



                        Map mpProjectMap = (Map)mlProjectsList.get(nProjectCount);

                        String strProjectId = (String)mpProjectMap.get(DomainConstants.SELECT_ID);



                        if(null != strProjectId && !"null".equalsIgnoreCase(strProjectId) && strProjectId.length() > 0)

                        {

                            // FETCH MASTER PROJECT FROM PROJECT ID

                            MQLCommand mql = new MQLCommand();

                            String sMql = "expand bus " + strProjectId + " to rel 'Subtask' recurse to end select bus id type name attribute[Percent Complete] dump |";

                            boolean bResult = mql.executeCommand(context, sMql);

                            if(bResult)

                            {

                                String sResult = mql.getResult().trim();

                                if(sResult!=null && !"".equals(sResult))

                                {

                                    StringTokenizer sResultTkz = new StringTokenizer(sResult,"|");

                                    sResultTkz.nextToken(); // level

                                    sResultTkz.nextToken(); // Relationship name

                                    sResultTkz.nextToken(); // to/from side

                                    sResultTkz.nextToken(); // Object Type

                                    sResultTkz.nextToken(); // Object Name

                                    sResultTkz.nextToken(); // Object Rev

                                    strMasterProjectId = (String)sResultTkz.nextToken(); // Object Id

                                    strMasterProjectType = (String)sResultTkz.nextToken(); // Object Type

                                    strMasterProjectName = (String)sResultTkz.nextToken(); // Object Name

                                    strMasterProjectPercentComplete = (String)sResultTkz.nextToken(); // attribute[Percent Complete]



                                    if((null != strMasterProjectId && strMasterProjectId.length() > 0 && !"null".equalsIgnoreCase(strMasterProjectId))

                                            && (null != strMasterProjectType && strMasterProjectType.length() > 0 && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strMasterProjectType)))

                                    {

                                        // KEEP ONLY THOSE DEPARTMENT SCHEDULES THAT ARE CONNECTED TO APPROVED MASTER PROJECT

                                        mpProjectMap.put("masterProjectName", strMasterProjectName);

                                        mpProjectMap.put("masterProjectPercentComplete", strMasterProjectPercentComplete);



                                        if(masterProjectMap.containsKey(strMasterProjectId))

                                        {

                                            MapList tempList = (MapList)masterProjectMap.get(strMasterProjectId);

                                            tempList.add(mpProjectMap);

                                            masterProjectMap.put(strMasterProjectId, tempList);

                                        }

                                        else

                                        {

                                            MapList tempList = new MapList();

                                            tempList.add(mpProjectMap);

                                            masterProjectMap.put(strMasterProjectId, tempList);

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

            }

            else if(bIsNPEUser)

            {

                Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);



                Pattern typePattern = new Pattern(DomainConstants.TYPE_PROJECT_SPACE);

                typePattern.addPattern(DomainConstants.TYPE_TASK_MANAGEMENT);



                StringList busSelects =  new StringList(7);

                busSelects.addElement(projectSpace.SELECT_ID);

                busSelects.addElement(projectSpace.SELECT_NAME);

                busSelects.addElement("attribute["+ATTRIBUTE_MSIL_PROJECT_DEPARTMENT+"]");

                busSelects.addElement(projectSpace.SELECT_TYPE);

                busSelects.addElement(projectSpace.SELECT_CURRENT);

                busSelects.addElement(projectSpace.SELECT_OWNER);

                busSelects.addElement("attribute[Percent Complete]");



                StringList slRelSelect = new StringList();

                

                String strBusWhere = "";

                String strRelWhere = "";



                Pattern includeType = new Pattern(DomainConstants.TYPE_PROJECT_SPACE);

                Pattern includeRelationship = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);

                

                Map includeMap = new HashMap();

                

                if(null != mlProjectsList && mlProjectsList.size() > 0)

                {

                    int nProjectsCount = mlProjectsList.size();

                    for(int nProjectCount = 0; nProjectCount < nProjectsCount; nProjectCount++)

                    {

                        Map mpMasterProjectMap = (Map)mlProjectsList.get(nProjectCount);

                        String strProjectId = (String)mpMasterProjectMap.get(DomainConstants.SELECT_ID);



                        if(null != strProjectId && !"null".equalsIgnoreCase(strProjectId) && strProjectId.length() > 0)

                        {

                            DomainObject masterProjectDO = DomainObject.newInstance(context, strProjectId);

                            

                            Map mpMasterProjectDetailMap = masterProjectDO.getInfo(context, busSelects);

                            

                            MapList mlDeptProjectList = masterProjectDO.getRelatedObjects(context,

                                    relPattern.getPattern(),

                                    typePattern.getPattern(),

                                    busSelects,

                                    null, 

                                    false,

                                    true,

                                    (short)0,

                                    strBusWhere,

                                    strRelWhere,

                                    includeType, 

                                    includeRelationship, 

                                    includeMap);

                            

                            if(null != mlDeptProjectList && mlDeptProjectList.size() > 0)

                            {

                                int nListSize = mlDeptProjectList.size();

                                for(int nCount = 0; nCount < nListSize; nCount++)

                                {

                                    Map mpProjectMap = (Map)mlDeptProjectList.get(nCount);



                                    // KEEP ONLY THOSE DEPARTMENT SCHEDULES THAT ARE CONNECTED TO APPROVED MASTER PROJECT

                                    mpProjectMap.put("masterProjectName", (String)mpMasterProjectDetailMap.get(projectSpace.SELECT_NAME));

                                    mpProjectMap.put("masterProjectPercentComplete", (String)mpMasterProjectDetailMap.get("attribute[Percent Complete]"));

                        

                                    if(masterProjectMap.containsKey(strProjectId))

                                    {

                                        MapList tempList = (MapList)masterProjectMap.get(strProjectId);

                                        tempList.add(mpProjectMap);

                                        masterProjectMap.put(strProjectId, tempList);

                                    }

                                    else

                                    {

                                        MapList tempList = new MapList();

                                        tempList.add(mpProjectMap);

                                        masterProjectMap.put(strProjectId, tempList);

                                    }

                                }

                            }                            

                        }                        

                    }

                }

            }

        }

        catch (Exception ex)

        {

            throw ex;

        }

        finally

        {

            return masterProjectMap;

        }

    }

    

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public Map getMasterProjectsMap(Context context, String[] args) throws Exception

    {

        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

        // ${CLASS:MSILGraphicalDashboard} dashboardJpoObj = new ${CLASS:MSILGraphicalDashboard}(context, args);

        // ${CLASS:MSILGraphicalProjectDashboard} projectDashboardJpoObj = new ${CLASS:MSILGraphicalProjectDashboard}(context, args);

        String strContextUser = context.getUser().trim();

        String strContextUserId = person.getPerson(context, strContextUser).getId();

        DomainObject personDO = DomainObject.newInstance(context, strContextUserId);



        // GET LIST OF DEPARTMENTS/BUs CONNECTED TO LOGGED IN PERSON

       // Map userMap = dashboardJpoObj.getUserList(context, personDO);

        

        Map userMap = getUserList(context, personDO);

        

        



        // LIST OF PROJECTS THAT ARE ACCESSIBLE TO LOGGED IN PERSON

       // MapList mlProjectsList = dashboardJpoObj.getLoggedInPersonProjectList(context, userMap);

        

         MapList mlProjectsList = getLoggedInPersonProjectList(context, userMap);

        



        // KEEP ONLY THOSE DEPARTMENT SCHEDULES THAT ARE CONNECTED TO APPROVED MASTER PROJECT

        //Map masterProjectsMap = dashboardJpoObj.getMasterProjectsList(context, mlProjectsList, userMap);

        

        Map masterProjectsMap = getMasterProjectsList(context, mlProjectsList, userMap);



        return masterProjectsMap;

    }



    

    ///////// Start Nikesh Code For Grapical DashBoard 

    @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getPEPersonData(Context context , String [] args)

    {

        MapList mlReturnList         =     new MapList();

        try {

                String objectId        = "DPM";

                DomainObject dObj     = new DomainObject(objectId);

                MapList mlPEPersonList          = getPEPerson (context , args);



                MapList mlPRGList      = getProductData(context , "DPM" ,  mlPEPersonList);

                MapList mlPGEList      = getProductData(context , "DPJ" , mlPEPersonList );

                mlReturnList           = margePRGndPGEMaps(mlPRGList , mlPGEList );

        } catch (Exception e) {

            e.printStackTrace();

        }



        return mlReturnList;

    }

    

    

    public MapList margePRGndPGEMaps(MapList mlPRGList ,  MapList mlPGEList )

    {

    	MapList mlreturnList 	= new MapList();
    	MapList mlSortedReturnList 	= new MapList();

    	try {

    		Map tempPRG;

    		

    		String strDeptPRGName	    = "";

    		Integer strDeptPRGPercent ;

    		

    		Map tempPGE;

    		String strDeptPGEName       = "";

    		Integer strDeptPGEPercent;

    		

    		for (int i = 0; i < mlPRGList.size() ; i++) {

    			tempPRG              = (Map) mlPRGList.get(i);

    			HashMap mapAddingMap = new HashMap();

    			strDeptPRGName       = (String) tempPRG.get("name");

    			strDeptPRGPercent    = (Integer) tempPRG.get("Percent");

    			mapAddingMap.put("name", strDeptPRGName);

    			mapAddingMap.put("DPM", strDeptPRGPercent);

    			mapAddingMap.put("PRGUsers", mergeUserAssignConsumedInfo(tempPRG));

    			for (int j = 0; j < mlPGEList.size(); j++) {

    				tempPGE	=	(Map) mlPGEList.get(j);

    				strDeptPGEName	=	(String) tempPGE.get("name");

    				strDeptPGEPercent	=	(Integer) tempPGE.get("Percent");

    				if(strDeptPGEName.equals(strDeptPRGName))

    				{

    					mapAddingMap.put("PGE", strDeptPGEPercent);

    					mapAddingMap.put("PGEUsers", mergeUserAssignConsumedInfo(tempPGE));

    				}

				}

    			

    			mlreturnList.add(mapAddingMap);

    			

			}
    		//Added and Modified by Dheeraj Garg <05-Sep-2016> Order of the dept list should be in particular order - License Graph - Start
    		StringList slDeptInfo = new StringList(SORTED_ARRAY_DEPARTMENT_NAMES);
    		slDeptInfo.add("Total");
    		for(Object objDeptName :slDeptInfo)
    		for(Object objUsersInfo : mlreturnList){
    	        String strDeptName = (String)((Map)objUsersInfo).get("name");
    	        if(strDeptName.equals(objDeptName)){
    	        	mlSortedReturnList.add(objUsersInfo);
    	        	break;
    	        }
    		}
    		// Added and Modified by Dheeraj Garg <05-Sep-2016> Order of the dept list should be in particular order - License Graph - End

		} catch (Exception e) {

			e.printStackTrace();

		}

    	

		return mlSortedReturnList ;

    }

    

    public HashMap mergeUserAssignConsumedInfo(Map tempPRG)

    {

        HashMap hmUserLicense      = new HashMap<String, String>();

        StringList slAssignedUsers = (StringList)tempPRG.get("Assign");

        StringList slConsumedUsers = (StringList)tempPRG.get("Consumed");

        Set<String> setUsers       = new HashSet<String>();

        if(null != slAssignedUsers && slAssignedUsers.size() > 0) setUsers.addAll(slAssignedUsers);

        if(null != slConsumedUsers && slConsumedUsers.size() > 0) setUsers.addAll(slConsumedUsers);

        Iterator<String> itrUser = setUsers.iterator();

        while(itrUser.hasNext()){

            String strUser   = itrUser.next();

            String strStatus = null != slAssignedUsers && slAssignedUsers.contains(strUser) ? "Y" : "N";

            strStatus = null != slConsumedUsers && slConsumedUsers.contains(strUser) ? strStatus + "|Y" : strStatus + "|N";

            hmUserLicense.put(strUser, strStatus);

        }

        return hmUserLicense;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList  getUserLicenseData(Context context , String [] args)throws Exception

    {

        MapList mlReturnList          = new MapList();

        try {

            Map programMap            = (Map) JPO.unpackArgs(args);

            String strLicenseData     = (String) programMap.get("UserLicenseInfo");

            strLicenseData            = strLicenseData.substring(1, strLicenseData.length() - 1);

            for(String strUserLicenseInfo:strLicenseData.split(","))

            {

                Map mpUserLicenseData = new HashMap();

                String strUserArray[] = strUserLicenseInfo.split("=");

                String arrLicense[]   = strUserArray[1].split("\\|");

                /*MapList mlUserData    = DomainObject.findObjects(context, "Person", "*", "name=="+strUserArray[0], new StringList(DomainConstants.SELECT_ID));

                if(null != mlUserData && mlUserData.size()>0){

                    String strUserID  = (String) ((Map)mlUserData.get(0)).get(DomainConstants.SELECT_ID);

                    String strUserName= strUserArray[0];

                    strUserArray[0]   = "<a href ='javascript:showModalDialog(\"../common/emxTree.jsp?objectId="+strUserID

                                        +"\", \"875\", \"550\", \"false\", \"popup\")'>"+strUserArray[0]+"</a>";

                    mpUserLicenseData.put("UserNameExport", strUserName);

                }*/

                // Modifed by Dheeraj Garg <10-Aug-2016> Add name of user instead of id. -- Start
                mpUserLicenseData.put("UserName", strUserArray[0].trim());
                // Modifed by Dheeraj Garg <10-Aug-2016> Add name of user instead of id. -- End

                mpUserLicenseData.put("Assigned", "Y".equals(arrLicense[0]) ? "Yes": "No");

                mpUserLicenseData.put("Consumed", "Y".equals(arrLicense[1]) ? "Yes": "No");

                mlReturnList.add(mpUserLicenseData);

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

        return mlReturnList;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getProductData(Context context , String objectId , MapList mlPEPersonList) throws Exception

    {

        MapList mlReturnList	=	 new MapList();

        try {

            String strLang         = context.getSession().getLanguage();

            String  mqlCommand     = "print product "+objectId+" select person dump |";

            StringList slAllPEPersonList = getMLToSL(mlPEPersonList);

            HashMap<String , String> mapPersonTODept    =     getPersonAndDepartmentValue(mlPEPersonList);

            if(objectId.indexOf(".")== -1 )

            {

                StringList slConsumedList = getConsumedUserListForPE(context,objectId,slAllPEPersonList);

                String title = getTitleValue(context,objectId,strLang);

                //Added to check whether the product is defined in Kernel or not

                if(!"".equals(title))

                {

                    String strMQL =  MqlUtil.mqlCommand(context,mqlCommand);

                    StringList strlAllAssignedPersons= FrameworkUtil.split(strMQL,"|");

                    StringList slPEAssignList        = getAllAssignList ( strlAllAssignedPersons  ,  slAllPEPersonList) ;

                   

                    

                    StringList slFinalAssignList     =     new StringList();

                    for (int i = 0; i < slConsumedList.size(); i++) {

                        if(! slPEAssignList.contains((String) slConsumedList.get(i))){

                            slFinalAssignList.add((String) slConsumedList.get(i));

                        }

                    }

                    slFinalAssignList.addAll(slPEAssignList);



                    Set<String> setlConsumedList     =     new HashSet<String>(slConsumedList);

                    slConsumedList.clear();

                    slConsumedList.addAll(setlConsumedList);

                    HashMap mapAssignDeptMap         =  getAssignMap(slFinalAssignList , mapPersonTODept );

                    HashMap mapConsumedList          =  getAssignMap(slConsumedList , mapPersonTODept );

                    mlReturnList                     =  margeMap(mlPEPersonList ,  mapConsumedList ,  mapAssignDeptMap);

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    private String getTitleValue(Context context, String name, String language)throws Exception

    {

        String key = "emxFramework."+name+".Title";

        String value = UINavigatorUtil.getI18nString(key, "emxFrameworkStringResource", language);

        String title = "";

        //Added to check whether the product is add-on product or not

        try{

            title = MqlUtil.mqlCommand(context,"print product '"+name+"' select title dump |");

        }

        catch(Exception e)

        {

            value = "";

            return value;

        }

        if(key.equals(value))

        {

            if(!"".equals(title))

                value = title;

            else

                value = name;

        }

        return value;

    }

    

    public MapList margeMap(MapList mlPEPersonList , HashMap mapConsumedList , Map  mapAssignDeptMap) throws Exception

    {

        MapList mlReturnList         = new MapList(); 

        try {

        	HashMap<String, String> hmTotalValue = new HashMap<String, String>();

        	hmTotalValue.put(DomainConstants.SELECT_NAME, "Total");

        	mlPEPersonList.add(hmTotalValue);

            int imlPEPersonListSize    = mlPEPersonList.size();

            Map programMap             = null;

            String strDeptName         = "";

            Integer  iAssignValue      = 0;

            Integer  iConsumedValue    = 0;

            StringList slAssignedUsers = null;

            StringList slConsumedUsers = null;

            int iPercent               = 0;

            for (int i = 0; i < imlPEPersonListSize; i++) {

                programMap      = (Map) mlPEPersonList.get(i);

                strDeptName     = (String ) programMap.get(DomainConstants.SELECT_NAME);

            	slAssignedUsers = (StringList) mapAssignDeptMap.get(strDeptName);

            	slConsumedUsers = (StringList) mapConsumedList.get(strDeptName);

            	iAssignValue    = slAssignedUsers != null ? slAssignedUsers.size() : 0;

            	iConsumedValue  = slConsumedUsers != null ? slConsumedUsers.size() : 0;

                 if(iAssignValue > 0)

                 {

	                 HashMap valueMap   = new HashMap();

	                 iPercent           = iConsumedValue * 100 / iAssignValue;

	                 

	                 valueMap.put("Assign"   , slAssignedUsers);

	                 valueMap.put("Consumed" , slConsumedUsers);

	                 valueMap.put("Percent"  , iPercent);

	                 valueMap.put("name"     , strDeptName);

	                 HashMap datamap     = new HashMap ();

	                 mlReturnList.add(valueMap);

                 }

             }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public HashMap<String , StringList>  getAssignMap(StringList slPEAssignList , HashMap mapPersonTODept  )throws Exception

    {

        HashMap<String , StringList > returnMap     =    new HashMap<String , StringList>();

        try {

            int slPEAssignListSize  = slPEAssignList.size();

            String strPersonName    = "";

            String strDeptName      = "";

            StringList slPersonList = null;

            StringList slAllPersonList = new StringList();

            int iDepartmentCount    = 0;

            for (int i = 0; i < slPEAssignListSize ; i++) {

                strPersonName       = (String ) slPEAssignList.get(i);

                strDeptName         = (String) mapPersonTODept.get(strPersonName);

                slAllPersonList.add(strPersonName);

                if(returnMap.containsKey(strDeptName))

                {

                	slPersonList    = returnMap.get(strDeptName);

                	slPersonList.add(strPersonName);

                    returnMap.put(strDeptName, slPersonList);

                }

                else

                {

                	slPersonList = new StringList();

                	slPersonList.add(strPersonName);

                    returnMap.put(strDeptName, slPersonList);

                }

            }

            returnMap.put("Total", slAllPersonList);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap ;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public StringList  getAllAssignList (StringList strlAllAssignedPersons , StringList slAllPEPersonList ) throws Exception

    {

        StringList slReturnList     =     new StringList();

        try {

            

                String strParsonName     =     "";

                

                for (int i = 0; i < strlAllAssignedPersons.size(); i++) {

                    strParsonName    =    (String) strlAllAssignedPersons.get(i);

                    if(slAllPEPersonList.contains(strParsonName))

                        slReturnList.add(strParsonName);

                }

            

        } catch (Exception e) {

            e.printStackTrace();

        }

        return slReturnList;

    }

    

    

 

        @com.matrixone.apps.framework.ui.ProgramCallable

    public StringList getConsumedUserListForPE(Context context, String prodId, StringList slAllPEPersonList) throws Exception

        {

            List consumedList = LicenseUtil.getLicenseUsage(context,prodId,null);

            StringList strlist = new StringList();

            for(int i=0; i<consumedList.size(); i++)

            {

                HashMap hm = (HashMap) consumedList.get(i);

                if(slAllPEPersonList.contains( (String)hm.get(LicenseUtil.USAGE_USER_NAME)))

                    strlist.add((String)hm.get(LicenseUtil.USAGE_USER_NAME));

            }

            return strlist;

        }

    

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public StringList getMLToSL(MapList mlPEPersonList) throws Exception

    {

        StringList slReturnList     =     new StringList();

        try {

            for (int i = 0; i < mlPEPersonList.size(); i++) 

            {

                Object objPerson = ((Map)mlPEPersonList.get(i)).get("from[Member].to.name");

                if(objPerson instanceof StringList)

                    slReturnList.addAll((StringList)objPerson);

                else if(objPerson instanceof String)

                    slReturnList.add((String)objPerson);

            }

            

        } catch (Exception e) {

            e.printStackTrace();

        }

        return slReturnList;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public HashMap<String , String>  getPersonAndDepartmentValue(MapList mlPEPersonList) throws Exception

    {

        HashMap<String , String> returnMap      = new HashMap<String , String>(); 

        try {

            Map  mapData            =    null;

            String strDepartmentName    = "";

            StringList slPersonList    =     new StringList();

            for (int i = 0; i < mlPEPersonList.size(); i++) 

            {

                mapData    =    (Map) mlPEPersonList.get(i);

                strDepartmentName    =    (String) mapData.get(DomainConstants.SELECT_NAME);

                Object objPerson = mapData.get("from[Member].to.name");

                if(objPerson instanceof StringList)

                {

                    slPersonList        =    (StringList) objPerson;

                    for (int j = 0; j < slPersonList.size(); j++) {

                        returnMap.put((String) slPersonList.get(j), strDepartmentName );

                    }

                }

                else if(objPerson instanceof String)

                {

                    returnMap.put((String) objPerson, strDepartmentName );

                }

            }

            

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getPEPerson (Context context , String [] args) throws Exception

    {

        MapList mlReturnList     =     new MapList();

        try {

                DomainConstants.MULTI_VALUE_LIST.add("from[Member].to.name");

                DomainObject domPEUnit     =    DomainObject.newInstance(context , new BusinessObject(DomainConstants.TYPE_BUSINESS_UNIT, PROJECT_MANAGEMENT_DIRECTORATE, "-" , VAULT_ESERVICE_PRODUCTION));

                StringList objectSelects = new StringList();

                objectSelects.add(DomainConstants.SELECT_ID);

                objectSelects.add(DomainConstants.SELECT_TYPE);

                objectSelects.add(DomainConstants.SELECT_NAME);

                objectSelects.add("from[Member|to.current=='Active'].to.name");

            

            

                StringList relSelects = new StringList();

                mlReturnList = domPEUnit.getRelatedObjects(context,

                                         DomainConstants.RELATIONSHIP_DIVISION +","+ DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT ,

                                        DomainConstants.TYPE_DEPARTMENT +","+ DomainConstants.TYPE_BUSINESS_UNIT ,

                                        false,

                                        true,

                                        (short) 0,

                                        objectSelects,

                                        relSelects,

                                        null,

                                        null,

                                        null,

                                        DomainConstants.TYPE_DEPARTMENT,

                                        null);

 



        } catch (Exception e) {

            e.printStackTrace();

        }

        finally {

            DomainConstants.MULTI_VALUE_LIST.remove("from[Member].to.name");



        }

        return mlReturnList;

    }

	

     @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getMasterProjectTasks(Context context,String strProjectId)

    {

    	MapList mlReturnList = new MapList();

    	try

    	{

             

    		String strOwner                = context.getUser();

    		DomainObject doObject = new DomainObject(strProjectId);

    		Pattern typePattern = new Pattern(

					DomainConstants.TYPE_PROJECT_MANAGEMENT);

			Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);

			relPattern.addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);

			relPattern.addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);

			Pattern includeType = new Pattern(ProgramCentralConstants.TYPE_PROJECT_SPACE);

			Pattern includeRelationship = new Pattern(

					DomainConstants.RELATIONSHIP_SUBTASK);

			Map includeMap = new HashMap();

			boolean getFrom = true;

			boolean getTo = false;

			short recurseToLevel = 0;

			//String strBusWhere = "attribute["+ATTRIBUTE_MSIL_TASK_TYPE+"]=='"+ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE+"'";

			String strBusWhere = "";

			String strRelWhere = "";

										

			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

			 StringList slObjectSelect = new StringList(7);

	            slObjectSelect.add(DomainConstants.SELECT_NAME);

	            slObjectSelect.add(DomainConstants.SELECT_ID);

	            slObjectSelect.add(DomainConstants.SELECT_TYPE);

	            slObjectSelect.add(DomainConstants.SELECT_CURRENT);

	            slObjectSelect.add(DomainConstants.SELECT_OWNER);



	            StringList slRelSelect = new StringList();

	            

			MapList mlProjectList = doObject

					.getRelatedObjects(context,

							relPattern.getPattern(),

							typePattern.getPattern(), slObjectSelect,

							null, getTo, getFrom, recurseToLevel,

							strBusWhere, strRelWhere, includeType,

							includeRelationship, includeMap);

			int iProjectSize = mlProjectList.size();

			String strDeptProjectId = "";

			String strProjectCurrent = "";

			MapList mlDeptProjectTaskList = new MapList();

		

			for(int iProjectCount=0 ; iProjectCount < iProjectSize ; iProjectCount++)

			{

				Map mpProjectMap = (Map) mlProjectList.get(iProjectCount);

				strProjectCurrent = (String) mpProjectMap.get(DomainConstants.SELECT_CURRENT);

				if(!strProjectCurrent.equalsIgnoreCase(STATE_ACTIVE_POLICY_PROJECT_SPACE))

				{

					strDeptProjectId = (String) mpProjectMap.get(DomainConstants.SELECT_ID);

					ProjectSpace project = new ProjectSpace(strDeptProjectId);

					MapList projectTaskList = task.getTasks(context, project, 0 , slObjectSelect, slRelSelect);

					mlReturnList.addAll(projectTaskList);

				}			

				

			}			

    	}

    	catch (Exception e) {

            e.printStackTrace();

        }

    	return mlReturnList;

    }

    

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public Map getProjectTaskXML (Context context , String strProjectId,emxTask_mxJPO emxtask)throws Exception

    {

        Map returnMap                   = new HashMap();

        ArrayList alAssignList          = new ArrayList();

        String strDuration              = DomainConstants.EMPTY_STRING;

        String sAttrbPrcComplete        = DomainConstants.EMPTY_STRING;

        String sEstStartDate            = DomainConstants.EMPTY_STRING;

        String sEstEndDate              = DomainConstants.EMPTY_STRING;

        String strDependentTasksDetail  = DomainConstants.EMPTY_STRING;

        String strTaskName              = DomainConstants.EMPTY_STRING;

        String strTaskId                = DomainConstants.EMPTY_STRING;

        String sAttribEstStartDate      = DomainConstants.EMPTY_STRING;

        String sAttribEstEndDate        = DomainConstants.EMPTY_STRING;

        String strParentSubtaskId       = DomainConstants.EMPTY_STRING;

        String strDependentTasksId      = DomainConstants.EMPTY_STRING;

        String strTaskType              = DomainConstants.EMPTY_STRING;

        String strDependentTaskId       = DomainConstants.EMPTY_STRING;

        String strDependencyType        = DomainConstants.EMPTY_STRING;

        String strAssignName            = DomainConstants.EMPTY_STRING;

        String strLastCode              = DomainConstants.EMPTY_STRING;

        String strActualStartDate       = DomainConstants.EMPTY_STRING;

        String strActualEndDate         = DomainConstants.EMPTY_STRING;

        String strLevel                 = DomainConstants.EMPTY_STRING;

        String strParentCode            = DomainConstants.EMPTY_STRING;

        String strProjectWBSCode        = DomainConstants.EMPTY_STRING;

        String strDependentNumbers      = DomainConstants.EMPTY_STRING;

        String strDependentChild        = DomainConstants.EMPTY_STRING;

        String strTitle                 = DomainConstants.EMPTY_STRING;

        String isTaskLeafLevel          = DomainConstants.EMPTY_STRING;

        String strReasonfordelay        = DomainConstants.EMPTY_STRING;

        String strActionPlanforcatchup  = DomainConstants.EMPTY_STRING;

        String strComments              = DomainConstants.EMPTY_STRING;

        String strIcon                  = DomainConstants.EMPTY_STRING;

        String strGANTTGanttIcons       = DomainConstants.EMPTY_STRING;

        String strGANTTGanttClass       = DomainConstants.EMPTY_STRING;

        String strBGFormatcolor         = DomainConstants.EMPTY_STRING;

        String strUserName              = DomainConstants.EMPTY_STRING;

        String strActDuretion           = DomainConstants.EMPTY_STRING;

        String strTaskOwner             = DomainConstants.EMPTY_STRING;

        String strDescription           = DomainConstants.EMPTY_STRING;

        String strMilestoneName         = DomainConstants.EMPTY_STRING;

        String strParentProjectNames    = DomainConstants.EMPTY_STRING;

        String strMSILTaskType          = DomainConstants.EMPTY_STRING;

        String strGateStatus            = DomainConstants.EMPTY_STRING;

        String strMSILCriticalTask      = DomainConstants.EMPTY_STRING;



        HashMap mapParentMap                 = new HashMap();

        Map projectTaskMap                   = new HashMap();

        StringList dependentTaskIdsList      = new StringList();

        StringList dependentTypeList         = new StringList();

        StringList slPersonList              = new StringList();

        StringList slDeliverableList         = new StringList();

        StringList slDeliverableListids      = new StringList();

        StringList slDeliverableListTitle    = new StringList();

        HashMap<String , String> mpPALTODept = new HashMap<String , String>();

        HashMap<String , String> mpPALTOName = new HashMap<String , String>();

        HashMap<String , String> mpPALTOCurrent = new HashMap<String , String>();

        

        Date sJavaDate                  = null;

        Map mapDepdency                 = null;

		Map mpStatusMap					= null;

        int nDependentTaskListSize      = 0;

        try

        {

            DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].from.name");

            DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.name");

            DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.id");

            DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.attribute[Title].value");



            //HashMap programMap       = (HashMap) JPO.unpackArgs(args);

            //String strProjectId      = (String)programMap.get("objectId");

            //String strFileName       = (String)programMap.get("FileName"); 

            //String strWorkSpace      = (String)programMap.get("workSpace"); 

            String strFileName         = "Project"+strProjectId.replaceAll("\\.", "_")+".xml"; 

            //String strPathMQL        = MqlUtil.mqlCommand(context,"print person '"+context.getUser()+"' select property[Dashboard_Path]  dump |");

            //strPathMQL               = strPathMQL.substring(strPathMQL.indexOf("value") +6 ,strPathMQL.length() );

            //strPathMQL               = strPathMQL.trim();



            //String strWorkSpace        = "E:\\WorkSpace\\Nikesh";

	   		//String strWorkSpace        = strPathMQL;

            String strWorkSpace        = context.createWorkspace();;



            String strProjectDepartment       = DomainConstants.EMPTY_STRING;

            String strCriticalTask           = DomainConstants.EMPTY_STRING;

            String strCurrent               = DomainConstants.EMPTY_STRING;

            String strOwner                = context.getUser();

            String strInboxTask               = DomainConstants.EMPTY_STRING;



            

            com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

            StringList slObjectSelect = new StringList(7);

            slObjectSelect.add(DomainConstants.SELECT_NAME);

            slObjectSelect.add(DomainConstants.SELECT_ID);

            slObjectSelect.add(DomainConstants.SELECT_TYPE);

            slObjectSelect.add(DomainConstants.SELECT_DESCRIPTION);

            slObjectSelect.add(DomainConstants.SELECT_CURRENT);

            slObjectSelect.add(DomainConstants.SELECT_OWNER);

            slObjectSelect.add(DomainConstants.SELECT_LEVEL);

            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");

            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");

            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");

            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]");

            slObjectSelect.add("to[Project Access Key].from.id");

            slObjectSelect.add("to[Project Access List].from.id");

            

            slObjectSelect.add("attribute[MSIL Task Type].value");

            slObjectSelect.add(task.SELECT_TASK_ACTUAL_START_DATE); 

            slObjectSelect.add(task.SELECT_TASK_ACTUAL_FINISH_DATE);

            slObjectSelect.add(task.SELECT_TASK_ACTUAL_DURATION); // 

            slObjectSelect.add("to[Subtask].from.id"); // fetch parent of task

            slObjectSelect.add("from[Subtask]"); // child Task

            slObjectSelect.add("to[Assigned Tasks].from.name"); // 

            slObjectSelect.add("attribute[MSILReasonfordelay].value");

            slObjectSelect.add("attribute[MSILActionPlanforcatchup].value");

            slObjectSelect.add("attribute[Comments].value");

            slObjectSelect.add("attribute[MSILProjectDepartment].value");

            slObjectSelect.add("attribute[MSIL Gate Status].value");

            slObjectSelect.add("attribute[MSIL Critical Task].value");

            slObjectSelect.add(task.SELECT_CRITICAL_TASK);

            slObjectSelect.add("from[Task Deliverable].to.name");

            slObjectSelect.add("from[Task Deliverable].to.id");

            slObjectSelect.add("from[Task Deliverable].to.attribute[Title].value");

            slObjectSelect.add("from[Object Route|to.current=='In Process'].to.to[Route Task|from.owner=="+strOwner+"].from.current");

            slObjectSelect.add("to[MSIL Milestone Task].from.name");

            slObjectSelect.add("attribute[MSIL Task Type].value");

            slObjectSelect.add("attribute[MSIL Enovia PO No].value");

            slObjectSelect.add("attribute[MSIL Enovia Part No].value");

            slObjectSelect.add("attribute[MSIL Deliverable].value");

            slObjectSelect.add("attribute[MSIL Machine/Process].value");

            slObjectSelect.add("to[Subtask].from.type");//fetch Parent Object Type

            slObjectSelect.add("to[Subtask].from.attribute[MSIL Task Type]");//fetch Parent Task Type

			

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            StringList slRelSelect = new StringList(4);

            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);

            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);

            slRelSelect.addElement(DomainConstants.SELECT_LEVEL);

            slRelSelect.add("attribute[Task WBS]");

            slRelSelect.add("attribute[Sequence Order]");



            ArrayList<String> alIds = new ArrayList<String>();

            ProjectSpace project    = new ProjectSpace(strProjectId);

            DomainObject taskDO     = DomainObject.newInstance(context);



            DocumentBuilderFactory docFactory   = DocumentBuilderFactory.newInstance();

            DocumentBuilder docBuilder          = docFactory.newDocumentBuilder();

            Document doc                        = docBuilder.newDocument();



            String strTaskActStartDate           = DomainConstants.EMPTY_STRING;

            String strTaskActFinishDate          = DomainConstants.EMPTY_STRING;

            String isDelay                       = DomainConstants.EMPTY_STRING;

            String strStatusIcon				 = DomainConstants.EMPTY_STRING;

            String strPAKId						 = DomainConstants.EMPTY_STRING;

            

            Date dEstStartDate = new Date(),            dEstFinishDate = new Date(), dTodayDate = new Date();

            

            ArrayList<String> alAllDepartmentList     =    new ArrayList<String>();



            // fetch all tasks of the project           

            MapList projectTaskList = task.getTasks(context, project, 0 , slObjectSelect, slRelSelect);

            

            //getting the tasks of Department Project which are not in active for this MasterPrj .

           /* MapList projectTaskListToRemove = getMasterProjectTasks(context,strProjectId);

            StringList slTaskToRemoveIdList = new StringList();

            int iprojectTaskListToRemoveSize = projectTaskListToRemove.size();

            if(iprojectTaskListToRemoveSize > 0)

            {

	            for(int iProjectTaskCount = 0 ; iProjectTaskCount < iprojectTaskListToRemoveSize ; iProjectTaskCount++)

	            {

	            	Map mpProjectTask = (Map) projectTaskListToRemove.get(iProjectTaskCount);

	            	slTaskToRemoveIdList.add((String)mpProjectTask.get(DomainConstants.SELECT_ID));

	            }

            }*/

                      

            if(null != projectTaskList && !projectTaskList.isEmpty())

            {

                int nListSize          = projectTaskList.size();



                Element elGrid          = doc.createElement("Grid"); //creating root node

                doc.appendChild(elGrid);



                Element elBody          = doc.createElement("Body"); //creating root node

                elGrid.appendChild(elBody);

                Map ProjectInfo     = (Map) project.getInfo(context, slObjectSelect);



                strParentProjectNames   =   (String)ProjectInfo.get("name");

                elBody.setAttribute("name"             , strParentProjectNames );

                elBody.setAttribute("id"               , strProjectId);

                elBody.setAttribute("percentComplete"  , (String)ProjectInfo.get("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]"));

                elBody.setAttribute("T"                , strParentProjectNames);

                elBody.setAttribute("C"                , (String)ProjectInfo.get("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]"));

                elBody.setAttribute("S"                , (String)ProjectInfo.get( "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]"));

                elBody.setAttribute("E"                , (String)ProjectInfo.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]"));

                elBody.setAttribute("TYPE"             , (String)ProjectInfo.get(DomainConstants.SELECT_TYPE));

                elBody.setAttribute("DUR"              , (String)ProjectInfo.get("attribute[" +DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]"));

                elBody.setAttribute("TaskId"           , strProjectId);

                elBody.setAttribute("objectId"         , strProjectId);

                elBody.setAttribute("id"               , strProjectId);

                elBody.setAttribute("Id"               , strProjectId);

                elBody.setAttribute("TaskActStartDate" , (String)ProjectInfo.get(task.SELECT_TASK_ACTUAL_START_DATE));

                elBody.setAttribute("TaskActFinishDate", (String)ProjectInfo.get(task.SELECT_TASK_ACTUAL_FINISH_DATE));

                elBody.setAttribute("Current"          , (String)ProjectInfo.get(DomainConstants.SELECT_CURRENT));

                elBody.setAttribute("ACTUAL_DURATION"  , (String)ProjectInfo.get(task.SELECT_TASK_ACTUAL_DURATION));

                elBody.setAttribute("OWNER"            , (String)ProjectInfo.get(DomainConstants.SELECT_OWNER));

                elBody.setAttribute("Description"      , (String)ProjectInfo.get(DomainConstants.SELECT_DESCRIPTION));

                elBody.setAttribute("Reason"           , (String)ProjectInfo.get("attribute[MSILReasonfordelay].value"));

                elBody.setAttribute("ActionPlan"       , (String)ProjectInfo.get("attribute[MSILActionPlanforcatchup].value"));

                elBody.setAttribute("Comments"         , (String)ProjectInfo.get("attribute[Comments].value"));

                elBody.setAttribute("GateStatus"       , (String)ProjectInfo.get("attribute[MSIL Gate Status].value"));

                elBody.setAttribute("WBSID"       , (String)ProjectInfo.get("attribute[Sequence Order]"));

                elBody.setAttribute("MSILTASKTYPE"       , (String)ProjectInfo.get("attribute[MSIL Task Type]"));

                elBody.setAttribute("MSILPONUMBER"       , (String)ProjectInfo.get("attribute[MSIL Enovia PO No].value"));

                elBody.setAttribute("MSILPARTNUMBER"       , (String)ProjectInfo.get("attribute[MSIL Enovia Part No].value"));

                elBody.setAttribute("MSILDeliverable"       , (String)ProjectInfo.get("attribute[MSIL Deliverable].value"));

                elBody.setAttribute("MSILMachine"       , (String)ProjectInfo.get("attribute[MSIL Machine/Process].value"));

				

				 //getting the status color..

                //mpStatusMap = emxtask.getStatusSlip(context, strProjectId); 

				//elBody.setAttribute("statuscolor"       , (String)mpStatusMap.get("statuscolor"));

                mpPALTODept.put((String)ProjectInfo.get("to[Project Access List].from.id") , (String)ProjectInfo.get("attribute[MSILProjectDepartment].value"));

                mpPALTOName.put((String)ProjectInfo.get("to[Project Access List].from.id") , strParentProjectNames);

                mpPALTOCurrent.put((String)ProjectInfo.get("to[Project Access List].from.id"), (String)ProjectInfo.get(DomainConstants.SELECT_CURRENT));



                strProjectDepartment= (String)ProjectInfo.get("attribute[MSILProjectDepartment].value");

                

                Element elB = doc.createElement("B"); //creating root node

                elBody.appendChild(elB);

                Element elParent        = elB;

                String strTaskProject   = strParentProjectNames;

                MapList mlProblemFaced  = getProblemFaced(context , strProjectId);

                MapList mlECN  			= getProjectSpaceECN(context, strProjectId);

                MapList mlIniatives  	= findGetIniatives(context, strProjectId);

                MapList mlMilestoneList = getMilestoneList(context);

                

                Date tempDate = new Date();

                Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());

                int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));

                Date todayDate = new Date();

                todayDate.setHours(0);

                todayDate.setMinutes(0);

                todayDate.setSeconds(0);

                for(int nTaskCount = 0; nTaskCount < nListSize; nTaskCount++)

                {

                    projectTaskMap      = (Map)projectTaskList.get(nTaskCount);

                    strTaskId           = (String)projectTaskMap.get(DomainConstants.SELECT_ID);

                   // if(slTaskToRemoveIdList.contains(strTaskId))

                   // 	continue;

						

					 //Getting status of the task..

                    //mpStatusMap = emxtask.getStatusSlip(context, strTaskId);

                    //strStatusIcon = (String) mpStatusMap.get("statuscolor");

					

                    strCurrent          = (String)projectTaskMap.get(DomainConstants.SELECT_CURRENT);

                    isTaskLeafLevel     = ((String)projectTaskMap.get("from[Subtask]")).equalsIgnoreCase("False") ? "Yes" : "No"; //False - leaf level task

                    strMilestoneName    = projectTaskMap.get("to[MSIL Milestone Task].from.name") == null  ? "" : (String) projectTaskMap.get("to[MSIL Milestone Task].from.name"); //False - leaf level task

                    strGANTTGanttIcons  = ((String)projectTaskMap.get("from[Subtask]")).equalsIgnoreCase("False") ? "" : "1"; 

                    strMSILCriticalTask	= (String)projectTaskMap.get("attribute[MSIL Critical Task].value");

                    strCriticalTask     = ((String)projectTaskMap.get(task.SELECT_CRITICAL_TASK)).equalsIgnoreCase("true")  ||  "YES".equalsIgnoreCase(strMSILCriticalTask) ?  "YES" : "NO";

                    strParentCode       = (String)projectTaskMap.get("attribute[Task WBS]");

                    strLevel            = (String)projectTaskMap.get("level");

                    strTaskName         = (String)projectTaskMap.get("name");

                    sAttrbPrcComplete   = (String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");

                    //strParentSubtaskId  = (String)projectTaskMap.get("to[Subtask].from.id");

                    strTaskType                 = (String)projectTaskMap.get(DomainConstants.SELECT_TYPE);

                    strInboxTask                = (String)projectTaskMap.get("from[Object Route].to.to[Route Task].from.current");

                    strReasonfordelay           = (String)projectTaskMap.get("attribute[MSILReasonfordelay].value");

                    strActionPlanforcatchup     = (String)projectTaskMap.get("attribute[MSILActionPlanforcatchup].value");

                    strComments                 = (String)projectTaskMap.get("attribute[Comments].value");

                    strMSILTaskType             = (String)projectTaskMap.get("attribute[MSIL Task Type].value");

                    slPersonList                = projectTaskMap.get("to[Assigned Tasks].from.name")  instanceof String ? new StringList((String)projectTaskMap.get("to[Assigned Tasks].from.name")) :(StringList) projectTaskMap.get("to[Assigned Tasks].from.name");

                    slDeliverableList           = projectTaskMap.get("from[Task Deliverable].to.name")  instanceof String ? new StringList((String)projectTaskMap.get("from[Task Deliverable].to.name")) :(StringList) projectTaskMap.get("from[Task Deliverable].to.name");

                    if(slDeliverableList==null)

                    {

                        slDeliverableList        =  new StringList();

                    }

                    slDeliverableListids         = projectTaskMap.get("from[Task Deliverable].to.id")  instanceof String ? new StringList((String)projectTaskMap.get("from[Task Deliverable].to.id")) :(StringList) projectTaskMap.get("from[Task Deliverable].to.id");

                    if(slDeliverableListids==null)

                    {

                       slDeliverableListids      =  new StringList();

                    }



                    slDeliverableListTitle       = projectTaskMap.get("from[Task Deliverable].to.attribute[Title].value")  instanceof String ? new StringList((String)projectTaskMap.get("from[Task Deliverable].to.attribute[Title].value")) :(StringList) projectTaskMap.get("from[Task Deliverable].to.attribute[Title].value");

                    if(slDeliverableListTitle==null)

                    {

                       slDeliverableListTitle    =    new StringList();

                    }

                    strGateStatus         = (String)projectTaskMap.get("attribute[MSIL Gate Status].value");

                    sAttribEstStartDate   = (String)projectTaskMap.get( "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");

                    strDuration           = (String)projectTaskMap.get("attribute[" +DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]");

                    sAttribEstEndDate     = (String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");

                    dEstFinishDate        = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(sAttribEstEndDate);

                    Date estFinishDate    = sdf.parse(sAttribEstEndDate);

                    long daysRemaining    = (long) task.computeDuration(sysDate, estFinishDate);

                    

                    if(strCurrent.equals("Complete"))

                    {

                        strStatusIcon = "Green";

                    }

                    else

                    {

                        if (sysDate.after(estFinishDate))

                        {

                            strStatusIcon = "Red";

                        }

                        else if (daysRemaining <= yellowRedThreshold)

                        {

                            strStatusIcon = "Yellow";

                        }

                        else

                        {

                            strStatusIcon = ProgramCentralConstants.EMPTY_STRING;

                        }

                    }
					// Added by Vinit - Start 29-Jun-2018
                    //int iSlipDays         = getTaskStatusSlip(context , estFinishDate ,  strCurrent);
					int iSlipDays           = getTaskStatusSlipBasedOnEstimatedFinishDate(context , sAttribEstEndDate ,  sAttribEstStartDate, strDuration, sAttrbPrcComplete);
					// Added by Vinit - End 29-Jun-2018
                    isDelay               = dEstFinishDate.compareTo(dTodayDate) <= 0 ? "Yes":"No";

                    strTaskActStartDate   = (String)projectTaskMap.get(task.SELECT_TASK_ACTUAL_START_DATE);

                    strTaskActFinishDate  = (String)projectTaskMap.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);

                    strActDuretion        = (String)projectTaskMap.get(task.SELECT_TASK_ACTUAL_DURATION);

                    strTaskOwner          = (String)projectTaskMap.get(DomainConstants.SELECT_OWNER);

                    strDescription        = (String)projectTaskMap.get(DomainConstants.SELECT_DESCRIPTION);



                    if("Project Space".equals(strTaskType)) {

                    	strProjectWBSCode    = (String)projectTaskMap.get("attribute[Task WBS]"); 

                        strTaskProject       = strTaskName;

                        strProjectDepartment = (String)projectTaskMap.get( "attribute[MSILProjectDepartment].value");

                        if(!mpPALTODept.containsKey((String)projectTaskMap.get("to[Project Access List].from.id"))) {

                            mpPALTODept.put((String)projectTaskMap.get("to[Project Access List].from.id") , strProjectDepartment );

                            strProjectDepartment = (String)projectTaskMap.get( "attribute[MSILProjectDepartment].value");

                        }

                        else {

                            strProjectDepartment = (String)mpPALTODept.get((String)projectTaskMap.get("to[Project Access List].from.id") );

                        }

                        if(!mpPALTOCurrent.containsKey((String)projectTaskMap.get("to[Project Access List].from.id"))) {

                        	mpPALTOCurrent.put((String)projectTaskMap.get("to[Project Access List].from.id") , strCurrent);                         

                        }

                    }

                    strPAKId = (String)projectTaskMap.get("to[Project Access Key].from.id");

                    if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strPAKId))                   	

                    	if(!STATE_ACTIVE_POLICY_PROJECT_SPACE.equalsIgnoreCase((String)mpPALTOCurrent.get(strPAKId)))

                    		continue;

                    alAllDepartmentList.add(strProjectDepartment);

                    

                    strLastCode  =  "";

                    if(strParentCode.indexOf(".")>0)

                    {

                         strLastCode  = strParentCode.substring(strParentCode.lastIndexOf(".") +1, strParentCode.length());

                    }

                    else

                    {

                        strLastCode  = strParentCode;

                    }

                    

                    strAssignName="";

                    if(slPersonList!=null )

                    {

                        for (int kk =0 ;kk<slPersonList.size() ;kk++ )

                        {

                            strUserName =  PersonUtil.getFullName(context , (String)slPersonList.get(kk));

                            alAssignList.add(strUserName);

                            if(kk==0)

                            {

                                strAssignName = strUserName;

                            }

                            else

                            {

                                strAssignName += ";" +  strUserName; 

                            }

                        }

                    }

                    if("1".equals(strLevel))

                    {

                        Element elNode = doc.createElement("I");

                        elNode.setAttribute("T"               ,strTaskName);

                        elNode.setAttribute("id"              ,strLastCode);

                        elNode.setAttribute("C"               ,sAttrbPrcComplete);

                        elNode.setAttribute("S"               ,sAttribEstStartDate);

                        elNode.setAttribute("E"               ,sAttribEstEndDate);

                        elNode.setAttribute("D"               ,strDependentNumbers);

                        elNode.setAttribute("RESOURCES"       ,strAssignName);

                        elNode.setAttribute("TYPE"            ,strTaskType);

                        elNode.setAttribute("LEVEL"           ,strLevel);

                        elNode.setAttribute("DUR"             ,strDuration);

                        elNode.setAttribute("TaskId"          ,strTaskId);

                        elNode.setAttribute("objectId"        ,strTaskId);

                        elNode.setAttribute("id"              ,strTaskId);

                        elNode.setAttribute("SLACK"           ,strTitle);

                        elNode.setAttribute("Summery"         ,isTaskLeafLevel);

                        elNode.setAttribute("Reason"          ,strReasonfordelay);

                        elNode.setAttribute("ActionPlan"      ,strActionPlanforcatchup);

                        elNode.setAttribute("Comments"        ,strComments);

                        elNode.setAttribute("TaskActStartDate" ,strTaskActStartDate);

                        elNode.setAttribute("TaskActFinishDate",strTaskActFinishDate);

                        

                        if("Project Space".equals(strTaskType)) {

                            elNode.setAttribute("Department"  , (String)mpPALTODept.get((String)projectTaskMap.get("to[Project Access List].from.id") ) );

                        }

                        else

                        {

                            elNode.setAttribute("Department"   , (String)mpPALTODept.get((String)projectTaskMap.get("to[Project Access Key].from.id") ) );

                        }

                        elNode.setAttribute("isCriticalTask"   , strCriticalTask);

                        elNode.setAttribute("Current"          , strCurrent);

                        elNode.setAttribute("Delay"            , isDelay);

                        elNode.setAttribute("Deliverable"      , ""+slDeliverableList.size());

                        elNode.setAttribute("ACTUAL_DURATION"  , strActDuretion);

                        elNode.setAttribute("OWNER"            , strTaskOwner);

                        elNode.setAttribute("Description"      , strDescription);

                        elNode.setAttribute("MilestoneName"    , strMilestoneName);

                        elNode.setAttribute("MSILTaskType"     , strMSILTaskType);

                        elNode.setAttribute("GateStatus"       , strGateStatus);

                        elNode.setAttribute("SlipDays"         , ""+iSlipDays);

                        elNode.setAttribute("WBSID"       , (String)projectTaskMap.get("attribute[Sequence Order]"));

                        elNode.setAttribute("MSILTASKTYPE"       , (String)projectTaskMap.get("attribute[MSIL Task Type]"));

                        elNode.setAttribute("MSILPONUMBER"       , (String)projectTaskMap.get("attribute[MSIL Enovia PO No].value"));

                        elNode.setAttribute("MSILPARTNUMBER"       , (String)projectTaskMap.get("attribute[MSIL Enovia Part No].value"));

                        elNode.setAttribute("MSILDeliverable"       , (String)projectTaskMap.get("attribute[MSIL Deliverable].value"));

                        elNode.setAttribute("MSILMachine"       , (String)projectTaskMap.get("attribute[MSIL Machine/Process].value"));

                        

						

                        String strTaskProjectName	= (String)mpPALTOName.get((String)projectTaskMap.get("to[Project Access Key].from.id"));

                        

                        if(strParentProjectNames.equals(strTaskProjectName))

                        {

                            elNode.setAttribute("isMasterProjectTask" , "Yes");

                        } else {

                            elNode.setAttribute("isMasterProjectTask" , "No");

                        }

                        

                        if(strInboxTask!=null && "Assigned".equals(strInboxTask))

                        {

                            elNode.setAttribute("IsTaskAssign","Yes");

                        }

                        if("Engineering Schedule".equals(strTaskName) &&  strParentProjectNames.equals(strTaskProjectName))

                        {

                            elNode.setAttribute("IsEngineeringSchedule","Yes");

                        }

                        if("Gate Schedule".equalsIgnoreCase(strMSILTaskType) &&  "Gate".equals(strTaskType)  &&  strParentProjectNames.equals(strTaskProjectName))

                        {

                           elNode.setAttribute("isGateSchedule","Yes");

                        }



                        elNode.setAttribute("MSILTaskType"     ,strMSILTaskType);

                        for (int iDeliverable = 0; iDeliverable < slDeliverableList.size(); iDeliverable++) {

                            

                            Element elDeliverable = doc.createElement("Deliverable");

                            elDeliverable.setAttribute("Name",(String)slDeliverableList.get(iDeliverable));

                            elDeliverable.setAttribute("Id",(String)slDeliverableListids.get(iDeliverable));

                            elDeliverable.setAttribute("Title",(String)slDeliverableListTitle.get(iDeliverable));

                            elNode.appendChild(elDeliverable);

                        }

                        elB.appendChild(elNode);

                        

                        elNode.setAttribute("SlipDays"          , ""+iSlipDays);

                        elNode.setAttribute("statuscolor"       , strStatusIcon);

                        if("Yes".equalsIgnoreCase(isTaskLeafLevel))

                        {

                            //updateParentSlipDays(elNode, iSlipDays,strProjectId);

                        }

                        mapParentMap.put(strTaskId , elNode);

                    }

                    else

                    {

                    	//Getting Gate Status of the task for GateClosure graph..

                        String strGateClosureStatus = getGateClosureStatusValue(context,(String)projectTaskMap.get("to[Subtask].from.type"),strMSILTaskType,strGateStatus,sAttribEstEndDate);

                        String strParentId1  = (String)projectTaskMap.get("to[Subtask].from.id");

                        Element elParentNode = (Element)mapParentMap.get(strParentId1);

                        Element elNode       = doc.createElement("I");

                        elNode.setAttribute("ParentID"   , strParentId1);

                        elNode.setAttribute("T"                  ,strTaskName);

                        elNode.setAttribute("id"                 ,strLastCode);

                        elNode.setAttribute("C"                  ,sAttrbPrcComplete);

                        elNode.setAttribute("S"                  ,sAttribEstStartDate);

                        elNode.setAttribute("E"                  ,sAttribEstEndDate);

                        elNode.setAttribute("D"                  ,strDependentNumbers);

                        elNode.setAttribute("TYPE"               ,strTaskType);

                        elNode.setAttribute("RESOURCES"          ,strAssignName);

                        elNode.setAttribute("DUR"                ,strDuration);

                        elNode.setAttribute("TaskId"             ,strTaskId);

                        elNode.setAttribute("objectId"           ,strTaskId);

                        elNode.setAttribute("id"                 ,strTaskId);

                        elNode.setAttribute("SLACK"              ,strTitle);

                        elNode.setAttribute("Summery"            ,isTaskLeafLevel);

                        elNode.setAttribute("Reason"             ,strReasonfordelay);

                        elNode.setAttribute("ActionPlan"         ,strActionPlanforcatchup);

                        elNode.setAttribute("Comments"           ,strComments);

                        elNode.setAttribute("TaskActStartDate"   ,strTaskActStartDate);

                        elNode.setAttribute("TaskActFinishDate"  ,strTaskActFinishDate);

						elNode.setAttribute("parentObjectType"       , (String)projectTaskMap.get("to[Subtask].from.type"));

                        elNode.setAttribute("parentTaskType"       , (String)projectTaskMap.get("to[Subtask].from.attribute[MSIL Task Type]"));

                        if("Project Space".equals(strTaskType)) {

                            elNode.setAttribute("Department"       , (String)mpPALTODept.get((String)projectTaskMap.get("to[Project Access List].from.id") ) );

                        }

                        else

                        {

                            elNode.setAttribute("Department"       , (String)mpPALTODept.get((String)projectTaskMap.get("to[Project Access Key].from.id") ) );

                        }

                        elNode.setAttribute("isCriticalTask"     ,strCriticalTask);

                        elNode.setAttribute("Current"            ,strCurrent);

                        elNode.setAttribute("Delay"              ,isDelay);

                        elNode.setAttribute("Deliverable"        ,""+slDeliverableList.size());

                        elNode.setAttribute("ACTUAL_DURATION"    ,strActDuretion);

                        elNode.setAttribute("OWNER"              ,strTaskOwner);

                        elNode.setAttribute("Description"        ,strDescription);

                        elNode.setAttribute("MilestoneName"      ,strMilestoneName);

                        elNode.setAttribute("MSILTaskType"       ,strMSILTaskType);

                        elNode.setAttribute("GateStatus"         ,strGateStatus);

                        elNode.setAttribute("GateClosureStatus"         ,strGateClosureStatus);

                        

                        

                        elNode.setAttribute("WBSID"       , (String)projectTaskMap.get("attribute[Sequence Order]"));

                        elNode.setAttribute("MSILTASKTYPE"       , (String)projectTaskMap.get("attribute[MSIL Task Type]"));

                        elNode.setAttribute("MSILPONUMBER"       , (String)projectTaskMap.get("attribute[MSIL Enovia PO No].value"));

                        elNode.setAttribute("MSILPARTNUMBER"       , (String)projectTaskMap.get("attribute[MSIL Enovia Part No].value"));

                        elNode.setAttribute("MSILDeliverable"       , (String)projectTaskMap.get("attribute[MSIL Deliverable].value"));

                        elNode.setAttribute("MSILMachine"       , (String)projectTaskMap.get("attribute[MSIL Machine/Process].value"));

                         

						

                        String strTaskProjectName	= (String)mpPALTOName.get((String)projectTaskMap.get("to[Project Access Key].from.id"));



                        if(strParentProjectNames.equals(strTaskProjectName))

                        {

                        	elNode.setAttribute("isMasterProjectTask" , "Yes");

                        } else {

                        	elNode.setAttribute("isMasterProjectTask" , "No");

                        }

                        

                        elNode.setAttribute("SlipDays" , ""+iSlipDays);



                        if(strInboxTask!=null && "Assigned".equals(strInboxTask))

                        {

                            elNode.setAttribute("IsTaskAssign","Yes");

                        }



                        if("Engineering Schedule".equals(strTaskName) &&  strParentProjectNames.equals(strTaskProjectName))

                        {

                            elNode.setAttribute("IsEngineeringSchedule","Yes");

                        }

                        if("Gate Schedule".equalsIgnoreCase(strMSILTaskType) &&  "Gate".equals(strTaskType)  &&  strParentProjectNames.equals(strTaskProjectName))

                        {

                           elNode.setAttribute("isGateSchedule","Yes");

                        }



                        for (int iDeliverable = 0; iDeliverable < slDeliverableList.size(); iDeliverable++) {

                            Element elDeliverable = doc.createElement("Deliverable");

                            elDeliverable.setAttribute("Name",(String)slDeliverableList.get(iDeliverable));

                            elDeliverable.setAttribute("Id",(String)slDeliverableListids.get(iDeliverable));

                            elDeliverable.setAttribute("Title",(String)slDeliverableListTitle.get(iDeliverable));

                            elNode.appendChild(elDeliverable);

                        }

                        

                        elParentNode.appendChild(elNode);

                        mapParentMap.put(strTaskId , elNode);

                        elNode.setAttribute("SlipDays" , ""+iSlipDays);

                        elNode.setAttribute("statuscolor"       , strStatusIcon);

                        if("Yes".equalsIgnoreCase(isTaskLeafLevel))

                        {

                            //updateParentSlipDays(elParentNode, mapParentMap, iSlipDays,strProjectId );

                        }

                    }

                }

                

                if("Project Space".equals(strTaskType))

                {

                    //strParentProjectNames      =   strTaskName;

                }

                //Add All Department In Dashboard

                Set<String> alDepartmentSet     =     new HashSet<String>(alAllDepartmentList);

                alAllDepartmentList.clear();

                alAllDepartmentList.addAll(alDepartmentSet);

                String strDepartmentName     =     alAllDepartmentList.toString();

                for (int i = 0; i < alAllDepartmentList.size(); i++) {

                    if(i==0)

                    {

                        strDepartmentName    =    alAllDepartmentList.get(i);

                    }

                    else

                    {

                        strDepartmentName    += ","+    alAllDepartmentList.get(i);

                    }

                }

				elBody.setAttribute("ALL_DEPARTMENT", strDepartmentName);

				  //Added By Yaseen..

                //in the ALL_DEPARTMENT tag,NPE is also included.So removing that..

                String strPersonId = PersonUtil.getPersonObjectID(context);

                DomainObject personDO = new DomainObject(strPersonId);

                Map mpPersonMap = getUserList(context, personDO);

                StringList slUserDepartments = (StringList) mpPersonMap.get("Department");

                String strUserDepartment = StringUtil.join(slUserDepartments, ",");

                elBody.setAttribute("USER_DEPARTMENT", strUserDepartment);

                //------------------------------------

								

                

                //updateProjectSlipDays(elBody , elB);

               

                //Start ECN Graph 

                

                

                Element elProblemFaced     = doc.createElement("ProblemFaced"); //creating root node

                elGrid.appendChild(elProblemFaced);

                Element elProblemFacedObj  = null;

                Map  mapProblam	           = null;

                String strProblamDept      = DomainConstants.EMPTY_STRING;

                String strProblamName      = DomainConstants.EMPTY_STRING;

				String strParentOwner 	   = DomainConstants.EMPTY_STRING;

                for (int i = 0; i < mlProblemFaced.size(); i++) {

                	mapProblam             = (Map) mlProblemFaced.get(i);

                	strProblamDept         = (String)mapProblam.get("attribute[MSILProjectDepartment].value");

                	strProblamName         = (String)mapProblam.get("attribute[MSIL Milestone].value");

					strParentOwner         = (String)mapProblam.get("from[MSIL Problem Faced Project].to.owner");

                	elProblemFacedObj      = doc.createElement("ProblemFacedObj");

                	elProblemFacedObj.setAttribute("Dept", strProblamDept);

                	elProblemFacedObj.setAttribute("Name", strProblamName);

					elProblemFacedObj.setAttribute("ParentProjectOwner", strParentOwner); 

                	elProblemFacedObj.setAttribute("DESCRIPTION", (String)mapProblam.get(DomainConstants.SELECT_DESCRIPTION ));

                	elProblemFacedObj.setAttribute("TemporaryCounterMeasure", (String)mapProblam.get("attribute[MSIL Temporary Counter Measure].value" ));

                	elProblemFacedObj.setAttribute("LongTermCounterMeasure", (String)mapProblam.get("attribute[MSIL Long Term Counter Measure].value" ));

                	elProblemFacedObj.setAttribute("Learning", (String)mapProblam.get("attribute[MSIL Learning].value" ));

                	elProblemFacedObj.setAttribute("Checksheet", (String)mapProblam.get("attribute[MSIL Checksheet].value" ));

                	elProblemFacedObj.setAttribute("UpdateReadCheck", (String)mapProblam.get("attribute[MSIL Update Read Check].value"));

                	elProblemFacedObj.setAttribute("WayAhead", (String)mapProblam.get("attribute[MSIL Way Ahead].value"));

                    elProblemFacedObj.setAttribute("IfRequiredThenDateOfUpdate", (String)mapProblam.get("attribute[MSIL If Update Reqd].value"));

                	elProblemFaced.appendChild(elProblemFacedObj);

                }

                

                

                Element elECN          = doc.createElement("ECN"); //creating root node

                elGrid.appendChild(elECN);

                Element elECNObj       = null;

                Map  mapECN	           = null;

                String strECNDept      = DomainConstants.EMPTY_STRING;

                String strECNName      = DomainConstants.EMPTY_STRING;

                for (int i = 0; i < mlECN.size(); i++) {

                	mapECN                 = (Map) mlECN.get(i);

                	strECNDept             = (String)mapECN.get("attribute[MSILProjectDepartment].value");

                	strECNName             = (String)mapECN.get("attribute[MSIL Milestone].value");

                	// Modifed by Dheeraj Garg <09-Aug-2016> Blank ECN graph. -- Start
					strParentOwner 	   = (String)mapECN.get("from[MSIL Project Space ECN].to.owner");
					// Modifed by Dheeraj Garg <09-Aug-2016> Blank ECN graph. -- End

                	elECNObj      = doc.createElement("ECNObj");

					

                	elECNObj.setAttribute("ECNIssueDate"      , (String)mapECN.get("attribute[MSIL ECN Issue Date].value"));

                	elECNObj.setAttribute("ECNDepartment"     , strECNDept);

					elECNObj.setAttribute("ParentProjectOwner"     , strParentOwner);

                	elECNObj.setAttribute("ECNPartAssmbly"    , (String)mapECN.get("attribute[MSIL Part Assembly].value"));

                	elECNObj.setAttribute("ECNNumber"         , (String)mapECN.get("attribute[MSIL ECN Number].value"));

                	elECNObj.setAttribute("ECNDetails"        , (String)mapECN.get("attribute[MSIL ECN Details].value"));

                	elECNObj.setAttribute("Milestone"         , (String)mapECN.get("attribute[MSIL Milestone].value") );

                	elECNObj.setAttribute("ECNCutOffDate"     , (String)mapECN.get("attribute[MSIL ECN Cutoff Date].value"));

                	elECNObj.setAttribute("ECNCost"           , (String)mapECN.get("attribute[MSIL Cost].value"));

                	elECNObj.setAttribute("ECNQuality"        , (String)mapECN.get("attribute[MSIL Quality].value"));

                	elECNObj.setAttribute("ECNTimeline"       , (String)mapECN.get("attribute[MSIL Timeline].value"));

                	elECNObj.setAttribute("FeasibilityCheck"  , (String)mapECN.get("attribute[MSIL Feasibility Check].value"));

                	elECNObj.setAttribute("chassisEngineNumber", (String)mapECN.get("attribute[MSIL Cut off Chassis/Engine No].value"));

                	elECNObj.setAttribute("StatusofECN", (String)mapECN.get("attribute[MSIL Status of ECN].value"));

                	elECNObj.setAttribute("ImplementationDate", (String)mapECN.get("attribute[MSIL Implementation Date].value"));

                	elECNObj.setAttribute("Remarks", (String)mapECN.get("attribute[MSIL Remarks].value"));

                	elECN.appendChild(elECNObj);

                }

                

                

                //mlIniatives

                

                //Adding Iniatives Data in XML 

                Element elIniatives     = doc.createElement("Iniatives"); //creating root node

                elGrid.appendChild(elIniatives);

                Element elIniativesObj  = null;

                Map  mapIniatives	    = null;

                String strIniativesDept = DomainConstants.EMPTY_STRING;

                String strIniativesName = DomainConstants.EMPTY_STRING;

                for (int i = 0; i < mlIniatives.size(); i++) {

                	mapIniatives                 = (Map) mlIniatives.get(i);

                	strIniativesDept             = (String)mapIniatives.get("attribute[MSILProjectDepartment].value");

                	strIniativesName             = (String)mapIniatives.get("attribute[MSIL Milestone].value");

					strParentOwner	 = (String)mapIniatives.get("from[MSIL Initiative Project].to.owner");

                	elIniativesObj               = doc.createElement("IniativesObj");

                	elIniativesObj.setAttribute("Milestone"                    , strIniativesName);

                	elIniativesObj.setAttribute("Department"                   , strIniativesDept);

                	elIniativesObj.setAttribute("objectType"                   , (String)mapIniatives.get("objectType"));

                	elIniativesObj.setAttribute("InitiativeType"                   , (String)mapIniatives.get(DomainConstants.SELECT_TYPE));

					elIniativesObj.setAttribute("ParentProjectOwner"                   , strParentOwner);

                	elIniativesObj.setAttribute("Initiative"                   , (String)mapIniatives.get("attribute[MSIL Initiative].value"));

                	elIniativesObj.setAttribute("BenefitType"                  , (String)mapIniatives.get("attribute[MSIL Benefit Type].value"));

                	elIniativesObj.setAttribute("BenefitDetails"               , (String)mapIniatives.get("attribute[MSIL Benefit Details].value"));

                	elIniativesObj.setAttribute("BenefitCategory"              , (String)mapIniatives.get("attribute[MSIL Benefit Category].value") );

                	elIniativesObj.setAttribute("Checksheet"                   , (String)mapIniatives.get("attribute[MSIL Checksheet].value"));

                	elIniativesObj.setAttribute("MSILUpdateRequired"           , (String)mapIniatives.get("attribute[MSIL Update Required].value"));

                	elIniativesObj.setAttribute("IfRequiredThenDateOfupdate"   , (String)mapIniatives.get("attribute[MSIL Update Reqd Date].value"));

                	elIniativesObj.setAttribute("WayAhead"                     , (String)mapIniatives.get("attribute[MSIL Way Ahead].value"));

                	elIniatives.appendChild(elIniativesObj);

                }

				

				

				  //Added By Yaseen..

				//Getting List of All Milestones in db and adding the list into XML..                     

                Element elMilestone          = doc.createElement("milestoneList"); //creating root node

                elGrid.appendChild(elMilestone);

                Element elMilestoneObj       = null;

                Map  mapMilestone	           = null;

               

                int iMilestoneSize = mlMilestoneList.size();

                for (int i = 0; i < iMilestoneSize ; i++) {

                	mapMilestone                 = (Map) mlMilestoneList.get(i);



                	elMilestoneObj               = doc.createElement("MilestoneObject");

                	elMilestoneObj.setAttribute("milestoneName" , (String)mapMilestone.get(DomainConstants.SELECT_NAME));

                	elMilestone.appendChild(elMilestoneObj);

                }

            

                //write the content into xml file

                updateParentSlipDaysAllProject(doc);

                updateProjectSlipDays(elBody , elB);

                updateParentStatusAllProject(doc);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                Transformer transformer = transformerFactory.newTransformer();

                DOMSource source        = new DOMSource(doc);

                File templateFile       = new File(strWorkSpace);

                File templateXMLFile    = new File(templateFile, strFileName);

                //if(templateXMLFile.getTotalSpace()>0)

                {

	                StreamResult result     = new StreamResult(templateXMLFile);

	                transformer.transform(source, result);

	                DomainObject doPerson = PersonUtil.getPersonObject(context);

	                StringList fileList = new StringList(1);

	                fileList.add(strFileName);

	                try{

	                    doPerson.checkinFromServer(context,

	                            true,

	                            true,

	                            "generic",

	                            fileList);

	                }

	                catch(Exception e)

	                {

	                    System.out.println("Get XML------------------->>>>>>>>>>"+e);

	                }

                }



                HashSet hs = new HashSet();

                hs.addAll(alAssignList);

                alAssignList.clear();

                alAssignList.addAll(hs);

                returnMap.put("assignUser",alAssignList);

            }

        }

        catch (Exception ex )

        {

            ex.printStackTrace();

        }

        finally

        {

             DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].from.name");

             DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.name");

             DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.id");

             DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.attribute[Title].value");

        }

        return returnMap;

    }

    

    public void updateParentSlipDaysAllProject(Document doc) throws Exception 

    {

         String isLeafTask	=	DomainConstants.EMPTY_STRING;

         try {

               NodeList nList = doc.getElementsByTagName("I");

               Node nNode =null;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement     = (Element) nNode;

                       isLeafTask           = (String) eElement.getAttribute("Summery");

                       if("Yes".equalsIgnoreCase(isLeafTask) )

                       {

                          updateTaskSlipDays(eElement);

                       }

                   }

               }

       } catch (Exception e) {

            e.printStackTrace();;

       }

    }

    

    public void updateTaskSlipDays(Element eElement) throws Exception 

    {

    	try {

    		if(eElement != null)

            {

            	

                Element parentNode     = (Element) eElement.getParentNode();

                

                if(!"B".equals(parentNode.getNodeName()))

       		    {

                	String strParentSlipDays = parentNode.getAttribute("SlipDays");

                    String strTaskSlipDays = eElement.getAttribute("SlipDays");



                	if(strParentSlipDays != null)

                	{

                		int parentSlipDays=0;

                		int taskSlipdays =0;

                		  try {

     	                	 parentSlipDays    = Integer.parseInt(strParentSlipDays);

     	                	taskSlipdays    = Integer.parseInt(strTaskSlipDays);

     					} catch (Exception e) {

     						

     					}

            		  if(taskSlipdays >= parentSlipDays)

                      {

            			  parentNode.setAttribute("SlipDays", ""+taskSlipdays);

            			  updateTaskSlipDays(parentNode);

                      }

                	}

                	else

                	{

                		parentNode.setAttribute("SlipDays",strTaskSlipDays );

                        updateTaskSlipDays(parentNode);

                	}

       		    }

            }

		} catch (Exception e) {

			e.printStackTrace();

		}

     }

    public void updateParentStatusAllProject(Document doc) throws Exception 

    {

         String isLeafTask    =    DomainConstants.EMPTY_STRING;

         try {

               NodeList nList = doc.getElementsByTagName("I");

               Node nNode =null;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement     = (Element) nNode;

                       isLeafTask           = (String) eElement.getAttribute("Summery");

                       if("Yes".equalsIgnoreCase(isLeafTask))

                       {

                           updateTaskStatus(eElement);

                       }

                   }

               }

       } catch (Exception e) {

            e.printStackTrace();;

       }

    }

    

    public void updateTaskStatus(Element eElement) throws Exception 

    {

        try {

            if(eElement != null)

            {

                Element parentNode     = (Element) eElement.getParentNode();

                if(!"B".equals(parentNode.getNodeName()))

                   {

                    String strParentStatus = parentNode.getAttribute("statuscolor");

                    String strTaskStatus = eElement.getAttribute("statuscolor");

                    if(strParentStatus != null)

                    {

                        

                        if("Red".equalsIgnoreCase(strTaskStatus))

                        {

                            parentNode.setAttribute("statuscolor",strTaskStatus );

                        }

                        else if("Yellow".equalsIgnoreCase(strTaskStatus) && !"Red".equalsIgnoreCase(strParentStatus) )

                        {

                               parentNode.setAttribute("statuscolor",strTaskStatus );

                        }

                        else if("".equalsIgnoreCase(strTaskStatus) && ("Green".equalsIgnoreCase(strParentStatus) ) )

                        {

                            parentNode.setAttribute("statuscolor","" );

                        }

                        else if("Green".equalsIgnoreCase(strTaskStatus))

                        {

                            String strCurrent     =    parentNode.getAttribute("Current");

                            if("Complete".equalsIgnoreCase(strCurrent))

                            {

                              parentNode.setAttribute("statuscolor",strTaskStatus );

                            }

                        }

                        updateTaskStatus(parentNode);

                    }

                    else

                    {

                        parentNode.setAttribute("statuscolor",strTaskStatus );

                        updateTaskStatus(parentNode);

                    }

                   }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

     }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public void genreteGDXML(Context context , String [] args)throws Exception 

    {

        try {

            System.out.println("Start -->> "+new Date());

			emxTask_mxJPO emxtask = new emxTask_mxJPO(context, args);

            String strUserName            = context.getUser();

            Map mpMasterProjectInfo       =    getMasterProjectsMap(context,args);

            Set slMasterProjectId         =    (Set) mpMasterProjectInfo.keySet();

            String strProjectId           =     DomainConstants.EMPTY_STRING;

            ArrayList<String> alMasterIds =     new ArrayList<String>(slMasterProjectId);

            

            ////////////////////////////////////////////////////////////////////////////

            DomainObject object = PersonUtil.getPersonObject(context);



			// check if the BO is locked or not

			BusinessObject personBO = new BusinessObject(object.getId());

			if(personBO.isLocked(context))						

				personBO.unlock(context);



			try

			{

				FileList fileList		= object.getFiles(context, "generic");

				FileItr  fileItr  = new FileItr(fileList);

				while (fileItr.next())

				{

					String fileName	 = fileItr.obj().getName();

					ContextUtil.pushContext(context);

					MqlUtil.mqlCommand(context,"delete bus Person '"+strUserName+"' - format generic file \""+fileName+"\"");

					ContextUtil.popContext(context);

				}

			}

			catch (Exception ex)

			{

				System.out.println("\n..Exception..."+ex);

			}

            

            for (int i = 0; i < alMasterIds.size(); i++) {

                strProjectId     = alMasterIds.get(i);

                getProjectTaskXML(context , strProjectId,emxtask);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        finally {

            System.out.println("End -->> "+new Date());

        }

    }

    

   

    

    public void updateProjectSlipDays(Element elBody , Element elB)

    {

    	try {

    		NodeList childNodeList   =   elB.getChildNodes();

    		Node	nNode;

    		ArrayList<Integer> alAllList 	=	 new ArrayList<Integer>();

    		Element eElement;

    		String strSlipDays	=	DomainConstants.EMPTY_STRING;

    		if(childNodeList!=null)

            {

               for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                   nNode = childNodeList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       eElement     = (Element) nNode;

                       strSlipDays	=	eElement.getAttribute("SlipDays");

                       int iSlipDays	=	0;

                       try {

                    	   iSlipDays=	Integer.parseInt(strSlipDays);

						} catch (Exception e) {

							

						}

                       alAllList.add(iSlipDays);

                   }

               }

               if(!alAllList.isEmpty())

               {

            	   Integer iMaxValue = Collections.max(alAllList);

            	   elBody.setAttribute("SlipDays", ""+iMaxValue);

               }

               else

               {

            	   elBody.setAttribute("SlipDays", "0");

               }

            }

    		

		} catch (Exception e) {

			e.printStackTrace();

		}

    }



    public  void updateParentSlipDays (Element elNode , HashMap mapParentMap , int iSlipDays , String strProjectId)

    {

        try {

            if(elNode != null)

            {

            	

                String strParentSlipDays    =    elNode.getAttribute("SlipDays");

                if(strParentSlipDays  != null )

                {

                	int parentSlipDays=0;

                    try {

	                	 parentSlipDays    = Integer.parseInt(strParentSlipDays);

					} catch (Exception e) {

						

					}

                    if(iSlipDays > parentSlipDays)

                    {

                        elNode.setAttribute("SlipDays", ""+iSlipDays);

                        //Element elParentNode  = (Element) elNode.getParentNode();

                        String strParentID	=	(String)elNode.getAttribute("ParentID");

                        Element elParentNode = (Element)mapParentMap.get(strParentID);

                        updateParentSlipDays(elParentNode , mapParentMap ,iSlipDays,strProjectId);

                    }

                }

                else

                {

                    elNode.setAttribute("SlipDays", ""+iSlipDays);

                }

            }

         } catch (Exception e) {

             e.printStackTrace();

         }

    }

	@com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getParcentCompleted(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList     =     new MapList();

        try {

           File[] listOfFiles                  = getAllProjectFile(context);

           

            DocumentBuilderFactory dbFactory          = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = null;

     

            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();

              

                  NodeList nList = doc.getElementsByTagName("Body");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement = (Element) nNode;

                          projectMap.put("id", eElement.getAttribute("id"));

                          projectMap.put("name", eElement.getAttribute("name"));

                          projectMap.put("percentComplete", eElement.getAttribute("percentComplete"));

                          mlreturnList.add(projectMap);

                      }

                  }

              }

           }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlreturnList;

    }

    

    //END : Division Dashboard

    


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCriticalTaskData(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList             =     new MapList();

        try {

            //File folder                 = new File("E:\\WorkSpace\\Nikesh");

            //File[] listOfFiles             = folder.listFiles();

           File[] listOfFiles                  = getAllProjectFile(context);

           

            DocumentBuilderFactory dbFactory          = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder       = dbFactory.newDocumentBuilder();

            Document doc                   = null;

            String isCriticalTask          = DomainConstants.EMPTY_STRING;

            String strCurrent              = DomainConstants.EMPTY_STRING;

            String strDelay                = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask     = DomainConstants.EMPTY_STRING;

            String strDepartment = DomainConstants.EMPTY_STRING;

            String strUserDepartment = DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            int iCompleted                 = 0;

            int iDelay                     = 0;

            int iTotal                     = 0;

            

                    

            for (int i = 0; i < listOfFiles.length; i++) {

                 iCompleted                 = 0;

                 iDelay                     = 0;

                 iTotal                     = 0;



              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();



                  NodeList nList = doc.getElementsByTagName("Body");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement = (Element) nNode;

                          projectMap.put("ProjectId", eElement.getAttribute("id"));

                          projectMap.put("ProjectName", eElement.getAttribute("name"));

                          strUserDepartment = eElement.getAttribute("USER_DEPARTMENT");

                          if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strUserDepartment))

                        	  slUserDepartment = StringUtil.split(strUserDepartment, ",");

                      }

                  }

                  nList = doc.getElementsByTagName("I");

                  nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement     = (Element) nNode;

                          isCriticalTask       = (String) eElement.getAttribute("isCriticalTask");

                          isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                          strDepartment = (String) eElement.getAttribute("Department"); 

                          if(slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT))

                          {

	                          if("No".equalsIgnoreCase(isMasterProjectTask))

	                          {

		                          if("YES".equalsIgnoreCase(isCriticalTask))

		                          {

		                              iTotal++;

		                              strCurrent   = (String) eElement.getAttribute("Current");

		                              if("Complete".equals(strCurrent))

		                              {

		                                  iCompleted++;

		                              }

		                              else

		                              {

		                                  strDelay   = (String) eElement.getAttribute("Delay");

		                                  if("Yes".equalsIgnoreCase(strDelay))

		                                  {

		                                      iDelay++;

		                                  } 

		                              }

		                          }

	                          }

                          }                          

                      }

                  }

                  projectMap.put("Delayed"      , iDelay);

                  projectMap.put("Total"        , iTotal);

                  projectMap.put("Completed"    , iCompleted);

                  mlreturnList.add(projectMap);

              }

           }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlreturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getThirdTableData(Context context , String [] args ) throws Exception

    {

        HashMap returnMap            =    new HashMap();

        MapList mlreturnList         =    new MapList();

        try {

            File[] listOfFiles                  = getAllProjectFile(context);

            DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

            Document doc                  = null;

            String isLeafTask             = DomainConstants.EMPTY_STRING;

            String strCurrent             = DomainConstants.EMPTY_STRING;

            String strDelay               = DomainConstants.EMPTY_STRING;

            String strAllDepartment       = DomainConstants.EMPTY_STRING;

            String strDepartment          = DomainConstants.EMPTY_STRING;

            String iDeliverable           = DomainConstants.EMPTY_STRING;

            String strDepartmentName      = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask	  = DomainConstants.EMPTY_STRING;

            int iCompleted                = 0;

            int iDelay                    = 0;

            int iTotal                    = 0;

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();



            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap         =     new HashMap();

                  Element eElementProject    = (Element)  doc.getElementsByTagName("Body").item(0);

                  projectMap.put("ProjectId"           , eElementProject.getAttribute("id"));

                  projectMap.put("ProjectName"         , eElementProject.getAttribute("name"));

                  strAllDepartment           = eElementProject.getAttribute("ALL_DEPARTMENT");



                  ArrayList<String> alDeparmentList      = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

                  alAllProjectDepartment.addAll(alDeparmentList);

                  int [] aryLeafTaskCount                = new int[alDeparmentList.size()];

                  int [] aryDeliverableCount             = new int[alDeparmentList.size()];

                  

                  NodeList nList = doc.getElementsByTagName("I");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement     = (Element) nNode;

                          isLeafTask           = (String) eElement.getAttribute("Summery");

                          isMasterProjectTask           = (String) eElement.getAttribute("isMasterProjectTask");

                          if("Yes".equalsIgnoreCase(isLeafTask) && !"Yes".equalsIgnoreCase(isMasterProjectTask))

                          {

                              strDepartment           = (String) eElement.getAttribute("Department");

                              aryLeafTaskCount[alDeparmentList.indexOf(strDepartment)]++;

                              iDeliverable            = (String) eElement.getAttribute("Deliverable");

                              

                              if(iDeliverable!=null && !"".equals(iDeliverable))

                              {

                                  try {

                                    int iDeliverableCount    =    Integer.parseInt(iDeliverable);

                                    aryDeliverableCount[alDeparmentList.indexOf(strDepartment)]     =     aryDeliverableCount[alDeparmentList.indexOf(strDepartment)] + iDeliverableCount;

                                } catch (Exception e) {

                                    e.printStackTrace();

                                }

                              }

                          }

                      }

                  }

                  int iTotalTaskList     =    0;

                  int iDeliverableList   =    0;

                  for (int j = 0; j < alDeparmentList.size(); j++) {

                      strDepartmentName        =    (String)alDeparmentList.get(j);

                      HashMap TaskCountMap     = new HashMap();

                      TaskCountMap.put("TotalDepartmentTask",  ""+aryLeafTaskCount[alDeparmentList.indexOf(strDepartmentName)]);

                      TaskCountMap.put("Deliverable",  ""+ aryDeliverableCount[alDeparmentList.indexOf(strDepartmentName)]);

                      projectMap.put(strDepartmentName        , TaskCountMap);

                  }

                  mlreturnList.add(projectMap);

              }

              Set<String> uniqueDepartmentList     =    new HashSet<String>(alAllProjectDepartment) ;

              alAllProjectDepartment.clear();

              // Modifed by Dheeraj Garg <11-Aug-2016> Order of the dept list should be in particular order -- Start
              // Modifed by Dheeraj Garg <05-Sep-2016> Moved list of departments to MSILConstants
              for(String str: SORTED_ARRAY_DEPARTMENT_NAMES)
              	if(uniqueDepartmentList.contains(str))
              		alAllProjectDepartment.add(str);
              // Modifed by Dheeraj Garg <11-Aug-2016> Order of the dept list should be in particular order -- End
              returnMap.put("DepartmentNames", alAllProjectDepartment);

              returnMap.put("MasterProjectDepartmentData", mlreturnList);

           }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getDelayedTaskDashboardData_GD(Context context , String [] args ) throws Exception

    {

        HashMap returnMap                 =    new HashMap();

        MapList mlreturnList             =    new MapList();

        try {

            //File folder                 = new File("E:\\WorkSpace\\Nikesh");

            //File[] listOfFiles             = folder.listFiles();

           

           File[] listOfFiles                  = getAllProjectFile(context);

           

            DocumentBuilderFactory dbFactory          = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            String strCurrent             = DomainConstants.EMPTY_STRING;

            String strDelay                 = DomainConstants.EMPTY_STRING;

            String strDepartment         = DomainConstants.EMPTY_STRING;

            String strType                 = DomainConstants.EMPTY_STRING;

            String strLevel                = DomainConstants.EMPTY_STRING;

            String isLeafTask             = DomainConstants.EMPTY_STRING;

			String isMasterProjectTask   = DomainConstants.EMPTY_STRING;

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

            String strAllDepartment        = DomainConstants.EMPTY_STRING;

        

            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();

                  

                  Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                  projectMap.put("ProjectId", eElementProject.getAttribute("id"));

                  projectMap.put("ProjectName", eElementProject.getAttribute("name"));

                  strAllDepartment    = eElementProject.getAttribute("USER_DEPARTMENT");





                  ArrayList<String> alDeparmentList     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

                  /*ArrayList<String> alTempList     =     new ArrayList<String>();

                  for (int j = 0; j < alDeparmentList.size(); j++) {

                      alTempList.add(alDeparmentList.get(j).trim());

                  }

                  alDeparmentList.clear();

                  alDeparmentList.addAll(alTempList);*/

                  

                  alAllProjectDepartment.addAll(alDeparmentList);

                  int [] aryTaskCount  = new int[alDeparmentList.size()];

                  int iTotaltask       = 0;



                  NodeList nList = doc.getElementsByTagName("I");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement = (Element) nNode;

                          //strType  = (String) eElement.getAttribute("TYPE");

                          //strLevel  = (String) eElement.getAttribute("LEVEL");

                          //if(!"Project Space".equalsIgnoreCase(strType) && !strLevel.equalsIgnoreCase("1") )

                          isLeafTask           = (String) eElement.getAttribute("Summery");

						  isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

						  strDepartment           = (String) eElement.getAttribute("Department");

						  if("No".equalsIgnoreCase(isMasterProjectTask) && alDeparmentList.contains(strDepartment))

                          {

							  if("Yes".equalsIgnoreCase(isLeafTask))

							  {

								  iTotaltask++;

								  strDelay       = (String) eElement.getAttribute("Delay");

								  strCurrent       = (String) eElement.getAttribute("Current");

								  if("Yes".equalsIgnoreCase(strDelay) &&  !"Complete".equals(strCurrent))

								  {

									 

									  if(alDeparmentList.contains(strDepartment))

											aryTaskCount[alDeparmentList.indexOf(strDepartment)]++;

								  }

							  }

						  }

                      }

                  }

                 

                  String strDepartmentName     = DomainConstants.EMPTY_STRING;

                  for (int j = 0; j < alDeparmentList.size(); j++) {

                      strDepartmentName         =    (String)alDeparmentList.get(j);

                      //iTotalTaskList        +=    aryTaskCount[alDeparmentList.indexOf(strDepartmentName)];

                      projectMap.put(strDepartmentName        , aryTaskCount[alDeparmentList.indexOf(strDepartmentName)]);

                  }

                  projectMap.put("TotalTasks",iTotaltask);

                  mlreturnList.add(projectMap);

              }

              

              Set<String> uniqueDepartmentList     =    new HashSet<String>(alAllProjectDepartment) ;

              alAllProjectDepartment.clear();

              alAllProjectDepartment.addAll(uniqueDepartmentList);



              returnMap.put("ListOfDepartments", alAllProjectDepartment);

              returnMap.put("MasterProjectDepartmentData", mlreturnList);

           }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap ;

    }

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getInboxtask(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList             =     new MapList();

        try {

           // File folder                 = new File("E:\\WorkSpace\\Nikesh");

            //File[] listOfFiles          = folder.listFiles();

           File[] listOfFiles            = getAllProjectFile(context);

           

            DocumentBuilderFactory dbFactory          = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            String isTaskAssign         = "";

                    

            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();

                  

                  Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                  projectMap.put("ProjectId", eElementProject.getAttribute("id"));

                  projectMap.put("ProjectName", eElementProject.getAttribute("name"));

                  

                  int iTotal                     = 0;

                  NodeList nList = doc.getElementsByTagName("I");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement     = (Element) nNode;

                          isTaskAssign       = (String) eElement.getAttribute("IsTaskAssign");

                          if("Yes".equalsIgnoreCase(isTaskAssign))

                          {

                              iTotal++;  

                          }

                      }

                  }

                  projectMap.put("InboxTask"    , iTotal);

                  mlreturnList.add(projectMap);

              }

           }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlreturnList;

    }

    

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public HashMap getCriticalTaskDataByDept(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList  = new MapList();

        HashMap projectMap    = new HashMap();



        try {

            Map programMap                = (Map) JPO.unpackArgs(args);

            String strObjectId            = (String) programMap.get("objectId");

            String strTaskLegend          = (String) programMap.get("taskLegend");

            File Projectfile              = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

            Document doc                  = null;

            String isCriticalTask         = DomainConstants.EMPTY_STRING;

            String strCurrent             = DomainConstants.EMPTY_STRING;

            String strDelay               = DomainConstants.EMPTY_STRING;

            String strAllDepartment       = DomainConstants.EMPTY_STRING;

            String strDepartment          = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask    = DomainConstants.EMPTY_STRING;

            String strUserDepartment          = DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

            strAllDepartment        = eElementProject.getAttribute("ALL_DEPARTMENT");

            strUserDepartment 		 = eElementProject.getAttribute("USER_DEPARTMENT");

              

              ArrayList<String> alDeparmentList     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

              int [] aryTaskCount = new int[alDeparmentList.size()];



              slUserDepartment = StringUtil.split(strUserDepartment, ",");

              

              NodeList nList = doc.getElementsByTagName("I");

              Node  nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement = (Element) nNode;

                      isCriticalTask   = (String) eElement.getAttribute("isCriticalTask");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      strDepartment           = (String) eElement.getAttribute("Department");

                      if(slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT))

                      {

	                      if("No".equalsIgnoreCase(isMasterProjectTask))

	                      {

		                      if("YES".equalsIgnoreCase(isCriticalTask))

		                      {

		                          strCurrent   = (String) eElement.getAttribute("Current"); 

		                          strDelay     = (String) eElement.getAttribute("Delay");

		                          

		                          

		                          if("Delayed".equalsIgnoreCase(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent) )

		                          {

		                              aryTaskCount[alDeparmentList.indexOf(strDepartment)]++; 

		                          }

		                          else if("Completed".equalsIgnoreCase(strTaskLegend) && "Complete".equals(strCurrent))

		                          {

		                              aryTaskCount[alDeparmentList.indexOf(strDepartment)]++; 

		                          }

		                          else if("Total".equalsIgnoreCase(strTaskLegend))

		                          {

		                              aryTaskCount[alDeparmentList.indexOf(strDepartment)]++;

		                          }

		                      }

	                      }

                      }                    

                      

                  }

              }

              

              for (int j = 0; j < alDeparmentList.size(); j++) {

                  strDepartment            =    (String)alDeparmentList.get(j);

                  int taskCount         =    aryTaskCount[alDeparmentList.indexOf(strDepartment)];

                  if(taskCount>0)

                  {

                      projectMap.put(strDepartment        , aryTaskCount[alDeparmentList.indexOf(strDepartment)]);

                  }

              }

              

        } catch (Exception e) {

            e.printStackTrace();

        }

        return projectMap;

    }




	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCriticalTaskDataByDept_ByDept(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList    =  new MapList();

        MapList mlReturnList1   =  new MapList();

        try {

            Map programMap                    = (Map) JPO.unpackArgs(args);

            String strObjectId                = (String) programMap.get("objectId");

            String strTaskLegend              = (String) programMap.get("taskLegend");

            String strSelectedDept            = (String) programMap.get("SelectData");

            File Projectfile                  = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

            Document doc                  = null;

            String isCriticalTask         = DomainConstants.EMPTY_STRING;

            String strCurrent             = DomainConstants.EMPTY_STRING;

            String strDelay               = DomainConstants.EMPTY_STRING;

            String strAllDepartment       = DomainConstants.EMPTY_STRING;

            String strDepartment          = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask    = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject       = (Element)  doc.getElementsByTagName("Body").item(0);

            HashMap ProjectMap            = getNodeAttributeValue(eElementProject);

            ArrayList<String> alParentIds = new ArrayList<String>();



            strAllDepartment      = eElementProject.getAttribute("ALL_DEPARTMENT");

            ArrayList<String> alDeparmentList     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

            int [] aryTaskCount = new int[alDeparmentList.size()];



            NodeList nList = doc.getElementsByTagName("I");

            Node  nNode =null;
			// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
            StringList slParentIds            = new StringList();
            StringList slDeptProjectIds       = new StringList();
            for (int temp = 0; temp < nList.getLength(); temp++) {

                nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement = (Element) nNode;

                      isCriticalTask   = (String) eElement.getAttribute("isCriticalTask");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      {

	                      if("YES".equalsIgnoreCase(isCriticalTask))

	                      {

	                          strCurrent   = (String) eElement.getAttribute("Current"); 

	                          strDelay     = (String) eElement.getAttribute("Delay");

	                          strDepartment           = (String) eElement.getAttribute("Department");

	                          HashMap  deleyMap    =    new HashMap();

	                          if(strSelectedDept.equals(strDepartment))

	                          {

	                              if("Delayed".equalsIgnoreCase(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent) )

	                              {

	                                  //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
                                      slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }

	                              else if("Completed".equalsIgnoreCase(strTaskLegend) && "Complete".equals(strCurrent))

	                              {

	                                  //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
	                                  slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }

	                              else if("Total".equalsIgnoreCase(strTaskLegend))

	                              {

	                                 //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
	                                  slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }
								// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End
								}
	                              if(!deleyMap.isEmpty())

	                              {

	                                     deleyMap.put("PersonType","Non-DB");

	                                     deleyMap.put("objectType","person");

	                                     mlreturnList.add(deleyMap);

	                              }

	                          

	                      }

                      }

                  }

              }

            

            // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
			/*Set<String> uniqueDepartmentList     =    new HashSet<String>(alParentIds) ;

            alParentIds.clear();

            alParentIds.addAll(uniqueDepartmentList);



            String strParentIds     =    DomainConstants.EMPTY_STRING;

            for (int i = 0; i < alParentIds.size(); i++) {

                if(i==0)

                {

                	strParentIds    =    alParentIds.get(i);

                }

                else

                {

                	strParentIds    += ","+    alParentIds.get(i);

                }

            }



              ProjectMap.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );

              ProjectMap.put("ProjectIds" , (String) ProjectMap.get("id"));

              ProjectMap.put("Summery" , "No");

              if(alParentIds.size()!=0)

              {

              mlReturnList1.add(ProjectMap);

              }*/
			  String strParentIds  = slParentIds.toString().replaceAll("\\[|\\]", "");
            for (int i = 0; i < nList.getLength(); i++) {
                nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String taskId   = (String) eElement.getAttribute("id");
                    if(slDeptProjectIds.contains(taskId)) {
                        HashMap ProjectMapEl = getNodeAttributeValue(eElement);
                        ProjectMapEl.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );
                        mlReturnList1.add(ProjectMapEl);
                    }
              }
            }
            // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList1;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getChildTaskForDEPT(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

            Map programMap              = (Map) JPO.unpackArgs(args);

            String strProjectId         = (String) programMap.get("relId");

            String strObjectId          = (String) programMap.get("parentId");

            String strTaskLegend        = (String) programMap.get("taskLegend");

            

            String strParentIds	= strProjectId.substring(strProjectId.indexOf(':')+1, strProjectId.length());

            List alParentIds    = Arrays.asList(strParentIds.split(","));

            

            strProjectId        = strProjectId.substring(0,strProjectId.indexOf(":"));

            File Projectfile            = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            String strClickIds           = (String) programMap.get("objectId");

            String strLevel              = (String) programMap.get("level");

            Node nNode                   = null;

            NodeList childNodeList       = null;
			// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
            /*if(strLevel.equals("0,0"))

            {

                Element eElementProject  = (Element)  doc.getElementsByTagName("B").item(0);

                childNodeList            = eElementProject.getChildNodes();

            }

            else

            {*/

               NodeList nList            = doc.getElementsByTagName("I");

               String strTaskId          = DomainConstants.EMPTY_STRING;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement  = (Element) nNode;

                       strTaskId         = (String) eElement.getAttribute("TaskId");

                       if(strClickIds.equals(strTaskId))

                       {

                          childNodeList  =   eElement.getChildNodes();

                          break;

                       }

                   }

               }

            //}

            

            //Map dataMap    =   null;

            

           

            for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode != null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement  = (Element) nNode;

                    Map dataMap       = getNodeAttributeValue(eElement);

                    

                    // if(alParentIds.contains((String) dataMap.get("TaskId")))
                    if(strParentIds.contains((String) dataMap.get("TaskId"))) 
                    // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

                    {

                    	

	                    dataMap.put("PersonType","Non-DB");

	                    dataMap.put("objectType","person");

	                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

	                    dataMap.put("Id",(String) dataMap.get("TaskId"));

	                    dataMap.put("id",(String) dataMap.get("TaskId"));

	                    dataMap.put("relId",strProjectId);

	                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId+":"+strParentIds);

	                    dataMap.put("hadChildren","true");

	                    mlReturnList.add(dataMap);

                    }

                }

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

       

        return mlReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getChildTaskForDEPT1(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

            Map programMap              = (Map) JPO.unpackArgs(args);

            

            String strProjectId         = (String) programMap.get("relId");

            String strObjectId          = (String) programMap.get("objectId");

            //String strParentIds         = (String) programMap.get("parentId");

            List alParentIds;

            String strTaskLegend        = (String) programMap.get("taskLegend");



            String strParentIds	=	strProjectId.substring(strProjectId.indexOf(':')+1, strProjectId.length());

            alParentIds     = Arrays.asList(strParentIds.split(","));

            strProjectId    = strProjectId.substring(0,strProjectId.indexOf(":"));



            

            

            File Projectfile            = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            String strClickIds           = (String) programMap.get("objectId");

            String strLevel              = (String) programMap.get("level");

            Node nNode                   = null;

            NodeList childNodeList       = null;

            if(strLevel.equals("0,0"))

            {

                Element eElementProject  = (Element)  doc.getElementsByTagName("B").item(0);

                childNodeList            = eElementProject.getChildNodes();

            }

            else

            {

               NodeList nList            = doc.getElementsByTagName("I");

               String strTaskId          = DomainConstants.EMPTY_STRING;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement  = (Element) nNode;

                       strTaskId         = (String) eElement.getAttribute("TaskId");

                       if(strClickIds.equals(strTaskId))

                       {

                          childNodeList  =   eElement.getChildNodes();

                          break;

                       }

                   }

               }

            }



            Map dataMap    =   null;

            for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    dataMap   =   getNodeAttributeValue(eElement);

                    if(alParentIds.contains((String) dataMap.get("id")))

                    {

	                    dataMap.put("objectId",(String) dataMap.get("TaskId"));

	                    dataMap.put("Id",(String) dataMap.get("TaskId"));

	                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId+":"+strParentIds);

                        mlReturnList.add(dataMap);

                    }

                }

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

        return mlReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getDelayedTaskDashboardData_ByDept(Context context , String [] args ) throws Exception

    {

        HashMap returnMap                 = new HashMap();

        MapList mlreturnList              = new MapList();

        HashMap projectMap                = new HashMap();

        try {

            Map programMap                 = (Map) JPO.unpackArgs(args);

            String strObjectId             = (String) programMap.get("objectId");

            String strDepartmentName       = (String) programMap.get("DepartmentName");

            String strMode               = (String) programMap.get("mode");

            File Projectfile   =   getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder       = dbFactory.newDocumentBuilder();

            Document doc                   = null;

            String strCurrent              = DomainConstants.EMPTY_STRING;

            String strDelay                = DomainConstants.EMPTY_STRING;

            String strDepartment           = DomainConstants.EMPTY_STRING;

            String strType                 = DomainConstants.EMPTY_STRING;

            String strLevel                = DomainConstants.EMPTY_STRING;

            String isLeafTask              = DomainConstants.EMPTY_STRING;



            int iTaskNotStart              =    0;

            int iTaskActive                =    0; 

            int iTaskReview                =    0;



            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

            String strAllDepartment        = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

			  

			   String strAttribute = "";

              if(strMode.equalsIgnoreCase("delayedTasks"))

              {

            	  //It is from Delayed % Task Chart.

            	  strAttribute = "Department";

              }

              else

              {

            	  strAttribute = "GateStatus";

              }

			  

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strType              = (String) eElement.getAttribute("TYPE");

                      strLevel             = (String) eElement.getAttribute("LEVEL");

                      strDepartment        = (String) eElement.getAttribute(strAttribute);

                      isLeafTask           = (String) eElement.getAttribute("Summery");

                      //if(!"Project Space".equalsIgnoreCase(strType) && !strLevel.equalsIgnoreCase("1") && strDepartmentName.equals(strDepartment) && !"Complete".equals(strCurrent))

                      if("Yes".equalsIgnoreCase(isLeafTask) && strDepartmentName.equals(strDepartment))

                      {

                          strDelay         = (String) eElement.getAttribute("Delay");

                          strCurrent       = (String) eElement.getAttribute("Current");

                          if("Yes".equalsIgnoreCase(strDelay) &&  !"Complete".equals(strCurrent)) {

                              if("Create".equals(strCurrent) ||  "Assign".equals(strCurrent)) {

                                  iTaskNotStart++; 

                              } else if("Active".equals(strCurrent)) {

                                  iTaskActive++;  

                              } else if("Review".equals(strCurrent)) {

                                  iTaskReview++;

                              }

                          }

                      }

                  }

              }

           projectMap.put("DelayedTasksNotStartedCount",iTaskNotStart);

           projectMap.put("DelayedTasksActiveCount",iTaskActive);

           projectMap.put("DelayedTasksReviewCount",iTaskReview);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return projectMap ;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDelayedTaskDashboardData_ByDeptData(Context context , String [] args ) throws Exception

    {

        HashMap returnMap                 = new HashMap();

        MapList mlreturnList             = new MapList();

        HashMap projectMap                 = new HashMap();

        HashMap dataMap                 = new HashMap(); 

        MapList mlReturnList1            = new MapList();

        try {

            Map programMap                 = (Map) JPO.unpackArgs(args);

            String strObjectId            = (String) programMap.get("objectId");

            String strDepartmentName     = (String) programMap.get("DepartmentName");

            String strSelectData         = (String) programMap.get("SelectData");

			String strMode               = (String) programMap.get("mode");

            File Projectfile             = getProjectFile(context, strObjectId);

            

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            String strCurrent            = DomainConstants.EMPTY_STRING;

            String strDelay              = DomainConstants.EMPTY_STRING;

            String strDepartment         = DomainConstants.EMPTY_STRING;

            String strType               = DomainConstants.EMPTY_STRING;

            String strLevel              = DomainConstants.EMPTY_STRING;

            String isLeafTask            = DomainConstants.EMPTY_STRING;



            int iTaskNotStart            =    0;

            int iTaskActive              =    0; 

            int iTaskReview              =    0;

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

            String strAllDepartment        = DomainConstants.EMPTY_STRING;

            

            

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject       = (Element)  doc.getElementsByTagName("Body").item(0);

            HashMap ProjectMap            = getNodeAttributeValue(eElementProject);

            ArrayList<String> alParentIds = new ArrayList<String>();

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

			  

			   String strAttribute = "";

              if(strMode.equalsIgnoreCase("delayedTasks"))

              {

            	  //It is from Delayed % Task Chart.

            	  strAttribute = "Department";

              }

              else

              {

            	  strAttribute = "GateStatus";

              }

				// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
				StringList slParentIds       = new StringList();
				StringList slDeptProjectIds  = new StringList();			  

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strType              = (String) eElement.getAttribute("TYPE");

                      strLevel             = (String) eElement.getAttribute("LEVEL");

                      strDepartment        =  (String) eElement.getAttribute(strAttribute);

                      isLeafTask           = (String) eElement.getAttribute("Summery");

                      //if(!"Project Space".equalsIgnoreCase(strType) && !strLevel.equalsIgnoreCase("1") && strDepartmentName.equals(strDepartment) && !"Complete".equals(strCurrent))

                      if("Yes".equalsIgnoreCase(isLeafTask) && strDepartmentName.equals(strDepartment))

                      {

                          strDelay         = (String) eElement.getAttribute("Delay");

                          strCurrent       = (String) eElement.getAttribute("Current");

                          HashMap deleyMap = new HashMap();

                          if("Yes".equalsIgnoreCase(strDelay) &&  !"Complete".equals(strCurrent)) { 

                              if(("Create".equals(strCurrent) ||  "Assign".equals(strCurrent) ) && "Tasks Not Started".equalsIgnoreCase(strSelectData)) {

                                  //deleyMap    =    getNodeAttributeValue(eElement);
                                  //alParentIds	=	getParentIds(eElement , alParentIds);
                                  slParentIds.add(eElement.getAttribute("id"));
                                  getParentIds(eElement , slParentIds, slDeptProjectIds);

                              } else if("Active".equals(strCurrent) && "Tasks In Progress".equalsIgnoreCase(strSelectData)) {

                                  //deleyMap      = getNodeAttributeValue(eElement);
                                  //alParentIds	= getParentIds(eElement , alParentIds);
                                  slParentIds.add(eElement.getAttribute("id"));
                                  getParentIds(eElement , slParentIds, slDeptProjectIds);

                              } else if("Review".equals(strCurrent) && "Tasks Under Approval".equalsIgnoreCase(strSelectData)) {

                                 //deleyMap    = getNodeAttributeValue(eElement);
                                  //alParentIds = getParentIds(eElement , alParentIds);
                                  slParentIds.add(eElement.getAttribute("id"));
                                  getParentIds(eElement , slParentIds, slDeptProjectIds);

                              }
 // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End
                          }

                         if(!deleyMap.isEmpty())

                         {

                             deleyMap.put("PersonType","Non-DB");

                             deleyMap.put("objectType","person");

                             mlreturnList.add(deleyMap);

                         }

                      }

                  }

              }

			   // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
              /*Set<String> uniqueDepartmentList     =    new HashSet<String>(alParentIds) ;

              alParentIds.clear();

              alParentIds.addAll(uniqueDepartmentList);

              

              String strParentIds     =    DomainConstants.EMPTY_STRING;

              for (int i = 0; i < alParentIds.size(); i++) {

                  if(i==0)

                  {

                     strParentIds    =    alParentIds.get(i);

                  }

                  else

                  {

                     strParentIds    += ","+    alParentIds.get(i);

                  }

              }

              ProjectMap.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );

              ProjectMap.put("ProjectIds" , (String) ProjectMap.get("id"));

              ProjectMap.put("Summery" , "No");

              if(alParentIds.size()!=0)

              {

              mlReturnList1.add(ProjectMap);

              }*/
			  String strParentIds  = slParentIds.toString().replaceAll("\\[|\\]", "");
            for (int iNode = 0; iNode < nList.getLength(); iNode++) {
                nNode = nList.item(iNode);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String taskId    = (String) eElement.getAttribute("id");
                    if(slDeptProjectIds.contains(taskId)) {
                        HashMap ProjectMapEl = getNodeAttributeValue(eElement);
                        ProjectMapEl.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );
                        mlReturnList1.add(ProjectMapEl);
              }
                }
            }
            // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList1;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public HashMap getNodeAttributeValue(Element eElement) 

    {

        HashMap returnValue    =    new HashMap();

        try {

            NamedNodeMap attrs = eElement.getAttributes();  

              for(int i = 0 ; i<attrs.getLength() ; i++) {

                Attr attribute = (Attr)attrs.item(i);  

                returnValue.put("" +attribute.getName()  , ""+attribute.getValue());

              }

            

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnValue;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public Vector  getDataColumn (Context context , String [] args) throws Exception

    {

        Vector returnVectore     = new Vector();

        try {

            HashMap programMap   = (HashMap)JPO.unpackArgs(args);

            Map columnMap        = (Map)programMap.get("columnMap");

            String strColumn     = (String)columnMap.get("name");

            MapList objList      = (MapList)programMap.get("objectList");

            Map data;

            

            Map paramList          = (Map)programMap.get("paramList");

            boolean isprinterFriendly = false;

            if(paramList.get("reportFormat") != null)

            {

                isprinterFriendly = true;

            }

            String strExport ="";

            for (int i = 0; i < objList.size(); i++) {

                data =(Map) objList.get(i);

                if(!isprinterFriendly)

                {

                	returnVectore.add(data.get(strColumn));

                }

                else

                {

                	strExport	=	(String)data.get(strColumn+"Export");

                	if(strExport!=null && !"".equals(strExport))

                	{

                		returnVectore.add(strExport);

                	}

                	else

                	{

                		returnVectore.add(data.get(strColumn));

                	}

                }

            }

            Vector columnVals      = new Vector(objList.size());

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnVectore;

    }

    @com.matrixone.apps.framework.ui.ProgramCallable

    public Vector  getDataColumnWBS (Context context , String [] args) throws Exception

    {

        Vector returnVectore     = new Vector();

        try {

            HashMap programMap   = (HashMap)JPO.unpackArgs(args);

            Map columnMap        = (Map)programMap.get("columnMap");

            String strColumn     = (String)columnMap.get("name");

            MapList objList      = (MapList)programMap.get("objectList");

            Map paramList          = (Map)programMap.get("paramList");

            boolean isprinterFriendly = false;

            if(paramList.get("reportFormat") != null)

            {

                isprinterFriendly = true;

            }

            Map data;

            String strName 		  = DomainConstants.EMPTY_STRING;

            StringBuffer	buffer = new StringBuffer();

            String strValue    =    "";

            for (int i = 0; i < objList.size(); i++) {

                data =(Map) objList.get(i);

                if(strColumn.equals("T"))

                {

                	strName	= (String)data.get("T");

                    if(!isprinterFriendly)

                    { 

                        strName = XSSUtil.encodeForXML(context, strName);

                        String isLeafTask    = (String) data.get("Summery");

                        String isCriticalTask   = (String) data.get("isCriticalTask");

                        String strTaskColor = "black";

                        if("YES".equalsIgnoreCase(isCriticalTask))

                            strTaskColor = "red";

                        buffer  = new StringBuffer();

                        buffer.append("<a href ='javascript:showModalDialog(\"");

                        buffer.append("../common/emxTree.jsp?objectId=");

                        buffer.append(data.get("id"));

                        buffer.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+strName+"\" style=\"color:"+strTaskColor+";\">");

                        if("No".equalsIgnoreCase(isLeafTask))

                        {

                             buffer.append("<b>");

                             buffer.append(strName);

                             buffer.append("</b>");

                        }

                        else 

                        {

                             buffer.append(strName);

                        }

                        buffer.append("</a>");

                        returnVectore.add(buffer.toString());

                    }

                    else

                    {

                        returnVectore.add(strName);

                    }

                }

                else

                {

                    strName =(String)data.get(strColumn);

                    if(strName==null)

                    {

                        strName="";

                    }

                    //strName = XSSUtil.encodeForXML(context, strName);

                    returnVectore.add(strName);

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnVectore;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public Vector  getDataColumnDefect (Context context , String [] args) throws Exception

    {

        Vector returnVectore       = new Vector();

        try {

            HashMap programMap     = (HashMap)JPO.unpackArgs(args);

            Map columnMap          = (Map)programMap.get("columnMap");

            Map paramList          = (Map)programMap.get("paramList");

            String strColumn       = (String)columnMap.get("name");

            String SelectedSource  = (String)paramList.get("SelectedSource");

            String TrailsNames     = (String)paramList.get("TrailsNames");

            String taskLegend      = (String)paramList.get("taskLegend");

            String objectId        = (String)paramList.get("objectId");

			String strHeader       = (String)paramList.get("header");

            String strSubHeader    = (String)paramList.get("subHeader");

            String strReportFormat = (String)paramList.get("reportFormat");

            MapList objList        = (MapList)programMap.get("objectList");

            Map data;

            String strDeptName = DomainConstants.EMPTY_STRING;

            Object strHref = null;

            for (int i = 0; i < objList.size(); i++) {

                data =(Map) objList.get(i);

                strDeptName    = (String)data.get("Name");

                strHref        = (String)data.get(strColumn);

                String strTotalClick = "Total".equals(strDeptName) ? "TotalClick=true&amp;" : "";
				// Added and Modifed by Dheeraj Garg <01-Aug-2016> Hinkai/SQR table no hyperlink on Open, Closed points -- Start
                String strSelColumn  = "Close".equals(strColumn) ? "Closed" : strColumn;
                strHeader = strHeader.replace("Total", strColumn).replace("Close", strColumn);
                //if("Total".equalsIgnoreCase(strColumn) && null == strReportFormat)
				if(("Total".equalsIgnoreCase(strColumn) || "Open".equalsIgnoreCase(strColumn) || "Close".equalsIgnoreCase(strColumn)) && null == strReportFormat && !"0".equals(strHref))

                    strHref    = "<a  title='Open Total Summary' href= \"javascript:showNonModalDialog('../common/emxIndentedTable.jsp?rowGrouping=false&amp;table=MSILDefectManagementData&amp;suiteKey=ProgramCentral&amp;" +

                                 "massPromoteDemote=false&amp;header="+strHeader+" for "+strDeptName+"&amp;subHeader="+strSubHeader+"&amp;program=MSILDefectManagementIntegration:getTrailDataDatailsByDeptTableData&amp;" +strTotalClick+

                                 "objectId="+objectId+"&amp;SelectedSource="+SelectedSource+"&amp;TrailsNames="+TrailsNames+"&amp;taskLegend="+strSelColumn+"&amp;DeptName="+strDeptName+"' ,700, 600, true) \">"+strHref+"</a>";

                    
                // Added and Modifed by Dheeraj Garg <01-Aug-2016> Hinkai/SQR table no hyperlink on Open, Closed points -- End

                if("Total".equals(strDeptName) && !"CSV".equalsIgnoreCase(strReportFormat))

                    strHref    = "<div style='font-weight: bold;background:#deefff;margin-top: -0px;margin-left: -5px;padding: 10px;width:100%'>"+ strHref +"</div>";

                

                strHref = "&nbsp;".equals(strHref) ? "" : strHref;

                returnVectore.add( strHref );

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnVectore;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMasterProjectSchedule(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        try {

            

            Map programMap                   = (Map) JPO.unpackArgs(args);

            String strObjectId               = (String) programMap.get("objectId");

            String strSelectedMilestone      = (String) programMap.get("milestone");

            StringList slSelectedMilestone   = FrameworkUtil.split(strSelectedMilestone, ",");

            

            int [] iTotalCount               = new int[slSelectedMilestone.size()];

            int [] iCloseCount               = new int[slSelectedMilestone.size()];

            int [] iDelayCount               = new int[slSelectedMilestone.size()];

            File Projectfile                 = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder         = dbFactory.newDocumentBuilder();

            Document doc                     = null;

            String IsEngineeringSchedule     = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask       = DomainConstants.EMPTY_STRING;

            NodeList childNodeList           = null;

            doc                              = dBuilder.parse(Projectfile);



            doc.getDocumentElement().normalize();

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement      = (Element) nNode;

                      IsEngineeringSchedule = (String) eElement.getAttribute("IsEngineeringSchedule");

                      isMasterProjectTask   = (String) eElement.getAttribute("isMasterProjectTask");

                      if("Yes".equalsIgnoreCase(IsEngineeringSchedule) &&  "Yes".equalsIgnoreCase(isMasterProjectTask))

                      {

                         childNodeList   =   eElement.getChildNodes();

                      }

                  }

              }

              ArrayList<String> listMountList 	= new ArrayList<String>();

              listMountList.add("Jan");

              listMountList.add("Feb");

              listMountList.add("Mar");

              listMountList.add("Apr");

              listMountList.add("May");

              listMountList.add("Jun");

              listMountList.add("Jul");

              listMountList.add("Aug");

              listMountList.add("Sep");

              listMountList.add("Oct");

              listMountList.add("Nov");

              listMountList.add("Dec");



              Map dataMap              =  null;

              if(childNodeList!=null)

              {

                 for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                     String strTaskLabelStyle =  "Green";

                     nNode = childNodeList.item(temp);

                     if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                         Element eElement     = (Element) nNode;

                         dataMap   =   getNodeAttributeValue(eElement);

                         if( (String)dataMap.get("T") != null  &&  !"Project Space".equals((String)dataMap.get("TYPE"))) {

	                         HashMap putMap   = new HashMap();

	                         putMap.put("TaskName",(String)dataMap.get("T"));

	                         putMap.put("Est Finish Date",(String)dataMap.get("E"));

	                         Date date = new Date((String)(String)dataMap.get("E"));

	                         int iDate 	=	date.getDate();

	                         

	                         SimpleDateFormat df = new SimpleDateFormat("yyyy");

	                         String year = df.format(date);

							//Edited by Yaseen..

	                         //date.getMonth()-1 was giving previous month

	                         if( iDate>0 &&  iDate<= 10)

	                         {

	                             putMap.put("Est Finish Date","Beg. "+listMountList.get(date.getMonth())+" "+year);

	                         }

	                         else if(iDate>10 &&  iDate<= 20 )

	                         {

	                             putMap.put("Est Finish Date","Mid. "+listMountList.get(date.getMonth())+" "+year);

	                         }

	                         else

	                         {

	                             putMap.put("Est Finish Date","End "+listMountList.get(date.getMonth())+" "+year);

	                         }

	                         

                             if("Complete".equalsIgnoreCase((String)dataMap.get("Current")))

                    		 {

                            	 strTaskLabelStyle = "Green";

                    		 }

	                         else if("Yes".equals(dataMap.get("Delay")) && !"Complete".equalsIgnoreCase((String)dataMap.get("Current")))

                             {

                                 strTaskLabelStyle = "Red";

                             }

                             else if("No".equals(dataMap.get("Delay")) && !"Complete".equalsIgnoreCase((String)dataMap.get("Current")))

                             {

                            	 strTaskLabelStyle = "white";

                             }

	                         putMap.put("TaskStyle",strTaskLabelStyle);

	                         mlReturnList.add(putMap);

	                     }

                     }

                 }

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }



    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getGateDocumentStatusForPEDashBoard(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        HashMap returnMap 	 = new HashMap();

        MapList mlreturnList = new MapList();

        try {

            

            Map programMap                   = (Map) JPO.unpackArgs(args);

            String strObjectId               = (String) programMap.get("objectId");

            String strSelectedMilestone      = (String) programMap.get("milestone");

            ArrayList<String> alAllGateDept	 = getALLGateDept(context );

             File[] listOfFiles                  = getAllProjectFile(context);

             DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

             DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

             Document doc                  = null;

             String isLeafTask             = DomainConstants.EMPTY_STRING;

             String strCurrent             = DomainConstants.EMPTY_STRING;

             String strDelay               = DomainConstants.EMPTY_STRING;

             String strAllDepartment       = DomainConstants.EMPTY_STRING;

             String strDepartment          = DomainConstants.EMPTY_STRING;



             //String iDeliverable         = DomainConstants.EMPTY_STRING;

             String strDepartmentName      = DomainConstants.EMPTY_STRING;



             int iCompleted                 = 0;

             int iDelay                     = 0;

             int iTotal                     = 0;



			 NodeList gateList              = null;

			 Node ngateListDocument         = null;

			 Element eGateElement           = null;



			 NodeList gateDeptList          = null;

			 Node nGateDeptDocument         = null;

			 Element eGateDeptElement       = null;

			 

			 NodeList gateDeptDeliverablesList         = null;

			 Node nGateDeptDeliverablesDocument        = null;

			 Element eGateDeptDeliverablesElement      = null;

			 

			 Node nNode                   = null;

			 Node nNodeChildGateDocument  = null;

			 

             ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

             String isGateSchedule	=	DomainConstants.EMPTY_STRING;

             

             String strParentObjectType = "";

             String strParentTaskType = "";

             String strType = "";

             String strTaskType = "";

             

             for (int i = 0; i < listOfFiles.length; i++) {

               if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                   doc = dBuilder.parse(listOfFiles[i]);

                   doc.getDocumentElement().normalize();

                   HashMap projectMap     =     new HashMap();



                   Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                   projectMap.put("ProjectId", eElementProject.getAttribute("id"));

                   projectMap.put("ProjectName", eElementProject.getAttribute("name"));

                   int [] intPlan                = new int[alAllGateDept.size()];

                   int [] intActual              = new int[alAllGateDept.size()];

                   

                   NodeList nList = doc.getElementsByTagName("I");

                   Element eElement;

                   ArrayList<String> slProjectDepartment	=	new ArrayList<String>();

                   for (int temp = 0; temp < nList.getLength(); temp++) {

                       nNode = nList.item(temp);

                       if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            eElement             = (Element) nNode;

                            isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                            if("Yes".equalsIgnoreCase(isGateSchedule))

                            {

                                gateList    = eElement.getChildNodes();

                                if(gateList!=null)

                                {

                              	  for (int temp1 = 0; temp1 < gateList.getLength(); temp1++) {

                              		ngateListDocument = gateList.item(temp1);

                              		  if (ngateListDocument.getNodeType() == Node.ELEMENT_NODE) {

                              			  	eGateElement       = (Element) ngateListDocument;

                              			    gateDeptList  	   = eGateElement.getChildNodes();

                              			  for (int igateDeptList = 0; igateDeptList < gateDeptList.getLength(); igateDeptList++) {

                              				  nGateDeptDocument = gateDeptList.item(igateDeptList);

                                      		  if (nGateDeptDocument.getNodeType() == Node.ELEMENT_NODE) {

                                      			  eGateDeptElement = (Element) nGateDeptDocument;

                                      			  strDepartmentName	        =	(String)eGateDeptElement.getAttribute("T");

												  

												   strParentObjectType = (String)eGateDeptElement.getAttribute("parentObjectType");

                                      			  strParentTaskType = (String)eGateDeptElement.getAttribute("parentTaskType");

                                      			  strType = (String)eGateDeptElement.getAttribute("TYPE");

                                      			  strTaskType = (String)eGateDeptElement.getAttribute("MSILTaskType");

                                      			  if(strType.equals(ProgramCentralConstants.TYPE_TASK) && strParentObjectType.equals(ProgramCentralConstants.TYPE_GATE)

                                      					  && strTaskType.equalsIgnoreCase("Gate Schedule") && strParentTaskType.equalsIgnoreCase("Gate Schedule"))

                                      			  {

                                      				  //Adding Only Department Task,

													  slProjectDepartment.add(strDepartmentName);

													  gateDeptDeliverablesList	=	eGateDeptElement.getChildNodes();

													  intPlan[alAllGateDept.indexOf(strDepartmentName)]+=gateDeptDeliverablesList.getLength();

													  for (int igateDeptDeliverablesList = 0; igateDeptDeliverablesList < gateDeptList.getLength(); igateDeptDeliverablesList++) 

													  {

														 nGateDeptDeliverablesDocument = gateDeptDeliverablesList.item(igateDeptDeliverablesList);

														if (nGateDeptDeliverablesDocument!=null && nGateDeptDeliverablesDocument.getNodeType() == Node.ELEMENT_NODE) {

															  eGateDeptDeliverablesElement = (Element) nGateDeptDeliverablesDocument;

															  String  iDeliverable           = (String) eGateDeptDeliverablesElement.getAttribute("Current");

															  if("Complete".equals(iDeliverable))

															  {

																  intActual[alAllGateDept.indexOf(strDepartmentName)]++;

															  }

															}

														}

													}

												}	

                                           }

                              		   }

                              	   }

                                }

                           }

                       }

                   }

                   

                   for (int j = 0; j < alAllGateDept.size(); j++) {

                       strDepartmentName        =    (String)alAllGateDept.get(j);

                       HashMap TaskCountMap     = new HashMap();

                       if(slProjectDepartment.contains(strDepartmentName))

                       {

                    	   TaskCountMap.put("Plan",  ""+intPlan[alAllGateDept.indexOf(strDepartmentName)]);

                    	   TaskCountMap.put("Actual",  ""+ intActual[alAllGateDept.indexOf(strDepartmentName)]);

                       }

                       else

                       {

                    	   TaskCountMap.put("Plan",  "");

                    	   TaskCountMap.put("Actual",  "");

                       }

                       projectMap.put(strDepartmentName        , TaskCountMap);

                   }

                   mlreturnList.add(projectMap);

                }

             }

           //Set<String> uniqueDepartmentList     =    new HashSet<String>(alAllProjectDepartment) ;

           //alAllProjectDepartment.clear();

           //alAllProjectDepartment.addAll(uniqueDepartmentList);

           returnMap.put("GateDeptNames", alAllGateDept);

           returnMap.put("GetData", mlreturnList);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public ArrayList<String> getALLGateDept(Context context )

    {

    	ArrayList<String> alReturnList 	=	new ArrayList<String>();

        try {

           File[] listOfFiles        = getAllProjectFile(context);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc             = null;

            String isGateSchedule 	 = DomainConstants.EMPTY_STRING;

            NodeList childNodeList   = null;

            NodeList DeptChildNodes  = null;

            Element eElement1        = null;

            Element eElement2        = null;

            

             NodeList gateList       	  = null;

			 Node ngateListDocument       = null;

			 Element eGateElement         = null;



			 NodeList gateDeptList      = null;

			 Node nGateDeptDocument     = null;

			 Element eGateDeptElement   = null;

			 

			 NodeList gateDeptDeliverablesList         = null;

			 Node nGateDeptDeliverablesDocument        = null;

			 Element eGateDeptDeliverablesElement      = null;

			 

			 Node nNode                   = null;

			 Node nNodeChildGateDocument  = null;

			 

            

			 String strParentObjectType = "";

             String strParentTaskType = "";

             String strType = "";

             String strTaskType = "";

            

            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();

                  NodeList nList = doc.getElementsByTagName("I");

                  Element eElement;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                           eElement             = (Element) nNode;

                           isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                           if("Yes".equalsIgnoreCase(isGateSchedule))

                           {

                               gateList    = eElement.getChildNodes();

                               if(gateList!=null)

                               {

                             	  for (int temp1 = 0; temp1 < gateList.getLength(); temp1++) {

                             		ngateListDocument = gateList.item(temp1);

                             		  if (ngateListDocument.getNodeType() == Node.ELEMENT_NODE) {

                             			  	eGateElement       = (Element) ngateListDocument;

                             			    gateDeptList  	   = eGateElement.getChildNodes();

                             			  for (int igateDeptList = 0; igateDeptList < gateDeptList.getLength(); igateDeptList++) {

                             				  nGateDeptDocument = gateDeptList.item(igateDeptList);



                                     		  if (nGateDeptDocument.getNodeType() == Node.ELEMENT_NODE) {

                                     			  eGateDeptElement = (Element) nGateDeptDocument;

												  												  

                                     			  strParentObjectType = (String)eGateDeptElement.getAttribute("parentObjectType");

                                     			  strParentTaskType = (String)eGateDeptElement.getAttribute("parentTaskType");

                                     			  strType = (String)eGateDeptElement.getAttribute("TYPE");

                                     			  strTaskType = (String)eGateDeptElement.getAttribute("MSILTaskType");

                                     			  

                                     			  if(strType.equals(ProgramCentralConstants.TYPE_TASK) && strParentObjectType.equals(ProgramCentralConstants.TYPE_GATE)

                                     					  && strTaskType.equalsIgnoreCase("Gate Schedule") && strParentTaskType.equalsIgnoreCase("Gate Schedule"))

                                     				  	alReturnList.add((String)eGateDeptElement.getAttribute("T"));

                                     		  }

                                          }

                             		   }

                             	   }

                               }

                               continue;

                          }

                      }

                  }

               }

            }

            Set<String> setGetDocumentDept     = new HashSet<String>(alReturnList);

            alReturnList.clear();
            
			// Modifed by Dheeraj Garg <11-Aug-2016> Order of the dept list should be in particular order -- Start
            // Modifed by Dheeraj Garg <05-Sep-2016> Moved list of departments to MSILConstants
            //alReturnList.addAll(setGetDocumentDept);
			for(String str: SORTED_ARRAY_DEPARTMENT_NAMES)
            	if(setGetDocumentDept.contains(str))
            		alReturnList.add(str);
            // Modifed by Dheeraj Garg <11-Aug-2016> Order of the dept list should be in particular order -- End

        }

        catch(Exception ex)

        {

        	ex.printStackTrace();

        }

        return alReturnList;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProjectMilestoneTasks(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        try {

            

            Map programMap                     = (Map) JPO.unpackArgs(args);

            String strObjectId                 = (String) programMap.get("objectId");

            String strSelectedMilestone        = (String) programMap.get("milestone");

            StringList slSelectedMilestone     = FrameworkUtil.split(strSelectedMilestone, ",");

            

            int [] iTotalCount                 =    new int[slSelectedMilestone.size()];

            int [] iCloseCount                 =    new int[slSelectedMilestone.size()];

            int [] iDelayCount                 =    new int[slSelectedMilestone.size()];

            File Projectfile   =   getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            String strMilestoneName      = DomainConstants.EMPTY_STRING;

            String strCurrent            = DomainConstants.EMPTY_STRING;
            String strDelay              = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask	 = DomainConstants.EMPTY_STRING;

            String strUserDepartment 	 = DomainConstants.EMPTY_STRING;

            String strDepartment  		= DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

            strUserDepartment = (String)eElementProject.getAttribute("USER_DEPARTMENT");

            slUserDepartment = StringUtil.split(strUserDepartment, ",");

            

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strMilestoneName     = (String) eElement.getAttribute("MilestoneName");

                      strCurrent           = (String) eElement.getAttribute("Current");

                      strDelay             = (String) eElement.getAttribute("Delay");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      strDepartment        = (String) eElement.getAttribute("Department");

                      

                      if(slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT))

                      {

	                      if("No".equalsIgnoreCase(isMasterProjectTask))

	                      {

		                      if(!"".equals(strMilestoneName) && slSelectedMilestone.contains(strMilestoneName) && "Yes".equalsIgnoreCase(strDelay))

		                      {

		                          iTotalCount[slSelectedMilestone.indexOf(strMilestoneName)]++;

		                          if("Complete".equals(strCurrent))

		                          {

		                              iCloseCount[slSelectedMilestone.indexOf(strMilestoneName)]++;  

		                          }

		                          else if("Yes".equalsIgnoreCase(strDelay))

		                          {

		                              iDelayCount[slSelectedMilestone.indexOf(strMilestoneName)]++;   

		                          }

		                      }

	                      }

                      }

                  }

              }

              int iTotalTaskList       = 0;

              int iDeliverableList     = 0;

              for (int j = 0; j < slSelectedMilestone.size(); j++) {

                  strMilestoneName         = (String)slSelectedMilestone.get(j);

                  HashMap TaskCountMap     = new HashMap();

                  TaskCountMap.put("MilestoneName",  strMilestoneName);

                  TaskCountMap.put("Total",  iTotalCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  TaskCountMap.put("Completed",  iCloseCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  TaskCountMap.put("Delayed", iDelayCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  mlReturnList.add(TaskCountMap);

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap  getProjectMilestoneTasks_byDept(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                 = new MapList();

        HashMap returnMap                    = new HashMap();

        try {

            

            Map programMap                     = (Map) JPO.unpackArgs(args);

            String strObjectId                = (String) programMap.get("objectId");

            String strTaskLegend              = (String) programMap.get("taskLegend");

            String strSelectedMilestoneName    = (String) programMap.get("MilestoneId");

            File Projectfile   =   getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;



            String strMilestoneName          = DomainConstants.EMPTY_STRING;

            String strCurrent                 = DomainConstants.EMPTY_STRING;

            String strDelay                      = DomainConstants.EMPTY_STRING;

            String strDepartment             = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask	=	DomainConstants.EMPTY_STRING;

            String strUserDepartment 	=	DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            

            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

            //returnMap.put("Name", eElementProject.getAttribute("name"));

            String strAllDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

            strUserDepartment = eElementProject.getAttribute("USER_DEPARTMENT");

            slUserDepartment = StringUtil.split(strUserDepartment, ",");

            

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

            int []  iDeptTotal = new int[alAllProjectDepartment.size()];

            

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strMilestoneName  = (String) eElement.getAttribute("MilestoneName");

                      strCurrent           = (String) eElement.getAttribute("Current");

                      strDelay           = (String) eElement.getAttribute("Delay");

                      strDepartment           = (String) eElement.getAttribute("Department");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask) && (slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT)))

                      {

                      if(strSelectedMilestoneName.equals(strMilestoneName) && "Yes".equalsIgnoreCase(strDelay))

                      {

                          if("Completed".equals(strTaskLegend) && "Complete".equals(strCurrent))

                          {

                              iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;

                          }

                          else if("Delayed".equals(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent)) 

                          {

                              iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;   

                          }

                          else if("Total".equals(strTaskLegend))

                          {

                              iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;

                          }

                      }

                  }

              }

              }

  

              for (int j = 0; j < alAllProjectDepartment.size(); j++) {

                  String strDepartmentName        =    (String)alAllProjectDepartment.get(j);

                  if(iDeptTotal[alAllProjectDepartment.indexOf(strDepartmentName)] >0)

                  returnMap.put(strDepartmentName , iDeptTotal[alAllProjectDepartment.indexOf(strDepartmentName)]);

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getProjectMilestoneTasks_byDeptData(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                  = new MapList();

        MapList mlReturnList1 = new MapList();

        try {

            Map programMap                    = (Map) JPO.unpackArgs(args);

            String strObjectId                = (String) programMap.get("objectId");

            String strTaskLegend              = (String) programMap.get("taskLegend");

            String strSelectedMilestoneName   = (String) programMap.get("MilestoneId");

            String strSelectData              = (String) programMap.get("SelectData");

            File Projectfile                  = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;



            String strMilestoneName           = DomainConstants.EMPTY_STRING;

            String strCurrent                 = DomainConstants.EMPTY_STRING;

            String strDelay                   = DomainConstants.EMPTY_STRING;

            String strDepartment              = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask		  = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject           = (Element)  doc.getElementsByTagName("Body").item(0);

            HashMap ProjectMap                = getNodeAttributeValue(eElementProject);

            ArrayList<String> alParentIds     = new ArrayList<String>();

            String strAllDepartment           = eElementProject.getAttribute("ALL_DEPARTMENT");

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

            int []  iDeptTotal                = new int[alAllProjectDepartment.size()];

            
// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
            StringList slParentIds            = new StringList();
            StringList slDeptProjectIds       = new StringList();
              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement      = (Element) nNode;

                      strMilestoneName      = (String) eElement.getAttribute("MilestoneName");

                      strCurrent            = (String) eElement.getAttribute("Current");

                      strDelay              = (String) eElement.getAttribute("Delay");

                      strDepartment         = (String) eElement.getAttribute("Department");

                      HashMap  deleyMap     =   new HashMap();

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      {

                      if(strSelectedMilestoneName.equals(strMilestoneName) && strSelectData.equals(strDepartment) && "Yes".equalsIgnoreCase(strDelay))

                      {

                          

                          if("Completed".equals(strTaskLegend) && "Complete".equals(strCurrent)) 

                          {

                             //deleyMap    =    getNodeAttributeValue(eElement);
                              //alParentIds	=	getParentIds(eElement , alParentIds);
                              slParentIds.add(eElement.getAttribute("id"));
                              getParentIds(eElement , slParentIds, slDeptProjectIds);



                           }

                          else if("Delayed".equals(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent)) 

                          {

                             //deleyMap    =    getNodeAttributeValue(eElement);
                              //alParentIds	=	getParentIds(eElement , alParentIds);
                              slParentIds.add(eElement.getAttribute("id"));
                              getParentIds(eElement , slParentIds, slDeptProjectIds);



                          }

                          else if("Total".equalsIgnoreCase(strTaskLegend))

                          {

                              //deleyMap    =    getNodeAttributeValue(eElement);
                              //alParentIds	=	getParentIds(eElement , alParentIds);
                              slParentIds.add(eElement.getAttribute("id"));
                              getParentIds(eElement , slParentIds, slDeptProjectIds);

                          }
 // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End
                      }

                      if(!deleyMap.isEmpty())

                      {

                             deleyMap.put("PersonType","Non-DB");

                             deleyMap.put("objectType","person");

                             mlReturnList.add(deleyMap);

                      }

                  }

              }

              }

               // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
			   /*Set<String> uniqueDepartmentList     =    new HashSet<String>(alParentIds) ;

              alParentIds.clear();

              alParentIds.addAll(uniqueDepartmentList);

              String strParentIds     =    DomainConstants.EMPTY_STRING;

              for (int i = 0; i < alParentIds.size(); i++) {

                  if(i==0)

                  {

                     strParentIds    =    alParentIds.get(i);

                  }

                  else

                  {

                     strParentIds    += ","+    alParentIds.get(i);

                  }

              }

              ProjectMap.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );

              ProjectMap.put("ProjectIds" , (String) ProjectMap.get("id"));

              ProjectMap.put("Summery" , "No");

              if(alParentIds.size()!=0)

              {

              mlReturnList1.add(ProjectMap);

              }*/
			  String strParentIds  = slParentIds.toString().replaceAll("\\[|\\]", "");
            for (int iNode = 0; iNode < nList.getLength(); iNode++) {
                nNode = nList.item(iNode);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String taskId   = (String) eElement.getAttribute("id");
                    if(slDeptProjectIds.contains(taskId)) {
                        HashMap ProjectMapEl = getNodeAttributeValue(eElement);
                        ProjectMapEl.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );
                        mlReturnList1.add(ProjectMapEl);
                    }
              }
            }
            // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList1;

    }

    

    @com.matrixone.apps.framework.ui.ProgramCallable

    public MapList  getProjectMilestoneCriticalTasks(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        try {

            

            Map programMap                     = (Map) JPO.unpackArgs(args);

            String strObjectId                 = (String) programMap.get("objectId");

            String strSelectedMilestone        = (String) programMap.get("milestone");

            StringList slSelectedMilestone     = FrameworkUtil.split(strSelectedMilestone, ",");

            int [] iTotalCount                 = new int[slSelectedMilestone.size()];

            int [] iCloseCount                 = new int[slSelectedMilestone.size()];

            int [] iDelayCount                 = new int[slSelectedMilestone.size()];

            File Projectfile                   = getProjectFile(context, strObjectId);



            DocumentBuilderFactory dbFactory   = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder           = dbFactory.newDocumentBuilder();

            Document doc                       = null;

            String strMilestoneName            = DomainConstants.EMPTY_STRING;

            String strCurrent                  = DomainConstants.EMPTY_STRING;

            String strDelay                    = DomainConstants.EMPTY_STRING;

            String isCriticalTask              = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask         = DomainConstants.EMPTY_STRING;

            String strUserDepartment 		   = DomainConstants.EMPTY_STRING;

            String strDepartment 		   = DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            

           Element eElementProject = (Element) doc.getElementsByTagName("Body").item(0);

           strUserDepartment =  (String)eElementProject.getAttribute("USER_DEPARTMENT");

           slUserDepartment = StringUtil.split(strUserDepartment, ","); 

           

            NodeList nList = doc.getElementsByTagName("I");

            Node nNode     = null;

              for (int temp  = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strMilestoneName     = (String) eElement.getAttribute("MilestoneName");

                      strCurrent           = (String) eElement.getAttribute("Current");

                      strDelay             = (String) eElement.getAttribute("Delay");

                      isCriticalTask       = (String) eElement.getAttribute("isCriticalTask");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      strDepartment        = (String) eElement.getAttribute("Department");

                      if("No".equalsIgnoreCase(isMasterProjectTask) && (slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT)))

                      {

	                      if("YES".equalsIgnoreCase(isCriticalTask) && "Yes".equalsIgnoreCase(strDelay))

	                      {

	                          if(!"".equals(strMilestoneName) && slSelectedMilestone.contains(strMilestoneName))

	                          {

	                              iTotalCount[slSelectedMilestone.indexOf(strMilestoneName)]++;

	                              if("Complete".equals(strCurrent))

	                              {

	                                  iCloseCount[slSelectedMilestone.indexOf(strMilestoneName)]++;  

	                              }

	                              else

	                              {

	                                  iDelayCount[slSelectedMilestone.indexOf(strMilestoneName)]++;

	                              }

	                          }

	                      }

                      }

                  }

              }

              int iTotalTaskList       =    0;

              int iDeliverableList     =    0;

              for (int j = 0; j < slSelectedMilestone.size(); j++) {

                  strMilestoneName        =    (String)slSelectedMilestone.get(j);

                  HashMap TaskCountMap    = new HashMap();

                  TaskCountMap.put("MilestoneName" ,  strMilestoneName);

                  TaskCountMap.put("Total"         ,  iTotalCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  TaskCountMap.put("Completed"     ,  iCloseCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  TaskCountMap.put("Delayed"       , iDelayCount[slSelectedMilestone.indexOf(strMilestoneName)]);

                  mlReturnList.add(TaskCountMap);

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap  getProjectMilestoneCriticalTasks_byDept(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                   = new MapList();

        HashMap returnMap                      = new HashMap();

        try {

            

            Map programMap                     = (Map) JPO.unpackArgs(args);

            String strObjectId                 = (String) programMap.get("objectId");

            String strTaskLegend               = (String) programMap.get("taskLegend");

            String strSelectedMilestoneName    = (String) programMap.get("MilestoneId");

            

            File Projectfile                   = getProjectFile(context, strObjectId);



            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;



            String strMilestoneName           = DomainConstants.EMPTY_STRING;

            String strCurrent                 = DomainConstants.EMPTY_STRING;

            String strDelay                   = DomainConstants.EMPTY_STRING;

            String strDepartment              = DomainConstants.EMPTY_STRING;

            String isCriticalTask             = DomainConstants.EMPTY_STRING;

            String  isMasterProjectTask       = DomainConstants.EMPTY_STRING;

            String strUserDepartment 		  = DomainConstants.EMPTY_STRING;

            

            StringList slUserDepartment = new StringList();

            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject           = (Element)  doc.getElementsByTagName("Body").item(0);

            String strAllDepartment           = eElementProject.getAttribute("ALL_DEPARTMENT");

            strUserDepartment = eElementProject.getAttribute("USER_DEPARTMENT");

            slUserDepartment = StringUtil.split(strUserDepartment, ",");

            

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

            int []  iDeptTotal = new int[alAllProjectDepartment.size()];

            

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement       = (Element) nNode;

                      strMilestoneName       = (String) eElement.getAttribute("MilestoneName");

                      strCurrent             = (String) eElement.getAttribute("Current");

                      strDelay               = (String) eElement.getAttribute("Delay");

                      strDepartment          = (String) eElement.getAttribute("Department");

                      isCriticalTask         = (String) eElement.getAttribute("isCriticalTask");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask) && (slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT)))

                      {

	                      if("YES".equalsIgnoreCase(isCriticalTask) && "Yes".equalsIgnoreCase(strDelay)) 

	                      {

	                          if(strSelectedMilestoneName.equals(strMilestoneName) )

	                          {

	                              

	                              if("Completed".equals(strTaskLegend) && "Complete".equals(strCurrent)) 

	                              {

	                                  iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;   

	                               }

	                              else if("Delayed".equals(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent)) 

	                              {

	                                  iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;   

	                              }

	                              else if("Total".equals(strTaskLegend))

	                              {

	                                  iDeptTotal[alAllProjectDepartment.indexOf(strDepartment)]++;

	                              }

	                          }

	                      }

                      }

                  }

              }

  

              for (int j = 0; j < alAllProjectDepartment.size(); j++) {

                  String strDepartmentName        =    (String)alAllProjectDepartment.get(j);

                  if(iDeptTotal[alAllProjectDepartment.indexOf(strDepartmentName)] >0)

                  returnMap.put(strDepartmentName , iDeptTotal[alAllProjectDepartment.indexOf(strDepartmentName)]);

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getProjectMilestoneCriticalTasks_byDeptData(Context context , String [] args)throws Exception 

    {

    	MapList mlReturnList1	= new MapList();

        MapList mlReturnList                 = new MapList();

        try {

            Map programMap                     = (Map) JPO.unpackArgs(args);

            String strObjectId                 = (String) programMap.get("objectId");

            String strTaskLegend               = (String) programMap.get("taskLegend");

            String strSelectedMilestoneName    = (String) programMap.get("MilestoneId");

            String strSelectData               = (String) programMap.get("SelectData");

            File Projectfile                   = getProjectFile(context, strObjectId);



            DocumentBuilderFactory dbFactory   = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder           = dbFactory.newDocumentBuilder();

            Document doc                       = null;



            String strMilestoneName            = DomainConstants.EMPTY_STRING;

            String strCurrent                  = DomainConstants.EMPTY_STRING;

            String strDelay                    = DomainConstants.EMPTY_STRING;

            String strDepartment               = DomainConstants.EMPTY_STRING;

            String isCriticalTask              = DomainConstants.EMPTY_STRING;

            String isMasterProjectTask        = DomainConstants.EMPTY_STRING;



            

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject  = (Element)  doc.getElementsByTagName("Body").item(0);

            HashMap ProjectMap            = getNodeAttributeValue(eElementProject);

            ArrayList<String> alParentIds = new ArrayList<String>();

            String strAllDepartment  = eElementProject.getAttribute("ALL_DEPARTMENT");

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));
            int []  iDeptTotal       = new int[alAllProjectDepartment.size()];
			// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
            StringList slParentIds            = new StringList();
            StringList slDeptProjectIds       = new StringList();
            NodeList nList           = doc.getElementsByTagName("I");

            Node nNode               = null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strMilestoneName     = (String) eElement.getAttribute("MilestoneName");

                      strCurrent           = (String) eElement.getAttribute("Current");

                      strDelay             = (String) eElement.getAttribute("Delay");

                      strDepartment        = (String) eElement.getAttribute("Department");

                      isCriticalTask       = (String) eElement.getAttribute("isCriticalTask");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      {

	                      HashMap  deleyMap    = new HashMap();

	                      if("YES".equalsIgnoreCase(isCriticalTask) && strSelectData.equals(strDepartment) &&  "Yes".equalsIgnoreCase(strDelay))

	                      {

	                          if(strSelectedMilestoneName.equals(strMilestoneName))

	                          {

	                              if("Completed".equals(strTaskLegend) && "Complete".equals(strCurrent)) 

	                              {

	                                 //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
                                      slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }

	                              else if("Delayed".equals(strTaskLegend) && "Yes".equalsIgnoreCase(strDelay) && !"Complete".equals(strCurrent)) {

	                                  //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
                                      slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }

	                              else if("Total".equals(strTaskLegend) ) { 

	                                  //deleyMap    =    getNodeAttributeValue(eElement);
	                                  //alParentIds	=	getParentIds(eElement , alParentIds);
                                      slParentIds.add(eElement.getAttribute("id"));
                                      getParentIds(eElement , slParentIds, slDeptProjectIds);

	                              }
// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End
	                          }

	                      }

	                      if(!deleyMap.isEmpty())

	                      {

	                          deleyMap.put("PersonType","Non-DB");

	                          deleyMap.put("objectType","person");

	                          mlReturnList.add(deleyMap);

	                      }

                      }

                  }

              }
			   // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
              /*Set<String> uniqueDepartmentList     =    new HashSet<String>(alParentIds) ;

              alParentIds.clear();

              alParentIds.addAll(uniqueDepartmentList);

              

              String strParentIds     =    DomainConstants.EMPTY_STRING;

              for (int i = 0; i < alParentIds.size(); i++) {

                  if(i==0)

                  {

                     strParentIds    =    alParentIds.get(i);

                  }

                  else

                  {

                     strParentIds    += ","+    alParentIds.get(i);

                  }

              }

              ProjectMap.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );

              ProjectMap.put("ProjectIds" , (String) ProjectMap.get("id"));

              ProjectMap.put("Summery" , "No");

              if(alParentIds.size()!=0)

              {

              mlReturnList1.add(ProjectMap);

              }*/
			  String strParentIds  = slParentIds.toString().replaceAll("\\[|\\]", "");
            for (int iNode = 0; iNode < nList.getLength(); iNode++) {
                nNode = nList.item(iNode);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String taskId   = (String) eElement.getAttribute("id");
                    if(slDeptProjectIds.contains(taskId)) {
                        HashMap ProjectMapEl = getNodeAttributeValue(eElement);
                        ProjectMapEl.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );
                        mlReturnList1.add(ProjectMapEl);
                    }
                }
              }
            // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList1;

    }

    

    @com.matrixone.apps.framework.ui.ProgramCallable
// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
    //public ArrayList getParentIds(Element eElement , ArrayList<String> alParentIds )
	public void getParentIds(Element eElement , StringList slParentIds, StringList slDeptProjectIds)

    {

    	//alParentIds.add(eElement.getAttribute("id"));

    	try {

    		Element parentNode	=	 (Element) eElement.getParentNode();

    		/* if(!"B".equals(parentNode.getNodeName()))

    		 {

    			 alParentIds.add(parentNode.getAttribute("id"));

    			 getParentIds(parentNode , alParentIds);

    		 }*/
			 if("No".equals(parentNode.getAttribute("isMasterProjectTask")))
            {
                String strParentTaskId = parentNode.getAttribute("id");
                if(!slParentIds.contains(strParentTaskId))
                    slParentIds.add(strParentTaskId);
                getParentIds(parentNode , slParentIds, slDeptProjectIds);

                if("Project Space".equals(parentNode.getAttribute("TYPE")) && !slDeptProjectIds.contains(strParentTaskId))
                    slDeptProjectIds.add(strParentTaskId);
            }

		} catch (Exception e) {

			

		}

    	//return alParentIds;

    }
// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

	 @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getWBSData(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

           Map programMap               = (Map) JPO.unpackArgs(args);

            String strObjectId          = (String) programMap.get("objectId");

          

            File Projectfile            =   getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Element eElementProject      = (Element) doc.getElementsByTagName("Body").item(0);

            HashMap projectMap           = getNodeAttributeValue(eElementProject); 

            projectMap.put( DomainRelationship.SELECT_ID , (String) projectMap.get("id"));

            projectMap.put("ProjectIds" , (String) projectMap.get("id"));

            projectMap.put("Summery" , "No");

            mlReturnList.add(projectMap);

        } catch(Exception ex) {

           ex.printStackTrace();

        }

        return mlReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getChildTask(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

            Map programMap              = (Map) JPO.unpackArgs(args);

            String strProjectId         = (String) programMap.get("relId");

            String strObjectId          = (String) programMap.get("parentId");

            String strTaskLegend        = (String) programMap.get("taskLegend");
            File Projectfile            = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            String strClickIds           = (String) programMap.get("objectId");

            String strLevel              = (String) programMap.get("level");

            Node nNode                   = null;

            NodeList childNodeList       = null;

            if(strLevel.equals("0,0"))

            {

                Element eElementProject  = (Element)  doc.getElementsByTagName("B").item(0);

                childNodeList            = eElementProject.getChildNodes();

            }

            else

            {

               NodeList nList            = doc.getElementsByTagName("I");

               String strTaskId          = DomainConstants.EMPTY_STRING;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement  = (Element) nNode;

                       strTaskId         = (String) eElement.getAttribute("TaskId");

                       if(strClickIds.equals(strTaskId))

                       {

                          childNodeList  =   eElement.getChildNodes();

                          break;

                       }

                   }

               }

            }



            Map dataMap    =   null;

            for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    dataMap   =   getNodeAttributeValue(eElement);

                    dataMap.put("PersonType","Non-DB");

                    dataMap.put("objectType","person");

                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

                    dataMap.put("Id",(String) dataMap.get("TaskId"));

                    dataMap.put("id",(String) dataMap.get("TaskId"));

                    dataMap.put("relId",strProjectId);

                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId);

                    dataMap.put("hadChildren","true");

                    mlReturnList.add(dataMap);

                }

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

        return mlReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTaskDocumetnData(Context context , String [] args)

    {

       MapList mlReturnList = new MapList ();

       try {

             Map programMap                = (Map) JPO.unpackArgs(args);

               String strObjectId          = (String) programMap.get("objectId");

               String strSelectedDeptName  = (String) programMap.get("departmentName");

               File Projectfile                  = getProjectFile(context, strObjectId);

               DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

               DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

               Document doc                 = null;

               doc                          = dBuilder.parse(Projectfile);

               doc.getDocumentElement().normalize();

               

               String isLeafTask         =   DomainConstants.EMPTY_STRING;

               String strDepartment      =   DomainConstants.EMPTY_STRING;

               String iDeliverable       =   DomainConstants.EMPTY_STRING;

               String strDocumentName    =   DomainConstants.EMPTY_STRING;

               String strDocumentTitle   =   DomainConstants.EMPTY_STRING;

               String strDocumentId      =   DomainConstants.EMPTY_STRING;

               String isMasterProjectTask=   DomainConstants.EMPTY_STRING;

               //String strTaskDepartment   =   DomainConstants.EMPTY_STRING;

               

               StringBuffer sbDocumentName   = new StringBuffer();

                 StringBuffer sbDocumentTitle= new StringBuffer();

               StringBuffer sbDocumentTitleExport= new StringBuffer();

               StringBuffer sbDocumentNameExport = new StringBuffer();



               NodeList nList = doc.getElementsByTagName("I");

                Node nNode =null;

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    nNode = nList.item(temp);

                    HashMap dataMap = new HashMap();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement     = (Element) nNode;

                        isLeafTask           = (String) eElement.getAttribute("Summery");

                        if("Yes".equalsIgnoreCase(isLeafTask))

                        {

                            strDepartment           = (String) eElement.getAttribute("Department");

                            if(strSelectedDeptName.equals(strDepartment))

                            {

                               

                               iDeliverable           = (String) eElement.getAttribute("Deliverable");

                               isMasterProjectTask    = (String) eElement.getAttribute("isMasterProjectTask");

                               if(iDeliverable!=null && !"".equals(iDeliverable) && !"Yes".equalsIgnoreCase(isMasterProjectTask))

                               {

                                   try {

                                      

                                     int iDeliverableCount    =    Integer.parseInt(iDeliverable);

                                     if(iDeliverableCount>0)

                                     {

                                        dataMap.put("Name", (String) eElement.getAttribute("T"));

                                        dataMap.put("Id", (String) eElement.getAttribute("TaskId"));

                                        dataMap.put("DocumentTotal", iDeliverable );

                                        NodeList childNodeList      =   eElement.getChildNodes();

                                        sbDocumentName.delete(0, sbDocumentName.length());

                                        sbDocumentTitle.delete(0, sbDocumentTitle.length());

                                         sbDocumentTitleExport= new StringBuffer();

                                         sbDocumentNameExport = new StringBuffer();

                                        for (int temp1 = 0; temp1 < childNodeList.getLength(); temp1++) {

                                             nNode = childNodeList.item(temp1);

                                             if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                                                 Element eElement1     = (Element) nNode;

                                                 strDocumentName   =   eElement1.getAttribute("Name");

                                                 strDocumentId      =   eElement1.getAttribute("Id");

                                                 strDocumentTitle   =   eElement1.getAttribute("Title");

                                                 sbDocumentName.append("<a href ='javascript:showModalDialog(\"");

                                                 sbDocumentName.append("../common/emxTree.jsp?objectId=");

                                                 sbDocumentName.append(strDocumentId);

                                                 sbDocumentName.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+strDocumentName+"\" style=\"color:red;"+""+"\">");

                                                 sbDocumentName.append("<img src=\"../common/images/iconSmallDocument.gif\" border=\"0\" />");

                                                 sbDocumentName.append(strDocumentName);

                                                 sbDocumentName.append("</a>");

                                                 sbDocumentName.append("<br></br>");

                                                 

                                                 sbDocumentTitle.append(strDocumentTitle);

                                                 sbDocumentTitle.append(";<br></br>");

                                                 

                                                 if(sbDocumentTitleExport.length()==0)

                                                 {

                                                	 sbDocumentTitleExport.append(strDocumentTitle);

                                                 }

                                                 else

                                                 {

                                                	 sbDocumentTitleExport.append(";"+strDocumentTitle);

                                                 }

                                                 

                                                 if(sbDocumentNameExport.length()==0)

                                                 {

                                                	 sbDocumentNameExport.append(strDocumentName);

                                                 }

                                                 else

                                                 {

                                                	 sbDocumentNameExport.append(";"+strDocumentName);

                                                 }

                                             }

                                         }

                                        dataMap.put("DocumentName", sbDocumentName.toString());

                                        dataMap.put("DocumentTitle", sbDocumentTitle.toString());

                                        dataMap.put("DocumentNameExport", sbDocumentNameExport.toString());

                                        dataMap.put("DocumentTitleExport", sbDocumentTitleExport.toString());

                                        ArrayList<String> alParentNames	= getParentTaskName(eElement , new ArrayList<String>());

                                        String strParentName 	=	getParentArrayListToStr(alParentNames,"arrow");

                                        dataMap.put("ParentName", strParentName );

                                        dataMap.put("ParentNameExport", getParentArrayListToStr(alParentNames,"") );

                                        

                                        

                                        mlReturnList.add(dataMap);

                                     }

                                 } catch (Exception e) {

                                     e.printStackTrace();

                                 }

                               }

                            }

                        }

                    }

                }

         

      } catch (Exception e) {

         e.printStackTrace();

      }

      return mlReturnList;

    }
	@com.matrixone.apps.framework.ui.ProgramCallable
   public String  getParentArrayListToStr(ArrayList<String> alParentNames , String strArrow)

   {

	   String strReturnString=	DomainConstants.EMPTY_STRING;

	   try {

		   if(alParentNames.size()==0)

		   {

			   strReturnString=DomainConstants.EMPTY_STRING;

		   }

		   else

		   {

			   for (int i = alParentNames.size()-1 ; i >= 0; i--) {

				   if(i == alParentNames.size()-1)

				   {

					   strReturnString=alParentNames.get(i);

				   }

				   else

				   {

					   if("arrow".equalsIgnoreCase(strArrow))
					   {

						   strReturnString += "<span style='font-size:18px;' ><b>&#x2192;</b></span>" + alParentNames.get(i);

					   }

					   else

					   {

						   strReturnString += "-->" + alParentNames.get(i);

					   }

				   }

			   }

		   }

	} catch (Exception e) {

		e.printStackTrace();

	}

	   return strReturnString;

   }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public ArrayList<String> getParentTaskName(Element eElement, ArrayList<String> buffer) throws Exception 

    {

        try {

            if(eElement != null)

            {

                Element parentNode     = (Element) eElement.getParentNode();

                String strType	=	parentNode.getAttribute("TYPE"); 

                if("Project Space".equals(strType))

                {

                	return buffer;

                }

                else

                {

                    String strParentName = parentNode.getAttribute("T");

                    buffer.add(strParentName);

                    getParentTaskName(parentNode,buffer);

                    

                }

               

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return buffer;

     }

    

		@com.matrixone.apps.framework.ui.ProgramCallable

    public MapList getTaskDocumetnTaskData(Context context , String [] args)

    {

       MapList mlReturnList = new MapList ();

       try {

             Map programMap                = (Map) JPO.unpackArgs(args);

               String strObjectId          = (String) programMap.get("objectId");

               String strSelectedDeptName  = (String) programMap.get("departmentName");

               File Projectfile                  = getProjectFile(context, strObjectId);

               DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

               DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

               Document doc                 = null;

               doc                          = dBuilder.parse(Projectfile);

               doc.getDocumentElement().normalize();

               

               String isLeafTask         =   DomainConstants.EMPTY_STRING;

               String strDepartment      =   DomainConstants.EMPTY_STRING;



               NodeList nList = doc.getElementsByTagName("I");

                Node nNode =null;

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    nNode = nList.item(temp);

                    HashMap dataMap = new HashMap();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement     = (Element) nNode;

                        isLeafTask           = (String) eElement.getAttribute("Summery");

                        if("Yes".equalsIgnoreCase(isLeafTask))

                        {

                            strDepartment           = (String) eElement.getAttribute("Department");

                            if(strSelectedDeptName.equals(strDepartment))

                            {

                            	mlReturnList.add(getNodeAttributeValue(eElement));

                            }

                        }

                    }

                }

      } catch (Exception e) {

         e.printStackTrace();

      }

      return mlReturnList;

    }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTaskDocumetnTaskDataForWBS(Context context , String [] args)

    {

    	MapList mlReturnList1	=	 new MapList();

       MapList mlReturnList = new MapList ();

       try {

             Map programMap                = (Map) JPO.unpackArgs(args);

               String strObjectId          = (String) programMap.get("objectId");

               String strSelectedDeptName  = (String) programMap.get("departmentName");

               File Projectfile                  = getProjectFile(context, strObjectId);

               DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

               DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

               Document doc                 = null;

               doc                          = dBuilder.parse(Projectfile);

               doc.getDocumentElement().normalize();

               

               Element eElementProject       = (Element)  doc.getElementsByTagName("Body").item(0);

               HashMap ProjectMap            = getNodeAttributeValue(eElementProject);

               ArrayList<String> alParentIds = new ArrayList<String>();

               

               

               String isLeafTask         =   DomainConstants.EMPTY_STRING;

               String strDepartment      =   DomainConstants.EMPTY_STRING;

               String isMasterProjectTask = DomainConstants.EMPTY_STRING;
			   // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
			   StringList slParentIds           = new StringList();
				StringList slDeptProjectIds       = new StringList();
				// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End


               NodeList nList = doc.getElementsByTagName("I");

                Node nNode =null;

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    nNode = nList.item(temp);

                    HashMap dataMap = new HashMap();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement     = (Element) nNode;

                        isLeafTask           = (String) eElement.getAttribute("Summery");

                        isMasterProjectTask           = (String) eElement.getAttribute("isMasterProjectTask");

                        if("Yes".equalsIgnoreCase(isLeafTask) && !"Yes".equalsIgnoreCase(isMasterProjectTask))

                        {

                            strDepartment           = (String) eElement.getAttribute("Department");

                            if(strSelectedDeptName.equals(strDepartment))

                            {

                            	 // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
                         //mlReturnList.add(getNodeAttributeValue(eElement));
                         //alParentIds = getParentIds(eElement , alParentIds);
                         slParentIds.add(eElement.getAttribute("id"));
                         getParentIds(eElement , slParentIds, slDeptProjectIds);
                         // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

                            }

                        }

                    }

                }

				// Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- Start
                /*Set<String> uniqueDepartmentList     =    new HashSet<String>(alParentIds) ;

                alParentIds.clear();

                alParentIds.addAll(uniqueDepartmentList);

                String strParentIds     =    DomainConstants.EMPTY_STRING;

                for (int i = 0; i < alParentIds.size(); i++) {

                    if(i==0)

                    {

                       strParentIds    =    alParentIds.get(i);

                    }

                    else

                    {

                       strParentIds    += ","+    alParentIds.get(i);

                    }

                }

                ProjectMap.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );

                ProjectMap.put("ProjectIds" , (String) ProjectMap.get("id"));

                ProjectMap.put("Summery" , "No");

                if(alParentIds.size()!=0)

                {

                mlReturnList1.add(ProjectMap);

                }*/
				String strParentIds  = slParentIds.toString().replaceAll("\\[|\\]", "");
             for (int iNode = 0; iNode < nList.getLength(); iNode++) {
                 nNode = nList.item(iNode);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                     Element eElement = (Element) nNode;
                     String taskId   = (String) eElement.getAttribute("id");
                     if(slDeptProjectIds.contains(taskId)) {
                         HashMap ProjectMapEl = getNodeAttributeValue(eElement);
                         ProjectMapEl.put( DomainRelationship.SELECT_ID , (String) ProjectMap.get("id")+":"+ strParentIds );
                         mlReturnList1.add(ProjectMapEl);
                     }
                 }
                }
             // Modifed by Dheeraj Garg <20-Jul-2016> WBS table that opens on any bar graph/pie graph should directly open the Dept schedule and not the master project -- End

      } catch (Exception e) {

         e.printStackTrace();

      }

      return mlReturnList1;

    }




	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProjectForGenerateDashboard(Context context , String [] args ) throws Exception

    {

        MapList mlreturnList             =     new MapList();

        try {

            File[] listOfFiles                  = getAllProjectFile(context);

            DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            String strMilestonesName     = DomainConstants.EMPTY_STRING;



            StringList slProjectName     = new StringList();

            StringList strProjectIds     = new StringList();

            ArrayList<String> alProjectMilestones  = new  ArrayList<String>();



            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();



                  Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                  slProjectName.add(eElementProject.getAttribute("id"));

                  strProjectIds.add(eElementProject.getAttribute("name"));



                  int iTotal     = 0;

				   //Edited By Yaseen..

                  //Getting the Milestones from the node milestoneObject..

                  NodeList nList = doc.getElementsByTagName("MilestoneObject");

                  Node nNode =null;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                          Element eElement     = (Element) nNode;

                          strMilestonesName    = (String) eElement.getAttribute("milestoneName");

                          if(strMilestonesName != null && !"".equals(strMilestonesName) && !alProjectMilestones.contains(strMilestonesName))

                          {

                              alProjectMilestones.add(strMilestonesName);

                          }

                      }

                  }

              }

           }



            

            //Set<String> setMilestones     = new HashSet<String>(alProjectMilestones);

            //alProjectMilestones.clear();

           // alProjectMilestones.addAll(setMilestones);



            StringList slMilestones       = new StringList();

            slMilestones.addAll(alProjectMilestones);

            

            

            //String attrStatus  = PropertyUtil.getSchemaProperty(context,"attribute_MSILMilestone");

           // slMilestones       = FrameworkUtil.getRanges(context, attrStatus);



            HashMap mapMilestonesSelect  = new HashMap();

            mapMilestonesSelect.put("field_choices", slMilestones);

            mapMilestonesSelect.put("field_display_choices", slMilestones);

            HashMap projectSelect        = new HashMap();

            projectSelect.put("field_choices", slProjectName);

            projectSelect.put("field_display_choices", strProjectIds );

            mlreturnList.add(projectSelect);

            mlreturnList.add(mapMilestonesSelect);

            

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlreturnList;

    }

    


	@com.matrixone.apps.framework.ui.ProgramCallable
    public File[] getAllProjectFile(Context context)

    {

       File[] listOfFiles = null;

       try {

                   String strPathMQL      = MqlUtil.mqlCommand(context,"print person '"+context.getUser()+"' select property[Dashboard_Path]  dump |");

                   //System.out.println("strPathMQL "+strPathMQL);

                   if(null != strPathMQL && strPathMQL.length() > 0)

					{



                       

                   strPathMQL               = strPathMQL.substring(strPathMQL.indexOf("value") +6 ,strPathMQL.length() );

                   strPathMQL=strPathMQL.trim();

                   File folder               = new File(strPathMQL);

                   

                   listOfFiles            = folder.listFiles();

                    }

      } catch (Exception e) {

         e.printStackTrace();

      }

       return listOfFiles ;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public File getProjectFile(Context context , String StrProjectId)

    {

       File Projectfile   =    null;

       try {

            String strPathMQL       = MqlUtil.mqlCommand(context,"print person '"+context.getUser()+"' select property[Dashboard_Path]  dump |");

            if(null != strPathMQL && strPathMQL.length() > 0)

            {

                strPathMQL              = strPathMQL.substring(strPathMQL.indexOf("value") +6 ,strPathMQL.length() );

                //System.out.println("strPathMQL  "+strPathMQL);

                strPathMQL=strPathMQL.trim();
                String strFileName      = "Project"+StrProjectId.replaceAll("\\.", "_")+".xml";

                 //Projectfile          = new File("E:\\WorkSpace\\Nikesh\\"+strFileName);

                //Projectfile             = new File(strPathMQL+"/"+strFileName);
				Projectfile             = new File(strPathMQL+"/"+strFileName);

            }

      } catch (Exception e) {

         e.printStackTrace();

      }

       return Projectfile;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProblemFaced(Context context, String strParentOID)throws Exception

    {

    	Pattern relPattern             = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);

        Pattern typePattern            = new Pattern(DomainConstants.TYPE_PROJECT_MANAGEMENT);

        MapList mlProblemFaced         = new MapList();

        MapList mlCheckMasterProject   = new MapList();

        MapList mlProSpaceObjects      = new MapList();

        MapList mlAllProblemFaced      = new MapList();

        Map mpAllProSpace              = null;

		Boolean bCheckMasterPrj        = false;

		MapList mlWithEditFlag= new MapList();

		String strMasterProjectOwner   = "";

		String strProjectOwner         = "";

		String strUser                 = context.getUser();

		String strPALProblemFacedobj   = "";

		DomainObject doProblemFaced    = DomainObject.newInstance(context);

		DomainObject doPALProblemFaced = DomainObject.newInstance(context);

		DomainObject doMasterPrj       = DomainObject.newInstance(context);

		DomainObject domProject        = DomainObject.newInstance(context);

		

        try

        {

            //HashMap programMap = (HashMap)JPO.unpackArgs(args);

            //String strParentOID = (String)programMap.get("parentOID");

        	//String strParentOID = (String)programMap.get("parentOID");

        	

            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

            objectSelects.add(DomainConstants.SELECT_NAME); 

			objectSelects.add(DomainConstants.SELECT_OWNER);

			objectSelects.add(DomainConstants.SELECT_DESCRIPTION);

			objectSelects.add("attribute[MSILProjectDepartment].value");

			objectSelects.add("attribute[MSIL Milestone].value");

			objectSelects.add("attribute[MSIL Temporary Counter Measure].value");

			objectSelects.add("attribute[MSIL Long Term Counter Measure].value");

			objectSelects.add("attribute[MSIL Learning].value");

			objectSelects.add("attribute[MSIL Learning].value");

			objectSelects.add("attribute[MSIL Checksheet].value");

			objectSelects.add("attribute[MSIL Update Read Check].value");

			objectSelects.add("attribute[MSIL If Update Reqd].value");

			objectSelects.add("attribute[MSIL Way Ahead].value");

			objectSelects.add("from[MSIL Problem Faced Project].to.owner");

            /*Checking Whether it is a  Mastert Project Or Dept Project

            *If mlCheckMasterProject size is zero then it is MP else it dept project.

            */

            if(null != strParentOID && !"null".equals(strParentOID) && !"".equals(strParentOID))

            {

            	DomainObject doProjectSpace = new DomainObject(strParentOID);

                bCheckMasterPrj = doProjectSpace.hasRelatedObjects(context, ProgramCentralConstants.RELATIONSHIP_SUBTASK, false);

                if (bCheckMasterPrj==false) 

                {

					doMasterPrj.setId(strParentOID);

					MapList mlPALProblemFacedobjList = (MapList)doMasterPrj.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_MASTER_PROJECT,TYPE_MSIL_PAL_PROBLEM_FACED,objectSelects,null,true,false,(short)1,"",null);

					if(mlPALProblemFacedobjList.size() != 0){

						Map mpPALProblemFacedobj =(Map)mlPALProblemFacedobjList.get(0);

						strPALProblemFacedobj=(String) mpPALProblemFacedobj.get(DomainConstants.SELECT_ID);

						doPALProblemFaced = new DomainObject(strPALProblemFacedobj);

						mlAllProblemFaced = doPALProblemFaced.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_PROBLEM_FACED,TYPE_MSIL_PROBLEM_FACED,objectSelects,null,true,false,(short)1,null,null);

					}

                }

                else

                {

				 if(null != strParentOID && !"null".equals(strParentOID) && !"".equals(strParentOID))

	               {

	                    //domProject.setId(strParentOID);

						//Map mpMasterProjects = (Map)getMasterProjectsMap(context,strParentOID);

					

						//strMasterProjectOwner=(String)mpMasterProjects.get("MasterProjectOwner");

						

	                    //strProjectOwner =(String) domProject.getInfo(context,DomainConstants.SELECT_OWNER);

	               }

					

				

                    /*

                    *Dept Project    

                    */

                    mlAllProblemFaced = doProjectSpace.getRelatedObjects(context,RELATIONSHIP_PROBLEM_FACED_PROJECT,TYPE_MSIL_PROBLEM_FACED,objectSelects,null,true,false,(short)1,null,null);

					

					

					// adding edit functionality start

				

				for(Object obj : mlAllProblemFaced)

                {	

					Map mpEditFalgMap = new HashMap();

					Map mpTempProjLearningObj = (Map) obj;

                    String strOwner =(String) mpTempProjLearningObj.get(DomainConstants.SELECT_OWNER);

					boolean bFlag = false;

					if(strMasterProjectOwner.equals(strUser) )

                    {   bFlag= true;

						mpEditFalgMap=mpTempProjLearningObj;

						mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                     else if(strProjectOwner.equals(strUser) )

                    {	bFlag= true;

						mpEditFalgMap=mpTempProjLearningObj;

						mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                    else if(strUser.equals(strOwner))

                    {	bFlag= true;

						mpEditFalgMap=mpTempProjLearningObj;

						mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    else

                    {	bFlag= false;

						mpEditFalgMap=mpTempProjLearningObj;

						mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                }

				// adding edit functionality End

					

                }

            }

        }

        catch(Exception e)

        {

            e.printStackTrace();

        }

		

		if (bCheckMasterPrj==false) 

			return mlAllProblemFaced;

		else

			return mlWithEditFlag;

    }



		@com.matrixone.apps.framework.ui.ProgramCallable

        public HashMap getProblamFacedGrapicalData(Context context , String [] args)throws Exception 

    {

        HashMap mapReturn = new HashMap();

        HashMap mpMyReturnMap = new HashMap();

        try {

            Map programMap                      = (Map) JPO.unpackArgs(args);

            String strObjectId                  = (String) programMap.get("objectId");

            String strLeadResponsibilty 	= (String) programMap.get("LeadResponsibilty");

			String strUserDepartment        = (String) programMap.get("UserDepartments");

            File Projectfile                    = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder            = dbFactory.newDocumentBuilder();

            Document doc                        = null;

            ArrayList<String> alDepartmentName  = new ArrayList<String>();

            ArrayList<String> alMilestoneName   = new ArrayList<String>();



			doc = dBuilder.parse(Projectfile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("ProblemFacedObj");

			NodeList nlMyList = doc.getElementsByTagName("ProblemFacedObj");

			Node nNode 	   = null;

			Node nMyNode = null;

			

			String strParentPrjOwner = DomainConstants.EMPTY_STRING;

			String strDept= DomainConstants.EMPTY_STRING;



			for (int temp  = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			         // alDepartmentName.add(((String) eElement.getAttribute("Dept")).trim());

			          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

			          strDept = (String) eElement.getAttribute("Dept").trim();

			          if(strLeadResponsibilty.equals("Department"))

			          {			

			        	 if(strUserDepartment.equals(strDept))

			        	  {

			        		  alDepartmentName.add(((String) eElement.getAttribute("Dept")).trim());	

			        	  }

			          }

			          else if(strLeadResponsibilty.equals("Business Unit"))

			          {

			        	  //Business Unit Head..

			        	  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

			        	  if(slDepartmentList.contains(strDept))			        		  

							alDepartmentName.add(((String) eElement.getAttribute("Dept")).trim());

			          }

			          else

			          {

				          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

		        		  {	        			

				        	  alDepartmentName.add(((String) eElement.getAttribute("Dept")).trim());	        			

		        		  }

			          }

			   }

			}

			Set<String> alDepartmentSet  =  new HashSet<String>(alDepartmentName);

			alDepartmentName.clear();

			alDepartmentName.addAll(alDepartmentSet);



			for (int temp = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

			          

			          if(strLeadResponsibilty.equals("Department"))

			          {		

			        	  strDept = (String) eElement.getAttribute("Dept").trim();

			        	  if(strUserDepartment.equals(strDept))

			        	  {

			        		  alMilestoneName.add(((String) eElement.getAttribute("Name")).trim());	  

			        	  }

			          }

			          else if(strLeadResponsibilty.equals("Business Unit"))

			          {

			        	  //Business Unit Head...

			        	  strDept = (String) eElement.getAttribute("Dept").trim();

			        	  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

			        	  if(slDepartmentList.contains(strDept))

			        		  alMilestoneName.add(((String) eElement.getAttribute("Name")).trim());	     

			          }

			          else

			          {

				          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

		        		  {	        			

				        	  alMilestoneName.add(((String) eElement.getAttribute("Name")).trim());	        			

		        		  }

			          }

			      //    alMilestoneName.add(((String) eElement.getAttribute("Name")).trim());

		      }

		   }

		   Set<String> alMilestoneSet  =  new HashSet<String>(alMilestoneName);

		   alMilestoneName.clear();

		   alMilestoneName.addAll(alMilestoneSet);



		   String strMilestoneNames   = DomainConstants.EMPTY_STRING;

		   String strDeptNames        = DomainConstants.EMPTY_STRING;

		   

		   int [] [] deptData         = new int [alMilestoneName.size()] [alDepartmentName.size()]; 



		   for (int temp = 0; temp < nList.getLength(); temp++) {

		      nNode = nList.item(temp);

		      if (nNode.getNodeType() == Node.ELEMENT_NODE)

		      {

		          Element eElement     = (Element) nNode;

		          strMilestoneNames    = (String) eElement.getAttribute("Name");

		          strDeptNames         = (String) eElement.getAttribute("Dept");

		          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

		          if(strMilestoneNames != null && strDeptNames != null )

		          {

		        	  if(strLeadResponsibilty.equals("Department") && strDeptNames.equals(strUserDepartment))

		        	  {

		        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		        	  else if(strLeadResponsibilty.equals("Business Unit"))

		        	  {

		        		  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");			        	  

			        	  if(slDepartmentList.contains(strDeptNames))

			        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		        	  else if(strParentPrjOwner.equals(context.getUser()))

		        	  {

		        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		          }

		      }

		   }

		  

		   MapList mlReturnData     = new MapList();

		   for (int i = 0; i < alMilestoneName.size(); i++) {

			   strMilestoneNames    = (String) alMilestoneName.get(i);

			   HashMap mapProject   = new HashMap();

			   mapProject.put("Name", strMilestoneNames);

			   for (int j = 0; j < deptData[i].length; j++) {

				   strDeptNames     = alDepartmentName.get(j);

				   mapProject.put(strDeptNames, deptData[i][j]);

			   }

			   mlReturnData.add(mapProject);

		   }

		   mapReturn.put("Name", mlReturnData);

		   mapReturn.put("Dept", alDepartmentName);



		   		  

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mapReturn;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getProblamFaces_byDeptData(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                  = new MapList();

        try {

	            Map programMap                    = (Map) JPO.unpackArgs(args);

	            String strObjectId                = (String) programMap.get("MasterProjectID");

	            String strSelectedDeptName		  = (String) programMap.get("DepartmentName");

	            String strSelectedProblamName     = (String) programMap.get("ProbalmFacedName");

	            File Projectfile   				  = getProjectFile(context, strObjectId);

	

	            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

	            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

	            Document doc                      = null;

	

	            String strProblamName             = DomainConstants.EMPTY_STRING;

	            String strDepartmentName          = DomainConstants.EMPTY_STRING;

	            doc = dBuilder.parse(Projectfile);

	            doc.getDocumentElement().normalize();



	            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

	            //returnMap.put("Name", eElementProject.getAttribute("name"));

	            String strAllDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

	            

	            

	            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

	            int []  iDeptTotal = new int[alAllProjectDepartment.size()];

            

              NodeList nList = doc.getElementsByTagName("ProblemFacedObj");

              Node nNode     = null;

              for (int temp  = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strProblamName       = (String) eElement.getAttribute("Name");

                      strDepartmentName    = (String) eElement.getAttribute("Dept");

                      HashMap  deleyMap    = new HashMap();

                      if(strSelectedProblamName.equals(strProblamName) && strSelectedDeptName.equals(strDepartmentName))

                      {

                              deleyMap    =    getNodeAttributeValue(eElement);

                              mlReturnList.add(deleyMap);

                      }

                  }

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    

    /// Code For ECN Graph 

    /**

     * Used To get ECN

     *

     * @param context the eMatrix <code>Context</code> object

     * @param args    holds ParamMap

     * @return        HashMap

     * @throws        Exception if the operation fails

     **/

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProjectSpaceECN(Context context, String strProjectSpaceId)

            throws Exception {

        MapList mlECN = new MapList();

        //HashMap programMap = (HashMap) JPO.unpackArgs(args);

       // String strProjectSpaceId = (String) programMap.get("objectId");

        DomainObject doMasterPrj = DomainObject.newInstance(context);

        StringList objectSelects = new StringList();

        MapList mlWithEditFlag= new MapList();

        String strMasterProjectOwner = "";

        String strProjectOwner = "";

        DomainObject domProject = DomainObject.newInstance(context);

        String strUser = context.getUser();

        

        try {

            // Expand Object Logic Statrt

          

            objectSelects.add(DomainConstants.SELECT_ID);

            objectSelects.add(DomainConstants.SELECT_NAME);

            objectSelects.add(DomainConstants.SELECT_TYPE);

            objectSelects.add(DomainConstants.SELECT_CURRENT);

            objectSelects.add(DomainConstants.SELECT_OWNER);

            objectSelects.add("attribute[MSIL ECN Issue Date].value");

            objectSelects.add("attribute[MSILProjectDepartment].value");

            objectSelects.add("attribute[MSIL Part Assembly].value");

            objectSelects.add("attribute[MSIL ECN Number].value");

            objectSelects.add("attribute[MSIL ECN Details].value");

            objectSelects.add("attribute[MSIL Milestone].value");

            objectSelects.add("attribute[MSIL ECN Cutoff Date].value");

            objectSelects.add("attribute[MSIL Cost].value");

            objectSelects.add("attribute[MSIL Quality].value");

            objectSelects.add("attribute[MSIL Timeline].value");

            objectSelects.add("attribute[MSIL Feasibility Check].value");

            objectSelects.add("attribute[MSIL Cut off Chassis/Engine No].value");

            objectSelects.add("attribute[MSIL Status of ECN].value");

            objectSelects.add("attribute[MSIL Implementation Date].value");

            objectSelects.add("attribute[MSIL Remarks].value");

         // Modifed by Dheeraj Garg <09-Aug-2016> Blank ECN graph. -- Start
			objectSelects.add("from[MSIL Project Space ECN].to.owner");
			// Modifed by Dheeraj Garg <09-Aug-2016> Blank ECN graph. -- End



            boolean getFrom = true;

            boolean getTo = false;

            short recurseToLevel = 0;

            String strBusWhere = "";

            String strRelWhere = "";

            Pattern typePattern = new Pattern(

                    DomainConstants.TYPE_PROJECT_MANAGEMENT);

            Pattern relPattern = new Pattern(

                    DomainConstants.RELATIONSHIP_SUBTASK);

            relPattern

                    .addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);

            relPattern

                    .addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);

            Pattern includeType = new Pattern(

                    DomainConstants.TYPE_PROJECT_SPACE);

            Pattern includeRelationship = new Pattern(

                    DomainConstants.RELATIONSHIP_SUBTASK);

            Map includeMap = new HashMap();

            MapList mlProjectECNList = new MapList();

            MapList mlDeptProjectList = new MapList();

            if(null != strProjectSpaceId && !"null".equals(strProjectSpaceId) && !"".equals(strProjectSpaceId))

            {

                DomainObject doProjectSpace = DomainObject.newInstance(context,

                    strProjectSpaceId);

               if (true) {

                    doMasterPrj.setId(strProjectSpaceId);

                    MapList mlPALECNobjList = (MapList)doMasterPrj.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_MASTER_PROJECT,TYPE_MSIL_PAL_ECN,objectSelects,null,true,false,(short)1,"",null);

                    if(mlPALECNobjList.size() != 0){

                        Map mpPALECNobj =(Map)mlPALECNobjList.get(0);

                        String strPALECNobj=(String) mpPALECNobj.get(DomainConstants.SELECT_ID);

                        DomainObject doPALECN = new DomainObject(strPALECNobj);

                        mlECN = doPALECN.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_ECN,TYPE_MSIL_ECN,objectSelects,null,true,false,(short)1,null,null);

                    }

               }

               else {

                    Map mpMasterProjects = new HashMap();// (Map)getMasterProjectsMap(context,strProjectSpaceId);

        

                    strMasterProjectOwner =(String)mpMasterProjects.get("MasterProjectOwner");

                    

                    mlECN = (MapList)doProjectSpace.getRelatedObjects(context,

                            RELATIONSHIP_PROJECT_SPACE_ECN,TYPE_MSIL_ECN, objectSelects,

                            null, true, false, (short) 1, null, null,(short)0);

                

                    // adding edit functionality start

                    domProject.setId(strProjectSpaceId);

                    strProjectOwner =(String) domProject.getInfo(context,DomainConstants.SELECT_OWNER);

                

                

                 for(Object obj : mlECN)

                {	

                    Map mpEditFalgMap = new HashMap();

                    Map mpTempProjLearningObj = (Map) obj;

                    String strOwner =(String) mpTempProjLearningObj.get(DomainConstants.SELECT_OWNER);

            

                    boolean bFlag = false;

                    if(strMasterProjectOwner.equals(strUser) )

                    {   bFlag= true;

                        mpEditFalgMap=mpTempProjLearningObj;

                        mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                     else if(strProjectOwner.equals(strUser) )

                    {	bFlag= true;

                        mpEditFalgMap=mpTempProjLearningObj;

                        mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                    else if(strUser.equals(strOwner))

                    {	bFlag= true;

                        mpEditFalgMap=mpTempProjLearningObj;

                        mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    else

                    {	bFlag= false;

                        mpEditFalgMap=mpTempProjLearningObj;

                        mpEditFalgMap.put("Flag",bFlag);

                        mlWithEditFlag.add(mpEditFalgMap);

                    }

                    

                }

                // adding edit functionality End

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        if (true) 

            return mlECN;

        else

            return mlWithEditFlag;

    }




	@com.matrixone.apps.framework.ui.ProgramCallable
   public HashMap getECNGrapicalData(Context context , String [] args)throws Exception 

    {

        HashMap mapReturn = new HashMap();

        try {

            Map programMap                      = (Map) JPO.unpackArgs(args);

            String strObjectId                  = (String) programMap.get("objectId");

            File Projectfile                    = getProjectFile(context, strObjectId);

            String strLeadResponsibilty 	= (String) programMap.get("LeadResponsibilty");

			String strUserDepartment        = (String) programMap.get("UserDepartments");

            DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder            = dbFactory.newDocumentBuilder();

            Document doc                        = null;

            ArrayList<String> alDepartmentName  = new ArrayList<String>();

            ArrayList<String> alMilestoneName   = new ArrayList<String>();



			doc = dBuilder.parse(Projectfile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("ECNObj");

			Node nNode 	   = null;

			

			String strParentPrjOwner = DomainConstants.EMPTY_STRING;

			String strDept= DomainConstants.EMPTY_STRING;

			

			for (int temp  = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			          //alDepartmentName.add(((String) eElement.getAttribute("ECNDepartment")).trim());

			          

			          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

			          if(strLeadResponsibilty.equals("Department"))

			          {			

			        	  strDept = (String) eElement.getAttribute("ECNDepartment").trim();

			        	  if(strUserDepartment.equals(strDept))

			        	  {

			        		  alDepartmentName.add(((String) eElement.getAttribute("ECNDepartment")).trim());

			        	  }

			          }

			          else if(strLeadResponsibilty.equals("Business Unit"))

			          {

			        	  //Business Unit Head..

						  strDept = (String) eElement.getAttribute("ECNDepartment").trim();

						  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

			        	  if(slDepartmentList.contains(strDept))			        		  

							alDepartmentName.add(((String) eElement.getAttribute("ECNDepartment")).trim());

			          }

			          else

			          {

				          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

		        		  {	        			

				        	  alDepartmentName.add(((String) eElement.getAttribute("ECNDepartment")).trim());      			

		        		  }

			          }

			   }

			}

			Set<String> alDepartmentSet  =  new HashSet<String>(alDepartmentName);

			alDepartmentName.clear();

			alDepartmentName.addAll(alDepartmentSet);



			for (int temp = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

			          if(strLeadResponsibilty.equals("Department"))

			          {		

			        	  strDept = (String) eElement.getAttribute("ECNDepartment").trim();

			        	  if(strUserDepartment.equals(strDept))

			        	  {

			        		  alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());  

			        	  }

			          }

			          else if(strLeadResponsibilty.equals("Business Unit"))

			          {

			        	  //Business Unit Head...

						  strDept = (String) eElement.getAttribute("ECNDepartment").trim();

						  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

			        	  if(slDepartmentList.contains(strDept))			        		  

							alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());    

			          }

			          else

			          {

				          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

		        		  {	        			

				        	  alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());     			

		        		  }

			          }

			        //  alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());

		      }

		   }

			

		   Set<String> alMilestoneSet  =  new HashSet<String>(alMilestoneName);

		   alMilestoneName.clear();

		   alMilestoneName.addAll(alMilestoneSet);

		   String strMilestoneNames   = DomainConstants.EMPTY_STRING;

		   String strDeptNames        = DomainConstants.EMPTY_STRING;

		   int [] [] deptData         = new int [alMilestoneName.size()] [alDepartmentName.size()]; 



		   for (int temp = 0; temp < nList.getLength(); temp++) {

		      nNode = nList.item(temp);

		      if (nNode.getNodeType() == Node.ELEMENT_NODE)

		      {

		          Element eElement     = (Element) nNode;

		          strMilestoneNames    = (String) eElement.getAttribute("Milestone");

		          strDeptNames         = (String) eElement.getAttribute("ECNDepartment");

		          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

		          if(strMilestoneNames != null && strDeptNames != null )

		          {

		        	  

		        	  if(strLeadResponsibilty.equals("Department") && strDeptNames.equals(strUserDepartment))

		        	  {

		        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		        	  else if(strLeadResponsibilty.equals("Business Unit"))

		        	  {

		        		  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");			        	  

			        	  if(slDepartmentList.contains(strDeptNames))

							deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		        	  else if(strParentPrjOwner.equals(context.getUser()))

		        	  {

		        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		        	  }

		        	  

		             // deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames)]++;

		          }

		      }

		   }

		  

		   MapList mlReturnData     = new MapList();

		   for (int i = 0; i < alMilestoneName.size(); i++) {

			   strMilestoneNames    = (String) alMilestoneName.get(i);

			   HashMap mapProject   = new HashMap();

			   mapProject.put("Name", strMilestoneNames);

			   for (int j = 0; j < deptData[i].length; j++) {

				   strDeptNames     = alDepartmentName.get(j);

				   mapProject.put(strDeptNames, deptData[i][j]);

			   }

			   mlReturnData.add(mapProject);

		   }

		   mapReturn.put("Name", mlReturnData);

		   mapReturn.put("Dept", alDepartmentName);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mapReturn;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getECN_byDeptData(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                 = new MapList();

        try {

            

            Map programMap                    = (Map) JPO.unpackArgs(args);

            String strObjectId                = (String) programMap.get("MasterProjectID");

            String strSelectedDeptName		  = (String) programMap.get("DepartmentName");

            String strSelectedECN   = (String) programMap.get("ECNName");

            File Projectfile   				  =   getProjectFile(context, strObjectId);



            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;



            String strProblamName          = DomainConstants.EMPTY_STRING;

            String strDepartmentName             = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            

            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

            //returnMap.put("Name", eElementProject.getAttribute("name"));

            String strAllDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

            

            

            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

            int []  iDeptTotal = new int[alAllProjectDepartment.size()];



              NodeList nList = doc.getElementsByTagName("ECNObj");

              Node nNode =null;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      strProblamName     = (String) eElement.getAttribute("Milestone");

                      strDepartmentName              = (String) eElement.getAttribute("ECNDepartment");

                      HashMap  deleyMap     =   new HashMap();

                      if(strSelectedECN.equals(strProblamName) && strSelectedDeptName.equals(strDepartmentName))

                      {

                              deleyMap    =    getNodeAttributeValue(eElement);

                              mlReturnList.add(deleyMap);

                      }

                  }

              }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    ///

    

    //Inactive Date 

    

    /*Initiatives*/

    /**  

     * Method : findGetIniatives

       * This method gets the Iniatives Objects for Department and Master Projects.

       *

       * @param context the eMatrix <code>Context</code> object

       * @param args    holds ParamMap

       * @return        MapList

       * @throws        Exception if the operation fails

    **/
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList findGetIniatives(Context context,String strParentOID)throws Exception

      {   

          MapList mlFinalReturnList = new MapList();

        

          Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);

          Pattern typePattern = new Pattern(DomainConstants.TYPE_PROJECT_MANAGEMENT);

          

          MapList mlInitativeList = new MapList();

          MapList mlCheckMasterProject = new MapList();

          MapList mlProSpaceObjects  = new MapList();

          MapList mlAllInitativeList = new MapList();

          Map mpAllProSpace = null;

          //HashMap programMap = (HashMap)JPO.unpackArgs(args);

          //String strParentOID = (String)programMap.get("parentOID");

		  String strMasterProjectOwner = "";

          StringList slObjSelects = new StringList();

          slObjSelects.add(DomainConstants.SELECT_ID);

          slObjSelects.add(DomainConstants.SELECT_OWNER);

          StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

          objectSelects.add(DomainConstants.SELECT_NAME);

          objectSelects.add(DomainConstants.SELECT_TYPE);

          objectSelects.add(DomainConstants.SELECT_OWNER);

          

          objectSelects.add("attribute[MSIL Milestone].value");

          objectSelects.add("attribute[MSILProjectDepartment].value");

          objectSelects.add("attribute[MSIL Initiative].value");

          /*objectSelects.add("attribute[MSIL Checksheet].value");

          objectSelects.add("attribute[MSIL Update Required].value");

          objectSelects.add("attribute[MSIL Update Reqd Date].value");

          objectSelects.add("attribute[MSIL Way Ahead].value");*/

		  objectSelects.add("from[MSIL Initiative Project].to.owner");



//          objectSelects.add("from[MSILAddBenifit].to.attribute[MSIL Benifit Catogery]");

//          objectSelects.add("from[MSILAddBenifit].to.attribute[MSIL Benifit Type]");

          DomainObject doProjectSpace = new DomainObject(strParentOID);

		  DomainObject doMasterPrj = DomainObject.newInstance(context);

        

          try

          {

              /*Checking Whether it is a  Mastert Project Or Dept Project

              *If mlCheckMasterProject size is zero then it MP else it dept project.

              */

              //mlCheckMasterProject = doProjectSpace.getRelatedObjects(context,relPattern.getPattern(),typePattern.getPattern(),slObjSelects,null,true,false,(short)0,null,null,(short) 0);

        	  if(!strParentOID.isEmpty()){

        	  //Boolean bCheckMasterPrj = doProjectSpace.hasRelatedObjects(context, ProgramCentralConstants.RELATIONSHIP_SUBTASK, false);

              

              if (true) {

                  /*

                  *Master Project    

                  *//*

                  mlProSpaceObjects = doProjectSpace.getRelatedObjects(context,relPattern.getPattern(),typePattern.getPattern(),slObjSelects,null,false,true,(short)0,null,null,(short) 0);

                  int iProjSize = mlProSpaceObjects.size();

                  for(int iProjCount =0 ; iProjCount<iProjSize ; iProjCount++)

                  {

                      mpAllProSpace =(Map) mlProSpaceObjects.get(iProjCount);

                      String strProjId = (String)mpAllProSpace.get(DomainConstants.SELECT_ID);

                      DomainObject doInicative = new DomainObject(strProjId);

                      mlInitativeList = doInicative.getRelatedObjects(context,RELATIONSHIP_MSIL_INITIATIVE,TYPE_MSIL_INITIATIVE,objectSelects,null,true,false,(short)1,null,null,(short) 0);

                      mlAllInitativeList.addAll(mlInitativeList);

                  }*/

            	 

				doMasterPrj.setId(strParentOID);

				MapList mlPALInitiativeobjList = (MapList)doMasterPrj.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_MASTER_PROJECT,TYPE_MSIL_PAL_INITIATIVE,objectSelects,null,true,false,(short)1,"",null);

			

				if(mlPALInitiativeobjList.size() != 0){

					Map mpPALInitiativeobj =(Map)mlPALInitiativeobjList.get(0);

					String strPALInitiativeobj=(String) mpPALInitiativeobj.get(DomainConstants.SELECT_ID);

					DomainObject doPALInitiative = new DomainObject(strPALInitiativeobj);

					mlAllInitativeList = doPALInitiative.getRelatedObjects(context,RELATIONSHIP_MSIL_PAL_INITIATIVE,TYPE_MSIL_INITIATIVE,objectSelects,null,true,false,(short)1,null,null);

					

				}	

				  

				  

              }

              else

              {

                  /*

                  *Dept Project    

                  */

				  Map mpMasterProjects = new HashMap();//(Map)getMasterProjectsMap(context,strParentOID);

				  strMasterProjectOwner=(String)mpMasterProjects.get("MasterProjectOwner");

                  mlAllInitativeList = doProjectSpace.getRelatedObjects(context,RELATIONSHIP_MSIL_INITIATIVE,TYPE_MSIL_INITIATIVE,objectSelects,null,true,false,(short)1,null,null,(short) 0);

              }

              

          Map tempMap = null;

          String strInitiativeId = "";

          DomainObject doInitiative = new DomainObject();

          DomainObject doBenefit = new DomainObject();

          StringList slBenefitList = new StringList();

                  

          StringList slBenefitSelect = new StringList();

          slBenefitSelect.add(SELECT_ATTRIBUTE_BENIFIT_CATOGERYRY);

          slBenefitSelect.add(SELECT_ATTRIBUTE_BENIFIT_TYPE);

          slBenefitSelect.add(SELECT_ATTRIBUTE_BENIFIT_DETAILS);

          slBenefitSelect.add("attribute[MSIL Checksheet].value");

          slBenefitSelect.add("attribute[MSIL Update Required].value");

          slBenefitSelect.add("attribute[MSIL Update Reqd Date].value");

          slBenefitSelect.add("attribute[MSIL Way Ahead].value");



          Map mBenefitMap = null;

          String strOwner = "";

          if(!mlAllInitativeList.isEmpty()){

          int iInitativeListSize = mlAllInitativeList.size();

          int iBenefitSize;

         for(int iInitiativesCount=0; iInitiativesCount<iInitativeListSize;iInitiativesCount++){

           tempMap = (Map)mlAllInitativeList.get(iInitiativesCount);

           strInitiativeId = (String)tempMap.get(DomainObject.SELECT_ID);

           if(!strInitiativeId.isEmpty()){

           doInitiative.setId(strInitiativeId);

           strOwner = (String)tempMap.get(DomainObject.SELECT_OWNER);

           slBenefitList = (StringList)doInitiative.getInfoList(context, "from["+RELATIONSHIP_MSIL_BENEFIT+"].to.id");

            if(!slBenefitList.isEmpty()){

           iBenefitSize = slBenefitList.size();



           for(int iBenefitListCount=0;iBenefitListCount<iBenefitSize;iBenefitListCount++){

	               

	               doBenefit.setId((String)slBenefitList.get(iBenefitListCount));

	               

	               HashMap tempMap1 = new HashMap();

	               mBenefitMap = doBenefit.getInfo(context, slBenefitSelect);

	               tempMap1.put(DomainObject.SELECT_ID, strInitiativeId);

	               tempMap1.put("BenifitId",(String)slBenefitList.get(iBenefitListCount));

	               tempMap1.put(DomainObject.SELECT_OWNER,strOwner);

	               tempMap1.put("attribute[MSIL Benefit Category].value",(String)mBenefitMap.get("attribute[MSIL Benefit Category]"));

	               tempMap1.put("attribute[MSIL Benefit Type].value",(String)mBenefitMap.get("attribute[MSIL Benefit Type]"));

	               tempMap1.put("attribute[MSIL Benefit Details].value" ,(String)mBenefitMap.get("attribute[MSIL Benefit Details]"));

	               tempMap1.put("attribute[MSIL Checksheet].value" ,(String)mBenefitMap.get("attribute[MSIL Checksheet].value"));

	               tempMap1.put("attribute[MSIL Update Required].value" ,(String)mBenefitMap.get("attribute[MSIL Update Required].value"));

	               tempMap1.put("attribute[MSIL Update Reqd Date].value" ,(String)mBenefitMap.get("attribute[MSIL Update Reqd Date].value"));

	               tempMap1.put("attribute[MSIL Way Ahead].value" ,(String)mBenefitMap.get("attribute[MSIL Way Ahead].value"));

				   tempMap1.put("MasterProjectOwner",strMasterProjectOwner);

				   //tempMap.putAll(tempMap1);

				   //tempMap1.putAll(tempMap);

				   

				   if(iBenefitListCount==0)

					   tempMap.put("objectType", "Initiative");

				   else

					   tempMap.put("objectType", "Benefit");

				   

				   HashMap tempMap3	= new HashMap();

				   tempMap3.putAll(tempMap1);

				   tempMap3.putAll(tempMap);

	               mlFinalReturnList.add(tempMap3);

             }

            }

           }

          }

         }

       }  

      }catch(Exception e)

          {

              e.printStackTrace();

          }

          return mlFinalReturnList;

      }


	@com.matrixone.apps.framework.ui.ProgramCallable
   public HashMap getInitiativeGrapicalData(Context context , String [] args)throws Exception 

    {

        HashMap mapReturn = new HashMap();

        try {

            Map programMap                      = (Map) JPO.unpackArgs(args);

            String strObjectId                  = (String) programMap.get("objectId");

            String strLeadResponsibilty 	= (String) programMap.get("LeadResponsibilty");

			String strUserDepartment        = (String) programMap.get("UserDepartments");

            File Projectfile                    = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder            = dbFactory.newDocumentBuilder();

            Document doc                        = null;

            ArrayList<String> alDepartmentName  = new ArrayList<String>();

            ArrayList<String> alMilestoneName   = new ArrayList<String>();



			doc = dBuilder.parse(Projectfile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("IniativesObj");

			Node nNode 	   = null;

			

			String strParentPrjOwner = DomainConstants.EMPTY_STRING;

			String strDept			 = DomainConstants.EMPTY_STRING;

			String strObjectType  	 =  DomainConstants.EMPTY_STRING;

			

			for (int temp  = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			          //alDepartmentName.add(((String) eElement.getAttribute("Department")).trim());

			          strObjectType =  (String) eElement.getAttribute("objectType");

			          if(strObjectType.equalsIgnoreCase("Initiative"))

			          {

				          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

				          if(strLeadResponsibilty.equals("Department"))

				          {			

				        	  strDept = (String) eElement.getAttribute("Department").trim();

				        	  if(strUserDepartment.equals(strDept))

				        	  {

				        		  alDepartmentName.add(((String) eElement.getAttribute("Department")).trim());

				        	  }

				          }

				          else if(strLeadResponsibilty.equals("Business Unit"))

				          {

				        	  //Business Unit Head..

				        	  strDept = (String) eElement.getAttribute("Department").trim();

			        	  StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

			        	  if(slDepartmentList.contains(strDept))

							alDepartmentName.add(((String) eElement.getAttribute("Department")).trim());

				          }

				          else

				          {

					          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

			        		  {	        			

					        	  alDepartmentName.add(((String) eElement.getAttribute("Department")).trim());      			

			        		  }

				          }	

			          }

			   }

			}

			Set<String> alDepartmentSet  =  new HashSet<String>(alDepartmentName);

			alDepartmentName.clear();

			alDepartmentName.addAll(alDepartmentSet);



			for (int temp = 0; temp < nList.getLength(); temp++) {

			      nNode = nList.item(temp);

			      if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			          Element eElement     = (Element) nNode;

			         // alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());

			          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

			          strObjectType =  (String) eElement.getAttribute("objectType");

			          if(strObjectType.equalsIgnoreCase("Initiative"))

			          {

				          if(strLeadResponsibilty.equals("Department"))

				          {		

				        	  strDept = (String) eElement.getAttribute("Department").trim();

				        	  if(strUserDepartment.equals(strDept))

				        	  {

				        		  alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());

				        	  }

				          }

				          else if(strLeadResponsibilty.equals("Business Unit"))

				          {

				        	  //Business Unit Head...

				        	 strDept = (String) eElement.getAttribute("Department").trim();

							StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

							if(slDepartmentList.contains(strDept))

								alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());  

				          }

				          else

				          {

					          if(strParentPrjOwner.equalsIgnoreCase(context.getUser()))

			        		  {	        			

					        	  alMilestoneName.add(((String) eElement.getAttribute("Milestone")).trim());       			

			        		  }

				          }

			          }

		      }

		   }

			

		   Set<String> alMilestoneSet  =  new HashSet<String>(alMilestoneName);

		   alMilestoneName.clear();

		   alMilestoneName.addAll(alMilestoneSet);

		   String strMilestoneNames   = DomainConstants.EMPTY_STRING;

		   String strDeptNames        = DomainConstants.EMPTY_STRING;



		   int [] [] deptData         = new int [alMilestoneName.size()] [alDepartmentName.size()];

		   HashMap<String , ArrayList<String>> countMap = new HashMap();



		   for (int temp = 0; temp < nList.getLength(); temp++) {

		      nNode = nList.item(temp);

		      if (nNode.getNodeType() == Node.ELEMENT_NODE)

		      {

		          Element eElement     = (Element) nNode;

		          strMilestoneNames    = (String) eElement.getAttribute("Milestone");

		          strDeptNames         = (String) eElement.getAttribute("Department");

		          strParentPrjOwner = (String) eElement.getAttribute("ParentProjectOwner");

		          strObjectType =  (String) eElement.getAttribute("objectType");

		          if(strObjectType.equalsIgnoreCase("Initiative"))

		          {

			          if(strMilestoneNames != null && strDeptNames != null  && deptData.length > 0)

			          {

			        	  //deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

	

			        	  if(strLeadResponsibilty.equals("Department") && strDeptNames.equals(strUserDepartment))

			        	  {

			        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

			        	  }

			        	  else if(strLeadResponsibilty.equals("Business Unit"))

			        	  {

			        		StringList slDepartmentList = FrameworkUtil.split(strUserDepartment, ",");

							if(slDepartmentList.contains(strDeptNames))

								deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

			        	  }

			        	  else if(strParentPrjOwner.equals(context.getUser()))

			        	  {

			        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

			        	  }

			        	  

			        	 /* if(countMap.containsKey(strMilestoneNames))

			        	  {

			        		  ArrayList	tempList 	=	countMap.get(strMilestoneNames.trim());

			        		  if(!tempList.contains(strDeptNames.trim()))

			        		  {

			        			  tempList.add(strDeptNames.trim());

			        			  countMap.put(strMilestoneNames.trim(), tempList);

				        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

			        		  }

			        	  }

			        	  else

			        	  {

			        		  ArrayList	tempList 	=	new ArrayList<String>();

			        		  tempList.add(strDeptNames.trim());

			        		  countMap.put(strMilestoneNames.trim(), tempList );

			        		  deptData[alMilestoneName.indexOf(strMilestoneNames.trim())][alDepartmentName.indexOf(strDeptNames.trim())]++;

			        	  }*/

			              

			          }

		          }

		      }

		   }

		  

		   MapList mlReturnData     = new MapList();

		   for (int i = 0; i < alMilestoneName.size(); i++) {

			   strMilestoneNames    = (String) alMilestoneName.get(i);

			   HashMap mapProject   = new HashMap();

			   mapProject.put("Name", strMilestoneNames);

			   for (int j = 0; j < deptData[i].length; j++) {

				   strDeptNames     = alDepartmentName.get(j);

				   mapProject.put(strDeptNames, deptData[i][j]);

			   }

			   mlReturnData.add(mapProject);

		   }

		   mapReturn.put("Name", mlReturnData);

		   mapReturn.put("Dept", alDepartmentName);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mapReturn;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getInitiative_byDeptData(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList                 = new MapList();

        try {

            

            Map programMap                    = (Map) JPO.unpackArgs(args);

            String strObjectId                = (String) programMap.get("MasterProjectID");

            String strSelectedDeptName		  = (String) programMap.get("DepartmentName");

            String strSelectedInitiative   		  = (String) programMap.get("InitiativeName");

            File Projectfile   				  =   getProjectFile(context, strObjectId);



            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;



            String strInitiativeName          = DomainConstants.EMPTY_STRING;

            String strDepartmentName             = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();



          NodeList nList = doc.getElementsByTagName("IniativesObj");

          Node nNode =null;

          for (int temp = 0; temp < nList.getLength(); temp++) {

              nNode = nList.item(temp);

              if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                  Element eElement      = (Element) nNode;

                  strInitiativeName     = (String) eElement.getAttribute("Milestone");

                  strDepartmentName     = (String) eElement.getAttribute("Department");

                  if(strSelectedInitiative.equals(strInitiativeName) && strSelectedDeptName.equals(strDepartmentName))

                  {

                      HashMap  deleyMap = new HashMap();

                      deleyMap          = getNodeAttributeValue(eElement);

                      mlReturnList.add(deleyMap);

                  }

              }

          }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    //

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getGateDocumentStatusForDevision(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        HashMap returnMap 	 = new HashMap();

        MapList mlreturnList = new MapList();

        try {

             Map programMap                   = (Map) JPO.unpackArgs(args);

             String strObjectId               = (String) programMap.get("objectId");

             String strSelectedMilestone      = (String) programMap.get("milestone");

             ArrayList<String> alAllGateDept	 = getALLGateDept(context);

             File[] listOfFiles                  = getAllProjectFile(context);

             DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

             DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

             Document doc                  = null;

             String isLeafTask             = DomainConstants.EMPTY_STRING;

             String strCurrent             = DomainConstants.EMPTY_STRING;

             String strDelay               = DomainConstants.EMPTY_STRING;

             String strAllDepartment       = DomainConstants.EMPTY_STRING;

             String strDepartment          = DomainConstants.EMPTY_STRING;



             //String iDeliverable         = DomainConstants.EMPTY_STRING;

             String strDepartmentName      = DomainConstants.EMPTY_STRING;

             

             int iCompleted                 = 0;

             int iDelay                     = 0;

             int iTotal                     = 0;



			 NodeList gateList              = null;

			 Node ngateListDocument         = null;

			 Element eGateElement           = null;



			 NodeList gateDeptList          = null;

			 Node nGateDeptDocument         = null;

			 Element eGateDeptElement       = null;

			 

			 NodeList gateDeptDeliverablesList         = null;

			 Node nGateDeptDeliverablesDocument        = null;

			 Element eGateDeptDeliverablesElement      = null;

			 

			 Node nNode                   = null;

			 Node nNodeChildGateDocument  = null;

			 

             ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

             String isGateSchedule    = DomainConstants.EMPTY_STRING;

             

             String strParentObjectType = "";

             String strParentTaskType = "";

             String strType = "";

             String strTaskType = "";

             

             for (int i = 0; i < listOfFiles.length; i++) {

               if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                   doc = dBuilder.parse(listOfFiles[i]);

                   doc.getDocumentElement().normalize();

                   HashMap projectMap     =     new HashMap();



                   Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                   projectMap.put("ProjectId", eElementProject.getAttribute("id"));

                   projectMap.put("ProjectName", eElementProject.getAttribute("name"));

                   int [] intPlan          = new int[alAllGateDept.size()];

                   int [] intActual        = new int[alAllGateDept.size()];



                   String  strAll_ProjDepartment    = eElementProject.getAttribute("USER_DEPARTMENT");

                   ArrayList<String> alProjDeptList = new ArrayList<String>(Arrays.asList(strAll_ProjDepartment.split(",")));

                   alAllProjectDepartment.addAll(alProjDeptList);



                   NodeList nList = doc.getElementsByTagName("I");

                   Element eElement;

                   ArrayList<String> slProjectDepartment	=	new ArrayList<String>();

                   for (int temp = 0; temp < nList.getLength(); temp++) {

                       nNode = nList.item(temp);

                       if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            eElement             = (Element) nNode;

                            isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                            if("Yes".equalsIgnoreCase(isGateSchedule))

                            {

                                gateList    = eElement.getChildNodes();

                                if(gateList!=null)

                                {

                              	  for (int temp1 = 0; temp1 < gateList.getLength(); temp1++) {

                              		ngateListDocument = gateList.item(temp1);

                              		  if (ngateListDocument!=null && ngateListDocument.getNodeType() == Node.ELEMENT_NODE) {

                              			  	eGateElement       = (Element) ngateListDocument;

                              			    gateDeptList  	   = eGateElement.getChildNodes();

                              			  for (int igateDeptList = 0; igateDeptList < gateDeptList.getLength(); igateDeptList++) {

                              				  nGateDeptDocument = gateDeptList.item(igateDeptList);

                                      		  if (nGateDeptDocument!=null && nGateDeptDocument.getNodeType() == Node.ELEMENT_NODE) {

                                      			  eGateDeptElement = (Element) nGateDeptDocument;

                                      			  strDepartmentName	        =	(String)eGateDeptElement.getAttribute("T");

												  

												   strParentObjectType = (String)eGateDeptElement.getAttribute("parentObjectType");

                                     			  strParentTaskType = (String)eGateDeptElement.getAttribute("parentTaskType");

                                     			  strType = (String)eGateDeptElement.getAttribute("TYPE");

                                     			  strTaskType = (String)eGateDeptElement.getAttribute("MSILTaskType");

												  

                                     			  if(strType.equals(ProgramCentralConstants.TYPE_TASK) && strParentObjectType.equals(ProgramCentralConstants.TYPE_GATE)

                                     					  && strTaskType.equalsIgnoreCase("Gate Schedule") && strParentTaskType.equalsIgnoreCase("Gate Schedule"))

                                     			  {

                                     				  slProjectDepartment.add(strDepartmentName);

                                      			  gateDeptDeliverablesList	=	eGateDeptElement.getChildNodes();

                                      			  intPlan[alAllGateDept.indexOf(strDepartmentName)]+=gateDeptDeliverablesList.getLength();

                                      			  for (int igateDeptDeliverablesList = 0; igateDeptDeliverablesList < gateDeptList.getLength(); igateDeptDeliverablesList++) 

                                      			  {

                                      				 nGateDeptDeliverablesDocument = gateDeptDeliverablesList.item(igateDeptDeliverablesList);

                                      				 if (nGateDeptDeliverablesDocument!=null && nGateDeptDeliverablesDocument.getNodeType() == Node.ELEMENT_NODE) {

                                            			  eGateDeptDeliverablesElement = (Element) nGateDeptDeliverablesDocument;

                                                          String  iDeliverable         = (String) eGateDeptDeliverablesElement.getAttribute("Current");

                                                          if("Complete".equals(iDeliverable))

                                                          {

                                                        	  intActual[alAllGateDept.indexOf(strDepartmentName)]++;

                                                          }

                                      				 }

                                      			  }

												  }

                                      		   }

                                           }

                              		   }

                              	   }

                               }

                           }

                       }

                   }

                   

                /*   for (int j = 0; j < alAllGateDept.size(); j++) {

                       strDepartmentName        =    (String)alAllGateDept.get(j);

                       HashMap TaskCountMap     = new HashMap();

                       TaskCountMap.put("Plan",  ""+intPlan[alAllGateDept.indexOf(strDepartmentName)]);

                       TaskCountMap.put("Actual",  ""+ intActual[alAllGateDept.indexOf(strDepartmentName)]);

                       if(alProjDeptList.contains(strDepartmentName))

                       {

                    	   projectMap.put(strDepartmentName    , TaskCountMap);

                       }

                   }*/

                   //----

                   for (int j = 0; j < alAllGateDept.size(); j++) {

                       strDepartmentName        =    (String)alAllGateDept.get(j);

                       HashMap TaskCountMap     = new HashMap();

                       if(slProjectDepartment.contains(strDepartmentName))

                       {

                    	   TaskCountMap.put("Plan",  ""+intPlan[alAllGateDept.indexOf(strDepartmentName)]);

                    	   TaskCountMap.put("Actual",  ""+ intActual[alAllGateDept.indexOf(strDepartmentName)]);

                       }

                       else

                       {

                    	   TaskCountMap.put("Plan",  "");

                    	   TaskCountMap.put("Actual",  "");

                       }

                       projectMap.put(strDepartmentName        , TaskCountMap);

                   }

                   //----

                                     

                   mlreturnList.add(projectMap);

                }

             }

             ArrayList<String> alFinalDeptList 	= new ArrayList<String>();

             String strDept= DomainConstants.EMPTY_STRING;

             for(int i=0;i<alAllGateDept.size();i++)

             {

            	 strDept	= (String)alAllGateDept.get(i);

            	 boolean bCheck	=	alAllProjectDepartment.contains(strDept);

            	 if(bCheck)

            	 {

            		 alFinalDeptList.add(strDept);

            	 }

             }

             

           returnMap.put("GateDeptNames" , alFinalDeptList);

           returnMap.put("GetData"       , mlreturnList);

           

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }

    


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getGateData(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        HashMap returnMap 	 = new HashMap();

        MapList mlreturnList = new MapList();

        try {

            Map programMap                   = (Map) JPO.unpackArgs(args);

            String strObjectId               = (String) programMap.get("objectId");

            String strSelectedDepartmentName = (String) programMap.get("departmentName");

            String strSelectedMilestone      = (String) programMap.get("milestone");

            ArrayList<String> alAllGateDept  = getALLGateDept(context);

            File Projectfile                 = getProjectFile(context, strObjectId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder         = dbFactory.newDocumentBuilder();

            Document doc                     = null;

            String isLeafTask                = DomainConstants.EMPTY_STRING;

            Node nNode                       = null;

            Node nNodeChildGateDocument      = null;



            ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

            String isGateSchedule    = DomainConstants.EMPTY_STRING;

            doc = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            

            HashMap projectMap               = new HashMap();

            Element eElementProject          = (Element)  doc.getElementsByTagName("Body").item(0);

            String  strAll_ProjDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

            ArrayList<String> alProjDeptList = new ArrayList<String>(Arrays.asList(strAll_ProjDepartment.split(",")));

            alAllProjectDepartment.addAll(alProjDeptList);



            NodeList nList = doc.getElementsByTagName("I");

            Element eElement;

            for (int temp = 0; temp < nList.getLength(); temp++) {

                nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    eElement             = (Element) nNode;

                    isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                    if("Yes".equalsIgnoreCase(isGateSchedule))  {

                        projectMap.put("ProjectId"      , eElementProject.getAttribute("id"));

                        projectMap.put("ProjectName"    , eElementProject.getAttribute("name"));

                        projectMap = getNodeAttributeValue(eElement);

                        projectMap.put("departmentName" , strSelectedDepartmentName);

                        projectMap.put(DomainRelationship.SELECT_ID , strObjectId );

                        projectMap.put("Click1" , "1");

                        break;

                    }

                }

            }

            if(!projectMap.isEmpty())

                mlReturnList.add(projectMap);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mlReturnList;

    }

    


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getGeteChildTask(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

            Map programMap              = (Map) JPO.unpackArgs(args);

            String strProjectId         = (String) programMap.get("relId");

            String strObjectId          = (String) programMap.get("parentId");

            String strSelectedDept      = (String) programMap.get("departmentName");

            File Projectfile            = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            String strClickIds           = (String) programMap.get("objectId");

            String strLevel              = (String) programMap.get("level");

            Node nNode                   = null;

            NodeList childNodeList       = null;

            String strTaskName	         = DomainConstants.EMPTY_STRING;

            Boolean clickFirst           =	false;

            if(strLevel.equals("0,0"))

            {

                NodeList nList            = doc.getElementsByTagName("I");

                String strTaskId          = DomainConstants.EMPTY_STRING;

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement  = (Element) nNode;

                        strTaskId         = (String) eElement.getAttribute("TaskId");

                        strTaskName       = (String) eElement.getAttribute("T");

                        if(strClickIds.equals(strTaskId))

                        {

                           childNodeList  =   eElement.getChildNodes();

                           clickFirst=true;

                           break;

                        }

                    }

                }

            }

            else

            {

               NodeList nList            = doc.getElementsByTagName("I");

               String strTaskId          = DomainConstants.EMPTY_STRING;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement  = (Element) nNode;

                       strTaskId         = (String) eElement.getAttribute("TaskId");

                       if(strClickIds.equals(strTaskId) )

                       {

                          childNodeList  =   eElement.getChildNodes();

                          if(strLevel.length()>5)

                          {

                              clickFirst=true;

                          }

                          break;

                       }

                   }

               }

            }



            Map dataMap    =   null;

            for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    strTaskName          = (String) eElement.getAttribute("T");

                    if(strLevel.equals("0,0"))

                    {

                    	if(isGateHaveDept(eElement, strSelectedDept))

                    	{

                    		dataMap   =   getNodeAttributeValue(eElement);

    	                    dataMap.put("PersonType","Non-DB");

    	                    dataMap.put("objectType","person");

    	                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

    	                    dataMap.put("Id",(String) dataMap.get("TaskId"));

    	                    dataMap.put("id",(String) dataMap.get("TaskId"));

    	                    dataMap.put("relId",strProjectId);

    	                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId);

    	                    dataMap.put("hadChildren","true");

    	                    mlReturnList.add(dataMap);

                    	}

                    }

                    else if(clickFirst || strSelectedDept.equals(strTaskName))

                    {

	                    dataMap   =   getNodeAttributeValue(eElement);

	                    dataMap.put("PersonType","Non-DB");

	                    dataMap.put("objectType","person");

	                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

	                    dataMap.put("Id",(String) dataMap.get("TaskId"));

	                    dataMap.put("id",(String) dataMap.get("TaskId"));

	                    dataMap.put("relId",strProjectId);

	                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId);

	                    dataMap.put("hadChildren","true");

	                    mlReturnList.add(dataMap);

                    }

                }

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

        return mlReturnList;

    }

    //Added By Yaseen

    /**

	 * Used in "Gate Closure Status" chart.

	 * This method is called when the user clicks on the graph a table is generated and then user expands the WBS data.

     *

     * @param context the eMatrix <code>Context</code> object

     * @param args holds the input arguments:

     * @return MapList

     * @throws Exception if the operation fails

     */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList  getGateClosureChildTask(Context context , String [] args)throws Exception

    {

        MapList mlReturnList            = new MapList();

        try {

            Map programMap              = (Map) JPO.unpackArgs(args);

            String strProjectId         = (String) programMap.get("relId");

            String strObjectId          = (String) programMap.get("parentId");

            String strSelectedDept      = (String) programMap.get("departmentName");

            File Projectfile            = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder     = dbFactory.newDocumentBuilder();

            Document doc                 = null;

            doc                          = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            String strClickIds           = (String) programMap.get("objectId");

            String strLevel              = (String) programMap.get("level");

            Node nNode                   = null;

            NodeList childNodeList       = null;

            String strTaskName	         = DomainConstants.EMPTY_STRING;

            Boolean clickFirst           =	false;

            String strUserDepartment = DomainConstants.EMPTY_STRING;         

            StringList slUserDeptList = new StringList();

                       

            Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

            strUserDepartment = eElementProject.getAttribute("USER_DEPARTMENT");

            slUserDeptList = StringUtil.split(strUserDepartment, ",");

            

            if(strLevel.equals("0,0"))

            {

                NodeList nList            = doc.getElementsByTagName("I");

                String strTaskId          = DomainConstants.EMPTY_STRING;

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement  = (Element) nNode;

                        strTaskId         = (String) eElement.getAttribute("TaskId");

                        strTaskName       = (String) eElement.getAttribute("T");

                        if(strClickIds.equals(strTaskId))

                        {

                           childNodeList  =   eElement.getChildNodes();

                           clickFirst=true;

                           break;

                        }

                    }

                }

            }

            else

            {

               NodeList nList            = doc.getElementsByTagName("I");

               String strTaskId          = DomainConstants.EMPTY_STRING;

               for (int temp = 0; temp < nList.getLength(); temp++) {

                   nNode = nList.item(temp);

                   if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                       Element eElement  = (Element) nNode;

                       strTaskId         = (String) eElement.getAttribute("TaskId");

                       if(strClickIds.equals(strTaskId) )

                       {

                          childNodeList  =   eElement.getChildNodes();

                          if(strLevel.length()>5)

                          {

                              clickFirst=true;

                          }

                          break;

                       }

                   }

               }

            }



            Map dataMap    =   null;

            for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    strTaskName          = (String) eElement.getAttribute("T");

                    String strGateStatus = (String) eElement.getAttribute("GateClosureStatus");

           			

                   // String strType = 

                    if(strLevel.equals("0,0"))

                    {

                    

                    	if(isGateHaveGateScheduleDept(eElement, strSelectedDept,slUserDeptList))

                    	{

                    		dataMap   =   getNodeAttributeValue(eElement);

    	                    dataMap.put("PersonType","Non-DB");

    	                    dataMap.put("objectType","person");

    	                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

    	                    dataMap.put("Id",(String) dataMap.get("TaskId"));

    	                    dataMap.put("id",(String) dataMap.get("TaskId"));

    	                    dataMap.put("relId",strProjectId);

    	                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId);

    	                    dataMap.put("hadChildren","true");

    	                    mlReturnList.add(dataMap);

                    	}

                    }

                    else if(strSelectedDept.equals(strGateStatus))

                    {

                    	if(slUserDeptList.contains(strTaskName))

                    	{

		                    dataMap   =   getNodeAttributeValue(eElement);

		                    dataMap.put("PersonType","Non-DB");

		                    dataMap.put("objectType","person");

		                    dataMap.put("objectId",(String) dataMap.get("TaskId") );

		                    dataMap.put("Id",(String) dataMap.get("TaskId"));

		                    dataMap.put("id",(String) dataMap.get("TaskId"));

		                    dataMap.put("relId",strProjectId);

		                    dataMap.put(DomainRelationship.SELECT_ID,strProjectId);

		                    dataMap.put("hadChildren","true");

		                    mlReturnList.add(dataMap);

                    	}

                    }

                }

            }

        } catch(Exception ex) {

            ex.printStackTrace();

        }

        return mlReturnList;

    }

    /**

	 * Used in "Gate Closure Status" chart.

	 * Checking whether the node contains any element which has the Gate Status of selected category.

     *

     * @param context the eMatrix <code>Context</code> object

     * @param args holds the input arguments:

     * @return boolean

     * @throws Exception if the operation fails

     */

    public boolean isGateHaveGateScheduleDept(Element GateNode , String strDeptName,StringList slUserDeptList ) throws Exception

    {

    	Boolean bReturn = false;

    	Node nNode                   = null;

    	String strTaskName = DomainConstants.EMPTY_STRING;

    	try {

    		NodeList childNodeList   =   GateNode.getChildNodes();

    		String strTaskGateStatus	     =	DomainConstants.EMPTY_STRING;

    		for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    strTaskGateStatus          = (String) eElement.getAttribute("GateClosureStatus");

                    strTaskName  = (String) eElement.getAttribute("T");

                     if( strDeptName.equals(strTaskGateStatus) && slUserDeptList.contains(strTaskName))

                    {

                    	 bReturn=true;

                    	 break;

                    }

                }

            }

			

		} catch (Exception e) {

			e.printStackTrace();

		}

    	return bReturn;

    }

    //-------------------

    

    public boolean isGateHaveDept(Element GateNode , String strDeptName ) throws Exception

    {

    	Boolean bReturn = false;

    	Node nNode                   = null;

    	try {

    		NodeList childNodeList   =   GateNode.getChildNodes();

    		String strTaskName	     =	DomainConstants.EMPTY_STRING;

    		for (int temp = 0; temp < childNodeList.getLength(); temp++) {

                nNode = childNodeList.item(temp);

                if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE  && "I".equals(nNode.getNodeName()) ) {

                    Element eElement     = (Element) nNode;

                    strTaskName          = (String) eElement.getAttribute("T");

                     if( strDeptName.equals(strTaskName))

                    {

                    	 bReturn=true;

                    	 break;

                    }

                }

            }

			

		} catch (Exception e) {

			e.printStackTrace();

		}

    	return bReturn;

    }




	@com.matrixone.apps.framework.ui.ProgramCallable
    public ArrayList<String> getALLGateDeptForDevision(Context context )

    {

    	ArrayList<String> alReturnList 	=	new ArrayList<String>();

        try {

            //File folder = new File("E:\\WorkSpace\\Nikesh");

            //File[] listOfFiles = folder.listFiles();

           File[] listOfFiles        = getAllProjectFile(context);

           

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc             = null;

            String isGateSchedule 	 = DomainConstants.EMPTY_STRING;

            NodeList childNodeList   = null;

            NodeList DeptChildNodes  = null;

            Element eElement1        = null;

            Element eElement2        = null;

            

             NodeList gateList       	  = null;

			 Node ngateListDocument       = null;

			 Element eGateElement         = null;



			 NodeList gateDeptList      = null;

			 Node nGateDeptDocument     = null;

			 Element eGateDeptElement   = null;

			 

			 NodeList gateDeptDeliverablesList         = null;

			 Node nGateDeptDeliverablesDocument        = null;

			 Element eGateDeptDeliverablesElement      = null;

			 

			 Node nNode                   = null;

			 Node nNodeChildGateDocument  = null;

			 

			 

			 Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

             String strAllDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

             ArrayList<String> alDeparmentList      = new ArrayList<String>(Arrays.asList(strAllDepartment.split(",")));

			 

            

            

            for (int i = 0; i < listOfFiles.length; i++) {

              if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                  doc = dBuilder.parse(listOfFiles[i]);

                  doc.getDocumentElement().normalize();

                  HashMap projectMap     =     new HashMap();

                  NodeList nList = doc.getElementsByTagName("I");

                  Element eElement;

                  for (int temp = 0; temp < nList.getLength(); temp++) {

                      nNode = nList.item(temp);

                      if (nNode!=null && nNode.getNodeType() == Node.ELEMENT_NODE) {

                           eElement             = (Element) nNode;

                           isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                           if("Yes".equalsIgnoreCase(isGateSchedule))

                           {

                               gateList    = eElement.getChildNodes();

                               if(gateList!=null)

                               {

                             	  for (int temp1 = 0; temp1 < gateList.getLength(); temp1++) {

                             		ngateListDocument = gateList.item(temp1);

                             		  if (ngateListDocument!=null && ngateListDocument.getNodeType() == Node.ELEMENT_NODE) {

                             			  	eGateElement       = (Element) ngateListDocument;

                             			    gateDeptList  	   = eGateElement.getChildNodes();

                             			  for (int igateDeptList = 0; igateDeptList < gateDeptList.getLength(); igateDeptList++) {

                             				  nGateDeptDocument = gateDeptList.item(igateDeptList);

                                     		  if (nGateDeptDocument!=null && nGateDeptDocument.getNodeType() == Node.ELEMENT_NODE) {

                                     			  eGateDeptElement = (Element) nGateDeptDocument;

                                     			  if(alDeparmentList.contains((String)eGateDeptElement.getAttribute("T")))

                                     			  {

                                     				  alReturnList.add((String)eGateDeptElement.getAttribute("T"));

                                     			  }

                                     		  }

                                          }

                             		   }

                             	   }

                               }

                               continue;

                          }

                      }

                  }

               }

            }

            Set<String> setGetDocumentDept     = new HashSet<String>(alReturnList);

            alReturnList.clear();

            alReturnList.addAll(setGetDocumentDept);

        }

        catch(Exception ex)

        {

        	ex.printStackTrace();

        }

        return alReturnList;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getGateClosureStatus(Context context , String [] args)throws Exception 

    {

        MapList mlReturnList = new MapList();

        HashMap returnMap 	 = new HashMap();

        MapList mlreturnList = new MapList();

        try {

            Map programMap                   = (Map) JPO.unpackArgs(args);

            String strObjectId               = (String) programMap.get("objectId");

            String strSelectedMilestone      = (String) programMap.get("milestone");

            ArrayList<String> alAllGateDept	 = new ArrayList<String>();//getALLGateDept(context);

             File[] listOfFiles                  = getAllProjectFile(context);

             DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();

             DocumentBuilder dBuilder      = dbFactory.newDocumentBuilder();

             Document doc                  = null;

             String isLeafTask             = DomainConstants.EMPTY_STRING;

             String strCurrent             = DomainConstants.EMPTY_STRING;

             String strDelay               = DomainConstants.EMPTY_STRING;

             String strAllDepartment       = DomainConstants.EMPTY_STRING;

             String strDepartment          = DomainConstants.EMPTY_STRING;



             //String iDeliverable         = DomainConstants.EMPTY_STRING;

             String strDepartmentName      = DomainConstants.EMPTY_STRING;

             

             int iCompleted                 = 0;

             int iDelay                     = 0;

             int iTotal                     = 0;



			 NodeList gateList              = null;

			 Node ngateListDocument         = null;

			 Element eGateElement           = null;



			 NodeList gateDeptList          = null;

			 Node nGateDeptDocument         = null;

			 Element eGateDeptElement       = null;

			 

			 NodeList gateDeptDeliverablesList         = null;

			 Node nGateDeptDeliverablesDocument        = null;

			 Element eGateDeptDeliverablesElement      = null;

			 

			 Node nNode                   = null;

			 Node nNodeChildGateDocument  = null;

			 

             ArrayList<String> alAllProjectDepartment     = new ArrayList<String>();

             String isGateSchedule	=	DomainConstants.EMPTY_STRING;

             String strUserDepartment = DomainConstants.EMPTY_STRING;

             

             StringList slUserDeptList = new StringList();

             ArrayList<String> alStatusRange	=	new ArrayList<String>();

             alStatusRange.add("Sign Off");

             alStatusRange.add("Under Review");

             alStatusRange.add("Under Correction");

             alStatusRange.add("Due for completion");

             alStatusRange.add("Pending for Submission");

            

             

             for (int i = 0; i < listOfFiles.length; i++) {

               if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Project")) {

                   doc = dBuilder.parse(listOfFiles[i]);

                   doc.getDocumentElement().normalize();

                   HashMap projectMap     =     new HashMap();



                   Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

                   projectMap.put("ProjectId", eElementProject.getAttribute("id"));

                   projectMap.put("ProjectName", eElementProject.getAttribute("name"));

                   strUserDepartment = eElementProject.getAttribute("USER_DEPARTMENT");

                   slUserDeptList = StringUtil.split(strUserDepartment, ",");

                   int [] intStausCount                = new int[alStatusRange.size()];



                   String  strAll_ProjDepartment    = eElementProject.getAttribute("ALL_DEPARTMENT");

                   ArrayList<String> alProjDeptList      = new ArrayList<String>(Arrays.asList(strAll_ProjDepartment.split(",")));

                    String  strGetStatus     = "";

                    String strTaskType       = "";

                    String strParentType     = "";

      			   

                   

                   NodeList nList = doc.getElementsByTagName("I");

                   Element eElement;

                   for (int temp = 0; temp < nList.getLength(); temp++) {

                       nNode = nList.item(temp);

                       if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            eElement             = (Element) nNode;

                            isGateSchedule       = (String) eElement.getAttribute("isGateSchedule");

                            if("Yes".equalsIgnoreCase(isGateSchedule))

                            {

                                gateList    = eElement.getChildNodes();

                                if(gateList!=null)

                                {

                              	  for (int temp1 = 0; temp1 < gateList.getLength(); temp1++) {

                              		ngateListDocument = gateList.item(temp1);

                              		  if (ngateListDocument.getNodeType() == Node.ELEMENT_NODE) {

                              			  	eGateElement       = (Element) ngateListDocument;

                              			    gateDeptList  	   = eGateElement.getChildNodes();

                              			  for (int igateDeptList = 0; igateDeptList < gateDeptList.getLength(); igateDeptList++) {

                              				  nGateDeptDocument = gateDeptList.item(igateDeptList);

                                      		  if (nGateDeptDocument.getNodeType() == Node.ELEMENT_NODE) {

                                      			  eGateDeptElement = (Element) nGateDeptDocument;

                                                    strDepartmentName =    (String)eGateDeptElement.getAttribute("T");

                                                    strGetStatus      = (String) eGateDeptElement.getAttribute("GateClosureStatus");

                                                    strTaskType       = (String) eGateDeptElement.getAttribute("MSILTaskType"); 

                                                    strParentType     = (String) eGateDeptElement.getAttribute("parentObjectType");

                                      			

                                      			 if(alStatusRange.contains(strGetStatus) && strTaskType.equalsIgnoreCase("Gate Schedule") && strParentType.equalsIgnoreCase(ProgramCentralConstants.TYPE_GATE) && slUserDeptList.contains(strDepartmentName))

                                                 {

                                               	  intStausCount[alStatusRange.indexOf(strGetStatus)]++;

                                                 }

                                      			 //------------

                                      			  gateDeptDeliverablesList	=	eGateDeptElement.getChildNodes();

                                      			/*  for (int igateDeptDeliverablesList = 0; igateDeptDeliverablesList < gateDeptList.getLength(); igateDeptDeliverablesList++) 

                                      			  {

                                      				 nGateDeptDeliverablesDocument = gateDeptDeliverablesList.item(igateDeptDeliverablesList);

                                      				if (nGateDeptDeliverablesDocument !=null && nGateDeptDeliverablesDocument.getNodeType() == Node.ELEMENT_NODE) {

                                            			  eGateDeptDeliverablesElement = (Element) nGateDeptDeliverablesDocument;

                                                          String  strGetaStatus         = (String) eGateDeptDeliverablesElement.getAttribute("GateStatus");

                                                          if(alStatusRange.contains(strGetaStatus))

                                                          {

                                                        	  intStausCount[alStatusRange.indexOf(strGetaStatus)]++;

                                                          }

                                      				 }

                                      			  }*/

                                      		  }

                                           }

                              		   }

                              	   }

                                }

                           }

                       }

                   }

                   String strStatusRange     = DomainConstants.EMPTY_STRING;

                   for (int j = 0; j < alStatusRange.size(); j++) {

                       strStatusRange        = (String)alStatusRange.get(j);

                       projectMap.put(strStatusRange  , intStausCount[alStatusRange.indexOf(strStatusRange)]);

                   }

                   mlreturnList.add(projectMap);

                }

             }

           returnMap.put("GateStatusValue" , alStatusRange);

           returnMap.put("GetStatusData"   , mlreturnList);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return returnMap;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public int getTaskStatusSlip(Context context , Date estFinishDate , String strState) throws Exception

    {

        int returnValue = 0;

        Date sysDate    = new Date();

        sysDate.setHours(0); sysDate.setMinutes(0); sysDate.setSeconds(0);

        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        if (!"Complete".equals(strState) && sysDate.after(estFinishDate))

        {
        	// Code modified by Dheeraj Garg <05-Aug-2016> Slip days are different in WBS page and Dashboard - Start
            long lSlipDay = task.computeDuration(estFinishDate, sysDate) - 1; //take out the starting day
            // Code modified by Dheeraj Garg <05-Aug-2016> Slip days are different in WBS page and Dashboard - End

            returnValue   = (int) java.lang.Math.abs(lSlipDay);

        }

        return returnValue;

    }
	
	// Added by Vinit - Start 29-June-2018
	@com.matrixone.apps.framework.ui.ProgramCallable
    public int getTaskStatusSlipBasedOnEstimatedFinishDate(Context context, String sAttribEstEndDate, String sAttribEstStartDate, String strDuration, String sAttrbPrcComplete) throws Exception
    {
        int returnValue = 0;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        Date tempDate = new Date();

        Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
        
        Date estFinishDate = sdf.parse(sAttribEstEndDate);
		Date estStartDate  = sdf.parse(sAttribEstStartDate);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		
        String strSlipDays = "0";
		
		double dCurrentPC = Double.parseDouble(sAttrbPrcComplete);
		double dDuration  = Double.parseDouble(strDuration);
		
		System.out.println("......dCurrentPC........."+dCurrentPC);
		
		if(dCurrentPC>=100)
		{
			// do nothing
		}
		else if (sysDate.after(estStartDate))
		{
		
			java.text.DecimalFormat dd = new  java.text.DecimalFormat("0");
			
			long daysPassedBasedOnStartDate  = (long) task.computeDuration(estStartDate, sysDate);
			Long ldaysPassedBasedStartDate   = new Long(daysPassedBasedOnStartDate);
			
			double dPassedBasedOnStartDate   = ldaysPassedBasedStartDate.doubleValue();
  
			if(dCurrentPC==0)
			{
				strSlipDays = dd.format(dPassedBasedOnStartDate);
			}
			else
			{
				double dPerDayPCValue = 100.0/dDuration;
				double dPercentageShouldHaveBeenAsOfToday = dPerDayPCValue*dPassedBasedOnStartDate;
				
				if(dCurrentPC > dPercentageShouldHaveBeenAsOfToday)
				{
					// if current percent is more than the as of today's percentage.. then: slipdays = 0;
				}
				else{
					//java.text.DecimalFormat dd = new  java.text.DecimalFormat("0");
					double dPCForSlipDays = dPercentageShouldHaveBeenAsOfToday - dCurrentPC;
					double dSlipDays      = dPCForSlipDays / dPerDayPCValue;
					strSlipDays           = dd.format(dSlipDays);
				}
			}
		}
		else{
			// then, task is not yet started.
		}
		
		returnValue = Integer.parseInt(strSlipDays);
        return returnValue;

    }
// Added by Vinit - End 29-June-2018

	@com.matrixone.apps.framework.ui.ProgramCallable
    public Map getProjectStatusSummary(Context context , String [] args)

    {

    	Map returnMap 	=	 new HashMap();

    	try {

             Map programMap                     = (Map) JPO.unpackArgs(args);

             String strObjectId                 = (String) programMap.get("objectId");

             String strSelectedMilestone        = (String) programMap.get("milestone");

             StringList slSelectedMilestone     = FrameworkUtil.split(strSelectedMilestone, ",");

             File Projectfile   				=   getProjectFile(context, strObjectId);

             DocumentBuilderFactory dbFactory   = DocumentBuilderFactory.newInstance();

             DocumentBuilder dBuilder           = dbFactory.newDocumentBuilder();

             Document doc                       = null;

             doc = dBuilder.parse(Projectfile);

             doc.getDocumentElement().normalize();

             

             Element eElementProject = (Element)  doc.getElementsByTagName("Body").item(0);

             String strParcentComp	 =	eElementProject.getAttribute("percentComplete");

             String strSlipDays  	 =	eElementProject.getAttribute("SlipDays");

             String strStatus  	     =	"";;

             int iSlipDays           =	0;

             try {

            	 iSlipDays	=	Integer.parseInt(strSlipDays);

            	 if(iSlipDays>0)

            	 {

            		 strStatus	=	"Delayed";

            	 }

            	 else

            	 {

            		 strStatus	=	"On Time"; 

            	 }

            } 

            catch (Exception e) {

            }

             strSlipDays	=	getChildProjectSlipDays(doc);

             try {

            	 iSlipDays	=	Integer.parseInt(strSlipDays);

            	 if(iSlipDays>0)

            	 {

            		 strStatus	=	"Delayed";

            	 }

            	 else

            	 {

            		 strStatus	=	"On Time"; 

            	 }

            } 

            catch (Exception e) {

            }

             String strLateTask            = getLateTaskCount(doc);

             String strLateCritalTask      = getLateCritalTask (doc);

             String strMilestonecompleted  = getProjectMilestonecompleted (doc , slSelectedMilestone );

             

            returnMap.put("Progress",strParcentComp+ " %");

            returnMap.put("Slipped by", ""+iSlipDays);

            returnMap.put("Status", strStatus);

            returnMap.put("Delayed Tasks", strLateTask+ " %");

            returnMap.put("Delayed Critical Tasks", strLateCritalTask);

            returnMap.put("Milestones completed", strMilestonecompleted + " %");



            StringList slBarList 	=	new StringList();

            slBarList.add("Progress");

            slBarList.add("Status");

            slBarList.add("Slipped by");

            slBarList.add("Delayed Tasks");

            slBarList.add("Delayed Critical Tasks");

            slBarList.add("Milestones completed");

            returnMap.put("BarOrder", slBarList);

         }

         catch(Exception ex)

         {

        	 ex.printStackTrace();

         }

    	

    	return returnMap;

    }

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public String  getLateTaskCount(Document doc ) throws Exception

    {

        String strReturnValue     = DomainConstants.EMPTY_STRING;

        try {

              String isLeafTask   = DomainConstants.EMPTY_STRING;

              String strDelay     = DomainConstants.EMPTY_STRING;

              String strCurrent   = DomainConstants.EMPTY_STRING;

              String  isMasterProjectTask       = DomainConstants.EMPTY_STRING;



              NodeList nList      = doc.getElementsByTagName("I");

              Node nNode          = null;

              int totalTask       = 0;

              int DelaysTask      = 0;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      isLeafTask           = (String) eElement.getAttribute("Summery");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      {

                      if("Yes".equalsIgnoreCase(isLeafTask))

                      {

                          totalTask++;

                          strDelay         = (String) eElement.getAttribute("Delay");

                          strCurrent       = (String) eElement.getAttribute("Current");

                          if("Yes".equalsIgnoreCase(strDelay) &&  !"Complete".equals(strCurrent)) {

                             DelaysTask++;

                          }

                      }

                      }

                  }

              }

              double dParcentValue	=	DelaysTask*100.0/totalTask;

              strReturnValue= String.format("%.2f", dParcentValue);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return strReturnValue;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public String  getLateCritalTask(Document doc ) throws Exception

    {

        String strReturnValue         = DomainConstants.EMPTY_STRING;

        try {

              String isLeafTask       = DomainConstants.EMPTY_STRING;

              String strDelay         = DomainConstants.EMPTY_STRING;

              String strCurrent       = DomainConstants.EMPTY_STRING;

              String isCriticalTask   = DomainConstants.EMPTY_STRING;

              String  isMasterProjectTask       = DomainConstants.EMPTY_STRING;

              

              NodeList nList = doc.getElementsByTagName("I");

              Node nNode =null;

              int totalTask           = 0;

              int DelaysTask          = 0;

              for (int temp = 0; temp < nList.getLength(); temp++) {

                  nNode = nList.item(temp);

                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                      Element eElement     = (Element) nNode;

                      isLeafTask           = (String) eElement.getAttribute("Summery");

                      strDelay             = (String) eElement.getAttribute("Delay");

                      isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      //if("Yes".equalsIgnoreCase(isLeafTask))

                      {

                          strCurrent       = (String) eElement.getAttribute("Current");

                          isCriticalTask   = (String) eElement.getAttribute("isCriticalTask");

                          if("Yes".equalsIgnoreCase(strDelay) &&  !"Complete".equals(strCurrent) && "YES".equalsIgnoreCase(isCriticalTask) ) {

                              totalTask++;

                          }

                      }

                  }

              }

              strReturnValue= ""+totalTask;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return strReturnValue;

    }

    

    
	@com.matrixone.apps.framework.ui.ProgramCallable
    public String getProjectMilestonecompleted(Document doc , StringList slSelectedMilestone )throws Exception 

    {

        String strReturnValue           = DomainConstants.EMPTY_STRING;

        try {

              String strMilestoneName   = DomainConstants.EMPTY_STRING;

			  String strCurrent         = DomainConstants.EMPTY_STRING;

			  String strDelay           = DomainConstants.EMPTY_STRING;

			  String  isMasterProjectTask       = DomainConstants.EMPTY_STRING;

			  int iTotal                = 0;

			  int iClose                = 0;

			  NodeList nList            = doc.getElementsByTagName("I");

	          Node nNode                = null;

	          for (int temp = 0; temp < nList.getLength(); temp++) {

	              nNode = nList.item(temp);

	              if (nNode.getNodeType() == Node.ELEMENT_NODE) {

	                  Element eElement     = (Element) nNode;

	                  strMilestoneName     = (String) eElement.getAttribute("MilestoneName");

	                  strCurrent           = (String) eElement.getAttribute("Current");

	                  strDelay             = (String) eElement.getAttribute("Delay");

	                  isMasterProjectTask  = (String) eElement.getAttribute("isMasterProjectTask");

                      if("No".equalsIgnoreCase(isMasterProjectTask))

                      {

		                  if(!"".equals(strMilestoneName) && slSelectedMilestone.contains(strMilestoneName) && "Yes".equalsIgnoreCase(strDelay))

		                  {

		                      iTotal++;

		                      if("Complete".equals(strCurrent))

		                      {

		                         iClose++;

		                      }

		                  }

                      }

	              }

	          }

	          if(iTotal==0)

	          {

	        	  strReturnValue	=	"0";

	          }

	          else

	          {

	        	  Double parcentOfComp  = (double) (iClose*100/iTotal);

	        	  strReturnValue        = ""+ parcentOfComp;

	          }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return strReturnValue;

    }


	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getChildProject(Context context , String [] args) 

    {

       MapList mlReturnValue                  = new MapList();

        try {

            Map programMap                    = (Map) JPO.unpackArgs(args);

            String strProjectId               = (String) programMap.get("objectId");

            File Projectfile                  = getProjectFile(context, strProjectId);

            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            Document doc                      = null;

            doc                               = dBuilder.parse(Projectfile);

            doc.getDocumentElement().normalize();

            Node nNode                        = null;

            

            String strUserDepartment =  DomainConstants.EMPTY_STRING;

            StringList slUserDepartment = new StringList();

            

            NodeList nList = doc.getElementsByTagName("Body");          

            for (int temp = 0; temp < nList.getLength(); temp++) {

                nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    strUserDepartment = eElement.getAttribute("USER_DEPARTMENT");

                    if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strUserDepartment))

                  	  slUserDepartment = StringUtil.split(strUserDepartment, ",");

                }

            }

            

            String strType                    = DomainConstants.EMPTY_STRING;

            String strCurrent				  = DomainConstants.EMPTY_STRING;

            String strDepartment           	  = DomainConstants.EMPTY_STRING;

            nList                    = doc.getElementsByTagName("I");

            for (int temp = 0; temp < nList.getLength(); temp++) {

               nNode = nList.item(temp);

               if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                   Element eElement     = (Element) nNode;

                   strType              = (String) eElement.getAttribute("TYPE");

                   strCurrent			= (String) eElement.getAttribute("Current");

                   strDepartment	    = (String) eElement.getAttribute("Department");

                   if("Project Space".equals(strType) && strCurrent.equalsIgnoreCase(STATE_ACTIVE_POLICY_PROJECT_SPACE))

                   {

                	   if(slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT))

                	   {

	                      HashMap tempMap 	=	 getNodeAttributeValue(eElement);

	                      mlReturnValue.add(tempMap);

                	   }

                   }

               }

           }

        } catch (Exception e) {

            e.printStackTrace();

       }

        return  mlReturnValue;

    }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public String  getChildProjectSlipDays(Document doc ) 

    {

       MapList mlReturnValue                  = new MapList();

       String strReturn ="0";

        try {

            //Map programMap                    = (Map) JPO.unpackArgs(args);

            //String strProjectId               = (String) programMap.get("objectId");

            //File Projectfile                  = getProjectFile(context, strProjectId);

            //DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();

            //DocumentBuilder dBuilder          = dbFactory.newDocumentBuilder();

            //Document doc                      = null;

            //doc                               = dBuilder.parse(Projectfile);

            //doc.getDocumentElement().normalize();

            Node nNode                        = null;

            

            String strUserDepartment =  DomainConstants.EMPTY_STRING;

            StringList slUserDepartment = new StringList();

            

            NodeList nList = doc.getElementsByTagName("Body");          

            for (int temp = 0; temp < nList.getLength(); temp++) {

                nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    strUserDepartment = eElement.getAttribute("USER_DEPARTMENT");

                    if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strUserDepartment))

                  	  slUserDepartment = StringUtil.split(strUserDepartment, ",");

                }

            }

            

            String strType          = DomainConstants.EMPTY_STRING;

            String strCurrent       = DomainConstants.EMPTY_STRING;

            String strDepartment    = DomainConstants.EMPTY_STRING;

            nList                    = doc.getElementsByTagName("I");

            

            ArrayList<Integer> ilist 	= new ArrayList<Integer>();

            for (int temp = 0; temp < nList.getLength(); temp++) {

               nNode = nList.item(temp);

               if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                   Element eElement     = (Element) nNode;

                   strType              = (String) eElement.getAttribute("TYPE");
System.out.println("Inside getChildProjectSlipDays strType"+strType);
                   strCurrent           = (String) eElement.getAttribute("Current");
System.out.println("Inside getChildProjectSlipDays  strCurrent"+strCurrent);
                   strDepartment        = (String) eElement.getAttribute("Department");
System.out.println("Inside getChildProjectSlipDays strDepartment::"+strDepartment);
                   if("Project Space".equals(strType) && strCurrent.equalsIgnoreCase(STATE_ACTIVE_POLICY_PROJECT_SPACE))

                   {

                       if(slUserDepartment.contains(strDepartment) || slUserDepartment.contains(PROJECT_CREATE_DEPARTMENT))

                       {

                    	   String strSlipDays  	 =	eElement.getAttribute("SlipDays");

                    	   

                    	   ilist.add(Integer.parseInt(eElement.getAttribute("SlipDays")));
						               
System.out.println("Inside getChildProjectSlipDays ilist updated");

                          //HashMap tempMap     =     getNodeAttributeValue(eElement);

                          //mlReturnValue.add(tempMap);

                       }

                   }

               }

           }

            
System.out.println("Inside getChildProjectSlipDays ilist"+ilist);
            Integer iMaxValue = Collections.max(ilist);

            strReturn	=	iMaxValue.toString();

        } catch (Exception e) {

            e.printStackTrace();

       }

        return  strReturn;

    }



    /**

     * This method will return the Graphical Dashboard access.

     * 1. If the user is the Lead of Department\Division

     * 2. If the user is not DVM PE & not NPE DPM but owns at least one Active master project.

     * the command Graphical Dashboard will be visible only if the user is an owner of any Project

     * @param context

     * @param args

     * @return

     * @throws Exception

     */

    public Boolean hasAccessForGraphicalDashboard(Context context, String[] args) throws Exception

    {

    	boolean bHasAccess=false;



    	try{

    		String strContextUserId = PersonUtil.getPersonObjectID(context);

    		DomainObject doPerson = DomainObject.newInstance(context, strContextUserId);

    		MSILGraphicalDashboard_mxJPO progGraphicalDashboard = new MSILGraphicalDashboard_mxJPO(context, args);

    		Map mpUserInfo=progGraphicalDashboard.getUserList(context, doPerson);


            System.out.println("mpUserInfo.............."+mpUserInfo);

    		// If user is Project Member and doesn't have lead role

    		String strLeadResponsibility = (String) mpUserInfo.get("LeadResponsibility");

			String strUserRole = (String) mpUserInfo.get("UserRole");

    		if("Project Member".equals(strUserRole) && MSILUtils_mxJPO.isNullOrEmpty(strLeadResponsibility))

				return false;

    		

    		if(null !=mpUserInfo && !mpUserInfo.isEmpty()){

    			// Removed code by Dheeraj Garg <03-Aug-2016> Blank Dashboard for DPM/DVM, if no Active project.
				/*String strConnectedAsLead=(String)mpUserInfo.get("ConnectedAsLead");

    			if(${CLASS:MSILUtils}.isNotNullAndNotEmpty(strConnectedAsLead)){

    				bHasAccess=true;

    			}else{*///If user is not DVM PE and not DPM NPE but owns at least one Active master project.

					// Changed by Dheeraj Garg <13-Sep-2016> Graphical Dashboard option is not present on �DPO NPE� Enovia login. - Start
    				//MapList mlProjectsList = progGraphicalDashboard.getLoggedInPersonProjectList(context, mpUserInfo);
					MapList mlProjectsList = getLoggedInPersonProjectList(context, mpUserInfo);
					// Changed by Dheeraj Garg <13-Sep-2016> Graphical Dashboard option is not present on �DPO NPE� Enovia login. - End

                    System.out.println("mlProjectsList....................."+mlProjectsList);

    				Map mpMasterProjectInfo = progGraphicalDashboard.getMasterProjectsList(context, mlProjectsList, mpUserInfo);


                    System.out.println("mpMasterProjectInfo....................."+mpMasterProjectInfo);


    				if(null != mpMasterProjectInfo && !mpMasterProjectInfo.isEmpty()){

    					bHasAccess=true;

    				}

    			//}

    		}

    	}catch(Exception ex){

    		throw ex;

    	}

    	return bHasAccess;

    }

	

	//Added By Yaseen..

	/**

     * This method will return All the Milestone in the System

     * @param context

     * @return MapList

     * @throws Exception

     */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMilestoneList(Context context)

    {

    	MapList mlReturnList = new MapList();

    	StringList slObjectSelects = new StringList(DomainConstants.SELECT_ID);

    	slObjectSelects.add(DomainConstants.SELECT_NAME);

		slObjectSelects.add("attribute[Sequence Order]");

		String strWhere = "!name == None || name == none";

		

    	try

    	{

    		mlReturnList = DomainObject.findObjects(context, TYPE_MSIL_MILESTONE, context.getVault().toString(), strWhere, slObjectSelects);   		

    		

   		  	mlReturnList.sort("attribute[Sequence Order]", "ascending",  "integer");	

   		  	

    	}

    	catch(Exception ex)

    	{

    		ex.printStackTrace();

    	}  

    	return mlReturnList;

    }

		/**

     * This method checks whether the user has access to Deparment column or not..

     * @param context

     * @return boolean

     * @throws Exception

     */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean getColumnAccess(Context context , String [] args) throws Exception

    {

		boolean bReturnVal = false;

		 

    	try

    	{

    		HashMap programMap   = (HashMap)JPO.unpackArgs(args);

    		String strLeadResponsibility = (String) programMap.get("LeadResponsibility");

    		if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strLeadResponsibility))

    		{

    			if(strLeadResponsibility.equalsIgnoreCase("Business Unit"))

    				bReturnVal = true;

    		}

    	}

    	catch(Exception ex)

    	{

    		ex.printStackTrace();

    	}  

    	return bReturnVal;

    }

	/**

     * Gate Status column will be only visible in "Gate Closure Status" chart..

     * @param context

     * @return boolean

     * @throws Exception

     */

	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean getGateStatusColumn(Context context , String [] args) throws Exception

    {

		boolean bReturnVal = false;

		 

    	try

    	{    	

    		HashMap programMap   = (HashMap)JPO.unpackArgs(args);

    		String strGate = (String) programMap.get("gateStatusColumn");

    		if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strGate))

    		{

    			if(strGate.equalsIgnoreCase("show"))

    				bReturnVal=true;

    		}

    	}

    	catch(Exception ex)

    	{

    		ex.printStackTrace();

    	}  

    	return bReturnVal;

    }

	/**

     * Getting Gate Status value for "Gate Closure Status" chart..

     * @param context

     * @return String

     * @throws Exception

     */

	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String getGateClosureStatusValue(Context context,String strFromType ,String strTaskType,String strGateStatus,String strEstFinishDate) throws Exception

    {

		String strReturnVal = DomainConstants.EMPTY_STRING;

		 

    	try

    	{    	

    		if(strFromType.equalsIgnoreCase(ProgramCentralConstants.TYPE_GATE) && strTaskType.equalsIgnoreCase("Gate Schedule"))

    		{

    			//Then task is a Gate Schedule Task..

    			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

    			java.util.Date todayDate = new java.util.Date();

    			java.util.Date estFinishDate = new java.util.Date();

    				

    			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");		

    			

    			todayDate.setHours(0);

    			todayDate.setMinutes(0);

    			todayDate.setSeconds(0);

    		            			

    			estFinishDate  = sdf.parse(strEstFinishDate);

    	        estFinishDate.setHours(0);

    	        estFinishDate.setMinutes(0);

    	        estFinishDate.setSeconds(0); 

    	        

    	        if (estFinishDate.before(todayDate) && !strGateStatus.equalsIgnoreCase("Sign Off")) 

    			{

    	        	//Means task is delayed and the gate status is not set to sign off

    	        	strReturnVal = "Pending for Submission";

    			}

    	        else if(strGateStatus.equalsIgnoreCase("Sign Off"))

    	        {

    	        	strReturnVal = "Sign Off"; 

    	        }

    	        else if(strGateStatus.equalsIgnoreCase("Under Review"))

    	        {

    	        	strReturnVal = "Under Review"; 

    	        }

    	        else if(strGateStatus.equalsIgnoreCase("Correction"))

    	        {

    	        	strReturnVal = " Under Correction"; 

    	        }

    	        else if(strGateStatus.equalsIgnoreCase("Pending"))

    	        {

    	        	strReturnVal = "Due for completion";

    	        }

    	        else

    	        {

    	        	strReturnVal = "";

    	        }

    	        

    		}

    		else

    		{

    			strReturnVal = "";

    		}

    	}

    	catch(Exception ex)

    	{

    		ex.printStackTrace();

    	}  

    	return strReturnVal;

    }



    /**

     * This method is used for Command Should not be Visible For NPE Department People. 

     * @param context

     * @param args holds the arguments

     * @returns boolean Value.

     * @throws Exception if the operation fails

     */

    public boolean isWeekwiseReportVisible(Context context, String[] args)throws Exception{

        try{

            DomainObject doPerson =  new DomainObject(PersonUtil.getPersonObjectID(context));

            MSILGraphicalDashboard_mxJPO graphicalDashboard = new MSILGraphicalDashboard_mxJPO(context, args);

            Map mpUserMap = graphicalDashboard.getUserList(context, doPerson);

            String strLeadResponsibility = (String) mpUserMap.get("LeadResponsibility");

            String strConnectedAsLead = (String) mpUserMap.get("ConnectedAsLead");

// Added and Modifed by Dheeraj Garg <12-Aug-2016> Week Wise Report access to be given to NPE DPM & NPE Incharge -- Start
            if("Department".equals(strLeadResponsibility) || "Project Lead".equals(strLeadResponsibility))

            //if(("Department".equals(strLeadResponsibility) && !${CLASS:MSILConstants}.PROJECT_CREATE_DEPARTMENT.equals(strConnectedAsLead)) || "Project Lead".equals(strLeadResponsibility))
// Added and Modifed by Dheeraj Garg <12-Aug-2016> Week Wise Report access to be given to NPE DPM & NPE Incharge -- End
                return true;

        }catch(Exception ex){

            ex.printStackTrace();

            throw ex;

        }

        return false;

    }

	// Added and Modifed by Dheeraj Garg <12-Aug-2016> Week Wise Report access to be given to NPE DPM & NPE Incharge -- Start
    /**
     * This method is used for Task Pending for Approval Should be Visible or not. 
     * @param context
     * @param args holds the arguments
     * @returns boolean Value.
     * @throws Exception if the operation fails
     */
    public boolean isTaskPendingForApprovalVisible(Context context, String[] args)throws Exception{
        try{
            DomainObject doPerson =  new DomainObject(PersonUtil.getPersonObjectID(context));
            MSILGraphicalDashboard_mxJPO graphicalDashboard = new MSILGraphicalDashboard_mxJPO(context, args);
            Map mpUserMap = graphicalDashboard.getUserList(context, doPerson);
            String strLeadResponsibility = (String) mpUserMap.get("LeadResponsibility");
            String strConnectedAsLead = (String) mpUserMap.get("ConnectedAsLead");
            if(("Department".equals(strLeadResponsibility) && !MSILConstants_mxJPO.PROJECT_CREATE_DEPARTMENT.equals(strConnectedAsLead)) || "Project Lead".equals(strLeadResponsibility))
                return true;
        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }
        return false;
    }
    // Added and Modifed by Dheeraj Garg <12-Aug-2016> Week Wise Report access to be given to NPE DPM & NPE Incharge -- End
@com.matrixone.apps.framework.ui.ProgramCallable
	public void generateAndCheckInXML(Context context , String [] args)throws Exception 

	{

		try 

		{

			//System.out.println("Start -->> "+new Date());



			// fetch list of all users

			MapList mlPEPersonList = getPEPerson (context , args);

			StringList slAllPEPersonList = getMLToSL(mlPEPersonList);



			if(slAllPEPersonList.size() > 0)

			{

				int nAllPEPersonListSize = slAllPEPersonList.size();

				for(int nPersonCount = 0; nPersonCount < nAllPEPersonListSize; nPersonCount++)

				{

					// iterate over each user					

					String strPersonName = (String)slAllPEPersonList.get(nPersonCount);

					// set the context to user

					ContextUtil.pushContext(context, strPersonName, "", VAULT_ESERVICE_PRODUCTION);

					// check if user has access to graphical dashboard

					boolean bHasAccess = hasAccessForGraphicalDashboard(context, args);

					ContextUtil.popContext(context);

					if(bHasAccess)

					{

						try

						{						

							String strPathMQL      = MqlUtil.mqlCommand(context,"print person '"+strPersonName+"' select property[Dashboard_Path].value dump |");

							if(!"".equals(strPathMQL))

							{

								strPathMQL = strPathMQL.trim();


									if(strPathMQL!=null && !"".equals(strPathMQL))

									{

										MqlUtil.mqlCommand(context,"mod person '"+strPersonName+"' property Dashboard_Path value ''");

									}

							}

							// set the context to user

							ContextUtil.pushContext(context, strPersonName, "", VAULT_ESERVICE_PRODUCTION);

							String strWorkspacePath = context.createWorkspace();

							ContextUtil.popContext(context);



							ContextUtil.startTransaction(context,true);

							MqlUtil.mqlCommand(context,"mod Person '"+strPersonName+"' property Dashboard_Path value \""+strWorkspacePath+"\"");							

							ContextUtil.commitTransaction(context);



							// set the context to user

							ContextUtil.pushContext(context, strPersonName, "", VAULT_ESERVICE_PRODUCTION);

							genreteGDXML(context, args);	

							ContextUtil.popContext(context);

						}

						catch (Exception e) 

						{

							e.printStackTrace();

						}	

					}						

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		finally {

			//System.out.println("End -->> "+new Date());

		}

	}

}