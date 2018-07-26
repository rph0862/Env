
/*
 *  emxRFQ.java
 *
 *
 * (c) Dassault Systemes, 1993 - 2010.  All rights reserved
 *
 * Date         | Modified By   | Remarks
 * 11/18/2014   | DS            | To Send Icon Mail to Buyer when "AL RFQ" promoted to "Response Complete" state
 * 11/22/2014   | DS            | Checks if the field should display while creating new Line item for AL RFQ
 * 11/26/2014   | DS            | Checks if the SupplierLineItemSummary can displayed to the user for AL RFQ 
 * 11/28/2014   | DS            | Checks if the RFQ Quotation Action commands can displayed to the user for AL RFQ to Award  
 * 12/10/2014   | DS            | To add Complete as Part of the Drop down filter of RFQs Page
 * 03/12/2015   | DS            | Line Item Attribute Groups hyper link should be visible for AL RFQ
 * 03/18/2014   | DS            | To show Completeed RFQ to All members of the Heirachy
 * 03/23/2015   | DS            | To Show New Window for seeing Comments given by Supplier on RFQ Quotations Page
 *
 * static const char RCSID[] = $Id: /ENOSourcingCentral/CNext/Modules/ENOSourcingCentral/JPOsrc/custom/${CLASSNAME}.java 1.2.2.1.1.1 Wed Oct 29 22:23:58 2008 GMT  Experimental$
 */
 
 
 
import matrix.db.*;
import matrix.util.StringList;
import java.lang.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;
import java.util.Map.Entry;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxBus;
import com.matrixone.apps.sourcing.RTSQuotation;
import com.matrixone.apps.sourcing.RequestToSupplier;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.sourcing.LineItem;
import com.matrixone.apps.sourcing.util.SourcingConstants;
// Added by DS -- 11/18/2014 --  To Send Icon Mail to Buyer when "AL RFQ" promoted to "Response Complete" state -- Start
import com.matrixone.apps.domain.util.MailUtil;
// Added by DS -- 11/18/2014 --  To Send Icon Mail to Buyer when "AL RFQ" promoted to "Response Complete" state -- End
//Added For 2018xMSIL-Upgrade
import com.matrixone.apps.domain.util.PersonUtil;
//Added For 2018xMSIL-Upgrade
//code added for B3 -start
import com.matrixone.apps.sourcing.LineItem;
import com.matrixone.apps.sourcing.RTSQuotation;
import matrix.util.SelectList;
import com.matrixone.apps.sourcing.util.SourcingConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import matrix.db.Page;
import java.util.Properties;
import java.io.InputStream;
import java.io.StringReader;
//code added for B3 -end

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class SourcingRFQ_mxJPO extends SourcingRFQBase_mxJPO
{
	//code added for B3 -start
	String ATTRIBUTE_WMS_CATEGORY_LIST = PropertyUtil.getSchemaProperty("attribute_WMSCategoryList");
	String ATTRIBUTE_TDR_BUDGET = PropertyUtil.getSchemaProperty("attribute_TDRBudget");
	String ATTRIBUTE_WMS_TURNOVER = PropertyUtil.getSchemaProperty("attribute_WMSTurnover");
	String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BELOW_10MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsbelow10MRs");
	String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BETWEEN_10TO100_MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsbetween10To100MRs");
	String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_ABOVE_100MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsAbove100MRs");
	String ATTRIBUTE_WMS_MAXIMUM_CONTRACT_VALUE = PropertyUtil.getSchemaProperty("attribute_WMSMaximumContractValue");
	String ATTRIBUTE_ANNUAL_PART_QUANTITY = PropertyUtil.getSchemaProperty("attribute_AnnualPartQuantity");
	//code added for B3 -end

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public SourcingRFQ_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }



    /**
     * Checks whether the RTS can be promoted from Started and Initial Review
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 if successful, else returns non zero value
     * @throws Exception if the operation fails
     */
    public int canPromote(matrix.db.Context context, String[] args) throws Exception
    {
        String currentState = getCurrentState(context).getName();
        boolean canPromote = true;
        String alertDueDate = "";
        String timeHorizon = FrameworkProperties.getProperty("emxSourcing.TimeHorizonValue", context.getVault().getName());
        // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- Start
        String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
        String sMSILRFQTypeAttr = "";
        sMSILRFQTypeAttr = getInfo(context, "attribute[" + strMSILRFQTypeAttr + "]");        
        // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- End

        try {
            alertDueDate = i18nNow.getI18nString("emxSourcing.BidCreateRTSDialog.DateTimeHorizonMessage", "emxSourcingStringResource", context.getSession().getLanguage()) + " " + timeHorizon + " " + i18nNow.getI18nString("emxSourcing.common.days", "emxSourcingStringResource", context.getSession().getLanguage());
        }catch(Exception ex) {
        }

        if(currentState.equals(STATE_REQUEST_TO_SUPPLIER_STARTED) || currentState.equals(STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW))
        {
            Date qrbDate = new Date(getInfo(context, "attribute[" + ATTRIBUTE_QUOTE_REQUESTED_BY_DATE + "]"));
            Date today = new Date();
            // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- Start
            if(!"ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr)){
                if( qrbDate.getTime() < (today.getTime() + Long.parseLong(timeHorizon)*24*60*60*1000))
                    canPromote = false;
            }else if(qrbDate.getTime() <= (today.getTime())){
                canPromote = false;
                if(!canPromote)
                {
                    throw new Exception("The Quote Requested By Date can not be less than or equals to today's date");
                }
            }
            // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- End
        }

        if(!canPromote)
        {
            throw new Exception(alertDueDate);
        }

        // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- Start

        if(null != args && args.length >0){            
            String sRFQId = args[0];           

            String sAttRouteBaseState = PropertyUtil.getSchemaProperty("attribute_RouteBaseState");
            String sAttRouteBasePolicy = PropertyUtil.getSchemaProperty("attribute_RouteBasePolicy");
            String sAttRouteBasePurpose = PropertyUtil.getSchemaProperty("attribute_RouteBasePurpose");
            String sAttRouteCompletionAction = PropertyUtil.getSchemaProperty("attribute_RouteCompletionAction");
            StringList objectSelects = new SelectList();
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_CURRENT);
            objectSelects.addElement("attribute["+sAttRouteCompletionAction+"]");
            StringList relSelects = new SelectList();  
            relSelects = new SelectList();
            relSelects.addElement("attribute["+sAttRouteBaseState+"]");
            relSelects.addElement("attribute["+sAttRouteBasePolicy+"]");   
            relSelects.addElement("attribute["+sAttRouteBasePurpose+"]");

            String sCurrentState = "";             
            String sCurrentRouteState ="";
            boolean isRouteNotComplete = false;            
            DomainObject domALRFQ = null;
            MapList mlRoute = new MapList();
            if(!"".equals(sRFQId)&& !"null".equals(sRFQId)&& null!=sRFQId){
                domALRFQ = DomainObject.newInstance(context,sRFQId);
            }      
            if(null!=domALRFQ){                     
                sMSILRFQTypeAttr = (String)domALRFQ.getAttributeValue(context,strMSILRFQTypeAttr);
                sCurrentState    = (String)domALRFQ.getInfo(context,DomainConstants.SELECT_CURRENT);             


                if("ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr)){             
                    if(null != sCurrentState){
                        StringBuffer sbBusWhere = new StringBuffer();
                        sbBusWhere.append("attribute["+sAttRouteCompletionAction+"]~~'Promote Connected Object'");
                        StringBuffer sbRelWhere = new StringBuffer();
                        if(sCurrentState.equals("Initial Review")){
                            sbRelWhere.append("attribute["+sAttRouteBaseState+"]~~'state_InitialPackageReview'");
                            sbRelWhere.append(" && attribute["+sAttRouteBasePolicy+"]~~'policy_RequestToSupplier'");
                            //sbRelWhere.append(" && attribute["+sAttRouteBasePurpose+"]~~'Approval'");              
                        }
                        else if    (sCurrentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)){
                            sbRelWhere.append("attribute["+sAttRouteBaseState+"]~~'state_ResponseComplete'");
                            sbRelWhere.append(" && attribute["+sAttRouteBasePolicy+"]~~'policy_RequestToSupplier'");
                            //sbRelWhere.append(" && attribute["+sAttRouteBasePurpose+"]~~'Approval'");               
                        }         

                        mlRoute = domALRFQ.getRelatedObjects(context,DomainConstants.RELATIONSHIP_OBJECT_ROUTE,DomainConstants.TYPE_ROUTE,objectSelects,relSelects,false,true,(short)1,sbBusWhere.toString(),sbRelWhere.toString());                       
                        if(null != mlRoute && (mlRoute.size()==0)){
                        emxContextUtil_mxJPO.mqlNotice(context,"One Approval Route Should be Created with Route Action 'Promote Connected Object' and Should be Completed before Promoting");
                            return 1;
                        }          

                    }
                }
            }

        }
        // Added by DS -- 11/18/2014 --  To Check Route is connected and Complete or not in "Response Complete" state -- End

        return 0;
    }

    /**
     * Trigger to be executed as a promote action on the Sent state of an RTS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds rtsId
     * @return int 0 if successful else returns 1
     * @throws Exception If the operation fails.
     */
    public int triggerPromoteActionSent(Context context,String[] args)
            throws Exception
    {
        String rtsId = args[0];
        setId(rtsId);       

        boolean closeBO = openObject(context);

        try
        {
            // start a write transaction
            //ContextUtil.startTransaction(context, true);
            String key = getInfo(context, SELECT_PRIMARY_KEY);
            String companyName = Company.getCompanyForKey(context,key).getName();

            // If the promote occured due to the "Quote Requested By Date"
            // being reached then all connected "RTS Quotation" objects that
            // are not in the "Returned" state will be processed based on
            // the "RTSResponseCompletedProcess" property.
            String rtsResponseCompleted = FrameworkProperties.getProperty(
                    "emxSourcing.RTSResponseCompletedProcess", companyName);

            // Get the current date.
            Date today = new Date();

            // Get and parse the "Quote Requested By Date".
            String requestedDate = getAttributeValue(context, ATTRIBUTE_QUOTE_REQUESTED_BY_DATE);

            SimpleDateFormat emxDateFormat = new SimpleDateFormat(
                    FrameworkProperties.getProperty(context,
                            "eServiceSuites.eMatrixDateFormat"),Locale.US);
            Date reqDate = emxDateFormat.parse(requestedDate,new ParsePosition(0));

            // If todays date is after the "Quote Requested By Date",
            // then the date has been reached.

            if (today.compareTo(reqDate) > 0)
            {
                _checkResult=STRING_DATE_REACHED;
            }else{
                _checkResult="";
            }

            if (_checkResult.equals(STRING_DATE_REACHED))
            {
                // Get all conected "RTS Quotaion" objects and
                // check if they have reached the "Returned" state.
                StringList busSelects = new StringList();
                busSelects.add(SELECT_CURRENT);
                busSelects.add(SELECT_ID);

                MapList connectedQuotations = getRelatedObjects(context,
                        RELATIONSHIP_RTS_QUOTATION, "*",
                        false, true, 1, busSelects, null, null, null,
                        null, null, null);

                Iterator rtsQuotationitr = connectedQuotations.iterator();
                while (rtsQuotationitr.hasNext())
                {
                    Map rtsQuotation = (Map) rtsQuotationitr.next();
                    String quotationId =
                            (String) rtsQuotation.get(SELECT_ID);
                    String quotationCurrentState =
                            (String) rtsQuotation.get(SELECT_CURRENT);

                    if(!quotationCurrentState.equals(STATE_RTS_QUOTATION_RETURNED))
                    {
                        // If property is set to "suspend", then change the
                        // policy of the connected "RTS Quotation" object
                        // to "Suspended"
                        if (rtsResponseCompleted.equals("suspend"))
                        {
                            RTSQuotation quote =
                                    (RTSQuotation) newInstance(context, quotationId, SOURCING);
                            quote.suspend(context);
                        }

                        // If property is set to "return", (1) promote the
                        // connected "RTS Quotation" objects to the "Returned"
                        // state, (2) notification will be sent to the owner
                        // and co-owners.
                        if (rtsResponseCompleted.equals("return"))
                        {
                            // Promote "RTS Quotation" to the "Returned" state
                            mxBus.gotoTargetState(context, quotationId,
                                    STATE_RTS_QUOTATION_RETURNED);

                            // Notify the owner of the RTS Quotation.
                            RTSQuotation quote =
                                    (RTSQuotation) newInstance(context, quotationId, SOURCING);
                            quote.open(context);

                            String[] messageKeys = {"rtsName"};
                            String[] messageValues = {getName()};

                            quote.notifyOwner(context,
                                    FrameworkStringResource.RTSQuotation_ReturnedSubject,
                                    null,
                                    null,
                                    FrameworkStringResource.RTSQuotation_ReturnedMessage,
                                    messageKeys,
                                    messageValues,
                                    companyName);
                            quote.close(context);
                        }
                    }
                }
            }

            // Added by DS -- 11/18/2014 --  To Send Icon Mail to Buyer when "AL RFQ" promoted to "Response Complete" state -- Start
            String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");         
            String sMSILRFQTypeAttr = "";
            String sCurrentState = ""; 
            String sOwner = "";
            String sALRFQName = "";            
            DomainObject domALRFQ = null;
            if(!"".equals(rtsId)&& !"null".equals(rtsId)&& null!=rtsId){
                domALRFQ = DomainObject.newInstance(context,rtsId);
            }      
            if(null!=domALRFQ){                     
                sMSILRFQTypeAttr = (String)domALRFQ.getAttributeValue(context,strMSILRFQTypeAttr);
                sCurrentState    = (String)domALRFQ.getInfo(context,DomainConstants.SELECT_CURRENT);
                sOwner   = (String)domALRFQ.getInfo(context,DomainConstants.SELECT_OWNER);
                sALRFQName = (String)domALRFQ.getInfo(context,DomainConstants.SELECT_NAME);
            }     


            if("ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr)){
                StringList mailToList = new StringList();
                if(!"".equals(sOwner)&& !"null".equals(sOwner)&& null!=sOwner){         
                    mailToList.add(sOwner);
                }

                //String sMailSubject = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyCriticalTaskOwner.Subject";
                StringBuffer sbMailBody = new StringBuffer();
                sbMailBody.append("Your RFQ : - ");
                sbMailBody.append(sALRFQName);
                sbMailBody.append(" has reached 'Response Complete' state. Please create an approval route for DDVM to 'Open the Quote'.");
                sbMailBody.append("\n Route should be created with following details : ");
                sbMailBody.append("\n\n");
                sbMailBody.append("Route Base Purpose = Approval");
                sbMailBody.append("\n");
                sbMailBody.append("Route Completion Action = Promote Connected Object");
                sbMailBody.append("\n");
                sbMailBody.append("State Condition = Response Complete");
                sbMailBody.append("\n\n");         
                sbMailBody.append("Please Create a task in that route for DDVM to 'Open Quote'");

                String sMailBody = sbMailBody.toString();
                String sMailSubject = "Please create an approval route for DDVM to 'Open the Quote'.";
                if(null != sCurrentState && sCurrentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)){   MailUtil.sendMessage(context, mailToList, null, null, sMailSubject, sMailBody, null);
                }
            }
            // Added by DS -- 11/18/2014 --  To Send Icon Mail to Buyer when "AL RFQ" promoted to "Response Complete" state -- End
            // commit work
            //ContextUtil.commitTransaction(context);


        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ContextUtil.abortTransaction(context);
            //throw (new Exception(e.getMessage()));
            //Start: Parameterized MQL statements
            //MqlUtil.mqlCommand(context, "notice '" + e.getMessage() + "'");
            String strMsg = "notice '$1'";
            MqlUtil.mqlCommand(context, strMsg, e.getMessage());
            //End: Parameterized MQL statements
            return 1;
        }
        finally
        {
            closeObject(context, closeBO);
        }


        return 0;
    }

    /**
     * Checks if the SupplierLineItemSummary can displayed to the user
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed HashMap containing ObjectId
     * @return boolean true Shows Supplier Line Item Summary else false
     * @throws Exception if the operation fails
     */
   /* public boolean showRFQSupplierLineItemSummary(matrix.db.Context context, String[] args) throws Exception
    {

        boolean showCmd=false;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);

        DomainObject requestToSupplierObj = new DomainObject();
        String rtsId = (String)programMap.get("objectId");
        requestToSupplierObj.setId(rtsId);      

        StringList selectList = new StringList(1);
        selectList.add(RequestToSupplier.SELECT_CURRENT);

        Map m = requestToSupplierObj.getInfo(context,selectList);

        String hasPending = (String)m.get(RequestToSupplier.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
        String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);

        // Added by DS -- 11/26/2014 -- Checks if the SupplierLineItemSummary can displayed to the user for AL RFQ
        String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
        String sMSILRFQTypeAttr = requestToSupplierObj.getAttributeValue(context,strMSILRFQTypeAttr);

        if("ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr) && !(context.isAssigned("DDVM"))){

            if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE))
            {            
                showCmd=true;          
            }                      

        }else{
            if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
                    currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE) ||
                    currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) )
            {

                showCmd=true;
            }
        }
        // Added by DS -- 11/26/2014 -- Checks if the SupplierLineItemSummary can displayed to the user for AL RFQ 
        return showCmd;
    }*/

    /**
     * Checks if the RFQ is in editable mode - UPDATE DOC HERE
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds packed HashMap containing objectId
     * @return boolean indicating editability of RFQ
     * @throws Exception if the operation fails
     * @exclude
     */
    public boolean showRFQQuotationsMenuCommand(matrix.db.Context context, String[] args) throws Exception
    {

        boolean showCmd=false;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
        String rtsId = (String)programMap.get("objectId");
        rts.setId(rtsId);

        boolean isEditable = false;
        boolean isCompareOnly = false;

        StringList selectList = new StringList(6);
        selectList.add(RequestToSupplier.SELECT_CURRENT);
        selectList.add("last.id");
        selectList.add("policy");
        selectList.add("current.access[fromconnect]");
        selectList.add("current.access[fromdisconnect]");
        selectList.add("current.access[modify]");

        Map m = rts.getInfo(context,selectList);

        String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);
        String lastRevId = (String) m.get("last.id");
        String policy = (String) m.get("policy");
        boolean hasFromConnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromconnect]"));
        boolean hasFromDisconnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromdisconnect]"));
        boolean hasModifyAccess = "true".equalsIgnoreCase((String)m.get("current.access[modify]"));

        boolean hasAccess = rts.isOwnerCoOwner(context);

        DebugUtil.debug("showRFQQuotationsMenuCommand::currentState,lastRevId, policy, hasFromConnectAccess, hasFromDisconnectAccess,hasModifyAccess,hasAccess::"
                +currentState+","+lastRevId+","+policy+","+hasFromConnectAccess+","+hasFromDisconnectAccess+","+hasModifyAccess+","+hasAccess);

        // Added by DS -- 11/28/2014 -- Checks if the RFQ Quotation Action commands can displayed to the user for AL RFQ to award Quotation -- Start
        String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
        String sMSILRFQTypeAttr = rts.getAttributeValue(context,strMSILRFQTypeAttr);    

        if("ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr) && !(context.isAssigned("DDVM"))){

            if ( (lastRevId.equals(rtsId)) && ( currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW))
                    && policy.equals(RequestToSupplier.POLICY_REQUEST_TO_SUPPLIER))
            {
                isEditable = true;
            }
        }else{

            if ( (lastRevId.equals(rtsId)) && ( (currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
                    currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)))
                    && policy.equals(RequestToSupplier.POLICY_REQUEST_TO_SUPPLIER) )
            {
                isEditable = true;
            }

        }
        // Added by DS -- 11/28/2014 -- Checks if the RFQ Quotation Action commands can displayed to the user for AL RFQ to award Quotation -- End

        if ( (lastRevId.equals(rtsId)) && ( (currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
                currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_SENT) ))
                && policy.equals(RequestToSupplier.POLICY_REQUEST_TO_SUPPLIER) )

        {
            isCompareOnly = true;
        }

        HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
        String cmdName = (String)settingsMap.get("cmdName");

        // This check has been introduced to enable [Comparison Reports...],[Compare and Award Selected],
        // [Export Quotations],[Import Awards] commands
        if ( (null != cmdName || !"".equals(cmdName)) &&
                "comparisonRepCmd".equals(cmdName) || "compareNAwardCmd".equals(cmdName) || "expQuotationsCmd".equals(cmdName) ||
                "importAwardsCmd".equals(cmdName)
                )
        {
            if (hasAccess && isEditable)
                showCmd=true;
        } else
            // This check has been introduced to enable [Quick Compare] command
            if ( (null != cmdName || !"".equals(cmdName)) && "quickCompareCmd".equals(cmdName) )
            {
                //Start - Fix for IR-165516V6R2013x - Check for sealed RFQ
                boolean checkForSealed = true;

                if((rts.getInfo(context,rts.SELECT_QUOTATION_RESPONSE_REVIEW)).equals("Sealed"))
                {
                    if(!currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)) checkForSealed = false;
                }

                if ( hasAccess && isCompareOnly && checkForSealed) //End - Fix for IR-165516V6R2013x - Check for sealed RFQ
                    showCmd=true;
            }
			Vector vAssignmentList = PersonUtil.getAssignments(context);
if(vAssignmentList.contains(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER) || vAssignmentList.contains(TDRConstants_mxJPO.ROLE_RM_BUYER)) {
				showCmd = true;
			}else {
				showCmd = false;
			}
			
        return showCmd;
    }


    // Added by DS -- 11/22/2014 -- Checks if the field should display while creating new Line item for AL RFQ -- Start 
    /**
     * Checks if the field should display while creating new Line item for AL RFQ
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds packed HashMap containing objectId
     * @return boolean indicating display of newWindow icon in the table
     * @throws Exception if the operation fails
     * @exclude
     */
    public boolean isFieldAccessForALRFQ(Context context, String[] args) throws Exception
    {

        boolean isFieldAccessForALRFQ = true;
        try
        {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap    = (HashMap)programMap.get("paramList");
            String sObjectId  = (String)programMap.get("objectId");
            String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType"); 

            String sMSILRFQTypeAttr = "";
            if(!"".equals(sObjectId)&& !"null".equals(sObjectId)&& null!= sObjectId){
                DomainObject domALObj = DomainObject.newInstance(context,sObjectId);
                if(null!=domALObj){
                    String sType = (String)domALObj.getInfo(context,"type");
                    if("RFQ".equals(sType)){
                        sMSILRFQTypeAttr = (String)domALObj.getInfo(context,"attribute["+strMSILRFQTypeAttr+"]");
                    }else if("Line Item".equals(sType)){
                        sMSILRFQTypeAttr = (String)domALObj.getInfo(context,"to[Line Item].from.attribute["+strMSILRFQTypeAttr+"]");
                    }     
                }
            }
            if("ALRFQ".equals(sMSILRFQTypeAttr)){
                isFieldAccessForALRFQ = false;
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }

        return isFieldAccessForALRFQ;
    }
    // Added by DS -- 11/22/2014 -- Checks if the field should display while creating new Line item for AL RFQ -- End 
    // Added by DS -- 12/10/2014 -- To add Complete as Part of the Drop down filter of RFQs Page -- Start  
    /**
     * Gets all Completed RFQ  to Suppliers list
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return Object of type MapList
     * @throws Exception if the operation fails
     */
    public MapList getCompletedRTSs(Context context, String[] args)
            throws Exception
    {
        try
        {       
            // Modified by DS -- 03/18/2014  -- To show Completeed RFQ to All members of the Heirachy -- Start
           // String sWhere ="current=='Complete' && owner=='"+context.getUser().trim()+"'";
            String sWhere ="current=='Complete'";
            // Modified by DS -- 03/18/2014  -- To show Completeed RFQ to All members of the Heirachy -- Start
            StringList selectList = new StringList();
            selectList.addElement(RequestToSupplier.SELECT_ID);
            MapList mlCompletedRTSList =  DomainObject.findObjects(context,"RFQ","*","*","*","*",sWhere,false,selectList);                     
            return mlCompletedRTSList;
        }
        catch (Exception ex)
        {
            DebugUtil.debug("Error in getCompletedRTSs= " + ex.getMessage());
            throw ex;
        }
    }       
    // Added by DS -- 12/10/2014 -- To add Complete as Part of the Drop down filter of RFQs Page -- End

    // Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- Start

    /**
          * Gets Names for LineItem Attribute Group Page
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args holds a packed HashMap containing ObjectId
          * @return Object of type Vector containing Names (URLs) for LIAG
          * @throws Exception if the operation fails
          */
         public Vector getLIAGName(Context context, String [] args) throws Exception
         {
         
                 Vector columnVals   = null;

                 HashMap programMap  = (HashMap)JPO.unpackArgs(args);
                 // getting the MapList of the objects.
                 MapList objList     = (MapList)programMap.get("objectList");

                 int listSize = 0;
                 Map map = null;
                 if(objList != null && (listSize = objList.size()) > 0 )
                 {
                         HashMap paramMap    = (HashMap)programMap.get("paramList");

                         boolean isPrinterFriendly = false;
                         String PrinterFriendly = (String)paramMap.get("reportFormat");
                         if ( PrinterFriendly != null )
                         {
                                 isPrinterFriendly = true;
                         }
                         boolean isExport = false;
                         String Export = (String)paramMap.get("exportFormat");
                         if ( Export != null )
                         {
                                 isExport = true;
                         }

                         String strObjectId   = (String)paramMap.get("objectId");

                         RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
                         rts.setId(strObjectId);
                         
                         // Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- Start
                         String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
                         String sMSILRFQTypeAttr = "";
                         sMSILRFQTypeAttr = rts.getInfo(context, "attribute[" + strMSILRFQTypeAttr + "]"); 
                         System.out.println("\n sMSILRFQTypeAttr inside SourcingRFQ:getLIAGName() !!!!! "+sMSILRFQTypeAttr);                         
                         // Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- End

                         Map mapSelect = getRTSSelects(context,rts);

                         boolean hasModifyAccess = "true".equalsIgnoreCase((String)mapSelect.get("current.access[modify]"));
                         boolean isEditable  = false;
                         String lastRevId = (String) mapSelect.get("last.id");
                         String currentState = (String) mapSelect.get(SELECT_CURRENT);
                         String hasPending   = (String)mapSelect.get(rts.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
                         String policy       = (String) mapSelect.get(SELECT_POLICY);
                         if ( strObjectId.equals(lastRevId)  &&  ( (rts.STATE_REQUEST_TO_SUPPLIER_STARTED.equals(currentState) ||
                                         rts.STATE_REQUEST_TO_SUPPLIER_SENT.equals(currentState)) &&
                                         hasPending == null) || policy.equals(rts.POLICY_RTS_PENDING_VERSION) )
                         {
                                 isEditable = true;
                         }
                         
                         
                         boolean bolShowLinks = (hasModifyAccess && isEditable);                         
                         columnVals   = new Vector(listSize);
                         StringBuffer strURLBuffer = null;;
                         for(int i = 0; i < listSize ; i++)
                         {
                                 map = (Map)objList.get(i);
                                 strURLBuffer = new StringBuffer();
                                 if(!isExport)
                                 {//Modified for Table to Structure Browser coonversion of Line Item Attribute Groups : START
                                  // Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- Start
                                  if("ALRFQ".equals(sMSILRFQTypeAttr)){
                                  bolShowLinks=true;
                                  }                               
                                  // Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- End
                                         if(bolShowLinks && !isPrinterFriendly)
                                         {
                                                 strURLBuffer.append("<a href=\"javascript:showModalDialog('../sourcing/editDefaultAttributeFilterDialogFS.jsp?relId=").append((String)map.get(SELECT_RELATIONSHIP_ID)).append("',600,600);\"><img src=\"../common/images/iconSmallAttributeGroup.gif\" align=\"absmiddle\" border=\"0\" /></a>");
                                                 //strURLBuffer.append("&nbsp;");
                                                 strURLBuffer.append("<a href=\"javascript:showModalDialog('../sourcing/editDefaultAttributeFilterDialogFS.jsp?relId=").append((String)map.get(SELECT_RELATIONSHIP_ID)).append("',600,600);\" class=\"object\" >").append((String)map.get(SELECT_NAME)).append("</a>");
                                         }
                                         else
                                         {
                                                 strURLBuffer.append("<img src=\"../common/images/iconSmallAttributeGroup.gif\" align=\"absmiddle\" border=\"0\" >");
                                                 strURLBuffer.append("&nbsp;");
                                                 strURLBuffer.append((String)map.get(SELECT_NAME));
                                         }
                                         //Modified for Table to Structure Browser coonversion of Line Item Attribute Groups : END
                                 }
                                 else
                                 {
                                         strURLBuffer.append((String)map.get(SELECT_NAME));
                                 }
                                 columnVals.add(strURLBuffer.toString());
                         }
                 }
                 return columnVals;
         }

// Added by DS -- 03/12/2015 -- Line Item Attribute Groups hyper link should be visible for AL RFQ -- End


/**
     * Checks if the NewWindow icon should be displayed in the Quotation Summary page of the RFQ
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds packed HashMap containing objectId
     * @return boolean indicating display of newWindow icon in the table
     * @throws Exception if the operation fails
     * @exclude
     */
    public boolean showNewWindow(matrix.db.Context context, String[] args) throws Exception
    {
        boolean showNewWindow = true;
        try
        {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        //HashMap paramMap    = (HashMap)programMap.get("paramList");
        String strObjectId  = (String)programMap.get("objectId");

        RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
        rts.setId(strObjectId);

        StringList selectList = new StringList();
        selectList.add(SELECT_CURRENT);
        selectList.add(rts.SELECT_QUOTATION_RESPONSE_REVIEW);

        Map rtsInfo = rts.getInfo(context, selectList);
        String strQuotationResponseReview = (String)rtsInfo.get(rts.SELECT_QUOTATION_RESPONSE_REVIEW);
        String currentState = (String)rtsInfo.get(RequestToSupplier.SELECT_CURRENT);
        // Added by DS -- 03/23/2015 -- To Show New Window for seeing Comments given by Supplier on RFQ Quotations Page -- Start
        String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
        String sMSILRFQTypeAttr = (String)rts.getAttributeValue(context,strMSILRFQTypeAttr);
		System.out.println("\n sMSILRFQTypeAttr >>>showNewWindow() "+sMSILRFQTypeAttr);
        if("ALRFQ".equalsIgnoreCase(sMSILRFQTypeAttr)){
            showNewWindow = true;       
        }else{
            if(strQuotationResponseReview.equals("Sealed"))
        {
            if(!currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)) showNewWindow = false;
        }
        }
        // Added by DS -- 03/23/2015 -- To Show New Window for seeing Comments given by Supplier on RFQ Quotations Page -- End
        }catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }

        return showNewWindow;
    }
    //End - Fix for IR-165516V6R2013x - Check for sealed RFQ
	    public boolean showRFQQuotationsMenuCommandForTechnicalBuyer(matrix.db.Context context, String[] args) throws Exception
    {
        boolean showCmd=false;

        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
        String rtsId = (String)programMap.get("objectId");
        rts.setId(rtsId);

        boolean isEditable = false;
        boolean isCompareOnly = false;

        StringList selectList = new StringList(6);
        selectList.add(RequestToSupplier.SELECT_CURRENT);
        selectList.add("last.id");
        selectList.add("policy");
        selectList.add("current.access[fromconnect]");
        selectList.add("current.access[fromdisconnect]");
        selectList.add("current.access[modify]");

        Map m = rts.getInfo(context,selectList);

        String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);
        String lastRevId = (String) m.get("last.id");
        String policy = (String) m.get("policy");
        boolean hasFromConnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromconnect]"));
        boolean hasFromDisconnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromdisconnect]"));
        boolean hasModifyAccess = "true".equalsIgnoreCase((String)m.get("current.access[modify]"));

        boolean hasAccess = rts.isOwnerCoOwner(context);

        DebugUtil.debug("showRFQQuotationsMenuCommand::currentState,lastRevId, policy, hasFromConnectAccess, hasFromDisconnectAccess,hasModifyAccess,hasAccess::"
                        +currentState+","+lastRevId+","+policy+","+hasFromConnectAccess+","+hasFromDisconnectAccess+","+hasModifyAccess+","+hasAccess);

        if ( (lastRevId.equals(rtsId)) && ( (currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
              currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)))
              && policy.equals(RequestToSupplier.POLICY_REQUEST_TO_SUPPLIER) )
        {
            isEditable = true;
        }

        if ( (lastRevId.equals(rtsId)) && ( (currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
              currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_SENT) ))
              && policy.equals(RequestToSupplier.POLICY_REQUEST_TO_SUPPLIER) )

        {
             isCompareOnly = true;
        }

        HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
        String cmdName = (String)settingsMap.get("cmdName");

        // This check has been introduced to enable [Comparison Reports...],[Compare and Award Selected],
        // [Export Quotations],[Import Awards] commands
        if ( (null != cmdName || !"".equals(cmdName)) &&
              "comparisonRepCmd".equals(cmdName) || "compareNAwardCmd".equals(cmdName) || "expQuotationsCmd".equals(cmdName) ||
              "importAwardsCmd".equals(cmdName)
             )
        {
             if (hasAccess && isEditable)
                  showCmd=true;
        } else
        // This check has been introduced to enable [Quick Compare] command
        if ( (null != cmdName || !"".equals(cmdName)) && "quickCompareCmd".equals(cmdName) )
        {
            //Start - Fix for IR-165516V6R2013x - Check for sealed RFQ
            boolean checkForSealed = true;

            if((rts.getInfo(context,rts.SELECT_QUOTATION_RESPONSE_REVIEW)).equals("Sealed"))
            {
                if(!currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)) checkForSealed = false;
            }

            if ( hasAccess && isCompareOnly && checkForSealed) //End - Fix for IR-165516V6R2013x - Check for sealed RFQ
                  showCmd=true;
        }
        Vector vAssignmentList = PersonUtil.getAssignments(context);
        if(vAssignmentList.contains(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER)) {
        	showCmd = true;
        }else {
        	showCmd = false;
        }        
        
        return showCmd;
    }
	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
		public MapList updateDynamicAttributes(Context context ,String[] args) throws Exception
	{
		MapList fieldMapList = new MapList();
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("requestMap");
			String strObjId = (String) paramMap.get("objectId");
			RequestToSupplier rfqObj=new RequestToSupplier(strObjId);
			StringList strListFilteredAttr = rfqObj.getFilteredAttributeNames(context);
			for (int i=0;i<strListFilteredAttr.size();i++) {
				HashMap settingsMap = new HashMap();
				HashMap fieldMap =new HashMap();
				String attributeName = (String)strListFilteredAttr.get(i);
				String attSymbolicName = FrameworkUtil.getAliasForAdmin(context, "attribute",attributeName, true);
				settingsMap.put("Registered Suite", "Sourcing");
				if(TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_NUMBER.equals(attributeName) || TDRConstants_mxJPO.ATTRIBUTE_TDR_PO_NUMBER.equals(attributeName) || TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_FORMULA.equals(attributeName) || TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_FORMULA.equals(attributeName)){
					settingsMap.put("Access Expression", "false");
				}
				fieldMap.put("name",attributeName);
				fieldMap.put("label",attributeName);
				fieldMap.put("expression_businessobject", "attribute["+attributeName+"]");
				fieldMap.put("settings", settingsMap);
				fieldMapList.add(fieldMap);
			}
			 StringList sysMandatoryAttrNames = RequestToSupplier.getSystemAttributeNames(context,false);
			 StringList sysDefaultAttrNams = new StringList();
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_PRODUCTION_PURPOSE);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_COMMENTS);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_ORIGINATOR);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_QUOTE_REQUESTED_BY_DATE);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_QUOTATION_RESPONSE_REVIEW);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_CURRENCY);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_CO_OWNER);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_SOURCING_PRODUCT);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_SOURCING_SPARES);
			  sysDefaultAttrNams.addElement(RequestToSupplier.ATTRIBUTE_ALLOW_USER_EDIT);
			  sysMandatoryAttrNames.removeAll(sysDefaultAttrNams);
			  
			  for (int i=0;i<sysMandatoryAttrNames.size();i++) {
				  	HashMap settingsMap = new HashMap();
					HashMap fieldMap =new HashMap();
					String attributeName = (String)sysMandatoryAttrNames.get(i);
					String attSymbolicName = FrameworkUtil.getAliasForAdmin(context, "attribute",attributeName, true);
					settingsMap.put("Registered Suite", "Sourcing");
					fieldMap.put("name",attributeName);
					fieldMap.put("label",attributeName);
					fieldMap.put("expression_businessobject", "attribute["+attributeName+"]");
					fieldMap.put("settings", settingsMap);
					fieldMapList.add(fieldMap);
				}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return fieldMapList;
	}
	/**
	* Range Values for Quotattion requested by time field in RFQ Creation Page
	* @param context
	* @param args
	* @return
	* @throws FrameworkException
	*/
	public static Object getQuotationRequestedByTimeRangeValues(Context context, String[] args) throws Exception
	{
	  HashMap tempMap = new HashMap();
	  StringList fieldRangeValues = new StringList();
	  StringList fieldDisplayRangeValues = new StringList();
	  //     initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues

	  fieldRangeValues.addElement("10:00:00 PM");
	  fieldRangeValues.addElement("10:30:00 PM");
	  fieldRangeValues.addElement("11:00:00 PM");
	  fieldRangeValues.addElement("11:30:00 PM");
	  fieldRangeValues.addElement("12:00:00 AM");
	  fieldRangeValues.addElement("12:30:00 AM");
	  fieldRangeValues.addElement("1:00:00 AM");
	  fieldRangeValues.addElement("1:30:00 AM");
	  fieldRangeValues.addElement("2:00:00 AM");
	  fieldRangeValues.addElement("2:30:00 AM");
	  fieldRangeValues.addElement("3:00:00 AM");
	  fieldRangeValues.addElement("3:30:00 AM");
	  fieldRangeValues.addElement("4:00:00 AM");
	  fieldRangeValues.addElement("4:30:00 AM");
	  fieldRangeValues.addElement("5:00:00 AM");
	  fieldRangeValues.addElement("5:30:00 AM");
	  fieldRangeValues.addElement("6:00:00 AM");
	  fieldRangeValues.addElement("6:30:00 AM");   
	  fieldRangeValues.addElement("7:00:00 AM");
	  fieldRangeValues.addElement("7:30:00 AM");
	  fieldRangeValues.addElement("8:00:00 AM");
	  fieldRangeValues.addElement("8:30:00 AM");
	  fieldRangeValues.addElement("9:00:00 AM");
	  fieldRangeValues.addElement("9:30:00 AM");
	  fieldRangeValues.addElement("10:00:00 AM");
	  fieldRangeValues.addElement("10:30:00 AM");
	  fieldRangeValues.addElement("11:00:00 AM");
	  fieldRangeValues.addElement("11:30:00 AM");
	  fieldRangeValues.addElement("12:00:00 PM");
	  fieldRangeValues.addElement("12:30:00 PM");
	  fieldRangeValues.addElement("1:00:00 PM");
	  fieldRangeValues.addElement("1:30:00 PM");
	  fieldRangeValues.addElement("2:00:00 PM");
	  fieldRangeValues.addElement("2:30:00 PM");
	  fieldRangeValues.addElement("3:00:00 PM");
	  fieldRangeValues.addElement("3:30:00 PM");
	  fieldRangeValues.addElement("4:00:00 PM");
	  fieldRangeValues.addElement("4:30:00 PM");
	  fieldRangeValues.addElement("5:00:00 PM");
	  fieldRangeValues.addElement("5:30:00 PM");
	  fieldRangeValues.addElement("6:00:00 PM");
	  fieldRangeValues.addElement("6:30:00 PM");
	  fieldRangeValues.addElement("7:00:00 PM");
	  fieldRangeValues.addElement("7:30:00 PM");
	  fieldRangeValues.addElement("8:00:00 PM");
	  fieldRangeValues.addElement("8:30:00 PM");
	  fieldRangeValues.addElement("9:00:00 PM");
	  fieldRangeValues.addElement("9:30:00 PM");

	  fieldDisplayRangeValues.addElement("10:00 PM");
	  fieldDisplayRangeValues.addElement("10:30 PM");
	  fieldDisplayRangeValues.addElement("11:00 PM");
	  fieldDisplayRangeValues.addElement("11:30 PM");
	  fieldDisplayRangeValues.addElement("12:00 AM");
	  fieldDisplayRangeValues.addElement("12:30 AM");
	  fieldDisplayRangeValues.addElement("1:00 AM");
	  fieldDisplayRangeValues.addElement("1:30 AM");
	  fieldDisplayRangeValues.addElement("2:00 AM");
	  fieldDisplayRangeValues.addElement("2:30 AM");
	  fieldDisplayRangeValues.addElement("3:00 AM");
	  fieldDisplayRangeValues.addElement("3:30 AM");
	  fieldDisplayRangeValues.addElement("4:00 AM");
	  fieldDisplayRangeValues.addElement("4:30 AM");
	  fieldDisplayRangeValues.addElement("5:00 AM");
	  fieldDisplayRangeValues.addElement("5:30 AM");
	  fieldDisplayRangeValues.addElement("6:00 AM");
	  fieldDisplayRangeValues.addElement("6:30 AM");
	  fieldDisplayRangeValues.addElement("7:00 AM");
	  fieldDisplayRangeValues.addElement("7:30 AM");
	  fieldDisplayRangeValues.addElement("8:00 AM");
	  fieldDisplayRangeValues.addElement("8:30 AM");
	  fieldDisplayRangeValues.addElement("9:00 AM");
	  fieldDisplayRangeValues.addElement("9:30 AM");
	  fieldDisplayRangeValues.addElement("10:00 AM");
	  fieldDisplayRangeValues.addElement("10:30 AM");
	  fieldDisplayRangeValues.addElement("11:00 AM");
	  fieldDisplayRangeValues.addElement("11:30 AM");
	  fieldDisplayRangeValues.addElement("12:00 PM");
	  fieldDisplayRangeValues.addElement("12:30 PM");
	  fieldDisplayRangeValues.addElement("1:00 PM");
	  fieldDisplayRangeValues.addElement("1:30 PM");
	  fieldDisplayRangeValues.addElement("2:00 PM");
	  fieldDisplayRangeValues.addElement("2:30 PM");
	  fieldDisplayRangeValues.addElement("3:00 PM");
	  fieldDisplayRangeValues.addElement("3:30 PM");
	  fieldDisplayRangeValues.addElement("4:00 PM");
	  fieldDisplayRangeValues.addElement("4:30 PM");
	  fieldDisplayRangeValues.addElement("5:00 PM");
	  fieldDisplayRangeValues.addElement("5:30 PM");
	  fieldDisplayRangeValues.addElement("6:00 PM");
	  fieldDisplayRangeValues.addElement("6:30 PM");
	  fieldDisplayRangeValues.addElement("7:00 PM");
	  fieldDisplayRangeValues.addElement("7:30 PM");
	  fieldDisplayRangeValues.addElement("8:00 PM");
	  fieldDisplayRangeValues.addElement("8:30 PM");
	  fieldDisplayRangeValues.addElement("9:00 PM");
	  fieldDisplayRangeValues.addElement("9:30 PM");
	  tempMap.put("field_choices", fieldRangeValues);
	  tempMap.put("field_display_choices", fieldDisplayRangeValues);
	  return tempMap;
	}
    /**
     * Gets all Request to Suppliers list
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return Object of type MapList
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRTSs(Context context, String[] args)
    throws Exception
    {
        try
        {
            //HashMap programMap = (HashMap) JPO.unpackArgs(args);
            //HashMap paramMap = (HashMap)programMap.get("paramMap");
			System.out.println("get all RTS...");
            StringList selectList = new StringList();
            selectList.addElement(RequestToSupplier.SELECT_ID);

            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            MapList rtsList = RequestToSupplier.getOwnedRTSs(context, person, selectList, true, null, null, "All");
            StringBuffer sbWhere = new StringBuffer();
            sbWhere.append("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_TECHNICAL_BUYER+"].to.name == '"+context.getUser()+"'");
            sbWhere.append(" && owner != '"+context.getUser()+"'");
            MapList mlTechnicalOwnershipRFQs = (MapList)RequestToSupplier.findObjects(context, DomainObject.TYPE_RFQ, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION,sbWhere.toString(),selectList);
			Map mapTemp = null;
            for(Object object : mlTechnicalOwnershipRFQs){
            	mapTemp = (Map)object;
            	if(!rtsList.contains(mapTemp)){            		
            		rtsList.add(mapTemp);
            	}
			}
            return rtsList;
        }
        catch (Exception ex)
        {
            DebugUtil.debug("Error in getAllRTSs= " + ex.getMessage());
            throw ex;
        }
    }
    
    
    /**
     * Checks if the SupplierLineItemSummary can displayed to the user
     *
     * @param context the eMatrix <code>Context</code> object
      * @param args holds a packed HashMap containing ObjectId
     * @return boolean true Shows Supplier Line Item Summary else false
     * @throws Exception if the operation fails
     */
    public boolean showRFQSupplierLineItemSummary(matrix.db.Context context, String[] args) throws Exception
    {
        boolean showCmd=false;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);

        DomainObject requestToSupplierObj = new DomainObject();
        String rtsId = (String)programMap.get("objectId");
        requestToSupplierObj.setId(rtsId);


        StringList selectList = new StringList(1);
        selectList.add(RequestToSupplier.SELECT_CURRENT);

        Map m = requestToSupplierObj.getInfo(context,selectList);

        String hasPending = (String)m.get(RequestToSupplier.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
        String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);

        if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) ||
                currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE) ||
                currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) )
        {
            showCmd=true;
        }
        
        //Added for MSIL Capital-Start
        String strOpenQuoteStatus = (String)requestToSupplierObj.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
		if(UIUtil.isNotNullAndNotEmpty(strOpenQuoteStatus) && "Complete".equals(strOpenQuoteStatus)) {
			showCmd = true;
		}else {
			showCmd = false;
		}
        

        return showCmd;
    }    
    
    //Modified for MSIL
    
    /**
     * Get RTS Quotations. - UPDATE DOC HERE
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object of type MapList
     * @throws Exception if the operation fails
     * @since 10-7
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getRTSQuotations(Context context, String[] args)
     throws Exception
     {
         HashMap programMap = (HashMap)JPO.unpackArgs(args);
         String strObjectId   = (String)programMap.get("objectId");

         RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
         RTSQuotation rtsquotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
         rts.setId(strObjectId);

        StringList busSelects = rts.getBusSelectList(6);
        busSelects.add(rts.SELECT_QUOTATION_RESPONSE_REVIEW);
        busSelects.add(rts.SELECT_QUOTE_REQUESTED_BY_DATE);
        busSelects.add(rts.SELECT_NAME);
        busSelects.add(rts.SELECT_CURRENT);
        busSelects.add(rts.SELECT_OWNER);
        busSelects.add(rts.SELECT_CO_OWNER);
        Map rtsInfo = rts.getInfo(context, busSelects);

        /* StringList busSelects = new StringList();
         busSelects.add(SELECT_ID);
         busSelects.add(SELECT_CURRENT);
         busSelects.add(RTSQuotation.SELECT_FROM_COMPANY_NAME);
         busSelects.add(RTSQuotation.SELECT_AWARD_STATUS); */

        // define selects for the RTS Quotations list

        busSelects = rtsquotation.getBusSelectList(8);
        busSelects.add(rtsquotation.SELECT_NAME);
        busSelects.add(rtsquotation.SELECT_ID);
        busSelects.add(rtsquotation.SELECT_FROM_COMPANY_NAME);
        busSelects.add(rtsquotation.SELECT_FROM_COMPANY_ID);
        busSelects.add(rtsquotation.SELECT_AWARD_STATUS);
        busSelects.add(rtsquotation.SELECT_ACTUAL_COMPLETION);
        busSelects.add(rtsquotation.SELECT_CURRENT);
        busSelects.add(rtsquotation.SELECT_POLICY);
        busSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");
        
        String sWhere = "current != Open && current != Review && current != Inactive";
        MapList mapList = rts.getRTSQuotations(context, busSelects, sWhere, false);

        if(mapList != null)
        {
             mapList.sort(RTSQuotation.SELECT_FROM_COMPANY_NAME,"ascending",null);
        }

        MapList rtsQuotationList = new MapList();

        boolean bStateReached = false;
        boolean bDateReached = true;
        String strReturned = rtsquotation.STATE_RTS_QUOTATION_RETURNED;
        String strClosed = rtsquotation.STATE_RTS_QUOTATION_CLOSED;

        // Get the value of attibute "Quote Requested By Date" and check if the
        // date has been reached.
        Date today = new Date();

        String requestedDate = rtsInfo.get(rts.SELECT_QUOTE_REQUESTED_BY_DATE).toString();

        // Parse the date/time based on the format.
        //Start Change for fix:IR-050203V6R2011



        Date reqDate = null;
        Locale locale = com.matrixone.apps.domain.util.i18nNow.getLocale(context.getSession().getLanguage()); //request.getLocale();
        if(locale == null)
            reqDate =    eMatrixDateFormat.getJavaDate(requestedDate);
        else
            reqDate =    eMatrixDateFormat.getJavaDate(requestedDate, context.getLocale()); //request.getLocale());
        //End Change for fix:IR-050203V6R2011
        // Compare the date values of the strings.

        int diff = today.compareTo(reqDate);
        if (diff < 0) {
          bDateReached = false;
        }

        if (rtsInfo.get(rts.SELECT_QUOTATION_RESPONSE_REVIEW).equals("Sealed")){
          for(int i=0;i < mapList.size();i++){
            Map rtsMap = (Map)mapList.get(i);
            bStateReached = true;
            // Get the state of all conected "RTS Quotation" objects and check if they have
            // reached the "Returned" state
            String quotationCurrentState = rtsMap.get(rtsquotation.SELECT_CURRENT).toString();
            if((!(quotationCurrentState.equals(strReturned))) && (!(quotationCurrentState.equals(rtsquotation.STATE_SUSPENDED_SUSPENDED))) && (!(quotationCurrentState.equals(strClosed)))){
              bStateReached = false;
              break;
            }
          }
        }

        // Iterate through available Quotations
        for(int q=0; q < mapList.size(); q++)
        {
            Map rtsMap = (Map)mapList.get(q);

            boolean canSeeDetails = true;
            if(bStateReached)
            {
                canSeeDetails = true;
            }
            else if((rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_RTS_QUOTATION_OPEN) ||
                rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_RTS_QUOTATION_REVIEW))||
               (rtsInfo.get(rts.SELECT_QUOTATION_RESPONSE_REVIEW).equals("Sealed") && !bDateReached)
              )
            {
               canSeeDetails = false;
            }

            if (!((String)rtsMap.get(rtsquotation.SELECT_CURRENT)).equals("#DENIED!") && !rtsMap.get(rtsquotation.SELECT_FROM_COMPANY_NAME).equals("#DENIED!"))
            {
                rtsQuotationList.add(rtsMap);
                /*count++;
                String rtsStatus = (String)rtsMap.get(rtsquotation.SELECT_AWARD_STATUS);
                  String rtsPolicy = (String)rtsMap.get(rtsquotation.SELECT_POLICY);

                if (((rtsInfo.get(rts.SELECT_CURRENT)).equals(rts.STATE_REQUEST_TO_SUPPLIER_SENT)||
                     (rtsInfo.get(rts.SELECT_CURRENT)).equals(rts.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) ||
                     (rtsInfo.get(rts.SELECT_CURRENT)).equals(rts.STATE_RESPONSE_COMPLETE)) && canSeeDetails &&
                     !rtsMap.get(rtsquotation.SELECT_CURRENT).equals(rtsquotation.STATE_SUSPENDED_SUSPENDED) &&
                     !rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_RTS_QUOTATION_INACTIVE))
                {
                  }else if(rtsMap.get(rtsquotation.SELECT_CURRENT).equals(rtsquotation.STATE_SUSPENDED_SUSPENDED) ||
                           rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_RTS_QUOTATION_INACTIVE))
                {
                }
                else
                {
                }

                if (canSeeDetails || rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_SUSPENDED_SUSPENDED))
                {
                  }
                  else
                  {
                }

                if (rtsMap.get(rtsquotation.SELECT_AWARD_STATUS) == null ||
                   ((String)rtsMap.get(rtsquotation.SELECT_AWARD_STATUS)).equals("") )
                {
                }
                else
                {
                    String strAwardStatusDisplay = getRangeI18NString(rts.ATTRIBUTE_AWARD_STATUS, (String)rtsMap.get(rtsquotation.SELECT_AWARD_STATUS),strLanguage);
                    String strAwardStatus = (String)rtsMap.get(rtsquotation.SELECT_AWARD_STATUS);

                    if("Awarded".equals(strAwardStatus) )
                    {
                        String popUpURL = "quotationFinalAwardLineItemSummaryFS.jsp?objectId=" + rtsMap.get(rtsquotation.SELECT_ID);
                    }else if("Awarded - Supplier Hidden".equals(strAwardStatus))
                    {
                         //Modified for the Bug No:321149 2/17/2007 begin
                           String popUpURL = "quotationFinalAwardLineItemSummaryFS.jsp?objectId=" + rtsMap.get(rtsquotation.SELECT_ID);
                    }
                    else
                    {
                    }
                }

                if ((rtsMap.get(rtsquotation.SELECT_CURRENT)).equals(strReturned) ||
                    (rtsMap.get(rtsquotation.SELECT_CURRENT)).equals(strClosed))
                {
                }
                else
                {
                }

                String newURL = UINavigatorUtil.getCommonDirectory(application) + "/emxTree.jsp?objectId=" + rtsMap.get(rtsquotation.SELECT_ID);

                if (canSeeDetails || rtsMap.get(rtsquotation.SELECT_CURRENT).toString().equals(rtsquotation.STATE_SUSPENDED_SUSPENDED))
                {
                }
                else
                {
                }*/
            } //for the if condition for checking if access is denied

        } // end-for loop

        if(rtsQuotationList != null)
        {
             rtsQuotationList.sort(RTSQuotation.SELECT_FROM_COMPANY_NAME,"ascending",null);
        }

        Map tempMap = null;
        for(int i=0;i<rtsQuotationList.size();i++) {
        	tempMap = (Map)rtsQuotationList.get(i);
        	String strTechResult = (String)tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"].value");
        	if(UIUtil.isNotNullAndNotEmpty(strTechResult)&& "OK".equalsIgnoreCase(strTechResult) == false) {
        		tempMap.put("disableSelection", "true");
        	}
        }
        
        return rtsQuotationList;
     }
    
    /**
     * Returns all the SupplierLineItems from all the Quotations attached to this RFQ
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed HashMap containing ObjectId
     * @return MapList
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllSupplierLineItems(Context context,String []args)
    throws Exception
    {
        MapList quoteCompareMap2 = new MapList();
        try
        {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            float BaseQuantity=0f;
            float splitPer=0f;
            String splitPercentage=null;
            float SplitQuantity=0f;
            String rtsId   = (String)programMap.get("objectId");
            DomainObject rts=new DomainObject(rtsId );

            StringList busList=new StringList();
            busList.add(RequestToSupplier.SELECT_ID);
            StringList relList=new StringList();
            String SELECT_ATTRIBUTE_PERCENTAGE=getAttributeSelect(RequestToSupplier.ATTRIBUTE_PERCENTAGE);
            relList.add(SELECT_ATTRIBUTE_PERCENTAGE);

//            MapList quotMapList=rts.getRelatedObjects(context,RequestToSupplier.RELATIONSHIP_RTS_QUOTATION,
//                    RequestToSupplier.TYPE_RTS_QUOTATION,busList,relList,false,true,(short)1,null,null);
            
            String strObjectWhere = "attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_RESULT+"] != NG";
            MapList quotMapList=rts.getRelatedObjects(context,RequestToSupplier.RELATIONSHIP_RTS_QUOTATION,
                    RequestToSupplier.TYPE_RTS_QUOTATION,busList,relList,false,true,(short)1,strObjectWhere,null);

            RTSQuotation rtsQuotation=new RTSQuotation();

            HashMap tmpLineItemInfo=null;
            String awardPercentage=null,strQuotId =null;
            Map quotMap=null;
            if(quotMapList != null)
            {
                MapList tmpquoteCompareMap = null;
                Iterator quotItr = quotMapList.iterator();
                while(quotItr.hasNext())
                {
                    quotMap= (Map)quotItr.next();
                    strQuotId = (String)quotMap.get(RequestToSupplier.SELECT_ID);
                    rtsQuotation.setId(strQuotId);

                    awardPercentage=(String)quotMap.get(SELECT_ATTRIBUTE_PERCENTAGE);

                    double dAwardPer=-1;
                    //if(awardPercentage!=null || awardPercentage!="" || ("0.0".equals(awardPercentage)==false))//JB's code cleanup
                    if((awardPercentage!=null) && (awardPercentage.trim().length()>0) && ((!"0.0".equals(awardPercentage))))
                    {
                        try {
                            dAwardPer=Double.parseDouble(awardPercentage);
                        } catch (RuntimeException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            dAwardPer=-1;
                        }
                    }
                    String[] inputArgs = new String[1];
                    inputArgs[0] = strQuotId;
                    tmpquoteCompareMap = (MapList) JPO.invoke(context, "SourcingRFQQuotation", null, "getSupplierLineItemsDetails", inputArgs,MapList.class);
                    tmpLineItemInfo=rtsQuotation.getLineItemInfo(context, true);

                    Iterator itr=tmpquoteCompareMap.iterator();
                    
                    String lineItemsplitname=null;
                    while(itr.hasNext())
                    {
                        Map map=(Map)itr.next();
                        String refli=(String)map.get("refli");
                        String response=(String)map.get(SourcingConstants.SLI_ATTRIBUTE_RESPONSE);

                        Map map12=(Map)tmpLineItemInfo.get(refli);
                        String lineItemId=(String)map12.get(SELECT_ID);
                        String relationshipId=(String)map12.get(SELECT_RELATIONSHIP_ID);
                        //start-fix IR-199289 
                        String lineItemname=(String)map12.get("attribute[Entered Name]");
                        String splitLineItemIdsList=(String) LineItem.SELECT_BASE_LINE_ITEM_ID;
                        SplitQuantity=Float.parseFloat((String)map12.get(SourcingConstants.SELECT_ANNUAL_PART_QUANTITY));
                        if(!lineItemname.equals(lineItemsplitname))
                        {
                            lineItemsplitname=lineItemname;
                             BaseQuantity= Float.parseFloat((String)map12.get(SourcingConstants.SELECT_ANNUAL_PART_QUANTITY));
                             map.put(SELECT_ID,lineItemId);
                             map.put(SELECT_RELATIONSHIP_ID,relationshipId);
                             map.put("split", "0.0");
                             
                        }
                        else
                        {
                            splitPer =((SplitQuantity)*100/BaseQuantity);
                            splitPercentage=String.valueOf(splitPer);
                            map.put(SELECT_ID,lineItemId);
                            map.put(SELECT_RELATIONSHIP_ID,relationshipId);
                            map.put("split", splitPercentage);
                        }
                        //end-fix IR-199289 
    
                        map.put(SELECT_ID,lineItemId);
                        map.put(SELECT_RELATIONSHIP_ID,relationshipId);

                        if(dAwardPer > 0 && (SourcingConstants.SLI_ATTRIBUTE_RESPONSE_NONE.equals(response)==false))
                        {
                            //Awarded at Supplier level.
                            map.put("awardpercentage",String.valueOf(dAwardPer));
                        }
                    }

                    quoteCompareMap2.addAll(tmpquoteCompareMap);
                }
            }
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //System.out.println("quoteCompareMap2:"+quoteCompareMap2);
        return quoteCompareMap2;
    }


    /**
     * Check Trigger Method to check Mandatory Attributes of LineItem while promoting from Started to Review State
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public int checkForMandAttributes(matrix.db.Context context, String[] args) throws Exception {
    	try {
    	  String objectId =args[0];
    	  DomainObject domain = DomainObject.newInstance(context, objectId);
          StringList unFilledAttributes = new StringList();
          boolean is_Mandatory_Attributes_Filled = true;
    	 unFilledAttributes = getUnfilledRequiredAttributes(context);
    	 Map attributeGroupManAtt = getEmptyMandatoryAttributeGroupAttributeData(context , "", "");
    	 Iterator it = unFilledAttributes.iterator();
    	 String unFilledAttributeString =  "";
    	 String strAttrName = "";
    	 while(it.hasNext()){
        	 //Modified for MSIL Capital-Start
//    		 unFilledAttributeString += (String)it.next();
    		 strAttrName = EnoviaResourceBundle.getAttributeI18NString(context, (String)it.next(), context.getSession().getLanguage());
    		 unFilledAttributeString = unFilledAttributeString +", "+strAttrName;
        	 //Modified for MSIL Capital-End
    	 }
    	    Set<Entry> entries = attributeGroupManAtt.entrySet();
    	 String attrGroupString =  "";
    	 for(Entry entry : entries){
    		 attrGroupString += entry.getKey()+":"+entry.getValue()+", ";
    	 }

    	  String RFQmessage = EnoviaResourceBundle.getProperty(context,"emxSourcingStringResource", (new Locale(context.getSession().getLanguage())), "emxSourcing.RFQPromotion.checkMandAttributesRFQ");
    	  String LImessage1 = EnoviaResourceBundle.getProperty(context,"emxSourcingStringResource", (new Locale(context.getSession().getLanguage())), "emxSourcing.RFQPromotion.checkMandAttributesLI1");
    	  String LImessage2 = EnoviaResourceBundle.getProperty(context,"emxSourcingStringResource", (new Locale(context.getSession().getLanguage())), "emxSourcing.RFQPromotion.checkMandAttributesLI2");
    	 
         if ( unFilledAttributes.size() > 0)
         {
        	 is_Mandatory_Attributes_Filled = false;
            	 emxContextUtil_mxJPO.mqlNotice(context,RFQmessage+unFilledAttributeString);
              return 1;    
         }else if(attributeGroupManAtt.size()>0){        	 
        	 is_Mandatory_Attributes_Filled = false;
             
        	 //Modified for MSIL Capital-Start
        	 if(context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER) || context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER)) {
        		 is_Mandatory_Attributes_Filled = true;
        		 return 0;
             }else 
            	 //Modified for MSIL Capital-End	 
            if(attributeGroupManAtt.size()>5){
            	 emxContextUtil_mxJPO.mqlNotice(context,LImessage1);
              }else {
             emxContextUtil_mxJPO.mqlNotice(context,LImessage2+attrGroupString );
             }
               return 1;
         }else {
             is_Mandatory_Attributes_Filled = true;        
             return 0;
         }
    	}catch (Exception e) {
    	     throw new FrameworkException(e);
       }
     }
	
	//Code added for B3-Actions - start
	
	/**
	 * displayProjectList Method - Display Civil department's Project List
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mProjectList 
	 * @throws Exception if the operation fails
	 */
	 
	public HashMap displayProjectList(Context context,String[] args) throws Exception 
	{
		MapList mlFindProjectList = new MapList();
		MapList mlConnectedDepartment = new MapList();
		Map mapObject = null;
		Map mapDept = null;
		HashMap mProjectList = new HashMap();
		String strProjectId = null;
		String strProjName = null;
		String strProjOwner = null;
		String strDeptId = null;
		String strDeptName = null;
		StringList slProjectList =  new StringList();
		slProjectList.add(DomainConstants.EMPTY_STRING);
		StringList slProjectIDList =  new StringList();
		slProjectIDList.add(DomainConstants.EMPTY_STRING);
		StringList slSelectables = new StringList();
		slSelectables.addElement(DomainConstants.SELECT_NAME);
		slSelectables.addElement(DomainConstants.SELECT_ID);
		slSelectables.addElement(DomainConstants.SELECT_OWNER);
		StringList slDepartmentSelect = new StringList(2);
		slDepartmentSelect.addElement(DomainConstants.SELECT_NAME);
		
		Properties _classCurrencyConfig  =   new Properties();
		Page page= new Page("MSILAccessRights");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
		String sCIVILDepartments = _classCurrencyConfig.getProperty("WMSCIVIL.DepartmentDashboard.ENAccess");
		
		StringList slDepartments =  FrameworkUtil.split(sCIVILDepartments,",");
		
		try {
			ContextUtil.pushContext(context);
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
											DomainConstants.TYPE_DEPARTMENT,	//pattern to match types
											slDepartmentSelect, 				// list of select statement pertaining to Business Obejcts.
											null,								//list of select statement pertaining to Relationships.
											true,								//get To relationships
											false, 								//get From relationships
											(short)1,							//the number of levels to expand, 0 equals expand all.
											null,								//where clause to apply to objects, can be empty ""
											null);								//where clause to apply to relationship, can be empty ""
				
				Iterator itrInner = mlConnectedDepartment.iterator();
				while(itrInner.hasNext()) {
					mapDept = (Map)itrInner.next();
					strDeptName = (String)mapDept.get(DomainConstants.SELECT_NAME);
					if (slDepartments.contains(strDeptName)){
						slProjectList.add(strProjName);
						slProjectIDList.add(strProjectId);
					} 
				}
			}
			mProjectList.put("field_choices", slProjectIDList);
			mProjectList.put("field_display_choices", slProjectList); 
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			ContextUtil.popContext(context);
		}
		return mProjectList;
	}
	/**
	 * connectProjectRFQ Method - connect RFQ and Project
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return 
	 * @throws Exception if the operation fails
	 */
 	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public void connectProjectRFQ(Context context,String[] args) throws Exception
	{
		String relationship_RFQ_Project = PropertyUtil.getSchemaProperty("relationship_RFQProject");
		String objectID = null;
		String strProject = null;
		DomainObject doRFQID = null;
		DomainObject doProjectID = null;
		StringBuilder sbCategoryList = new StringBuilder();
		ContextUtil.pushContext(context);
		try {
			Map programMap = (Map)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			objectID = (String)paramMap.get("objectId");
			doRFQID = DomainObject.newInstance(context,objectID);
			strProject = (String)paramMap.get("New Value");
			doProjectID = DomainObject.newInstance(context,strProject);
			
			DomainRelationship.connect(context,doRFQID, relationship_RFQ_Project, doProjectID);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		finally{
			ContextUtil.popContext(context);
		}
	}
	
	/**
	 * getSupplierList Method to get the connected Objects on expansion
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlFindSupplierList MapList containing the connected objects
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSupplierList (Context context, String[] args) throws Exception
	{
		DomainObject doRFQID  = null;
		MapList mlUniqueSupplierList = new MapList();
		MapList mlFindSupplierList = new MapList();
		Map mapObject = null;
		String strCompId = null;
		String strCompName = null;
		String strCompCatList = null;
		String strContractsAbove100 = null;
		String strContractsBetween10To100 = null;
		String strContractsBelow10 = null;
		StringList slSupplierCatList = new StringList();
		StringList slRFQCatList = new StringList();
		boolean returnVal = false;
		String strValue = DomainConstants.EMPTY_STRING;
		
		/*GetAll Line items and calculated over all budget ( Rate * Quantity) */
		
		try {
			HashMap programMap   = (HashMap) JPO.unpackArgs(args);
			String strRFQID = (String) programMap.get("parentOID");
			doRFQID = DomainObject.newInstance(context,strRFQID);
			double dTotalCalculatedRFQBudget = 	calculatedTotalBudgetFromLineItems(context,strRFQID);
			StringList slInfo=new StringList(2);
			slInfo.add("attribute["+ATTRIBUTE_WMS_CATEGORY_LIST+"]");
			slInfo.add("attribute["+ATTRIBUTE_TDR_BUDGET+"]");
			
			Map mRFQInfo = doRFQID.getInfo(context, slInfo);
			String strRFQCategoryList = (String)mRFQInfo.get("attribute["+ATTRIBUTE_WMS_CATEGORY_LIST+"]");
			String strRFQBudget = (String)mRFQInfo.get("attribute["+ATTRIBUTE_TDR_BUDGET+"]");
			
			StringList slCompanyIDs = doRFQID.getInfoList(context, "from["+RELATIONSHIP_RTS_SUPPLIER+"].to.id");
			
			float fRFQBudget = Float.parseFloat(strRFQBudget);
			
			slRFQCatList = FrameworkUtil.split(strRFQCategoryList, ",");
			
			StringList slSelectables = new StringList();
			slSelectables.addElement(DomainConstants.SELECT_NAME);
			slSelectables.addElement(DomainConstants.SELECT_ID);
			slSelectables.addElement("attribute[" + ATTRIBUTE_WMS_CATEGORY_LIST + "]");
			slSelectables.addElement("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_ABOVE_100MRS + "]");
			slSelectables.addElement("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BETWEEN_10TO100_MRS + "]");
			slSelectables.addElement("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BELOW_10MRS + "]");

			//get all the suppliers 
			mlFindSupplierList = DomainObject.findObjects(
													context,								//current context object
													DomainConstants.TYPE_COMPANY,			//type pattern
													DomainConstants.QUERY_WILDCARD,			//name pattern.
													DomainConstants.QUERY_WILDCARD,			//revision pattern.
													DomainConstants.QUERY_WILDCARD,			//owner pattern.
													DomainConstants.QUERY_WILDCARD,			//vault pattern.
													null, 									//where expression.
													false,									//false - do not find subtypes
													slSelectables);							//select clause.
			
			Iterator itr = mlFindSupplierList.iterator();
			boolean bMatch=true;
			while(itr.hasNext()) {
				returnVal = false;
				bMatch=true;
				Map mFinalResult = new HashMap();
				mapObject = (Map)itr.next();
				strCompId = (String)mapObject.get(DomainConstants.SELECT_ID);
				strCompName = (String)mapObject.get(DomainConstants.SELECT_NAME);
				strCompCatList = (String)mapObject.get("attribute[" + ATTRIBUTE_WMS_CATEGORY_LIST + "]");
				
				//Code added to compare budget and max contract value
				strContractsAbove100 = (String)mapObject.get("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_ABOVE_100MRS + "]");
				strContractsBetween10To100 = (String)mapObject.get("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BETWEEN_10TO100_MRS + "]");
				strContractsBelow10 = (String)mapObject.get("attribute[" + ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BELOW_10MRS + "]");
				
				//code added to check attribute value
				if(strContractsAbove100.equals("Yes")){
					//above 100MRs
					if (fRFQBudget > 100000000){
						returnVal = true;
					}
				} else if (strContractsBetween10To100.equals("Yes")){
					//in between 10MRs to 100MRs
					if ((10000000 <= fRFQBudget) || (fRFQBudget <= 100000000)){
						returnVal = true;
					}
				} else if (strContractsBelow10.equals("Yes")) {
					//below 10MRs
					if (10000000 <= fRFQBudget){
						returnVal = true;
					}
				}
				slSupplierCatList = FrameworkUtil.split(strCompCatList,",");
				//final comparison
				
				if(slRFQCatList.size()==slSupplierCatList.size()) {
					Iterator<String> itrRFQ= slRFQCatList.iterator();
					while(itrRFQ.hasNext()) {
						strValue=itrRFQ.next();
						if(slSupplierCatList.contains(strValue)) {
							continue;
						} else {
							bMatch=false;
							break;
						}
					}//end while
					if(bMatch && returnVal) {
						if(!slCompanyIDs.contains(strCompId)) {
							mFinalResult.put("id", strCompId);
							mlUniqueSupplierList.add(mFinalResult);
						}
					}
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return mlUniqueSupplierList;
	}
	/**
	 * getLineItemAttribute Method to get the total quotation amount
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return vector vecResponse
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Map getLineItemAttribute(Context context,String inputXML) throws Exception {
		Map mAttribute =new HashMap();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(inputXML)));
			/* java.io.File fXmlFile = new java.io.File("E:\\ag.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile); */
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression xpath4Rel = xpath.compile("//*[@role='role_Supplier']");
			NodeList relTypeNodes = (NodeList)xpath4Rel.evaluate(doc, XPathConstants.NODESET);
			String strKey=DomainConstants.EMPTY_STRING;
			String strNodeName=DomainConstants.EMPTY_STRING;
			String strAGName=DomainConstants.EMPTY_STRING;
			String strAgKey=DomainConstants.EMPTY_STRING;
			int iLenth="attribute_".length();
			for (int i = 0; i < relTypeNodes.getLength(); i++)
			{
				org.w3c.dom.Element eleRelType = (org.w3c.dom.Element)relTypeNodes.item(i);
				strKey = eleRelType.getAttribute("key");
				NodeList nodeSupAttributes  =  eleRelType.getChildNodes();
				for(int k=0;k<nodeSupAttributes.getLength();k++) {
					Node current  = nodeSupAttributes.item(k);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						org.w3c.dom.Element element = (org.w3c.dom.Element) current;
						strAgKey = element.getAttribute("key");
						strAGName=element.getAttribute("name");
						strAGName = strAGName.substring(iLenth, strAGName.length());
						mAttribute.put(strAGName, strKey+"::"+strAgKey);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return mAttribute;
	 }
	
	
	/**
	 * getTotalQuotationAmount Method to get the total quotation amount
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return vector vecResponse
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
		public Vector getTotalQuotationAmount(Context context, String[] args) throws Exception {
		Map mLIAttr = null;
		String strReducedQty = DomainConstants.EMPTY_STRING;
		String strObjeID = DomainConstants.EMPTY_STRING;
		String strQty = DomainConstants.EMPTY_STRING;
		String strSinceAmount = DomainConstants.EMPTY_STRING;
		String strRate = DomainConstants.EMPTY_STRING;
		double fTotalAmount = 0d;
		double fLineItemQty = 0d;
		double fLineItemRate = 0d;
		
		try {
			//String RELATIONSHIP_RTSQUOTATION = PropertyUtil.getSchemaProperty("relationship_RTSQuotation");
			String ATTRIBUTE_RFQHEADERXML = PropertyUtil.getSchemaProperty("attribute_RFQHeaderXML");
			
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			HashMap paramList= (HashMap) programMap.get("paramList");
			String strRFQQuoteID = (String) paramList.get("parentOID");
			Iterator<Map<String, String>> iteratorMap = objectList.iterator();
			Map<String, String> mapObjectData = new HashMap<String, String>();
			
			int intSize = objectList.size();
			Vector vecResponse = new Vector(intSize);
			
			//getting RFQ ID and attribute
			ContextUtil.pushContext(context);
			DomainObject doRFQQuoteID = DomainObject.newInstance(context, strRFQQuoteID);
			String sRFQAttrRFQHeaderXML = (String) doRFQQuoteID.getInfo(context, "to["+DomainConstants.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+ATTRIBUTE_RFQHEADERXML+"]");
			
			mLIAttr = getLineItemAttribute(context,sRFQAttrRFQHeaderXML);
			String sRate = (String)mLIAttr.get("Rate");
			
			while (iteratorMap.hasNext()) {
		 		mapObjectData = iteratorMap.next();
				strObjeID = mapObjectData.get("id");
				strQty = (String)mapObjectData.get("attribute["+ATTRIBUTE_ANNUAL_PART_QUANTITY+"]");
				strRate = mapObjectData.get(sRate);
				
				fLineItemQty = Double.parseDouble(strQty);
				fLineItemRate = Double.parseDouble(strRate);
				fTotalAmount = fLineItemQty * fLineItemRate;
				vecResponse.add(String.valueOf(fTotalAmount));
			}
			return vecResponse;
		} catch (Exception exception) {
			exception.printStackTrace();
			throw exception;
		} finally {
			ContextUtil.popContext(context);
		}
	}
	
	/**
	 * quoteSubmissionDateCheck Method to get the connected Objects on expansion
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return int 0 for success and 1 for failure 
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public int quoteSubmissionDateCheck(Context context, String[] args) throws Exception
	{
		String ATTRIBUTE_TDR_QUOTE_SUBMISSION_DATE = PropertyUtil.getSchemaProperty("attribute_TDRQuoteSubmissionDate");
		String strRFQID = args[0];
		DomainObject doRFQID  = null;
		Date date = new Date();//Date1
		
		try {
			doRFQID = DomainObject.newInstance(context,strRFQID);
			String strRFQSubDate= doRFQID.getAttributeValue(context, ATTRIBUTE_TDR_QUOTE_SUBMISSION_DATE);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
			Date RFQSubDate = sdf.parse(strRFQSubDate); //Date 2
			
			if (date.compareTo(RFQSubDate) > 0) {
				sendNotificationToApprover(context, strRFQID, strRFQSubDate);
				return 1;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return 0;
	}
	
	/**
	 * Method returns int value. This method will get Send email notifications to Approver
	 * Return 0 if success else 1.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object ID
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public int sendNotificationToApprover(Context context, String strRFQID, String strRFQSubDate)throws Exception {
		
		MapList mlPerson = new MapList();
		String strPersonID = null;
		String strEmail = null;
		StringList toList= new StringList();
		Map tempMap = null;
		
		String RELATIONSHIP_ROUTE_NODE = PropertyUtil.getSchemaProperty("relationship_RouteNode");
		String RELATIONSHIP_OBJECT_ROUTE = PropertyUtil.getSchemaProperty("relationship_ObjectRoute");
		String TYPE_PERSON = PropertyUtil.getSchemaProperty("type_Person");
		String TYPE_ROUTE = PropertyUtil.getSchemaProperty("type_Route");
		
		StringBuffer sbRelSelect = new StringBuffer(2);
		sbRelSelect.append(RELATIONSHIP_ROUTE_NODE);
		sbRelSelect.append(",");
		sbRelSelect.append(RELATIONSHIP_OBJECT_ROUTE);
		
		StringBuffer sbTypeSelect = new StringBuffer(2);
		sbTypeSelect.append(TYPE_ROUTE);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_PERSON);
		
		StringList slBusSelect = new StringList(1);
		slBusSelect.add(DomainObject.SELECT_ID);
		slBusSelect.add(DomainObject.SELECT_NAME);
		slBusSelect.add("attribute["+DomainObject.ATTRIBUTE_EMAIL_ADDRESS+"].value");
		
		matrix.util.Pattern filterTypePattern = new matrix.util.Pattern(TYPE_PERSON);
		matrix.util.Pattern filterRelPattern  = new matrix.util.Pattern(RELATIONSHIP_ROUTE_NODE);
		
		StringList slObjectSelect = new StringList();
		slObjectSelect.add(DomainObject.SELECT_ID);
		slObjectSelect.add("from["+TDRConstants_mxJPO.RELATIONSHIP_RFQ_SUPPLIER+"].to.id");
		slObjectSelect.add(DomainObject.SELECT_NAME);
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
			Date RFQSubDate = sdf.parse(strRFQSubDate); //Date 2
			
			DomainObject doObject = new DomainObject(strRFQID);
				
			mlPerson = doObject.getRelatedObjects(context,								//context
														sbRelSelect.toString(),			//relationshipPattern
														sbTypeSelect.toString(),		//typePattern 
														slBusSelect,					//objectSelects 
														null,							//relationshipSelects 
														true,							//getTo 
														true,							//getFrom 
														(short)3,						//recurseToLevel 
														null,							//objectWhere 
														null,							//relationshipWhere 
														filterTypePattern,				//TYPE_WMS_WORK_ORDER includeType
														filterRelPattern,				//RELATIONSHIP_BILL_OF_QUANTITY includeRelationship 
														null);							//includeMap
			
			for(int i=0;i<mlPerson.size();i++) {
				tempMap = (Map)mlPerson.get(i);
				strEmail = (String)tempMap.get("attribute["+DomainObject.ATTRIBUTE_EMAIL_ADDRESS+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strEmail)) {
					toList.add((String)tempMap.get(DomainObject.SELECT_NAME));
				}
			}
			
			Map mObjectInfo = doObject.getInfo(context, slObjectSelect);
			String message = "Quote submit date '"+RFQSubDate+"' is passed for RFQ '"+(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"'";
			StringList ccList = new StringList("");
			StringList bccList= new StringList("");
			StringList objectIdList= new StringList(strRFQID);
			String subject = "RFQ '" +(String)mObjectInfo.get(DomainObject.SELECT_NAME)+"' Reviewed and Sent";
			String notifyType = "both";
			StringList replyTo = new StringList("");
			String fromAgent = context.getUser();
			String messageHTML = "";
			emxNotificationUtil_mxJPO.sendJavaMail(context, toList, null, null, subject, message, messageHTML, fromAgent, null, objectIdList, notifyType);
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return 0;
	}

	/**
	 * @param context
	 * @param language
	 * @return
	 * @throws Exception
	 */
	public MapList getDynamicSupplierColumns (Context context , String [] args) throws Exception
	{
		MapList mlReturnList = new MapList();
		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String strObjectId = (String)requestMap.get("objectId");
		StringList slObjSelect=new StringList(2);
		slObjSelect.add(DomainConstants.SELECT_NAME);
		slObjSelect.add(DomainConstants.SELECT_ID);
		MapList contractList = new MapList();
		StringList slRelSelect=new StringList(2);
		slRelSelect.add(DomainRelationship.SELECT_NAME);
		slRelSelect.add(DomainRelationship.SELECT_ID);
		MapList supplierList = new MapList();
		if (strObjectId != null && !"".equals(strObjectId) && !"null".equals(strObjectId)){
			DomainObject supplierObject = DomainObject.newInstance(context,strObjectId);

			if (contractList != null && contractList.size() == 0){
				supplierList =  supplierObject.getRelatedObjects(context,
																"RFQ Supplier",
																"Company",
																slObjSelect,
																slRelSelect,
																false,
																true,
																(short)2,
																"",
																"");
				StringList slVendors = new StringList();

				for (int iContract = 0 ; iContract < supplierList.size(); iContract++)
				{
					Map mpContract           = (Map) supplierList.get(iContract);
					String strId             = (String) mpContract.get(DomainConstants.SELECT_ID);
					String strContractName   = (String) mpContract.get(DomainConstants.SELECT_NAME);

					slVendors.add(strContractName);

					Map mapColumnView = new HashMap();
					mapColumnView.put("label", "Rate");
					mapColumnView.put("name", strContractName);
					mapColumnView.put("expression", "name");

					Map mapSettingsView = new HashMap();

					mapSettingsView.put("Registered Suite","Sourcing");
					mapSettingsView.put("program","SourcingRFQ");
					mapSettingsView.put("function","getFinalOfferedRate");
					mapSettingsView.put("Column Type","programHTMLOutput");
					mapSettingsView.put("Editable","true");
					mapSettingsView.put("Group Header",strContractName);
					mapSettingsView.put("Vendor Name",strContractName);
					mapSettingsView.put("Field Type","attribute");
					mapSettingsView.put("Export","true");
					mapSettingsView.put("Field Type","attribute");
					mapSettingsView.put("PrinterFriendly","true");
					mapColumnView.put("settings", mapSettingsView);
					mlReturnList.add(mapColumnView);

					Map mapColumnView1 = new HashMap();
					mapColumnView1.put("label", "Amount");
					mapColumnView1.put("name", strContractName);
					
					Map mapSettingsView1 = new HashMap();
					mapSettingsView1.put("Registered Suite","Sourcing");
					mapSettingsView1.put("program","SourcingRFQ");
					mapSettingsView1.put("function","getTotalAmount");
					mapSettingsView1.put("Column Type","programHTMLOutput");
					mapSettingsView1.put("Editable","false");
					mapSettingsView1.put("Group Header",strContractName);
					mapSettingsView1.put("Vendor Name",strContractName);
					mapSettingsView1.put("Export","true");
					mapSettingsView1.put("PrinterFriendly","true");
					mapColumnView1.put("settings", mapSettingsView1);
					mlReturnList.add(mapColumnView1);

					Map mapColumnView2 = new HashMap();
					Map mapSettingsView2 = new HashMap();
					mapSettingsView2.put("Registered Suite","Sourcing");
					mapSettingsView2.put("Column Type","separator");
					mapColumnView2.put("settings", mapSettingsView2);
					mlReturnList.add(mapColumnView2);
				}
			}
		}
		return mlReturnList;
	}
	
	/**
	 * @param context
	 * @param language
	 * @return
	 * @throws Exception
	 */
	public Vector getFinalOfferedRate (Context context , String [] args) throws Exception
	{
		Vector vcReturn =new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Map paramList = (Map) programMap.get("paramList");
		String objectId = (String) paramList.get("parentOID");
		Map columnMap = (Map)programMap.get("columnMap");

		Map settingsMap         = (Map) columnMap.get("settings");
		String vendorName = (String)settingsMap.get("Vendor Name");
		StringList slObjSelect=new StringList(2);
		slObjSelect.add(DomainConstants.SELECT_NAME);
		slObjSelect.add(DomainConstants.SELECT_ID);
		StringList slRelSelect=new StringList(2);
		slRelSelect.add(DomainRelationship.SELECT_NAME);
		LineItem lineItem = (LineItem)DomainObject.newInstance(context, DomainConstants.TYPE_LINE_ITEM,  DomainConstants.SOURCING  );
		RequestToSupplier rts= (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
		RTSQuotation rtsQuotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
		rts.setId(objectId);
		SelectList busSelects = new SelectList(3);
		busSelects.add(lineItem.SELECT_ID);
		busSelects.add(lineItem.SELECT_ENTERED_NAME);
		busSelects.add("attribute[Annual Part Quantity]");
		SelectList relSelects = new SelectList(1);
		
		MapList liList = rts.getLineItems(context, busSelects, null, relSelects, null, false);
		String strTaxRate = rts.getAttributeValue(context, "MSILRFQTaxRate");
		HashMap hmLineItemMap=new HashMap();
		for(int k=0;k<liList.size();k++){
			Map lineItemsTemp=(Map)liList.get(k);
			String strQtyTemp = (String)lineItemsTemp.get("attribute[Annual Part Quantity]");
			String strLINameTemp = (String)lineItemsTemp.get(lineItem.SELECT_ENTERED_NAME);
			hmLineItemMap.put(strLINameTemp, strQtyTemp);
		}
		DomainObject rts1 = new DomainObject(objectId);
		StringList busList = new StringList();
		busList.addElement(DomainConstants.SELECT_ID);
		busList.addElement(DomainConstants.SELECT_NAME);
		busList.addElement("from[Supplier Response].to.name");

		StringList relList = new StringList();
		relList.addElement(DomainRelationship.SELECT_ID);
		String strWhereCondition = "from[Supplier Response].to.name ==\""+vendorName+"\"";

		MapList quotationIds=rts1.getRelatedObjects(context,
													"RFQ Quotation",
													"RFQ Quotation",
													busList,
													relList,
													false,
													true,
													(short)1,
													strWhereCondition,
													null);

		Iterator itre=quotationIds.iterator();
		Map lineItemMap;
		while(itre.hasNext()){
			Hashtable quoteIdMap=(Hashtable)itre.next();
			if(quoteIdMap.get("id") != null){
				rtsQuotation.setId((String)quoteIdMap.get("id"));
				MapList lineItemsList = rtsQuotation.getAllSupplierLineItems(context);
				String unitprice = "";

				for (Object object : lineItemsList) {
					lineItemMap = (Map)object;
					 unitprice = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_UNIT_PRICE);
					 vcReturn.add(unitprice);
				}
			}
		}
		return vcReturn;
	}
	
	
	/**
	* @param context
	* @param language
	* @return
	* @throws Exception
	*/
	public Vector getTotalAmount (Context context , String [] args) throws Exception
	{
		Vector vcReturn =new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		MapList objectList = (MapList) programMap.get("objectList");
		Map paramList = (Map) programMap.get("paramList");
		String objectId = (String) paramList.get("parentOID");
		Map columnMap = (Map)programMap.get("columnMap");
		Map settingsMap         = (Map) columnMap.get("settings");
		String vendorName = (String)settingsMap.get("Vendor Name");
		StringList slObjSelect=new StringList(2);
		slObjSelect.add(DomainConstants.SELECT_NAME);
		slObjSelect.add(DomainConstants.SELECT_ID);
		StringList slRelSelect=new StringList(2);
		slRelSelect.add(DomainRelationship.SELECT_NAME);
		LineItem lineItem = (LineItem)DomainObject.newInstance(context, DomainConstants.TYPE_LINE_ITEM,  DomainConstants.SOURCING  );
		RequestToSupplier rts= (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
		RTSQuotation rtsQuotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
		rts.setId(objectId);
		SelectList busSelects = new SelectList(3);
		busSelects.add(lineItem.SELECT_ID);
		busSelects.add(lineItem.SELECT_ENTERED_NAME);
		busSelects.add("attribute[Annual Part Quantity]");
		SelectList relSelects = new SelectList(1);
		StringList totalList = new StringList();
		MapList liList = rts.getLineItems(context, busSelects, null, relSelects, null, false);
		String strTaxRate = rts.getAttributeValue(context, "MSILRFQTaxRate");
		HashMap hmLineItemMap=new HashMap();
		for(int k=0;k<liList.size();k++){
			Map lineItemsTemp=(Map)liList.get(k);
			String strQtyTemp = (String)lineItemsTemp.get("attribute[Annual Part Quantity]");
			String strLINameTemp = (String)lineItemsTemp.get(lineItem.SELECT_ENTERED_NAME);
			hmLineItemMap.put(strLINameTemp, strQtyTemp);
		}
		MapList quotationIds1=rts.getRTSQuotations(context,null,null,true);
		Iterator itre=quotationIds1.iterator();
		StringList slSupList = new StringList();
		Map lineItemMap;
		String strKey = "";
		HashMap sliInfoMap=new HashMap();
		HashMap hmCompTotalMap=new HashMap();
		HashMap hmLowestPriceMap = new HashMap();
		String strLILowestPrice = "";
		while(itre.hasNext()){
			Hashtable quoteIdMap=(Hashtable)itre.next();
			if(quoteIdMap.get("id") != null){
				rtsQuotation.setId((String)quoteIdMap.get("id"));
				MapList lineItemsList = rtsQuotation.getAllSupplierLineItems(context);
				String SupplierName = rtsQuotation.getSupplierNameforQuotation(context);
				slSupList.addElement(SupplierName);
				for (Object object : lineItemsList) {
					lineItemMap = (Map)object;
					String unitprice = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_UNIT_PRICE);
					String lineItemName = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_NAME);
					strKey = lineItemName+"~"+SupplierName;
					sliInfoMap.put(strKey, unitprice);
					String strLIQty = (String)hmLineItemMap.get(lineItemName);
					String total = String.valueOf(Float.parseFloat(unitprice)*Float.parseFloat(strLIQty));
					totalList.add(total);
				}

			}
		}
		double dTemp = 0;
		double dTemp1 = 0;
		int iSize = totalList.size();
		for(int iCount=0;iCount<iSize;iCount++)
		{
			dTemp = Double.parseDouble((String)totalList.get(iCount));
			if(iCount == 0)
			{
				dTemp1 = dTemp;
			}
			else
			{
				if(dTemp < dTemp1)
					dTemp1 = dTemp;
			}
		}
		String strLowOfrdRate = String.valueOf(dTemp1);
		StringList busList = new StringList();
		busList.addElement(DomainConstants.SELECT_ID);
		busList.addElement(DomainConstants.SELECT_NAME);
		StringList relList = new StringList();
		relList.addElement(DomainRelationship.SELECT_ID);
		String strWhereCondition = "from[Supplier Response].to.name ==\""+vendorName+"\"";

		MapList quotationIds=rts.getRelatedObjects(context,
													"RFQ Quotation",
													"RFQ Quotation",
													busList,
													relList,
													false,
													true,
													(short)1,
													strWhereCondition,
													null);

		Iterator itre1=quotationIds.iterator();
		Map lineItemMap1;
		while(itre1.hasNext()){
			Hashtable quoteIdMap=(Hashtable)itre1.next();
			if(quoteIdMap.get("id") != null){
				rtsQuotation.setId((String)quoteIdMap.get("id"));
				MapList lineItemsList = rtsQuotation.getAllSupplierLineItems(context);
				String strAmount ="";
				for (Object object : lineItemsList) {
					lineItemMap1 = (Map)object;
					String unitprice = (String) lineItemMap1.get(SourcingConstants.SLI_ATTRIBUTE_UNIT_PRICE);
					String lineItemName = (String) lineItemMap1.get(SourcingConstants.SLI_ATTRIBUTE_NAME);
					String strLIQty = (String)hmLineItemMap.get(lineItemName);
					strAmount =String.valueOf( Float.parseFloat(unitprice)*Float.parseFloat(strLIQty));
					vcReturn.add(strAmount);
				}
			}
		}
		return vcReturn;
	}
	
	
	private  Double calculatedTotalBudgetFromLineItems(Context context,String strRFQId) throws Exception{
	   double dTotalBudget=0.0;
      try{
		    DomainObject domRFQ=DomainObject.newInstance(context,strRFQId);
		    ContextUtil.pushContext(context);
			String strRFQXML= domRFQ.getAttributeValue(context, "RFQ Header XML");
			String strAttributeKey = getAttributeKeyFromRFQ(context,strRFQXML);
			RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
			rts.setId(strRFQId);
			StringList busSelects = new StringList();
			busSelects.add(DomainConstants.SELECT_ID);
	 		busSelects.add("attribute[Line Item XML]");
		 	busSelects.add(LineItem.SELECT_ANNUAL_QUANTITY);
	 		MapList allLineItemMapList=rts.getAllLineItems(context, busSelects,null, null, null,false);
			Iterator<Map> itrLi = allLineItemMapList.iterator();
			String strQty="0.0";
			String strLiXML=DomainConstants.EMPTY_STRING;
			String strRate=DomainConstants.EMPTY_STRING;
		    while(itrLi.hasNext()) {
				Map mLineItem = itrLi.next();
			 	strQty=(String)mLineItem.get(LineItem.SELECT_ANNUAL_QUANTITY);
			    strLiXML=(String)mLineItem.get("attribute[Line Item XML]");
		 		strRate = getLineItemRate(context,strLiXML, strAttributeKey);
				dTotalBudget = dTotalBudget + (Double.parseDouble(strQty) * Double.parseDouble(strRate)) ;
	 		}
	      }catch(Exception e){
		 e.printStackTrace();
		 
	    }
		finally{
			ContextUtil.popContext(context);
		}		
	 	
		
	return dTotalBudget;	
	}
	

	/**
 * Read line item rate 
 * 
 * @param context
 * @param liXML
 * @param attributeKey
 * @return
 */
	private String  getLineItemRate(Context context, String liXML, String attributeKey)  throws Exception
	{
		String strRate="0.0";
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		DocumentBuilder dBuilDocumentBuilderFactoryder = dbFactory.newDocumentBuilder();
		Document docLi = dBuilder.parse(new InputSource(new StringReader(liXML)));

		xpath = xPathfactory.newXPath();
		XPathExpression xpathExpression = xpath.compile("//*[@refattr='"+attributeKey+"']");
		NodeList liNodes = (NodeList)xpathExpression.evaluate(docLi, XPathConstants.NODESET);

		for (int i = 0; i < liNodes.getLength(); i++)
		{
			org.w3c.dom.Element eleRelType = (org.w3c.dom.Element)liNodes.item(i);
			strRate = eleRelType.getAttribute("value");
		}
		return strRate;
	}


	public String getAttributeKeyFromRFQ(Context context,String strRFQXML) throws Exception
	{
		String strKey=DomainConstants.EMPTY_STRING;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(strRFQXML)));
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression xpath4Rel = xpath.compile("//*[@role='role_Buyer']");
			NodeList relTypeNodes = (NodeList)xpath4Rel.evaluate(doc, XPathConstants.NODESET);

			String strNodeName=DomainConstants.EMPTY_STRING;
			String strAGName=DomainConstants.EMPTY_STRING;
			String strAgKey=DomainConstants.EMPTY_STRING;
			int iLenth="attribute_".length();
		 
			for (int i = 0; i < relTypeNodes.getLength(); i++)
			{
				org.w3c.dom.Element eleRelType = (org.w3c.dom.Element)relTypeNodes.item(i);
				strKey = eleRelType.getAttribute("key");
				NodeList nodeSupAttributes  =  eleRelType.getChildNodes();
				for(int k=0;k<nodeSupAttributes.getLength();k++) {
				Node current  = nodeSupAttributes.item(k);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						org.w3c.dom.Element element = (org.w3c.dom.Element) current;
						strAgKey = element.getAttribute("key");
						strAGName=element.getAttribute("name");
						strAGName = strAGName.substring(iLenth, strAGName.length());
					   if(strAGName.equalsIgnoreCase("RATE")) 
							strKey= strAgKey;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		throw e;
		}
		return strKey;
	}
	
	
	
	//Code added for B3-Actions - end


}
