/*
** PMSSupplierDevelopmentTask.java
** Copyright (c) 1993-2015 Dassault Systemes.All Rights Reserved.
** Inc.  Copyright notice is precautionary only
** and does not evidence any actual or intended publication of such program
**
** static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.7.2.2 Fri Dec  5 10:07:04 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7.2.1 Fri Dec  5 09:57:07 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7 Wed Oct 22 15:53:35 2008 przemek Experimental przemek $
*/

import java.text.DecimalFormat;
 
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
 

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

public class WMSSupplierDevelopmentPlan_mxJPO extends WMSConstants_mxJPO {
	
	public WMSSupplierDevelopmentPlan_mxJPO(Context context,String[] args) {
		
		super(context,args);
	}
	
	public int triggerCheckConnectedSDPState(Context context,String[] args) throws Exception
	{
		int iReturn = 0;
		String sObjectId = args[0];
		
		String sSDPStateSelect = "to["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].from.current";
		StringList slBusSelect = new StringList();
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				slBusSelect.addElement(sSDPStateSelect);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				String sSDPState = (String) mObjInfo.get(sSDPStateSelect);
				if(UIUtil.isNotNullAndNotEmpty(sSDPState) && sSDPState.equals("Draft"))
					iReturn = 1;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iReturn;
	}

	public int triggerCheckFileCheckedIn(Context context,String[] args) throws Exception
	{
		int iReturn = 1;
		String sObjectId = args[0];
		String sFormatFileNameSelect = "format.file.name";
		StringList slBusSelect = new StringList();
		StringList slFileName = new StringList();
		String strFileName="";
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				slBusSelect.addElement(sFormatFileNameSelect);
				DomainConstants.MULTI_VALUE_LIST.add(sFormatFileNameSelect);
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				System.out.println("mObjInfo : triggerCheckFileCheckedIn"+mObjInfo);
				if(mObjInfo.get(sFormatFileNameSelect) instanceof String){
					strFileName = ((String)mObjInfo.get(sFormatFileNameSelect));
			 		if(!strFileName.trim().isEmpty()) {
			 		    slFileName.addElement(strFileName);
					}
		 		}
				else if(mObjInfo.get(sFormatFileNameSelect) instanceof StringList){
				StringList slFileList = (StringList)mObjInfo.get(sFormatFileNameSelect);
				for(int i = 0;i<slFileList.size();i++){
				      strFileName=(String) slFileList.get(i);
					  	if(!strFileName.trim().isEmpty()) {
	 				         slFileName.addElement(strFileName);
					}
					
				}
	 	     }
					
	 			if(slFileName.size()>0)
					return 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  String strShowError =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.Deliverable.ReviewPromoteFaild.NOFileUploaded"); 
		  emxContextUtil_mxJPO.mqlError(context, strShowError);
	 	 return iReturn;
	}
	
	public int triggerCheckValidUntilDate(Context context,String[] args) throws Exception
	{
		int iReturn = 0;
		String sObjectId = args[0];
		String sAttrValidUntilDateSelect = "attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]";
	
		StringList slBusSelect = new StringList();
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				slBusSelect.addElement(sAttrValidUntilDateSelect);
				slBusSelect.addElement("attribute["+ATTRIBUTE_WMS_CALCULATED_DOCUMENT_AMOUNT+"]");
				slBusSelect.addElement("attribute["+ATTRIBUTE_WMS_REVIEWED_AMOUNT+"]");
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				String strValidity = (String) mObjInfo.get(sAttrValidUntilDateSelect);
				String strShowError =  DomainConstants.EMPTY_STRING;
				if(strValidity.isEmpty()) {
					strShowError = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.Deliverable.ReviewPromoteFaild.ValidityDateEmpty"); 
				    emxContextUtil_mxJPO.mqlError(context, strShowError);
				    return 1;
			 	 }
			   String strCalcAmount= (String) mObjInfo.get("attribute["+ATTRIBUTE_WMS_CALCULATED_DOCUMENT_AMOUNT+"]");
			   String strReviewedAmount = (String)	  mObjInfo.get("attribute["+ATTRIBUTE_WMS_REVIEWED_AMOUNT+"]");
			   if(strCalcAmount!=null && !strCalcAmount.equalsIgnoreCase("NA")) {
			    if(Double.parseDouble(strCalcAmount)>Double.parseDouble(strReviewedAmount)) {
			     	String strAlert = 	MessageUtil.getMessage(context, null, "WMS.Deliverable.ReviewPromoteFaild.ReviwedAmountNotEqual",
        					new String[] {String.valueOf(strReviewedAmount),String.valueOf(strCalcAmount)}, null, context.getLocale(),
        					"wmsStringResource");	
				    emxContextUtil_mxJPO.mqlError(context, strAlert);
				    return 1;
			      }
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
		 return 0;
	}
	
	public int triggerUpdateSubmittedBy(Context context,String[] args) throws Exception
	{
		int iReturn = 0;
		String sObjectId = args[0];
		LocalDateTime ldTime=new  LocalDateTime();
		 DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
		
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{  String strUser = context.getUser();
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				Map mAttirtibure = new HashMap();
				mAttirtibure.put(ATTRIBUTE_WMS_SUBMITED_BY, strUser);
				mAttirtibure.put(ATTRIBUTE_SUBMITTED_DATE, dateStringFormat.print(ldTime));
				doObj.setAttributeValues(context, mAttirtibure);
				ContextUtil.popContext(context);
			}
		} catch (Exception e) {
			ContextUtil.popContext(context);
			e.printStackTrace();
		}
		return iReturn;
	}
	
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getSupDevelopmentPlanOfWO(Context context,String args[]) throws Exception
	   {
	       try
	       {   
	           HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	           String strWorkOrder = (String)paramMap.get("objectId");
	           DomainObject domWorkOrder = DomainObject.newInstance(context, strWorkOrder);
	           String strComapnyId = PersonUtil.getUserCompanyId(context);
	           //Get the object id of the context context user's Company and create a domain object
	           DomainObject domCompany = DomainObject.newInstance(context,strComapnyId);
	           String strUserComapny = domCompany.getInfo(context, DomainConstants.SELECT_NAME);
	           String strWhere=DomainConstants.EMPTY_STRING;
	           if(!strUserComapny.equalsIgnoreCase("MSIL")) {
	        	   strWhere="current!=Draft";
	           }
	           SelectList selectList = new SelectList();
	           selectList.add(DomainConstants.SELECT_ID);
	           selectList.add(DomainObject.SELECT_NAME);
	           selectList.add(DomainObject.SELECT_CURRENT);
	           selectList.add("attribute["+ATTRIBUTE_WMS_IS_MANDATORY_DELIVERABLES+"]");
	           selectList.add( "attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]");
	          
	           selectList.add("format.file.name");
	         
	           selectList.addAttribute(PropertyUtil.getSchemaProperty(context, "attribute_ScheduledCompletionDate"));
	           selectList.addAttribute(PropertyUtil.getSchemaProperty(context, "attribute_Status"));
			   ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
	               MapList supDevelopmentPlan   =   domWorkOrder.getRelatedObjects(context,
	        		                                     RELATIONSHIP_WMS_WORK_ORDER_SDP,
	        		                                     TYPE_DELIVERABLE1,
	                                                     selectList,
	                                                     null,
	                                                     false,
	                                                     true,
	                                                     (short)1,
	                                                     strWhere,
	                                                     DomainConstants.EMPTY_STRING,0);
														 ContextUtil.popContext(context);

	           return supDevelopmentPlan;
	       }catch (Exception e) {
	              throw e;
	       }
	   
	   }

	/*
    * This method connect Delivarables to the Tempalate
    *
    * @param context      the eMatrix <code>Context</code> object
    * @param args         holds the following input arguments:
    *    objectId         String containing the context Location objectId
    * @return
    * @throws             Exception if the operation fails
    * @since              SUC V6211x
   */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
  public Map connectDeliverabletoWorkOrder(Context context, String args[]) throws Exception
    {  Map mAttribute = new HashMap();
	   try {	
   	   HashMap map = (HashMap)JPO.unpackArgs(args);
   	   String strWorkOrderId =(String) map.get("objectId"); 
   	   String strIsMandatory =(String) map.get("WMSIsMadatory"); 
   	   String strTargetDate =(String) map.get("ScheduledCompletionDate"); 
   	   String strDescription =(String) map.get("Description"); 
   	   String strValidateDate =(String) map.get("ValidateDate"); 
   	   String strCalculatedAmount = (String) map.get("CalAmount");
   	   String strPercentOfContractValue = (String) map.get("PercentOfContract");
   	   String strName =(String) map.get("DelivName");  
   	   DomainObject domWO= DomainObject.newInstance(context,strWorkOrderId);
   	   DomainObject domDummy= DomainObject.newInstance(context);
   	   String strAutorevision  = domDummy.getUniqueName("11");
    	   DomainRelationship domWOSDP = domDummy.createAndConnect(context, TYPE_DELIVERABLE1,  strName, strAutorevision,
   			 DomainConstants.POLICY_DELIVERABLE, context.getVault().getName(),
                RELATIONSHIP_WMS_WORK_ORDER_SDP,
               domWO,true);
   	  DateTimeFormatter dateStringFormatMMM = DateTimeFormat.forPattern("MMM dd, yyyy");
   	  DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
   	  DateTime dtimeTarget = dateStringFormatMMM.parseDateTime(strTargetDate);
   	  if(strValidateDate!=null && !strValidateDate.isEmpty()) {
   	  DateTime dtimeValidity = dateStringFormatMMM.parseDateTime(strValidateDate);
   	   	mAttribute.put(ATTRIBUTE_WMS_VALID_UNTIL_DATE, dateStringFormat.print(dtimeValidity));
   	  }
   	    mAttribute.put(DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, dateStringFormat.print(dtimeTarget));
   	    mAttribute.put( ATTRIBUTE_WMS_IS_MANDATORY_DELIVERABLES, strIsMandatory);
   	    mAttribute.put( ATTRIBUTE_WMS_PERCENTAGE_CONTRACT_VALUE, strPercentOfContractValue);
   	    mAttribute.put( ATTRIBUTE_WMS_CALCULATED_DOCUMENT_AMOUNT, strCalculatedAmount);
   	    domWOSDP.setAttributeValue(context, DomainConstants.ATTRIBUTE_SEQUENCE_ORDER, "1");;
   	    //mAttribute.put( , "1");
   	
   	   domDummy.setAttributeValues(context, mAttribute);
   	   domDummy.setDescription(context,strDescription);
       mAttribute.clear();
   	   mAttribute.put(DomainConstants.SELECT_ID, domDummy.getId(context));
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	 return mAttribute;
   }

	
	public int triggerPromoteSupplierDevelopmentPlan(Context context,String[] args) throws Exception
	{
		int iReturn = 0;
		String sObjectId = args[0];
		String sDeliverablStateSelect = "to["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].from.from["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].to.current";
		String sSDPIdSelect = "to["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].from.id";
		StringList slBusSelect = new StringList();
		StringList slDeliverableState = new StringList();
		String sCurrent = DomainConstants.EMPTY_STRING;
		String sSDPId = DomainConstants.EMPTY_STRING;
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				slBusSelect.addElement(sDeliverablStateSelect);
				DomainConstants.MULTI_VALUE_LIST.add(sDeliverablStateSelect);
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				if(mObjInfo.get(sDeliverablStateSelect) instanceof String)
					slDeliverableState.addElement((String)mObjInfo.get(sDeliverablStateSelect));
				else if(mObjInfo.get(sDeliverablStateSelect) instanceof StringList)
					slDeliverableState.addAll((StringList)mObjInfo.get(sDeliverablStateSelect));
				int iSize = slDeliverableState.size();
				int iIndex = 0;
				for(;iIndex<iSize;iIndex++)
				{
					sCurrent = (String)slDeliverableState.get(iIndex);
					if(!sCurrent.equals("Complete"))
						break;
				}
				if(iIndex==iSize)
				{
					sSDPId = (String) mObjInfo.get(sSDPIdSelect);
					DomainObject doSDP = DomainObject.newInstance(context,sSDPId);
					doSDP.setState(context, "Complete");
				}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iReturn;
	}
	
	public int triggerIsDeliverableComplete(Context context,String[] args) throws Exception
	{
		int iReturn = 1;
		String sCurrent = DomainConstants.EMPTY_STRING;
		StringList slBusSelect = new StringList();
		StringList slDeliverableState = new StringList();
		String sObjectId = args[0];
		String sRelatedDeliverableStateSelect = "from["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].to.current";
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				slBusSelect.addElement(sRelatedDeliverableStateSelect);
				DomainConstants.MULTI_VALUE_LIST.add(sRelatedDeliverableStateSelect);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				if(mObjInfo.get(sRelatedDeliverableStateSelect) instanceof String)
					slDeliverableState.addElement((String)mObjInfo.get(sRelatedDeliverableStateSelect));
				else if(mObjInfo.get(sRelatedDeliverableStateSelect) instanceof StringList)
					slDeliverableState.addAll((StringList)mObjInfo.get(sRelatedDeliverableStateSelect));
				int iSize = slDeliverableState.size();
				int iIndex = 0;
				for(;iIndex<iSize;iIndex++)
				{
					sCurrent = (String)slDeliverableState.get(iIndex);
					if(!sCurrent.equals("Complete"))
						break;
				}
				if(iIndex==iSize)
					iReturn=0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iReturn;
	}
	
	public int triggerSendMailToSubmittedBy(Context context,String[] args) throws Exception
	{
		String sObjectId = args[0];
		Locale locl = new Locale(context.getSession().getLanguage());
		String sSubmitedByUserId = DomainConstants.EMPTY_STRING;
		String sSubmittedBySelect = "attribute["+ATTRIBUTE_WMS_SUBMITED_BY+"]";
		StringList slBusSelect = new StringList();
		String sMailSubject = EnoviaResourceBundle.getProperty(context,"emxSupplierCentral",locl ,"emxSupplierCentral.DemoteFromReview.Subject");
		String sMailMessage = EnoviaResourceBundle.getProperty(context,"emxSupplierCentral",locl ,"emxSupplierCentral.DemoteFromReview.Message");
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				DomainObject doObj = DomainObject.newInstance(context,sObjectId);
				Map mObjInfo = doObj.getInfo(context, slBusSelect);
				sSubmitedByUserId = (String) mObjInfo.get(sSubmittedBySelect);
				if(UIUtil.isNotNullAndNotEmpty(sSubmitedByUserId))
				{
					HashMap arg = new HashMap();
					arg.put("toList", new StringList(sSubmitedByUserId));
					arg.put("ccList", DomainConstants.EMPTY_STRINGLIST);
					arg.put("bccList", DomainConstants.EMPTY_STRINGLIST);
					arg.put("subject",sMailSubject);
					arg.put("message",sMailMessage);
					arg.put("objectIdList",new StringList(sObjectId));
					JPO.invoke(context, "emxMailUtil",null,"sendMessage",JPO.packArgs(arg),null);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int triggerCheckDeliverableTargetDate(Context context,String[] args) throws Exception
	{
		int iReturn = 1;
		String sObjectId = args[0];
		String sDeliverableTargetDateSelect = "from["+DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE+"].attribute["+DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE+"]";
		StringList slBusSelect = new StringList();
		StringList slDeliverableTargetDate = new StringList();
		String sTargetDate = DomainConstants.EMPTY_STRING;
		Date currentDate = new Date();
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				slBusSelect.addElement(sDeliverableTargetDateSelect);
				DomainConstants.MULTI_VALUE_LIST.add(sDeliverableTargetDateSelect);
				DomainObject doSDP = DomainObject.newInstance(context,sObjectId);
				Map mObjInfo = doSDP.getInfo(context,slBusSelect);
				if(mObjInfo.get(sDeliverableTargetDateSelect) instanceof String)
					slDeliverableTargetDate.addElement((String)mObjInfo.get(sDeliverableTargetDateSelect));
				else if(mObjInfo.get(sDeliverableTargetDateSelect) instanceof StringList)
					slDeliverableTargetDate.addAll((StringList)mObjInfo.get(sDeliverableTargetDateSelect));
				int iSize = slDeliverableTargetDate.size();
				int iIndex =0;
				for(;iIndex<iSize;iIndex++)
				{
					sTargetDate = (String)slDeliverableTargetDate.get(iIndex);
					if(UIUtil.isNullOrEmpty(sTargetDate))
						break;
						else{
							Date dateTarget = eMatrixDateFormat.getJavaDate(sTargetDate);
							if(dateTarget.getDate()-currentDate.getDate()<0)
								break;
						}				
				}
				if(iIndex==iSize)
					iReturn=0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iReturn;
	}
	
	public int triggerCheckDeliverableSequenceOrder(Context context,String[] args) throws Exception
	{
		int iReturn = 1;
		String sObjectId = args[0];
		//String sDeliverableSequenceOrderSelect = "attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
		StringList slBusSelect = new StringList();
		StringList slRelSelect = new StringList();
		String strTypeDeliveravle = PropertyUtil.getSchemaProperty(context, "type_Deliverable");
		Pattern typePattern = new Pattern(strTypeDeliveravle);
		Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE);
		StringList slDeliverableTargetDate = new StringList();
		try {
			if(UIUtil.isNotNullAndNotEmpty(sObjectId))
			{
				slBusSelect.addElement(DomainConstants.SELECT_ID);
				slRelSelect.addElement(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
				DomainObject doSDP = DomainObject.newInstance(context,sObjectId);
				MapList mlRelatedDeliverable = doSDP.getRelatedObjects(context, // context
																			relPattern.getPattern(),// relationship pattern
																			typePattern.getPattern(),     // type pattern
																			slBusSelect,               // object selects
																			slRelSelect,         // relationship selects
																			false,               // to direction
																			true,              // from direction
																			(short) 1,          // recursion level
																			"",        // object where clause
																			null,
																			0);
				mlRelatedDeliverable.addSortKey(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "Integer");
				mlRelatedDeliverable.sort();
				boolean bCheckOrder = checkSequenceOrder(mlRelatedDeliverable,DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
				if(checkSequenceOrder(mlRelatedDeliverable,DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER))
					iReturn=0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iReturn;
	}
	
	public boolean checkSequenceOrder(MapList mlInput,String sKey)
	{
		boolean bSeqErrorFound = false;
		String strSeqNo = null;
		int iCurrentSeqNumber;
		int iPrevSeqNumber=0;
		int iSize = mlInput.size();
		Map mObjInfo = new HashMap();
		for (int i = 0; i < iSize; i++) {
			mObjInfo = (Map) mlInput.get(i);
            if (!bSeqErrorFound)
            {
                strSeqNo = (String)mObjInfo.get(sKey);
                iCurrentSeqNumber = Integer.parseInt(strSeqNo);
                if (iCurrentSeqNumber == iPrevSeqNumber + 1)
                {
                    iPrevSeqNumber = iCurrentSeqNumber;
                }
                else
                	bSeqErrorFound = true;
                 
            }
		}
		return !bSeqErrorFound;
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
	
	
	
	public void notifyValidityExpiredNotification(Context context,String [] args) throws Exception
	{
		 
		 DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
		 DateTimeFormatter dateStringFormatMMM = DateTimeFormat.forPattern("MMM dd-yyyy");
		//String strDiffCompare =  EnoviaResourceBundle.getProperty(context,"wmsCustom", context.getLocale(), "WMS.ValidityDate.Reminder.Days");
		//strDiffCompare="6";
 		 int iDiff = Integer.parseInt("6");
		 LocalDate today=new LocalDate();
 	  try {	
		  
		  StringList objectSelects=new StringList();
		  objectSelects.add(DomainConstants.SELECT_ID);
		  objectSelects.add(DomainConstants.SELECT_NAME);
		  objectSelects.add(DomainConstants.SELECT_OWNER);
		  objectSelects.add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
		  objectSelects.add("to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
		  objectSelects.add("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.name");
		  System.out.println(" inside ..... method 4");
		  //String strWhere="current=='Active'";
		  String strWhere="name=='W/O-12'";
		  Map mOwner = new HashMap();
		  MapList mlActiveWOs = DomainObject.findObjects(context,
        		  TYPE_WMS_WORK_ORDER,                                 // type filter
                  DomainConstants.QUERY_WILDCARD,         // vault filter
                  strWhere,                            // where clause
                  objectSelects);  
	 	  Iterator<Map> itr= mlActiveWOs.iterator();
		  String strWOId = DomainConstants.EMPTY_STRING;
		  String strWOrkOrderOwner = DomainConstants.EMPTY_STRING;
		  String TYPE_DELIVERABLE = PropertyUtil.getSchemaProperty(context,"type_Deliverable1");
		  Pattern pType = new Pattern(DomainConstants.TYPE_SUPPLIER_DEVELOPMENT_PLAN);
	  		pType.addPattern(TYPE_DELIVERABLE);
	  		Pattern pRel = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_SDP);
	  		pRel.addPattern(DomainConstants.RELATIONSHIP_PLAN_DELIVERABLE);
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
							  				 pRel.getPattern(),                         // relationship pattern
							  				 pType.getPattern(),                                    // object pattern
							  				 true,                                                        // to direction
							  				 true,                                                       // from direction
							  				 (short)0,                                                      // recursion level
							  				 objectSelects,                                                 // object selects
							  				 slRelSelect,                                                         // relationship selects
							  				 DomainConstants.EMPTY_STRING,                                // object where clause
							  				 DomainConstants.EMPTY_STRING,                                // relationship where clause
							  				 (short)0,                                                      // No expand limit
							  				 DomainConstants.EMPTY_STRING,                                // postRelPattern
							  				 TYPE_DELIVERABLE,                                                // postTypePattern
							  				 null);
			      Iterator<Map> itrDeliverable=mlDeliverables.iterator();
			      Map mDeliverOwnerData=new HashMap();
			      Map mDeliverPlan=new HashMap();
			      List<Map> slPlan=new ArrayList<Map>();
			      while(itrDeliverable.hasNext()) {
			    	 Map mDeliver= itrDeliverable.next(); 
			    	 strValidUntilDate=(String)mDeliver.get("attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]");
			    	 LocalDate dValidateDate = dateStringFormat.parseLocalDate(strValidUntilDate);
			    	 int iDiffDays= (Days.daysBetween(today, dValidateDate)).getDays();
			    	 	 if(iDiffDays < iDiff) {
		 	     	 		strSubmittedBy = strValidUntilDate=(String)mDeliver.get("attribute["+ATTRIBUTE_WMS_SUBMITED_BY+"]");
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
		 
  		//process each 	   
		if(slWorkOrderOwneList.size() >0 ) {
			for(int i=0;i< slWorkOrderOwneList.size();i++) {
				StringBuilder sb=new StringBuilder();
				//System.out.println("***8mOwner ******"+mOwner);
		     	if(mOwner.containsKey((String)slWorkOrderOwneList.get(i))){
		     		Map  mWorkOrderTemp = (Map) mOwner.get(slWorkOrderOwneList.get(i));
		     		 sb.append("OWNER ").append((String)slWorkOrderOwneList.get(i)).append("   ");
			 		 for(int k=0;k<slWorkOrderIdList.size();k++ ) { 
						  if(mWorkOrderTemp.containsKey((String)slWorkOrderIdList.get(k))) {
							  Map mWOSubData =(Map) mWorkOrderTemp.get((String)slWorkOrderIdList.get(k));
								sb.append((String)mWOSubData.get("WO_NAME")).append("--------").append( (String)mWOSubData.get("WO_PROJECT_NAME")).append("----").append((String)mWOSubData.get("WO_CONTRACTOR"));
							    for(int n=0;n<slSubmittedByList.size();n++) {
							 		   if(mWOSubData.containsKey((String)slSubmittedByList.get(n))) {
									   Map mPlanOwner =(Map) mWOSubData.get((String)slSubmittedByList.get(n));
							 		   List lstPlanDel = (List) mPlanOwner.get("PLAN_DELIVERABLE");
									    for(int j=0;j<lstPlanDel.size();j++) {
									    	Map m  = (Map) lstPlanDel.get(j);
									    	sb.append("\n").append(m.get("name"));
				 					     }
			 					    }
								   
							    }
							  
						  }
						  
						 
					 }
					
					
					
				}
				
				System.out.println("sb "+sb.toString());
				
				
			}
			
			
			
			
		}	   
		//System.out.println( " <<<< mWorkOrdeData>>>> "+mOwner);
		
	    }catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Trigger on Work Order create promote action , promotes all connected deliverables to Active state
	 * 
	 * 
	 * 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	
	
	
	public void triggerPromoteWorkOrderDeliverables(Context context,String[] args) throws Exception{
 	 try {
		 String strWorkOrderId = args[0];
		 DomainObject domDeliverable = DomainObject.newInstance(context);
		 Map paramMap = new HashMap();
		 paramMap.put("objectId", strWorkOrderId);
	 	  MapList mlDeliverabels = (MapList) getSupDevelopmentPlanOfWO(context,JPO.packArgs(paramMap));
 	      Iterator<Map> itrDeliverabels = mlDeliverabels.iterator();
 	      String strDeliverableId = DomainConstants.EMPTY_STRING;
 	      String strCurrent = DomainConstants.EMPTY_STRING;
 	      while(itrDeliverabels.hasNext()) {
 	    	 Map m =  itrDeliverabels.next();
 	    	 strDeliverableId = (String) m.get(DomainConstants.SELECT_ID);
 	    	 strCurrent       =  (String) m.get(DomainConstants.SELECT_CURRENT);
 	    	 domDeliverable.setId(strDeliverableId);
 	    	 if(strCurrent.equalsIgnoreCase("Draft")){
 	    		domDeliverable.promote(context);
 	     	 }
 	    	  
 	      }
 	   }catch(Exception e) {
		 e.printStackTrace();
	 }	
	}
	
	/** Trigger checks Mandatory documents submission on AMB promotion from create >> Active 
	 * 
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	
	public String  triggerAreMandatoryDocumentsUploaded(Context context,String[] args) throws Exception
	{
		String strShowError=DomainConstants.EMPTY_STRING;
 	try{
	      Map inputMap = JPO.unpackArgs(args);
	      String strWorkOrdeId=(String)inputMap.get("WORKORDEROiD");
	      //DomainObject domAMB  = DomainObject.newInstance(context, strAMBOid);
	    //  String strWorkOrdeId =  domAMB.getInfo(context, "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      DomainObject domWorkOrder = DomainObject.newInstance(context, strWorkOrdeId);
	      Map<String,String> paramMap = new HashMap<String,String>();
		  paramMap.put("objectId", strWorkOrdeId);
		  StringList slNotUploaded = new StringList();
		  StringList slNotCompleted = new StringList();
		  StringList slExpired = new StringList();
		  MapList mlDeliverabels = (MapList) getSupDevelopmentPlanOfWO(context,JPO.packArgs(paramMap));
		  Iterator<Map> itrDeliverables =  mlDeliverabels.iterator();
		  String strIsMandatory = DomainConstants.EMPTY_STRING;
		  String strFileName    = DomainConstants.EMPTY_STRING;
		  String strCurrent      =DomainConstants.EMPTY_STRING;
		  String strDocumentName =DomainConstants.EMPTY_STRING;
		  String strValidityDate =DomainConstants.EMPTY_STRING;
		  LocalDate lDToday = new LocalDate();
		  DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
		  while(itrDeliverables.hasNext()) {
			  Map m = itrDeliverables.next();
			  strIsMandatory = (String) m.get("attribute["+ATTRIBUTE_WMS_IS_MANDATORY_DELIVERABLES+"]");
			  strDocumentName=(String)m.get(DomainConstants.SELECT_NAME);
			  strValidityDate=(String) m.get("attribute["+ATTRIBUTE_WMS_VALID_UNTIL_DATE+"]");
			  strCurrent=(String) m.get(DomainConstants.SELECT_CURRENT);
			  if(strIsMandatory.equalsIgnoreCase("YES")) {
				  if( m.containsKey("format.file.name")) {
					  strFileName=(String)m.get("format.file.name") ;
					  if(strFileName.isEmpty()) {
					 	  slNotUploaded.add(strDocumentName); 
					  }
				  }
				 if(!strCurrent.equalsIgnoreCase("Complete")) {
					  slNotCompleted.add(strDocumentName);
				  }else {
					 LocalDate ldvalidityDate =   dateStringFormat.parseLocalDate(strValidityDate);
					 if(ldvalidityDate.isBefore(lDToday)) {
						 slExpired.add(strDocumentName);
				    }
				 }
				  
			  }
			  
		  }
	       
		 if(!slNotUploaded.isEmpty()) {
			   strShowError =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.AbsMBE.Error.MandatoryDocumentsNotUploaded"); 
			   strShowError =strShowError+"   \n" +slNotUploaded.toString();
			// emxContextUtil_mxJPO.mqlError(context, strShowError);
		 }else if(!slNotCompleted.isEmpty()) { // check for Complete state of approved document 
			   strShowError =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.AbsMBE.Error.MandatoryDocumentsNotApprovedYet"); 
			   strShowError =strShowError+"   \n" +slNotCompleted.toString();
		 }else if(!slExpired.isEmpty()) {
			 strShowError =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.AbsMBE.Error.MandatoryDocumentsHasExpired"); 
			   strShowError =strShowError+"   \n" +slExpired.toString();
		 }
	  }catch(Exception e) {e.printStackTrace();}
 
	
	return strShowError;
}
	/** method gets called form wmsAjaxUtil ; on Change handler of Create Deliverables page 
	 * 
	 * 
	 * @param context
	 * @param args
	 * @return
	 */
	
	public String getDeliverablesDetails(Context context,String[] args) {
		StringBuilder sbDetails =new StringBuilder();
		try {
			Map inputMap= JPO.unpackArgs(args);
			String strWorkOrderId = (String)inputMap.get("WorkOrderId");
			DomainObject domWorkOrder= DomainObject.newInstance(context, strWorkOrderId);
			String strContractValue = domWorkOrder.getAttributeValue(context, ATTRIBUTE_WMS_VALUE_OF_CONTRACT);
			String strSelDeliverable = (String)inputMap.get("SelDeliverable");
	    	String strDeliverables =  EnoviaResourceBundle.getProperty(context, "WMS.WO.ContractotDocument.DeliverablesList");
			String[] strFirstCommanSpilt = strDeliverables.split(",");
			StringList slRanges=new StringList(strFirstCommanSpilt.length);
			Map mDetail =new HashMap();
			String[] strSecondPipeSpilt=new String[3];
			Map mInfo=new HashMap();
			String strPercent=DomainConstants.EMPTY_STRING;
			String strMandatory=DomainConstants.EMPTY_STRING;
			for(int i =0;i<strFirstCommanSpilt.length;i++) {
			    mInfo=new HashMap();
				strSecondPipeSpilt =strFirstCommanSpilt[i].split("\\|");
				strMandatory=strSecondPipeSpilt[1];
				mInfo.put("MANDATORY", strSecondPipeSpilt[1]);
				mInfo.put("PERCENT", strSecondPipeSpilt[2]);
				mDetail.put(strSecondPipeSpilt[0], mInfo);
		 	}
			
		     if(mDetail.containsKey(strSelDeliverable))	{
		    	 DecimalFormat  dd=new DecimalFormat("0.00");
    	    	 sbDetails.append("IGNORE|");
		    	 mInfo =  (Map) mDetail.get(strSelDeliverable);
		    	 sbDetails.append((String)mInfo.get("MANDATORY")).append("|");
		    	 strPercent =(String) mInfo.get("PERCENT");
		    	 sbDetails.append(strPercent).append("|");
		    	 if(!strPercent.equalsIgnoreCase("NA")) {
		    		 double dAmount = ((Double.parseDouble(strPercent)/100.0) *Double.parseDouble(strContractValue));
		    		 sbDetails.append(dd.format(dAmount));
		    	 }else {
		    		 sbDetails.append("NA");
		    	 } 
		    	 
		     }else {
		    	 sbDetails.append("IGNORE|No|NA|NA");
		     }
			
		  }catch(Exception e) {
			e.printStackTrace();
		}
		
		return sbDetails.toString();
		
	}
	
	
	public void cloneExpiredDeliverables(Context context,String[] args) throws Exception{
 	 try {	
		   //Map mInput  = JPO.unpackArgs(args);
		 //  String strWorkOrderId = (String)mInput.get("WorkOrderId");
		   //String strDeliverableId = (String)mInput.get("deliverableId");
		   DomainObject domObject = new DomainObject("49888.45246.18560.33657"); 
		   String strNewRevision  = domObject.getUniqueName("12");
		   Map m = new HashMap();
		 /*  DomainObject domCopy = domObject.cloneWithFileMove(context,
				     TYPE_DELIVERABLE1, 
				     domObject.getInfo(context, DomainConstants.SELECT_NAME),
				     strNewRevision, DomainConstants.POLICY_DELIVERABLE, context.getVault().getName(), null, m); */
		   BusinessObject bo   = domObject.cloneObject(context,"XXXX", strNewRevision,  context.getVault().getName(), true);
		   bo.open(context);
		 //  bo.getObjectId(arg0)
 		  //  dom.cloneWI(arg0, arg1, arg2, arg3, arg4)
 		 // bsObject.clone(arg0, arg1, bsObject.getRevision(), arg3)
		   System.out.println("domCopy"+bo.getObjectId(context));
		   bo.close(context);
	   }catch(Exception e) {
		 e.printStackTrace();
	 }
	}
/**
 * 
 * 
 * 	
 * @param context
 * @param args
 * @return
 */
	
public StringList editColumnAccessControl(Context context,String[] args) {
     StringList accessFlags=new StringList();
	try {
		 Map programMap = JPO.unpackArgs(args);
		 Map columnMap =(Map) programMap.get("columnMap");
		 Map settings = (Map) columnMap.get("settings");
		 String strKey= (String) settings.get("EDIT_STATE");
		 StringList slTokens= FrameworkUtil.split(strKey,",");
		 MapList objectList = (MapList) programMap.get("objectList");
		 Iterator<Map> itr= objectList.iterator();
		 while(itr.hasNext()){
			Map m= itr.next();
			 if(slTokens.contains( ((String)m.get(DomainConstants.SELECT_CURRENT)).toUpperCase())){
				 accessFlags.add("true");
			 }else {
				 accessFlags.add("false");
			 }
			
	 	 }
		
 	    }catch(Exception e) {
		
		e.printStackTrace();
	}
	
     return accessFlags;
  }	
	
}