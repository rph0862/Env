import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.ProjectManagement;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

public class MSILExportProjectData_mxJPO  extends com.matrixone.apps.program.Task implements MSILConstants_mxJPO
{

	public static  String START_DATE_SET_TIME = DomainObject.EMPTY_STRING;
	public static  int DEFAULT_START_TIME ;

	static
	{
		try{
			START_DATE_SET_TIME = FrameworkProperties.getProperty("emxFramework.Schedule.StartTime");
			DEFAULT_START_TIME = Integer.parseInt(START_DATE_SET_TIME);

			if(DEFAULT_START_TIME <12){
				START_DATE_SET_TIME 	= DEFAULT_START_TIME+":00:00 AM";
			}else if(DEFAULT_START_TIME >12 && DEFAULT_START_TIME <= 24){
				START_DATE_SET_TIME 	= (DEFAULT_START_TIME-12)+":00:00 PM";
			}else if(DEFAULT_START_TIME == 12){
				START_DATE_SET_TIME 	= DEFAULT_START_TIME+":00:00 PM";
			}else{
				DEFAULT_START_TIME = 8;
				START_DATE_SET_TIME = "08:00:00 AM";
			}
		}catch(Exception e){
			DEFAULT_START_TIME = 8;
			START_DATE_SET_TIME = "08:00:00 AM";
		}
	}

	public static  String FINISH_DATE_SET_TIME = DomainObject.EMPTY_STRING;
	public static  int DEFAULT_FINISH_TIME ;
	static
	{
		try{
			String[] lunchTiming = FrameworkProperties.getProperty("emxFramework.Schedule.LunchTime").split("-");
			if(lunchTiming != null && lunchTiming.length == 2) {
				int DEFAULT_LUNCH_HOUR	= Integer.parseInt(lunchTiming[0]);
				int DEFAULT_LUNCH_DURATION = Integer.parseInt(lunchTiming[1]) - DEFAULT_LUNCH_HOUR;

				int DEFAULT_HOURS_PER_DAY = Integer.parseInt(FrameworkProperties.getProperty("emxFramework.Schedule.WorkingHoursPerDay"));
				FINISH_DATE_SET_TIME =  String.valueOf(DEFAULT_START_TIME + DEFAULT_HOURS_PER_DAY + DEFAULT_LUNCH_DURATION);
			}
			DEFAULT_FINISH_TIME = Integer.parseInt(FINISH_DATE_SET_TIME);

			if(DEFAULT_FINISH_TIME <12){
				FINISH_DATE_SET_TIME 	= DEFAULT_FINISH_TIME +":00:00 AM";
			}else if(DEFAULT_FINISH_TIME >12 && DEFAULT_FINISH_TIME <= 24){
				FINISH_DATE_SET_TIME 	= (DEFAULT_FINISH_TIME-12) +":00:00 PM";
			}else if(DEFAULT_FINISH_TIME == 12){
				FINISH_DATE_SET_TIME 	= DEFAULT_FINISH_TIME +":00:00 PM";
			}else{
				DEFAULT_FINISH_TIME = 17;
				FINISH_DATE_SET_TIME = "05:00:00 PM";
			}
		}catch(Exception e){
			DEFAULT_FINISH_TIME = 17;
			FINISH_DATE_SET_TIME = "05:00:00 PM";
		}
	}

	// exec prog MSILExportProjectData -method createProjectExcel;
	public void createProjectExcel(Context context, String[] args) throws FrameworkException, IOException
	{
		try
		{
			FileWriter fMQL           =  new FileWriter("D:\\Vartika\\MSIL Support\\PE support\\YHBProjectData//PNE_exportProject_Attributes.xls");            
			BufferedWriter outLog         =  new BufferedWriter(fMQL);
			FileWriter fMQL2           =  new FileWriter("D:\\Vartika\\MSIL Support\\PE support\\YHBProjectData//PNE_exportProject_Connections.xls");            
			BufferedWriter outLog2         =  new BufferedWriter(fMQL2);

			FileWriter fMQL1           =  new FileWriter("D:\\Vartika\\MSIL Support\\PE support\\YHBProjectData//PNE_exportProject.log");            
			BufferedWriter outLog1         =  new BufferedWriter(fMQL1);

			// fetch all data from SMIPL vault
			StringList slObjSelects = new StringList();
			slObjSelects.add(DomainConstants.SELECT_ID);
			slObjSelects.add(DomainConstants.SELECT_NAME);
			slObjSelects.add(DomainConstants.SELECT_TYPE);
			slObjSelects.add(DomainConstants.SELECT_REVISION);

			outLog.write("Type\tName\tRevision\tState\tSequence Order\tProjectName\tObjectId\tMSILTaskType\tMSILDeliverable\tOOTBCritical\tMSILCritical\tMSILMachine\tMSILPONumber\tMSILPartNumber\tOwner\tISchildTask\n");
			outLog.flush();
			outLog2.write("Type\tName\tRevision\tState\tSequence Order\tProjectName\tObjectId\tMSILMilestoneId\tMSILMilestoneRelId\tAssigneesId\tAssigneeRelId\tDocumentsNames\tDocumentIds\tDocRelId\tRouteNames\tRouteIds\tRouteRelId\n");
			outLog2.flush();

			MapList tasksForProject = new MapList();

			ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

			DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].from.name");
			DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].from.id");
			DomainConstants.MULTI_VALUE_LIST.add("to[Assigned Tasks].id");
			DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.name");
			DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].to.id");
			DomainConstants.MULTI_VALUE_LIST.add("from[Task Deliverable].id");
			DomainConstants.MULTI_VALUE_LIST.add("from[Object Route].to.name");
			DomainConstants.MULTI_VALUE_LIST.add("from[Object Route].to.id");
			DomainConstants.MULTI_VALUE_LIST.add("from[Object Route].id");

			StringList taskSelects = new StringList(7);
			taskSelects.add(DomainConstants.SELECT_ID);
			taskSelects.add(DomainConstants.SELECT_CURRENT);
			taskSelects.add(DomainConstants.SELECT_NAME);
			taskSelects.add(DomainConstants.SELECT_POLICY);
			taskSelects.add(DomainConstants.SELECT_REVISION);
			taskSelects.add(DomainConstants.SELECT_TYPE);
			taskSelects.add(DomainConstants.SELECT_OWNER);
			//	taskSelects.add("from[Subtask].to.name");
			taskSelects.add(task.SELECT_HAS_SUBTASK);
			taskSelects.add("to[MSIL Milestone Task].from.id");
			taskSelects.add("to[MSIL Milestone Task].id");
			taskSelects.add("to[Assigned Tasks].from.name");
			taskSelects.add("to[Assigned Tasks].from.id");
			taskSelects.add("to[Assigned Tasks].id");
			taskSelects.add("attribute[MSIL Task Type]");
			taskSelects.add("attribute[MSIL Critical Task]");
			taskSelects.add("attribute[MSILReasonfordelay]");
			taskSelects.add("attribute[MSILActionPlanforcatchup]");
			taskSelects.add("attribute[MSIL Enovia Part No]");
			taskSelects.add("attribute[MSIL Machine/Process]");
			taskSelects.add("attribute[MSIL Enovia PO No]");
			taskSelects.add("attribute[MSIL Deliverable]");
			taskSelects.add("attribute[Critical Task]");
			taskSelects.add("from[Task Deliverable].to.name");
			taskSelects.add("from[Task Deliverable].to.id");
			taskSelects.add("from[Task Deliverable].id");
			taskSelects.add("from[Object Route].to.name");
			taskSelects.add("from[Object Route].to.id");
			taskSelects.add("from[Object Route].id");
			taskSelects.add("attribute[Comments]");
			taskSelects.add("to[Project Access Key].from.from[Project Access List].to.name");
			taskSelects.add("attribute[Task Estimated Start Date]");
			taskSelects.add("attribute[Task Estimated End Date]");

			StringList relSelects = new StringList();
			relSelects.addElement("id[connection]");
			relSelects.addElement("attribute[Sequence Order]");

			StringList slTasksIdList = new StringList();

			// hard code the name of the project
			String strQuery = "temp query bus 'Project Space' 'YHB Body PNE' 1201465995021716 select id dump |;";
			System.out.println("----------strQuery----------"+strQuery);
			MQLCommand mql = new MQLCommand();
			boolean bResult = mql.executeCommand(context, strQuery);
			if(bResult)
			{
				String sResult = mql.getResult().trim();
				if(sResult!=null && !"".equals(sResult))
				{
					StringTokenizer sResultTkz = new StringTokenizer(sResult,"|");
					sResultTkz.nextToken(); // type
					sResultTkz.nextToken(); // name
					sResultTkz.nextToken(); // revision
					String strProjectId = (String)sResultTkz.nextToken(); // Object Id
					project.setId(strProjectId);

					// fetch all tasks from the project that are in Assign/Active state
					tasksForProject = project.getTasks(context, 0, taskSelects, relSelects, false);
					//System.out.println("----------tasksForProject----------"+tasksForProject);
					tasksForProject.sort("attribute[Sequence Order]", "ascending",  "integer");
					outLog1.write("\n...tasksForProject..."+tasksForProject.size());outLog1.flush();
					//Iterator projectTasksItr = tasksForProject.iterator();
					for(int nCount = 0; nCount < tasksForProject.size(); nCount++)
					{
						outLog1.write("\n...nCount..."+nCount);outLog1.flush();
						Map taskMap = (Map) tasksForProject.get(nCount);
						outLog1.write("\n...taskMap..."+taskMap);outLog1.flush();
						String strSequenceOrder = (String)taskMap.get("attribute[Sequence Order]");
						String strTaskId = (String)taskMap.get("id");
						outLog.write((String)taskMap.get(DomainConstants.SELECT_TYPE));
						outLog.write("\t");
						//outLog1.write("\n...aa...");outLog1.flush();
						outLog.write((String)taskMap.get(DomainConstants.SELECT_NAME));
						outLog.write("\t");
						outLog.write((String)taskMap.get(DomainConstants.SELECT_REVISION));
						outLog.write("\t");
						outLog.write((String)taskMap.get(DomainConstants.SELECT_CURRENT));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[Sequence Order]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("to[Project Access Key].from.from[Project Access List].to.name"));
						outLog.write("\t");
						//outLog1.write("\n...bb...");outLog1.flush();
						outLog.write((String)taskMap.get(DomainConstants.SELECT_ID));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Task Type]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Deliverable]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[Critical Task]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Critical Task]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Machine/Process]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Enovia PO No]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get("attribute[MSIL Enovia Part No]"));
						outLog.write("\t");
						outLog.write((String)taskMap.get(DomainConstants.SELECT_OWNER));
						outLog.write("\t");
						outLog.write((String)taskMap.get(task.SELECT_HAS_SUBTASK));
						outLog.write("\n");
						outLog.flush();


						outLog2.write((String)taskMap.get(DomainConstants.SELECT_TYPE));
						outLog2.write("\t");
						outLog2.write((String)taskMap.get(DomainConstants.SELECT_NAME));
						outLog2.write("\t");
						outLog2.write((String)taskMap.get(DomainConstants.SELECT_REVISION));
						outLog2.write("\t");
						outLog2.write((String)taskMap.get(DomainConstants.SELECT_CURRENT));
						outLog2.write("\t");
						outLog2.write((String)taskMap.get("attribute[Sequence Order]"));
						outLog2.write("\t");
						outLog2.write((String)taskMap.get("to[Project Access Key].from.from[Project Access List].to.name"));
						outLog2.write("\t");
						//outLog1.write("\n...dd...");outLog1.flush();
						outLog2.write((String)taskMap.get(DomainConstants.SELECT_ID));
						outLog2.write("\t");
						if(taskMap.containsKey("to[MSIL Milestone Task].from.id"))
						{
							outLog2.write((String)taskMap.get("to[MSIL Milestone Task].from.id"));
							outLog2.write("\t");
							outLog2.write((String)taskMap.get("to[MSIL Milestone Task].id"));
						}
						else
						{
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
						}
						outLog2.write("\t");
						//outLog1.write("\n...ee...");outLog1.flush();
						if(taskMap.containsKey("to[Assigned Tasks].from.id"))
						{
							StringList slPersonList = taskMap.get("to[Assigned Tasks].from.id")  instanceof String ? new StringList((String)taskMap.get("to[Assigned Tasks].from.id")) :(StringList) taskMap.get("to[Assigned Tasks].from.id");
							outLog2.write(FrameworkUtil.join(slPersonList, ","));
							outLog2.write("\t");

							StringList slPersonRelIdList = taskMap.get("to[Assigned Tasks].id")  instanceof String ? new StringList((String)taskMap.get("to[Assigned Tasks].id")) :(StringList) taskMap.get("to[Assigned Tasks].id");
							outLog2.write(FrameworkUtil.join(slPersonRelIdList, ","));
						}
						else
						{
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
						}
						outLog2.write("\t");
						//outLog1.write("\n...ff...");outLog1.flush();
						if(taskMap.containsKey("from[Task Deliverable].to.id"))
						{
							StringList slDeliverableNameList = taskMap.get("from[Task Deliverable].to.name")  instanceof String ? new StringList((String)taskMap.get("from[Task Deliverable].to.name")) :(StringList) taskMap.get("from[Task Deliverable].to.name");
							outLog2.write(FrameworkUtil.join(slDeliverableNameList, ","));							
							outLog2.write("\t");

							StringList slDeliverableIdList = taskMap.get("from[Task Deliverable].to.id")  instanceof String ? new StringList((String)taskMap.get("from[Task Deliverable].to.id")) :(StringList) taskMap.get("from[Task Deliverable].to.id");
							outLog2.write(FrameworkUtil.join(slDeliverableIdList, ","));
							outLog2.write("\t");

							StringList slDeliverableRelIdList = taskMap.get("from[Task Deliverable].id")  instanceof String ? new StringList((String)taskMap.get("from[Task Deliverable].id")) :(StringList) taskMap.get("from[Task Deliverable].id");
							outLog2.write(FrameworkUtil.join(slDeliverableRelIdList, ","));
						}
						else
						{
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
						}
						outLog2.write("\t");
						//outLog1.write("\n...gg...");outLog1.flush();
						if(taskMap.containsKey("from[Object Route].to.id"))
						{
							outLog2.write((String)taskMap.get("from[Object Route].to.name"));
							outLog2.write("\t");
							outLog2.write((String)taskMap.get("from[Object Route].to.id"));
							outLog2.write("\t");
							outLog2.write((String)taskMap.get("from[Object Route].id"));
						}
						else
						{
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
							outLog2.write("\t");
							outLog2.write("");
						}
						//outLog1.write("\n...hh...");outLog1.flush();
						outLog2.write("\n");
						outLog2.flush();
					}
				}
			}
			DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].from.name");
			DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].from.id");
			DomainConstants.MULTI_VALUE_LIST.remove("to[Assigned Tasks].id");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Task Deliverable].to.name");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Task Deliverable].to.id");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Task Deliverable].id");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Object Route].to.name");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Object Route].to.id");
			DomainConstants.MULTI_VALUE_LIST.remove("from[Object Route].id");

			outLog2.close();
			outLog.close();
			outLog1.close();
		}
		catch (Exception ex)
		{
			System.out.println("Exception ex........>>>>>>"+ex);
			ex.printStackTrace();  
		}
	}

	//String strfolderPath = "D:\\Vartika\\MSIL Support\\PE support\\YHBProjectData\\ProjectsInputFiles\\";
	String strfolderPath = "//appv62013x//Spinner//ExportProject//YHBProjectData//ProjectsInputFiles//";

	// exec prog MSILExportProjectData -method connectMilestone;
	// input file format : New Task Id|Milestone|Old Task-Milestone Rel Id
	public void connectMilestone (Context context,String[] args) throws Exception
	{
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "Milestone";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();				
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".txt"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "Milestone//Logs//"+sTemp+".log";
							fw = new FileWriter(strNameofLogFile);
							while((strLine = br.readLine()) != null)
							{
								fw.write("\n...strLine..."+strLine); fw.flush();
								StringList slTemp = FrameworkUtil.split(strLine,"|");

								String strNewTaskObjectId = (String)slTemp.get(0);
								fw.write("\n...strObjectId..."+strNewTaskObjectId); fw.flush();

								String strMilestone = (String)slTemp.get(1);
								fw.write("\n...strMilestone..."+strMilestone); fw.flush();

								if(null != strMilestone && !"".equalsIgnoreCase(strMilestone) && strMilestone.length() > 0)
								{
									DomainObject milestoneDO = DomainObject.newInstance(context, strMilestone);
									if(null != strNewTaskObjectId && !"".equalsIgnoreCase(strNewTaskObjectId) && strNewTaskObjectId.length() > 0)
									{
										DomainObject taskDO = DomainObject.newInstance(context, strNewTaskObjectId);
										DomainRelationship.connect(context, milestoneDO, REL_MSIL_MILESTONE_TASK, taskDO);
									}
								}
								String strOldRelId = (String)slTemp.get(2);	
								if(null != strOldRelId && !"".equalsIgnoreCase(strOldRelId) && strOldRelId.length() > 0)
								{
									DomainRelationship.disconnect(context, strOldRelId);
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}	
	}		

	// exec prog MSILExportProjectData -method connectAssignee;
	// input file format : New Task Id|Assignee|Old Task-Assignee Rel Id
	public void connectAssignee (Context context,String[] args) throws Exception
	{
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "Assignee";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();				
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".txt"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "Assignee//Logs//"+sTemp+".log";
							fw = new FileWriter(strNameofLogFile);
							while((strLine = br.readLine()) != null)
							{
								fw.write("\n...strLine..."+strLine); fw.flush();
								StringList slTemp = FrameworkUtil.split(strLine,"|");

								String strNewTaskObjectId = (String)slTemp.get(0);
								fw.write("\n...strObjectId..."+strNewTaskObjectId); fw.flush();

								String strAssignee = (String)slTemp.get(1);
								fw.write("\n...strAssignee..."+strAssignee); fw.flush();

								if(null != strAssignee && !"".equalsIgnoreCase(strAssignee) && strAssignee.length() > 0)
								{
									if(strAssignee.contains(","))
									{
										StringList slAssigneeList = FrameworkUtil.split(strAssignee, ",");
										for(int n = 0; n < slAssigneeList.size(); n++)
										{
											String strAssigneeFromList = (String)slAssigneeList.get(n);
											DomainObject assigneeDO = DomainObject.newInstance(context, strAssigneeFromList);
											if(null != strNewTaskObjectId && !"".equalsIgnoreCase(strNewTaskObjectId) && strNewTaskObjectId.length() > 0)
											{
												DomainObject taskDO = DomainObject.newInstance(context, strNewTaskObjectId);
												DomainRelationship.connect(context, assigneeDO, "Assigned Tasks", taskDO);
											}
										}
									}
									else
									{
										DomainObject assigneeDO = DomainObject.newInstance(context, strAssignee);
										if(null != strNewTaskObjectId && !"".equalsIgnoreCase(strNewTaskObjectId) && strNewTaskObjectId.length() > 0)
										{
											DomainObject taskDO = DomainObject.newInstance(context, strNewTaskObjectId);
											DomainRelationship.connect(context, assigneeDO, "Assigned Tasks", taskDO);
										}
									}				
								}
								String strOldRelId = (String)slTemp.get(2);	
								if(null != strOldRelId && !"".equalsIgnoreCase(strOldRelId) && strOldRelId.length() > 0)
								{
									if(strOldRelId.contains(","))
									{
										StringList slOldRelIdList = FrameworkUtil.split(strOldRelId, ",");
										for(int n = 0; n < slOldRelIdList.size(); n++)
										{
											String strOldRelIdList = (String)slOldRelIdList.get(n);
											DomainRelationship.disconnect(context, strOldRelIdList);
										}
									}
									else
										DomainRelationship.disconnect(context, strOldRelId);
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}
	}		

	// exec prog MSILExportProjectData -method updateAttributes;
	// input file format : New Task Id|MSILTaskType|MSILDeliverable|OOTBCritical|MSILCritical|MSILMachine|MSILPONumber|MSILPartNumber|Owner
	public void updateAttributes (Context context,String[] args) throws Exception
	{
		try
		{
			FileWriter fMQL           =  new FileWriter(strfolderPath + "Task_Attributes.txt");            
			BufferedWriter outLog         =  new BufferedWriter(fMQL);

			FileInputStream fis =new FileInputStream(strfolderPath + "Task_Attributes.txt");
			System.out.println("\n...fis ..."+fis);

			DataInputStream dr              = new DataInputStream(fis);
			BufferedReader br               = new BufferedReader(new InputStreamReader(dr));

			String strLine;

			while((strLine = br.readLine()) != null)
			{
				Map attributeMap = new HashMap();
				outLog.write("\n...strLine..."+strLine); outLog.flush();
				StringList slTemp = FrameworkUtil.split(strLine,"|");

				String strNewTaskObjectId = (String)slTemp.get(0);
				outLog.write("\n...strObjectId..."+strNewTaskObjectId); outLog.flush();

				String strMSILTaskType = (String)slTemp.get(1);
				outLog.write("\n...strMSILTaskType..."+strMSILTaskType); outLog.flush();
				if(null != strMSILTaskType && strMSILTaskType.length() > 0)
					attributeMap.put("MSIL Task Type", strMSILTaskType);

				String strMSILDeliverable = (String)slTemp.get(2);
				outLog.write("\n...MSILDeliverable..."+strMSILDeliverable); outLog.flush();
				if(null != strMSILDeliverable && strMSILDeliverable.length() > 0)
					attributeMap.put("MSIL Deliverable", strMSILDeliverable);

				/*String strOOTBCritical = (String)slTemp.get(3);
				outLog.write("\n...OOTBCritical..."+strOOTBCritical); outLog.flush();
				if(null != strOOTBCritical && strOOTBCritical.length() > 0)
					attributeMap.put("Critical Task", strOOTBCritical);*/

				String strMSILCritical = (String)slTemp.get(4);
				outLog.write("\n...MSILCritical..."+strMSILCritical); outLog.flush();
				if(null != strMSILCritical && strMSILCritical.length() > 0)
					attributeMap.put("MSIL Critical Task", strMSILCritical);

				String strMSILMachine = (String)slTemp.get(5);
				outLog.write("\n...MSILMachine..."+strMSILMachine); outLog.flush();
				if(null != strMSILMachine && strMSILMachine.length() > 0)
					attributeMap.put("MSIL Machine/Process", strMSILMachine);

				String strMSILPONumber = (String)slTemp.get(6);
				outLog.write("\n...MSILPONumber..."+strMSILPONumber); outLog.flush();
				if(null != strMSILPONumber && strMSILPONumber.length() > 0)
					attributeMap.put("MSIL Enovia PO No", strMSILPONumber);

				String strMSILPartNumber = (String)slTemp.get(7);
				outLog.write("\n...MSILPartNumber..."+strMSILPartNumber); outLog.flush();
				if(null != strMSILPartNumber && strMSILPartNumber.length() > 0)
					attributeMap.put("MSIL Enovia Part No", strMSILPartNumber);

				String strOwner = (String)slTemp.get(8);
				outLog.write("\n...Owner..."+strOwner); outLog.flush();

				if(null != strNewTaskObjectId && !"".equalsIgnoreCase(strNewTaskObjectId) && strNewTaskObjectId.length() > 0)
				{
					DomainObject taskDO = DomainObject.newInstance(context, strNewTaskObjectId);
					taskDO.setAttributeValues(context,attributeMap);
					taskDO.setOwner(context, strOwner);
				}							
			}
		}
		catch (Exception ex)
		{
			System.out.println("Exception ex........>>>>>>"+ex);
			ex.printStackTrace();  
		}
	}	

	// exec prog MSILExportProjectData -method connectDocuments;
	// input file format : New Task Id|DocumentId|Old Task-Doc Rel Id
	public void connectDocuments (Context context,String[] args) throws Exception
	{
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "Documents";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();				
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".txt"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "Documents//Logs//"+sTemp+".log";
							fw = new FileWriter(strNameofLogFile);
							while((strLine = br.readLine()) != null)
							{
								fw.write("\n...strLine..."+strLine); fw.flush();
								StringList slTemp = FrameworkUtil.split(strLine,"|");

								String strObjectId = (String)slTemp.get(0);
								fw.write("\n...strObjectId..."+strObjectId); fw.flush();

								String strDocId = (String)slTemp.get(1);
								fw.write("\n...strDocId..."+strDocId); fw.flush();

								if(null != strDocId && !"".equalsIgnoreCase(strDocId) && strDocId.length() > 0)
								{
									if(strDocId.contains(","))
									{
										StringList slDocumentList = FrameworkUtil.split(strDocId, ",");
										for(int n = 0; n < slDocumentList.size(); n++)
										{
											String strDocFromList = (String)slDocumentList.get(n);
											DomainObject docDO = DomainObject.newInstance(context, strDocFromList);
											if(null != strObjectId && !"".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
											{
												DomainObject taskDO = DomainObject.newInstance(context, strObjectId);
												DomainRelationship.connect(context, taskDO, "Task Deliverable", docDO);
											}
										}
									}
									else
									{
										DomainObject docDO = DomainObject.newInstance(context, strDocId);
										if(null != strObjectId && !"".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
										{
											DomainObject taskDO = DomainObject.newInstance(context, strObjectId);
											DomainRelationship.connect(context, taskDO, "Task Deliverable", docDO);
										}
									}				
								}

								String strOldRelId = (String)slTemp.get(2);	
								if(null != strOldRelId && !"".equalsIgnoreCase(strOldRelId) && strOldRelId.length() > 0)
								{
									if(strOldRelId.contains(","))
									{
										StringList slOldRelIdList = FrameworkUtil.split(strOldRelId, ",");
										for(int n = 0; n < slOldRelIdList.size(); n++)
										{
											String strOldRelIdList = (String)slOldRelIdList.get(n);
											DomainRelationship.disconnect(context, strOldRelIdList);
										}
									}
									else
										DomainRelationship.disconnect(context, strOldRelId);
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}	
	}

	// exec prog MSILExportProjectData -method connectRoutes;
	// input file format : New Task Id|RouteId|Old Task-Route Rel Id
	public void connectRoutes (Context context,String[] args) throws Exception
	{
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "Routes";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();				
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".txt"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "Routes//Logs//"+sTemp+".log";
							fw = new FileWriter(strNameofLogFile);
							while((strLine = br.readLine()) != null)
							{
								fw.write("\n...strLine..."+strLine); fw.flush();
								StringList slTemp = FrameworkUtil.split(strLine,"|");

								String strObjectId = (String)slTemp.get(0);
								fw.write("\n...strObjectId..."+strObjectId); fw.flush();

								String strRouteId = (String)slTemp.get(1);
								fw.write("\n...strRouteId..."+strRouteId); fw.flush();

								if(null != strRouteId && !"".equalsIgnoreCase(strRouteId) && strRouteId.length() > 0)
								{
									if(strRouteId.contains(","))
									{
										StringList slRouteList = FrameworkUtil.split(strRouteId, ",");
										for(int n = 0; n < slRouteList.size(); n++)
										{
											String strRouteIdFromList = (String)slRouteList.get(n);
											DomainObject routeDO = DomainObject.newInstance(context, strRouteIdFromList);
											if(null != strObjectId && !"".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
											{
												DomainObject taskDO = DomainObject.newInstance(context, strObjectId);
												DomainRelationship.connect(context, taskDO, "Object Route", routeDO);
											}
										}
									}
									else
									{
										DomainObject routeDO = DomainObject.newInstance(context, strRouteId);
										if(null != strObjectId && !"".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
										{
											DomainObject taskDO = DomainObject.newInstance(context, strObjectId);
											DomainRelationship.connect(context, taskDO, "Object Route", routeDO);
										}
									}				
								}

								String strOldRelId = (String)slTemp.get(2);	
								if(null != strOldRelId && !"".equalsIgnoreCase(strOldRelId) && strOldRelId.length() > 0)
								{
									if(strOldRelId.contains(","))
									{
										StringList slOldRelIdList = FrameworkUtil.split(strOldRelId, ",");
										for(int n = 0; n < slOldRelIdList.size(); n++)
										{
											String strOldRelIdList = (String)slOldRelIdList.get(n);
											DomainRelationship.disconnect(context, strOldRelIdList);
										}
									}
									else
										DomainRelationship.disconnect(context, strOldRelId);
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}
	}

	// exec prog MSILExportProjectData -method updateCommentsAndReasonforDelay;
	// input file format : New Task Id|Old Task Id
	public void updateCommentsAndReasonforDelay (Context context,String[] args) throws Exception
	{
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "Comments";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();				
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".txt"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "Comments//Logs//"+sTemp+".log";
							fw = new FileWriter(strNameofLogFile);
							while((strLine = br.readLine()) != null)
							{
								fw.write("\n...strLine..."+strLine); fw.flush();
								StringList slTemp = FrameworkUtil.split(strLine,"|");

								String strNewTaskObjectId = (String)slTemp.get(0);
								fw.write("\n...strNewTaskObjectId..."+strNewTaskObjectId); fw.flush();

								String strOldTaskId = (String)slTemp.get(1);
								fw.write("\n...strOldTaskId..."+strOldTaskId); fw.flush();

								if(null != strNewTaskObjectId && !"".equalsIgnoreCase(strNewTaskObjectId) && strNewTaskObjectId.length() > 0)
								{
									DomainObject taskDO = DomainObject.newInstance(context, strOldTaskId);
									String strComments = taskDO.getInfo(context, "attribute[Comments]");
									String strReasonForDelay = taskDO.getInfo(context, "attribute[MSILReasonfordelay]");
									String strMSILActionPlanforcatchup = taskDO.getInfo(context, "attribute[MSILActionPlanforcatchup]");

									Map attributeMap = new HashMap();
									attributeMap.put("Comments", strComments);
									attributeMap.put("MSILReasonfordelay", strReasonForDelay);
									attributeMap.put("MSILActionPlanforcatchup", strMSILActionPlanforcatchup);
									fw.write("\n...attributeMap..."+attributeMap); fw.flush();
									DomainObject newTaskDO = DomainObject.newInstance(context, strNewTaskObjectId);
									newTaskDO.setAttributeValues(context,attributeMap);
								}	
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}	
	}

	private static boolean match(String _strFileName, String _suffix)
	{
		String strFileExtension = _strFileName.substring(_strFileName.length() - _suffix.length(), _strFileName.length());
		if (_strFileName.length() >= _suffix.length() && strFileExtension.equalsIgnoreCase(_suffix))
			return true;
		else
			return false;
	}

	public void readAndImportCSVLowestLevelTask(Context context, String[] args) throws FrameworkException, IOException
	{
		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);		

		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "ActualCSVsToBeUpdated";

		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			System.out.println("\n...nFileListSize = "+nFileListSize);
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();
				//logFile.write("\n...Is it a file = "+bIsFile);    logFile.flush();
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					//logFile.write("\n...Name of the file ...."+strFileName);    logFile.flush();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".csv"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile =strfolderPath + "ActualCSVsToBeUpdated//Logs//"+sTemp+"_importProjectDetailsLog.log";
							fw = new FileWriter(strNameofLogFile);
							int iTemp = 0;
							while((strLine = br.readLine()) != null)
							{
								if (iTemp == 0) // for skipping first row in file
								{
									iTemp = 1;
									continue;
								}
								//System.out.println("\n..strLine = "+strLine);

								StringList slTemp = FrameworkUtil.split(strLine, ",");

								if(null != slTemp && slTemp.size() > 0)
								{									
									String strTaskNameFromCSV = ((String)slTemp.get(0)).trim();
									String strTaskId = ((String)slTemp.get(1)).trim();
									String strPercentComplete = ((String)slTemp.get(2)).trim();

									String strActualStartDate = ((String)slTemp.get(3)).trim();
									String strActualFinishDate = ((String)slTemp.get(4)).trim();

									SimpleDateFormat MATRIX_DATE_FORMAT = new SimpleDateFormat ("M/d/yyyy");
									java.text.SimpleDateFormat testDate = new java.text.SimpleDateFormat("MM/dd/yy");

									String strTaskUniqueId = ((String)slTemp.get(5)).trim();

									fw.write("\n....slTemp...."+slTemp); fw.flush();

									task.setId(strTaskId);

									StringList busSelect = new StringList();
									busSelect.add("name");
									busSelect.add("to[Project Access Key].from.from[Project Access List].to.id");
									//busSelect.add("from[Subtask].attribute[Sequence Order]");
									Map taskInfo = task.getInfo(context, busSelect);
									fw.write("\n\t....taskInfo...."+taskInfo); fw.flush();
									String strTaskName = (String)taskInfo.get("name");
									String strProjectId = (String)taskInfo.get("to[Project Access Key].from.from[Project Access List].to.id");

									StringList slSubtasks = task.getInfoList(context, SELECT_SUBTASK_IDS);
									boolean bIsSummaryTask = !(slSubtasks == null || slSubtasks.size() == 0);
									fw.write("\n\t....isSummaryTask...."+bIsSummaryTask); fw.flush();

									if(null != strTaskName && !"".equalsIgnoreCase(strTaskName) && !"null".equalsIgnoreCase(strTaskName))
									{
										if(!bIsSummaryTask)
										{
											fw.write("\n\t========================----------strTaskName----------"+strTaskName); fw.flush();
											fw.write("\n\t----------strPercentComplete----------"+strPercentComplete); fw.flush();
											fw.write("\n\t----------strActualStartDate----------"+strActualStartDate); fw.flush();
											fw.write("\n\t----------strActualFinishDate----------"+strActualFinishDate); fw.flush();

											// if actual end date == null, update actual start date & percent complete
											// if actual end date != null, update actual start date & actual end date
											Map attributeMap = new HashMap(5);

											if(null != strActualFinishDate && !"".equalsIgnoreCase(strActualFinishDate) && !"null".equalsIgnoreCase(strActualFinishDate) && !"NA".equalsIgnoreCase(strActualFinishDate))
											{
												fw.write("\n\t\t----------if actual end date != null, update actual start date & actual end date----------"); fw.flush();
												if(null != strActualStartDate && !"".equalsIgnoreCase(strActualStartDate) && !"null".equalsIgnoreCase(strActualStartDate) && !"NA".equalsIgnoreCase(strActualStartDate))
												{
													HashMap paramMap = new HashMap();
													HashMap programMap = new HashMap();
													HashMap requestMap = new HashMap();
													String[] arrJPOArguments = new String[1];

													Locale locale = new Locale("en_US");
													Date date1 = eMatrixDateFormat.getJavaDate(strActualStartDate);
													int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
													java.text.DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, Locale.US);
													strActualStartDate  = format.format(date1);

													paramMap.put("New Value", strActualStartDate);
													paramMap.put("Old Value", null);
													paramMap.put("objectId", strTaskId);
													programMap.put("paramMap", paramMap);

													requestMap.put("locale", locale);
													programMap.put("requestMap", requestMap);

													arrJPOArguments = JPO.packArgs(programMap);
													fw.write("\n\t\t\t----------updateTaskActualStartDate start----------"+strActualStartDate); fw.flush();
													JPO.invoke(context,"emxTaskBase",null,"updateTaskActualStartDate",arrJPOArguments);

													TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
													double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
													double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

													String strNewDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strActualStartDate, clientTZOffset, locale);
													Date dtNewValue = eMatrixDateFormat.getJavaDate(strNewDateVal);
													Calendar calToday = Calendar.getInstance();
													calToday.setTime(dtNewValue);													

													String strNewVal = format.format((calToday.getTime()));
													String strFieldValueAttr = eMatrixDateFormat.getFormattedInputDateTime(context, strNewVal,START_DATE_SET_TIME, clientTZOffset, locale);
													String strTempfieldValueAttr = eMatrixDateFormat.getFormattedDisplayDateTime(context, strFieldValueAttr, true, iDateFormat, clientTZOffset, locale);
													Date newStartDate = format.parse(strTempfieldValueAttr);																										

													HashMap _taskMap = new HashMap();
													Map objectMap = new HashMap();
													objectMap.put("actualStartDate", newStartDate);
													_taskMap.put(strTaskId, objectMap);
													String strMsg = Task.updateDates(context, _taskMap);

													if (strMsg != null && !"".equals(strMsg)
															&& !"false".equalsIgnoreCase(strMsg)) {
														throw new Exception(strMsg);
													}

													//Add rollup logic here
													//boolean enableAutoschedule = ProgramCentralUtil.enableAutoSchedule(context);
													boolean enableAutoschedule = false;
													if(enableAutoschedule){
														Task project = new Task(strProjectId);
														project.rollupAndSave(context);
													}

													fw.write("\n\t\t\t----------updateTaskActualStartDate finish----------"); fw.flush();
												}
												if(null != strActualFinishDate && !"".equalsIgnoreCase(strActualFinishDate) && !"null".equalsIgnoreCase(strActualFinishDate) && !"NA".equalsIgnoreCase(strActualFinishDate))
												{
													HashMap paramMap = new HashMap();
													HashMap requestMap = new HashMap();
													HashMap programMap = new HashMap();
													String[] arrJPOArguments = new String[1];

													Locale locale = new Locale("en_US");
													Date date1 = eMatrixDateFormat.getJavaDate(strActualFinishDate);
													int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
													java.text.DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, Locale.US);
													strActualFinishDate  = format.format(date1);

													paramMap.put("New Value", strActualFinishDate);
													paramMap.put("Old Value", null);
													paramMap.put("objectId", strTaskId);
													programMap.put("paramMap", paramMap);

													requestMap.put("locale", locale);
													programMap.put("requestMap", requestMap);

													arrJPOArguments = JPO.packArgs(programMap);
													fw.write("\n\t\t\t----------updateTaskActualFinishDate start----------"+strActualFinishDate); fw.flush();
													JPO.invoke(context,"emxTaskBase",null,"updateTaskActualFinishDate",arrJPOArguments);

													TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
													double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
													double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
													Calendar calToday = Calendar.getInstance();

													String strNewDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strActualFinishDate, clientTZOffset, locale);
													Date dtNewValue = eMatrixDateFormat.getJavaDate(strNewDateVal);
													calToday.setTime(dtNewValue);

													String strNewVal = format.format((calToday.getTime()));
													String strFieldValueAttr = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strNewVal, clientTZOffset, locale);
													String strTempfieldValueAttr = eMatrixDateFormat.getFormattedDisplayDateTime(context, strFieldValueAttr, true, iDateFormat, clientTZOffset, locale);
													Date newFinishDate = format.parse(strTempfieldValueAttr);																									

													Calendar calNewFinishDate = Calendar.getInstance();
													calNewFinishDate.setTime(newFinishDate);
													calNewFinishDate.set(Calendar.HOUR_OF_DAY,DEFAULT_FINISH_TIME);
													calNewFinishDate.set(Calendar.MINUTE,0);
													calNewFinishDate.set(Calendar.SECOND,0);
													calNewFinishDate.set(Calendar.MILLISECOND,0);
													newFinishDate = calNewFinishDate.getTime();

													HashMap _taskMap = new HashMap();
													Map objectMap = new HashMap();
													objectMap.put("actualFinishDate", newFinishDate);
													_taskMap.put(strTaskId, objectMap);
													String strMsg = Task.updateDates(context, _taskMap);

													if (strMsg != null && !"".equals(strMsg)
															&& !"false".equalsIgnoreCase(strMsg)) {
														throw new Exception(strMsg);
													}

													//Add rollup logic here
													//boolean enableAutoschedule = ProgramCentralUtil.enableAutoSchedule(context);
													boolean enableAutoschedule = false;
													if(enableAutoschedule){
														Task project = new Task(strProjectId);
														project.rollupAndSave(context);
													}
													fw.write("\n\t\t\t----------updateTaskActualFinishDate finish----------"); fw.flush();
												}
											}
											else //if (null == strActualFinishDate || "".equalsIgnoreCase(strActualFinishDate) || "null".equalsIgnoreCase(strActualFinishDate) || "NA".equalsIgnoreCase(strActualFinishDate))
											{
												fw.write("\n\t\t----------if actual end date == null, update actual start date & percent complete----------"); fw.flush();
												if(null != strActualStartDate && !"".equalsIgnoreCase(strActualStartDate) && !"null".equalsIgnoreCase(strActualStartDate) && !"NA".equalsIgnoreCase(strActualStartDate))
												{
													HashMap paramMap = new HashMap();
													HashMap programMap = new HashMap();
													HashMap requestMap = new HashMap();
													String[] arrJPOArguments = new String[1];

													Locale locale = new Locale("en_US");
													Date date1 = eMatrixDateFormat.getJavaDate(strActualStartDate);
													int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
													java.text.DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, Locale.US);
													strActualStartDate  = format.format(date1);

													paramMap.put("New Value", strActualStartDate);
													paramMap.put("Old Value", null);
													paramMap.put("objectId", strTaskId);
													programMap.put("paramMap", paramMap);

													requestMap.put("locale", locale);
													programMap.put("requestMap", requestMap);

													arrJPOArguments = JPO.packArgs(programMap);
													fw.write("\n\t\t\t----------updateTaskActualStartDate start----------"+strActualStartDate); fw.flush();
													JPO.invoke(context,"emxTaskBase",null,"updateTaskActualStartDate",arrJPOArguments);

													TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
													double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
													double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

													String strNewDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strActualStartDate, clientTZOffset, locale);
													Date dtNewValue = eMatrixDateFormat.getJavaDate(strNewDateVal);
													Calendar calToday = Calendar.getInstance();
													calToday.setTime(dtNewValue);													

													String strNewVal = format.format((calToday.getTime()));
													String strFieldValueAttr = eMatrixDateFormat.getFormattedInputDateTime(context, strNewVal,START_DATE_SET_TIME, clientTZOffset, locale);
													String strTempfieldValueAttr = eMatrixDateFormat.getFormattedDisplayDateTime(context, strFieldValueAttr, true, iDateFormat, clientTZOffset, locale);
													Date newStartDate = format.parse(strTempfieldValueAttr);																										

													HashMap _taskMap = new HashMap();
													Map objectMap = new HashMap();
													objectMap.put("actualStartDate", newStartDate);
													_taskMap.put(strTaskId, objectMap);
													String strMsg = Task.updateDates(context, _taskMap);

													if (strMsg != null && !"".equals(strMsg)
															&& !"false".equalsIgnoreCase(strMsg)) {
														throw new Exception(strMsg);
													}

													//Add rollup logic here
													//boolean enableAutoschedule = ProgramCentralUtil.enableAutoSchedule(context);
													boolean enableAutoschedule = false;
													if(enableAutoschedule){
														Task project = new Task(strProjectId);
														project.rollupAndSave(context);
													}
													fw.write("\n\t\t\t----------updateTaskActualStartDate finish----------"); fw.flush();
												}

												if(null != strPercentComplete && !"".equalsIgnoreCase(strPercentComplete) && !"null".equalsIgnoreCase(strPercentComplete) && !"0".equalsIgnoreCase(strPercentComplete))
												{
													HashMap paramMap = new HashMap();
													HashMap programMap = new HashMap();
													HashMap requestMap = new HashMap();
													String[] arrJPOArguments = new String[1];

													Locale locale = new Locale("en_US");

													paramMap.put("New Value", strPercentComplete);
													paramMap.put("Old Value", null);
													paramMap.put("objectId", strTaskId);
													programMap.put("paramMap", paramMap);

													requestMap.put("locale", locale);
													programMap.put("requestMap", requestMap);

													arrJPOArguments = JPO.packArgs(programMap);
													fw.write("\n\t\t\t----------updateTaskPercentageComplete start----------"+strPercentComplete); fw.flush();
													JPO.invoke(context,"emxTaskBase",null,"updateTaskPercentageComplete",arrJPOArguments);
													fw.write("\n\t\t\t----------updateTaskPercentageComplete finish----------"); fw.flush();
												}
											}
										}
									}
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}
	}

	public void readAndImportCSVLowestLevelTask_Estimated(Context context, String[] args) throws FrameworkException, IOException
	{
		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);		

		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "EstimatedCSVsToBeUpdated";

		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			System.out.println("\n...nFileListSize = "+nFileListSize);
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();
				//logFile.write("\n...Is it a file = "+bIsFile);    logFile.flush();
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					//logFile.write("\n...Name of the file ...."+strFileName);    logFile.flush();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".csv"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile =strfolderPath + "EstimatedCSVsToBeUpdated//Logs//"+sTemp+"_importProjectDetailsLog.log";
							fw = new FileWriter(strNameofLogFile);
							int iTemp = 0;

							HashMap requestMap = new HashMap();
							MapList taskSchedulingInfoList = new MapList();
							String strProjectId = "";
							while((strLine = br.readLine()) != null)
							{
								if (iTemp == 0) // for skipping first row in file
								{
									iTemp = 1;
									continue;
								}
								//System.out.println("\n..strLine = "+strLine);

								StringList slTemp = FrameworkUtil.split(strLine, ",");

								if(null != slTemp && slTemp.size() > 0)
								{									
									String strTaskNameFromCSV = ((String)slTemp.get(0)).trim();
									String strTaskId = ((String)slTemp.get(1)).trim();
									String strEstDuration = ((String)slTemp.get(2)).trim();

									String strEstStartDate = ((String)slTemp.get(3)).trim();
									String strEstFinishDate = ((String)slTemp.get(4)).trim();

									SimpleDateFormat MATRIX_DATE_FORMAT = new SimpleDateFormat ("M/d/yyyy");
									java.text.SimpleDateFormat testDate = new java.text.SimpleDateFormat("MM/dd/yy");

									String strTaskUniqueId = ((String)slTemp.get(5)).trim();

									fw.write("\n....slTemp...."+slTemp); fw.flush();

									task.setId(strTaskId);

									StringList busSelect = new StringList();
									busSelect.add("name");
									busSelect.add("to[Project Access Key].from.from[Project Access List].to.id");
									busSelect.add("attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE+"]");
									busSelect.add("attribute[Task Estimated Finish Date]");
									busSelect.add("attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION+"]");

									Map taskInfo = task.getInfo(context, busSelect);
									fw.write("\n\t....taskInfo...."+taskInfo); fw.flush();
									String strTaskName = (String)taskInfo.get("name");
									strProjectId = (String)taskInfo.get("to[Project Access Key].from.from[Project Access List].to.id");
									String strTaskEstStartDate = (String)taskInfo.get("attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE+"]");
									String strTaskEstDuration = (String)taskInfo.get("attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION+"]");

									StringList slSubtasks = task.getInfoList(context, SELECT_SUBTASK_IDS);
									boolean bIsSummaryTask = !(slSubtasks == null || slSubtasks.size() == 0);
									fw.write("\n\t....isSummaryTask...."+bIsSummaryTask); fw.flush();

									if(null != strTaskName && !"".equalsIgnoreCase(strTaskName) && !"null".equalsIgnoreCase(strTaskName))
									{
										if(!bIsSummaryTask)
										{
											fw.write("\n\t========================----------strTaskName----------"+strTaskName); fw.flush();											
											fw.write("\n\t----------strEstStartDate----------"+strEstStartDate); fw.flush();
											fw.write("\n\t----------strEstFinishDate----------"+strEstFinishDate); fw.flush();
											fw.write("\n\t----------strEstDuration----------"+strEstDuration); fw.flush();

											strEstDuration = strEstDuration.substring(0, strEstDuration.lastIndexOf("."));
											String strEstDurationToCheck = strEstDuration + ".0";
											strEstDuration = strEstDuration + " d";
											
											String strEstStartDateToCheck = strEstStartDate;
											SimpleDateFormat formatter = new SimpleDateFormat ("MMM d, yyyy");											
											java.util.Date date = new Date(strEstStartDateToCheck);
											strEstStartDateToCheck =  formatter.format(date);
											
											HashMap taskDateMap = new HashMap();
											HashMap programMap = new HashMap();

											Locale locale = new Locale("en_US");											

											TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
											double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
											double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
											
											SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
											SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getInputDateFormat(), Locale.US);											
											Date sEstJavaDate           = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strEstStartDate);
											Date sTaskEstJavaDate           = sdf1.parse(strTaskEstStartDate);
											
											Calendar calEstJavaDate = Calendar.getInstance();
											Calendar calTaskEstJavaDate = Calendar.getInstance();

											calEstJavaDate.setTime(sEstJavaDate);
											calTaskEstJavaDate.setTime(sTaskEstJavaDate); 
											
											calEstJavaDate.set(Calendar.HOUR,0);
											calEstJavaDate.set(Calendar.HOUR_OF_DAY,0);
											calEstJavaDate.set(Calendar.MINUTE,0);
											calEstJavaDate.set(Calendar.SECOND,0);
											strEstStartDate = simpleDateFormat.format(calEstJavaDate.getTime());
											
											calTaskEstJavaDate.set(Calendar.HOUR,0);
											calTaskEstJavaDate.set(Calendar.HOUR_OF_DAY,0);
											calTaskEstJavaDate.set(Calendar.MINUTE,0);
											calTaskEstJavaDate.set(Calendar.SECOND,0);
											strTaskEstStartDate = simpleDateFormat.format(calTaskEstJavaDate.getTime());
											
											strEstStartDate = strEstStartDate.trim();
											strTaskEstStartDate = strTaskEstStartDate.trim();
											strEstDurationToCheck = strEstDurationToCheck.trim();
											strTaskEstDuration = strTaskEstDuration.trim();
											
											fw.write("\n\t----------Check Date----------"+(strEstStartDate.equals(strTaskEstStartDate))); fw.flush();
											fw.write("\n\t----------Check Duration----------"+(strEstDurationToCheck.equalsIgnoreCase(strTaskEstDuration))); fw.flush();
											fw.write("\n\t----------Check Condition----------"+(!(strEstDurationToCheck.equalsIgnoreCase(strTaskEstDuration)) || !(strEstStartDate.equals(strTaskEstStartDate)))); fw.flush();

											if(!(strEstDurationToCheck.equalsIgnoreCase(strTaskEstDuration)) || !(strEstStartDate.equals(strTaskEstStartDate)))
											{
												requestMap.put("objectId", strProjectId);
												requestMap.put("locale",locale);
												requestMap.put("timeZone",clientTZOffset);

												taskDateMap.put("objectId",strTaskId);
												if(!(strEstDurationToCheck.equalsIgnoreCase(strTaskEstDuration)))
													taskDateMap.put("PhaseEstimatedDuration",strEstDuration);
												else
													taskDateMap.put("PhaseEstimatedDuration","");

												taskDateMap.put("EstimatedDuration","");
												if(!(strEstStartDate.equals(strTaskEstStartDate)))
													taskDateMap.put("PhaseEstimatedStartDate",strEstStartDateToCheck);
												else
													taskDateMap.put("PhaseEstimatedStartDate","");

												taskDateMap.put("PhaseEstimatedEndDate","");
												taskDateMap.put("Constraint Date","");
												taskDateMap.put("ConstraintType","");   	

												taskSchedulingInfoList.add(taskDateMap);

												fw.write("\n\t\t\t----------taskDateMap----------"+taskDateMap); fw.flush();	
											}																			
										}
									}
								}
							}
							updateTaskSchedulingInfo(context, requestMap, taskSchedulingInfoList,args,fw);

							//Add rollup logic here
							//boolean enableAutoschedule = ProgramCentralUtil.enableAutoSchedule(context);
							boolean enableAutoschedule = false;
							if(enableAutoschedule){
								Task project = new Task(strProjectId);
								project.rollupAndSave(context);
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}
	}

	private void updateTaskSchedulingInfo(Context context,Map requestMap, MapList taskSchedulingInfoList, String [] args, FileWriter fw)throws Exception 
	{
		emxTaskBase_mxJPO baseJPO = new emxTaskBase_mxJPO(context, args);
		try{
			String SELECT_IS_TASK_MANAGEMENT    =   "type.kindof["+DomainConstants.TYPE_TASK_MANAGEMENT+"]";
			String ATTRIBUTE_SCHEDULED_FROM = PropertyUtil.getSchemaProperty(context,"attribute_ScheduleFrom");
			String SELECT_ATTRIBUTE_SCHEDULED_FROM = "attribute["+ATTRIBUTE_SCHEDULED_FROM+"]";

			String SCHEDULE_FROM_START = "Project Start Date";
			String SCHEDULE_FROM_FINISH = "Project Finish Date";

			String ATTRIBUTE_SCHEDULE = PropertyUtil.getSchemaProperty("attribute_Schedule");
			String SELECT_ATTRIBUTE_SCHEDULE = "attribute["+ATTRIBUTE_SCHEDULE+"]";
			StringList stateMsgNameList = new StringList();
			StringList summaryTaskNameList = new StringList();
			StringList constraintDateTaskList = new StringList();


			String []taskIdArr = new String[taskSchedulingInfoList.size()];
			int tasksize = taskSchedulingInfoList.size();
			for (int i=0; i<tasksize ;i++) {
				Map<String,String> taskDateMap 	= (Map<String,String>)taskSchedulingInfoList.get(i);
				String taskId 					= taskDateMap.get("objectId");
				taskIdArr[i] 					= taskId;
			}

			String projectSchedule 		= EMPTY_STRING;
			String projectId = (String)requestMap.get("objectId");
			if(ProgramCentralUtil.isNotNullString(projectId)){
				DomainObject project = DomainObject.newInstance(context, projectId);

				StringList projectSelectable = new StringList(3);
				projectSelectable.addElement(SELECT_ATTRIBUTE_SCHEDULED_FROM);
				projectSelectable.addElement(SELECT_IS_TASK_MANAGEMENT);
				projectSelectable.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);

				Map<String,String> projectInfo = project.getInfo(context, projectSelectable);
				String isTaskManagement = projectInfo.get(SELECT_IS_TASK_MANAGEMENT);

				if("true".equalsIgnoreCase(isTaskManagement)){
					projectId = projectInfo.get(ProgramCentralConstants.SELECT_PROJECT_ID);
					projectSelectable.clear();
					projectSelectable.addElement(SELECT_ATTRIBUTE_SCHEDULED_FROM);

					project = DomainObject.newInstance(context, projectId);
					projectInfo = project.getInfo(context, projectSelectable);

					projectSchedule 		= projectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_FROM);
				}else{
					projectSchedule 		= projectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_FROM);
				}
			}

			String SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "].inputunit";
			StringList selectable = new StringList(14);
			selectable.addElement(SELECT_NAME);
			selectable.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
			selectable.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			selectable.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
			selectable.addElement(ProjectManagement.SELECT_TASK_CONSTRAINT_DATE);
			selectable.addElement(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);
			selectable.addElement(ProjectManagement.SELECT_CURRENT);
			selectable.addElement(SELECT_HAS_SUBTASK);
			selectable.addElement(SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
			selectable.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			selectable.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);

			BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, taskIdArr, selectable);
			SimpleDateFormat MATRIX_DATE_FORMAT = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);

			Locale locale = (Locale)requestMap.get("locale");
			if(null==locale){
				locale = (Locale)requestMap.get("localeObj");
			}

			double clientTZOffset 	= (Double)(requestMap.get("timeZone"));
			int iDateFormat 		= eMatrixDateFormat.getEMatrixDisplayDateFormat();
			DateFormat format 		= DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);

			Calendar calDate = Calendar.getInstance();

			for (int i=0; i<tasksize ;i++) {
				int counter = 0;
				Map<String,String> taskDateMap 	= (Map<String,String>)taskSchedulingInfoList.get(i);
				String taskId 					= taskDateMap.get("objectId");
				String taskEstDuration			= taskDateMap.get("PhaseEstimatedDuration");
				String taskEstDurationForTemp	= taskDateMap.get("EstimatedDuration");
				String taskEstStartDate 		= taskDateMap.get("PhaseEstimatedStartDate");
				String taskEstEndDate 			= taskDateMap.get("PhaseEstimatedEndDate");
				String taskEstCostraintDate		= taskDateMap.get("Constraint Date");
				String taskEstCostraintType		= taskDateMap.get("ConstraintType");   				

				BusinessObjectWithSelect bws = bwsl.getElement(i);
				String taskName             = bws.getSelectData(SELECT_NAME);
				String taskOldEstDuration	= bws.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
				String taskOldEstStartDate	= bws.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				String taskOldEstFinishDate	= bws.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				String taskConstraintType	= bws.getSelectData(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);
				String taskConstraintDate	= bws.getSelectData(ProjectManagement.SELECT_TASK_CONSTRAINT_DATE);
				String taskState			= bws.getSelectData(ProjectManagement.SELECT_CURRENT);
				String isSummaryTask		= bws.getSelectData(SELECT_HAS_SUBTASK);
				String taskEstDurationUnit	= bws.getSelectData(SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
				String isKindOfProjectSpace = bws.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String isKindOfProjectConcept = bws.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);

				StringBuilder mqlQuery = new StringBuilder();
				mqlQuery.append("modify bus $"+ ++counter);

				List<String> queryParameterList = new ArrayList<String>();
				queryParameterList.add(taskId);

				boolean isDurationUpdated = false;

				//Estimated duration
				if(ProgramCentralUtil.isNotNullString(taskEstDuration) || ProgramCentralUtil.isNotNullString(taskEstDurationForTemp)){ 

					if(!baseJPO.checkEditable(0, taskState, isSummaryTask)){
						if("true".equalsIgnoreCase(isSummaryTask)){
							summaryTaskNameList.add(taskName);
						}else{
							stateMsgNameList.add(taskName);	
						}
						continue;
					}

					mqlQuery.append(" $"+ ++counter);
					mqlQuery.append(" $"+ ++counter);

					queryParameterList.add(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION);
					if(ProgramCentralUtil.isNotNullString(taskEstDurationForTemp)){
						taskEstDuration = taskEstDurationForTemp;
					}
					queryParameterList.add(taskEstDuration);

					isDurationUpdated = true;

				}

				//Estimated Start Date
				if(ProgramCentralUtil.isNotNullString(taskEstStartDate)) {

					if(!baseJPO.checkEditable(1, taskState, isSummaryTask)){
						stateMsgNameList.add(taskName);
						continue;
					}

					taskEstStartDate	= eMatrixDateFormat.getFormattedInputDate(context,taskEstStartDate, clientTZOffset,locale);
					taskEstStartDate	= eMatrixDateFormat.getFormattedDisplayDateTime(context, taskEstStartDate, true,iDateFormat, clientTZOffset,locale);
					Date taskEstDate	= format.parse(taskEstStartDate);

					calDate.clear();
					calDate.setTime(taskEstDate);
					calDate.set(Calendar.HOUR_OF_DAY, 8);
					calDate.set(Calendar.MINUTE, 0);
					calDate.set(Calendar.SECOND, 0); 

					String duration = "";
					if(ProgramCentralUtil.isNotNullString(taskEstDuration)){
						duration= taskEstDuration;
					}else{
						duration= taskOldEstDuration;
					}

					//taskEstDate = computeStartDate(context, duration, calDate.getTime());

					taskEstStartDate = MATRIX_DATE_FORMAT.format(calDate.getTime());

					mqlQuery.append(" $"+ ++counter);
					mqlQuery.append(" $"+ ++counter);

					queryParameterList.add(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE);
					queryParameterList.add(taskEstStartDate);

					if(ProgramCentralUtil.isNullString(taskEstCostraintType)){
						mqlQuery.append(" $"+ ++counter);
						mqlQuery.append(" $"+ ++counter);

						queryParameterList.add(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_DATE);
						queryParameterList.add(taskEstStartDate);

						if(SCHEDULE_FROM_START.equalsIgnoreCase(projectSchedule)){

							mqlQuery.append(" $"+ ++counter);
							mqlQuery.append(" $"+ ++counter);

							queryParameterList.add(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE);
							queryParameterList.add(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET);

						}
					}

				}

				fw.write("\n\n----------mqlQuery.toString()----------"+mqlQuery.toString()); fw.flush();
				fw.write("\n\n---------queryParameterList----------"+queryParameterList); fw.flush();
				//updating estimated value of task 
				if(queryParameterList.size() > 1){
					String[] queryParameterArray = new String[queryParameterList.size()];
					queryParameterList.toArray(queryParameterArray);

					MqlUtil.mqlCommand(context, false, false, mqlQuery.toString(), true,queryParameterArray);
				}
			}
			//Processing for Error/warning msg
			String summaryTaskName = EMPTY_STRING;
			String stateMsgName = EMPTY_STRING;
			String constraintDateTask = EMPTY_STRING;
			fw.write("\n\n---------stateMsgNameList----------"+stateMsgNameList); fw.flush();
			fw.write("\n\n---------summaryTaskNameList----------"+summaryTaskNameList); fw.flush();
			fw.write("\n\n---------constraintDateTaskList----------"+constraintDateTaskList); fw.flush();
			if(!stateMsgNameList.isEmpty()||!summaryTaskNameList.isEmpty()|| !constraintDateTaskList.isEmpty()){
				
				fw.write("\n\n---------In here for errors----------"); fw.flush();
				if(!stateMsgNameList.isEmpty()){
					stateMsgName = stateMsgNameList.toString();
				}
				if(!summaryTaskNameList.isEmpty()){
					summaryTaskName = summaryTaskNameList.toString();
				}
				if(!constraintDateTaskList.isEmpty()){
					constraintDateTask =constraintDateTaskList.toString();
				}

				String strErrorMsg = "emxProgramCentral.WBS.TaskCannotUpdated";
				String[] messageValues = new String[4];
				messageValues[0] = stateMsgName.toString();
				messageValues[1] = summaryTaskName.toString();
				messageValues[2] = constraintDateTask.toString();
				strErrorMsg = MessageUtil.getMessage(context,
						null,
						strErrorMsg,
						messageValues,
						null,
						context.getLocale(),
						ProgramCentralConstants.RESOURCE_BUNDLE);

				MqlUtil.mqlCommand(context, "notice " + strErrorMsg);

			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void updateTaskState(Context context, String [] args) throws FrameworkException, IOException
	{

		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		FileWriter fw;
		String strWorkspaceFolder = strfolderPath + "TaskStates";
		// FILE OBJECT OF THE SHARED LOCATION FOLDER
		File folder = new File(strWorkspaceFolder.trim());
		// LIST OF FILES/FOLDERS IN SHARED LOCATION FOLDER
		File[] listOfFiles = folder.listFiles();
		if(null != listOfFiles)
		{
			// ITERATE OVER THE CONTENTS OF SHARED LOCATION FOLDER
			int nFileListSize = listOfFiles.length;
			for (int nFileCount = 0; nFileCount < nFileListSize; nFileCount++)
			{
				// CHECK IF ITEM FETCHED FROM SHARED LOCATION FOLDER IS A FILE OR NOT
				boolean bIsFile = listOfFiles[nFileCount].isFile();
				//logFile.write("\n...Is it a file = "+bIsFile);    logFile.flush();
				System.out.println("\n...Is it a file = "+bIsFile);
				if (bIsFile)
				{
					// NAME OF THE FILE IN THE SHARED LOCATION
					String strFileName = listOfFiles[nFileCount].getName();
					//logFile.write("\n...Name of the file ...."+strFileName);    logFile.flush();
					System.out.println("\n..Name of the file = "+strFileName);
					// CHECK THE EXTENSION OF THE FILE
					if(match(strFileName, ".csv"))
					{
						boolean bAnyFileProcessed = true;
						// PATH & NAME OF THE FILE IN THE SHARED LOCATION
						String strFileSource = strWorkspaceFolder.trim() + java.io.File.separatorChar + strFileName;
						//logFile.write("\n...strFileSource ...."+strFileSource);    logFile.flush();
						System.out.println("\n..strFileSource = "+strFileSource);
						// PROCESSING START
						FileInputStream fis = new FileInputStream(strFileSource);
						DataInputStream dr = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dr));
						String strLine;
						try
						{
							String sTemp = strFileName.substring(0, strFileName.length()-4);							
							String strNameofLogFile = strfolderPath + "TaskStates//Logs//"+sTemp+"_importProjectDetailsLog.log";
							fw = new FileWriter(strNameofLogFile);
							int iTemp = 0;
							Map parentTaskMap = new HashMap();
							while((strLine = br.readLine()) != null)
							{
								if (iTemp == 0) // for skipping first row in file
								{
									iTemp = 1;
									continue;
								}
								//System.out.println("\n..strLine = "+strLine);

								StringList slTemp = FrameworkUtil.split(strLine, ",");

								if(null != slTemp && slTemp.size() > 0)
								{									
									String strTaskNameFromCSV = ((String)slTemp.get(0)).trim();
									String strTaskId = ((String)slTemp.get(1)).trim();
									String strTaskStateFromCSV = ((String)slTemp.get(2)).trim();

									fw.write("\n....slTemp...."+slTemp); fw.flush();

									task.setId(strTaskId);

									StringList busSelect = new StringList();
									busSelect.add("name");
									busSelect.add("current");
									//busSelect.add("from[Subtask].attribute[Sequence Order]");
									Map taskInfo = task.getInfo(context, busSelect);
									fw.write("\n\t....taskInfo...."+taskInfo); fw.flush();
									String strTaskName = (String)taskInfo.get("name");
									String strTaskState = (String)taskInfo.get("current");

									StringList slSubtasks = task.getInfoList(context, SELECT_SUBTASK_IDS);
									boolean bIsSummaryTask = !(slSubtasks == null || slSubtasks.size() == 0);
									fw.write("\n\t....isSummaryTask...."+bIsSummaryTask); fw.flush();

									if(null != strTaskName && !"".equalsIgnoreCase(strTaskName) && !"null".equalsIgnoreCase(strTaskName))
									{
										if(!bIsSummaryTask)
										{
											fw.write("\n\t========================----------strTaskName----------"+strTaskName); fw.flush();
											fw.write("\n\t----------strTaskStateFromCSV----------"+strTaskStateFromCSV); fw.flush();
											fw.write("\n\t----------strTaskState----------"+strTaskState); fw.flush();

											if(null != strTaskState && !"".equalsIgnoreCase(strTaskState) && !"null".equalsIgnoreCase(strTaskState))
											{
												if(!strTaskStateFromCSV.equalsIgnoreCase(strTaskState))
												{
													task.setState(context, strTaskStateFromCSV);
												}
											}
										}
										else
										{
											if(null != strTaskState && !"".equalsIgnoreCase(strTaskState) && !"null".equalsIgnoreCase(strTaskState))
											{
												if(!strTaskStateFromCSV.equalsIgnoreCase(strTaskState))
												{
													parentTaskMap.put(strTaskId, strTaskStateFromCSV);
												}
											}
										}

									}									
								}
							}

							fw.write("\n\t========================----------parentTaskMap----------"+parentTaskMap); fw.flush();
							// update parent task states
							if(!parentTaskMap.isEmpty() && null!=parentTaskMap)
							{
								Iterator objectIdItr = parentTaskMap.keySet().iterator();
								while(objectIdItr.hasNext())
								{
									String strTaskId = (String)objectIdItr.next();
									String strTaskStateFromCSV = (String)parentTaskMap.get(strTaskId);

									fw.write("\n\t========================----------strTaskName----------"+strTaskId); fw.flush();
									fw.write("\n\t----------strTaskStateFromCSV----------"+strTaskStateFromCSV); fw.flush();

									task.setId(strTaskId);
									task.setState(context, strTaskStateFromCSV);
								}
							}
						}
						catch(Exception ex)
						{
							//	logFile.write("\n Error while processing file.");    logFile.flush();
							throw new FrameworkException(ex);
						}
					}
				}
			}
		}
	}
}