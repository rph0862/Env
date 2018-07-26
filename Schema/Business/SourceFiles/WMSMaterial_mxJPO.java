import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

 

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable;
import com.matrixone.apps.framework.ui.IncludeOIDProgramCallable;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIShortcut;
import com.matrixone.apps.framework.ui.UIShortcutUtil;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.jdom.Element;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;



public class WMSMaterial_mxJPO extends WMSConstants_mxJPO{

 
	 
     public WMSMaterial_mxJPO(Context context, String[] args) throws Exception
     {
       super(context,args);
     }

	 @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getAllMaterial(Context context, String[] args) throws Exception {
        MapList returnList = new MapList();
        try {
                String strVault = context.getVault().getName();
                String strWhere = DomainConstants.EMPTY_STRING;

                StringList slBusSelect = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(DomainConstants.SELECT_NAME);
                slBusSelect.add(DomainConstants.SELECT_REVISION);
                slBusSelect.add("to["+RELATIONSHIP_WMS_STOCK_MATERIAL+"]");
                slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"]");
                //slBusSelect.add("to["+REL_MATERIAL_CONSUMPTION_TO_MATERIAL+"]");

                returnList = DomainObject.findObjects(context,
                                                    TYPE_WMS_MATERIAL ,
                                                    DomainConstants.QUERY_WILDCARD, 
                                                    DomainConstants.QUERY_WILDCARD,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    strVault,
                                                    strWhere, // where expression
                                                    DomainConstants.EMPTY_STRING, 
                                                    false,
                                                    slBusSelect, // object selects
                                                    (short) 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public StringList getMaterialNameEdit(Context context, String[] args) throws Exception
    {
        StringList slReturnList   =    new StringList();
         try {
             StringList returnStringList = new StringList();
             Map programMap              = (Map) JPO.unpackArgs(args);
             MapList objectList          = (MapList) programMap.get("objectList");
             HashMap requestMap          = (HashMap) programMap.get("requestMap");
             int iObjectListSize         = objectList.size();
             Map dataMap                 = null;
             String isEditValue          = DomainConstants.EMPTY_STRING;
             String strStockToMatrial    = "";
             String strWOToMaterial      = "";
             String strMCToMaterial      = "";
             for (int i = 0; i < iObjectListSize; i++) {
                 dataMap                = (Map) objectList.get(i);
                 strStockToMatrial      = (String)dataMap.get("to["+RELATIONSHIP_WMS_STOCK_MATERIAL+"]");
                 strWOToMaterial        = (String)dataMap.get("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"]");
                 //strMCToMaterial        = (String)dataMap.get("to["+REL_MATERIAL_CONSUMPTION_TO_MATERIAL+"]");
                 if("True".equalsIgnoreCase(strStockToMatrial) || "True".equalsIgnoreCase(strWOToMaterial) ||  "True".equalsIgnoreCase(strMCToMaterial))
                 {
                     slReturnList.add("false");
                 }
                 else
                 {
                     slReturnList.add("true");
                 }
             }
         }
         catch (Exception e) {
            
             e.printStackTrace();
             throw e;
         }
         return slReturnList;
    }
     
    /**
     * method is used to create new segment 
     * Used for BOQ page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args 
     * @returns HashMap
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 417
     */
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public HashMap createMaterial(Context context, String [] args) throws Exception
    {
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        Map paramMap            = (Map) programMap.get("paramMap");
        HashMap retMap          = new HashMap();
        String sRowId           = DomainConstants.EMPTY_STRING;
        HashMap objMap          = new HashMap(); 
        MapList mlItems         = new MapList();


       Element elm              = (Element) programMap.get("contextData");        
       MapList chgRowsMapList   = UITableIndented.getChangedRowsMapFromElement(context, elm);
       Map mapAttr              = new HashMap();
       Map changedRowMap        = null;
       Map columnsMap           = null;
       String strName           = DomainConstants.EMPTY_STRING;
       String strMaterialUOm    = DomainConstants.EMPTY_STRING;

       for (int i = 0, size     = chgRowsMapList.size(); i < size; i++) 
       {
           try
           {   
               retMap               = new HashMap();
               changedRowMap        = (HashMap) chgRowsMapList.get(i);
               columnsMap           = (HashMap) changedRowMap.get("columns");
               sRowId               = (String) changedRowMap.get("rowId");
               strName              = (String) columnsMap.get("Name");
               strMaterialUOm       = (String) columnsMap.get("UOM");

               strName                  = strName.trim();
               String strDescription    = (String) columnsMap.get("Description");
               strDescription           = strDescription.trim();
               DomainObject doNewObject = DomainObject.newInstance(context);

               doNewObject.createObject(context,
                                       TYPE_WMS_MATERIAL ,
                                       strName,
                                       "1",
                                       POLICY_WMS_MATERIAL,
                                       context.getVault().getName());
                String strNewObjectId     = doNewObject.getId();

                doNewObject.setAttributeValue(context, ATTRIBUTE_WMS_MATERIAL_UOM , strMaterialUOm);

                doNewObject.setDescription(context,strDescription);
                retMap = new HashMap();
                retMap.put("oid", doNewObject.getObjectId(context));
                //retMap.put("relid", domRel.toString());
                //retMap.put("pid",strSelectedObjId);
                retMap.put("rid", "");
                retMap.put("markup", "new");
                retMap.put("rowId", sRowId);
                mlItems.add(retMap);
                objMap.put("changedRows", mlItems);// Adding the key "ChangedRows"
                objMap.put("Action", "success"); // Here the action can be "Success" or "refresh"
            }
            catch(Exception Ex)
            {
                Ex.printStackTrace();
                throw Ex;
            }
       }
        return objMap;
    }
    
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeSOR(Context context , String [] args)
    {
        
        StringList slReturnList = new StringList();
        try {
             HashMap programMap         = (HashMap)JPO.unpackArgs(args);
             String strObjectId = (String) programMap.get("parentOID") ;
             if(UIUtil.isNotNullAndNotEmpty(strObjectId))
             {
                StringList slBusSelect  = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                DomainObject domSor     = DomainObject.newInstance(context, strObjectId);
                MapList mlSorList       = domSor.getRelatedObjects(context, // matrix context
                                                            RELATIONSHIP_WMS_MATERIAL_TO_SOR, // relationship pattern
                                                            TYPE_WMS_SOR, // type pattern
                                                            slBusSelect, // object selects
                                                            null, // relationship selects
                                                            false, // to direction
                                                            true, // from direction
                                                            (short) 1, // recursion level
                                                            DomainConstants.EMPTY_STRING, // object where clause
                                                            DomainConstants.EMPTY_STRING, // relationship where clause
                                                            0);
                slReturnList            = WMSUtil_mxJPO.convertToStringList(mlSorList, DomainObject.SELECT_ID);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slReturnList;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMaterialSOR (Context context , String [] args ) throws Exception
	{
	   MapList mlReturnList = new MapList();
	   try {
			   Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
			   String strObjectID               = (String) programMap.get("objectId");
			   
			   StringList slBusSelect           = new StringList();
			   slBusSelect.add(DomainConstants.SELECT_ID);
			   slBusSelect.add(DomainConstants.SELECT_NAME);
			   
			   StringList slRelSelect           = new StringList();
			   slRelSelect.add(DomainRelationship.SELECT_ID);

			   DomainObject domMaterial         = DomainObject.newInstance(context , strObjectID);
			   mlReturnList                	    = domMaterial.getRelatedObjects(context, // matrix context
																				RELATIONSHIP_WMS_MATERIAL_TO_SOR, // relationship pattern
																				TYPE_WMS_SOR, // type pattern
																				slBusSelect, // object selects
																				null, // relationship selects
																				false, // to direction
																				true, // from direction
																				(short) 1, // recursion level
																				DomainConstants.EMPTY_STRING, // object where clause
																				DomainConstants.EMPTY_STRING, // relationship where clause
																				0);
		   

		   } catch (Exception e) {
		   e.printStackTrace();
		}
	   return mlReturnList;
	}
  
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWOMAdvanceRate(Context context  , String [] args) throws Exception
    {
        MapList mlReturnList = new MapList();
        try {
            HashMap programMap      = (HashMap)JPO.unpackArgs(args);
            String strObjectId      = (String) programMap.get("objectId") ;

            StringList slBusSelect  = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);
         
            StringList slRelSelect  = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            slRelSelect.add("attribute["+ATTRIBUTE_WMS_BASE_RATE+"]");
            DomainObject domWo      = DomainObject.newInstance(context , strObjectId);
            mlReturnList            = domWo.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                                TYPE_WMS_MATERIAL, // type pattern
                                                                slBusSelect, // object selects
                                                                slRelSelect, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  mlReturnList;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllWOMaterialBill(Context context  , String [] args) throws Exception
    {
        MapList mlReturnList = new MapList();
        try {
            HashMap programMap      = (HashMap)JPO.unpackArgs(args);
            String strObjectId      = (String) programMap.get("objectId") ;

            StringList slBusSelect  = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            DomainObject domWo      = DomainObject.newInstance(context , strObjectId);
            mlReturnList            = domWo.getRelatedObjects(context, // matrix context
                                                            RELATIONSHIP_WMS_WO_MATERIAL_BILL, // relationship pattern
                                                            TYPE_WMS_MATERIAL_BILL, // type pattern
                                                            slBusSelect, // object selects
                                                            null, // relationship selects
                                                            false, // to direction
                                                            true, // from direction
                                                            (short) 1, // recursion level
                                                            DomainConstants.EMPTY_STRING, // object where clause
                                                            DomainConstants.EMPTY_STRING, // relationship where clause
                                                            0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  mlReturnList;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllWOMaterialBillApprove(Context context  , String [] args) throws Exception
    {
        MapList mlReturnList = new MapList();
        try {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            String strObjectId  = (String) programMap.get("objectId") ;

            StringList strBusSelect         =     new StringList();
            strBusSelect.add(DomainConstants.SELECT_ID);
            strBusSelect.add(DomainConstants.SELECT_TYPE);
            
            StringList strListRelSelects    = new StringList(1);
            strListRelSelects.add(DomainRelationship.SELECT_ID);
            
            DomainObject domWorkOrder       = DomainObject.newInstance(context, strObjectId);
            
            Pattern patternType             = new Pattern(TYPE_WMS_MATERIAL_BILL);
            patternType.addPattern(TYPE_WMS_STOCK_ENTRIES);
            
            Pattern patternRel              = new Pattern(RELATIONSHIP_WMS_WO_MATERIAL_BILL);
            patternRel.addPattern(RELATIONSHIP_WMS_MATERIALBILL_STOCK);
            
           
            String strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_MATERIAL_BILL+"' && current=='"+STATE_MATERIAL_BILL_APPROVE+"') || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_STOCK_ENTRIES+")";
            
            mlReturnList    = domWorkOrder.getRelatedObjects(context, // matrix context
                                                                patternRel.getPattern(),               // relationship pattern
                                                                patternType.getPattern(),                     // object pattern
                                                                false,                                            // to direction
                                                                true,                                            // from direction
                                                                (short)2,                                     // recursion level
                                                                strBusSelect,                          // object selects
                                                                strListRelSelects,                             // relationship selects
                                                                strWhere,                                // object where clause
                                                                DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                (short)0,                                  // No expand limit
                                                                DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                TYPE_WMS_STOCK_ENTRIES,                            // postTypePattern
                                                                null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  mlReturnList;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMaterialStockForRegister(Context context  , String [] args) throws Exception
    {
        MapList mlReturnList = new MapList();
        try {
                HashMap programMap             = (HashMap)JPO.unpackArgs(args);
                String strObjectId             = (String) programMap.get("objectId") ;

                StringList slBusSelect         = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);

                StringList slRelSelect         = new StringList();
                slRelSelect.add(DomainConstants.SELECT_ID);

                DomainObject domMaterialBill    = DomainObject.newInstance(context , strObjectId);
                mlReturnList                    = domMaterialBill.getRelatedObjects(context, // matrix context
                                                                                    RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                                                    TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                                    slBusSelect, // object selects
                                                                                    null, // relationship selects
                                                                                    false, // to direction
                                                                                    true, // from direction
                                                                                    (short) 1, // recursion level
                                                                                    DomainConstants.EMPTY_STRING, // object where clause
                                                                                    DomainConstants.EMPTY_STRING, // relationship where clause
                                                                                    0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  mlReturnList;
    }

   public int updateAdvanceRate(Context context, String[] args) throws Exception
   {
       int iReturnValue = 0;
       try{
		   HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		   HashMap paramMap     = (HashMap) programMap.get("paramMap");
		   String strRelId      = (String) paramMap.get("relId");
		   String newValue      = (String) paramMap.get("New Value");
		   ContextUtil.pushContext(context, "User Agent",DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
		   DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_ADVANCE_RATE , newValue );  
       } catch (FrameworkException e) {
           e.printStackTrace();
           throw e;
       } finally {
           ContextUtil.popContext(context);
       }
       return iReturnValue;   
   }
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getAllMaterialWithSOR(Context context , String [] args)
    { 
        StringList slReturnValue    = new StringList();
        try {
                String strVault     = context.getVault().getName();
                String strWhere     = DomainConstants.EMPTY_STRING;
                
                StringList slBusSelect = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"]");
               
                strWhere    =   "from["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"]==True && current=='"+STATE_MATERIAL_ACTIVE+"'";
               
                MapList mlMaterialList = DomainObject.findObjects(context,
                                                                TYPE_WMS_MATERIAL ,
                                                                DomainConstants.QUERY_WILDCARD, 
                                                                DomainConstants.QUERY_WILDCARD,
                                                                DomainConstants.QUERY_WILDCARD,
                                                                strVault,
                                                                strWhere, // where expression
                                                                DomainConstants.EMPTY_STRING, 
                                                                false,
                                                                slBusSelect, // object selects
                                                                (short) 0);
                slReturnValue = WMSUtil_mxJPO.convertToStringList(mlMaterialList, DomainObject.SELECT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slReturnValue;
    }

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeWOMaterial (Context context , String [] args)
    {
        StringList slReturnList = new StringList();
        try {
             HashMap programMap         = (HashMap)JPO.unpackArgs(args);
             String strObjectId         = (String) programMap.get("objectId");
             if(strObjectId !=null && !"".equals(strObjectId) && !"null".equals(strObjectId))
             {
                StringList slBusSelect      =     new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                DomainObject domWO          = DomainObject.newInstance(context, strObjectId);
                MapList mlSorList           = domWO.getRelatedObjects(context, // matrix context
																		RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
																		TYPE_WMS_MATERIAL, // type pattern
																		slBusSelect, // object selects
																		null, // relationship selects
																		false, // to direction
																		true, // from direction
																		(short) 1, // recursion level
																		DomainConstants.EMPTY_STRING, // object where clause
																		DomainConstants.EMPTY_STRING, // relationship where clause
																		0);
                slReturnList = WMSUtil_mxJPO.convertToStringList(mlSorList, DomainObject.SELECT_ID);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slReturnList;
    }

    /**
     * To create a SOR Library
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *      0 - requestMap
     * @return Map contains created objectId
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createMaterialBill(Context context, String[] args) throws Exception {
        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();
        SimpleDateFormat simpleDateFormatMatrix  = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),context.getLocale());

        try {
                String strWorkOrderId       = (String) requestMap.get("WorkOrder");  
                String strBillNumber        = (String) requestMap.get("BillNumber");  
                String strBillDate          = (String) requestMap.get("BillDate_msvalue");
                String strDesc              = (String) requestMap.get("Description"); 
                String strSuplierDetails    = (String) requestMap.get("SupplierDetails");
                String attrSupplierGSTNumber   = PropertyUtil.getSchemaProperty(context, "attribute_WMSSupplierGSTNumber");
                Map mapAttributeMap = new HashMap();
                mapAttributeMap.put(ATTRIBUTE_WMS_BILL_NUMBER, strBillNumber);
                mapAttributeMap.put(ATTRIBUTE_WMS_MATERIAL_BILL_SUPPLIER, strSuplierDetails);
                mapAttributeMap.put(ATTRIBUTE_WMS_BILL_DATE, getEmatrixDateFormat(context, strBillDate));
                String strSupplierOrgId = PersonUtil.getUserCompanyId(context);
                DomainObject domSupplier= DomainObject.newInstance(context, strSupplierOrgId);
                String strGSTNumber = domSupplier.getAttributeValue(context, attrSupplierGSTNumber);

                DomainObject doNewObject    = DomainObject.newInstance(context);
                doNewObject.createObject(context,
                                          TYPE_WMS_MATERIAL_BILL ,
                                           doNewObject.getUniqueName("Bill_"),
                                           "1",
                                           POLICY_WMS_MATERIAL_BILL,
                                           context.getVault().getName());

                String strNewObjectId     = doNewObject.getId();
                mapAttributeMap.put(attrSupplierGSTNumber, strGSTNumber);
                doNewObject.setAttributeValues(context, mapAttributeMap);
                doNewObject.setDescription(context, strDesc);
                DomainRelationship.connect(context,strWorkOrderId ,RELATIONSHIP_WMS_WO_MATERIAL_BILL ,strNewObjectId ,true);    
                returnMap.put("id", strNewObjectId);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return returnMap;
    }

    public String getEmatrixDateFormat(Context context,String strDateForFormat )throws Exception 
    {
        String strFormattedDate = "";
        try{
                Calendar calendarDate   = Calendar.getInstance();
                long lDateForFormat     = Long.parseLong(strDateForFormat);
                calendarDate.setTimeInMillis(lDateForFormat);
                SimpleDateFormat simpleDateFormatMatrix  = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),context.getLocale());
                strFormattedDate        = simpleDateFormatMatrix.format(calendarDate.getTime());

                Date dStartDate         = simpleDateFormatMatrix.parse(strFormattedDate);                
                DateFormat dateFormat   = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),context.getLocale());
                strFormattedDate        = dateFormat.format(dStartDate);
        }
        catch(Exception exception) 
        {
            exception.printStackTrace();
            throw exception;
        }
        return strFormattedDate;
    }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getBillStock(Context context  , String [] args) throws Exception
    {
        MapList mlReturnList = new MapList();
        try {
                HashMap programMap  = (HashMap)JPO.unpackArgs(args);
                HashMap requestMap  = (HashMap) programMap.get("requestMap");
                String strObjectId  = (String) programMap.get("objectId") ;

                StringList slBusSelect  = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(DomainConstants.SELECT_NAME);
                slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
                StringList slRelSelect  = new StringList();
                slRelSelect.add(DomainRelationship.SELECT_ID); 
                
                DomainObject domBill    =   DomainObject.newInstance(context , strObjectId);
                mlReturnList            =   domBill.getRelatedObjects(context, // matrix context
																		RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
																		TYPE_WMS_STOCK_ENTRIES, // type pattern
																		slBusSelect, // object selects
																		slRelSelect, // relationship selects
																		false, // to direction
																		true, // from direction
																		(short) 1, // recursion level
																		DomainConstants.EMPTY_STRING, // object where clause
																		DomainConstants.EMPTY_STRING, // relationship where clause
																		0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  mlReturnList;
    }
	
     /**
     * method is used to create new segment 
     * Used for BOQ page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args 
     * @returns HashMap
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 417
     */
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public HashMap createMaterialBillStock(Context context, String [] args) throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        Map paramMap                = (Map) programMap.get("paramMap");

        String strMaterialBillId    = (String) paramMap.get("objectId"); 
        HashMap retMap              = new HashMap();
        String sRowId               = DomainConstants.EMPTY_STRING;
        HashMap objMap              = new HashMap(); 
        MapList mlItems             = new MapList();

       Element elm                  = (Element) programMap.get("contextData");
       MapList chgRowsMapList       = UITableIndented.getChangedRowsMapFromElement(context, elm);
       Map mapAttr                  = new HashMap();
       Map changedRowMap            = null;
       Map columnsMap               = null;

       String strParticulars        = DomainConstants.EMPTY_STRING;
       String strMaterialType       = DomainConstants.EMPTY_STRING;
       String strQuantity           = DomainConstants.EMPTY_STRING;
       String strRatePerUnit        = DomainConstants.EMPTY_STRING;
       String strPhysicalStock      = DomainConstants.EMPTY_STRING;
       String strAmount             = DomainConstants.EMPTY_STRING;
       String strCGST               = DomainConstants.EMPTY_STRING;
       String strSGST               = DomainConstants.EMPTY_STRING;
       String strMaker              = DomainConstants.EMPTY_STRING;
       Map attributeMap             = new HashMap();


       for (int i = 0, size = chgRowsMapList.size(); i < size; i++) 
       {
           try
           {
               retMap               = new HashMap();
               changedRowMap        = (HashMap) chgRowsMapList.get(i);
               columnsMap           = (HashMap) changedRowMap.get("columns");
               sRowId               = (String) changedRowMap.get("rowId");
               strParticulars       = (String) columnsMap.get("Particulars");
               strMaterialType      = (String) columnsMap.get("MaterialType");
               strQuantity          = (String) columnsMap.get("Quantity");
               strRatePerUnit       = (String) columnsMap.get("RatePerUnit");
               strPhysicalStock     = (String) columnsMap.get("PhysicalStock");
               strCGST              = (String) columnsMap.get("WMSStockSGST");
               strSGST              = (String) columnsMap.get("WMSStockCGST");
               strMaker             =(String) columnsMap.get("MBMaker");
               
               strCGST              =   strCGST==null || "".equals(strCGST) || "null".equals(strCGST) ? "0.0" : strCGST;
               strSGST              =   strSGST==null || "".equals(strSGST) || "null".equals(strSGST) ? "0.0" : strSGST;

               attributeMap         = new HashMap();
               attributeMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_PARTICULARS      , strParticulars );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY         , strQuantity );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY            , strQuantity );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY              , "0.0" );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_RATE_PER_UNIT    , strRatePerUnit );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK   , strPhysicalStock );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_SGST                     , strSGST );
               attributeMap.put(ATTRIBUTE_WMS_STOCK_CGST                     , strCGST );
               
               try {
                   Double dRateParUnit  = Double.parseDouble(strRatePerUnit);
                   Double dQuantity     = Double.parseDouble(strQuantity);
                   Double bAmount       = dRateParUnit * dQuantity;

                   Double dSGST         = Double.parseDouble(strSGST);
                   Double dCGST         = Double.parseDouble(strCGST);
                   Double bTotalTex     =  dSGST+dSGST;
                   if(bTotalTex>0)
                   {
                       Double bTotalTexAount    = (bAmount*bTotalTex)/100;
                       bAmount                  = bTotalTexAount+bAmount;
                   }
                   attributeMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT, ""+bAmount);
                } catch (Exception e) {
                    
                }

               DomainObject doNewObject = DomainObject.newInstance(context);
              doNewObject.createObject(context,
                                       TYPE_WMS_STOCK_ENTRIES,
                                       doNewObject.getUniqueName("STOCK_"),
                                       "",
                                       POLICY_WMS_STOCK_ENTRIES,
                                       context.getVault().getName());

                String strNewObjectId      = doNewObject.getId();
                doNewObject.setAttributeValues(context,attributeMap);
                DomainRelationship.connect(context,strNewObjectId ,RELATIONSHIP_WMS_STOCK_MATERIAL,strMaterialType  ,true);  
                DomainRelationship domRel   =  DomainRelationship.connect(context, DomainObject.newInstance(context, strMaterialBillId), RELATIONSHIP_WMS_MATERIALBILL_STOCK , doNewObject);
                if(!strMaker.isEmpty())
                      DomainRelationship.connect(context, strMaker, RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF,strNewObjectId ,false);
                
                retMap = new HashMap();
                retMap.put("oid", doNewObject.getObjectId(context));
                retMap.put("relid", domRel.toString());
                retMap.put("pid",strMaterialBillId);
                retMap.put("rid", "");
                retMap.put("markup", "new");
                retMap.put("rowId", sRowId);
                mlItems.add(retMap);
                objMap.put("changedRows", mlItems);// Adding the key "ChangedRows"
                objMap.put("Action", "success"); // Here the action can be "Success" or "refresh"
            }
            catch(Exception Ex)
            {
                Ex.printStackTrace();
                throw Ex;
            }
       }
        return objMap;
    }


    public HashMap getWOMaterial(Context context  , String [] args) throws Exception
    {
        HashMap returnMap = new HashMap();
        try {
                HashMap programMap          = (HashMap)JPO.unpackArgs(args);
                HashMap requestMap          = (HashMap) programMap.get("requestMap");
                String strObjectId          = (String) requestMap.get("objectId") ;

                StringList slBusSelect      = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(DomainConstants.SELECT_NAME);

                DomainObject domMaterial    = DomainObject.newInstance(context , strObjectId);
                String strWoId              = domMaterial.getInfo(context, "to["+RELATIONSHIP_WMS_WO_MATERIAL_BILL+"].from.id");    
                DomainObject domWo          = DomainObject.newInstance(context , strWoId);
                MapList mlWOMaterial        = domWo.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                                TYPE_WMS_MATERIAL, // type pattern
                                                                slBusSelect, // object selects
                                                                null, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);

                StringList slDisplayValue   = new StringList();
                StringList slActualValue    = new StringList();

                Map dataMap                 = null;
                Integer iListSize           = mlWOMaterial.size();
                for (int i  = 0; i < iListSize ; i++) {
                    dataMap    =    (Map)mlWOMaterial.get(i);
                    slDisplayValue.add((String) dataMap.get(DomainConstants.SELECT_NAME));
                    slActualValue.add((String) dataMap.get(DomainConstants.SELECT_ID));
                }
                slDisplayValue.add(DomainConstants.EMPTY_STRING);
                slActualValue.add(DomainConstants.EMPTY_STRING);
                returnMap.put("field_choices", slActualValue);
                returnMap.put("field_display_choices", slDisplayValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  returnMap;
    }

   public void updateMaterial(Context context , String[] args) throws Exception {
       try{
           Map programMap              = (Map)JPO.unpackArgs(args);
           HashMap paramMap            = (HashMap) programMap.get("paramMap");
           HashMap requestMap          = (HashMap) programMap.get("requestMap");
           String strStockId           = (String) paramMap.get("objectId");
           String strMaterialNewValue  = (String) paramMap.get("New Value");
           if(UIUtil.isNotNullAndNotEmpty(strStockId)){
               DomainObject doStock = DomainObject.newInstance(context,strStockId);
               DomainRelationship.modifyTo(context, doStock.getInfo(context , "from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].id"), DomainObject.newInstance(context , strMaterialNewValue));
           }
       }catch(Exception e){
           e.printStackTrace();
       }
   }
 
  public int updateStockQut (Context context, String[] args) throws Exception
  {
      int iReturnValue = 0;
      try{
              HashMap programMap   = (HashMap) JPO.unpackArgs(args);
              HashMap paramMap     = (HashMap) programMap.get("paramMap");
              String strRelId      = (String) paramMap.get("relId");
              String strObjId      = (String) paramMap.get("objectId");
              String newValue      = (String) paramMap.get("New Value");
              ContextUtil.pushContext(context,
                                      "User Agent",
                                      DomainConstants.EMPTY_STRING,
                                      DomainConstants.EMPTY_STRING);
              HashMap mapAttrMap        = new HashMap();
              mapAttrMap.put(ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY, newValue);
              mapAttrMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY, newValue);
              DomainObject domStock     =    DomainObject.newInstance(context,strObjId );
              domStock.setAttributeValues(context, mapAttrMap);
      } catch (FrameworkException e) {
          e.printStackTrace();
          throw e;
      } finally {
          ContextUtil.popContext(context);
      }
      return iReturnValue;   
  }
    
  /** Trigger method  State Create Check on Policy Material Bill
   * 
   * 
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  
  public int checkStockQut(Context  context , String [] args) throws Exception
  {
      int iReturn =0;
      try {
          String strObjectId       = args[0];
          StringList slBusSelect   = new StringList();
          slBusSelect.add(DomainConstants.SELECT_ID);
          slBusSelect.add(DomainConstants.SELECT_NAME);
          slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");

          StringList slRelSelect   = new StringList();
          slRelSelect.add(DomainRelationship.SELECT_ID); 
          
          DomainObject domBill     = DomainObject.newInstance(context , strObjectId);
          MapList mlStockList      = domBill.getRelatedObjects(context, // matrix context
                                                          RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                          TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                          slBusSelect, // object selects
                                                          slRelSelect, // relationship selects
                                                          false, // to direction
                                                          true, // from direction
                                                          (short) 1, // recursion level
                                                          DomainConstants.EMPTY_STRING, // object where clause
                                                          DomainConstants.EMPTY_STRING, // relationship where clause
                                                          0);
          

          Boolean bCheck            = Boolean.TRUE;
          Map dataMap               = null;
          String strAvailebleQut    = DomainConstants.EMPTY_STRING;
          int iListSize             = mlStockList.size();
          Double bValue             = null;
          
          if(iListSize==0)
          {
              iReturn   =   1;
          }
          
          for (int i = 0; i < iListSize; i++) {
              dataMap   =   (Map)mlStockList.get(i);
              strAvailebleQut   =   (String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");
              if(strAvailebleQut!=null && !"".equals(strAvailebleQut)&& !"null".equals(strAvailebleQut))
              {
                  bValue    =   Double.parseDouble(strAvailebleQut);
                  if(bValue==0.0)
                  {
                      bCheck    =  Boolean.TRUE;;
                      break;
                  }
              }
              else
              {
                  bCheck    =  Boolean.TRUE;
                  break;
              }
          }
          if(bCheck==false)
          {
              iReturn   =   1;
          }
          
          if(iReturn==1)
          {
              emxContextUtil_mxJPO.mqlNotice(context,"Kndly Add Stock and fill QUT");
          }
    } catch (Exception e) {
       e.printStackTrace();
    }
    return  iReturn;  
  }
  /** Method bring SOR connected to Material 
   * 
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
 
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAssociatedSOR(Context context,String[] args) throws Exception
  {
	MapList mlData = new MapList();
	try {
		  HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		  String strMatOid = (String) programMap.get("objectId");
		  DomainObject domMaterial = DomainObject.newInstance(context,strMatOid);
		  StringList slBusSelects = new StringList();
		  slBusSelects.add(DomainConstants.SELECT_ID);
		  slBusSelects.add("attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]");
		  StringList slRelSelects = new StringList();
		  slRelSelects.add(DomainRelationship.SELECT_ID);
		 return  domMaterial.getRelatedObjects(context, 
				                            RELATIONSHIP_WMS_MATERIAL_TO_SOR,
				                            TYPE_WMS_SOR, 
				                            slBusSelects,
				                            slRelSelects,
				                            false,
				                            true,
				                            (short)1,
				                            DomainConstants.EMPTY_STRING,
				                            DomainConstants.EMPTY_STRING,
				                            0);
	    }catch(Exception e) {
		e.printStackTrace();
	}
	return mlData;
  }
/**
 *   
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
 
  public Vector getSORLink(Context context,String[] args) throws Exception
  {
	  
	  HashMap programMap   = (HashMap) JPO.unpackArgs(args);
	  MapList mlMaterils = (MapList) programMap.get("objectList");
	  Vector vColumn =new Vector(mlMaterils.size());
	  String strObjectId= DomainConstants.EMPTY_STRING;
	  for(int i=0;i<mlMaterils.size();i++) {
		  StringBuilder sbItemLink=new StringBuilder();
		    Map m =(Map)mlMaterils.get(i);
		    strObjectId =(String) m.get(DomainConstants.SELECT_ID);
		    sbItemLink.append("<a title='View DSR' href ='javascript:showModalDialog(\"");
			sbItemLink.append("../common/emxIndentedTable.jsp?table=WMSSORViewTable&amp;Export=true&amp;expandLevelFilter=true&amp;selection=multiple&amp;header=WMS.Table.Header.AssociatedDSR&amp;suiteKey=WMS&amp;HelpMarker=emxhelpgatechecklist&amp;toolbar=WMSAssociatedDSRActions&amp;program=WMSMaterial:getAssociatedSOR&amp;editLink=true&amp;objectId=");
			sbItemLink.append(strObjectId);
			sbItemLink.append("\", \"875\", \"550\", \"false\", \"popup\")'>");
			sbItemLink.append("<img src='../common/images/iconSmallCostRate.png'></img>");
			sbItemLink.append("</a>");
			vColumn.add(sbItemLink.toString());
		  
	  }
	  
	 return vColumn; 
  }
  
  
  /***************************************  Material Makers Library ************************************************************/
  /****************************************************************************************************************************/
  
  /**
   * method is used to create new Material Maker 
   * Used for Material Makers Library
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args 
   * @returns HashMap
   * @throws Exception if the operation fails
   * @author WMS
   * @since 417
   */
  @com.matrixone.apps.framework.ui.ConnectionProgramCallable
  public HashMap createMaterialMaker(Context context, String [] args) throws Exception
  {
      HashMap programMap      = (HashMap) JPO.unpackArgs(args);
      Map paramMap            = (Map) programMap.get("paramMap");
      HashMap retMap          = new HashMap();
      String sRowId           = DomainConstants.EMPTY_STRING;
      HashMap objMap          = new HashMap(); 
      MapList mlItems         = new MapList();
      Element elm              = (Element) programMap.get("contextData");        
      MapList chgRowsMapList   = UITableIndented.getChangedRowsMapFromElement(context, elm);
      Map mapAttr              = new HashMap();
      Map changedRowMap        = null;
      Map columnsMap           = null;
      String strName           = DomainConstants.EMPTY_STRING;
      String strMaterialUOm    = DomainConstants.EMPTY_STRING;

     for (int i = 0, size     = chgRowsMapList.size(); i < size; i++) 
     {
         try
         {   
             retMap               = new HashMap();
             changedRowMap        = (HashMap) chgRowsMapList.get(i);
             columnsMap           = (HashMap) changedRowMap.get("columns");
             sRowId               = (String) changedRowMap.get("rowId");
             strName              = (String) columnsMap.get("Name");
              strName                  = strName.trim();
             String strDescription    = (String) columnsMap.get("Description");
             strDescription           = strDescription.trim();
             DomainObject doNewObject = DomainObject.newInstance(context);
             String strMMOid  = FrameworkUtil.autoName(context, "type_WMSMaterialMaker", "policy_WMSMaterialMaker");
             doNewObject.setId(strMMOid);
              String strNewObjectId     = doNewObject.getId();
              doNewObject.setAttributeValue(context, ATTRIBUTE_WMS_WORK_ORDER_TITLE , strName);

              doNewObject.setDescription(context,strDescription);
              retMap = new HashMap();
              retMap.put("oid", doNewObject.getObjectId(context));
              retMap.put("rid", "");
              retMap.put("markup", "new");
              retMap.put("rowId", sRowId);
              mlItems.add(retMap);
              objMap.put("changedRows", mlItems);// Adding the key "ChangedRows"
              objMap.put("Action", "success"); // Here the action can be "Success" or "refresh"
          }
          catch(Exception Ex)
          {
              Ex.printStackTrace();
              throw Ex;
          }
     }
      return objMap;
  }
  
  /** Method which brings Active Material Makers 
   * 
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAllMaterialMakers(Context context, String[] args) throws Exception {
     MapList returnList = new MapList();
     try {
             String strVault = context.getVault().getName();
             String strWhere = DomainConstants.EMPTY_STRING;

             StringList slBusSelect = new StringList();
             slBusSelect.add(DomainConstants.SELECT_ID);
             returnList = DomainObject.findObjects(context,
            		                             TYPE_WMS_MATERIAL_MAKER ,
                                                 DomainConstants.QUERY_WILDCARD, 
                                                 DomainConstants.QUERY_WILDCARD,
                                                 DomainConstants.QUERY_WILDCARD,
                                                 strVault,
                                                 "current=='Active' && name!='Others (Approval Taken)'", // where expression
                                                 DomainConstants.EMPTY_STRING, 
                                                 false,
                                                 slBusSelect, // object selects
                                                 (short) 0);
     } catch (Exception e) {
         e.printStackTrace();
     }
     return returnList;
 }
  /**Method bring Makers of context Material 
   * 
   * @param context
   * @param args
   * @return
   * @throws Exception
   */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAssociatedMakers (Context context , String [] args ) throws Exception
	{
	   MapList mlReturnList = new MapList();
	   try {
			   Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
			   String strObjectID               = (String) programMap.get("objectId");
			   
			   StringList slBusSelect           = new StringList();
			   slBusSelect.add(DomainConstants.SELECT_ID);
			   slBusSelect.add(DomainConstants.SELECT_NAME);
			   
			   StringList slRelSelect           = new StringList();
			   slRelSelect.add(DomainRelationship.SELECT_ID);
		       DomainObject domMaterial         = DomainObject.newInstance(context , strObjectID);
			   mlReturnList                	    = domMaterial.getRelatedObjects(context, // matrix context
					                                                            RELATIONSHIP_WMS_MAKER_OF, // relationship pattern
																				TYPE_WMS_MATERIAL_MAKER, // type pattern
																				slBusSelect, // object selects
																				slRelSelect, // relationship selects
																				true, // to direction
																				false, // from direction
																				(short) 1, // recursion level
																				DomainConstants.EMPTY_STRING, // object where clause
																				DomainConstants.EMPTY_STRING, // relationship where clause
																				0);
		   

		   } catch (Exception e) {
		   e.printStackTrace();
		}
	   return mlReturnList;
	}

	
	/**
	 *   
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	 
	  public Vector getAssociatedMakerLink(Context context,String[] args) throws Exception
	  {
		  
		  HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		  MapList mlMaterils = (MapList) programMap.get("objectList");
		  Vector vColumn =new Vector(mlMaterils.size());
		  String strObjectId= DomainConstants.EMPTY_STRING;
		  for(int i=0;i<mlMaterils.size();i++) {
			  StringBuilder sbItemLink=new StringBuilder();
			    Map m =(Map)mlMaterils.get(i);
			    strObjectId =(String) m.get(DomainConstants.SELECT_ID);
			    sbItemLink.append("<a  title='View Maker' href ='javascript:showModalDialog(\"");
				sbItemLink.append("../common/emxIndentedTable.jsp?table=WMSMaterialMakers&amp;Export=true&amp;expandLevelFilter=true&amp;selection=multiple&amp;header=WMS.Table.Header.AssociatedMakers&amp;suiteKey=WMS&amp;HelpMarker=emxhelpgatechecklist&amp;toolbar=WMSAssociatedMakersActions&amp;program=WMSMaterial:getAssociatedMakers&amp;editLink=false&amp;objectId=");
				sbItemLink.append(strObjectId);
				sbItemLink.append("\", \"875\", \"550\", \"false\", \"popup\")'>");
				sbItemLink.append("<img style='height:30px;width:30px' src='../common/images/I_Material_Maker.png'></img>");
				sbItemLink.append("</a>");
				vColumn.add(sbItemLink.toString());
			  
		  }
		  
		 return vColumn; 
	  }
	  
	
	  @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	    public StringList excludeAssociatedMakers(Context context , String [] args)
	    {
	        StringList slReturnList = new StringList();
	        try {
	             HashMap programMap         = (HashMap)JPO.unpackArgs(args);
	             String strObjectId = (String) programMap.get("parentOID");
	             String strExId = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE_WMS_MATERIAL_MAKER, "Others (Approval Taken)", "-","id");
	             if(UIUtil.isNotNullAndNotEmpty(strObjectId))
	             {
	                StringList slBusSelect  = new StringList();
	                slBusSelect.add(DomainConstants.SELECT_ID);
	              
	                DomainObject domMaterial     = DomainObject.newInstance(context, strObjectId);
	                MapList mlSorList            =  domMaterial.getRelatedObjects(context, // matrix context
									                            RELATIONSHIP_WMS_MAKER_OF, // relationship pattern
																TYPE_WMS_MATERIAL_MAKER, // type pattern
																slBusSelect, // object selects
																null, // relationship selects
																true, // to direction
																false, // from direction
																(short) 1, // recursion level
																DomainConstants.EMPTY_STRING, // object where clause
																DomainConstants.EMPTY_STRING, // relationship where clause
																0);
	                
	                
	                slReturnList            = WMSUtil_mxJPO.convertToStringList(mlSorList, DomainObject.SELECT_ID);
	                if(!slReturnList.contains(strExId.trim())) {
	                	slReturnList.add(strExId);
	                }
	             }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return slReturnList;
	    }
	  
	  
/**
 * Trigger which checks if file is checked in if Maker is Others (Approval Taken) 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	  
public int triggerMakerApprovalTakenFileCheck(Context context,String[] args) throws Exception{
 	int iReturn=0;
	try {
		
		 String strMBillId = args[0];
		 StringList slBusSelect   = new StringList();
         slBusSelect.add(DomainConstants.SELECT_ID);
         slBusSelect.add(DomainConstants.SELECT_NAME);
         slBusSelect.add("from["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"]");
         slBusSelect.add("to["+RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF+"].from.name");
         slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PARTICULARS+"]");
         StringList slRelSelect   = new StringList();
         slRelSelect.add(DomainRelationship.SELECT_ID); 
         DomainObject domBill     = DomainObject.newInstance(context , strMBillId);
         List lsParticulars=new ArrayList();
         MapList mlStockList      = domBill.getRelatedObjects(context, // matrix context
                                                         RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                         TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                         slBusSelect, // object selects
                                                         slRelSelect, // relationship selects
                                                         false, // to direction
                                                         true, // from direction
                                                         (short) 1, // recursion level
                                                        "to["+RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF+"].from.name == 'Others (Approval Taken)'", // object where clause
                                                         DomainConstants.EMPTY_STRING, // relationship where clause
                                                         0);
         Iterator<Map> itr = mlStockList.iterator();
         String strReferenceDocument=DomainConstants.EMPTY_STRING;
         String strParticularName=DomainConstants.EMPTY_STRING;
         while(itr.hasNext()) {
        	 Map m = itr.next();
        	 strReferenceDocument = (String) m.get("from["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"]");
        	 if(strReferenceDocument.equalsIgnoreCase("FALSE")) {
        		 lsParticulars.add((String)m.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PARTICULARS+"]"));
         	 }
          }
          if( lsParticulars.size() >0 ) {
        	  Locale locale = context.getLocale();
    		  String strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", locale, "WMS.alert.MakerApprovalNoFileFound");
        	  String strError = lsParticulars.toString();
        	  strError=strError.replaceAll("[\\[\\]]","");
        	  emxContextUtil_mxJPO.mqlNotice(context,strMessage+" \n" + strError); 
        	  return 1;
         }
         
      
		
		}catch(Exception e) {
			
			e.printStackTrace();
		}
	
	return iReturn;
    }	
/**
 *  Check trigger to confirm wehte reason is updated or not 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */



public int isEEFillComment(Context  context , String [] args) throws Exception
{
       int iReturn =1;
    try {

        String strObjectId        = args[0];
        StringList slBusSelect    = new StringList();
        slBusSelect.add(DomainConstants.SELECT_ID);
        slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE+"]");
        slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
        slBusSelect.add("attribute["+ATTRIBUTE_WMS_REASON_FOR_NO_ADVANCE+"]");
        DomainObject domBill     = DomainObject.newInstance(context , strObjectId);
        MapList mlStockList      = domBill.getRelatedObjects(context, // matrix context
                                                        RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                        TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                        slBusSelect, // object selects
                                                        null, // relationship selects
                                                        false, // to direction
                                                        true, // from direction
                                                        (short) 1, // recursion level
                                                        DomainConstants.EMPTY_STRING, // object where clause
                                                        DomainConstants.EMPTY_STRING, // relationship where clause
                                                        0);
        Boolean bCheck            = Boolean.TRUE;
        Map dataMap               = null;
        String strPhyStock        = DomainConstants.EMPTY_STRING;
        String strReason          = DomainConstants.EMPTY_STRING;
        String strApproveForAvance= DomainConstants.EMPTY_STRING;
        
        int iListSize             = mlStockList.size();
        Double bValue             = null;
            
        ArrayList<String>  alStockList    =   new  ArrayList<String>();
        
        String strMassage         =   DomainConstants.EMPTY_STRING;
        Boolean bCheck1           =   false;
        for (int i = 0; i < iListSize; i++) {
            dataMap               = (Map)mlStockList.get(i);
            strPhyStock           = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
            strReason             = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_REASON_FOR_NO_ADVANCE+"]");
            strApproveForAvance   = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE+"]");
            if("Yes".equals(strPhyStock))
            {
                if("".equals(strApproveForAvance))
                {
                    bCheck1       = true;
                    strMassage    =  "WMS.alert.UpdateApproveForAdvance";
                    iReturn=1;
                    break;
                }
                else if("No".equals(strApproveForAvance) && "".equals(strReason))
                {
                    bCheck1       =  true;
                    strMassage    = "WMS.alert.ReasonForNoAdvance";
                    iReturn=1;
                    break; 
                }
            }
        }
        if(bCheck1)
        {
            emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), strMassage)); 
        }
        else
        {
            iReturn=0;
        }
  } catch (Exception e) {
     e.printStackTrace();
     throw e;
  }
   return  iReturn;  
}



/** 
 * On Policy Material Bill Review Action
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */


public int promoteStock(Context context , String [] args) throws Exception
{
    int iReturn   = 0;
    try {
            String strObjectId        = args[0];
            StringList slBusSelect    = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
       
            DomainObject domBill      = DomainObject.newInstance(context , strObjectId);
            MapList mlStockList       = domBill.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                null, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        
        
            
            Map dataMap               = null;
            String strStockId         = DomainConstants.EMPTY_STRING;
            int iListSize             = mlStockList.size();
            ArrayList<String>  alStockList    = new  ArrayList<String>();
            DomainObject domStock             = DomainObject.newInstance(context);
            
            for (int i = 0; i < iListSize; i++) {
                dataMap       = (Map)mlStockList.get(i);
                strStockId    = (String) dataMap.get(DomainConstants.SELECT_ID);
                domStock.setId(strStockId);
                domStock.promote(context);
            }
  } catch (Exception e) {
     e.printStackTrace();
  }
   return  iReturn;
}

/**
 *  Trigger on Material Bill Create Promote Action ,which Calculates Transportation cost /Loading unloading charge on Stock Entries 
 * 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public int triggerCalculateBillParticularsCostDetails(Context context,String[] args) throws Exception{
	 	try {
		    
	 		String strMaterialBillid= args[0];
			StringList slBusSelect    = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]");
            slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_RATE_PER_UNIT+"]");
            slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT+"]");
            DomainObject domBill      = DomainObject.newInstance(context , strMaterialBillid);
            
            StringList slBus=new StringList(2);
            slBus.add("attribute["+ATTRIBUTE_WMS_TRANSPORTATION_AMOUNT+"]");
            slBus.add("attribute["+ATTRIBUTE_WMS_LOAD_UNLOAD_AMOUNT+"]");
            Map mMBillInfo =  domBill.getInfo(context, slBus);
            double dLoadUnloadCharge=Double.parseDouble((String)mMBillInfo.get("attribute["+ATTRIBUTE_WMS_LOAD_UNLOAD_AMOUNT+"]"));
            double dTranspostAmount=Double.parseDouble((String)mMBillInfo.get("attribute["+ATTRIBUTE_WMS_TRANSPORTATION_AMOUNT+"]"));
            /*MQL<110>eval expression 'SUM attribute[WMSStockEntriesQuantity]' on temp query bus WMSStockEntries * * where "to[WMSMaterialBillStock].from.name=='Bill_11524140201865'";
                 1150.0*/
            MapList mlStockList       = domBill.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIALBILL_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                null, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        
           Iterator<Map> itr =mlStockList.iterator();
           double dStockEntryQty=0;
           double dTotalQty=0;
           double dStockLoadinCharges=0;
           double dPurchaseCost=0;
           StringList slStockIds = new StringList(mlStockList.size());
           Map mStockCacheData=new HashMap();
           String strStockID=DomainConstants.EMPTY_STRING;
            while(itr.hasNext()) {
            	 Map mTemp=new HashMap();
        	     Map m =itr.next();  
        	     strStockID=(String)m.get(DomainConstants.SELECT_ID);
        	     slStockIds.add(strStockID);
        	     dStockEntryQty  = Double.parseDouble((String)m.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]"));
        	     dPurchaseCost=Double.parseDouble((String)m.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT+"]"));
        	     dStockLoadinCharges=dLoadUnloadCharge*dStockEntryQty;
         	     mTemp.put("QTY", String.valueOf(dStockEntryQty));
         	     mTemp.put("LOAD-UNLOADCHARGE", String.valueOf(dStockLoadinCharges));
         	     mTemp.put("PCOST", String.valueOf(dPurchaseCost));
         	     mStockCacheData.put(strStockID, mTemp);
        	     dTotalQty=dTotalQty+dStockEntryQty;
            }
            DecimalFormat  dd=new DecimalFormat("0.00");
		   double dTransportRateUnit= dTranspostAmount/dTotalQty;
           Iterator<String> itrIds = slStockIds.iterator();
           DomainObject domStockEntries=DomainObject.newInstance(context);
           double dTransportCostPerEntry=0;
           double dLoadingCostPerEntry=0;
           double dTotalCostPerEntry=0;
            while(itrIds.hasNext()) {
            	strStockID=itrIds.next();
            	domStockEntries.setId(strStockID);
            	Map mTemp = (Map) mStockCacheData.get(strStockID);
            	dStockEntryQty=Double.parseDouble((String)mTemp.remove("QTY"));
            	dLoadingCostPerEntry=Double.parseDouble((String)mTemp.remove("LOAD-UNLOADCHARGE"));
            	dTransportCostPerEntry=dTransportRateUnit*dStockEntryQty;
            	dTotalCostPerEntry=dTransportCostPerEntry+dLoadingCostPerEntry+Double.parseDouble((String)mTemp.remove("PCOST"));
            	mTemp.put(ATTRIBUTE_WMS_TRANSPORTATION_AMOUNT, dd.format(dTransportCostPerEntry));
            	mTemp.put(ATTRIBUTE_WMS_LOAD_UNLOAD_AMOUNT,  dd.format(dLoadingCostPerEntry));
            	mTemp.put(ATTRIBUTE_WMS_STOCK_ENTRIES_TOTAL_COST,  dd.format(dTotalCostPerEntry));  
            	domStockEntries.setAttributeValues(context, mTemp);
             }
		
		
		     }catch(Exception e) {
			
			e.printStackTrace();
		}
     return 0;
 }

/**
 *  DO NOT PROMOTE THIS 
 * 
 */
@com.matrixone.apps.framework.ui.ProgramCallable
 public MapList  getMaterialBill(Context context,String[] args) {
	
	try {
	 Map inputMap=(Map)JPO.unpackArgs(args);
    String strState=(String)inputMap.get("state");
	String strWhre = "current =="+strState+" && owner=="+context.getUser();
	StringList slBusSelect    = new StringList();
	    slBusSelect.add(DomainConstants.SELECT_ID);
		 return DomainObject.findObjects(context,
	             TYPE_WMS_MATERIAL_BILL ,
	             DomainConstants.QUERY_WILDCARD, 
	             DomainConstants.QUERY_WILDCARD,
	             DomainConstants.QUERY_WILDCARD,
	             context.getVault().getName(),
	             strWhre, // where expression
	             DomainConstants.EMPTY_STRING, 
	             false,
	             slBusSelect, // object selects
	             (short) 0);
	     }catch(Exception e) {
		       e.printStackTrace();
	         }
	return new MapList();
      }

/** Should be visible to DPM approver and has inbox task
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
public boolean isEEAndOpenTask(Context context, String[] args)throws Exception
{
    HashMap programMap    = (HashMap) JPO.unpackArgs(args);
    String strObjectId    = (String) programMap.get("objectId");
    boolean bAccess       = false;
    try
    {       
          if(isDPMUser(context,args))
        {
            Boolean isTaskOpen    =  isTaskOpenForCotextUser(context , strObjectId);
            if(isTaskOpen)
            {
                bAccess = true;    
            }
        }
    }catch(Exception e)
    {
        e.printStackTrace();
        throw e;
    }
    return bAccess;
}



/** Access Function on command 
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public boolean isEEAndOpenTaskNot(Context context, String[] args)throws Exception
{
    HashMap programMap    = (HashMap) JPO.unpackArgs(args);
    String strObjectId    = (String) programMap.get("objectId");
    boolean bAccess       = true;
    try
    {       
        if(isDPMUser(context,args))
        {
            if(isTaskOpenForCotextUser(context , strObjectId))
            {
                bAccess = false;    
            }
        } 
    }
    catch(Exception e)
    {
        e.printStackTrace();
        throw e;
    }
    return bAccess;
 }

 public boolean isDPMUser(Context context,String[] args) throws Exception
 {
	 boolean bReturn =false;
	try { 
	 
	       String mqlString = "print person $1 select $2 dump $3";
         	List<String> queryParameterList = new ArrayList<String>();
         	queryParameterList.add(context.getUser());
         	queryParameterList.add("product.derivative.derivative");
         	queryParameterList.add("|");

         	String[] queryParameterArray = new String[queryParameterList.size()];
         	queryParameterList.toArray(queryParameterArray);

         	String productNameList = MqlUtil.mqlCommand(context, true, true, mqlString, true,queryParameterArray);
         	StringList assignProductList = FrameworkUtil.split(productNameList, "|");
        	if( assignProductList.contains(ProgramCentralConstants.PRG_LICENSE_ARRAY[0])){
        		return true;
        	}
    	}catch(Exception e) {
		
		
	}
	return bReturn;
 }


public  Boolean isTaskOpenForCotextUser(Context context, String strMbId )  throws Exception {
    Boolean bReturn = new Boolean(Boolean.FALSE);
    try
    {
        String strVault           = context.getVault().getName();
        StringList strListBusInfo = new StringList(2);
        strListBusInfo.add(DomainConstants.SELECT_ID);
        strListBusInfo.add(DomainConstants.SELECT_CURRENT);
        
        StringList slRelSelect  =   new StringList();
        slRelSelect.add(DomainRelationship.SELECT_ID);
        
        String strWhere     = DomainConstants.EMPTY_STRING;
        String strRelWhere  = DomainConstants.EMPTY_STRING;
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
        
        
        Pattern patternRel  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
        patternRel.addPattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
        patternRel.addPattern(DomainConstants.RELATIONSHIP_PROJECT_TASK);

        Pattern patternType = new Pattern(DomainConstants.TYPE_INBOX_TASK);
        patternType.addPattern(DomainConstants.TYPE_ROUTE);    
        patternType.addPattern(TYPE_WMS_MATERIAL_BILL); 
        
        
        DomainObject domObj = DomainObject.newInstance(context,strMbId);
        String strObjectType= domObj.getInfo(context,DomainConstants.SELECT_TYPE);
        
        if(strObjectType.equals(TYPE_WMS_MATERIAL_BILL))
        {
            strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+DomainConstants.TYPE_INBOX_TASK+"' && current==Assigned )||("+DomainConstants.SELECT_TYPE+"=="+DomainConstants.TYPE_ROUTE+" && current=='In Process'  )||("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_BILL+"&& current==Review && id=="+strMbId+")";
        }
       

        
        MapList mlMBEList                                       = contextPerson.getRelatedObjects(context,
                                                                                //patternRel.getPattern(),  // relationship pattern
                                                                                patternRel.getPattern(),
                                                                                patternType.getPattern(),  // object pattern
                                                                                true,                                                        // to direction
                                                                                true,                                                       // from direction
                                                                                (short)0,                                                      // recursion level
                                                                                strListBusInfo,                                                 // object selects
                                                                                slRelSelect,                                                         // relationship selects
                                                                                strWhere,                                // object where clause
                                                                                strRelWhere,                                // relationship where clause
                                                                                (short)0,                                                      // No expand limit
                                                                                DomainConstants.EMPTY_STRING,                                // postRelPattern
                                                                                TYPE_WMS_MATERIAL_BILL,                                                // postTypePattern
           
                                                                                null);
       

        if(mlMBEList.size()>0)
        {
            bReturn=    Boolean.TRUE;
        }   
    }
    catch(Exception exception)
    {
        exception.printStackTrace();
        throw exception;
    }
    return bReturn;
}
/**
 *  Edit Access Function for column ApproveforAdvance/Reason for No Advance  
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
public StringList isColumnEditable(Context context, String[] args) throws Exception
{
    StringList slReturnList           = new StringList();
    try {
        StringList returnStringList   = new StringList();
        Map programMap                = (Map) JPO.unpackArgs(args);
        MapList objectList            = (MapList) programMap.get("objectList");
        HashMap requestMap            = (HashMap) programMap.get("requestMap");
        int iObjectListSize           = objectList.size();
        Map dataMap                   = null;
        String isEditValue            = DomainConstants.EMPTY_STRING;

        for (int i = 0; i < iObjectListSize; i++) {
            dataMap       = (Map) objectList.get(i);
            isEditValue   = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
            if("Yes".equalsIgnoreCase(isEditValue))
            {
                slReturnList.add("true");
            }
            else
            {
                slReturnList.add("false");
            }
        }
    }
    catch (Exception e) {
        throw e;
    }
    return slReturnList;
}


@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getSecuredAdvanceStock(Context context, String [] args) throws Exception 
{
   MapList mlReturnList        = new MapList();
   try
   {    
       HashMap programMap      = (HashMap)JPO.unpackArgs(args);
       String strobjectId      = (String)programMap.get("objectId");

       if(null != strobjectId && !"".equals(strobjectId) && !"null".equals(strobjectId))
       {

       StringList slBusSelect  = new StringList(2);
       slBusSelect.add(DomainConstants.SELECT_ID);

       StringList slRelSelect  =     new StringList();
       slRelSelect.add(DomainRelationship.SELECT_ID);

       DomainObject domABS     = DomainObject.newInstance(context , strobjectId );
       mlReturnList            = domABS.getRelatedObjects(context, // matrix context
    		   RELATIONSHIP_WMS_ABS_STOCK, // relationship pattern
                                                            TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                            slBusSelect, // object selects
                                                            slRelSelect, // relationship selects
                                                            false, // to direction
                                                            true, // from direction
                                                            (short) 1, // recursion level
                                                            DomainConstants.EMPTY_STRING, // object where clause
                                                            DomainConstants.EMPTY_STRING, // relationship where clause
                                                            0);
       }
   }
   catch(Exception e)
   {
       e.printStackTrace();
       throw e;
   }
   return mlReturnList;
}

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList  getSecuredRecoveryStock(Context context, String [] args) throws Exception 
{
   MapList mlReturnList = new MapList();
   try
   {    
       HashMap programMap      = (HashMap)JPO.unpackArgs(args);
       String strobjectId      = (String)programMap.get("objectId");

       if(null != strobjectId && !"".equals(strobjectId) && !"null".equals(strobjectId))
       {
           StringList slBusSelect  = new StringList(2);
           slBusSelect.add(DomainConstants.SELECT_ID);
           slBusSelect.add("attribute["+ATTRIBUTE_WMS_REDUCED_RATE_FOR_ADVANCE+"]");
           slBusSelect.add("to["+RELATIONSHIP_WMS_ABS_STOCK+"|from.id=="+strobjectId+"]");
           slBusSelect.add("to["+RELATIONSHIP_WMS_ABS_STOCK+"|from.id=="+strobjectId+"].attribute["+ATTRIBUTE_WMS_SECURED_ADVANCE_TEMP+"]");
      
           
           StringList slRelSelect  =     new StringList();
           slRelSelect.add(DomainRelationship.SELECT_ID);
           slRelSelect.add("attribute["+ATTRIBUTE_WMS_RECOVERY_AMOUNT+"]");
           slRelSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");

  
           DomainObject domABS     = DomainObject.newInstance(context , strobjectId );
           Pattern patternType             = new Pattern(TYPE_WMS_MATERIAL_CONSUMPTION);
           patternType.addPattern(TYPE_WMS_STOCK_ENTRIES);
           
           Pattern patternRel          = new Pattern(RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB);
           patternRel.addPattern(RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK);

           //String strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_MATERIAL_CONSUMPTION+"') || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_STOCK_ENTRIES+"&& current=="+STATE_STOCK_ENTRIES_STOCKED+")";
           String strWhere        = DomainConstants.EMPTY_STRING;
           
           mlReturnList           = domABS.getRelatedObjects(context, // matrix context
                                                               patternRel.getPattern(),               // relationship pattern
                                                               patternType.getPattern(),                     // object pattern
                                                               true,                                            // to direction
                                                               true,                                            // from direction
                                                               (short)2,                                     // recursion level
                                                               slBusSelect,                          // object selects
                                                               slRelSelect,                             // relationship selects
                                                               strWhere,                                // object where clause
                                                               DomainConstants.EMPTY_STRING,             // relationship where clause
                                                               (short)0,                                  // No expand limit
                                                               DomainConstants.EMPTY_STRING,             // postRelPattern
                                                               TYPE_WMS_STOCK_ENTRIES,                            // postTypePattern
                                                               null);
           
           Map dataMap                     = null;
           Map<String,Double> keyValueMap  = new HashMap<String , Double>();
           Map<String,Double> keyQutMap    = new HashMap<String , Double>();
           Map<String,Map> keyDataMap      = new HashMap<String , Map>();
           String strObjectId              = DomainConstants.EMPTY_STRING;
           String strRecoveryValue         = DomainConstants.EMPTY_STRING;
           String strQut                   = DomainConstants.EMPTY_STRING;
           String strAdvanceQunty          = DomainConstants.EMPTY_STRING;
           String strCurrentBillAdv        = DomainConstants.EMPTY_STRING;
           
           HashMap<String,Double> mapAdvValue   = new HashMap<String,Double>();
           
           Double dCurrentBillAdv =   0.0;

           for (int i = 0; i < mlReturnList.size() ; i++) {
               dataMap              = (Map)mlReturnList.get(i);
               strObjectId          = (String)dataMap.get(DomainConstants.SELECT_ID) ;
               strRecoveryValue     = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_RECOVERY_AMOUNT+"]"); 
               strAdvanceQunty      = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_REDUCED_RATE_FOR_ADVANCE+"]");
               strQut               = (String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]"); 
               strCurrentBillAdv    = (String)dataMap.get("to["+RELATIONSHIP_WMS_ABS_STOCK+"].attribute["+ATTRIBUTE_WMS_SECURED_ADVANCE_TEMP+"]"); 
               
               dCurrentBillAdv      =   0.0;
               if(strCurrentBillAdv!=null && !"".equals(strCurrentBillAdv) &&  !"null".equals(strCurrentBillAdv))
               {
                   dCurrentBillAdv  =   Double.parseDouble(strCurrentBillAdv);
               }
               
                    
               Double dAdvanceQunty = Double.parseDouble(strAdvanceQunty);

               if(dAdvanceQunty>0 || dCurrentBillAdv>0)
               {
                   
                   if(dAdvanceQunty>0)
                   {
                       mapAdvValue.put(strObjectId, dAdvanceQunty);
                   }
                   else if(dCurrentBillAdv>0)
                   {
                       mapAdvValue.put(strObjectId, dCurrentBillAdv);
                   }

                   if(strRecoveryValue!=null && !"".equals(strRecoveryValue) && !"null".equals(strRecoveryValue))
                   {
                       Double bRecoberyValue  =   Double.parseDouble(strRecoveryValue);
                       if(keyValueMap.containsKey(strObjectId))
                       {
                           Double bPreRecovetForObjectValue   =   keyValueMap.get(strObjectId);
                           bPreRecovetForObjectValue          =   bPreRecovetForObjectValue+bRecoberyValue;
                           keyValueMap.put(strObjectId, bPreRecovetForObjectValue);
                       }
                       else
                       {
                           keyValueMap.put(strObjectId,bRecoberyValue);
                       }
                   }
                   
                   if(strQut!=null && !"".equals(strQut) && !"null".equals(strQut))
                   {
                       Double bQutValue  =   Double.parseDouble(strQut);
                       if(keyQutMap.containsKey(strObjectId))
                       {
                           Double bPreQut   =   keyQutMap.get(strObjectId);
                           bPreQut          =   bPreQut+bQutValue;
                           keyQutMap.put(strObjectId, bPreQut);
                       }
                       else
                       {
                           keyQutMap.put(strObjectId,bQutValue);
                       }
                   }
                   
                   if(!keyDataMap.containsKey(strObjectId))
                   {
                       keyDataMap.put(strObjectId, dataMap);
                   }
               }
           }

           //AllocatedQuntity
           Double dTotalRecovetValue;
           Double dQut;
           Double dTotalAdvUpTotal  = 0.0;
           Double dPendingAdv       = 0.0;
           MapList mlFinalList      = new MapList();
           for (Map.Entry<String, Map> entry : keyDataMap.entrySet()) {
               dTotalRecovetValue   = keyValueMap.get(entry.getKey());
               dQut                 = keyQutMap.get(entry.getKey());
               dTotalAdvUpTotal     = mapAdvValue.get(entry.getKey());
               dPendingAdv          = dTotalAdvUpTotal - dTotalRecovetValue;
               Map fullMap          = entry.getValue();
               fullMap.put("Recovery"         , ""+dTotalRecovetValue);
               fullMap.put("PendingAdvance"   , ""+dPendingAdv);
               fullMap.put("AllocatedQuntity" , ""+dQut);
               if(dTotalRecovetValue>0)
               {
                   mlFinalList.add(fullMap);
               }
           }
          mlReturnList.clear();
          mlReturnList.addAll(mlFinalList);  
       }
   }
   catch(Exception e)
   {
       e.printStackTrace();
       throw e;
   }
   return mlReturnList;
}


/**
 * Method to get the connected Material OBjec under the Work Order 
 *
 * @author CHiPS
 * @since 418
 */
@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
public StringList getMaterialBillStock (Context context, String[] args) throws Exception 
{
    StringList strListMaterialbillOID       = new StringList();
    try
    {
        String strObjectId                  = WMSUtil_mxJPO.getContextObjectOIDFromArgs(args);
        if(null != strObjectId && !"".equals(strObjectId) && !"null".equals(strObjectId))
        {
            DomainObject domObjAbstractMBE  = DomainObject.newInstance(context, strObjectId);
            String strWorkOrderOID          = domObjAbstractMBE.getInfo(context, "to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
                            
            if(null != strWorkOrderOID && !"".equals(strWorkOrderOID) && !"null".equals(strWorkOrderOID))
            {
                StringList strBusSelect     =     new StringList();
                strBusSelect.add(DomainConstants.SELECT_ID);
                strBusSelect.add(DomainConstants.SELECT_TYPE);
                strBusSelect.add(DomainConstants.SELECT_CURRENT);
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");

                StringList strListRelSelects = new StringList(1);
                strListRelSelects.add(DomainRelationship.SELECT_ID);
                DomainObject domWorkOrder    = DomainObject.newInstance(context, strWorkOrderOID);
                
                Pattern patternType          = new Pattern(TYPE_WMS_MATERIAL_BILL);
                patternType.addPattern(TYPE_WMS_STOCK_ENTRIES);
                
                Pattern patternRel           = new Pattern(RELATIONSHIP_WMS_WO_MATERIAL_BILL);
                patternRel.addPattern(RELATIONSHIP_WMS_MATERIALBILL_STOCK);

                String strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_MATERIAL_BILL+"') || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_STOCK_ENTRIES+"&& current=="+STATE_STOCK_ENTRIES_STOCKED+" && to["+RELATIONSHIP_WMS_ABS_STOCK+"]==False && attribute["+ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE+"]==Yes)";

                MapList mlWOStockList = domWorkOrder.getRelatedObjects(context, // matrix context
                                                                        patternRel.getPattern(),               // relationship pattern
                                                                        patternType.getPattern(),                     // object pattern
                                                                        false,                                            // to direction
                                                                        true,                                            // from direction
                                                                        (short)2,                                     // recursion level
                                                                        strBusSelect,                          // object selects
                                                                        strListRelSelects,                             // relationship selects
                                                                        strWhere,                                // object where clause
                                                                        DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                        (short)0,                                  // No expand limit
                                                                        DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                        TYPE_WMS_STOCK_ENTRIES,                            // postTypePattern
                                                                        null);                                   // postPatterns
                
                Map dataMap                  = null;
                Object woOrderList           = null;
                StringList slWorkOrderIDs    = new StringList ();
                int iSize                    = mlWOStockList.size();
                String strAvlQut             = DomainConstants.EMPTY_STRING;
                String strCurrent            = DomainConstants.EMPTY_STRING;
                String strId                 = DomainConstants.EMPTY_STRING;


                for (int i=0;i< iSize; i++) {
                    dataMap      =(Map) mlWOStockList.get(i);
                    strAvlQut    =(String)dataMap.get("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");
                    strId        =(String)dataMap.get(DomainConstants.SELECT_ID);
                    strCurrent   =(String)dataMap.get(DomainConstants.SELECT_CURRENT);
                    strListMaterialbillOID.add(strId);
                }
               // strListMaterialbillOID     = ${CLASS:CHiPSUtil}.convertToStringList(mpWOStockList, DomainObject.SELECT_ID);
            }
        }    
    }
    catch(Exception exception)
    {
        exception.printStackTrace();
        throw exception;
    }
    return strListMaterialbillOID;
}

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeABSConnectedStocks(Context context , String [] args) throws Exception
{
    StringList slReturnList = new StringList();
    try {
            HashMap programMap         = (HashMap)JPO.unpackArgs(args);
            String strObjectId         = (String)programMap.get("objectId");
            if(null != strObjectId && !"".equals(strObjectId) && !"null".equals(strObjectId))
            {
                StringList slBusSelect      = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                DomainObject domMAterials   = DomainObject.newInstance(context, strObjectId);
                MapList mlSorList           = domMAterials.getRelatedObjects(context, // matrix context
                                                            RELATIONSHIP_WMS_ABS_STOCK, // relationship pattern
                                                            TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                            slBusSelect, // object selects
                                                            null, // relationship selects
                                                            false, // to direction
                                                            true, // from direction
                                                            (short) 1, // recursion level
                                                            DomainConstants.EMPTY_STRING, // object where clause
                                                            DomainConstants.EMPTY_STRING, // relationship where clause
                                                            0);
                slReturnList                 = WMSUtil_mxJPO.convertToStringList(mlSorList, DomainObject.SELECT_ID);
            }
        }
     catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
    return slReturnList;
}
    
public Vector getTotalRecoveryValue(Context context , String [] args)
{ 
    Vector vReturnList    =   new Vector();
    try {
        Map<String,Object> programMap = (Map<String,Object>)JPO.unpackArgs(args);
        HashMap hmColumnMap           = (HashMap)programMap.get("columnMap");
        String strColumnName          = (String) hmColumnMap.get("name");
        MapList objectList = (MapList)programMap.get("objectList");
        int intSize = objectList.size();
        Iterator<Map<String,String>> iterator  = objectList.iterator();
        Map<String,String> mapData;
        while(iterator.hasNext())
        {
            mapData = iterator.next();
            String strAbsMBEQuantity = mapData.get(strColumnName);
            vReturnList.add(strAbsMBEQuantity);
        }
      } catch (Exception e) {
         e.printStackTrace();
      }
    return vReturnList;
}

public Vector getTotalAdvanceValueInRecoveryTable(Context context , String [] args)
{ 
    Vector vReturnList                = new Vector();
    try {
        Map<String,Object> programMap = (Map<String,Object>)JPO.unpackArgs(args);
        HashMap hmColumnMap           = (HashMap)programMap.get("columnMap");
        String strColumnName          = (String) hmColumnMap.get("name");
        MapList objectList            = (MapList)programMap.get("objectList");
        int intSize                   = objectList.size();
        Iterator<Map<String,String>> iterator  = objectList.iterator();
        Map<String,String> mapData;
        String strTotalAdvnce         = "";
        String strCurrentBillAdv      = "";
        Double dCurrentBillAdv        = 0.0;
        Double dTotalAvd              = 0.0;
        while(iterator.hasNext())
        {
            mapData             = iterator.next();
            strTotalAdvnce      = (String)mapData.get("attribute["+ATTRIBUTE_WMS_REDUCED_RATE_FOR_ADVANCE+"]");
            strCurrentBillAdv   = (String)mapData.get("to["+RELATIONSHIP_WMS_ABS_STOCK+"].attribute["+ATTRIBUTE_WMS_SECURED_ADVANCE_TEMP+"]");  
            dCurrentBillAdv     = 0.0; 
            dTotalAvd           = 0.0;
            if(strCurrentBillAdv!=null && !"".equals(strCurrentBillAdv) &&  !"null".equals(strCurrentBillAdv))
            {
                dCurrentBillAdv =   Double.parseDouble(strCurrentBillAdv);
            }
            dTotalAvd=Double.parseDouble(strTotalAdvnce);
            Double dFinalTotal   =  dTotalAvd + dCurrentBillAdv;
            if(dCurrentBillAdv!=0)
            {
                vReturnList.add(dCurrentBillAdv.toString());  
            }
            else
            {
                vReturnList.add(dTotalAvd.toString());   
            }
        }
      } catch (Exception e) {
         e.printStackTrace();
      }
    return vReturnList;
}


public boolean checkOwnerAndCreateState(Context context, String[] args) throws Exception 
{
    boolean bReturnValue = false;
    try
    {
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        String sABSObjectId    = (String) programMap.get("ABSid");
        
        
        if(sABSObjectId==null || "".equals(sABSObjectId))
        {
            return true;
        }
        
        DomainObject obj       = (DomainObject) DomainObject.newInstance(context);
        obj.setId(sABSObjectId);

        
        String strObjOwner     = null;
        strObjOwner            = obj.getOwner(context).getName();
        String strOwner        = context.getUser();
        

        StringList strListSelects = new StringList(2);
        strListSelects.add(DomainConstants.SELECT_CURRENT);
        strListSelects.add("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
       
        Map<String,String> mapAbsMBEInfo     = obj.getInfo(context, strListSelects);
        String strWorkOrderID                = mapAbsMBEInfo.get("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
        String strAbsMBEState                = mapAbsMBEInfo.get(DomainConstants.SELECT_CURRENT);
        
        if(strOwner.equals(strObjOwner) && ("Create".equals(strAbsMBEState))){
            bReturnValue= true;
        } 
    }
    catch(Exception exception)
    {
        exception.printStackTrace();
        throw exception;
    }
    return bReturnValue; 
} 

public HashMap updateSecuredAdvanceRate(Context context, String[] args)throws Exception
{
    HashMap mapReturnMap  = null;
    try
    {       
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        String strAMBId             = (String)programMap.get("objectId");
        String strStockId           = (String)programMap.get("strStockId");
        String strRelID                = (String)programMap.get("relID");
        DomainObject domStock       = DomainObject.newInstance(context , strStockId);
        String strStockAmount       = domStock.getAttributeValue(context, ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT);
        Double bStockAmount         = Double.parseDouble(strStockAmount);
        
        String strMaterialid        = domStock.getInfo(context, "from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
        String strWordOrderID       = domStock.getInfo(context, "to["+RELATIONSHIP_WMS_MATERIALBILL_STOCK+"].from.to["+RELATIONSHIP_WMS_WO_MATERIAL_BILL+"].from.id");
          
        String strWhere             = "id=="+strMaterialid;
        StringList slBusSelect      =  new StringList();
        slBusSelect.add(DomainConstants.SELECT_ID);
        StringList slRelSelect      =  new StringList();
        slRelSelect.add(DomainRelationship.SELECT_ID);
        slRelSelect.add("attribute["+ATTRIBUTE_WMS_ADVANCE_RATE+"]");
        
        
        DomainObject domWO          = DomainObject.newInstance(context, strWordOrderID);
        MapList mlMaterialList      = domWO.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                                TYPE_WMS_MATERIAL, // type pattern
                                                                slBusSelect, // object selects
                                                                slRelSelect, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                strWhere, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        
        String strMaterialAdvRate     = DomainConstants.EMPTY_STRING;
        if(mlMaterialList.size()>0)
        {
            strMaterialAdvRate        = (String)((Map) mlMaterialList.get(0)).get("attribute["+ATTRIBUTE_WMS_ADVANCE_RATE+"]");
        }

        if( !DomainConstants.EMPTY_STRING.equals(strMaterialAdvRate) )
        {
            Double dMaterialAdvRate   =  Double.parseDouble(strMaterialAdvRate);
            Double dFinalValue        =  ( dMaterialAdvRate * bStockAmount ) / 100;
            //domStock.setAttributeValue(context, ATTR_REDUCED_RATE_FOR_ADVANCE, dFinalValue.toString());
            DomainRelationship.setAttributeValue(context, strRelID , ATTRIBUTE_WMS_SECURED_ADVANCE_TEMP , dFinalValue.toString());
        }
    }
    catch(Exception e)
    {
        e.printStackTrace();
        throw e;
    }
    return mapReturnMap;
 }  

/**
 * Column function for material link  Called from table WMSAbsParticular 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public  Vector getMaterialConsumptionIcon(Context context , String [] args ) throws Exception
{
 Vector vReturn = new Vector();
 try {
         Map<String,Object> programMap    =   (Map<String,Object>)JPO.unpackArgs(args);
         Map paramList            = (Map) programMap.get("paramList");
         String strABSObjectId    = (String) paramList.get("objectId");
         DomainObject domABS      =  DomainObject.newInstance(context , strABSObjectId );
         String strWorkOrderId    =  domABS.getInfo(context, "to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
         DomainObject domWo       = DomainObject.newInstance(context , strWorkOrderId);

         
         StringList slBusSelect  = new StringList();
         slBusSelect.add(DomainConstants.SELECT_ID);
         slBusSelect.add(DomainConstants.SELECT_NAME);

         StringList slRelSelect  = new StringList();
         slRelSelect.add(DomainRelationship.SELECT_ID); 
         
         
         MapList mlMaterialList  = domWo.getRelatedObjects(context, // matrix context
                                                         RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                         TYPE_WMS_MATERIAL, // type pattern
                                                         slBusSelect, // object selects
                                                         slRelSelect, // relationship selects
                                                         false, // to direction
                                                         true, // from direction
                                                         (short) 1, // recursion level
                                                         DomainConstants.EMPTY_STRING, // object where clause
                                                         DomainConstants.EMPTY_STRING, // relationship where clause
                                                         0);

         StringList slMaterialList = WMSUtil_mxJPO.convertToStringList(mlMaterialList, DomainObject.SELECT_ID);

         MapList objectList        = (MapList)programMap.get("objectList");
         int intSize               = objectList.size();
         Map dataMap               = null;
         String strValue           = DomainObject.EMPTY_STRING; 
         String strItemId          = DomainConstants.EMPTY_STRING;
         String strMaterialId      = DomainConstants.EMPTY_STRING;
         StringList slSORMaterialList     =  new StringList(); 

         for (int i = 0; i < intSize; i++) {
             dataMap      = (Map)objectList.get(i);

             if(dataMap.get("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].from.id") instanceof StringList)
             {
                 slSORMaterialList    =   (StringList)dataMap.get("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].from.id");
             }
             else if(dataMap.get("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].from.id") !=null)
             {
                 slSORMaterialList    =    new StringList ((String)dataMap.get("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].from.id"));

             }
            boolean isAddImage   =   false;
            for (int j = 0; j < slSORMaterialList.size(); j++) {
                   strMaterialId = (String)slSORMaterialList.get(j);
                  if(slMaterialList.contains(strMaterialId))
                  {
                      isAddImage=true; 
                  }
            }
            strItemId    = (String) dataMap.get(DomainConstants.SELECT_ID);
            if(isAddImage)
            {
                vReturn.add("<img border=\"0\" style=\"cursor:pointer\" src=\"../common/images/I_Move_32.png\" title=\"Material Consumption\" onclick=\"openMaterialCo('"+strItemId+"' , '"+strWorkOrderId+"' , '"+strABSObjectId+"' )\" ></img>");
            }
            else
            {
                vReturn.add(DomainConstants.EMPTY_STRING);
            }
         }
  } catch (Exception e) {
     e.printStackTrace();
     throw e;
  }
 return vReturn;
}

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getMaterialConsumption (Context context , String [] args ) throws Exception
{
    MapList mlReturnList = new MapList();
    try {
        Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
        String strObjectID               = (String) programMap.get("objectId");
        String strABSId               = (String) programMap.get("ABSid");
        
        
        StringList slBusSelect           = new StringList();
        slBusSelect.add(DomainConstants.SELECT_ID);
        slBusSelect.add(DomainConstants.SELECT_NAME);
        //add here
        slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_PENDING_QTY+"].value");
        
        String strWhere  =    "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id=="+strABSId;

        DomainObject domBOQ              = DomainObject.newInstance(context , strObjectID);
        mlReturnList                     = domBOQ.getRelatedObjects(context, // matrix context
        		                                                    RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION, // relationship pattern
                                                                    TYPE_WMS_MATERIAL_CONSUMPTION, // type pattern
                                                                    slBusSelect, // object selects
                                                                    null, // relationship selects
                                                                    false, // to direction
                                                                    true, // from direction
                                                                    (short) 1, // recursion level
                                                                    strWhere, // object where clause
                                                                    DomainConstants.EMPTY_STRING, // relationship where clause
                                                                    0);
        } catch (Exception e) {
        e.printStackTrace();
     }
    return mlReturnList;
}


@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeMaterialConsumption (Context context , String [] args)
{
    StringList slReturnList = new StringList();
    try {
         HashMap programMap         = (HashMap)JPO.unpackArgs(args);
         String strObjectId         = (String) programMap.get("objectId");
         if(strObjectId !=null && !"".equals(strObjectId) && !"null".equals(strObjectId))
         {
            StringList slBusSelect   =     new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            DomainObject domWO       = DomainObject.newInstance(context, strObjectId);
            MapList mlSorList        = domWO.getRelatedObjects(context, // matrix context
                                                        RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                        TYPE_WMS_MATERIAL, // type pattern
                                                        slBusSelect, // object selects
                                                        null, // relationship selects
                                                        false, // to direction
                                                        true, // from direction
                                                        (short) 1, // recursion level
                                                        DomainConstants.EMPTY_STRING, // object where clause
                                                        DomainConstants.EMPTY_STRING, // relationship where clause
                                                        0);
            slReturnList = WMSUtil_mxJPO.convertToStringList(mlSorList, DomainObject.SELECT_ID);
         }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return slReturnList;
}


@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
public StringList getMateriaforMaterialConsumption (Context context , String [] args)
{
    StringList slReturnList = new StringList();
    try {
         HashMap programMap         = (HashMap)JPO.unpackArgs(args);
         String strObjectId         = (String) programMap.get("objectId");
         String strParentId         = (String) programMap.get("parentOID");
         String strABSID            = (String) programMap.get("relId");

         if(strObjectId !=null && !"".equals(strObjectId) && !"null".equals(strObjectId))
         {
            DomainObject domTaskID   = DomainObject.newInstance(context , strObjectId);
            StringList slBusSelect   = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"].from.id");

            Pattern patternType      = new Pattern(TYPE_WMS_SOR);
            patternType.addPattern(TYPE_WMS_MATERIAL);
            Pattern patternRel       = new Pattern(RELATIONSHIP_WMS_TASK_SOR);
            patternRel.addPattern(RELATIONSHIP_WMS_MATERIAL_TO_SOR);

            String strWhere          = DomainConstants.EMPTY_STRING;//"to["+REL_ABS_STOCK+"]==False";
            MapList mlSORMaterial    = domTaskID.getRelatedObjects(context, // matrix context
                                                                    patternRel.getPattern(),               // relationship pattern
                                                                    patternType.getPattern(),                     // object pattern
                                                                    true,                                            // to direction
                                                                    true,                                            // from direction
                                                                    (short)2,                                     // recursion level
                                                                    slBusSelect,                          // object selects
                                                                    null,                             // relationship selects
                                                                    strWhere,                                // object where clause
                                                                    DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                    (short)0,                                  // No expand limit
                                                                    DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                    TYPE_WMS_MATERIAL,                         // postTypePattern
                                                                    null);
            
          

            Pattern patternType1         = new Pattern(TYPE_WMS_MATERIAL_CONSUMPTION);
            patternType1.addPattern(TYPE_WMS_MATERIAL);
            Pattern patternRel1          = new Pattern(RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION);
            patternRel1.addPattern(RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL);

            
            strWhere = "("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_CONSUMPTION+" && from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id=="+strABSID+")|| ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL+")";
            
            
            
            
            MapList mlMCMaterial         = domTaskID.getRelatedObjects(context, // matrix context
                                                                    patternRel1.getPattern(),               // relationship pattern
                                                                    patternType1.getPattern(),                     // object pattern
                                                                    false,                                            // to direction
                                                                    true,                                            // from direction
                                                                    (short)2,                                     // recursion level
                                                                    slBusSelect,                          // object selects
                                                                    null,                             // relationship selects
                                                                    strWhere,                                // object where clause
                                                                    DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                    (short)0,                                  // No expand limit
                                                                    DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                    TYPE_WMS_MATERIAL,                         // postTypePattern
                                                                    null);
            
            
            StringList slMCMaterial      = WMSUtil_mxJPO.convertToStringList(mlMCMaterial, DomainObject.SELECT_ID);
            MapList mlFinalList          = new MapList();
            int iSize                    = mlSORMaterial.size(); 
            Map dataMap                  = null;
            Object woOrderList           = null;
            StringList slWorkOrderIDs    = new StringList();
            for (int i=0;i< iSize; i++) {
                dataMap = (Map) mlSORMaterial.get(i);
                woOrderList  =  dataMap.get("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"].from.id")  ;
                if(woOrderList!=null && woOrderList instanceof StringList )
                {
                    slWorkOrderIDs  =  (StringList) dataMap.get("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"].from.id")  ; 
                }
                else if(woOrderList!=null && woOrderList instanceof String)
                {
                    slWorkOrderIDs  =  new StringList((String) dataMap.get("to["+RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE+"].from.id")) ; 
                }
                else 
                {
                    slWorkOrderIDs  = new StringList ();
                }
                if(slWorkOrderIDs.contains(strParentId))
                {
                    mlFinalList.add(dataMap);
                }
            }
            mlSORMaterial.clear();
            mlSORMaterial.addAll(mlFinalList);
            
          
            slReturnList = WMSUtil_mxJPO.convertToStringList(mlSORMaterial, DomainObject.SELECT_ID);
            //Exclude Material Con Connected Object 
            slReturnList.removeAll(slMCMaterial);
         }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return slReturnList;
}


public Map createMaterialConsumption(Context context , String [] args ) throws Exception
{
    Map returnMap = new HashMap();
    try {
            HashMap programMap         = (HashMap)JPO.unpackArgs(args);
            String strObjectId         = (String) programMap.get("objectId"); 
            String strMaterialId       = (String) programMap.get("MaterialId");
            DomainObject domBOQ        = DomainObject.newInstance(context , strObjectId);
            String strAMBID            = (String) programMap.get("ABSid");            
            DomainObject doNewObject   = DomainObject.newInstance(context);             
            doNewObject.createObject(context,
                                     TYPE_WMS_MATERIAL_CONSUMPTION ,
                                     doNewObject.getUniqueName("CONSUMPTION_"),
                                      "1",
                                      POLICY_WMS_MATERIAL_CONSUMPTION,
                                     context.getVault().getName());

             String strNewObjectId     = doNewObject.getId();          
             DomainRelationship.connect(context, strObjectId     , RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION  , strNewObjectId ,true); 
             DomainRelationship.connect(context, strNewObjectId  ,RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB , strAMBID ,true);   
             DomainRelationship.connect(context,strNewObjectId  , RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL , strMaterialId ,true);  
             //set conversion factor and auto-calculate required qty
             DomainObject domMT=DomainObject.newInstance(context, strObjectId);
             StringList slBusSelect = new StringList(2);
             slBusSelect.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
             slBusSelect.add("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id");
             Map mInfo= domMT.getInfo(context, slBusSelect);
             String strSORID =  (String) mInfo.get("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.id");
             double dMBQQty=Double.parseDouble((String) mInfo.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]"));
             DomainObject domSOR= DomainObject.newInstance(context, strSORID);
             String stSelect = "to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"|.from.id=="+strMaterialId+"].attribute["+ATTRIBUTE_WMS_CONVERSION_RATE+"]";
             Map m  = new HashMap();
             StringList slSel=new StringList();
             slSel.add(stSelect);
             Map mResult =   domSOR.getInfo(context, slSel);
             double dConFactr =  Double.valueOf((String)mResult.get("to["+RELATIONSHIP_WMS_MATERIAL_TO_SOR+"].attribute["+ATTRIBUTE_WMS_CONVERSION_RATE+"]"));
             Map mAttribute=new HashMap();
             double dCalQty= Math.round(dConFactr*dMBQQty)/100.0;
             mAttribute.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, String.valueOf(dCalQty));
             mAttribute.put(ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY, String.valueOf(dCalQty));
             mAttribute.put(ATTRIBUTE_WMS_CONVERSION_FACTOR, String.valueOf(dConFactr));
             doNewObject.setAttributeValues(context, mAttribute);
         
             
             
     } catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
   return returnMap;
}

public StringList showColorRequiredQty(Context context, String[] args)  throws Exception 
{
    StringList slOutput   = new StringList();
    try 
    {
        // Get object list information from packed arguments
        HashMap programMap    = (HashMap) JPO.unpackArgs(args);
        MapList objectList    = (MapList) programMap.get("objectList");
        String sItemQty       = DomainConstants.EMPTY_STRING;
        Map<String,String> mapObjectInfo;
        for (Iterator<Map<String,String>> itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
        {
            mapObjectInfo     = itrTableRows.next();
             sItemQty   = (String)mapObjectInfo.get("attribute["+ATTRIBUTE_WMS_STOCK_PENDING_QTY+"].value");
           //if 0 show green if not show red
          
          
           if("0".equals(sItemQty) || "0.0".equals(sItemQty)) {
               slOutput.addElement("BudgetGreenBackGroundColor");    
            }
            else {
                slOutput.addElement("BudgetRedBackGroundColorAndBold");
            }
        }

    } catch (Exception exp) {
        exp.printStackTrace();
        throw exp;
    }
    return slOutput;

} 

public HashMap updateAvlQut(Context context, String[] args) throws Exception
{
    HashMap returnMap    = new HashMap();
    int iReturnValue =   0;
    try{
        
            HashMap programMap       = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap         = (HashMap) programMap.get("paramMap");
            String strMCId           = (String) paramMap.get("objectId");
            String newValue          = (String) paramMap.get("New Value");
            DomainObject domMC       = DomainObject.newInstance(context , strMCId );
            HashMap mapAttributeMap  = new HashMap();
            mapAttributeMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, newValue);
            mapAttributeMap.put(ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY, newValue);
            
            
            StringList slBusSelect           = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);
 
            StringList slRelSelect           = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
 
            MapList mlMCStockList            = domMC.getRelatedObjects(context, // matrix context
            		RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                slRelSelect, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
            int iSize        = mlMCStockList.size();
            Map dataMap      = null;
            String strRelID  = DomainConstants.EMPTY_STRING;
            String [] relIDs = new String [iSize];
            for (int i = 0; i < iSize ; i++) {
                 dataMap      = (Map) mlMCStockList.get(i);
                 strRelID     = (String) dataMap.get(DomainRelationship.SELECT_ID);
                 relIDs [i]   = strRelID;
        }
        domMC.setAttributeValues(context, mapAttributeMap);
        DomainRelationship.disconnect(context, relIDs); 
    } catch (FrameworkException e) {
        e.printStackTrace();
        throw e;
    } finally {
    }
    returnMap.put("Action", "refresh");
    returnMap.put("Message","Done");  
    
    returnMap.put ("Action", "execScript");
    returnMap.put("Message", "{ main:function() {refreshRows();arrUndoRows = new Object();postDataXML.loadXML(\"<mxRoot/>\");expandAll();}}");
    
    //return iReturnValue;  
    return returnMap;
}


public Vector getMaterialConsumptionStockIconFormF(Context context , String [] args ) throws Exception
{
    Vector vReturn = new Vector();
    try {
            Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
            Map paramList                    = (Map) programMap.get("paramList");          
            MapList objectList               = (MapList)programMap.get("objectList");    
            
            String strABSID                  = (String)paramList.get("objectId");
            if(strABSID==null || "".equals(strABSID) || "null".equals(strABSID))
            {
                strABSID = (String)paramList.get("objectId");
            }
            int intSize                      = objectList.size();
            Map dataMap                      = null;
            String strValue                  = DomainObject.EMPTY_STRING; 
            String strItemId                 = DomainConstants.EMPTY_STRING;
            String strMaterialId             = DomainConstants.EMPTY_STRING;
            StringList slSORMaterialList     = new StringList(); 
            String strMCId                   = DomainConstants.EMPTY_STRING;
            String strLavel                  = DomainConstants.EMPTY_STRING;
            for (int i = 0; i < intSize; i++) {
                dataMap  = (Map)objectList.get(i);
                strMCId  = (String) dataMap.get(DomainConstants.SELECT_ID);
                strLavel  = (String) dataMap.get("id[level]");
                vReturn.add("<img border=\"0\" style=\"cursor:pointer\" src=\"../common/images/CATFmtHexMesher_32x32.png\" title=\"Stock Consumption\" onclick=\"openMaterialConsumptionStockFormF('"+strMCId+"' , '"+strABSID+"' , '"+strLavel+"' )\" ></img>");
            }
     } catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
    return vReturn;
}

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getMCStockForFomF (Context context , String [] args ) throws Exception
{
    MapList mlReturnList = new MapList();
    try {
            Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
            String strObjectID               = (String) programMap.get("objectId");
            StringList slBusSelect           = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);

            StringList slRelSelect           = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);

            DomainObject domMC               = DomainObject.newInstance(context , strObjectID);
            mlReturnList                     = domMC.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                slRelSelect, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        
       } catch (Exception e) {
        e.printStackTrace();
     }
    return mlReturnList;
}

public boolean checkMCAddStockAccessCheckFormF(Context context, String[] args) throws Exception 
{
    boolean bReturnValue = false;
    try
    {
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        String sABSObjectId    = (String) programMap.get("ABSid");
        String strObjOwner     = null;
        DomainObject obj       = (DomainObject) DomainObject.newInstance(context);
        obj.setId(sABSObjectId);
        strObjOwner            = obj.getOwner(context).getName();
        String strOwner        = context.getUser();
        StringList strListSelects = new StringList(2);
        strListSelects.add(DomainConstants.SELECT_CURRENT);
        strListSelects.add("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
        Map<String,String> mapAbsMBEInfo     = obj.getInfo(context, strListSelects);
        String strWorkOrderID                =  mapAbsMBEInfo.get("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
        String strAbsMBEState                = mapAbsMBEInfo.get(DomainConstants.SELECT_CURRENT);
        if(strOwner.equals(strObjOwner) && ("Create".equals(strAbsMBEState))){
            bReturnValue= true;
        }        
    }
    catch(Exception exception)
    {
        exception.printStackTrace();
        throw exception;
    }
    return bReturnValue; 
}

@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
public StringList getWOStockFormF (Context context , String [] args ) throws Exception
{
    StringList slReturnList = new StringList();
    try {
        Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
        String strObjectId               = (String) programMap.get("objectId");
        
        if(null != strObjectId && !"".equals(strObjectId) && !"null".equals(strObjectId))
        {
            DomainObject domObjMC        = DomainObject.newInstance(context, strObjectId);
            String strWorkOrderOID       = domObjMC.getInfo(context, "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
            String strMaterialId         = domObjMC.getInfo(context, "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL+"].to.id");

                            
            if(null != strWorkOrderOID && !"".equals(strWorkOrderOID) && !"null".equals(strWorkOrderOID))
            {
                StringList strBusSelect     =    new StringList();
                strBusSelect.add(DomainConstants.SELECT_ID);
                strBusSelect.add(DomainConstants.SELECT_TYPE);
                strBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");


                StringList strListRelSelects    = new StringList(1);
                strListRelSelects.add(DomainRelationship.SELECT_ID);
                DomainObject domWorkOrder       = DomainObject.newInstance(context, strWorkOrderOID);
                Pattern patternType             = new Pattern(TYPE_WMS_MATERIAL_BILL);
                patternType.addPattern(TYPE_WMS_STOCK_ENTRIES);
                Pattern patternRel              = new Pattern(RELATIONSHIP_WMS_WO_MATERIAL_BILL);
                patternRel.addPattern(RELATIONSHIP_WMS_MATERIALBILL_STOCK);
                
               // String strWhere                = "attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"] >0 && from["+REL_STOCK_MATERIAL+"].to.id=="+strMaterialId;
                String strWhere                  = "("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_BILL+")||(("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_STOCK_ENTRIES+")&&( current=="+STATE_STOCK_ENTRIES_STOCKED+" && attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"] >0 && attribute["+ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE+"]=='Yes' && from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id=="+strMaterialId+") )";
                MapList mpWOStockList            = domWorkOrder.getRelatedObjects(context, // matrix context
                                                                        patternRel.getPattern(), // relationship pattern
                                                                        patternType.getPattern(), // object pattern
                                                                        false,                                            // to direction
                                                                        true,                                            // from direction
                                                                        (short)2,                                     // recursion level
                                                                        strBusSelect,                          // object selects
                                                                        strListRelSelects,                             // relationship selects
                                                                        strWhere,                                // object where clause
                                                                        DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                        (short)0,                                  // No expand limit
                                                                        DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                        TYPE_WMS_STOCK_ENTRIES,                         // postTypePattern
                                                                        null);  

                slReturnList  = WMSUtil_mxJPO.convertToStringList(mpWOStockList, DomainObject.SELECT_ID);
            }
        }
       } catch (Exception e) {
        e.printStackTrace();
     }
    return slReturnList;
}

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeAlteradConnectedStock (Context context , String [] args ) throws Exception
{
    StringList slReturnList = new StringList();
    try {
        Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
        String strObjectID               = (String) programMap.get("objectId");
        StringList slBusSelect           = new StringList();
        slBusSelect.add(DomainConstants.SELECT_ID);
        slBusSelect.add(DomainConstants.SELECT_NAME);
        DomainObject domMC               = DomainObject.newInstance(context , strObjectID);
        MapList mlMCStock                = domMC.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                null, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
       } catch (Exception e) {
        e.printStackTrace();
     }
    return slReturnList;
}


 
public  Vector getMaterialConsumptionStockIcon(Context context , String [] args ) throws Exception
{
    Vector vReturn = new Vector();
    try {
            Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
            Map paramList                    = (Map) programMap.get("paramList");          
            MapList objectList               = (MapList)programMap.get("objectList");             
            String strABSID                  = (String)paramList.get("ABSid");
            if(strABSID==null || "".equals(strABSID) || "null".equals(strABSID))
            {
                strABSID = (String)paramList.get("objectId");
            }
            int intSize                      = objectList.size();
            Map dataMap                      = null;
            String strValue                  = DomainObject.EMPTY_STRING; 
            String strItemId                 = DomainConstants.EMPTY_STRING;
            String strMaterialId             = DomainConstants.EMPTY_STRING;
            StringList slSORMaterialList     = new StringList(); 
            String strMCId                   = DomainConstants.EMPTY_STRING;
            for (int i = 0; i < intSize; i++) {
                dataMap  = (Map)objectList.get(i);
                strMCId  = (String) dataMap.get(DomainConstants.SELECT_ID);
                vReturn.add("<img border=\"0\" style=\"cursor:pointer\" src=\"../common/images/CATFmtHexMesher_32x32.png\" title=\"Stock Consumption\" onclick=\"openMaterialConsumptionStock('"+strMCId+"' , '"+strABSID+"' , '"+""+"' )\" ></img>");
            }
     } catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
    return vReturn;
}

@ProgramCallable
public MapList getMCStock (Context context , String [] args ) throws Exception
{
    MapList mlReturnList = new MapList();
    try {
            Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
            String strObjectID               = (String) programMap.get("objectId");
            StringList slBusSelect           = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);

            StringList slRelSelect           = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);

            DomainObject domMC               = DomainObject.newInstance(context , strObjectID);
            mlReturnList                     = domMC.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
                                                                TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                slBusSelect, // object selects
                                                                slRelSelect, // relationship selects
                                                                false, // to direction
                                                                true, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
        
       } catch (Exception e) {
        e.printStackTrace();
     }
    return mlReturnList;
}

@IncludeOIDProgramCallable
public StringList getWOStock (Context context , String [] args ) throws Exception
{
    StringList slReturnList = new StringList();
    try {
        Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
        String strObjectId               = (String) programMap.get("objectId");
        
        if(null != strObjectId && !"".equals(strObjectId) && !"null".equals(strObjectId))
        {
            DomainObject domObjMC        = DomainObject.newInstance(context, strObjectId);
            String strWorkOrderOID       = domObjMC.getInfo(context, "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
            
            String strMaterialId         = domObjMC.getInfo(context, "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL+"].to.id");   
            if(null != strWorkOrderOID && !"".equals(strWorkOrderOID) && !"null".equals(strWorkOrderOID))
            {
                StringList strBusSelect     =    new StringList();
                strBusSelect.add(DomainConstants.SELECT_ID);
                strBusSelect.add(DomainConstants.SELECT_TYPE);
                strBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_PHYSICAL_STOCK+"]");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]");
                strBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"]");


                StringList strListRelSelects    = new StringList(1);
                strListRelSelects.add(DomainRelationship.SELECT_ID);
                DomainObject domWorkOrder       = DomainObject.newInstance(context, strWorkOrderOID);
                Pattern patternType             = new Pattern(TYPE_WMS_MATERIAL_BILL);
                patternType.addPattern(TYPE_WMS_STOCK_ENTRIES);
                Pattern patternRel              = new Pattern(RELATIONSHIP_WMS_WO_MATERIAL_BILL);
                patternRel.addPattern(RELATIONSHIP_WMS_MATERIALBILL_STOCK);
                
               // String strWhere                = "attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"] >0 && from["+REL_STOCK_MATERIAL+"].to.id=="+strMaterialId;
                String strWhere = "("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_BILL+")||(("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_STOCK_ENTRIES+")&&( current=="+STATE_STOCK_ENTRIES_STOCKED+" && attribute["+ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY+"] >0 && attribute["+ATTRIBUTE_WMS_STOCK_APPROVE_FOR_ADVANCE+"]=='Yes' && from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id=="+strMaterialId+") )";

                MapList mpWOStockList            = domWorkOrder.getRelatedObjects(context, // matrix context
                                                                        patternRel.getPattern(), // relationship pattern
                                                                        patternType.getPattern(), // object pattern
                                                                        false,                                            // to direction
                                                                        true,                                            // from direction
                                                                        (short)2,                                     // recursion level
                                                                        strBusSelect,                          // object selects
                                                                        strListRelSelects,                             // relationship selects
                                                                        strWhere,                                // object where clause
                                                                        DomainConstants.EMPTY_STRING,             // relationship where clause
                                                                        (short)0,                                  // No expand limit
                                                                        DomainConstants.EMPTY_STRING,             // postRelPattern
                                                                        TYPE_WMS_STOCK_ENTRIES,                         // postTypePattern
                                                                        null);  
                
                
                slReturnList  = WMSUtil_mxJPO.convertToStringList(mpWOStockList, DomainObject.SELECT_ID);
            }
        }
       } catch (Exception e) {
        e.printStackTrace();
     }
    return slReturnList;
}


public boolean checkMCAddStockAccessCheck(Context context, String[] args) throws Exception 
{
    boolean bReturnValue = false;
    try
    {  
     
    	
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        String sABSObjectId    = (String) programMap.get("ABSid");
        
        if(sABSObjectId==null || "".equals(sABSObjectId) || "null".equals(sABSObjectId))
        {
            sABSObjectId       = (String) programMap.get("relId");
        }
        
        String strObjOwner     = null;
        DomainObject obj       = (DomainObject) DomainObject.newInstance(context);
        obj.setId(sABSObjectId);
        strObjOwner            = obj.getOwner(context).getName();
        String strOwner        = context.getUser();
        

        StringList strListSelects = new StringList(2);
        strListSelects.add(DomainConstants.SELECT_CURRENT);
        strListSelects.add("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
       
        Map<String,String> mapAbsMBEInfo     = obj.getInfo(context, strListSelects);
        String strWorkOrderID                =  mapAbsMBEInfo.get("to["+RELATIONSHIP_WORKORDER_ABSTRACT_MBE+"].from.id");
        String strAbsMBEState                = mapAbsMBEInfo.get(DomainConstants.SELECT_CURRENT);
        
        if(strOwner.equals(strObjOwner) && ("Create".equals(strAbsMBEState))){
            bReturnValue= true;
        }
                 
    }
    catch(Exception exception)
    {
        exception.printStackTrace();
        throw exception;
    }
    return bReturnValue; 
} 



@ProgramCallable
public MapList getAMBMaterialConsumption (Context context , String [] args ) throws Exception
{
    MapList mlReturnList = new MapList();
    try {
            Map<String,Object> programMap    = (Map<String,Object>)JPO.unpackArgs(args);
            String strObjectID               = (String) programMap.get("objectId");
            StringList slBusSelect           = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);
            slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_PENDING_QTY+"]");
            DomainObject domABM              = DomainObject.newInstance(context , strObjectID);
            mlReturnList                     = domABM.getRelatedObjects(context, // matrix context
                                                                RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB, // relationship pattern
                                                                TYPE_WMS_MATERIAL_CONSUMPTION, // type pattern
                                                                slBusSelect, // object selects
                                                                null, // relationship selects
                                                                true, // to direction
                                                                false, // from direction
                                                                (short) 1, // recursion level
                                                                DomainConstants.EMPTY_STRING, // object where clause
                                                                DomainConstants.EMPTY_STRING, // relationship where clause
                                                                0);
       } catch (Exception e) {
        e.printStackTrace();
     }
    return mlReturnList;
}

/** Trigger on relationship  WMSMaterialConsumptionToStockCreateAction 
 *  
 * @param context
 * @param args
 * @return
 * @throws Exception
 */


public int setMCStockQut(Context context, String[] args) throws Exception
{
    int iReturnValue =   0;
    try{
    
        String strStockId             = args[0];
        String strRelId               = args[1];
        String strMCId                = args[2];
        
        DomainObject domStock         = DomainObject.newInstance(context , strStockId);
        DomainObject domMC            = DomainObject.newInstance(context , strMCId);
        
        Map mapStockAttr              = domStock.getAttributeMap(context);
        Map mapMCAttr                 = domMC.getAttributeMap(context);

        String strMCReqQun            = (String) mapMCAttr.get(ATTRIBUTE_WMS_STOCK_PENDING_QTY); 
        String strMCAVQun             = (String) mapMCAttr.get(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY); 

        String strStockAvl            = (String) mapStockAttr.get(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY); 
        String strStockPending        = (String) mapStockAttr.get(ATTRIBUTE_WMS_STOCK_PENDING_QTY); 
       
        Double dMCReqQun              = Double.parseDouble(strMCReqQun);
        Double dMCAVQun               = Double.parseDouble(strMCAVQun);
        Double dStockAvl              = Double.parseDouble(strStockAvl);
        Double dStockPending          = Double.parseDouble(strStockPending);

       if(dMCReqQun==0.0)
       {
           throw new Exception("Error : ");
       }
       
       Double dFMCPen           = 0.0;
       Double dFMCAV            = 0.0;
       Double dFStockAV         = 0.0;
       Double dFStockPending    = 0.0;
       Double bTekeQunt         = 0.0;

       if(dMCReqQun > dStockAvl)
       {
           bTekeQunt          = dStockAvl;
           dFMCPen            = dMCReqQun -  dStockAvl ;
           dFMCAV             = dMCAVQun  +  dStockAvl ;
           dFStockAV          = dStockAvl  -  dStockAvl ;
           dFStockPending     = dStockPending   +  dStockAvl ;
       }
       else
       {
            bTekeQunt          = dMCReqQun;
            dFMCAV             = dMCAVQun  +  bTekeQunt;
            dFMCPen            = dMCReqQun -  bTekeQunt ;
            dFStockAV          = dStockAvl -  dMCReqQun ;
            dFStockPending     = dStockPending   +  dMCReqQun ;
       }

       HashMap mapStockAttrMap   =    new HashMap();
       mapStockAttrMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY, dFStockAV.toString()); 
       mapStockAttrMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, dFStockPending.toString() ); 

       HashMap mapMCAttrSetMap   =    new HashMap();
       mapMCAttrSetMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY, dFMCAV.toString());
       mapMCAttrSetMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, dFMCPen.toString());

       domStock.setAttributeValues(context, mapStockAttrMap);
       domMC.setAttributeValues(context, mapMCAttrSetMap);
       DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY ,  bTekeQunt.toString());  
       
       //Recovery Code 
       String strAdvanceAmount         = (String)mapStockAttr.get(ATTRIBUTE_WMS_REDUCED_RATE_FOR_ADVANCE);//domStock.getAttributeValue(context, );
       Double dAdvanceAmount           = Double.parseDouble(strAdvanceAmount);
       
       
      // if(dAdvanceAmount>0)
       {
           StringList slStockBusSelect      =  new StringList() ;
           slStockBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
           slStockBusSelect.add("to["+RELATIONSHIP_WMS_MATERIALBILL_STOCK+"].from.to["+RELATIONSHIP_WMS_WO_MATERIAL_BILL+"].from.id");
           
           Map stockDataMap            = domStock.getInfo(context, slStockBusSelect);      
           String strMaterialid        = (String)stockDataMap.get("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
           String strWordOrderID       = (String)stockDataMap.get("to["+RELATIONSHIP_WMS_MATERIALBILL_STOCK+"].from.to["+RELATIONSHIP_WMS_WO_MATERIAL_BILL+"].from.id");

           String strWhere             = "id=="+strMaterialid;
           StringList slBusSelect      =  new StringList();
           slBusSelect.add(DomainConstants.SELECT_ID);
           StringList slRelSelect      =  new StringList();
           slRelSelect.add(DomainRelationship.SELECT_ID);
           slRelSelect.add("attribute["+ATTRIBUTE_WMS_ADVANCE_RATE+"]");
           
           
           DomainObject domWO                = DomainObject.newInstance(context, strWordOrderID);
           MapList mlMaterialList            = domWO.getRelatedObjects(context, // matrix context
                                                                   RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
                                                                   TYPE_WMS_MATERIAL, // type pattern
                                                                   slBusSelect, // object selects
                                                                   slRelSelect, // relationship selects
                                                                   false, // to direction
                                                                   true, // from direction
                                                                   (short) 1, // recursion level
                                                                   strWhere, // object where clause
                                                                   DomainConstants.EMPTY_STRING, // relationship where clause
                                                                   0);
           
           String strMaterialAdvRate         = DomainConstants.EMPTY_STRING;
           if(mlMaterialList.size()>0)
           {
               strMaterialAdvRate            = (String)((Map) mlMaterialList.get(0)).get("attribute["+ATTRIBUTE_WMS_ADVANCE_RATE+"]");
           }
           if( !DomainConstants.EMPTY_STRING.equals(strMaterialAdvRate) )
           {
               Double dMaterialAdvanceRate   = Double.parseDouble(strMaterialAdvRate);
               String strRateParUnit         = (String)mapStockAttr.get(ATTRIBUTE_WMS_STOCK_ENTRIES_RATE_PER_UNIT);//domStock.getAttributeValue(context, );
               Double bRateParUnit           = Double.parseDouble(strRateParUnit);
               Double dTotalAmountForRecovey = bTekeQunt   *  bRateParUnit;
               Double dFinalRecoveyValue     = (dMaterialAdvanceRate * dTotalAmountForRecovey ) / 100;
               /**
                *   Commented as of now ..as Taxes are not cosidered in UI : may be need this in future 
                */
            /*   String strCGST                = (String)mapStockAttr.get(ATTR_STOCK_CGST);
               String strSGST                = (String)mapStockAttr.get(ATTR_STOCK_SGST);
               strCGST                       = strCGST==null || "".equals(strCGST) || "null".equals(strCGST) ? "0.0" : strCGST;
               strSGST                       = strSGST==null || "".equals(strSGST) || "null".equals(strSGST) ? "0.0" : strSGST;
               Double   dCGST                = Double.parseDouble(strCGST);
               Double   dSGST                = Double.parseDouble(strSGST);
               Double dTotalTexOnStock       = dCGST+dSGST;
               if(dTotalTexOnStock>0)
               {
                   Double bTotalTexAount     = (dFinalRecoveyValue*dTotalTexOnStock)/100;
                   dFinalRecoveyValue        = bTotalTexAount+dFinalRecoveyValue;
               }*/
               DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_RECOVERY_AMOUNT ,  dFinalRecoveyValue.toString()); 
           } 
       }
    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    } finally {
    }
    return iReturnValue;
}

 
/**
 * trigger on  WMSMaterialConsumptionToStockCreateCheck
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
public int checkMCReqQut(Context context, String[] args) throws Exception
{
    int iReturnValue =   0;
    try{
    
        String strStockId        = args[0];
        String strRelId          = args[1];
        String strMCId           = args[2];
        
        DomainObject domStock    = DomainObject.newInstance(context , strStockId);
        DomainObject domMC       = DomainObject.newInstance(context , strMCId);
        Map mapMCAttr            = domMC.getAttributeMap(context);
        String strMCReqQun       = (String) mapMCAttr.get(ATTRIBUTE_WMS_STOCK_PENDING_QTY); 
        Double dMCReqQun         = Double.parseDouble(strMCReqQun);

       if(dMCReqQun==0.0)
       {
           iReturnValue=1;
       } 
    }
    catch(Exception ex)
    {
        ex.printStackTrace();
        throw new Exception("Quntity Fully Consumed");
    }
    return iReturnValue;
 }

/** trigger on WMSMaterialConsumptionToStockDeleteAction
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */

public int removeMCStockQut(Context context, String[] args) throws Exception
{
    int iReturnValue = 0;
    try{
            String strStockId             = args[0];
            String strRelId               = args[1];
            String strMCId                = args[2];

            DomainRelationship domRel     = DomainRelationship.newInstance(context, strRelId);
            domRel.open(context);
            String strRelAveQunty         = domRel.getAttributeValue(context ,ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY);
           //  strRelAveQunty                = DomainRelationship.getAttributeValue(context, strRelId, "CHiPSStockAvailableQty");
            DomainObject domStock         = DomainObject.newInstance(context , strStockId);
            Map mapStockAttr              = domStock.getAttributeMap(context);
            String strStockAvl            = (String) mapStockAttr.get(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY); 
            String strStockPending        = (String) mapStockAttr.get(ATTRIBUTE_WMS_STOCK_PENDING_QTY); 

            Double dStockAvl              = Double.parseDouble(strStockAvl);
            Double dStockPending          = Double.parseDouble(strStockPending);
            Double dRelQun                = Double.parseDouble(strRelAveQunty); 
            Double dFStockAv              = dStockAvl+ dRelQun;
            Double dFStockPanding         = dStockPending - dRelQun;

            HashMap mapStockAttrMap       = new HashMap();
            mapStockAttrMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY, dFStockAv.toString());
            mapStockAttrMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, dFStockPanding.toString());
            domStock.setAttributeValues(context, mapStockAttrMap);

            if(strMCId!= null && !"".equals(strMCId) && !"null".equals(strMCId))
            {
                DomainObject domMC            = DomainObject.newInstance(context , strMCId);
                if(domMC.exists(context))
                {
                    Map mapMCAttr             = domMC.getAttributeMap(context);
                    String strMCReqQun        = (String) mapMCAttr.get(ATTRIBUTE_WMS_STOCK_PENDING_QTY); 
                    String strMCAVQun         = (String) mapMCAttr.get(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY); 
                    Double dMCReqQun          = Double.parseDouble(strMCReqQun);
                    Double dMCAVQun           = Double.parseDouble(strMCAVQun);
                    Double dFMCAv             = dMCAVQun - dRelQun;
                    Double dFMCReqd           = dMCReqQun + dRelQun;
                    HashMap mapMCAttrMap      = new HashMap();
                    mapMCAttrMap.put(ATTRIBUTE_WMS_STOCK_AVAILABLE_QTY, dFMCAv.toString());
                    mapMCAttrMap.put(ATTRIBUTE_WMS_STOCK_PENDING_QTY, dFMCReqd.toString());
                    domMC.setAttributeValues(context, mapMCAttrMap);
                }
            }
    } catch (Exception e) {
        e.printStackTrace();
        iReturnValue=1;
        throw e;
    } finally {
    }
    return iReturnValue;
}


public HashMap removeMCStock(Context context , String [] args) throws Exception
{
    HashMap returnMap = new HashMap();
    try {
            HashMap programMap               = (HashMap)JPO.unpackArgs(args);
            String [] stStockId              = (String[])programMap.get("stockId");
            String strMaterialMCId           = (String)programMap.get("MCId");

            ArrayList<String> alStockList    = new ArrayList<String>(Arrays.asList(stStockId));
            String strObjectID               = (String) programMap.get("objectId");
            StringList slBusSelect           = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_NAME);

            StringList slRelSelect           = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);

            DomainObject domMC               = DomainObject.newInstance(context , strMaterialMCId);
            MapList mlMCStock                = domMC.getRelatedObjects(context, // matrix context
                                                                    RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
                                                                    TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                                    slBusSelect, // object selects
                                                                    slRelSelect, // relationship selects
                                                                    true, // to direction
                                                                    true, // from direction
                                                                    (short) 1, // recursion level
                                                                    DomainConstants.EMPTY_STRING, // object where clause
                                                                    DomainConstants.EMPTY_STRING, // relationship where clause
                                                                    0);
      

      Map dataMap    =   null;
      String strStockId  =  DomainConstants.EMPTY_STRING;
      for (int i = 0; i < mlMCStock.size(); i++) {
          dataMap    =   (Map) mlMCStock.get(i);
          strStockId =   (String) dataMap.get(DomainConstants.SELECT_ID);
          if(alStockList.contains(strStockId))
          {
              DomainRelationship.disconnect(context, (String) dataMap.get(DomainRelationship.SELECT_ID));
          }
      }
   } catch (Exception e) {
  }
    return returnMap;
}
	/**Update maker : update maker on Material Bill particular page
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	
	public boolean updateMaker(Context context,String[] args) throws Exception
	{ 
		 Map m =  (HashMap) JPO.unpackArgs(args);
		 Map paramMap=(Map) m.get("paramMap");
		 String strRowObjectId =  (String) paramMap.get("objectId");
		 DomainObject domStock=DomainObject.newInstance(context, strRowObjectId);
		 String strRelid = domStock.getInfo(context, "to["+RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF+"].id");
		 if(strRelid!=null && !strRelid.isEmpty()) {
			 
			 DomainRelationship.disconnect(context, strRelid);
		 }
		 String strNewMaker = (String) paramMap.get("New Value");
		 if(strNewMaker!=null && !strNewMaker.isEmpty()) {
			 DomainObject domMaker=DomainObject.newInstance(context,strNewMaker);
			 DomainRelationship.connect(context, domMaker, RELATIONSHIP_WMS_STOCK_ENTRIES_MAKER_OF, domStock);
		 }
		
	   	try {
	
	
	
		   }catch(Exception e) {
			e.printStackTrace();
		}
	 return true;
	}
	
	/**Update WorkOrder : Field defined on Material Bill Property 
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	
	public boolean updateWorkOrder(Context context,String[] args) throws Exception
	{ 
 		 try {
 			 Map inputMap =  (HashMap) JPO.unpackArgs(args);
 			 Map paramMap = (HashMap)inputMap.get("paramMap");
 			 String strNewValue = (String) paramMap.get("New Value");
 			 String strOldValue = (String) paramMap.get("Old Value");
 			 String strMaterialBill = (String) paramMap.get("objectId");
 			 if(!strNewValue.equalsIgnoreCase(strOldValue)) {
 				 DomainObject domMaterial= DomainObject.newInstance(context,strMaterialBill);
 				 String strRelId = domMaterial.getInfo(context,"to["+RELATIONSHIP_WMS_WO_MATERIAL_BILL+"].id");
 			 	 DomainRelationship.disconnect(context, strRelId);
 			 	 DomainRelationship.connect(context, strNewValue, RELATIONSHIP_WMS_WO_MATERIAL_BILL, strMaterialBill, false);
 				  
 			 }
 	 	   }catch(Exception e) {
			e.printStackTrace();
		}
	 return true;
	}
/**
 * Material escalation : 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	
@ProgramCallable	
public MapList getAMBMaterialEscalations(Context context,String[] args) throws Exception{
	MapList ml=new MapList();
	try {
	      Map input=JPO.unpackArgs(args);
	      String strAMBId =(String) input.get("objectId");
	      DomainObject domAMB=DomainObject.newInstance(context,strAMBId);
	      StringList slBusSelect  = new StringList();
          slBusSelect.add(DomainConstants.SELECT_ID);
          slBusSelect.add(DomainConstants.SELECT_NAME);

          StringList slRelSelect  = new StringList();
          slRelSelect.add(DomainRelationship.SELECT_ID);
           return  domAMB.getRelatedObjects(context, // matrix context
                                                              RELATIONSHIP_WMS_MATERIAL_ESCALATION, // relationship pattern
                                                              TYPE_WMS_STOCK_ENTRIES, // type pattern
                                                              slBusSelect, // object selects
                                                              slRelSelect, // relationship selects
                                                              false, // to direction
                                                              true, // from direction
                                                              (short) 1, // recursion level
                                                              DomainConstants.EMPTY_STRING, // object where clause
                                                              DomainConstants.EMPTY_STRING, // relationship where clause
                                                              0);
	
	
	
	}catch(Exception e) {
		e.printStackTrace();
	}
	return ml;
  }

@IncludeOIDProgramCallable
public StringList getMaterialWithBaseRate(Context context,String[] args) throws Exception
{
	StringList slIncludeIds =new StringList();
    try {
    	 
          Map inputMap= JPO.unpackArgs(args);
          String strObjectId= (String)inputMap.get("objectId");
          DomainObject domAMB=DomainObject.newInstance(context,strObjectId);
          String strWorkOrderId = domAMB.getInfo(context, "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
          DomainObject domWO=DomainObject.newInstance(context,strWorkOrderId);
          StringList slBusSelect  = new StringList();
          slBusSelect.add(DomainConstants.SELECT_ID);
          slBusSelect.add(DomainConstants.SELECT_NAME);
          StringList slRelSelect  = new StringList();
          slRelSelect.add("attribute["+ATTRIBUTE_WMS_BASE_RATE+"]");
          slRelSelect.add(DomainRelationship.SELECT_ID);
          String strBusWhere = "to["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]=='False'";
          DomainObject domWo      = DomainObject.newInstance(context , strWorkOrderId);
          String strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_MATERIAL_BILL+"' && current=='"+STATE_MATERIAL_BILL_APPROVE+"') || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_STOCK_ENTRIES+")";
          slBusSelect.add("to["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
	        Map mPostWhere = new HashMap(); 
 	        mPostWhere.put("to["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]", "False");
 	       
	        Pattern pType = new Pattern(TYPE_WMS_MATERIAL_BILL);
	  		pType.addPattern(TYPE_WMS_STOCK_ENTRIES);
	  		Pattern pRel = new Pattern(RELATIONSHIP_WMS_WO_MATERIAL_BILL);
	  		pRel.addPattern(RELATIONSHIP_WMS_MATERIALBILL_STOCK);
	  	    MapList mlBillStocks = domWO.getRelatedObjects(context,
	  				 pRel.getPattern(),                         // relationship pattern
	  				 pType.getPattern(),                                    // object pattern
	  				true,                                                        // to direction
	  				true,                                                       // from direction
	  				(short)0,                                                      // recursion level
	  				slBusSelect,                                                 // object selects
	  				slRelSelect,                                                         // relationship selects
	  				strWhere,                                // object where clause
	  				DomainConstants.EMPTY_STRING,                                // relationship where clause
	  				(short)0,                                                      // No expand limit
	  				DomainConstants.EMPTY_STRING,                                // postRelPattern
	  				TYPE_WMS_STOCK_ENTRIES,                                                // postTypePattern
	  				mPostWhere);
	  	  return   WMSUtil_mxJPO.convertToStringList(mlBillStocks, "id");
	  	    
       }catch(Exception e) {
    	
    	e.printStackTrace();
    }
    return slIncludeIds;
}


@ExcludeOIDProgramCallable
public StringList excludeConnectedMaterialEscalations(Context context,String[] args) throws Exception
{
	StringList slExcludeIds =new StringList();
    try {





      }catch(Exception e) {
    	
    	e.printStackTrace();
    }
    return slExcludeIds;
}


/**
 * Material escalation :  tab 2 : 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	
@ProgramCallable	
public MapList getAllEscalatedMaterials(Context context,String[] args) throws Exception{
	MapList ml=new MapList();
	try {
		  double dConsumed=0.0;
		  String strMatId=DomainConstants.EMPTY_STRING;
		  String strBillId=DomainConstants.EMPTY_STRING;
		  String strSotckId=DomainConstants.EMPTY_STRING;
	 	  DomainObject domStock=DomainObject.newInstance(context);
	      Map input=JPO.unpackArgs(args);
	      String strFilterValue = (String)  input.get("WMSMaterialFilter"); 
	       String strAMBId =(String) input.get("objectId");
	      DomainObject domAMB=DomainObject.newInstance(context,strAMBId);
	      StringList slBusSelect = new StringList();
	      slBusSelect.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
	      Map mInfo =   domAMB.getInfo(context, slBusSelect);
	      String strWorkOrderId = (String)mInfo.get( "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      String strSeqNo = (String) mInfo.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      DomainObject domWO=DomainObject.newInstance(context, strWorkOrderId);
	    
          slBusSelect.add(DomainConstants.SELECT_ID);
          slBusSelect.add(DomainConstants.SELECT_NAME);
          slBusSelect.add(DomainConstants.SELECT_TYPE);
          StringList slRelSelect  = new StringList();
          slRelSelect.add(DomainRelationship.SELECT_ID);
          StringList slAMBsToConsider=new StringList();
          String strHasMEConnected =  (String) mInfo.get("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
     
        
          //get all  AMB  where Sequence is less than current AMB and which has Material Escalation connected
          
          MapList mlAbsList    = domWO.getRelatedObjects(context, // matrix context
					 RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE, // relationship pattern
					 TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY, // type pattern
	                 slBusSelect, // object selects
	                 null, // relationship selects
	                 false, // to direction
	                 true, // from direction
	                 (short) 1, // recursion level
	                 "attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] < "+strSeqNo+" && from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]==True" , // object where clause
	                 DomainConstants.EMPTY_STRING, // relationship where clause
	                 0);
         StringList slAMBList =  WMSUtil_mxJPO.convertToStringList(mlAbsList, DomainConstants.SELECT_ID);
         if(strHasMEConnected.equalsIgnoreCase("TRUE"))
        	 slAMBList.add(strAMBId);   // current AMB 
        MapList mlAllStockEntries= new MapList();
        String strWhere=DomainConstants.EMPTY_STRING;
        Iterator<String> itrAMB = slAMBList.iterator();
         if(input.containsKey("FilterRange")){
        	 slBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.name");
         }else {
        	 if(!strFilterValue.equalsIgnoreCase("All"))
        	 strWhere="from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.name=='"+strFilterValue+"'";
         }
         while(itrAMB.hasNext()) {
        	 domAMB.setId((String)itrAMB.next());
        	 MapList mlStockList  = domAMB.getRelatedObjects(context, // matrix context
												  RELATIONSHIP_WMS_MATERIAL_ESCALATION, // relationship pattern
												  TYPE_WMS_STOCK_ENTRIES, // type pattern
									              slBusSelect, // object selects
									              null, // relationship selects
									              false, // to direction
									              true, // from direction
									              (short) 1, // recursion level
									              strWhere, // object where clause
									              DomainConstants.EMPTY_STRING, // relationship where clause
									              0);
        	 
        	 for(int i=0;i<mlStockList.size();i++) {
        		 mlAllStockEntries.add(mlStockList.get(i));
         	 }
           }
        
    return mlAllStockEntries;
         
        		          
	}catch(Exception e) {
		e.printStackTrace();
	}
	return ml;
  }


@ProgramCallable	
public MapList getEscalatedMaterialConsumptionSummary(Context context,String[] args) throws Exception{
	MapList mlStocks = new MapList();
	try {
		
		 DecimalFormat  dd=new DecimalFormat("0.000");
		 double dConsumed=0.0;
		  String strMatId=DomainConstants.EMPTY_STRING;
		  String strBillId=DomainConstants.EMPTY_STRING;
		  String strSotckId=DomainConstants.EMPTY_STRING;
	 	  DomainObject domStock=DomainObject.newInstance(context);
	      Map input=JPO.unpackArgs(args);
	      String strFilterValue = (String)  input.get("WMSMaterialFilter"); 
	      String strAMBId =(String) input.get("objectId");
	      DomainObject domAMB=DomainObject.newInstance(context,strAMBId);
	      StringList slBusSelect = new StringList();
	      slBusSelect.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
	      Map mInfo =   domAMB.getInfo(context, slBusSelect);
	      String strWorkOrderId = (String)mInfo.get( "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      String strSeqNo = (String) mInfo.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      DomainObject domWO=DomainObject.newInstance(context, strWorkOrderId);
	    
         slBusSelect.add(DomainConstants.SELECT_ID);
         slBusSelect.add(DomainConstants.SELECT_NAME);
         slBusSelect.add(DomainConstants.SELECT_TYPE);
         StringList slRelSelect  = new StringList();
         slRelSelect.add(DomainRelationship.SELECT_ID);
         StringList slAMBsToConsider=new StringList();
         String strHasMEConnected =  (String) mInfo.get("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
    
       
         //get all  AMB  where Sequence is less than current AMB and which has Material Escalation connected
          MapList mlAbsList    = domWO.getRelatedObjects(context, // matrix context
					 RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE, // relationship pattern
					 TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY, // type pattern
	                 slBusSelect, // object selects
	                 null, // relationship selects
	                 false, // to direction
	                 true, // from direction
	                 (short) 1, // recursion level
	                 "attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] <= "+strSeqNo,// , // object where clause
	                 DomainConstants.EMPTY_STRING, // relationship where clause
	                 0);
		StringList slCandidateAMBs= WMSUtil_mxJPO.convertToStringList(mlAbsList, DomainConstants.SELECT_ID);
		//Now get only thoseStockList where Material Escalation is reported 
		String strVault = context.getVault().getName();
		String strWhere="to["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]==True ";
		 if(!input.containsKey("FilterRange")){
		   if(!strFilterValue.equalsIgnoreCase("All")){
       	        strWhere=strWhere +" && from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.name=='"+strFilterValue+"'";
			}
			 strWhere = strWhere + " &&  to["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"].from.id=="+strAMBId;
		 } 
	 
		StringList slMConsumption=new StringList();
		slMConsumption.add(DomainConstants.SELECT_ID);
		slMConsumption.add(DomainConstants.SELECT_NAME);
		slMConsumption.add("from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id");
		slMConsumption.add("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
		slBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
		MapList mlEscalatedStockList = DomainObject.findObjects(context,
                TYPE_WMS_STOCK_ENTRIES ,
                DomainConstants.QUERY_WILDCARD, 
                DomainConstants.QUERY_WILDCARD,
                DomainConstants.QUERY_WILDCARD,
                strVault,
                strWhere, // where expression
                DomainConstants.EMPTY_STRING, 
                false,
                slBusSelect, // object selects
                (short) 0);
	 Iterator itrStock = mlEscalatedStockList.iterator();
	 boolean bConsidetStock=false;
	 while(itrStock.hasNext()) {
		 dConsumed=0.00;
		 Map mStock=(Map)itrStock.next();
		 domStock.setId( (String)mStock.get(DomainConstants.SELECT_ID));
		 MapList mlConsumption=   domStock.getRelatedObjects(context, // matrix context
					                  RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
					                  TYPE_WMS_MATERIAL_CONSUMPTION, // type pattern
					                  slMConsumption, // object selects
					                  null, // relationship selects
					                  true, // to direction
					                  false, // from direction
					                  (short) 1, // recursion level
					                  DomainConstants.EMPTY_STRING,
					                  DomainConstants.EMPTY_STRING, // relationship where clause
					                  0);
		   Iterator<Map> itrMConsumption= mlConsumption.iterator();
	 	   while(itrMConsumption.hasNext()) {
			  Map mConsuption =itrMConsumption.next();
			  strAMBId = (String) mConsuption.get("from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id");
			  if(slCandidateAMBs.contains(strAMBId)) {
				  dConsumed=dConsumed+ Double.parseDouble((String)mConsuption.get("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]"));
			  }
	 	   }
	 	    if(dConsumed>0) {
	 		  mStock.put("CONSUMED", dd.format(dConsumed));
	 		  mlStocks.add(mStock);
	 	   }
  	 }
 	}catch(Exception e) {
		e.printStackTrace();
	}
	
	
	return mlStocks;
	
	
	
}





/**
 * Material escalation :  tab 2 : 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	
@ProgramCallable	
public MapList getConsumptionSummary(Context context,String[] args) throws Exception{
	
	MapList mlAllStockEntries=new MapList();
	try {/*
		  double dConsumed=0.0;
		  String strMatId=DomainConstants.EMPTY_STRING;
		  String strBillId=DomainConstants.EMPTY_STRING;
		  String strSotckId=DomainConstants.EMPTY_STRING;
	 	  DomainObject domStock=DomainObject.newInstance(context);
	      Map input=JPO.unpackArgs(args);
	      String strFilterValue = (String)  input.get("WMSMaterialFilter"); 
	       String strAMBId =(String) input.get("objectId");
	      DomainObject domAMB=DomainObject.newInstance(context,strAMBId);
	      StringList slBusSelect = new StringList();
	      slBusSelect.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
	      Map mInfo =   domAMB.getInfo(context, slBusSelect);
	      String strWorkOrderId = (String)mInfo.get( "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
	      String strSeqNo = (String) mInfo.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
	      DomainObject domWO=DomainObject.newInstance(context, strWorkOrderId);
	    
          slBusSelect.add(DomainConstants.SELECT_ID);
          slBusSelect.add(DomainConstants.SELECT_NAME);
          slBusSelect.add(DomainConstants.SELECT_TYPE);
          StringList slRelSelect  = new StringList();
          slRelSelect.add(DomainRelationship.SELECT_ID);
          StringList slAMBsToConsider=new StringList();
          String strHasMEConnected =  (String) mInfo.get("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
     
        
          //get all  AMB  where Sequence is less than current AMB and which has Material Escalation connected
          
          MapList mlAbsList    = domWO.getRelatedObjects(context, // matrix context
					 RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE, // relationship pattern
					 TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY, // type pattern
	                 slBusSelect, // object selects
	                 null, // relationship selects
	                 false, // to direction
	                 true, // from direction
	                 (short) 1, // recursion level
	                 "attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] < "+strSeqNo+" && from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]==True" , // object where clause
	                 DomainConstants.EMPTY_STRING, // relationship where clause
	                 0);
         StringList slAMBList =  WMSUtil_mxJPO.convertToStringList(mlAbsList, DomainConstants.SELECT_ID);
         if(strHasMEConnected.equalsIgnoreCase("TRUE"))
        	 slAMBList.add(strAMBId);   // current AMB 
     
        String strWhere=DomainConstants.EMPTY_STRING;
        slBusSelect.add("to["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK+"].from.name");
        
        slBusSelect.add("to["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK+"].from.attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
        DomainConstants.MULTI_VALUE_LIST.add("to["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK+"].attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
        Iterator<String> itrAMB = slAMBList.iterator();
         if(input.containsKey("FilterRange")){
        	 slBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.name");
         }else {
        	 if(!strFilterValue.equalsIgnoreCase("All"))
        	 strWhere="from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.name=='"+strFilterValue+"'";
         }
         slBusSelect.add("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
         double dConsumedTotal=0;
         String strStockId=DomainConstants.EMPTY_STRING;
      StringList slMaterialConsu=new StringList();
      slMaterialConsu.add(DomainConstants.SELECT_ID);
      slMaterialConsu.add(DomainConstants.SELECT_NAME);
      slMaterialConsu.add("from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.name");
      slMaterialConsu.add("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
      StringList slConsumptionIds= new StringList();
      String strConsumId=DomainConstants.SELECT_ID;
         while(itrAMB.hasNext()) {
        	  strAMBId=(String)itrAMB.next();
        	  domAMB.setId(strAMBId);
        	  MapList   ml  = domAMB.getRelatedObjects(context, // matrix context
												  RELATIONSHIP_WMS_MATERIAL_ESCALATION, // relationship pattern
												  TYPE_WMS_STOCK_ENTRIES, // type pattern
									              slBusSelect, // object selects
									              null, // relationship selects
									              false, // to direction
									              true, // from direction
									              (short) 1, // recursion level
									              strWhere, // object where clause
									              DomainConstants.EMPTY_STRING, // relationship where clause
									              0);
        	  Iterator<Map> itrStockEntries = ml.iterator();
        	  while(itrStockEntries.hasNext()) {
        		  Map mStock=itrStockEntries.next();
        		  strStockId =  (String) mStock.get(DomainConstants.SELECT_ID);
        		  domStock.setId(strStockId);
               	   	  MapList mlConsumption=   domStock.getRelatedObjects(context, // matrix context
						                          RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_STOCK, // relationship pattern
						                          TYPE_WMS_MATERIAL_CONSUMPTION, // type pattern
						                          slMaterialConsu, // object selects
						                          null, // relationship selects
						                          true, // to direction
						                          false, // from direction
						                          (short) 1, // recursion level
						                          "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id=='"+strAMBId+"'", // object where clause
						                          DomainConstants.EMPTY_STRING, // relationship where clause
						                          0);
               
            	Iterator<Map>  itrMC= mlConsumption.iterator();
            	dConsumed=0;
             	while(itrMC.hasNext()) {
            		Map m = itrMC.next();
            		strConsumId= (String)m.get(DomainConstants.SELECT_ID);
	            		if(!slConsumptionIds.contains(strConsumId)) {
	            			 
	             			dConsumed = dConsumed+Double.parseDouble((String)m.get("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]"));
	             			slConsumptionIds.add(strConsumId);
	            	 	}
             		  
             		}
             	   mStock.put("CONSUMED", String.valueOf(dConsumed)) ;
         		mlAllStockEntries.add(mStock);  
        	  }
        	  
 
           }
          
  	*/}catch(Exception e) {
		e.printStackTrace();
	 }
	return  getEscalatedMaterialConsumptionSummary(context,args);
	//return mlAllStockEntries;
	}


@ProgramCallable
public MapList getAllMaterialsForConsumptionSummary(Context context,String[] args) throws Exception
{
	 MapList ml=new MapList();
  try {
      Map programMap = JPO.unpackArgs(args);
      String strAMBId = (String) programMap.get("objectId");
      DomainObject domAMB=DomainObject.newInstance(context, strAMBId);
      StringList slBusSelect = new StringList();
      slBusSelect.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
      slBusSelect.add("to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
      slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]");
      Map mInfo =   domAMB.getInfo(context, slBusSelect);
      String strWorkOrderId = (String)mInfo.get( "to["+RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE+"].from.id");
      String strSeqNo = (String) mInfo.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
      DomainObject domWO=DomainObject.newInstance(context, strWorkOrderId);
      slBusSelect.clear();
      slBusSelect.add(DomainConstants.SELECT_ID);
      StringList slRelSelect  = new StringList();
      slRelSelect.add(DomainRelationship.SELECT_ID);
      MapList mlMaterialList    = domWO.getRelatedObjects(context, // matrix context
							              RELATIONSHIP_WMS_WORK_ORDER_ADVANCE_RATE, // relationship pattern
							              TYPE_WMS_MATERIAL, // type pattern
							              slBusSelect, // object selects
							              slRelSelect, // relationship selects
							              false, // to direction
							              true, // from direction
							              (short) 1, // recursion level
							              DomainConstants.EMPTY_STRING, // object where clause
							              "attribute["+ATTRIBUTE_WMS_BASE_RATE+"]!=''", // relationship where clause
							              0);
      
      //get AMB 
      
        StringList slMaterialId=WMSUtil_mxJPO.convertToStringList(mlMaterialList, DomainConstants.SELECT_ID);
      String strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY+"' && attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] <= "+strSeqNo+") || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_CONSUMPTION+")";
      Pattern pType = new Pattern(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY);
  	  pType.addPattern(TYPE_WMS_MATERIAL_CONSUMPTION);
  	  Pattern pRel = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE);
  	  pRel.addPattern(RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB);
  	  slBusSelect.clear();
  	  slBusSelect.add(DomainConstants.SELECT_ID);
  	  slBusSelect.add(DomainConstants.SELECT_TYPE);
  	  slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL+"].to.id");
      slBusSelect.add("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
      Map mMaterialInfo = new HashMap();
      double dTotalConsumption=0;
      Map m=new HashMap();
      m.put("FilterRange", "All");
      m.put("objectId", strAMBId);
        
     MapList mlStocks= getConsumptionSummary(context,JPO.packArgs(m));
     StringList sl  = WMSUtil_mxJPO.convertToStringList(mlStocks, DomainConstants.SELECT_ID);
     String strMatId=DomainConstants.EMPTY_STRING;
     Map mStore=new HashMap();
     String strMatIdTemp=DomainConstants.EMPTY_STRING;
     double dConsumedStock=0.0;
     double dTotalMatConsumed=0.0;
     for(int i=0;i<slMaterialId.size();i++)
     {      strMatIdTemp=(String)slMaterialId.get(i);
		    Iterator itr = mlStocks.iterator();
		    Map mTemp =new HashMap();
    	    while(itr.hasNext()) {
    	    	Map m1= (Map)itr.next();
    	    	dConsumedStock = Double.parseDouble((String)m1.get("CONSUMED"));
    	    	strMatId  = (String)m1.get("from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id");
    	    	if(strMatId.equalsIgnoreCase(strMatIdTemp)) {
    	    		if(mMaterialInfo.containsKey(strMatIdTemp)) {
    	    	 		Map mInner = (Map)mMaterialInfo.get(strMatIdTemp);
    	    	 		dConsumedStock =dConsumedStock+ Double.parseDouble((String)mInner.get("TOTAL_CONSUMED"));
    	    		}
    	    	 
    	    		mTemp.put("TOTAL_CONSUMED", String.valueOf(dConsumedStock));
    	    		mMaterialInfo.put(strMatIdTemp, mTemp); // need to change this 
    	    	}
    	  	    	
    	    }
    	 }
     
     /***  Getting total received ***/
     
     DecimalFormat  dd=new DecimalFormat("0.00");
     
     strWhere = "("+DomainConstants.SELECT_TYPE+"=='"+TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY+"' && attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] <= "+strSeqNo+") || ("+DomainConstants.SELECT_TYPE+"=="+TYPE_WMS_MATERIAL_CONSUMPTION+")";
     Pattern pType2 = new Pattern(TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY);
 	  pType2.addPattern(TYPE_WMS_MATERIAL_CONSUMPTION);
 	  Pattern pRel2 = new Pattern(RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE);
 	  pRel.addPattern(RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB);
 	  slBusSelect.clear();
 	  slBusSelect.add(DomainConstants.SELECT_ID);
 	  slBusSelect.add(DomainConstants.SELECT_TYPE);
 	 slBusSelect.add("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
 	  slBusSelect.add("from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_MATERIAL+"].to.id");
     slBusSelect.add("attribute["+ATTRIBUTE_WMS_MATERIAL_CONSUMPTION_QUANTITY+"]");
   
      MapList  mlAbsList    = domWO.getRelatedObjects(context, // matrix context
									 RELATIONSHIP_WMS_WORK_ORDER_ABSTRACT_MBE, // relationship pattern
									 TYPE_WMS_ABSTRACT_MEASUREMENT_BOOK_ENTRY, // type pattern
					                 slBusSelect, // object selects
					                 null, // relationship selects
					                 false, // to direction
					                 true, // from direction
					                 (short) 1, // recursion level
					                "attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"] <= "+strSeqNo+" && from["+RELATIONSHIP_WMS_MATERIAL_ESCALATION+"]==True" , // object where clause
					                 DomainConstants.EMPTY_STRING, // relationship where clause
					                 0);
      
      
      mlAbsList.sort("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]", "ascending", "integer");
    
      /* Get Base Rate from WO and Mat rel */
      m.clear();
      m.put("objectId", strWorkOrderId);
      MapList mlMaterials = getWOMAdvanceRate(context,JPO.packArgs(m));
      Iterator<Map> itrMat = mlMaterials.iterator();
    //  String strMatId=DomainConstants.EMPTY_STRING;
      while(itrMat.hasNext()) {
    	  Map mMaterial =itrMat.next();
    	  strMatId = (String)mMaterial.get(DomainConstants.SELECT_ID);
    	  if(mMaterialInfo.containsKey(strMatId)) {
    		  Map mMInfo=(Map)mMaterialInfo.get(strMatId);
    		  mMInfo.put("BASE_RATE", mMaterial.get("attribute["+ATTRIBUTE_WMS_BASE_RATE+"]"));
    		  mMaterialInfo.put(strMatId, mMInfo);
    	   }
    	  
      }
      slBusSelect.clear();
      slBusSelect.add(DomainConstants.SELECT_ID);
 	  slBusSelect.add(DomainConstants.SELECT_TYPE);
 	  slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]");
 	  slBusSelect.add("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT+"]");
 	  slRelSelect.clear();
 	  slRelSelect.add("attribute[WMSBillAmount]");
 	  slRelSelect.add(DomainRelationship.SELECT_ID);
 	   String strRelId= DomainConstants.EMPTY_STRING;
 	   double dTotalReceived=0.0;
 	   double dTotalAmount=0.0;
 	   double dAvg=0.0;
 	   double dTillPrevious=0.0;
 	   String strSeqNoTemp=DomainConstants.EMPTY_STRING;
         for(int i=0;i<slMaterialId.size();i++)
         {    
        	 
        	   dTotalReceived=0.0;
               dTotalAmount=0.0;
               Iterator<Map> itrAMBS = mlAbsList.iterator();
              while(itrAMBS.hasNext()) {
            	 Map mAMBs =  itrAMBS.next();
            	 strAMBId=(String)mAMBs.get(DomainConstants.SELECT_ID);
            	 domAMB.setId(strAMBId);
            	 
            	 strSeqNoTemp=(String)mAMBs.get("attribute["+ProgramCentralConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
                 MapList mlStockEntries= domAMB.getRelatedObjects(context, // matrix context
								  RELATIONSHIP_WMS_MATERIAL_ESCALATION, // relationship pattern
								  TYPE_WMS_STOCK_ENTRIES, // type pattern
					              slBusSelect, // object selects
					              slRelSelect, // relationship selects
					              false, // to direction
					              true, // from direction
					              (short) 1, // recursion level
					               "from["+RELATIONSHIP_WMS_STOCK_MATERIAL+"].to.id=='"+slMaterialId.get(i)+"'", // object where clause
					              DomainConstants.EMPTY_STRING, // relationship where clause
					              0);
                
                 Iterator<Map> itrStock= mlStockEntries.iterator();
                 while(itrStock.hasNext()) {
                	Map mStock=  itrStock.next();
                	dTotalReceived=dTotalReceived+Double.parseDouble( ((String)mStock.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_QUANTITY+"]")));
                	dTotalAmount=dTotalAmount+Double.parseDouble( ((String)mStock.get("attribute["+ATTRIBUTE_WMS_STOCK_ENTRIES_AMOUNT+"]")));
                	strRelId=(String)mStock.get(DomainRelationship.SELECT_ID);
                	dTillPrevious=Double.valueOf((String)mStock.get("attribute[WMSBillAmount]"));;
                	
                 }
              }
                 if(Integer.parseInt(strSeqNo)-Integer.parseInt((strSeqNoTemp))==0){
                	 dTillPrevious=0;
                 }
	              Map matDetail = (Map)mMaterialInfo.get(slMaterialId.get(i));
	              matDetail.put("TOTAL_RECEIVED", String.valueOf(dTotalReceived));
	              matDetail.put("TOTAL_AMOUNT", String.valueOf(dTotalAmount));
	              matDetail.put("AVG_RATE", dd.format(dTotalAmount/dTotalReceived));
	              double diff=  (dTotalAmount/dTotalReceived) - Double.parseDouble((String)matDetail.get("BASE_RATE"));
	              matDetail.put("RATE_DIFF", dd.format(diff) );
	              matDetail.put("VARI_AMOUNT", dd.format(Double.parseDouble((String)matDetail.get("TOTAL_CONSUMED")) * diff));
	              matDetail.put("VARI_AMOUNT_CURR_BILL", "0.00");
	           
	              mMaterialInfo.put(slMaterialId.get(i), matDetail);
	              matDetail.put("id", slMaterialId.get(i));
	               if(Integer.parseInt(strSeqNo)!=1) {	
	                  if(Integer.parseInt(strSeqNo)-Integer.parseInt((strSeqNoTemp))==1){
	                	   matDetail.put("VARI_AMOUNT_CURR_BILL", dd.format( ((Double.parseDouble((String)matDetail.get("TOTAL_CONSUMED")) * diff)-dTillPrevious)));
	                      }
	               } 
	              if(Integer.parseInt(strSeqNo)==Integer.parseInt((strSeqNoTemp))) {
	            	   DomainRelationship domRel = new DomainRelationship(strRelId);
	            	   domRel.setAttributeValue(context, "WMSBillAmount", dd.format(Double.parseDouble((String)matDetail.get("TOTAL_CONSUMED")) * diff));
	            	    matDetail.put("VARI_AMOUNT_CURR_BILL", dd.format( (Double.parseDouble((String)matDetail.get("TOTAL_CONSUMED")) * diff)-dTillPrevious));
	            	    
	              } 
	             
	              
	              ml.add(matDetail);
           
              
          }	
          
	}catch(Exception e) {
		e.printStackTrace();
	}
	
	return ml;
 }
 public Vector getMaterialColumnValue(Context context,String[] args) throws Exception
 {   Vector vColumn=new Vector();
	try {
		
		 Map programMap              = (Map) JPO.unpackArgs(args);
		 Map columnMap               = (Map)programMap.get("columnMap");
		 Map   setting               = (Map) columnMap.get("settings");
         MapList objectList          = (MapList) programMap.get("objectList");
         HashMap requestMap          = (HashMap) programMap.get("requestMap");
         int iObjectListSize         = objectList.size();
         Map dataMap                 = null;
         String isEditValue          = DomainConstants.EMPTY_STRING;
         String strStockToMatrial    = "";
         String strWOToMaterial      = "";
         String strMCToMaterial      = "";
         for (int i = 0; i < iObjectListSize; i++) {
             dataMap                = (Map) objectList.get(i);
             vColumn.add((String)dataMap.get((String)setting.get("KEY")))   ;
         }
		
	} catch(Exception e) {
		e.printStackTrace();
	}
	 
	return vColumn; 
	 
 }
 
 public HashMap removeConnectedMC(Context context , String strItemOID , String strABSId )
 { 
     HashMap returnMap    =   new HashMap();
     try {
             StringList slBusSelect           = new StringList();
             slBusSelect.add(DomainConstants.SELECT_ID);
             slBusSelect.add(DomainConstants.SELECT_NAME);
             DomainObject domBOQ              = DomainObject.newInstance(context , strItemOID);
             String strWherer                 = "from["+RELATIONSHIP_WMS_MATERIAL_CONSUMPTION_TO_AMB+"].to.id=="+strABSId;
             MapList mlMaterialCon            = domBOQ.getRelatedObjects(context, // matrix context
            		 RELATIONSHIP_WMS_ITEM_MATERIAL_CONSUMPTION, // relationship pattern
                                                                             TYPE_WMS_MATERIAL_CONSUMPTION, // type pattern
                                                                             slBusSelect, // object selects
                                                                             null, // relationship selects
                                                                             false, // to direction
                                                                             true, // from direction
                                                                             (short) 1, // recursion level
                                                                             strWherer , // object where clause
                                                                             DomainConstants.EMPTY_STRING, // relationship where clause
                                                                        0);
             Map dataMap               = null;
             String strMCID            = DomainConstants.EMPTY_STRING;
             int iListSize             = mlMaterialCon.size();
             String [] strIdArrays     = new String [iListSize]   ;
             
             for (int i = 0; i < iListSize ; i++) {
                 dataMap   = (Map) mlMaterialCon.get(i);
                 strMCID   = (String) dataMap.get(DomainConstants.SELECT_ID);
                 strIdArrays[i]=strMCID;
             }
             DomainObject.deleteObjects(context, strIdArrays);
       } catch (Exception e) {
          e.printStackTrace();
       }
     return returnMap;
 }
 
}