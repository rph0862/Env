/* emxTask.java

   Copyright (c) 1992-2015 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxTask.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

/*
Change History:
Date       	   |	Change By 	 | Tag to be searched |		  Details
===========================================================================================================
12-Nov-2014    |   Intelizign	 |	12-Nov-2014       | PE Build 2.1 - Changes made to show delay status icon on parent task if any of the child task is delayed
27-May-2015    | Intelizign      | 27/05/2015         | CR - Only child tasks to be sent for approval.
7/23/2015      | Intelizign      |  INZ_0001          | CR - Array Index out of bound Issue .
26/03/2016     | Ajit            | 26/03/2016             | For Improvement of Slowness of Assigned Selected mutiple Task
15/04/2016     | Ajit            | 15/04/2016             | Export to Excel was not working for Status column of WBS Page
12-Feb-2016    | Intelizign      |  12-Feb-2016       | Changes for Phase 2
27/07/2016     | Ajit            |  27/07/2016        | To get Slipdays and Status Icon Column Values while creating New Table View -- Start
07/10/2016     | Vartika         |  FP1638            | Code merged with FP1638 OOTB code
09/09/2016     | Ajit            |  09/09/2016        | Refresh command was working for Slipdays and Status Icon Column
21-Oct-2016    | Dheeraj Garg    |                    | The system is showing 158 slip days for line side equipment whereas the max delay in the child task is 6 days.
25-Nov-2016    | Dheeraj Garg    |  25-Nov-2016       | ASR-89676: DIFFERENCE IN TASK STATE
30-Nov-2016    | Dheeraj Garg    |                    | Status icon is not refreshing after clicking on Refresh command
30-Nov-2016    | Dheeraj Garg    |                    | Change the date and click on Refresh command, slip days not refreshing for the root node.
12/12/2016     | Vartika         |  12/12/2016        | Code merged with FP1638 OOTB code - Revert this change
05-Dec-2016    | Dheeraj Garg    |        -           | SCR - Ringi Integration.
29-Jun-2018    | Vinit           |  29-Jun-2018       | Slip Days to be calculated based on Task start date and percent complete
*/

import com.matrixone.apps.common.AssignedTasksRelationship;
import com.matrixone.apps.common.DependencyRelationship;
import com.matrixone.apps.common.ICDocument;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.ProjectManagement;
import com.matrixone.apps.common.SubtaskRelationship;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIForm;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.DurationKeyword;
import com.matrixone.apps.program.DurationKeywords;
import com.matrixone.apps.program.DurationKeywordsUtil;
import com.matrixone.apps.program.Financials;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.Risk;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Dimension;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Relationship;
import matrix.db.Unit;
import matrix.db.UnitItr;
import matrix.db.UnitList;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;
import matrix.util.StringResource;

/**
 * The <code>emxTask</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxTask_mxJPO extends emxTaskBase_mxJPO
{
	
	private static final String SELECT_IS_DELETED_SUBTASK = "to[" + DomainConstants.RELATIONSHIP_DELETED_SUBTASK + "]";
	private static final String SELECT_ATTRIBUTE_COMMENTS = "attribute[" + ATTRIBUTE_COMMENTS + "]";
	private static final String SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS = SELECT_IS_DELETED_SUBTASK + "." + SELECT_ATTRIBUTE_COMMENTS;
	private static final String ATTRIBUTE_CRITICAL_TASK = PropertyUtil.getSchemaProperty("attribute_CriticalTask");
	private static final String SELECT_ATTRIBUTE_CRITICAL_TASK = "attribute[" + ATTRIBUTE_CRITICAL_TASK + "]";	
	// Added for FP1638 code merging - start
	private static final String SELECT_KINDOF_GATE = "type.kindof[" + ProgramCentralConstants.TYPE_GATE+ "]";
	private static final String SELECT_KINDOF_MILESTONE = "type.kindof[" + ProgramCentralConstants.TYPE_MILESTONE+ "]";
	// Added for FP1638 code merging - end
	
	
	private static final String SELECT_TASK_DEPENDENCY_REL_ID = "from[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].id";
	private static final String SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
	private static final String SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER;
	private static final String SELECT_SUBTASK_ATTRIBUTE_TASK_WBS = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
	private static final String SELECT_PREDECESSOR_TASK_ATTRIBUTE_TASK_WBS = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_SUBTASK_ATTRIBUTE_TASK_WBS;
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_NAME = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_TYPE = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_ID = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String ATTRIBUTE_LAG_TIME = PropertyUtil.getSchemaProperty("attribute_LagTime");
    private static final String SELECT_PREDECESSOR_LAG_TIME_INPUT = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputvalue";
	private static final String SELECT_PREDECESSOR_LAG_TIME_UNITS = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputunit";
	private static final String SELECT_TASK_PROJECT_NAME = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
	private static final String SELECT_TASK_PROJECT_TYPE = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_TASK_PROJECT_ID = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String SELECT_PARENT_TASK_IDS = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].from.id";
	private static final String TOTAL_EFFORT = "Total_Effort";
	private static final String SELECT_TASK_ASSIGNEES ="to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from."+ProgramCentralConstants.SELECT_NAME;
	private static final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";
	private static final String SELECT_IS_TASK_MANAGEMENT=  "type.kindof["+ProgramCentralConstants.TYPE_TASK_MANAGEMENT+"]";
	private static final String SELECT_KINDOF_PROJECT_SPACE=  "type.kindof["+ProgramCentralConstants.TYPE_PROJECT_SPACE+"]";
	private static final String SELECT_KINDOF_PROJECT_CONCEPT=  "type.kindof["+ProgramCentralConstants.TYPE_PROJECT_CONCEPT+"]";
	
    /** Id of the Access List Object for this Project. */
    protected DomainObject _accessListObject = null;

    /** The project access list id relative to project. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID ="to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

    /** The project access key id relative to task predecessor. */
    static protected final String SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_PROJECT_ACCESS_KEY_ID;

    /** state "Create" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_CREATE =PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Create");

    /** state "Assign" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ASSIGN =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Assign");

    /** state "Active" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ACTIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Active");

    /** state "Review" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_REVIEW =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Review");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_COMPLETE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Complete");

    /** state "Archive" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ARCHIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_SPACE,
                                           "state_Archive");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_BUSINESS_GOAL_CREATED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_BUSINESS_GOAL,
                                           "state_Created");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_BUSINESS_GOAL_ACTIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_BUSINESS_GOAL,
                                           "state_Active");
    /** The parent type of the task. */
    public static final String SELECT_SUBTASK_TYPE =
            "to[" + RELATIONSHIP_SUBTASK + "].from.type";

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_TASK_COMPLETE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Complete");
	/** attribute "Percent Allocation". */
	public static final String ATTRIBUTE_PERCENT_ALLOCATION =
		PropertyUtil.getSchemaProperty("attribute_PercentAllocation");


    /** used in triggerPromoteAction and triggerDemoteAction functions. */
    boolean _doNotRecurse = false;
	
	private static final String SELECT_IS_PARENT_TASK_DELETED = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].from.to[" + DomainConstants.RELATIONSHIP_DELETED_SUBTASK + "]";
	private static final String SELECT_TASK_PROJECT_CURRENT = "to["+RELATIONSHIP_SUBTASK+"].from.current";
	private static final String SELECT_TASK_ASSIGNEE_ID = "to[" + DomainObject.RELATIONSHIP_ASSIGNED_TASKS + "].from.id";
	private static final String SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_EXPERIMENT+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_PROJECT_BASELINE+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+DomainObject.TYPE_PROJECT_TEMPLATE+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_CONCEPT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+DomainObject.TYPE_PROJECT_CONCEPT+"]";
	private static final String FROM_PROJECT_TEMPLATE_WBS ="FromProjectTemplateWBS";
	private static final String FROM_PROJECT_WBS ="FromProjectWBS";
	private static final String SELECT_ATTRIBUTE_SCHEDULE_FROM = "attribute[Schedule From]";
	private static final String RELATIONSHIP_CONTRIBUTES_TO = PropertyUtil.getSchemaProperty("relationship_ContributesTo");
	private static final String SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID = "from[" + RELATIONSHIP_CONTRIBUTES_TO + "].id";
	private static final String RELATIONSHIP_SHADOW_GATE = PropertyUtil.getSchemaProperty("relationship_ShadowGate");
	private static final String SELECT_SHADOW_GATE_ID = "from["+RELATIONSHIP_SHADOW_GATE+"].id";

	private static final String SELECT_SUCCESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER = "to[" + RELATIONSHIP_DEPENDENCY + "].from." + SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER;
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_NAME = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_TYPE = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_ID = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String SELECT_SUCCESSOR_LAG_TIME_INPUT = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputvalue";
	private static final String SELECT_SUCCESSOR_LAG_TIME_UNITS = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputunit";
	private static final String SELECT_SUCCESSOR_IDS = "to[" + RELATIONSHIP_DEPENDENCY + "].from.id";
	public static final String SELECT_SUCCESSOR_TYPES = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" +DependencyRelationship.ATTRIBUTE_DEPENDENCY_TYPE + "]";


	// Create an instant of emxUtil JPO
	protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;


	static protected final String SELECT_PROJECT_ACCESS_KEY_ID_FOR_SUCCESSOR = "to[" + RELATIONSHIP_DEPENDENCY + "].from." + SELECT_PROJECT_ACCESS_KEY_ID;
	public static final String SELECT_FROM_SUBTASK = "from["+RELATIONSHIP_SUBTASK+"]";



	/** used in triggerPromoteAction and triggerDemoteAction functions. */
	boolean isGateOrMilestone = false;
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
    
    /**
     * This method is used to show the status image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Vector containing all the status image value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     * 
     * Method moved from Base JPO to non-base JPO to include customization for MSIL
     */
    /*public Vector getStatusIcon(Context context, String[] args) throws Exception
    {
        Vector showIcon = new Vector();

        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
        String policyName = task.getDefaultPolicy(context);
        String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy", policyName, "state_Complete");
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            //End:08-June-2010:ak4:R210:PRG:Bug:055631
            MapList objectList = (MapList) programMap.get("objectList");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
            Map objectMap = null;
            int i = 0;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            boolean flag = false;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
                i++;
            }

            StringList busSelect = new StringList(7);
            busSelect.add(DomainConstants.SELECT_ID);
            busSelect.add(DomainConstants.SELECT_TYPE);
            busSelect.add(task.SELECT_BASELINE_CURRENT_END_DATE);
            busSelect.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            busSelect.add(task.SELECT_BASELINE_CURRENT_END_DATE);
            busSelect.add(task.SELECT_CURRENT);
            busSelect.add(task.SELECT_PERCENT_COMPLETE);
    		busSelect.add(SELECT_IS_DELETED_SUBTASK);
    		busSelect.add(SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS);
    		//Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
    		busSelect.add(SELECT_TASK_ACTUAL_FINISH_DATE);
            //End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
    		
    		// MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
    		busSelect.addElement(task.SELECT_HAS_SUBTASK);
    		// MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014

    		MapList actionList = DomainObject.getInfo(context, objIdArr, busSelect);

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
            Date tempDate = new Date();
            Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());

    		StringList taskSubtypesList = Task.getAllTaskTypeNames(context);
    		String strI18nBehindSchedule = i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage());
    		String strI18nOnTime = i18nNow.getI18nString("emxProgramCentral.Common.OnTime", "emxProgramCentralStringResource", context.getSession().getLanguage());
    		String strI18nLate = i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage());

                String statusGif = "";
                String statusToolTip = "";
                boolean blDeletedTask = false;
                String strComments = null;
                String strObjectId =  "";
                String strObjectType = "";
                 String strHasSubtasks = "";
                String strProjetDelay = "";

                 Date baselineCurrentEndDate = null;
                String baselineCurrentEndDateString = "";
                Date estFinishDate = null;
                //Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
                Date actualFinishDate = null;
                String actualFinishDateString	=	"";

            for (i = 0; i < actionListSize && actionList != null; i++)
            {
                 statusGif = "";
                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                 statusToolTip = "";
                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                objectMap = (Map) actionList.get(i);
                // Start Deleted Task tooltip should change with comments
                 blDeletedTask = false;
                 strComments = null;
                 strObjectId = (String) objectMap.get(DomainConstants.SELECT_ID);
                 strObjectType = (String) objectMap.get(DomainConstants.SELECT_TYPE);

                if(strObjectType.equalsIgnoreCase(DomainConstants.TYPE_PERSON)){
                    statusGif = ProgramCentralConstants.EMPTY_STRING;
                    showIcon.add(statusGif);
                    continue;
                }
                if(taskSubtypesList.indexOf(strObjectType)>=0){
    				if("TRUE".equalsIgnoreCase((String) objectMap.get(SELECT_IS_DELETED_SUBTASK))){
                        blDeletedTask = true;
    					strComments = (String) objectMap.get(SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS);
                    }
                }
                //End Deleted Task tooltip should change with comments
                
                // MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
                 strHasSubtasks = (String)objectMap.get(task.SELECT_HAS_SUBTASK);
                
                 strProjetDelay = "";
                if(null != strHasSubtasks && "True".equalsIgnoreCase(strHasSubtasks))
                {
                	com.matrixone.apps.program.Task parentTask = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
                	// Define selectables for each Task object.
                	StringList taskSelects = new StringList(7);
                	taskSelects.add(DomainConstants.SELECT_ID);
                	taskSelects.add(DomainConstants.SELECT_TYPE);
                	taskSelects.add(task.SELECT_BASELINE_CURRENT_END_DATE);
                	taskSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                	taskSelects.add(task.SELECT_CURRENT);
                	taskSelects.add(task.SELECT_PERCENT_COMPLETE);

                	parentTask.setId(strObjectId);

                	MapList childTaskList = getTasks(context,parentTask,0,taskSelects,null);

                	${CLASS:emxProjectSpace} jpoObj = new ${CLASS:emxProjectSpace}(strObjectId);
                	StringList slTaskStatusList = jpoObj.getTaskStatusIcon(context, childTaskList);
                	if(slTaskStatusList.contains("Red"))
                		strProjetDelay = "Red";
                	else if(slTaskStatusList.contains("Yellow"))
                		strProjetDelay = "Yellow";
                }
                // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                 baselineCurrentEndDate = null;
                 baselineCurrentEndDateString = (String) objectMap.get(task.SELECT_BASELINE_CURRENT_END_DATE);
                 estFinishDate = sdf.parse((String) objectMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
                //Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
                 actualFinishDate = null;
                 actualFinishDateString	=	(String) objectMap.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
                if (actualFinishDateString != null && !actualFinishDateString.equals("")) {
                	actualFinishDate	=	sdf.parse(actualFinishDateString);
                }
                //End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
                if (!"".equals(baselineCurrentEndDateString))
                {
                    baselineCurrentEndDate = sdf.parse((String) objectMap.get(task.SELECT_BASELINE_CURRENT_END_DATE));
                }
                long daysRemaining;
                if (null == baselineCurrentEndDate)
                {
                    daysRemaining = (long) task.computeDuration(sysDate, estFinishDate);
                }
                else
                {
                    daysRemaining = (long) task.computeDuration(sysDate, baselineCurrentEndDate);
                }

                if (null == baselineCurrentEndDate)
                {
                    if (objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) || ((String) objectMap.get(task.SELECT_PERCENT_COMPLETE)).equals("100"))
                    {
                        statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                        	//Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
                            if(actualFinishDate != null && actualFinishDate.compareTo(estFinishDate)<=0) {
                            statusGif += strI18nOnTime + "\" title=\""+strI18nOnTime;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nOnTime;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }else{
                            		statusGif += strI18nOnTime + "\" title=\""+strI18nLate;
                            		statusToolTip = strI18nLate;
                            }
                            //End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
                        }
                        statusGif += "\"/>";
                    }
                    else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && sysDate.after(estFinishDate))
                    {
                        statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) +"\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                            statusGif += strI18nLate + "\" title=\""+strI18nLate;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nLate;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }
                        statusGif += "\"/>";
                    }
                    else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && daysRemaining <= yellowRedThreshold)
                    {
                        statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                            statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nBehindSchedule;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }
                        statusGif += "\"/>";
                    }
                    else
                    {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                    
                    // MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
                    if(null != strProjetDelay && strProjetDelay.length() > 0)
                    {
                    	if("Red".equalsIgnoreCase(strProjetDelay))
                    	{
                            statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
                            if(blDeletedTask){
                            	//Added for special character.
                                statusGif += XSSUtil.encodeForHTML(context,strComments) +"\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strComments;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }else {
                                statusGif += strI18nLate + "\" title=\""+strI18nLate;
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strI18nLate;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }
                            statusGif += "\"/>";
                        }
                    	else if("Yellow".equalsIgnoreCase(strProjetDelay))
                    	{
                            statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                            if(blDeletedTask){
                            	//Added for special character.
                                statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strComments;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }else {
                                statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strI18nBehindSchedule;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }
                            statusGif += "\"/>";
                        }
                    }
                    // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                }
                else
                {
                    if (objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) || ((String) objectMap.get(task.SELECT_PERCENT_COMPLETE)).equals("100"))
                    {
                        statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                            statusGif += strI18nOnTime + "\" title=\""+strI18nOnTime;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nOnTime;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }
                        statusGif += "\"/>";
                    }
                    else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && sysDate.after(baselineCurrentEndDate))
                    {
                        statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                            statusGif += strI18nLate + "\" title=\""+strI18nLate;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nLate;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }
                        statusGif += "\"/>";
                    }
                    else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && (daysRemaining <= yellowRedThreshold))
                    {
                        statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                        if(blDeletedTask){
                        	//Added for special character.
                            statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strComments;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }else {
                            statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
                            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                            statusToolTip = strI18nBehindSchedule;
                            //End:08-June-2010:ak4:R210:PRG:Bug:055631
                        }
                        statusGif += "\"/>";
                    }
                    else
                    {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                    
                 // MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
                    if(null != strProjetDelay && strProjetDelay.length() > 0)
                    {
                    	if("Red".equalsIgnoreCase(strProjetDelay))
                    	{
                            statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
                            if(blDeletedTask){
                            	//Added for special character.
                                statusGif += XSSUtil.encodeForHTML(context,strComments) +"\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strComments;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }else {
                                statusGif += strI18nLate + "\" title=\""+strI18nLate;
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strI18nLate;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }
                            statusGif += "\"/>";
                        }
                    	else if("Yellow".equalsIgnoreCase(strProjetDelay))
                    	{
                            statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                            if(blDeletedTask){
                            	//Added for special character.
                                statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strComments;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }else {
                                statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
                                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                                statusToolTip = strI18nBehindSchedule;
                                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                            }
                            statusGif += "\"/>";
                        }
                    }
                    // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                }
                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
                	showIcon.add(statusToolTip);// statusToolTip will get exported
                 }else{
                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                	 showIcon.add("<label>"+statusGif+"</label>");// this default string displayed in Browser
                 }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showIcon;
        }
    }*/
	
	/**
     * When the task is promoted this function is called.
     * Depending on which state the promote is triggered from
     * it performs the necessary actions based on the arg-STATENAME value
     *
     * Note: object id must be passed as first argument.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing the from state
     *        2 - String containing the to state
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     */
    public void triggerPromoteAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering triggerPromoteAction");

        // get values from args.
        String objectId  = args[0];
        String fromState = args[1];
        String toState   = args[2];
        String checkAssignees = args[3];

        setId(objectId);

        String strParentType = getInfo(context, SELECT_SUBTASK_TYPE);

        if (strParentType != null && strParentType.equals(TYPE_PART_QUALITY_PLAN))
        {
            _doNotRecurse = false;
        }

		if (fromState.equals(STATE_PROJECT_TASK_CREATE) && (_doNotRecurse && !isGateOrMilestone))
		{
			return;
		}

        java.util.ArrayList taskToPromoteList = new java.util.ArrayList();
        StringList busSelects = new StringList(4);
        busSelects.add(SELECT_ID);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_TYPE);
      //Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
        busSelects.add(SELECT_POLICY);
      //End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
	  // Added for FP1638 code merging - start
		busSelects.add(ProgramCentralConstants.SELECT_KINDOF_GATE);
		busSelects.add(ProgramCentralConstants.SELECT_KINDOF_MILESTONE);
		// Added for FP1638 code merging - end

        if (fromState.equals(STATE_PROJECT_TASK_CREATE))
        {
            //The first time this function is called this value will be false
            //second time around this will be true
            //The reason for doing this is since getTasks function gets all the
            //sub-tasks in one call all the sub-tasks are promoted in one pass
            //thereon if the sub-tasks call the function it returns without
            //doing anything
            //if (_doNotRecurse)
            //{
                //function called recursively return without doing anything
            //    return;
            //}

			busSelects.add(SELECT_HAS_ASSIGNED_TASKS);
                        busSelects.add("relationship["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to");

			// get all the subtasks
			MapList utsList = getTasks(context, this, 0, busSelects, null);
			if (utsList.size() > 0)
			{
				_doNotRecurse = true;
				Iterator itr = utsList.iterator();
				while (itr.hasNext())
				{
					boolean promoteTask = false;
					Map map = (Map) itr.next();
					String state = (String) map.get(SELECT_CURRENT);
                                        Object routes =  map.get("relationship["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to");
					StringList taskStateList =
							(StringList) map.get(SELECT_STATES);

                    //get the position of the task's current state wrt
                    //to its state list
                    int taskCurrentPosition = taskStateList.indexOf(state);

					//get the position to which the task need to be promoted
					//if the toState does not exist then taskPromoteToPosition
					//will be -1
					//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
					String type = (String) map.get(SELECT_TYPE);
					String policy = (String) map.get(SELECT_POLICY);

					boolean isGateType      =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
					boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));

					if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)){

    						if( !_doNotRecurse && (STATE_PROJECT_TASK_ASSIGN.equals(toState) || STATE_PROJECT_TASK_ACTIVE.equals(toState))){
							// Added for FP1638 code merging - end
                    			String [] arg1 = new String[4];
                    			arg1[0] = objectId;
                    			arg1[1] = STATE_PROJECT_TASK_ASSIGN;
                    			arg1[2] = STATE_PROJECT_TASK_ACTIVE;
                    			arg1[3] = "false";
                    			triggerPromoteAction(context,arg1);
                    			triggerSetPercentageCompletion(context,arg1);

                    			arg1[0] = objectId;
                    			arg1[1] = STATE_PROJECT_TASK_ACTIVE;
                    			arg1[2] = STATE_PROJECT_TASK_REVIEW;
                    			arg1[3] = "false";
                    			triggerPromoteAction(context,arg1);
                    		}
                    	}
                    //End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
                    int taskPromoteToPosition = taskStateList.indexOf(toState);
                    //check if the toState exists and if the current
                    //position of the task is less than the toState
                    if(taskPromoteToPosition != -1 &&
                       taskCurrentPosition < taskPromoteToPosition)
                    {
                        if ("true".equalsIgnoreCase(checkAssignees))
                        {
                            //is this task assigned to anyone?
                            //if true promote otherwise do not promote
                            if ("true".equalsIgnoreCase(
                                (String) map.get(SELECT_HAS_ASSIGNED_TASKS)))
                            {
                                promoteTask = true;
                            }
                        }
                        else
                        {
                            //task can be promoted even if the task is not
                            //assigned to anyone
                            promoteTask = true;
                        }
                    }
                    if (promoteTask)
                    {
                        String taskId = (String) map.get(SELECT_ID);
			if (routes != null) {
				DomainObject taskObject = DomainObject.newInstance(
									context, taskId);
				StringList relSelects = new StringList("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE+ "]");
				String relWhere = "attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE+ "] == \"state_Create\"";
				MapList routeList = taskObject.getRelatedObjects(
						context,
						DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
						DomainConstants.TYPE_ROUTE, null,
						relSelects, false, true, (short) 0, "",
						relWhere, (short) 0);
				if (routeList != null && routeList.size() > 0) {
					promoteTask = false;
				}
			}
			if(promoteTask)
						{
                        taskToPromoteList.add(taskId);
                    }
                }
            }
			}
            else{
                // Added for FP1638 code merging - start
				/*//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
            	String type = (String) getInfo(context,SELECT_TYPE);
            	String policy = (String) getInfo(context,SELECT_POLICY);
            	if(null != type && (ProgramCentralConstants.TYPE_GATE.equals(type) || ProgramCentralConstants.TYPE_MILESTONE.equals(type)) &&
            			ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy)){*/
				Map mInfo = getInfo(context, busSelects);
				String type = (String) mInfo.get(SELECT_TYPE);
				String policy = (String) mInfo.get(SELECT_POLICY);
				boolean isGateType      =   "TRUE".equalsIgnoreCase((String)mInfo.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
				boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)mInfo.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));
				if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)) {
				// Added for FP1638 code merging - end
        			  //toState = STATE_PROJECT_TASK_REVIEW;
            			String [] arg1 = new String[4];
            			arg1[0] = objectId;
            			arg1[1] = STATE_PROJECT_TASK_ASSIGN;
            			arg1[2] = STATE_PROJECT_TASK_ACTIVE;
            			arg1[3] = "false";
            			triggerPromoteAction(context,arg1);

            			//String [] arg2 = new String[4];
            			arg1[0] = objectId;
            			arg1[1] = STATE_PROJECT_TASK_ACTIVE;
            			arg1[2] = STATE_PROJECT_TASK_REVIEW;
            			arg1[3] = "false";
            			triggerPromoteAction(context,arg1);

					triggerSetPercentageCompletion(context,arg1);
					isGateOrMilestone = true;
				}
				//End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
			}
		}
	        else if (fromState.equals(STATE_PROJECT_TASK_ASSIGN))
		{
			//******************start Business Goal promote to Active state*********
			//when the project is promoted from the assign to active state
			//promote the business goal if it is the first business goal
			//use super user to overcome access issue
			ContextUtil.pushContext(context);
			try
			{
				com.matrixone.apps.program.BusinessGoal businessGoal =
						(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context,
								DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
				com.matrixone.apps.program.ProjectSpace project =
						(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
								DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
				project.setId(objectId);
				MapList businessGoalList = new MapList();
				businessGoalList = businessGoal.getBusinessGoals(context, project, busSelects, null);
				if (null != businessGoalList && businessGoalList.size()>0)
				{
					Iterator businessGoalItr = businessGoalList.iterator();
					while(businessGoalItr.hasNext())
					{
						Map businessGoalMap = (Map) businessGoalItr.next();
						String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
						String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
						businessGoal.setId(businessGoalId);
						if(fromState.equals(STATE_PROJECT_TASK_ASSIGN) && businessGoalState.equals(STATE_BUSINESS_GOAL_CREATED))
						{
							businessGoal.changeTheState(context);
						} //ends if
					}//ends while
				}//ends if
			}//ends try
			catch (Exception e)
			{
				DebugUtil.debug("Exception Task triggerPromoteAction- ",
						e.getMessage());
				throw e;
			}
			finally
			{
				ContextUtil.popContext(context);
			}
			//******************end Business Goal promote to Active state*********

            //when the task is promoted from Assign to Active
            //promote the parent to Active

            //get the parent task
            MapList parentList = getParentInfo(context, 1, busSelects);
            if (parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                StringList taskStateList = (StringList) map.get(SELECT_STATES);

                //get the position of the task's current state wrt to
                //its state list
                int taskCurrentPosition = taskStateList.indexOf(state);

                //get the position to which the task need to be promoted
                //if the toState does not exist then taskPromoteToPosition
                //will be -1
                //Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
            	String type = (String) map.get(SELECT_TYPE);
            	String policy = (String) map.get(SELECT_POLICY);
            	// Added for FP1638 code merging - start
				/*if(null != type && (ProgramCentralConstants.TYPE_GATE.equals(type) || ProgramCentralConstants.TYPE_MILESTONE.equals(type)) &&
            			ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy)){*/
				
				boolean isGateType      =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
				boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));

				if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)){
				// Added for FP1638 code merging - end
            		if(STATE_PROJECT_TASK_ACTIVE.equals(toState)){
            			toState = STATE_PROJECT_TASK_REVIEW;
            			_doNotRecurse = true;
            		}
            	}
               //End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight

                int taskPromoteToPosition = taskStateList.indexOf(toState);

                //check if the toState exists and if the current
                //position of the task is less than the toState
                if (taskPromoteToPosition != -1 &&
                    taskCurrentPosition < taskPromoteToPosition)
                {
                    String taskId = (String) map.get(SELECT_ID);
                    Task task = new Task(taskId);
                    //use super user to overcome access issue
                    ContextUtil.pushContext(context);
                    try
                    {
                        //setId(taskId);
                        //setState(context, toState);
                        task.setState(context, toState);
                    }
                    finally
                    {
                        ContextUtil.popContext(context);
                    }
                }
                //Added:nr2:PRG:R210:For Project Hold Cancel Highlight
                //Coming in this condition since Project May be in Hold
                //and Task is promoted.
                else if(taskPromoteToPosition == -1){
                	String id = (String) map.get(SELECT_ID); //Should be Project Space id
                	DomainObject dObj = DomainObject.newInstance(context,id);

                	if(dObj.isKindOf(context, TYPE_PROJECT_SPACE)){
	                	ProjectSpace ps = new ProjectSpace();
	                	StringList projectSpaceStates = ps.getStates(context, POLICY_PROJECT_SPACE);

	                	int tasktoBePromotedToPosition = projectSpaceStates.indexOf(toState);
	                	//Check the Value stored in Previous Project State Attribute.
	                	//To store the new state only if greater than the one stored.
	                	Task task = new Task(id);
	                	HashMap programMap = new HashMap();
	        			programMap.put(SELECT_ID, id);
	        			String[] arrJPOArguments = JPO.packArgs(programMap);
	                	String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);

	                	int previousStatePos = projectSpaceStates.indexOf(previousState);

	                	if(previousStatePos == -1){
	                		previousStatePos = 6;
	                	}

	                    if (tasktoBePromotedToPosition != -1 && tasktoBePromotedToPosition > previousStatePos)
	                    {
	                    	//String taskId = (String) map.get(SELECT_ID);

	                    	//use super user to overcome access issue
	                    	ContextUtil.pushContext(context);
	                    	try
	                    	{
	                    		programMap.put(SELECT_CURRENT, toState);
	                    		String[] arrJPOArgs = JPO.packArgs(programMap);
	                    		JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
	                    	}
	                    	finally
	                    	{
	                    		ContextUtil.popContext(context);
	                    	}
	                    }
                	}
                } //End of Else if
                //End:nr2:PRG:R210:For Project Space Hold Cancel Highlight
            }
        }
        else if(fromState.equals(STATE_PROJECT_TASK_ACTIVE))
        {
            //do nothing for now
        }
        else if(fromState.equals(STATE_PROJECT_TASK_REVIEW))
        {
        //******************start Business Goal promote****to Complete state****
             //when the project is promoted from the review to complete state
            //promote the business goal if it is the first business goal
            //use super user to overcome access issue
            ContextUtil.pushContext(context);
            try
            {
                com.matrixone.apps.program.BusinessGoal businessGoal =
                  (com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context,
                  DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
                com.matrixone.apps.program.ProjectSpace project =
                  (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                  DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
                project.setId(objectId);
                MapList businessGoalList = new MapList();
                businessGoalList = businessGoal.getBusinessGoals(context, project, busSelects, null);
                if (null != businessGoalList && businessGoalList.size()>0)
                {
                    Iterator businessGoalItr = businessGoalList.iterator();
                    while(businessGoalItr.hasNext())
                    {
                        Map businessGoalMap = (Map) businessGoalItr.next();
                        String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
                        String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
                        businessGoal.setId(businessGoalId);
                        if(fromState.equals(STATE_PROJECT_TASK_REVIEW) && businessGoalState.equals(STATE_BUSINESS_GOAL_ACTIVE))
                        {
                            MapList projectList = businessGoal.getProjects(context, busSelects, null);
                            boolean changeState = true;
                            if (null != projectList && projectList.size()>0)
                            {
                                Iterator projectItr = projectList.iterator();
                                while(projectItr.hasNext())
                                {
                                    Map projectMap = (Map) projectItr.next();
                                    String projectId = (String) projectMap.get(project.SELECT_ID);
                                    String projectState = (String) projectMap.get(project.SELECT_CURRENT);
                                    if(!projectState.equals(STATE_PROJECT_TASK_COMPLETE) && !projectState.equals(STATE_PROJECT_TASK_ARCHIVE)  && !projectId.equals(objectId))
                                    {
                                        changeState = false;
                                    }
                                }
                                if(changeState)
                                {
                                    businessGoal.changeTheState(context);
                                }//ends if
                            }//ends if
                        }//ends if
                    }//ends while
                }//ends if
            }//ends try
            catch (Exception e)
            {
                DebugUtil.debug("Exception Task triggerPromoteAction- ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
          //******************end Business Goal promote to Complete state*******

            MapList parentList = getParentInfo(context, 1, busSelects);
            if (parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                String parentType = (String) map.get(SELECT_TYPE);
                StringList parentStateList = (StringList)map.get(SELECT_STATES);

				   //Modified - 27/05/2015 for - CR - Only child tasks to be sent for approval - Start 
				   //if (state.equals(STATE_PROJECT_TASK_ACTIVE))
					if (state.equals(STATE_PROJECT_TASK_ACTIVE) || state.equals(STATE_PROJECT_TASK_REVIEW)) //Modified - 27/05/2015 for - CR - Only child tasks to be sent for approval - End
					{
                    String parentId = (String) map.get(SELECT_ID);
                    //set up the args as required for the check trigger
                    //check whether all the children for this parent is in the
                    //specified state
                    //String sArgs[] = {parentId, "", "state_Complete"};

                    setId(parentId);
                    boolean checkPassed = checkChildrenStates(context,
                                STATE_PROJECT_TASK_COMPLETE, null);

                    //int status = triggerCheckChildrenStates(context, sArgs);

                    //all children in complete state
                    if (checkPassed)
                    {
                        //get the position of the task's current state wrt to
                        //its state list
                        int taskCurrentPosition =parentStateList.indexOf(state);

                        //get the position of the Review state wrt to its
                        //state list
                        int taskPromoteToPosition =
                            parentStateList.indexOf(STATE_PROJECT_TASK_REVIEW);
                        if (parentType != null && parentType.equals(TYPE_PART_QUALITY_PLAN))
                        {
                            taskPromoteToPosition =
                              parentStateList.indexOf(STATE_PART_QUALITY_PLAN_COMPLETE);
                        }
			
						//Modified - 27/05/2015 for - CR - Only child tasks to be sent for approval - Start 
						if (parentType != null && parentType.equals("Task"))
                        {
                            // Added for FP1638 issue fixing - start
// Added for FP1638 issue fixing - revert the change - 12/12/2016 - start
                            taskPromoteToPosition = parentStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);
							//taskPromoteToPosition = parentStateList.indexOf(STATE_PROJECT_TASK_REVIEW);
// Added for FP1638 issue fixing - revert the change - 12/12/2016 - end
							// Added for FP1638 issue fixing - end
                        }
						//Modified - 27/05/2015 for - CR - Only child tasks to be sent for approval - End 

                        //Review state exists and is the state next to Active.
                        //Promote the parent
                        if (taskPromoteToPosition != -1 &&
                            taskPromoteToPosition == (taskCurrentPosition + 1))
                        {
                            //use super user to overcome access issue
                            ContextUtil.pushContext(context);
                            try
                            {
                                setId(parentId);
                                promote(context);
                            }
                            finally
                            {
                                ContextUtil.popContext(context);
                            }
                        }
                        //Added:nr2:PRG:R210:For Project Hold Cancel Highlight
                        //Coming in this condition since Project May be in Hold
                        //and Task is promoted.
                        else if(taskPromoteToPosition == -1){
                        	String id = (String) map.get(SELECT_ID); //Should be Project Space id
                        	DomainObject dObj = DomainObject.newInstance(context,id);

                        	if(dObj.isKindOf(context, TYPE_PROJECT_SPACE)){
        	                	ProjectSpace ps = new ProjectSpace();
        	                	StringList projectSpaceStates = ps.getStates(context, POLICY_PROJECT_SPACE);

        	                	int tasktoBePromotedToPosition = projectSpaceStates.indexOf(toState); //toState is Completed hence 4
        	                	//Check the Value stored in Previous Project State Attribute.
        	                	//To store the new state only if greater than the one stored.
        	                	Task task = new Task(id);
        	                	HashMap programMap = new HashMap();
        	            		programMap.put(SELECT_ID, id);
        	            		String[] arrJPOArguments = JPO.packArgs(programMap);
        	                	String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);

        	                	int previousStatePos = projectSpaceStates.indexOf(previousState); //This will be 2 (Active) most Probably

        	                	if(previousStatePos == -1){
        	                		previousStatePos = 6;
        	                	}

        	                    if (tasktoBePromotedToPosition != -1 && tasktoBePromotedToPosition > previousStatePos)
        	                    {
        	                    	//String taskId = (String) map.get(SELECT_ID);

        	                    	//use super user to overcome access issue
        	                    	ContextUtil.pushContext(context);
        	                    	try
        	                    	{
        	                    		programMap.put(SELECT_CURRENT, STATE_PROJECT_TASK_REVIEW);
        	                    		String[] arrJPOArgs = JPO.packArgs(programMap);
        	                    		JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
        	                    	}
        	                    	finally
        	                    	{
        	                    		ContextUtil.popContext(context);
        	                    	}
        	                    }
                        	}
                        } //End of Else if
                        //End:nr2:PRG:R210:For Project Space Hold Cancel Highlight
                    }
                }
            }
        }
        if(! taskToPromoteList.isEmpty())
        {
            //promote each of the tasks in the list
            //use super user to overcome access issue
            ContextUtil.pushContext(context);
            try
            {
                for(int i=0; i<taskToPromoteList.size(); i++)
                {
                    String id = (String)taskToPromoteList.get(i);
                    setId(id);
                    promote(context);
                }
            }
            catch (Exception e)
            {
                DebugUtil.debug("Exception Task triggerPromoteAction- ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
        }

        DebugUtil.debug("Exiting Task triggerPromoteAction");
    }
	// Add for CR - 02/07/2015  - Slip days should be available as a column on WBS - Start
	/*public Vector getSlipdaysForWBS(Context context, String[] args)
      throws Exception
    {
	System.out.println("##################### Start getSlipdaysForWBS "+new Date());
        Vector showSlipDays = new Vector();
        try
        {
            com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sSlipDays = "";
            StringList taskSelects = new StringList();
            taskSelects.add("from[Subtask]");
            taskSelects.add("attribute[Task Estimated Finish Date]");
            taskSelects.add("current");
            java.util.Date todayDate     = new java.util.Date();
            java.util.Date estFinishDate = new java.util.Date();
            long lSlipDay                = 0;
            long lSlipDayAbsolute        = 0;

            MapList objectList = (MapList) programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String sTaskId = null;
            String strCurrent   =   null;
            String isTaskLeafLevel  =   null;
            String sTaskEstFinishDate = null;
            String strTaskSlipDays = "";
            Map mData = null;
            Map objectMap = null;
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                sTaskId = (String) objectMap.get("id");
                task.setId(sTaskId);
                //DomainObject domTaskId = DomainObject.newInstance(context,sTaskId);
                //mData = domTaskId.getInfo(context,taskSelects);
                isTaskLeafLevel = (String) objectMap.get("from[Subtask]"); //False - leaf level task
                sTaskEstFinishDate = (String) objectMap.get("attribute[Task Estimated Finish Date]");
                strCurrent  =    (String) objectMap.get("current");
                strTaskSlipDays = "";
                if(null!=sTaskEstFinishDate)
                {
                    if("Complete".equals(strCurrent))
                    {
                       strTaskSlipDays = "0";
                    }
                    else
                    {
                        if(null!=isTaskLeafLevel && !"".equals(isTaskLeafLevel) && isTaskLeafLevel.equals("False"))
                        {
                            strTaskSlipDays = calculateSlipDays(context, sTaskId, true, sTaskEstFinishDate , objectList); //for child task
                        }else{
                            strTaskSlipDays = calculateSlipDays(context, sTaskId, false, sTaskEstFinishDate,objectList); //for parent task
                        }
                    }
                }
                showSlipDays.add(strTaskSlipDays);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
			System.out.println("$$$$$$$$$$$$$ Finish getSlipdaysForWBS "+new Date());
			System.out.println("$$$$$$$$$$$$$ Finish showSlipDays "+showSlipDays);
            return showSlipDays;
        }
    }*/
	
	public String calculateSlipDays(Context context, String strTaskId, boolean bIsLeafLevelTask, String strTaskEstimatedFinishDate , MapList objectList) throws FrameworkException
	{		
		String strSlipDays = "";
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		com.matrixone.apps.program.Task childTask = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		try
		{
			if(null != strTaskId && strTaskId.length() > 0)
			{
				ArrayList<Long> slipDaysList = new ArrayList<Long>();

				java.util.Date todayDate = new java.util.Date();
				java.util.Date estFinishDate = new java.util.Date();
				long lSlipDay                      		= 0;
				long lSlipDayAbsolute                   = 0;

				task.setId(strTaskId);

				if(!bIsLeafLevelTask)
				{
					StringList slObjSelectList = new StringList();
					slObjSelectList.addElement(task.SELECT_ID);
					slObjSelectList.addElement(task.SELECT_CURRENT);
					slObjSelectList.addElement(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
					slObjSelectList.addElement(task.SELECT_HAS_SUBTASK);
                    MapList childTasksList = getTasks(context, task, 0 , slObjSelectList, null);
                    childTasksList.sort(task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending",  "date");
                    todayDate.setHours(0);
                    todayDate.setMinutes(0);
                    todayDate.setSeconds(0);
					
					if(null != childTasksList && childTasksList.size() > 0)
					{
						Map childTaskMap = null;
						String strChildTaskHasSubtask	="";
						String strChildTaskCurrent="";
						String strChildTaskEstimatedFinishDate="";
						int nChildTaskListSize = childTasksList.size();
						for(int nChildTaskCount = 0; nChildTaskCount < nChildTaskListSize; nChildTaskCount++)
						{
							 childTaskMap = (Map)childTasksList.get(nChildTaskCount);
                             strChildTaskEstimatedFinishDate = (String)childTaskMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE); // IF TASK EST. FINISH DATE HAS PASSED
                             strChildTaskCurrent = (String)childTaskMap.get(task.SELECT_CURRENT);

                            //if the task is incomplete use the Estimated Finish date for slip days calculations
                                estFinishDate  = sdf.parse(strChildTaskEstimatedFinishDate);
                                estFinishDate.setHours(0);
                                estFinishDate.setMinutes(0);
                                estFinishDate.setSeconds(0);
                               

                                if (estFinishDate.after(todayDate)) 
                                {
                                    continue;
                                }
                                if("Complete".equalsIgnoreCase(strChildTaskCurrent))
                                {
                                    continue;
                                }
                                strChildTaskHasSubtask = (String)childTaskMap.get(SELECT_HAS_SUBTASK);

							
							//if("False".equalsIgnoreCase(strChildTaskHasSubtask)) // if the child task is the leaf level task
							{
								
								childTask.setId((String)childTaskMap.get(task.SELECT_ID));
								
								strChildTaskCurrent = (String)childTaskMap.get(task.SELECT_CURRENT);
								//if(null != strChildTaskCurrent && !"Complete".equalsIgnoreCase(strChildTaskCurrent))
								{
									 /*strChildTaskEstimatedFinishDate = (String)childTaskMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE); // IF TASK EST. FINISH DATE HAS PASSED
									
                                    //if the task is incomplete use the Estimated Finish date for slip days calculations
									estFinishDate  = sdf.parse(strChildTaskEstimatedFinishDate);

									estFinishDate.setHours(0);
									estFinishDate.setMinutes(0);
									estFinishDate.setSeconds(0);
									todayDate.setHours(0);
									todayDate.setMinutes(0);
									todayDate.setSeconds(0);

									if (estFinishDate.before(todayDate)) 
									{*/
										//calculate the slip days and change color according to the amount of days
										//the milestone (task) has slipped
										lSlipDay = childTask.computeDuration(estFinishDate,todayDate) - 1;//take out the starting day
										lSlipDayAbsolute = java.lang.Math.abs(lSlipDay);
									/*}	*/
									slipDaysList.add(lSlipDayAbsolute);
								}
								//else
                                {
									//slipDaysList.add(lSlipDayAbsolute);
								}
							}
							//else
                                {
								//slipDaysList.add(lSlipDayAbsolute);
							}
                            break;
						}
					}
				}
				else
				{
					//if the task is incomplete use the Estimated Finish date for slip days calculations
					estFinishDate  = sdf.parse(strTaskEstimatedFinishDate);

					estFinishDate.setHours(0);
					estFinishDate.setMinutes(0);
					estFinishDate.setSeconds(0);
					todayDate.setHours(0);
					todayDate.setMinutes(0);
					todayDate.setSeconds(0);

					if (estFinishDate.before(todayDate)) 
					{
						//calculate the slip days according to the amount of days the milestone (task) has slipped
						//the milestone (task) has slipped
						lSlipDay = childTask.computeDuration(estFinishDate,todayDate) - 1;//take out the starting day
						lSlipDayAbsolute = java.lang.Math.abs(lSlipDay);
					}	
                    
                     slipDaysList.add(lSlipDayAbsolute);
				}
           
				long largestSlipValue = Collections.max(slipDaysList); 
				strSlipDays = String.valueOf(largestSlipValue);
			}
		}catch (Exception ex)
		{
			throw new FrameworkException();
		}
		return strSlipDays;
	}
	// Add for CR - 02/07/2015  - Slip days should be available as a column on WBS - End
	
	// Code commented to merge FP1626 code - 18 Jul 2016
   /* public Vector getTaskDelivarablesIcon(Context context, String[] args) throws MatrixException
          {
        	Vector vecIconList = new Vector();
            //Start Adding For INZ_0001 
            String SELECT_DELEVERABLE_MD = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";
            String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
            String SELECT_DELEVERABLE_STATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.current";
            String SELECT_DELEVERABLE_POLICY = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.policy";
			String SELECT_DELEVERABLE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
            //End Adding For INZ_0001 

        	try
        	{
        		HashMap programMap = (HashMap) JPO.unpackArgs(args);
        		HashMap paramList = (HashMap) programMap.get("paramList");
        		MapList objectList = (MapList) programMap.get("objectList");
        		Map objectMap = null;
        		int i = 0;
        		Iterator objectListIterator = objectList.iterator();
        		String[] objIdArr = new String[objectList.size()];
        		
        		//Get a list of Project Space Types to hide the icon at root level
        		StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);

				//Remove Milestone and its sub-types from the list as milestone are not allowed to have Delivarables.
				slTaskSubTypes.remove(ProgramCentralConstants.TYPE_MILESTONE);
				//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::Start
				StringList mileStoneSubtypeList	=	ProgramCentralUtil.getSubTypesList(context,ProgramCentralConstants.TYPE_MILESTONE);
				slTaskSubTypes.removeAll(mileStoneSubtypeList);
				//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::End
        		while (objectListIterator.hasNext())
        		{
        			objectMap = (Map) objectListIterator.next();
        			objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
        			i++;
        		}

        		String strLanguage = context.getSession().getLanguage();
				// Start Comment  For INZ_0001
                //String SELECT_DELEVERABLE_MD = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";
                //String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
                //String SELECT_DELEVERABLE_STATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.current";
                //String SELECT_DELEVERABLE_POLICY = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.policy";
                //String SELECT_DELEVERABLE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
				// End Comment  For INZ_0001
                        
                //Start Adding For INZ_0001 
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_MD);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_NAME);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_STATE);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_POLICY);
				DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE);
                //End Adding For INZ_0001 


        		StringList busSelect = new StringList(7);
        		busSelect.add(DomainConstants.SELECT_ID);
        		busSelect.add(SELECT_DELEVERABLE_POLICY);
        		busSelect.add(DomainConstants.SELECT_TYPE);
        		busSelect.add(DomainConstants.SELECT_POLICY);
						// Start Comment  For INZ_0001
					    // busSelect.add(SELECT_DELEVERABLE);
						// End Comment  For INZ_0001
        		busSelect.add(SELECT_DELEVERABLE_NAME);
        		busSelect.add(SELECT_DELEVERABLE_STATE);
        		busSelect.add(SELECT_DELEVERABLE_MD);

        		MapList mlTaskDetails = DomainObject.getInfo(context, objIdArr, busSelect);
        		int actionListSize = 0;
        		if (mlTaskDetails != null)
        		{
        			actionListSize = mlTaskDetails.size();
        		}

        		String strI18nAddDeliverable = i18nNow.getI18nString("emxProgramCentral.TaskAssignment.AddDeliverable",
        				"emxProgramCentralStringResource",
        				strLanguage);
        		
        		//Strings for Delivarables and Tooltip
        		String strIcon = "";
        		String strToolTip = "";
        		
				//Variables for Delivarables and Tooltip
        		String strDelvarables = "";
        		String strTaskId = "";
        		String strTaskType = "";
        		String strHref = "";
        		String strDelNames = "";
        		String strDelState = "";
        		String strModifiedDate = "";
        		String iconDeliverable = "";
        		String strPolicy = "";
        		String strIntState = "";
        		String taskPolicy = "";
        		StringList slDelNames = new StringList();
        		StringList slDelState = new StringList();
        		StringList slDelModDate = new StringList();
        		StringList slPolicy = new StringList();
        		
        		//Variables for building the href
        		StringBuffer sbHrefMaker = new StringBuffer();
        		StringBuffer sbLinkMaker = new StringBuffer();
        		
				Date dtModifiedDate;
				String strInternationalDate;
				SimpleDateFormat formatter;
				formatter = new SimpleDateFormat("d MMM yy",ProgramCentralUtil.getLocale(context));
        		for (i = 0; i < actionListSize; i++)
        		{
        			Map mpObjDetails = (Map) mlTaskDetails.get(i);
                                // Start Comment  For INZ_0001
								//strDelvarables = (String) mpObjDetails.get(SELECT_DELEVERABLE);
								// End Comment  For INZ_0001
        			strTaskId = (String) mpObjDetails.get(SELECT_ID);
        			strTaskType = (String)mpObjDetails.get(SELECT_TYPE);
        			taskPolicy = (String) mpObjDetails.get(SELECT_POLICY);
        			strToolTip = "";
        			if(slTaskSubTypes.contains(strTaskType) || mileStoneSubtypeList.contains(strTaskType)&& ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)){

								// Start Comment  For INZ_0001
                                        //if(ProgramCentralUtil.isNotNullString(strDelvarables)){
										if(mpObjDetails.containsKey(SELECT_DELEVERABLE))
										{
										// End Comment  For INZ_0001
                            //Start Comment For INZ_0001
        					/*strDelNames = (String) mpObjDetails.get(SELECT_DELEVERABLE_NAME);
        					slDelNames = FrameworkUtil.splitString(strDelNames, " ");
        					strDelState = (String) mpObjDetails.get(SELECT_DELEVERABLE_STATE);
        					slDelState = FrameworkUtil.splitString(strDelState, " ");
        					slDelModDate = FrameworkUtil.splitString(strModifiedDate, "##");
                            strPolicy = (String) mpObjDetails.get(SELECT_DELEVERABLE_POLICY);
        					slPolicy = FrameworkUtil.splitString(strPolicy, " ");
                            //End Comment For INZ_0001

                            //Start Adding For INZ_0001 
                            slDelNames = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_NAME);
                            slDelState = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_STATE);
                            slDelModDate = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_MD);
                            slPolicy    =   (StringList) mpObjDetails.get(SELECT_DELEVERABLE_POLICY); 
                            //End Adding For INZ_0001 

        					for(int j=0;j<slDelModDate.size();j++){
        						dtModifiedDate = new Date(slDelModDate.get(j).toString());
        						strInternationalDate = formatter.format(dtModifiedDate);
        						strIntState = i18nNow.getStateI18NString((String)slPolicy.get(j),(String)slDelState.get(j), strLanguage);
        						if(j==0)
        							strToolTip += slDelNames.get(j)+" "+strIntState+" "+strInternationalDate;
        						else
        							strToolTip += "&#xD; "+slDelNames.get(j)+" "+strIntState+" "+strInternationalDate;
        					}
        					strIcon = "iconSmallDocumentAttachment";
        				}else{
        					strToolTip = strI18nAddDeliverable;
        					strIcon = "utilTreeLineNodeClosedSBDisabled";
        				}
        				sbHrefMaker = new StringBuffer();
        				//Added for special character.
        				//strToolTip = XSSUtil.encodeForHTML(context, strToolTip); //Commented for: 168842V6R2013x&167942V6R2013x
        				iconDeliverable = "<img src=\"../common/images/"+strIcon+".gif\" border=\"0\" alt=\"" + strToolTip + "\" title=\""+ strToolTip + "\"/>";

        				sbHrefMaker.append("../common/emxPortal.jsp?portal=PMCDefaultGatePortal");
        				sbHrefMaker.append("&amp;suiteKey=ProgramCentral");  
        				sbHrefMaker.append("&amp;StringResourceFileId=emxProgramCentralStringResource");
        				sbHrefMaker.append("&amp;SuiteDirectory=programcentral&amp;objectId="+strTaskId+"&amp;isFromRMB=true&amp;isFromRMB=true");

        				strHref = sbHrefMaker.toString();
        				sbLinkMaker = new StringBuffer();
        				sbLinkMaker.append("<a href=\"javascript:emxTableColumnLinkClick('" + strHref);
        				sbLinkMaker.append("', '600', '600', 'false', 'popup','')\"  class='object'>");
        				sbLinkMaker.append(iconDeliverable);
        				sbLinkMaker.append("</a>");
        				vecIconList.add(sbLinkMaker.toString());
        			}else{
        				vecIconList.add("");
        			}
        		}
        		return vecIconList;
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        		throw new MatrixException();
        	}
            finally { 
                // Start adding  For INZ_0001 
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_MD);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_NAME);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_STATE);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_POLICY);
				DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE);
               // End adding  For INZ_0001 
            }
          }*/
		  public Vector getTaskDelivarablesIcon(Context context, String[] args) throws MatrixException
          {
        	Vector vecIconList = new Vector();
			//Start Adding For INZ_0001 
            String SELECT_DELEVERABLE_MD = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";
            String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
            String SELECT_DELEVERABLE_STATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.current";
            String SELECT_DELEVERABLE_POLICY = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.policy";
			String SELECT_DELEVERABLE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
            //End Adding For INZ_0001 
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramList = (HashMap) programMap.get("paramList");
			String exportFormat = (String)paramList.get("exportFormat");
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			int i = 0;
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];

			//Get a list of Project Space Types to hide the icon at root level
			StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);

				//Remove Milestone and its sub-types from the list as milestone are not allowed to have Delivarables.
				slTaskSubTypes.remove(ProgramCentralConstants.TYPE_MILESTONE);
				//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::Start
				StringList mileStoneSubtypeList	=	ProgramCentralUtil.getSubTypesList(context,ProgramCentralConstants.TYPE_MILESTONE);
				slTaskSubTypes.removeAll(mileStoneSubtypeList);
				//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::End
        		while (objectListIterator.hasNext())
        		{
        			objectMap = (Map) objectListIterator.next();
        			objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
        			i++;
        		}

        		String strLanguage = context.getSession().getLanguage();
				// Start Comment  For INZ_0001
        		//String SELECT_DELEVERABLE_POLICY = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.policy";
        		//String SELECT_DELEVERABLE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
        		//String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
        		//String SELECT_DELEVERABLE_STATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.current";
        		//String SELECT_DELEVERABLE_MD = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";
				// End Comment  For INZ_0001
                        
                //Start Adding For INZ_0001 
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_MD);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_NAME);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_STATE);
                DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE_POLICY);
				DomainConstants.MULTI_VALUE_LIST.add(SELECT_DELEVERABLE);
                //End Adding For INZ_0001 

        		StringList busSelect = new StringList(7);
        		busSelect.add(DomainConstants.SELECT_ID);
        		busSelect.add(SELECT_DELEVERABLE_POLICY);
        		busSelect.add(DomainConstants.SELECT_TYPE);
        		busSelect.add(DomainConstants.SELECT_POLICY);
        		busSelect.add(SELECT_DELEVERABLE);
				busSelect.add(SELECT_DELEVERABLE_NAME);
        		busSelect.add(SELECT_DELEVERABLE_STATE);
        		busSelect.add(SELECT_DELEVERABLE_MD);

        		MapList mlTaskDetails = DomainObject.getInfo(context, objIdArr, busSelect);

        		int actionListSize = 0;
        		if (mlTaskDetails != null)
        		{
        			actionListSize = mlTaskDetails.size();
        		}

			String strI18nAddDeliverable = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskAssignment.AddDeliverable", strLanguage);

			//Strings for Delivarables and Tooltip
			String strIcon = "";
			String strToolTip = "";

			//Variables for Delivarables and Tooltip
			String strDelvarables = "";
			String strTaskId = "";
			String strTaskType = "";
			String strHref = "";
			String strDelNames = "";
			String strDelState = "";
			String strModifiedDate = "";
			String iconDeliverable = "";
			String strPolicy = "";
			String strIntState = "";
			String taskPolicy = "";
			StringList slDelNames = new StringList();
			StringList slDelState = new StringList();
			StringList slDelModDate = new StringList();
			StringList slPolicy = new StringList();

			//Variables for building the href
			StringBuffer sbHrefMaker = new StringBuffer();
			StringBuffer sbLinkMaker = new StringBuffer();

			Date dtModifiedDate;
			String strInternationalDate;
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat("d MMM yy",ProgramCentralUtil.getLocale(context));
			for (i = 0; i < actionListSize; i++)
			{
				Map mpObjDetails = (Map) mlTaskDetails.get(i);
        			// Start Comment  For INZ_0001
					//strDelvarables = (String) mpObjDetails.get(SELECT_DELEVERABLE);
					// End Comment  For INZ_0001
        			strTaskId = (String) mpObjDetails.get(SELECT_ID);
        			strTaskType = (String)mpObjDetails.get(SELECT_TYPE);
        			taskPolicy = (String) mpObjDetails.get(SELECT_POLICY);
        			strToolTip = "";
        			if(slTaskSubTypes.contains(strTaskType) || mileStoneSubtypeList.contains(strTaskType)&& ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)){

        				// Start Comment  For INZ_0001
						//if(ProgramCentralUtil.isNotNullString(strDelvarables)){
						if(mpObjDetails.containsKey(SELECT_DELEVERABLE))
						{
						// End Comment  For INZ_0001
							//Start Comment For INZ_0001
        					/*strDelNames = (String) mpObjDetails.get(SELECT_DELEVERABLE_NAME);
        					slDelNames = FrameworkUtil.splitString(strDelNames, matrix.db.SelectConstants.cSelectDelimiter);
        					strDelState = (String) mpObjDetails.get(SELECT_DELEVERABLE_STATE);
        					slDelState = FrameworkUtil.splitString(strDelState, matrix.db.SelectConstants.cSelectDelimiter);
        					strModifiedDate = (String) mpObjDetails.get(SELECT_DELEVERABLE_MD);
        					slDelModDate = FrameworkUtil.splitString(strModifiedDate, matrix.db.SelectConstants.cSelectDelimiter);
        					strPolicy = (String) mpObjDetails.get(SELECT_DELEVERABLE_POLICY);
        					slPolicy = FrameworkUtil.splitString(strPolicy, matrix.db.SelectConstants.cSelectDelimiter);*/
							//End Comment For INZ_0001

                            //Start Adding For INZ_0001 
                            slDelNames = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_NAME);
                            slDelState = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_STATE);
                            slDelModDate = (StringList) mpObjDetails.get(SELECT_DELEVERABLE_MD);
                            slPolicy    =   (StringList) mpObjDetails.get(SELECT_DELEVERABLE_POLICY); 
                            //End Adding For INZ_0001 
						for(int j=0;j<slDelModDate.size();j++){
							dtModifiedDate = new Date(slDelModDate.get(j).toString());
							strInternationalDate = formatter.format(dtModifiedDate);
							strIntState = i18nNow.getStateI18NString((String)slPolicy.get(j),(String)slDelState.get(j), strLanguage);
							if(j==0)
								strToolTip += XSSUtil.encodeForHTML(context, (String)slDelNames.get(j))+" "+strIntState+" "+strInternationalDate;
							else
								strToolTip += "&#xD; "+XSSUtil.encodeForHTML(context, (String)slDelNames.get(j))+" "+strIntState+" "+strInternationalDate;
						}
						strIcon = "iconSmallDocumentAttachment";
					}else{
						strToolTip = strI18nAddDeliverable;
						strIcon = "utilTreeLineNodeClosedSBDisabled";
					}
					sbHrefMaker = new StringBuffer();
					//Added for special character.
					//strToolTip = XSSUtil.encodeForHTML(context, strToolTip); //Commented for: 168842V6R2013x&167942V6R2013x
					iconDeliverable = "<img src=\"../common/images/"+strIcon+".gif\" border=\"0\" alt=\"" + strToolTip + "\" title=\""+ strToolTip + "\"/>";

        				sbHrefMaker.append("../common/emxPortal.jsp?portal=PMCDefaultGatePortal");
        				sbHrefMaker.append("&amp;suiteKey=ProgramCentral");  
        				sbHrefMaker.append("&amp;StringResourceFileId=emxProgramCentralStringResource");
        				sbHrefMaker.append("&amp;SuiteDirectory=programcentral&amp;objectId="+strTaskId+"&amp;isFromRMB=true&amp;isFromRMB=true");

					strHref = sbHrefMaker.toString();
					sbLinkMaker = new StringBuffer();
					sbLinkMaker.append("<a href=\"javascript:emxTableColumnLinkClick('" + strHref);
					sbLinkMaker.append("', '600', '600', 'false', 'popup','')\"  class='object'>");
					sbLinkMaker.append(iconDeliverable);
					sbLinkMaker.append("</a>");
					if(("CSV".equals(exportFormat) || "HTML".equals(exportFormat)) &&
							!strI18nAddDeliverable.equals(strToolTip)){
						vecIconList.add(strToolTip);
					}else{
						vecIconList.add(sbLinkMaker.toString());
					}
				}else{
					vecIconList.add(DomainConstants.EMPTY_STRING);
				}
			}

			return vecIconList;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
			// Start adding  For INZ_0001 
			finally
			{
			    DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_MD);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_NAME);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_STATE);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE_POLICY);
				DomainConstants.MULTI_VALUE_LIST.remove(SELECT_DELEVERABLE);            
			}
			// End adding  For INZ_0001 
          }
		  /**
     * Where : In the Structure Browser, TableMenu - > WBS Tasks
     * How : Get the objectId from argument map and extract the objects
     *          with "Subtask" relationship through getWBSTasks
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the "paramMap"
     *        paramMap holds the following input arguments:
     *          0 - String containing "objectId"
     * @returns MapList
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
// Code commented to merge FP1626 code - 18 Jul 2016
   /* public MapList getWBSSubtasks(Context context, String[] args) throws Exception
    {
        HashMap arguMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String) arguMap.get("objectId");
        String strExpandLevel = (String) arguMap.get("expandLevel");
		String selectedProgram = (String) arguMap.get("selectedProgram");
		String selectedTable = (String) arguMap.get("selectedTable");
		//Added:10-June-2010:vf2:R210 PRG:IR-056503
		String effortFilter = (String) arguMap.get("PMCWBSEffortFilter");
		//End:10-June-2010:vf2:R210 PRG:IR-056503
		MapList mapList = new MapList();
		MapList tempMapList = new MapList();
        
		// Added for Phase 2 - start
		StringList slObjSelect = new StringList();
		slObjSelect.addElement(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
		slObjSelect.addElement("attribute[Critical Task]");
		DomainObject doObject = DomainObject.newInstance(context);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		// Added for Phase 2 - end
        DomainObject domobjTask = DomainObject.newInstance(context,strObjectId);
		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
		if(domobjTask.isKindOf(context, TYPE_PROJECT_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
			String[] arrJPOArguments = new String[1];
			HashMap programMap = new HashMap();
			programMap.put("objectId", strObjectId);
			//Added:10-June-2010:vf2:R210 PRG:IR-056503
			if(!"null".equals(effortFilter) && null!= effortFilter && !"".equals(effortFilter)) {
				programMap.put("effortFilter", effortFilter);
			}
			//End:10-June-2010:vf2:R210 PRG:IR-056503

			arrJPOArguments = JPO.packArgs(programMap);
			mapList = (MapList)JPO.invoke(context,
					"emxEffortManagementBase", null, "getProjectTaskList",
					arrJPOArguments, MapList.class);

		}
		else
		{
			mapList = (MapList) getWBSTasks(context,strObjectId,DomainConstants.RELATIONSHIP_SUBTASK,nExpandLevel);

            //Modify the list to include the details of Status Icon and Slip Days
            //Do it only for 2 views
            if (selectedTable.equalsIgnoreCase("PMCWBSViewTable") ||  selectedTable.equalsIgnoreCase("PMCWBSPlanningViewTable"))
            {
                tempMapList = mapList;
                mapList = new MapList();
                int iTaskListSize = tempMapList.size();
                Map mObjMap = null;
                String strObjId = "";
                Map mStatusSlip = null;
                String strStatusColor = "";
                String strSlipDays = "";
                for (int i = 0; i < iTaskListSize ; i++)
                {
                    mObjMap = (Map)tempMapList.get(i);
                    strObjId = (String)mObjMap.get(DomainConstants.SELECT_ID);
					
					// Added for Phase 2 - start
                    if(null != strObjId && strObjId.length() > 0)
                    {
                    	doObject = new DomainObject(strObjId);
                    	Map attributeMap = doObject.getInfo(context, slObjSelect);
                    
                    	String strCriticalTask = (String)attributeMap.get("attribute[Critical Task]");
                        String strMSILCriticalTask = (String)attributeMap.get("attribute[MSIL Critical Task]");
                        String strTableCriticalTask = "No";
                        
                        if(strCriticalTask.equalsIgnoreCase("TRUE") || strMSILCriticalTask.equalsIgnoreCase("Yes"))
                        	strTableCriticalTask = "Yes";
                        mObjMap.put("attribute[MSIL Critical Task]", strTableCriticalTask);
                    }
					// Added for Phase 2 - end
                    mStatusSlip = getStatusSlip(context, strObjId);
                    strStatusColor = (String)mStatusSlip.get("statuscolor");
                    strSlipDays = (String)mStatusSlip.get("slipdays");
                    mObjMap.put("statuscolor", strStatusColor);
                    mObjMap.put("slipdays", strSlipDays);
                    mapList.add(mObjMap);
                }
            }

        }

        //Added:nr2:PRG:R211:IR-072682V6R2012
        HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
        mapList.add(hmTemp);
        //End::nr2:PRG:R211:IR-072682V6R2012

        return mapList;

    }*/
	/**
	 * Where : In the Structure Browser, TableMenu - > WBS Tasks
	 * How : Get the objectId from argument map and extract the objects
	 *          with "Subtask" relationship through getWBSTasks
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSSubtasks(Context context, String[] args) throws Exception
	{
		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) arguMap.get("objectId");
		String strExpandLevel = (String) arguMap.get("expandLevel");
		String selectedProgram = (String) arguMap.get("selectedProgram");
		String selectedTable = (String) arguMap.get("selectedTable");
		//Added:10-June-2010:vf2:R210 PRG:IR-056503
		String effortFilter = (String) arguMap.get("PMCWBSEffortFilter");
		//End:10-June-2010:vf2:R210 PRG:IR-056503
		MapList mapList = new MapList();
        
		MapList tempMapList = new MapList(); // MSIL Change
		// Added for Phase 2 - start
		StringList slObjSelect = new StringList();
		slObjSelect.addElement(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
		slObjSelect.addElement("attribute[Critical Task]");
		DomainObject doObject = DomainObject.newInstance(context);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		// Added for Phase 2 - end

		DomainObject domobjTask = DomainObject.newInstance(context,strObjectId);
		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
		if(domobjTask.isKindOf(context, TYPE_PROJECT_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
			String[] arrJPOArguments = new String[3];
			HashMap programMap = new HashMap();
			programMap.put("objectId", strObjectId);
			programMap.put("ExpandLevel", strExpandLevel);
			//Added:10-June-2010:vf2:R210 PRG:IR-056503
			if(!"null".equals(effortFilter) && null!= effortFilter && !"".equals(effortFilter)) {
				programMap.put("effortFilter", effortFilter);
			}
			//End:10-June-2010:vf2:R210 PRG:IR-056503
			arrJPOArguments = JPO.packArgs(programMap);
			mapList = (MapList)JPO.invoke(context,
					"emxEffortManagementBase", null, "getProjectTaskList",
					arrJPOArguments, MapList.class);
		}
		else
		{
			mapList = (MapList) getWBSTasks(context,strObjectId,DomainConstants.RELATIONSHIP_SUBTASK,nExpandLevel);
			
			// MSIL Change start
			//Modify the list to include the details of Status Icon and Slip Days
            //Do it only for 2 views
            // Added by Ajit -- 27/07/2016 -- To get Slipdays and Status Icon Column Values while creating New Table View -- Start
            //if (selectedTable.equalsIgnoreCase("PMCWBSViewTable") ||  selectedTable.equalsIgnoreCase("PMCWBSPlanningViewTable"))
            if (selectedTable.contains("PMCWBSViewTable") ||  selectedTable.contains("PMCWBSPlanningViewTable"))  
            // Added by Ajit -- 27/07/2016 -- To get Slipdays and Status Icon Column Values while creating New Table View -- End
            {
                tempMapList = mapList;
                mapList = new MapList();
                int iTaskListSize = tempMapList.size();
                Map mObjMap = null;
                String strObjId = "";
                Map mStatusSlip = null;
                String strStatusColor = "";
                String strSlipDays = "";
                for (int i = 0; i < iTaskListSize ; i++)
                {
                    mObjMap = (Map)tempMapList.get(i);
                    strObjId = (String)mObjMap.get(DomainConstants.SELECT_ID);
					
					// Added for Phase 2 - start
                    if(null != strObjId && strObjId.length() > 0)
                    {
                    	doObject = new DomainObject(strObjId);
                    	Map attributeMap = doObject.getInfo(context, slObjSelect);
                    
                    	String strCriticalTask = (String)attributeMap.get("attribute[Critical Task]");
                        String strMSILCriticalTask = (String)attributeMap.get("attribute[MSIL Critical Task]");
                        String strTableCriticalTask = "No";
                        
                        if(strCriticalTask.equalsIgnoreCase("TRUE") || strMSILCriticalTask.equalsIgnoreCase("Yes"))
                        	strTableCriticalTask = "Yes";
                        mObjMap.put("attribute[MSIL Critical Task]", strTableCriticalTask);
                    }
					// Added for Phase 2 - end
                    mStatusSlip = getStatusSlip(context, strObjId);
                    strStatusColor = (String)mStatusSlip.get("statuscolor");
                    strSlipDays = (String)mStatusSlip.get("slipdays");
                    mObjMap.put("statuscolor", strStatusColor);
                    mObjMap.put("slipdays", strSlipDays);
                    mapList.add(mObjMap);
                }
            }
			// MSIL Change end
		}

		//Added:nr2:PRG:R211:IR-072682V6R2012
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");
		mapList.add(hmTemp);
		//End::nr2:PRG:R211:IR-072682V6R2012
		boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		if(isAnDInstalled){
			boolean isLocked = Task.isParentProjectLocked(context, strObjectId);
	 		if(isLocked){
				for(Object tempMap : mapList){
					((Map)tempMap).put("disableSelection", "true"); 
					((Map)tempMap).put("RowEditable", "readonly");
				}
			}
 		}
		return mapList;
	}
	// Added by Vinit - Start 29-Jun-2018
	// to return the Status Slip days for the project.
	@com.matrixone.apps.framework.ui.ProgramCallable
    public String getStatusSlipForDashboard(Context context, String[] args) throws Exception 
	{
		String strSlipDays  = "0";
		try
         {
             HashMap programMap  = (HashMap)JPO.unpackArgs(args);
             String strProjectID = (String) programMap.get("objectId");
             Map returnMap       = getStatusSlip(context, strProjectID);
			 
			 strSlipDays  = (String)returnMap.get("slipdays");
         }
		 catch(Exception exception)
         {
             exception.printStackTrace();
             throw exception;
         }
		 
		 return strSlipDays;
	}
	
	// Added by Vinit - End 29-Jun-2018

    public Map getStatusSlip(Context context, String strProjId) throws Exception
    {
        String strStatusColor = "";
        String strSlipDays = "";
        Map mReturnMap = new HashMap();
        com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
        DomainObject doObject = DomainObject.newInstance(context);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

    	String policyName = task.getDefaultPolicy(context);
    	String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy", policyName, "state_Complete");
    	try
    	{
            String strEstFinishDate = "";
            doObject.setId(strProjId);
            StringList taskSelects = new StringList();
            taskSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            taskSelects.add("from[Subtask]");
            taskSelects.add(task.SELECT_CURRENT);
			// Added by Vinit - Start 29-Jun-2018
			taskSelects.add(task.SELECT_TASK_ESTIMATED_START_DATE);
			taskSelects.add(task.SELECT_TASK_ESTIMATED_DURATION);
			taskSelects.add(DomainObject.SELECT_PERCENTCOMPLETE);
			// Added by Vinit - End 29-Jun-2018

            Map mObjInfo = doObject.getInfo(context, taskSelects);
            String strHasSubTask = (String)mObjInfo.get("from[Subtask]");
            String strState      = (String)mObjInfo.get(task.SELECT_CURRENT);
			
			// Added by Vinit - Start 29-Jun-2018
			String strTaskStartDate   = "";
			String strDuration        = "";
			String strPercentComplete = "";
			// Added by Vinit - End 29-Jun-2018
		
            //First check the Task State, if its Complete then return Green and slip days as ""
            if (strState.equals(COMPLETE_STATE))
            {
                mReturnMap.put("statuscolor", "Green");
                mReturnMap.put("slipdays", "0");
            }
            else
            {
                if (strHasSubTask.equalsIgnoreCase("False"))
                {
                    //get Status based on Task's Estimated Finish Date
                    strEstFinishDate = (String)mObjInfo.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    //Get Slip Days and Status 
					
					// Added by Vinit - Start 29-Jun-2018
					
					strTaskStartDate   = (String)mObjInfo.get(task.SELECT_TASK_ESTIMATED_START_DATE);
					strDuration        = (String)mObjInfo.get(task.SELECT_TASK_ESTIMATED_DURATION);
					strPercentComplete = (String)mObjInfo.get(DomainObject.SELECT_PERCENTCOMPLETE);
					
					//mReturnMap = getTaskStatusSlip(context , strEstFinishDate);
                    mReturnMap = getTaskStatusSlip(context , strEstFinishDate, strTaskStartDate, strDuration, strPercentComplete);
					
					// Added by Vinit - End 29-Jun-2018
                }
                else 
                {
                    //Noe get all incomplete tasks
					// Added by Vinit Start - 29-Jun-2018
                    //String strWhere = "current != '" + COMPLETE_STATE + "'";
					
					//String strWhere = "DomainObject.SELECT_PERCENTCOMPLETE<100";
					
					String strWhere = "current!='Complete' && current!='Review'";
					
                    //MapList tasksForProject = project.getAllWBSTasks(context, taskSelects, strWhere);
					// Added by Vinit Ends - 29-Jun-2018

                     MapList tasksForProject = doObject.getRelatedObjects(context,
                                                                         "Subtask",
                                                                         "*",
                                                                         taskSelects,
                                                                        null,
                                                                        false,
                                                                        true,
                                                                        (short)0,
                                                                        strWhere,
                                                                        null,
                                                                        0);
                    tasksForProject.sort(project.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
                    
                    // Added and Modified by Dheeraj Garg <21-Oct-2016> 88067 : The system is showing 158 slip days for line side equipment whereas the max delay in the child task is 6 days. -- Start
                    //if (tasksForProject.size() > 0)
                    //{
                    //    Map mFirstTask = (Map) tasksForProject.get(0);
                    //    strEstFinishDate = (String) mFirstTask.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    //    mReturnMap = getTaskStatusSlip(context , strEstFinishDate);
                    //}
                    //else
                    //{
                    mReturnMap.put("statuscolor", "");
                    mReturnMap.put("slipdays", "0");

                    for (int i = 0; i < tasksForProject.size() ; i++)

                    {
                        Map mTaskInfo = (Map) tasksForProject.get(i);

                        String strTaskHasSubTask = (String)mTaskInfo.get("from[Subtask]");

                        if (strTaskHasSubTask.equalsIgnoreCase("False"))
                        {

                            strEstFinishDate   = (String) mTaskInfo.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
							
							// Added by Vinit - Start 29-Jun-2018
							strTaskStartDate   = (String)mTaskInfo.get(task.SELECT_TASK_ESTIMATED_START_DATE);
							strDuration        = (String)mTaskInfo.get(task.SELECT_TASK_ESTIMATED_DURATION);
							strPercentComplete = (String)mTaskInfo.get(DomainObject.SELECT_PERCENTCOMPLETE);
							
							//mReturnMap = getTaskStatusSlip(context , strEstFinishDate);

                            mReturnMap = getTaskStatusSlip(context , strEstFinishDate, strTaskStartDate, strDuration, strPercentComplete);
							// Added by Vinit - End 29-Jun-2018
							
                            break;

                        }

                    }
                    // Added and Modified by Dheeraj Garg <21-Oct-2016> 88067 : The system is showing 158 slip days for line side equipment whereas the max delay in the child task is 6 days. -- End

                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return mReturnMap;
    }
	
	// Added by Vinit - 29-Jun-2018
	// old method is available as back-up at the end of this JPO.

    public Map getTaskStatusSlip(Context context , String strEstFinishDate, String strTaskStartDate, String strDuration, String strPercentComplete) throws Exception
    {
        Map mReturnMap = new HashMap();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        Date tempDate = new Date();

        Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
        int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
        
        Date estFinishDate = sdf.parse(strEstFinishDate);
		Date estStartDate  = sdf.parse(strTaskStartDate);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        long daysRemaining = (long) task.computeDuration(sysDate, estFinishDate);
		
        String strStatusColor = "";
        String strSlipDays = "0";
        long lSlipDay                      		= 0;
        long lSlipDayAbsolute                   = 0;
		Date todayDate = new Date();
		todayDate.setHours(0);
		todayDate.setMinutes(0);
		todayDate.setSeconds(0);
		
		double dCurrentPC = Double.parseDouble(strPercentComplete);
		double dDuration  = Double.parseDouble(strDuration);
		
		
		if(dCurrentPC>=100)
		{
			// do nothing
		}
		else if (sysDate.after(estStartDate))
		{
		
			java.text.DecimalFormat dd = new  java.text.DecimalFormat("0");

			
			long daysPassedBasedOnStartDate  = (long) task.computeDuration(estStartDate, sysDate);
			
			Long ldaysPassedBasedStartDate = new Long(daysPassedBasedOnStartDate);
			
			
			double dPassedBasedOnStartDate   = ldaysPassedBasedStartDate.doubleValue();
  
			if(dCurrentPC==0)
			{
				// slip days will be sysDate - estStartDate : dPassedBasedOnStartDate
				//strSlipDays = dPassedBasedOnStartDate.toString();
				
				//strSlipDays = String.valueOf(dPassedBasedOnStartDate);
				
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
					double dPCForSlipDays =  dPercentageShouldHaveBeenAsOfToday - dCurrentPC;
					
					double dSlipDays = dPCForSlipDays / dPerDayPCValue;

					strSlipDays  = dd.format(dSlipDays);

					//strSlipDays = dSlipDays.toString();
					//strSlipDays = String.valueOf(dSlipDays);
				}
			}
		}
		else{
			// then, task is not yet started.
		}
		
		if (sysDate.after(estFinishDate))
        {
            strStatusColor = "Red";
        }
        else if (daysRemaining <= yellowRedThreshold)
        {
            strStatusColor = "Yellow";
        }
        else
        {
            strStatusColor = ProgramCentralConstants.EMPTY_STRING;
        }
		
        mReturnMap.put("statuscolor", strStatusColor);
        mReturnMap.put("slipdays", strSlipDays);
        return mReturnMap;
    }

    public Vector getSlipdaysForWBS(Context context, String[] args)
      throws Exception
    {
        Vector showSlipDays = new Vector();
        try
        {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sSlipDays = "0";

            // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - start
            Map paramListMap = (Map) programMap.get("paramList");
            boolean bIsRefreshCommand = (Boolean)paramListMap.get("HasMergedCell"); // in case of refresh command, this value will be true. else it will be false
            //System.out.println("\n...HasMergedCell ..."+bIsRefreshCommand);
            // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - end
            
            MapList objectList = (MapList) programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            Map objectMap = null;
            String strIsRoot        = "";
            ArrayList<Integer> alSlipDay = new  ArrayList<Integer>();
            Map mapChild            = null;
            MapList mlChildList =   null;
            String strSlipDayes =   "";

            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - Start
                String strObjId = (String)objectMap.get("id");              
                if(bIsRefreshCommand)               
                {
                    Map mStatusSlip = getStatusSlip(context, strObjId);                 
                    sSlipDays = (String)mStatusSlip.get("slipdays");                    
                }
                else
                {
                    sSlipDays = (String)objectMap.get("slipdays");                  
                    if((null == sSlipDays) || "null".equals(sSlipDays)){
                        Map mStatusSlip = getStatusSlip(context, strObjId);                     
                        sSlipDays = (String)mStatusSlip.get("slipdays");
                    }
                }
                // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - End             
                strIsRoot = (String)objectMap.get("Root Node");
                if("true".equals(strIsRoot))
                {
                    mlChildList =   (MapList)objectMap.get("children");
                    for (int kk=0; kk< mlChildList.size();kk++ ){
                        mapChild=(Map) mlChildList.get(kk);
                        // Modified by Dheeraj Garg <30-Nov-2016> Change the date and click on Refresh command, slip days not refreshing for the root node - Start
                        String strChildObjId = (String) mapChild.get("id");

                        String sChildSlipDays = "0";

                        if (bIsRefreshCommand) {

                            Map mStatusSlip = getStatusSlip(context, strChildObjId);

                            sChildSlipDays = (String) mStatusSlip.get("slipdays");

                        } else {

                            sChildSlipDays = (String) mapChild.get("slipdays");

                            if ((null == sChildSlipDays) || "null".equals(sChildSlipDays)) {

                                Map mStatusSlip = getStatusSlip(context, strChildObjId);

                                sChildSlipDays = (String) mStatusSlip.get("slipdays");

                            }

                        }

                        alSlipDay.add((Integer) Integer.parseInt(sChildSlipDays));

                        // strSlipDayes = (String) mapChild.get("slipdays");
                        // if(null != strSlipDayes &&
                        // !"".equals(strSlipDayes.trim()))

                        // alSlipDay.add((Integer)
                        // Integer.parseInt(strSlipDayes));
                        // Modified by Dheeraj Garg <30-Nov-2016> Change the date and click on Refresh command, slip days not refreshing for the root node - End
                    }
                    // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - Start
                    if(null != mlChildList && mlChildList.size()==0){
                        showSlipDays.add(sSlipDays);
                    }
                    else if(alSlipDay.size()>0)
                    // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - End
                    {
                        showSlipDays.add(String.valueOf(Collections.max(alSlipDay)));
                    }
                    else
                    {
                        showSlipDays.add("0");
                    }
                }
                else
                {
                    showSlipDays.add(sSlipDays);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return showSlipDays;
    }

    // Added and modified -- 15/04/2016 -- Ajit -- Export to Excel was not working for Status column of WBS Page -- start
    public Vector getStatusIcon(Context context, String[] args) throws Exception
    {
        Vector vStatusIcon = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strProjetDelay   = "";
            String statusGif        = "";
            String strIsRoot        = "";
            // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - Start
            Map paramListMap = (Map) programMap.get("paramList");
            boolean bIsRefreshCommand = (Boolean)paramListMap.get("HasMergedCell"); // in case of refresh command, this value will be true. else it will be false
            //System.out.println("\n...HasMergedCell :: getStatusIcon() ..."+bIsRefreshCommand);
            // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - End
            //Added:08-June-2010:ak4:R210:PRG:Bug:055631
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            //End:08-June-2010:ak4:R210:PRG:Bug:055631
            MapList objectList      = (MapList) programMap.get("objectList");
            MapList mlChildList     = null;
            Iterator objectListItr  = objectList.iterator();
            Map objectMap           = null;
            Map mapChild            = null;

            StringList slIconList   =   new StringList();
            
            while(objectListItr.hasNext())
            {
            	//Added:08-June-2010:ak4:R210:PRG:Bug:055631
                String statusToolTip = "";
                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                objectMap = (Map) objectListItr.next();

                // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - Start
                String strObjId = (String)objectMap.get("id");              
                if(bIsRefreshCommand)               
                {
                    Map mStatusSlip = getStatusSlip(context, strObjId);
                    statusToolTip = (String)mStatusSlip.get("statuscolor");                 
                }
                else
                {
                    statusToolTip = (String)objectMap.get("statuscolor");                   
                    if((null == statusToolTip) || "null".equals(statusToolTip)){
                        Map mStatusSlip = getStatusSlip(context, strObjId);
                        statusToolTip = (String)mStatusSlip.get("statuscolor");                     
                    }                   
                }
                // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - End

                strIsRoot = (String)objectMap.get("Root Node");
                if("true".equals(strIsRoot))
                {
                   
                    mlChildList =   (MapList)objectMap.get("children");
                    for (int kk=0; kk< mlChildList.size();kk++ ){
                        mapChild=(Map) mlChildList.get(kk);
                        slIconList.add((String) mapChild.get("statuscolor"));
                    }
                    // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - Start
                    if(null !=mlChildList && mlChildList.size()==0){
                        slIconList.add(statusToolTip);
                    }                   
                    // Added by Ajit -- 09/09/2016 -- Refresh command was working for Slipdays and Status Icon Column - End

                    if (slIconList.contains("Red")){
                    	statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage());                    	
                        statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" title=\"";                        
                        statusGif += (statusToolTip + "\"/>");
                    } else if (slIconList.contains("Yellow")){
                    	statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage());
                        statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" title=\"";
                        statusGif += (statusToolTip+ "\"/>");

						// Added and Modified by Dheeraj Garg <25-Nov-2016> ASR-89676: DIFFERENCE IN TASK STATE - Start
					} else if (slIconList.contains(null) || slIconList.contains("") || slIconList.contains("null")) {
						// } else if (slIconList.contains("Green")){
						// statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.Legend.OnTime", "emxProgramCentralStringResource", context.getSession().getLanguage());
						// statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" title=\"";
						// statusGif += (statusToolTip+ "\"/>");
						statusGif = ProgramCentralConstants.EMPTY_STRING;
					} else {
						// statusGif = ProgramCentralConstants.EMPTY_STRING;
						statusToolTip = i18nNow.getI18nString("emxProgramCentral.Common.Legend.OnTime",
								"emxProgramCentralStringResource", context.getSession().getLanguage());
                        statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" title=\"";
                        statusGif += (statusToolTip+ "\"/>");

					}

					// Added and Modified by Dheeraj Garg <25-Nov-2016> ASR-89676: DIFFERENCE IN TASK STATE - End
                }
                else
                {
                    // Modified by Dheeraj Garg <30-Nov-2016> Status icon is not refreshing after clicking on Refresh command - Start
					//strProjetDelay = (String)objectMap.get("statuscolor");

					strProjetDelay = statusToolTip;

					// Modified by Dheeraj Garg <30-Nov-2016> Status icon is not refreshing after clicking on Refresh command - End
                   
                    if(strProjetDelay==null)
                    {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                    else if (strProjetDelay.equals("Green"))
                    {
                    	statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.OnTime", "emxProgramCentralStringResource", context.getSession().getLanguage());
                        statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" title=\"";
                        statusGif += (statusToolTip+ "\"/>");
                    } else if (strProjetDelay.equals("Red"))
                    {
                    	statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage());
                        statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" title=\"";
                        statusGif += (statusToolTip+ "\"/>");
                    } else if (strProjetDelay.equals("Yellow"))
                    {
                    	statusToolTip=i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage());
                        statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" title=\"";
                        statusGif += (statusToolTip + "\"/>");
                    } else {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                }
                
                //Added:08-June-2010:ak4:R210:PRG:Bug:055631
                if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
                	vStatusIcon.add(statusToolTip);// statusToolTip will get exported
                 }else{
                //End:08-June-2010:ak4:R210:PRG:Bug:055631
                	 vStatusIcon.add("<label>"+statusGif+"</label>");// this default string displayed in Browser
                 }
                
                //vStatusIcon.add(statusGif);
                
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return vStatusIcon;
    }

// Added and modified -- 15/04/2016 -- Ajit -- Export to Excel was not working for Status column of WBS Page -- End

    /****************************************************************************************************
     *       Methods for Config Table Conversion Task
     ****************************************************************************************************/
    /**
     * gets the list of Assigned WBS Task Objects to context User
     * Used for PMCAssignedWBSTaskSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns Object
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
	 @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getAssignedWBSTask(Context context, String[] args)
      throws Exception
    {
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
                DomainConstants.TYPE_TASK, "PROGRAM");

        // Assigned status means not in the complete,archive or create state
        StringBuffer busWhere =  new StringBuffer();
        busWhere.append(task.SELECT_CURRENT);
        busWhere.append("!='");
        busWhere.append(STATE_PROJECT_TASK_COMPLETE);
        busWhere.append("'");

        // append the Not create state
        if (!"".equals(STATE_PROJECT_TASK_CREATE))
        {
            busWhere.append(" && ");
            busWhere.append(task.SELECT_CURRENT);
            busWhere.append("!='");
            busWhere.append(STATE_PROJECT_TASK_CREATE);
            busWhere.append("'");
        }

        // append the Not archive state
        if (!"".equalsIgnoreCase(STATE_PROJECT_TASK_ARCHIVE))
        {
            busWhere.append(" && ");
            busWhere.append(task.SELECT_CURRENT);
            busWhere.append("!='");
            busWhere.append(STATE_PROJECT_TASK_ARCHIVE);
            busWhere.append("'");
        }

         busWhere.append(" && from[Subtask]==False");
            
        return getMyTasks(context, args, busWhere.toString());
    }

    // 26/03/2016  | Ajit | For Improvement of Slowness of Assigned Selected mutiple Task -- Start

    /**
         * This method is used to do Background Processing while multiple task is Assign Selected.
         * @return void 
         * @throws Exception if operation fails.
         * @author Ajit
         */

        public void  assignedSelectedTasks (Context context, String[] args) throws Exception
        {
                String languageStr = context.getSession().getLanguage();
        try
        {
                String strSelectedIDs = args[0];
                String sPersonIds = args[1];
                String [] strTaskIdArray = strSelectedIDs.split(",");
                StringList strPersonIds = FrameworkUtil.split(sPersonIds,";");
                
                // [ADDED::PRG:RG6:Jan 13, 2011:IR-075151V6R2012 :R211::start]
                BusinessObjectWithSelectList taskWithSelectList = null;
                BusinessObjectWithSelect bows = null;
                
                if(null != strTaskIdArray && strTaskIdArray.length > 0){
                        
                        DomainObject dObjTask = DomainObject.newInstance(context);
                DomainObject dObjPerson = DomainObject.newInstance(context);
                
                String strAssignStateName = PropertyUtil.getSchemaProperty(context,"policy",DomainConstants.POLICY_PROJECT_TASK,"state_Assign");
                String strCreateStateName = PropertyUtil.getSchemaProperty(context,"policy",DomainConstants.POLICY_PROJECT_TASK,"state_Create");
                final String  SELECT_ASSIGNED_PERSONS_ID = "to["+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from.id";
                MapList assigneesList = null;
                        StringList slAssignOperationSelects = new StringList();
                        slAssignOperationSelects.add(DomainConstants.SELECT_POLICY);
                        slAssignOperationSelects.add(DomainConstants.SELECT_CURRENT);
                        slAssignOperationSelects.add(DomainConstants.SELECT_ID);
                        slAssignOperationSelects.add(SELECT_ASSIGNED_PERSONS_ID);
                                                                                                        
                taskWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strTaskIdArray,slAssignOperationSelects);
                
                for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(taskWithSelectList); itr.next();)
                {
                    bows = itr.obj();
                    String policyName = bows.getSelectData(DomainConstants.SELECT_POLICY);
                    String strTaskStateName = bows.getSelectData(DomainConstants.SELECT_CURRENT);
                    String sTaskObjId = bows.getSelectData(DomainConstants.SELECT_ID);
                    
                    if(ProgramCentralUtil.isNotNullString(sTaskObjId)){
                        dObjTask.setId(sTaskObjId);                        
                        StringList slAssingedPersonObjIds = new StringList();
                        
                        slAssingedPersonObjIds = bows.getSelectDataList(SELECT_ASSIGNED_PERSONS_ID);
                        if(null != strPersonIds){
                    for(int itr1 = 0; itr1 < strPersonIds.size(); itr1++){
                       String strToAssignPersonObjectId = (String) strPersonIds.get(itr1);
                       
                       if(ProgramCentralUtil.isNotNullString(strToAssignPersonObjectId)){
                           
                                   if(null == slAssingedPersonObjIds || !slAssingedPersonObjIds.contains(strToAssignPersonObjectId)){
                                            dObjPerson.setId(strToAssignPersonObjectId);                                        
                            DomainRelationship dmoRel = new DomainRelationship();
                                                        dmoRel.connect(context, dObjPerson,
                                                                        DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, dObjTask);                                                       

                                                        Map mapTaskParam = new HashMap();
                                                        mapTaskParam.put("taskId", sTaskObjId);
                                                        mapTaskParam.put("taskState", strTaskStateName);
                                                        mapTaskParam.put("taskPolicy", policyName);
                                                        
                                                        if (Task.isToMoveTaskInToAssignState(context, mapTaskParam)) {
                                                                dObjTask.setState(context, strAssignStateName);
                                                        }
                                                        
                                                }
                                        }
                                }
                        }
                }
        }

}
// [ADDED::PRG:RG6:Jan 13, 2011:IR-075151V6R2012 :R211::end]        
        } catch (Exception e) {
                System.out.println("Error in  Processing  >> "+e.getMessage());
                                try{                            
                                String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.ActiveTemplateForApproval", "emxProgramCentralStringResource", languageStr);
                                emxContextUtil_mxJPO.mqlNotice(context, strMessage);                               
                                }catch(Exception ex) {
                                }
                e.printStackTrace();
                }
        

                
        }
        // 26/03/2016  | Ajit | For Improvement of Slowness of Assigned Selected mutiple Task -- End
        


	/****************************************************************************************************
     *       PE Phase 2 - Start - 12-Feb-2016
     ****************************************************************************************************/
	 /**Method : taskAttributesEditable
	 * The task is in Active or beyond state, user will not be able to change the Task Type selection
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args    holds ParamMap
	 * @return        StringList
	 * @throws Exception if the operation fails.
	 */
	
	
	public StringList taskAttributesEditable(Context context,String[] args)	throws Exception
	{ 
	try 
		{
			Map programMap =   (Map)JPO.unpackArgs(args);
			Map requestMap=(Map)programMap.get("requestMap");
			Map reqMap=(Map)programMap.get("columnMap");
			String sColumnName = (String)reqMap.get(DomainConstants.SELECT_NAME);
			DomainObject doTask = new DomainObject();
			StringList slObjlist = new StringList();
			MapList mlObjectList = (MapList)programMap.get("objectList");
			
			
			if(null == mlObjectList)
			{
				throw new IllegalArgumentException("Object id List is null");
			}
			String strObjectId = "";
			String strCurrent = "";
			Map map = null;
			int iSize=mlObjectList.size();
			
			MSILProjectSummary_mxJPO msil = new MSILProjectSummary_mxJPO(context, args);
			String strLoggedInUser = context.getUser();
			for(int i=0;i<iSize;i++)
			{
				map = (Map)mlObjectList.get(i);
				strObjectId = (String)map.get(DomainObject.SELECT_ID);
				doTask.setId(strObjectId);
				
				DomainObject doStrObjectId = new DomainObject(strObjectId);
				
				String attrTaskType = (String) doStrObjectId.getInfo(context,"attribute[MSIL Task Type]");
								
				if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
				{		
					StringList slTaskSelect = new StringList(DomainObject.SELECT_CURRENT);
					slTaskSelect.add(DomainObject.SELECT_OWNER);
					Map mtaskDatamap = (Map) doTask.getInfo(context, slTaskSelect);
					
					strCurrent = (String) mtaskDatamap.get(DomainObject.SELECT_CURRENT);
					String strOwner = (String) mtaskDatamap.get(DomainObject.SELECT_OWNER);
					Boolean bMasterProject = msil.checkMasterProject(context,args);
					if (bMasterProject || strOwner.equals(strLoggedInUser)) {
						if (doTask.isKindOf(context, DomainObject.TYPE_PROJECT_MANAGEMENT)) {
							if (strCurrent.equals("Create") || strCurrent.equals("Assign")) {
								// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
								// if(attrTaskType.equals("PO") && (sColumnName.equals("TaskRequirement") ||sColumnName.equals("Complete") ||sColumnName.equals("PhaseEstimatedDuration") ||sColumnName.equals("PhaseEstimatedStartDate") || sColumnName.equals("PhaseEstimatedEndDate") || sColumnName.equals("PhaseActualStartDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("State"))){
								if ((attrTaskType.equals("PO") || attrTaskType.equals("Ringi")) && (sColumnName.equals("TaskRequirement") || sColumnName.equals("Complete") || sColumnName.equals("PhaseEstimatedDuration") || sColumnName.equals("PhaseEstimatedStartDate") || sColumnName.equals("PhaseEstimatedEndDate") || sColumnName.equals("PhaseActualStartDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("State"))) {
								// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
								  slObjlist.add("false");
								}
								else {
								slObjlist.add("true");
								}
								//slObjlist.add(true);
							}
							else 
							{
								slObjlist.add("false");
							}
						}				 
						else slObjlist.add("false");
					}
					else 
					slObjlist.add("false");
				}
			}
		return slObjlist;
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;  
		}	 
	}
	
	 /**Method : msilTaskAttributesEditable
	 * The task is in Active or beyond state, user will not be able to change the Task Type selection
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args    holds ParamMap
	 * @return        StringList
	 * @throws Exception if the operation fails.
	 */
		public StringList msilTaskAttributesEditable(Context context,String[] args)	throws Exception
	{ 
	try 
		{
		    com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			Map programMap =   (Map)JPO.unpackArgs(args);
			Map requestMap=(Map)programMap.get("requestMap");
			Map reqMap=(Map)programMap.get("columnMap");
			String sColumnName = (String)reqMap.get(DomainConstants.SELECT_NAME);
			DomainObject doTask = new DomainObject();
			StringList slObjlist = new StringList();
			MapList mlObjectList = (MapList)programMap.get("objectList");
						
			if(null == mlObjectList)
			{
				throw new IllegalArgumentException("Object id List is null");
			}
			String strObjectId = "";
			String strCurrent = "";
			String strAttrTaskType = "";
			
			Map map = null;
			int iSize=mlObjectList.size();
			
			MSILProjectSummary_mxJPO msil = new MSILProjectSummary_mxJPO(context, args);
			String strLoggedInUser = context.getUser();
			
			StringList slTaskSelect = new StringList(DomainObject.SELECT_CURRENT);
			slTaskSelect.add(DomainObject.SELECT_OWNER);
			slTaskSelect.add(DomainObject.SELECT_ID);
			slTaskSelect.add(DomainObject.SELECT_TYPE);
						
            slTaskSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
            slTaskSelect.add("to[Assigned Tasks].from.name");

            StringList slParentSelect = new StringList(DomainObject.SELECT_CURRENT);
            slParentSelect.add(DomainObject.SELECT_OWNER);
            slParentSelect.add(DomainObject.SELECT_TYPE);
			String strMasterProjectOwner = "";
			String strMasterProjectId = "";
			String strObjectType = "";
			String strParentObjectOwner = "";
			StringList slOwnerList = new StringList();
			
			MapList mlParentList = new MapList();
			int iParentListSize = 0;
            Map mtaskDatamap = null;
            Map mpTemp  =   null;
            String strOwner = "";
            Boolean bMasterProject =null;
			for(int i=0;i<iSize;i++)
			{
				map = (Map)mlObjectList.get(i);
				strObjectId = (String)map.get(DomainObject.SELECT_ID);																				
				if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
				{					
                    //doTask.setId(strObjectId);	
                    task.setId(strObjectId);
                    //strAttrTaskType = (String) task.getInfo(context,${CLASS:MSILConstants}.SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
                    mtaskDatamap    = (Map) task.getInfo(context, slTaskSelect);
                    strAttrTaskType = (String)mtaskDatamap.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_TASK_TYPE);

                    StringList slAssignedList   = mtaskDatamap.get("to[Assigned Tasks].from.name")  instanceof String ? new StringList((String)mtaskDatamap.get("to[Assigned Tasks].from.name")) :(StringList) mtaskDatamap.get("to[Assigned Tasks].from.name");

                    strCurrent = (String) mtaskDatamap.get(DomainObject.SELECT_CURRENT);
                    strOwner = (String) mtaskDatamap.get(DomainObject.SELECT_OWNER);
                    bMasterProject = msil.checkMasterProject(context,args);

                    //Getting the Parent Dept Project/Master Project Owners..
                    mlParentList = task.getParentInfo(context, 0, slParentSelect);
                    iParentListSize = mlParentList.size();
                    for(int iParentListCount=0;iParentListCount<iParentListSize;iParentListCount++)
                    {
                        mpTemp = (Map) mlParentList.get(iParentListCount);
                        strObjectType = (String) mpTemp.get(DomainObject.SELECT_TYPE);
                        if(strObjectType.equals(ProgramCentralConstants.TYPE_PROJECT_SPACE) || strObjectType.equals(ProgramCentralConstants.TYPE_PROJECT_CONCEPT) )
                        {
                            strParentObjectOwner = (String) mpTemp.get(DomainObject.SELECT_OWNER);
                            slOwnerList.add(strParentObjectOwner);
                        }
                        
                    }
					if (strOwner.equals(strLoggedInUser) || slOwnerList.contains(strLoggedInUser)) {
						if (task.isKindOf(context, DomainObject.TYPE_TASK_MANAGEMENT)) {
							if (strCurrent.equals("Create") || strCurrent.equals("Assign")) {
								// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
								// if(strAttrTaskType.equals("PO") && (sColumnName.equals("PartNo") || sColumnName.equals("TaskRequirement") || sColumnName.equals("Complete") || sColumnName.equals("PhaseEstimatedDuration") ||sColumnName.equals("PhaseEstimatedStartDate") || sColumnName.equals("PhaseEstimatedEndDate") || sColumnName.equals("PhaseActualStartDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("State"))){
								if (strAttrTaskType.equals("PO") && (sColumnName.equals("RingiNos") || sColumnName.equals("PartNo") || sColumnName.equals("TaskRequirement") || sColumnName.equals("Complete") || sColumnName.equals("PhaseEstimatedDuration") || sColumnName.equals("PhaseEstimatedStartDate") || sColumnName.equals("PhaseEstimatedEndDate") || sColumnName.equals("PhaseActualStartDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("PhaseActualEndDate") || sColumnName.equals("State"))) {
                                  slObjlist.add("false");
                                }
								// else if(strAttrTaskType.equals("Sourcing") &&  (sColumnName.equals("PONo")))
								else if (strAttrTaskType.equals("Sourcing")
										&& (sColumnName.equals("RingiNos") || sColumnName.equals("PONo"))) {
                                    slObjlist.add("false");
                                }
								// else if(strAttrTaskType.equals("Gate Schedule") && (sColumnName.equals("PONo") || sColumnName.equals("PartNo")))
								else if (strAttrTaskType.equals("Gate Schedule") && (sColumnName.equals("PONo")
										|| sColumnName.equals("PartNo") || sColumnName.equals("RingiNos"))) {
									slObjlist.add("false");
								} else if (strAttrTaskType.equals("Ringi")
										&& (sColumnName.equals("PONo") || sColumnName.equals("PartNo"))) {
                                    slObjlist.add("false");
                                }
                                else {
                                    slObjlist.add("true");
                                }
                                //slObjlist.add(true);
                            }
                            else 
                            {
                                slObjlist.add("false");
                            }
                        }
                        else{ 
                            slObjlist.add("false");
                        }
                    }
					// else if(slAssignedList!=null && slAssignedList.size()>0 && slAssignedList.contains(strLoggedInUser) && (sColumnName.equals("PONo") || sColumnName.equals("PartNo") ) )
					else if (slAssignedList != null && slAssignedList.size() > 0 && slAssignedList.contains(strLoggedInUser) && (sColumnName.equals("PONo") || sColumnName.equals("PartNo") || sColumnName.equals("RingiNos"))) {
						if (strAttrTaskType.equals("PO") && (sColumnName.equals("PONo"))) {
                            slObjlist.add("true");
                         }
						if (strAttrTaskType.equals("Sourcing") && (sColumnName.equals("PartNo"))) {
							slObjlist.add("true");
						} else if (strAttrTaskType.equals("Ringi") && (sColumnName.equals("RingiNos"))) {
                            slObjlist.add("true");
                         }
						// else if(strAttrTaskType.equals("Gate Schedule") && (sColumnName.equals("PONo") || sColumnName.equals("PartNo")))
						else if (strAttrTaskType.equals("Gate Schedule") && (sColumnName.equals("PONo")
								|| sColumnName.equals("PartNo") || sColumnName.equals("RingiNos"))) {
                            slObjlist.add("false");
						} else {
                           slObjlist.add("false");
                        }
						// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
					} else {
                    slObjlist.add("false");
                    }
                }
            }
        return slObjlist;
        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;  
        }
	}

	/**Method : checkDeliverableConnectedToTask
	 * if deliverable are mandatory ,it should have document attached to it
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args    holds objectId
	 * @return        integer
	 * @throws Exception if the operation fails.
	 */

	public int checkDeliverableConnectedToTask (Context context, String[] args)throws Exception{
		int status = 0;
		try {
			// get values from args.
			String strObjectId = args[0];
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domTask = DomainObject.newInstance(context,strObjectId);
				String strAttributeValue = domTask.getAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_DELIVERABLES);
				String strTaskType = domTask.getAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_TASK_TYPE);
				if("" != strAttributeValue && null != strAttributeValue && strAttributeValue.equals("Mandatory"))
				{
					String SELECT_DELIVERABLE = "attribute[" + MSILConstants_mxJPO.ATTRIBUTE_MSIL_DELIVERABLES + "]";
					StringList slBusSelects = new StringList();
					slBusSelects.addElement(SELECT_DELIVERABLE);
					slBusSelects.addElement(DomainConstants.SELECT_ID);

					MapList mlAttribute = new MapList();
					String strLanguage = context.getLocale().getLanguage();
					String strErrMsg = i18nNow.getI18nString("emxProgramCentral.ErrorMessage","emxProgramCentralStringResource", strLanguage);
					if(null != strObjectId){
						mlAttribute=domTask.getRelatedObjects(context,DomainRelationship.RELATIONSHIP_TASK_DELIVERABLE,DomainConstants.TYPE_DOCUMENT,slBusSelects,null,false,true,(short)1,null,null);
						if(null!= mlAttribute)
						{
							int iSize = mlAttribute.size();
							if(iSize == 0){
								status=1;
								MqlUtil.mqlCommand(context, "notice " + strErrMsg);
							}
						}
					}
				}
				//If the attribute MSIL Task Type value is PO then the user should not be able to promote the task to Review.
				if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strTaskType))
				{
					if(strTaskType.equalsIgnoreCase("PO"))
					{
						//status=1;
						//String strErrMsg = "Promotion will happen with the status, fetched from ERP system.";
						//MqlUtil.mqlCommand(context, "notice " + strErrMsg);
					}
				}
			}
			return status;
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}

	/**Method : modifyCriticalTaskValue
	 * Compare ootb Critical task and MSIL critical task and set the value for MSIL Critical task attribute
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args    holds objectId
	 * @return        integer
	 * @throws Exception if the operation fails.
	 */
	public int modifyCriticalTaskValue(Context context, String[] args)throws Exception{
		try {
			String strObjectId = args[0];
			int iStatus = 0;
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domTask = DomainObject.newInstance(context,strObjectId);
				String strCriticalTask = domTask.getAttributeValue(context,ATTRIBUTE_CRITICAL_TASK);
				if(strCriticalTask.equals("TRUE"))
				{
					domTask.setAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_CRITICAL_TASK,"Yes");
				}
			}
			return iStatus;

		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	/**
	 * Where : Task/ProjectSpace/ProjectConcept Name at WBS StrctureBrowser Table View Column
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectList"
	 *        1 - String containing "paramList"
	 * @returns Vector
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */
// Code commented to merge FP1626 code - 18 Jul 2016
	/*public Vector getNameColumn (Context context, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String exportFormat = (String)paramList.get("exportFormat");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}

		//
		// Find all the required infomration on each of the tasks here
		//
		String[] strObjectIds = new String[objectList.size()];
		int size = objectList.size();
		for (int i = 0; i < size; i++) {
			Map mapObject = (Map) objectList.get(i);
			String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
			strObjectIds[i] = taskId;
		}

		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_TYPE);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		slBusSelect.add(DomainConstants.SELECT_CURRENT);
		slBusSelect.add(DomainConstants.SELECT_POLICY);
		slBusSelect.add(SELECT_TASK_PROJECT_ID);
		slBusSelect.add(SELECT_TASK_PROJECT_TYPE);
		slBusSelect.add(SELECT_IS_DELETED_SUBTASK);
		slBusSelect.add(SELECT_ATTRIBUTE_CRITICAL_TASK);
		// MSIL CHANGES START BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
		slBusSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
		// MSIL CHANGES END BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
		//Added:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
		String IS_SUMMARY_TASK = "from["+DomainRelationship.RELATIONSHIP_SUBTASK+"].to.id";
		slBusSelect.add(DomainConstants.SELECT_DESCRIPTION);
		slBusSelect.add(IS_SUMMARY_TASK);
		//End:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
		Map mapTaskInfo = new HashMap();
		BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
		for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
			BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

			Map mapTask = new HashMap();
			for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
				String strSelectable = (String)itrSelectables.next();
				mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
			}

			mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
		}

		//
		// No proceed with the logic of generating column values
		//
		Iterator objectListIterator = objectList.iterator();
		Vector columnValues = new Vector(objectList.size());
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
			String taskLevel = (String) objectMap.get(DomainObject.SELECT_LEVEL);

			Map objectInfo = (Map)mapTaskInfo.get(taskId);
			DomainObject taskObj  = DomainObject.newInstance(context, taskId);
			boolean blDeletedTask = false;

			String strName = (String)objectInfo.get(SELECT_NAME);
			//Added:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			String strDescription = (String)objectInfo.get(SELECT_DESCRIPTION);
			strDescription = " - "+strDescription;
			String strChildTasIds = (String) objectInfo.get(IS_SUMMARY_TASK);
			String strStrong = "";
			if(!strChildTasIds.isEmpty()){
				strStrong = "font-weight: bold";
			}
			//End:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			String taskObjType = (String)objectInfo.get(SELECT_TYPE);;
			if(taskObjType.equalsIgnoreCase(DomainConstants.TYPE_PERSON)){
				strName = (String)objectMap.get(person.SELECT_LAST_NAME)+","+(String)objectMap.get(person.SELECT_FIRST_NAME);
			}
			//Added for special character.
			//strName = FrameworkUtil.findAndReplace(strName,"&","&amp;");
			String strNameandDesc = strName+strDescription;
			//Added for special character.
			//strNameandDesc = FrameworkUtil.findAndReplace(strNameandDesc,"\"", "&quot;");

			if("TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_DELETED_SUBTASK))) {
				blDeletedTask = true;
			}

			String critcalTask = (String)objectInfo.get(SELECT_ATTRIBUTE_CRITICAL_TASK);
			// MSIL CHANGES START BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
			String strMSILCritcalTask = (String)objectInfo.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
			// MSIL CHANGES END BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
			String sState = (String)objectInfo.get(SELECT_CURRENT);
			//Added:02-Jun-10:nr2:R210:PRG:For Project Gate Highlight
			String taskPolicy =  (String)objectInfo.get(SELECT_POLICY);
			boolean isPolicyProjectReview = false;
			if(ProgramCentralConstants.TYPE_GATE.equals(taskObjType) && (ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy))){
				isPolicyProjectReview = true;
			}
			//End:02-Jun-10:nr2:R210:PRG:For Project Gate Highlight
			StringBuffer sBuff = new StringBuffer();
			//Added:09-May-09:nr2:R207:PRG:Bug :371521
			//Check if the task comes from Project Template
			String parentId = null;
			String menuLink = "";
			boolean fromProjTemp = false;
			parentId = (String)objectInfo.get(SELECT_TASK_PROJECT_ID);
			if(parentId!=null && !"".equals(parentId)){
				if(((String)objectInfo.get(SELECT_TASK_PROJECT_TYPE)).equals(DomainConstants.TYPE_PROJECT_TEMPLATE)){
					menuLink = "&amp;treeMenu=type_TaskTemplate";
					fromProjTemp = true;
				}
			}
			//End:R207:PRG:Bug :371521
			//  Display critical path only for not completed
			//Added for special character.
			// MSIL - EXPORT TO EXCEL - START
			String strTaskName = " "+strName;
			// MSIL - EXPORT TO EXCEL - END
			strName = XSSUtil.encodeForXML(context, strName);
			strNameandDesc = XSSUtil.encodeForXML(context, strNameandDesc);
			if (blDeletedTask){
				if("CSV".equalsIgnoreCase(exportFormat)){
					// MSIL - EXPORT TO EXCEL - START
					//sBuff.append(strName);
					sBuff.append(strTaskName);
					// MSIL - EXPORT TO EXCEL - END
				}
				else {
					sBuff.append("<font color='red'>");
					sBuff.append(strName);
					sBuff.append("</font>");
				}
				//Checks even for MSIL CRITICAL TASK ATTRIBUTE VALIE
			}else if( critcalTask!=null && sState!=null && (strMSILCritcalTask!=null && sState!=null) && 
					critcalTask.equalsIgnoreCase("true")|| strMSILCritcalTask.equalsIgnoreCase("Yes") &&
					!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
				if(!isPrinterFriendly){
					sBuff.append("<a href ='javascript:showModalDialog(\"");
					sBuff.append("../common/emxTree.jsp?objectId=");
					sBuff.append(taskId);
					//Added:09-May-09:nr2:R207:PRG:Bug :371521
					if(fromProjTemp && !isPolicyProjectReview){
						sBuff.append(menuLink);
					}
					//End:R207:PRG:Bug :371521
					//<!--Modified for the Bug No: 349125 0 02/06/2008 Start-- >
					sBuff.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+strNameandDesc+"\" style=\"color:red;"+strStrong+"\">");
					//<!--Modified for the Bug No: 349125 0 02/06/2008 End-- >
					sBuff.append(strName);
				}
				else {
					if("CSV".equalsIgnoreCase(exportFormat)){
						// MSIL - EXPORT TO EXCEL - START
						//sBuff.append(strName);
						sBuff.append(strTaskName);
						// MSIL - EXPORT TO EXCEL - END
					}
					else {
						sBuff.append("<font color='red'>");
						sBuff.append(strName);
						sBuff.append("</font>");
					}
				}

				if(!isPrinterFriendly){
					sBuff.append("</a>");
				}
			}
			else{
				if(!isPrinterFriendly){
					sBuff.append("<a href ='javascript:showModalDialog(\"");
					sBuff.append("../common/emxTree.jsp?objectId=");
					sBuff.append(taskId);
					//Added:09-May-09:nr2:R207:PRG:Bug :371521
					if(fromProjTemp && !isPolicyProjectReview){
						sBuff.append(menuLink);
					}
					//End:R207:PRG:Bug :371521
					//<!--Modified for the Bug No: 349125 0 02/06/2008 Start-- >
					sBuff.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+strNameandDesc+"\" style=\""+strStrong+"\">");
					//<!--Modified for the Bug No: 349125 0 02/06/2008 End-- >
					// MSIL - EXPORT TO EXCEL - START
					sBuff.append(strName);
					// MSIL - EXPORT TO EXCEL - END
				}
				// MSIL - EXPORT TO EXCEL - START
				//sBuff.append(strName);
				else
					sBuff.append(strTaskName);
				// MSIL - EXPORT TO EXCEL - END
				if(!isPrinterFriendly){
					sBuff.append("</a>");
				}
			}
			columnValues.add(sBuff.toString());
		}
		return columnValues;
	}*/
	
	/**
	 * Where : Task/ProjectSpace/ProjectConcept Name at WBS StrctureBrowser Table View Column
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectList"
	 *        1 - String containing "paramList"
	 * @returns Vector
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public Vector getNameColumn (Context context, String[] args) throws Exception
	{
		return getNameColumnData(context,FROM_PROJECT_WBS,args);
	}

	public Vector getProjectTemplateWBSNameColumn (Context context, String[] args) throws Exception
	{
		return getNameColumnData(context,FROM_PROJECT_TEMPLATE_WBS,args);
	}
	
		public Vector getNameColumnData (Context context,String mode, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String exportFormat = (String)paramList.get("exportFormat");
		//boolean isMobile = UINavigatorUtil.isMobile(context);
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}
		String menuLink = DomainConstants.EMPTY_STRING;
		boolean fromProjTemp = false;
		if(FROM_PROJECT_TEMPLATE_WBS.equals(mode)) {
			menuLink = "&amp;treeMenu=type_TaskTemplate";
			fromProjTemp = true;
		}

        	//
        	// Find all the required infomration on each of the tasks here
        	//
        	String[] strObjectIds = new String[objectList.size()];
        	int size = objectList.size();
        	for (int i = 0; i < size; i++) {
        		Map mapObject = (Map) objectList.get(i);
        		String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
        		strObjectIds[i] = taskId;
        	}

		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_TYPE);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		slBusSelect.add(DomainConstants.SELECT_CURRENT);
		slBusSelect.add(DomainConstants.SELECT_POLICY);
		slBusSelect.add(SELECT_IS_DELETED_SUBTASK);
		slBusSelect.add(SELECT_ATTRIBUTE_CRITICAL_TASK);
		slBusSelect.add(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT);
		slBusSelect.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		slBusSelect.add(Person.SELECT_LAST_NAME);
		slBusSelect.add(Person.SELECT_FIRST_NAME);

		// MSIL CHANGES START BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
		slBusSelect.add(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
		// MSIL CHANGES END BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
		String IS_SUMMARY_TASK = "from["+DomainRelationship.RELATIONSHIP_SUBTASK+"].to.id";
		slBusSelect.add(DomainConstants.SELECT_DESCRIPTION);
		slBusSelect.add(IS_SUMMARY_TASK);
		slBusSelect.add(SELECT_IS_PARENT_TASK_DELETED);
		Map mapTaskInfo = new LinkedHashMap();
		BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
		for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
			BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

        		Map mapTask = new HashMap();
        		for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
        			String strSelectable = (String)itrSelectables.next();
        			mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
        		}

        		mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
        	}

        	//
        	// No proceed with the logic of generating column values
        	//
        	Vector columnValues = new Vector(objectList.size());
            /*Iterator objectListIterator = objectList.iterator();
            
		//Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

            while (objectListIterator.hasNext())
            {
                Map objectMap = (Map) objectListIterator.next();
                String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
                String taskLevel = (String) objectMap.get(DomainObject.SELECT_LEVEL);*/
        	  Iterator objectIdItr = mapTaskInfo.keySet().iterator();
              while(objectIdItr.hasNext()){
                		
               	String taskId = (String)objectIdItr.next();
        		Map objectInfo = (Map)mapTaskInfo.get(taskId);
           
                 boolean blDeletedTask = false;

			String strName = (String)objectInfo.get(SELECT_NAME);
			//Added:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			String strDescription = (String)objectInfo.get(SELECT_DESCRIPTION);
			strDescription = " - "+strDescription;
			String strChildTasIds = (String) objectInfo.get(IS_SUMMARY_TASK);
			String strStrong = "";
			if(!strChildTasIds.isEmpty()){
				strStrong = "font-weight: bold";
			}
			//End:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			boolean isPersonType = false;
			String taskObjType = (String)objectInfo.get(SELECT_TYPE);
			if(taskObjType.equalsIgnoreCase(DomainConstants.TYPE_PERSON)){
				strName = (String)objectInfo.get(Person.SELECT_LAST_NAME)+","+(String)objectInfo.get(Person.SELECT_FIRST_NAME);
				isPersonType = true;
			}
			String strNameandDesc = strName+strDescription;

			if("TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_DELETED_SUBTASK)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_PARENT_TASK_DELETED))) {
				blDeletedTask = true;
			}

        		String critcalTask = (String)objectInfo.get(SELECT_ATTRIBUTE_CRITICAL_TASK);
				// MSIL CHANGES START BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
			String strMSILCritcalTask = (String)objectInfo.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
			// MSIL CHANGES END BY INTELIZIGN - 12-Feb-2016 :add MSIL Critical task value
			String sState = (String)objectInfo.get(SELECT_CURRENT);
			String taskPolicy =  (String)objectInfo.get(SELECT_POLICY);
			boolean isPolicyProjectReview = false;
			if(ProgramCentralConstants.TYPE_GATE.equals(taskObjType) && (ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy))){
				isPolicyProjectReview = true;
			}
			StringBuffer sBuff = new StringBuffer();
			//Check if the task comes from Project Template

		/*	if(ProgramCentralUtil.isNullString(exportFormat)) {
				if(isMobile){
				strName = XSSUtil.encodeForHTML(context, strName);
				strNameandDesc = XSSUtil.encodeForHTML(context, strNameandDesc);
				} else {
				strName = XSSUtil.encodeForXML(context, strName);
				strNameandDesc = XSSUtil.encodeForXML(context, strNameandDesc);
				}
			}*/
			if(ProgramCentralUtil.isNullString(exportFormat)) {
				strName = XSSUtil.encodeForXML(context, strName);
				strNameandDesc = XSSUtil.encodeForXML(context, strNameandDesc);
			}

			boolean isExperimentTask = "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT));
			boolean isProjectBaselineTask = "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE));
			//Added for "What If" functionality to hide hyperlink from Experiment project and task:start
			if(!(isExperimentTask || isProjectBaselineTask)){
				if (blDeletedTask){
					if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}
					else {
						sBuff.append("<font color='red'>");
						sBuff.append(strName);
						sBuff.append("</font>");
					}
					//Checks even for MSIL CRITICAL TASK ATTRIBUTE VALIE
        		/*}else if( critcalTask!=null && sState!=null &&
                        critcalTask.equalsIgnoreCase("true") &&
                        !sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {*/
				}else if( critcalTask!=null && sState!=null && (strMSILCritcalTask!=null && sState!=null) && 
					critcalTask.equalsIgnoreCase("true")|| strMSILCritcalTask.equalsIgnoreCase("Yes") &&
						!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
					if(!isPrinterFriendly){
						if (isPersonType) { // if block added to show person object in content page as per Widgetization HL.
							sBuff.append("<a href ='../common/emxTree.jsp?objectId=").append(XSSUtil.encodeForURL(context,taskId));
							sBuff.append("' title=\"").append(strNameandDesc);
							sBuff.append("\" style=\"color:red;").append(strStrong).append("\">");
							sBuff.append(strName);
						} else {
							sBuff.append("<a href ='javascript:showModalDialog(\"");
							sBuff.append("../common/emxTree.jsp?objectId=");
							sBuff.append(XSSUtil.encodeForURL(context,taskId));
							if(fromProjTemp){
							sBuff.append("&amp;headerLifecycle=hide");
							}
							if(fromProjTemp && !isPolicyProjectReview && !"True".equalsIgnoreCase((String) objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE))){
								sBuff.append(menuLink);
							}
							sBuff.append("\", \"812\", \"700\",\"true\",\"popup\",\"Medium\")' title=\""+strNameandDesc+"\" style=\"color:red;"+strStrong+"\">");
							sBuff.append(strName);
						}
						sBuff.append("</a>");
					}
					else {
						if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
							sBuff.append(strName);
						}
						else {
							sBuff.append("<font color='red'>");
							sBuff.append(strName);
							sBuff.append("</font>");
						}
					}
				}
				else{
					if(!isPrinterFriendly){
						if (isPersonType) { // if block added to show person object in content page as per Widgetization HL.
							sBuff.append("<a href ='../common/emxTree.jsp?objectId=").append(XSSUtil.encodeForURL(context,taskId));
							sBuff.append("' title=\"").append(strNameandDesc);
							sBuff.append("\" style=\"").append(strStrong).append("\">");
						} else {
							sBuff.append("<a href ='javascript:showModalDialog(\"");
							sBuff.append("../common/emxTree.jsp?objectId=");
							sBuff.append(XSSUtil.encodeForURL(context,taskId));
							if(fromProjTemp){
								sBuff.append("&amp;headerLifecycle=hide");
								}
							if(fromProjTemp && !isPolicyProjectReview && !"True".equalsIgnoreCase((String) objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE))){
								sBuff.append(menuLink);
							}
							sBuff.append("\", \"812\", \"700\",\"true\",\"popup\",\"Medium\")' title=\""+strNameandDesc+"\" style=\""+strStrong+"\">");
						}
					}
					sBuff.append(strName);
					if(!isPrinterFriendly){
						sBuff.append("</a>");
					}
				}
			}else{
				if(critcalTask!=null && sState!=null &&
						critcalTask.equalsIgnoreCase("true") &&
						!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
					if("CSV".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}else{
						sBuff.append("<p style=\"color:red;"+strStrong+"\"  title=\""+strNameandDesc+"\">");
						sBuff.append(strName);
						sBuff.append("</p>");
					}
				}else{
					if("CSV".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}else{
						sBuff.append("<p style=\""+strStrong+"\" title=\""+strNameandDesc+"\">");
						sBuff.append(strName);
						sBuff.append("</p>");
					}
				}
			}
			columnValues.add(sBuff.toString());
		}
		return columnValues;
	}
	/**
	 * Overriding this method inorder to paste the relationship connected to the new instance that is getting pasted	
     * cutPasteTasksInWBS - This Method will move the task in the WBS SB from
     *   the Edit menu.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments:
     * @return Void
     * @throws Exception if the operation fails
     * @since PMC V6R2008-2.0
     */
	
	 public HashMap cutPasteTasksInWBS(Context context, String[] args) throws Exception // changing the return type void To HashMap
    {
        HashMap returnHashMap = new HashMap(); // To be return from this method
        MapList mlItems=new MapList(); // To store the key "changedRows"

        // unpack the incoming arguments into a HashMap called 'programMap'
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        com.matrixone.jdom.Element rootElement = null;
        rootElement = (com.matrixone.jdom.Element)programMap.get("contextData");
        String sObjectId = null;
        String sRelId = null;
        String sRowId = null;
        String sRelType  = null;
        String markup    = null;
        String spasteAbove    = null;
        String pasteAtId = null;
        HashMap attribMap;
        StringList objectList = null;
        String nextSequence = null;
        String nextWBS = null;
        Task task = null;
        Map seqMap = null;
        DomainRelationship domRel = null;
        String notice="";
        // [ADDED::PRG:RG6:Jun 14, 2011:IR-109118V6R2012x:R212::Start]
        Map mParamMap = (Map)programMap.get("paramMap");
		
        String sRootTreeId = (String)mParamMap.get("objectId");
        String sParentOID = (String)rootElement.getAttributeValue("objectId");
     // [ADDED::PRG:RG6:Jun 14, 2011:IR-109118V6R2012x:R212::End]

        //[ADDED::VM3:09-12-2010:IR-073634::Start]
        Map errorMap = new HashMap();
        Map mValidityTestBasedOnObjectId = new HashMap();
        int validCutPasteOpeation = 0; //0 = validOperation, 1 = InvalidOpearion
        int stopSearch = 0; //If 1 stop searching
        com.matrixone.jdom.Element rootParent = rootElement.getParentElement();
        if(null!=rootParent)
        {
        java.util.List childList = rootParent.getChildren();
        for(int i=0;(i<childList.size() && stopSearch == 0);i++){
        	com.matrixone.jdom.Element e = (com.matrixone.jdom.Element) childList.get(i);
        	java.util.List ccElementList = e.getChildren();
        	for(int j=0;j<ccElementList.size();j++){
        		com.matrixone.jdom.Element ccElement  = (com.matrixone.jdom.Element) ccElementList.get(j);
        		String operation = ccElement.getAttributeValue("markup");
        		String objId = ccElement.getAttributeValue("objectId");
        		if(null == operation || "null".equalsIgnoreCase(operation)){
        			stopSearch = 1;
        			break;
        		}
        		else if("cut".equalsIgnoreCase(operation)){
        			validCutPasteOpeation++;
        			if(mValidityTestBasedOnObjectId.containsKey(objId)){
        				Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
        				mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))+1);

        			}
        			mValidityTestBasedOnObjectId.put(objId,1);
        		}
        		else if("add".equalsIgnoreCase(operation)){
        			if(mValidityTestBasedOnObjectId.containsKey(objId)){
        				Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
        				mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))-1);
        			}
        			validCutPasteOpeation--;
        		}
        		else{
        			validCutPasteOpeation = 0;
        		}
            	//System.out.println("ChildElement = " + i + " Child-Child Element = " + j + " operation >> " + operation + " objId = " + objId + " ObjectId = " + sParentOID);

        	}
        }
        }
      //[ADDED::VM3:09-12-2010:IR-073634::End]

   	 	//[ADDED::NR2:01-12-2010:HF-067541V6R2010x_::Start]
			String strLastOperation = (String)rootElement.getAttributeValue("lastOperation");
   	 	//[ADDED::NR2:01-12-2010:HF-067541V6R2010x_::End]

		Task taskObject = new Task();
        java.util.List lCElement     = rootElement.getChildren();
        StringList slDependencyTaskNameList = new StringList(); 

        if(lCElement != null){
            java.util.Iterator itrC  = lCElement.iterator();
             while(itrC.hasNext()){
                 com.matrixone.jdom.Element childCElement = (com.matrixone.jdom.Element)itrC.next();
                 sObjectId = (String)childCElement.getAttributeValue("objectId");
                 sRelId = (String)childCElement.getAttributeValue("relId");
                 sRowId = (String)childCElement.getAttributeValue("rowId");
                 markup    = (String)childCElement.getAttributeValue("markup");
                 spasteAbove    = (String)childCElement.getAttributeValue("paste-above");
                 pasteAtId = null;
                //rg6
				
                task = new Task(sObjectId);
                StringList sList = new StringList();
      	    	sList.add(Task.SELECT_CURRENT);
				// Added for MSIL phase2 - 12-Feb-2016 - Start
				sList.add("to[MSIL Milestone Task].from.id");
				// Added for MSIL phase2 - 12-Feb-2016 - End
      	    	Map mapTaskInfo = task.getInfo(context, sList);
				
      	    	String strTaskState = (String)mapTaskInfo.get(DomainConstants.SELECT_CURRENT);
				//Added for MSIL phase2 for getting the milestone connected to the selected task - 12-Feb-2016 - start
				String strMilestoneConnected = (String)mapTaskInfo.get("to["+MSILConstants_mxJPO.REL_MSIL_MILESTONE_TASK+"].from.id");
				//Added for MSIL phase2 for getting the milestone connected to the selected task - 12-Feb-2016 - End
               //[ADDED::VM3:09-12-2010:IR-073634::Start]
                 Integer value = (Integer) mValidityTestBasedOnObjectId.get(sObjectId);
                 if(null!= value && Integer.valueOf(value)>0){
                	 errorMap.put("validCutPasteOpeation", validCutPasteOpeation);
                	 errorMap.put("objectId",sObjectId);
                	 continue;
                 }
               //[ADDED::VM3:09-12-2010:IR-073634::End]

                 if (spasteAbove != null){
                    objectList = FrameworkUtil.split(spasteAbove,"|");
                    pasteAtId = (String)objectList.elementAt(0) ;
                 }

                 if ("resequence".equals(markup))
                 {
                	 // Added:5-Oct-2010:PRG:RG6:R211:IR-073001V6R2012
                	 if(!DomainConstants.STATE_PROJECT_SPACE_CREATE.equalsIgnoreCase(strTaskState)){
                		 notice = i18nNow.getI18nString("emxProgramCentral.cutTask.CutOperationNotice",
                				 "emxProgramCentralStringResource",
                				 context.getSession().getLanguage());
                		 returnHashMap.put("Message", notice);
                		 returnHashMap.put("Action", "ERROR");
                		 return(returnHashMap);
                	 }

                	 if(task.isMandatoryTask(context,sObjectId)){
                		 String strTaskRequirement = (String)task.getAttributeValue(context, ATTRIBUTE_TASK_REQUIREMENT);
                		 if(strTaskRequirement.equalsIgnoreCase("Mandatory"))
                		 {
                			 notice = i18nNow.getI18nString("emxProgramCentral.cutTask.MandatoryTaskNotice",
                					 "emxProgramCentralStringResource",
                					 context.getSession().getLanguage());
                			 returnHashMap.put("Message", notice);
                			 returnHashMap.put("Action", "ERROR");
                			 return(returnHashMap);

                		 }
                	 }
                	 //End Added:5-Oct-2010:PRG:RG6:R211:IR-073001V6R2012
                    task = new Task(sParentOID);
                    seqMap = Task.getNextSequenceInformation(context, task, pasteAtId);
                    nextSequence = (String) seqMap.get(KEY_SEQ_NUMBER);
                    nextWBS = (String) seqMap.get(KEY_WBS_NUMBER);

                    attribMap = new HashMap();
                    attribMap.put(ATTRIBUTE_SEQUENCE_ORDER , nextSequence);
                    attribMap.put(ATTRIBUTE_TASK_WBS , nextWBS);
                    domRel = new DomainRelationship(sRelId);
                    domRel.setAttributeValues(context,attribMap);
                    Task project = new Task(sParentOID);
                    project.reSequence(context, sParentOID);

                    HashMap tempHashMap = new HashMap(); // temp HashMap to store these values
                    tempHashMap.put("oid", sObjectId);
                    tempHashMap.put("rowId", sRowId);
                    tempHashMap.put("relid", sRelId);//new relId
                    tempHashMap.put("markup", markup);
                    mlItems.add(tempHashMap); //Storing in the global MapList mlItems to be added for "changedRows" key

                 }
                 //Modified:29-Mar-10:s4e:R209:PRG:Bug:035773
                 //Modified to restrict single task connection to multiple parents
                 else if ("add".equals(markup))
                 {
                	 Task parentTaskObj = new Task(sParentOID);
                	 StringList slSelect = new StringList();
                	 slSelect.add(DomainConstants.SELECT_CURRENT);
                	 slSelect.add(DomainConstants.SELECT_NAME);
                	 
                	 Map mMapInfo =  parentTaskObj.getInfo(context, slSelect);
                	 String strParentTaskState = (String)mMapInfo.get(DomainConstants.SELECT_CURRENT);
                	 String strParentTaskName = (String)mMapInfo.get(DomainConstants.SELECT_NAME);
                	 
                	 //H1A : Starts : HF-164790V6R2013x
                	 boolean isToshowDependencyMsg = parentTaskObj.isToshowDependencyMessage(context, sParentOID);
                	 if(isToshowDependencyMsg)
                	 {
                		 if(!slDependencyTaskNameList.contains(strParentTaskName))
                		 {
                			 slDependencyTaskNameList.add(strParentTaskName);
                		 }
                	 }
                	 //H1A : Ends : HF-164790V6R2013x
                	 
                	 String strLanguage=context.getSession().getLanguage();
			        	 if("cut".equalsIgnoreCase(strLastOperation))
			        	 {
                	 DomainObject taskDob = DomainObject.newInstance(context,sObjectId);

                	 String strCheckRel = taskDob.getInfo(context,"to["+RELATIONSHIP_SUBTASK+"].id");
                	 String sErrMsg = "";

                	 if(strCheckRel!=null)
                	 {

                		 if(!(TYPE_PROJECT_SPACE.equals(taskDob.getInfo(context,"to["+RELATIONSHIP_SUBTASK+"].type"))))
                		 {
                			 sErrMsg = i18nNow.getI18nString(
                                      "emxProgramCentral.Task.CannotBeAddedToMultipleParents",
                                      "emxProgramCentralStringResource",
                                      strLanguage);
            			         //MqlUtil.mqlCommand(context, "notice " + sErrMsg ); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
            				 //break;
            				 returnHashMap.put("Message",sErrMsg);
            				 returnHashMap.put("Action", "ERROR");
            				 return(returnHashMap);
                		 }

                	 }
                	 //End:29-Mar-10:s4e:R209:PRG:Bug:035773
                	 
                     task = new Task(sParentOID);
                     //Added 18-Aug-2010:PRG:rg6:IR-066837V6R2011x
                   //  String strParentTaskState = task.getInfo(context, DomainConstants.SELECT_CURRENT);
                     if(null != strParentTaskState){
  	                	if(DomainConstants.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(strParentTaskState)
  	                			|| DomainConstants.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(strParentTaskState)
  	                		         ||DomainConstants.STATE_PROJECT_SPACE_ARCHIVE.equalsIgnoreCase(strParentTaskState)
  	                	  ){
  	                		String strErrMsg = i18nNow.getI18nString(
                                    "emxProgramCentral.Project.TaskInState3",
                                    "emxProgramCentralStringResource",
                                    strLanguage);
  	                		returnHashMap.put("Message", strErrMsg);
        	                 	returnHashMap.put("Action", "ERROR");
        	                 	return(returnHashMap);
  	                	}
  	                }
                   //End 18-Aug-2010:PRG:rg6:IR-066837V6R2011x
                     seqMap = Task.getNextSequenceInformation(context, task, pasteAtId);
                     nextSequence = (String) seqMap.get(KEY_SEQ_NUMBER);
                     nextWBS = (String) seqMap.get(KEY_WBS_NUMBER);

                     attribMap = new HashMap();
                     attribMap.put(ATTRIBUTE_SEQUENCE_ORDER , nextSequence);
                     attribMap.put(ATTRIBUTE_TASK_WBS , nextWBS);

                     domRel = DomainRelationship.connect(context, DomainObject.newInstance(context, sParentOID),
                                                         RELATIONSHIP_SUBTASK, DomainObject.newInstance(context, sObjectId));

                     domRel.setAttributeValues(context,attribMap);
                     String newRelId = domRel.getName();

                     // gqh: not necessary as rollup call below takes care of PC.
                     // update parent percent
                     //calculatePercentComplete(context, sParentOID);
                     TaskDateRollup rollup = new TaskDateRollup(sParentOID);
                     rollup.validateTask(context);

                     HashMap tempHashMap = new HashMap(); // temp HashMap to store these values
                     tempHashMap.put("oid", sObjectId);
                     tempHashMap.put("rowId", sRowId);
                     tempHashMap.put("relid", newRelId);//new relId
                     tempHashMap.put("markup", markup);
                     mlItems.add(tempHashMap); //Storing in the global MapList mlItems to be added for "changedRows" key
                 }
			        	 else if("copy".equalsIgnoreCase(strLastOperation))
			        	 {
			        		 Task taskSource =new Task(sObjectId);
							 
			        		 // [ADDED::PRG:RG6:Jun 14, 2011:IR-109118V6R2012x:R212::Start]
			        		 if(ProgramCentralUtil.isNotNullString(sRootTreeId) && checkIfProject(context,sRootTreeId) && sRootTreeId.equals(sObjectId))
			        		 {
			        			 String strErrMsg = i18nNow.getI18nString("emxProgramCentral.Project.WBS.cutPaste.ErrorOnCopyProject","emxProgramCentralStringResource",strLanguage);
			        			 returnHashMap.put("Message", strErrMsg);
			        			 returnHashMap.put("Action", "ERROR");
			        			 return(returnHashMap);
			        		 }
			        		 // [ADDED::PRG:RG6:Jun 14, 2011:IR-109118V6R2012x:R212::End]
			        		 Task taskTargetParent = new Task(sParentOID);
			        		 boolean isCopyingDeliverables=true;

			        		 //String strParentTaskState = taskTargetParent.getInfo(context, DomainConstants.SELECT_CURRENT);
			        		 if(null != strParentTaskState)
			        		 {
			        			 if(DomainConstants.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(strParentTaskState)
			        					 || DomainConstants.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(strParentTaskState)
			        					 ||DomainConstants.STATE_PROJECT_SPACE_ARCHIVE.equalsIgnoreCase(strParentTaskState)
			        			 )
			        			 {
			        				 String strErrMsg = i18nNow.getI18nString("emxProgramCentral.Project.TaskInState3","emxProgramCentralStringResource",context.getSession().getLanguage());
			        				 returnHashMap.put("Message", strErrMsg);
			        				 returnHashMap.put("Action", "ERROR");
			        				 return(returnHashMap);
			        			 }
			        		 }
                		 
			        		 Map copyTaskMap	=	new HashMap();
			        		 taskSource.cloneTaskWithStructure(context, taskTargetParent, null, copyTaskMap, isCopyingDeliverables);
			        		 DomainObject copyTask	=	(DomainObject)copyTaskMap.get(sObjectId);
							 //Added for MSIL phase2 for setting the milestone connected to the selected task to copied or cut task - 12-Feb-2016 - start
			        		 if(null!= strMilestoneConnected)
							 { 
								DomainObject copyMilestone	=  DomainObject.getObject(context,strMilestoneConnected);
								String strDisconnectRelId = copyTask.getInfo(context, "to["+MSILConstants_mxJPO.REL_MSIL_MILESTONE_TASK+"].id");
					            if(null != strDisconnectRelId && !"null".equals(strDisconnectRelId) && strDisconnectRelId.length() > 0)
					            {
					                Relationship disconnectOldRel = new Relationship(strDisconnectRelId);
					                copyTask.disconnect(context,disconnectOldRel);
					            }
								copyTask.connectFrom(context,MSILConstants_mxJPO.REL_MSIL_MILESTONE_TASK,copyMilestone);
							 }
							 //Added for MSIL phase2 for setting the milestone connected to the selected task to copied or cut task  - 12-Feb-2016 - End
							 
			        		 if(null==copyTask)
								{
									String strErrMsg  = i18nNow.getI18nString("emxProgramCentral.WBS.copypaste.project","emxProgramCentralStringResource",context.getSession().getLanguage());
									returnHashMap.put("Message", strErrMsg);
									returnHashMap.put("Action", "ERROR");
									return(returnHashMap);
								}
			        		 
							 //H1A : IR-226891V6R2013x : Starts
							 sObjectId = copyTask.getId(context);
							 //Retrieving the relId for the cloned task starts.
							 DomainObject clonedTask = DomainObject.newInstance(context,sObjectId);
							 sRelId = clonedTask.getInfo(context,"to["+RELATIONSHIP_SUBTASK+"].id");
							 //Generating next sequence no. & WBS starts.
							 seqMap = Task.getNextSequenceInformation(context, taskTargetParent, pasteAtId);
							 nextSequence = (String) seqMap.get(KEY_SEQ_NUMBER);
							 nextWBS = (String) seqMap.get(KEY_WBS_NUMBER);
							 
							 attribMap = new HashMap();
							 attribMap.put(ATTRIBUTE_SEQUENCE_ORDER , nextSequence);
							 attribMap.put(ATTRIBUTE_TASK_WBS , nextWBS);
							 //Generating next sequence no. & WBS ends.
							 
							 //Assigning the Seqence no. & next WBS upon the relationship.
							 domRel = new DomainRelationship(sRelId); //newRelId
							 domRel.setAttributeValues(context,attribMap);
							 
							 taskTargetParent.reSequence(context, sParentOID);
							 
							 taskTargetParent.rollupAndSave(context);

			        		 HashMap tempHashMap = new HashMap(); // temp HashMap to store these values
							 tempHashMap.put("oid", sObjectId); 
							 //H1A : IR-226891V6R2013x : Ends
			        		 tempHashMap.put("rowId", sRowId);
			        		 tempHashMap.put("relid", sRelId);//new relId
			        		 tempHashMap.put("markup", markup);
			        		 mlItems.add(tempHashMap); //Storing in the global MapList mlItems to be added for "changedRows" key
							 
			        	 }
			         }
                 else if ("cut".equals(markup)) {

                         //[ADDED::VM3:09-DEC-2010:IR-073634::START]
                	 String errorMessage = null;
                	 taskObject.setId(sObjectId);
                	 String currentState = (String)taskObject.getInfo(context,SELECT_CURRENT);
                	 if (validCutPasteOpeation>0){
                		 String errorMessageKey = "emxProgramCentral.Common.InvalidCutPasteOperation";
                		 errorMessage	=	emxProgramCentralUtilClass.getMessage(context,errorMessageKey,null,null,null);
                		 returnHashMap.put("Message",errorMessage);
      	                 returnHashMap.put("Action", "ERROR");
       	                 return(returnHashMap);
       	             }
                	 //[ADDED::VM3:03-DEC-2010:IR-073634::END]
                	//Added:30-Sep-09:nr2:R208:PRG Bug:370928
                  	//Check if this object is connected to its parent by
                  	//DomainConstants.RELATIONSHIP_DELETED_SUBTASK
                  	                 StringList sl1 = new StringList(2);
                  	                 sl1.add(DomainConstants.SELECT_NAME);
                  	                 sl1.add(DomainConstants.SELECT_ID);

                  	                 StringList sl2 = new StringList(2);
                  	                 sl2.add(DomainConstants.SELECT_NAME);
                  	                 sl2.add(DomainConstants.SELECT_ID);
                  	                 Map m = DomainObject.newInstance(context, sObjectId).getRelatedObject(context,
                  	                		 DomainConstants.RELATIONSHIP_DELETED_SUBTASK, false,sl1 ,sl2);
                  	               notice = FrameworkProperties.getProperty("emxProgramCentral.cutTask.Notice");
                  	                 if(m!=null){
	                 	                 returnHashMap.put("Message", notice);
	                 	                 returnHashMap.put("Action", "ERROR");
	                  	                 return(returnHashMap);
                 	                   //throw new Exception(notice);
                  	                 }
                  	//End:R208:PRG Bug:370928

                	 task = new Task(sObjectId);
                  	                 //Added:5-Oct-2010:PRG:RG6:R210:IR-068106V6R2011x
         	    	if(!DomainConstants.STATE_PROJECT_SPACE_CREATE.equalsIgnoreCase(strTaskState)){
         	    		notice = i18nNow.getI18nString("emxProgramCentral.cutTask.CutOperationNotice",
           					 "emxProgramCentralStringResource",
           					 context.getSession().getLanguage());
         	    	         returnHashMap.put("Message", notice);
           			 returnHashMap.put("Action", "ERROR");
           			 return(returnHashMap);
         	    	}

                  	                 if(task.isMandatoryTask(context,sObjectId)){
                		 String strTaskRequirement = (String)task.getAttributeValue(context, ATTRIBUTE_TASK_REQUIREMENT);
                		 if(strTaskRequirement.equalsIgnoreCase("Mandatory"))
                		 {
                	 		 notice = i18nNow.getI18nString("emxProgramCentral.cutTask.MandatoryTaskNotice",
                					 "emxProgramCentralStringResource",
                					 context.getSession().getLanguage());
                			 returnHashMap.put("Message", notice);
                			 returnHashMap.put("Action", "ERROR");
                			 return(returnHashMap);

                		 }
                	 }
                  	                 //End Added:5-Oct-2010:PRG:RG6:R210:IR-068106V6R2011x,EnforceMandatoryTasks

                     String relationshipIds[] = new String[1];
                     relationshipIds[0]= sRelId;
                     task.disconnectTasks(context, relationshipIds, sParentOID);
                     // gqh: not necessary as rollup call below takes care of PC.
                     // update parent percent
                     //calculatePercentComplete(context, sParentOID);
                     TaskDateRollup rollup = new TaskDateRollup(sParentOID);
                     rollup.validateTask(context);

                    HashMap tempHashMap = new HashMap(); // temp HashMap to store these values
                    tempHashMap.put("oid", sObjectId);
                    tempHashMap.put("rowId", sRowId);
                    tempHashMap.put("relid", sRelId);//new relId
                    tempHashMap.put("markup", markup);
                    mlItems.add(tempHashMap); //Storing in the global MapList mlItems to be added for "changedRows" key
                 }
             }
           //[ADDED::VM3:09-12-2010:IR-073634::Start]
             if(null != errorMap && errorMap.size()>0){
            	 //Throw error message
            	 String errorMessage = null;
           		 String errorMessageKey = "emxProgramCentral.Common.InvalidCutPasteOperation";
           		 errorMessage	=	emxProgramCentralUtilClass.getMessage(context,errorMessageKey,null,null,null);
           		 returnHashMap.put("Message",errorMessage);
           		 returnHashMap.put("Action", "ERROR");
           		 return(returnHashMap);
             }
           //[ADDED::VM3:09-12-2010:IR-073634::End]
             if(slDependencyTaskNameList.size() > 0)
             {
            	 String sKey[] = {"dependencyType1","dependencyType2"};
                 String sValue[] = {Task.START_TO_FINISH,Task.FINISH_TO_FINISH};
                 String warningKey = emxProgramCentralUtilClass.getMessage(context,"emxProgramCentral.WBS.cutpaste.removeDependency",sKey,sValue,null);
                                  
            	 MqlUtil.mqlCommand(context, "notice "+warningKey+"\\n"+slDependencyTaskNameList.toString()+"");
             }
        }
        returnHashMap.put("changedRows", mlItems);
        returnHashMap.put("Action", "success");
        return(returnHashMap);
    }

	/**
    	 * This Method will return boolean true if object is of type project space,concept,template and false otherwise.
    	 * @param context Matrix Context object
    	 * @param objectId String
    	 * @return boolean
    	 * @throws MatrixException
    	 * @author RG6
    	 */
    	private boolean checkIfProject(Context context,String sObjectId) throws MatrixException
    	{
    		if(ProgramCentralUtil.isNullString(sObjectId))
    		{
    			throw new IllegalArgumentException("Object id is null");
    		}

    		boolean isProject = false;
    		try
    		{
    			StringList slSelectInfo = new StringList();
    			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
    			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
    			DomainObject dObj = DomainObject.newInstance(context);
    			dObj.setId(sObjectId);
    			Map mProjectInfo = dObj.getInfo(context, slSelectInfo);
    			if(null != mProjectInfo)
    			{
    				String sIsProjectConcept = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
    				String sIsProjectSpace = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    				String sIsProjectTemplate = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

    				isProject = "true".equalsIgnoreCase(sIsProjectConcept) || "true".equalsIgnoreCase(sIsProjectSpace) || "true".equalsIgnoreCase(sIsProjectTemplate);
    			}
    		}
    		catch(Exception e)
    		{
    			throw new MatrixException(e);
    		}
    		return isProject;
    	}

    	//Added:MS9:27-07-2011:PRG:R212:IR-118680V6R2012x
		
		
		/**
	 * This Method will return boolean value of the PO Number if task type is PO.
	 * @param context Matrix Context object
	 * @throws MatrixException
	 * @author RG6
	 */

	public boolean getPONumber(Context context, String[] args) throws Exception {	   
	   String strTaskType = "";	   
	    boolean mlResult = false;
        try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
		    String strObjectId = (String) programMap.get("objectId");
			DomainObject doProject = new DomainObject(strObjectId);
			if(! doProject.isKindOf(context,DomainConstants.TYPE_PROJECT_SPACE)){
			strTaskType =(String)doProject.getAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_TASK_TYPE);
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strTaskType) && "PO".equals(strTaskType))
			mlResult=true;
			}
            return mlResult;
        } 
        catch (Exception exp){
            exp.printStackTrace();
            throw exp;
        }
   }
   /**
	 * This Method will return boolean value of the Part Number if task type is Sourcing.
	 * @param context Matrix Context object
	 * @throws MatrixException
	 * @author RG6
	 */
   
    public boolean getPartNumber(Context context, String[] args) throws Exception {	   
	   String strTaskType = "";	   
	   boolean mlResult = false;
        try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
		    String strObjectId = (String) programMap.get("objectId");
			DomainObject doProject = new DomainObject(strObjectId);
			if(! doProject.isKindOf(context,DomainConstants.TYPE_PROJECT_SPACE)){
			strTaskType =(String)doProject.getAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_TASK_TYPE);
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strTaskType) && "Sourcing".equals(strTaskType))
			mlResult = true;
			}
            return mlResult;
        } 
        catch (Exception exp){
            exp.printStackTrace();
            throw exp;
        }
   }
   
	 /**Method : checkAttributeTaskType
	 * if Attribute MSIL Task Type value is "PO",then user should not be able to promote the task to Active state from Assign state
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args    holds objectId
	 * @return        integer
	 * @throws Exception if the operation fails.
	 */

	public int checkAttributeTaskType (Context context, String[] args)throws Exception{
		int status = 0;
		try {
			// get values from args.
			String strObjectId = args[0];
            String strCurrent   = args[1];
			if(MSILUtils_mxJPO.isNotNullAndNotEmpty(strObjectId))
			{
                boolean isFormPOPage    = false;
                DomainObject domTask    = DomainObject.newInstance(context,strObjectId);
                String strPOCurrent     = domTask.getAttributeValue(context, "MSIL PO Task Promote");

                if(!"".equals(strPOCurrent))
                {
                    isFormPOPage=true;
                }


				if (!isFormPOPage) {
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
                    String strTaskType = domTask.getAttributeValue(context,MSILConstants_mxJPO.ATTRIBUTE_MSIL_TASK_TYPE);
                    //If the attribute MSIL Task Type value is PO then the task should not be promoted to Active state by the user. 
					if (MSILUtils_mxJPO.isNotNullAndNotEmpty(strTaskType)) {
						// if(strTaskType.equalsIgnoreCase("PO"))
						if (strTaskType.equalsIgnoreCase("PO") || strTaskType.equalsIgnoreCase("Ringi")) {
						// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
                            status=1;
                            String strErrMsg = "Promotion will happen with the status, fetched from ERP system.";
                            MqlUtil.mqlCommand(context, "notice " + strErrMsg);
                        }
                    }
                }
				if (!"".equals(strPOCurrent)) {
					if (strCurrent.equals(strPOCurrent)) 
                    {
                        domTask.setAttributeValue(context, "MSIL PO Task Promote", "");
                    }
                }
            }
            return status;
        } catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

	public Vector getCriticalTaskForWBS(Context context, String[] args) throws Exception
	{
		Vector vCriticalTask = new Vector();
        try
        {
        	StringList slObjSelect = new StringList();
    		slObjSelect.addElement(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK);
    		slObjSelect.addElement("attribute[Critical Task]");
    		DomainObject doObject = DomainObject.newInstance(context);
    		
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strProjetDelay   = "";
            String statusGif        = "";
            String strIsRoot        = "";
            MapList objectList      = (MapList) programMap.get("objectList");
            MapList mlChildList     = null;
            Iterator objectListItr  = objectList.iterator();
            Map objectMap           = null;
            Map mapChild            = null;

            StringList slCriticalTask   =   new StringList();
            
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                strIsRoot = (String)objectMap.get("Root Node");
                if("true".equals(strIsRoot))
                {
                    mlChildList =   (MapList)objectMap.get("children");
                    if(null != mlChildList && mlChildList.size() > 0)
                    {
                    	for (int kk=0; kk< mlChildList.size();kk++ ){
                    		mapChild=(Map) mlChildList.get(kk);                        
                    		vCriticalTask.add((String) mapChild.get(MSILConstants_mxJPO.SELECT_ATTRIBUTE_MSIL_CRITICAL_TASK));
                    	}
                    }
                    else
                    {
                    	String strObjId = (String)objectMap.get("id");
                    	 if(null != strObjId && strObjId.length() > 0)
                         {
                         	doObject = new DomainObject(strObjId);
                         	Map attributeMap = doObject.getInfo(context, slObjSelect);
                         	String strCriticalTask = (String)attributeMap.get("attribute[Critical Task]");
                             String strMSILCriticalTask = (String)attributeMap.get("attribute[MSIL Critical Task]");
                             String strTableCriticalTask = "No";
                             
                             if(strCriticalTask.equalsIgnoreCase("TRUE") || strMSILCriticalTask.equalsIgnoreCase("Yes"))
                             	strTableCriticalTask = "Yes";
                                                          
                             vCriticalTask.add(strTableCriticalTask);
                         }
                    }
                }
                else
                {
					String strObjId = (String)objectMap.get("id");
                    	 if(null != strObjId && strObjId.length() > 0)
                         {
                         	doObject = new DomainObject(strObjId);
                         	Map attributeMap = doObject.getInfo(context, slObjSelect);
                         	String strCriticalTask = (String)attributeMap.get("attribute[Critical Task]");
                             String strMSILCriticalTask = (String)attributeMap.get("attribute[MSIL Critical Task]");
                             String strTableCriticalTask = "No";
                             
                             if(strCriticalTask.equalsIgnoreCase("TRUE") || strMSILCriticalTask.equalsIgnoreCase("Yes"))
                             	strTableCriticalTask = "Yes";
                                                          
                             vCriticalTask.add(strTableCriticalTask);
                         }				
                	
                }                
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return vCriticalTask;
    }
	
	public HashMap getCriticalTaskRange(Context context, String[] args) throws Exception
	{
		try {
			String sLanguage = context.getSession().getLanguage();

			AttributeType atrMSILCriticalTask = new AttributeType(MSILConstants_mxJPO.ATTRIBUTE_MSIL_CRITICAL_TASK);
			atrMSILCriticalTask.open(context);
			StringList strList = atrMSILCriticalTask.getChoices(context);
			atrMSILCriticalTask.close(context);

			StringList slCriticalTaskRanges = new StringList();
			StringList slCriticalTaskTranslated = new StringList();
			HashMap map = new HashMap();
			String i18nSelectedRole = null;

			for(int i=0; i<strList.size();i++){
				String strTaskConstraintRange = (String)strList.get(i);

				slCriticalTaskRanges.add(strTaskConstraintRange);
				slCriticalTaskTranslated.add(strTaskConstraintRange);
			}

			map.put("field_choices", slCriticalTaskRanges);
			map.put("field_display_choices", slCriticalTaskTranslated);

			return  map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void updateCriticalTask(Context context,String[] args) throws Exception
	{		
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 HashMap paramMap = (HashMap) programMap.get("paramMap");
		 String strObjectId = (String) paramMap.get("objectId");
		 String strNewValue = (String) paramMap.get("New Value");

		 DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
		 if(!dmoObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) && !dmoObject.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT))
		 {
			 dmoObject.setAttributeValue(context, MSILConstants_mxJPO.ATTRIBUTE_MSIL_CRITICAL_TASK, strNewValue);			 
		 }
	 }
	/****************************************************************************************************
     *       PE Phase 2 - End
     ****************************************************************************************************/
	 // Added by Vinit - Start 29-Jun-2018
	 // this is the back-up for getTaskStatusSlip method
	 public Map getTaskStatusSlipBackup(Context context , String strEstFinishDate) throws Exception
     {
        Map mReturnMap = new HashMap();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        Date tempDate = new Date();

        Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
        int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
        
        Date estFinishDate = sdf.parse(strEstFinishDate);
        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        long daysRemaining = (long) task.computeDuration(sysDate, estFinishDate);
        String strStatusColor = "";
        String strSlipDays = "0";
        long lSlipDay                      		= 0;
        long lSlipDayAbsolute                   = 0;
		Date todayDate = new Date();
		todayDate.setHours(0);
		todayDate.setMinutes(0);
		todayDate.setSeconds(0);
        if (sysDate.after(estFinishDate))
        {
            strStatusColor = "Red";
            //Calculate Slip Days
            lSlipDay = task.computeDuration(estFinishDate,todayDate);//take out the starting day
            lSlipDayAbsolute = java.lang.Math.abs(lSlipDay);
            strSlipDays = String.valueOf(lSlipDayAbsolute);
        }
        else if (daysRemaining <= yellowRedThreshold)
        {
            strStatusColor = "Yellow";
        }
        else
        {
            strStatusColor = ProgramCentralConstants.EMPTY_STRING;
        }
        mReturnMap.put("statuscolor", strStatusColor);
        mReturnMap.put("slipdays", strSlipDays);
        return mReturnMap;
    }
	
	// Show Slip Review Days for Task. This is applicable for Task only. Its not recorded for Project.
	
	 public Vector getReviewSlipDays(Context context, String[] args) throws Exception
     {
        Vector showSlipReviewDays = new Vector();
		

        try
        {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
            HashMap programMap       = (HashMap) JPO.unpackArgs(args);
            String strSlipReviewDays   = "";
			
			
			
        	HashMap paramList = (HashMap) programMap.get("paramList");
        	MapList objectList = (MapList) programMap.get("objectList");
			
            Iterator objectListItr   = objectList.iterator();
            Map objectMap            = null;
			
			String strType             = "";
			String strReviewActualTime = "";
			String strCurrent          = "";
			StringList taskSelects     = new StringList(3);
			
            taskSelects.add("state[Review].actual");
			taskSelects.add("type");
			taskSelects.add("current");
			
			
			Date tempDate = new Date();

			Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
			
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
			
			String strObjId = "";
			
			DomainObject doObject = DomainObject.newInstance(context);

            Map mObjInfo = null;
            while(objectListItr.hasNext())
            {
                objectMap      = (Map) objectListItr.next();
                strObjId       = (String)objectMap.get("id");
				
				if(null!=strObjId && !"".equals(strObjId) && !"null".equalsIgnoreCase(strObjId))
				{
					doObject.setId(strObjId);
					mObjInfo = doObject.getInfo(context, taskSelects);
					
					if(null!=mObjInfo)
					{
						/*strType    = (String)mObjInfo.get("type");
						if(strType.equals("Project Space"))
						{
							showSlipReviewDays.add("");
						}
						else
						{*/
							strCurrent = (String)mObjInfo.get("current");
							if(strCurrent.equals("Review"))
							{
								strReviewActualTime    = (String)mObjInfo.get("state[Review].actual");	
								
								Date estFinishDate = sdf.parse(strReviewActualTime);

								long daysDifference = (long) task.computeDuration(estFinishDate, sysDate);
								

								
								long lSlipReviewDaysAbsolute = java.lang.Math.abs(daysDifference);
								strSlipReviewDays = String.valueOf(lSlipReviewDaysAbsolute);
								
								showSlipReviewDays.add(strSlipReviewDays);	
							}
							else
							{
								showSlipReviewDays.add("");	
							}
						/*}*/
					}
				}
			}
				
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return showSlipReviewDays;
    }
	
	// Added by Vinit - End 29-Jun-2018
}
