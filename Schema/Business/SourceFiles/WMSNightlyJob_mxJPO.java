import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.util.Pattern;
import matrix.util.StringList;
import java.util.Collections;
import org.apache.commons.lang.time.DateUtils;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
 
import com.matrixone.apps.domain.util.PersonUtil;
 
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import matrix.db.BusinessInterface;
import matrix.db.BusinessInterfaceList;
import matrix.db.BusinessObject;
 
import matrix.db.JPO;
import matrix.db.Vault;
 
import matrix.util.SelectList;
 



public class WMSNightlyJob_mxJPO extends WMSConstants_mxJPO{

	
	public WMSNightlyJob_mxJPO(Context context,String[] args) {
		super(context,args);
	}
	  
	  
	  
	  
	/** Batch job finds all Active WorkOrder ,get its SDP check validity date if its expires send notification
	 * to owne and supplier person who  has submitted the documents 
	 * 
	 * 
	 * 
	 * 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
 	public void sendNotificationOfDocumentValidity(Context context,String [] args) throws Exception
	{
		 
		DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
		DateTimeFormatter dateStringFormatMMM = DateTimeFormat.forPattern("MMM dd-yyyy");
		String strDiffCompare =  EnoviaResourceBundle.getProperty(context,"WMS.ValidityDate.Reminder.Days");
		int iDiff = Integer.parseInt(strDiffCompare);
		LocalDate today=new LocalDate();
		try {
			StringList objectSelects=new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add(DomainConstants.SELECT_NAME);
			objectSelects.add(DomainConstants.SELECT_OWNER);
			objectSelects.add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
			objectSelects.add("to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
			objectSelects.add("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.name");
			//String strWhere="current=='Active'";
			String strWhere="current==Active";
			Map mOwner = new HashMap();
			MapList mlActiveWOs = DomainObject.findObjects(context,
															TYPE_WMS_WORK_ORDER,				// type filter
															DomainConstants.QUERY_WILDCARD,		// vault filter
															strWhere,							// where clause
															objectSelects);
			Iterator<Map> itr= mlActiveWOs.iterator();
			String strWOId = DomainConstants.EMPTY_STRING;
			String strWOrkOrderOwner = DomainConstants.EMPTY_STRING;
			String TYPE_DELIVERABLE = PropertyUtil.getSchemaProperty(context,"type_Deliverable1");
			Pattern pType = new Pattern(DomainConstants.TYPE_SUPPLIER_DEVELOPMENT_PLAN);
			pType.addPattern(TYPE_DELIVERABLE);
			Pattern pRel = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_SDP);
			//	pRel.addPattern(DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE);
			objectSelects.clear();
			objectSelects.add("attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]");
			objectSelects.add("attribute["+ATTRIBUTE_WMS_SUBMITED_BY+"]");
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add(DomainConstants.SELECT_NAME);
			objectSelects.add(DomainConstants.SELECT_OWNER);
			StringList slRelSelect=new StringList();
			slRelSelect.add(DomainRelationship.SELECT_ID);
			DomainObject domWorkOrder = DomainObject.newInstance(context);
			String strValidUntilDate= DomainConstants.EMPTY_STRING;
			String strSubmittedBy   = DomainConstants.EMPTY_STRING;
			String strWorkOrderTitle =DomainConstants.EMPTY_STRING;
			String strProjectSpace  = DomainConstants.EMPTY_STRING;
			String strSupplierName  =DomainConstants.EMPTY_STRING;
			StringList slWorkOrderOwneList =new StringList();
			StringList slWorkOrderIdList =new StringList();
			StringList slSubmittedByList =new StringList();
			while(itr.hasNext()) {
				Map mWorkOrder = itr.next();  
				strWOId = (String) mWorkOrder.get(DomainConstants.SELECT_ID);
				strWOrkOrderOwner=(String) mWorkOrder.get(DomainConstants.SELECT_OWNER);
				strWorkOrderTitle=(String) mWorkOrder.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
				strProjectSpace =(String) mWorkOrder.get("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.name");
				strSupplierName = (String) mWorkOrder.get("to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
				Map mWorkOrdeData=new HashMap();
				Map mWorkOrdeSubData=new HashMap();
				if(mOwner.containsKey(strWOrkOrderOwner)) {
					mWorkOrdeData =(Map) mOwner.get(strWOrkOrderOwner);
				}
				if(!slWorkOrderOwneList.contains(strWOrkOrderOwner)) slWorkOrderOwneList.add(strWOrkOrderOwner);
				if(!slWorkOrderIdList.contains(strWOId)) slWorkOrderIdList.add(strWOId);
				domWorkOrder.setId(strWOId);
				if(mWorkOrdeData.containsKey(strWOId)) {
					mWorkOrdeSubData=(Map) mWorkOrdeData.get(strWOId);
				}
				mWorkOrdeSubData.put("WO_NAME", strWorkOrderTitle);
				mWorkOrdeSubData.put("WO_PROJECT_NAME", strProjectSpace);
				mWorkOrdeSubData.put("WO_CONTRACTOR", strSupplierName);
				MapList mlDeliverables = domWorkOrder.getRelatedObjects(context,
																			pRel.getPattern(),				// relationship pattern
																			pType.getPattern(),				// object pattern
																			false,							// to direction
																			true,							// from direction
																			(short)0,						// recursion level
																			objectSelects,					// object selects
																			slRelSelect,					// relationship selects
																			"current==Complete",			// object where clause
																			DomainConstants.EMPTY_STRING,	// relationship where clause
																			(short)0,						// No expand limit
																			DomainConstants.EMPTY_STRING,	// postRelPattern
																			null,							// postTypePattern
																			null);
				Iterator<Map> itrDeliverable=mlDeliverables.iterator();
				Map mDeliverPlan=new HashMap();
				while(itrDeliverable.hasNext()) {
					List<Map> slPlan=new ArrayList<Map>();
					Map mDeliverOwnerData=new HashMap();
					Map mDeliver= itrDeliverable.next(); 
					strValidUntilDate=(String)mDeliver.get("attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]");
					LocalDate dValidateDate = dateStringFormat.parseLocalDate(strValidUntilDate);
					int iDiffDays= (Days.daysBetween(today, dValidateDate)).getDays();
					if(iDiffDays < iDiff) {
						strSubmittedBy = (String)mDeliver.get("attribute["+ATTRIBUTE_WMS_SUBMITED_BY+"]");
						if(!slSubmittedByList.contains(strSubmittedBy)) slSubmittedByList.add(strSubmittedBy)  ;
							if(mWorkOrdeSubData.containsKey(strSubmittedBy)) {
								mDeliverOwnerData=(Map) mWorkOrdeSubData.get(strSubmittedBy);
								if(mDeliverOwnerData.containsKey("PLAN_DELIVERABLE")) {
									slPlan=(List)mDeliverOwnerData.get("PLAN_DELIVERABLE");
								}
							}
							slPlan.add(mDeliver);
							mDeliverOwnerData.put("PLAN_DELIVERABLE", slPlan);
							mWorkOrdeSubData.put(strSubmittedBy, mDeliverOwnerData);
						}
				}
				if(mWorkOrdeSubData.size()>0) {
					mWorkOrdeData.put(strWOId, mWorkOrdeSubData);
					mOwner.put(strWOrkOrderOwner, mWorkOrdeData);
				}
			}
			StringBuilder sbSubject =new StringBuilder();
			StringBuilder sbMsgBody=new StringBuilder();
			//process each 	   
			String strWorkOrderName=DomainConstants.EMPTY_STRING;
			String strProjectName=DomainConstants.EMPTY_STRING;
			boolean bAnyDeliverable=false;
			if(slWorkOrderOwneList.size() >0 ) {
			for(int i=0;i< slWorkOrderOwneList.size();i++) {
				StringBuilder sb=new StringBuilder();
				if(mOwner.containsKey((String)slWorkOrderOwneList.get(i))){
					Map  mWorkOrderTemp = (Map) mOwner.get(slWorkOrderOwneList.get(i));
					for(int k=0;k<slWorkOrderIdList.size();k++ ) { 
						if(mWorkOrderTemp.containsKey((String)slWorkOrderIdList.get(k))) {
							Map mWOSubData =(Map) mWorkOrderTemp.get((String)slWorkOrderIdList.get(k));
								for(int n=0;n<slSubmittedByList.size();n++) {
									if(mWOSubData.containsKey((String)slSubmittedByList.get(n))) {
									sbMsgBody.append("<html> <table border=\"1px\"> <tr> <th>Work Order Name</th>  <th>Project Name</th> <th>Document Name</th><th>Validity Date</th> <th>Submitted By</th>   <tr> </tr>");
									StringList personCCList = new StringList();
									personCCList.add((String)slSubmittedByList.get(n));
									Map mPlanOwner =(Map) mWOSubData.get((String)slSubmittedByList.get(n));
									List lstPlanDel = (List) mPlanOwner.get("PLAN_DELIVERABLE");
									strWorkOrderName=(String)mWOSubData.get("WO_NAME");
									strProjectName=(String)mWOSubData.get("WO_PROJECT_NAME");
									sbMsgBody.append("<tr><td rowspan='"+lstPlanDel.size()+"'>").append(strWorkOrderName).append("</td>");
									sbMsgBody.append("<td rowspan='"+lstPlanDel.size()+"'>").append(strProjectName).append("</td>");
									StringBuilder child=new StringBuilder();
									bAnyDeliverable=false;
										for(int j=0;j<lstPlanDel.size();j++) {
											Map m  = (Map) lstPlanDel.get(j);
											if(j!=0) {
												sbMsgBody.append("<tr>");
											}
											sbMsgBody.append("<td>").append((String)m.get("name")).append("</td>");
											sbMsgBody.append("<td>").append(dateStringFormatMMM.print(dateStringFormat.parseLocalDate((String)m.get("attribute[WMSValidUntilDate]")))).append("</td>");
											sbMsgBody.append("<td>").append((String)m.get("attribute[WMSSubmittedBy]")).append("</td></tr>");
											bAnyDeliverable=true;
										}
										StringList slObjectIdList = new StringList(1);
										StringList personToList = new StringList();
										personToList.add((String)slWorkOrderOwneList.get(i));
										sbMsgBody.append("</table> <br> </br> -Regards,</br> Team MPower </html>");
										sbSubject.append("Validity is about to expired/has expired for below Documents ");
										if(bAnyDeliverable){
											emxNotificationUtil_mxJPO.sendJavaMail(context,null,personCCList,null,sbSubject.toString(),sbMsgBody.toString(),null,null,null,null,"email");
											// MailUtil.sendMessage(context,personCCList,null,null,sbSubject.toString(),sbMessage.toString(),slObjectIdList);
										}
										sbMsgBody.setLength(0);
										sbSubject.setLength(0);
									}
								}
							}
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Code added for B3- start
	public void sendDPRMailNotification(Context context, String args[])throws Exception {
		try {
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_OWNER);
			String strDataVaults = "Data Vaults";
			String strSubVaults = "Sub Vaults";
			StringList selectStmts = new StringList(5);
			selectStmts.addElement(DomainConstants.SELECT_ID);
			selectStmts.addElement(DomainConstants.SELECT_NAME);
			selectStmts.addElement(DomainConstants.SELECT_ORIGINATED);
			selectStmts.addElement("from[Sub Vaults]");
			selectStmts.addElement("to[Sub Vaults].from[Controlled Folder].id");
			selectStmts.addElement("from[Vaulted Documents Rev2]");
			selectStmts.addElement("from[Vaulted Documents Rev2].to.originated");
			ContextUtil.pushContext(context);
			MapList mlProjectSpace = DomainObject.findObjects(context,
					"Project Space",                                 // type filter
					"*",         // vault filter
					"",                            // where clause
					objectSelects);

			for (int i = 0; i < mlProjectSpace.size(); i++) {
				Map map = (Map) mlProjectSpace.get(i);
				DomainObject projectObj = new DomainObject(
						(String) map.get(DomainConstants.SELECT_ID));
				String strProjectOwner = (String) map.get(DomainConstants.SELECT_OWNER);
				BusinessObject personObject = new BusinessObject("Person",strProjectOwner,"-","eService Production");
				String personId = personObject.getObjectId(context);
				DomainObject domPerson = new DomainObject(personId);
				MapList mapListItems = domPerson.getRelatedObjects(context, // matrix context
						"WMSReportingManager", // relationship pattern
						"Person", // type pattern
						objectSelects, // object selects
						null, // relationship selects
						false, // to direction
						true, // from direction
						(short) 1, // recursion level
						DomainConstants.EMPTY_STRING, // object where clause
						DomainConstants.EMPTY_STRING, // relationship where clause
						0);
				int iSize = mapListItems.size();
				Map mapTemp = null;
				StringList slAllDepartmentMembers = new StringList();
				HashMap hmToList = new HashMap();
				Map mObjectInfo = (Map) projectObj.getInfo(context, objectSelects);
				for (int iCount=0;iCount<iSize; iCount++)
				{
					mapTemp=(Map)mapListItems.get(iCount);
					slAllDepartmentMembers.add((String)mapTemp.get(DomainConstants.SELECT_NAME));
				}
				StringList toList = new StringList(strProjectOwner);
				toList.addAll(slAllDepartmentMembers);
				HashSet<String> hsToList = new HashSet<String>(toList);
				for (String toMembers : hsToList) {
					if (hmToList.containsKey(toMembers)) {
						MapList mapListComp = (MapList) hmToList.get(toMembers);
						mapListComp.add(mObjectInfo);
						hmToList.put(toMembers, mapListComp);
					} else {
						MapList temp = new MapList();
						temp.add(mObjectInfo);
						hmToList.put(toMembers, temp);
					}
				}
				Iterator<Map.Entry<String, MapList>> itr = hmToList.entrySet().iterator();
				
				MapList mlProjectFolder = projectObj.getRelatedObjects(context,
						strDataVaults + ","
								+ strSubVaults, // relationship  pattern
								"Controlled Folder", // object pattern
								selectStmts, // object selects
								null, // relationship selects
								false, // to direction
								true, // from direction
								(short) 0, // recursion level
								null, // object where clause
								null);
				java.util.Date sysDate = new java.util.Date();

				for (int l = 0; l < mlProjectFolder.size(); l++) {
					Map mapFolder = (Map) mlProjectFolder.get(l);
					String isFolder = (String) mapFolder.get("from[Sub Vaults]");
					String isDoc = (String) mapFolder.get("from[Vaulted Documents Rev2]");
					Object docIds = (Object) mapFolder.get("from[Vaulted Documents Rev2].to.originated");
					StringList documents = new StringList();
					if(docIds instanceof String) {
						documents.add((String)docIds);
					}else {
						documents = (StringList)docIds;
					}
					if("False".equalsIgnoreCase(isFolder)) {
						boolean sendMail = false;
						ArrayList<String> paths = new ArrayList<String>();
						if("True".equalsIgnoreCase(isDoc)) {
							boolean checkedInToday = false;
							for(Object obj : documents) {
								java.util.Date originated = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate((String)obj);

								if(DateUtils.isSameDay(sysDate, originated)) {
									checkedInToday = true;
									break;
								}
							}
							if(!checkedInToday) {
								DomainObject doF = DomainObject.newInstance(context);
								doF.setId((String)mapFolder.get("id"));
								paths.add((String)mapFolder.get("id"));
								getCompleteFolderPath(context,mlProjectFolder, mapFolder, paths);
								sendMail = true;
							}
						}else {
							DomainObject doF = DomainObject.newInstance(context);
							doF.setId((String)mapFolder.get("id"));
							paths.add((String)mapFolder.get("id"));
							getCompleteFolderPath(context,mlProjectFolder, mapFolder, paths);
							sendMail = true;
						}
						DomainObject doF = DomainObject.newInstance(context);
						doF.setId((String) map.get(DomainConstants.SELECT_ID));
						paths.add((String) map.get(DomainConstants.SELECT_ID));
						Collections.reverse(paths);
						//
						StringList slToPersonList = new StringList();
						while (itr.hasNext()) {
							Map.Entry<String, MapList> entry = itr.next();
							String strKeyName = entry.getKey();
							if(strKeyName!=null && !strKeyName.isEmpty()) {
								slToPersonList.add(strKeyName);
							}
							MapList mlRFQs = entry.getValue();
							String strToEmail = PersonUtil.getEmail(context, strKeyName);
						}
						String fromAgent = context.getUser();
						String notifyType = "IconMail";
						MailUtil.setTreeMenuName(context, "type_ControlledFolder");
						String strSubject=EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(), "WMS.DPR.Subject");
						StringBuffer sbSubject = new StringBuffer();
						sbSubject.append(strSubject);
						sbSubject.append(" : ");
						String strMessage=EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(), "WMS.DPR.Message");
						StringBuffer sbMessage = new StringBuffer();
						sbMessage.append(strMessage);
						sbMessage.append(" : ");
						sbMessage.append(paths);
						StringList slObjectIdList = new StringList();
						slObjectIdList.addAll(paths);
						//slToPersonList.add("203262");
						if(sendMail) {
							//${CLASS:emxNotificationUtil}.sendJavaMail(context, new StringList(strKeyName), null, null, "Discussion Thread Created", sbMessage.toString(), null, fromAgent, null, strMessageId, notifyType);
							emxNotificationUtil_mxJPO.sendJavaMail(context, slToPersonList, null, null, sbSubject.toString(), sbMessage.toString(), "", fromAgent, null, slObjectIdList, notifyType);
							//MailUtil.sendMessage(context,new StringList(strKeyName) ,null,null,sbSubject.toString(),sbMessage.toString(),slObjectIdList);
						}
						//}
					}
					
				}
			}

			ContextUtil.popContext(context);
		}catch (Exception e) {
			e.printStackTrace();//TODO: handle exception
		}
	}

	private void getCompleteFolderPath(Context context,MapList folders,Map folderInfo, ArrayList<String> paths) throws FrameworkException {
		DomainObject doF = DomainObject.newInstance(context);
		String objectId = (String)folderInfo.get("to[Sub Vaults].from[Controlled Folder].id");
		if(objectId!=null && !objectId.isEmpty()){
			doF.setId(objectId);
			paths.add(objectId);
		}
		for(Object folder : folders) {
			Map folderDetails = (Map)folder;
			if(((String)folderDetails.get("id")).equals((String)folderInfo.get("to[Sub Vaults].from[Controlled Folder].id"))) {
				getCompleteFolderPath(context,folders,folderDetails,paths);
			}
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
 		public void sendProjectTaskPreFinishNotification(Context context,String[] args) throws Exception 
 		{
 			MapList mlFindProjectList = new MapList();
 	 		Map mapObject = null;
 	 		String strProjectId = null;
 			String strProjName = null;
 			String strProjOwner = null;
 			StringList slSelectables = new StringList();
 			slSelectables.addElement(DomainConstants.SELECT_NAME);
 			slSelectables.addElement(DomainConstants.SELECT_ID);
 			slSelectables.addElement(DomainConstants.SELECT_OWNER);
 			StringList slDepartmentSelect = new StringList(2);
 			slDepartmentSelect.addElement(DomainConstants.SELECT_NAME);
 			Map mPersonTaskMapping = new HashMap();
 			try {
 				
 			mlFindProjectList = DomainObject.findObjects(
 									context,								//current context object
 									DomainConstants.TYPE_PROJECT_SPACE,		//type pattern
 									DomainConstants.QUERY_WILDCARD,			//name pattern.
 									DomainConstants.QUERY_WILDCARD,			//revision pattern.
 									DomainConstants.QUERY_WILDCARD,			//owner pattern.
 									DomainConstants.QUERY_WILDCARD,			//vault pattern.
 								    "current=='Active'",							//where expression.
 									false,									//false - do not find subtypes
 									slSelectables);							//select clause.
 				
 				Iterator itr = mlFindProjectList.iterator();
 	
 				while(itr.hasNext()) {
 					mapObject = (Map)itr.next();
 					strProjectId = (String)mapObject.get(DomainConstants.SELECT_ID);
 					strProjName = (String)mapObject.get(DomainConstants.SELECT_NAME);
 					
 					strProjOwner = (String)mapObject.get(DomainConstants.SELECT_OWNER);
 					ContextUtil.pushContext(context, strProjOwner,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
 					MSILAccessRights_mxJPO msilObj= new MSILAccessRights_mxJPO(context, args);
 					if(!msilObj.isPersonFromCivil(context, args))  continue;
 			  			//calling below method to get Tasks
 					
 							getAllConnectedTasks (context, strProjectId,strProjOwner,strProjName,mPersonTaskMapping);
 	 	         	}
 				//System.out.println("mPersonTaskMapping"+mPersonTaskMapping);
 				Set<String> keyUser = mPersonTaskMapping.keySet();
 				Iterator<String> itrUsers = keyUser.iterator();
 				String userNameRole=DomainConstants.EMPTY_STRING;
 				String strToUser=DomainConstants.EMPTY_STRING;
 				StringBuilder sbHTMLMsg =new StringBuilder();
 				String strProjectName=DomainConstants.EMPTY_STRING;
 				while(itrUsers.hasNext()) {
 					StringList personToList=new StringList();
 					userNameRole=itrUsers.next();
 					Map mProjectData  =(Map) mPersonTaskMapping.get(userNameRole);
 					strToUser = userNameRole.substring(0, userNameRole.indexOf("~"));
 					personToList.add(strToUser);
 					Set sProjectName = mProjectData.keySet();
 				 	Iterator<String> itrProjects = sProjectName.iterator();
 					while(itrProjects.hasNext()) {
	 					 strProjectName=itrProjects.next();
	 					 List slTaskList = 	(List) mProjectData.get(strProjectName);
	 					 sbHTMLMsg.append("<table border='1px'> <tr> <th>Project Name</th><th>Task Name</th><th> Est. Finish Date (YYYY-MM-DD)</th>");
	 					 sbHTMLMsg.append("<tr> <td rowspan='").append(slTaskList.size()).append("'>").append(strProjectName).append("</td>");
	 					for(int i=0;i<slTaskList.size();i++) {
	 						Map mTask = (Map) slTaskList.get(i);
	 						if(i!=0) sbHTMLMsg.append("<tr>");
	 						sbHTMLMsg.append("<td>").append(mTask.get("TaskName")).append("</td>");
	 						sbHTMLMsg.append("<td>").append(mTask.get("TaskFinishDate")).append("</td>");
	 						if(i==0) {sbHTMLMsg.append("</tr>");
	 						if(i!=0) sbHTMLMsg.append("</tr>");
 						 }
 						
 					   }
 					   sbHTMLMsg.append("</table> </br> </br>"); 
 					   emxNotificationUtil_mxJPO.sendJavaMail(context,personToList,null,null,"Task Finish date due reminder".toString(),sbHTMLMsg.toString(),sbHTMLMsg.toString(),context.getUser(),null,null,"email");
 				 	   sbHTMLMsg.setLength(0);
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
 		public void getAllConnectedTasks(Context context,String strProjectId, String strProjOwner,String strProjName, Map mPersonTaskMapping) throws Exception 
 		{   
 		 
 			DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
 			MapList mlConnectedTasks = new MapList();
 			String strTaskFinishDate = null;
 			String strPercentComplete = null;
 			double dblPercentComplete;
 			Map mapTask = null;
 			try {
 			 	StringList slTaskSelect = new StringList();
 				slTaskSelect.addElement(DomainConstants.SELECT_NAME);
 				slTaskSelect.addElement(DomainConstants.SELECT_ID);
 				slTaskSelect.addElement("attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE +"]");
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
 												(short)0,							//the number of levels to expand, 0 equals expand all.
 												"current!='Complete'",	,								//where clause to apply to objects, can be empty ""
 												null);								//where clause to apply to relationship, can be empty ""
 				
 				LocalDate ldToday=new LocalDate();
 				Iterator itrTask = mlConnectedTasks.iterator();
 	 		 	while(itrTask.hasNext()) {
 					mapTask = (Map)itrTask.next();
 					strTaskFinishDate = (String)mapTask.get("attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE +"]");
 					strPercentComplete = (String)mapTask.get("attribute["+ DomainConstants.ATTRIBUTE_PERCENT_COMPLETE +"]");
 					dblPercentComplete = Double.parseDouble(strPercentComplete);
 					LocalDate dTaslFinishDate =  dateStringFormat.parseLocalDate(strTaskFinishDate);
 			 	 	int iDateDiff = Days.daysBetween(ldToday, dTaslFinishDate).getDays();
 				 	if(iDateDiff<=5) {
 			 	 		Map mInfo = new HashMap();
 		 				mInfo.put("ProjectName", strProjName);
 				 		mInfo.put("TaskName", (String)mapTask.get(DomainConstants.SELECT_NAME));
 					 	mInfo.put("TaskFinishDate", dTaslFinishDate.toString());
 					    getUserDetails(context,strProjectId,strProjOwner,iDateDiff,dblPercentComplete, mInfo,mPersonTaskMapping);
 				 	}
 				}
 			}catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 	
 			/*
 		/**
 		 * Added Method getAllProjects
 		 * @param context - current context
 		 * @param String String[] args - holds ObjID
 		 * @throws Exception - the exception
 		*/
 		public void getUserDetails (Context context, String strProjectId, String strProjOwner, long DateDiff, double dblPercentComplete,Map mInfo,Map mPersonTaskMapping) throws Exception
 		{
 			com.matrixone.apps.program.ProjectSpace projectSpace = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
 			StringList slSelect = new StringList(3);
 			slSelect.add(Person.SELECT_NAME);
 			StringList slMemberSelect = new StringList(1);
 			slMemberSelect.add(MemberRelationship.SELECT_PROJECT_ROLE);
 			 
 			StringList slMailToList = new StringList();
 			String strProjectName=(String)mInfo.remove("ProjectName");
 			Map mTaskData = new HashMap();
 	 		try {
 				projectSpace.setId(strProjectId);
 				MapList membersList = projectSpace.getMembers(context, slSelect, slMemberSelect, null, null);
 				Iterator membersListItr = null;
 				membersListItr = membersList.iterator();
 				Map mProjectSM=new HashMap();
 				List<Map> listTaskDataSM=new ArrayList<Map>();
 				Map mProjectPOwner=new HashMap();
 				List<Map> listTaskDataPOwner=new ArrayList<Map>();
 				Map mProjectReviwer=new HashMap();
 				List<Map> listTaskDataReviewer=new ArrayList<Map>();
 				while (membersListItr.hasNext())
 				{
 					Map Currentmember = (Map) membersListItr.next();
 					String strMemberName = (String) Currentmember.get(Person.SELECT_NAME);
 		 			String strProjectRole = (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ROLE);
 		 	 		if (DateDiff == 5) {
 					
 						
 						if ("SectionManager".equals(strProjectRole)) {
 							if(mPersonTaskMapping.containsKey(strMemberName+"~SectionManager")) {
 	 							mProjectSM=(Map)mPersonTaskMapping.get(strMemberName+"~SectionManager");
 	 						}
 	 						if(mProjectSM.containsKey(strProjectName)) {
 	 							listTaskDataSM =(List<Map>)mProjectSM.get(strProjectName);
 	 						}
 	 						listTaskDataSM.add(mInfo);
 	 						mProjectSM.put(strProjectName, listTaskDataSM);
 	 						mPersonTaskMapping.put(strMemberName+"~SectionManager", mProjectSM);
 						}
 					} 
 					else if (DateDiff  == 3) {
 						if (strMemberName.equals(strProjOwner)) { 
 							if(mPersonTaskMapping.containsKey(strMemberName+"~ProjectLead")) {
 								mProjectPOwner=(Map)mPersonTaskMapping.get(strMemberName+"~ProjectLead");
 	 						}
 	 						if(mProjectPOwner.containsKey(strProjectName)) {
 	 							listTaskDataPOwner =(List<Map>)mProjectPOwner.get(strProjectName);
 	 						}
 	 						listTaskDataPOwner.add(mInfo);
 	 						mProjectPOwner.put(strProjectName, listTaskDataPOwner);
 	 						mPersonTaskMapping.put(strMemberName+"~ProjectLead", mProjectPOwner);
 							
 							//MailUtil.sendMessage(context, slMailToList, null, null,sMailSubject, sFinalMailMessage, null);
 						}
 					} 
 					else if (DateDiff == 1 || (dblPercentComplete < 100 && DateDiff<1)) {
 						if (strProjectRole.equals("Reviewer") ) {
 							if(mPersonTaskMapping.containsKey(strMemberName+"~"+strProjectRole)) {
 									mProjectReviwer=(Map)mPersonTaskMapping.get(strMemberName+"~"+strProjectRole);
 	 	 						}
 	 	 						if(mProjectReviwer.containsKey(strProjectName)) {
 	 	 							listTaskDataReviewer =(List<Map>)mProjectReviwer.get(strProjectName);
 	 	 						}
 	 	 						listTaskDataReviewer.add(mInfo);
 	 	 						mProjectReviwer.put(strProjectName, listTaskDataReviewer);
 	 	 						mPersonTaskMapping.put(strMemberName+"~"+strProjectRole, mProjectReviwer);
 	 		 			}
 					}
 				}
 				
 				 
 			} catch (Exception e) {
 				System.out.println("Exception in sending mail!!!!");
 				e.printStackTrace();
 			}
 		}
 		//Code added for B3 Action- end

 	
 	
	
	
	
	//Code added for B3- end
}
