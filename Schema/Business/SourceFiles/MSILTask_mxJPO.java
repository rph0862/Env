/* ${CLASSNAME}.java

Author : Intelizign
Change History:
Date       	   |	Change By 	 | Tag to be searched |		  Details
===========================================================================================================
24-June-2014   |    Intelizign	 |	     -   		  |      Initial Release
10-Mar-2015    |    Intelizign   |    10/03/2015	  |		 In case context user fetched is User Agent, make the task owner as the owner of the route
20-Mar-2015    |    Intelizign   |    10/03/2015	  |		 Project Name not shown on Inbox Task page when the task is created for Project Concept
15-Apr-2015    |    Intelizign   |    15/04/2015	  |		 Displaying Task hierarchy 
01-May-2015    |    Intelizign   |    1/05/2015       |          Assignee name to be visible in WBS View under a new column
03-May-2015    |    Intelizign   |    03/05/2015      |     CR - Route Stops on Rejection. Task still in revew state & 100% complete. Task cannot be tracked if user doen't change the %age completion.
03-May-2015    |    Intelizign   |    03/05/2015      |     CR - Stopped routes have to be explictly started for all tasks. Tracking has to be done for all such routes. Also route owner has to be tracked & restarted from his login.
05/04/2016     | Ajit            |  05/04/2016        | Not to show Hold and Cancel Projets As part of Gantt Chart
05/06/2016     | 	Dheeraj      |  05/06/2016        | Add Slip days column and critical task column in Gantt Chart
12-Feb-2016    | 	Intelizign   |  12-Feb-2016       | Changes for Phase 2
27-Jul-2016    |    Intelizign   |    27-Jul-2016	  |		 In case context user fetched is User Agent, make the assignee as the route owner, if no assignee found make the task owner as the owner of the route
20-Oct-2016    | 	Dheeraj Garg |  20-Oct-2016       | Not able to Attach Projects in Project concept.
02-Nov-2016    | 	Dheeraj Garg |  02-Nov-2016       | SCR - Route Approval Template on Task.
05-Dec-2016    |    Dheeraj Garg |  05-Dec-2016       | SCR - Ringi Integration
05-Apr-2017    |    Intelizign   |  05-Apr-2017       | Ringi Integration - Issue fix (Route not deleted if Task type is set to Ringi)
*/

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;


import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.Task;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.domain.util.DateUtil;

import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringItr;
import matrix.util.StringList;

import matrix.db.*;

import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MqlUtil;


import java.io.File;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class MSILTask_mxJPO implements MSILConstants_mxJPO
{

	public MSILTask_mxJPO (Context context, String[] args) throws Exception
	{

	}

	//Code add for CR - 1/05/2015 - Start
    public Vector getTaskAssignee(Context context, String[] args) throws Exception
   {
                try
                {
                    Map objectMap = null;
                    Vector taskAssignee = new Vector();
                    HashMap programMap = (HashMap) JPO.unpackArgs(args);
                    HashMap paramMap = (HashMap) programMap.get("paramList");
                    MapList objectList = (MapList)programMap.get("objectList");
                    int objectListSize = 0 ;
                    if(objectList != null)
                    {
                           objectListSize = objectList.size();
                    }
                    for(int i=0; i< objectListSize; i++)
                    {
                            try
                            {
                                    objectMap = (HashMap) objectList.get(i);
                            }
                            catch(ClassCastException cce)
                            {
                                    objectMap = (Hashtable) objectList.get(i);
                            }
                            String sTaskId = (String) objectMap.get(DomainObject.SELECT_ID);
                                int assigneeListSize = 0 ;
                                if(null!=sTaskId && !"".equals(sTaskId))
                                {
                                        DomainObject doTask = new DomainObject(sTaskId);
                                        StringList slAssignee = doTask.getInfoList(context,"to[Assigned Tasks].from.name");
                                        if(null!=slAssignee)
                                        {
                                           assigneeListSize = slAssignee.size();
                                        }
                                        StringList slFinalList = new StringList();
                                        
                                        if(null!=slAssignee && assigneeListSize>0)
                                        {
                                                for(int j=0;j<assigneeListSize;j++)
                                                {
                                                String sStaffId = (String) slAssignee.get(j);
                                                sStaffId = PersonUtil.getFullName(context, sStaffId);
                                                slFinalList.add(sStaffId);
                                                }
                                        }
                                String strAssignee = FrameworkUtil.join(slFinalList,",");
                                if(!"".equals(strAssignee))
                                {
                                        taskAssignee.add(strAssignee);
                                }
                                else
                                {
                                        taskAssignee.add("");
                                        }
                                }
                        }
                        return taskAssignee;
                }
                catch (Exception ex)
            {
                    System.out.println("Error in getTaskAssignee= " + ex.getMessage());
                    throw ex;
            }
   }
    //Code add for CR - 1/05/2015 - End

   /**
	 * Trigger method called on
	 * 	1) Task promotion from Create to Assign state (Promote Check trigger)
	 *
	 * This method checks for Route Template object to be connected to Project.
	 * If no active template is found, system will not allow task to be promoted to Assign state
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID - Object Id
	 *
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */
   public int checkApprovalTemplateExists(Context context, String[] args) throws FrameworkException
    {
        System.out.println("\n...JPO:MSILTask:checkApprovalTemplateExists start...");
        if (args == null || args.length < 1)
    {
            throw (new IllegalArgumentException());
        }
        try
        {
            String strTaskId = args[0];
            String languageStr = context.getSession().getLanguage();
            if(null != strTaskId && !"null".equalsIgnoreCase(strTaskId) && strTaskId.length() > 0)
            {
                DomainObject taskDO = DomainObject.newInstance(context, strTaskId);
                StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
                relSelects.addElement(DomainRelationship.SELECT_NAME);

                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                objectSelects.addElement(DomainConstants.SELECT_NAME);
                objectSelects.addElement("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");

                ContextUtil.pushContext(context);
                /*MapList projectList = taskDO.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_SUBTASK,
                        "*",
                        true,
                        false,
                        (short) 0,
                        objectSelects,
                        relSelects,
                        null,
                        null,
                        null,
                        DomainConstants.TYPE_PROJECT_SPACE,
                        null);*/
                // Added and Modified by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- Start
                StringList slTaskSelects = new StringList("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
                slTaskSelects.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");
                slTaskSelects.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
                
                // Added by Dheeraj Garg <20-Oct-2016> 88252 : Not able to Attach Projects in Project concept. -- Start
                Map mapTaskInfo = taskDO.getInfo(context, slTaskSelects);
                String strProjectType = (String) mapTaskInfo.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");
                if(DomainConstants.TYPE_PROJECT_CONCEPT.equals(strProjectType))
                    return 0;
                // Added by Dheeraj Garg <20-Oct-2016> 88252 : Not able to Attach Projects in Project concept. -- End
                
                String strConnectedRouteTemplate = (String) mapTaskInfo.get("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
                if(null == strConnectedRouteTemplate || "null".equals(strConnectedRouteTemplate) || "".equals(strConnectedRouteTemplate))
                {
                    strConnectedRouteTemplate = (String) mapTaskInfo.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
                }
                // Added and Modified by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- End
                ContextUtil.popContext(context);
                /*if(null != projectList && projectList.size() > 0)
                {
                    projectList.sort(DomainObject.SELECT_LEVEL, "ascending",  "integer");
                    // PROJECT MAP TO WHICH TASK IS CONNECTED
                    Map projectMap = (Map)projectList.get(0);
                    // FETCH CONNECTED ROUTE TEMPLATE FROM PROJECT
                    String strConnectedRouteTemplate = (String)projectMap.get("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");*/
                    // IF NO TEMPLATE IS FOUND, BLOCK THE PROMOTION OF PROJECT OBJECT
                    // IF TEMPLATE FOUND, PROMOTE THE PROJECT TO PROMOTE STATE
                    if(null != strConnectedRouteTemplate && strConnectedRouteTemplate.length() > 0)
                    {
                        return 0;
                    }
                    else
                    {
                        String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.ActiveTemplateForApproval", "emxProgramCentralStringResource", languageStr);
                        System.out.println("\nTrigger Blocked for Reason JPO:MSILTask:checkApprovalTemplateExists::"+strMessage);
                        MqlUtil.mqlCommand(context, "notice "+strMessage);
                        return 1;
                    }
                //}
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception JPO:MSILTask:checkApprovalTemplateExists :::: " + e);
            throw new FrameworkException(e);
        }
        System.out.println("\n... JPO:MSILTask:checkApprovalTemplateExists exit...");
        return 1;
    }

	/**
	 * This method will give name of parent task on WBS page
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *
	 * @returns Vector
	 * 			  name of parent task
	 * @throws Exception
	 *             if the operation fails
	 */
    public Vector getParentTaskName(Context context, String[] args) throws Exception
    {
        System.out.println("\n... JPO:MSILTask:getParentTaskName start...");
        Vector vecParentTaskName = new Vector();
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
        //15/04/2015 - Displaying Task hierarchy - start
            String strParentHierarchy = "";
            //15/04/2015 - Displaying Task hierarchy - End
            Iterator objectListIterator = objectList.iterator();
            int nCount = 0;
            String[] objIdArr = new String[objectList.size()];
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[nCount] = (String) objectMap.get(DomainObject.SELECT_ID);
                nCount++;
            }
            StringList busSelect = new StringList(1);
            busSelect.add(task.SELECT_ID);
        //15/04/2015 - Displaying Task hierarchy - Start
            busSelect.add("to[Project Access Key].from.id");
            //15/04/2015 - Displaying Task hierarchy - End
            busSelect.add(task.SELECT_NAME);
            
            MapList idList = DomainObject.getInfo(context, objIdArr, busSelect);
            int actionListSize = 0;
            if (idList != null)
            {
                actionListSize = idList.size();
            }
            busSelect = new StringList(2);
            busSelect.add(DomainConstants.SELECT_NAME);
            //15/04/2015 - Displaying Task hierarchy - Start
            busSelect.add("to[Project Access Key].from.id"); 
            //15/04/2015 - Displaying Task hierarchy - End
            busSelect.add(DomainConstants.SELECT_TYPE);
        for (nCount = 0; nCount < actionListSize; nCount++)

            {
                //15/04/2015 - Displaying Task hierarchy - Start
                strParentHierarchy  = "";
                //15/04/2015 - Displaying Task hierarchy - End
                objectMap = (Map) idList.get(nCount);



                String taskId = (String) objectMap.get(task.SELECT_ID);
                //15/04/2015 - Displaying Task hierarchy - Start
                String strTaskPALId = (String) objectMap.get("to[Project Access Key].from.id");
                //15/04/2015 - Displaying Task hierarchy - Start

                task.setId(taskId);

                //15/04/2015 - Code commented for Displaying Task hierarchy - start
                //MapList parentList = task.getParentInfo(context, 1, busSelect);
                //15/04/2015 - Code commented for Displaying Task hierarchy - End
                //15/04/2015 - Displaying Task hierarchy - start
                ContextUtil.pushContext(context);
                MapList parentList = task.getParentInfo(context, 0, busSelect);
                ContextUtil.popContext(context);
                //15/04/2015 - Displaying Task hierarchy - End


                if(parentList.size() > 0)

                {
                //15/04/2015 - Displaying Task hierarchy - start
                int parentTaskListSize = 0;
                if (parentList != null)
                {
                    parentTaskListSize = parentList.size();
                }
                for(int nParentTaskCount = 0; nParentTaskCount < parentTaskListSize; nParentTaskCount++)
                {
                    //15/04/2015 - Displaying Task hierarchy - End
                    //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                    //Map map = (Map) parentList.get(0);
                    //15/04/2015 - Code commented for Displaying Task hierarchy - End
                    //15/04/2015 - Displaying Task hierarchy - Start
                    Map map = (Map) parentList.get(nParentTaskCount);
                    String strParentTaskPALId = (String)map.get("to[Project Access Key].from.id");
                    //15/04/2015 - Displaying Task hierarchy - End

                    String pName = (String) map.get(DomainConstants.SELECT_NAME);

                    String pType = (String) map.get(DomainConstants.SELECT_TYPE);
                    //15/04/2015 - Displaying Task hierarchy - start
                    if(null != strParentTaskPALId && strTaskPALId.equalsIgnoreCase(strParentTaskPALId))
                    {
                        if(null!=pType && !"".equals(pType) && !pType.equals("Project Space") && !pType.equals("Project Concept"))
                        {
                            if(null != strParentHierarchy && strParentHierarchy.length() > 0)
                                strParentHierarchy = pName + " --> " + strParentHierarchy;
                            else
                                strParentHierarchy = pName;
                        }
                    }
                    //15/04/2015 - Displaying Task hierarchy - End

                    //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                    /*if ((pName == null) || "#DENIED!".equals(pName))
                    {
                        pName = "";
                    }
                    else
                    {
                        if(null != pType && (DomainConstants.TYPE_PROJECT_SPACE.equals(pType)))
                            pName = "<img src=\"../common/images/iconSmallProject.gif\">" + pName;
                        else
                            pName = "<img src=\"../common/images/iconSmallTask.gif\">" + pName;
                    }*/
                    //15/04/2015 - Code commented for Displaying Task hierarchy - End
                    
                    //15/04/2015 - Displaying Task hierarchy - start
                    if ((strParentHierarchy == null) || "#DENIED!".equals(strParentHierarchy))
                    {
                        strParentHierarchy = "";
                    }
                    else
                    {
                        strParentHierarchy = strParentHierarchy;
                    }
                    //15/04/2015 - Displaying Task hierarchy - End
                   }
                   //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                   //vecParentTaskName.add(pName);
                   //15/04/2015 - Code commented for Displaying Task hierarchy - End
                   //15/04/2015 - Displaying Task hierarchy - Start
                  vecParentTaskName.add(strParentHierarchy);
                  //15/04/2015 - Displaying Task hierarchy - End
                }
                else
                    vecParentTaskName.add("");
            }
        }
        catch (Exception ex)
        {
            System.out.println("\n... JPO:MSILTask:getParentTaskName ex..."+ex);
            throw ex;
        }
        finally
        {
            System.out.println("\n... JPO:MSILTask:getParentTaskName exit...");
            return vecParentTaskName;
        }

    }

/**
     * getParentTask - Retrives the Tasks parent objects
     * Inbox Task - Route
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
  public Vector getParentTask(Context context, String[] args) throws Exception
   {
       String objectRouteIdSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.id";
       String objectRouteTypeSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.type";
       String objectRouteNameSelectStr ="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.name";
       com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
       try
       {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap paramMap = (HashMap) programMap.get("paramList");
           MapList objectList = (MapList)programMap.get("objectList");
       Map objectMap = null;
           Vector showRoute = new Vector();
           String statusImageString = "";
           String sRouteString = "";
           boolean isPrinterFriendly = false;
           String strPrinterFriendly = (String)paramMap.get("reportFormat");
           String languageStr = (String)paramMap.get("languageStr");
           String sAccDenied = i18nNow.getI18nString( "emxComponents.Common.AccessDenied","emxComponentsStringResource",languageStr);
           //15/04/2015 - Displaying Task hierarchy - Start
           String strParentHierarchy = "";
           //15/04/2015 - Displaying Task hierarchy - End
           if (strPrinterFriendly != null )
           {
               isPrinterFriendly = true;
           }
           int objectListSize = 0 ;
           if(objectList != null)
           {
               objectListSize = objectList.size();
           }
           for(int i=0; i< objectListSize; i++)
           {
               statusImageString = "";
               sRouteString = "";
               try
               {
                   objectMap = (HashMap) objectList.get(i);
               }
               catch(ClassCastException cce)
               {
                   objectMap = (Hashtable) objectList.get(i);
               }
               String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);
               //15/04/2015 - Displaying Task hierarchy - start
               String sTypeId = (String) objectMap.get(DomainObject.SELECT_ID);
               String sName = (String) objectMap.get(DomainObject.SELECT_NAME);
               DomainObject doChildTask = new DomainObject(sTypeId);
               ContextUtil.pushContext(context);
               String strTaskPALId = (String) doChildTask.getInfo(context,"from[Route Task].to.to[Object Route].from.to[Project Access Key].from.id");
               ContextUtil.popContext(context);
               //15/04/2015 - Displaying Task hierarchy - End
               String sObjectId = "";
               String sObjectName = "";

               String sObjectType = "";
               //15/04/2015 - Displaying Task hierarchy - start
               strParentHierarchy = "";
               //15/04/2015 - Displaying Task hierarchy - End



               sObjectId   =(String)objectMap.get(objectRouteIdSelectStr);

               sObjectName = (String)objectMap.get(objectRouteNameSelectStr);

               sObjectType = (String)objectMap.get(objectRouteTypeSelectStr);

               StringList busSelectList = new StringList(3);

               busSelectList.add(DomainConstants.SELECT_NAME);
               //15/04/2015 - Displaying Task hierarchy - start
               busSelectList.add(DomainConstants.SELECT_TYPE);
               busSelectList.add("to[Project Access Key].from.id");
               //15/04/2015 - Displaying Task hierarchy - End


               if(sObjectId != null && sObjectName != null )

               {
                    
                   if(null != sObjectType && DomainConstants.TYPE_TASK.equals(sObjectType))

                   {

                       task.setId(sObjectId);

                       //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                       //MapList parentList = task.getParentInfo(context, 1, busSelectList);
                       //15/04/2015 - Code commented for Displaying Task hierarchy - End
                       //15/04/2015 - Displaying Task hierarchy - Start
                       ContextUtil.pushContext(context);
                       MapList parentList = task.getParentInfo(context, 0, busSelectList);
                       ContextUtil.popContext(context);
                       //15/04/2015 - Displaying Task hierarchy - End

                       if(parentList.size() > 0)

                       {
                        //15/04/2015 - Displaying Task hierarchy - Start
                       int parentTaskListSize = 0;
                        if (parentList != null)
                        {
                            parentTaskListSize = parentList.size();
                        }
                        for(int nParentTaskCount = 0; nParentTaskCount < parentTaskListSize; nParentTaskCount++)
                        {
                           //15/04/2015 - Displaying Task hierarchy - End
                           //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                           //Map map = (Map) parentList.get(0);
                           //15/04/2015 - Code commented for Displaying Task hierarchy - End
                           //15/04/2015 - Displaying Task hierarchy - Start
                           Map map = (Map) parentList.get(nParentTaskCount);
                           String strParentTaskPALId = (String)map.get("to[Project Access Key].from.id");
                           //15/04/2015 - Displaying Task hierarchy - End


                           String pName = (String) map.get(DomainConstants.SELECT_NAME);
                           //15/04/2015 - Displaying Task hierarchy - Start
                           String pType = (String) map.get(DomainConstants.SELECT_TYPE);
                           if(null != strParentTaskPALId && strTaskPALId.equalsIgnoreCase(strParentTaskPALId))
                           {
                               if(null!=pType && !"".equals(pType) && !pType.equals("Project Space") && !pType.equals("Project Concept"))
                               {
                                   if(null != strParentHierarchy && strParentHierarchy.length() > 0)
                                        strParentHierarchy = pName + " --> " + strParentHierarchy;
                                    else
                                        strParentHierarchy = pName;
                               }
                           }
                            //15/04/2015 - Displaying Task hierarchy - End

                            //15/04/2015 - Code commented for Displaying Task hierarchy - Start
                           /*if (null == pName || "#DENIED!".equals(pName))
                           {
                               sRouteString = sAccDenied;
                           }
                           else
                           {
                               sRouteString = pName;
                           }*/
                           //15/04/2015 - Code commented for Displaying Task hierarchy - End
                           
                           //15/04/2015 - Displaying Task hierarchy - Start
                           if (null == strParentHierarchy || "#DENIED!".equals(strParentHierarchy))
                           {
                               sRouteString = sAccDenied;
                           }
                           else
                           {
                               sRouteString = strParentHierarchy;
                           }
                           //15/04/2015 - Displaying Task hierarchy - End
                         }
                        
                       }
                       else
                           sRouteString = sAccDenied;
                   }
                    //15/04/2015 - Code Commented for Displaying Task hierarchy - Start
                   /*else if (null != sObjectType && DomainConstants.TYPE_PROJECT_SPACE.equals(sObjectType))
                   {
                       sRouteString = sObjectName;
                   }*/
                   //15/04/2015 - Code Commented for Displaying Task hierarchy - End
                   showRoute.add(sRouteString);
               }
               else
               {
                   showRoute.add(sAccDenied);
               }
           }
           return showRoute;
       }
       catch (Exception ex)
       {
           System.out.println("Error in getParentTask= " + ex.getMessage());
           throw ex;
       }
   }

 public Vector getParentProject(Context context, String[] args) throws Exception

   {

       String objectRouteIdSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.id";

       String objectRouteTypeSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.type";

       String objectRouteNameSelectStr ="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.name";

       com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

       try

       {

           HashMap programMap = (HashMap) JPO.unpackArgs(args);

           HashMap paramMap = (HashMap) programMap.get("paramList");

           MapList objectList = (MapList)programMap.get("objectList");



           Map objectMap = null;

           Vector showRoute = new Vector();

           String sRouteString = "";

           String languageStr = (String)paramMap.get("languageStr");



           String sAccDenied = i18nNow.getI18nString( "emxComponents.Common.AccessDenied","emxComponentsStringResource",languageStr);

        

           int objectListSize = 0 ;

           if(objectList != null)

           {

               objectListSize = objectList.size();

           }

           for(int i=0; i< objectListSize; i++)

           {

               sRouteString = "";

               try

               {

                   objectMap = (HashMap) objectList.get(i);

               }

               catch(ClassCastException cce)

               {

                   objectMap = (Hashtable) objectList.get(i);

               }



               String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);

               String sObjectId = "";

               String sObjectName = "";
           String sObjectType = "";
               sObjectId   =(String)objectMap.get(objectRouteIdSelectStr);
               sObjectName = (String)objectMap.get(objectRouteNameSelectStr);
               sObjectType = (String)objectMap.get(objectRouteTypeSelectStr);
               if(sObjectId != null && sObjectName != null )
               {
                   if(null != sObjectType && DomainConstants.TYPE_TASK.equals(sObjectType))
                   {
                       DomainObject taskDO = DomainObject.newInstance(context, sObjectId);
                       String busSelect = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
                   String strProjectName = taskDO.getInfo(context, busSelect);
                       if(null == strProjectName || strProjectName.length() <= 0)
                           sRouteString = sAccDenied;
                       else
                           sRouteString = strProjectName;
                   }
                   else if (null != sObjectType && (DomainConstants.TYPE_PROJECT_SPACE.equals(sObjectType) || DomainConstants.TYPE_PROJECT_CONCEPT.equals(sObjectType)))
                   {
                       sRouteString = sObjectName;
                   }
                   showRoute.add(sRouteString);
               }
               else
               {
                   showRoute.add(sAccDenied);
               }
           }
           return showRoute;
       }
       catch (Exception ex)
       {
           System.out.println("Error in getParentProject= " + ex.getMessage());
           throw ex;
           }
    }

    /**

     * Trigger method called on
     *  1) Task promotion from Assign to Active state (Promote Action trigger)
     *  2) Task promotion from Active to Review state (Promote Action trigger)
     *
     * This method will change the owner of the route and set the context user as the owner of the route
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args:
     *            0 - Object Id
     *            1 - From State
     *            2 - To state
     *
     * @returns 0 for success and 1 for trigger failure
     * @throws Exception
     *             if the operation fails
     *
     */
       public int changeRouteOwner(Context context, String[] args) throws FrameworkException
    {
        System.out.println("\n... JPO:MSILTask:changeRouteOwner start...");
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        try
        {
            String languageStr = context.getSession().getLanguage();
            String strObjectId = args[0];
            if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
            {
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                objectSelects.addElement(DomainConstants.SELECT_CURRENT);
                String strContextUser = context.getUser();
                MapList mlRoutesList = null;
                DomainObject objectDO = DomainObject.newInstance(context, strObjectId);
                // 10/03/2015 - Route Owner Issue - Start
                String strObjOwner = objectDO.getInfo(context, "owner");
                // MSIL start - 27-Jul-2016	- In case context user fetched is User Agent, make the assignee as the route owner, if no assignee found make the task owner as the owner of the route
                StringList slAssigneeList = (StringList)objectDO.getInfoList(context, "to[" + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name");
                String strAssignee = "";
                // MSIL end - 27-Jul-2016	- In case context user fetched is User Agent, make the assignee as the route owner, if no assignee found make the task owner as the owner of the route
                // 10/03/2015 - Route Owner Issue - End
                // GET ROUTE THAT IS IN DEFINED STATE FROM OBJECT
                ContextUtil.pushContext(context);
            mlRoutesList = objectDO.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                        DomainConstants.TYPE_ROUTE,
                    objectSelects,
                        null,
                        true,
                        true,
                        (short) 1,
                        null,
                        null);
                // IF NO DEFINED STATE ROUTE IS PRESENT, USER WILL RESUME THE ROUTE
                // ELSE START THE ROUTE
                if (null != mlRoutesList || !mlRoutesList.isEmpty() || mlRoutesList.size() > 0)
                {
                    Iterator itrRouteList = mlRoutesList.iterator();
                    while (itrRouteList.hasNext())
                    {
                        Map routeMap = (Map) itrRouteList.next();
                        String strRouteState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
                        if("Define".equalsIgnoreCase(strRouteState))
                        {
                            String strRouteId = (String) routeMap.get(DomainConstants.SELECT_ID);
                            // 10/03/2015 - Route Owner Issue - Start
                            if ("User Agent".equals(strContextUser))
                            {
                            	// MSIL start - 27-Jul-2016	- In case context user fetched is User Agent, make the assignee as the route owner, if no assignee found make the task owner as the owner of the route
                            	if(null != slAssigneeList && slAssigneeList.size() > 0)
								{
									strAssignee = (String)slAssigneeList.get(0); // in case of multiple assignees, make any one assignee as the route owner
									strContextUser = strAssignee;
								}
                            	// MSIL end - 27-Jul-2016	- In case context user fetched is User Agent, make the assignee as the route owner, if no assignee found make the task owner as the owner of the route
                            	else if (null != strObjOwner && strObjOwner.length() > 0)
                                    strContextUser = strObjOwner;                               
                            }
                            // 10/03/2015 - Route Owner Issue- End
                            MQLCommand mql = new MQLCommand();
                            String sMql = "mod bus " + strRouteId + " owner '" + strContextUser + "'";
                            boolean bResult = mql.executeCommand(context, sMql);
                            return 0;
                        }
                    }
                }
                ContextUtil.popContext(context);
            }
        } catch (Exception e) {
            System.out.println("Exception JPO:MSILTask:changeRouteOwner:::: " + e);
            throw new FrameworkException(e);
        }
        System.out.println("\n... JPO:MSILTask:changeRouteOwner exit...");
        return 0;
    }

    /**
     * Trigger method called on
     *  1) Attribute 'Approval Status' is updated to Reject. (Attribute Modify Action)
     *
     * This method will send notification to users when a task is rejected.
     * The notification will be sent to Task Assignee & person who has completed the task till now.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args:
     *            0 - Object Id
     *            1 - New Attribute value
     * @return
     * @returns true for sucess and false for trigger failure
     * @throws Exception
     *             if the operation fails
     *
     */
    public int notifyTaskOwners(Context context, String[] args) throws FrameworkException
    {
        System.out.println("\n... JPO:MSILTask:notifyTaskOwners start...");
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        try
        {
            DomainConstants.MULTI_VALUE_LIST.add("from[Route Task].to.to[Route Task].from.owner");
            DomainConstants.MULTI_VALUE_LIST.add("from[Route Task].to.to[Object Route].from.to[" + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name");
            StringList slSelectList = new StringList(3);
            slSelectList.addElement("name");
            slSelectList.addElement("from[Route Task].to.to[Object Route].from.type");
            slSelectList.addElement("from[Route Task].to.to[Route Task].from.owner");
            slSelectList.addElement("from[Route Task].to.to[Object Route].from.to[" + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name");
            // Form the subject and body
            String languageStr = context.getSession().getLanguage();
            String strSubject = i18nNow.getI18nString("emxProgramCentral.Notification.NotifyTaskAsignee.TaskRejectedSubject", "emxProgramCentralStringResource", languageStr);
            String strBody = i18nNow.getI18nString("emxProgramCentral.Notification.NotifyTaskAsignee.TaskRejected", "emxProgramCentralStringResource", languageStr);
            String strInboxTaskId = args[0];
            if(null != strInboxTaskId && !"null".equalsIgnoreCase(strInboxTaskId) && strInboxTaskId.length() > 0)
            {
                String strAttributeValue = args[1];
                if("Reject".equalsIgnoreCase(strAttributeValue))
                {
                    DomainObject inboxTaskDO = DomainObject.newInstance(context, strInboxTaskId);
                    ContextUtil.pushContext(context);
                    Map inboxTaskDetailMap = inboxTaskDO.getInfo(context, slSelectList);
                    ContextUtil.popContext(context);
                    if(null != inboxTaskDetailMap && inboxTaskDetailMap.size() > 0)
                    {
                        String strObjectRouteType = (String)inboxTaskDetailMap.get("from[Route Task].to.to[Object Route].from.type");
                        if(null != strObjectRouteType && strObjectRouteType.length() > 0 && TYPE_PROJECT_TASK.equalsIgnoreCase(strObjectRouteType))
                        {
                            StringList toList = new StringList();
                            StringList ccList = new StringList();
                            StringList slAssigneeList = (StringList)inboxTaskDetailMap.get("from[Route Task].to.to[Object Route].from.to[" + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name");
                            StringList slInboxTaskOwnersList = (StringList)inboxTaskDetailMap.get("from[Route Task].to.to[Route Task].from.owner");
                            StringBuffer messageBody = new StringBuffer();
                            messageBody.append("'");
                            messageBody.append((String)inboxTaskDetailMap.get("name"));
                            messageBody.append("' ");
                            messageBody.append(strBody);
                            if(null != slAssigneeList && slAssigneeList.size() > 0)
                                toList.addAll(slAssigneeList);
                            if(null != slInboxTaskOwnersList && slInboxTaskOwnersList.size() > 0)
                                toList.addAll(slInboxTaskOwnersList);
                            if(null != toList && toList.size() > 0)
                            {
                                MailUtil.sendNotification(context,
                                        toList, //toList
                                        ccList, //ccList
                                        null,   //bccList
                                        strSubject, //subjectKey
                                        null, //subjectKeys
                                        null, //subjectValues
                                        messageBody.toString(), //messageKey
                                        null, //messageKeys
                                        null, //messageValues
                                        new StringList(strInboxTaskId), //objectIdList
                                        null); //companyName
                            }
                            else
                            {
                                // send mail to PM support
                                MailUtil.sendNotification(context,
                                        new StringList(PM_SUPPORT_EMAIL),   // To List
                                        null,     // Cc List
                                        null,     // Bcc List
                                        "emxComponents.Message.Subject.MailSendFailed",  // Subject key
                                        null,                                       // Subject keys
                                        null,                                       // Subject values
                                    messageBody.toString(),  // Message key
                                    null,         // Message keys
                                        null,         // Message values
                                        new StringList(strInboxTaskId), // Object list
                                        null,         // company name
                                        "emxComponentsStringResource");     // Property file
                            }
                        }
                    }
                }
            }
            DomainConstants.MULTI_VALUE_LIST.remove("from[Route Task].to.to[Route Task].from.owner");
            DomainConstants.MULTI_VALUE_LIST.remove("from[Route Task].to.to[Object Route].from.to[" + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name");
        }
        catch (Exception e) {
            System.out.println("Exception JPO:MSILTask:notifyTaskOwners:::: " + e);
            throw new FrameworkException(e);
        }
        System.out.println("\n...JPO:MSILTask:notifyTaskOwners exit...");
        return 0;
    }

    /**
     * Trigger method called on
     *  1) Task promotion from Active to Review state (Promote Action trigger)
     *
     * This method will start the route that was created on promotion of Task Object from Create to Assign state.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args:
     *            0 - Object Id
     *            1 - From State
     *            2 - To state
     *
     * @returns 0 for success and 1 for trigger failure
     * @throws Exception
     *             if the operation fails
     *
     */

    public int startRouteOnTask(Context context, String[] args) throws FrameworkException
    {
        System.out.println("\n... JPO:MSILTask:startRouteOnTask start...");
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        try
        {
            String languageStr = context.getSession().getLanguage();
            String strObjectId = args[0];
            if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
            {
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                objectSelects.addElement(DomainConstants.SELECT_CURRENT);
                String strContextUser = context.getUser();
                MapList mlRoutesList = null;
                DomainObject objectDO = DomainObject.newInstance(context, strObjectId);
                // GET ROUTE THAT IS IN DEFINED STATE FROM OBJECT
                ContextUtil.pushContext(context);
                mlRoutesList = objectDO.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                        DomainConstants.TYPE_ROUTE,
                        objectSelects,
                        null,
                        true,
                        true,
                        (short) 1,
                        null,
                        null);
                // IF NO DEFINED STATE ROUTE IS PRESENT, USER WILL RESUME THE ROUTE
                // ELSE START THE ROUTE
                if(null!= mlRoutesList && !mlRoutesList.isEmpty())
                {
                    Iterator itrRouteList = mlRoutesList.iterator();
                    while (itrRouteList.hasNext())
                    {
                        Map routeMap = (Map) itrRouteList.next();
                        String strRouteState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
                        // if define state route exists, start the route
                        // if In-process route exists, do nothing
                        // if complete/archive state route exists, report to Admin
                        if("Define".equalsIgnoreCase(strRouteState))
                        {
                            emxChange_mxJPO changeJPO = new emxChange_mxJPO(context, args);
                            String strRouteId = (String) routeMap.get(DomainConstants.SELECT_ID);
                            try
                            {
                                /*MQLCommand mql = new MQLCommand();
                                String sMql = "mod bus " + strRouteId + " owner '" + strContextUser + "'";
                                boolean bResult = mql.executeCommand(context, sMql);
                                if(bResult)*/
                                    changeJPO.startRoute(context, strRouteId);
                            } catch (Exception ex) {
                                System.out.println("Exception JPO:MSILTask:startRouteOnTask starting route !!:::: " + ex);
                                throw new FrameworkException(ex);
                            }
                            return 0;
                        }
                        else if("Complete".equalsIgnoreCase(strRouteState) || "Archive".equalsIgnoreCase(strRouteState))
                        {
							//do nothing........
                            //String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.NoRouteOnObject", "emxProgramCentralStringResource", languageStr);
                            //System.out.println("Trigger Blocked for Reason JPO:MSILTask:startRouteOnTask::"+strMessage);
                            //MqlUtil.mqlCommand(context, "notice "+strMessage);
                            //return 1;
							//continue;
                        }
                        else
                        {
                            // do nothing
                                                        //Modified for CR -  03/05/2015 Start
                                                        // Stopped routes do not have to be explicitly started for all tasks
                                                        String strRouteIds = (String) routeMap.get(DomainConstants.SELECT_ID);
                                                        com.matrixone.apps.common.Route route = new com.matrixone.apps.common.Route(strRouteIds);
                                                        route.resume(context);
                                                        //Modified for CR -  03/05/2015 End
                        }
                    }
                }
                else
                {
                    String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.NoRouteOnObject", "emxProgramCentralStringResource", languageStr);
                    System.out.println("\nTrigger Blocked for Reason JPO:MSILTask:startRouteOnTask::"+strMessage);
                    MqlUtil.mqlCommand(context, "notice "+strMessage);
                    return 1;
                }
                ContextUtil.popContext(context);
            }
        } catch (Exception e) {
            System.out.println("Exception JPO:MSILTask:startRouteOnTask:::: " + e);
            throw new FrameworkException(e);
        }
        System.out.println("\n... JPO:MSILTask:startRouteOnTask exit...");
        return 0;
    }


 public Map getProjectTaskXML (Context context , String [] args)
    {
        Map returnMap                   = new HashMap();
        ArrayList alAssignList          =  new ArrayList();
        String strDuration              = "";
        String sAttrbPrcComplete        = "";
        String sEstStartDate            = "";
        String sEstEndDate              = "";
        String strDependentTasksDetail  = "";
        String strTaskName              = "";
        String strTaskId                = "";
        String sAttribEstStartDate      = "";
        String sAttribEstEndDate        = "";
        String strParentSubtaskId       = "";
        String strDependentTasksId      = "";
        String strTaskType              = "";
        String strDependentTaskId       = "";
        String strDependencyType        = "";
        String strAssignName            = "";
        String strLastCode              = "";
        String strActualStartDate       = "";
        String strActualEndDate         = "";
        String strLevel                 = "";
        String strParentCode            = "";
        String strProjectWBSCode        = "";
        String strDependentNumbers      = "";
        String strDependentChild        = "";
        String strTitle                 = "";
        String isTaskLeafLevel          = "";
        String strReasonfordelay        = "";
        String strActionPlanforcatchup  = "";
        String strComments              = "";
        String strIcon                  = "";
        String strGANTTGanttIcons       = "";
        String strGANTTGanttClass       = "";
        String strBGFormatcolor         = "";
        String strUserName              = "";
        // 05/06/2016 - Added for SCR - Add Critical Task filter - start
        String strCriticalTask	        = "";
        String strMSILCriticalTask	        = "";
        String strOOTBCriticalTask	        = "";
        String isCriticalTaskFlag 		= "";
        // 05/06/2016 - Added for SCR - Add Critical Task filter - end
        // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
        String strProjectState         = "";
        String strProjectStateAssociatedToTask = "";        
        // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End


        HashMap mapParentMap            = new HashMap();
        Map projectTaskMap              = new HashMap();
        StringList dependentTaskIdsList = new StringList();
        StringList dependentTypeList    = new StringList();
        StringList slPersonList         = new StringList();
        Date sJavaDate                  = null;
        // 05/06/2016 - Added for SCR - Add Slip days filter - start
        Date attrEstEndDate             = null;
        String strTaskState             = "";
        // 05/06/2016 - Added for SCR - Add Slip days filter - end
        Map mapDepdency                 = null;
        int nDependentTaskListSize      = 0;
        try
        {
            DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].from.name");

            HashMap programMap       = (HashMap) JPO.unpackArgs(args);
            String strProjectId      = (String)programMap.get("objectId");
            String strShowResource   = (String)programMap.get("ShowResource");
            String strShowDependency = (String)programMap.get("showDependency");
            String strFileName       = (String)programMap.get("FileName"); 
            String strWorkSpace      = (String)programMap.get("workSpace"); 

            com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

            StringList slObjectSelect = new StringList(16);
            slObjectSelect.add(DomainConstants.SELECT_NAME);
            slObjectSelect.add(DomainConstants.SELECT_ID);
            slObjectSelect.add(DomainConstants.SELECT_LEVEL);
            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");
            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");
            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");
            slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]");
            slObjectSelect.add(task.SELECT_PREDECESSOR_TYPES);
            slObjectSelect.add(task.SELECT_PREDECESSOR_IDS);
            slObjectSelect.add("to[Subtask].from.id"); // fetch parent of task
            slObjectSelect.add("from[Subtask]"); // child Task
            slObjectSelect.add("to[Assigned Tasks].from.name"); // 
            slObjectSelect.add("attribute[MSILReasonfordelay].value");
            slObjectSelect.add("attribute[MSILActionPlanforcatchup].value");
            slObjectSelect.add("attribute[Comments].value");
            // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
            slObjectSelect.add(DomainConstants.SELECT_CURRENT);
            slObjectSelect.add("to[Project Access Key].from.from[Project Access List].to.current");         
            // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
            // 05/06/2016 - Added for SCR - Add Critical Task filter - start
            slObjectSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
            slObjectSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_OOTB_CRITICAL_TASK);
            // 05/06/2016 - Added for SCR - Add Critical Task filter - end


            //slObjectSelect.add(SELECT_TASK_ACTUAL_START_DATE); // 
            //slObjectSelect.add(SELECT_TASK_ACTUAL_FINISH_DATE); // 

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            StringList slRelSelect = new StringList(4);
            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
            slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
            slRelSelect.addElement(DomainConstants.SELECT_LEVEL);
            slRelSelect.add("attribute[Task WBS]");

            ArrayList<String> alIds = new ArrayList<String>();			
            ProjectSpace project    = new ProjectSpace(strProjectId);
            DomainObject taskDO     = DomainObject.newInstance(context);

            DocumentBuilderFactory docFactory   = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder          = docFactory.newDocumentBuilder();
            Document doc                        = docBuilder.newDocument();
           
            // fetch all tasks of the project
            MapList projectTaskList = task.getTasks(context, project, 0 , slObjectSelect, slRelSelect);
            if(null != projectTaskList && !projectTaskList.isEmpty())
            {
                int nListSize = projectTaskList.size();

                Element elGrid = doc.createElement("Grid"); //creating root node
                doc.appendChild(elGrid);

                Element elBody = doc.createElement("Body"); //creating root node
                elGrid.appendChild(elBody);

                Element elB = doc.createElement("B"); //creating root node
                elBody.appendChild(elB);

                Element elParent    =   elB;				
                for(int nTaskCount = 0; nTaskCount < nListSize; nTaskCount++)
                {
                    alIds.add((String)((Map)projectTaskList.get(nTaskCount)).get(DomainConstants.SELECT_ID));
                }

                for(int nTaskCount = 0; nTaskCount < nListSize; nTaskCount++)
                {
                    projectTaskMap      = (Map)projectTaskList.get(nTaskCount);					
                    strTaskId           = (String)projectTaskMap.get(DomainConstants.SELECT_ID);
                    isTaskLeafLevel     = ((String)projectTaskMap.get("from[Subtask]")).equals("False") ? "0" : "1"; //False - leaf level task
                    strGANTTGanttIcons  = ((String)projectTaskMap.get("from[Subtask]")).equals("False") ? "" : "1"; //False - leaf level task
                    strParentCode       = (String)projectTaskMap.get("attribute[Task WBS]");
                    strLevel            = (String)projectTaskMap.get("level");
                    strTaskName         = (String)projectTaskMap.get("name");
                    sAttrbPrcComplete   = (String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");
					//strParentSubtaskId  = (String)projectTaskMap.get("to[Subtask].from.id");					
                    strTaskType         = (String)projectTaskMap.get(DomainConstants.SELECT_TYPE);


                    strReasonfordelay           = (String)projectTaskMap.get("attribute[MSILReasonfordelay].value");
                    strActionPlanforcatchup     = (String)projectTaskMap.get("attribute[MSILActionPlanforcatchup].value");
                    strComments                 = (String)projectTaskMap.get("attribute[Comments].value");

                    // 05/06/2016 - Added for SCR - Add Critical Task filter - start
                    strMSILCriticalTask           = (String)projectTaskMap.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
                    strOOTBCriticalTask           = (String)projectTaskMap.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_OOTB_CRITICAL_TASK);

                    strCriticalTask     = (strOOTBCriticalTask.equalsIgnoreCase("true")||strMSILCriticalTask.equalsIgnoreCase("true")) ?  "Yes" : "No";
                    isCriticalTaskFlag = strCriticalTask.equalsIgnoreCase("Yes") ?  "1" : "0"; // 0 if task is not critical
                    // 05/06/2016 - Added for SCR - Add Critical Task filter - end
                 
                    slPersonList        = projectTaskMap.get("to[Assigned Tasks].from.name")  instanceof String ? new StringList((String)projectTaskMap.get("to[Assigned Tasks].from.name")) :(StringList) projectTaskMap.get("to[Assigned Tasks].from.name");
                    

                    sAttribEstStartDate = (String)projectTaskMap.get( "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");
                    sJavaDate           = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(sAttribEstStartDate);
                    sAttribEstStartDate = sdf.format(sJavaDate);
                    strDuration         = (String)projectTaskMap.get("attribute[" +DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]");

                    sAttribEstEndDate   = (String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");
                    // 05/06/2016 - Added/Modified for SCR - Add Slip days filter - start
                    if(null != sAttribEstEndDate && sAttribEstEndDate.length() > 0)
                    {
                    attrEstEndDate      = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(sAttribEstEndDate);
                    sAttribEstEndDate   = sdf.format(attrEstEndDate);
                    }
                    strTaskState        = (String)projectTaskMap.get(DomainConstants.SELECT_CURRENT);
                    // 05/06/2016 - Added/Modified for SCR - Add Slip days filter - end
                    strBGFormatcolor    =   "100".equals(sAttrbPrcComplete)?"Green":  "Red";



                    sAttribEstStartDate = "Milestone".equals(strTaskType)?"":sAttribEstStartDate;
                    // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
                    boolean isTaskTobeShown=true;
                    // if current Object is a Project
                    if("Project Space".equals(strTaskType))
                    {
                        strProjectWBSCode   = (String)projectTaskMap.get("attribute[Task WBS]"); 
                        strIcon             = "../common/images/iconSmallProject.gif";
                        strGANTTGanttClass  = "Group";                      
                        strProjectState         = (String)projectTaskMap.get(DomainConstants.SELECT_CURRENT);
                        if ( "Cancel".equalsIgnoreCase(strProjectState) || "Hold".equalsIgnoreCase(strProjectState))
                        {                   
                            isTaskTobeShown=false;                          
                        }               
                    
                    }
                    // else current Object is a Task
                    else
                    {
                        strIcon             = "../common/images/iconSmallTask.gif";
                        strGANTTGanttClass  = "";

                        if(projectTaskMap.containsKey("to[Project Access Key].from.from[Project Access List].to.current")){
                        strProjectStateAssociatedToTask = (String)projectTaskMap.get("to[Project Access Key].from.from[Project Access List].to.current");                       
                        if ( "Cancel".equalsIgnoreCase(strProjectStateAssociatedToTask) || "Hold".equalsIgnoreCase(strProjectStateAssociatedToTask))
                        {                           
                            isTaskTobeShown=false;                          
                        }   
                      }
                    }
                    // if Project itself or Project Associated To Task is in Hold or Cancel State then dont consider
                    if(!isTaskTobeShown)
                    {
                        continue;
                    }
                    
                    // Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End

                    // dependent task ids & type
                    dependentTaskIdsList    = new StringList();
                    dependentTypeList       = new StringList();
                    if(projectTaskMap.containsKey(task.SELECT_PREDECESSOR_IDS))
                    {
                        dependentTaskIdsList    =   projectTaskMap.get(task.SELECT_PREDECESSOR_IDS)  instanceof String ? new StringList((String)projectTaskMap.get(task.SELECT_PREDECESSOR_IDS)) :(StringList) projectTaskMap.get(task.SELECT_PREDECESSOR_IDS);
                        dependentTypeList       =   projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES)  instanceof String ? new StringList((String)projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES)) :(StringList) projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES);
                    }

                    strDependentTasksId         = "";
                    strDependentTasksDetail     = "";
                    strDependentNumbers         = "";
                    strDependentChild           = "";
                    if("True".equals(strShowDependency) && null != dependentTaskIdsList && dependentTaskIdsList.size() > 0 )
                    {
                        nDependentTaskListSize  = dependentTaskIdsList.size();
                        for(int nCount = 0; nCount < nDependentTaskListSize; nCount++)
                        {
                            strDependentTaskId  = (String)dependentTaskIdsList.get(nCount);
                            //taskDO.setId(strDependentTaskId);
                            //strDependentChild   =   taskDO.getInfo(context , "to[Subtask].attribute[Task WBS].value");
                            int indexOfTask     = alIds.indexOf(strDependentTaskId);
                            try
                            {
                                 mapDepdency         = (Map)projectTaskList.get(indexOfTask);
                            }
                            catch (Exception Ex)
                            {
                                mapDepdency=null;
                            }
                            if(mapDepdency!=null)
                            {
                                strDependentChild   = (String) mapDepdency.get("attribute[Task WBS]");
                                if(!"".equals(strProjectWBSCode))
                                {
                                   strDependentChild   = strProjectWBSCode+"."+strDependentChild;
                                }
                                if(nCount == 0)
                                {
                                    strDependentNumbers = strDependentChild+(String)dependentTypeList.get(nCount);
                                }
                                else
                                {
                                    strDependentNumbers = strDependentNumbers + ";" + strDependentChild+(String)dependentTypeList.get(nCount);
                                }
                            }
                        }
                            strDependentNumbers = strDependentNumbers.replaceAll("\\.", "_");
                           // strDependentNumbers = strDependentNumbers.replaceAll("_", "\\$");

                    }
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
                    if("True".equals(strShowResource) && slPersonList!=null )
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
                            elNode.setAttribute("T",strTaskName);
                            //elNode.setAttribute("T","|../common/emxTree.jsp?objectId="+strTaskId+"|"+strTaskName+"|new_window");
                            //elNode.setAttribute("TType","Link");
                            //elNode.setAttribute("TCanEdit" ,"0");
                            //elNode.setAttribute("TOnClickLink","Grids.Alert=1;var ret=!confirm('Do you want to navigate here?');Grids.Alert=0;return ret");

                          
                            elNode.setAttribute("Expanded","0");
                            elNode.setAttribute("TIcon",strIcon);
                            elNode.setAttribute("TLink","../common/emxTree.jsp?objectId="+strTaskId);
                            elNode.setAttribute("TLinkTarget","_blank");

                            elNode.setAttribute("GGanttIcons",strGANTTGanttIcons);
                            elNode.setAttribute("GGanttClass",strGANTTGanttClass);
                            elNode.setAttribute("id",strLastCode);
                            elNode.setAttribute("C",sAttrbPrcComplete);
                            elNode.setAttribute("CTip",sAttrbPrcComplete);
                            elNode.setAttribute("S",sAttribEstStartDate);
                            elNode.setAttribute("E",sAttribEstEndDate);
                            elNode.setAttribute("D",strDependentNumbers);
                            elNode.setAttribute("RESOURCES",strAssignName);
                            elNode.setAttribute("DUR",strDuration);
                            elNode.setAttribute("TaskId",strTaskId);
                            elNode.setAttribute("SLACK",strTitle);
                            elNode.setAttribute("Summery",isTaskLeafLevel);
                            elNode.setAttribute("Reason",strReasonfordelay);
                            elNode.setAttribute("ActionPlan",strActionPlanforcatchup);
                            elNode.setAttribute("Comments",strComments);
                            // 05/06/2016 - Added for SCR - Add Slip days filter - start
                            elNode.setAttribute("SlipDays", "" +getTaskStatusSlip(context, attrEstEndDate, strTaskState));
                            // 05/06/2016 - Added for SCR - Add Slip days filter - end
                            // 05/06/2016 - Added for SCR - Add Critical Task filter - start
                            elNode.setAttribute("CriticalTask", strCriticalTask);
                            elNode.setAttribute("CriticalTaskFlag", isCriticalTaskFlag);
                            // 05/06/2016 - Added for SCR - Add Critical Task filter - end
                            elB.appendChild(elNode);
                            mapParentMap.put(strTaskId , elNode);
                        }
                        else
                        {   
							// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start

							StringList slParentIdList   = projectTaskMap.get("to[Subtask].from.id")  instanceof String ? new StringList((String)projectTaskMap.get("to[Subtask].from.id")) :(StringList) projectTaskMap.get("to[Subtask].from.id");
							if(slParentIdList !=null && slParentIdList.size()>0){
							for(int p=0;p<slParentIdList.size();p++)
							{
								//String strParentId1  = (String)projectTaskMap.get("to[Subtask].from.id");	
								String strParentId1  = (String)slParentIdList.get(p);								
								Element elParentNode = (Element)  mapParentMap.get(strParentId1);
								if(elParentNode !=null){								
								Element elNode = doc.createElement("I");
								 elNode.setAttribute("T",strTaskName);
								//elNode.setAttribute("T","|../common/emxTree.jsp?objectId="+strTaskId+"|"+strTaskName+"|new_window");
								//elNode.setAttribute("TType","Link");
								//elNode.setAttribute("TOnClickLink","");
								elNode.setAttribute("Expanded","0");
								elNode.setAttribute("TIcon",strIcon);
								elNode.setAttribute("TLink","../common/emxTree.jsp?objectId="+strTaskId);
								elNode.setAttribute("TLinkTarget","_blank");

								elNode.setAttribute("GGanttIcons",strGANTTGanttIcons);
								elNode.setAttribute("GGanttClass",strGANTTGanttClass);
								elNode.setAttribute("id",strLastCode);
								elNode.setAttribute("C",sAttrbPrcComplete);
								elNode.setAttribute("CTip",sAttrbPrcComplete);
								elNode.setAttribute("S",sAttribEstStartDate);
								elNode.setAttribute("E",sAttribEstEndDate);
								elNode.setAttribute("D",strDependentNumbers);
								elNode.setAttribute("RESOURCES",strAssignName);
								elNode.setAttribute("DUR",strDuration);
								elNode.setAttribute("TaskId",strTaskId);
								elNode.setAttribute("SLACK",strTitle);
								elNode.setAttribute("Summery",isTaskLeafLevel);
								elNode.setAttribute("Reason",strReasonfordelay);
								elNode.setAttribute("ActionPlan",strActionPlanforcatchup);
								elNode.setAttribute("Comments",strComments);
                                // 05/06/2016 - Added for SCR - Add Slip days filter - start
	                            elNode.setAttribute("SlipDays", "" +getTaskStatusSlip(context, attrEstEndDate, strTaskState));
                                // 05/06/2016 - Added for SCR - Add Slip days filter - end
								// 05/06/2016 - Added for SCR - Add Critical Task filter - start
	                            elNode.setAttribute("CriticalTask", strCriticalTask);
	                            elNode.setAttribute("CriticalTaskFlag", isCriticalTaskFlag);
	                            // 05/06/2016 - Added for SCR - Add Critical Task filter - end
								elParentNode.appendChild(elNode);
								mapParentMap.put(strTaskId , elNode );

								// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End
								}
							   }
							}
                        }
                   
                }
                // 05/06/2016 - Added for SCR - Add Slip days filter - start
                updateParentSlipDaysAllProject(doc);
                // 05/06/2016 - Added for SCR - Add Slip days filter - end
            }
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File templateFile       =   new File(strWorkSpace);
            File templateXMLFile    =   new File(templateFile, strFileName);
            StreamResult result     = new StreamResult(templateXMLFile);			
            transformer.transform(source, result);			
            HashSet hs = new HashSet();
            hs.addAll(alAssignList);
            alAssignList.clear();
            alAssignList.addAll(hs);
            returnMap.put("assignUser",alAssignList);
        }
        catch (Exception ex )
        {
            ex.printStackTrace();
        }
        finally
        {
             DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].from.name");
        }
        return returnMap;
    }
    // 05/06/2016 - Added for SCR - Add Slip days filter - start
    public void updateParentSlipDaysAllProject(Document doc) throws Exception 
    {
        try{
            NodeList nList = doc.getElementsByTagName("I");
			int iListCount = nList.getLength();
            for (int count = 0; count < iListCount; count++) {
                Element eElement     = (Element) nList.item(count);
                String isLeafTask    = (String) eElement.getAttribute("Summery");
                if("0".equalsIgnoreCase(isLeafTask))
                   updateTaskSlipDays(eElement);
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    
     public void updateTaskSlipDays(Element eElement) throws Exception 
     {
        if(eElement == null)
            return;
        try
        {
            Element parentNode     = (Element) eElement.getParentNode();
            if(!"B".equals(parentNode.getNodeName()))
            {
                String strParentSlipDays = parentNode.getAttribute("SlipDays");
                String strTaskSlipDays   = eElement.getAttribute("SlipDays");
                if(null != strParentSlipDays)
                {
                    int parentSlipDays  = Integer.parseInt(strParentSlipDays);
                    int taskSlipdays    = Integer.parseInt(strTaskSlipDays);
                    if(taskSlipdays > parentSlipDays)
                    {
                        parentNode.setAttribute("SlipDays", strTaskSlipDays);
                        updateTaskSlipDays(parentNode);
                    }
                }else {
                    parentNode.setAttribute("SlipDays", strTaskSlipDays);
                    updateTaskSlipDays(parentNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTaskStatusSlip(Context context , Date estFinishDate, String strState) throws Exception
    {
        int returnValue = 0;
        Date sysDate    = new Date();
        sysDate.setHours(0); sysDate.setMinutes(0); sysDate.setSeconds(0);
        estFinishDate.setHours(0); estFinishDate.setMinutes(0); estFinishDate.setSeconds(0);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
        if(!"Complete".equals(strState) && sysDate.after(estFinishDate))
        {
            long lSlipDay = task.computeDuration(estFinishDate, sysDate);
            returnValue   = (int) java.lang.Math.abs(lSlipDay);
        }
        return returnValue;
    } 
    // Added for SCR - Add Slip days filter - end
	 
	/****************************************************************************************************
     *       PE Phase 2 - Start - 12-Feb-2016
     ****************************************************************************************************/	 
	 /**
	 * Called from table "PMCWBSViewTable" column "gateStatus" Update program/update function
	 * This method sets the child objects attribute MSIL Task Type to value "Gate Schedule"
	 * When the parents attribute value is set to "Gate Schedule" 
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds ParamMap
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 **/
 /**
	 * Called from table "PMCWBSViewTable" column "gateStatus" Update program/update function
	 * This method sets the child objects attribute MSIL Task Type to value "Gate Schedule"
	 * When the parents attribute value is set to "Gate Schedule" 
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds ParamMap
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 **/
public void getGateSchedule(Context context,String[] args)
{
	   StringList slReturnList = new StringList();
	   Pattern typePattern = new Pattern(
				DomainConstants.TYPE_PROJECT_MANAGEMENT);
		Pattern relPattern = new Pattern(
				DomainConstants.RELATIONSHIP_SUBTASK);
		relPattern
				.addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);
		relPattern
				.addPattern(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);
		Pattern includeType = new Pattern(
				DomainConstants.TYPE_TASK);
		Pattern includeRelationship = new Pattern(
				DomainConstants.RELATIONSHIP_SUBTASK);
		Map includeMap = new HashMap();
		StringList slBusSelect = new StringList(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		boolean getFrom = true;
		boolean getTo = false;
		short recurseToLevel = 0;
		String strRelWhere = "";
		String strBusWhere = "";
	   try
	   {
		   Map programMap = (Map) JPO.unpackArgs(args);
		   Map paramMap = (Map) programMap.get("paramMap");
		   String strobjectId = (String) paramMap.get("objectId");
		   String strNewValue = (String) paramMap.get("New Value");
		   StringList slIdList = FrameworkUtil.split(strobjectId, ",");
		   if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strobjectId)) {
				DomainObject doObject = new DomainObject(strobjectId);				
				// if(UIUtil.isNotNullAndNotEmpty(strNewValue))
				doObject.setAttributeValue(context, ATTRIBUTE_MSIL_TASK_TYPE,
						strNewValue);
				if(strNewValue.equals(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE))
				{				
					MapList mlGateConnectedObj = doObject.getRelatedObjects(
							context, relPattern.getPattern(),
							typePattern.getPattern(), slBusSelect, null,
							false, true, (short) 0, null, null);
					
					int iGateConnectedObjSize = mlGateConnectedObj.size();
					if(iGateConnectedObjSize>0)
					{
						for(int iGateConnectedObjCount=0 ; iGateConnectedObjCount<iGateConnectedObjSize;iGateConnectedObjCount++)
						{
							Map mpConnectedObjMap = (Map) mlGateConnectedObj.get(iGateConnectedObjCount);
							strobjectId = (String) mpConnectedObjMap.get(DomainConstants.SELECT_ID);
							doObject.setId(strobjectId);
							doObject.setAttributeValue(context, ATTRIBUTE_MSIL_TASK_TYPE,
							strNewValue);
						}
					}
				}
			}			
	   }
	   catch(Exception ex)
	   {
		   ex.printStackTrace();
	   } 
}
/**
	 * Allowing Gate Status to be edited only by Master Project owner.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds mpRequestMap
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 **/
	public StringList editGateStatus(Context context,String[] args)
	{
	   StringList slReturnList = new StringList();
	   try
	   {
		   Map programMap = (Map) JPO.unpackArgs(args);
		   Map mpRequestMap = (Map)programMap.get("requestMap");
		   String strSelectedObjId = (String) mpRequestMap.get("objectId");
		   MapList objectList = (MapList) programMap.get("objectList");
			if (null == objectList) {
				throw new IllegalArgumentException("Object id List is null");
			}
			//Getting the Master Project ID and Owner as it will be edited by only the owner of master Prj owner
			MQLCommand mql = new MQLCommand();
			String strMql = "expand bus " + strSelectedObjId + " to rel 'Subtask' recurse to end select bus id owner dump |";
			
			boolean bResult = mql.executeCommand(context, strMql);
			String strMasterProjectOwner = "";
			String strMasterProjectId = "";
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
					strMasterProjectOwner = (String)sResultTkz.nextToken(); // Object Owner
				}
			}			
			int iObjListSize = objectList.size();
			DomainObject doObject = new DomainObject();
			String strObjectId = "";
			Map mpObjectMap = null;
			String strObjectType = "";
			String strParentObjType = "";			
			for (int iObjectListCount = 0; iObjectListCount < iObjListSize; iObjectListCount++) {
				mpObjectMap = (Map)objectList.get(iObjectListCount);
				strObjectId = (String) mpObjectMap.get(DomainConstants.SELECT_ID);
				if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
				{
					doObject.setId(strObjectId);
					strObjectType = doObject.getInfo(context, DomainConstants.SELECT_TYPE);
					strParentObjType = doObject.getInfo(context,"to["+ProgramCentralConstants.RELATIONSHIP_SUBTASK+"].from.type" );
			
					if(strObjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_TASK) && strMasterProjectOwner.equalsIgnoreCase(context.getUser()) && strParentObjType.equalsIgnoreCase(ProgramCentralConstants.TYPE_GATE))
					{
						slReturnList.add("true");
					}
					else
					{
						slReturnList.add("false");
					}
				}		
			}
	   }
	   catch(Exception ex)
	   {
		   ex.printStackTrace();
	   }
	return slReturnList;  
}
	/**
	 * Displaying Gate Status column only in Gate Schedule WBS 
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds ParamMap
	 * @return boolean
	 * @throws Exception
	 *             if the operation fails
	 **/
	public boolean getGateStatusColumn(Context context, String[] args)
			throws Exception {
		boolean bFlag = false;
		try {
			HashMap mpProgramMap = JPO.unpackArgs(args);
			String strParentOID = (String) mpProgramMap.get("parentOID");
			String strObjectId = (String) mpProgramMap.get("objectId");
			if(MSILUtils_mxJPO.isNullOrEmpty(strParentOID))
			{
				strParentOID = strObjectId;
			}			
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strParentOID))
			{
				DomainObject doObject = new DomainObject(strParentOID);
				String strObjName = doObject.getInfo(context, DomainConstants.SELECT_NAME);
				String strTaskType = doObject.getInfo(context,SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
				if (strTaskType.equalsIgnoreCase(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE)) {
					bFlag = true;
				} else {
					bFlag = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFlag;
	}
	/**
	 * Trigger which promotes the task of task type "gate schedule" when a document is connected 
	 * And also sends a mail to the assignees and the owner
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            OBJECTID,FROMOBJECTID
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 **/
	public void promoteConnectedTask(Context context, String[] args)
			throws Exception {
		 try
	        {
			   com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
	           // get values from args, get the connected docs parent id 
	           String strDocObjId = args[0];	          
	           String strParentObjId = args[1];

	           DomainObject doObj = new DomainObject(strParentObjId);
	          //Get the details about parent Obj
	           StringList busSelects = new StringList(3);
	           busSelects.add(DomainConstants.SELECT_NAME);
	           busSelects.add(DomainConstants.SELECT_TYPE);
	           busSelects.add(DomainConstants.SELECT_OWNER);
	           busSelects.add(DomainConstants.SELECT_CURRENT);
	           busSelects.add(SELECT_ATTRIBUTE_MSIL_TASK_TYPE);	         
	           Map objMap = doObj.getInfo(context, busSelects);
	           
	           String strParentObjType = (String) objMap.get(DomainConstants.SELECT_TYPE);
	           if(strParentObjType.equalsIgnoreCase(DomainConstants.TYPE_TASK))
	           {
	        	   String strParentTaskType = (String) objMap.get(SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
	        	   //Promoting the task if it is of task type Gate Schedule
	        	   
	        	    if(strParentTaskType.equalsIgnoreCase(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE))
	        	   {
	        		   if(STATE_ASSIGN_POLICY_PROJECT_TASK.equalsIgnoreCase((String)(objMap.get(DomainConstants.SELECT_CURRENT))))
	        		   {
	        			   doObj.promote(context);	 
	        		   }
	        		   else if(STATE_CREATE_POLICY_PROJECT_TASK.equalsIgnoreCase((String)(objMap.get(DomainConstants.SELECT_CURRENT))))
	        		   {
	        			   doObj.promote(context);	 
	        			   doObj.promote(context);	 
	        		   }
	        		   
	        		 //Sending Mail to the assignee of the task and the corresponding task owner
	        		   String strMasterProjectId = "";
	        		   String strMasterProjectOwner = "";
	        		   task.setId(strParentObjId);        		  
	        		   String strAssignee =  task.getInfo(context, Task.SELECT_ASSIGNEENAMES);
	        		   
	        		   StringList toList = new StringList(1);
	                   toList.addElement((String)objMap.get(DomainConstants.SELECT_OWNER));
	                   toList.addElement(strAssignee);

	                // setup the id list
	                   StringList objectIdList = new StringList();
	                   objectIdList.add((String)objMap.get(DomainConstants.SELECT_ID));
	                   
	                // setup message key and value pairs
	                   String[] messageKeys = {"TaskName- "};
	                   String[] messageValues = {(String)objMap.get(DomainConstants.SELECT_NAME)};
	        		   
	                   String strSubject = "Attachment of Deliverables to the Task";
	                   String strMessage = "Deliverables are attached to the below mentioned task";
	                   
	                   MailUtil.sendNotification(context,
	                           toList,        // To List
	                           null,          // Cc List
	                           null,          // Bcc List
	                           "MSIL.ProgramCentral.NotificationMail.Subject",  // Subject key
	                           null,                                            // Subject keys
	                           null,                                            // Subject values
	                           "MSIL.ProgramCentral.NotificationMail.Message",  // Message key
	                           messageKeys,   // Message keys
	                           messageValues, // Message values
	                           objectIdList,  // Object list
	                           null,          // company name
	                           "emxProgramCentralStringResource"); 
	        	   }
	           }	                  
	        }
		 catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }		
	}		
	/**
     /**
	 * Trigger which connects task of task type "Gate schedule" with 
	 * document status object or if already connected then updates 
	 * the value.
	 * Wriiten on MSIL Gate Status attribute,called on modify.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            OBJECTID,TYPE
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 **/
	public void connectDocumentStatus(Context context,String[] args)
	{
		try
		   {
				com.matrixone.apps.common.Task task = (com.matrixone.apps.common.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
				
				String strTaskId = args[0];	          
		        String strObjectType = args[1];
		        StringList slObjSelects = new StringList("to["+ProgramCentralConstants.RELATIONSHIP_SUBTASK+"].from.type");
		        slObjSelects.add("to["+ProgramCentralConstants.RELATIONSHIP_SUBTASK+"].from.id");
		        slObjSelects.add("to["+ProgramCentralConstants.RELATIONSHIP_SUBTASK+"].from."+SELECT_ATTRIBUTE_MSIL_TASK_TYPE+"");
		        slObjSelects.add(DomainConstants.SELECT_NAME);
		        slObjSelects.add(SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
		        slObjSelects.add(SELECT_ATTRIBUTE_MSIL_GATE_STATUS);
		        
		        Date today = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
				
		        if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectType) && MSILUtils_mxJPO.isNotNullAndNotEmpty(strTaskId))
		        {
		        	if(strObjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_TASK))
		        	{		        		
		        		DomainObject doTaskObject = new DomainObject(strTaskId);
		        		boolean bVal = doTaskObject.hasRelatedObjects(context, RELATIONSHIP_MSIL_DOCUMENT_STATUS_TASK, false);
		        		Map mpTaskInfoMap = doTaskObject.getInfo(context, slObjSelects);
		        		String strObjectName = (String) mpTaskInfoMap.get(DomainConstants.SELECT_NAME);
		        		String strObjectTaskType = (String) mpTaskInfoMap.get(SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
		        		String strParentType = (String) mpTaskInfoMap.get("to["+ProgramCentralConstants.RELATIONSHIP_SUBTASK+"].from.type");
		        		String strParentTaskType = (String) mpTaskInfoMap.get("to[Subtask].from."+SELECT_ATTRIBUTE_MSIL_TASK_TYPE+"");
		        		String strGateStatus = (String) mpTaskInfoMap.get(SELECT_ATTRIBUTE_MSIL_GATE_STATUS);
			        	String strLastReceived = "";
			        	String strCount = "";
		        		if(bVal)
			        	{
		        			System.out.println("MSILTask:Setting the Document Status Value--Start--");
			        		//Document Status is already connected..
			        		String strTaskDocumentId = doTaskObject.getInfo(context, "to["+RELATIONSHIP_MSIL_DOCUMENT_STATUS_TASK+"].from.id");
			        		DomainObject doDocObject = new DomainObject(strTaskDocumentId);
						   if(strGateStatus.equalsIgnoreCase("Pending"))
						   {
							   //doObject.setAttributeValue(context, arg1, arg2)
						   }
						   else if(strGateStatus.equalsIgnoreCase("Correction"))
						   {
							   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_SENT_FOR_CORRECTION,sdf.format(today));
							   
						   }
						   else if(strGateStatus.equalsIgnoreCase("Under Review"))
						   {					
							   strLastReceived = doDocObject.getAttributeValue(context, ATTRIBUTE_MSIL_LAST_RECEIVED);
							   if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strLastReceived))
							   {
								   //The Last Received is already set so need to increase count..
								   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_LAST_RECEIVED,sdf.format(today));	
								   //Increasing the correction count..
								   strCount = doDocObject.getAttributeValue(context, "MSIL Correction Count");
								   int iCount = Integer.parseInt(strCount);
								   iCount++;					
								   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_CORRECTION_COUNT,String.valueOf(iCount));
								   
								   //Setting sent for correction to null..
								   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_SENT_FOR_CORRECTION,"");
							   }
							   else
							   {
								   //Last Received is being set for the first time,so no need to increase the correction count.
								   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_LAST_RECEIVED,sdf.format(today));	
							   }
							  					
						   }
						   else if(strGateStatus.equalsIgnoreCase("Sign Off"))
						   {
							   doDocObject.setAttributeValue(context, ATTRIBUTE_MSIL_SIGN_OFF, sdf.format(today));
						   }
			        		
			        	}
			        	else
			        	{
			        		//Connecting Document Status Object To The Task..
			        		if(strParentType.equalsIgnoreCase(ProgramCentralConstants.TYPE_GATE) && strParentTaskType.equalsIgnoreCase(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE) && strObjectTaskType.equalsIgnoreCase(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE))
			        		{
			        			DomainObject doObject = DomainObject.newInstance(context);
			        			String strDocStatusName = com.matrixone.apps.domain.util.FrameworkUtil.autoName(context,"type_MSILDocumentStatus",null,"policy_MSILDocumentStatus",null,null,true,true);
			        			
			        			doObject.createObject(context, TYPE_MSIL_DOCUMENT_STATUS,
			        					strDocStatusName, "-",
			        					POLICY_MSIL_DOCUMENT_STATUS, context.getVault().toString());
			        			String strid = doObject.getId();
								DomainRelationship.connect(context, doObject, RELATIONSHIP_MSIL_DOCUMENT_STATUS_TASK, doTaskObject);
								if(strGateStatus.equalsIgnoreCase("Under Review"))
								{
									doObject.setAttributeValue(context, ATTRIBUTE_MSIL_LAST_RECEIVED, sdf.format(today));
								}
			        			System.out.println("MSILTask:Document Status Object Connected-");
			        		}
			        	}
		        	}
		        }
		   }
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Trigger method called on
	 * 	1) Task Type attribute modify (Modify Action trigger)
	 *
	 * This method will delete any existing route on task when task type is either changed to PO or Gate Schedule. 
	 * This will also create a route in case task type is changed from PO/Gate schedule to Sourcing or blank value
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - Object Id
	 *            1 - From State
	 *            2 - To state
	 *
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 *
	 */
	public int deleteCreateRouteOnTask(Context context, String[] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILTask:deleteCreateRouteOnTask start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			com.matrixone.apps.common.Task task = (com.matrixone.apps.common.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			Route routeObj = (Route) DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
			MSILProject_mxJPO jpoObj = new MSILProject_mxJPO(context, args);

			StringList slSelectList = new StringList(2);
			slSelectList.addElement(DomainConstants.SELECT_ID);
			slSelectList.addElement("from[Object Route|to.current=='Define'].to.id");

			String strObjectId = args[0];
			String strObjectState = args[1];
			String strNewAttrValue = args[2];
			String strOldAttrValue = args[3];
			if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
			{
				if(DomainConstants.STATE_PROJECT_SPACE_ASSIGN.equalsIgnoreCase(strObjectState))
				{
					// IF TASK TYPE IS SET TO PO/GATE SCHEDULE, CHECK THE STATE OF TASK
					// IF TASK STATE IS CREATE - DO NOTHING
					// IF TASK STATE > ASSIGN - DO NOTHING, SINCE AT THAT STATE OF TASK USER WILL NOT BE ABLE TO MODIFY THE TASK TYPE ATTRIBUTE
					// IF TASK STATE IS ASSIGN - CHECK FOR ANY CREATED ROUTE. IF PRESENT, DELETE THE ROUTE
					
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
					//if(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strNewAttrValue) || "PO".equalsIgnoreCase(strNewAttrValue))
					if(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strNewAttrValue) || "PO".equalsIgnoreCase(strNewAttrValue) || "Ringi".equalsIgnoreCase(strNewAttrValue))
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
					{
						task.setId(strObjectId);

						// fetch routes on this task												ContextUtil.pushContext(context); // 05-Apr-2017 - Ringi Integration - Issue fix (Route not deleted if Task type is set to Ringi) - Added
						Map mpTaskRouteMap = task.getInfo(context, slSelectList);						
						if(null != mpTaskRouteMap)
						{
							String strRouteId = (String)mpTaskRouteMap.get("from[Object Route].to.id");
							if(null != strRouteId && !"null".equalsIgnoreCase(strRouteId) && strRouteId.length() > 0)
							{
								routeObj.setId(strRouteId);

								//ContextUtil.pushContext(context); // 05-Apr-2017 - Ringi Integration - Issue fix (Route not deleted if Task type is set to Ringi) - Commented								
								routeObj.deleteRoute(context);								//ContextUtil.popContext(context);  // 05-Apr-2017 - Ringi Integration - Issue fix (Route not deleted if Task type is set to Ringi) - Commented
								
							}
						}						ContextUtil.popContext(context); // 05-Apr-2017 - Ringi Integration - Issue fix (Route not deleted if Task type is set to Ringi) - Added
					}
					// IF TASK TYPE IS SET FROM PO/GATE SCHEDULE TO BLANK/SOURCING, CHECK THE STATE OF TASK
					// IF TASK STATE IS CREATE - DO NOTHING
					// IF TASK STATE > ASSIGN - DO NOTHING, SINCE AT THAT STATE OF TASK USER WILL NOT BE ABLE TO MODIFY THE TASK TYPE ATTRIBUTE
					// IF TASK STATE IS ASSIGN - CHECK FOR ANY CREATED ROUTE. IF NOT PRESENT, CREATE THE ROUTE
					
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
					// else if(!ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strNewAttrValue) && !"PO".equalsIgnoreCase(strNewAttrValue))
					else if(!ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strNewAttrValue) && !"PO".equalsIgnoreCase(strNewAttrValue) && !"Ringi".equalsIgnoreCase(strNewAttrValue))
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
					{
						DomainObject objectDO = DomainObject.newInstance(context, strObjectId);

						// GET NOT COMPLETED ROUTES FROM OBJECT
						String strWhereClause = "current!='Complete'";
						ContextUtil.pushContext(context);
						MapList mlRoutesList = objectDO.getRelatedObjects(context,
								DomainConstants.RELATIONSHIP_OBJECT_ROUTE, 
								DomainConstants.TYPE_ROUTE, 
								slSelectList, 
								null,
								true, 
								true, 
								(short) 1, 
								strWhereClause, 
								null);
						ContextUtil.popContext(context);

						// CREATE ROUTE ON TASK, IF NO IN-PROCESS ROUTE IS PRESENT
						if (null == mlRoutesList || mlRoutesList.isEmpty() || mlRoutesList.size() <= 0) 
						{
							String routeArgs[] = {strObjectId, strObjectState, strObjectState, "state_Review", "Approval", args[3], args[4], args[5], args[6]};
							
							return (jpoObj.createRoute(context, routeArgs));
						}
					}                	
				}
			}
		} catch (Exception e) {
			System.out.println("Exception JPO:MSILTask:deleteCreateRouteOnTask:::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILTask:deleteCreateRouteOnTask exit...");
		return 0;
	}
/**
  	 * Trigger method called on
  	 * 	1) Task promotion from Create to Assign state (Promote Check trigger)
  	 *
  	 * This method checks for Milestone object to be connected to the task.
  	 * If no Milestone is found, system will not allow task to be promoted to Assign state
  	 *
  	 * @param context
  	 *            the eMatrix <code>Context</code> object
  	 * @param args:
  	 *            0 - OBJECTID - Object Id
  	 *
  	 * @returns 0 for success and 1 for trigger failure
  	 * @throws Exception
  	 *             if the operation fails
  	 */
   public int msilCheckMilestoneConnected(Context context,String[] args)throws Exception
   {
	   System.out.println("\n...msilCheckMilestoneConnected start...");	   
		if (args == null || args.length < 1)
		{
				throw (new IllegalArgumentException());
			}
		
		try
		{
			String strTaskId = args[0];
			String languageStr = context.getSession().getLanguage();
			if(null != strTaskId && !"null".equalsIgnoreCase(strTaskId) && strTaskId.length() > 0)
			{
				DomainObject taskDO = DomainObject.newInstance(context, strTaskId);
				String strTaskName = taskDO.getInfo(context, DomainConstants.SELECT_NAME);
				String strCurrent = taskDO.getInfo(context, DomainConstants.SELECT_CURRENT);
				if(!strCurrent.equalsIgnoreCase(STATE_CREATE_POLICY_PROJECT_TASK))
					return 0;
				boolean bVal = taskDO.hasRelatedObjects(context, MSILConstants_mxJPO.REL_MSIL_MILESTONE_TASK, false);
				if(bVal)
				{
				    System.out.println("\n...msilCheckMilestoneConnected end...");
					return 0;
				}
				else
				{
					String strMessage = i18nNow.getI18nString("MSILProgramCentral.Project.Promote.NoMilestonePresent", "emxProgramCentralStringResource", languageStr);
					System.out.println("\nTrigger Blocked for Reason JPO:MSILTask:CheckMilestoneConnected::"+strMessage);
					strMessage = strMessage + " - " + strTaskName;
					MqlUtil.mqlCommand(context, "notice "+strMessage);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception JPO:MSILTask:CheckMilestoneConnected :::: " + e);
			throw new FrameworkException(e);
		}
	   System.out.println("\n...msilCheckMilestoneConnected end...");
	   return 1;
   }

   /**
	 * Not Displaying custom task extension column in Gate Schedule WBS 
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds ParamMap
	 * @return boolean
	 * @throws Exception
	 *             if the operation fails
	 **/
	public boolean hideTaskExtensionColumnsForGateTable(Context context, String[] args) throws Exception 
	{
		boolean bFlag = false;
		try 
		{
			HashMap mpProgramMap = JPO.unpackArgs(args);
			String strParentOID = (String) mpProgramMap.get("parentOID");
			String strObjectId = (String) mpProgramMap.get("objectId");
			if(MSILUtils_mxJPO.isNullOrEmpty(strParentOID))
			{
				strParentOID = strObjectId;
			}			
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strParentOID))
			{
				DomainObject doObject = new DomainObject(strParentOID);
				String strObjName = doObject.getInfo(context, DomainConstants.SELECT_NAME);
				String strTaskType = doObject.getInfo(context,SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
				if (!strTaskType.equalsIgnoreCase(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE)) {
					bFlag = true;
				} else {
					bFlag = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFlag;
	}
	
	/**
	 * Trigger method called on
	 * 	1) Task Type attribute modify (Modify Action trigger)
	 *
	 * This method will connect 'None' Milestone Object to task when task type is either changed to Gate Schedule
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - Object Id
	 *
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 *
	 */
	public int connectDefaultMilestone(Context context, String[] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILTask:connectDefaultMilestone start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			com.matrixone.apps.common.Task task = (com.matrixone.apps.common.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

			StringList slSelectList = new StringList(2);
			slSelectList.addElement(DomainConstants.SELECT_ID);
			slSelectList.addElement("to[" + REL_MSIL_MILESTONE_TASK + "].from.id");

			String strObjectId = args[0];
			String strNewAttrValue = args[1];
			if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
			{
				// IF TASK TYPE IS SET TO GATE SCHEDULE, CHECK FOR ANY CONNECTED MILESTONE
				// IF NO MILESTONE CONNECTED, CONNECT DEFAULT ('NONE') MILESTONE TO TASK
				if(ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strNewAttrValue))
				{
					task.setId(strObjectId);

					// fetch Milestones on this task            			
					Map mpConnectedMilestoneMap = task.getInfo(context, slSelectList);

					if(null != mpConnectedMilestoneMap)
					{
						String strMilestoneId = (String)mpConnectedMilestoneMap.get("to[" + REL_MSIL_MILESTONE_TASK + "].from.id");

						if(null != strMilestoneId && !"null".equalsIgnoreCase(strMilestoneId) && strMilestoneId.length() > 0)
						{
							//do nothing							
						}
						else
						{
							DomainObject milestoneDO = DomainObject.newInstance(context, new BusinessObject(TYPE_MSIL_MILESTONE,DEFAULT_MILESTONE,"-",VAULT_ESERVICE_PRODUCTION));
							DomainObject taskDO = DomainObject.newInstance(context, strObjectId);
							DomainRelationship.connect(context, milestoneDO, REL_MSIL_MILESTONE_TASK, taskDO);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception JPO:MSILTask:connectDefaultMilestone:::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILTask:connectDefaultMilestone exit...");
		return 0;
	}
	
	/**
	 * Method:getTaskTypeRange gets range of Task type. Gate Schedule option in Task Type dropdown should not be visible to users not from NPE.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arg
	 * @return Map
	 * @throws Exception
	 *             if the operation fails
	 */
	public Map getTaskTypeRange(Context context, String[] args) throws Exception 
	{
		try {
			String sLanguage = context.getSession().getLanguage();

			boolean bIsNPEUser = false;
			String strPersonId = PersonUtil.getPersonObjectID(context);
            DomainObject personDO = new DomainObject(strPersonId);
            
            StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			
			// FETCH DEPARTMENT/BUSINESS UNIT OF logged in user
			MapList departmentList = personDO.getRelatedObjects(context, 
					DomainConstants.RELATIONSHIP_MEMBER, 
					DomainConstants.TYPE_DEPARTMENT + "," + DomainConstants.TYPE_BUSINESS_UNIT,
					objectSelects, 
					null,
					true, 
					false,
					(short) 1, 
					null, 
					null);

			if(null != departmentList && departmentList.size() > 0)
			{
				Map departmentMap = (Map)departmentList.get(0);
				// DEPARTMENT/BUSINESS UNIT NAME
				String strDepartmentName = (String)departmentMap.get(DomainConstants.SELECT_NAME);

				if(null != strDepartmentName && !"null".equalsIgnoreCase(strDepartmentName) && strDepartmentName.length() > 0 && 
						PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentName))
				{
					bIsNPEUser = true;
				}
			}
			AttributeType attrMSILTaskType = new AttributeType(MSILConstants_mxJPO.ATTRIBUTE_MSIL_TASK_TYPE);
			attrMSILTaskType.open(context);
			StringList strList = attrMSILTaskType.getChoices(context);
			attrMSILTaskType.close(context);

			StringList slTaskTypeRange = new StringList();
			StringList slTaskTypeRangeDisplay = new StringList();
			HashMap map = new HashMap();

			for(int i=0; i<strList.size();i++)
			{
				String strTaskTypeRange = (String)strList.get(i);

				if(bIsNPEUser)
				{
					slTaskTypeRange.add(strTaskTypeRange);
					slTaskTypeRangeDisplay.add(strTaskTypeRange);
				}
				else
				{
					if(!ATTRIBUTE_TASK_TYPE_GATE_SCHEDULE.equalsIgnoreCase(strTaskTypeRange))
					{
						slTaskTypeRange.add(strTaskTypeRange);
						slTaskTypeRangeDisplay.add(strTaskTypeRange);
					}
				}
			}

			map.put("field_choices", slTaskTypeRange);
			map.put("field_display_choices", slTaskTypeRangeDisplay);

			return  map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	/****************************************************************************************************
     *       PE Phase 2 - End - 12-Feb-2016
     ****************************************************************************************************/
	
	public Map getProjectBaselineTaskXML (Context context , String [] args)
	{
		boolean bIsBaselineCreated = false;
		Map returnMap                   = new HashMap();
		ArrayList alAssignList          =  new ArrayList();
		String strDuration              = "";
		String sAttrbPrcComplete        = "";
		String sEstStartDate            = "";
		String sEstEndDate              = "";
		String strDependentTasksDetail  = "";
		String strTaskName              = "";
		String strTaskId                = "";
		String sAttribEstStartDate      = "";
		String sAttribEstEndDate        = "";
		String strParentSubtaskId       = "";
		String strDependentTasksId      = "";
		String strTaskType              = "";
		String strDependentTaskId       = "";
		String strDependencyType        = "";
		String strAssignName            = "";
		String strLastCode              = "";
		String strActualStartDate       = "";
		String strActualEndDate         = "";
		String strLevel                 = "";
		String strParentCode            = "";
		String strProjectWBSCode        = "";
		String strDependentNumbers      = "";
		String strDependentChild        = "";
		String strTitle                 = "";
		String isTaskLeafLevel          = "";
		String strReasonfordelay        = "";
		String strActionPlanforcatchup  = "";
		String strComments              = "";
		String strIcon                  = "";
		String strGANTTGanttIcons       = "";
		String strGANTTGanttClass       = "";
		String strBGFormatcolor         = "";
		String strUserName              = "";
		// 05/06/2016 - Added for SCR - Add Critical Task filter - start
		String strCriticalTask	        = "";
		String strMSILCriticalTask	        = "";
		String strOOTBCriticalTask	        = "";
		String isCriticalTaskFlag 		= "";
		// 05/06/2016 - Added for SCR - Add Critical Task filter - end
		// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
		String strProjectState         = "";
		String strProjectStateAssociatedToTask = "";        
		// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End


		HashMap mapParentMap            = new HashMap();
		Map projectTaskMap              = new HashMap();
		StringList dependentTaskIdsList = new StringList();
		StringList dependentTypeList    = new StringList();
		StringList slPersonList         = new StringList();
		Date sJavaDate                  = null;
		// 05/06/2016 - Added for SCR - Add Slip days filter - start
		Date attrEstEndDate             = null;
		String strTaskState             = "";
		// 05/06/2016 - Added for SCR - Add Slip days filter - end
		Map mapDepdency                 = null;
		int nDependentTaskListSize      = 0;
		try
		{
			DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].from.name");

			HashMap programMap       = (HashMap) JPO.unpackArgs(args);
			String strProjectId      = (String)programMap.get("objectId");
			String strShowResource   = (String)programMap.get("ShowResource");
			String strShowDependency = (String)programMap.get("showDependency");
			String strFileName       = (String)programMap.get("FileName"); 
			String strWorkSpace      = (String)programMap.get("workSpace"); 

			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			StringList slObjectSelect = new StringList(16);
			slObjectSelect.add(DomainConstants.SELECT_NAME);
			slObjectSelect.add(DomainConstants.SELECT_ID);
			slObjectSelect.add(DomainConstants.SELECT_LEVEL);
			slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");
			slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");
			slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");
			slObjectSelect.add("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]");

			slObjectSelect.add("attribute[Baseline Initial End Date]");
			slObjectSelect.add("attribute[Baseline Initial Start Date]");

			slObjectSelect.add(task.SELECT_PREDECESSOR_TYPES);
			slObjectSelect.add(task.SELECT_PREDECESSOR_IDS);
			slObjectSelect.add("to[Subtask].from.id"); // fetch parent of task
			slObjectSelect.add("from[Subtask]"); // child Task
			slObjectSelect.add("to[Assigned Tasks].from.name"); // 
			slObjectSelect.add("attribute[MSILReasonfordelay].value");
			slObjectSelect.add("attribute[MSILActionPlanforcatchup].value");
			slObjectSelect.add("attribute[Comments].value");
			// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
			slObjectSelect.add(DomainConstants.SELECT_CURRENT);
			slObjectSelect.add("to[Project Access Key].from.from[Project Access List].to.current");         
			// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
			// 05/06/2016 - Added for SCR - Add Critical Task filter - start
			slObjectSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
			slObjectSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_OOTB_CRITICAL_TASK);
			// 05/06/2016 - Added for SCR - Add Critical Task filter - end


			//slObjectSelect.add(SELECT_TASK_ACTUAL_START_DATE); // 
			//slObjectSelect.add(SELECT_TASK_ACTUAL_FINISH_DATE); // 

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

			StringList slRelSelect = new StringList(4);
			slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
			slRelSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
			slRelSelect.addElement(DomainConstants.SELECT_LEVEL);
			slRelSelect.add("attribute[Task WBS]");

			ArrayList<String> alIds = new ArrayList<String>();			
			ProjectSpace project    = new ProjectSpace(strProjectId);
			DomainObject taskDO     = DomainObject.newInstance(context);

			DocumentBuilderFactory docFactory   = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder          = docFactory.newDocumentBuilder();
			Document doc                        = docBuilder.newDocument();

			StringList relSelects = new StringList(2);
			relSelects.add(DomainRelationship.SELECT_ID);
			MapList baselineList = project.getBaselineLog(context, relSelects);

			int revision = baselineList.size();
			if(revision == 0)
				bIsBaselineCreated = false;
			else
				bIsBaselineCreated = true;
			// fetch all tasks of the project
			MapList projectTaskList = task.getTasks(context, project, 0 , slObjectSelect, slRelSelect);
			if(null != projectTaskList && !projectTaskList.isEmpty())
			{
				int nListSize = projectTaskList.size();

				Element elGrid = doc.createElement("Grid"); //creating root node
				doc.appendChild(elGrid);

				Element elBody = doc.createElement("Body"); //creating root node
				elGrid.appendChild(elBody);

				Element elB = doc.createElement("B"); //creating root node
				elBody.appendChild(elB);

				Element elParent    =   elB;				
				for(int nTaskCount = 0; nTaskCount < nListSize; nTaskCount++)
				{
					alIds.add((String)((Map)projectTaskList.get(nTaskCount)).get(DomainConstants.SELECT_ID));
				}

				for(int nTaskCount = 0; nTaskCount < nListSize; nTaskCount++)
				{
					projectTaskMap      = (Map)projectTaskList.get(nTaskCount);					
					strTaskId           = (String)projectTaskMap.get(DomainConstants.SELECT_ID);
					isTaskLeafLevel     = ((String)projectTaskMap.get("from[Subtask]")).equals("False") ? "0" : "1"; //False - leaf level task
					strGANTTGanttIcons  = ((String)projectTaskMap.get("from[Subtask]")).equals("False") ? "" : "1"; //False - leaf level task
					strParentCode       = (String)projectTaskMap.get("attribute[Task WBS]");
					strLevel            = (String)projectTaskMap.get("level");
					strTaskName         = (String)projectTaskMap.get("name");
					sAttrbPrcComplete   = (String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_PERCENT_COMPLETE + "]");
					//strParentSubtaskId  = (String)projectTaskMap.get("to[Subtask].from.id");					
					strTaskType         = (String)projectTaskMap.get(DomainConstants.SELECT_TYPE);


					strReasonfordelay           = (String)projectTaskMap.get("attribute[MSILReasonfordelay].value");
					strActionPlanforcatchup     = (String)projectTaskMap.get("attribute[MSILActionPlanforcatchup].value");
					strComments                 = (String)projectTaskMap.get("attribute[Comments].value");

					// 05/06/2016 - Added for SCR - Add Critical Task filter - start
					strMSILCriticalTask           = (String)projectTaskMap.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
					strOOTBCriticalTask           = (String)projectTaskMap.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_OOTB_CRITICAL_TASK);

					strCriticalTask     = (strOOTBCriticalTask.equalsIgnoreCase("true")||strMSILCriticalTask.equalsIgnoreCase("true")) ?  "Yes" : "No";
					isCriticalTaskFlag = strCriticalTask.equalsIgnoreCase("Yes") ?  "1" : "0"; // 0 if task is not critical
					// 05/06/2016 - Added for SCR - Add Critical Task filter - end

					slPersonList        = projectTaskMap.get("to[Assigned Tasks].from.name")  instanceof String ? new StringList((String)projectTaskMap.get("to[Assigned Tasks].from.name")) :(StringList) projectTaskMap.get("to[Assigned Tasks].from.name");

					sAttribEstStartDate = (String)projectTaskMap.get("attribute[Baseline Initial Start Date]");//(String)projectTaskMap.get( "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]");
					if(null != sAttribEstStartDate && sAttribEstStartDate.length() > 0)
					{
						sJavaDate           = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(sAttribEstStartDate);
						sAttribEstStartDate = sdf.format(sJavaDate);
					}
								
					sAttribEstEndDate   = (String)projectTaskMap.get("attribute[Baseline Initial End Date]");//(String)projectTaskMap.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");
                    					
					// 05/06/2016 - Added/Modified for SCR - Add Slip days filter - start
					int iSlipDays = 0;
                    if(null != sAttribEstEndDate && sAttribEstEndDate.length() > 0)
                    {   
                    	attrEstEndDate      = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(sAttribEstEndDate);                    
                    	sAttribEstEndDate   = sdf.format(attrEstEndDate);
                    	
                    	iSlipDays = getTaskStatusSlip(context, attrEstEndDate, strTaskState);
                    }
                    strTaskState        = (String)projectTaskMap.get(DomainConstants.SELECT_CURRENT);
                    // 05/06/2016 - Added/Modified for SCR - Add Slip days filter - end
                    
                    task.setId(strTaskId);
                    if((null != sAttribEstStartDate && sAttribEstStartDate.length() > 0) && (null != sAttribEstEndDate && sAttribEstEndDate.length() > 0))
                    {
						Date estStartdate = sdf.parse(sAttribEstStartDate);
						Date estFinishdate = sdf.parse(sAttribEstEndDate);
						long lDuration = task.computeDuration(estStartdate, estFinishdate);
						strDuration = String.valueOf(lDuration);
                    }
                    else
                    	strDuration = "";
					strBGFormatcolor    =   "100".equals(sAttrbPrcComplete)?"Green":  "Red";

					sAttribEstStartDate = "Milestone".equals(strTaskType)?"":sAttribEstStartDate;
					// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start
					boolean isTaskTobeShown=true;
					// if current Object is a Project
					if("Project Space".equals(strTaskType))
					{
						strProjectWBSCode   = (String)projectTaskMap.get("attribute[Task WBS]"); 
						strIcon             = "../common/images/iconSmallProject.gif";
						strGANTTGanttClass  = "Group";                      
						strProjectState         = (String)projectTaskMap.get(DomainConstants.SELECT_CURRENT);
						if ( "Cancel".equalsIgnoreCase(strProjectState) || "Hold".equalsIgnoreCase(strProjectState))
						{                   
							isTaskTobeShown=false;                          
						}               

					}
					// else current Object is a Task
					else
					{
						strIcon             = "../common/images/iconSmallTask.gif";
						strGANTTGanttClass  = "";

						if(projectTaskMap.containsKey("to[Project Access Key].from.from[Project Access List].to.current")){
							strProjectStateAssociatedToTask = (String)projectTaskMap.get("to[Project Access Key].from.from[Project Access List].to.current");                       
							if ( "Cancel".equalsIgnoreCase(strProjectStateAssociatedToTask) || "Hold".equalsIgnoreCase(strProjectStateAssociatedToTask))
							{                           
								isTaskTobeShown=false;                          
							}   
						}
					}
					// if Project itself or Project Associated To Task is in Hold or Cancel State then dont consider
					if(!isTaskTobeShown)
					{
						continue;
					}

					// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End

					// dependent task ids & type
					dependentTaskIdsList    = new StringList();
					dependentTypeList       = new StringList();
					if(projectTaskMap.containsKey(task.SELECT_PREDECESSOR_IDS))
					{
						dependentTaskIdsList    =   projectTaskMap.get(task.SELECT_PREDECESSOR_IDS)  instanceof String ? new StringList((String)projectTaskMap.get(task.SELECT_PREDECESSOR_IDS)) :(StringList) projectTaskMap.get(task.SELECT_PREDECESSOR_IDS);
						dependentTypeList       =   projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES)  instanceof String ? new StringList((String)projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES)) :(StringList) projectTaskMap.get(task.SELECT_PREDECESSOR_TYPES);
					}

					strDependentTasksId         = "";
					strDependentTasksDetail     = "";
					strDependentNumbers         = "";
					strDependentChild           = "";
					if("True".equals(strShowDependency) && null != dependentTaskIdsList && dependentTaskIdsList.size() > 0 )
					{
						nDependentTaskListSize  = dependentTaskIdsList.size();
						for(int nCount = 0; nCount < nDependentTaskListSize; nCount++)
						{
							strDependentTaskId  = (String)dependentTaskIdsList.get(nCount);
							//taskDO.setId(strDependentTaskId);
							//strDependentChild   =   taskDO.getInfo(context , "to[Subtask].attribute[Task WBS].value");
							int indexOfTask     = alIds.indexOf(strDependentTaskId);
							try
							{
								mapDepdency         = (Map)projectTaskList.get(indexOfTask);
							}
							catch (Exception Ex)
							{
								mapDepdency=null;
							}
							if(mapDepdency!=null)
							{
								strDependentChild   = (String) mapDepdency.get("attribute[Task WBS]");
								if(!"".equals(strProjectWBSCode))
								{
									strDependentChild   = strProjectWBSCode+"."+strDependentChild;
								}
								if(nCount == 0)
								{
									strDependentNumbers = strDependentChild+(String)dependentTypeList.get(nCount);
								}
								else
								{
									strDependentNumbers = strDependentNumbers + ";" + strDependentChild+(String)dependentTypeList.get(nCount);
								}
							}
						}
						strDependentNumbers = strDependentNumbers.replaceAll("\\.", "_");


					//	strDependentNumbers = strDependentNumbers.replaceAll("_", "\\$");





					}
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
					if("True".equals(strShowResource) && slPersonList!=null )
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
						elNode.setAttribute("T",strTaskName);
						//elNode.setAttribute("T","|../common/emxTree.jsp?objectId="+strTaskId+"|"+strTaskName+"|new_window");
						//elNode.setAttribute("TType","Link");
						//elNode.setAttribute("TCanEdit" ,"0");
						//elNode.setAttribute("TOnClickLink","Grids.Alert=1;var ret=!confirm('Do you want to navigate here?');Grids.Alert=0;return ret");


						elNode.setAttribute("Expanded","0");
						elNode.setAttribute("TIcon",strIcon);
						elNode.setAttribute("TLink","../common/emxTree.jsp?objectId="+strTaskId);
						elNode.setAttribute("TLinkTarget","_blank");

						elNode.setAttribute("GGanttIcons",strGANTTGanttIcons);
						elNode.setAttribute("GGanttClass",strGANTTGanttClass);
						elNode.setAttribute("id",strLastCode);
						elNode.setAttribute("C",sAttrbPrcComplete);
						elNode.setAttribute("CTip",sAttrbPrcComplete);
						elNode.setAttribute("S",sAttribEstStartDate);
						elNode.setAttribute("E",sAttribEstEndDate);
						elNode.setAttribute("D",strDependentNumbers);
						elNode.setAttribute("RESOURCES",strAssignName);
						elNode.setAttribute("DUR",strDuration);
						elNode.setAttribute("TaskId",strTaskId);
						elNode.setAttribute("SLACK",strTitle);
						elNode.setAttribute("Summery",isTaskLeafLevel);
						elNode.setAttribute("Reason",strReasonfordelay);
						elNode.setAttribute("ActionPlan",strActionPlanforcatchup);
						elNode.setAttribute("Comments",strComments);
						// 05/06/2016 - Added for SCR - Add Slip days filter - start
						elNode.setAttribute("SlipDays", "" +iSlipDays);
						// 05/06/2016 - Added for SCR - Add Slip days filter - end
						// 05/06/2016 - Added for SCR - Add Critical Task filter - start
						elNode.setAttribute("CriticalTask", strCriticalTask);
						elNode.setAttribute("CriticalTaskFlag", isCriticalTaskFlag);
						// 05/06/2016 - Added for SCR - Add Critical Task filter - end
						elB.appendChild(elNode);
						mapParentMap.put(strTaskId , elNode);
					}
					else
					{   
						// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- Start

						StringList slParentIdList   = projectTaskMap.get("to[Subtask].from.id")  instanceof String ? new StringList((String)projectTaskMap.get("to[Subtask].from.id")) :(StringList) projectTaskMap.get("to[Subtask].from.id");
						if(slParentIdList !=null && slParentIdList.size()>0){
							for(int p=0;p<slParentIdList.size();p++)
							{
								//String strParentId1  = (String)projectTaskMap.get("to[Subtask].from.id");	
								String strParentId1  = (String)slParentIdList.get(p);								
								Element elParentNode = (Element)  mapParentMap.get(strParentId1);
								if(elParentNode !=null){								
									Element elNode = doc.createElement("I");
									elNode.setAttribute("T",strTaskName);
									//elNode.setAttribute("T","|../common/emxTree.jsp?objectId="+strTaskId+"|"+strTaskName+"|new_window");
									//elNode.setAttribute("TType","Link");
									//elNode.setAttribute("TOnClickLink","");
									elNode.setAttribute("Expanded","0");
									elNode.setAttribute("TIcon",strIcon);
									elNode.setAttribute("TLink","../common/emxTree.jsp?objectId="+strTaskId);
									elNode.setAttribute("TLinkTarget","_blank");

									elNode.setAttribute("GGanttIcons",strGANTTGanttIcons);
									elNode.setAttribute("GGanttClass",strGANTTGanttClass);
									elNode.setAttribute("id",strLastCode);
									elNode.setAttribute("C",sAttrbPrcComplete);
									elNode.setAttribute("CTip",sAttrbPrcComplete);
									elNode.setAttribute("S",sAttribEstStartDate);
									elNode.setAttribute("E",sAttribEstEndDate);
									elNode.setAttribute("D",strDependentNumbers);
									elNode.setAttribute("RESOURCES",strAssignName);
									elNode.setAttribute("DUR",strDuration);
									elNode.setAttribute("TaskId",strTaskId);
									elNode.setAttribute("SLACK",strTitle);
									elNode.setAttribute("Summery",isTaskLeafLevel);
									elNode.setAttribute("Reason",strReasonfordelay);
									elNode.setAttribute("ActionPlan",strActionPlanforcatchup);
									elNode.setAttribute("Comments",strComments);
									// 05/06/2016 - Added for SCR - Add Slip days filter - start
									elNode.setAttribute("SlipDays", "" +iSlipDays);
									// 05/06/2016 - Added for SCR - Add Slip days filter - end
									// 05/06/2016 - Added for SCR - Add Critical Task filter - start
									elNode.setAttribute("CriticalTask", strCriticalTask);
									elNode.setAttribute("CriticalTaskFlag", isCriticalTaskFlag);
									// 05/06/2016 - Added for SCR - Add Critical Task filter - end
									elParentNode.appendChild(elNode);
									mapParentMap.put(strTaskId , elNode );

									// Added and Modifed by Ajit -- 05/04/2016 -- Not to show Hold and Cancel Projets As part of Gantt Chart -- End
								}
							}
						}
					}
				}
				// 05/06/2016 - Added for SCR - Add Slip days filter - start
				updateParentSlipDaysAllProject(doc);
				// 05/06/2016 - Added for SCR - Add Slip days filter - end
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File templateFile       =   new File(strWorkSpace);
			File templateXMLFile    =   new File(templateFile, strFileName);
			StreamResult result     = new StreamResult(templateXMLFile);			
			transformer.transform(source, result);			
			HashSet hs = new HashSet();
			hs.addAll(alAssignList);
			alAssignList.clear();
			alAssignList.addAll(hs);
			returnMap.put("assignUser",alAssignList);
			returnMap.put("isBaselineCreated",bIsBaselineCreated);
		}
		catch (Exception ex )
		{
			ex.printStackTrace();
		}
		finally
		{
			DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].from.name");
		}
		return returnMap;
	}

    // Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- Start
   /**
    * Method: This method updates Route Template for Task.
    * 
    * @param context
    *            the eMatrix <code>Context</code> object
    * @param args
    *            holds program map.
    * @return void
    * @throws Exception

    *
             if the operation fails

    */
    public void updateRouteTemplate(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap) programMap.get("paramMap");
        String strObjectId = (String) paramMap.get("objectId");
        String strNewOId   = (String) paramMap.get("New OID");
        
        try {
            if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId)){
                DomainObject doTask = new DomainObject(strObjectId);
                String strOldRelId = (String) doTask.getInfo(context, "from["+MSILConstants_mxJPO.REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE+"].id");
                
                if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strOldRelId))
                {
                    DomainRelationship.disconnect(context, strOldRelId);
                }
                
                if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strNewOId))
                {
                    DomainObject doRouteTemplate = new DomainObject(strNewOId);
                    DomainRelationship.connect(context, doTask, MSILConstants_mxJPO.REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE, doRouteTemplate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
    
   /**
    * Method: This method check Access of Define Approval Route command.
    * 
    * @param context
    *            the eMatrix <code>Context</code> object
    * @param args
    *            holds program map.
    * @return boolean
    * @throws Exception
    *             if the operation fails
    */
    public boolean hasAccessDefineApprovalRoute(Context context,String[] args) throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");
        try {
            DomainObject domObj    = new DomainObject(objectId);
            StringList slSelect = new StringList();
            slSelect.add(DomainConstants.SELECT_TYPE);
            slSelect.add(DomainConstants.SELECT_OWNER);
            slSelect.add("to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.owner");
            slSelect.add("to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id");
            Map mapTaskInfo = domObj.getInfo(context, slSelect);
            String strObjectType   = (String)mapTaskInfo.get(DomainConstants.SELECT_TYPE);
            StringList slProjOwner = new StringList();

            // Query Project Owner of Project/Task.
            if (DomainConstants.TYPE_PROJECT_CONCEPT.equals(strObjectType) || DomainConstants.TYPE_PROJECT_SPACE.equals(strObjectType))
            {
                slProjOwner.add((String)mapTaskInfo.get(DomainConstants.SELECT_OWNER));
            }
            else if (DomainConstants.TYPE_TASK.equals(strObjectType))
            {
                slProjOwner.add((String)mapTaskInfo.get(DomainConstants.SELECT_OWNER));
                slProjOwner.add((String)mapTaskInfo.get("to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.owner"));
                objectId = (String)mapTaskInfo.get("to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id");
            }
            

            if(slProjOwner.contains(context.getUser()))
                return true;
            else if (DomainConstants.TYPE_PROJECT_CONCEPT.equals(strObjectType))
                return false;
            
            // Query Master Project Owner.
            MQLCommand mql  = new MQLCommand();
            String sMql     = "expand bus " + objectId + " to rel 'Subtask' recurse to end select bus id";
            boolean bResult = mql.executeCommand(context, sMql);
            if(bResult)
            {
                String sResult = mql.getResult();
                if(null !=  sResult && sResult.contains("="))
                {
                    String strMasterProjectId = sResult.substring(sResult.indexOf("=") + 1).trim();
                    DomainObject domMasterObj = new DomainObject(strMasterProjectId);
                    slProjOwner.add(domMasterObj.getOwner(context).toString());
                }
            }
            
            if(slProjOwner.contains(context.getUser()))
                return true;

        }catch (Exception e) {
            throw e;
        }
        return false;
    }
    
    
    /**
     * showRouteTemplateEditable Edit access should be only in Create state.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds programMap
     * @return boolean
     * @throws Exception
     *             if the operation fails
     */
    public boolean showRouteTemplateEditable(Context context, String args[]) throws Exception 
    {
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
        String strObjectId  = (String) programMap.get("objectId");
        try 
        {
            settingsMap.put("Editable", "false");
            if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
            {
                StringList slTaskSelect = new StringList(DomainObject.SELECT_CURRENT);

                DomainObject doTask = new DomainObject(strObjectId);
                Map mtaskDatamap    = (Map) doTask.getInfo(context, slTaskSelect);
                String strCurrent   = (String) mtaskDatamap.get(DomainObject.SELECT_CURRENT);

                if(hasAccessDefineApprovalRoute(context, args) && "Create".equalsIgnoreCase(strCurrent))
                {
                    settingsMap.put("Editable", "true");
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        throw ex;
        }
        return true;
    }
    // Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- End
}