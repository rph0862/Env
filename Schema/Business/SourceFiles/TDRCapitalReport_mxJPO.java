/*
 * (c) Dassault Systemes, 1993 - 2010.  All rights reserved
 */

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.sourcing.Calculator;
import com.matrixone.apps.sourcing.RequestToSupplier;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.util.StringList;

/**
 * The <code>LineItem</code> class contains code for the "Line Item" business type
 *
 * @version SC 9.5.Rossini.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class TDRCapitalReport_mxJPO 
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
	public Properties  _classCurrencyConfig = new Properties();
    public TDRCapitalReport_mxJPO (Context context, String[] args)
        throws Exception
    {
		Page page= new Page("TDRCapitalConfiguration");
		_classCurrencyConfig.load(page.getContentsAsStream(context, "TDRCapitalConfiguration"));
    }
    
    /**
     * This is a trigger method which is invoked when RFQ is promoted from Started to Initial Review state.
     * RFQ Summary report will be generated in docx format.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since SC 9.5.Rossini.0
     */
	public void generateRFQSummaryReport(Context context, String[] args)throws Exception{
		try {
			String strObjectId = args[0];
			StringList slStandardDocs = null;
			StringList slStandardDocIds = null;
			Map mObjectInfo = null;
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ANNUAL_QUANTITY+"].value");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].attribute["+DomainObject.ATTRIBUTE_RECIPIENT+"].value");
			DomainObject.MULTI_VALUE_LIST.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.name");
			DomainObject.MULTI_VALUE_LIST.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.type");
			DomainObject.MULTI_VALUE_LIST.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.id");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.name");
			DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.id");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)) {
				
				String strTempFolder = context.createWorkspace();
				DomainObject doObject = new DomainObject(strObjectId);
				StringList slSelectList = new StringList();
				slSelectList.add(DomainObject.SELECT_ID);
				slSelectList.add(DomainObject.SELECT_NAME);
				slSelectList.add(DomainObject.SELECT_OWNER);
				slSelectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_NUMBER+"].value");
				slSelectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RFQ_RINGI_LEVEL+"].value");
				slSelectList.add(DomainObject.SELECT_ORIGINATED);
				//User Dept
				slSelectList.add("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.name");
				slSelectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
				slSelectList.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
				slSelectList.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
				slSelectList.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ANNUAL_QUANTITY+"].value");
				slSelectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_PROJECT_HEAD+"].value");
				
				slSelectList.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");				
				slSelectList.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].attribute["+DomainObject.ATTRIBUTE_RECIPIENT+"].value");				
				slSelectList.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_FINAL_SOURCE+"].value");				
				slSelectList.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.name");
				slSelectList.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.type");
				slSelectList.add("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.id");
				
				// To get all details of RFQ to generate Report
				mObjectInfo = (Map)doObject.getInfo(context, slSelectList);
				
				//To get RFQ Owner Department
				String strRFQOwner = (String)mObjectInfo.get(DomainObject.SELECT_OWNER);
				System.out.println("strRFQOwner : "+strRFQOwner);
				String personObjectID = PersonUtil.getPersonObjectID(context, strRFQOwner);
				DomainObject doPerson = new DomainObject(personObjectID);
				System.out.println("doPerson : "+doPerson);
				String strUserDept = (String)doPerson.getInfo(context, "to["+DomainObject.RELATIONSHIP_MEMBER+"].from["+DomainObject.TYPE_DEPARTMENT+"].name");

				//To get Standard Documents 
				String strWorkspaceStdDocument = _classCurrencyConfig.getProperty("TDR.Workspace.StandardDocuments.Name");
				String strHyperLinkURL = _classCurrencyConfig.getProperty("TDR.Application.URL");
				if(UIUtil.isNotNullAndNotEmpty(strWorkspaceStdDocument)){
					StringList slSelectables = new StringList();
					slSelectables.add(DomainObject.SELECT_ID);
					slSelectables.add("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.name");
					slSelectables.add("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.id");
					MapList mlWorkspaceStdDocument =  DomainObject.findObjects(context,DomainObject.TYPE_WORKSPACE_VAULT,"eService Production",DomainObject.SELECT_NAME+"=='"+strWorkspaceStdDocument+"'",slSelectables);
					if(mlWorkspaceStdDocument != null && mlWorkspaceStdDocument.size()>0){
						for(Object object : mlWorkspaceStdDocument){
							Map m = (Map)object;
							slStandardDocs = (StringList)m.get("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.name");
							slStandardDocIds = (StringList)m.get("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.id");
						}
					}
				}
				
				//Internal and External Documents 
		    	StringList slDocuments =  null;
		    	StringList slDocumentIds =  null;
		    	StringList slInternalDocuments =  new StringList();
		    	StringList slInternalDocumentIds =  new StringList();
		    	StringList slExternalDocuments =  new StringList();
		    	StringList slExternalDocumentIds =  new StringList();
		    	slDocuments = (StringList)mObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.name");
		    	slDocumentIds = (StringList)mObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.id");
		    	System.out.println("slDocuments : "+slDocuments);
	    		StringList slDocumentTypes = (StringList)mObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.type");
	    		System.out.println("slDocumentTypes : "+slDocumentTypes);
		    	String strInternalDocTypes = MqlUtil.mqlCommand(context, "print type $1 select derivative dump $2",TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT,"|");
		    	System.out.println("strInternalDocTypes : "+strInternalDocTypes);
		    	StringList slInternalDocTypes = FrameworkUtil.split(strInternalDocTypes, "|");
		    	slInternalDocTypes.add(TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT);
		    	System.out.println("slInternalDocTypes : "+slInternalDocTypes);
		    	if(slDocuments != null && slDocuments.size()>0 ){	    		
		    		for (int iCount = 0; iCount < slDocuments.size(); iCount++) {
		    			if(slInternalDocTypes.contains((String)slDocumentTypes.get(iCount))){
		    				slInternalDocuments.add((String)slDocuments.get(iCount));
		    				slInternalDocumentIds.add((String)slDocumentIds.get(iCount));
		    			}else{
		    				slExternalDocuments.add((String)slDocuments.get(iCount));
		    				slExternalDocumentIds.add((String)slDocumentIds.get(iCount));
		    			}
		    		}
					if(slStandardDocs != null && slStandardDocs.size()>0 ){
						slExternalDocuments.addAll(slStandardDocs);
						slExternalDocumentIds.addAll(slStandardDocIds);
					}
		    	}else{
					slDocuments =  new StringList();
					slDocumentIds =  new StringList();
				}

				//Add additional info
				mObjectInfo.put("InternalDocumentNames", slInternalDocuments);
				mObjectInfo.put("InternalDocumentIds", slInternalDocumentIds);
				mObjectInfo.put("ExternalDocumentNames", slExternalDocuments);
				mObjectInfo.put("ExternalDocumentIds", slExternalDocumentIds);
				mObjectInfo.put("UserDepartment", strUserDept);
				mObjectInfo.put("fileFolder", strTempFolder);
				mObjectInfo.put("docHyperLink", strHyperLinkURL);
				
				System.out.println("slInternalDocuments : "+slInternalDocuments);
				System.out.println("ExternalDocumentNames : "+slExternalDocuments);
				mObjectInfo.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.name");
				mObjectInfo.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.id");
				mObjectInfo.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.type");
				
				String strName = (String)mObjectInfo.get(DomainObject.SELECT_NAME);
				String strRFQSummaryName = _classCurrencyConfig.getProperty("TDR.RFQSummary.DocumentName");		
				strName = strName +" "+strRFQSummaryName;
				
				if(UIUtil.isNotNullAndNotEmpty(strName)) {
					mObjectInfo.put("fileName", strName);
					writeDocx(context,mObjectInfo);
					DomainObject doNewDoc = DomainObject.newInstance(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT);
					doNewDoc.createObject(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT, strName, "1", TDRConstants_mxJPO.POLICY_TDR_INTERNAL_DOCUMENT, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION);	
					doNewDoc.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, strName);
					doNewDoc.checkinFile(context, true, true, "", "generic", strName+".doc", strTempFolder);	
					DomainRelationship.connect(context, doObject, TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT, doNewDoc);
				}
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ANNUAL_QUANTITY+"].value");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].attribute["+DomainObject.ATTRIBUTE_RECIPIENT+"].value");
			DomainObject.MULTI_VALUE_LIST.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.type");
			DomainObject.MULTI_VALUE_LIST.remove("from["+TDRConstants_mxJPO.RELATIONSHIP_SOURCING_DOCUMENT+"].to.id");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].to.name");
		}
	}

	public void writeDocx(Context context, Map mObjectInfo) throws Exception {
		FileOutputStream fos = null;
		System.out.println("mObjectInfo : "+mObjectInfo);
		String strRFQId = DomainObject.EMPTY_STRING;
		String strRFQNo = DomainObject.EMPTY_STRING;
		String strRFQRingiNo = DomainObject.EMPTY_STRING;
		String strRFQRingiLevel = DomainObject.EMPTY_STRING;
		String strRFQOrinated = DomainObject.EMPTY_STRING;
		String strUserDept = DomainObject.EMPTY_STRING;
		String strCommercialDept = DomainObject.EMPTY_STRING;
		String strBudget = DomainObject.EMPTY_STRING;
		StringList slRFQLineItems = null;
		StringList slRFQLineItemsName = null;
		StringList slRFQLineItemsQty = null;
		String strProjectHead = DomainObject.EMPTY_STRING;
		StringList slRFQSuppliers = null;
		StringList slRFQSupplierRecipients = null;
		String strRFQFinalSource = DomainObject.EMPTY_STRING;
		String fileName = DomainObject.EMPTY_STRING;  	
		String fileLocation = DomainObject.EMPTY_STRING;  	
		String strDocHyperLink = DomainObject.EMPTY_STRING;  	

		try {
			strRFQId = (String)mObjectInfo.get(DomainObject.SELECT_ID);
			strRFQNo = (String)mObjectInfo.get(DomainObject.SELECT_NAME);
			strRFQRingiNo = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_MSIL_RINGI_NUMBER+"].value");
			strRFQRingiLevel = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RFQ_RINGI_LEVEL+"].value");
			strRFQOrinated = (String)mObjectInfo.get(DomainObject.SELECT_ORIGINATED);
			strUserDept = (String)mObjectInfo.get("UserDepartment");
			strCommercialDept = (String)mObjectInfo.get("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.name");
			strBudget = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
			slRFQLineItems = (StringList)mObjectInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
			slRFQLineItemsName = (StringList)mObjectInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
			slRFQLineItemsQty = (StringList)mObjectInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ANNUAL_QUANTITY+"].value");
			strProjectHead = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_PROJECT_HEAD+"].value");
			strRFQNo = (String)mObjectInfo.get(DomainObject.SELECT_NAME);
			slRFQSuppliers = (StringList)mObjectInfo.get("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			slRFQSupplierRecipients = (StringList)mObjectInfo.get("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].attribute["+DomainObject.ATTRIBUTE_RECIPIENT+"].value");
			strRFQFinalSource = (String)mObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_FINAL_SOURCE+"].value");
			fileName = (String)mObjectInfo.get("fileName");
			fileLocation = (String)mObjectInfo.get("fileFolder");
			strDocHyperLink = (String)mObjectInfo.get("docHyperLink");

			XWPFDocument document = new XWPFDocument();
			//Heading
			XWPFParagraph paraHeading = document.createParagraph();
			alignParagraph(paraHeading, ParagraphAlignment.CENTER,false, true, "Calibri (Body)", 12, UnderlinePatterns.NONE, strRFQNo + " SUMMARY", false, "");
			addLineBreak(document);
			//create first Table
			XWPFTable tableFirst = document.createTable();
			//tableFirst.setInsideVBorder(XWPFBorderType.SINGLE, 0, 0, "1C7331");
			XWPFTableRow tableRowOne = tableFirst.getRow(0);
			tableRowOne.getCell(0).setText("RFQ No: "+strRFQNo);
			tableRowOne.addNewTableCell().setText("RFQ Ringi No: "+strRFQRingiNo);
			tableRowOne.addNewTableCell().setText("Ringi Level: "+strRFQRingiLevel);
			alignTable(tableFirst, "FirstTable");
			addLineBreak(document);

			//create second Table
			XWPFTable tableSecond = document.createTable();
			//tableFirst.setInsideVBorder(XWPFBorderType.SINGLE, 0, 0, "1C7331");
			XWPFTableRow tableRowSec = tableSecond.getRow(0);
			tableRowSec.getCell(0).setText("Date: "+strRFQOrinated);
			tableRowSec.addNewTableCell().setText("User Dept: "+strUserDept);
			tableRowSec.addNewTableCell().setText("Commercial Dept: "+strCommercialDept);
			tableRowSec.addNewTableCell().setText("Budget / MRs: "+strBudget);  
			tableRowSec.addNewTableCell().setText("Project/Budget Head: "+strProjectHead);
			alignTable(tableSecond, "SecondTable");   		
			addLineBreak(document);  

			//Item Details
			XWPFParagraph paraItemDetails = document.createParagraph();  
			alignParagraph(paraItemDetails, ParagraphAlignment.LEFT,false, true, "Calibri (Body)", 11, UnderlinePatterns.NONE, "Item Details:", false, "");

			XWPFTable tableItem = document.createTable();
			tableItem.setInsideVBorder(XWPFBorderType.SINGLE, 0, 0, "000000");
			XWPFTableRow tableItemHeaderRow = tableItem.getRow(0);
			tableItemHeaderRow.getCell(0).setText("Item No");
			tableItemHeaderRow.addNewTableCell().setText("Item Name");
			tableItemHeaderRow.addNewTableCell().setText("Quantity");
			//tableItemHeaderRow.addNewTableCell().setText("Project / Budget Head");

			XWPFTableRow tableItemBodyRow  = null;
			//        	int iSizeLineItems = slRFQLineItems.size();
			int iSizeLineItems = slRFQLineItemsName.size();
			for(int i=0 ; i< iSizeLineItems;i++)
			{
				tableItemBodyRow = tableItem.createRow();
				tableItemBodyRow.getCell(0).setText(String.valueOf(i+1));
				tableItemBodyRow.getCell(1).setText(""+(String)slRFQLineItemsName.get(i));
				//        	   tableItemBodyRow.getCell(2).setText(""+strProjectHead);
				tableItemBodyRow.getCell(2).setText(""+(String)slRFQLineItemsQty.get(i));
			}
			alignTable(tableItem, "ItemDetails"); 
			addLineBreak(document);

			//Vendor Details
			XWPFParagraph paraVendorDetails = document.createParagraph();
			alignParagraph(paraVendorDetails, ParagraphAlignment.LEFT,false, true, "Calibri (Body)", 11, UnderlinePatterns.NONE, "Vendor Details:", false, "");	    		

			XWPFParagraph paraNoOfVendorDetails = document.createParagraph();
			alignParagraph(paraNoOfVendorDetails, ParagraphAlignment.LEFT,false, false, "Calibri (Body)", 11, UnderlinePatterns.NONE, "Number of Suppliers Considered: "+slRFQSuppliers.size(), false, "");

			XWPFTable tableVendor = document.createTable();
			tableVendor.setInsideVBorder(XWPFBorderType.SINGLE, 0, 0, "000000");
			XWPFTableRow tableVendorHeaderRow = tableVendor.getRow(0);
			tableVendorHeaderRow.getCell(0).setText("Sl. No");
			tableVendorHeaderRow.addNewTableCell().setText("Vendor Name");
			tableVendorHeaderRow.addNewTableCell().setText("Recipient");

			XWPFTableRow tableVendorBodyRow  = null;
			int iSizeVendors = slRFQSuppliers.size();
			for(int i=0 ; i< iSizeVendors;i++)
			{
				tableVendorBodyRow = tableVendor.createRow();
				tableVendorBodyRow.getCell(0).setText(String.valueOf(i+1));
				tableVendorBodyRow.getCell(1).setText(""+(String)slRFQSuppliers.get(i));
				tableVendorBodyRow.getCell(2).setText(""+(String)slRFQSupplierRecipients.get(i));
			}
			alignTable(tableVendor, "VendorDetails"); 
			addLineBreak(document);

			//Final Sources to be selected
			XWPFParagraph paraFinalSources = document.createParagraph();  
			alignParagraph(paraFinalSources, ParagraphAlignment.LEFT,false, true, "Calibri (Body)", 11, UnderlinePatterns.NONE, "Final Sources to be selected: "+strRFQFinalSource, false, "");

			//Enclosure
			XWPFParagraph paraEnclosure = document.createParagraph(); 
			alignParagraph(paraEnclosure, ParagraphAlignment.CENTER,false, false, "Calibri (Body)", 11, UnderlinePatterns.SINGLE, "ENCLOSURES", false, "");

			addLineBreak(document);

			//Documents to be sent to supplier
			XWPFParagraph paraSupplierDocument = document.createParagraph(); 
			alignParagraph(paraSupplierDocument, ParagraphAlignment.LEFT,false, true, "Calibri (Body)", 11, UnderlinePatterns.NONE, "Documents to be sent to supplier: ", false, "");
			StringList slExternalDocuments = (StringList)mObjectInfo.get("ExternalDocumentNames");
			StringList slExternalDocumentIds = (StringList)mObjectInfo.get("ExternalDocumentIds");
			for (int iCount = 0; iCount < slExternalDocuments.size(); iCount++) {
				String strHyperLinkURL = strDocHyperLink + "/3dspace/components/emxCommonDocumentPreCheckout.jsp?action=download&trackUsagePartId="+strRFQId+"&objectId="+(String)slExternalDocumentIds.get(iCount)+"";
				alignParagraph(document.createParagraph(), ParagraphAlignment.LEFT,true, false, "Calibri (Body)", 11, UnderlinePatterns.NONE, (String)slExternalDocuments.get(iCount), true, strHyperLinkURL);
			}
			addLineBreak(document);
			//MSIL Documents
			XWPFParagraph paraMSILDocument = document.createParagraph(); 
			alignParagraph(paraMSILDocument, ParagraphAlignment.LEFT,false, true, "Calibri (Body)", 11, UnderlinePatterns.NONE, "MSIL Documents: ", false, "");
			StringList slInternalDocuments = (StringList)mObjectInfo.get("InternalDocumentNames");
			StringList slInternalDocumentIds = (StringList)mObjectInfo.get("InternalDocumentIds");
			for (int iCount = 0; iCount < slInternalDocuments.size(); iCount++) {
				String strHyperLinkURL = strDocHyperLink + "/3dspace/components/emxCommonDocumentPreCheckout.jsp?action=download&trackUsagePartId="+strRFQId+"&objectId="+(String)slInternalDocumentIds.get(iCount)+"";
				alignParagraph(document.createParagraph(), ParagraphAlignment.LEFT,true, false, "Calibri (Body)", 11, UnderlinePatterns.NONE, (String)slInternalDocuments.get(iCount), true, strHyperLinkURL);
			}

			fos = new FileOutputStream(new File(fileLocation+"\\"+fileName+ ".doc"));
			document.write(fos);
			fos.close();

		}catch (Exception e) {
			e.printStackTrace();
			//throw e;
		}finally{
			fos.close();	
		}
	}

	private void addLineBreak(XWPFDocument document) throws Exception {
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun tmpRun = paragraph.createRun();
		tmpRun.setText("");
	}
	private void alignParagraph(XWPFParagraph paragraph, ParagraphAlignment pAlignment,boolean isBullets, boolean bBold,String sFontFamily, int iFontSize, UnderlinePatterns uPattern, String sText, boolean bHyperLink, String hyperLinkURL) throws Exception {
		paragraph.setAlignment(pAlignment);
		XWPFRun run = null;
		if(isBullets){
			run = paragraph.createRun();
			run.setText(String.valueOf((char) 110));//Character Square
			run.setFontFamily("Wingdings");
			run.setFontSize(6);
		}
		if(bHyperLink){
			String id=paragraph.getDocument().getPackagePart().addExternalRelationship(hyperLinkURL, XWPFRelation.DOCUMENT.getRelation()).getId();
			CTHyperlink cLink=paragraph.getCTP().addNewHyperlink();
			cLink.setId(id);
			CTText ctText=CTText.Factory.newInstance();
			ctText.setStringValue(sText);
			CTR ctr=CTR.Factory.newInstance();
			ctr.setTArray(new CTText[]{ctText});
			cLink.setRArray(new CTR[]{ctr});
		}else{    		
			run = paragraph.createRun();
			run.setBold(bBold);
			run.setFontFamily(sFontFamily);
			run.setFontSize(iFontSize);
			run.setUnderline(uPattern);
			run.setText(sText);
			//run.addBreak();
		}
	}
	private void alignTable(XWPFTable table, String paragraph) throws Exception {
		for(int x = 0;x < table.getNumberOfRows(); x++)
		{
			if(x==0)
			{
				XWPFTableRow row = table.getRow(x);
				int numberOfCell = row.getTableCells().size();
				for(int y = 0; y < numberOfCell ; y++)
				{
					XWPFTableCell cell = row.getCell(y);
					CTTcPr cellPropertie = cell.getCTTc().addNewTcPr();
					CTVerticalJc verticalCell = cellPropertie.addNewVAlign();
					verticalCell.setVal(STVerticalJc.CENTER);
					XWPFParagraph para = cell.getParagraphs().get(0);
					XWPFRun rh = para.createRun();
					rh.setBold(true);
					rh.setFontSize(15);
					rh.setFontFamily("Calibri (Body)");
					if("ItemDetails".equals(paragraph))
					{    					
						para.setAlignment(ParagraphAlignment.CENTER);
						if(y==0)
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
						else
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4000));                    
						CTShd ctshd = cellPropertie.addNewShd();
						ctshd.setColor("auto");
						ctshd.setVal(STShd.CLEAR);
						ctshd.setFill("00ccff");
					}else if("VendorDetails".equals(paragraph)){    					
						para.setAlignment(ParagraphAlignment.CENTER);
						if(y==0)
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
						else
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4000));                    
						CTShd ctshd = cellPropertie.addNewShd();
						ctshd.setColor("auto");
						ctshd.setVal(STShd.CLEAR);
						ctshd.setFill("00ccff");
					}else if("SecondTable".equals(paragraph)){
						para.setAlignment(ParagraphAlignment.LEFT);
						if (y==0 || y==3)
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1500));
						else
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(2000));
					}else if("FirstTable".equals(paragraph)){
						para.setAlignment(ParagraphAlignment.LEFT);
						cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(3000));
					}
				}
			}
			if(x!=0)
			{
				XWPFTableRow row = table.getRow(x);
				int numberOfCell = row.getTableCells().size();
				for(int y = 0; y < numberOfCell ; y++)
				{
					XWPFTableCell cell = row.getCell(y);
					CTTcPr cellPropertie = cell.getCTTc().addNewTcPr();
					CTVerticalJc verticalCell = cellPropertie.addNewVAlign();
					verticalCell.setVal(STVerticalJc.CENTER);
					XWPFParagraph para = cell.getParagraphs().get(0);
					XWPFRun rh = para.createRun();
					// style cell as desired
					rh.setBold(false);
					rh.setFontSize(15);
					rh.setFontFamily("Calibri (Body)");
					para.setAlignment(ParagraphAlignment.LEFT);
					if("ItemDetails".equals(paragraph)){    					
						para.setAlignment(ParagraphAlignment.CENTER);
						if(y==0)
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
						else
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4000));                    
					}else if("VendorDetails".equals(paragraph)){     					
						if(y==0)
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
						else
							cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4000));
					}
				}
			}
		}
	}

	/**
	 * This method is used to generate sourcing Report.
	 * Sourcing report will be generated in xlsx format.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since SC 9.5.Rossini.0
	 */
	public String generateSourcingReport(Context context, String[] args)throws Exception{
		String iReturnStatus = "Fail";
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+DomainObject.ATTRIBUTE_AWARD_STATUS+"]");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].to.from["+DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_STATE+"]");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to."+DomainObject.SELECT_ORIGINATED);
		
		String strGenerateRFQReportFailedSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.GenerateRFQReport.FailedSubject");
		String strGenerateRFQReportFailedMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.GenerateRFQReport.FailedBody");
		String strGenerateRFQReportSuccessSubject = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.GenerateRFQReport.SuccessSubject");
		String strGenerateRFQReportSuccessMessage = EnoviaResourceBundle.getProperty(context, "emxSourcingStringResource", context.getLocale(), "TDR.GenerateRFQReport.SuccessBody");
		try {			
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strRFQStatus = (String)programMap.get("RFQStatus");
			String strStartDate = (String)programMap.get("StartDate");
			String strEndDate = (String)programMap.get("EndDate");
			
			String strFolderName = context.createWorkspace(); 
			System.out.println("Workspace Location >>>>> "+ strFolderName);
			String strFileName = _classCurrencyConfig.getProperty("TDR.RFQReport.DocumentName");
			StringBuffer sbWhere = new StringBuffer();
			if(strRFQStatus != null && "All".equalsIgnoreCase(strRFQStatus)){				
				sbWhere.append(DomainObject.SELECT_ORIGINATED +">='"+strStartDate+"'");
				sbWhere.append(" && ");
				sbWhere.append(DomainObject.SELECT_ORIGINATED +"<='"+strEndDate+"'");
			}else if(strRFQStatus != null && "Open".equalsIgnoreCase(strRFQStatus)){
				sbWhere.append(DomainObject.SELECT_CURRENT+"!='"+DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE+"'");
			}
			
			//If any user logs in from this list, report has to be generated for all departments under context person business unit/department.
			boolean bIsDepartmentHead = false;
			String strDepartmentHead = _classCurrencyConfig.getProperty("TDR.DepartementHead");
			StringList slDepartmentHeads = FrameworkUtil.split(strDepartmentHead, ",");
			if(slDepartmentHeads != null && slDepartmentHeads.size()>0 && slDepartmentHeads.contains((String)context.getUser())){
				bIsDepartmentHead = true;
			}
			
			//If person having any other role(other than Commercial or Technical), then display only specific persons RFQs.
			boolean bIsTechCommericalUser = true;
			if(!bIsDepartmentHead){
				if( !(context.isAssigned(TDRConstants_mxJPO.ROLE_COMMERCIAL_BUYER) || context.isAssigned(TDRConstants_mxJPO.ROLE_TECHNICAL_BUYER)) ){
					bIsTechCommericalUser = false;
					sbWhere.append(" && "+DomainObject.SELECT_OWNER+"=='"+(String)context.getUser()+"'");
				}				
			}else{
				bIsTechCommericalUser = false;
			}
			
			StringList  objectSelects =  new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			objectSelects.add(DomainObject.SELECT_CURRENT);
			objectSelects.add(DomainObject.SELECT_OWNER);
			objectSelects.add(DomainObject.SELECT_NAME);
			objectSelects.add(DomainObject.SELECT_ORIGINATED);
			objectSelects.add("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT+"].actual");
			objectSelects.add("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"].actual");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE+"]");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"]");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"]");
			objectSelects.add("attribute["+DomainObject.ATTRIBUTE_COMMENTS+"]");			
			objectSelects.add("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"]");
			objectSelects.add("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"].to.name");			
			objectSelects.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
			objectSelects.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_STATE+"]");
			objectSelects.add("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to."+DomainObject.SELECT_ORIGINATED);
			objectSelects.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
			objectSelects.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");			
			objectSelects.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			objectSelects.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+DomainObject.ATTRIBUTE_AWARD_STATUS+"]");
			objectSelects.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].to.from["+DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_PROJECT_HEAD+"]");			
			objectSelects.add("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.name");			
			objectSelects.add("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.name");
			
			MapList mlRFQ = DomainObject.findObjects(context, DomainObject.TYPE_RFQ, DomainObject.QUERY_WILDCARD, DomainObject.QUERY_WILDCARD, DomainObject.QUERY_WILDCARD, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION, sbWhere.toString(), "", true, objectSelects, (short)0);
			if(mlRFQ != null && mlRFQ.size()>0){
				writeXlsx(context,mlRFQ,strFolderName,strFileName,bIsTechCommericalUser);
				ContextUtil.pushContext(context);
				try {
					DomainObject doNewDoc = DomainObject.newInstance(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT);
					BusinessObject dom= new BusinessObject(TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT,strFileName,"1","");
					if(dom.exists(context)){
						doNewDoc.setId((String)dom.getObjectId(context));
					}else{
						doNewDoc.createObject(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT, strFileName, "1", TDRConstants_mxJPO.POLICY_TDR_INTERNAL_DOCUMENT, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION);							
					}
					doNewDoc.checkinFile(context, true, true, "", "generic", strFileName+".xlsx", strFolderName);
					iReturnStatus = (String)doNewDoc.getObjectId(context);
					ContextUtil.popContext(context);
				} catch (Exception e) {
					ContextUtil.popContext(context);
					System.out.println("TDRGCapitalReport : generateSourcingReport() "+e.getMessage());
					throw e;
				} 
			}		
		}catch(Exception ex) {
			System.out.println("TDRGCapitalReport : generateSourcingReport() "+ex.getMessage());
			iReturnStatus = "Fail";
			ex.printStackTrace();
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+DomainObject.ATTRIBUTE_AWARD_STATUS+"]");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].to.from["+DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_STATE+"]");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to."+DomainObject.SELECT_ORIGINATED);
		}
		return iReturnStatus;
	}

	private void writeXlsx(Context context, MapList mlInfo, String fileFolder, String fileName, boolean bIsTechCommericalUser)throws Exception {
		FileOutputStream outputStream = null;
		String strUserPerson = DomainObject.EMPTY_STRING;
		String strSC = DomainObject.EMPTY_STRING;
		String strRFQNo = DomainObject.EMPTY_STRING;
		String strRFQDate = DomainObject.EMPTY_STRING;
		StringList slPartDescription = null;
		StringList slPartNo = null;
		String strTotalSourcesOnRFQ = DomainObject.EMPTY_STRING;
		StringList slBidderNames = null;
		String strPendingCategory = DomainObject.EMPTY_STRING;
		String strCategory = DomainObject.EMPTY_STRING;
		String strPendingAt = DomainObject.EMPTY_STRING;
		String strPendingSince = DomainObject.EMPTY_STRING;
		String strPendingDays = DomainObject.EMPTY_STRING;
		String strActionToBeTaken = DomainObject.EMPTY_STRING;
		String strBidSubmittetoSC = DomainObject.EMPTY_STRING;
		String strRFQSentToSCDate = DomainObject.EMPTY_STRING;
		String strRemarksCriticalIssue = DomainObject.EMPTY_STRING;
		String strSCPerson = DomainObject.EMPTY_STRING;
		String strSourceSelectionCriteria = DomainObject.EMPTY_STRING;
		String strObjectControlNumber = DomainObject.EMPTY_STRING;
		String strSelectedVendor = DomainObject.EMPTY_STRING;
		String strSourceSelectionDate = DomainObject.EMPTY_STRING;
		String strNatureofProject = DomainObject.EMPTY_STRING;

		try {
	        XSSFWorkbook workbook = new XSSFWorkbook();
		/**Data (Cap)*****Start*/	
	        XSSFSheet sheetDateCapital = workbook.createSheet("Data (Cap)");
			String[] headerDateCapital = {"R No", "User Dept", "User Person", "SC", "RFQ No", "RFQ Date", "Part Description", "Part No", "Total Sources On RFQ","Bidder Names","PendingCategory","PendingAt","PendingSince","PendingDays","ActionToBeTaken","Bid Submitted tito SC","RFQ Sent To SC Date","Remarks Critical Issue","SC Person","Selected Vendor","Source Selection Date","Nature of Project"};
			XSSFSheet sheetRFQSummaryCapital = workbook.createSheet("RFQ Summary (Cap)");
	        String[] headerRFQSummaryCapital = {"","","Dept","Tech. workflow","Quote/Re-quote by vendor","Update Tech Review Result","Open quote","SC Dash Board","SC-Buyer","Update Buyer Response","Send PO to Vendor","Complete","GrandTotal"};
		    
	        CellStyle styleHeading = workbook.createCellStyle();
	        styleHeading.setBorderLeft(CellStyle.ALIGN_CENTER);
	        styleHeading.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        styleHeading.setFillPattern(CellStyle.SOLID_FOREGROUND); 
	        styleHeading.setBorderLeft(CellStyle.BORDER_THIN);
	        styleHeading.setBorderRight(CellStyle.BORDER_THIN);
	        styleHeading.setBorderTop(CellStyle.BORDER_THIN);
	        styleHeading.setBorderBottom(CellStyle.BORDER_THIN);
	        
	        CellStyle styleHeadingRFQSummaryCap = workbook.createCellStyle();
	        styleHeadingRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);

	        XSSFFont font= workbook.createFont();
	        font.setFontHeightInPoints((short)10);
	        font.setFontName("Arial");
	        font.setColor(IndexedColors.WHITE.getIndex());
	        font.setBold(true);
	        styleHeadingRFQSummaryCap.setFont(font);
	        styleHeadingRFQSummaryCap.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	        styleHeadingRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
	        styleHeadingRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
	        styleHeadingRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
	        styleHeadingRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
	        styleHeadingRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);
	        
	        CellStyle styleBodyRFQSummaryCap = workbook.createCellStyle();
	        styleBodyRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);
	        styleBodyRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
	        styleBodyRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
	        styleBodyRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
	        styleBodyRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
	        styleBodyRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);
	        
	        CellStyle styleTotalRFQSummaryCap = workbook.createCellStyle();
	        styleTotalRFQSummaryCap.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
	        styleTotalRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);
	        styleTotalRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
	        styleTotalRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
	        styleTotalRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
	        styleTotalRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
	        styleTotalRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);
	        
	        // Heading Row---Start
            Row row = sheetDateCapital.createRow(0);
            for (int columnCount=0; columnCount < headerDateCapital.length; columnCount++) {
                Cell cell = row.createCell(columnCount);
                cell.setCellValue((String) headerDateCapital[columnCount]);
                cell.setCellStyle(styleHeading);
            }
			
            Row rowSummary = sheetRFQSummaryCapital.createRow(2);
            for (int columnCount=2; columnCount < headerRFQSummaryCapital.length; columnCount++) {
                Cell cell = rowSummary.createCell(columnCount);
                cell.setCellValue((String) headerRFQSummaryCapital[columnCount]);
                cell.setCellStyle(styleHeadingRFQSummaryCap);
            }
            //If user is having technical/commercial buyer role, display all RFQs which has all the departments under SC business unit name mentioned in page file.
            StringList slSCDepartments = new StringList();
            if(bIsTechCommericalUser){
            	Map mapSCDepartment = (Map)new TDRCapital_mxJPO(context,null).getListOfSCDepartments(context, null);
            	if(mapSCDepartment != null && mapSCDepartment.size()>0){
            		slSCDepartments = (StringList)mapSCDepartment.get("field_display_choices");
            	}            	
            }
            // Heading Row---End
            StringList slUserDepartments = null;
			HashMap mpRFQSummaryCap = new HashMap();
			HashMap mpRFQSummaryCapUserDept = new HashMap();
			String[] cellDateCapital = new String[headerDateCapital.length];			
            Row rowData = null;
			int iSize = mlInfo.size();
			Map mapObjectInfo = null;
			int iCountRow = 1;
			for(int iCount=0; iCount<iSize; iCount++){
				mapObjectInfo = (Map)mlInfo.get(iCount);
				strSC = (String)mapObjectInfo.get("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.name");
				if(!bIsTechCommericalUser || (bIsTechCommericalUser && slSCDepartments.contains(strSC))){//Check 
					strUserPerson = (String)mapObjectInfo.get(DomainObject.SELECT_OWNER);
					strRFQNo = (String)mapObjectInfo.get(DomainObject.SELECT_NAME);
					strRFQDate = (String)mapObjectInfo.get(DomainObject.SELECT_ORIGINATED);
					slPartDescription = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
					slPartNo = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.name");
					slBidderNames = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
					strTotalSourcesOnRFQ = slBidderNames != null ?  String.valueOf(slBidderNames.size()) : "0";
					slUserDepartments = getUserDepartments(context,strUserPerson);
					cellDateCapital[0] = iCountRow+""; //R No
					cellDateCapital[1] = (String)getStringWithSeperator(slUserDepartments); //User Dept
					cellDateCapital[2] = PersonUtil.getFullName(context, strUserPerson); //User Person
					cellDateCapital[3] = strSC; //SC
					cellDateCapital[4] = strRFQNo; //RFQ No
					cellDateCapital[5] = strRFQDate; //RFQ Date
					cellDateCapital[6] = getStringWithSeperator(slPartDescription); //Part Description
					cellDateCapital[7] = getStringWithSeperator(slPartNo); //Part No
					cellDateCapital[8] = strTotalSourcesOnRFQ; //Total Sources On RFQ
					cellDateCapital[9] = getStringWithSeperator(slBidderNames);  //Bidder Names
					
					strPendingCategory = (String)getPendingCategory(mapObjectInfo);
					cellDateCapital[10] = getPendingCategory(mapObjectInfo);  //PendingCategory
					cellDateCapital[11] = getPendingAt(context, mapObjectInfo);  //PendingAt
					cellDateCapital[12] = getPendingSinceAndDays(context,mapObjectInfo,"PendingSince");  //PendingSince
					cellDateCapital[13] = getPendingSinceAndDays(context,mapObjectInfo,"PendingDays");  //PendingDays
					cellDateCapital[14] = getActionToBeTaken(mapObjectInfo);  //ActionToBeTaken
					cellDateCapital[15] = ((String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]")).equals("Complete") ? "Yes" : "No";  //Bid Submitted to SC
					cellDateCapital[16] = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE+"]");  //RFQ Sent To SC Date : Send For Commercial Negotiation Date
					cellDateCapital[17] = (String)mapObjectInfo.get("attribute["+DomainObject.ATTRIBUTE_COMMENTS+"]");  //Remarks Critical Issue : Comments
					cellDateCapital[18] = PersonUtil.getFullName(context, (String)mapObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"].to.name"));  //SC Person : Commercial Ownership Taken)
					cellDateCapital[19] = getAwardedSuppliers(mapObjectInfo);  //Selected Vendor : Awarded Supplier Names
					cellDateCapital[20] = "";  //Source Selection Date : Make it empty 
					cellDateCapital[21] = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_PROJECT_HEAD+"]");  //Nature of Project : Project head attribute value on RFQ
					rowData = sheetDateCapital.createRow(iCountRow);
					for (int columnCount=0; columnCount < headerDateCapital.length; columnCount++) {
						Cell cell = rowData.createCell(columnCount);
						cell.setCellValue((String) cellDateCapital[columnCount]);
					}
					
					if(UIUtil.isNotNullAndNotEmpty(strSC) && UIUtil.isNotNullAndNotEmpty(strPendingCategory)){
						if(mpRFQSummaryCap.containsKey(strSC)){
							HashMap mSummaryMap = (HashMap)mpRFQSummaryCap.get(strSC);
							if(mSummaryMap.containsKey(strPendingCategory)){
								String sCountPendingCategory = (String)mSummaryMap.get(strPendingCategory);
								int iCountPendingCategory = Integer.parseInt(sCountPendingCategory) + 1;
								mSummaryMap.put(strPendingCategory,String.valueOf(iCountPendingCategory));
							}else{
								mSummaryMap.put(strPendingCategory,"1");
							}
							mpRFQSummaryCap.put(strSC,mSummaryMap);
						}else{
							HashMap mSummaryMap = new HashMap();
							mSummaryMap.put(strPendingCategory,"1");
							mpRFQSummaryCap.put(strSC,mSummaryMap);
						}					
					}
					
					//User Dept wise Report
					if(slUserDepartments != null && slUserDepartments.size()>0 && UIUtil.isNotNullAndNotEmpty(strPendingCategory)){
						if(mpRFQSummaryCapUserDept.containsKey(slUserDepartments.get(0))){
							HashMap mSummaryMap = (HashMap)mpRFQSummaryCapUserDept.get(slUserDepartments.get(0));
							if(mSummaryMap.containsKey(strPendingCategory)){
								String sCountPendingCategory = (String)mSummaryMap.get(strPendingCategory);
								int iCountPendingCategory = Integer.parseInt(sCountPendingCategory) + 1;
								mSummaryMap.put(strPendingCategory,String.valueOf(iCountPendingCategory));
							}else{
								mSummaryMap.put(strPendingCategory,"1");
							}
							mpRFQSummaryCapUserDept.put(slUserDepartments.get(0),mSummaryMap);
						}else{
							HashMap mSummaryMap = new HashMap();
							mSummaryMap.put(strPendingCategory,"1");
							mpRFQSummaryCapUserDept.put(slUserDepartments.get(0),mSummaryMap);
						}					
					}
					iCountRow = iCountRow + 1;
				}
			}
		/**Data (Cap)*****End*/
		
		/**RFQ Summary (Cap)*****Start*/
			int iTotalTechworkflow = 0;
			int iTotalQuote = 0;
			int iTotalUpdateTech = 0;
			int iTotalOpenquote = 0;
			int iTotalSCDashBoard = 0;
			int iTotalSCBuyer = 0;
			int iTotalUpdateBuyerResponse = 0;
			int iTotalSendPOVendor = 0;
			int iTotalComplete = 0;
			int iTotalGrandTotal = 0;
			String[] cellRFQSummaryCapital = new String[headerRFQSummaryCapital.length];
			if(mpRFQSummaryCap.size()>0){
				Row rRFQSummaryCap = null;
				rRFQSummaryCap = sheetRFQSummaryCapital.createRow(1);
				Cell cellRFQSummaryCap = rRFQSummaryCap.createCell(2);
				cellRFQSummaryCap.setCellValue("SC- Report");
				int iCellCount = 2;
				Iterator<Entry<String, HashMap>> it = mpRFQSummaryCap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, HashMap> pair = (Map.Entry<String, HashMap>) it.next();
					String strSCDept = (String)pair.getKey();
					HashMap hmSummary = (HashMap)pair.getValue(); 
					System.out.println(hmSummary);
					cellRFQSummaryCapital[2] = strSCDept;
					cellRFQSummaryCapital[3] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Tech workflow")) ? (String)hmSummary.get("Tech workflow") : "0";
					cellRFQSummaryCapital[4] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Quote/Requote by Vendor")) ? (String)hmSummary.get("Quote/Requote by Vendor") : "0";
					cellRFQSummaryCapital[5] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Update Tech Review Result")) ? (String)hmSummary.get("Update Tech Review Result") : "0";
					cellRFQSummaryCapital[6] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Open Quote")) ? (String)hmSummary.get("Open Quote") : "0";
					cellRFQSummaryCapital[7] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("SC dashboard")) ? (String)hmSummary.get("SC dashboard") : "0";
					cellRFQSummaryCapital[8] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("SC-Buyer")) ? (String)hmSummary.get("SC-Buyer") : "0";
					cellRFQSummaryCapital[9] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Update Buyer Response")) ? (String)hmSummary.get("Update Buyer Response") : "0";
					cellRFQSummaryCapital[10] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Send PO to Vendor")) ? (String)hmSummary.get("Send PO to Vendor") : "0";
					cellRFQSummaryCapital[11] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Complete")) ? (String)hmSummary.get("Complete") : "0";
					
					Iterator<Entry<String, String>> itr = hmSummary.entrySet().iterator();
					int iGranToatal = 0;
					while (itr.hasNext()) {
						Map.Entry<String, String> pair1 = (Map.Entry<String, String>) itr.next();
						if(UIUtil.isNotNullAndNotEmpty(pair1.getValue())){
							iGranToatal = iGranToatal + Integer.parseInt(pair1.getValue());
						}
					}
 
					//Grand Total Column
					cellRFQSummaryCapital[12] = String.valueOf(iGranToatal);
					rRFQSummaryCap = sheetRFQSummaryCapital.createRow(++iCellCount);
		            for (int columnCount=2; columnCount < cellRFQSummaryCapital.length; columnCount++) {
		            	cellRFQSummaryCap = rRFQSummaryCap.createCell(columnCount);
		            	cellRFQSummaryCap.setCellValue((String) cellRFQSummaryCapital[columnCount]);
		            	cellRFQSummaryCap.setCellStyle(styleBodyRFQSummaryCap);
		            }
		            
		            //Total Row
					iTotalTechworkflow = iTotalTechworkflow + Integer.parseInt(cellRFQSummaryCapital[3]);
					iTotalQuote = iTotalQuote + Integer.parseInt(cellRFQSummaryCapital[4]);
					iTotalUpdateTech = iTotalUpdateTech + Integer.parseInt(cellRFQSummaryCapital[5]);
					iTotalOpenquote = iTotalOpenquote + Integer.parseInt(cellRFQSummaryCapital[6]);
					iTotalSCDashBoard = iTotalSCDashBoard + Integer.parseInt(cellRFQSummaryCapital[7]);
					iTotalSCBuyer = iTotalSCBuyer + Integer.parseInt(cellRFQSummaryCapital[8]);
					iTotalUpdateBuyerResponse = iTotalUpdateBuyerResponse + Integer.parseInt(cellRFQSummaryCapital[9]);
					iTotalSendPOVendor = iTotalSendPOVendor + Integer.parseInt(cellRFQSummaryCapital[10]);
					iTotalComplete = iTotalComplete + Integer.parseInt(cellRFQSummaryCapital[11]);
					iTotalGrandTotal = iTotalGrandTotal + Integer.parseInt(cellRFQSummaryCapital[12]);
				}
				String[] cellRFQSummaryCapitalTotal = new String[headerRFQSummaryCapital.length];
				Row rRFQSummaryCapTotal = sheetRFQSummaryCapital.createRow(mpRFQSummaryCap.size()+3);
				cellRFQSummaryCapitalTotal[2]="Total";
				cellRFQSummaryCapitalTotal[3]=String.valueOf(iTotalTechworkflow);
				cellRFQSummaryCapitalTotal[4]=String.valueOf(iTotalQuote);
				cellRFQSummaryCapitalTotal[5]=String.valueOf(iTotalUpdateTech);
				cellRFQSummaryCapitalTotal[6]=String.valueOf(iTotalOpenquote);
				cellRFQSummaryCapitalTotal[7]=String.valueOf(iTotalSCDashBoard);
				cellRFQSummaryCapitalTotal[8]=String.valueOf(iTotalSCBuyer);
				cellRFQSummaryCapitalTotal[9]=String.valueOf(iTotalUpdateBuyerResponse);
				cellRFQSummaryCapitalTotal[10]=String.valueOf(iTotalSendPOVendor);
				cellRFQSummaryCapitalTotal[11]=String.valueOf(iTotalComplete);
				cellRFQSummaryCapitalTotal[12]=String.valueOf(iTotalGrandTotal);
	            for (int columnCount=2; columnCount < cellRFQSummaryCapital.length; columnCount++) {	            	
	            	Cell cellRFQSummaryCapTotal = rRFQSummaryCapTotal.createCell(columnCount);
	            	cellRFQSummaryCapTotal.setCellValue((String) cellRFQSummaryCapitalTotal[columnCount]);
	            	cellRFQSummaryCapTotal.setCellStyle(styleTotalRFQSummaryCap);
	            }
			}
			
			iTotalTechworkflow = 0;
			iTotalQuote = 0;
			iTotalUpdateTech = 0;
			iTotalOpenquote = 0;
			iTotalSCDashBoard = 0;
			iTotalSCBuyer = 0;
			iTotalUpdateBuyerResponse = 0;
			iTotalSendPOVendor = 0;
			iTotalComplete = 0;
			iTotalGrandTotal = 0;
			// user Department wise
			headerRFQSummaryCapital[1] = "Commer Dept.";
			headerRFQSummaryCapital[2] = "Tech. Dept";
			String[] cellUserDept = new String[headerRFQSummaryCapital.length];
			int iRow = mpRFQSummaryCap.size()+6;
			if(mpRFQSummaryCapUserDept.size()>0){
				Row rRFQSummaryCap = sheetRFQSummaryCapital.createRow(iRow);
				Cell cellRFQSummaryCap = rRFQSummaryCap.createCell(2);
				cellRFQSummaryCap.setCellValue("Technical User wise Report");
				Row rowDeptUser = sheetRFQSummaryCapital.createRow(iRow+1);
				for (int columnCount=2; columnCount < headerRFQSummaryCapital.length; columnCount++) {
					Cell cell = rowDeptUser.createCell(columnCount);
					cell.setCellValue((String) headerRFQSummaryCapital[columnCount]);
					cell.setCellStyle(styleHeadingRFQSummaryCap);
				}
				int iCellCount = iRow+1;
				Iterator<Entry<String, HashMap>> it = mpRFQSummaryCapUserDept.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, HashMap> pair = (Map.Entry<String, HashMap>) it.next();
					String strSCDept = (String)pair.getKey();
					HashMap hmSummary = (HashMap)pair.getValue(); 
					System.out.println(hmSummary);
					cellUserDept[2] = strSCDept;
					cellUserDept[3] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Tech workflow")) ? (String)hmSummary.get("Tech workflow") : "0";
					cellUserDept[4] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Quote/Requote by Vendor")) ? (String)hmSummary.get("Quote/Requote by Vendor") : "0";
					cellUserDept[5] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Update Tech Review Result")) ? (String)hmSummary.get("Update Tech Review Result") : "0";
					cellUserDept[6] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Open Quote")) ? (String)hmSummary.get("Open Quote") : "0";
					cellUserDept[7] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("SC dashboard")) ? (String)hmSummary.get("SC dashboard") : "0";
					cellUserDept[8] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("SC-Buyer")) ? (String)hmSummary.get("SC-Buyer") : "0";
					cellUserDept[9] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Update Buyer Response")) ? (String)hmSummary.get("Update Buyer Response") : "0";
					cellUserDept[10] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Send PO to Vendor")) ? (String)hmSummary.get("Send PO to Vendor") : "0";
					cellUserDept[11] = UIUtil.isNotNullAndNotEmpty((String)hmSummary.get("Complete")) ? (String)hmSummary.get("Complete") : "0";
					
					Iterator<Entry<String, String>> itr = hmSummary.entrySet().iterator();
					int iGranToatal = 0;
					while (itr.hasNext()) {
						Map.Entry<String, String> pair1 = (Map.Entry<String, String>) itr.next();
						if(UIUtil.isNotNullAndNotEmpty(pair1.getValue())){
							iGranToatal = iGranToatal + Integer.parseInt(pair1.getValue());
						}
					}
					
					//Grand Total Column
					cellUserDept[12] = String.valueOf(iGranToatal);
					rRFQSummaryCap = sheetRFQSummaryCapital.createRow(++iCellCount);
					for (int columnCount=2; columnCount < cellUserDept.length; columnCount++) {
						cellRFQSummaryCap = rRFQSummaryCap.createCell(columnCount);
						cellRFQSummaryCap.setCellValue((String) cellUserDept[columnCount]);
						cellRFQSummaryCap.setCellStyle(styleBodyRFQSummaryCap);
					}
					
					//Total Row
					iTotalTechworkflow = iTotalTechworkflow + Integer.parseInt(cellUserDept[3]);
					iTotalQuote = iTotalQuote + Integer.parseInt(cellUserDept[4]);
					iTotalUpdateTech = iTotalUpdateTech + Integer.parseInt(cellUserDept[5]);
					iTotalOpenquote = iTotalOpenquote + Integer.parseInt(cellUserDept[6]);
					iTotalSCDashBoard = iTotalSCDashBoard + Integer.parseInt(cellUserDept[7]);
					iTotalSCBuyer = iTotalSCBuyer + Integer.parseInt(cellUserDept[8]);
					iTotalUpdateBuyerResponse = iTotalUpdateBuyerResponse + Integer.parseInt(cellUserDept[9]);
					iTotalSendPOVendor = iTotalSendPOVendor + Integer.parseInt(cellUserDept[10]);
					iTotalComplete = iTotalComplete + Integer.parseInt(cellUserDept[11]);
					iTotalGrandTotal = iTotalGrandTotal + Integer.parseInt(cellUserDept[12]);
				}
				String[] cellRFQSummaryCapitalTotal = new String[headerRFQSummaryCapital.length];
				Row rRFQSummaryCapTotal = sheetRFQSummaryCapital.createRow(iRow+mpRFQSummaryCapUserDept.size()+2);
				cellRFQSummaryCapitalTotal[2]="Total";
				cellRFQSummaryCapitalTotal[3]=String.valueOf(iTotalTechworkflow);
				cellRFQSummaryCapitalTotal[4]=String.valueOf(iTotalQuote);
				cellRFQSummaryCapitalTotal[5]=String.valueOf(iTotalUpdateTech);
				cellRFQSummaryCapitalTotal[6]=String.valueOf(iTotalOpenquote);
				cellRFQSummaryCapitalTotal[7]=String.valueOf(iTotalSCDashBoard);
				cellRFQSummaryCapitalTotal[8]=String.valueOf(iTotalSCBuyer);
				cellRFQSummaryCapitalTotal[9]=String.valueOf(iTotalUpdateBuyerResponse);
				cellRFQSummaryCapitalTotal[10]=String.valueOf(iTotalSendPOVendor);
				cellRFQSummaryCapitalTotal[11]=String.valueOf(iTotalComplete);
				cellRFQSummaryCapitalTotal[12]=String.valueOf(iTotalGrandTotal);
				for (int columnCount=2; columnCount < cellUserDept.length; columnCount++) {	            	
					Cell cellRFQSummaryCapTotal = rRFQSummaryCapTotal.createCell(columnCount);
					cellRFQSummaryCapTotal.setCellValue((String) cellRFQSummaryCapitalTotal[columnCount]);
					cellRFQSummaryCapTotal.setCellStyle(styleTotalRFQSummaryCap);
				}
			}
		/**RFQ Summary (Cap)*****End*/
			outputStream = new FileOutputStream(new File(fileFolder+"\\"+fileName+ ".xlsx"));
	        workbook.write(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
	}
	private String getStringWithSeperator(StringList slInfoList)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		StringList slList = new StringList();
		if(slInfoList != null && slInfoList.size()>0){
			HashSet hsInfoList = new HashSet();
			hsInfoList.addAll(slInfoList);
			slList.addAll(hsInfoList);
			strReturn = FrameworkUtil.join(slList, ";");
		}
		return strReturn;
	}
	/**
	 * To get PendingCategory
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getPendingCategory(Map mapObjectInfo)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		String strCurrentState = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
		String strTechReviewStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
		String strCommercialOpenStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"]");
		String strHasCommercialBuyer = (String)mapObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"]");
		if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_STARTED) || strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){
			strReturn = "Tech workflow";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT)){
			strReturn = "Quote/Requote by Vendor";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strTechReviewStatus.equals("Complete")){
			strReturn = "Update Tech Review Result";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strCommercialOpenStatus.equals("Complete")){
			strReturn = "Open Quote";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && strHasCommercialBuyer.equals("False")){
			strReturn = "SC dashboard";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){
			strReturn = "Send PO to Vendor";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)){
			strReturn = "Complete";
		}
		return strReturn;
	}
	/**
	 * To get PendingAt
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getPendingAt(Context context, Map mapObjectInfo)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		String strCurrentState = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
		String strOwner = (String)mapObjectInfo.get(DomainObject.SELECT_OWNER);
		String strTechReviewStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
		String strCommercialOpenStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"]");
		String strHasCommercialBuyer = (String)mapObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"]");
		String strCommercialQuoteOpener = (String)mapObjectInfo.get("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.name");

		if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_STARTED)){
			strReturn = PersonUtil.getFullName(context, strOwner);
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){
			strReturn = getRouteDetails(context,mapObjectInfo,"state_InitialPackageReview","Assignee");
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT)){
			strReturn = "Vendors";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strTechReviewStatus.equals("Complete")){
			strReturn = PersonUtil.getFullName(context, strOwner);
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strCommercialOpenStatus.equals("Complete")){
			strReturn = PersonUtil.getFullName(context, strCommercialQuoteOpener);
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && strHasCommercialBuyer.equals("False")){
			strReturn = "SC dashboard";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){
			strReturn = getRouteDetails(context,mapObjectInfo,"state_FinalPackageReview","Assignee");
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)){
			strReturn = "";
		}
		return strReturn;
	}

	private String getRouteDetails(Context context, Map mapObjectInfo, String currentState,String param2)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		String strRouteId = DomainObject.EMPTY_STRING;
		DomainObject doRoute = DomainObject.newInstance(context);
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.name");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_PROJECT_ROUTE+"].to.name");
		try {			
			StringList slSelectable = new StringList();
			slSelectable.add("from["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.name");
			slSelectable.add("from["+DomainObject.RELATIONSHIP_PROJECT_ROUTE+"].to.name");
			StringList slRouteIds = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.id");
			StringList slRoutes = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_STATE+"]");
			StringList slRouteOriginted = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to."+DomainObject.SELECT_ORIGINATED);
			if(slRoutes != null && slRoutes.size()>0){
				int iSize = slRoutes.size();
				for(int iCount=0; iCount<iSize; iCount++){	
					if(slRoutes.get(iCount).equalsIgnoreCase(currentState) && "Originated".equalsIgnoreCase(param2)){
						strReturn = (String)slRouteOriginted.get(iCount);
					}
					if(slRoutes.get(iCount).equalsIgnoreCase(currentState)){
						strRouteId = slRouteIds.get(iCount);
						doRoute.setId(strRouteId);
						Map mapInfo = (Map)doRoute.getInfo(context, slSelectable);
						if("Assignee".equalsIgnoreCase(param2)){
							StringList slAssignees = (StringList)mapInfo.get("from["+DomainObject.RELATIONSHIP_PROJECT_ROUTE+"].to.name");
							if(slAssignees != null && slAssignees.size()>0 ){
								for(int jCount=0; jCount<slAssignees.size(); jCount++){	
									slAssignees.set(jCount, PersonUtil.getFullName(context, (String)slAssignees.get(jCount)));
								}
							}
							strReturn = getStringWithSeperator(slAssignees);
						}
						if("Task".equalsIgnoreCase(param2)){
							StringList slTasks = (StringList)mapInfo.get("from["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.name");
							strReturn = getStringWithSeperator(slTasks);
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_PROJECT_ROUTE+"].to.name");
		}
		return strReturn;
	}
	/**
	 * To get Pending since and pending days
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getPendingSinceAndDays(Context context, Map mapObjectInfo, String action)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		String strCurrentState = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
		String strTechReviewStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
		String strTechReviewCompleteionDate = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE+"]");
		String strQuoteOpenDate = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"]");
		String strCommercialOpenStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"]");
		String strHasCommercialBuyer = (String)mapObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"]");
		String strOriginated = (String)mapObjectInfo.get(DomainObject.SELECT_ORIGINATED);
		String strSentDate = (String)mapObjectInfo.get("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT+"].actual");
		String strResponseCompleteDate = (String)mapObjectInfo.get("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE+"].actual");
		if("PendingSince".equalsIgnoreCase(action))
		{			
			if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_STARTED)){
				strReturn = strOriginated;
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){
				strReturn = (String)getRouteDetails(context,mapObjectInfo,"state_InitialPackageReview","Originated");
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT)){
				strReturn = strSentDate;
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strTechReviewStatus.equals("Complete")){
				strReturn = strResponseCompleteDate;
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strCommercialOpenStatus.equals("Complete")){
				strReturn = strTechReviewCompleteionDate;
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && strHasCommercialBuyer.equals("False")){
				strReturn = strQuoteOpenDate;
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){
				strReturn = (String)getRouteDetails(context,mapObjectInfo,"state_FinalPackageReview","Originated");
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)){
				strReturn = "";
			}
		}
		else if("PendingDays".equalsIgnoreCase(action))
		{			
			if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_STARTED)){
				strReturn = getDayDiffrence(strOriginated);
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){
				strReturn = getDayDiffrence((String)getRouteDetails(context,mapObjectInfo,"state_InitialPackageReview","Originated"));
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT)){
				strReturn = getDayDiffrence(strSentDate);
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strTechReviewStatus.equals("Complete")){
				strReturn = getDayDiffrence(strResponseCompleteDate);
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strCommercialOpenStatus.equals("Complete")){
				strReturn = getDayDiffrence(strTechReviewCompleteionDate);
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && strHasCommercialBuyer.equals("False")){
				strReturn = getDayDiffrence(strQuoteOpenDate);
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){
				strReturn = getDayDiffrence((String)getRouteDetails(context,mapObjectInfo,"state_FinalPackageReview","Originated"));
			}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)){
				strReturn = "";
			}
		}
		return strReturn;
	}
	
	/**
	 * To get day difference between two dates
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getDayDiffrence(String strDateAssigned)
	{
		String strReturn = DomainObject.EMPTY_STRING;
		if(UIUtil.isNotNullAndNotEmpty(strDateAssigned)){    		
			Date dAssignedDate = eMatrixDateFormat.getJavaDate(strDateAssigned);
			Date dtToday = new Date();
			long diff = dtToday.getTime() - dAssignedDate.getTime();
			int days = (int) TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
			strReturn = String.valueOf(days);
		}
		return strReturn;
	}
	/**
	 * To get day difference between two dates
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getDayDiffrence(String strStartDate, String strEndDate)
	{
		String strReturn = DomainObject.EMPTY_STRING;
		if(UIUtil.isNotNullAndNotEmpty(strStartDate) && UIUtil.isNotNullAndNotEmpty(strEndDate)){    		
			Date dStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
			Date dEndDate = eMatrixDateFormat.getJavaDate(strStartDate);
			long diff = dEndDate.getTime() - dStartDate.getTime();
			int days = (int) TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
			strReturn = String.valueOf(days);
		}
		return strReturn;
	}


	/**
	 * To get Action to be taken
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getActionToBeTaken(Map mapObjectInfo)throws Exception {
		String strReturn = DomainObject.EMPTY_STRING;
		String strCurrentState = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
		String strOwner = (String)mapObjectInfo.get(DomainObject.SELECT_OWNER);
		String strTechReviewStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_TECH_REVIEW_STATUS+"]");
		String strCommercialOpenStatus = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS+"]");
		String strHasCommercialBuyer = (String)mapObjectInfo.get("from["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_BUYER+"]");
		String strCommercialQuoteOpener = (String)mapObjectInfo.get("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER+"].from.name");
		StringList slInitialReviewTaskAssignees = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.from["+DomainObject.RELATIONSHIP_ROUTE_NODE+"].to.name");
		StringList slRouteBaseState = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_STATE+"]");
		StringList slRouteOriginted = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to."+DomainObject.SELECT_ORIGINATED);
		if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_STARTED)){
			strReturn = "Submit for Approval";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_INITIAL_REVIEW)){
			strReturn = getStringWithSeperator(slInitialReviewTaskAssignees);//Show Task Action Details
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_SENT)){
			strReturn = "Submit Quotations";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strTechReviewStatus.equals("Complete")){
			strReturn = "Update Tech Review Result";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && !strCommercialOpenStatus.equals("Complete")){
			strReturn = "To Open Quote";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE) && strHasCommercialBuyer.equals("False")){
			strReturn = "Take Commercial Ownership";
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)){
			strReturn = getStringWithSeperator(slInitialReviewTaskAssignees);//Show Task Action Details
		}else if(strCurrentState.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)){
			strReturn = "";
		}
		return strReturn;
	}
	/**
	 * To get Awarded Suppliers
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private String getAwardedSuppliers(Map mapObjectInfo)throws Exception {
		StringBuffer strReturn = new StringBuffer();
		StringList slAwarded = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+DomainObject.ATTRIBUTE_AWARD_STATUS+"]");
		StringList slAwardedSuppliers = (StringList)mapObjectInfo.get("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].to.from["+DomainObject.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
		if(slAwarded != null && slAwarded.size()>0){
			int iSize=slAwarded.size();
			for(int iCount=0; iCount<iSize; iCount++){	
				if(slAwarded.get(iCount).equalsIgnoreCase("Awarded")){					
					strReturn.append((String)slAwardedSuppliers.get(iCount));
					if(iCount != (iSize-1)){
						strReturn.append(";");
					}
				}
			}
		}
		return strReturn.toString();
	}
	/**
	 * To get user departments
	 * @param slInfoList
	 * @return
	 * @throws Exception
	 */
	private StringList getUserDepartments(Context context,String strPerson)throws Exception {
		StringList slDepartmentList = new StringList();
		if(UIUtil.isNotNullAndNotEmpty(strPerson)){
			String strPersonObjectId = (String)PersonUtil.getPersonObjectID(context, strPerson);
			DomainObject doPerson = new DomainObject(strPersonObjectId);
			slDepartmentList = (StringList)doPerson.getInfoList(context, "to["+DomainObject.RELATIONSHIP_MEMBER+"].from["+DomainObject.TYPE_DEPARTMENT+"].name");
		}
		return slDepartmentList;
	}
	public String generateCommercialTrackingReport(Context context, String[] args)throws Exception{
		String strReturnStatus = "Failure";

		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
		DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE+"]");
		try{
			String strFolderName = context.createWorkspace(); 
			System.out.println("Workspace Location >>>>> "+ strFolderName);
			String strFileName = _classCurrencyConfig.getProperty("TDR.CommercialReport.DocumentName");

			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String strStartDate = (String)programMap.get("StartDate");
			String strEndDate = (String)programMap.get("EndDate");
			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value >='"+strStartDate+"'");
			sbWhere.append(" && ");
			sbWhere.append("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value <='"+strEndDate+"'");
			String strWhere = "revision ==  last && attribute[TDRCommercialOpenStatus] == Complete && (current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_RESPONSE_COMPLETE +"' || current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW +"' || current == '"+DomainObject.STATE_RTS_CANCELLED_CANCELLED +"' || current == '"+DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE +"')";
			sbWhere.append("&& ( "+strWhere+")");

			StringList  objectSelects =  new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			objectSelects.add(DomainObject.SELECT_CURRENT);
			objectSelects.add(DomainObject.SELECT_OWNER);
			objectSelects.add(DomainObject.SELECT_NAME);
			objectSelects.add(DomainObject.SELECT_ORIGINATED);
			objectSelects.add("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW+"].actual");
			objectSelects.add("current["+DomainObject.STATE_RTS_CANCELLED_CANCELLED+"].actual");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
			objectSelects.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value");
			objectSelects.add("to["+TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT+"].from.name");
			MapList mlRFQ = DomainObject.findObjects(context, DomainObject.TYPE_RFQ, DomainObject.QUERY_WILDCARD, DomainObject.QUERY_WILDCARD, DomainObject.QUERY_WILDCARD, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION, sbWhere.toString(), "", true, objectSelects, (short)0);

			Map mAgingSummaryMap = getCommercialAgingSummary(context,mlRFQ);			
			Map mCommercialTrackingSummary = getTrackingSummary(context, mlRFQ);

			if(mlRFQ != null && mlRFQ.size()>0){
				writeXlsxForCommercialDocument(context,mlRFQ,mAgingSummaryMap,mCommercialTrackingSummary,strFolderName,strFileName);
				try {					
					DomainObject doNewDoc = DomainObject.newInstance(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT);
					BusinessObject dom= new BusinessObject(TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT,strFileName,"1","");
					if(dom.exists(context)){
						doNewDoc.setId((String)dom.getObjectId(context));
					}else{
						doNewDoc.createObject(context, TDRConstants_mxJPO.TYPE_TDR_INTERNAL_RFQ_DOCUMENT, strFileName, "1", TDRConstants_mxJPO.POLICY_TDR_INTERNAL_DOCUMENT, TDRConstants_mxJPO.VAULT_E_SERVICE_PRODUCTION);							
					}

					doNewDoc.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, strFileName);
					ContextUtil.pushContext(context);
					doNewDoc.checkinFile(context, true, true, "", DomainConstants.FORMAT_GENERIC, strFileName+".xlsx", strFolderName);
					ContextUtil.popContext(context);
					strReturnStatus = (String)doNewDoc.getObjectId(context);
				} catch (Exception e) {
					System.out.println("TDRGCapitalReport : generateCommercialTrackingReport() "+e.getMessage());
				}
			}	


		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally {
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
			DomainObject.MULTI_VALUE_LIST.remove("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE+"]");
		}
		return strReturnStatus;

	}

	private void writeXlsxForCommercialDocument(Context context, MapList mlInfo,Map mAgingSummaryMap,Map mCommercialTrackingSummary,String fileFolder, String fileName)throws Exception {
		FileOutputStream outputStream = null;
		String strUserPerson = DomainObject.EMPTY_STRING;
		String strSC = DomainObject.EMPTY_STRING;
		String strRFQNo = DomainObject.EMPTY_STRING;
		String strRFQId = DomainObject.EMPTY_STRING;
		String strRFQDate = DomainObject.EMPTY_STRING;
		StringList slPartDescription = null;
		StringList slPartNo = null;
		StringList slAwardDates = null;
		String strTotalSourcesOnRFQ = DomainObject.EMPTY_STRING;
		StringList slBidderNames = null;
		String strSingleMultiBidder = DomainObject.EMPTY_STRING;	
		String strBidOpenDate = DomainObject.EMPTY_STRING;	
		String strL1Vendor = DomainObject.EMPTY_STRING;	
		String strRBIRate = DomainObject.EMPTY_STRING;
		String strCBDSRate = DomainObject.EMPTY_STRING;
		String strBudget = DomainObject.EMPTY_STRING;
		String strCancelledDate = DomainObject.EMPTY_STRING;
		String strCurrent = DomainObject.EMPTY_STRING;
		String strAwardDate = DomainObject.EMPTY_STRING;
		String strFinalReviewDate = DomainObject.EMPTY_STRING;
		String strNoOfAwardDays = DomainObject.EMPTY_STRING;
		String strNoOfFinalReviewDays = DomainObject.EMPTY_STRING;
		String strAgingDays = DomainObject.EMPTY_STRING;
		Map mCostInfo = null;

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			/**Data (Cap)*****Start*/	
			XSSFSheet sheetDataCommercial = workbook.createSheet("Data");
			String[] headerDataCommercial = {"S No", "User Dept", "SC", "RFQ No", "RFQ Date", "Item Description", "Item No", "Total Sources On RFQ","Bidder Names","Single Bidder/Multiple Bidder","Bids opened date","L1 Vendor","Sum Total of Line Item cost as per RBI rate","Sum Total of Line Item cost as per CBDS rate","MSIL Estimate Total, INR","Cancelled date","Award Date","Final Review Initiation Date","No. of Days(upto Award date)","No. of Days(Award Date to Final Review Initiation)","Aging Days"};


			XSSFSheet sheetRFQCommercialSummary = workbook.createSheet("Commercial Tracking Summary");
			String[] headerRFQCommercialSummary1 = {"Month","OPENING BALANCE","","RECIEVED","","WITHDRAWN","","NET REVIEVED","","PO RINGI INITIATED","","","","","","SINGLE BIDDER RFQ (CLOSED)","","","","MULTI PARTY RFQ (CLOSED)","","","","Average Days (for Ringi Approved Cases)",""};
			String[] headerRFQCommercialSummary2 = {"","No.","MRs","No.","MRs","No.","MRs","No.","MRs","No.","MRs(Initial)","MRs(Final)","MRs(Estimate)","Discount","Discount(w.r.t Estimate)","No.","MRs(Initial)","MRs(Final)","Discount","No.","MRs(Initial)","MRs(Final)","Discount","Bid Openng to Ringi Initiation","Ringi Initiation to Ringi Approval"};


			XSSFSheet sheetRFQAgingSummary = workbook.createSheet("RFQ Aging Summary");
			String[] headerRFQAgingSummary1 = {"`As on last completed month (Buyer response as on last date of completed month)","","","","","As on Report generation date","",""};
			String[] headerRFQAgingSummary2 = {"No of RFQs Pending","","","","","No of RFQs Pending","",""};
			String[] headerRFQAgingSummary3 = {"< =30 days","> 30 days and <45 days","> 45 days","","","< =30 days","> 30 days and <45 days","> 45 days"};

			CellStyle styleHeading = workbook.createCellStyle();
			styleHeading.setBorderLeft(CellStyle.ALIGN_CENTER);
			styleHeading.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			styleHeading.setFillPattern(CellStyle.SOLID_FOREGROUND); 
			styleHeading.setBorderLeft(CellStyle.BORDER_THIN);
			styleHeading.setBorderRight(CellStyle.BORDER_THIN);
			styleHeading.setBorderTop(CellStyle.BORDER_THIN);
			styleHeading.setBorderBottom(CellStyle.BORDER_THIN);
			styleHeading.setAlignment(CellStyle.ALIGN_CENTER);

			CellStyle styleHeadingRFQSummaryCap = workbook.createCellStyle();
			styleHeadingRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);

			XSSFFont font= workbook.createFont();
			font.setFontHeightInPoints((short)10);
			font.setFontName("Arial");
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);
			styleHeadingRFQSummaryCap.setFont(font);
			styleHeadingRFQSummaryCap.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
			styleHeadingRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
			styleHeadingRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
			styleHeadingRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
			styleHeadingRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
			styleHeadingRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);

			CellStyle styleBodyRFQSummaryCap = workbook.createCellStyle();
			styleBodyRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);
			styleBodyRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
			styleBodyRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
			styleBodyRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
			styleBodyRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
			styleBodyRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);

			CellStyle styleTotalRFQSummaryCap = workbook.createCellStyle();
			styleTotalRFQSummaryCap.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
			styleTotalRFQSummaryCap.setBorderLeft(CellStyle.ALIGN_CENTER);
			styleTotalRFQSummaryCap.setFillPattern(CellStyle.SOLID_FOREGROUND); 
			styleTotalRFQSummaryCap.setBorderLeft(CellStyle.BORDER_THIN);
			styleTotalRFQSummaryCap.setBorderRight(CellStyle.BORDER_THIN);
			styleTotalRFQSummaryCap.setBorderTop(CellStyle.BORDER_THIN);
			styleTotalRFQSummaryCap.setBorderBottom(CellStyle.BORDER_THIN);


			Row row = sheetDataCommercial.createRow(0);
			for (int columnCount=0; columnCount < headerDataCommercial.length; columnCount++) {
				Cell cell = row.createCell(columnCount);
				cell.setCellValue((String) headerDataCommercial[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			Row row1 = sheetRFQAgingSummary.createRow(5);
			for (int columnCount=0; columnCount < headerRFQAgingSummary1.length; columnCount++) {
				Cell cell = row1.createCell(columnCount);
				cell.setCellValue((String) headerRFQAgingSummary1[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			Row row2 = sheetRFQAgingSummary.createRow(6);
			for (int columnCount=0; columnCount < headerRFQAgingSummary2.length; columnCount++) {
				Cell cell = row2.createCell(columnCount);
				cell.setCellValue((String) headerRFQAgingSummary2[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			Row row3 = sheetRFQAgingSummary.createRow(7);
			for (int columnCount=0; columnCount < headerRFQAgingSummary3.length; columnCount++) {
				Cell cell = row3.createCell(columnCount);
				cell.setCellValue((String) headerRFQAgingSummary3[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			sheetRFQAgingSummary.addMergedRegion(new CellRangeAddress(5, 5, 0, 2));
			sheetRFQAgingSummary.addMergedRegion(new CellRangeAddress(5, 5, 5, 7));
			sheetRFQAgingSummary.addMergedRegion(new CellRangeAddress(6, 6, 0, 2));
			sheetRFQAgingSummary.addMergedRegion(new CellRangeAddress(6, 6, 5, 7));


			Row row4 = sheetRFQCommercialSummary.createRow(3);
			for (int columnCount=0; columnCount < headerRFQCommercialSummary1.length; columnCount++) {
				Cell cell = row4.createCell(columnCount);
				cell.setCellValue((String) headerRFQCommercialSummary1[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			Row row5 = sheetRFQCommercialSummary.createRow(4);
			for (int columnCount=0; columnCount < headerRFQCommercialSummary2.length; columnCount++) {
				Cell cell = row5.createCell(columnCount);
				cell.setCellValue((String) headerRFQCommercialSummary2[columnCount]);
				cell.setCellStyle(styleHeading);
			}

			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 4, 0, 0));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 1, 2));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 3, 4));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 5, 6));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 7, 8));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 9, 14));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 15, 18));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 19, 22));
			sheetRFQCommercialSummary.addMergedRegion(new CellRangeAddress(3, 3, 23, 24));

			HashMap mpRFQSummaryCap = new HashMap();
			String[] cellDateCapital = new String[headerDataCommercial.length];			
			Row rowData = null;
			int iSize = mlInfo.size();
			Map mapObjectInfo = null;

			//Commercial RFQ Data -Start
			for(int iCount=0; iCount<iSize; iCount++){
				mapObjectInfo = (Map)mlInfo.get(iCount);

				strUserPerson = (String)mapObjectInfo.get(DomainObject.SELECT_OWNER);
				strSC = (String)mapObjectInfo.get("to[" + TDRConstants_mxJPO.RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT + "].from.name");
				strRFQNo = (String)mapObjectInfo.get(DomainObject.SELECT_NAME);
				strRFQId = (String)mapObjectInfo.get(DomainObject.SELECT_ID);
				strRFQDate = (String)mapObjectInfo.get(DomainObject.SELECT_ORIGINATED);

				DomainObject doObject = DomainObject.newInstance(context);
				doObject.setId(strRFQId);

				DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
				DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
				DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
				DomainObject.MULTI_VALUE_LIST.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE+"]");

				StringList slSelect = new StringList();
				slSelect.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
				slSelect.add("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
				slSelect.add("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
				slSelect.add("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE+"].value");

				Map mRFQInfo = doObject.getInfo(context, slSelect);

				slPartDescription = (StringList)mRFQInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.description");
				slPartNo = (StringList)mRFQInfo.get("from["+DomainObject.RELATIONSHIP_LINE_ITEM+"].to.attribute["+DomainConstants.ATTRIBUTE_ENTERED_NAME+"].value");
				slBidderNames = (StringList)mRFQInfo.get("from["+DomainObject.RELATIONSHIP_RTS_SUPPLIER+"].to.name");
				strTotalSourcesOnRFQ = slBidderNames != null ?  String.valueOf(slBidderNames.size()) : "0";
				strBidOpenDate = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value"); 
				strBudget = (String)mapObjectInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
				strCurrent = (String)mapObjectInfo.get(DomainConstants.SELECT_CURRENT);
				if(DomainConstants.STATE_RTS_CANCELLED_CANCELLED.equals(strCurrent)) {
					strCancelledDate = (String)mapObjectInfo.get("current["+DomainObject.STATE_RTS_CANCELLED_CANCELLED+"].actual");
				}else {
					strCancelledDate = DomainObject.EMPTY_STRING;
				}

				Object awardDates = (Object)mRFQInfo.get("from["+DomainObject.RELATIONSHIP_RTS_QUOTATION+"].attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_AWARD_DATE+"].value");
				if(null != slAwardDates && awardDates instanceof StringList) {
					slAwardDates = (StringList) awardDates;
					for(int i=0;i<slAwardDates.size();i++) {
						strAwardDate = (String)slAwardDates.get(i);
						if(UIUtil.isNotNullAndNotEmpty(strAwardDate)) {
							break;
						}else {
							strAwardDate = DomainObject.EMPTY_STRING;
						}
					}
				}else if(null != slAwardDates && awardDates instanceof String) {
					strAwardDate = (String) awardDates;
				}

				strFinalReviewDate = (String)mapObjectInfo.get("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW+"].actual");

				cellDateCapital[0] = iCount+1+""; //R No
				
				StringList slUserDepts = getUserDepartments(context,strUserPerson);
				String strDept = DomainConstants.EMPTY_STRING;
				for(int k=0;k<slUserDepts.size();k++) {
					if(k==0) {
						strDept = slUserDepts.get(k);
					}else {
						strDept = strDept +","+slUserDepts.get(k);
					}
				}

				cellDateCapital[1] = strDept; //User Dept
				cellDateCapital[2] = strSC; //SC
				cellDateCapital[3] = strRFQNo; //RFQ No
				cellDateCapital[4] = strRFQDate; //RFQ Date
				cellDateCapital[5] = getStringWithSeperator(slPartDescription); //Part Description
				cellDateCapital[6] = getStringWithSeperator(slPartNo); //Part No
				cellDateCapital[7] = strTotalSourcesOnRFQ; //Total Sources On RFQ
				cellDateCapital[8] = getStringWithSeperator(slBidderNames);  //Bidder Names

				if(null != slBidderNames) {
					if(slBidderNames.size()>1) {
						strSingleMultiBidder = "Multi";
					}else if(slBidderNames.size()== 1){
						strSingleMultiBidder = "Single";
					}
				}
				cellDateCapital[9] =strSingleMultiBidder;  //Single or Multi bidder
				cellDateCapital[10] = strBidOpenDate;

				mCostInfo = getCostInfo(context, strRFQId);
				if(null !=mCostInfo && mCostInfo.isEmpty() == false) {
					strL1Vendor = (String)mCostInfo.get("LIVendor");
					strRBIRate = (String)mCostInfo.get("RBIRate");
					strCBDSRate = (String)mCostInfo.get("CBDSRate");
				}

				cellDateCapital[11] = strL1Vendor;
				cellDateCapital[12] = strRBIRate;
				cellDateCapital[13] = strCBDSRate;
				cellDateCapital[14] = strBudget;
				cellDateCapital[15] = strCancelledDate;
				cellDateCapital[16] = strAwardDate;
				cellDateCapital[17] = strFinalReviewDate;
				cellDateCapital[18] = getDayDiffrence(strAwardDate);
				cellDateCapital[19] = getDayDiffrence(strFinalReviewDate,strAwardDate); 
				cellDateCapital[20] = getDayDiffrence(strBidOpenDate);
				rowData = sheetDataCommercial.createRow(iCount+1);
				for (int columnCount=0; columnCount < headerDataCommercial.length; columnCount++) {
					Cell cell = rowData.createCell(columnCount);
					cell.setCellValue((String) cellDateCapital[columnCount]);
				}


			}
			//Commercial RFQ Data -End

			//RFQ Aging Summary Start
			Row rowDataAging = sheetRFQAgingSummary.createRow(8);
			String[] cellRFQAging = new String[headerRFQAgingSummary3.length];
			cellRFQAging[0] = (String) mAgingSummaryMap.get("TodayLessThan30");
			cellRFQAging[1] = (String) mAgingSummaryMap.get("TodayLessThan45");
			cellRFQAging[2] = (String) mAgingSummaryMap.get("ToayMoreThan45");
			cellRFQAging[3] = DomainConstants.EMPTY_STRING;
			cellRFQAging[4] = DomainConstants.EMPTY_STRING;
			cellRFQAging[5] = (String) mAgingSummaryMap.get("TodayLessThan30");
			cellRFQAging[6] = (String) mAgingSummaryMap.get("LastMonthLessThan45");
			cellRFQAging[7] = (String) mAgingSummaryMap.get("LastMonthMoreThan30");

			for (int columnCount=0; columnCount < headerRFQAgingSummary3.length; columnCount++) {
				Cell cell = rowDataAging.createCell(columnCount);
				cell.setCellValue((String) cellRFQAging[columnCount]);
			}
			//RFQ Aging Summary End

			//RFQ Commercial Summary -Start
			String[] cellCommercial = new String[headerRFQCommercialSummary2.length];
			HashMap hmDataMap = null;
			Double dDevisionFactor = 1000000.0D;
			String strMonth =DomainConstants.EMPTY_STRING;
			Iterator itrOwnerMap = mCommercialTrackingSummary.keySet().iterator();
			int rowCtr = 0;
			while(itrOwnerMap.hasNext()) {		
//				hmDataMap = (HashMap) mCommercialTrackingSummary.get(String.valueOf(i));
				String key = (String) itrOwnerMap.next();
				hmDataMap = (HashMap)mCommercialTrackingSummary.get(key);
				if(hmDataMap != null && hmDataMap.isEmpty()==false) {
					rowCtr++;
					if("0".equals(key)) 
						strMonth = "January";
					if("1".equals(key)) 
						strMonth = "February";
					if("2".equals(key)) 
						strMonth = "March";
					if("3".equals(key)) 
						strMonth = "April";
					if("4".equals(key)) 
						strMonth = "May";
					if("5".equals(key)) 
						strMonth = "June";
					if("6".equals(key)) 
						strMonth = "July";
					if("7".equals(key)) 
						strMonth = "August";
					if("8".equals(key)) 
						strMonth = "September";
					if("9".equals(key)) 
						strMonth = "October";
					if("10".equals(key)) 
						strMonth = "November";
					if("11".equals(key)) 
						strMonth = "December";
						
					cellCommercial[0] = strMonth;
					cellCommercial[1] = (String)hmDataMap.get("OpenCount");
					cellCommercial[2] = (String)hmDataMap.get("OpeningBalance");
					
					String strRecCnt = (String)hmDataMap.get("RecievedCount");
					if(UIUtil.isNullOrEmpty(strRecCnt))
						strRecCnt = "0";
					cellCommercial[3] = strRecCnt;
					String strRec = (String)hmDataMap.get("Recieved");
					if(UIUtil.isNullOrEmpty(strRec)) {
						strRec = "0";
					}
					cellCommercial[4] = String.valueOf(Double.valueOf(strRec)/dDevisionFactor);
					String witDrawnCnt = (String)hmDataMap.get("WithdrawnCount");
					if(UIUtil.isNullOrEmpty(witDrawnCnt))
						witDrawnCnt = "0";
					cellCommercial[5] = witDrawnCnt;
					String strWithDrawn = (String)hmDataMap.get("Withdrawn");
					if(UIUtil.isNullOrEmpty(strWithDrawn)) {
						strWithDrawn = "0";
					}
					cellCommercial[6] = String.valueOf(Double.valueOf(strWithDrawn)/dDevisionFactor);

					String strNetCount = String.valueOf(Integer.valueOf(strRecCnt)- Integer.valueOf(strWithDrawn));
					cellCommercial[7] = strNetCount;
					String strNetAmount = String.valueOf((Double.valueOf(strRec)- Double.valueOf(strWithDrawn))/dDevisionFactor);
					cellCommercial[8] = strNetAmount;

					String strPOInitiated = (String)hmDataMap.get("POInitiated");
					if(UIUtil.isNullOrEmpty(strPOInitiated)) {
						strPOInitiated = "0";
					}
					cellCommercial[9] = strPOInitiated;
					String strInitialBudget = (String)hmDataMap.get("InitialBudget");
					if(UIUtil.isNullOrEmpty(strInitialBudget)) {
						strInitialBudget = "0";
					}
					String strPOInitialMRs = String.valueOf(Double.valueOf(strInitialBudget)/dDevisionFactor);
					cellCommercial[10] = strPOInitialMRs;
					
					String strFinalBudget = (String)hmDataMap.get("FinalBudget");
					if(UIUtil.isNullOrEmpty(strFinalBudget)) {
						strFinalBudget = "0";
					}
					String strPOFinalMRs = String.valueOf(Double.valueOf(strFinalBudget)/dDevisionFactor);
					cellCommercial[11] = strPOFinalMRs;
					
					cellCommercial[12] = (String)hmDataMap.get("Budget");
					
					String strDiscount = String.valueOf(Double.valueOf(strPOInitialMRs)-Double.valueOf(strPOFinalMRs));
					cellCommercial[13] = strDiscount;
					
					String strBudgetvalue = (String)hmDataMap.get("Budget");
					if(UIUtil.isNullOrEmpty(strBudgetvalue)) {
						strBudgetvalue = "0";
					}
					String strDiscountWRTEstimate = String.valueOf(Double.valueOf(strBudgetvalue)-Double.valueOf(strPOFinalMRs));
					cellCommercial[14] = strDiscountWRTEstimate;
					
					cellCommercial[15] = (String)hmDataMap.get("SingleBidCnt");
					String strSingleBidFirstBudget = (String)hmDataMap.get("SingleBidBudget");
					if(UIUtil.isNullOrEmpty(strSingleBidFirstBudget)) {
						strSingleBidFirstBudget = "0";
					}
					String strSingleBidBudget = String.valueOf(Double.valueOf(strSingleBidFirstBudget)/dDevisionFactor);
					cellCommercial[16] = strSingleBidBudget;
					
					String strSingleBidFinalBudget = (String)hmDataMap.get("SingleBidFinal");
					if(UIUtil.isNullOrEmpty(strSingleBidFinalBudget)) {
						strSingleBidFinalBudget = "0";
					}
					String strSingleBidFinal = String.valueOf(Double.valueOf(strSingleBidFinalBudget)/dDevisionFactor);
					cellCommercial[17] = strSingleBidFinal;
					
					cellCommercial[18] = String.valueOf((Double.valueOf(strSingleBidBudget)-Double.valueOf(strSingleBidFinal)));
					
					
					cellCommercial[19] = (String)hmDataMap.get("MultiBidCnt");
					String strFinalBidBudget = (String)mCommercialTrackingSummary.get("MultiBidBudget");
					if(UIUtil.isNullOrEmpty(strFinalBidBudget)) {
						strFinalBidBudget = "0";
					}
						
					String strMultiBidBudget = String.valueOf(Double.valueOf(strFinalBidBudget)/dDevisionFactor);
					cellCommercial[20] = strMultiBidBudget;
					
					String strFinalMultiBudget = (String)hmDataMap.get("MultiBidFinal");
					if(UIUtil.isNullOrEmpty(strFinalMultiBudget)) {
						strFinalMultiBudget = "0";
					}
					String strMultiBidFinal = String.valueOf(Double.valueOf(strFinalMultiBudget)/dDevisionFactor);
					cellCommercial[21] = strMultiBidFinal;
					
					cellCommercial[22] = String.valueOf((Double.valueOf(strMultiBidBudget)-Double.valueOf(strMultiBidFinal)));
					
					if("0".equals(strPOInitiated)) {
						strPOInitiated = "1";
					}
					String strNoOfRFQs = strPOInitiated;
					String strRingiDays = (String)hmDataMap.get("BidToRingiDays");
					if(UIUtil.isNullOrEmpty(strRingiDays)) {
						strRingiDays = "0";
					}
					String strBidToRingi = String.valueOf(Double.valueOf(strRingiDays)/Double.valueOf(strNoOfRFQs));
					cellCommercial[23] = strBidToRingi;
					
					String strRingiApprovedDays = (String)hmDataMap.get("RingiToApprovalDays");
					if(UIUtil.isNullOrEmpty(strRingiApprovedDays)) {
						strRingiApprovedDays = "0";
					}
					String strRingiToApproval = String.valueOf(Double.valueOf(strRingiApprovedDays)/Double.valueOf(strNoOfRFQs));
					cellCommercial[24] = strRingiToApproval;
					
					Row rowCommercial = sheetRFQCommercialSummary .createRow(4+rowCtr);
					for (int columnCount=0; columnCount < headerRFQCommercialSummary2.length; columnCount++) {
						Cell cell = rowCommercial.createCell(columnCount);
						cell.setCellValue((String) cellCommercial[columnCount]);
					}
				}
			}
			//RFQ Commercial Summary -End

			outputStream = new FileOutputStream(new File(fileFolder+"\\"+fileName+ ".xlsx"));
			workbook.write(outputStream);

		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}	


	private Map getCostInfo(Context context, String strRFQID)throws Exception{
		Map mInfo = new HashMap();
		String strL1Vendor = DomainConstants.EMPTY_STRING;
		String strRBIRate = DomainConstants.EMPTY_STRING;
		String strCBDSRate = DomainConstants.EMPTY_STRING;
		String strBudget = DomainConstants.EMPTY_STRING;
		String strBidders = DomainConstants.EMPTY_STRING;

		double dTempRBI = 0.0D;
		double dTempCBDS = 0.0D;
		try {
			if(UIUtil.isNotNullAndNotEmpty(strRFQID)) {
				DomainObject doRFQ = new DomainObject(strRFQID);

				StringList slRFQSelect = new StringList();
				slRFQSelect.add("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				slRFQSelect.add("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
				slRFQSelect.add("from["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].to.id");
				Map mRFQInfo = doRFQ.getInfo(context, slRFQSelect);

				String strRBIFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA+"].value");
				String strCBDSFormula = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA+"].value");
				String strCurrency = (String)mRFQInfo.get("attribute["+DomainConstants.ATTRIBUTE_CURRENCY+"].value");
				strBudget = (String)mRFQInfo.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");

				if(UIUtil.isNullOrEmpty(strRBIFormula)) {
					strRBIFormula = DomainConstants.EMPTY_STRING;
				}
				if(UIUtil.isNullOrEmpty(strCBDSFormula)) {
					strCBDSFormula = DomainConstants.EMPTY_STRING;
				}


				String strQuoteId = DomainConstants.EMPTY_STRING;
				StringList slQuotationIds = new StringList();
				Object oQuotation = (Object)mRFQInfo.get("from["+DomainRelationship.RELATIONSHIP_RTS_QUOTATION+"].to.id");
				if(null != oQuotation && oQuotation instanceof String) {
					strQuoteId = (String)oQuotation;
				}
				if(null != oQuotation && oQuotation instanceof StringList) {
					slQuotationIds = (StringList)oQuotation;
				}
				String strVendorName = DomainConstants.EMPTY_STRING;
				String strLiId = DomainConstants.EMPTY_STRING;
				DomainObject doQuote = null;
				if(UIUtil.isNullOrEmpty(strQuoteId) && slQuotationIds.size()>0) {
					if(slQuotationIds.size()>1) {
						strBidders = "Multi";
					}else {
						strBidders = "Single";
					}
					for(int i=0;i<slQuotationIds.size();i++) {
						strQuoteId = (String)slQuotationIds.get(i);
						doQuote = new DomainObject(strQuoteId);
						StringList slSelectList = new StringList();
						slSelectList.add("from["+DomainRelationship.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
						slSelectList.add("to["+RequestToSupplier.RELATIONSHIP_LINE_ITEM_QUOTATION+"].from.id");

						Map mQuoteInfo = (Map)doQuote.getInfo(context, slSelectList);
						strVendorName = (String)mQuoteInfo.get("from["+DomainRelationship.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
						Object oLineItems = mQuoteInfo.get("to["+RequestToSupplier.RELATIONSHIP_LINE_ITEM_QUOTATION+"].from.id");
						StringList slLineItemList = new StringList();

						if(null != oLineItems && oLineItems instanceof StringList)
							slLineItemList = (StringList)oLineItems;
						if(null != oLineItems && oLineItems instanceof String)
							slLineItemList.add((String)oLineItems);

						double dCumulativeRBI = 0.0D;
						double dCumulativeCBDS = 0.0D;
						for(int j=0;j<slLineItemList.size();j++) {
							strLiId = (String)slLineItemList.get(j);
							double d = 0.0D;
							if(UIUtil.isNotNullAndNotEmpty(strRBIFormula)) {
								d = Calculator.CalculateExpression(context, strQuoteId, strLiId, strRBIFormula, null);
								if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
									d = 0.0D;
								}
							}
							dCumulativeRBI = dCumulativeRBI + d;

							double d1 = 0.0D;
							if(UIUtil.isNotNullAndNotEmpty(strCBDSFormula)) {
								d1= Calculator.CalculateExpression(context, strQuoteId, strLiId, strCBDSFormula, null);
								if ((Double.isInfinite(d1)) || (Double.toString(d).equals("NaN")) || (d1 < 0.0D)) {
									d1 = 0.0D;
								}
							}
							dCumulativeCBDS = dCumulativeCBDS + d1;
						}

						if(i==0) {
							dTempRBI = dCumulativeRBI;
							strL1Vendor = strVendorName;
						}else if(dCumulativeRBI < dTempRBI) {
							dTempRBI = dCumulativeRBI;
							strL1Vendor = strVendorName;
						}						

						if(i==0) {
							dTempCBDS = dCumulativeCBDS;
						}else if(dCumulativeCBDS < dTempCBDS) {
							dTempCBDS = dCumulativeCBDS;
						}
					}
				}else {
					if(UIUtil.isNotNullAndNotEmpty(strQuoteId)) {
						strVendorName = MqlUtil.mqlCommand(context, "print bus "+strQuoteId+" select from["+DomainRelationship.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name dump");

						doQuote = new DomainObject(strQuoteId);
						StringList slSelectList = new StringList();
						slSelectList.add("from["+DomainRelationship.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
						slSelectList.add("to["+RequestToSupplier.RELATIONSHIP_LINE_ITEM_QUOTATION+"].to.id");

						Map mQuoteInfo = (HashMap)doQuote.getInfo(context, slSelectList);
						String oVendorList = (String)mQuoteInfo.get("from["+DomainRelationship.RELATIONSHIP_SUPPLIER_RESPONSE+"].to.name");
						Object oLineItems = mQuoteInfo.get("to["+RequestToSupplier.RELATIONSHIP_LINE_ITEM_QUOTATION+"].to.id");
						StringList slLineItemList = new StringList();

						if(null != oLineItems && oLineItems instanceof StringList)
							slLineItemList = (StringList)oLineItems;
						if(null != oLineItems && oLineItems instanceof String)
							slLineItemList.add((String)oLineItems);

						double dCumulativeRBI = 0.0D;
						double dCumulativeCBDS = 0.0D;
						for(int j=0;j<slLineItemList.size();j++) {
							strLiId = (String)slLineItemList.get(j);
							double d = Calculator.CalculateExpression(context, strQuoteId, strLiId, strRBIFormula, null);
							if ((Double.isInfinite(d)) || (Double.toString(d).equals("NaN")) || (d < 0.0D)) {
								d = 0.0D;
							}
							dCumulativeRBI = dCumulativeRBI + d;

							double d1 = Calculator.CalculateExpression(context, strQuoteId, strLiId, strCBDSFormula, null);
							if ((Double.isInfinite(d1)) || (Double.toString(d).equals("NaN")) || (d1 < 0.0D)) {
								d1 = 0.0D;
							}
							dCumulativeCBDS = dCumulativeCBDS + d1;
						}

						dTempRBI = dCumulativeRBI;
						strL1Vendor = strVendorName;
						dTempCBDS = dCumulativeCBDS;


					}
				}
			}
			strRBIRate = String.valueOf(dTempRBI);
			strCBDSRate = String.valueOf(dTempCBDS);

			mInfo.put("LIVendor", strL1Vendor);
			mInfo.put("RBIRate", strRBIRate);
			mInfo.put("CBDSRate", strCBDSRate);
			mInfo.put("EstimatedBudget", strBudget);
			mInfo.put("Bidders", strBidders);

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mInfo;


	}


	private Map getCommercialAgingSummary(Context context, MapList mlRFQList)throws Exception{
		Map mReturnMap = new HashMap();
		try {
			Date dToday = new Date();
			Date dLastDayOfLastMonth = new Date();
			dLastDayOfLastMonth.setDate(dLastDayOfLastMonth.getDate() - dToday.getDate());

			Date d30DaysBeforeToday = new Date();
			d30DaysBeforeToday.setDate(d30DaysBeforeToday.getDate()-30);

			Date d45DaysBeforeToday = new Date();
			d45DaysBeforeToday.setDate(d30DaysBeforeToday.getDate()-44);

			Date d30DaysBeforeLastDay = new Date();
			d30DaysBeforeLastDay.setDate(dLastDayOfLastMonth.getDate()-30);

			Date d45DaysBeforeLastDay = new Date();
			d45DaysBeforeLastDay.setDate(dLastDayOfLastMonth.getDate()-44);

			int iLessThan30daysToday = 0;
			int iLessThan45daysToday = 0;
			int iLessMore45daysToday = 0;

			int iLessThan30daysLastToday = 0;
			int iLessThan45daysLastToday = 0;
			int iLessMore45daysLastToday = 0;

			String strBidOpenDate = DomainConstants.EMPTY_STRING;
			Map mTempMap = null;
			for(int i=0;i<mlRFQList.size();i++) {
				mTempMap = (Map)mlRFQList.get(i);
				strBidOpenDate = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value");
				if(UIUtil.isNotNullAndNotEmpty(strBidOpenDate)) {
					Date dBidOpenDate = eMatrixDateFormat.getJavaDate(strBidOpenDate);

					if(dBidOpenDate.after(d30DaysBeforeToday)) {
						iLessThan30daysToday++;
					}
					if(dBidOpenDate.after(d45DaysBeforeToday) && dBidOpenDate.before(d30DaysBeforeToday)) {
						iLessThan45daysToday++;
					}
					if(dBidOpenDate.before(d45DaysBeforeToday)) {
						iLessMore45daysToday ++;
					}
					if(dBidOpenDate.after(d30DaysBeforeLastDay)) {
						iLessThan30daysLastToday++;
					}
					if(dBidOpenDate.after(d45DaysBeforeLastDay) && dBidOpenDate.before(d30DaysBeforeLastDay)) {
						iLessThan45daysLastToday++;
					}
					if(dBidOpenDate.before(d45DaysBeforeLastDay)) {
						iLessMore45daysLastToday++;
					}

				}				
			}			

			mReturnMap.put("TodayLessThan30", String.valueOf(iLessThan30daysToday));
			mReturnMap.put("TodayLessThan45", String.valueOf(iLessThan45daysToday));
			mReturnMap.put("ToayMoreThan45", String.valueOf(iLessMore45daysToday));
			mReturnMap.put("LastMonthLessThan30", String.valueOf(iLessThan30daysLastToday));
			mReturnMap.put("LastMonthLessThan45", String.valueOf(iLessThan45daysLastToday));
			mReturnMap.put("LastMonthMoreThan30", String.valueOf(iLessMore45daysLastToday));

		}catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return mReturnMap;


	}


	private String getFirstRevision(Context context, String strObjectId)throws Exception{
		String strFirstRevision = MqlUtil.mqlCommand(context, "print bus "+strObjectId+" select first.id dump");
		return strFirstRevision;		
	}


	private Map getTrackingSummary(Context context,  MapList mlRFQList)throws Exception{
		Map mReturnMap = new HashMap();
		Map mTempMap = null;
		String strBidOpenDate = DomainConstants.EMPTY_STRING;
		int iMonth = 0;
		Date dBidOpenDate = null;
		HashMap hmMonthInfo = null;
		for(int i=0;i<mlRFQList.size();i++) {
			hmMonthInfo = new HashMap();
			mTempMap = (Map)mlRFQList.get(i);
			strBidOpenDate = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value");
			dBidOpenDate = eMatrixDateFormat.getJavaDate(strBidOpenDate);
			iMonth = dBidOpenDate.getMonth();

			if(mReturnMap.containsKey(String.valueOf(iMonth))==false) {
				updateMonthWiseInfo(context, mTempMap, hmMonthInfo);
				mReturnMap.put(String.valueOf(iMonth), hmMonthInfo);
			}else {
				hmMonthInfo = (HashMap) mReturnMap.get(String.valueOf(iMonth));
				updateMonthWiseInfo(context, mTempMap, hmMonthInfo);
				mReturnMap.replace(String.valueOf(iMonth), hmMonthInfo);			
			}			
		}

		return mReturnMap;
	}

	private HashMap updateMonthWiseInfo(Context context, Map mTempMap, HashMap hmReturnMap)throws Exception{
		String strBidOpenDate = DomainConstants.EMPTY_STRING;
		String strBuyerResponseDate = DomainConstants.EMPTY_STRING;
		String strRFQId = DomainConstants.EMPTY_STRING;
		String strBudget = DomainConstants.EMPTY_STRING;
		String strCurrent = DomainConstants.EMPTY_STRING;
		int iOpeningCnt = 0;
		int iRecievedCnt = 0;
		int iWithDrawnCnt = 0;
		int iPOInitiatedCnt = 0;
		int iSingleBidCnt = 0;
		int iMultiBidCnt = 0;

		double dBidToRingi = 0.0D;
		double dRingiToApprove = 0.0D;
		double dOpening = 0.0D;
		double dRecieved = 0.0D;
		double dWithdrawn = 0.0D;
		double dPOInitial = 0.0D;
		double dPOFinal = 0.0D;
		double dSingleInital = 0.0D;
		double dSingleFinal = 0.0D;
		double dMultiInitial = 0.0D;
		double dMultiFinal = 0.0D;
		Date dPODate = null;

		strBudget = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_BUDGET+"].value");
		strRFQId = (String)mTempMap.get(DomainObject.SELECT_ID);
		strCurrent = (String)mTempMap.get(DomainObject.SELECT_CURRENT);
		strBuyerResponseDate = (String)mTempMap.get("current["+DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW+"].actual");
		strBidOpenDate = (String)mTempMap.get("attribute["+TDRConstants_mxJPO.ATTRIBUTE_TDR_QUOTE_OPEN_DATE+"].value");

		Map mCostInfo = getCostInfo(context, strRFQId);

		String strFirstRevId = getFirstRevision(context, strRFQId);
		Map mFirstCostInfo = getCostInfo(context, strFirstRevId);
		
		hmReturnMap.put("Budget", strBudget);

		if(hmReturnMap.containsKey("OpenCount")) {
			iOpeningCnt = Integer.valueOf((String)hmReturnMap.get("OpenCount"));
			iOpeningCnt++;
			hmReturnMap.put("OpenCount", String.valueOf(iOpeningCnt));
		}else {
			hmReturnMap.put("OpenCount", "1");
		}

		if(hmReturnMap.containsKey("RecievedCount")) {
			iRecievedCnt = Integer.valueOf((String)hmReturnMap.get("RecievedCount"));
			iRecievedCnt++;
			hmReturnMap.put("RecievedCount", String.valueOf(iRecievedCnt));
		}else {
			hmReturnMap.put("RecievedCount", "1");
		}

		if(strCurrent.equals(RequestToSupplier.STATE_RTS_CANCELLED_CANCELLED)) {
			if(hmReturnMap.containsKey("WithdrawnCount")) {
				iWithDrawnCnt = Integer.valueOf((String)hmReturnMap.get("WithdrawnCount"));
				iWithDrawnCnt++;
				hmReturnMap.put("WithdrawnCount", String.valueOf(iWithDrawnCnt));
			}else {
				hmReturnMap.put("WithdrawnCount", "1");
			}
		}

		if(mCostInfo.isEmpty()==false) {
			dOpening = Double.valueOf((String)mCostInfo.get("RBIRate"));
			if(hmReturnMap.containsKey("OpeningBalance")) {
				dOpening = dOpening + Double.valueOf((String)hmReturnMap.get("OpeningBalance"));
				hmReturnMap.put("OpeningBalance", String.valueOf(dOpening));
			}else {
				hmReturnMap.put("OpeningBalance", String.valueOf(dOpening));
			}
		}

		if(mCostInfo.isEmpty()==false) {
			dRecieved = Double.valueOf((String)mCostInfo.get("RBIRate"));
			if(hmReturnMap.containsKey("Recieved")) {
				dRecieved = dRecieved + Double.valueOf((String)hmReturnMap.get("Recieved"));
				hmReturnMap.put("Recieved", String.valueOf(dRecieved));
			}else {
				hmReturnMap.put("Recieved", String.valueOf(dRecieved));
			}
		}

		if(strCurrent.equals(RequestToSupplier.STATE_RTS_CANCELLED_CANCELLED)) {
			if(mCostInfo.isEmpty()==false) {
				dRecieved = Double.valueOf((String)mCostInfo.get("RBIRate"));
				if(hmReturnMap.containsKey("Withdrawn")) {
					dRecieved = dRecieved + Double.valueOf((Double)hmReturnMap.get("Withdrawn"));
					hmReturnMap.put("Withdrawn", String.valueOf(dRecieved));
				}else {
					hmReturnMap.put("Withdrawn", String.valueOf(dRecieved));
				}
			}
		}

		if(strCurrent.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_FINAL_REVIEW)) {

			if(hmReturnMap.containsKey("POInitiated")) {
				dPOInitial = Integer.valueOf((String)hmReturnMap.get("POInitiated"));
				dPOInitial++;
				hmReturnMap.put("POInitiated", String.valueOf(dPOInitial));
			}else {
				hmReturnMap.put("POInitiated", "1");
			}

			if(mCostInfo.isEmpty()==false) {
				dPOFinal = Double.valueOf((String)mCostInfo.get("RBIRate"));
				if(hmReturnMap.containsKey("POFinal")) {
					dPOFinal = dPOFinal + Double.valueOf((String)hmReturnMap.get("POFinal"));
					hmReturnMap.put("POFinal", String.valueOf(dPOFinal));
				}else {
					hmReturnMap.put("POFinal", String.valueOf(dPOFinal));
				}
				strBudget = (String)mCostInfo.get("EstimatedBudget");
				if(hmReturnMap.containsKey("FinalBudget")) {
					strBudget = String.valueOf(Double.valueOf(strBudget) + Double.valueOf((String)hmReturnMap.get("FinalBudget")));
					hmReturnMap.put("FinalBudget", strBudget);
				}else {
					hmReturnMap.put("FinalBudget", strBudget);
				}

			}

			if(mFirstCostInfo.isEmpty()==false) {
				dPOInitial = Double.valueOf((String)mFirstCostInfo.get("RBIRate"));
				if(hmReturnMap.containsKey("POInitial")) {
					dPOInitial = dPOInitial + Double.valueOf((String)hmReturnMap.get("POInitial"));
					hmReturnMap.put("POInitial", String.valueOf(dPOInitial));
				}else {
					hmReturnMap.put("POInitial", String.valueOf(dPOInitial));
				}
				strBudget = (String)mCostInfo.get("EstimatedBudget");
				if(hmReturnMap.containsKey("InitialBudget")) {
					strBudget = String.valueOf(Double.valueOf(strBudget) + Double.valueOf((String)hmReturnMap.get("InitialBudget")));
					hmReturnMap.put("InitialBudget", strBudget);
				}else {
					hmReturnMap.put("InitialBudget", strBudget);
				}
			}			


			String strDiffInDays = getDayDiffrence(strBidOpenDate, strBuyerResponseDate);
			if(hmReturnMap.containsKey("BidToRingiDays")) {
				dBidToRingi = Double.valueOf((String)hmReturnMap.get("BidToRingiDays")) + Double.valueOf(strDiffInDays);
				hmReturnMap.put("BidToRingiDays", String.valueOf(dBidToRingi));
			}else {
				hmReturnMap.put("BidToRingiDays", "1");
			}


		}

		if(strCurrent.equals(DomainObject.STATE_REQUEST_TO_SUPPLIER_COMPLETE)) {

			if(mCostInfo.isEmpty()==false) {

				String strBidType = (String)mCostInfo.get("Bidders");
				if(UIUtil.isNotNullAndNotEmpty(strBidType) && "Single".equals(strBidType)) {
					if(hmReturnMap.containsKey("SingleBidCnt")) {
						iSingleBidCnt = Integer.valueOf((String)hmReturnMap.get("SingleBidCnt"));
						iSingleBidCnt++;
						hmReturnMap.put("SingleBidCnt", String.valueOf(iSingleBidCnt));
					}else {
						hmReturnMap.put("SingleBidCnt", "1");
					}

					dSingleFinal = Double.valueOf((String)mCostInfo.get("RBIRate"));
					if(hmReturnMap.containsKey("SingleBidFinal")) {
						dSingleFinal = dSingleFinal + Double.valueOf((String)hmReturnMap.get("SingleBidFinal"));
						hmReturnMap.put("SingleBidFinal", String.valueOf(dSingleFinal));
					}else {
						hmReturnMap.put("SingleBidFinal", String.valueOf(dSingleFinal));
					}
					strBudget = (String)mCostInfo.get("EstimatedBudget");
					if(hmReturnMap.containsKey("SingleBidBudget")) {
						strBudget = String.valueOf(Double.valueOf(strBudget) + Double.valueOf((String)hmReturnMap.get("SingleBidBudget")));
						hmReturnMap.put("SingleBidBudget", strBudget);
					}else {
						hmReturnMap.put("SingleBidBudget", strBudget);
					}

				}else if(UIUtil.isNotNullAndNotEmpty(strBidType) && "Multi".equals(strBidType)) {
					if(hmReturnMap.containsKey("MultiBidCnt")) {
						iMultiBidCnt = Integer.valueOf((String)hmReturnMap.get("MultiBidCnt"));
						iMultiBidCnt++;
						hmReturnMap.put("MultiBidCnt", String.valueOf(iMultiBidCnt));
					}else {
						hmReturnMap.put("MultiBidCnt", "1");
					}

					dMultiFinal = Double.valueOf((String)mCostInfo.get("RBIRate"));
					if(hmReturnMap.containsKey("MultiBidFinal")) {
						dMultiFinal = dMultiFinal + Double.valueOf((String)hmReturnMap.get("MultiBidFinal"));
						hmReturnMap.put("MultiBidFinal", String.valueOf(dMultiFinal));
					}else {
						hmReturnMap.put("MultiBidFinal", String.valueOf(dMultiFinal));
					}

					strBudget = (String)mCostInfo.get("EstimatedBudget");
					if(hmReturnMap.containsKey("MultiBidBudget")) {
						strBudget = String.valueOf(Double.valueOf(strBudget) + Double.valueOf((String)hmReturnMap.get("MultiBidBudget")));
						hmReturnMap.put("MultiBidBudget", strBudget);
					}else {
						hmReturnMap.put("MultiBidBudget", strBudget);
					}
				}

			}


			String strDiffInDays = getDayDiffrence(strBidOpenDate, strBuyerResponseDate);
			if(hmReturnMap.containsKey("RingiToApprovalDays")) {
				dRingiToApprove = Double.valueOf((String)hmReturnMap.get("RingiToApprovalDays")) + Double.valueOf(strDiffInDays);
				hmReturnMap.put("RingiToApprovalDays", String.valueOf(dRingiToApprove));
			}else {
				hmReturnMap.put("RingiToApprovalDays", "1");
			}

		}


		return hmReturnMap;
	}

}