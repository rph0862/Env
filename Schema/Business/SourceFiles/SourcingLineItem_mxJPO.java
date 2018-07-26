/*
 * (c) Dassault Systemes, 1993 - 2010.  All rights reserved
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.sourcing.LineItem;
import com.matrixone.apps.sourcing.RequestToSupplier;
import com.matrixone.apps.sourcing.util.AttributeMaster;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

import java.text.*;

/**
 * The <code>LineItem</code> class contains code for the "Line Item" business type
 *
 * @version SC 9.5.Rossini.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class SourcingLineItem_mxJPO extends SourcingLineItemBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since SC 9.5.Rossini.0
     */
	private int counter=0;
    public SourcingLineItem_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
    
    /**
     * Displays Dynamic Column to show Attribute Group and UDA added at Line Item
     *
     * @param context the eMatrix <code>Context</code> object
	 * @param args holds packed HashMap containing objectList, requestMap, _SRCShowAGs, parentOID, RequestValuesMap
	 * @return MapList containing  dynamic column relevent for the Line Item
     * @throws Exception if the operation fails
     */
    public MapList getDynamicLineItemAGAttributesForBuyer(Context context, String[] args) throws Exception
    {
        MapList returnMap = new MapList();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap)programMap.get("requestMap");
            String _SRCShowAGs=(String)paramList.get("_SRCShowAGs");

            if("false".equals(_SRCShowAGs))
                return returnMap;

            RequestToSupplier rtsObj = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
            String rtsId=(String)paramList.get("parentOID");
            rtsObj.setId(rtsId);
            String currentState=rtsObj.getInfo(context,DomainConstants.SELECT_CURRENT);
            boolean editable=false;
            //Start -Modified to fix IR-150218V6R2013
            //Added a condition to check if pending RFQ is in Exists state and if yes granting edit access.
            String POLICY_RTS_PENDING_VERSION_EXISTS = PropertyUtil.getSchemaProperty("policy",POLICY_RTS_PENDING_VERSION,"state_Exists");
            if(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_STARTED.equals(currentState) || currentState.equals(POLICY_RTS_PENDING_VERSION_EXISTS))
                editable=true;
            //End -Modified to fix IR-150218V6R2013

            Map RequestValuesMap = (Map)paramList.get("RequestValuesMap");
            StringList OriginatedList = new StringList();
            StringList busSelects = new StringList();
            StringList relSelects = new StringList();
            int increment = 1;


            Map objectIdMap;
            for(int i = 0; i < objectList.size(); i++)
            {
                objectIdMap = (Map) objectList.get(i);
                getLineItemSpecificDynamicColumnsForAG(context,rtsObj,returnMap,editable);
            }
        }

        catch(Exception  ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            return returnMap;
        }
    }


    private void getLineItemSpecificDynamicColumnsForAG(Context context,RequestToSupplier rts,MapList returnMap,boolean editable)
    throws Exception
    {
        Document rfqDocument=rts.getRFQHeaderDataDocument(context);
        Element rfqDocumentRoot = rfqDocument.getRootElement();

        int ct123=1;
        SelectList supplierSellist = new SelectList(1);
        supplierSellist.add(rts.SELECT_ID);
        supplierSellist.add(rts.SELECT_NAME);
        MapList totalresultList = rts.getAllLineItems(context, supplierSellist, null, false);
        
       //Added for MSIL---Start
        SelectList slObjectSelect = new SelectList();
        slObjectSelect.add(rts.SELECT_ID);
        slObjectSelect.add(rts.SELECT_NAME);
        slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
        SelectList slRelSelect = new SelectList();
        slRelSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
        ContextUtil.pushContext(context);
        MapList mlTRSTemplates = rts.getRelatedObjects(context,DomainConstants.RELATIONSHIP_DEFAULT_LINE_ITEM_ATTRIBUTE_GROUP,DomainConstants.TYPE_LINE_ITEM_TEMPLATE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
        ContextUtil.popContext(context);        
        mlTRSTemplates.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
        
    	String strOpenStatus = rts.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
		String strCurrent = rts.getInfo(context, DomainObject.SELECT_CURRENT);
		boolean bQuoteOpen = false;
		if(UIUtil.isNotNullAndNotEmpty(strOpenStatus) && "Complete".equals(strOpenStatus) && (strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE)))
			bQuoteOpen = true;

		
      //Added for MSIL----End
        Iterator iter=totalresultList.iterator();

        StringList ProcessedLIAgList = new StringList();
        String previousGroup[]=new String[1];

        Document liDoc = null;
        Element lineItemRoot = null;
        LineItem li = null;
        String lineItemId=null;
        Map lineItemMap=null;
        MapList allLineItemAgList=null;
        Iterator allLineItemAgListItr=null;
        Map attributeGroupMap=null;
        while(iter.hasNext())
        {
            lineItemMap=(Map)iter.next();
            lineItemRoot=null;

            lineItemId=(String)lineItemMap.get(DomainConstants.SELECT_ID);
            li = new LineItem(lineItemId);
			allLineItemAgList = li.getAttributeGroupsForExport(context, rts.getId());
		
		  //Added for MSIL---Start
			MapList mlTempList = allLineItemAgList;
            int iSize=mlTRSTemplates.size();
	        for(int iCount=0; iCount<iSize;iCount++){
        	   Map hmMap = (Map) mlTRSTemplates.get(iCount);
        	   String strSeq = (String)hmMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
        	   String strVisibility = (String)hmMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
        	   String strName = (String)hmMap.get(DomainConstants.SELECT_NAME);
               int jSize=mlTempList.size();
               for(int jCount=0; jCount<jSize;jCount++){
            	   Map attributeGroupMap1 = (Map) mlTempList.get(jCount);
            	   if((context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER) ||  context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER)) && "Commercial".equals(strVisibility)){
            		   allLineItemAgList.remove(jCount);
            	   }
            	   if(strName.equals((attributeGroupMap1.get("name").toString())))
            	   {
            		   attributeGroupMap1.put("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]",strSeq);
            		   break;
            	   }
               }
	        }
            allLineItemAgList.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
          //Added for MSIL---End
            
            allLineItemAgListItr = allLineItemAgList.iterator();
            while (allLineItemAgListItr.hasNext())
            {
                attributeGroupMap = (Map) allLineItemAgListItr.next();

                //Get the key,name,role & scope of the Attribute Group.
                String agkey = attributeGroupMap.get("key").toString();
                String AttrGroupName = attributeGroupMap.get("name").toString();
                String AttrGroupRole = attributeGroupMap.get("role").toString();
                String AttrGroupScope = attributeGroupMap.get("scope").toString();
                String AttrGroupLevel =  attributeGroupMap.get("AGLevel").toString();

                String attributeGroupSupplierName = "";

                if("role_Buyer".equals(AttrGroupRole))
                {
                    if(AttrGroupScope.contains("- All Line Item"))
                    {
                        attributeGroupSupplierName = "Buyer";

                        if(lineItemRoot == null)
                        {
                            liDoc = li.getLineItemDataDocument(context);
                            lineItemRoot = liDoc.getRootElement();
                        }
                        //processAG
                        processAGForLineItem(context,li,lineItemRoot,agkey,
                                AttrGroupName,AttrGroupLevel,lineItemId,ProcessedLIAgList,attributeGroupSupplierName,
                                previousGroup,returnMap,AttrGroupRole,AttrGroupScope,null,ct123,rfqDocumentRoot,editable);

                    }// End of -->if("role_Buyer".equals(AttrGroupRole))
                    else if(AttrGroupScope.contains("- Specific Line Item"))
                    {
                        MapList LiSuppliersList = li.getSuppliersFromXML(context);
                        if(lineItemRoot == null)
                        {
                            liDoc = li.getLineItemDataDocument(context);
                            lineItemRoot = liDoc.getRootElement();
                        }

                        Iterator LiSuppliersListItr = LiSuppliersList.iterator();
                        while(LiSuppliersListItr.hasNext())
                        {
                            HashMap CurrSupplier = (HashMap) LiSuppliersListItr.next();
                            String CurrSupplierName =(String) CurrSupplier.get("name");

                            processAGForLineItem(context,li,lineItemRoot,agkey,
                                    AttrGroupName,AttrGroupLevel,lineItemId,ProcessedLIAgList,CurrSupplierName,
                                    previousGroup,returnMap,AttrGroupRole,AttrGroupScope,CurrSupplierName,ct123,rfqDocumentRoot,editable);

                        }//end of -- while(LiSuppliersListItr.hasNext())
                    }// end of --  else if("role_Supplier".equals(AttrGroupRole))
                }//end of if("role_Buyer".equals(AttrGroupRole))
            }// end of --> while (allLineItemAgListItr.hasNext())
        }
    }
    /**
     *  process the AttributeG for the Line Item and build the dynamic Column for the same
     *
     * @param context
     * @param li
     * @param lineItemRoot
     * @param agkey
     * @param AttrGroupName
     * @param AttrGroupLevel
     * @param lineItemId
     * @param ProcessedLIAgList
     * @param attributeGroupSupplierName
     * @param previousGroup
     * @param returnMap
     * @param AttrGroupRole
     * @param AttrGroupScope
     * @param CurrSupplierName
     * @param ct123
     * @throws Exception
     * @exclude
     */
    private void processAGForLineItem(Context context,LineItem li,Element lineItemRoot,String agkey,
            String AttrGroupName,String AttrGroupLevel,String lineItemId,StringList ProcessedLIAgList,String attributeGroupSupplierName,
            String []previousGroup,MapList returnMap,String AttrGroupRole,String AttrGroupScope,String CurrSupplierName,
            int ct123,Element rfqDocumentRoot,boolean editable)
    throws Exception
    {

        //Get all the Attribute Names & Flags in the Attribute Group
        //MapList AttrNamelist = li.getAttributeFlags(context,lineItemRoot,agkey,rtsid);
        MapList AttrNamelist = li.getAttributeFlags(context,lineItemRoot,agkey,rfqDocumentRoot);

        String sLanguage=context.getSession().getLanguage();
        Iterator AttrNamelistItr = AttrNamelist.iterator();
        StringList choicesList=null;
        Map attrNamelistMap=null;
        while(AttrNamelistItr.hasNext())
        {
            attrNamelistMap = (Map) AttrNamelistItr.next();
            String agAttrKey = attrNamelistMap.get("key").toString();
            String agAttrRequired = attrNamelistMap.get("req").toString();
            String agAttrSymbolicName = attrNamelistMap.get("name").toString();
            String agAttrSchemaName = PropertyUtil.getSchemaProperty(context,agAttrSymbolicName);

            HashMap attrInfoMap = AttributeMaster.getAttributeInfo(context,agAttrSchemaName,true);

            choicesList=(StringList)attrInfoMap.get("choices");
            String strChoices=null;

            String agAttrType ="";
            if(attrInfoMap.size()>0 && attrInfoMap.containsKey("type"))
            {
                agAttrType = (String) attrInfoMap.get("type");

                if(agAttrType.equals(""))
                    agAttrType = "string";

                if("boolean".equals(agAttrType) && (choicesList==null || choicesList.size()==0))
                {
                    String displayTrue = i18nNow.getRangeI18NString(agAttrSchemaName,"TRUE",sLanguage);
                    String displayFalse = i18nNow.getRangeI18NString(agAttrSchemaName,"FALSE",sLanguage);
                    if(displayTrue!=null && displayFalse!=null)
                    {
                        if(choicesList==null)
                            choicesList=new StringList();
                        choicesList.add(displayTrue);
                        choicesList.add(displayFalse);
                    }


                }
            }
            else
            {
                agAttrType = "string";
            }
            if(choicesList!=null)
                strChoices=FrameworkUtil.join(choicesList, ",");

            DebugUtil.debug("The Type of the attribute "+ agAttrSchemaName  + " is " + agAttrType);
            String TitleListAttr = AttrGroupName + ";" + agAttrSymbolicName + ";"  + agAttrKey + ";" + agkey + ";" + agAttrType + ";" + AttrGroupLevel+ ";"+  attributeGroupSupplierName;
            StringBuffer columnIdInfo = new StringBuffer();
            columnIdInfo=columnIdInfo.append(agkey).append("::").append(agAttrKey);

            if("LI".equals(AttrGroupLevel))
                TitleListAttr+=";"+lineItemId;

            if(!"Buyer".equals(attributeGroupSupplierName))
            {
                columnIdInfo.append("::").append(attributeGroupSupplierName);
            }

            if(!ProcessedLIAgList.contains(TitleListAttr) )
            {
                String groupHeader=null;
                if("role_Buyer".equals(AttrGroupRole))
                {
                    //String title12=attributeGroupSupplierName+ "::" + AttrGroupName + "::" + agAttrSchemaName + "::" + AttrGroupLevel;
                    //String title12=agAttrSchemaName;
                	String title12=i18nNow.getAttributeI18NString(agAttrSchemaName, sLanguage);  //Fix for IR-176090V6R2013x
                    groupHeader=attributeGroupSupplierName+ "::" + AttrGroupName + "::" + AttrGroupLevel;;
                    buildColumnInfo(context,previousGroup[0],attributeGroupSupplierName,AttrGroupName,
                            returnMap,title12,TitleListAttr,strChoices,agAttrType,ProcessedLIAgList,groupHeader,agAttrRequired,columnIdInfo,editable);
                }
                else if(AttrGroupScope.contains("- Specific Line Item"))
                {
                    //String title123=CurrSupplierName+ "::" + AttrGroupName + "::" + agAttrSchemaName + "::" + AttrGroupLevel;
                    String title123=agAttrSchemaName;
                    groupHeader=CurrSupplierName+ "::" + AttrGroupName + "::" + AttrGroupLevel;
                    buildColumnInfo(context,previousGroup[0],attributeGroupSupplierName,AttrGroupName,
                            returnMap,title123,TitleListAttr,strChoices,agAttrType,ProcessedLIAgList,groupHeader,agAttrRequired,columnIdInfo,editable);
                }
                previousGroup[0]=groupHeader;
            }
        }// end of while(AttrNamelistItr.hasNext())
    }
    /**
    *
    * This builds the MapList which will be returned to emxIndentedTable containing the definition of dynamic columns for a given line item
    *
    * @param context
    * @param previousGroup
    * @param attributeGroupSupplierName
    * @param AttrGroupName
    * @param returnMap
    * @param title12
    * @param TitleListAttr
    * @param strChoices
    * @param agAttrType
    * @param ProcessedLIAgList
    * @param groupHeader
    * @param ct123
    * @exclude
    */
   private void buildColumnInfo(Context context,String previousGroup,String attributeGroupSupplierName,String AttrGroupName,
           MapList returnMap,String title12,String TitleListAttr,String strChoices,String agAttrType,
           StringList ProcessedLIAgList,String groupHeader,String agAttrRequired,StringBuffer columnIdInfo,boolean editable)
   {
       if(previousGroup==null || previousGroup.equals(groupHeader)==false)
       {
           counter++;
           HashMap MapUpdated = new HashMap();
           HashMap map123 = new HashMap();
           map123.put("Column Type", "separator");
           map123.put("Editable", "false");
           map123.put("Registered Suite", "Sourcing");
           MapUpdated.put("settings", map123);
           MapUpdated.put("name", "seperator_"+counter);
           returnMap.add(MapUpdated);
       }

       HashMap MapUpdated = new HashMap();
       HashMap map123 = new HashMap();
       map123.put("Column Type", "programHTMLOutput");
       map123.put("Custom_Title", TitleListAttr);
       map123.put("attribute_id", columnIdInfo.toString());
       map123.put("Registered Suite", "Sourcing");
       map123.put("Sortable", "true");
       if(editable)
           map123.put("Editable", "true");
       if("yes".equals(agAttrRequired))
       {
           map123.put("Validate","validateRequired");
       }

       if(strChoices!=null)
       {
           map123.put("Input Type","combobox");
           map123.put("strChoices",strChoices);
           map123.put("Range Function","getCustomAttributeRange");
           map123.put("Range Program","SourcingLineItem");
       }

       if(agAttrType.contains("timestamp"))
       {
           map123.put("Display Format","3");
           map123.put("Display Time","false");
           map123.put("format","date");
           map123.put("Sort Type","date");
       }
       //fix-Start IR-235648V6R2014x
      // else if(agAttrType.contains("real")||agAttrType.contains("integer"))
       else if(agAttrType.contains("integer"))
       	//fix-end IR-235648V6R2014x
       {
           map123.put("Validate","validateInteger");
       }else if(agAttrType.contains("real"))
       {
           map123.put("Validate","validateReal");
       }
         try {
         MSILAccessRights_mxJPO msilObj = new MSILAccessRights_mxJPO(context, new String[] {});
         if(msilObj.isPersonFromCivil(context, new String[] {}));
         map123.put("On Change Handler","CalculateRFQBudget");
         }catch(Exception e) {
        	 
         }

       StringTokenizer columnTitleStrTokenizer = new  StringTokenizer(TitleListAttr,";");

       String AGName       = columnTitleStrTokenizer.nextToken();
       columnTitleStrTokenizer.nextToken();
       columnTitleStrTokenizer.nextToken();
       columnTitleStrTokenizer.nextToken();
       columnTitleStrTokenizer.nextToken();
       String agLevel             = columnTitleStrTokenizer.nextToken();

       map123.put("Style Function", "getCellStyle");
       map123.put("Style Program", "SourcingLineItem");

       map123.put("Edit Access Program", "SourcingLineItem");
       map123.put("Edit Access Function", "getEditableCells");

       if("yes".equalsIgnoreCase(agAttrRequired))//Fix IR-231934V6R2014x
           map123.put("Required", "true");
       else map123.put("Required", "false");

       map123.put("Update Program", "SourcingLineItem");
       map123.put("Update Function", "updateAttributeGroupValues");

       map123.put("Group Header", groupHeader);
       map123.put("function", "getAttributeGroupValueForEachLineItem");
       map123.put("program", "SourcingLineItem");     //The name of the program where the above function exists
       MapUpdated.put("settings", map123);
       MapUpdated.put("label", title12);
       counter++;
       MapUpdated.put("name", "name_"+counter);


       returnMap.add(MapUpdated);
       ProcessedLIAgList.add(TitleListAttr);
   }

   /**
	 * Creates/Updates the Line Item info as part of creation
	 *
	 * @param context the eMatrix <code>Context</code> object
    * @param args holds packed HashMap containing requestMap,paramMap,objectId,Name,Description,LineItemPartOID,RequiredQuantity,RequiredAvailability,RequiredUofM,ReceivingPlant
    * @throws FrameworkException,Exception if the operation fails
    */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createLineItem(Context context,String args[])
	throws FrameworkException, Exception {				//Fix for IR-121407V6R2012x
		ComponentsUtil.checkLicenseReserved(context,"ENO_SRC_TP");

		ContextUtil.startTransaction(context, true);
       LineItem lineItem = (LineItem)DomainObject.newInstance(context, DomainConstants.TYPE_LINE_ITEM, DomainConstants.SOURCING);
		RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		// Get RFQ Id
		String rfqId=(String)requestMap.get("objectId");
		// Get Line Item Id
		String liId=(String)paramMap.get("objectId");
		String lineItemName = (String)requestMap.get("Name");
		String strDescription = (String)requestMap.get("Description");
		String partId         = (String)requestMap.get("LineItemPartOID");
       String quantity = (String)requestMap.get("RequiredQuantity");
       String strRequiredAvailability =  (String)requestMap.get("RequiredAvailability");
       String uom =  (String)requestMap.get("RequiredUofM");
       String recvPlant = (String)requestMap.get("ReceivingPlant");

		DebugUtil.debug("createLineItem >>>> programMap "+programMap);
		DebugUtil.debug("createLineItem >>>> liId "+liId);
		DebugUtil.debug("createLineItem >>>> rfqId "+rfqId);
		DebugUtil.debug("createLineItem >>>> partId "+partId);

		// If value for Part is not chosen then make it as null (the value received from field can be "" or null)
		if ( null == partId || "".equals(partId) )
		     partId = null;
		//Start : To Integrate SRC-PRG

		String partNameInp         = (String)requestMap.get("LineItemPartDisplay");

		partId = ((partNameInp!=null)&&(partNameInp.length()>0)&&(partId!=null)&&(partId.length()>0))?partId:null;

		String taskId         = (String)requestMap.get("LineItemTaskOID");
		String taskNameInp         = (String)requestMap.get("LineItemTaskDisplay");

		taskId = ((taskNameInp!=null)&&(taskNameInp.length()>0)&&(taskId!=null)&&(taskId.length()>0))?taskId:null;

		if((taskId!=null)&&(partId!=null))
		{
			String crtErrorMsg = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", (new Locale(context.getSession().getLanguage())), "emxSourcing.Common.LineItem.Tasks.AssociateValidate");//MULTITENANT UPDATE

			crtErrorMsg = (crtErrorMsg !=null)&&(crtErrorMsg.length()>0)?crtErrorMsg:"Line Item can be associated only to a Part or Task, but not both";

			throw new FrameworkException(crtErrorMsg);

		}

		if((taskId!=null)&&(partId==null))
		{
			partId=taskId;
		}

		//End : To Integrate SRC-PRG

		if( null != liId )
		{
			lineItem.setId(liId);

			String LineItemEnteredName = "from["+rts.RELATIONSHIP_LINE_ITEM+"].to.attribute["+rts.ATTRIBUTE_ENTERED_NAME+"]";
			rts.setId(rfqId);

			// check and set the company key before creating line item
			setCompanyKeyInRPE(context);

			DomainObject person = PersonUtil.getPersonObject(context);
			DebugUtil.debug("createLineItem >>>> person ");
			String companyName = person.getInfo(context, Person.SELECT_COMPANY_NAME);
			DebugUtil.debug("createLineItem >>>> companyName "+companyName);
			String allowDuplicateEnteredName = EnoviaResourceBundle.getProperty(context,"emxSourcing.AllowDuplicateLineItemEnteredName");//MULTITENANT UPDATE

			// throw error if duplicate exists
			if ( !"true".equalsIgnoreCase(allowDuplicateEnteredName))
			{
				DebugUtil.debug("createLineItem >>>> isDuplicate? ");
				StringList liNames = rts.getInfoList(context,LineItemEnteredName);
				if(liNames.contains(lineItemName))
				{
				  String dupErrorMsg = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", (new Locale(context.getSession().getLanguage())), "emxSourcing.LienItem.DuplicateEnteredName");//MULTITENANT UPDATE

				  dupErrorMsg=dupErrorMsg+lineItemName;			//Fix for IR-121407V6R2012x
				  throw new FrameworkException(dupErrorMsg);	//Fix for IR-121407V6R2012x
			    }
			}

			DebugUtil.debug("createLineItem >>>> before deletion ");
			lineItem.deleteObject(context); // need to delete as to create new one with autoname LI
			DebugUtil.debug("createLineItem >>>> after deletion ");

			Map liDataMap = new HashMap();
			/*liDataMap.put(lineItem.ATTRIBUTE_ANNUAL_QUANTITY, quantity);
			liDataMap.put(lineItem.ATTRIBUTE_REQUIRED_AVAILABILITY, strRequiredAvailability);
			liDataMap.put(lineItem.ATTRIBUTE_UNIT_OF_MEASURE, uom);
			liDataMap.put(lineItem.ATTRIBUTE_RECEIVING_PLANT, recvPlant);
			*/

			java.util.HashMap attrMap = new java.util.HashMap();
			
			if((UIUtil.isNullOrEmpty(quantity) || String.valueOf(quantity).equals(0)) && context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER)) {
				quantity = "1";
			}
			attrMap.put(lineItem.ATTRIBUTE_ANNUAL_QUANTITY, quantity);
			attrMap.put(lineItem.ATTRIBUTE_REQUIRED_AVAILABILITY, strRequiredAvailability);
			attrMap.put(lineItem.ATTRIBUTE_UNIT_OF_MEASURE, uom);

			liId = rts.createLineItem(context,lineItemName, partId,rfqId, liDataMap,attrMap, true);
			lineItem.setId(liId);

			DebugUtil.debug("createLineItem >>>> new liId "+liId);

			lineItem.setAttributeValue(context,lineItem.ATTRIBUTE_RECEIVING_PLANT, recvPlant);
			lineItem.setDescription(context,strDescription);
		} // null != liId

       ContextUtil.commitTransaction(context);
       DebugUtil.debug("createLineItem >>>> Successful ");
   }

	private void setCompanyKeyInRPE(Context context)
	throws Exception {
		// This logic has been moved from JSP which set value in session that has been replaced with set/get GlobalRPE
		// Getting Company Key from session
		String companyKey = PropertyUtil.getGlobalRPEValue(context, com.matrixone.apps.common.Person.RPE_COMPANY_KEY);

		// Check Company key is null then get comapny key from admin person
		// and set in session for fututre
		if ( null == companyKey || "".equals(companyKey) )
		{
		  companyKey = com.matrixone.apps.common.Person.getCompanyKey(context);
		  DebugUtil.debug("setCompanyKeyInRPE >>>> companyKey "+companyKey);
		}

		// setting Comapny key as RPE value for triggers
		PropertyUtil.setGlobalRPEValue(context, com.matrixone.apps.common.Person.RPE_COMPANY_KEY, companyKey);
   }
   

	 public boolean showLineItemMenuCommand (matrix.db.Context context, String[] args) throws Exception
	    {
	        boolean showCmd=false;
	        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
	        RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
	        String rtsId = (String)programMap.get("objectId");
	        rts.setId(rtsId);
	        boolean isEditable = false;
	        boolean canAdd=true;
	        StringList selectList = new StringList(10);
	        selectList.add(RequestToSupplier.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
	        selectList.add(RequestToSupplier.SELECT_VAULT);
	        selectList.add(RequestToSupplier.SELECT_PRIMARY_KEY);
	        selectList.add(RequestToSupplier.SELECT_CURRENT);
	        selectList.add("last.id");
	        selectList.add("policy");
	        selectList.add("current.access[fromconnect]");
	        selectList.add("current.access[fromdisconnect]");
	        selectList.add("to["+RequestToSupplier.RELATIONSHIP_PASS_THROUGH_RFQ+"].id");
	        selectList.add("current.access[modify]");  // Split related check

	        Map m = rts.getInfo(context,selectList);

	        String hasPending = (String)m.get(RequestToSupplier.SELECT_RELATIONSHIP_PENDING_VERSION_ID);
	        String currentState = (String)m.get(RequestToSupplier.SELECT_CURRENT);
	        String rtsCompanyId = Company.getCompanyForKey(context,(String)m.get(RequestToSupplier.SELECT_PRIMARY_KEY)).getId();
	        String lastRevId = (String) m.get("last.id");
	        String policy = (String) m.get("policy");
	        boolean hasFromConnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromconnect]"));
	        boolean hasFromDisconnectAccess = "true".equalsIgnoreCase((String)m.get("current.access[fromdisconnect]"));
	        boolean hasModifyAccess = "true".equalsIgnoreCase((String)m.get("current.access[modify]"));  // Split related check

	        String isSetupAsPublicExchange = EnoviaResourceBundle.getProperty(context,"emxSourcing.isSetupAsPublicExchange");//MULTITENANT UPDATE
	        boolean isPrivateExchageSetup = "false".equalsIgnoreCase(isSetupAsPublicExchange);

	        boolean isHostCompany = false;
	        if ( (lastRevId.equals(rtsId)) && ( (currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_STARTED) ||
	                currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_SENT)) &&
	                hasPending == null) || policy.equals(RequestToSupplier.POLICY_RTS_PENDING_VERSION) )
	        {
	            isEditable = true;
	        }


	        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
	        person.setToContext(context);
	        String myCompanyId = person.getInfo(context,person.SELECT_COMPANY_ID);

	        if((Company.getHostCompany(context)).equals(myCompanyId))
	        {
	            isHostCompany = true;
	        }
	        if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_SENT))
	            canAdd=false;



	        String select = "to["+rts.RELATIONSHIP_PASS_THROUGH_RFQ+"].id";
	        String strPassThroughRFQRelId = rts.getInfo(context,select);

	        boolean isPassThroughRFQ = false;
	        if(strPassThroughRFQRelId != null && strPassThroughRFQRelId.length() > 0)
	        {
	            isPassThroughRFQ = true;
	        }
	        /*
	        if(!isPrivateExchageSetup || (isPrivateExchageSetup && (isHostCompany || isPassThroughRFQ))){
	            if(isEditable){
	                if(hasFromConnectAccess && canAdd){
	                    showCmd=true;
	                }

	            }
	        } */

			HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
			String cmdName = (String)settingsMap.get("cmdName");

	        // This check has beeen introduced to enable [Add Split] command
	        if ( (null != cmdName || !"".equals(cmdName)) && "createLineItemSplitCmd".equals(cmdName) )
	        {    if (hasFromConnectAccess && hasModifyAccess && isEditable) // Checks to show split link
	        	 	 showCmd=true;
			} else
	        // This check has beeen introduced to enable [Delete Line Item] command
	        if ( (null != cmdName || !"".equals(cmdName)) && "deleteLineItemCmd".equals(cmdName) )
	        {
		        if ( hasFromDisconnectAccess && isEditable )
	        	 	 showCmd=true;
			} else // This check has beeen introduced to enable [Copy Select To RFQ] command
			if ( (null != cmdName || !"".equals(cmdName)) && "copyLineItemsToRFQCmd".equals(cmdName) )
			{
	        	 if ( myCompanyId.equals(rtsCompanyId)  )
	        	 	  showCmd=true;
			}
			else
			{
				if(!isPrivateExchageSetup || (isPrivateExchageSetup && (isHostCompany || isPassThroughRFQ))){
					if(isEditable){
						if(hasFromConnectAccess && canAdd){
							showCmd=true;
						}

					}
				}
			}
//Modified for MSIL-Start
	        if((null !=cmdName || !"".equals(cmdName)) && "TDREditLineItem".equals(cmdName)) {
	        	if(currentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE))
	        	showCmd = true;
	        }
	      //Modified for MSIL-Start
	        return showCmd;
	    }


	    /**
	     * Pre process to check the state of the RFQ before enabling editing
	     *
	     * @param context the eMatrix <code>Context</code> object
		 * @param args holds packed HashMap containing paramMap, objectId
		 * @return HashMap
	     * @throws Exception if the operation fails
	     */
	     @com.matrixone.apps.framework.ui.PreProcessCallable
	    public HashMap preProcessLineItemEdit (matrix.db.Context context, String[] args) throws Exception
	    {

	        HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        HashMap paramMap = (HashMap) programMap.get("paramMap");
	        String rfqID=(String)paramMap.get("objectId");

	        RequestToSupplier rts = (RequestToSupplier)DomainObject.newInstance(context, DomainConstants.TYPE_REQUEST_TO_SUPPLIER, DomainConstants.SOURCING);
	        rts.setId(rfqID);
	        String rfqCurrentState=rts.getInfo(context,RequestToSupplier.SELECT_CURRENT);

	        HashMap returnMap=new HashMap();

	        if((!rfqCurrentState.equals(STATE_REQUEST_TO_SUPPLIER_EXISTS))&&(!rfqCurrentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_STARTED) && !rfqCurrentState.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE)))//Fix for IR-122412V6R2012x
	        {

	            returnMap.put("Action","STOP");
	            returnMap.put("Message","Not editable");
	            returnMap.put("ObjectList",new MapList());
	        }

	        return returnMap;
	    }

	 
	 
}
