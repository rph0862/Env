/* ${CLASSNAME}.java

Author : Intelizign
Change History:
Date       	   |	Change By 	 | Tag to be searched |		  Details
===========================================================================================================
01-July-2014   |    Intelizign   |       -			  |		Initial Release
22-Dec-2014    |    Intelizign   |    22/12/2014	  |		Changes related to User Dashboard Issues
06-Feb-2015    |    Intelizign   |    21/03/2015	  |		System to show only Master Project at all levels in "My Projects" tab
07-Apr-2015    |    Intelizign   |    07/04/2015	  |		"Dept Projects" tab to be dropped for DVM-PE & NPE DPM
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;

public class MSILUserDashboard_mxJPO  extends com.matrixone.apps.program.ProjectSpace implements MSILConstants_mxJPO
{

	public boolean showCommandToBelowDPM(Context context, String args[]) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:showCommandToBelowDPM start...");
		boolean bAccess = false;
		try
		{      MSILAccessRights_mxJPO msilAccessRts= new  MSILAccessRights_mxJPO(context,args);
		     //  MSILAccessRights msilAccessRts= new MSILAccessRights(context,args);
			  if(msilAccessRts.isPersonFromCivil(context,args)){
			    return false;
			   }
			   
			   
			String strContextUser = context.getUser().trim();

			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
			String strContextUserId = person.getPerson(context, strContextUser).getId();

			DomainObject personDO = DomainObject.newInstance(context, strContextUserId);

			boolean bIsDPMOrAbove = personDO.hasRelatedObjects(context, DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY, false);
			if(bIsDPMOrAbove)
				bAccess = false;
			else
				bAccess = true;
		}
		catch (Exception ex)
		{
			System.out.println("\n... JPO:MSILUserDashboard:showCommandToBelowDPM Exception..."+ex);
			throw new FrameworkException(ex);
		}
		System.out.println("\n... JPO:MSILUserDashboard:showCommandToBelowDPM exit..."+bAccess);
		return bAccess;
	}

	/**
	 * gets the list of active Projects owned by the user
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the ids of Project objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getActiveProjects(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getActiveProjects start...");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		MapList returnList = new MapList();
		try
		{
			String strPortalCmdName = (String)programMap.get("portalCmdName");

			MapList projectList = new MapList();
			if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName) || "MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
				projectList = getProjectSummary(context, args, STATE_PROJECT_ARCHIVE);
			else if ("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
			{
				projectList = getDeptProjects(context, args, STATE_PROJECT_ARCHIVE);
			}
            
			String strContextUser = context.getUser().trim();
			if(null != projectList && projectList.size() > 0)
			{
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
				String strProjectId  = "";
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
				Iterator listItr = projectList.iterator();
				while (listItr.hasNext())
				{
					Map projectMap = (Map) listItr.next();
                    
					String strProjectOwner  = (String) projectMap.get(DomainConstants.SELECT_OWNER);
					String strProjectType  = (String) projectMap.get(DomainConstants.SELECT_TYPE);

					if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(strProjectOwner.equalsIgnoreCase(strContextUser))
							returnList.add(projectMap);

					}
					else if("MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
					{						
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
						{
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
							// CHECK IF THE PROJECT IS A MASTER PROJECT OR NOT
							strProjectId  = (String) projectMap.get(DomainConstants.SELECT_ID);
							boolean bIsMaster = checkIsMasterProject(context, strProjectId);
                    
							if(bIsMaster)
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
								returnList.add(projectMap);
						}
					}
					else if("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
							returnList.add(projectMap);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception JPO:MSILUserDashboard:getActiveProjects :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILUserDashboard:getActiveProjects exit...");
		return returnList;
	}

	/**
	 * gets the list of complete Projects owned by the user
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the ids of Project objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCompletedProjects(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getCompletedProjects start...");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList returnList = new MapList();
		try
		{
			String strPortalCmdName = (String)programMap.get("portalCmdName");

			MapList projectList = new MapList();
			if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName) || "MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
				projectList = getProjectSummary(context, args, STATE_PROJECT_COMPLETE);
			else if ("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
			{
				projectList = getDeptProjects(context, args, STATE_PROJECT_COMPLETE);
			}
			String strContextUser = context.getUser().trim();
			if(null != projectList && projectList.size() > 0)
			{
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
				String strProjectId  = "";
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
				Iterator listItr = projectList.iterator();
				while (listItr.hasNext())
				{
					Map projectMap = (Map) listItr.next();
					String strProjectOwner  = (String) projectMap.get(DomainConstants.SELECT_OWNER);
					String strProjectType  = (String) projectMap.get(DomainConstants.SELECT_TYPE);

					if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(strProjectOwner.equalsIgnoreCase(strContextUser))
							returnList.add(projectMap);
					}
					else if("MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
						{
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
							// CHECK IF THE PROJECT IS A MASTER PROJECT OR NOT
							strProjectId  = (String) projectMap.get(DomainConstants.SELECT_ID);
							boolean bIsMaster = checkIsMasterProject(context, strProjectId);
							if(bIsMaster)
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
								returnList.add(projectMap);
						}
					}
					else if("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
							returnList.add(projectMap);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception JPO:MSILUserDashboard:getCompletedProjects :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILUserDashboard:getCompletedProjects exit...");
		return returnList;
	}

	/**
	 * gets the list of Hold Projects owned by the user
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the ids of Project objects
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getHoldProjects(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getHoldProjects start...");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList returnList = new MapList();
		try
		{
			String strPortalCmdName = (String)programMap.get("portalCmdName");

			MapList projectList = new MapList();
			if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName) || "MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
				projectList = getProjectSummary(context, args, ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
			else if ("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
			{
				projectList = getDeptProjects(context, args, ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
			}
			String strContextUser = context.getUser().trim();
			if(null != projectList && projectList.size() > 0)
			{
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
				String strProjectId  = "";
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
				Iterator listItr = projectList.iterator();
				while (listItr.hasNext())
				{
					Map projectMap = (Map) listItr.next();
					String strProjectOwner  = (String) projectMap.get(DomainConstants.SELECT_OWNER);
					String strProjectType  = (String) projectMap.get(DomainConstants.SELECT_TYPE);

					if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(strProjectOwner.equalsIgnoreCase(strContextUser))
							returnList.add(projectMap);
					}
					else if("MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
						{
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
							// CHECK IF THE PROJECT IS A MASTER PROJECT OR NOT
							strProjectId  = (String) projectMap.get(DomainConstants.SELECT_ID);
							boolean bIsMaster = checkIsMasterProject(context, strProjectId);
							if(bIsMaster)
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
								returnList.add(projectMap);
						}
					}
					else if("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
							returnList.add(projectMap);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception JPO:MSILUserDashboard:getHoldProjects :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILUserDashboard:getHoldProjects exit...");
		return returnList;
	}

	/**
	 * gets the list of Cancel Projects owned by the user
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the ids of Project objects
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCancelProjects(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getCancelProjects start...");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList returnList = new MapList();
		try
		{
			String strPortalCmdName = (String)programMap.get("portalCmdName");

			MapList projectList = new MapList();
			if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName) || "MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
				projectList = getProjectSummary(context, args, ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
			else if ("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
			{
				projectList = getDeptProjects(context, args, ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
			}
			String strContextUser = context.getUser().trim();
			if(null != projectList && projectList.size() > 0)
			{
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
				String strProjectId  = "";
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
				Iterator listItr = projectList.iterator();
				while (listItr.hasNext())
				{
					Map projectMap = (Map) listItr.next();
					String strProjectOwner  = (String) projectMap.get(DomainConstants.SELECT_OWNER);
					String strProjectType  = (String) projectMap.get(DomainConstants.SELECT_TYPE);

					if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(strProjectOwner.equalsIgnoreCase(strContextUser))
							returnList.add(projectMap);
					}
					else if("MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
						{
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
							// CHECK IF THE PROJECT IS A MASTER PROJECT OR NOT
							strProjectId  = (String) projectMap.get(DomainConstants.SELECT_ID);
							boolean bIsMaster = checkIsMasterProject(context, strProjectId);
							if(bIsMaster)
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
								returnList.add(projectMap);
						}
					}
					else if("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
							returnList.add(projectMap);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception JPO:MSILUserDashboard:getCancelProjects :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILUserDashboard:getCancelProjects exit...");
		return returnList;
	}

	/**
	 * gets the list of All Projects owned by the user
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the ids of Project objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllProjects(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getAllProjects start...");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList returnList = new MapList();
		try
		{
			String strPortalCmdName = (String)programMap.get("portalCmdName");

			MapList projectList = new MapList();
			if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName) || "MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
				projectList = getProjectSummary(context, args, null);
			else if ("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
			{
				projectList = getDeptProjects(context, args, null);
			}
			String strContextUser = context.getUser().trim();
			if(null != projectList && projectList.size() > 0)
			{
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
				String strProjectId  = "";
				// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
				Iterator listItr = projectList.iterator();
				while (listItr.hasNext())
				{
					Map projectMap = (Map) listItr.next();
					String strProjectOwner  = (String) projectMap.get(DomainConstants.SELECT_OWNER);
					String strProjectType  = (String) projectMap.get(DomainConstants.SELECT_TYPE);

					if("MSILOwnedProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(strProjectOwner.equalsIgnoreCase(strContextUser))
							returnList.add(projectMap);
					}
					else if("MSILMyProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
						{
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - Start
							// CHECK IF THE PROJECT IS A MASTER PROJECT OR NOT
							strProjectId  = (String) projectMap.get(DomainConstants.SELECT_ID);
							boolean bIsMaster = checkIsMasterProject(context, strProjectId);
							if(bIsMaster)
							// 21/03/2015 - System to show only Master Project in "My Projects" tab - End
								returnList.add(projectMap);
						}
					}
					else if("MSILDeptProjects".equalsIgnoreCase(strPortalCmdName))
					{
						if(!strProjectOwner.equalsIgnoreCase(strContextUser) && !DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strProjectType))
							returnList.add(projectMap);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception JPO:MSILUserDashboard:getAllProjects :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILUserDashboard:getAllProjects exit...");
		return returnList;
	}

	/**
	 * This method is used to gets the list of projects owned by the user.
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @param busWhere optional business object where clause
	 * @return MapList containing the id of projects owned by the user.
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public MapList getProjectSummary(Context context, String[] args, String selectState) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getProjectSummary start...");
		// Check license while listing Project Concepts, Project Space, if license check fails here
		// the projects will not be listed. This is mainly done to avoid Project Concepts from being listed
		// but as this is the common method, the project space objects will also not be listed.
		//
		//ComponentsUtil.checkLicenseReserved(context, "DPJ");

		MapList projectList = null;
		com.matrixone.apps.program.Program program = (com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM, DomainConstants.PROGRAM);
		com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");

			// Retrieve the person's project's info
			String busWhere = null;
			String busId = null; 

			String vaultPattern = "";

			String vaultOption = PersonUtil.getSearchDefaultSelection(context);

			vaultPattern = PersonUtil.getSearchVaults(context, false ,vaultOption);

			StringBuffer sbWhereClause = new StringBuffer(500);
			//use the matchlist keyword to filter by vaults, need this if option is not "All Vaults"
			if (!vaultOption.equals(PersonUtil.SEARCH_ALL_VAULTS) && vaultPattern.length() > 0)
			{
				sbWhereClause.append("vault matchlist '");
				sbWhereClause.append(vaultPattern);
				sbWhereClause.append("' ','");
			}

			if ((STATE_PROJECT_COMPLETE).equals(selectState))
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current=='");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append("'");
				}
				else
				{
					sbWhereClause.append(" && current=='");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append("'");
				}
			}
			else if ((STATE_PROJECT_ARCHIVE).equals(selectState))
			{
				// Active Projects - not in the complete state or in the archive state
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current!=");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_ARCHIVE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}
				else
				{
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_ARCHIVE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}
			}
			else if((ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD).equals(selectState))
			{
				sbWhereClause = new StringBuffer(500);
				sbWhereClause.append("current==");
				sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
			}
			else if((ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL).equals(selectState))
			{
				sbWhereClause = new StringBuffer(500);
				sbWhereClause.append("current==");
				sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
			}
			else
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}
				else
				{
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}
			}

			StringList busSelects = new StringList(2);
			busSelects.addElement(project.SELECT_VAULT);
			busSelects.add(project.SELECT_ID);
			busSelects.add(project.SELECT_NAME);
			busSelects.add(project.SELECT_TYPE);
			busSelects.add(project.SELECT_CURRENT);
			busSelects.add(project.SELECT_OWNER);
            busSelects.add("from[Subtask]");
			
			projectList = project.getUserProjects(context, person, busSelects, null, sbWhereClause.toString(), null);
		}
		catch (Exception ex)
		{
			System.out.println("\n Exception JPO:MSILUserDashboard:getProjectSummary ==="+ex);
			throw ex;
		}
		finally
		{
			System.out.println("\n... JPO:MSILUserDashboard:getProjectSummary exit...");
			return projectList;
		}
	}
	/**
	 * This method is used to gets the list of projects owned by the user.
	 * Used for PMCProjectSpaceMyDesk table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @param busWhere optional business object where clause
	 * @return MapList containing the id of projects owned by the user.
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public MapList getDeptProjects(Context context, String[] args, String selectState) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getDeptProjects start...");
		// Check license while listing Project Concepts, Project Space, if license check fails here
		// the projects will not be listed. This is mainly done to avoid Project Concepts from being listed
		// but as this is the common method, the project space objects will also not be listed.
		//
		//ComponentsUtil.checkLicenseReserved(context, "DPJ");
		MapList returnProjectList = new MapList();
		MapList projectList = null;
		com.matrixone.apps.program.Program program = (com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM, DomainConstants.PROGRAM);
		com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");

			boolean bIsDVMPE = false;
			boolean bIsDPM = false;
			boolean bIsDDVM = false;

			// Retrieve the person's project's info
			String busWhere = null;
			String busId = null;

			String vaultPattern = "";

			String vaultOption = PersonUtil.getSearchDefaultSelection(context);

			vaultPattern = PersonUtil.getSearchVaults(context, false ,vaultOption);

			StringBuffer sbWhereClause = new StringBuffer(500);
			StringBuffer sbDeptBUWhereClause = new StringBuffer(100);

			// LOGGED IN PERSON
			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			loginPerson = com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonId = (String)loginPerson.getObjectId();

			String strContextUser = context.getUser().trim();
			
			// DVM PE Id
			String strBUHeadId = "";
			MQLCommand mql = new MQLCommand();
			String strMQL = "print bus '" + DomainConstants.TYPE_BUSINESS_UNIT + "' '" + PROJECT_MANAGEMENT_DIRECTORATE + "' - select from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id dump |";
			boolean bResult = mql.executeCommand(context, strMQL);

			if(bResult)
			{
				String sResult = mql.getResult().trim();
				if(sResult!=null && !"".equals(sResult)) 
				{
					StringList slResultList = FrameworkUtil.split(sResult, "|");
					strBUHeadId = (String)slResultList.get(0);
				}
			}
			
			if(null != strBUHeadId && strBUHeadId.length() > 0 && strLoginPersonId.equalsIgnoreCase(strBUHeadId))
				bIsDVMPE = true;	
			
			// IF LOGGED-IN PERSON IS NPE HEAD, HE HAS TO VIEW PROJECTS AS THAT ARE VISIBLE TO PE-DVM - START			 
			mql = new MQLCommand();
			strMQL = "print bus " + strLoginPersonId + " select to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].from.name to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute.value dump |";

			bResult = mql.executeCommand(context, strMQL);
			if(bResult)
			{
				String sResult = mql.getResult().trim();
				if(sResult!=null && !"".equals(sResult)) 
				{
					StringList slResultList = FrameworkUtil.split(sResult, "|");
					String strDepartmentBUName = (String)slResultList.get(0);

					if(null != strDepartmentBUName && strDepartmentBUName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentBUName))
					{
						String strLeadResponsibilityAttribute = (String)slResultList.get(1);
						if(null != strLeadResponsibilityAttribute && strLeadResponsibilityAttribute.length() > 0)
						{
							if(null != strBUHeadId && strBUHeadId.length() > 0)
							{
								strLoginPersonId = strBUHeadId;
								bIsDVMPE = true;
							}
						}
					}
				}
			}
			// IF LOGGED-IN PERSON IS NPE HEAD, HE HAS TO HAVE VIEW ACCESS AS THAT OF PE-DVM - END

			DomainObject personDO = DomainObject.newInstance(context, strLoginPersonId);
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");

			// FETCH DEPARTMENT/BUSINESS UNIT OF CONTEXT USER
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
				String strBUDeptType = (String)departmentMap.get(DomainConstants.SELECT_TYPE);
				String strBUDeptId = (String)departmentMap.get(DomainConstants.SELECT_ID);
				String strBUDepartmentName = (String)departmentMap.get(DomainConstants.SELECT_NAME);

				if(DomainConstants.TYPE_DEPARTMENT.equalsIgnoreCase(strBUDeptType) && null != strBUDepartmentName && !"null".equalsIgnoreCase(strBUDepartmentName) && strBUDepartmentName.length() > 0)
				{
					String strLeadResponsibilityName = (String)departmentMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");
					if(null != strLeadResponsibilityName && strLeadResponsibilityName.length() > 0 && strContextUser.equalsIgnoreCase(strLeadResponsibilityName)) // DPM
					{
						bIsDPM = true;
					}
					else // Project Member/User
					{
						bIsDPM = false;
					}

					if(sbDeptBUWhereClause.length() > 0)
					{
						sbDeptBUWhereClause.append(" && ");
					}

					sbDeptBUWhereClause.append("(attribute[");
					sbDeptBUWhereClause.append(ATTRIBUTE_MSIL_PROJECT_DEPARTMENT);
					sbDeptBUWhereClause.append("]");
					sbDeptBUWhereClause.append("==");
					sbDeptBUWhereClause.append("'");
					sbDeptBUWhereClause.append(strBUDepartmentName);
					sbDeptBUWhereClause.append("')");
				}
				else if(!bIsDVMPE && DomainConstants.TYPE_BUSINESS_UNIT.equalsIgnoreCase(strBUDeptType) && null != strBUDepartmentName && !"null".equalsIgnoreCase(strBUDepartmentName) && strBUDepartmentName.length() > 0)
				{
					bIsDDVM = true;
					// ADD BUSINESS UNIT TO WHERE CLAUSE
					if(sbDeptBUWhereClause.length() > 0)
					{
						sbDeptBUWhereClause.append(" && ");
					}
					sbDeptBUWhereClause.append("((attribute[");
					sbDeptBUWhereClause.append(ATTRIBUTE_MSIL_PROJECT_DEPARTMENT);
					sbDeptBUWhereClause.append("]");
					sbDeptBUWhereClause.append("==");
					sbDeptBUWhereClause.append("'");
					sbDeptBUWhereClause.append(strBUDepartmentName);
					sbDeptBUWhereClause.append("')");

					// FETCH DEPARTMENTS FROM BUSINESS UNIT
					DomainObject BUObj = DomainObject.newInstance(context, strBUDeptId);

					MapList deptList = BUObj.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_DIVISION+","+DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT,
							DomainConstants.TYPE_DEPARTMENT+","+DomainConstants.TYPE_BUSINESS_UNIT,
							objectSelects,
							null,
							false, // get to
							true, // get from
							(short)0, // All level
							null, // Obj where
							null, // rel where
							0);   // all objects
					
					// ITERATE OVER CONNECTED DEPARTMENT LIST AND APPEND IN WHERE CLAUSE
					for(int nCount = 0; nCount < deptList.size(); nCount++)
					{
						Map mDept = (Map)deptList.get(nCount);
						String strDeptName = (String)mDept.get(DomainConstants.SELECT_NAME);

						if(sbDeptBUWhereClause.length() > 0)
						{
							sbDeptBUWhereClause.append(" || ");
						}
						sbDeptBUWhereClause.append("(attribute[");
						sbDeptBUWhereClause.append(ATTRIBUTE_MSIL_PROJECT_DEPARTMENT);
						sbDeptBUWhereClause.append("]");
						sbDeptBUWhereClause.append("==");
						sbDeptBUWhereClause.append("'");
						sbDeptBUWhereClause.append(strDeptName);
						sbDeptBUWhereClause.append("')");
					}
					sbDeptBUWhereClause.append(")");
				}
			}			
			if(sbDeptBUWhereClause.length() > 0)
				sbWhereClause.append(sbDeptBUWhereClause);
			
			if ((STATE_PROJECT_COMPLETE).equals(selectState))
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current=='");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append("'");
				}
				else
				{
					sbWhereClause.append(" && current=='");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append("'");
				}
			}
			else if ((STATE_PROJECT_ARCHIVE).equals(selectState))
			{
				// Active Projects - not in the complete state or in the archive state
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current!=");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_ARCHIVE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}
				else
				{
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_COMPLETE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(STATE_PROJECT_ARCHIVE);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}
			}
			else if((ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD).equals(selectState))
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause = new StringBuffer(500);				
					sbWhereClause.append("current==");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}else
				{
					sbWhereClause.append(" && current==");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}
			}
			else if((ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL).equals(selectState))
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause = new StringBuffer(500);
					sbWhereClause.append("current==");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}else
				{
					sbWhereClause.append(" && current==");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
				}
			}
			else
			{
				if(sbWhereClause.length() <= 0)
				{
					sbWhereClause.append("current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}
				else
				{
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
					sbWhereClause.append(" && current!=");
					sbWhereClause.append(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
				}
			}

			StringList busSelects = new StringList(2);
			busSelects.addElement(project.SELECT_VAULT);
			busSelects.addElement(project.SELECT_ID);
			busSelects.addElement(project.SELECT_NAME);
			busSelects.addElement(project.SELECT_TYPE);
			busSelects.addElement(project.SELECT_CURRENT);
			busSelects.addElement(project.SELECT_OWNER);
			busSelects.addElement(project.SELECT_ORIGINATED);
			busSelects.addElement(project.SELECT_DESCRIPTION);			
			busSelects.addElement(project.SELECT_TASK_ESTIMATED_FINISH_DATE);
			busSelects.addElement(project.SELECT_TASK_ACTUAL_FINISH_DATE);
			
			ContextUtil.pushContext(context);
			projectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, VAULT_ESERVICE_PRODUCTION,sbWhereClause.toString(),busSelects);
			ContextUtil.popContext(context);
			
			if(!bIsDPM && !bIsDVMPE && !bIsDDVM) // DISPLAY PROJECTS THAT BELONGS TO HIS DEPARTMENT BUT USER IS NOT A MEMBER OF THOSE PROJECTS 
			{
				// FIND PROJECTS IN WHICH CONTEXT USER IS A MEMBER
				MapList memberProjectList = project.getUserProjects(context, person, busSelects, null, null, null);
				StringList slMemberProjectIdList = convertMLToSL(memberProjectList, project.SELECT_ID);

				// PREPARE LIST OF PROJECTS ON WHICH CONTEXT USER HAS VIEW ACCESS BUT HE IS NOT A MEMBER OF THE PROJECT
				ListIterator projectListItr = projectList.listIterator();
				while (projectListItr.hasNext())
				{
					Map tempMap = (Map) projectListItr.next();
					String strProjectId = (String) tempMap.get(project.SELECT_ID);

					if(!slMemberProjectIdList.contains(strProjectId))
					{
						returnProjectList.add(tempMap);
					}
				}
			}else // SHOW ALL PROJECTS OF HIS DEPARTMENT/DIVISION
			{
				// PREPARE LIST OF PROJECTS ON WHICH CONTEXT USER HAS VIEW ACCESS BUT HE IS NOT A MEMBER OF THE PROJECT
				ListIterator projectListItr = projectList.listIterator();
				while (projectListItr.hasNext())
				{
					Map tempMap = (Map) projectListItr.next();
					String strProjectId = (String) tempMap.get(project.SELECT_ID);

					returnProjectList.add(tempMap);
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("\nException JPO:MSILUserDashboard:getDeptProjects..."+ex);
			throw ex;
		}
		System.out.println("\n... JPO:MSILUserDashboard:getDeptProjects exit...");
		return returnProjectList;
	}
	
	// Method added - 22/12/2014
	public Vector showProjectData(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:showProjectData start...");
		Vector projectDataVector = new Vector();
		try
		{
			ContextUtil.pushContext(context);
			com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
			HashMap projectMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) projectMap.get("objectList");

			Map columnMap = (Map) projectMap.get("columnMap");
			Map settingsMap = (Map) columnMap.get("settings");
			String strColumnName = (String)settingsMap.get("ColumnName");

			Map objectMap = null;

			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				String strProjectData = (String) objectMap.get(strColumnName);   		
				if("owner".equalsIgnoreCase(strColumnName))
					strProjectData = com.matrixone.apps.common.Person.getDisplayName(context, strProjectData);
				projectDataVector.add(strProjectData);
			} //ends while
		}
		catch (Exception ex)
		{
			System.out.println("\n...Exception JPO:MSILUserDashboard:showProjectData..."+ex);
			throw ex;
		}
		finally
		{
			ContextUtil.popContext(context);
			System.out.println("\n... JPO:MSILUserDashboard:showProjectData exit...");
			return projectDataVector;
		}
	}
	private static StringList convertMLToSL(MapList paramMapList, String paramString) throws Exception
	{
		int iListSize = paramMapList.size();
		StringList localStringList = new StringList(iListSize);

		for (int nCount = 0; nCount < iListSize; nCount++)
		{
			Map localMap = (Map)paramMapList.get(nCount);
			String strObjectId = (String)localMap.get(paramString);
			if(!"".equalsIgnoreCase(strObjectId) && !"null".equalsIgnoreCase(strObjectId) && null != strObjectId)
				localStringList.add(strObjectId);
		}
		return localStringList;
	}
	public MapList getAllUserManuals(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getAllUserManuals start...");
		MapList mlFinalList = new MapList();
		StringList strlobjectSelects = new StringList(1);
		strlobjectSelects.addElement(DomainConstants.SELECT_ID);

		MapList mlUserManualList = DomainObject.findObjects(context,
				PropertyUtil.getSchemaProperty("type_MSILUserManual"),
				"eService Production",
				"",
				strlobjectSelects);
		
		System.out.println("\n... JPO:MSILUserDashboard:getAllUserManuals end...");
		return mlUserManualList;
	}

	public static Vector getSampleFormatsDownloadLink(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getSampleFormatsDownloadLink start...");
		Vector vActions = new Vector();

		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			StringBuffer strActionURL = null;
			if(objectList == null || objectList.size() <= 0)
			{
				return vActions;
			}

			StringBuffer sbCheckoutURL = null;
			String languageStr = (String)context.getSession().getLanguage();
			String sTipDownload = i18nNow.getI18nString("emxComponents.DocumentSummary.ToolTipDownload", "emxComponentsStringResource", languageStr);

			StringList selectTypeStmts = new StringList(1);
			selectTypeStmts.add(DomainConstants.SELECT_ID);

			//Getting all the content ids
			String oidsArray[] = new String[objectList.size()];
			for (int i = 0; i < objectList.size(); i++)
			{
				try
				{
					oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
				} catch (Exception ex)
				{
					oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
				}
			}

			MapList objList = DomainObject.getInfo(context, oidsArray, selectTypeStmts);

			Iterator objectListItr = objList.iterator();
			while(objectListItr.hasNext()){
				Map contentObjectMap = (Map)objectListItr.next();
				int fileCount = 0;
				StringList sDocName = null;
				StringBuffer strBufOnlyDownload = new StringBuffer(1256);// added by sunil
				String documentId = (String)contentObjectMap.get(DomainConstants.SELECT_ID);
				String strFileFormat  = "generic";
				DomainObject docObject = DomainObject.newInstance(context,documentId);
				//strFileFormat = CommonDocument.getFileFormat(context,docObject);

				//For getting the count of files
				HashMap filemap = new HashMap();
				filemap.put(CommonDocument.SELECT_FILE_NAME, contentObjectMap.get(CommonDocument.SELECT_FILE_NAME));
				fileCount = CommonDocument.getFileCount(context,filemap); 
				sbCheckoutURL = new StringBuffer(128);
				sbCheckoutURL.append("../components/emxCommonDocumentPreCheckout.jsp?objectId=");
				sbCheckoutURL.append(documentId);
				sbCheckoutURL.append("&amp;action=download");

				strBufOnlyDownload.append("<a href='javascript:showNonModalDialog(\"");
				strBufOnlyDownload.append(sbCheckoutURL.toString());
				strBufOnlyDownload.append("\",575,575)'>");
				strBufOnlyDownload.append("<img border='0' src='../common/images/iconActionDownload.gif' alt='");
				strBufOnlyDownload.append(sTipDownload);
				strBufOnlyDownload.append("' title='");
				strBufOnlyDownload.append(sTipDownload);
				strBufOnlyDownload.append("'></img></a>&#160;");
				vActions.add(strBufOnlyDownload.toString());
			}
		} catch(Exception ex){
			System.out.println("\n...Exception JPO:MSILUserDashboard:getSampleFormatsDownloadLink..."+ex);
			ex.printStackTrace();
			throw ex;
		}
		finally{
			System.out.println("\n... JPO:MSILUserDashboard:getSampleFormatsDownloadLink exit...");
			return vActions;  
		}
	}

	// Method added - 22/12/2014
	public Vector getStatusIcon(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getStatusIcon start...");
		Vector showIcon = new Vector();
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		try
		{
			ContextUtil.pushContext(context);
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
			busSelect.add(project.SELECT_ID);

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

			for (i = 0; i < actionListSize; i++)
			{
				//determine which gif should be display for status
				String statusGif = "";
				Date baselineCurrentEndDate = null;
				objectMap = (Map) actionList.get(i);

				String strProjectId = (String)objectMap.get(project.SELECT_ID);

				com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

				// Define selectables for each Task object.
				StringList taskSelects = new StringList(7);
				taskSelects.add(DomainConstants.SELECT_ID);
				taskSelects.add(DomainConstants.SELECT_TYPE);
				taskSelects.add(task.SELECT_BASELINE_CURRENT_END_DATE);
				taskSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
				taskSelects.add(task.SELECT_CURRENT);
				taskSelects.add(task.SELECT_PERCENT_COMPLETE);

				project.setId(strProjectId);

				MapList tasksForProject = project.getTasks(context, 0, taskSelects, null, false);

				emxProjectSpace_mxJPO jpoObj = new emxProjectSpace_mxJPO(context, args);
				StringList slTaskStatusList = jpoObj.getTaskStatusIcon(context, tasksForProject);

				String strProjetDelay = "";
				if(slTaskStatusList.contains("Red"))
					strProjetDelay = "Red";
				else if(slTaskStatusList.contains("Yellow"))
					strProjetDelay = "Yellow";

				String baselineCurrentEndDateString = (String) objectMap.get(project.SELECT_BASELINE_CURRENT_END_DATE);

				Date estFinishDate = sdf.parse((String) objectMap.get(project.SELECT_TASK_ESTIMATED_FINISH_DATE));

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
					}
				}
				showIcon.addElement(statusGif);
			}
		}
		catch (Exception ex)
		{
			System.out.println("\n...Exception JPO:MSILUserDashboard:getStatusIcon..."+ex);
			throw ex;
		}
		finally
		{
			ContextUtil.popContext(context);
			System.out.println("\n... JPO:MSILUserDashboard:getStatusIcon exit...");
			return showIcon;
		}
	}

	/**
	 * This function displays the list of programs
	 *
	 * Method added - 22/12/2014
	 */
	public Vector getProgram(Context context, String[] args) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:getProgram start...");
		Vector programs = new Vector();
		try
		{
			ContextUtil.pushContext(context);
			com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
			HashMap projectMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) projectMap.get("objectList");
			Map paramList = (Map) projectMap.get("paramList");
			boolean isprinterFriendly=false;
			if(paramList.get("reportFormat") != null)
			{
				isprinterFriendly = true;
			}
			Map objectMap = null;

			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];

			String strOutput = "";
			String strProgramId = "";
			String strProgramName= "";
			String strfinal =""; 
			int arrayCount = 0;
			
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				String strProjectId =(String) objectMap.get(project.SELECT_ID);
				DomainObject projectDobj = DomainObject.newInstance(context,strProjectId);
				StringList strProgramsIds =projectDobj.getInfoList(context,"to["+RELATIONSHIP_PROGRAM_PROJECT+"].from.id");
				if ((strProgramsIds == null) || strProgramsIds.isEmpty())
				{
					programs.add("");
				}
				else{
					StringBuffer output = new StringBuffer();
					for(int nCount=0;nCount<strProgramsIds.size();nCount++)
					{
						strProgramId = (String)strProgramsIds.get(nCount);
						DomainObject programDobj = DomainObject.newInstance(context,strProgramId);
						strProgramName = programDobj.getInfo(context, SELECT_NAME);
						if(!isprinterFriendly)
						{
							output.append("<a href=\"javascript:emxTableColumnLinkClick('" + com.matrixone.apps.domain.util.XSSUtil.encodeForURL("../common/emxTree.jsp?objectId="+strProgramId+"&relId=null&amp;suiteKey=ProgramCentral"));
							output.append("','popup','','','','')\">");
						}
						output.append("<img src='../common/images/iconSmallProgram.gif' border='0' valign='absmiddle'/>");						
						//Added for special character.
						output.append(XSSUtil.encodeForXML(context,strProgramName));
						if(!isprinterFriendly)
							output.append("</a>,");														
					}
					strOutput=output.toString();
					programs.add(strOutput.substring(0, strOutput.length()-1));	

					arrayCount++;
				}
			}		
		}
		catch (Exception ex)
		{
			System.out.println("\n...Exception JPO:MSILUserDashboard:getProgram..."+ex);
			throw ex;
		}
		finally
		{
			ContextUtil.popContext(context);
			System.out.println("\n... JPO:MSILUserDashboard:getProgram exit...");
			return programs;
		}
	}
	
	// 21/03/2015 - method added for System to show only Master Project in "My Projects" tab - Start
	public boolean checkIsMasterProject(Context context, String strProjectId) throws FrameworkException
	{
		boolean bIsMasterProject = true;
		try
		{
			if(null != strProjectId && strProjectId.length() > 0)
			{
				// FETCH MASTER PROJECT FROM PROJECT ID
				MQLCommand mql = new MQLCommand();
				String sMql = "expand bus " + strProjectId + " to rel 'Subtask' recurse to end select bus id dump |";
				boolean bResult = mql.executeCommand(context, sMql);
				if(bResult)
				{
					String sResult = mql.getResult().trim();
					if(null != sResult && sResult.length() > 0 && !"".equals(sResult))
					{
						StringTokenizer sResultTkz = new StringTokenizer(sResult,"|");
						sResultTkz.nextToken(); // level
						sResultTkz.nextToken(); // Relationship name
						sResultTkz.nextToken(); // to/from side
						sResultTkz.nextToken(); // Object Type
						sResultTkz.nextToken(); // Object Name
						sResultTkz.nextToken(); // Object Rev
						String strMasterProjectId = (String)sResultTkz.nextToken(); // Object Id
						
						if((null != strMasterProjectId && strMasterProjectId.length() > 0 && !"null".equalsIgnoreCase(strMasterProjectId)))
							bIsMasterProject = false;
					}
				}
			}
		}catch (Exception ex)
		{
			throw new FrameworkException();
		}
		return bIsMasterProject;
	}
	// 21/03/2015 - method added for System to show only Master Project in "My Projects" tab - End
	// 07/04/2015 - method added for "Dept Projects" tab to be dropped for DVM-PE & NPE DPM - Start
	public boolean commandAccessForDVM(Context context, String args[]) throws Exception
	{
		System.out.println("\n... JPO:MSILUserDashboard:commandAccessForDVM start...");
		boolean bAccess = false;
		try
		{			
			boolean bIsDVMPE = false;
			boolean bIsNPEDPM = false;
						
			// LOGGED IN PERSON
			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			loginPerson = com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonId = (String)loginPerson.getObjectId();
	
			// DVM PE Id - Start
			String strDVMPEId = "";
			MQLCommand mql = new MQLCommand();
			String strMQL = "print bus '" + DomainConstants.TYPE_BUSINESS_UNIT + "' '" + PROJECT_MANAGEMENT_DIRECTORATE + "' - select from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id dump |";
			
			boolean bResult = mql.executeCommand(context, strMQL);
			if(bResult)
			{
				String sResult = mql.getResult().trim();
				if(sResult!=null && !"".equals(sResult)) 
				{
					StringList slResultList = FrameworkUtil.split(sResult, "|");
					strDVMPEId = (String)slResultList.get(0);
				}
			}			
			if(null != strDVMPEId && strDVMPEId.length() > 0 && strLoginPersonId.equalsIgnoreCase(strDVMPEId))
				bIsDVMPE = true;
			// DVM PE Id - End
			
			// DPM NPE Id - Start
			String strDPMNPEId = "";
			mql = new MQLCommand();
			strMQL = "print bus '" + DomainConstants.TYPE_DEPARTMENT + "' '" + PROJECT_CREATE_DEPARTMENT + "' - select from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id dump |";
			
			bResult = mql.executeCommand(context, strMQL);
			if(bResult)
			{
				String sResult = mql.getResult().trim();
				if(sResult!=null && !"".equals(sResult))
				{
					StringList slResultList = FrameworkUtil.split(sResult, "|");
					strDPMNPEId = (String)slResultList.get(0);
				}
			}
			if(null != strDPMNPEId && strDPMNPEId.length() > 0 && strLoginPersonId.equalsIgnoreCase(strDPMNPEId))
				bIsNPEDPM = true;			
			// DPM NPE Id - End
			
			if(bIsDVMPE || bIsNPEDPM)
				bAccess = false;
			else
				bAccess = true;
		}
		catch (Exception ex)
		{
			System.out.println("\n... JPO:MSILUserDashboard:commandAccessForDVM Exception..."+ex);
			throw new FrameworkException(ex);
		}
		System.out.println("\n... JPO:MSILUserDashboard:commandAccessForDVM exit...");
		return bAccess;
	}
}