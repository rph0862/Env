/** Name of the JPO	: WMSJob
 ** WMS program
 **/
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.Job.BackgroundJob;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.common.Person;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * The purpose of this JPO is to create back ground jobs.
 * @version R417 
 */
public class WMSJob_mxJPO implements BackgroundJob
{
	protected Job _job = new Job();
	protected String _jobId = null;

	public static final String SYMBOLIC_attribute_NotifyOwner = "attribute_NotifyOwner";
    public static final String SYMBOLIC_attribute_ActionOnCompletion = "attribute_ActionOnCompletion";
    public static final String ATTRIBUTE_NOTIFY_OWNER = PropertyUtil.getSchemaProperty(SYMBOLIC_attribute_NotifyOwner);
    public static final String ATTRIBUTE_ACTION_COMPLETE = PropertyUtil.getSchemaProperty(SYMBOLIC_attribute_ActionOnCompletion);
    
	public void setJob(Job job)
	{
		_job = job;
	}

	public Job getJob()
	{
		return _job;
	}

	/**
	 * Constructor.
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - holds no arguments
	 * @throws Exception if the operation fails
	 * @author GVPP 
	 * @since 417
	 */

	public WMSJob_mxJPO(Context context, String[] args) throws Exception
	{
		if (args.length > 0 && args[0] != null && !"".equals(args[0]))
		{
			HashMap objectMap = (HashMap) JPO.unpackArgs(args);
			_jobId = (String) objectMap.get("Job ID");
			_job = Job.getInstance(context, _jobId, "EDM");
		}
	}
	/** 
	 * Method to create Job for the Import Items from file
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps for the table
	 * @throws Exception if the operation fails
	 * @since 417
	 */
	public void importItems(Context context, String args[]) throws Exception
	{
		Job jobObj = getJob();
		String strAction =  "";
		StringList strError = new StringList();
		if(args.length > 4)
		{
		    strAction = args[4];
		}
		setJobAttributes(context, jobObj, "No");
		boolean booleanError = false;
		String strMethodName = "";
		String packArgs[] = null;
		try
		{
		    if("SORItemImport".equals(strAction))
		    {
		        strMethodName = "importClassifiedItems";
		    	packArgs = new String[] {args[0],args[1],args[2],args[3],args[4],args[5],jobObj.getId(context)};
		    	args = packArgs;
		    }
		    if("MeasurementsImport".equals(strAction))
		    {
		        strMethodName = "importMeasurementItems";
		    }
		    if("SORImportForWO".equals(strAction))
		    {
		        strMethodName = "importSORItems";
		    }
		    if("BOQImport".equals(strAction))
		    {
		        strMethodName = "importBOQ";
		    }		    
		    if("ReviseBOQ".equals(strAction))
		    {
		        strMethodName = "reviseBOQ";
		    }
		    strError = (StringList)JPO.invoke(context,"WMSImport",args,strMethodName,args,StringList.class);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	    if(!"ReviseBOQ".equals(strAction))
	    {	        
	    	if(strError.size() > 0)
	    	{
	    		String strErrorMessage = strError.get(0).toString();
	    		if(strErrorMessage.indexOf("Message")>=0)
	    		{
	    			StringList error = FrameworkUtil.split(strErrorMessage, ":");
	    			setJobFinishStatus(context, jobObj, true,error.get(1).toString());
	    			sendJobCompletionMail(context, jobObj, true,error.get(1).toString());			
	    		}
	    	}
	    	else
	    	{
	    		setJobFinishStatus(context, jobObj, booleanError,DomainConstants.EMPTY_STRING);
	    		sendJobCompletionMail(context, jobObj, booleanError,strAction);
	    	}	
	    }	    
	}
	
	/** 
	 * Method set the Job status based on error value
	 * @param context the eMatrix <code>Context</code> object
	 * @param jobObj Job on which status to be set
	 * @param booleanError if True sets status as Succeeded else as Failed 
	 * @throws MatrixException if the operation fails
	 * @throws FrameworkException if the operation fails
	 * @since 417
	 */
	public void setJobFinishStatus(Context context, Job jobObj,
			boolean booleanError,String strErrorMsg) throws MatrixException, FrameworkException {
		if (jobObj.exists(context))
		{
			if (!booleanError)
			{
				jobObj.finish(context, "Succeeded");
			}
			else
			{
				jobObj.finish(context, "Failed");
				jobObj.setAttributeValue(context, "Error Message", strErrorMsg);
			}
		}

	}
	/** 
	 * Method to send mail on Job completion
	 * @param context the eMatrix <code>Context</code> object
	 * @param jobObj Job on which status to be set	
	 * @param booleanError if True with set success body and subject message 
	 * @param strAction - Job details
	 * @throws FrameworkException if the operation fails
	 * @since 417
	 */
	public void sendJobCompletionMail(Context context, Job jobObj,
			boolean booleanError,String strAction) throws FrameworkException {
		String strSubject = DomainConstants.EMPTY_STRING;
		String strBody = DomainConstants.EMPTY_STRING;
		if(booleanError)
		{
			strSubject = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strAction+".Import.FailedSubject");  
			strBody =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strAction+".Import.FailedBody");
		}
		else
		{		    
			strSubject = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strAction+".Import.SuccessSubject");
			strBody = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strAction+".Import.SuccessBody");
		}
		sendJobCompletionMail(context, jobObj, strSubject, strBody);
	}
	/** 
	 * Method to send mail on Job completion
	 * @param context the eMatrix <code>Context</code> object
	 * @param jobObj Job on which status to be set
	 * @param strSubject Subject of mail 
	 * @param strBody body of mail 
	 * @throws FrameworkException if the operation fails
	 * @since 417
	 */
	public void sendJobCompletionMail(Context context, Job jobObj,
			String strSubject, String strBody) throws FrameworkException {
		StringList strListToMail = new StringList(1);
		strListToMail.add(jobObj.getInfo(context, DomainConstants.SELECT_OWNER));

		StringList strListJobId = new StringList(jobObj.getInfo(context, DomainConstants.SELECT_ID));
		MailUtil.sendMessage(context,
				strListToMail,
				null,
				null,
				strSubject,
				strBody,
				strListJobId);
	}
	/** 
     * Method to set attribute values on Job
     * @param context the eMatrix <code>Context</code> object
     * @param jobObj Job on which status to be set
     * @param strNotifyOwner attribute value for notfy owner
     * @throws MatrixException if the operation fails
     * @throws FrameworkException if the operation fails
     * @since 417
     */
    public void setJobAttributes(Context context, Job jobObj, String strNotifyOwner)
            throws MatrixException, FrameworkException {

        if (jobObj.exists(context))
        {
             
            jobObj.setAttributeValue(context,ATTRIBUTE_NOTIFY_OWNER, strNotifyOwner);
            jobObj.setAttributeValue(context,ATTRIBUTE_ACTION_COMPLETE, "None");
        }
    }
	
	//Code added for B3 Action- Start
	/*
	/**
	 * Added Method getAllProjects
	 * @param context - current context
	 * @param String String[] args - holds ObjID
	 * @throws Exception - the exception
	*/
	public void getAllCIVILProjects(Context context,String[] args) throws Exception 
	{
		MapList mlFindProjectList = new MapList();
		MapList mlConnectedDepartment = new MapList();
		Map mapObject = null;
		Map mapDept = null;
		String strProjectId = null;
		String strProjName = null;
		String strProjOwner = null;
		String strDeptId = null;
		String strDeptName = null;
		StringList slSelectables = new StringList();
		slSelectables.addElement(DomainConstants.SELECT_NAME);
		slSelectables.addElement(DomainConstants.SELECT_ID);
		slSelectables.addElement(DomainConstants.SELECT_OWNER);
		StringList slDepartmentSelect = new StringList(2);
		slDepartmentSelect.addElement(DomainConstants.SELECT_NAME);
		try {
			
			mlFindProjectList = DomainObject.findObjects(
								context,								//current context object
								DomainConstants.TYPE_PROJECT_SPACE,		//type pattern
								DomainConstants.QUERY_WILDCARD,			//name pattern.
								DomainConstants.QUERY_WILDCARD,			//revision pattern.
								DomainConstants.QUERY_WILDCARD,			//owner pattern.
								DomainConstants.QUERY_WILDCARD,			//vault pattern.
								null, 									//where expression.
								false,									//false - do not find subtypes
								slSelectables);							//select clause.
			
			Iterator itr = mlFindProjectList.iterator();
			while(itr.hasNext()) {
				mapObject = (Map)itr.next();
				strProjectId = (String)mapObject.get(DomainConstants.SELECT_ID);
				strProjName = (String)mapObject.get(DomainConstants.SELECT_NAME);
				strProjOwner = (String)mapObject.get(DomainConstants.SELECT_OWNER);
				
				String srtPersonID = PersonUtil.getPersonObjectID(context, strProjOwner);
				DomainObject doPersonID = DomainObject.newInstance(context, srtPersonID);
				
				mlConnectedDepartment = doPersonID.getRelatedObjects(
											context, 							//the context for this request
											DomainConstants.RELATIONSHIP_MEMBER,//pattern to match relationships
											DomainConstants.TYPE_DEPARTMENT,			//pattern to match types
											slDepartmentSelect, 		// list of select statement pertaining to Business Obejcts.
											null,						//list of select statement pertaining to Relationships.
											true,						//get To relationships
											false, 						//get From relationships
											(short)1,					//the number of levels to expand, 0 equals expand all.
											null,				//where clause to apply to objects, can be empty ""
											null);						//where clause to apply to relationship, can be empty ""
				
				Iterator itrInner = mlConnectedDepartment.iterator();
				while(itrInner.hasNext()) {
					mapDept = (Map)itrInner.next();
					strDeptName = (String)mapDept.get(DomainConstants.SELECT_NAME);
					if ((strDeptName.equals("CVL-MNT") || strDeptName.equals("CVL-P") || strDeptName.equals("IPD"))){
						//calling below method to get Tasks
						getAllConnectedTasks (context, strProjectId,strProjOwner);
					} 
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//return slProjectList;
	}
	
	
	/** Method to get all connected tasks with attribute 'task estimated finish date'
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void getAllConnectedTasks(Context context,String strProjectId, String strProjOwner) throws Exception 
	{
		MapList mlConnectedTasks = new MapList();
		String strTaskFinishDate = null;
		String strPercentComplete = null;
		double dblPercentComplete;
		Map mapTask = null;
		try {
			String ATTRIBUTE_TASKESTINATEDFINISHDATE=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
			String RELATIONSHIP_SUBTASK=(String)PropertyUtil.getSchemaProperty(context,"relationship_Subtask");
			StringList slTaskSelect = new StringList();
			slTaskSelect.addElement(DomainConstants.SELECT_NAME);
			slTaskSelect.addElement(DomainConstants.SELECT_ID);
			slTaskSelect.addElement("attribute["+ ATTRIBUTE_TASKESTINATEDFINISHDATE +"]");
			slTaskSelect.addElement("attribute["+ DomainConstants.ATTRIBUTE_PERCENT_COMPLETE +"]");
			
			
			DomainObject doProjectId = DomainObject.newInstance(context, strProjectId);
			mlConnectedTasks = doProjectId.getRelatedObjects(
											context, 							//the context for this request
											DomainConstants.RELATIONSHIP_SUBTASK,//pattern to match relationships
											DomainConstants.TYPE_TASK,			//pattern to match types
											slTaskSelect, 						// list of select statement pertaining to Business Obejcts.
											null,								//list of select statement pertaining to Relationships.
											false,								//get To relationships
											true, 								//get From relationships
											(short)1,							//the number of levels to expand, 0 equals expand all.
											null,								//where clause to apply to objects, can be empty ""
											null);								//where clause to apply to relationship, can be empty ""
			
			Date today = new Date();
			Iterator itrTask = mlConnectedTasks.iterator();
			
			
			while(itrTask.hasNext()) {
				mapTask = (Map)itrTask.next();
				strTaskFinishDate = (String)mapTask.get("attribute["+ ATTRIBUTE_TASKESTINATEDFINISHDATE +"]");
				strPercentComplete = (String)mapTask.get("attribute["+ DomainConstants.ATTRIBUTE_PERCENT_COMPLETE +"]");
				dblPercentComplete = Double.parseDouble(strPercentComplete);
				
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
				Date CurrentMonthFirstLimit = sdf.parse(strTaskFinishDate);
				
				long DateDiff = daysBetween(today, CurrentMonthFirstLimit);
				getUserDetails(context,strProjectId,strProjOwner,DateDiff,dblPercentComplete);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Method to calculate days difference
	 * @param date one 
	 * @param date two
	 * @returns days difference 
	 */
	private static long daysBetween(Date one, Date two) {
		long difference = (one.getTime()-two.getTime())/86400000;
		return Math.abs(difference);
	}
	
		/*
	/**
	 * Added Method getAllProjects
	 * @param context - current context
	 * @param String String[] args - holds ObjID
	 * @throws Exception - the exception
	*/
	public void getUserDetails (Context context, String strProjectId, String strProjOwner, long DateDiff, double dblPercentComplete) throws Exception
	{
		com.matrixone.apps.program.ProjectSpace projectSpace = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		StringList slSelect = new StringList(3);
		slSelect.add(Person.SELECT_NAME);
		slSelect.add(Person.SELECT_ID);
		slSelect.add(Person.SELECT_EMAIL_ADDRESS);
		StringList slMemberSelect = new StringList(2);
		slMemberSelect.add(MemberRelationship.SELECT_PROJECT_ROLE);
		slMemberSelect.add(MemberRelationship.SELECT_PROJECT_ACCESS);
		String sMailSubject = "Due date is closer";
		String sFinalMailMessage = "Test Message";
		StringList slMailToList = new StringList();
		
		
		try {
			projectSpace.setId(strProjectId);
			MapList membersList = projectSpace.getMembers(context, slSelect, slMemberSelect, null, null);
			Iterator membersListItr = null;
			membersListItr = membersList.iterator();
			while (membersListItr.hasNext())
			{
				Map Currentmember = (Map) membersListItr.next();
				String strMemberName = (String) Currentmember.get(Person.SELECT_NAME);
				String mailToList = (String) Currentmember.get(Person.SELECT_ID);
				String strMemberEmail = (String) Currentmember.get(Person.SELECT_EMAIL_ADDRESS);
				String strProjectRole = (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ROLE);
				String strProjectAccess = (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ACCESS);
				slMailToList.add(strMemberEmail);
				
				if (DateDiff == 5) {
					if ("SectionManager".equals(strProjectRole)) {
						MailUtil.sendMessage(context, slMailToList, null, null,sMailSubject, sFinalMailMessage, null);
					}
				} else if (DateDiff == 3) {
					if (strMemberName.equals(strProjOwner) && "Project Owner".equals(strProjectAccess)) { 
						MailUtil.sendMessage(context, slMailToList, null, null,sMailSubject, sFinalMailMessage, null);
					}
				} else if (DateDiff <= 1 || dblPercentComplete < 100) {
					if (strProjectRole.equals("Reviewer")) {
						String userAgentAssignments = MqlUtil.mqlCommand(context, "print person $1 select  assignment dump;", strMemberName);
						if( userAgentAssignments.contains("DDVM") ) {
							MailUtil.sendMessage(context, slMailToList, null, null,sMailSubject, sFinalMailMessage, null);
						}
					}
				}
			}
			
		} catch (Exception e) {
			System.out.println("Exception in sending mail!!!!");
			e.printStackTrace();
		}
	}
	//Code added for B3 Action- end

	
	
}