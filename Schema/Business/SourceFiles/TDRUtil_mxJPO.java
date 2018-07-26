
/*
 *  PackageBase
 *
 *
 * (c) Dassault Systemes, 1993 - 2017.  All rights reserved
 *
 *
 *  static const char RCSID[] = $Id: /ENOSourcingCentral/CNext/Modules/ENOSourcingCentral/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1.1.1 Thu Nov 13 08:27:30 2008 GMT  Experimental$
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.mail.PasswordAuthentication;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.StringList;

/**
 * The <code>${CLASSNAME}</code> class contains Package related utilites The
 * methods of this class are used to create packages, list all the packages
 * based on selected filter, list the attachments for the packages,list the
 * PackageRFQ
 */

public class TDRUtil_mxJPO extends emxDomainObject_mxJPO {
	/**
	 * Constructs a <code>${CLASSNAME}</code> Object
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @throws Exception
	 *             if the operation fails
	 */

	Properties _classCurrencyConfig = new Properties();

	public TDRUtil_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		Page page = new Page("TDRCapitalConfiguration");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
	}

	/**
	 * Default method to be executed when no method is specified
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no value
	 * @return int 0 for success and non-zero for failure
	 * @throws Exception
	 *             if the operation fails
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}

	/**
	 * Method returns StringList.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param strSelectable
	 *            a String value
	 * @param mlObjectList
	 *            a MapList
	 * @throws Exception
	 *             if the operation fails
	 */

	public static StringList toStringList(Context context, String strSelectable, MapList mlObjectList)
			throws Exception {
		StringList slReturnList = new StringList();
		int iSize = mlObjectList.size();
		Map tempMap = null;
		String strValue = "";
		for (int i = 0; i < iSize; i++) {
			tempMap = (Map) mlObjectList.get(i);
			strValue = (String) tempMap.get(strSelectable);
			if (UIUtil.isNotNullAndNotEmpty(strValue)) {
				slReturnList.add(strValue);
			}
		}
		return slReturnList;
	}

	/**
	 * Method returns String. This method will return Department Lead person id from
	 * SC department.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param strDepartmetId
	 *            a String value
	 * @throws Exception
	 *             if the operation fails
	 */

	public static String getSCDepartmentLead(Context context, String strDepartmetId) throws Exception {
		String strPersonId = DomainConstants.EMPTY_STRING;
		try {
			if (UIUtil.isNotNullAndNotEmpty(strDepartmetId)) {
				Properties _classCurrencyConfig = new Properties();
				Page page = new Page("TDRCapitalConfiguration");
				_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
				String strAccessKey = "TDRCapital.ConcernedSCDept.BusinessUnit";
				String strBusinessUnitName = _classCurrencyConfig.getProperty(strAccessKey);
				StringList slSelect = new StringList(DomainObject.SELECT_ID);
				slSelect.add(DomainObject.SELECT_NAME);
				slSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList(DomainRelationship.SELECT_ID);
				slRelSelect.add("attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ROLE + "].value");

				String strSCDeptLeadRoles = "TDRCapital.SCDepartmentLead.Roles";
				strSCDeptLeadRoles = _classCurrencyConfig.getProperty(strSCDeptLeadRoles);
				StringList slSCLeadRoleList = FrameworkUtil.splitString(strSCDeptLeadRoles, "|");

				MapList mlBUList = DomainObject.findObjects(context, DomainObject.TYPE_BUSINESS_UNIT,
						strBusinessUnitName, DomainConstants.QUERY_WILDCARD, "*",
						TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION, "", false, slSelect);
				if (mlBUList.isEmpty() == false) {
					Map tempMap = (HashMap) mlBUList.get(0);
					String strBUId = (String) tempMap.get(DomainObject.SELECT_ID);
					DomainObject doBU = new DomainObject(strBUId);
					MapList mlMemberList = doBU.getRelatedObjects(context, DomainRelationship.RELATIONSHIP_MEMBER,
							DomainObject.TYPE_PERSON, slSelect, slRelSelect, false, true, (short) 0, null, null);

					strSCDeptLeadRoles = DomainConstants.EMPTY_STRING;
					for (int i = 0; i < mlMemberList.size(); i++) {
						tempMap = (Map) mlMemberList.get(i);
						strSCDeptLeadRoles = (String) tempMap
								.get("attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ROLE + "].value");
						for (String strRole : slSCLeadRoleList) {
							if (UIUtil.isNotNullAndNotEmpty(strSCDeptLeadRoles)
									&& strSCDeptLeadRoles.contains(strRole)) {
								strPersonId = (String) tempMap.get(DomainObject.SELECT_ID);
								break;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return strPersonId;

	}

	/**
	 * Method returns String. This method will return all members of SC department.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param strDepartmetId
	 *            a String value
	 * @throws Exception
	 *             if the operation fails
	 */

	public static StringList getAllSCDepartmentMembers(Context context, String strDepartmetId) throws Exception {
		StringList slAllDepartmentMembers = new StringList();
		try {
			if (UIUtil.isNotNullAndNotEmpty(strDepartmetId)) {
				StringList slSelect = new StringList(DomainObject.SELECT_ID);
				slSelect.add(DomainObject.SELECT_NAME);
				slSelect.add(DomainObject.SELECT_TYPE);

				StringList slRelSelect = new StringList(DomainRelationship.SELECT_ID);
				DomainObject doDepartment = new DomainObject(strDepartmetId);
				MapList mlMemberList = doDepartment.getRelatedObjects(context, DomainRelationship.RELATIONSHIP_MEMBER,
						DomainObject.TYPE_PERSON, slSelect, slRelSelect, false, true, (short) 0, null, null);
				int iSize = mlMemberList.size();
				Map mapTemp = null;
				for (int iCount = 0; iCount < iSize; iCount++) {
					mapTemp = (Map) mlMemberList.get(iCount);
					slAllDepartmentMembers.add((String) mapTemp.get(DomainConstants.SELECT_NAME));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slAllDepartmentMembers;

	}

	/**
	 * Method returns int value. This method will get Send email notifications to SC
	 * Department lead Return 0 if success else 1.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds object ID
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static int sendNotificationTo(Context context, StringList objectIdList, String strSubject) throws Exception {
		int iReturn = 0;
		try {
			if (objectIdList != null && objectIdList.size() > 0) {
				int iSize = objectIdList.size();
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				slObjectSelect.add(DomainObject.SELECT_NAME);
				slObjectSelect.add(DomainObject.SELECT_OWNER);
				slObjectSelect.add("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.id");
				slObjectSelect.add("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.name");

				HashMap hmToList = new HashMap();

				for (int iCount = 0; iCount < iSize; iCount++) {
					DomainObject doObject = new DomainObject((String) objectIdList.get(iCount));
					StringList slRFQMembers = (StringList) getPeopleFromMemberList(context,
							(String) objectIdList.get(iCount));
					Map mObjectInfo = (Map) doObject.getInfo(context, slObjectSelect);
					String strOwner = (String) mObjectInfo.get(DomainObject.SELECT_OWNER);
					String strSCDeptId = (String) mObjectInfo
							.get("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.id");
					if (UIUtil.isNotNullAndNotEmpty(strSCDeptId)) {
						StringList slDepartmentMembers = (StringList) getAllSCDepartmentMembers(context, strSCDeptId);
						StringList toList = new StringList(strOwner);
						toList.addAll(slDepartmentMembers);
						toList.addAll(slRFQMembers);
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
					}
				}

				Iterator<Map.Entry<String, MapList>> itr = hmToList.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, MapList> entry = itr.next();
					String strKeyName = entry.getKey();
					MapList mlRFQs = entry.getValue();
					String strToEmail = PersonUtil.getEmail(context, strKeyName);

					if (null != mlRFQs && mlRFQs.size() > 0) {
						StringList objectIdLists = new StringList();
						String strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
								context.getLocale(), "TDR.RFQ.PendingRFQ.Message");
						StringBuffer sbMessage = new StringBuffer();
						sbMessage.append(strMessage);
						sbMessage.append("\n");
						// For email
						StringBuffer sbHTMLBody = new StringBuffer();
						sbHTMLBody.append("<html>");
						sbHTMLBody.append(
								"<head><style>.datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden;}.datagrid table td, .datagrid table th { padding: 3px 10px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; color:#FFFFFF; font-size: 13px; font-weight: bold; border-left: 1px solid #0070A8; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #00557F; border-left: 1px solid #E1EEF4;font-size: 12px;font-weight: normal; }.datagrid table tbody .alt td { background: #E1EEf4; color: #00557F; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }</style></head>");
						sbHTMLBody.append("<body>");
						sbHTMLBody.append("<div class='datagrid'>");
						sbHTMLBody.append("<b>Dear User , <b> <br><br>");
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append(strMessage);
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append("<center>");
						sbHTMLBody.append("<table width='100%' border='1'>");
						sbHTMLBody.append("<thead>");
						sbHTMLBody.append(
								"<tr align='center'><th width='5%'><b>S.No</b></th><th width='10%'><b>RFQ Name</b></th><th width='15%'><b>Owner</b></th><th width='15%'><b>Department</b></th></tr>");
						sbHTMLBody.append("</thead>");
						sbHTMLBody.append("<tbody>");

						int iSizeRFQList = mlRFQs.size();
						Map mapRFQ = null;
						for (int iCount = 0; iCount < iSizeRFQList;) {
							mapRFQ = (Map) mlRFQs.get(iCount);
							iCount++;
							String sName = (String) mapRFQ.get(DomainObject.SELECT_NAME);
							String sOwner = (String) mapRFQ.get(DomainObject.SELECT_OWNER);
							String sDepartment = (String) mapRFQ.get(
									"to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.name");
							sbHTMLBody.append("<tr><th width='5%'>" + iCount + "</th><th width='10%'>" + sName
									+ "</th><th width='15%'>" + sOwner + "</th><th width='15%'>" + sDepartment
									+ "</th></tr>");
							// For Icon Mail
							objectIdLists.add((String) mapRFQ.get(DomainObject.SELECT_ID));
							sbMessage.append("\n");
							sbMessage.append(iCount + ". ");
							sbMessage.append("'" + sName + "'");
							sbMessage.append(" sent by ");
							sbMessage.append("'" + sOwner + "'");
							sbMessage.append("  from department ");
							sbMessage.append("'" + sDepartment + "'");
							sbMessage.append("\n");
						}
						sbHTMLBody.append("</tbody>");
						sbHTMLBody.append("</table>");
						sbHTMLBody.append("</body>");
						sbHTMLBody.append("</html>");
						// For sending Icon Mail
						// MailUtil.sendMessage(context, new StringList(strKeyName), null, null,
						// strSubject,sbMessage.toString(), objectIdLists);
						System.out.println("To Email : " + strToEmail);

						String fromAgent = context.getUser();
						String notifyType = "both";
						emxNotificationUtil_mxJPO.sendJavaMail(context, new StringList(strKeyName), null, null,
								strSubject, sbMessage.toString(), sbHTMLBody.toString(), fromAgent, null, objectIdLists,
								notifyType);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return iReturn;
	}

	/**
	 * This method sendMail to send mail to respective persons.
	 * 
	 * @param context
	 * @param strToPerson,strFromPerson,strSubject,strMailBody
	 * @return HashMap
	 * @throws Exception
	 *             if operation fails
	 */
	public static boolean sendMail(Context context, StringList slTOPersonList, String strSubject, String strMailBody)
			throws Exception {
		Properties _classCurrencyConfig = new Properties();
		Page page = new Page("TDRCapitalConfiguration");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
		String strAuth = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.auth");
		String strStarttls = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.starttls");
		String strHost = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.host");
		String strPort = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.port");
		final String username = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.user");
		final String password = (String) _classCurrencyConfig.getProperty("TDR.MailSMTP.password");

		System.out.println("From Email : " + username);

		try {

			Properties props = new Properties();
			props.put("mail.smtp.auth", strAuth);
			props.put("mail.smtp.starttls.enable", strStarttls);
			props.put("mail.smtp.host", strHost);// change the host
			props.put("mail.smtp.port", strPort);// change the port

			Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			InternetAddress[] addressTo = new InternetAddress[slTOPersonList.size()];
			for (int i = 0; i < slTOPersonList.size(); i++) {
				addressTo[i] = new InternetAddress((String) slTOPersonList.get(i));
			}

			Message message = new MimeMessage(session);
			// message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, addressTo);
			message.setSubject(strSubject);
			message.setText(strMailBody);
			message.setHeader("Mime-Version", "1.0");
			message.setSentDate(new java.util.Date());
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(strMailBody);
			mbp1.setHeader("Content-Type", "text/html");
			mbp1.setHeader("Content-Transfer-Encoding", "base64");
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			message.setContent(mp);
			message.saveChanges();
			javax.mail.Transport.send(message);
			System.out.println("Mail has been sent successfully......!");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return true;
	}

	/**
	 * Method returns int value. This method will get the status of Ringi Number
	 * with help of external query. Return 0 if success else 1.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds object ID
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public String checkStatusByRingiNumber(Context context, String[] args)throws Exception{
		String strMessage = DomainConstants.EMPTY_STRING;
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strRingiType = (String) programMap.get("checkType");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				String strRingiNumber = doObject.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_NUMBER);
				if(UIUtil.isNullOrEmpty(strRingiNumber)) {
					strMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.Alert.ValidMSILRingiNumber");
					emxContextUtil_mxJPO.mqlNotice(context, strMessage);
				}else {
					MapList mlRingiDetails = getRingiDetailsByRingiNo(context, strRingiNumber);
					if(mlRingiDetails != null && mlRingiDetails.isEmpty() == false) {
						Map ringiDetailMap = (HashMap)mlRingiDetails.get(0);
						
						String strStatus = (String)ringiDetailMap.get("RNGI_STATUS");
						String strLevel = (String)ringiDetailMap.get("RNGI_LEVEL");
						String strAppDate = (String)ringiDetailMap.get("RNGI_APP_REJ_ON");
						
						strAppDate = strAppDate.substring(0, strAppDate.indexOf("."));
						
						String strMatrixDateFormat = eMatrixDateFormat.getEMatrixDateFormat();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						Date dAppDate = sdf.parse(strAppDate);
						strAppDate = dAppDate.toString();
						
						SimpleDateFormat sdf1 = new SimpleDateFormat(strMatrixDateFormat);
						String strDate = sdf1.format(dAppDate);
						
						if("A".equals(strStatus)) {
							strMessage = "Success";
						}else {
							strMessage = "Failure";
						}
						
						if("A".equalsIgnoreCase(strStatus)) {
							strStatus = "Approved";
						}
						if("R".equalsIgnoreCase(strStatus)) {
							strStatus = "Rejected";
						}
						if("I".equalsIgnoreCase(strStatus)) {
							strStatus = "Initiated";
						}
						if("C".equalsIgnoreCase(strStatus)) {
							strStatus = "Cancelled";
						}
						
						if(UIUtil.isNullOrEmpty(strAppDate)) {
							strAppDate = "";
						}

						HashMap hmAttributeMap = new HashMap();
						
						if(UIUtil.isNotNullAndNotEmpty(strRingiType) && "CommercialRingi".equals(strRingiType)) {
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_LEVEL, strLevel);
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_APPROVED_DATE, strDate);
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_RINGI_STATUS, strStatus);
						}else {
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_LEVEL, strLevel);
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_APPROVED_DATE, strDate);
							hmAttributeMap.put(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_STATUS, strStatus);
						}
						if(hmAttributeMap.isEmpty()==false) {
							doObject.setAttributeValues(context, hmAttributeMap);
						}
																		
					}
				}
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return strMessage;
	}

	/**
	 * Method returns StringList value. This method will return the person list
	 * based on the memberlist connected to RFQ.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param strObjectId
	 * @throws Exception
	 *             if the operation fails
	 */

	public static StringList getPeopleFromMemberList(Context context, String strObjectId) throws Exception {
		StringList slPersonList = new StringList();
		try {
			if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slObjectSelect = new StringList();
				slObjectSelect.add(DomainObject.SELECT_ID);
				MapList mlMemberList = doObject.getRelatedObjects(context,
						TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_MEMBER_LIST, DomainObject.TYPE_MEMBER_LIST,
						slObjectSelect, null, false, true, (short) 1, null, null);

				StringList slMemberList = toStringList(context, DomainObject.SELECT_ID, mlMemberList);
				StringList slPersons = new StringList();
				DomainObject doMember = null;
				for (String strMemberId : slMemberList) {
					doMember = new DomainObject(strMemberId);
					slPersons = (StringList) doMember.getInfoList(context,
							"from[" + DomainRelationship.RELATIONSHIP_LIST_MEMBER + "].to." + DomainObject.SELECT_NAME);
					for (String strPerson : slPersons) {
						if (slPersonList.contains(strPerson) == false) {
							slPersonList.add(strPerson);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return slPersonList;
	}

	/**
	 * Method returns int value. This method will get Send email notifications to
	 * Owner and Co-Owners of RFQ/Quotation Return 0 if success else 1.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds object ID
	 * @throws Exception
	 *             if the operation fails
	 */
	public static int sendDueDateNotification(Context context, Map hmToList, String strSubject, String strMessage)
			throws Exception {
		int iReturn = 0;
		try {
			if (hmToList != null && hmToList.size() > 0) {
				StringList slCCList = new StringList();
				Iterator<Map.Entry<String, MapList>> itr = hmToList.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, MapList> entry = itr.next();
					String strKeyName = entry.getKey();
					MapList mlRFQs = entry.getValue();
					String strToEmail = PersonUtil.getEmail(context, strKeyName);

					if (null != mlRFQs && mlRFQs.size() > 0) {
						StringList objectIdLists = new StringList();
						StringBuffer sbMessage = new StringBuffer();
						sbMessage.append(strMessage);
						sbMessage.append("\n");

						// For email----Start
						StringBuffer sbHTMLBody = new StringBuffer();
						sbHTMLBody.append("<html>");
						sbHTMLBody.append(
								"<head><style>.datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden;}.datagrid table td, .datagrid table th { padding: 3px 10px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; color:#FFFFFF; font-size: 13px; font-weight: bold; border-left: 1px solid #0070A8; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #00557F; border-left: 1px solid #E1EEF4;font-size: 12px;font-weight: normal; }.datagrid table tbody .alt td { background: #E1EEf4; color: #00557F; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }</style></head>");
						sbHTMLBody.append("<body>");
						sbHTMLBody.append("<div class='datagrid'>");
						sbHTMLBody.append("<b>Dear User , <b> <br><br>");
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append(strMessage);
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append("<BR>");
						sbHTMLBody.append("<center>");
						sbHTMLBody.append("<table width='100%' border='1'>");
						sbHTMLBody.append("<thead>");
						sbHTMLBody.append(
								"<tr align='center'><th width='5%'><b>S.No</b></th><th width='10%'><b>Name</b></th><th width='15%'><b>Description</b></th><th width='15%'><b>Due Date</b></th><th width='15%'><b>Suppliers</b></th></tr>");
						sbHTMLBody.append("</thead>");
						sbHTMLBody.append("<tbody>");
						// For email----End

						int iSizeRFQList = mlRFQs.size();
						Map mapRFQ = null;
						String sSuppliers = "";
						for (int iCount = 0; iCount < iSizeRFQList;) {
							mapRFQ = (Map) mlRFQs.get(iCount);
							iCount++;
							String sName = (String) mapRFQ.get(DomainObject.SELECT_NAME);
							String sType = (String) mapRFQ.get(DomainObject.SELECT_TYPE);
							String sOwner = (String) mapRFQ.get(DomainObject.SELECT_OWNER);
							String sDueDate = (String) mapRFQ.get("dueDate");
							sSuppliers = (String) mapRFQ.get("suppliers");
							String sDescription = (String) mapRFQ.get(DomainObject.SELECT_DESCRIPTION);
							slCCList = (StringList) mapRFQ.get("members");
							sbHTMLBody.append("<tr><th width='5%'>" + iCount + "</th><th width='10%'>" + sName
									+ "</th><th width='15%'>" + sDescription + "</th><th width='15%'>" + sDueDate
									+ "</th><th width='15%'>" + sSuppliers + "</th></tr>");
							// For Icon Mail
							objectIdLists.add((String) mapRFQ.get(DomainObject.SELECT_ID));
							sbMessage.append("\n");
							sbMessage.append(iCount + ". ");
							sbMessage.append(sType);
							sbMessage.append(" '" + sName + "'");
							sbMessage.append(" is due by ");
							sbMessage.append("'" + sDueDate + "'");
							sbMessage.append("\n");
						}
						sbHTMLBody.append("</tbody>");
						sbHTMLBody.append("</table>");
						sbHTMLBody.append("</body>");
						sbHTMLBody.append("</html>");
						// For sending Icon Mail
						// MailUtil.sendMessage(context, new StringList(strKeyName), slCCList, null,
						// strSubject,sbMessage.toString(), objectIdLists);
						String fromAgent = context.getUser();
						String notifyType = "both";
						emxNotificationUtil_mxJPO.sendJavaMail(context, new StringList(strKeyName), null, null,
								strSubject, sbMessage.toString(), sbHTMLBody.toString(), fromAgent, null, objectIdLists,
								notifyType);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return iReturn;
	}

	/**
	 * Method returns DB Connection object. This method will get data base
	 * connection for Ringi system.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds object ID
	 * @throws Exception
	 *             if the operation fails
	 */
	public Connection getRingiConnection(Context context) throws Exception {
		Connection conn = null;
		try {
			String strDriverName = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.Driver");
			String strDBUrl = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.Server");
			String strUserName = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.User");
			String strPassword = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.Password");
		
			Class.forName(strDriverName).newInstance();
			System.out.println(" in getRingiConnection CLASS loaded");
			conn = DriverManager.getConnection(strDBUrl, strUserName, strPassword);
			System.out.println("Ringi Connection >>>>" + conn);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("RINGI CONNECTION EXCEPTION ----->>" + ex);
			throw ex;
		}
		return conn;
	}

	public MapList getDataFromConnection(Context context, String query, StringList slSelectables) throws Exception {
		MapList mlReturnList = new MapList();
		try {
			Connection connection = getRingiConnection(context);
			if (null == connection) {
				System.out.println("Could not connect to RINGI DB.. Please contact system Admin.");
				return mlReturnList;
			}
			int iSize = slSelectables.size();

			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			System.out.println(" rs >> " + rs);
			Map map = null;
			while (rs.next()) {
				map = new HashMap();
				for (int iCount = 0; iCount < iSize; iCount++) {
					try {
						String result = rs.getString(rs.findColumn((String) slSelectables.get(iCount)));
						map.put((String) slSelectables.get(iCount), result);
					} catch (SQLException ex) {
						ex.printStackTrace();
						System.out.println("RINGI CONNECTION EXCEPTION -ResultSet---->>" + ex);
						throw ex;
					}
				}
				mlReturnList.add(map);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("RINGI CONNECTION EXCEPTION ----->>" + ex);
			throw ex;
		}
		return mlReturnList;
	}

	public MapList getRingiDetailsByRingiNo(Context context, String strRingiNo) throws Exception {
		MapList mlReturnList = new MapList();
		try {
			String strTable = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.Table");
			String strQuery = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.Query");
			String strDBLink = _classCurrencyConfig.getProperty("MSILRingiIntegration.Database.DBLink");
			if(UIUtil.isNotNullAndNotEmpty(strTable) && UIUtil.isNotNullAndNotEmpty(strQuery) && UIUtil.isNotNullAndNotEmpty(strDBLink)) {
				strQuery = strQuery+" from "+strTable+"@"+strDBLink+" rn where rn.rngi_no='"+strRingiNo+"'";
				StringList slSelectables = new StringList();
				slSelectables.add("RNGI_NO");
				slSelectables.add("RNGI_LEVEL");
				slSelectables.add("RNGI_STATUS");
				slSelectables.add("RNGI_APP_REJ_ON");
				mlReturnList = (MapList) getDataFromConnection(context, strQuery, slSelectables);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mlReturnList;
	}
}
