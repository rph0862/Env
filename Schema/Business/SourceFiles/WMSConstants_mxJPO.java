/** Name of the JPO    :
 ** Developed by    : DSIS 
 ** Client            : MSIL
 ** Description        : The purpose of this JPO is to manage all code for Workorder
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/


import com.matrixone.apps.domain.util.PropertyUtil;
import matrix.db.Context;

/**
 * The purpose of this JPO is to handle functionality of SOR
 * @author DSIS
 */
public class WMSConstants_mxJPO	
{
	
	Context context = null;
	
    public WMSConstants_mxJPO(Context context,String[] args) {
		this.context = context;
	}
    
    public static String getSchemaProperty(Context context,String adminType,String adminName){
    	   

        return  PropertyUtil.getSchemaProperty(context, adminType+"_"+adminName);

  }
	
    
  
    
	/*******************************************Relationship constants goes below *******************************************/
	public static final String RELATIONSHIP_WMS_TASK_SOR = PropertyUtil.getSchemaProperty("relationship_WMSTaskSOR");
	public static final String RELATIONSHIP_WMS_REPORTING_MANAGER = PropertyUtil.getSchemaProperty("relationship_WMSReportingManager");
	public static final String RELATIONSHIP_BILL_OF_QUANTITY = PropertyUtil.getSchemaProperty("relationship_WMSMeasurementBookItems");
	public static final String RELATIONSHIP_WMS_PORJECT_WORK_ORDER = PropertyUtil.getSchemaProperty("relationship_WMSProjectWorkOrder");
	public static final String RELATIONSHIP_WMS_WORK_ORDER_ASSIGNEE = PropertyUtil.getSchemaProperty("relationship_WMSWorkOrderAssignee");
	public static final String RELATIONSHIP_WMS_PROJECT_WORK_ORDER  = PropertyUtil.getSchemaProperty("relationship_WMSProjectWorkOrder");
	public static final String RELATIONSHIP_REPORTING_MANAGER = PropertyUtil.getSchemaProperty("relationship_WMSReportingManager");
	public static final String RELATIONSHIP_WMS_WORK_ORDER_APPROVAL_TEMPLATE= PropertyUtil.getSchemaProperty("relationship_WMSWorkOrderApprovalTemplate");
	public static final String RELATIONSHIP_WMS_WORK_ORDER_CONTRACTOR = PropertyUtil.getSchemaProperty("relationship_WMSWorkorderContractor");
	public static final String RELATIONSHIP_WORKORDER_ABSTRACT_MBE = PropertyUtil.getSchemaProperty("relationship_WMSWOAbstractMBE");
	public static final String RELATIONSHIP_WMS_AMBE_APPROVAL_TEMPLATE = PropertyUtil.getSchemaProperty("relationship_WMSAMBEApprovalTemplate");   
	public static final String RELATIONSHIP_WMS_WORK_ORDER_MBE = PropertyUtil.getSchemaProperty("relationship_WMSWOMBE");
	public static final String RELATIONSHIP_WMS_MBE_ACTIVITIES = PropertyUtil.getSchemaProperty("relationship_WMSMBEActivities");
	public static final String RELATIONSHIP_WMS_SEGMENT_MBE = PropertyUtil.getSchemaProperty("relationship_WMSSegmentMBE");
	public static final String RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS = PropertyUtil.getSchemaProperty("relationship_WMSActivityMeasurements");
	public static final String RELATIONSHIP_WMS_MBE_APPROVAL_TEMPLATE = PropertyUtil.getSchemaProperty("relationship_WMSMBEApprovalTemplate");




	//ABSMBE
	public static final String RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE = PropertyUtil.getSchemaProperty("relationship_WMSWOAbstractMBE");





	public static final String RELATIONSHIP_WMS_ITEM_TECHNICAL_DEDUCTION = PropertyUtil.getSchemaProperty("relationship_WMSItemTechnicalDeduction");
	public static final String RELATIONSHIP_WMS_ABSMBE_TECHNICAL_DEDUCTION_RELEASE = PropertyUtil.getSchemaProperty("relationship_WMSAbstractMBETechnicalDeductionRelease");
	public static final String RELATIONSHIP_WMS_WO_TECHNICAL_DEDUCTION = PropertyUtil.getSchemaProperty("relationship_WMSWorkOrderTechnicalDeduction");
	public static final String RELATIONSHIP_WMS_ABSTRACT_MBE_ITEMS = PropertyUtil.getSchemaProperty("relationship_WMSAbstractMBEActivities");
	public static final String RELATIONSHIP_WMS_ABSMBE_TECHNICALDEDUCTION = PropertyUtil.getSchemaProperty("relationship_WMSAbstractMBETechnicalDeduction");
	//Abs Particular
	public static final String RELATIONSHIP_WMS_BILL_REDUCTION_RELEASE = "relationship_WMSBillReductionRelease";
	public static final String RELATIONSHIP_WMS_BILL_REDUCTION = "relationship_WMSBillReduction";
	public static final String RELATIONSHIP_WMS_ADVANCE_RECOVERY = PropertyUtil.getSchemaProperty("relationship_WMSAMBAdvanceRecovery");
	public static final String RELATIONSHIP_WMS_RECOVERY = PropertyUtil.getSchemaProperty("relationship_WMSAMBRecovery");
	public static final String RELATIONSHIP_WMS_WORK_ORDER_ADVANCES = PropertyUtil.getSchemaProperty("relationship_WMSWorkOrderAdvances");

	//Other head deduction 

	public static final String RELATIONSHIP_WMS_ABSTRACT_MBE_HEAD_DEDUCTION = PropertyUtil.getSchemaProperty("relationship_WMSAbstractMBEHeadDeduction");
	//Material bill
	public static final String RELATIONSHIP_WMS_MATERIALBILL_STOCK = PropertyUtil.getSchemaProperty("relationship_WMSMaterialBillStock");
	public static final String RELATIONSHIP_WMS_MATERIAL_TO_SOR = PropertyUtil.getSchemaProperty("relationship_WMSMaterialToSOR");
	public static final String RELATIONSHIP_WMS_STOCK_MATERIAL = PropertyUtil.getSchemaProperty("relationship_WMSStockMaterial");
	public static final String RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE = PropertyUtil.getSchemaProperty("relationship_WMSWorkorderAdvanceRate");
	public static final String RELATIONSHIP_WMS_WO_MATERIAL_BILL = PropertyUtil.getSchemaProperty("relationship_WMSWOMaterialBill");
	public static final String RELATIONSHIP_WMS_MB_APPROVAL_TEMPLATE = PropertyUtil.getSchemaProperty("relationship_WMSMBApprovalTemplate");

	public static final String RELATIONSHIP_WMS_MAKER_OF = PropertyUtil.getSchemaProperty("relationship_WMSMakerOf");
	public static final String RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF = PropertyUtil.getSchemaProperty("relationship_WMSStockEntriesMakerOf");

	public static final String RELATIONSHIP_WMS_ABS_STOCK						        = PropertyUtil.getSchemaProperty("relationship_WMSABSStock");

	public static final String RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB             = PropertyUtil.getSchemaProperty("relationship_WMSMaterialConsumptionToAMB");
	public static final String RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK    = PropertyUtil.getSchemaProperty("relationship_WMSMaterialConsumptionToStock");

	public static final String RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION               = PropertyUtil.getSchemaProperty("relationship_WMSItemMaterialConsumption");

	public static final String RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL        = PropertyUtil.getSchemaProperty("relationship_WMSMaterialConsumptionToMaterial");
	public static final String RELATIONSHIP_WMS_MATERIAL_ESCALATION        = PropertyUtil.getSchemaProperty("relationship_WMSMaterialEscalation");

	//SDP  
	public static final String RELATIONSHIP_WMS_WORK_ORDER_SDP        = PropertyUtil.getSchemaProperty("relationship_WMSWorkOrderSDP");


	/*******************************************************Type constants goes below *******************************************/
	public static final String TYPE_WMS_MEASUREMENT_TASK = PropertyUtil.getSchemaProperty("type_WMSMeasurementTask");
	public static final String TYPE_WMS_SEGMENT = PropertyUtil.getSchemaProperty("type_WMSSegment");
	public static final String TYPE_WMS_MEASUREMENT_BOOK = PropertyUtil.getSchemaProperty("type_WMSMeasurementBook"); 
	public static final String TYPE_WMS_WORK_ORDER = PropertyUtil.getSchemaProperty("type_WMSWorkOrder");
	public static final String TYPE_WMS_SOR_CHAPTER = PropertyUtil.getSchemaProperty("type_WMSSORChapter");
	public static final String TYPE_WMS_SOR = PropertyUtil.getSchemaProperty("type_WMSSOR");
	public static final String TYPE_WMS_SOR_LIBRARY = PropertyUtil.getSchemaProperty("type_WMSSORLibrary");
	public static final String TYPE_ABSTRACT_MBE = PropertyUtil.getSchemaProperty("type_WMSAbstractMeasurementBookEntry");
	//MBE 
	public static final String TYPE_WMS_MEASUREMENT_BOOK_ENTRY = PropertyUtil.getSchemaProperty("type_WMSMeasurementBookEntry");
	public static final String TYPE_WMS_PAYMENT_ITEM = PropertyUtil.getSchemaProperty("type_WMSPaymentItem");//remove
	public static final String TYPE_WMS_MEASUREMENTS = PropertyUtil.getSchemaProperty("type_WMSMeasurements");


	//ABSMBE

	public static final String TYPE_WMS_BILL_REDUCTION_ITEM = "type_WMSBillReductionItem";
	public static final String TYPE_WMS_TECHNICAL_DEDUCTION = PropertyUtil.getSchemaProperty("type_WMSTechnicalDeduction");

	//ABS Particular
	public static final String TYPE_WMS_ADVANCE_RECOVERY_ITEM = PropertyUtil.getSchemaProperty("type_WMSAdvanceRecoveryItem");
	//othe deduction

	public static final String TYPE_WMS_OTER_HEAD_DEDUCTION = PropertyUtil.getSchemaProperty("type_WMSOtherHeadDeduction");

	//Material library
	public static final String TYPE_WMS_MATERIAL = PropertyUtil.getSchemaProperty("type_WMSMaterialCategory");
	public static final String TYPE_WMS_MATERIAL_BILL = PropertyUtil.getSchemaProperty("type_WMSMaterialBill");
	public static final String TYPE_WMS_STOCK_ENTRIES = PropertyUtil.getSchemaProperty("type_WMSStockEntries");

	public static final String TYPE_WMS_MATERIAL_MAKER = PropertyUtil.getSchemaProperty("type_WMSMaterialMaker");
	public static final String TYPE_WMS_MATERIAL_CONSUMPTION           = PropertyUtil.getSchemaProperty("type_WMSMaterialConsumption");

	/*************************************************Attribute constants goes below **************************************/
	public static final String ATTRIBUTE_WMS_ITEM_RATE_ESCALATION = PropertyUtil.getSchemaProperty("attribute_WMSItemRateEscalation");
	public static final String ATTRIBUTE_WMS_WORK_ORDER_ROLE = PropertyUtil.getSchemaProperty("attribute_WMSWorkOrderRole");
	public static final String ATTRIBUTE_WMS_DEFAULT_ROLE = PropertyUtil.getSchemaProperty("attribute_WMSDefaultRole");
	public static final String ATTRIBUTE_WMS_MBE_ITEM_TYPE = PropertyUtil.getSchemaProperty("attribute_WMSMBEItemType");
	public static final String ATTRIBUTE_WMS_APPROVAL_TEMPLATE_PURPOSE= PropertyUtil.getSchemaProperty("attribute_WMSApprovalTemplatePurpose");
	public static final String ATTRIBUTE_WMS_COMPLETION_DUE_DATE = PropertyUtil.getSchemaProperty("attribute_WMSCompletionDueDate");
	public static final String ATTRIBUTE_WMS_VALUE_OF_CONTRACT = PropertyUtil.getSchemaProperty("attribute_WMSValueOfContract");
	public static final String ATTRIBUTE_WMS_SOR_RATE = PropertyUtil.getSchemaProperty("attribute_WMSSORRate");
	public static final String ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER = PropertyUtil.getSchemaProperty("attribute_WMSMSILSORItemNumber");
	public static final String ATTRIBUTE_WMS_WORK_ORDER_TITLE = PropertyUtil.getSchemaProperty("attribute_WMSWorkorderTitle");

	//MEB attribute 
	public static final String ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSMBEActivityQuantity");
	public static final String ATTRIBUTE_WMS_BOQ_SERIAL_NUMBER = PropertyUtil.getSchemaProperty("attribute_WMSBOQSequenceNumber");
	public static final String ATTRIBUTE_WMS_QUANTITY_PAID_TILL_DATE =  PropertyUtil.getSchemaProperty("attribute_WMSQtyPaidTillDate");
	public static final String ATTRIBUTE_WMS_REDUCED_SOR_RATE =  PropertyUtil.getSchemaProperty("attribute_WMSReducedSORRate"); 
	public static final String ATTRIBUTE_WMS_MBE_COST =  PropertyUtil.getSchemaProperty("attribute_WMSMBECost");
	public static final String ATTRIBUTE_WMS_MBE_QUANTITY =  PropertyUtil.getSchemaProperty("attribute_WMSMBEQuantity");
	public static final String ATTRIBUTE_WMS_TOTAL_QUANTITY =  PropertyUtil.getSchemaProperty("attribute_WMSTotalQuantity");


	public static final String ATTRIBUTE_WMS_PERCENTAGE_OF_WEIGHTAGE =  PropertyUtil.getSchemaProperty("attribute_WMSPercentageOfWeightage"); //remove

	public static final String ATTRIBUTE_WMS_IS_DEDUCTION = PropertyUtil.getSchemaProperty("attribute_WMSIsDeduction");
	public static final String ATTRIBUTE_WMS_ITEM_CO_EFFICIENT_FACTOR = PropertyUtil.getSchemaProperty("attribute_WMSItemCoEfficientFactor");
	public static final String ATTRIBUTE_WMS_QTY_SUBMITTED_TILL_DATE = PropertyUtil.getSchemaProperty("attribute_WMSQtySubmitedTillDate");
	public static final String ATTRIBUTE_WMS_MEASUREMENT_LOCATION = PropertyUtil.getSchemaProperty("attribute_WMSMeasurementLocation");
	public static final String ATTRIBUTE_WMS_MEASUREMENT_ADDRESS = PropertyUtil.getSchemaProperty("attribute_WMSMeasurementAddress");
	public static final String ATTRIBUTE_WMS_DATE_OF_MEASUREMENT = PropertyUtil.getSchemaProperty("attribute_WMSMBEDateOfMeasurementDate");

	public static final String ATTRIBUTE_WMS_MBE_FREQUENCY = PropertyUtil.getSchemaProperty("attribute_WMSMBEFrequency");
	public static final String ATTRIBUTE_WMS_MBE_DEPTH = PropertyUtil.getSchemaProperty("attribute_WMSMBEDepth");
	public static final String ATTRIBUTE_WMS_MBE_LENGTH = PropertyUtil.getSchemaProperty("attribute_WMSMBELength");
	public static final String ATTRIBUTE_WMS_MBE_RADIUS = PropertyUtil.getSchemaProperty("attribute_WMSMBERadius");
	public static final String ATTRIBUTE_WMS_MBE_BREADTH = PropertyUtil.getSchemaProperty("attribute_WMSMBEBreadth");
	public static final String ATTRIBUTE_WMS_PO_NUMBER = PropertyUtil.getSchemaProperty("attribute_WMSPONumber");
	public static final String ATTRIBUTE_WMS_UNIT_OF_MEASURE = PropertyUtil.getSchemaProperty("attribute_WMSUnitOfMeasure");


	//public static final String ATTRIBUTE_ABSMBE_ITEM_RATE = PropertyUtil.getSchemaProperty("attribute_WMSDeductionRate");
	//public static final String ATTRIBUTE_ITEM_TOTAL_DEDUCTION = PropertyUtil.getSchemaProperty("attribute_WMSTotalDeduction");
	public static final String ATTRIBUTE_WMS_ABSMBE_ITEM_TOTAL_COST = PropertyUtil.getSchemaProperty("attribute_WMSAMBItemTotalCost");
	public static final String ATTRIBUTE_WMS_TOTAL_COST = PropertyUtil.getSchemaProperty("attribute_WMSTotalCost");
	public static final String ATTRIBUTE_WMS_ABS_MBE_ITEM_COST = PropertyUtil.getSchemaProperty("attribute_WMSAbsMBEItemCost");
	public static final String ATTRIBUTE_WMS_ITEM_ENTRY_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSMBEActivityQuantity");
	public static final String ATTRIBUTE_WMS_PAYABLE_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEItemPayableQuantity");
	public static final String ATTRIBUTE_WMS_WITHHELD_CAUSE = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEItemWithheldCause");
	public static final String ATTRIBUTE_WMS_ITEMWITHHELD_RELEASED_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSItemWithHeldReleasedQuantity");



	//ABSMBE
	public static final String TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY = PropertyUtil.getSchemaProperty("type_WMSAbstractMeasurementBookEntry");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RELEASE_AMOUNT= PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionReleaseAmount");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionAmount");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_TYPE= PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionType");
	public static final String ATTRIBUTE_WMS_CURRENT_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT= PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionReleaseCurrentBill");
	public static final String ATTRIBUTE_WMS_PREVIOUS_BILL_TECHNICAL_DEDUCTION_RELEASE_AMOUNT= PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionReleasePreviousBill");
	public static final String ATTRIBUTE_WMS_ABS_MBE_OID = PropertyUtil.getSchemaProperty("attribute_WMSAbsMBEOID");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionQuanity");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_RATE= PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionRate");
	public static final String ATTRIBUTE_WMS_TECHNICAL_DEDUCTION_BILL_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSTechnicalDeductionBillQuanity");

	public static final String ATTRIBUTE_MSIL_RINGI_APPROVAL_DATE= PropertyUtil.getSchemaProperty("attribute_MSILRingiApprovalDate");
	public static final String ATTRIBUTE_WMS_WORK_ORDER_DATE= PropertyUtil.getSchemaProperty("attribute_WMSWorkOrderDate");

	/** Advance particular
	* 
	* 
	*/

	public static final String ATTRIBUTE_WMS_BILL_REDUCTION_RELEASE_AMOUNT_TILL_DATE = PropertyUtil.getSchemaProperty("attribute_WMSBillReductionReleaseAmountTillDate");
	public static final String ATTRIBUTE_WMS_REDUCTION_RELEASE_AMOUNT_TILL_PREVIOUS = "attribute_WMSReductionReleaseAmountTillPrevious";
	public static final String ATTRIBUTE_WMS_BILL_REDUCTION_AMOUNT = "attribute_WMSBillReductionAmount";
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_WITHHELD_AMOUNT = "attribute_WMSAbstractMBEWithHeldAmount";
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_WITHHELD_RELEASED_AMOUNT = "attribute_WMSAbstractMBEWithHeldReleasedAmount";

	public static final String ATTRIBUTE_WMS_ADVANCE_PARTICULARS = PropertyUtil.getSchemaProperty("attribute_WMSAdvanceParticulars");
	public static final String ATTRIBUTE_WMS_ADVANCE_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSAdvanceAmount");
	public static final String ATTRIBUTE_WMS_TYPE_OF_ADVANCE = PropertyUtil.getSchemaProperty("attribute_WMSTypeofAdvance");
	public static final String ATTRIBUTE_WMS_ADVANCE_REMARKS = PropertyUtil.getSchemaProperty("attribute_WMSAdvanceRemarks");
	public static final String ATTRIBUTE_WMS_RECOVERY_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSRecoveryAmount");
	public static final String ATTRIBUTE_WMS_RECOVERY_AMOUNT_TILL_PREVIOUS = PropertyUtil.getSchemaProperty("attribute_WMSRecoveryAmountTillPrevious");
	public static final String ATTRIBUTE_WMS_RECOVERY_REMARKS = PropertyUtil.getSchemaProperty("attribute_WMSRecoveryRemarks");
	public static final String ATTRIBUTE_WMS_ADVANCE_AMOUNT_RECOVER_TILL_DATE = PropertyUtil.getSchemaProperty("attribute_WMSAdvanceAmountRecoveredTillDate");
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_PLANT_MACHINARY_ADVANCE_RECOVERED = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEPlantAndMachinaryAdvanceRecovered");
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_MOBILISATION_ADVANCE_RECOVERED = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEMobilistionAdvanceRecovered");
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_SECURED_ADVANCE_RECOVERED = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBESecuredAdvanceRecovered");
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_PLANT_MACHINARY_ADVANCE_PAID = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEPlantAndMachinaryAdvancePaid");
	public static final String ATTRIBUTE_WMS_ABSTRACT_MBE_MOBILISATION_ADVANCE_PAID = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBEMobilistionAdvancePaid");
	public static final String ATTRIBUTE_WMS_MBE_SECURED_ADVANCE_PAID = PropertyUtil.getSchemaProperty("attribute_WMSAbstractMBESecuredAdvancePaid");

	public static final String ATTRIBUTE_WMS_MATERIAL_UOM = PropertyUtil.getSchemaProperty("attribute_WMSMaterialUOM");
	public static final String ATTRIBUTE_WMS_ADVANCE_RATE = PropertyUtil.getSchemaProperty("attribute_WMSAdvanceRate");
	public static final String ATTRIBUTE_WMS_BILL_NUMBER = PropertyUtil.getSchemaProperty("attribute_WMSBillNumber");
	public static final String ATTRIBUTE_WMS_MATERIAL_BILL_SUPPLIER = PropertyUtil.getSchemaProperty("attribute_WMSMaterialBillSupplier");
	public static final String ATTRIBUTE_WMS_BILL_DATE = PropertyUtil.getSchemaProperty("attribute_WMSBillDate");

	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesPhysicalStock");
	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_PARTICULARS = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesParticulars");  
	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesQuantity");       
	public static final String ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY = PropertyUtil.getSchemaProperty("attribute_WMSStockAvailableQty");
	public static final String ATTRIBUTE_WMS_STOCK_PENDING_QTY = PropertyUtil.getSchemaProperty("attribute_WMSStockPendingQty");
	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_RATE_PER_UNIT = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesRatePerUnit");
	public static final String ATTRIBUTE_WMS_STOCK_SGST = PropertyUtil.getSchemaProperty("attribute_WMSStockSGST");
	public static final String ATTRIBUTE_WMS_STOCK_CGST = PropertyUtil.getSchemaProperty("attribute_WMSStockCGST");
	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesAmount");
	public static final String ATTRIBUTE_WMS_CONVERSION_RATE = PropertyUtil.getSchemaProperty("attribute_WMSConversionRate");
	public static final String ATTRIBUTE_WMS_TRANSPORTATION_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSTransportationBillAmount");
	public static final String ATTRIBUTE_WMS_TRANSPORTATION_BILL_NUMBER = PropertyUtil.getSchemaProperty("attribute_WMSTransportationBillNumber");
	public static final String ATTRIBUTE_WMS_LOAD_UNLOAD_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSLoadUnloadAmount");
	public static final String ATTRIBUTE_WMS_STOCK_ENTRIES_TOTAL_COST = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesTotalCost");
	public static final String ATTRIBUTE_WMS_REDUCED_RATE_FOR_ADVANCE        = PropertyUtil.getSchemaProperty("attribute_WMSReducedRateforAdvance");
	public static final String ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY   = PropertyUtil.getSchemaProperty("attribute_WMSMaterialConsumptionQuantity");
	// public static final String ATTRIBUTE_WMS_STOCK_RECOVERY_AMOUNT           = PropertyUtil.getSchemaProperty("attribute_WMSStockRecoveryAmount");
	public static final String ATTRIBUTE_WMS_SECURED_ADVANCE_TEMP            = PropertyUtil.getSchemaProperty("attribute_WMSSecuredAdvanceTemp");
	public static final String ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE       = PropertyUtil.getSchemaProperty("attribute_WMSStockApproveforAdvance");
	public static final String ATTR_WMS_STOCK_ENTRIES_AMOUNT       = PropertyUtil.getSchemaProperty("attribute_WMSStockEntriesAmount"); 
	public static final String ATTRIBUTE_WMS_REASON_FOR_NO_ADVANCE       = PropertyUtil.getSchemaProperty("attribute_WMSReasonforNoAdvance");

	//other head deduction

	public static final String ATTRIBUTE_WMS_HEAD_OTHER_DEDUCTION_DEFAULT_VALUE = PropertyUtil.getSchemaProperty("attribute_WMSHeadDeductionDefaultValue");
	public static final String ATTRIBUTE_WMS_HEAD_OTHER_DEDUCTION_DESCRIPTION = PropertyUtil.getSchemaProperty("attribute_WMSHeadDeductionDescription");
	public static final String ATTRIBUTE_WMS_HEAD_OTHER_DEDUCTION_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSHeadDeductionAmount");
	public static final String ATTRIBUTE_WMS_HEAD_OTHER_DEDUCTION_PER_STATUS = PropertyUtil.getSchemaProperty("attribute_WMSHeadDeductionCalculationUsingPercentStatus");
	public static final String ATTRIBUTE_WMS_CONVERSION_FACTOR = PropertyUtil.getSchemaProperty("attribute_WMSConversionFactor");
	public static final String ATTRIBUTE_WMS_BASE_RATE = PropertyUtil.getSchemaProperty("attribute_WMSBaseRate");

	//SDP
	public static final String ATTRIBUTE_WMS_VALID_UNTIL_DATE = PropertyUtil.getSchemaProperty("attribute_WMSValidUntilDate");
	public static final String ATTRIBUTE_WMS_SUBMITED_BY = PropertyUtil.getSchemaProperty("attribute_WMSSubmittedBy");
	public static final String ATTRIBUTE_WMS_IS_MANDATORY_DELIVERABLES = PropertyUtil.getSchemaProperty("attribute_WMSIsMandatoryDeliverables");
	public static final String ATTRIBUTE_WMS_PERCENTAGE_CONTRACT_VALUE = PropertyUtil.getSchemaProperty("attribute_WMSPercentageOfContractValue");
	public static final String  ATTRIBUTE_WMS_CALCULATED_DOCUMENT_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSCalculatedDocumentAmount");
	public static final String  ATTRIBUTE_WMS_REVIEWED_AMOUNT = PropertyUtil.getSchemaProperty("attribute_WMSReviewedAmmount");






	/***********************************POlicy constants goes below**************************************************/
	public static final String POLICY_WMS_MEASUREMENT_ITEM= PropertyUtil.getSchemaProperty("policy_WMSMeasurementItem");
	public static final String POLICY_WMS_MEASUREMENT_BOOK_ENTRY= PropertyUtil.getSchemaProperty("policy_WMSMeasurementBookEntry");
	public static final String POLICY_WMS_ABSTRACT_MEASUREMENT_BOOKENTRY= PropertyUtil.getSchemaProperty("policy_WMSAbstractMeasurementBookEntry");

	public static final String POLICY_WMS_ADVANCE_RECOVERY_ITEM = PropertyUtil.getSchemaProperty("policy_WMSAdvanceRecoveryItem");

	//Material 
	public static final String POLICY_WMS_MATERIAL = PropertyUtil.getSchemaProperty("policy_WMSMaterialCategory");
	public static final String POLICY_WMS_STOCK_ENTRIES = PropertyUtil.getSchemaProperty("policy_WMSStockEntries");
	public static final String POLICY_WMS_MATERIAL_BILL = PropertyUtil.getSchemaProperty("policy_WMSMaterialBill");
	public static final String POLICY_WMS_MATERIAL_CONSUMPTION = PropertyUtil.getSchemaProperty("policy_WMSMaterialConsumption");
	public static final String STATE_PLAN = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_ABSTRACT_MEASUREMENT_BOOKENTRY, "state_Paid");
	public static final String STATE_APPROVED = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_ABSTRACT_MEASUREMENT_BOOKENTRY, "state_Approved");
	public static final String STATE_MATERIAL_BILL_APPROVE = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_MATERIAL_BILL, "state_Approved");
	public static final String STATE_MATERIAL_ACTIVE = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_MATERIAL, "state_Active");

	public static final String STATE_STOCK_ENTRIES_STOCKED         = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_STOCK_ENTRIES, "state_Stocked");
	public static final String STATE_STOCK_ENTRIES_CONSUMED        = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_STOCK_ENTRIES, "state_Consumed");

	public static final  String TYPE_DELIVERABLE1 = PropertyUtil.getSchemaProperty("type_Deliverable1");
	public static final  String ATTRIBUTE_SUBMITTED_DATE = PropertyUtil.getSchemaProperty("attribute_SubmittedDate");
	
	//For Revised BOQ
	public static final String ATTRIBUTE_MSIL_RINGI_NUMBER                 = PropertyUtil.getSchemaProperty("attribute_MSILRingiNumber");	
	public static final String ATTRIBUTE_WMS_TOTAL_VALUE                 = PropertyUtil.getSchemaProperty("attribute_WMSTotalValue");	
	public static final String ATTRIBUTE_WMS_RINGI_AMOUNT                 = PropertyUtil.getSchemaProperty("attribute_WMSRingiAmount");	
	public static final String ATTRIBUTE_WMS_RINGI_STATUS                 = PropertyUtil.getSchemaProperty("attribute_WMSRingiStatus");	
	public static final String ATTRIBUTE_WMS_REVISED_BOQ_STATUS                 = PropertyUtil.getSchemaProperty("attribute_WMSRevisedBOQStatus");	
	public static final String RELATIONSHIP_WMS_REVISED_BOQ   = PropertyUtil.getSchemaProperty("relationship_WMSRevisedBOQ");
	public static final String TYPE_WMS_REVISED_BOQ = PropertyUtil.getSchemaProperty("type_WMSRevisedBOQ");
	public static final String POLICY_WMS_REVISED_BOQ = PropertyUtil.getSchemaProperty("policy_WMSRevisedBOQ");
	public static final String STATE_WMS_MEASUREMENT_ITEM_ACTIVE = PropertyUtil.getSchemaProperty("policy",POLICY_WMS_MEASUREMENT_ITEM, "state_Active");
	public static final String ATTRIBUTE_WMS_IS_REVISED_BOQ                 = PropertyUtil.getSchemaProperty("attribute_WMSIsRevisedBOQ");
	
	//B3-Actions start
	public static final String ATTRIBUTE_WMS_SUPPLIER_GST_NUMBER       = PropertyUtil.getSchemaProperty("attribute_WMSSupplierGSTNumber");
	public static final String TYPE_LINE_ITEM                          = PropertyUtil.getSchemaProperty("type_LineItem");
	public static final String RELATIONSHIP_WMS_SOR_TO_LINEITEM        = PropertyUtil.getSchemaProperty("relationship_WMSSORToLineItem");
	public static final String RELATIONSHIP_CLASSIFIED_ITEM            = PropertyUtil.getSchemaProperty("relationship_ClassifiedItem");
	public static final String RELATIONSHIP_RFQ_PROJECT                = PropertyUtil.getSchemaProperty("relationship_RFQProject");
	public static final String TYPE_RFQ                                = PropertyUtil.getSchemaProperty("type_RequestToSupplier");
	public static final String RELATIONSHIP_WMS_WORKORDER_RFQ          = PropertyUtil.getSchemaProperty("relationship_WMSWorkorderRFQ");
	public static final String RELATIONSHIP_RTS_SUPPLIER               = PropertyUtil.getSchemaProperty("relationship_RTSSupplier");
	public static final String RELATIONSHIP_WMS_ABSTRACT_BILL_DOCUMENTS               = PropertyUtil.getSchemaProperty("relationship_WMSAbstractBillDocuments ");
	public static final String TYPE_DELIVERABLE2               = PropertyUtil.getSchemaProperty("type_Deliverable2 ");
	public static final String ATTRIBUTE_WMS_CATEGORY_LIST = PropertyUtil.getSchemaProperty("attribute_WMSCategoryList");
	public static final String ATTRIBUTE_WMS_TURNOVER = PropertyUtil.getSchemaProperty("attribute_WMSTurnover");
	public static final String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BELOW_10MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsbelow10MRs");
	public static final String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_BETWEEN_10TO100_MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsbetween10To100MRs");
	public static final String ATTRIBUTE_WMS_CONSIDER_FOR_CONTRACTS_ABOVE_100MRS = PropertyUtil.getSchemaProperty( "attribute_WMSConsiderforContractsAbove100MRs");
	public static final String ATTRIBUTE_WMS_MAXIMUM_CONTRACT_VALUE = PropertyUtil.getSchemaProperty("attribute_WMSMaximumContractValue");

	
	



	
}