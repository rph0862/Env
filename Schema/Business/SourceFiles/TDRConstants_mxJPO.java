
/*
 *  PackageBase
 *
 *
 * (c) Dassault Systemes, 1993 - 2017.  All rights reserved
 *
 *
 *  static const char RCSID[] = $Id: /ENOSourcingCentral/CNext/Modules/ENOSourcingCentral/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1.1.1 Thu Nov 13 08:27:30 2008 GMT  Experimental$
 */
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;

/**
 * The <code>${CLASSNAME}</code> class contains Package related utilites
 * The methods of this class are used to create packages, list all the packages based on selected filter,
 * list the attachments for the packages,list the PackageRFQ
 */

public class TDRConstants_mxJPO extends emxDomainObject_mxJPO
{
	public static String RELATIONSHIP_TDR_RFQ_TEMPATE_DEPARTMENT = PropertyUtil.getSchemaProperty("relationship_TDRRFQTemplateDepartment");
	public static String RELATIONSHIP_TDR_RFQ_CONCERNED_SC_DEPT = PropertyUtil.getSchemaProperty("relationship_TDRRFQConcernedSCDept");
	public static String RELATIONSHIP_TDR_RFQ_INITIAL_APPROVAL_TEMPLATE = PropertyUtil.getSchemaProperty("relationship_TDRRFQInitialApprovalTemplate");
	public static String RELATIONSHIP_TDR_COMMERCIAL_QUOTE_OPENER = PropertyUtil.getSchemaProperty("relationship_TDRCommercialQuoteOpener");
	public static String RELATIONSHIP_TDR_RFQ_LOCATION = PropertyUtil.getSchemaProperty("relationship_TDRRFQLocation");
	public static String RELATIONSHIP_RFQ_SUPPLIER = PropertyUtil.getSchemaProperty("relationship_RTSSupplier");
	public static String RELATIONSHIP_SOURCING_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_SourcingDocument");
	public static String RELATIONSHIP_TDR_MANDATORY_QUOTATION_DOCUMENTS = PropertyUtil.getSchemaProperty("relationship_TDRMandatoryQuotationDocuments");
	public static String RELATIONSHIP_TDR_COMMERCIAL_BUYER = PropertyUtil.getSchemaProperty("relationship_TDRCommercialBuyer");
	public static String RELATIONSHIP_TDR_RFQ_MEMBER_LIST = PropertyUtil.getSchemaProperty("relationship_TDRRFQMemberList");
	public static String RELATIONSHIP_TDR_RFQ_FINAL_APPROVAL_TEMPLATE = PropertyUtil.getSchemaProperty("relationship_TDRRFQFinalApprovalTemplate");
	public static String RELATIONSHIP_TDR_RFQ_TECHNICAL_BUYER = PropertyUtil.getSchemaProperty("relationship_TDRRFQTechnicalBuyer");

	public static String ATTRIBUTE_TDR_VISIBILITY = PropertyUtil.getSchemaProperty("attribute_TDRAttributeVisibility");
	public static String ATTRIBUTE_TDR_AG_SEQUENCE_NUMBER = PropertyUtil.getSchemaProperty("attribute_TDRAGSequenceNumber");
	public static String ATTRIBUTE_MSIL_RINGI_NUMBER = PropertyUtil.getSchemaProperty("attribute_MSILRFQRingiNumber");
	public static String ATTRIBUTE_MSIL_RINGI_LEVEL = PropertyUtil.getSchemaProperty("attribute_TDRRFQRingiLevel");
	public static String ATTRIBUTE_MSIL_RINGI_STATUS = PropertyUtil.getSchemaProperty("attribute_TDRRFQRingiStatus");
	public static String ATTRIBUTE_MSIL_RINGI_APPROVED_DATE = PropertyUtil.getSchemaProperty("attribute_TDRRFQRingiApprovedDate");
	public static String ATTRIBUTE_TDR_BUDGET = PropertyUtil.getSchemaProperty("attribute_TDRBudget");
	public static String ATTRIBUTE_TDR_ROUND_TYPE = PropertyUtil.getSchemaProperty("attribute_TDRRoundType");
	public static String ATTRIBUTE_TDR_COMMERCIAL_OPEN_STATUS = PropertyUtil.getSchemaProperty("attribute_TDRCommercialOpenStatus");
	public static String ATTRIBUTE_TDR_TECH_REVIEW_STATUS = PropertyUtil.getSchemaProperty("attribute_TDRTechReviewStatus");
	public static String ATTRIBUTE_TDR_TECH_REVIEW_COMPLETION_DATE = PropertyUtil.getSchemaProperty("attribute_TDRTechReviewCompletionDate");
	public static String ATTRIBUTE_TDR_TECH_REVIEW_RESULT = PropertyUtil.getSchemaProperty("attribute_TDRTechReviewResult");
	public static String ATTRIBUTE_TDR_TECH_REVIEW_RESULT_REMARKS = PropertyUtil.getSchemaProperty("attribute_TDRTechReviewResultRemarks");
	public static String ATTRIBUTE_TDR_COMMERCIAL_RINGI_NUMBER = PropertyUtil.getSchemaProperty("attribute_TDRCommercialRingiNumber");
	public static String ATTRIBUTE_TDR_COMMERCIAL_RINGI_LEVEL = PropertyUtil.getSchemaProperty("attribute_TDRCommercialRingiLevel");
	public static String ATTRIBUTE_TDR_COMMERCIAL_RINGI_STATUS = PropertyUtil.getSchemaProperty("attribute_TDRCommercialRingiStatus");
	public static String ATTRIBUTE_TDR_COMMERCIAL_RINGI_APPROVED_DATE = PropertyUtil.getSchemaProperty("attribute_TDRCommercialRingiApprovedDate");
	public static String ATTRIBUTE_TDR_ACKNOWLEDGE = PropertyUtil.getSchemaProperty("attribute_TDRAcknowledge");
	public static String ATTRIBUTE_TDR_RFQ_RINGI_LEVEL = PropertyUtil.getSchemaProperty("attribute_TDRRFQRingiLevel");
	public static String ATTRIBUTE_TDR_FINAL_SOURCE = PropertyUtil.getSchemaProperty("attribute_TDRFinalSource");
	public static String ATTRIBUTE_TDR_PROJECT_HEAD = PropertyUtil.getSchemaProperty("attribute_TDRProjectHead");
	public static String ATTRIBUTE_TDR_PO_NUMBER = PropertyUtil.getSchemaProperty("attribute_TDRPONumber");
	public static String ATTRIBUTE_TDR_NOTIFY_AWARDED_SUPPLIER = PropertyUtil.getSchemaProperty("attribute_TDRNotifyAwardedSupplier");
	public static String ATTRIBUTE_TDR_QUOTE_OPEN_DATE = PropertyUtil.getSchemaProperty("attribute_TDRQuoteOpenDate");
	public static String ATTRIBUTE_TDR_CBDS_RATE_FORMULA = PropertyUtil.getSchemaProperty("attribute_TDRRateAsCBDSFormula");
	public static String ATTRIBUTE_TDR_RBI_RATE_FORMULA = PropertyUtil.getSchemaProperty("attribute_TDRRateAsRBIFormula");
	public static String ATTRIBUTE_TDR_CBDS_RATE_CALCULATION_FORMULA = PropertyUtil.getSchemaProperty("attribute_TDRRateCalculationCBDSFormula");
	public static String ATTRIBUTE_TDR_RBI_RATE_CALCULATION_FORMULA = PropertyUtil.getSchemaProperty("attribute_TDRRateCalculationRBIFormula");
	public static String ATTRIBUTE_TDR_EDITABLE_IN_RESPONSE_COMPLETE = PropertyUtil.getSchemaProperty("attribute_TDREditableInResponseComplete");
	public static String ATTRIBUTE_TDR_AWARD_DATE = PropertyUtil.getSchemaProperty("attribute_TDRAwardDate");

	public static String TYPE_TDR_INTERNAL_RFQ_DOCUMENT = PropertyUtil.getSchemaProperty("type_TDRInternalRFQDocument");
	public static String TYPE_TDR_EXTERNAL_RFQ_DOCUMENT = PropertyUtil.getSchemaProperty("type_TDRExternalRFQDocument");
	public static String TYPE_TDR_RINGI_DOCUMENT = PropertyUtil.getSchemaProperty("type_TDRRingiDocument");
	public static String TYPE_TDR_TECHNICAL_DOCUMENT = PropertyUtil.getSchemaProperty("type_TDRTechnicalDocument");
	public static String TYPE_TDR_COMMERCIAL_DOCUMENT = PropertyUtil.getSchemaProperty("type_TDRCommercialDocument");
	public static String TYPE_TDR_QUOTATION_DOCUMENT_TYPE = PropertyUtil.getSchemaProperty("type_TDRQuotationDocumentType");
	public static String TYPE_TDR_REFERENCE_DOCUMENT_TYPE = PropertyUtil.getSchemaProperty("type_TDRReferenceDocument");
	public static String TYPE_TDR_PO_DOCUMENT_TYPE = PropertyUtil.getSchemaProperty("type_TDRPODocument");
	
	public static String POLICY_TDR_INTERNAL_DOCUMENT = PropertyUtil.getSchemaProperty("policy_TDRInternalDocument");

	public static String ROLE_TECHNICAL_BUYER = PropertyUtil.getSchemaProperty("role_TDRTechnicalBuyer");
	public static String ROLE_COMMERCIAL_BUYER = PropertyUtil.getSchemaProperty("role_TDRCommercialBuyer");
	public static String ROLE_MSIL_DPM = PropertyUtil.getSchemaProperty("role_MSILDPM");
	public static String ROLE_MSIL_DDVM = PropertyUtil.getSchemaProperty("role_MSILDDVM");
	public static String ROLE_RM_BUYER = PropertyUtil.getSchemaProperty("role_RMBuyer");
	public static String ROLE_CIVIL_BUYER = PropertyUtil.getSchemaProperty("role_CivilBuyer");
	
	public static String VAULT_E_SERVICE_PRODUCTION = "eService Production";


	/**
	 * Constructs a <code>${CLASSNAME}</code> Object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 */
	public TDRConstants_mxJPO (Context context, String[] args)
			throws Exception
	{
		super(context, args);
	}

}
