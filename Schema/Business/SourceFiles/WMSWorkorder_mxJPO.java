/** Name of the JPO    : WMSWorkorder
 ** Developed by    : DSIS 
 ** Client            : MSIL
 ** Description        : The purpose of this JPO is to manage all code for Workorder
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/
import com.matrixone.apps.domain.util.MailUtil;
import org.joda.time.LocalDate;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
 
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.Calendar;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.io.StringReader;
import java.text.DateFormat;
import matrix.util.Pattern;
import matrix.util.StringList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Page;
import matrix.db.RelationshipType;
 
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.sourcing.LineItem;
import com.matrixone.apps.sourcing.RequestToSupplier;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import org.xml.sax.InputSource;
import com.matrixone.apps.common.Company;

/**
 * The purpose of this JPO is to handle functionality of SOR
 * @author DSIS
 */
public class WMSWorkorder_mxJPO extends WMSConstants_mxJPO
{
    /**
     * Constructor.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds no arguments
     * @throws Exception if the operation fails
     * @author DSIS
     */

    public WMSWorkorder_mxJPO(Context context, String[] args) throws Exception
    {
       super(context,args);
    }
/**
     * Method to get the connected Work Orders from the Project
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps from the command or form or table
     * @return mapListWorkOrders MapList containing the Work Order MBEs
     * @throws Exception if the operation fails
     * @author WMS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWorkOrders (Context context, String[] args) throws Exception 
    {
        try
        {     
		 
            MapList mapListWorkOrders = new MapList();
            String strObjectId = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
		    if(UIUtil.isNotNullAndNotEmpty(strObjectId))
            {
                DomainObject domObjWO = DomainObject.newInstance(context, strObjectId);
                mapListWorkOrders = getWorkOrders(context, domObjWO);
			 
            }
            return mapListWorkOrders;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

    /**
     * Function to get the Work Orders connected to the Project Space
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domObjProjectSpace DomainObject instance of selected Work Order 
     * @return mapListMBEs MapList containing the MBEs connected to Work Order with ID
     * @throws FrameworkException if the operation fails
     * @author WMS
     * @since 418
     */
    private MapList getWorkOrders(Context context, DomainObject domObjProjectSpace)
            throws Exception {
        try
        {
            StringList strListBusSelects     = new StringList(1);
            strListBusSelects.add(DomainConstants.SELECT_ID);
            StringList strListRelSelects     = new StringList(1);
            strListRelSelects.add(DomainRelationship.SELECT_ID);

            MapList mapListWorkOrders = domObjProjectSpace.getRelatedObjects(context, // matrix context
                    "WMSProjectWorkOrder",//RELATIONSHIP_PROJECT_WO, // relationship pattern
                    "WMSWorkOrder", //TYPE_WMSWorkorder, // type pattern
                    strListBusSelects, // object selects
                    strListRelSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 0, // recursion level
                    DomainConstants.EMPTY_STRING, // object where clause
                    DomainConstants.EMPTY_STRING, // relationship where clause
                    0);
            return mapListWorkOrders;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }


    /**
     * Access function for formType of Work Order set Non editableto make it editable
     * @param context The Matrix Context object
     * @param args holds the arguments:     
     * @throws Exception if the operation fails
     */ 
    public String setCompletionDuedateField(Context context, String[] args)throws Exception 
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");

        StringBuffer strPAC = new StringBuffer();
        try
        {
            if("create".equals(strMode))
            {
                strPAC.append("<input type=\"text\" readonly=\"true\" size=\"20\" name=\"CompletionDueDate1\" id=\"CompletionDueDate1\" />");
            }
            else if("edit".equals(strMode))
            {
                HashMap paramMap = (HashMap) programMap.get("paramMap");
                String sWorkOrderOid = (String) paramMap.get("objectId");
                DomainObject domObj = DomainObject.newInstance(context, sWorkOrderOid);
                String strAttrCompletionDueDate = domObj.getInfo(context, "attribute["+ATTRIBUTE_WMS_COMPLETION_DUE_DATE+"]");
                if(UIUtil.isNotNullAndNotEmpty(strAttrCompletionDueDate)){
                    Date date = new Date(strAttrCompletionDueDate);  
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");  
                    String strDate = formatter.format(date);  
                    strPAC.append("<input type=\'text\' readonly=\'true\' size=\'20\' name=\'CompletionDueDate1\' id=\'CompletionDueDate1\' value =\'"+strDate+"\'/>");
                }
            }
        }
        catch(Exception exception) 
        {
            exception.printStackTrace();
            throw exception;
        } 	
        return strPAC.toString();
    }



    /** 
      * Method will connect the Work Order In Projects
      * @param args Packed program and request maps for the table
      * @throws Exception if the operation fails
      * @author CHiPS
      * @since 418
      */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map connectWorkOrderInProjects(Context context, String[] args) throws Exception {
        HashMap mapReturnMap = new HashMap();
     
        try {
            HashMap programMap              = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap             = (HashMap)programMap.get("paramMap");
            HashMap requestMap           = (HashMap)programMap.get("requestMap");
            String strWOOID             = (String) paramMap.get("objectId");
            String strProjectOID     = (String) requestMap.get("parentOID");
            String ContractorOID     =(String) requestMap.get("NameOfContractorOID");//RFQ
            String strCompletionDate     =(String) requestMap.get("CompletionDueDate1");
            String strValueOfContract     =(String) requestMap.get("ValueOfContract1");
        
            Date dCompletionDate = new Date(strCompletionDate);
            long lCompletionTime = dCompletionDate.getTime();
       
        
            //use MatrixDateFormat's pattern
            SimpleDateFormat mxDateFrmt = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
            String strFormattedCompletionDate = mxDateFrmt.format(dCompletionDate);
         
            if( UIUtil.isNotNullAndNotEmpty(strWOOID))
            {
                                  
                HashMap<String,String> hashMapAttributes = new HashMap<>(2);
                hashMapAttributes.put(ATTRIBUTE_WMS_VALUE_OF_CONTRACT, strValueOfContract);
                hashMapAttributes.put(ATTRIBUTE_WMS_COMPLETION_DUE_DATE, strFormattedCompletionDate);
                DomainObject domObjWO = DomainObject.newInstance(context, strWOOID);
                domObjWO.setAttributeValues(context, hashMapAttributes);
                 
            }
            if(UIUtil.isNotNullAndNotEmpty(ContractorOID) && UIUtil.isNotNullAndNotEmpty(strWOOID))
            {
                DomainRelationship.connect(context, ContractorOID, RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR, strWOOID, true);
            }
            if(UIUtil.isNotNullAndNotEmpty(strProjectOID) && UIUtil.isNotNullAndNotEmpty(strWOOID))
            {
                
                DomainRelationship.connect(context, strProjectOID, RELATIONSHIP_WMS_PROJECT_WORK_ORDER, strWOOID, true);
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("<mxRoot>");
                strBuffer.append("<action><![CDATA[add]]></action>");
                strBuffer.append("<data status=\"committed\">");
                strBuffer.append("<item oid=\""+strWOOID+"\" relId=\""+""+"\" pid=\""+strWOOID+"\"  direction=\"from\" />");
                strBuffer.append("</data>");
                strBuffer.append("</mxRoot>");
                mapReturnMap.put("selectedOID", strProjectOID);
                mapReturnMap.put("rows",strBuffer.toString());
                //mapReturnMap.put ("Action", "execScript");
                //mapReturnMap.put("Message", "{ main:function() {alert(11);refreshRows();refreshStructureWithOutSort();}}");
                mapReturnMap.put("Insertdata",strBuffer.toString());
                //mapReturnMap.put("Message","Successfully created workorder ");
                mapReturnMap.put("Action","success");
            }
        } catch (Exception exception) {
         exception.printStackTrace();
            mapReturnMap.put("Action","Stop");
            mapReturnMap.put("Message",exception.getMessage());
        }
        return mapReturnMap;
    }


   @com.matrixone.apps.framework.ui.ProgramCallable
    public String SetWOCompleteDateRainySeason(Context context, String[] args) throws Exception
    {
        String strNewEnddate = "";
        try
        {
            HashMap programMap          = (HashMap)JPO.unpackArgs(args);
            String strWOStartDate = (String)programMap.get("strWOStartDate");
            String strWOEndDate = (String)programMap.get("strWOEndDate");
            String strDuration = (String)programMap.get("Duration");
            String strPreparationTime = (String)programMap.get("PreparationTime");
            int intDuration = Integer.parseInt(strDuration);
                    
            //String strLanguage = context.getSession().getLanguage();
            String strDefaultRainyStartDate =  EnoviaResourceBundle.getProperty(context,"wmsCustom", context.getLocale(), "WMS.RainySeason.StartDate");
            String strDefaultRainyEndDate = EnoviaResourceBundle.getProperty(context,"wmsCustom", context.getLocale(), "WMS.RainySeason.EndDate");
            String strWOPreparationTime = EnoviaResourceBundle.getProperty(context,"wmsCustom", context.getLocale(), "WMS.WorkOrder.PreparationTime");
            int intPreparationTime = Integer.parseInt(strWOPreparationTime.trim());
            if("Yes".equals(strPreparationTime))
            {
                intDuration = intDuration+intPreparationTime;
            }
            //Get rainy Start Date and Month
            StringList strListRainyDaysMonthStart = FrameworkUtil.split(strDefaultRainyStartDate, "-");
            StringList strListRainyDaysMonthEnd = FrameworkUtil.split(strDefaultRainyEndDate, "-");
            
            int intRainySeasonStartDate = -1;
            int intRainySeasonStartMonth = -1;
            int intRainySeasonEndDate = -1;
            int intRainySeasonEndMonth = -1;
            
            if(strListRainyDaysMonthStart!=null && strListRainyDaysMonthEnd!=null)
            {
                if(strListRainyDaysMonthStart.size()>=0)
                {
                    String strRainySeasonStartDate = (String)strListRainyDaysMonthStart.get(0);
                    String strRainySeasonStartMonth = (String)strListRainyDaysMonthStart.get(1);
                    intRainySeasonStartDate = WMSUtil_mxJPO.convertToInteger(strRainySeasonStartDate);
                    intRainySeasonStartMonth = WMSUtil_mxJPO.convertToInteger(strRainySeasonStartMonth);
                }
                if(strListRainyDaysMonthEnd.size()>=0)
                {
                    String strRainySeasonStartDate = (String)strListRainyDaysMonthEnd.get(0);
                    String strRainySeasonStartMonth = (String)strListRainyDaysMonthEnd.get(1);
                    intRainySeasonEndDate = WMSUtil_mxJPO.convertToInteger(strRainySeasonStartDate);
                    intRainySeasonEndMonth = WMSUtil_mxJPO.convertToInteger(strRainySeasonStartMonth);
                }
            }
                    
                            
            Calendar calendarStartDate = Calendar.getInstance();
            Calendar calendarEndDate = Calendar.getInstance();
            
            int intCurrentYear = calendarStartDate.get(Calendar.YEAR);
            
            long lWOStartDate=Long.parseLong(strWOStartDate);  
            long lWOEndDate=Long.parseLong(strWOEndDate);
            calendarStartDate.setTimeInMillis(lWOStartDate);
            calendarEndDate.setTimeInMillis(lWOEndDate);
                                
            Calendar calendarRainyStartDate = Calendar.getInstance();
            calendarRainyStartDate.set(intCurrentYear,intRainySeasonStartMonth-1 ,intRainySeasonStartDate,0,0);
            Date dRainyStartDate = calendarRainyStartDate.getTime();
            
            Calendar calendarRainyEndDate = Calendar.getInstance();
            calendarRainyEndDate.set(intCurrentYear,intRainySeasonEndMonth-1 ,intRainySeasonEndDate,0,0);
            Date dRainyEndDate = calendarRainyEndDate.getTime();
                                
            //Duration of Rainy days
            long lRainyDuration = (dRainyStartDate.getTime() - dRainyEndDate.getTime())/(1000*60*60*24);
            lRainyDuration =Math.abs(lRainyDuration);
            int intRainyDuration= (int) lRainyDuration;

            //Check end date falling into Rainy Season days
            int intEndMonth = calendarEndDate.get(Calendar.MONTH);
            int intEndDay = calendarEndDate.get(Calendar.DAY_OF_MONTH);					
                                    
            boolean bEndDateflag = false;          
            intEndMonth++;
            if(intEndMonth == intRainySeasonStartMonth)
            {
              if(intEndDay >=intRainySeasonStartDate)
                {
                    bEndDateflag =	true;				
                }
            }
            else if(intEndMonth>intRainySeasonStartMonth)
            {
                if(intEndMonth < intRainySeasonEndMonth)
                {
                  bEndDateflag =	true;
                }
                else if((intEndMonth == intRainySeasonEndMonth&& intEndDay <= intRainySeasonEndDate))
                {
                    bEndDateflag =	true;
                }
            }
            int intEnddateYear = calendarEndDate.get(Calendar.YEAR);
            //end Date falls into Rainy and Same Year
            int intCounter=0;
            if(bEndDateflag && (intCurrentYear == intEnddateYear))
            {
                //Check Start date falls In between Rainy Days to calculate only rainy days fall between start and rainy End date
                boolean bStartDateflag = false;
                int intStartMonth = calendarStartDate.get(Calendar.MONTH);
                int intStartDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
                intStartMonth++;   
                if( intStartMonth == intRainySeasonStartMonth )
                {
                  if(intStartDay >= intRainySeasonStartDate )
                    {
                        bStartDateflag =	true;				
                    }
                }
                else if(intStartMonth>intRainySeasonStartMonth)
                {
                    if( intStartMonth < intRainySeasonEndMonth)
                    {
                      bStartDateflag =	true;
                    }
                    else if( (intEndMonth == intRainySeasonEndMonth&& intEndDay <= intRainySeasonEndDate))
                    {
                        bStartDateflag =	true;
                    }
                }
                if(bStartDateflag)
                {						
                    //start date and end date falling into Rainy days same year..Start date will be one day after rainy end date
                    calendarStartDate.set(intCurrentYear,intRainySeasonEndMonth-1 ,intRainySeasonEndDate+1,0,0);
                    calendarStartDate.add(Calendar.DATE, intDuration);
                    long lDateInMillis = calendarStartDate.getTimeInMillis();
                    strNewEnddate = Long.toString(lDateInMillis);
                }
                else
                {
                intDuration = intDuration+intRainyDuration;
                calendarStartDate.add(Calendar.DATE, intDuration);
                long lDateInMillis = calendarStartDate.getTimeInMillis();
                strNewEnddate = Long.toString(lDateInMillis);
                }
            }
            //end Date falls into Rainy and Future Year
            else if(bEndDateflag && (intCurrentYear < intEnddateYear))
            {
                //Check Start date falls In between Rainy Days to calculate only rainy days fall between start and rainy End date
                boolean bStartDateflag = false;
                int intStartMonth = calendarStartDate.get(Calendar.MONTH);
                int intStartDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
                intStartMonth++;   
                if( intStartMonth == intRainySeasonStartMonth )
                {
                  if(intStartDay >= intRainySeasonStartDate )
                    {
                        bStartDateflag =	true;				
                    }
                }
                else if(intStartMonth>intRainySeasonStartMonth)
                {
                    if( intStartMonth < intRainySeasonEndMonth)
                    {
                      bStartDateflag =	true;
                    }
                    else if( (intEndMonth == intRainySeasonEndMonth&& intEndDay <= intRainySeasonEndDate))
                    {
                        bStartDateflag =	true;
                    }
                }
                if(bStartDateflag)
                {
                    intCounter=0;
                    //Calculating the Number of days falling into rainy days
                    while(calendarStartDate.before(calendarRainyEndDate))
                    {
                        int intMonth = calendarStartDate.get(Calendar.MONTH);
                        int intDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
                        intMonth++;                       
                        if(intMonth == intRainySeasonStartMonth)
                        {
                            if(intDay >= intRainySeasonStartDate)
                            {
                                intCounter++;								
                            }
                        }
                        //Check if it in equal to rainy Start date and Month
                        else if(intMonth>intRainySeasonStartMonth)
                        {
                            if(intMonth < intRainySeasonEndMonth)
                            {
                                intCounter++;
                            }
                            else if(intMonth == intRainySeasonEndMonth&&intDay <= intRainySeasonEndDate)
                            {
                                intCounter++;
                            }
                        }
                        calendarStartDate.add(Calendar.DATE, 1);
                    }//End of Calculation
                }
                else
                {
                    intCounter=0;
                    //Calculating the Number of days falling into rainy days
                    while(calendarStartDate.before(calendarRainyEndDate))
                    {
                        int intMonth = calendarStartDate.get(Calendar.MONTH);
                        int intDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
            
                        intMonth++;                       
                        if(intMonth == intRainySeasonStartMonth)
                        {
                            if(intDay >= intRainySeasonStartDate)
                            {
                                intCounter++;								
                            }
                        }
                        //Check if it in equal to rainy Start date and Month
                        else if(intMonth>intRainySeasonStartMonth)
                        {
                            if(intMonth < intRainySeasonEndMonth)
                            {
                                intCounter++;
                            }
                            else if(intMonth == intRainySeasonEndMonth&&intDay <= intRainySeasonEndDate)
                            {
                                intCounter++;
                            }
                        }
                        calendarStartDate.add(Calendar.DATE, 1);
                    }//End of Calculation
                }//
                calendarStartDate.set(intCurrentYear,intRainySeasonEndMonth-1 ,intRainySeasonEndDate+1,0,0);
                int intSecondDuration =0;
                while(calendarStartDate.before(calendarEndDate))
                {
                    int intMonth = calendarStartDate.get(Calendar.MONTH);
                    int intDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
                    
                    intMonth++;                       
                    if(intMonth == intRainySeasonStartMonth)
                    {
                        if(intDay >= intRainySeasonStartDate)
                        {
                            intSecondDuration++;								
                        }
                    }
                    //Check if it in equal to rainy Start date and Month
                    else if(intMonth>intRainySeasonStartMonth)
                    {
                        if(intMonth < intRainySeasonEndMonth)
                        {
                            intSecondDuration++;
                        }
                        else if(intMonth == intRainySeasonEndMonth&&intDay <= intRainySeasonEndDate)
                        {
                            intSecondDuration++;
                        }
                    }
                    calendarStartDate.add(Calendar.DATE, 1);
                }//End of Calculation
                calendarStartDate.setTimeInMillis(lWOStartDate);
                //set Second Duration 92 days if it less than rainy season days since it will fall into rainy day again it should be considered as 92 days
                if(intSecondDuration>0 && intSecondDuration< intRainyDuration )
                {
                    intSecondDuration = intRainyDuration;
                }
                //intcounter is from current year and add rainy days since End date falls into rainy days
                intDuration = intDuration+intCounter+intSecondDuration;
                calendarStartDate.add(Calendar.DATE, intDuration);
                long lDateInMillis = calendarStartDate.getTimeInMillis();
                Calendar calendarNewEndDate = Calendar.getInstance();
                calendarNewEndDate.setTimeInMillis(lDateInMillis);
                
                //Check the New End date falls again between Rainy Days
                intEndMonth = calendarNewEndDate.get(Calendar.MONTH);
                intEndDay = calendarNewEndDate.get(Calendar.DAY_OF_MONTH);					
                                        
                boolean bNewEndDateflag = false;          
                intEndMonth++;
                if(intEndMonth == intRainySeasonStartMonth)
                {
                  if(intEndDay >=intRainySeasonStartDate)
                    {
                        bNewEndDateflag =	true;				
                    }
                }
                else if(intEndMonth>intRainySeasonStartMonth)
                {
                    if(intEndMonth < intRainySeasonEndMonth)
                    {
                      bNewEndDateflag =	true;
                    }
                    else if((intEndMonth == intRainySeasonEndMonth&& intEndDay <= intRainySeasonEndDate))
                    {
                        bNewEndDateflag =	true;
                    }
                }
                if(bNewEndDateflag)
                {
                 //Add Rainy days Again to Final Duration
                 intDuration =intDuration+intRainyDuration;
                 calendarStartDate.setTimeInMillis(lWOStartDate);
                 calendarStartDate.add(Calendar.DATE, intDuration);
                 lDateInMillis = calendarStartDate.getTimeInMillis();
                 strNewEnddate = Long.toString(lDateInMillis);
                }
                else
                {
                    strNewEnddate = Long.toString(lDateInMillis);
                }
                
            }
            //if End date falls after Rainy Days
            else
            {
                 intCounter=0;
                    //Calculating the Number of days falling into rainy days
                    while(calendarStartDate.before(calendarEndDate))
                    {
                        int intMonth = calendarStartDate.get(Calendar.MONTH);
                        int intDay = calendarStartDate.get(Calendar.DAY_OF_MONTH);
                        
                intMonth++;                       
                if(intMonth==intRainySeasonStartMonth)
                {
                    if(intDay>=intRainySeasonStartDate)
                    {
                        intCounter++;								
                    }
                }
                //Check if it in equal to rainy Start date and Month
                else if(intMonth>intRainySeasonStartMonth)
                {
                    if(intMonth<intRainySeasonEndMonth)
                    {
                        intCounter++;
                    }
                    else if(intMonth==intRainySeasonEndMonth&&intDay<=intRainySeasonEndDate)
                    {
                        intCounter++;
                    }
                }
                calendarStartDate.add(Calendar.DATE, 1);
                    }//End of Calculation
                calendarStartDate.setTimeInMillis(lWOStartDate);
                intDuration = intDuration+intCounter;
                calendarStartDate.add(Calendar.DATE, intDuration);
                long lDateInMillis = calendarStartDate.getTimeInMillis();
                Calendar calendarNewEndDate = Calendar.getInstance();
                calendarNewEndDate.setTimeInMillis(lDateInMillis);
                
                //Calculate Rainy days between End Date and New End date
                   int intNewCounter = 0;
                   while(calendarEndDate.before(calendarNewEndDate))
                    {
                         int intMonth = calendarEndDate.get(Calendar.MONTH);
                         int intDay = calendarEndDate.get(Calendar.DAY_OF_MONTH);
                        
                        intMonth++;                       
                        if(intMonth == intRainySeasonStartMonth)
                        {
                            if(intDay >= intRainySeasonStartDate)
                            {
                                intNewCounter++;								
                            }
                        }
                        //Check if it in equal to rainy Start date and Month
                        else if(intMonth>intRainySeasonStartMonth)
                        {
                            if(intMonth < intRainySeasonEndMonth)
                            {
                                intNewCounter++;
                            }
                            else if(intMonth == intRainySeasonEndMonth&&intDay <= intRainySeasonEndDate)
                            {
                                intNewCounter++;
                            }
                        }
                        calendarEndDate.add(Calendar.DATE, 1);
                    }//End of Calculation
                if(intNewCounter>0 && intNewCounter< intRainyDuration )
                {
                    intNewCounter = intRainyDuration;
                }
                //set new End date since New count 
                calendarStartDate.setTimeInMillis(lWOStartDate);
                intDuration = intDuration+intNewCounter;
                calendarStartDate.add(Calendar.DATE, intDuration);
                lDateInMillis = calendarStartDate.getTimeInMillis();
                calendarNewEndDate.setTimeInMillis(lDateInMillis);
                
                //Check the New End date falls again between Rainy Days
                intEndMonth = calendarNewEndDate.get(Calendar.MONTH);
                intEndDay = calendarNewEndDate.get(Calendar.DAY_OF_MONTH);					
                                        
                boolean bNewEndDateflag = false;          
                intEndMonth++;
                if(intEndMonth == intRainySeasonStartMonth)
                {
                  if(intEndDay >=intRainySeasonStartDate)
                    {
                        bNewEndDateflag =	true;				
                    }
                }
                else if(intEndMonth>intRainySeasonStartMonth)
                {
                    if(intEndMonth < intRainySeasonEndMonth)
                    {
                      bNewEndDateflag =	true;
                    }
                    else if((intEndMonth == intRainySeasonEndMonth&& intEndDay <= intRainySeasonEndDate))
                    {
                        bNewEndDateflag =	true;
                    }
                }
                if(bNewEndDateflag)
                {
                 //Add Rainy days Again to Final Duration
                 intDuration =intDuration+intRainyDuration;
                 calendarStartDate.setTimeInMillis(lWOStartDate);
                 calendarStartDate.add(Calendar.DATE, intDuration);
                 lDateInMillis = calendarStartDate.getTimeInMillis();
                 strNewEnddate = Long.toString(lDateInMillis);
                }
                else
                {
                    strNewEnddate = Long.toString(lDateInMillis);
                }
                
            }
                
                                        
        }
        catch (Exception e) {
            throw new Exception("Error in Method SetWOCompleteDateRainySeason:"+e.getMessage());
        }  
                         
        return strNewEnddate;
    }

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeHostCompany(Context context, String[] args) throws Exception {
        StringList slReturnValue = new StringList();
        try {
            String strHostId = Company.getHostCompany(context);
            slReturnValue.add(strHostId);
        } catch (Exception e) {
            throw new FrameworkException(e.getMessage());
        }
        return slReturnValue;
    }

        /** 
     * Method will update the Work Order deatils in Edit mode
     * @param args Packed program and request maps for the table
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map updateWorkOrderDetails(Context context, String[] args) throws Exception {
        HashMap mapReturnMap = new HashMap();
        try {
            HashMap programMap 		= (HashMap) JPO.unpackArgs(args);
            HashMap paramMap		= (HashMap) programMap.get("paramMap");
            HashMap requestMap 		= (HashMap) programMap.get("requestMap");
            String strWOOID 		= (String) paramMap.get("objectId");
            //String strCompletionDate	 	= (String) requestMap.get("CompletionDueDate1");
            String strValueOfContract 		= (String) requestMap.get("ValueOfContract1");
            //Date dCompletionDate = new Date(strCompletionDate);
            //long lCompletionTime = dCompletionDate.getTime();

            SimpleDateFormat mxDateFrmt = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
            //String strFormattedCompletionDate = mxDateFrmt.format(dCompletionDate);
         
            if (UIUtil.isNotNullAndNotEmpty(strWOOID))
            {

                HashMap<String, String> hashMapAttributes = new HashMap<>(2);
                hashMapAttributes.put(ATTRIBUTE_WMS_VALUE_OF_CONTRACT, strValueOfContract);
                //hashMapAttributes.put(ATTRIBUTE_COMPLETION_DUE_DATE, strFormattedCompletionDate);
                DomainObject domObjWO = DomainObject.newInstance(context, strWOOID);
                domObjWO.setAttributeValues(context, hashMapAttributes);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return mapReturnMap;
    }

    /**
     * Method to connect the Work Order and MBE
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed program and request maps from the command
     * @throws Exception if the operation fails
     * @author WMS
     * @since 418
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public void connectWorkOrderAndContractor(Context context,String[]args)throws Exception
    {
        try{
        Map programMap                 = JPO.unpackArgs(args);
        HashMap paramMap             = (HashMap) programMap.get("paramMap");
        HashMap requestMap      = (HashMap)programMap.get("requestMap");
        String strWorkOrderOID             = (String) paramMap.get("objectId");
        String strNewSuppOID             = (String) paramMap.get("New OID");

        
        if(UIUtil.isNotNullAndNotEmpty(strWorkOrderOID) )
            {
                 DomainObject domObjWO = DomainObject.newInstance(context, strWorkOrderOID);
                StringList strListBusSelects     = new StringList(1);
                StringList strListRelSelects     = new StringList(1);
                strListRelSelects.add(DomainRelationship.SELECT_ID);
                 String strOldRelID = "";
                MapList mapListOrg = domObjWO.getRelatedObjects(context, // matrix context
                		RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR, // relationship pattern
                        DomainConstants.TYPE_COMPANY, // type pattern
                        strListBusSelects, // object selects
                        strListRelSelects, // relationship selects
                        true, // to direction
                        true, // from direction
                        (short) 1, // recursion level
                        DomainConstants.EMPTY_STRING, // object where clause
                        DomainConstants.EMPTY_STRING, // relationship where clause
                        0);
                
                if(mapListOrg.size() > 0)
                {
                            Iterator<Map<String,String>> itemsIterator = mapListOrg.iterator();
                            Map<String,String> itemsmapData;
                            DomainObject domObjItem  = DomainObject.newInstance(context);
                                
                            while(itemsIterator.hasNext()){
                                itemsmapData = itemsIterator.next();
                                strOldRelID = itemsmapData.get(DomainRelationship.SELECT_ID);
                            }
                            if(UIUtil.isNotNullAndNotEmpty(strOldRelID) && UIUtil.isNotNullAndNotEmpty(strNewSuppOID))
                            {
                                 //Disconnect Old Supplier and Connect New Supplier
                                 DomainRelationship.disconnect(context,strOldRelID); 	 
                                 DomainRelationship.connect(context, strNewSuppOID,RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR, strWorkOrderOID, true);
                            }
                            if(UIUtil.isNotNullAndNotEmpty(strOldRelID) && UIUtil.isNullOrEmpty(strNewSuppOID))
                            {
                                //Disconnect current Supplier
                                DomainRelationship.disconnect(context,strOldRelID); 	 
                            }
                }
                //Connect New Supplier 
                else{
                    if(UIUtil.isNotNullAndNotEmpty(strNewSuppOID))
                    {
                        DomainRelationship.connect(context, strNewSuppOID,RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR, strWorkOrderOID, true);
                    }
                 }
            }
                
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }

/**
* Returns ematrix Date display format for Forms
* @author WMS
* @throws Exception 
* @since 418
		**/
public String getEmatrixDateFormat(Context context, String[] args)
		throws Exception {
				String strFormattedDate = "";
				try{ 
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				String strDateForFormat = (String)programMap.get("Date");

				Calendar calendarDate = Calendar.getInstance();
				long lDateForFormat=Long.parseLong(strDateForFormat);
				calendarDate.setTimeInMillis(lDateForFormat);
				SimpleDateFormat simpleDateFormatMatrix = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),context.getLocale());
				strFormattedDate = simpleDateFormatMatrix.format(calendarDate.getTime());

				Date dStartDate=simpleDateFormatMatrix.parse(strFormattedDate);	
				DateFormat dateFormat = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),context.getLocale());
				strFormattedDate = dateFormat.format(dStartDate);

				}
				catch(Exception exception) 
				{
				exception.printStackTrace();
				throw exception;
				} 
				return strFormattedDate;
				}


	/*
	 * 
	 *     a.	If Ringi Approval Date is more than 7 days
	 *      before today’s date or workorder creation date,
	 *       then send mail to Owner of the project connected to workorder. 
	 *       Message should be a Workorder is being activated with name <> for contractor <> 
	 *       under Project <Project Name>. Ringi Approval date for this workorder is more than 7 days earlier than today. 
	 */
	
	public void sendRingiSevenDaysApprovalReminder(Context context,String[] args){
		
		try{
             String sWOId = args[0];
		     String sName= args[1];
		     DomainObject domWO=DomainObject.newInstance(context, sWOId);
             MailUtil.setTreeMenuName(context, "type_WMSWorkOrder");
         
		     DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
			 DateTimeFormatter dateStringFormatMMM = DateTimeFormat.forPattern("MMM dd-yyyy");

		 	 StringList slBusSel=new StringList(2);
			 slBusSel.add("attribute["+ATTRIBUTE_MSIL_RINGI_APPROVAL_DATE+"]");
			 slBusSel.add("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.owner");
             slBusSel.add("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.name");
			 slBusSel.add("attribute["+ATTRIBUTE_WMS_WORK_ORDER_DATE+"]");
             slBusSel.add("to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
			 Map mInfo =  domWO.getInfo(context, slBusSel);
		     String strProjectOwner =(String)mInfo.get("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.owner");
			 String strRingiApprovDate = (String)mInfo.get("attribute["+ATTRIBUTE_MSIL_RINGI_APPROVAL_DATE+"]");
			 String strWorkOrderCreationDate = (String)mInfo.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_DATE+"]");
             String strProjectName =(String)mInfo.get("to["+RELATIONSHIP_WMS_PROJECT_WORK_ORDER+"].from.name");
             String sContractor =(String)mInfo.get("to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
			 LocalDate dRingiApproDate = dateStringFormat.parseLocalDate(strRingiApprovDate);
			 LocalDate dWorkOrderCreationDate = dateStringFormat.parseLocalDate(strWorkOrderCreationDate);
			 LocalDate today=new LocalDate();
			 LocalDate dCompareWith =new LocalDate();
			 if(dRingiApproDate.compareTo(today)<0){
				 dCompareWith=today;
			 } else if(dRingiApproDate.compareTo(dWorkOrderCreationDate)<0){
				 dCompareWith=dWorkOrderCreationDate;
			 }
		      int iDiffDays= (Days.daysBetween(dRingiApproDate, dCompareWith)).getDays();
			      if(iDiffDays>7){
                  StringBuilder sbSubject = new StringBuilder();
		         sbSubject.append("Work Order : ").append(sName).append(" Ringi Approval Date is older than 7 days");
				 StringBuilder sbMessage = new StringBuilder();
			     sbMessage.append(" Workorder is being activated with name ");
	             sbMessage.append(sName);
				 sbMessage.append( ".Created on :-  ");
				 sbMessage.append(dateStringFormatMMM.print(dWorkOrderCreationDate));
	             sbMessage.append(" for contractor : ").append(sContractor);
	             sbMessage.append(",under the project: ").append(strProjectName); 
				 sbMessage.append(".Ringi Approval date [").append(dateStringFormatMMM.print(dRingiApproDate));
  		         sbMessage.append(" ] for this workorder is more than 7 days earlier than today." );
				 
	            StringList slObjectIdList = new StringList(1);
	            slObjectIdList.addElement(sWOId);
          
		    	  StringList personList = new StringList();
	              personList.add(strProjectOwner);
	              MailUtil.sendMessage(context,personList,null,null,sbSubject.toString(),sbMessage.toString(),slObjectIdList);
					    	  
		      }
		       
		 	
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
	}
public int triggerEnsureApprovalTemplate(Context context, String[] args) throws Exception {
    	String STR_ERROR_MSG_EMPTY="Purpose is empty for Approval Template ";
    	String STR_ERROR_MSG="Please add Approval Template for value";
    	try{
    	
		Properties _classCurrencyConfig  =   new Properties();
		Page page= new Page("MSILAccessRights");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "MSILAccessRights"));
		String strApprovapPurposeList = _classCurrencyConfig.getProperty("WMSCIVIL.WorkOrder.ApprovalTemplatePurpose");
		StringList slApprovalReasons = FrameworkUtil.split(strApprovapPurposeList, ",");//mxAttr.getChoices(context,"WMSApprovalTemplatePurpose");
    	Map<String,Integer> mCounter=new HashMap<String,Integer>();
    	Iterator itr = slApprovalReasons.iterator();
    	String strRange=DomainConstants.EMPTY_STRING;
    	while(itr.hasNext()){
    	strRange=(String)itr.next();
    	if(!strRange.isEmpty()){
    	mCounter.put(strRange, 0);
    	}

    	}
    		//now get all approval template with

    	StringList strListBusSelects = new StringList(DomainConstants.SELECT_ID);
    	strListBusSelects.add(DomainConstants.SELECT_NAME);
    	StringList strListRelSelects = new StringList(DomainConstants.SELECT_ID);
    	strListRelSelects.add("attribute["+ATTRIBUTE_WMS_APPROVAL_TEMPLATE_PURPOSE+"]");
    	DomainObject domWorkOrder = DomainObject.newInstance(context,args[0]);
    	MapList mlApprovalTemplates= domWorkOrder.getRelatedObjects(context, // matrix context
			    	RELATIONSHIP_WMS_WORK_ORDER_APPROVAL_TEMPLATE, // relationship pattern
			    	DomainConstants.TYPE_ROUTE_TEMPLATE, // type pattern // Approval Template type
			    	strListBusSelects, // object selects
			    	strListRelSelects, // relationship selects
			    	false, // to direction
			    	true, // from direction
			    	(short) 1, // recursion level
			    	DomainConstants.EMPTY_STRING, // object where clause
			    	DomainConstants.EMPTY_STRING, // relationship where clause
			    	0);
    	
           if(mlApprovalTemplates.size()==0) {
        	   emxContextUtil_mxJPO.mqlNotice(context, "No Approval Templates are added,Please add approval Templates for below purpose; \n1.MB Approval\n2.AMB Approval \n3.Material Bill Approval" );
        	   return 1;
           }else {	
		    	Iterator<Map> itrApprovalTemplate = mlApprovalTemplates.iterator();
		    	String strPurpose=DomainConstants.EMPTY_STRING;
		    	String strName=DomainConstants.EMPTY_STRING;
		    	while(itrApprovalTemplate.hasNext()){
			    	Map m=itrApprovalTemplate.next();
			    	strPurpose = (String)m.get("attribute["+ATTRIBUTE_WMS_APPROVAL_TEMPLATE_PURPOSE+"]");
			    	strName = (String)m.get(DomainConstants.SELECT_NAME);
			    	if(strPurpose.isEmpty()){
			    	emxContextUtil_mxJPO.mqlNotice(context, STR_ERROR_MSG_EMPTY+" "+strName);
			    	return 1; 
			    	}
			    	// Start counting...
			    	int iCount = mCounter.get(strPurpose);
			    	mCounter.put(strPurpose, iCount+1);
			    	
		    	}
		    	java.util.Set keys = mCounter.keySet();
		    	Iterator itrKeys = keys.iterator();
		    	while(itrKeys.hasNext()){
				    	strName=(String)itrKeys.next();
				    	int i =mCounter.get(strName);
				    	if(i==0){
				    	     emxContextUtil_mxJPO.mqlNotice(context, "Approval Template with Purpose "+strName+" is not added");
				    	return 1; 
				    	}
		       	}
		    	
		    	/*  Please check the SDP is connected  added for WorkOrder Vendor Document Management */
		    	
		    	String strIsSDPConnected = domWorkOrder.getInfo(context, "from["+RELATIONSHIP_WMS_WORK_ORDER_SDP+"]");
		    	if(strIsSDPConnected.equalsIgnoreCase("False")) {
		    		 emxContextUtil_mxJPO.mqlNotice(context, "Contractor document list is not defined,Please add Contractor document list" );
		    		 return 1; 
		     	}
     }
    	}catch(Exception e){
    		throw e;
    	}
    	return 0;
    }
    /**
	 * This program is to exclude already connected Line Item Templates.
	 * @param context
	 * @param args
	 * @return 
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedApprovalTemplates(Context context, String[] args) throws Exception{
	
		StringList excludeOids = new StringList();
		try {
			String whereExpression = "to[" +RELATIONSHIP_WMS_WORK_ORDER_APPROVAL_TEMPLATE + "].from.id!=''";
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList rATList = DomainObject.findObjects(context, DomainConstants.TYPE_ROUTE_TEMPLATE, DomainConstants.QUERY_WILDCARD, DomainConstants.QUERY_WILDCARD, null,
					null, whereExpression, true, new StringList(DomainConstants.SELECT_ID));
			if(rATList!=null) {
				for(Object info: rATList) {
					excludeOids.add((String)((Map)info).get(DomainConstants.SELECT_ID));
				}
			}
		}catch (Exception ex) {
			throw ex;// TODO: handle exception
		}
		return excludeOids;
	}
	/**
	 * This Load Apprval Template table content.
	 * @param context
	 * @param args
	 * @return 
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getApprovalTemplate(Context context, String[] args) throws Exception {
		MapList mlApprovalTemplate=new MapList();
		try {
			StringList strListBusSelects     = new StringList(1);
            strListBusSelects.add(DomainConstants.SELECT_ID);
            strListBusSelects.add(DomainConstants.SELECT_NAME);
           
            StringList strListRelSelects     = new StringList(1);
            strListRelSelects.add(DomainRelationship.SELECT_ID);
            strListRelSelects.add("attribute["+ATTRIBUTE_WMS_APPROVAL_TEMPLATE_PURPOSE+"]");
			Map mInputMap = (Map) JPO.unpackArgs(args);
			String strObjId = (String)mInputMap.get("objectId");
			DomainObject domObjWO = DomainObject.newInstance(context, strObjId);
			mlApprovalTemplate=domObjWO.getRelatedObjects(context, // matrix context
					 RELATIONSHIP_WMS_WORK_ORDER_APPROVAL_TEMPLATE, // relationship pattern
                     DomainConstants.TYPE_ROUTE_TEMPLATE, // type pattern
                    strListBusSelects, // object selects
                    strListRelSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    DomainConstants.EMPTY_STRING, // object where clause
                    DomainConstants.EMPTY_STRING, // relationship where clause
                    0);
		}catch (Exception ex) {
			throw ex;// TODO: handle exception
		}
		return mlApprovalTemplate;
	}

/** Trigger method on Workorder create promote action, stamps ownership of Person's organization connected to WorkOrder
 * so only WorkOrder only those Work Order can be visible to it 
 * mod bus 20220.13561.10676.36117 add ownership "RG Constructions" "Common Space";
 * @param context
 * @param args
 * @throws Exception
 */
	

public int triggerUpdateObjectOwnership(Context context,String[] args) throws Exception{
	
	try {
	
	
	    String strWOId = args[0];
	    DomainObject domWO=DomainObject.newInstance(context, strWOId);
	    MQLCommand mql=new MQLCommand();
	
	    String strSupplierOrg = domWO.getInfo(context, "to["+RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR+"].from.name");
	     ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
	     mql.executeCommand(context, "mod bus $1 add ownership $2 $3", strWOId, strSupplierOrg,"GLOBAL");
	    ContextUtil.popContext(context);
	 
	  }catch(Exception e) {
		  ContextUtil.popContext(context);
		 e.printStackTrace();
	 }
	   return 0;
 }

	public StringList getProjectNameForWorkorder(Context context, String[] args)throws Exception{
		StringList slNameList = new StringList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			Map tempMap = null;
			DomainObject doObject = null;
			ContextUtil.pushContext(context);
			if(mlObjectList != null && mlObjectList.isEmpty() == false) {
				String strType = DomainConstants.EMPTY_STRING;
				String strWorkOrderID = DomainConstants.EMPTY_STRING;
				String strProjectName = DomainConstants.EMPTY_STRING;
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strType = (String)tempMap.get(DomainObject.SELECT_TYPE);
					strWorkOrderID = (String)tempMap.get(DomainObject.SELECT_ID);
					if(UIUtil.isNotNullAndNotEmpty(strWorkOrderID) && (strType.equals(TYPE_WMS_WORK_ORDER))) {
						doObject = new DomainObject(strWorkOrderID);
						strProjectName = (String)doObject.getInfo(context,"to[WMSProjectWorkOrder].from.name");
						slNameList.add(strProjectName);
					} else {
						slNameList.add("");
					}
				}
			}
			ContextUtil.popContext(context);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slNameList;

	}
	
	
	public StringList getProjectOwnerNameForWorkorder(Context context, String[] args)throws Exception{
		StringList slNameList = new StringList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)programMap.get("objectList");
			Map tempMap = null;
			DomainObject doObject = null;
			ContextUtil.pushContext(context);
			if(mlObjectList != null && mlObjectList.isEmpty() == false) {
				String strType = DomainConstants.EMPTY_STRING;
				String strWorkOrderID = DomainConstants.EMPTY_STRING;
				String strProjectOwnerName = DomainConstants.EMPTY_STRING;
				for(int i=0;i<mlObjectList.size();i++) {
					tempMap = (Map)mlObjectList.get(i);
					strType = (String)tempMap.get(DomainObject.SELECT_TYPE);
					strWorkOrderID = (String)tempMap.get(DomainObject.SELECT_ID);
					if(UIUtil.isNotNullAndNotEmpty(strWorkOrderID) && (strType.equals(TYPE_WMS_WORK_ORDER))) {
						doObject = new DomainObject(strWorkOrderID);
						strProjectOwnerName = (String)doObject.getInfo(context,"to[WMSProjectWorkOrder].from.owner");
						strProjectOwnerName = MqlUtil.mqlCommand(context, "print person $1 select $2 dump",strProjectOwnerName,"fullname");
						slNameList.add(strProjectOwnerName);
					} else {
						slNameList.add("");
					}
				}
			}
			ContextUtil.popContext(context);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return slNameList;
	}
 
	//Code added for B3-Actions - end
	/**
	* getWorkOrderLink Method to get Work order link
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args Packed program and request maps from the command or form or table
	* @return vColumn Vector containing the SOR Object List
	* @throws Exception if the operation fails
	* @author WMS
	* @since 418
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Vector getWorkOrderLink (Context context, String[] args) throws Exception
	{
		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		MapList mlSOR = (MapList) programMap.get("objectList");
		Vector vColumn =new Vector(mlSOR.size());
		String strObjectId= DomainConstants.EMPTY_STRING;
		String strObjectType= DomainConstants.EMPTY_STRING;
		DomainObject doTaskID = null;
		try
		{
			for(int i=0;i<mlSOR.size();i++) {
				StringBuilder sbItemLink=new StringBuilder();
				Map m =(Map)mlSOR.get(i);
				strObjectId =(String) m.get(DomainConstants.SELECT_ID);
				strObjectType =(String) m.get(DomainConstants.SELECT_TYPE);
				
				if(strObjectType.equals(TYPE_WMS_MEASUREMENT_TASK)) {
					doTaskID = DomainObject.newInstance(context,strObjectId);
					strObjectId = doTaskID.getInfo(context, "from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id");
				}
				
				if(strObjectType.equals(TYPE_LINE_ITEM)) {
					doTaskID = DomainObject.newInstance(context,strObjectId);
					strObjectId = doTaskID.getInfo(context, "to["+RELATIONSHIP_WMS_SOR_TO_LINEITEM+"].from.id");
				}
				sbItemLink.append("<a title='View DSR' href ='javascript:showModalDialog(\"");
				sbItemLink.append("../common/emxIndentedTable.jsp?table=WMSWOSummary&amp;Export=true&amp;expandLevelFilter=true&amp;selection=multiple&amp;header=WMS.Table.Header.WorkOrder&amp;suiteKey=WMS&amp;HelpMarker=emxhelpgatechecklist&amp;program=WMSWorkorder:getAssociatedWorkOrderList&amp;expandProgram=WMSWorkorder:getAssociatedTask&amp;objectId=");
				sbItemLink.append(strObjectId);
				sbItemLink.append("\", \"875\", \"550\", \"false\", \"popup\")'>");
				sbItemLink.append("<img src='../common/images/utilCollaborativeSpace.png'></img>");
				sbItemLink.append("</a>");
				
				vColumn.add(sbItemLink.toString());
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return vColumn; 
	}
	
	/**
	* getAssociatedWorkOrderList Method to get the connected Work Orders from the SORChapter
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args Packed program and request maps from the command or form or table
	* @return mlWorkOrdersList MapList containing the Work Order connected to SOR 
	* @throws Exception if the operation fails
	* @author WMS
	* @since 418
	*/
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAssociatedWorkOrderList(Context context,String[] args) throws Exception
	{
		String strSORID = null;
		String strWorkOrderID = null;
		String strWorkOrderType = null;
		String strProjName = null;
		String strWorkOrderDate = null;
		
		MapList mlWorkOrdersList = new MapList();
		MapList mlWorkOrders = new MapList();
		
		Map mapObject = null;
		Map mInfo = null;
		Map mapTaskObject = null;
		
		DomainObject doSORID = null;
		DomainObject doWorkOrderOID  = null;
		
		StringBuffer sbTypeSelect = new StringBuffer(10);
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_TASK);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_SEGMENT);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_BOOK);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_WORK_ORDER);
		
		StringBuffer sbRelSelect = new StringBuffer(10);
		sbRelSelect.append(RELATIONSHIP_CLASSIFIED_ITEM);
		sbRelSelect.append(",");
		sbRelSelect.append(RELATIONSHIP_WMS_TASK_SOR);
		sbRelSelect.append(",");
		sbRelSelect.append(RELATIONSHIP_BILL_OF_QUANTITY);
		
		StringList slBusSelect = new StringList(1);
		slBusSelect.addElement(DomainConstants.SELECT_ID);
		slBusSelect.addElement(DomainConstants.SELECT_TYPE);
		slBusSelect.addElement("attribute["+ATTRIBUTE_WMS_WORK_ORDER_DATE+"]");
		
		matrix.util.Pattern filterTypePattern = new matrix.util.Pattern(TYPE_WMS_WORK_ORDER);
		matrix.util.Pattern filterRelPattern  = new matrix.util.Pattern(RELATIONSHIP_BILL_OF_QUANTITY);
		
		try {
				HashMap programMap   = (HashMap) JPO.unpackArgs(args);
				strSORID = (String) programMap.get("objectId");
				doSORID = DomainObject.newInstance(context,strSORID);
				
				mlWorkOrders = doSORID.getRelatedObjects(context,							//context
															sbRelSelect.toString(),			//relationshipPattern
															sbTypeSelect.toString(),		//typePattern 
															slBusSelect,					//objectSelects 
															null,							//relationshipSelects 
															true,							//getTo 
															true,							//getFrom 
															(short)0,						//recurseToLevel 
															null,							//objectWhere 
															null,							//relationshipWhere 
															filterTypePattern,				//TYPE_WMS_WORK_ORDER includeType
															filterRelPattern,				//RELATIONSHIP_BILL_OF_QUANTITY includeRelationship 
															null);							//includeMap
				
				//sort date in descending oder
				mlWorkOrders.sort("attribute["+ATTRIBUTE_WMS_WORK_ORDER_DATE+"]", "descending", "date");
				
				if((null!=mlWorkOrders)&&(mlWorkOrders.size()>0)){
				Iterator itr = mlWorkOrders.iterator();
					while(itr.hasNext()) {
						Map mFinalResult = new HashMap();
						mapObject = (Map)itr.next();
						strWorkOrderID = (String)mapObject.get(DomainConstants.SELECT_ID);
						strWorkOrderType = (String)mapObject.get(DomainConstants.SELECT_TYPE);
						strWorkOrderDate = (String)mapObject.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_DATE+"]");
						mFinalResult.put("id", strWorkOrderID);
						mFinalResult.put("type", strWorkOrderType);
						mlWorkOrdersList.add(mFinalResult);
					}
				}
			}
			catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return mlWorkOrdersList;
	}
	
	/**
	 * getAssociatedTask Method to get the connected tasks from the SOR
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Packed program and request maps from the command or form or table
	 * @return mlTaskList MapList containing the Work Order connected to SOR 
	 * @throws Exception if the operation fails
	 * @author WMS
	 * @since 418
	 */
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAssociatedTask (Context context, String[] args) throws Exception
	{
		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		String strSORID = (String) programMap.get("parentId");
		String strWorkOrderID = (String) programMap.get("objectId");
		
		DomainObject doSORID  = null;
		DomainObject doWorkOrderID  = null;
		
		doSORID = DomainObject.newInstance(context,strSORID);
		StringList slTaskIDs = doSORID.getInfoList(context, "to["+RELATIONSHIP_WMS_TASK_SOR+"].from.id");
		
		doWorkOrderID = DomainObject.newInstance(context,strWorkOrderID);
		MapList mlTaskList = new MapList();
		MapList mlGetTaskList = new MapList();
		Map mapTaskObject = null;
		String strTaskID = null;
		String strTaskName = null;
		String strTaskType = null;
		String strTaskRate = null;
		
		StringBuffer sbTypeSelect = new StringBuffer(10);
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_TASK);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_SEGMENT);
		sbTypeSelect.append(",");
		sbTypeSelect.append(TYPE_WMS_MEASUREMENT_BOOK);
		
		StringBuffer sbRelSelect = new StringBuffer(10);
		sbRelSelect.append(RELATIONSHIP_BILL_OF_QUANTITY);
		
		StringList slBusSelect = new StringList(1);
		slBusSelect.addElement(DomainConstants.SELECT_ID);
		slBusSelect.addElement(DomainConstants.SELECT_NAME);
		slBusSelect.addElement(DomainConstants.SELECT_TYPE);
		slBusSelect.addElement("attribute[" + ATTRIBUTE_WMS_REDUCED_SOR_RATE + "]");
		
		matrix.util.Pattern filterTypePattern = new matrix.util.Pattern(TYPE_WMS_MEASUREMENT_TASK);
		
		try {
			mlGetTaskList = doWorkOrderID.getRelatedObjects(context,						//context
															sbRelSelect.toString(),			//relationshipPattern
															sbTypeSelect.toString(),		//typePattern 
															slBusSelect,					//objectSelects 
															null,							//relationshipSelects 
															false,							//getTo 
															true,							//getFrom 
															(short)3,						//recurseToLevel 
															null,							//objectWhere 
															null,							//relationshipWhere 
															filterTypePattern,				//includeType
															null,							//includeRelationship 
															null);							//includeMap
			
			if((null!=mlGetTaskList)&&(mlGetTaskList.size()>0)){
			Iterator itr = mlGetTaskList.iterator();
				while(itr.hasNext()) {
					Map mFinalResult = new HashMap();
					mapTaskObject = (Map)itr.next();
					
					strTaskID = (String)mapTaskObject.get(DomainConstants.SELECT_ID);
					strTaskName = (String)mapTaskObject.get(DomainConstants.SELECT_NAME);
					strTaskType = (String)mapTaskObject.get(DomainConstants.SELECT_TYPE);
					strTaskRate = (String)mapTaskObject.get("attribute[" + ATTRIBUTE_WMS_REDUCED_SOR_RATE + "]");
					
					if(slTaskIDs.contains(strTaskID)){
						mFinalResult.put("id", strTaskID);
						mFinalResult.put("name", strTaskName);
						mFinalResult.put("type", strTaskType);
						mFinalResult.put("attribute[WMSReducedSORRate]", strTaskRate);
						mlTaskList.add(mFinalResult);
					}
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return mlTaskList;
	}
	
	/**
	* getProjectRFQ Method to get the connected RFQ from the Project
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args Packed program and request maps from the command or form or table
	* @return mlWorkOrdersList MapList containing the Work Order connected to SOR 
	* @throws Exception if the operation fails
	* @author WMS
	* @since 418
	*/
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getProjectRFQ(Context context, String[] args) throws Exception
	{
		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		String strProjectID = null;
		DomainObject doProjectID  = null;
		
		strProjectID = (String) programMap.get("objectId");
		doProjectID = DomainObject.newInstance(context,strProjectID);
		
		StringList slProjectRFQ = new StringList(1);
		
		MapList mlTaskList = new MapList();
		MapList mlGetTaskList = new MapList();
		Map mapTaskObject = null;
		String strTaskID = null;
		String strTaskName = null;
		String strTaskRate = null;
		
		StringList slBusSelect = new StringList(1);
		slBusSelect.addElement(DomainConstants.SELECT_ID);
		slBusSelect.addElement(DomainConstants.SELECT_NAME);
		slBusSelect.addElement(DomainConstants.SELECT_TYPE);
		
		String objectWhere = DomainConstants.SELECT_CURRENT + "== Complete";
		//String objectWhere = "";

		try {
			mlGetTaskList = doProjectID.getRelatedObjects(context,							//context
															RELATIONSHIP_RFQ_PROJECT,		//relationshipPattern
															TYPE_RFQ,						//typePattern 
															slBusSelect,					//objectSelects 
															null,							//relationshipSelects 
															true,							//getTo 
															false,							//getFrom 
															(short)0,						//recurseToLevel 
															objectWhere,					//objectWhere 
															null,							//relationshipWhere 
															null,							//includeType
															null,							//includeRelationship 
															null);							//includeMap 
			
			if((null!=mlGetTaskList)&&(mlGetTaskList.size()>0)){
			Iterator itr = mlGetTaskList.iterator();
				while(itr.hasNext()) {
					Map mFinalResult = new HashMap();
					mapTaskObject = (Map)itr.next();
					
					strTaskID = (String)mapTaskObject.get(DomainConstants.SELECT_ID);
					slProjectRFQ.add(strTaskID);
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		} 
		return slProjectRFQ;
	}
	/** 
	* connectWorkOrderInRFQ Method will connect the Work Order In Projects
	* @param args Packed program and request maps for the table
	* @throws Exception if the operation fails
	* @author CHiPS
	* @since 418
	*/
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map connectWorkOrderInRFQ(Context context, String[] args) throws Exception {
		HashMap mapReturnMap = new HashMap();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		String strWOID = (String) paramMap.get("objectId");
		String strProjectOID = (String) requestMap.get("parentOID");
		String strRFQOID =(String) requestMap.get("NameOfRFQOID");//RFQ
		String strCompletionDate =(String) requestMap.get("CompletionDueDate1");
		String strValueOfContract =(String) requestMap.get("ValueOfContract1");
		Date dCompletionDate = new Date(strCompletionDate);
		long lCompletionTime = dCompletionDate.getTime();
		//use MatrixDateFormat's pattern
		SimpleDateFormat mxDateFrmt = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		String strFormattedCompletionDate = mxDateFrmt.format(dCompletionDate);
		
		try {
			if( UIUtil.isNotNullAndNotEmpty(strWOID))
			{
				HashMap<String,String> hashMapAttributes = new HashMap<>(2);
				hashMapAttributes.put(ATTRIBUTE_WMS_VALUE_OF_CONTRACT, strValueOfContract);
				hashMapAttributes.put(ATTRIBUTE_WMS_COMPLETION_DUE_DATE, strFormattedCompletionDate);
				DomainObject domObjWO = DomainObject.newInstance(context, strWOID);
				domObjWO.setAttributeValues(context, hashMapAttributes);
				 
			}
			if(UIUtil.isNotNullAndNotEmpty(strRFQOID) && UIUtil.isNotNullAndNotEmpty(strWOID))
			{
				DomainRelationship domRel = DomainRelationship.connect(context, DomainObject.newInstance(context, strRFQOID), RELATIONSHIP_WMS_WORKORDER_RFQ, DomainObject.newInstance(context, strWOID));
				//get contractor ID form RFQ
				DomainObject domRFQ = DomainObject.newInstance(context,strRFQOID);
				String ContractorOID = domRFQ.getInfo(context, "from["+RELATIONSHIP_RTS_SUPPLIER+"].to.id");
				
				if(UIUtil.isNotNullAndNotEmpty(ContractorOID) && UIUtil.isNotNullAndNotEmpty(strWOID))
				{
					DomainRelationship domRelTwo = DomainRelationship.connect(context, DomainObject.newInstance(context, ContractorOID), RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR, DomainObject.newInstance(context, strWOID));
				}
				//copy boq from RFQ Line Item 
				createBOQFromRFQLineItem(context,strRFQOID,strWOID);
			}
			if(UIUtil.isNotNullAndNotEmpty(strProjectOID) && UIUtil.isNotNullAndNotEmpty(strWOID))
			{
				DomainRelationship domRelThree = DomainRelationship.connect(context, DomainObject.newInstance(context, strProjectOID), RELATIONSHIP_WMS_PROJECT_WORK_ORDER, DomainObject.newInstance(context, strWOID));
				
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("<mxRoot>");
				strBuffer.append("<action><![CDATA[add]]></action>");
				strBuffer.append("<data status=\"committed\">");
				strBuffer.append("<item oid=\""+strWOID+"\" relId=\""+""+"\" pid=\""+strWOID+"\"  direction=\"from\" />");
				strBuffer.append("</data>");
				strBuffer.append("</mxRoot>");
				mapReturnMap.put("selectedOID", strProjectOID);
				mapReturnMap.put("rows",strBuffer.toString());
				mapReturnMap.put("Insertdata",strBuffer.toString());
				mapReturnMap.put("Action","success");
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
			mapReturnMap.put("Action","Stop");
			mapReturnMap.put("Message",exception.getMessage());
		}
		return mapReturnMap;
	}
	
	
	public void createBOQFromRFQLineItem(Context context,String strRFQID,String strWorkOrderId) throws Exception
	{
		try {
			DomainObject domWorkOrder = DomainObject.newInstance(context, strWorkOrderId);
			String strMBEId= domWorkOrder.getInfo(context, "from[WMSMeasurementBookItems].to.id");
			StringList slBusSelelct =new StringList();
			slBusSelelct.add("attribute["+DomainConstants.ATTRIBUTE_ANNUAL_QUANTITY+"]");
			slBusSelelct.add(DomainConstants.SELECT_ID);
			DomainObject domRFQ=DomainObject.newInstance(context,strRFQID);
			String owner = context.getUser();
			ContextUtil.pushContext(context);
			String strRFQXML= domRFQ.getAttributeValue(context, "RFQ Header XML");
			String strAttributeKey = getAttributeKeyFromRFQ(context,strRFQXML);
			RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
			rts.setId(strRFQID);
			
			StringList busSelects = new StringList();
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_DESCRIPTION);
			busSelects.add(LineItem.SELECT_ENTERED_NAME);
			busSelects.add("attribute[Line Item XML]");
			busSelects.add("attribute[Unit of Measure]");
			busSelects.add(LineItem.SELECT_ANNUAL_QUANTITY);
			busSelects.add("to[WMSSORToLineItem].from.attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]");
			busSelects.add("to[WMSSORToLineItem].from.id");
			busSelects.add("to[WMSSORToLineItem].from.to[WMSSORChapter].from.attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
			MapList allLineItemMapList=rts.getAllLineItems(context, busSelects,null, null, null,false);
			Iterator<Map> itrLi = allLineItemMapList.iterator();
			String strEnteredName= DomainConstants.EMPTY_STRING;
			String strLiXML=DomainConstants.EMPTY_STRING;
			String strQty="0.0";
			String strLiid=DomainConstants.EMPTY_STRING;
			String strRate=DomainConstants.EMPTY_STRING;
			String strDSRTitle=DomainConstants.EMPTY_STRING;
			String strRevision1="1";
			Map mapSegTitle=new HashMap();
			Map mapAttribute=new HashMap();
			String strSegmentId=DomainConstants.EMPTY_STRING;
			String strParentId=strWorkOrderId;
			String strObjName=DomainConstants.EMPTY_STRING;
			String strSORId=DomainConstants.EMPTY_STRING;
			String strDescription=DomainConstants.EMPTY_STRING;
			String strUOM=DomainConstants.EMPTY_STRING;
			DomainObject domObjNewItem= DomainObject.newInstance(context);
			Map mUoM=new HashMap();
			mUoM.put("EA (each)", "each");
			while(itrLi.hasNext()) {
				Map mLineItem = itrLi.next();
				strEnteredName=(String)mLineItem.get(LineItem.SELECT_ENTERED_NAME);
				strDescription=(String)mLineItem.get(DomainConstants.SELECT_DESCRIPTION);
				strQty=(String)mLineItem.get(LineItem.SELECT_ANNUAL_QUANTITY);
				strLiid=(String)mLineItem.get(DomainConstants.SELECT_ID);
				strSORId=(String)mLineItem.get("to[WMSSORToLineItem].from.id");
				strLiXML=(String)mLineItem.get("attribute[Line Item XML]");
				strUOM=(String)mLineItem.get("attribute[Unit of Measure]");
				strRate = getLineItemRate(context,strLiXML, strAttributeKey);
				mapAttribute.put("WMSTotalQuantity", strQty);
				mapAttribute.put("Title", strEnteredName);
				strParentId=strWorkOrderId;
				if(mUoM.containsKey(strUOM)) {
					strUOM=(String) mUoM.get(strUOM);
				}
				mapAttribute.put("WMSUnitOfMeasure", strUOM);
					mapAttribute.put(ATTRIBUTE_WMS_REDUCED_SOR_RATE, strRate);
					strDSRTitle=(String)mLineItem.get("to[WMSSORToLineItem].from.to[WMSSORChapter].from.attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
					if(UIUtil.isNotNullAndNotEmpty(strDSRTitle)&&!mapSegTitle.containsKey(strDSRTitle))
					{
						strParentId = strMBEId;
						strSegmentId = FrameworkUtil.autoName(context, "type_WMSSegment", "policy_WMSMeasurementItem");
						DomainObject domSegment = DomainObject.newInstance(context,strSegmentId);
						domSegment.connect(context,new RelationshipType(RELATIONSHIP_BILL_OF_QUANTITY),false, DomainObject.newInstance(context,strParentId));
						domSegment.setAttributeValue(context,"Title", strDSRTitle);
						 mapSegTitle.put(strDSRTitle, strSegmentId);
						//alreadyAddedSegment.add(ItemCode);
					}
					if(mapSegTitle.containsKey(strDSRTitle)) {
						strParentId = (String) mapSegTitle.get(strDSRTitle);
					}
					strObjName = FrameworkUtil.autoName(context,
														"type_WMSMeasurementTask", 
														DomainConstants.EMPTY_STRING,
														DomainConstants.EMPTY_STRING, 
														DomainConstants.EMPTY_STRING, 
														DomainConstants.EMPTY_STRING, 
														true,
														false);
					domObjNewItem.createAndConnect(context, 
													TYPE_WMS_MEASUREMENT_TASK, 
													strObjName, 
													strRevision1,
													POLICY_WMS_MEASUREMENT_ITEM, 
													context.getVault().getName(), 
													RELATIONSHIP_BILL_OF_QUANTITY, 
													DomainObject.newInstance(context,strParentId), 
													true);
					if(strSORId!=null&&!strSORId.isEmpty()) {
						domObjNewItem.connect(context,new RelationshipType(RELATIONSHIP_WMS_TASK_SOR),true, DomainObject.newInstance(context,strSORId));
					}
					domObjNewItem.setDescription(context, strDescription);
					domObjNewItem.setOwner(context, owner);
					domObjNewItem.setAttributeValues(context, mapAttribute);  
			}
	
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally {
		ContextUtil.popContext(context);
		}
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
			Map mAttribute =new HashMap();
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
						mAttribute.put(strAGName, strAgKey+"::"+strKey);
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