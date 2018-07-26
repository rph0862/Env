//
/* 
Change History:
Date       	   |	Change By 	 | Tag to be searched |		  Details
===========================================================================================================
12-Nov-2014    |   Intelizign	 |	12-Nov-2014       | PE Build 2.1 - Changes made to show delay status icon on parent task if any of the child task is delayed
*/
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.ProgramCentralConstants;
import java.io.*;
import matrix.db.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import java.util.ArrayList;
import java.util.List;
import matrix.db.JPO;
import matrix.util.StringList;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.domain.util.ContextUtil;
 


// $Id: emxProjectSpace.java.rca 1.6 Wed Oct 22 16:21:26 2008 przemek Experimental przemek $ 
//
// emxProjectSpace.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
 

/**
 * The <code>emxProjectSpace</code> class represents the Project Space JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectSpace_mxJPO extends emxProjectSpaceBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectSpace_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * Constructs a new emxProjectSpace JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param String the business object id
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     */
    public emxProjectSpace_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }
    
    /**
     * This method is used to show the status image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector containing all the status image value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     * 
     *  Method moved from Base JPO to non-base JPO to include customization for MSIL
     */

    public Vector getStatusIcon(Context context, String[] args) throws Exception
    {
        Vector showIcon = new Vector();
        String languageString = context.getSession().getLanguage();
        String stooltip=ProgramCentralConstants.EMPTY_STRING;
        String sStatusOnTime = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.OnTime",languageString);
        String sStatusLate = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Late",languageString);
        String sStatusBehindSchedule = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.BehindSchedule",languageString);

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            int actionListSize = objectList.size();
            Map objectMap = null;
            int i = 0;
            String strProjectId = "";
            String statusGif = "";
            String strProjetDelay ="";
            String strHasChild  =   "";
            for (i = 0; i < actionListSize; i++)
            {
                objectMap = (Map) objectList.get(i);
               
                strProjetDelay = "";
                strProjectId = (String)objectMap.get(SELECT_ID);
                strHasChild  = (String)objectMap.get("from[Subtask]");

                //Call the method to get project status
                strProjetDelay = getTaskStatusColor(context, strProjectId , strHasChild );
                if (strProjetDelay.equals("Green"))
                {
                    statusGif = "<img src=\"images/iconStatusGreen.gif\" border=\"0\" title=\"";
                    statusGif += (sStatusOnTime + "\"/>");
    		    stooltip=sStatusOnTime;
                } else if (strProjetDelay.equals("Red"))
                {
                    statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" title=\"";
                    statusGif += (sStatusLate + "\"/>");
    		    stooltip=sStatusLate;
		    
                } 
		
		else if (strProjetDelay.equals("Yellow"))
                {
                    statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" title=\"";
                    statusGif += (sStatusBehindSchedule + "\"/>");
    		    stooltip=sStatusBehindSchedule;
                } else {
                    statusGif = ProgramCentralConstants.EMPTY_STRING;
                }
    			//showIcon.addElement(statusGif);
    			StringBuffer sBuff = new StringBuffer();
    			sBuff.append("<p title=\""+stooltip+"\">");
    			sBuff.append(statusGif);
    			sBuff.append("</p>");
    			showIcon.add(sBuff.toString());
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
    }





    /*public Vector getStatusIcon_OLD(Context context, String[] args) throws Exception
    {
        Vector showIcon = new Vector();
        com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
            Map objectMap = null;
            int i = 0;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
                i++;
            }

            StringList busSelect = new StringList(6);
            busSelect.add(project.SELECT_BASELINE_CURRENT_END_DATE);
            busSelect.add(project.SELECT_TASK_ESTIMATED_FINISH_DATE);
            busSelect.add(project.SELECT_TASK_ACTUAL_FINISH_DATE);
            busSelect.add(project.SELECT_CURRENT);
            busSelect.add(project.SELECT_TYPE);
            
            // MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
            busSelect.add(project.SELECT_ID);
            // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014

            MapList actionList = DomainObject.getInfo(context, objIdArr, busSelect);

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            //set the yellow red threshold from the properties file
            int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));

            Date tempDate = new Date();
            Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());

            String statusGif = "";
            Date baselineCurrentEndDate = null;
            String strProjectId = "";
            String strProjetDelay = "";
            MapList tasksForProject = new MapList();
            StringList slTaskStatusList = new StringList(10, 2);
            String baselineCurrentEndDateString = "";
            Date estFinishDate = new Date();

            // Define selectables for each Task object.
            StringList taskSelects = new StringList(7);
            taskSelects.add(DomainConstants.SELECT_ID);
            taskSelects.add(DomainConstants.SELECT_TYPE);
            taskSelects.add(task.SELECT_BASELINE_CURRENT_END_DATE);
            taskSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            taskSelects.add(task.SELECT_CURRENT);
            taskSelects.add(task.SELECT_PERCENT_COMPLETE);

            for (i = 0; i < actionListSize; i++)
            {
                //determine which gif should be display for status
                statusGif = "";
                baselineCurrentEndDate = null;
                objectMap = (Map) actionList.get(i);

                // MSIL CHANGES START BY INTELIZIGN - 12-Nov-2014
                strProjectId = (String)objectMap.get(project.SELECT_ID);
                project.setId(strProjectId);
               
                tasksForProject = project.getTasks(context, 0, taskSelects, null, false);
                
                slTaskStatusList = getTaskStatusIcon(context, tasksForProject);
                
                strProjetDelay = "";
                if(slTaskStatusList.contains("Red"))
                    strProjetDelay = "Red";
                else if(slTaskStatusList.contains("Yellow"))
                    strProjetDelay = "Yellow";
                
                // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                baselineCurrentEndDateString = (String) objectMap.get(project.SELECT_BASELINE_CURRENT_END_DATE);

                estFinishDate = sdf.parse((String) objectMap.get(project.SELECT_TASK_ESTIMATED_FINISH_DATE));

                if (!"".equals(baselineCurrentEndDateString))
                {
                    baselineCurrentEndDate = sdf.parse((String) objectMap.get(project.SELECT_BASELINE_CURRENT_END_DATE));
                }
                long daysRemaining;
                if (null == baselineCurrentEndDate)
                {
                    daysRemaining = (long) DateUtil.computeDuration(sysDate, estFinishDate);
                }
                else
                {
                    daysRemaining = (long) DateUtil.computeDuration(sysDate, baselineCurrentEndDate);
                }

                // determine which status gif to display
                if (null == baselineCurrentEndDate)
                {
                    if (objectMap.get(project.SELECT_TYPE).equals(project.TYPE_PROJECT_CONCEPT))
                    {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                    else
                    {
                        if (objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE))
                        {
                            statusGif = "<img src=\"images/iconStatusGreen.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.OnTime", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        }
                        else if (!objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE) && sysDate.after(estFinishDate))
                        {
                            statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        }
                        else if (!objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE) && (daysRemaining <= yellowRedThreshold))
                        {
                            statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
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
                        		statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" alt=\"";
                        		statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        	}
                        	else if("Yellow".equalsIgnoreCase(strProjetDelay))
                        	{
                        		statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                        		statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        	}
                        }
                        // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                    }
                }
                else
                {
                    if (objectMap.get(project.SELECT_TYPE).equals(project.TYPE_PROJECT_CONCEPT))
                    {
                        statusGif = ProgramCentralConstants.EMPTY_STRING;
                    }
                    else
                    {
                        if (objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE))
                        {
                            statusGif = "<img src=\"images/iconStatusGreen.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.OnTime", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        }
                        else if (!objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE) && sysDate.after(baselineCurrentEndDate))
                        {
                            statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        }
                        else if (!objectMap.get(project.SELECT_CURRENT).equals(STATE_PROJECT_COMPLETE) && (daysRemaining <= yellowRedThreshold))
                        {
                            statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                            statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
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
                        		statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" alt=\"";
                        		statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Late", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        	}
                        	else if("Yellow".equalsIgnoreCase(strProjetDelay))
                        	{
                        		statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" alt=\"";
                        		statusGif += (i18nNow.getI18nString("emxProgramCentral.Common.Legend.BehindSchedule", "emxProgramCentralStringResource", context.getSession().getLanguage()) + "\"/>");
                        	}
                        }
                        // MSIL CHANGES END BY INTELIZIGN - 12-Nov-2014
                    }
                }
                showIcon.addElement(statusGif);
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
	 * Method called on My Dashboard page - to find the status of the tasks of the project
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param mlProjectTasksList
	 *            
	 * @throws Exception
	 *             if the operation fails
	 *             
	 * @return StringList
	 * 			List of the task status (green,yellow,red,blank)
	 */
    public StringList getTaskStatusIcon(Context context, MapList mlProjectTasksList) throws Exception
    {
    	StringList slTaskStatusIconList = new StringList();

    	com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
    	String policyName = task.getDefaultPolicy(context);
    	String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy", policyName, "state_Complete");
    	try
    	{
    		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
    		Map objectMap = null;
    		int i = 0;
 
    		int actionListSize = 0;
    		if (mlProjectTasksList != null)
    		{
    			actionListSize = mlProjectTasksList.size();
    		}

    		int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
    		Date tempDate = new Date();
    		Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());

    		for (i = 0; i < actionListSize && mlProjectTasksList != null; i++)
    		{
    			String statusGif = "";
    			objectMap = (Map) mlProjectTasksList.get(i);
    			
    			Date baselineCurrentEndDate = null;
    			String baselineCurrentEndDateString = (String) objectMap.get(task.SELECT_BASELINE_CURRENT_END_DATE);
    			Date estFinishDate = sdf.parse((String) objectMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
    	
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
    					statusGif = "Green";
    				}
    				else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && sysDate.after(estFinishDate))
    				{
    					statusGif = "Red";
    				}
    				else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && daysRemaining <= yellowRedThreshold)
    				{
    					statusGif = "Yellow";
    				}
    				else
    				{
    					statusGif = ProgramCentralConstants.EMPTY_STRING;
    				}
    			}
    			else
    			{
    				if (objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) || ((String) objectMap.get(task.SELECT_PERCENT_COMPLETE)).equals("100"))
    				{
    					statusGif = "Green";
    				}
    				else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && sysDate.after(baselineCurrentEndDate))
    				{
    					statusGif = "Red";
    				}
    				else if (!objectMap.get(task.SELECT_CURRENT).equals(COMPLETE_STATE) && (daysRemaining <= yellowRedThreshold))
    				{
    					statusGif = "Yellow";
    				}
    				else
    				{
    					statusGif = ProgramCentralConstants.EMPTY_STRING;
    				}
    			}    			
    			slTaskStatusIconList.add(statusGif);
    			
    			// THIS IS DONE TO STOP UNNECESSARY PROCESSING OF THE TASKS. IF ANY ONE OF THE CHILD TASK IS DELAYED, THE TOP LEVEL PROJECT ALSO HAS TO BE SHOWN RED, SO BREAKING THE LOOP
    			if("Red".equalsIgnoreCase(statusGif))
    				break;
    		}
    	}
    	catch (Exception ex)
    	{
    		throw ex;
    	}
    	finally
    	{
    		return slTaskStatusIconList;
    	}
    }



    public String getTaskStatusColor(Context context, String strProjId , String strHasChild) throws Exception
    {
        String strStatusColor = "";
        com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);

        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        String policyName = task.getDefaultPolicy(context);
        String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy", policyName, "state_Complete");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        Date tempDate = new Date();
        Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
          int yellowRedThreshold = Integer.parseInt(FrameworkProperties.getProperty("eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
        try
        {
            project.setId(strProjId);
            StringList taskSelects = new StringList();
            taskSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);

            String strWhere = "current != '" + COMPLETE_STATE + "'";
            //MapList tasksForProject = project.getAllWBSTasks(context, taskSelects, strWhere);

             MapList tasksForProject = project.getRelatedObjects(context,
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
            if (null == tasksForProject || tasksForProject.size() == 0) {
                strStatusColor = "Green";

                String strEstFinishDate = (String) project.getInfo(context , task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                Date estFinishDate = sdf.parse(strEstFinishDate);
                long daysRemaining = (long) task.computeDuration(sysDate, estFinishDate);
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
            } else {
                tasksForProject.sort(project.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
                Map mFirstTask = (Map) tasksForProject.get(0);
                String strEstFinishDate = (String) mFirstTask.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                Date estFinishDate = sdf.parse(strEstFinishDate);
                long daysRemaining = (long) task.computeDuration(sysDate, estFinishDate);
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
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return strStatusColor;
    }
	
	
	/** 
	 * Method will connect Managers of context user with Project as Member
	 * Method will copy only if person is from CIVIL Dept and Owner of Project
	 * Memebers will be copied if Person creating Project is from CIVIL Dept
	 * @param context the eMatrix <code>Context</code> object    
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public void copyReportingStructure(Context context, String[] args) throws Exception {
		try {
			 com.matrixone.apps.program.ProjectSpace project =
					 (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
							 DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			String strProjectId = args[0];
			DomainObject domProject = DomainObject.newInstance(context, strProjectId);
			String contextUser = context.getUser();
			String sProjectOwner  = domProject.getInfo(context, DomainConstants.SELECT_OWNER);
			MSILAccessRights_mxJPO msilAccess=new MSILAccessRights_mxJPO(context,args);
			boolean isNonCivilCIVIL  = msilAccess.hasAccessForOOTBCommand(context, "WMSCIVIL.DepartmentDashboard.ENAccess");
			if(sProjectOwner.equalsIgnoreCase(contextUser) && !isNonCivilCIVIL ) {
				 String userID      = PersonUtil.getPersonObjectID(context, contextUser);
				ArrayList<String>arrayListIds = new ArrayList<String>();
			   if(UIUtil.isNotNullAndNotEmpty(userID))
				{
				StringList strListBusSelects = new StringList(DomainConstants.SELECT_ID);
				strListBusSelects.add("attribute["+WMSConstants_mxJPO.ATTRIBUTE_WMS_DEFAULT_ROLE+"]");
				DomainObject domContextUser = DomainObject.newInstance(context,userID);
				MapList mlPerson= domContextUser.getRelatedObjects(context, // matrix context
						WMSConstants_mxJPO.RELATIONSHIP_WMS_REPORTING_MANAGER, // relationship pattern
					   DomainConstants.TYPE_PERSON, // type pattern
						strListBusSelects, // object selects
						DomainConstants.EMPTY_STRINGLIST, // relationship selects
						false, // to direction
						true, // from direction
						(short) 1, // recursion level
						DomainConstants.EMPTY_STRING, // object where clause
						DomainConstants.EMPTY_STRING, // relationship where clause
						0);
				Map m = new HashMap();
				String[] arrOid = new String[mlPerson.size()];
				String[] arrAttriute = new String[mlPerson.size()];
				String strObjId = "";
				Iterator<Map> itr = mlPerson.iterator();
				int i=0;
				while(itr.hasNext())
				{
					 m = itr.next();
					 strObjId = (String)m.get(DomainConstants.SELECT_ID);
					 arrayListIds.add(strObjId);
					 arrOid[i]=strObjId;
					 arrAttriute[i]=(String)m.get("attribute["+WMSConstants_mxJPO.ATTRIBUTE_WMS_DEFAULT_ROLE+"]");
					 i++;
				  
				}
				
				Map members = new HashMap();
				project.setId(strProjectId);
				i=0;
				for(String id : arrOid){
					  Map access = new HashMap(1);
					  access.put(MemberRelationship.ATTRIBUTE_PROJECT_ROLE, arrAttriute[i]);
					  members.put(id, access);
					 i++;
				  }
				if(members.size()>0)
				   project.addMembers(context, members);
			}
		   }
		}
		catch(Exception exception)
		{
			System.out.println("exception   "+exception.getMessage());
			exception.printStackTrace();
			throw exception;            
		}
	}
	    
   /**Method returns Last logged in Person from project member, column should be shown for all tables where projects list shown
	* 
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/


	public List getLastLoggedInUser(Context context,String[] args) throws Exception
	  {
		List<String> lstLLoggedIn = new Vector<String>(); 
		String sLoggedInUser = context.getUser();
		DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
		DateTimeFormatter dMMMDDYYYY = DateTimeFormat.forPattern("MMM-dd-yyyy hh:mm:ss a");
		 try{
			Map mInputMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList)mInputMap.get("objectList");
			Iterator<Map> itr = objectList.iterator();
			String strProjectOid=DomainConstants.EMPTY_STRING;
			String strName=DomainConstants.EMPTY_STRING;
			String strLLTime=DomainConstants.EMPTY_STRING;
			ProjectSpace ps=new ProjectSpace();
			com.matrixone.apps.common.Person person =
					(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
							DomainConstants.TYPE_PERSON);
			StringList memberSelects = new StringList(5);
			 memberSelects.add(person.SELECT_ID);
			 memberSelects.add(person.SELECT_NAME);
			 memberSelects.add(person.SELECT_FIRST_NAME);
			 memberSelects.add(person.SELECT_LAST_LOGIN_DATE);
			 memberSelects.add(person.SELECT_LAST_NAME);
			 StringList relSelects = new StringList(5);
			while(itr.hasNext()){
			  Map m = itr.next();
			  strProjectOid = (String)m.get(DomainConstants.SELECT_ID);
			  ps.setId(strProjectOid);
			  MapList mlMembers = ps.getRelatedObjects(context, 
					  ProgramCentralConstants.RELATIONSHIP_MEMBER,
					  ProgramCentralConstants.TYPE_PERSON,
					  memberSelects,
					  relSelects,
					  false, true,
					  (short)1, "name!="+sLoggedInUser, null,0);
			  mlMembers.sort(person.SELECT_LAST_LOGIN_DATE,
						"descending", "date");
			
			  if(mlMembers.size()>0){
				Map mMember = (Map)mlMembers.get(0);
				strName = (String)mMember.get(person.SELECT_NAME);
				 strLLTime = (String)mMember.get(person.SELECT_LAST_LOGIN_DATE);
				 if(!strLLTime.isEmpty()){
					LocalDateTime dWDTStartDate = dateStringFormat.parseLocalDateTime(strLLTime);
				   lstLLoggedIn.add(strName+" ("+dMMMDDYYYY.print(dWDTStartDate)+")");
				 }else{
					 lstLLoggedIn.add("");
				 }
			  
			  }else{
				 lstLLoggedIn.add("");
			  }
			}
			 
			 }catch(Exception e){
			 e.printStackTrace();
		 }
		  
		return lstLLoggedIn;  
		  
	  }
		  
		    
}
