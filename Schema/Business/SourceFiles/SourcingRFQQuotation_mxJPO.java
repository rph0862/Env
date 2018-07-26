/*
 * (c) Dassault Systemes, 1993 - 2017.  All rights reserved
 * 
 * Date        |    Modified By     | Remarks 
 * 12/05/2013  |    DS              | To Set Default Value for "Unit Price" , "Effectivity Date" and "Supplier Resonse" for AL RFQ -- End
 */


import java.lang.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import matrix.db.Context; //Comment by Shailesh - Please remove if not needed
import matrix.db.JPO; //Comment by Shailesh - Please remove if not needed
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.sourcing.RTSQuotation;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.sourcing.AttributeGroup;
import com.matrixone.apps.sourcing.LineItem;
import com.matrixone.apps.sourcing.RequestToSupplier;
import com.matrixone.apps.sourcing.XMLHelper;
import com.matrixone.apps.sourcing.util.SourcingConstants;
import com.matrixone.apps.sourcing.util.SourcingElement;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import java.util.ArrayList;
import matrix.db.MQLCommand;
import matrix.util.SelectList;
import matrix.util.StringList;
public class SourcingRFQQuotation_mxJPO extends SourcingRFQQuotationBase_mxJPO
{

  public SourcingRFQQuotation_mxJPO(Context context, String[] args) throws Exception
  {
    super(context, args);
  }


    /**
     * Reloads the Power view Channel.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed HashMap containing objectId
     * @return Map contains execScript which reloads the page.
     * @throws Exception if the operation fails
     */

   /* public Map reloadUnitPrice(Context  context,  String[]  args)  throws  Exception
    {

        Map returnMap = new HashMap();
        returnMap.put ("Action", "execScript");
        returnMap.put("Message", "{ main:function()  {window.location.href=window.location.href;}}");

        // Added by DS -- 12/05/2013 -- To Set Default Value for "Unit Price" , "Effectivity Date" and "Supplier Resonse" for AL RFQ -- Start

        HashMap programMap = (HashMap) JPO.unpackArgs( args );
        HashMap paramMap = (HashMap) programMap.get( "paramMap" );
        HashMap requestMap = (HashMap) programMap.get( "requestMap" );          
        String quotObjID=(String)requestMap.get("parentOID");          
        RTSQuotation rtsQuot=new RTSQuotation(quotObjID);
        String lineItemId = (String) paramMap.get("objectId");

        if(null != rtsQuot){
            String strMSILRFQTypeAttr   = PropertyUtil.getSchemaProperty("attribute_MSILRFQType");
            String sMSILRFQTypeAttr = rtsQuot.getInfo(context,"to[RFQ Quotation].from.attribute["+strMSILRFQTypeAttr+"]"); 
            MapList supplierLineItemMapList = rtsQuot.getSelectedSupplierLineItem(context,lineItemId);
            Map supplierLineItemMap=null;
            if(supplierLineItemMapList.iterator().hasNext())
                supplierLineItemMap=(Map)supplierLineItemMapList.iterator().next();
            String slikey = "";
            if(supplierLineItemMap !=null){
                slikey =(String)supplierLineItemMap.get("key"); 
            }                       
            System.out.println(" sMSILRFQTypeAttr  SourcingRFQQuotationBase >> reloadUnitPrice() >> "+sMSILRFQTypeAttr);                    
            if("ALRFQ".equals(sMSILRFQTypeAttr)){

                HashMap hashMapValues=new HashMap();
                hashMapValues.put("unitprice","1.0");
                hashMapValues.put("effectivitydate","12/31/2999 12:00:00 AM");
                hashMapValues.put("response","Submit");
                HashMap sliMap=new HashMap();
                sliMap.put(slikey,hashMapValues);
                rtsQuot.setXMLAttributeValues(context,sliMap);                          
            }
        }  

        // Added by DS -- 12/05/2013 -- To Set Default Value for "Unit Price" , "Effectivity Date" and "Supplier Resonse" for AL RFQ -- End

        return returnMap;

    }*/
  //Start UDA
  /**
   * Get additional attributes of the supplier line item.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds a packed HashMap containing ObjectId
   * @return Object of type MapList
   * @throws Exception if the operation fails
   */
	@com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getSLIAdditionalAttributes(Context context, String[] args)
  throws Exception
  {
      MapList totalresultList = new MapList();
      try{
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          String strObjectId   = (String)programMap.get("objectId");
          //Start: Common component conversion:R215
          String quotationId = "";
          String sliKey = "";

          RTSQuotation quotation = (RTSQuotation)DomainObject.newInstance(context, DomainConstants.TYPE_RTS_QUOTATION, DomainConstants.SOURCING);
          StringList combinedIdKey = FrameworkUtil.split(strObjectId, "~");
          if ( combinedIdKey.size() >= 2 )
          {
          	quotationId = (String) combinedIdKey.get(0);
          	sliKey = (String) combinedIdKey.get(1);
          	quotation.setId(quotationId);
          }
          else
          {
          	String relId = (String)programMap.get("relId");
				String relIdArray[] = {relId};
		        StringList selects = new StringList(2);
		        selects.addElement("to.id");	  //Fetches RFQ Quotation ID
		        selects.addElement("from.id");    //Fetches LineItem ID.
				DomainRelationship rel = DomainRelationship.newInstance(context, relId);
	            MapList relInfoList = rel.getInfo(context, relIdArray, selects);
	            Iterator relItr = relInfoList.iterator();
	            Map relInfo = null;
	            if (relItr.hasNext())
	            {
	               relInfo = (Map)relItr.next();
	            }
	            quotationId = (String)relInfo.get("to.id");
	            String strLineId = (String)relInfo.get("from.id");
	            quotation.setId(quotationId);
	            Map supplierLineItemMap = null;
	            MapList supplierLineItemMapList=quotation.getSelectedSupplierLineItem(context,strLineId);
				if(supplierLineItemMapList.iterator().hasNext())
					supplierLineItemMap=(Map)supplierLineItemMapList.iterator().next();
				sliKey = (String)supplierLineItemMap.get("key");
          }
			//End: Common component conversion:R215
          LineItem lineItem = (LineItem)DomainObject.newInstance(context, DomainConstants.TYPE_LINE_ITEM, DomainConstants.SOURCING);

          String liKey=null;
          MapList mapList=quotation.getSLIInfo(context,sliKey);

          String SELECT_SUPPLIER = "from["+quotation.RELATIONSHIP_SUPPLIER_RESPONSE + "].to."+quotation.SELECT_NAME;
          String supplierName=quotation.getInfo(context,SELECT_SUPPLIER);
          if(mapList.iterator().hasNext())
          {
          HashMap hMap=(HashMap)mapList.get(0);
          liKey=(String)hMap.get(SourcingConstants.REF_LINE_ITEM_KEY_ELEMENT_NAME);
          }

          lineItem=quotation.getLineItem(context,liKey);
          String supplierLineItemName = lineItem.getInfo(context,lineItem.SELECT_ENTERED_NAME);
          String quotationCurrent = (String) quotation.getInfo(context,quotation.SELECT_CURRENT);
          String lineItemId=lineItem.getId();
          StringList rtsSelects=new StringList();
          rtsSelects.add(RequestToSupplier.SELECT_ID);
          MapList rtsDetailsMapList = quotation.getRequestToSupplier(context,rtsSelects,null);
          java.util.Hashtable hMap=(java.util.Hashtable)rtsDetailsMapList.iterator().next();
          String rtsId=(String)hMap.get(RequestToSupplier.SELECT_ID);

          boolean hasAccess = false;
          if ( quotationCurrent.equals(quotation.STATE_RTS_QUOTATION_OPEN) )
          {
              hasAccess = quotation.isOwnerCoOwner(context);
          }

          MapList suppLineItemMapList=quotation.getSLIInfo(context,sliKey);
          Map suppLineItemMap=null;
          if(suppLineItemMapList!=null && suppLineItemMapList.size () >0)
              suppLineItemMap=(Map)suppLineItemMapList.get(0);

          if(suppLineItemMap == null)
              throw new FrameworkException("Error:: Supplier Line Item not found");

          String supplierResponse = (String) suppLineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_RESPONSE);
          if(supplierResponse==null || supplierResponse.equals(""))
              supplierResponse="Unassigned";
          boolean isBidding = ("Submit".equals(supplierResponse) || "Unassigned".equals(supplierResponse))?true:false;

          //Added for MSIL---Start
          SelectList slObjectSelect = new SelectList();
          slObjectSelect.add(DomainConstants.SELECT_ID);
          slObjectSelect.add(DomainConstants.SELECT_NAME);
          SelectList slRelSelect = new SelectList();
          slRelSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
          DomainObject doRFQ = DomainObject.newInstance(context, rtsId);
          ContextUtil.pushContext(context);
          MapList mlTRSTemplates = doRFQ.getRelatedObjects(context,DomainConstants.RELATIONSHIP_DEFAULT_LINE_ITEM_ATTRIBUTE_GROUP,DomainConstants.TYPE_LINE_ITEM_TEMPLATE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
          ContextUtil.popContext(context);        
          mlTRSTemplates.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
        //Added for MSIL----End
          
          MapList attributeGroupList = lineItem.getSupplierValidAttributeGroups(context,rtsId,quotationId);
		  //Added for MSIL---Start
          int iSize=mlTRSTemplates.size();
	        for(int iCount=0; iCount<iSize;iCount++){
      	   Map hmMap = (Map) mlTRSTemplates.get(iCount);
      	   String strSeq = (String)hmMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
      	   String strName = (String)hmMap.get(DomainConstants.SELECT_NAME);
             int jSize=attributeGroupList.size();
             for(int jCount=0; jCount<jSize;jCount++){
          	   Map attributeGroupMap1 = (Map) attributeGroupList.get(jCount);
          	   if(strName.equals((attributeGroupMap1.get("name").toString())))
          	   {
          		   attributeGroupMap1.put("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]",strSeq);
          		   break;
          	   }
             }
	        }
	        attributeGroupList.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
        //Added for MSIL---End
          Iterator attributeListItr = attributeGroupList.iterator();
          HashMap addMap = null;

          while(attributeListItr.hasNext()) {
              HashMap attrMap = (HashMap) attributeListItr.next();
              addMap = new HashMap();
              String id = (String) attrMap.get(SourcingElement.COMMON_KEY);
              String user = (String) attrMap.get(SourcingElement.ROLE_ATTRIBUTE_NAME);
              String role = PropertyUtil.getSchemaProperty(context, user);
              String type = (String) attrMap.get(SourcingElement.TYPE_ATTRIBUTE_NAME);
              String status = (String)attrMap.get("STATUS");
              AttributeGroup attributeGroup = (AttributeGroup)DomainObject.newInstance(context, DomainConstants.TYPE_ATTRIBUTE_GROUP, DomainConstants.SOURCING);
              MQLCommand mqlCommand = new MQLCommand();
              String command = "print type '" + type + "' select description dump";
              //Start - To cleanup deprecated method
              //String description = attributeGroup.mqlCommand(context, mqlCommand, command);
              String description = MqlUtil.mqlCommand(context, mqlCommand, command);
              //End - To cleanup deprecated method

              //Build the map for display href - Start
              HashMap displayMap = new HashMap();
              displayMap.put(SourcingConstants.ATTRIBUTE_ID, id);
              displayMap.put("sliName", supplierLineItemName);
              displayMap.put(SourcingConstants.TYPE_ATTRIBUTE_NAME, type);
              displayMap.put(SourcingConstants.ROLE_ATTRIBUTE_NAME, user);
              displayMap.put("lineItemId", lineItemId);
              displayMap.put("rtsId", rtsId);
              displayMap.put(SourcingConstants.COMPANY_NAME, supplierName);
              //Build the map for display href - End
              addMap.put("group", SourcingConstants.ATTRIBUTE_GROUP_ELEMENT_NAME);
              addMap.put(SourcingConstants.ATTRIBUTE_NAME, (String) attrMap.get(SourcingElement.COMMON_NAME));
              addMap.put("viewMap", displayMap);
              addMap.put(SourcingConstants.ROLE_ATTRIBUTE_NAME, role);
              addMap.put(SourcingConstants.SLI_DESCRIPTION, description);
              addMap.put("STATUS", status);
              //Build editDetailsMap - Start
              HashMap editMap = null;
              if ( hasAccess && isBidding) {
                  editMap = new HashMap();
                  editMap.put(SourcingConstants.ATTRIBUTE_ID, id);
                  editMap.put("sliName", supplierLineItemName);
                  editMap.put(SourcingConstants.ATTRIBUTE_NAME, (String)attrMap.get(SourcingElement.COMMON_NAME));
                  editMap.put(SourcingConstants.ROLE_ATTRIBUTE_NAME, user);
                  editMap.put("lineItemId", lineItemId);
                  editMap.put("rtsId", rtsId);
                  editMap.put(SourcingConstants.COMPANY_NAME, supplierName);
                  editMap.put("quotId", quotationId);
                  editMap.put("sliKey", sliKey);
              }
              //Build editDetailsMap - End
              addMap.put("editMap", editMap);
              totalresultList.add(addMap);
          }

          //Start UDA
          MapList sliUDAs = lineItem.getSLIUDAs(context, rtsId, quotationId);
          Iterator sliUDAsItr = sliUDAs.iterator();
          while(sliUDAsItr.hasNext()) {
              HashMap udaElemMap = (HashMap) sliUDAsItr.next();
              udaElemMap.put("group", SourcingConstants.UDA_ELEMENT_NAME);
              //Build the map for display href - Start
              HashMap displayMap = new HashMap();
              displayMap.put("sliKey", sliKey);
              displayMap.put(SourcingConstants.ATTRIBUTE_NAME, supplierLineItemName);
              displayMap.put("quotationId", quotationId);
              displayMap.put("lineItemId", lineItemId);
              //Build the map for display href - End
              udaElemMap.put("viewMap", displayMap);
              //Build editDetailsMap - Start
              HashMap editMap = null;
              if ( hasAccess && isBidding) {
                  editMap = new HashMap();
                  editMap.put("sliKey", sliKey);
                  editMap.put(SourcingConstants.ATTRIBUTE_NAME, supplierLineItemName);
                  editMap.put("lineItemId", lineItemId);
                  editMap.put("quotId", quotationId);
              }
              //Build editDetailsMap - End
              udaElemMap.put("editMap", editMap);
              totalresultList.add(udaElemMap);
          }
          //End UDA

      }catch(Exception e) {
          e.printStackTrace();
      }

      return totalresultList;
  }
  	/**
	 * Check Trigger Method to check Mandatory Attributes of RFQ Quotation and Supplier Line Items while promoting from Open to Review State
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds an object id of RFQ Quotation
	 * @return 0 if success, else returns 1
	 * @throws Exception
	 */
	public int checkForMandatoryAttributes(Context context, String[] args) throws Exception {
		String objectId = args[0];
		Map lineItemMap;
		String currency = "";
		String unitprice = "";
		String effectivedate = "";
		String lineItemName = "";
		String supResponse = "";
		String quotedQuantity = "";
		Float fUnitPrice;
		int returnValue=0;
		String commentsMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.EditQuotation.CommentsMessage");
		String liAttrMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.RTSPromote.LineItemAttributeAlert");
		String liUnitPrice = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.QuotationDetail.LineItemUnitPrice");
		String liCurrency = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.Common.Currency");
		String liEffDate = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.Common.EffectivityDate");
		String liQuotedQuantity = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource",
				(new Locale(context.getSession().getLanguage())), "emxSourcing.QuotationDetail.LineItemQuotedQty");
		String ATTRIBUTE_COMMENTS=PropertyUtil.getSchemaProperty(context, "attribute_Comments");

		RTSQuotation rtsQuotation = new RTSQuotation(objectId);
		String strComments = rtsQuotation.getInfo(context, "attribute["+ATTRIBUTE_COMMENTS+"]");
		if (strComments == null || "null".equalsIgnoreCase(strComments) || "".equals(strComments)) {
			emxContextUtil_mxJPO.mqlNotice(context, commentsMessage);
			return 1;
		}

		MapList lineItemsList = rtsQuotation.getAllSupplierLineItems(context);
		String strRoundType = (String)rtsQuotation.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
		//Modification for MSIL-Start

		for (Object object : lineItemsList) {
			lineItemMap = (Map)object;
			currency = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_CURRENCY);
			unitprice = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_UNIT_PRICE);
			effectivedate = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_EFFECTIVITY_DATE);
			lineItemName = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_NAME);
			supResponse = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_RESPONSE);
			quotedQuantity = (String) lineItemMap.get(SourcingConstants.SLI_ATTRIBUTE_QUANTITY);

			if (!"None".equals(supResponse)){
				if(!"RM".equalsIgnoreCase(strRoundType) && !"Commercial".equalsIgnoreCase(strRoundType) && !"Technical".equalsIgnoreCase(strRoundType) && !"Civil".equals(strRoundType)) {
					if (UIUtil.isNullOrEmpty(unitprice)){
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liUnitPrice);
						returnValue=1;
					}else if (Float.parseFloat(unitprice) == 0) {
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liUnitPrice);
						returnValue=1;
					} 
				}
				if(!"RM".equalsIgnoreCase(strRoundType)) {
					if ("Unassigned".equals(currency)){
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liCurrency);
						returnValue=1;
					}
				}
				if(!"RM".equalsIgnoreCase(strRoundType) && !"Commercial".equalsIgnoreCase(strRoundType) && !"Technical".equalsIgnoreCase(strRoundType) && !"Civil".equals(strRoundType)) {
					if (UIUtil.isNullOrEmpty(effectivedate) ){
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liEffDate);
						returnValue=1;
					}
				}
				if(!"RM".equalsIgnoreCase(strRoundType) && !"Commercial".equalsIgnoreCase(strRoundType) && !"Technical".equalsIgnoreCase(strRoundType) && !"Civil".equals(strRoundType)) {
					if (UIUtil.isNullOrEmpty(quotedQuantity)){
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liQuotedQuantity);
						returnValue=1;
					} else if (Integer.parseInt(quotedQuantity) == 0){
						emxContextUtil_mxJPO.mqlNotice(context, liAttrMessage + "\\n" + lineItemName + "-" + liQuotedQuantity);
						returnValue=1;
					}
				}
			}
		}

		//Modification for MSIL-End
		return returnValue;
	}
	
	
	
	/**
	 * Reloads the Power view Channel.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing objectId
	 * @return Map contains execScript which reloads the page.
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map reloadUnitPrice(Context  context,  String[]  args)  throws  Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String quotationId = (String) requestMap.get("objectId");
		RTSQuotation rtsQuot = new RTSQuotation(quotationId);
		Document sliDoc = rtsQuot.getSupplierLineItemDataDocument(context);
		HashMap lineiteminfo = rtsQuot.getLineItemInfo(context);
		Element sliRoot = sliDoc.getRootElement();
		List sliElemList = XMLHelper.findAllSLIElements(SourcingElement.NAMESPACE_PRE_FIX, sliRoot);
		Document doc = (Document) programMap.get("XMLDoc");
		UITableIndented uiti = new UITableIndented();
		com.matrixone.jdom.Element rootElement = doc.getRootElement();
		HashMap sliMap = new HashMap();
		java.util.List objList = rootElement.getChildren("object");
		Iterator objItr = objList.iterator();
		String strRequiredQty = "1";
		String strRoundType = rtsQuot.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_ROUND_TYPE+"].value");
		while (objItr.hasNext()) {
			com.matrixone.jdom.Element eleChild = (com.matrixone.jdom.Element) objItr.next();
			HashMap hashMapValues = new HashMap();
			String sObjectId = (String) eleChild.getAttributeValue("objectId");
			LineItem liObj = new LineItem(sObjectId);
			HashMap liMap = null;
			Element liElem = null;
			for (int j = 0; j < sliElemList.size(); j++) {
				liElem = (Element) sliElemList.get(j);
				String key = liElem.getAttributeValue(SourcingConstants.REF_LINE_ITEM_KEY_ELEMENT_NAME);
				Hashtable m1 = (Hashtable) lineiteminfo.get(key);
				String id = (String) m1.get(SELECT_ID);
				if (!sObjectId.contains(id)) {
					continue;
				}
				liMap = new HashMap();
				XMLHelper.getXMLAttributeValues(liElem, liMap);
				break;
			}
			String slikey = (String) liMap.get(SourcingConstants.ATTRIBUTE_KEY);
			java.util.List columList = (java.util.List) eleChild.getChildren("column");
			Iterator colItr = columList.iterator();
			
			strRequiredQty = (String)liObj.getAttributeValue(context, ATTRIBUTE_ANNUAL_QUANTITY);

			while (colItr.hasNext()) {
				Element eleAttrib = (Element) colItr.next();
				String isEdited = eleAttrib.getAttributeValue("edited");
				String attrName = eleAttrib.getAttributeValue("name");
				String attrValue = eleAttrib.getText();
				if (UIUtil.isNotNullAndNotEmpty(isEdited) && "true".equals(isEdited)) {
					if (attrName.equals("quotedUnitPrice")) {
						attrName = SourcingConstants.SLI_ATTRIBUTE_UNIT_PRICE;
					} else if (attrName.equals("QuotedCurrency")) {
						attrName = SourcingConstants.SLI_ATTRIBUTE_CURRENCY;
					} else if (attrName.equals("QuotedQuantity")) {
						attrName = SourcingConstants.SLI_ATTRIBUTE_QUANTITY;
					} else if (attrName.equals("QuotedUOM")) {
						attrName = SourcingConstants.SLI_ATTRIBUTE_UOM;
					} else if (attrName.equals("EffectiveDate")) {
						attrName = SourcingConstants.SLI_ATTRIBUTE_EFFECTIVITY_DATE;
					}

					hashMapValues.put(attrName, attrValue);
					
					if("RM".equalsIgnoreCase(strRoundType)) {
						hashMapValues.put("quantity", "1");
						hashMapValues.put("response","Submit");
					}else if("Technical".equalsIgnoreCase(strRoundType) || "Commercial".equalsIgnoreCase(strRoundType)) {
						if(UIUtil.isNullOrEmpty(strRequiredQty) || strRequiredQty == "0") {
							strRequiredQty = "1";
						}
						hashMapValues.put("quantity", strRequiredQty);
						hashMapValues.put("response","Submit");
					}
				}
			}
			sliMap.put(slikey, hashMapValues);
		}
		rtsQuot.setXMLAttributeValues(context, sliMap);

		Map returnMap = new HashMap();
		returnMap.put("Action", "execScript");
		returnMap.put("Message", "{ main:function()  {window.location.href=window.location.href;}}");
		return returnMap;

	}

	/**
	 * Returns the dynamic column relevent for the Supplier Line Item. Dynamic columns are the Attribute Groups and UDA defined at the header and line item level.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing  objectList, requestMap
	 * @return MapList
	 * @throws Exception if the operation fails
	 */
	public MapList getDynamicSupplierLineItemSummaryColumn(Context context, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		HashMap paramList = (HashMap)programMap.get("requestMap");

		MapList returnMap = new MapList();
		String _SRCShowAGs=(String)paramList.get("_SRCShowAGs");

		//START: To fix the IR-109916V6R2012x
		String objectId = (String)paramList.get("objectId");
		//END: To fix the IR-109916V6R2012x

		if("false".equals(_SRCShowAGs))
			return returnMap;

		try
		{
			if(objectList!=null && objectList.size()>0)
			{
				Map objectIdMap = (Map) objectList.get(0);
				//START: To fix the IR-109916V6R2012x
				//Since the objectList does not contain Object Id, we are using paramList to fetch the objectId.
				//String objectId = (String) objectIdMap.get("id");
				//END: To fix the IR-109916V6R2012x

				DomainObject domainObj=new DomainObject(objectId);
				String rtsId=null;

				StringList quotIdList=new StringList();
				if(DomainConstants.TYPE_REQUEST_TO_SUPPLIER.equals(domainObj.getInfo(context,domainObj.SELECT_TYPE)))
				{
					rtsId=objectId;
					quotIdList=domainObj.getInfoList(context,RequestToSupplier.SELECT_RTS_QUOTATION_IDS);
				}
				else if(DomainConstants.TYPE_RTS_QUOTATION.equals(domainObj.getInfo(context,domainObj.SELECT_TYPE)))
				{
					//Start: Parameterized MQL statements
					//rtsId=MqlUtil.mqlCommand(context,"print bus "+objectId+" select to["+RELATIONSHIP_RTS_QUOTATION+"].from.id dump;");



					String strPrintBus="print bus $1 select $2 dump;";
					StringBuffer strBuff = new StringBuffer(128);
					strBuff.append("to[");
					strBuff.append(RELATIONSHIP_RTS_QUOTATION);
					strBuff.append("].from.id");

					rtsId=MqlUtil.mqlCommand(context, strPrintBus, objectId, strBuff.toString());
					//End: Parameterized MQL statements
					quotIdList=new StringList();
					quotIdList.add(objectId);
				}
				else throw new FrameworkException("Object type is neither RequestToSupplier not RTSQuotation type");

				StringList rfqList = FrameworkUtil.split(rtsId,",");
				List <String>processedLIAgList=new ArrayList<String>();
				boolean editable=false;
				if(rfqList.size() > 0)
				{
					for(int ct=0;ct<quotIdList.size();ct++)
					{
						buildSupplierLineItemAdditionalAttributes(context,(String)quotIdList.get(ct),rtsId,returnMap,processedLIAgList,editable);
					}
				}
			}
		}
		catch(Exception  ex)
		{
			ex.printStackTrace();
			throw ex;
		}


		//MSIL Capital - Start
		MapList mlFinalReturnList = new MapList();

		boolean isTechBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER);
		boolean isCommBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER);
		boolean isRMBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER);

		DomainObject doRFQ = new DomainObject(objectId);
		SelectList slObjectSelect = new SelectList();
		slObjectSelect.add(doRFQ.SELECT_ID);
		slObjectSelect.add(doRFQ.SELECT_NAME);
		slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
		SelectList slRelSelect = new SelectList();
		slRelSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
		String strWhere = "attribute["+DomainConstants.ATTRIBUTE_USER_SYMBOLIC_NAME+"].value == role_Supplier";
		ContextUtil.pushContext(context);
		MapList mlTRSTemplates = doRFQ.getRelatedObjects(context,DomainConstants.RELATIONSHIP_DEFAULT_LINE_ITEM_ATTRIBUTE_GROUP,DomainConstants.TYPE_LINE_ITEM_TEMPLATE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
		ContextUtil.popContext(context);        
		mlTRSTemplates.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");

		MapList mlTempReturnList = returnMap;

		Map tempMap = null; 
		Map tempRetMap = null;
		Map tempSettingMap = null; 
		String strLineItemTemplateName = "";
		String strLineItemTemplateVisibility = "";
		String strLineItemTemplateSequence = "";

		String strCustomTitle = "";
		String strOpenStatus = doRFQ.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
		String strCurrent = doRFQ.getInfo(context, DomainObject.SELECT_CURRENT);
		boolean bQuoteOpen = false;
		if(UIUtil.isNotNullAndNotEmpty(strOpenStatus) && "Complete".equals(strOpenStatus) && (strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE)))
			bQuoteOpen = true;

		for(int iCnt = 0; iCnt <mlTRSTemplates.size();iCnt++) {
			tempMap = (Map)mlTRSTemplates.get(iCnt);
			strLineItemTemplateName = (String)tempMap.get(DomainConstants.SELECT_NAME);
			strLineItemTemplateVisibility = (String)tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
			strLineItemTemplateSequence = (String)tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");

			for(int jCnt=0;jCnt<returnMap.size();jCnt++) {
				tempRetMap = (Map)returnMap.get(jCnt);
				tempSettingMap = (Map) tempRetMap.get("settings");
				tempSettingMap.put("Export", "true");
				strCustomTitle = (String)tempSettingMap.get("Custom_Title");
				if(UIUtil.isNotNullAndNotEmpty(strCustomTitle)) {
					StringList slHeaderList = FrameworkUtil.split(strCustomTitle, ";");
					strCustomTitle = slHeaderList.get(0);
					if(UIUtil.isNotNullAndNotEmpty(strCustomTitle) && strLineItemTemplateName.equals(strCustomTitle)) {
						tempRetMap.put("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", strLineItemTemplateSequence);
						if(isTechBuyer && ("Commercial".equals(strLineItemTemplateVisibility) ||  "All".equals(strLineItemTemplateVisibility))) {
							tempSettingMap.put("Access Expression", "false");
						}else if(isCommBuyer && ("Technical".equals(strLineItemTemplateVisibility) || "All".equals(strLineItemTemplateVisibility))) {
							tempSettingMap.put("Access Expression", "false");
						}else if(isRMBuyer && !"All".equals(strLineItemTemplateVisibility)) {
							tempSettingMap.put("Access Expression", "false");
						}else if(bQuoteOpen){
							if(isTechBuyer && ("Technical".equals(strLineItemTemplateVisibility) ||  "All".equals(strLineItemTemplateVisibility))) {
								mlFinalReturnList.add(tempRetMap);
							}else if(isCommBuyer){
								mlFinalReturnList.add(tempRetMap);
							}else if(isRMBuyer && "All".equals(strLineItemTemplateVisibility)) {
								mlFinalReturnList.add(tempRetMap);
							}else {
								tempSettingMap.put("Access Expression", "false");
							}
						}else {
							tempSettingMap.put("Access Expression", "false");	tempSettingMap.put("Access Expression", "false");
						}
					}
				}
			}			
		}

		mlFinalReturnList.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
		//MSIL Capital - End
		return mlFinalReturnList;
	}



	/**
	 * Returns the dynamic column relevent for the Quotation Supplier Line Item.Dynamic columns are the Attribute Groups and UDA defined at the header and line item level.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a packed HashMap containing  objectList, requestMap
	 * @return MapList
	 * @throws Exception if the operation fails
	 */
	public MapList getDynamicQuotationSupplierLineItemSummaryColumn(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		HashMap paramList = (HashMap)programMap.get("requestMap");

		MapList returnMap = new MapList();
		String _SRCShowAGs=(String)paramList.get("_SRCShowAGs");

		if("false".equals(_SRCShowAGs))
			return returnMap;

		try
		{
			Map objectIdMap=null;
			String quotObjectId=null;
			if(objectList!=null && objectList.size()>0)
			{
				objectIdMap = (Map) objectList.get(0);
				quotObjectId = (String) objectIdMap.get("id");
				//Start: Parameterized MQL statements
				//String rfqId=MqlUtil.mqlCommand(context,"print bus "+quotObjectId+" select to["+RELATIONSHIP_RTS_QUOTATION+"].from.id dump;");

				String strPrintBus="print bus $1 select $2 dump;";
				StringBuffer strBuff = new StringBuffer(128);
				strBuff.append("to[");
				strBuff.append(RELATIONSHIP_RTS_QUOTATION);
				strBuff.append("].from.id");

				String rfqId=MqlUtil.mqlCommand(context, strPrintBus, quotObjectId, strBuff.toString());
				//End: Parameterized MQL statements

				StringList rfqList = FrameworkUtil.split(rfqId,",");
				boolean editable=true;
				List <String>processedLIAgList=new ArrayList<String>();
				if(rfqList.size() > 0)
				{
					buildSupplierLineItemAdditionalAttributes(context,quotObjectId,rfqId,returnMap,processedLIAgList,editable);
				}
			}
		}
		catch(Exception  ex)
		{
			ex.printStackTrace();
			throw ex;
		}

		MapList mlFinalReturnList = new MapList();
		//MSIL Capital - Start


		boolean isTechBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER);
		boolean isCommBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER);
		boolean isRMBuyer = context.isAssigned(TDRConstants_mxJPO.ROLE_RM_BUYER);

		if(isTechBuyer || isCommBuyer || isRMBuyer) {
			String strOjectId = (String)paramList.get("objectId");
			DomainObject doRFQ = new DomainObject(strOjectId);
			String strRFQId = (String)doRFQ.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].from.id");
			doRFQ = new DomainObject(strRFQId);
			SelectList slObjectSelect = new SelectList();
			slObjectSelect.add(doRFQ.SELECT_ID);
			slObjectSelect.add(doRFQ.SELECT_NAME);
			slObjectSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
			SelectList slRelSelect = new SelectList();
			slRelSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");
			String strWhere = "attribute["+DomainConstants.ATTRIBUTE_USER_SYMBOLIC_NAME+"].value == role_Supplier";
			ContextUtil.pushContext(context);
			MapList mlTRSTemplates = doRFQ.getRelatedObjects(context,DomainConstants.RELATIONSHIP_DEFAULT_LINE_ITEM_ATTRIBUTE_GROUP,DomainConstants.TYPE_LINE_ITEM_TEMPLATE,slObjectSelect,slRelSelect,false,true,(short)1,null,null);
			ContextUtil.popContext(context);        
			mlTRSTemplates.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");

			String strOpenStatus = doRFQ.getAttributeValue(context, TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS);
			String strCurrent = doRFQ.getInfo(context, DomainObject.SELECT_CURRENT);
			boolean bQuoteOpen = false;
			if(UIUtil.isNotNullAndNotEmpty(strOpenStatus) && "Complete".equals(strOpenStatus) && (strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW) || strCurrent.equals(RequestToSupplier.STATE_REQUEST_TO_SUPPLIER_COMPLETE)))
				bQuoteOpen = true;

			MapList mlTempReturnList = returnMap;

			Map tempMap = null; 
			Map tempRetMap = null;
			Map tempSettingMap = null; 
			String strLineItemTemplateName = "";
			String strLineItemTemplateVisibility = "";
			String strLineItemTemplateSequence = "";

			String strCustomTitle = "";
			for(int iCnt = 0; iCnt <mlTRSTemplates.size();iCnt++) {
				tempMap = (Map)mlTRSTemplates.get(iCnt);
				strLineItemTemplateName = (String)tempMap.get(DomainConstants.SELECT_NAME);
				strLineItemTemplateVisibility = (String)tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_VISIBILITY+"]");
				strLineItemTemplateSequence = (String)tempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]");

				for(int jCnt=0;jCnt<returnMap.size();jCnt++) {
					tempRetMap = (Map)returnMap.get(jCnt);
					tempSettingMap = (Map) tempRetMap.get("settings");
					strCustomTitle = (String)tempSettingMap.get("Custom_Title");
					if(UIUtil.isNotNullAndNotEmpty(strCustomTitle)) {
						StringList slHeaderList = FrameworkUtil.split(strCustomTitle, ";");
						strCustomTitle = slHeaderList.get(0);
						if(UIUtil.isNotNullAndNotEmpty(strCustomTitle) && strLineItemTemplateName.equals(strCustomTitle)) {
							tempRetMap.put("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", strLineItemTemplateSequence);
							if(isTechBuyer && ("Commercial".equals(strLineItemTemplateVisibility) ||  "All".equals(strLineItemTemplateVisibility))) {
								tempSettingMap.put("Access Expression", "false");
							}else if(isCommBuyer && ("Technical".equals(strLineItemTemplateVisibility) || "All".equals(strLineItemTemplateVisibility))) {
								tempSettingMap.put("Access Expression", "false");
							}else if(isRMBuyer && !"All".equals(strLineItemTemplateVisibility)) {
								tempSettingMap.put("Access Expression", "false");
							}else if(bQuoteOpen){
								if(isTechBuyer && ("Technical".equals(strLineItemTemplateVisibility) ||  "All".equals(strLineItemTemplateVisibility))) {
									mlFinalReturnList.add(tempRetMap);
								}else if(isCommBuyer){
									mlFinalReturnList.add(tempRetMap);
								}else if(isRMBuyer && "All".equals(strLineItemTemplateVisibility)) {
									mlFinalReturnList.add(tempRetMap);
								}else {
									tempSettingMap.put("Access Expression", "false");
								}
							}else {
								tempSettingMap.put("Access Expression", "false");
							}
						}
					}
				}			
			}

			mlFinalReturnList.sortStructure("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER+"]", "ascending", "Integer");
			return mlFinalReturnList;
		}else {
			return returnMap;	
		}




	}



}
