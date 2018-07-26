/*
 *  emxInboxTask.java
 *
 * Copyright (c) 1992-2017 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
 /* 
Change History:
Date           |    Change By    | Tag to be searched |       Details
===========================================================================================================
26-Aug-2014    |   Intelizign    |  26-Aug-2014       | PE Build 2.1 - Changes made to show only Inbox Task on "Task" page
02/05/2016     |   Ajit          |  02/05/2016        | To Add Content Name column in the Tasks Page
07/10/2016     | Vartika         |  FP1638            | Code merged with FP1638 OOTB code
*/
import matrix.db.*;
import java.lang.*;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;
import java.lang.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.RouteTemplate;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import matrix.db.Access;
import matrix.db.Attribute;
import matrix.db.AttributeItr;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectAttributes;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.Group;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Relationship;
import matrix.db.Role;
import matrix.util.List;
import com.dassault_systemes.enovia.bps.widget.UIFieldValue;
import com.dassault_systemes.enovia.bps.widget.UIWidget;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import java.lang.reflect.Method;

// Added by Ajit -- 02/05/2016 -- To Add Content Name column in the Tasks Page -- Start
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.util.FrameworkProperties;
// Added by Ajit -- 02/05/2016 -- To Add Content Name column in the Tasks Page -- End
/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxInboxTask_mxJPO extends emxInboxTaskBase_mxJPO
{
    private static final String sAttrReviewCommentsNeeded = PropertyUtil.getSchemaProperty("attribute_ReviewCommentsNeeded");
    private static final String sAttrRouteAction = PropertyUtil.getSchemaProperty("attribute_RouteAction");
    private static final String sAttrScheduledCompletionDate = PropertyUtil.getSchemaProperty("attribute_ScheduledCompletionDate");
    private static final String sAttrTitle = PropertyUtil.getSchemaProperty("attribute_Title");
    private static final String selTaskCompletedDate = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
    private static final String sTypeInboxTask = PropertyUtil.getSchemaProperty("type_InboxTask");
    private static final String sRelProjectTask = PropertyUtil.getSchemaProperty("relationship_ProjectTask");
    private static final String sRelRouteTask = PropertyUtil.getSchemaProperty("relationship_RouteTask");
    private static final String sRelRouteScope = PropertyUtil.getSchemaProperty("relationship_RouteScope");
    private static final String strAttrRouteAction = "attribute["+sAttrRouteAction +"]";
    private static final String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
    private static final String strAttrTitle="attribute["+sAttrTitle+"]";
    private static final String strAttrTaskCompletionDate ="attribute["+selTaskCompletedDate+"]";
    private static String routeIdSelectStr="from["+sRelRouteTask+"].to.id";
    private static String routeTypeSelectStr="from["+sRelRouteTask+"].to.type";
    private static String routeNameSelectStr ="from["+sRelRouteTask+"].to.name";
    private static String routeOwnerSelectStr="from["+sRelRouteTask+"].to.owner";
    private static String objectNameSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.name";
    private static String objectIdSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.id";
    private static final String sRelAssignedTask = PropertyUtil.getSchemaProperty("relationship_AssignedTasks");
    private static final String sRelSubTask = PropertyUtil.getSchemaProperty("relationship_Subtask");
    private static final String sRelWorkflowTask = PropertyUtil.getSchemaProperty("relationship_WorkflowTask");
    private static final String sRelWorkflowTaskAssinee = PropertyUtil.getSchemaProperty("relationship_WorkflowTaskAssignee");
    private static final String workflowIdSelectStr = "to["+sRelWorkflowTask+"].from.id";
    private static final String workflowNameSelectStr = "to["+sRelWorkflowTask+"].from.name";
    private static final String workflowTypeSelectStr = "to["+sRelWorkflowTask+"].from.type";
    private static final String sTypeWorkflowTask = PropertyUtil.getSchemaProperty("type_WorkflowTask");
    private static final String policyWorkflowTask = PropertyUtil.getSchemaProperty("policy_WorkflowTask");
    private static final String attrworkFlowDueDate = PropertyUtil.getSchemaProperty("attribute_DueDate");
    private static final String attrTaskEstinatedFinishDate = PropertyUtil.getSchemaProperty("attribute_TaskEstimatedFinishDate");
    private static final String attrworkFlowActCompleteDate = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
    private static final String attrTaskFinishDate = PropertyUtil.getSchemaProperty("attribute_TaskActualFinishDate");
    private static String strAttrworkFlowDueDate = "attribute[" + attrworkFlowDueDate + "]";
    private static String strAttrTaskEstimatedFinishDate = "attribute[" + attrTaskEstinatedFinishDate + "]";
    private static String strAttrTaskFinishDate = "attribute[" + attrTaskFinishDate + "]";
    private static String strAttrworkFlowCompletinDate = "attribute[" + attrworkFlowActCompleteDate + "]";
    private static final String strAttrBracket  = "attribute[";
    private static final String strCloseBracket = "]";
    private static final String strFromBracket = "from[";
    private static final String strBracketToToBracket = "].to.to[";

    private static final String sAttrReviewersComments = PropertyUtil.getSchemaProperty("attribute_ReviewersComments");
    private static final String sAttrReviewTask = PropertyUtil.getSchemaProperty("attribute_ReviewTask");
    private static final String policyTask = PropertyUtil.getSchemaProperty("policy_InboxTask");
    private i18nNow loc = new i18nNow();
    protected String lang=null;
    protected String rsBundle=null;
    private static final String sRelWorkflowTaskDeliverable = PropertyUtil.getSchemaProperty("relationship_TaskDeliverable");
    private static final String sTypeWorkflow = PropertyUtil.getSchemaProperty("type_Workflow");
    private static final String attrworkFlowInstructions = PropertyUtil.getSchemaProperty("attribute_Instructions");
    private static final String TYPE_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty("Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");
    private static final String TYPE_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty("Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");
    private static final String sRelProjectRouteTask = PropertyUtil.getSchemaProperty("relationship_ProjectRoute");
    private static final String strAttrTaskApprovalStatus  = getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS);
    private static final String routeApprovalStatusSelectStr ="from["+sRelRouteTask+"].to."+Route.SELECT_ROUTE_STATUS ;
    private static final String TASK_PROJECT_ID = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
    private static final String TASK_PROJECT_TYPE = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
    private static final String TASK_PROJECT_NAME = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";

    // added for IR - 043921V6R2011
    public static final String  SELECT_TEMPLATE_OWNING_ORG_ID =  "from["+ RELATIONSHIP_ROUTE_TASK + "].to.from[" + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE + "].to.to[" + RELATIONSHIP_OWNING_ORGANIZATION + "].from.id";
    public static final String SELECT_ROUTE_NODE_ID = getAttributeSelect(ATTRIBUTE_ROUTE_NODE_ID);
    public static final String SELECT_TASK_ASSIGNEE_CONNECTION = "from[" + RELATIONSHIP_PROJECT_TASK + "].id";
    protected static final String PERSON_WORKSPACE_LEAD_GRANTOR = PropertyUtil.getSchemaProperty("person_WorkspaceLeadGrantor");
    protected static final String SELECT_TASK_ASSIGNEE_NAME = "from[" + RELATIONSHIP_PROJECT_TASK + "].to.name";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxInboxTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
     /**
     * getMyDeskTasks - gets the list of Tasks the user has access
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     * 
     * Method moved from Base JPO to non-base JPO to include customization for MSIL
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getMyDeskTasks(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
            selectTypeStmts.add(taskObject.SELECT_NAME);
            selectTypeStmts.add(taskObject.SELECT_ID);
            selectTypeStmts.add(taskObject.SELECT_DESCRIPTION);
            selectTypeStmts.add(taskObject.SELECT_OWNER);
            selectTypeStmts.add(taskObject.SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add(strAttrTaskApprovalStatus);
            selectTypeStmts.add(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION));
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(routeApprovalStatusSelectStr);

            selectTypeStmts.add(taskObject.SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_TYPE);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();
            Pattern relPattern = new Pattern(sRelProjectTask);
            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);
            Pattern typePattern = new Pattern(sTypeInboxTask);
            // MSIL CHANGES START BY INTELIZIGN - 26-Aug-2014
            // comment the following types
            //typePattern.addPattern(DomainObject.TYPE_TASK);
            //typePattern.addPattern(sTypeWorkflowTask);
            typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);

            // add selects for Parent Task & Project column
            String objectRouteIdSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.id";
            String objectRouteTypeSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.type";
            String objectRouteNameSelectStr ="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.name";
            selectTypeStmts.add(objectRouteIdSelectStr);
            selectTypeStmts.add(objectRouteTypeSelectStr);
            selectTypeStmts.add(objectRouteNameSelectStr);
            // MSIL CHANGES END BY INTELIZIGN - 26-Aug-2014
            SelectList selectStmts = new SelectList();
            taskObject.setId(sPersonId);
            String busWhere = null;
            ContextUtil.startTransaction(context,false);
            ExpansionIterator expItr = taskObject.getExpansionIterator(context,
                                                                       relPattern.getPattern(),
                                                                       typePattern.getPattern(),
                                                                       selectTypeStmts,
                                                                       selectRelStmts,
                                                                       true,
                                                                       true,
                                                                       (short)2,
                                                                       busWhere,
                                                                       null,
                                                                       (short)0,
                                                                       false,
                                                                       false,
                                                                       (short)100,
                                                                       false);
            com.matrixone.apps.domain.util.MapList taskMapList = null;
            try {
                taskMapList = FrameworkUtil.toMapList(expItr,(short)0,null,null,null,null);
            } finally {
                expItr.close();
            }
            ContextUtil.commitTransaction(context);
            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                // if Task is WBS then add the context (top) object information
                if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
                {
                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {
                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {
                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }
                finalTaskMapList.add(objectMap);
            }
            return finalTaskMapList;
        }
        catch (Exception ex)
        {
           System.out.println("Error in getMyDeskTasks = " + ex.getMessage());
            throw ex;
        }
  }
  /**
     * getActiveTasks - gets the list of Tasks in Assigned State
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getActiveTasks(Context context, String[] args) throws Exception
   {
        String stateInboxTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_INBOX_TASK,"state_Assigned");
        String stateWorkFlowTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Assigned");
        String stateTaskAssign = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Assign");
        String stateTaskActive = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Active");
        String stateTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Review");
         //commented for Bug NO:338177
        /* String WBSWhereExp = "(type == 'Task'";
        if(stateTaskReview == null || "".equals(stateTaskReview) || "null".equals(stateTaskReview))
        {
          WBSWhereExp = WBSWhereExp+")";
        } else {
          WBSWhereExp = WBSWhereExp +" && current == " + stateTaskReview + ")";
        }*/
        StringBuffer sbf=new StringBuffer();
        if(stateInboxTaskAssigned != null && !"".equals(stateInboxTaskAssigned))
        {
          sbf.append("(current == "+stateInboxTaskAssigned);
          sbf.append(" && " + "from[" + RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + ATTRIBUTE_ROUTE_STATUS + "] != \"Stopped\") ");          
        }
        if(stateWorkFlowTaskAssigned!=null &&!"".equals(stateWorkFlowTaskAssigned))
        {
            if(sbf.length()!=0) {
              sbf.append(" || (");
            }
            sbf.append("type == \"" + sTypeWorkflowTask + "\" && ");
            sbf.append("current == "+stateWorkFlowTaskAssigned + ")");
         }
        if( stateTaskAssign!=null &&!"".equals( stateTaskAssign))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskAssign);
        }
        if( stateTaskActive!=null &&!"".equals( stateTaskActive))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskActive);
        }
        if(stateTaskReview!=null &&!"".equals( stateTaskReview))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskReview);
        }
       // commented for Bug NO:338177
        /*  if(  WBSWhereExp!=null &&!"".equals(  WBSWhereExp))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append(WBSWhereExp);
        }*/
        return getTasks(context,sbf.toString()) ;
   }
     /**
     * getCompletedTasks - gets the list of Tasks in Complete State
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     * 
     * Method moved from Base JPO to non-base JPO to include customization for MSIL
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getCompletedTasks(Context context, String[] args) throws Exception
   {
        String stateInboxTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Complete");
    String stateWorkFlowTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Completed");
    String stateTaskComplete = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Complete");
    //added for the 325218
    StringBuffer sbf=new StringBuffer();
    if(stateInboxTaskComplete !=null && !"".equals(stateInboxTaskComplete))
      sbf.append("  current == "+ stateInboxTaskComplete);
    if(stateWorkFlowTaskComplete!=null &&!"".equals(stateWorkFlowTaskComplete))
     {
      if(sbf.length()!=0)
        sbf.append(" || ");
      sbf.append("current == "+stateWorkFlowTaskComplete);
     }
    if(stateTaskComplete!=null&&!"".equals(stateTaskComplete))
     {
      if(sbf.length()!=0)
        sbf.append(" || ");
        sbf.append("current == "+stateTaskComplete);
     }
       return getTasks(context,sbf.toString());
    //till here
   }
  /**
     * getTasks - gets the list of Tasks depending on condition
     * @param context the eMatrix <code>Context</code> object
     * @param busWhere condition to query
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     * 
     * Method moved from Base JPO to non-base JPO to include customization for MSIL
     */
   public Object getTasks(Context context, String busWhere ) throws Exception
   {
        try
        {
      long start=System.currentTimeMillis();
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            String stateInboxTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_INBOX_TASK,"state_Review");
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
      //Added for Bug No 338177 Begin
            StringList selectTypeStmtId = new StringList();
            selectTypeStmtId.add(taskObject.SELECT_ID);
            //Added for Bug No 338177 End
            selectTypeStmts.add(taskObject.SELECT_NAME);
            selectTypeStmts.add(taskObject.SELECT_ID);
            selectTypeStmts.add(taskObject.SELECT_DESCRIPTION);
            selectTypeStmts.add(taskObject.SELECT_OWNER);
            selectTypeStmts.add(taskObject.SELECT_MODIFIED);
            selectTypeStmts.add(taskObject.SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(taskObject.SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_TYPE);

            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();
            Pattern relPattern = new Pattern(sRelProjectTask);
            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);
            Pattern typePattern = new Pattern(sTypeInboxTask);
            // MSIL CHANGES START BY INTELIZIGN - 26-Aug-2014
            // comment the following types
            //typePattern.addPattern(DomainObject.TYPE_TASK);
            // typePattern.addPattern(sTypeWorkflowTask);
			typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);

            // add selects for Parent Task & Project column
            String objectRouteIdSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.id";
            String objectRouteTypeSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.type";
            String objectRouteNameSelectStr ="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.name";
            selectTypeStmts.add(objectRouteIdSelectStr);
            selectTypeStmts.add(objectRouteTypeSelectStr);
            selectTypeStmts.add(objectRouteNameSelectStr);
            // MSIL CHANGES END BY INTELIZIGN - 26-Aug-2014
            SelectList selectStmts = new SelectList();
            taskObject.setId(sPersonId);
           // get the list of tasks that needs owner review
       //Added for bug 352071
           String strResult = MqlUtil.mqlCommand(context,"temp query bus '"+sTypeInboxTask+"' * * where 'attribute["+sAttrReviewCommentsNeeded+"]==Yes && current=="+stateInboxTaskReview+" && from["+sRelRouteTask+"].to.owner==\""+context.getUser()+"\"' select id dump |");
       //end of bug 352071
            //Added for Bug No 338177 Begin
      com.matrixone.apps.domain.util.MapList taskMapList =  taskObject.getRelatedObjects(context,
                                                                                relPattern.getPattern(),
                                                                                typePattern.getPattern(),
                                                                                selectTypeStmtId,
                                                                                selectRelStmts,
                                                                                true,
                                                                                true,
                                                                                (short)2,
                                                                                busWhere,
                                                                                null,
                                                                                null,
                                                                                null,
                                                                                null);
       //Added for bug 352071
      if(strResult!=null && !"".equals(strResult))
            {
                String taskInbox = "";
                StringList strlResult = new StringList();
                String strTemp = "";
                StringList taskIds =FrameworkUtil.split(strResult,"\n");
                Iterator taskIdIterator=taskIds.iterator();
                while(taskIdIterator.hasNext())
                {
                    Map tempMap= new HashMap();
                    taskInbox=(String)taskIdIterator.next();
                    strlResult = FrameworkUtil.split(taskInbox,"|");
                    strTemp=(String)strlResult.get(3);
					boolean isPresent = false;
                    for( int i=0; i<taskMapList.size(); i++){
            			Map map = (Map)taskMapList.get(i);
            			String id = (String)map.get("id");
            			if(strTemp.equals(id)){
            				isPresent = true;
							break;
            			}
            		}
                    if(!isPresent){
                    tempMap.put("id",strTemp);
                    taskMapList.add(tempMap);
                }
            }
            }
       //end for bug 352071
            String[] objectIds=new String[taskMapList.size()];
            Iterator idsIterator=taskMapList.iterator();
            for(int i=0;idsIterator.hasNext();i++){
                Map map=(Map)idsIterator.next();
                objectIds[i]=(String)map.get("id");
            }
            taskMapList=DomainObject.getInfo(context,objectIds,selectTypeStmts);
            //Added for Bug No 338177 End
            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
           Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                // if Task is WBS then add the context (top) object information
                if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
                {
                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {
                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {
                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }
                finalTaskMapList.add(objectMap);
            }
      long end=System.currentTimeMillis();
            return finalTaskMapList;
        }
        catch (Exception ex)
        {
            System.out.println("Error in getTasks = " + ex.getMessage());
            throw ex;
        }
   }
    /**
     * getTasksToBeAccepted - gets the list of Tasks assigned to any of the person assignments
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     * 
     * Method moved from Base JPO to non-base JPO to include customization for MSIL
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getTasksToBeAccepted(Context context, String[] args)
          throws Exception
      {
        MapList taskMapList = new MapList();
        try
        {
           final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
           StringList selectTypeStmts = new StringList();
           selectTypeStmts.add(SELECT_NAME);
           selectTypeStmts.add(SELECT_ID);
           selectTypeStmts.add(SELECT_DESCRIPTION);
           selectTypeStmts.add(SELECT_OWNER);
           selectTypeStmts.add(SELECT_CURRENT);
           selectTypeStmts.add(strAttrRouteAction);
           selectTypeStmts.add(strAttrCompletionDate);
           selectTypeStmts.add(strAttrTaskCompletionDate);
           selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
           selectTypeStmts.add(strAttrTitle);
           selectTypeStmts.add(objectIdSelectStr);
           selectTypeStmts.add(objectNameSelectStr);
           selectTypeStmts.add(routeIdSelectStr);
           selectTypeStmts.add(routeNameSelectStr);
           selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_TYPE);

           String strPersonAssignments = "";
           Vector groupAssignments = new Vector();
           Vector personAssignments = PersonUtil.getAssignments(context);
           personAssignments.remove(context.getUser());
           Iterator assignmentsItr = personAssignments.iterator();
           //Begin : Bug 346478
           Role roleObj = null;
           Group groupObj = null;
           StringList slParents = new StringList();
           StringList slParentRolesOrGroups = new StringList();
           //End : Bug 346478
           while(assignmentsItr.hasNext())
           {
               String assignment = (String)assignmentsItr.next();
               //Added the below lines of code for the bug 344483, to handle persons under a group and role
               /*String cmd = MqlUtil.mqlCommand(context, "print user \"" + assignment + "\" select isagroup isarole dump |");
               boolean isGroup = "TRUE|FALSE".equalsIgnoreCase(cmd);
               boolean isRole = "FALSE|TRUE".equalsIgnoreCase(cmd);
               if(isGroup || isRole)
               {
                   if(isGroup)
                   {
                       groupAssignments = new Group(assignment).getAssignments(context);
                   }
                   else
                   {
                       groupAssignments = new Role(assignment).getAssignments(context);
                   }
                   Iterator assignmentsItrr = groupAssignments.iterator();
                   while(assignmentsItrr.hasNext())
                   {
                        String grpAssignment = ((matrix.db.Person)assignmentsItrr.next()).getName();
                        strPersonAssignments +=  grpAssignment + ",";
                   }
               }*/
               //End of code for the bug 344483
               //strPersonAssignments += assignment + ",";
               //Begin : Bug 346478 code modification
               // Is it role?
               try {
                   roleObj = new Role(assignment);
                   roleObj.open(context);
                   // Find all its parents
                   slParents = roleObj.getParents(context, true);
                   if (slParents != null) {
                       slParentRolesOrGroups.addAll(slParents);
                   }
                   roleObj.close(context);
               } catch (MatrixException me){
                   // Is it group?
                   try {
                       groupObj = new Group(assignment);
                       groupObj.open(context);
                       // Find all its parents
                       slParents = groupObj.getParents(context, true);
                       if (slParents != null) {
                           slParentRolesOrGroups.addAll(slParents);
                       }
                       groupObj.close(context);
                   }
                   catch (MatrixException me2){
                       // This is neither role nor group, must be person
                   }
               }
               //End : Bug 346478 code modification
           }
           //Remove the last ","
           //strPersonAssignments = strPersonAssignments.substring(0,(strPersonAssignments.length())-1);
           // Begin : Bug 346478 code modification
           slParentRolesOrGroups.addAll(personAssignments);
           strPersonAssignments = FrameworkUtil.join(slParentRolesOrGroups,",");
           // End : Bug 346478 code modification
           StringBuffer objWhere = new StringBuffer();
           objWhere.append(DomainObject.SELECT_OWNER + " matchlist " + "\"" + strPersonAssignments + "\" \",\"");
           // Bug 346478 : The "Notify Only" tasks whether assigned to role/group/person, are auto completed. If they are assigned to role/group then after completion
           // they should not be visible in the Tasks to be Accepted list.
           objWhere.append(" && current!=\"" + POLICY_INBOX_TASK_STATE_COMPLETE + "\"");
		   // Added for FP1638 code merging - start
			//Commented by MSIL Upgrade team deliberately
		   //objWhere.append(" && from[" + RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + ATTRIBUTE_ROUTE_STATUS + "] != \"Stopped\") ");

		   // Added for FP1638 code merging - end
            Pattern typePattern = new Pattern(TYPE_INBOX_TASK);
            // MSIL CHANGES START BY INTELIZIGN - 26-Aug-2014
            // comment the following type
            //typePattern.addPattern(TYPE_TASK);
            // add selects for Parent Task & Project column
            String objectRouteIdSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.id";
            String objectRouteTypeSelectStr="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.type";
            String objectRouteNameSelectStr ="from["+PropertyUtil.getSchemaProperty("relationship_RouteTask")+"].to.to[Object Route].from.name";
            selectTypeStmts.add(objectRouteIdSelectStr);
            selectTypeStmts.add(objectRouteTypeSelectStr);
            selectTypeStmts.add(objectRouteNameSelectStr);
            // MSIL CHANGES END BY INTELIZIGN - 26-Aug-2014
            //typePattern.addPattern(sTypeWorkflowTask);// For Bug 346478, we shall find the WF tasks later
            taskMapList = DomainObject.findObjects(context,
                                                 typePattern.getPattern(),
                                                 null,
                                                 objWhere.toString(),
                                                 selectTypeStmts);
            // Removing those 'Inbox Tasks' that satisfy the following criteria
            // 1) The connected Route has a Route Template that has 'Owning Organization' relationship &
            // 2) The context user is not a member of that Organization
// IR-043921V6R2011 - Changes START
            StringList slInboxTasks = new StringList( taskMapList.size() );
            for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                if( TYPE_INBOX_TASK.equals( (String) mTask.get( SELECT_TYPE ) ) ) {
                    slInboxTasks.addElement( (String) mTask.get( SELECT_ID ) );
                }
            }
            StringList busSelects = new StringList(2);
            busSelects.addElement( SELECT_ID );
            busSelects.addElement( SELECT_TYPE );
            DomainObject doPerson = PersonUtil.getPersonObject(context);
            MapList mlOrganizations = doPerson.getRelatedObjects( context, RELATIONSHIP_MEMBER, TYPE_ORGANIZATION,
                busSelects, new StringList( SELECT_RELATIONSHIP_ID ), true, false, (short) 1, "", "", 0 );
            StringList slMember = new StringList( mlOrganizations.size() );
            for( Iterator mlItr = mlOrganizations.iterator(); mlItr.hasNext(); ) {
                Map mOrg = (Map) mlItr.next();
                slMember.addElement( (String) mOrg.get( SELECT_ID ) );
            }
            busSelects.addElement( SELECT_TEMPLATE_OWNING_ORG_ID );
            MapList mlIboxTasksInfo = DomainObject.getInfo(context, (String[])slInboxTasks.toArray(new String[slInboxTasks.size()]), busSelects );
            StringList slToRemoveTask = new StringList( mlIboxTasksInfo.size() );
            for( Iterator mlItr = mlIboxTasksInfo.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                String sOrgId = (String) mTask.get( SELECT_TEMPLATE_OWNING_ORG_ID );
                if( sOrgId !=null && !"null".equals( sOrgId ) && !"".equals( sOrgId ) && !(slMember.contains( sOrgId ))) {
                    slToRemoveTask.addElement( (String) mTask.get( SELECT_ID ) );
                }
            }
            for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                if( slToRemoveTask.contains( (String) mTask.get( SELECT_ID ))) {
                    mlItr.remove();
                }
            }
// IR-043921V6R2011 - Changes END
            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
              Map objectMap = (Map) objectListItr.next();
              sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
              // if Task is WBS then add the context (top) object information
              if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
              {
                  sTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
                  sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                  bResult = mql.executeCommand(context, sMql);
                  if(bResult) {
                      sResult = mql.getResult().trim();
                      //Bug 318325. Added if condition to check sResult object as not null and not empty.
                      if(sResult!=null && !"".equals(sResult)) {
                          sResultTkz = new StringTokenizer(sResult,"|");
                          sResultTkz.nextToken();
                          sResultTkz.nextToken();
                          sResultTkz.nextToken();
                          objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                          objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                          sResultTkz.nextToken();
                          objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                      }
                  }
              }
              finalTaskMapList.add(objectMap);
            }
            //Begin : Bug 346478 code modification
            // The Workflow Task objects are not having any infomration in them to know if they are assigned to any role or group.
            // The owner for the tasks is the Workflow owner, the assignee is not set and state Started. Therefore, we shall find
            // all the Workflow Task objects in Started state and then find out the assignee for these tasks from their activities
            // These assignees will be either role or group, we shall check if the context user has these role/group.
            final String POLICY_WORKFLOW_TASK = PropertyUtil.getSchemaProperty(context, "policy_WorkflowTask");
            final String POLICY_WORKFLOW_TASK_STATE_STARTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_WORKFLOW_TASK, "state_Started");
            final String ATTRIBUTE_ACTIVITY = PropertyUtil.getSchemaProperty(context, "attribute_Activity");
            final String ATTRIBUTE_PROCESS = PropertyUtil.getSchemaProperty(context, "attribute_Process");
            final String SELECT_ATTRIBUTE_ACTIVITY = "attribute[" + ATTRIBUTE_ACTIVITY + "]";
            final String SELECT_WORKFLOW_PROCESS_NAME = "to["+sRelWorkflowTask+"].from.attribute[" + ATTRIBUTE_PROCESS + "]";
            selectTypeStmts.add(SELECT_ATTRIBUTE_ACTIVITY);
            selectTypeStmts.add(SELECT_WORKFLOW_PROCESS_NAME);
            typePattern = new Pattern(sTypeWorkflowTask);
            taskMapList = DomainObject.findObjects(context,
                                                    typePattern.getPattern(),
                                                    null,
                                                    "current==\"" + POLICY_WORKFLOW_TASK_STATE_STARTED + "\"",
                                                    selectTypeStmts);
            Map mapTaskInfo = null;
            String strProcessName = null;
            String strActivityName = null;
            String strResult = null;
            String strAssigneeName = null;
            StringList slActivityAssignees = new StringList();
            for (Iterator itrTasks = taskMapList.iterator(); itrTasks.hasNext();) {
                mapTaskInfo = (Map) itrTasks.next();
                strProcessName = (String)mapTaskInfo.get(SELECT_WORKFLOW_PROCESS_NAME);
                strActivityName = (String)mapTaskInfo.get(SELECT_ATTRIBUTE_ACTIVITY);
                // Get assignee for the activity
                strResult = MqlUtil.mqlCommand(context, "print process \"" + strProcessName + "\" select interactive[" + strActivityName + "].assignee dump \"|\"", true);
                slActivityAssignees = FrameworkUtil.split(strResult, "|");
                for (Iterator itrAssignees = slActivityAssignees.iterator(); itrAssignees.hasNext();) {
                    strAssigneeName = (String) itrAssignees.next();
                    if (slParentRolesOrGroups.contains(strAssigneeName)) {
                        finalTaskMapList.add(mapTaskInfo);
                        break;
                    }
                }
            }
            //End : Bug 346478 code modification
            return finalTaskMapList;
      }
      catch(Exception e)
      {
          throw new FrameworkException(e.getMessage());
      }
    }
   // Added by Ajit -- 02/05/2016 -- To Add Content Name column in the Tasks Page -- Start
    /**
     * showContentName - get the Content Name asssociated to Inbox Task
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showContentName(Context context, String[] args) throws Exception
    {
        try
        {
           Vector contentNameList = new Vector();
           HashMap programMap = (HashMap) JPO.unpackArgs(args);         
           MapList objectList = (MapList)programMap.get("objectList");            
           Map paramList = (Map)programMap.get("paramList"); 
           Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
           String appDirectory =(String)paramList.get("SuiteDirectory");
           // build select params
           StringList selListObj = new SelectList(6);
           selListObj.add(routeObject.SELECT_NAME);
           selListObj.add(routeObject.SELECT_ID);
           selListObj.add(routeObject.SELECT_TYPE);
           selListObj.add(routeObject.SELECT_DESCRIPTION);
           selListObj.add(routeObject.SELECT_POLICY);
           selListObj.add(routeObject.SELECT_CURRENT);
           //Added By Ravi
           selListObj.add("attribute["+WMSConstants_mxJPO.ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
           String strCustomTitle=DomainConstants.EMPTY_STRING;
           // build select params for Relationship
           StringList selListRel = new SelectList(3);
           selListRel.add(routeObject.SELECT_RELATIONSHIP_ID);
           selListRel.addElement(routeObject.SELECT_ROUTE_BASEPOLICY);
           selListRel.addElement(routeObject.SELECT_ROUTE_BASESTATE);
           MapList routableObjsList = new MapList();
           // Get a list of Document and its sub type to be used for checking against the attachment's type
           String subtypes = MqlUtil.mqlCommand(context, "print type \"" + DomainConstants.TYPE_DOCUMENT + "\" select derivative dump |");
           StringList documentTypesList = null;
           documentTypesList = FrameworkUtil.split(subtypes, "|");
           documentTypesList.add(0, DomainConstants.TYPE_DOCUMENT);
           StringBuffer output = new StringBuffer();
           StringList slUniqueContentName = new StringList();
            if(null !=objectList && !objectList.isEmpty()){
                for(int obj=0; obj < objectList.size();obj++){
                    output = new StringBuffer();
                    slUniqueContentName = new StringList();
                    Map objMap = (Map)objectList.get(obj);
                    if(null !=objMap && objMap.containsKey("from[Route Task].to.id")){
                        String strRouteId =(String)objMap.get("from[Route Task].to.id");                                             
                         routeObject.setId(strRouteId);
                         routableObjsList = routeObject.getConnectedObjects(context,
                                                       selListObj,
                                                       selListRel,
                                                       false);                       
                         if(null !=routableObjsList && !routableObjsList.isEmpty()){
                             output.append("<table border=\"0\"><tr><td>");
                             for(int rcObj=0; rcObj < routableObjsList.size();rcObj++){
                             Map routableObjsMap = (Map)routableObjsList.get(rcObj);
                             String contentName =(String)routableObjsMap.get(routeObject.SELECT_NAME);
                             if(null != slUniqueContentName && !slUniqueContentName.contains(contentName)){                                 
                                     String docType = (String)routableObjsMap.get(routeObject.SELECT_TYPE);   
                                     boolean docOrSubType = false;
                                     if(documentTypesList.contains(docType)){      
                                      docOrSubType = true;
                                     }
                                    String nextURL ="../common/emxTree.jsp?objectId=" + routableObjsMap.get(routeObject.SELECT_ID);                              
                                     //show the alternate menu only if the app is ProgramCentral and the type is Document or its subtype
                                     if(null!=appDirectory && appDirectory.equalsIgnoreCase(FrameworkProperties.getProperty(context,"eServiceSuiteProgramCentral.Directory"))  && docOrSubType)
                                     {
                                       nextURL += "&treeMenu=" + FrameworkProperties.getProperty(context,"eServiceSuiteProgramCentral.emxTreeAlternateMenuName.type_Document");
                                     }
									 String strContentName = (String)routableObjsMap.get(routeObject.SELECT_NAME);
									 strCustomTitle= (String)routableObjsMap.get("attribute["+WMSConstants_mxJPO.ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
									 if(strCustomTitle!=null && !strCustomTitle.isEmpty()) strContentName=strCustomTitle;
									 if(strContentName.contains("&")){
										 strContentName = strContentName.replaceAll("&","and");
									 }
                                     String href = (new StringBuilder().append("<a href=\"javascript:emxTableColumnLinkClick('").append(nextURL).append("',700,600,false,'popup','')\">").append(strContentName).append("</a>")).toString();
                                     //System.out.println("href >> "+href);
                                      slUniqueContentName.add(href);                                
                                 }
                             }
                            output.append(FrameworkUtil.join(slUniqueContentName, ","));
                            output.append("</td></tr></table>");                            
                            if(null !=output){
                                contentNameList.add(output.toString());
                            }else{
                                contentNameList.add("");
                            }
                         }else{
                            contentNameList.add("");
                         }
                    }else{
                        contentNameList.add("");
                    }
                }
            }
            return contentNameList;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showContentName  " + ex.getMessage());
            throw ex;
        }
    }
 // Added by Ajit -- 02/05/2016 -- To Add Content Name column in the Tasks Page -- End
}