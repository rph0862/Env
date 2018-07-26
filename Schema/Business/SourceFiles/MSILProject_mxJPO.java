/* ${CLASSNAME}.java

Author : Intelizign
Change History:
Date       	   |	Change By 	 | Tag to be searched |		  Details
===========================================================================================================
24-June-2014   |    Intelizign	 |	    -             |      Initial Release
28-Apr-2015    |    Intelizign   |    28/04/2015      |      CR - Only child tasks to be sent for approval.
03-May-2015     |    Intelizign  |    03/05/2015      | CR - Route Stops on Rejection. Task still in revew state & 100% complete. Task cannot be tracked if user doen't change the %age completion.
03-May-2015     |    Intelizign  |    03/05/2015      | CR - Stopped routes have to be explictly started for all tasks. Tracking has to be done for all such routes. Also route owner has to be tracked & restarted from his login.
02-Nov-2016     |   Dheeraj Garg |    02-Nov-2016     | CR - Route Approval Template on Task.
05-Dec-2016     |   Dheeraj Garg |        -           | CR - Ringi Integration.
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.Access;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectRoleVaultAccess;
import com.matrixone.apps.program.ProjectSpace;


public class MSILProject_mxJPO implements MSILConstants_mxJPO
{
	public MSILProject_mxJPO (Context context, String[] args) throws Exception
	{

	}

	/**
	 * Trigger method called on 
	 * 	1) Project Concept promotion from Concept to Review state (Promote Check trigger)
	 *  2) Project Space promotion from Create to Assign state (Promote Check trigger)
	 * 
	 * This method checks for Route Template object to be connected to Project.
	 * If no active template is found, system will not allow project to be promoted to Assign state
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID - Project id
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */
	public int checkApprovalTemplateExists(Context context, String[] args) throws FrameworkException 
	{
		System.out.println("\n...JPO:MSILProject:checkApprovalTemplateExists start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		int nReturn = 0;
		try 
		{
			String strProjectId = args[0];
			String languageStr = context.getSession().getLanguage();
			if(null != strProjectId && !"null".equalsIgnoreCase(strProjectId) && strProjectId.length() > 0)
			{
				DomainObject projectDO = DomainObject.newInstance(context, strProjectId);

				// FETCH CONNECTED ROUTE TEMPLATE FROM PROJECT
				String strConnectedRouteTemplate = projectDO.getInfo(context, "from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");

				// IF NO TEMPLATE IS FOUND, BLOCK THE PROMOTION OF PROJECT OBJECT
				if(null != strConnectedRouteTemplate && strConnectedRouteTemplate.length() > 0)
				{
					nReturn = 0;
				}
				else
				{
					String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.ActiveTemplateForApproval", "emxProgramCentralStringResource", languageStr);
					System.out.println("\nTrigger Blocked for Reason JPO:MSILProject:checkApprovalTemplateExists::"+strMessage);
					MqlUtil.mqlCommand(context, "notice "+strMessage);
					nReturn = 1;
				}
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception JPO:MSILProject:checkApprovalTemplateExists :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:checkApprovalTemplateExists exit...");
		return nReturn;
	}

	/**
	 * Trigger method called on 
	 * 	1) Project Concept promotion from Concept to Review state (Promote Action trigger)
	 *  2) WBS Task promotion from Create to Assign state  (Promote Action trigger)
	 * 
	 * This method will create route from route template and attache the route to project/task object
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - Object Id
	 *            1 - From State
	 *            2 - To state                     
	 *            3 - Route Base State - (object state on which the route will be started)   
	 *            4 - Route Base Purpose
	 *            5 - Object Policy
	 *            6 - Object Type
	 *            7 - Object Name
	 *            8 - Object Owner
	 *            
	 * @returns 0 for sucess and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 * 
	 */
	public int createRouteFromRouteTemplate(Context context, String[] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILProject:createRouteFromRouteTemplate start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try 
		{
			String strObjectId = args[0];
			if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
			{
				String strFromState = args[1];
				String strPolicy = args[5];
				String strType = args[6];
				
				String strTaskType = "";
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				MapList mlRoutesList = null;

				// IF TYPE TASK IS PROMOTED FROM CREATE STATE TO ASSIGN STATE OR IF TYPE 'PROJECT CONCEPT' IS PROMOTED FROM CONCEPT STATE TO REVIEW STATE
				if ((STATE_CREATE_POLICY_PROJECT_TASK.equals(strFromState) && DomainConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(strPolicy) && DomainConstants.TYPE_TASK.equalsIgnoreCase(strType))
						|| (STATE_CONCEPT_POLICY_PROJECT_CONCEPT.equals(strFromState) && DomainConstants.POLICY_PROJECT_CONCEPT.equalsIgnoreCase(strPolicy) && DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType)))
				{
                    //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - Start
					DomainObject objectDO = DomainObject.newInstance(context, strObjectId);
					strTaskType = objectDO.getInfo(context, SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
					
                    String isChildTask = objectDO.getInfo(context,"from[Subtask].to.id");
                    if(isChildTask==null || (DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType))){
                    //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - End
					// GET NOT COMPLETED ROUTES FROM OBJECT
					String strWhereClause = "current!='Complete'";
					ContextUtil.pushContext(context);
					mlRoutesList = objectDO.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_OBJECT_ROUTE, 
							DomainConstants.TYPE_ROUTE, 
							objectSelects, 
							null,
							true, 
							true, 
							(short) 1, 
							strWhereClause, 
							null);
					ContextUtil.popContext(context);
					
					//If the task type is "gate schedule" or "po" then no need for creation of routes
					if(MSILUtils_mxJPO.isNullOrEmpty(strTaskType))
					{
						//In order To avoid null pointer Exception
						strTaskType = "-";
					}
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
					// if(strType.equalsIgnoreCase(DomainConstants.TYPE_TASK) && strTaskType.equalsIgnoreCase("Gate Schedule") || strTaskType.equalsIgnoreCase("po"))
					if(strType.equalsIgnoreCase(DomainConstants.TYPE_TASK) && strTaskType.equalsIgnoreCase("Gate Schedule") || strTaskType.equalsIgnoreCase("po") || strTaskType.equalsIgnoreCase("Ringi"))
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
					{
						System.out.println("\n... JPO:MSILProject:createRouteFromRouteTemplate Route Not Created for Task Type GateSchedule/PO/Ringi...");
					}

					else
					{
						// CREATE ROUTE ON TASK/PROJECT CONCEPT, IF NO IN-PROCESS ROUTE IS PRESENT
						if (null == mlRoutesList || mlRoutesList.isEmpty() || mlRoutesList.size() <= 0) 
						{
							return (createRoute(context, args));
						}
	                    //Code add for CR - 03/05/2015 - Start
	                    else
	                    {
	                        StringList sSelects = new StringList();
	                        sSelects.add(DomainConstants.SELECT_NAME);
	                        sSelects.add("current");
	                        sSelects.add("attribute[Route Status]");
	                        if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
	                        {
	                            DomainObject projectDO = DomainObject.newInstance(context, strObjectId);
	                            String sRouteObj = projectDO.getInfo(context,"from[Object Route].to.id");
	                            DomainObject routetDO = DomainObject.newInstance(context, sRouteObj);
	                            Map mRouteInfo = (Map) routetDO.getInfo(context,sSelects);
	                            String strCurrent = (mRouteInfo.get("current")).toString();
	                            String strRouteStatus = (mRouteInfo.get("attribute[Route Status]")).toString();
	                            if(null!=strCurrent && strCurrent.equals("In Process") && null!=strRouteStatus && strRouteStatus.equals("Stopped"))
	                            {
	                                com.matrixone.apps.common.Route route = new com.matrixone.apps.common.Route(sRouteObj);
	                                route.resume(context);
	                            }
	                        }
	                    }
					
                    //Code add for CR - 03/05/2015 - End
                    //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - Start
					}
                  }
                  //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - End
				}
			}
		} catch (Exception e) {
			System.out.println("Exception JPO:MSILProject:createRouteFromRouteTemplate :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:createRouteFromRouteTemplate exit...");
		return 0;
	}

	/**
	 * This method creates route from route template and attaches to object
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - Object Id
	 *            1 - From State
	 *            2 - To state                     
	 *            3 - Route Base State - (object state on which the route will be started)   
	 *            4 - Route Base Purpose
	 *            5 - Object Policy
	 *            6 - Object Type
	 *            7 - Object Name
	 *            8 - Object Owner
	 *            
	 * @returns true for sucess and false for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 * 
	 */
	public int createRoute(Context context, String[] args) throws Exception 
	{
		System.out.println("\n... JPO:MSILProject:createRoute start...");
		// DECALRE CONSTANTS
		String POLICY_ROUTE_ADMIN_ALIAS 			= 		FrameworkUtil.getAliasForAdmin(context, DomainObject.SELECT_POLICY, DomainObject.POLICY_ROUTE, true);
		String TYPE_ROUTE_ADMIN_ALIAS 				= 		FrameworkUtil.getAliasForAdmin(context, DomainObject.SELECT_TYPE, DomainObject.TYPE_ROUTE, true);
		String ATTRIBUTE_ROUTE_BASE_PURPOSE 		= 		PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
		String ATTRIBUTE_AUTO_STOP_REJECTION 		= 		PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection"); 
		String SELECT_ATTRIBUTE_AUTO_STOP_REJECTION = 		"attribute[" + ATTRIBUTE_AUTO_STOP_REJECTION + "]"; 
		String ATTRIBUTE_ROUTE_COMPELTION_ACTION 	= 		PropertyUtil.getSchemaProperty(context, "attribute_RouteCompletionAction");
		String VAULT_PRODUCTION 					= 		PropertyUtil.getSchemaProperty(context, context.getVault().toString());
		String PROMOTE_CONNECTED_OBJECT				=		"Promote Connected Object";

		// ARGUMENTS FETCHED FROM TRIGGER OBJECT
		String strObjectId = args[0];
		String strBasePolicy = FrameworkUtil.getAliasForAdmin(context, "Policy", args[5], false);

		// INITIALIZE LOCAL VARIABLES
		String strTemplateId = "";
		String strTemplateDesc = "";
		String strTemplateState = "";
		String strAutoStopOnRejection = "";		
		Route routeObj = null;

		try 
		{
			String languageStr = context.getSession().getLanguage();
			// OBJECT DO
			DomainObject objDO = (DomainObject) DomainObject.newInstance(context);
			objDO.setId(strObjectId);

			String strProjectId = "";			
			String strPolicy = args[5];
			String strType = args[6];
			String strObjectName = args[7];
			String strObjOwner = args[8];

			// Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- Start
			StringList busSelect = new StringList("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
			busSelect.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
			Map mapInfo = objDO.getInfo(context, busSelect);
			// Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- End

			// IF TYPE IS TASK, GET THE ROUTE TEMPLATE FROM THE CONNECTED PROJECT
			if (DomainConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(strPolicy) && DomainConstants.TYPE_TASK.equalsIgnoreCase(strType))
			{
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_NAME);
				objectSelects.addElement(DomainConstants.SELECT_TYPE);
				objectSelects.addElement(DomainConstants.SELECT_REVISION);

				StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
				relSelects.addElement(DomainRelationship.SELECT_NAME);

				ContextUtil.pushContext(context);
				/*MapList projectList = objDO.getRelatedObjects(context, 
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

				//String busSelect = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";

				//strProjectId = objDO.getInfo(context, busSelect);

				strProjectId = (String)mapInfo.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");

                // Added and Modified by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- End



				ContextUtil.popContext(context);

				/*// PROJECT ID TO WHICH CONTEXT OBJECT IS CONNECTED
				if(null != projectList && projectList.size() > 0)
				{
					projectList.sort(DomainObject.SELECT_LEVEL, "ascending",  "integer");
					Map projectMap = (Map)projectList.get(0);
					strProjectId = (String)projectMap.get(DomainConstants.SELECT_ID);
				}*/
			}
			else if (DomainConstants.POLICY_PROJECT_CONCEPT.equalsIgnoreCase(strPolicy) && DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType))
			{
				strProjectId = strObjectId;
			}
			if(null != strProjectId && strProjectId.length() > 0 && !"null".equalsIgnoreCase(strProjectId))
			{
				ContextUtil.pushContext(context);
				// GET CONNECTED ROUTE TEMPLATE
				StringList slSelectList = new StringList(6);
				slSelectList.addElement(DomainConstants.SELECT_ID);
				slSelectList.addElement(DomainConstants.SELECT_NAME);
				slSelectList.addElement(DomainConstants.SELECT_REVISION);
				slSelectList.addElement(DomainConstants.SELECT_CURRENT);
				slSelectList.addElement(DomainConstants.SELECT_DESCRIPTION);
				slSelectList.addElement(DomainConstants.SELECT_OWNER);
				slSelectList.addElement(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION);

				// FETCH CONNECTED ROUTE TEMPLATE FROM PROJECT
				DomainObject projectDO = DomainObject.newInstance(context, strProjectId);
				
				// Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- Start
				String strConnectedRouteTemplateId = (String)mapInfo.get("from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
				if(null == strConnectedRouteTemplateId || "null".equals(strConnectedRouteTemplateId) || "".equals(strConnectedRouteTemplateId))
				{
					strConnectedRouteTemplateId = projectDO.getInfo(context, "from[" + REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE + "].to.id");
				}
				// Added by Dheeraj Garg <02-Nov-2016> SCR - Route Approval Template on Task. -- End

				// IF NO TEMPLATE IS FOUND, BLOCK THE PROMOTION OF PROJECT OBJECT
				// IF TEMPLATE FOUND, PROMOTE THE PROJECT TO PROMOTE STATE
				if(null != strConnectedRouteTemplateId && strConnectedRouteTemplateId.length() > 0 && !"null".equalsIgnoreCase(strConnectedRouteTemplateId))
				{
					DomainObject templateDO = DomainObject.newInstance(context, strConnectedRouteTemplateId);
					Map templateMap = templateDO.getInfo(context, slSelectList);
					strTemplateId = (String) templateMap.get(DomainConstants.SELECT_ID);
					strTemplateDesc = (String) templateMap.get(DomainConstants.SELECT_DESCRIPTION);
					strTemplateState = (String) templateMap.get(DomainConstants.SELECT_CURRENT);
					strAutoStopOnRejection = (String) templateMap.get(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION); 

					if (null != strTemplateId && !"null".equals(strTemplateId) && strTemplateId.length() > 0 && "Active".equals(strTemplateState)) 
					{
						// CREATE ROUTE OBJECT
						String strRouteId = FrameworkUtil.autoName(context, TYPE_ROUTE_ADMIN_ALIAS, "", POLICY_ROUTE_ADMIN_ALIAS, VAULT_PRODUCTION);
						routeObj = (Route) DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
						routeObj.setId(strRouteId);

						// RENAME ROUTE OBJECT            				
						String strRouteName = routeObj.getInfo(context, DomainConstants.SELECT_NAME);
						strRouteName = "Route_"+ strRouteName + "_" + strObjectName;

						// SET NEW NAME TO ROUTE OBJECT
						routeObj.setName(context, strRouteName);
						routeObj.open(context);
						// SET DESCRIPTION FROM ROUTE TEMPLATE TO ROUTE OBJECT
						routeObj.setDescription(context, strTemplateDesc);

						// SET ATTRIBUTE VALUES ON ROUTE OBJECT AS FETCHED FROM ROUTE TEMPLATE OBJECT
						// HashMap to carry all the attribute values to be set
						HashMap attrMap = new HashMap();
						attrMap.put(ATTRIBUTE_ROUTE_COMPELTION_ACTION, PROMOTE_CONNECTED_OBJECT);
						attrMap.put(ATTRIBUTE_AUTO_STOP_REJECTION, strAutoStopOnRejection);

						routeObj.setAttributeValues(context,attrMap);

						// ADD CONTENTS TO ROUTE OBJECT, i.e, CONNECT OBJECT TO ROUTE
						RelationshipType relationshipType = new RelationshipType(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
						DomainRelationship newRel = routeObj.addFromObject(context, relationshipType, strObjectId);

						// CONNECT ROUTE OBJECT TO ROUTE TEMPLATE
						routeObj.connectTemplate(context, strTemplateId);

						// ADD MEMBERLIST FROM TEMPLATE TO ROUTE OBJECT
						routeObj.addMembersFromTemplate(context, strTemplateId);

						// UPDATE OBJECT ROUTE RELATIONSHIP ATTRIBUTES
						attrMap = new HashMap();
						attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE, args[3]);
						attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY, strBasePolicy);
						attrMap.put(ATTRIBUTE_ROUTE_BASE_PURPOSE, args[4]);

						newRel.setAttributeValues(context, attrMap);

						// CHANGE ROUTE ACTION ATTRIBUTE VALUE TO APPROVE
						SelectList relSelectList = new SelectList(1);
						relSelectList.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

						String strTypePattern = DomainConstants.TYPE_PERSON +  "," + DomainConstants.TYPE_ROUTE_TASK_USER;
						MapList routeNodeList = null;
						DomainObject routeDomObj = (DomainObject) DomainObject.newInstance(context);
						routeDomObj.setId(strRouteId);
						// GET ALL TASKS FROM ROUTE
						routeNodeList = routeDomObj.getRelatedObjects(context,
								DomainConstants.RELATIONSHIP_ROUTE_NODE,
								strTypePattern, 
								null,
								relSelectList, 
								false, 
								true, 
								(short) 1,
								null,
								null);
						if(null != routeNodeList && routeNodeList.size() > 0)
						{
							Iterator itrRouteNodeList = routeNodeList.iterator();
							while (itrRouteNodeList.hasNext()) 
							{
								Map routeMap = (Map) itrRouteNodeList.next();
								DomainRelationship routeNodeId = new DomainRelationship((String) routeMap.get(DomainConstants.SELECT_RELATIONSHIP_ID));
								// ATTRIBUTE MAP FOR ROUTE NODE RELATIONSHIP
								HashMap attributeMap = new HashMap();
								attributeMap.put(DomainConstants.ATTRIBUTE_ROUTE_ACTION, "Approve");
								routeNodeId.setAttributeValues(context, attributeMap);
							}
						}
						// IF TYPE IS PROJECT CONCEPT, START THE ROUTE ON PROMOTION.
						if (DomainConstants.POLICY_PROJECT_CONCEPT.equalsIgnoreCase(strPolicy) && DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType))
						{
							emxChange_mxJPO changeJPO = new emxChange_mxJPO(context, args);
							changeJPO.startRoute(context, strRouteId);	
						}
						if ("User Agent".equals(routeDomObj.getOwner(context).getName()))
						{
							if (null != strObjOwner && strObjOwner.length() > 0)
							{
								routeDomObj.changeOwner(context, strObjOwner);
							}
						}
					}
				}
				else
				{
					String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Route.NoTemplateDefined", "emxProgramCentralStringResource", languageStr);
					System.out.println("\nTrigger Blocked for Reason JPO:MSILProject:createRoute::"+strMessage);
					MqlUtil.mqlCommand(context, "notice "+strMessage);
					return 1;
				}
				ContextUtil.popContext(context);
			}
			else
			{
				String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Route.NoProjectConnectedToTask", "emxProgramCentralStringResource", languageStr);
				System.out.println("\nTrigger Blocked for Reason JPO:MSILProject:createRoute::"+strMessage);
				MqlUtil.mqlCommand(context, "notice "+strMessage);
				return 1;
			}
		} catch (Exception e) {
			System.out.println("Exception JPO:MSILProject:createRoute:::: " + e);
			throw e;
		}
		System.out.println("\n... JPO:MSILProject:createRoute exit...");
		return 0;
	}

	/**
	 * Trigger method called on 
	 * 	1) Relationship Member create (Create Action trigger)
	 * 
	 * This method will connect the member to the Master Project when person is added to Sub-Project.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - FROMOBJECTID 		- 	From Object Id (From side object is Project)
	 *            1 - TOOBJECTID	 	- 	To Object Id (To side object is Person)
	 *            2 - PROJECTROLE 		- 	Parameter passed (Reference Member)
	 *            3 - PROJECTACCESS		- 	Parameter passed (Project Member)
	 *            4 - FROMTYPE			- 	From Type
	 *            5 - TOTYPE			- 	To Type
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */
	public int addMemberToMasterProject(Context context, String [] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILProject:addMemberToMasterProject start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try 
		{
			MSILAccessRights_mxJPO object = new MSILAccessRights_mxJPO(context,args);
			if(object.isPersonFromCivil(context,args)){
			return 0;
			}
		
			String strFromId  = args[0]; // PROJECT SPACE OBJECT ID
			String strToId  = args[1]; // PERSON ID
			if(ProgramCentralUtil.isNotNullString(strFromId) && ProgramCentralUtil.isNotNullString(strToId)) 
			{
				String strFromType = args[4];
				String strToType = args[5];
				if(DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(strFromType) && DomainConstants.TYPE_PERSON.equalsIgnoreCase(strToType))
				{
					//ContextUtil.pushContext(context);
					// FETCH MASTER PROJECT FROM PROJECT ID
					MQLCommand mql = new MQLCommand();
					String sMql = "expand bus " + strFromId + " to rel 'Subtask' recurse to end select bus id dump |";
					boolean bResult = mql.executeCommand(context, sMql);
					if(bResult)
					{
						String sResult = mql.getResult().trim();
						if(sResult!=null && !"".equals(sResult)) 
						{
							StringTokenizer sResultTkz = new StringTokenizer(sResult,"|\n");
							sResultTkz.nextToken(); // level
							sResultTkz.nextToken(); // Relationship name
							sResultTkz.nextToken(); // to/from side
							sResultTkz.nextToken(); // Object Type
							sResultTkz.nextToken(); // Object Name
							sResultTkz.nextToken(); // Object Rev
							String strMasterProjectId = (String)sResultTkz.nextToken(); // Object Id 

							if(null != strMasterProjectId && strMasterProjectId.length() > 0 && !"null".equalsIgnoreCase(strMasterProjectId))
							{
								DomainObject masterProjectDO = DomainObject.newInstance(context, strMasterProjectId);

								// QUERY TO FIND IF THE SAME PERSON IS ALREADY CONNECTED TO MASTER PROJECT OR NOT
								mql = new MQLCommand();
								sMql = "expand bus " + strMasterProjectId + " from rel 'Member' select bus name where \"(id=='" + strToId + "')\" dump |;";
								bResult = mql.executeCommand(context, sMql);
								if(bResult)
								{									
									sResult = mql.getResult().trim();
									if(sResult == null || "".equals(sResult) || sResult.length() <= 0) 
									{
										DomainObject domPerson = DomainObject.newInstance(context,strToId);

										ContextUtil.startTransaction(context,true);
										/*DomainRelationship domRel = DomainRelationship.connect(context, masterProjectDO, DomainConstants.RELATIONSHIP_MEMBER, domPerson);
										Map attributeMap = new HashMap();
										attributeMap.put(DomainConstants.ATTRIBUTE_PROJECT_ACCESS, args[3]);
										attributeMap.put(DomainConstants.ATTRIBUTE_PROJECT_ROLE, args[2]);
										domRel.setAttributeValues(context, attributeMap);*/
										
										addMemberToProject(context, strToId, strMasterProjectId, args[3], args[2]);

										ContextUtil.commitTransaction(context);
									}
								}
							}
						}
					}
					//ContextUtil.popContext(context);
				}
			}
		}catch (Exception e) {
			System.out.println("Exception JPO:MSILProject:addMemberToMasterProject:::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:addMemberToMasterProject exit...");
		return 0;
	}

	/**
	 * Trigger method called on 
	 * 	1) Project Concept create (Create Action trigger)
	 *  2) Project Space create (Create Action trigger)
	 *  3) Project Concept change owner (ChangeOwner Action trigger)
	 *  4) Project Space change owner (ChangeOwner Action trigger)
	 *  
	 * This method will fetch the department of the owner of the project and update the same on project.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID 			- 	Object id
	 *            1 - NEWOWNER/OWNER 	- 	Object Owner
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */
	public int updateProjectDepartment(Context context, String [] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILProject:updateProjectDepartment start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try 
		{
			MSILAccessRights_mxJPO object = new MSILAccessRights_mxJPO(context,args);
			if(object.isPersonFromCivil(context,args)){
			return 0;
			}
			String strObjectId = args[0];
			if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
			{
				// Project DO
				DomainObject objectDO = DomainObject.newInstance(context, strObjectId);
				// PERSON CREATING THE PROJECT OR BEING UPDATED AS PROJECT OWNER
				String strOwner = args[1];				
				if(UIUtil.isNullOrEmpty(strOwner)) {
					strOwner = context.getUser();
				}				
				com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
				String strOwnerId = person.getPerson(context, strOwner).getId();

				// OWNER ID
				DomainObject personDO = DomainObject.newInstance(context, strOwnerId);
				StringList objectSelects = new StringList(1);
				objectSelects.addElement(DomainConstants.SELECT_NAME);

				// FETCH DEPARTMENT/BUSINESS UNIT OF OWNER
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

					if(null != strDepartmentName && !"null".equalsIgnoreCase(strDepartmentName) && strDepartmentName.length() > 0)
					{
						// SET DEPARTMENT/BUSINESS UNIT ON PROJECT
						objectDO.setAttributeValue(context, ATTRIBUTE_MSIL_PROJECT_DEPARTMENT, strDepartmentName);
					}
				}
			}
		}catch (Exception e) {
			System.out.println("Exception JPO:MSILProject:updateProjectDepartment:::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:updateProjectDepartment exit...");
		return 0;
	}

	/**
	 * Trigger method called on 
	 *  1) Project Concept demote from Review to Active state (Demote Check trigger)
	 *  2) Project Task demote from Review to Active state (Demote Check trigger)
	 *  
	 * This method checks if there is any in process && not rejected route is still connected to Project/Task.
	 * If any such route is found, block demote
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID 	- 	Object id
	 *            1 - STATENAME - 	From State
	 *            2 - NEXTSTATE - 	To State
	 *            3 - TYPE 		- 	Object Type
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */
	public int checkConnectedRoute(Context context, String[] args) throws FrameworkException 
	{
		System.out.println("\n... JPO:MSILProject:checkConnectedRoute start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try 
		{
			String strObjectId = args[0];
			String languageStr = context.getSession().getLanguage();
			if(null != strObjectId && !"null".equalsIgnoreCase(strObjectId) && strObjectId.length() > 0)
			{
				String strType = args[3];

				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_CURRENT);
				objectSelects.addElement("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_STATUS + "]");
				MapList mlRoutesList = null;

				DomainObject objectDO = DomainObject.newInstance(context, strObjectId);
                //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - Start
                String isChildTask = objectDO.getInfo(context,"from[Subtask].to.id");
                if(isChildTask==null || (DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType))){
                //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - End
                	String strTaskTypeAttribute = objectDO.getInfo(context,SELECT_ATTRIBUTE_MSIL_TASK_TYPE);
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
				ContextUtil.popContext(context);

				if (null == mlRoutesList || mlRoutesList.isEmpty() || mlRoutesList.size() <= 0) 
				{
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- Start
					//if(null != strTaskTypeAttribute && strTaskTypeAttribute.length() > 0 && ("PO".equalsIgnoreCase(strTaskTypeAttribute) || "Gate Schedule".equalsIgnoreCase(strTaskTypeAttribute)))
					if(null != strTaskTypeAttribute && strTaskTypeAttribute.length() > 0 && ("PO".equalsIgnoreCase(strTaskTypeAttribute) || "Gate Schedule".equalsIgnoreCase(strTaskTypeAttribute) || "Ringi".equalsIgnoreCase(strTaskTypeAttribute)))
					// Modifed by Dheeraj Garg <05-Dec-2016> SCR - Ringi Integration. -- End
					{
						return 0;
					}
					else
					{
						String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Promote.NoRoutePresent", "emxProgramCentralStringResource", languageStr);
						System.out.println("\nTrigger Blocked for Reason JPO:MSILProject:checkConnectedRoute::"+strMessage);
						MqlUtil.mqlCommand(context, "notice "+strMessage);
						return 1;
					}
				}
				else
				{
					Iterator itrRouteList = mlRoutesList.iterator();
					while (itrRouteList.hasNext()) 
					{
						Map routeMap = (Map) itrRouteList.next();

						String strRouteState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
						String strRouteStatusValue = (String)routeMap.get("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_STATUS + "]");
						// if define state route exists, start the route
						// if In-process route exists with Stopped status - do nothing						
						// if complete/archive state route exists, report to Admin
						if("In process".equalsIgnoreCase(strRouteState) && !"Stopped".equalsIgnoreCase(strRouteStatusValue))
						{
							String strMessage = i18nNow.getI18nString("emxProgramCentral.Project.Demote.InProcessRouteOnObject", "emxProgramCentralStringResource", languageStr);
							System.out.println("\nTrigger Blocked for Reason JPO:MSILProject:checkConnectedRoute::"+strMessage);
							MqlUtil.mqlCommand(context, "notice "+strMessage);
							return 1;
						}
                        //Code add for CR - 03/05/2015 - Start
                        if("In process".equalsIgnoreCase(strRouteState) && "Stopped".equalsIgnoreCase(strRouteStatusValue))
                        {
                            return 0;
                        }
                        //Code add for CR - 03/05/2015 - End
				}
                }
                //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - Start
              }
              //Added Code - 28/04/2015 - CR - Only child tasks to be sent for approval. - End
			}
		} catch (Exception e) {
			System.out.println("Exception JPO:MSILProject:checkConnectedRoute :::: " + e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:checkConnectedRoute exit...");
		return 0;
	}

	/**
	 * This method will give list of Route Template id that will be excluded from the search result.
	 * Route Templates that are connected to the context object, will not be shown in the search result.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            
	 * @returns StringList
	 * 			  List of Route Template Ids that will be excluded from search result
	 * @throws Exception
	 *             if the operation fails
	 */

	public StringList excludeRouteTemplateIDs(Context context, String[] args) throws FrameworkException 
	{
		System.out.println("\n......JPO:MSILProject:excludeRouteTemplateIDs start....");
		StringList slExcludeRouteTempIds = new StringList();
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectId = (String)programMap.get("parentOID");
			if(null != strProjectId && !"null".equalsIgnoreCase(strProjectId) && strProjectId.length() > 0)
			{
				StringList objSelects = new StringList();
				objSelects.addElement(DomainConstants.SELECT_ID);

				DomainObject projectDO = DomainObject.newInstance(context , strProjectId);
				String strProjectRouteTemplateId = projectDO.getInfo(context, "from["+ REL_MSIL_PROJECT_TO_ROUTE_TEMPLATE +"].to.id");

				slExcludeRouteTempIds.addElement(strProjectRouteTemplateId);
			}
		} catch (Exception e) {
			System.out.println("\n..Exception....JPO:MSILProject:excludeRouteTemplateIDs...."+e);
			throw new FrameworkException(e);
		}
		System.out.println("\n......JPO:MSILProject:excludeRouteTemplateIDs exit....");
		return slExcludeRouteTempIds;
	}

	/**
	 * Trigger method called on 
	 * 	1) Project Concept creation (Create Action trigger)
	 * 
	 * This method will connect DPMs and above as the member to the Master Project
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID 		- 	Object Id
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */

	public int addDPMAndAboveToMasterProject(Context context, String[] args) throws Exception
	{
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToMasterProject start.....");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			String strProjectId = args[0];
			String strPersonName ="";
			String strPersonId = "";

			if(null != strProjectId && strProjectId.length() > 0 && !"null".equalsIgnoreCase(strProjectId))
			{
				Pattern typePattern = new Pattern(DomainConstants.TYPE_DEPARTMENT);
				typePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);

				Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_DIVISION);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT);

				StringList objectSelectsList = new StringList();
				objectSelectsList.addElement(DomainObject.SELECT_ID);				
				objectSelectsList.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "|to.current=='Active'].to.id");
				objectSelectsList.addElement(DomainObject.SELECT_TYPE);
				objectSelectsList.addElement(DomainObject.SELECT_NAME);
								
				DomainObject buDO = DomainObject.newInstance(context, new BusinessObject(DomainConstants.TYPE_BUSINESS_UNIT,PROJECT_MANAGEMENT_DIRECTORATE,"-",VAULT_ESERVICE_PRODUCTION));
				
				if(null != buDO)
				{
					// fetch all BUs & departments of system							
					MapList connectedBUDeptList = buDO.getRelatedObjects(context, 
							relPattern.getPattern(), 
							typePattern.getPattern(), 
							objectSelectsList,
							null, 
							false, 
							true, 
							(short)0, 
							"", 
							"");
					
					int nConnectedBUDeptListSize = 0;
					if(null != connectedBUDeptList)
						nConnectedBUDeptListSize = connectedBUDeptList.size();

					for(int nBUDeptCount = 0; nBUDeptCount < nConnectedBUDeptListSize; nBUDeptCount++)
					{
						Map tempMap = (Map)connectedBUDeptList.get(nBUDeptCount);
						
						String strType = (String)tempMap.get(DomainConstants.SELECT_TYPE);
						String strBUDeptName = (String)tempMap.get(DomainConstants.SELECT_NAME);
						String strBUDeptHeadId = (String)tempMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id");
						
						addMemberToProject(context, strBUDeptHeadId, strProjectId, "Project Member", "");
					}
				}
				
				
				
				/*DomainObject projectDO = DomainObject.newInstance(context, strProjectId);

				String strContextUser = context.getUser().trim();
				StringList objSelects = new StringList(2);
				objSelects.addElement(DomainConstants.SELECT_NAME);
				objSelects.addElement(DomainConstants.SELECT_ID);
				
				String strWhereClause = "(current==Active && to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "]==true && to[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute.value!='')";

				MapList mlPersonList = DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, VAULT_ESERVICE_PRODUCTION,strWhereClause,objSelects);

				if (null!= mlPersonList && !mlPersonList.isEmpty())
				{
					Iterator listItr = mlPersonList.iterator();
					while (listItr.hasNext()) 
					{
						Map personMap = (Map) listItr.next();
						strPersonName = (String) personMap.get(DomainConstants.SELECT_NAME);
						strPersonId   = (String) personMap.get(DomainConstants.SELECT_ID);

						if(null != strPersonName && !strPersonName.equalsIgnoreCase(strContextUser))
						{
							//projectDO.addToObject(context,relationshipType, strPersonId);
							addMemberToProject(context, strPersonId, strProjectId, "Project Member", "");
						}
					}
				}*/
			}
		}
		catch (Exception e) 
		{
			System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToMasterProject Exception....."+e);
			throw new FrameworkException(e);
		}
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToMasterProject exit.....");
		return 0;
	}

	/**
	 * Trigger method called on 
	 * 	1) Project Concept promotion from Review to Approved state (Promote Action trigger)
	 * 
	 * This method will notify all the members of project once the project is approved
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - Object Id
	 *            1 - Object Name
	 *            
	 * @returns 0 for sucess and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 * 
	 */
	public int notifyProjectMembers(Context context, String[] args) throws FrameworkException
	{
		System.out.println("\n... JPO:MSILProject:notifyProjectMembers start...");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}

		try
		{
			String strProjectId = args[0];
			StringList toList = new StringList();
		
			com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

			if(null != strProjectId && !"".equalsIgnoreCase(strProjectId) && strProjectId.length() > 0)
			{
				String strProjectName = args[1];
				// Form the subject and body
				String languageStr = context.getSession().getLanguage();
				String strSubject = i18nNow.getI18nString("emxProgramCentral.Project.Approve.MailNotificationSubject", "emxProgramCentralStringResource", languageStr);
				String strBody = i18nNow.getI18nString("emxProgramCentral.Project.Approve.MailNotificationBody", "emxProgramCentralStringResource", languageStr);
				
				StringBuffer messageBody = new StringBuffer();
				messageBody.append(strBody);
				
				StringList memberSelects = new StringList(1);
				memberSelects.add(person.SELECT_NAME);
				
				StringList relSelects = new StringList(2);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
				
				project.setId(strProjectId);
				MapList memberList = project.getMembers(context, memberSelects, relSelects, null, null, true);
				
				if(null != memberList && memberList.size() > 0)
				{
					Iterator memberListItr = memberList.iterator();
					Map objectMap = null;
					while (memberListItr.hasNext())
					{
						objectMap = (Map) memberListItr.next();
						String strProjectAccess = (String) objectMap.get(MemberRelationship.SELECT_PROJECT_ACCESS);
						if(null != strProjectAccess && !"".equalsIgnoreCase(strProjectAccess) && strProjectAccess.length() > 0 && !"Project Owner".equalsIgnoreCase(strProjectAccess))
						{
							toList.add((String)objectMap.get(person.SELECT_NAME));							
						}
					}
						
					if(null != toList && toList.size() > 0)
					{						
						MailUtil.sendNotification(context,
								toList, //toList
								null, //ccList
								null,   //bccList
								strSubject, //subjectKey
								null, //subjectKeys
								null, //subjectValues
								messageBody.toString(), //messageKey
								null, //messageKeys
								null, //messageValues
								new StringList(strProjectId), //objectIdList
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
								new StringList(strProjectId), // Object list
								null,         // company name
								"emxComponentsStringResource");     // Property file
					}
				}
			}
		}
		catch (Exception e) 
		{
			System.out.println("\n..... JPO:MSILProject:notifyProjectMembers Exception....."+e);
			throw new FrameworkException(e);
		}
		System.out.println("\n... JPO:MSILProject:notifyProjectMembers exit...");
		return 0 ;
	}
	
	/**
	 * Trigger method called on 
	 * 	1) Project Space creation (Create Action trigger)
	 *  2) Project Space change owner (ChangeOwner Action trigger)
	 * 
	 * This method will connect DPMs and above (of the department of the project owner) as the member to the Sub Project
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID 		- 	Object Id
	 *            1 - OWNER			-   Object Owner
	 *            
	 * @returns 0 for success and 1 for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 */

	public int addDPMAndAboveToSubProject(Context context, String[] args) throws Exception
	{
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject start.....");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			RelationshipType relationshipType = new RelationshipType(DomainConstants.RELATIONSHIP_MEMBER);
			com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
			
			String strProjectId = args[0];
			String strPersonName ="";
			String strPersonId = "";

			if(null != strProjectId && strProjectId.length() > 0 && !"null".equalsIgnoreCase(strProjectId))
			{
				String strProjectOwner = args[1];

				// FETCH PROJECT MEMBERS
				StringList memberSelects = new StringList(6);
				memberSelects.add(person.SELECT_ID);
				memberSelects.add(person.SELECT_TYPE);
				memberSelects.add(person.SELECT_NAME);
				memberSelects.add(person.SELECT_LEVEL);
				memberSelects.add(person.SELECT_FIRST_NAME);
				memberSelects.add(person.SELECT_COMPANY_NAME);
				memberSelects.add(person.SELECT_LAST_NAME);

				StringList relSelects = new StringList(2);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
				
				// FETCH REPORTING HIERARCHY OF THE PROJECT OWNER
				com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
				String strOwnerId = loginPerson.getPerson(context, strProjectOwner).getId();

				DomainObject personDO = DomainObject.newInstance(context, strOwnerId);
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_NAME);
				objectSelects.addElement(DomainConstants.SELECT_TYPE);
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id");
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[Project Role].value");

				Pattern typePattern = new Pattern(DomainConstants.TYPE_BUSINESS_UNIT);
				typePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
				
				Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_DIVISION);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_MEMBER);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT);
				
				// FETCH DEPARTMENT/BUSINESS UNIT OF CONTEXT USER
				MapList departmentBUList = personDO.getRelatedObjects(context, 
						relPattern.getPattern(), 
						typePattern.getPattern(),
						objectSelects, 
						null,
						true, 
						false, 
						(short) 0, 
						null, 
						null);
				
				DomainObject projectDO = DomainObject.newInstance(context, strProjectId);
				String strContextUser = context.getUser().trim();
			
				if (null!= departmentBUList && !departmentBUList.isEmpty())
				{
					Iterator listItr = departmentBUList.iterator();
					while (listItr.hasNext()) 
					{
						Map personMap = (Map) listItr.next();
						strPersonName = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");
						strPersonId   = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id");
						String strLeadResponsibilityAttribute   = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[Project Role].value");
						
						if(null != strPersonName && !strPersonName.equalsIgnoreCase(strContextUser) && (null != strLeadResponsibilityAttribute && strLeadResponsibilityAttribute.length() > 0))
						{			
							//projectDO.addToObject(context,relationshipType, strPersonId);
							addMemberToProject(context, strPersonId, strProjectId, "Project Member", "");
						}
					}
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject Exception....."+e);
			throw new FrameworkException(e);
		}
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject exit.....");
		return 0;
	}
	
	public int addDPMAndAboveToSubProjectChangeOwner(Context context, String[] args) throws Exception
	{
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject start.....");
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			RelationshipType relationshipType = new RelationshipType(DomainConstants.RELATIONSHIP_MEMBER);
			com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
			
			String strProjectId = args[0];
			String strPersonName ="";
			String strPersonId = "";

			if(null != strProjectId && strProjectId.length() > 0 && !"null".equalsIgnoreCase(strProjectId))
			{
				String strProjectOwner = args[1];

				// FETCH PROJECT MEMBERS
				StringList memberSelects = new StringList(6);
				memberSelects.add(person.SELECT_ID);
				memberSelects.add(person.SELECT_TYPE);
				memberSelects.add(person.SELECT_NAME);
				memberSelects.add(person.SELECT_LEVEL);
				memberSelects.add(person.SELECT_FIRST_NAME);
				memberSelects.add(person.SELECT_COMPANY_NAME);
				memberSelects.add(person.SELECT_LAST_NAME);

				StringList relSelects = new StringList(2);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
				relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
				
				project.setId(strProjectId);
				MapList memberList = project.getMembers(context, memberSelects, relSelects, null, null, true);

				StringList slProjectMemberList = new StringList();
				if(null != memberList && memberList.size() > 0)
				{
					Iterator memberListItr = memberList.iterator();
					Map objectMap = null;
					while (memberListItr.hasNext())
					{
						objectMap = (Map) memberListItr.next();
						String strMemberName = (String) objectMap.get(person.SELECT_NAME);
						if(null != strMemberName && !"".equalsIgnoreCase(strMemberName) && strMemberName.length() > 0)
						{
							slProjectMemberList.addElement(strMemberName);							
						}
					}
				}
				
				
				// FETCH REPORTING HIERARCHY OF THE PROJECT OWNER
				com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
				String strOwnerId = loginPerson.getPerson(context, strProjectOwner).getId();

				DomainObject personDO = DomainObject.newInstance(context, strOwnerId);
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_NAME);
				objectSelects.addElement(DomainConstants.SELECT_TYPE);
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id");
				objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[Project Role].value");

				Pattern typePattern = new Pattern(DomainConstants.TYPE_BUSINESS_UNIT);
				typePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
				
				Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_DIVISION);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_MEMBER);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT);
				
				// FETCH DEPARTMENT/BUSINESS UNIT OF CONTEXT USER
				MapList departmentBUList = personDO.getRelatedObjects(context, 
						relPattern.getPattern(), 
						typePattern.getPattern(),
						objectSelects, 
						null,
						true, 
						false, 
						(short) 0, 
						null, 
						null);
				
				DomainObject projectDO = DomainObject.newInstance(context, strProjectId);
				String strContextUser = context.getUser().trim();
			
				if (null!= departmentBUList && !departmentBUList.isEmpty())
				{
					Iterator listItr = departmentBUList.iterator();
					while (listItr.hasNext()) 
					{
						Map personMap = (Map) listItr.next();
						strPersonName = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.name");
						strPersonId   = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].to.id");
						String strLeadResponsibilityAttribute   = (String) personMap.get("from[" + DomainConstants.RELATIONSHIP_LEAD_RESPONSIBILITY + "].attribute[Project Role].value");
						
						if((null != strPersonName && !strPersonName.equalsIgnoreCase(strContextUser) && !slProjectMemberList.contains(strPersonName)) && (null != strLeadResponsibilityAttribute && strLeadResponsibilityAttribute.length() > 0))
						{			
							//projectDO.addToObject(context,relationshipType, strPersonId);
							addMemberToProject(context, strPersonId, strProjectId, "Project Member", "");
						}
					}
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject Exception....."+e);
			throw new FrameworkException(e);
		}
		System.out.println("\n..... JPO:MSILProject:addDPMAndAboveToSubProject exit.....");
		return 0;
	}
	
	public void addMemberToProject(Context context, String strPersonId, String strProjectId, String strProjectAccess, String strProjectRole) throws FrameworkException
	{
		try {
		ContextUtil.startTransaction(context,true);
		DomainObject domPerson = DomainObject.newInstance(context,strPersonId);
		DomainObject domProject = DomainObject.newInstance(context,strProjectId);

		DomainRelationship domRel = DomainRelationship.connect(context, domProject, DomainConstants.RELATIONSHIP_MEMBER, domPerson);
		domRel.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ACCESS,strProjectAccess);
		domRel.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ROLE,strProjectRole);


		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		com.matrixone.apps.common.WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);

		ProjectSpace projectSpace;
		
			projectSpace = new ProjectSpace(strProjectId);
		
		StringList busSelects = new StringList();
		busSelects.add(WorkspaceVault.SELECT_NAME);
		busSelects.add(WorkspaceVault.SELECT_ID);
		busSelects.add(WorkspaceVault.SELECT_ACCESS_TYPE);
		busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
		busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
		workspaceVault.setContentRelationshipType(WorkspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);

		// code to get all the Vaults that we want to set permissions on
		// old code to set access needed to set access on all the folders so set recurse level to 0
		// new code only needs to set top level folders and the children will inherit
		short sLevel = 0;
		String newSecurityModel = "false";
		try
		{
			newSecurityModel = FrameworkProperties.getProperty(context,"emxComponents.NewSecurityModel");
		} catch(Exception ex)
		{
			//Do nothing 
		}
		if("true".equalsIgnoreCase(newSecurityModel) )
		{
			sLevel = 1;  // only need to set top level folders for new security model
		} else {
			sLevel = 0;  //  need to set access on all folders in old security model
		}

		String relationship = DomainConstants.RELATIONSHIP_WORKSPACE_VAULTS;

		// multi-level expand requires 2 different relationships.
		relationship += "," + DomainConstants.RELATIONSHIP_SUB_VAULTS;
		//Only the Folder objects with inherited access type will be searched down the hierarchy
		String sWhereClause = "attribute["+DomainConstants.ATTRIBUTE_ACCESS_TYPE+"] == Inherited";
		// expand from parent
		MapList Vaultlist = projectSpace.getRelatedObjects(
				context,        // context.
				relationship,   // rel filter.
				DomainConstants.TYPE_WORKSPACE_VAULT,            // type filter.
				busSelects,  // business object selectables.
				null,           // relationship selectables.
				false,          // expand to direction.
				true,           // expand from direction.
				sLevel,         // level
				sWhereClause,           // object where clause
				null);          // relationship where clause

		WorkspaceVault vault = null;
		String defaultAccess = null;
		String strAccessXML = null;
		ProjectRoleVaultAccess vaultAccess = null;
		MapList inheritedVL = new MapList();
		String strVaultId = null;

		for(int i=0; i < Vaultlist.size(); i++)
		{    
			Map vaultMap = (Map) Vaultlist.get(i);    
			defaultAccess = (String) vaultMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
			strAccessXML = (String) vaultMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
			String strThisVaultId=(String)vaultMap.get(WorkspaceVault.SELECT_ID);

			workspaceVault.setId(strThisVaultId);
			Map accessMap = new HashMap();

			String strPerson= domPerson.getInfo(context,DomainConstants.SELECT_NAME);
			String strRole = "";
			String roleVaultAccess = null;

			accessMap.put(strPerson, defaultAccess);

			MapList vaultList = new MapList();
			HashMap vaultIDMap = new HashMap();
			vaultIDMap.put(DomainConstants.SELECT_ID, strThisVaultId);
			vaultList.add(vaultIDMap);

			workspaceVault.setUserPermissions(context, accessMap, vaultList);
		}  

		ContextUtil.commitTransaction(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Command 'Create New Project Concept' will be visible to only NPE users.
	 *  
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            
	 * @returns boolean
	 * 			  true if command to be shown
	 * @throws Exception
	 *             if the operation fails
	 */
	public boolean hasAccessToCreateConcept(Context context, String args[]) throws Exception
	{
		System.out.println("\n... MSILProject:hasAccessToCreateConcept start...");
		boolean bAccess = false;
		try
		{
			String strContextUser = context.getUser().trim();

			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			String strContextUserId = loginPerson.getPerson(context, strContextUser).getId();

			DomainObject personDO = DomainObject.newInstance(context, strContextUserId);

			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			
			Pattern typePattern = new Pattern(DomainConstants.TYPE_BUSINESS_UNIT);
			typePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
				
			// FETCH DEPARTMENT/BUSINESS UNIT OF CONTEXT USER
			MapList departmentBUList = personDO.getRelatedObjects(context, 
					DomainConstants.RELATIONSHIP_MEMBER, 
					typePattern.getPattern(),
					objectSelects, 
					null,
					true, 
					false, 
					(short) 0, 
					null, 
					null);
			
			if (null!= departmentBUList && !departmentBUList.isEmpty())
			{
				Iterator listItr = departmentBUList.iterator();
				while (listItr.hasNext()) 
				{
					Map personMap = (Map) listItr.next();
					String strDepartmentName = (String) personMap.get(DomainConstants.SELECT_NAME);
					
					if(null != strDepartmentName && strDepartmentName.length() > 0 && PROJECT_CREATE_DEPARTMENT.equalsIgnoreCase(strDepartmentName))
					{			
						bAccess = true;
					}
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("\n... MSILProject:hasAccessToCreateConcept Exception..."+ex);
			throw new FrameworkException(ex);
		}
		System.out.println("\n... MSILProject:hasAccessToCreateConcept exit...");
		return bAccess;
	}
}