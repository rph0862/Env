/** Name of the JPO    : WMSImport
 ** Description        : The purpose of this JPO is to use import classified items and projects
 ** Revision Log:
 ** -----------------------------------------------------------------
 ** Author                    Modified Date                History
 ** -----------------------------------------------------------------

 ** -----------------------------------------------------------------
 **/
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;


/**
 * The purpose of this JPO is to use import classified items and projects
 *
 * @version R417 - Copyright (c) 1993-2016 Dassault Systems.
 */

public class WMSImport_mxJPO  extends WMSConstants_mxJPO {
    private SimpleDateFormat MATRIX_DATE_FORMAT = null;
  
    /**
     * Default Constructor.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return noting,constructor
     * @throws Exception
     *             if the operation fails
     * @since R417
     */
    public WMSImport_mxJPO(Context context, String[] args)
            throws Exception {
    	 super(context,args);
        MATRIX_DATE_FORMAT     = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
    }

    String[] saSubjectKeys = {DomainConstants.EMPTY_STRING};
    String[] saSubjectValues ={DomainConstants.EMPTY_STRING};
    String[] saMessageKeys ={DomainConstants.EMPTY_STRING};
    String[] saMessageValues ={DomainConstants.EMPTY_STRING};

    /**
     * Main entry point.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     *
     * @since R417
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }
    /**
     * importClassifiedItems - creates classified items inside a class 
     * Used in the process page wmsImportClassifiedItemsImportProcess.jsp from command WMSImportClassifiedItems
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - Selected Class Id
     *        2 - ownerfieldId - Selected Owner Name
     *        3 - txtImmportClassifiedItemsVault - Selected vault Name
     *        4 - file - imported file name
     * @throws Exception if the operation fails
     * @since R417 ////////////////USED
     */
    public StringList importClassifiedItems(Context context, String[] args) throws Exception
    {
        StringList strListLog = new StringList(1);
        try {
            ContextUtil.startTransaction(context, true);
            String strLanguage = context.getSession().getLanguage();
            MapList mapListMappingInfo = getAllowedTypeInfo(context, strLanguage);
            if(!(mapListMappingInfo!=null && mapListMappingInfo.size()>0))
            {
                return strListLog;
            }
            if (args == null || args.length < 7) {
                throw new Exception("Illegal Argument Exception: Missing inputs to process import SOR classified items");
            }
            String strSORLibOID = args[0];
            DomainObject domObjSORLib = DomainObject.newInstance(context, strSORLibOID);
            String strFileName = args[3];
            String strWorkspacePath = context.createWorkspace();
            String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
            FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
            XSSFWorkbook workbook  = new XSSFWorkbook(fileIS);
            int intSheetCount = workbook.getNumberOfSheets();
            Map mValidationsInfo = validateImportClassifiedItemsFileData(context, args, workbook, intSheetCount);
            String sAction = (String) mValidationsInfo.get("action");
            if ("continue".equalsIgnoreCase(sAction))
                mValidationsInfo = importClassifiedItems(context, args, workbook, intSheetCount);
            boolean hasErrors = false;
            MapList mlValidationsInfo = (MapList)mValidationsInfo.get("ValidationsInfo");
            for (int i=0; i<mlValidationsInfo.size(); i++)
            {
                Map mSheetValidationsInfo = (Map)mlValidationsInfo.get(i);
                StringList errorList = (StringList)mSheetValidationsInfo.get("SheetStatusInfo");
                int iStatusCellIndex = (Integer)mSheetValidationsInfo.get("statusSheetIndex");
                StringList slErrorList = updateFileWithErrorMsg(context,strWorkspacePath,strFileName,errorList, i, iStatusCellIndex);
                //strListLog.addAll(slErrorList);
                if(slErrorList.size() > 0)
                {
                    hasErrors = true;
					strListLog.add("Message:InvalidFileFormatClassifiedItems");
                }
            }
            if(hasErrors)
            {
                ContextUtil.abortTransaction(context);
                String  strDocumentId = FrameworkUtil.autoName(context, CommonDocument.SYMBOLIC_type_Document, CommonDocument.SYMBOLIC_policy_Document);
                domObjSORLib.connect(context,new RelationshipType(DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT),true, DomainObject.newInstance(context,strDocumentId));
                checkInFile(context, strDocumentId, strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
            }
            else
            {
                ContextUtil.commitTransaction(context);
                String  strDocumentId = FrameworkUtil.autoName(context, CommonDocument.SYMBOLIC_type_Document, CommonDocument.SYMBOLIC_policy_Document);
                domObjSORLib.connect(context,new RelationshipType(DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT),true, DomainObject.newInstance(context,strDocumentId));
                checkInFile(context, strDocumentId, strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
            }
        }
        catch(Exception exception)
        {
            ContextUtil.abortTransaction(context);
            strListLog.add("Message:Error processing import classified Items "+exception.getMessage());
        }
        return strListLog;
    }

    public Map validateImportClassifiedItemsFileData(Context context, String[] args, XSSFWorkbook workbook, int intSheetCount) throws Exception
    {
        MapList mlValidationsInfo = new MapList();
        StringList strListLog = new StringList(1);
        Map mValidationsInfo = new HashMap();
        mValidationsInfo.put("action", "continue");
        String sDefaultHeader = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.DefaultHeader");
        String sMandatorySORColumnNames = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.MandatoryColumnNames");
        String sNumericColumnNames = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.NumericColumnNames");
        StringList slValidationsInfo = new StringList();
        for(int i=0; i<intSheetCount; i++)
        {
            int iStatusCellIndex = 1;
            MapList mlSheetValidationsInfo = new MapList();
            Map mSheetValidationsInfo = new HashMap();
            XSSFSheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.iterator();
            if(!rowIterator.hasNext())
                continue;
            // Validate Chapter Data
            int iStatus = validateForMandatoryChapterData(rowIterator, slValidationsInfo);
            // Validate Headrer Row
            // Get Header Row Data
            StringList strListHeaderValues = getHeaderValues(rowIterator);
            if (iStatusCellIndex < strListHeaderValues.size())
                iStatusCellIndex = strListHeaderValues.size();
            if (iStatus == 0)
                iStatus = validateForSORDataColumnHeader(strListHeaderValues, sDefaultHeader, slValidationsInfo);

            // Validate SOR Item Data
            if (iStatus == 0)
                iStatus = validateSORColumnData(rowIterator, workbook, strListHeaderValues, sMandatorySORColumnNames, sNumericColumnNames, slValidationsInfo);

            if (iStatus == 1)
                mValidationsInfo.put("action", "stop");

            mSheetValidationsInfo.put("statusSheetIndex", iStatusCellIndex);
            mSheetValidationsInfo.put("SheetStatusInfo", slValidationsInfo);
            mlValidationsInfo.add(mSheetValidationsInfo);
            strListLog.addAll(slValidationsInfo);
        }
        mValidationsInfo.put("ValidationsInfo", mlValidationsInfo);
        mValidationsInfo.put("ValidationsLogList", strListLog);
        return mValidationsInfo;
    }

    private int validateSORColumnData(Iterator<Row> rowIterator, XSSFWorkbook workbook, StringList strListHeaderValues, String sMandatorySORColumnNames, String sNumericColumnNames, StringList slValidationsInfo) throws Exception
    {
        int iStatus = 0;
        MapList mapListFileData = getDataFromFile(strListHeaderValues, rowIterator, workbook);
        Iterator<Map<String,String>> iterator = mapListFileData.iterator();
        //String sMandatorySORColumnNames = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.MandatoryColumnNames");
        StringList slMandatorySORColumnNames = FrameworkUtil.split(sMandatorySORColumnNames, ",");
        //String sNumericColumnNames = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.NumericColumnNames");
        StringList slNumericColumnNames = FrameworkUtil.split(sNumericColumnNames, ",");
        while(iterator.hasNext())
        {
            Map<String,String> mapImportObjectInfo = iterator.next();
            String sStatusMessage = "";
            String sFailedMessage = validateForSORMandatoryData(mapImportObjectInfo, slMandatorySORColumnNames);
            if (UIUtil.isNotNullAndNotEmpty(sFailedMessage)) {
                iStatus = 1;
                sStatusMessage = sFailedMessage;
            }
            sFailedMessage = validateSORRateDataForNumeric(mapImportObjectInfo, slNumericColumnNames);
            if (UIUtil.isNotNullAndNotEmpty(sFailedMessage)) {
                iStatus = 1;
                if ("".equals(sStatusMessage))
                    sStatusMessage = sFailedMessage;
                else
                    sStatusMessage = sStatusMessage + "\n" + sFailedMessage;
            }
            if ("".equals(sStatusMessage))
                sStatusMessage = "O.K.";

            slValidationsInfo.add(sStatusMessage);
        }
        return iStatus;
    }

    private int validateForSORDataColumnHeader(StringList strListHeaderValues, String sDefaultHeader, StringList slValidationsInfo) throws Exception
    {
        int iStatus = 0;
        String sMessage = "Status";
        // Get Default Header
        //String sDefaultHeader = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type_WMSSOR.DefaultHeader");
        StringList slDefaultHeader = FrameworkUtil.split(sDefaultHeader, ",");
        String sMissingColumns = "";
        Iterator<String> itr  = slDefaultHeader.iterator();
        while (itr.hasNext())
        {
            String sHeader = itr.next();
            if(!strListHeaderValues.contains(sHeader))
            {
                if ("".equals(sMissingColumns))
                    sMissingColumns = sHeader;
                else
                    sMissingColumns = sMissingColumns + ", "+ sHeader;
            }
        }
        if (!"".equals(sMissingColumns)) {
            sMessage = "Message:Invalid Header. Following Header columns "+sMissingColumns+" missing.";
            iStatus = 1;
        }
        slValidationsInfo.add(sMessage);
        return iStatus;
    }

    private String validateForSORMandatoryData(Map<String,String> mapImportObjectInfo, StringList slMandatorySORColumnNames) throws Exception
    {
        String sFailedMessage = "";
        String sFailedColumnNames = "";
        Iterator<String> iterator = slMandatorySORColumnNames.iterator();
        while(iterator.hasNext())
        {
            String sMandatoryColumnName = iterator.next();
            String sMandatoryColumnValue = mapImportObjectInfo.get(sMandatoryColumnName);
            if (UIUtil.isNullOrEmpty(sMandatoryColumnValue)) {
                if ("".equals(sFailedColumnNames))
                    sFailedColumnNames = sMandatoryColumnName;
                else
                    sFailedColumnNames = sFailedColumnNames + ", " + sMandatoryColumnName;
            }
        }
        if (!"".equals(sFailedColumnNames))
            sFailedMessage = "Missing values for the following mandatory columns:"+sFailedColumnNames;

        return sFailedMessage;
    }

    private String validateSORRateDataForNumeric(Map<String,String> mapImportObjectInfo, StringList slNumericColumnNames) throws Exception
    {
        String sFailedMessage = "";
        String sFailedColumnNames = "";
        Iterator<String> iterator = slNumericColumnNames.iterator();
        while(iterator.hasNext())
        {
            String sNumericColumnName = iterator.next();
            String sNumericColumnValue = mapImportObjectInfo.get(sNumericColumnName);
            if(UIUtil.isNotNullAndNotEmpty(sNumericColumnValue))
            {
                if (!WMSUtil_mxJPO.isNumeric(sNumericColumnValue)) {
                    if ("".equals(sFailedColumnNames))
                        sFailedColumnNames = sNumericColumnName;
                    else
                        sFailedColumnNames = sFailedColumnNames + ", " + sNumericColumnName;
                }
            }
        }
        if (!"".equals(sFailedColumnNames))
            sFailedMessage = "Values for the following columns should be numeric:"+sFailedColumnNames;

        return sFailedMessage;
    }

    private int validateForMandatoryChapterData(Iterator<Row> rowIterator, StringList slValidationsInfo) throws Exception
    {
        int iStatus = 0;
        String sMessage = "O.K.";
        String strChapterName = "";
        Row chapterRowData = (Row)rowIterator.next();
        Iterator<Cell> cellIterator = chapterRowData.cellIterator();
        if(cellIterator.hasNext())
        {
            Cell cell = cellIterator.next();
            strChapterName = cell.getStringCellValue().trim();
        }
        if ("".equals(strChapterName)) {
            sMessage = "Message:Missing Chapter data";
            iStatus = 1;
        }
        slValidationsInfo.add(sMessage);
        return iStatus;
    }

    public Map importClassifiedItems(Context context, String[] args, XSSFWorkbook workbook, int intSheetCount) throws Exception
    {
        MapList mlValidationsInfo = new MapList();
        Map mValidationsInfo = new HashMap();
        mValidationsInfo.put("action", "continue");
        StringList strListLog = new StringList(1);
        String strSORLibOID = args[0];
        MapList mapListImportObjectInfo = new MapList();
        String strFileName = args[3];
        String strLanguage = context.getSession().getLanguage();
        String strWorkspacePath = context.createWorkspace();
        MapList mapListMappingInfo = getAllowedTypeInfo(context, strLanguage);
        StringList strListHeaderValues = new StringList();
        if(mapListMappingInfo!=null && mapListMappingInfo.size()>0)
        {
            DomainObject domObjSORLib = DomainObject.newInstance(context, strSORLibOID);

            MapList mapListSORChapters = getConnectedChapters(context, domObjSORLib);
            for(int i=0;i<intSheetCount;i++)
            {
                MapList mlSheetValidationsInfo = new MapList();
                Map mSheetValidationsInfo = new HashMap();
                StringList strSheetListLog = new StringList(1);
                XSSFSheet sheet = workbook.getSheetAt(i);
                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                Row rowHeader = (Row)rowIterator.next();
                    
                Iterator<Cell> cellIterator = rowHeader.cellIterator();
                String strChapterName = DomainConstants.EMPTY_STRING;
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    strChapterName =cell.getStringCellValue().trim();
                    if (strChapterName.length() != 0) {
                        break;
                    }
                }
                if(UIUtil.isNotNullAndNotEmpty(strChapterName))
                {
                    strListHeaderValues = getHeaderValues(rowIterator);
                    mSheetValidationsInfo.put("statusSheetIndex", strListHeaderValues.size());
                    String strChapterOID = "";
                    try {
                        strChapterOID = getChapterOID(context, domObjSORLib, mapListSORChapters,strChapterName);
                        strSheetListLog.add("O.K.");
                    }
                    catch(Exception e)
                    {
                        strSheetListLog.add("Message:Error creating chapter "+e.getMessage());
                        continue;
                    }
                    args[0] = strChapterOID;
                    if(strListHeaderValues.size()>0)
                    {
                        strSheetListLog.add("Status");
                        MapList mapListFileData = getDataFromFile(strListHeaderValues, rowIterator, workbook);
                        formatRateData(context, mapListFileData);
                        Map<String, String> mapAllowedTypeInfo = getMappingInfoFromData(mapListMappingInfo, mapListFileData);
                        if(mapAllowedTypeInfo!=null && !mapAllowedTypeInfo.isEmpty())
                        {
                            String strPolicy = getPolicyFromInfoMap(context, mapAllowedTypeInfo);
                            mapAllowedTypeInfo.put(DomainConstants.SELECT_POLICY, strPolicy);
                            if(UIUtil.isNotNullAndNotEmpty(strPolicy))
                            {
                                clearBasicsInHeader(strListHeaderValues);                                
                                mapListImportObjectInfo.add(mapAllowedTypeInfo);
                                StringList strSheetListDataLog = createClassifiedItemsFromFileData(context,
                                                                                args, mapListFileData,
                                                                                mapAllowedTypeInfo,strListHeaderValues, mapListImportObjectInfo);
                                strSheetListLog.addAll(strSheetListDataLog);
                            }
                        }
                    }
                }
                mSheetValidationsInfo.put("SheetStatusInfo", strSheetListLog);
                mlValidationsInfo.add(mSheetValidationsInfo);
                strListLog.addAll(strSheetListLog);
            }
            mValidationsInfo.put("ValidationsInfo", mlValidationsInfo);
            mValidationsInfo.put("StatusLogList", strListLog);
        }
        //Added to connect a Job with the Library and to displayed under "BackGround Jobs" command
        if(args.length>=6){
            new Job(args[6]).connectFrom(context, LibraryCentralConstants.RELATIONSHIP_JOBS, new DomainObject(strSORLibOID));
        }
        //Added to checkin imported document file to document object and it should be connected to the Library and displayed under "Reference Document" command 
        //${CLASS:WMSMBAppIntegrationBase}.checkinDocument(context, strWorkspacePath, strFileName, new DomainObject(strSORLibOID));
        return mValidationsInfo;
    }

    private void formatRateData(Context context, MapList mapListFileData) throws Exception
    {
        Iterator<Map<String,String>> iterator = mapListFileData.iterator();
        while(iterator.hasNext())
        {
            Map<String,String> mapImportObjectInfo = iterator.next();
            String sRateValue = mapImportObjectInfo.get("Rate");
            if(UIUtil.isNotNullAndNotEmpty(sRateValue))
            {
                DecimalFormat df = new DecimalFormat("##.##");
                sRateValue = ""+df.format(Double.parseDouble(sRateValue));
                mapImportObjectInfo.put("Rate", sRateValue);
            }
        }
    }

    /**
     * Method to get chapter OID from the Parent SOR Library OID
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domObjSORLib DomainObject instance of context SOR library OID
     * @param mapListSORChapters MapList connected Chapter of the SOR Library
     * @param strChapterName String value containing the SOR Chapter name provide din the excel file
     * @throws FrameworkException if the operation fails
     * @since R417 /////////////////USED
     */
    private String getChapterOID(Context context, DomainObject domObjSORLib, MapList mapListSORChapters,
            String strChapterName) throws FrameworkException {
        try
        {
            Map<String,String> mapChapter = WMSUtil_mxJPO.getMap(mapListSORChapters, DomainConstants.SELECT_NAME, strChapterName);

            String strChapterOID = mapChapter.get(DomainConstants.SELECT_ID);
            if(UIUtil.isNullOrEmpty(strChapterOID))
            {
                DomainObject domObjSORChapter = DomainObject.newInstance(context);
                domObjSORChapter.createAndConnect(context,
                		TYPE_WMS_SOR_CHAPTER,
                        strChapterName,
                        domObjSORLib.getUniqueName(context),
                        DomainConstants.POLICY_CLASSIFICATION,
                        null,
                        LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
                        domObjSORLib,
                        true);
                strChapterOID = domObjSORChapter.getInfo(context, DomainConstants.SELECT_ID);
                //Added to set Title on Chapter
                domObjSORChapter.setAttributeValue(context,"Title", strChapterName);
            }
            return strChapterOID;
        }
        catch(FrameworkException frameworkException)
        {
            frameworkException.printStackTrace();
            throw frameworkException;
        }
    }
    /**
     * Method to get connected  chapters from the Parent SOR Library OID
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domObjSORLib DomainObject instance of context SOR library OID
     * @throws FrameworkException if the operation fails
     * @since R417 ////////////////USED
     */ 
    private MapList getConnectedChapters(Context context, DomainObject domObjSORLib) throws FrameworkException {
        try
        {
            SelectList selListBusSelects     = new SelectList(2);
            selListBusSelects.add(DomainConstants.SELECT_ID);
            selListBusSelects.add(DomainConstants.SELECT_NAME);

            MapList mapListSORChapters = domObjSORLib.getRelatedObjects(context, // matrix context
                    LibraryCentralConstants.RELATIONSHIP_SUBCLASS, // relationship pattern
                    TYPE_WMS_SOR_CHAPTER, // type pattern
                    selListBusSelects, // object selects
                    null, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    DomainConstants.EMPTY_STRING, // object where clause
                    DomainConstants.EMPTY_STRING, // relationship where clause
                    0);
            return mapListSORChapters;
        }
        catch(FrameworkException frameworkException)
        {
            frameworkException.printStackTrace();
            throw frameworkException;
        }
    }
    /**
     *
     * Use to remove Name , Type and Description from StringList derived from header row of input file
     * @param strListHeaderValues StringList containing header values
     * @since R417 /////////////////USED 
     */
    private void clearBasicsInHeader(StringList strListHeaderValues) {

        strListHeaderValues.remove(DomainConstants.SELECT_NAME);
        strListHeaderValues.remove(DomainConstants.SELECT_DESCRIPTION);
        strListHeaderValues.remove(DomainConstants.SELECT_TYPE);
        strListHeaderValues.remove(DomainConstants.SELECT_POLICY);
        strListHeaderValues.remove(DomainConstants.SELECT_CURRENT);
        strListHeaderValues.remove("State");
    }
    /**
     *
     * Method to create Classified item and connect to Class
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - Selected Class Id
     *        2 - ownerfieldId - Selected Owner Name
     *        3 - txtImmportClassifiedItemsVault - Selected vault Name
     *        4 - file - imported file name
     * @param mapListFileData contains import file data.Where each single row from file is map where header's cell value becomes key and corresponding column value becomes value
     * @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     * @param strListHeaderValues header values mentioned in import file
     * @return strListLog log containing the list of status of each row imported from the file 
     * @throws Exception if the operation fails
     * @since R417 WMS ///////////////USED
     */
    private StringList createClassifiedItemsFromFileData(Context context,
            String[] args, MapList mapListFileData,
            Map<String, String> mapAllowedTypeInfo,StringList strListHeaderValues,MapList mapListImportObjectInfo) throws FrameworkException,
    Exception {
        StringList strListLog = new StringList(mapListFileData.size());
        try
        {
            String strKey = DomainConstants.EMPTY_STRING;
            for ( Map.Entry<String, String> entry : mapAllowedTypeInfo.entrySet()) 
            {
                String strValue  = entry.getValue();
                if(strValue.equals(ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER))
               {
                    strKey =  entry.getKey();
               }
           }
            DomainObject domObjNewClassifiedItem;
            Map<String,String> mapDimensions         = new HashMap();

            String strType                            = mapAllowedTypeInfo.get(DomainConstants.SELECT_TYPE);
            String strTypePolicy                     = mapAllowedTypeInfo.get(DomainConstants.SELECT_POLICY);
            String strRevision                         = getDefaultRevison(context, strTypePolicy);

            mapAllowedTypeInfo.put(DomainConstants.SELECT_REVISION, strRevision);            
            DomainObject domObjClass         = DomainObject.newInstance(context,args[0]);
            MapList mapListObjects             = getExistingSORs( context,args, mapAllowedTypeInfo);            
            int intSize                     = mapListObjects.size();
            StringList strListExisitngNames = new StringList();
            StringList strListExisitngOIDs = new StringList();
            if(intSize>0)
            {
                Iterator iteratorTemp = mapListObjects.iterator();
                while(iteratorTemp.hasNext())
                {
                    Map<String,String> mapObject = (Map<String,String>)iteratorTemp.next();
                    String strName = mapObject.get("attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]");
                    String strOID = mapObject.get(DomainConstants.SELECT_ID);
                    strListExisitngNames.add(strName);
                    strListExisitngOIDs.add(strOID);
                }
            }

            Iterator<Map<String,String>> iterator = mapListFileData.iterator();
            while(iterator.hasNext())
            {
                Map<String,String> mapImportObjectInfo= new HashMap<String, String>();
                String strMessage = DomainConstants.EMPTY_STRING;
                Map<String,String> mapFileData = iterator.next();
                String strItemCode = mapFileData.get(strKey);
                //SOR import will work only for first time import. So revisioning is not to be handled with Import. Specifically in MSIL case as no revisioning concept is required.
                if(UIUtil.isNotNullAndNotEmpty(strItemCode))
                {
                    try {
                        if(strListExisitngNames.contains(strItemCode))
                        {
                            int index = strListExisitngNames.indexOf(strItemCode);
                            String strClassifiedItemOID = strListExisitngOIDs.get(index);
                            domObjNewClassifiedItem = DomainObject.newInstance(context, strClassifiedItemOID);
                            domObjNewClassifiedItem.setDescription(context, mapFileData.get(DomainConstants.SELECT_DESCRIPTION));
                            Map<String,String> mapAttribute =  getAttributeMap(mapAllowedTypeInfo, strListHeaderValues, mapFileData);    
                            domObjNewClassifiedItem.setAttributeValues(context, mapAttribute);
                        }
                        else
                        {
                            domObjNewClassifiedItem = createClassifiedItemsFromFileData(
                                                                                        context, args, mapAllowedTypeInfo,
                                                                                        strListHeaderValues,  domObjClass,
                                                                                        mapFileData);
                            String strClassifiedItemOID = domObjNewClassifiedItem.getInfo(context, DomainConstants.SELECT_ID);
                            strListExisitngNames.add(strItemCode);
                            strListExisitngOIDs.add(strClassifiedItemOID);
                            domObjNewClassifiedItem.promote(context);
                        }
                        strListLog.add("O.K.");
                    }
                    catch(Exception e)
                    {
                        strListLog.add("Message:Error importing SOR "+e.getMessage());
                    }
                }

                mapImportObjectInfo.put("Message",strMessage);
                mapListImportObjectInfo.add(mapImportObjectInfo);
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            strListLog.add("Message:Error importing SOR "+exception.getMessage());
        }
        return strListLog;
    }

    /**
     *
     * Method to create Classified item and connect to Class
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - Selected Class Id
     *        2 - ownerfieldId - Selected Owner Name
     *        3 - txtImmportClassifiedItemsVault - Selected vault Name
     *        4 - file - imported file name
     * @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     * @param strListHeaderValues header values mentioned in import file
     * @param domObjClass General Class domain to which classified item to be class
     * @param mapFileData contains import file data single row data
     * @return DomainObject of newly created Classified item
     *
     * @throws Exception 
     * @since R417 ////////////////////USED
     */
    private DomainObject createClassifiedItemsFromFileData(Context context,
            String[] args, Map<String, String> mapAllowedTypeInfo,
            StringList strListHeaderValues, 
            DomainObject domObjClass, Map<String, String> mapFileData)
                    throws Exception {
        DomainObject domObjNewClassifiedItem= DomainObject.newInstance(context);
        try
        {

            String strType             = mapAllowedTypeInfo.get(DomainConstants.SELECT_TYPE);
            String strTypePolicy     = mapAllowedTypeInfo.get(DomainConstants.SELECT_POLICY);
            String strRevision1     = mapAllowedTypeInfo.get(DomainConstants.SELECT_REVISION);
            String strTempPolicy     = mapFileData.get(DomainConstants.SELECT_POLICY);
            if(UIUtil.isNotNullAndNotEmpty(strTempPolicy))
            {
                strTypePolicy = strTempPolicy;
            }
            String strObjName = mapFileData.get(DomainConstants.SELECT_NAME);
            if(UIUtil.isNullOrEmpty(strObjName))
            {
                //TODO Use constant file entries
                strObjName = FrameworkUtil.autoName(context, "type_WMSSOR", DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, true,
                        false);                    
            }    
            domObjNewClassifiedItem.createAndConnect(context, strType, strObjName, strRevision1,
                    strTypePolicy, args[2], LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, domObjClass, true);
            domObjNewClassifiedItem.setDescription(context, mapFileData.get(DomainConstants.SELECT_DESCRIPTION));
            domObjNewClassifiedItem.setOwner(context, args[1]);
            Map<String,String> mapAttribute =  getAttributeMap(mapAllowedTypeInfo, strListHeaderValues, mapFileData);    
            domObjNewClassifiedItem.setAttributeValues(context, mapAttribute);                            
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
        return domObjNewClassifiedItem;
    }
    /**
     *
     * generate attribute from the import file for a single row
     * @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     * @param strListHeaderValues header values mentioned in import file
     * @param mapFileData contains import file data single row data
     *
     * @since R417 /////////////////USED
     */
    private Map<String, String> getAttributeMap(Map<String, String> mapAllowedTypeInfo,
            StringList strListHeaderValues, 
            Map<String, String> mapFileData) {
        Map<String, String> mapAttribute                         = new HashMap(strListHeaderValues.size());
        Iterator<String> iteratorHeader =  strListHeaderValues.iterator();
        while(iteratorHeader.hasNext())
        {
            String strHeader             = iteratorHeader.next();
            String strAttributeName     = mapAllowedTypeInfo.get(strHeader);
            if(UIUtil.isNotNullAndNotEmpty(strAttributeName))
            {
                mapAttribute.put(strAttributeName, mapFileData.get(strHeader));
            }
        }

        return mapAttribute;
    }
    /**
     *
     * Method to create Classified item and connect to Class
     * @param context the eMatrix <code>Context</code> object
     * @param strTypePolicy policy name for which default revision to be generated
     * @return strRevision containing default revision based on policy
     * @throws FrameworkException if the operation fails
     *
     * @since R417 ///////////////USED
     */
    private String getDefaultRevison(Context context, String strTypePolicy)
            throws FrameworkException {
        DomainObject domObjTemp            = DomainObject.newInstance(context);
        String strRevision                 = domObjTemp.getDefaultRevision(context, strTypePolicy);

        return strRevision;
    }
    /**
     *
     * Method to get the classified item based on mapAllowedTypeInfo data in the system 
     * @param context the eMatrix <code>Context</code> object
     * @param strVault vault from which data to be retrieved
     * @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     * @return strListExisitngNames list of Classified items names
     * @throws FrameworkException if the operation fails
     *
     * @since R417 ///////////////USED
     */
    private MapList getExistingSORs(Context context,
            String[] args, Map<String, String> mapAllowedTypeInfo) throws FrameworkException
    {
        try
        {
            MapList mapListObjects = new MapList();
            String strChapterOID = args[0];
            if(UIUtil.isNotNullAndNotEmpty(strChapterOID))
            {
                DomainObject domObjSORChapter = DomainObject.newInstance(context, strChapterOID);
                StringList strListBusSelects = new StringList(2);
                strListBusSelects.add(DomainConstants.SELECT_ID);
                strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]");
                String strWhere = "(relationship["+LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM+"]==True)";
                strWhere += "&&(revision==last)";
                mapListObjects = domObjSORChapter.getRelatedObjects(context, // matrix context
                                                                    LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, // relationship pattern
                                                                    TYPE_WMS_SOR, // type pattern
                                                                    strListBusSelects, // object selects
                                                                    null, // relationship selects
                                                                    false, // to direction
                                                                    true, // from direction
                                                                    (short) 1, // recursion level
                                                                    strWhere, // object where clause
                                                                    DomainConstants.EMPTY_STRING, // relationship where clause
                                                                    0);
            }
            return mapListObjects;
        }
        catch(FrameworkException frameworkException)
        {
            frameworkException.printStackTrace();
            throw frameworkException;
        }
    }
    /**
     *
     * Method to get the corresponding policy based on type form mapAllowedTypeInfo
     * @param context the eMatrix <code>Context</code> object
     * @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     * @return string value of the Policy 
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since R417 WMS //////////////USED
     */
    private String getPolicyFromInfoMap(Context context,
            Map<String, String> mapAllowedTypeInfo) throws FrameworkException,
            MatrixException {
        String strPolicy = DomainConstants.EMPTY_STRING;

        String strType = mapAllowedTypeInfo.get(DomainConstants.SELECT_TYPE);
        strType = strType.trim();
        Map defaultMap = mxType.getDefaultPolicy(context, strType, false);
        strPolicy = (String)defaultMap.get("name");

        if(UIUtil.isNullOrEmpty(strPolicy))
        {
            MapList policyList = mxType.getPolicies(context,strType,false);
            if(policyList!= null && policyList.size()>0)
            {
                strPolicy = (String)((Map)policyList.get(0)).get("name");
            }
        }
        return strPolicy;
    }
    /**
     *
     * Method to get the corresponding Type info based on type mentioned in the first row of import file
     * @param mapListMappingInfo a list of map containing symbolic , schema and i18n names of the types mentioned in property emxLibraryCentral.ImportClassifiedItem.type
     * @param mapListFileData contains import file data. Where each single row from file is map where header's cell value becomes key and corresponding column value becomes value
     * @return mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
     *
     * @since R417 WMS /////////////////USED
     */
    private Map<String, String> getMappingInfoFromData(
            MapList mapListMappingInfo, MapList mapListFileData) {
        Map<String,String> mapAllowedTypeInfo = new HashMap();
        if(mapListFileData!=null && mapListFileData.size()>0)
        {
            Map<String,String> mapData = (Map<String,String>)mapListFileData.get(0);
            String strDataType = mapData.get("Type");
            Iterator iterator = mapListMappingInfo.iterator();
                while(iterator.hasNext())
                {
                    Map<String,String> mapInfo = (Map<String,String>)iterator.next();                    
                    mapAllowedTypeInfo.putAll(mapInfo);
                }
            
        }
        return mapAllowedTypeInfo;
    }
    /**
     *
     * Method to get symbolic , schema and i18n names of the types mentioned in property emxLibraryCentral.ImportClassifiedItem.type
     * @param context the eMatrix <code>Context</code> object
     * @param strLanguage context language 
     * @return mapListMappingInfo a list of map containing symbolic , schema and i18n names of the types mentioned in property emxLibraryCentral.ImportClassifiedItem.type
     * @throws FrameworkException if the operation fails
     * @throws Exception if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since R417 WMS /////////////////USED
     */
    private MapList getAllowedTypeInfo(Context context, String strLanguage)
            throws FrameworkException, Exception, MatrixException {
        StringList strListAllowedTypes = getAllowedTypes(context,strLanguage);
        MapList mapListMappingInfo = new MapList(strListAllowedTypes.size());
        
        Iterator iterator  = strListAllowedTypes.iterator();
        while(iterator.hasNext())
        {
            String strAllowedType = (String) iterator.next();
            strAllowedType = strAllowedType.trim();
            HashMap<String, String> mapInfo = getAllowedTypeInfo(
                    context, strAllowedType, strLanguage);            
            HashMap<String,String> mapMapping = getAllowedTypeAttributeMappingFromProperties(context,
                    strAllowedType, strLanguage,DomainConstants.EMPTY_STRING);
            mapInfo.putAll(mapMapping);

            mapListMappingInfo.add(mapInfo);
        }
        return mapListMappingInfo;
    }
    /**
     *
     * Method to get the import file data into mapList
     * @param strListHeaderValues header values mentioned in import file
     * @param rowIterator which starts from the second row of the import file
     * @return mapListFileData contains import file data. Where each single row from file is map where header's cell value becomes key and corresponding column value becomes value
     *
     * @since R417 WMS /////////////////USED
     */
    private MapList getDataFromFile(StringList strListHeaderValues,
            Iterator<Row> rowIterator,XSSFWorkbook workbook) {
        MapList mapListFileData = new MapList();
        DataFormatter formatter = new DataFormatter();
        while(rowIterator.hasNext())
        {
            Row rowData = (Row)rowIterator.next();
            HashMap<String, String> mapRowData = new HashMap(strListHeaderValues.size());
            Iterator<Cell> cellIterator = rowData.cellIterator();
            XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                String strCellvalue = DomainConstants.EMPTY_STRING;                
                switch (cell.getCellType())
                {
                case Cell.CELL_TYPE_FORMULA:
                {
                    switch(evaluator.evaluateInCell(cell).getCellType())
                    {
                    case Cell.CELL_TYPE_NUMERIC:
                        double doubleValue =cell.getNumericCellValue();
                        strCellvalue = String.valueOf(doubleValue);
                        if(UIUtil.isNullOrEmpty(strCellvalue))
                        {
                            strCellvalue = "0";
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        strCellvalue =cell.getStringCellValue().trim();                    
                        break;
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:
                    strCellvalue =cell.getStringCellValue().trim();                    
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    strCellvalue = formatter.formatCellValue(cell);
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    boolean booleanValue =cell.getBooleanCellValue();
                    strCellvalue = String.valueOf(booleanValue);
                    if(UIUtil.isNullOrEmpty(strCellvalue))
                    {
                        strCellvalue = "FALSE";
                    }
                    else
                    {                        
                        strCellvalue = String.valueOf(booleanValue);
                    }
                    break;
                }
                
                if(UIUtil.isNullOrEmpty(strCellvalue))
                {
                    strCellvalue = DomainConstants.EMPTY_STRING;
                }
                String strColumnKey = (String)strListHeaderValues.get(cell.getColumnIndex());
                mapRowData.put(strColumnKey, strCellvalue);                
            }
            mapRowData.put("RowNumber", String.valueOf( rowData.getRowNum() ) );
            mapListFileData.add(mapRowData);
        }
        return mapListFileData;
    }
 
    /**
     *
     * get the mapping between attribute and header of import file.The Mapping is derived form property emxLibraryCentral.ImportClassifiedItem.{TYPE}.ImportMappig
     * @param context the eMatrix <code>Context</code> object
     * @param strAllowedType type mentioned in the import file
     * @param strLanguage context user language
     * @return mapMapping a map where Key is header and value is attribute schema name
     * @throws FrameworkException if the operation fails
     * @throws Exception if the operation fails
     *
     * @since R417 ////////////////USED
     */
    private HashMap<String,String> getAllowedTypeAttributeMappingFromProperties(Context context,
            String strAllowedType, String strLanguage,String  strPropKey)
                    throws FrameworkException, Exception {
        String strAllowedTypeMapping  = DomainConstants.EMPTY_STRING;
        if("WMS.BOQ.type".equals(strPropKey))
        {
              strAllowedTypeMapping                = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.BOQ.ImportMappig");                   
        }
        else
        {
            strAllowedTypeMapping                = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem."+strAllowedType+".ImportMappig");
        }
       StringList strListAllowedTypeMapping = FrameworkUtil.splitString(strAllowedTypeMapping, ",");        
        int intMappingSize = strListAllowedTypeMapping.size();
        HashMap<String,String> mapMapping = new HashMap<String, String>(intMappingSize);
        Iterator iteratorMapping  = strListAllowedTypeMapping.iterator();
        while(iteratorMapping.hasNext())
        {
            String strMapping = (String) iteratorMapping.next();
            if(UIUtil.isNotNullAndNotEmpty(strMapping))
            {
                StringList strListMapping = FrameworkUtil.splitString(strMapping, "|");                
                if(strListMapping.size()==2)
                {
                    mapMapping.put((String)strListMapping.get(1), (String)strListMapping.get(0));
                }
            }
        }
        return mapMapping;
    }
    /**
     *
     * Method to get symbolic , schema and i18n names of the type 
     * @param context the eMatrix <code>Context</code> object
     * @param strAllowedType type for which information need to be retrieved 
     * @param strLanguage context user language
     * @return mapInfo a map contains symbolic , schema and i18n names of the type 
     * @throws MatrixException if the operation fails
     *
     * @since R417  ///////////////USED
     */
    private HashMap<String, String> getAllowedTypeInfo(
            Context context, String strAllowedType, String strLanguage)
                    throws MatrixException {
        HashMap<String,String> mapInfo = new HashMap<String, String>();

        mapInfo.put(DomainConstants.SELECT_TYPE, strAllowedType);
        mapInfo.put(DomainConstants.SELECT_TYPE, DomainConstants.EMPTY_STRING);
        mapInfo.put(DomainConstants.SELECT_TYPE, DomainConstants.EMPTY_STRING);
        String strAllowedTypeSchemaName     = PropertyUtil.getSchemaProperty(context, strAllowedType);
        if(UIUtil.isNotNullAndNotEmpty(strAllowedTypeSchemaName))
        {
            mapInfo.put(DomainConstants.SELECT_TYPE, strAllowedTypeSchemaName);
            String strAllowedTypeSchemaI18nName = EnoviaResourceBundle.getAdminI18NString(context,"Type", strAllowedTypeSchemaName, strLanguage);
            if(UIUtil.isNotNullAndNotEmpty(strAllowedTypeSchemaI18nName))
            {
                mapInfo.put("i18TypeName", strAllowedTypeSchemaI18nName);                
            }
            else
            {
                mapInfo.put(DomainConstants.SELECT_TYPE, strAllowedTypeSchemaName);
            }
        }
        return mapInfo;
    }
    /**
     *
     * Method to get list of types mentioned in property emxLibraryCentral.ImportClassifiedItem.type 
     * @param context the eMatrix <code>Context</code> object
     * @param strLanguage context language 
     * @return strListAllowedTypes a list of types mentioned in property emxLibraryCentral.ImportClassifiedItem.type 
     * @throws FrameworkException if the operation fails
     * @throws Exception if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since R417 /////////////////USED
     */
    private StringList getAllowedTypes(Context context, String strLanguage)
            throws FrameworkException, Exception {

        String strAllowedTypes                = EnoviaResourceBundle.getProperty(context,"WMS.ImportClassifiedItem.type");
        if(UIUtil.isNotNullAndNotEmpty(strAllowedTypes))
        {
            StringList strListAllowedTypes = FrameworkUtil.splitString(strAllowedTypes, ",");            
            return strListAllowedTypes;
        }
        else
        {
            return new StringList();
        }
    }
    /**
     *
     * Method to get the header from the first row of import file
     * @param rowIterator which starts from the first row of the import file
     * @return strListColumnHeader contains header values mentioned in the first row of the import file
     * 
     * @since R417 WMS /////////////////USED
     */
    private StringList getHeaderValues(Iterator<Row> rowIterator) {
        StringList strListColumnHeader = new StringList();
        if(rowIterator.hasNext())
        {
            Row rowHeader = (Row)rowIterator.next();
            DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
            Iterator<Cell> cellIterator = rowHeader.cellIterator();
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                String strCellvalue = DomainConstants.EMPTY_STRING;
                strCellvalue =formatter.formatCellValue(cell);
                if(UIUtil.isNullOrEmpty(strCellvalue))
                {
                    strCellvalue = DomainConstants.EMPTY_STRING;
                }
                if(strCellvalue.equalsIgnoreCase(DomainConstants.SELECT_NAME))
                {
                    strCellvalue = DomainConstants.SELECT_NAME;
                }
                if(strCellvalue.equalsIgnoreCase(DomainConstants.SELECT_DESCRIPTION))
                {
                    strCellvalue = DomainConstants.SELECT_DESCRIPTION;
                }
                if(strCellvalue.equalsIgnoreCase(DomainConstants.SELECT_POLICY))
                {
                    strCellvalue = DomainConstants.SELECT_POLICY;
                }
                if(strCellvalue.equalsIgnoreCase(DomainConstants.SELECT_CURRENT))
                {
                    strCellvalue = DomainConstants.SELECT_CURRENT;
                }
                if(strCellvalue.equalsIgnoreCase("DSR Number"))
                {
                    strCellvalue = "DSR Number";
                }
                if(strCellvalue.equalsIgnoreCase("MSIL Number"))
                {
                    strCellvalue = "MSIL Number";
                }
                if(strCellvalue.equalsIgnoreCase("Units"))
                {
                    strCellvalue = "Units";
                }
                if(strCellvalue.equalsIgnoreCase("Rate"))
                {
                    strCellvalue = "Rate";
                }
                
                
                strListColumnHeader.add(strCellvalue);
            }
        }
        return  strListColumnHeader;
    }

    /**
     * updateFileWithErrorMsg - update import File with error message
     *
     * @param context the eMatrix <code>Context</code> object
     * @param  strWorkspacePath - File path
     * @param  strFileName - File Name 
     * @param  errorList - maplist for all the item status 
     * @throws Exception if the operation fails
     * @since R417
     */
    private StringList updateFileWithErrorMsg(Context context,String strWorkspacePath,String strFileName,StringList errorList, int iSheetNumber, int iStatusCellIndex) throws Exception
    {
        StringList slErrorList = new StringList();

        String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
        FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
        //Create Workbook instance holding reference to .xls file
        XSSFWorkbook workbook  = new XSSFWorkbook(fileIS);
        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(iSheetNumber);
        Iterator<Row> rowIteratorToWrite = sheet.iterator();
        Iterator errorListItr = errorList.iterator();
        while(rowIteratorToWrite.hasNext()&&errorListItr.hasNext())
        {
            Row row = (Row)rowIteratorToWrite.next();
            String strMessage = (String)errorListItr.next();
            if(strMessage.indexOf("Message")>=0)
            {
                slErrorList.add(strMessage);
            }
            Cell cell = row.createCell(iStatusCellIndex);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue(strMessage);
        }
        FileOutputStream outputStream = new FileOutputStream(strFilePath);
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        return slErrorList;
    }
    
    /** 
     * Method will check in the file using document object
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param Document id,file name,file description,vault and folder
     * @throws Exception if the operation fails
     * @author CHiPS
     * @since 418
     */
    public static void checkInFile(Context context,
                String objectId,
                String fileName,
                String fileDescription,
                String vault,
                String folder) throws Exception
    {
        try
        {
            String masterObjectId = "";

            // this objectId could be master or version file id depending on Move Files To Version attribute
            DomainObject object = DomainObject.newInstance(context, objectId);

            // this will be true, if attribute Move Files To Version is True.
            String isVersion = object.getAttributeValue(context, CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT);

            // if this is version document, get masterdocument id and use that object as parent object for checkin
            if("true".equalsIgnoreCase(isVersion)){
                masterObjectId = object.getInfo(context, CommonDocument.SELECT_MASTER_ID);
            } else {
                masterObjectId = objectId;
            }

            CommonDocument masterObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
            masterObject.setId(masterObjectId);

            String moveFilesToVersion = masterObject.getInfo(context, CommonDocument.SELECT_MOVE_FILES_TO_VERSION);

            StringList selectList = new StringList(1);
            selectList.add(CommonDocument.SELECT_ID);

            // Lock the object
            String objectWhere = CommonDocument.SELECT_TITLE + "== '" + fileName +"'";
            MapList mlist = masterObject.getRelatedObjects(context,
                    CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                    CommonDocument.TYPE_DOCUMENTS,
                    selectList,
                    null,
                    false,
                    true,
                    (short) 1,
                    objectWhere,
                    CommonDocument.EMPTY_STRING);

            DomainObject versionObject = null;
            if (mlist != null && mlist.size() > 0 )
            {
                Map versionMap = (Map) mlist.get(0);
                versionObject = DomainObject.newInstance(context, (String) versionMap.get(CommonDocument.SELECT_ID));
                versionObject.lock(context);
            }

            // Create/revise the version object
            String versionId = masterObject.reviseVersion(context, null, fileName, new HashMap());

            if(versionObject != null)
            {
                if(versionObject.isLocked(context))
                {
                    versionObject.unlock(context);
                }
            }

            //Check the file in
            if(moveFilesToVersion != null && "false".equalsIgnoreCase(moveFilesToVersion))
            {
                object.setId(masterObjectId);
                object.checkinFile(context, true, true, "", DomainConstants.FORMAT_GENERIC , fileName, folder);
            }
            else if (moveFilesToVersion != null && "true".equalsIgnoreCase(moveFilesToVersion))
            {
                object.setId(versionId);
                object.checkinFile(context, true, true, "",  DomainConstants.FORMAT_GENERIC , fileName, folder);
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

	/**
	 * importBOQ - import BOQ Items [Segment,SOR Items and Non SOR Items] 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 -  strWOId - Selected Work Order Id
	 *        3 - file - imported file name
	 * @throws Exception if the operation fails
	 * @since R417
	 */
	public StringList importBOQ(Context context, String[] args) throws Exception
	{
		StringList strListLog = new StringList();
		MapList mapListImportObjectInfo = new MapList();
		String strWorkspacePath    = context.createWorkspace();
		String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.BOQImport.Import.DoneMessage");
		StringList slErrorList  = new StringList();
		String strNoBOQItemMessage         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.WO.NoBOQItem.Alert");
		if (args.length == 0 )
		{
			throw new IllegalArgumentException();
		}
		try {
			ContextUtil.startTransaction(context, true);
			MapList errorList = new MapList();
			String strFileName        = args[3];
			String strLanguage        = context.getSession().getLanguage();
			String strWOId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strWOId)){
				//StringList slObjSelect = new StringList("attribute[CHIPSFormType]");
				StringList slObjSelect = new StringList();
				slObjSelect.add("from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.id");   
				DomainObject domWO = DomainObject.newInstance(context, strWOId);
				String strParentId = DomainConstants.EMPTY_STRING;
				String strMBId = DomainConstants.EMPTY_STRING;
				Map mapWOInfo =  domWO.getInfo(context,slObjSelect);

				String strSegmentId = DomainConstants.EMPTY_STRING;
				String strFormType = DomainConstants.EMPTY_STRING;
				if(mapWOInfo!=null&&!mapWOInfo.isEmpty())
				{
					strMBId = (String)mapWOInfo.get("from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.id");
					//strFormType = (String)mapWOInfo.get("attribute[CHIPSFormType]");
				}
				strParentId = strMBId;
				boolean isValidFile=true;
				Map mapError = new HashMap();
				if(UIUtil.isNotNullAndNotEmpty(strFileName))
				{

					StringList strListHeaderValues     = new StringList();

					String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
					FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
					//Create Workbook instance holding reference to .xls file
					XSSFWorkbook workbook=null;
					try{
						workbook  = new XSSFWorkbook(fileIS);
					}catch(Exception e) {
						isValidFile=false;
						mapError.put("Message","Invalid File Format");
						errorList.add(mapError);
						strListLog.add("Message:InvalidFileFormatBOQ");
					}
					//Get first/desired sheet from the workbook
					if(isValidFile){
						XSSFSheet sheet = workbook.getSheetAt(0);
						//Iterate through each rows one by one
						Iterator<Row> rowIterator = sheet.iterator();
						MapList mapListMappingInfo         = getAllowedItemTypeInfo(context,
								strLanguage,"WMS.BOQ.type");
								
						StringList alreadyAddedSegment = new StringList();
						if(mapListMappingInfo!=null && mapListMappingInfo.size()>0)
						{
							strListHeaderValues = getHeaderValues(rowIterator);
							
							Map mapAttribute = new HashMap(strListHeaderValues.size());
							if(strListHeaderValues.size()>0)
							{
								/*
								${CLASS:WMSSOR} sorJPO = new ${CLASS:WMSSOR}(context,args);
								ArrayList<String> arrayListSORItemsOID = new ArrayList<String>();
								MapList mlWOSORInfo =  sorJPO.getWorkOrderSpecificSOR(context, domWO);
								String strSORId = DomainConstants.EMPTY_STRING;
								StringList strListSOROIDs = ${CLASS:WMSUtil}.convertToStringList(mlWOSORInfo, DomainConstants.SELECT_ID);
								Iterator<String> iterarorSOR = strListSOROIDs.iterator();
								while(iterarorSOR.hasNext()){
									strSORId = iterarorSOR.next();
									{
										arrayListSORItemsOID.add(strSORId);
									}
								}
								*/
								MapList mapListFileData = getDataFromFile(strListHeaderValues,
										rowIterator,workbook);
								Map<String, String> mapAllowedTypeInfo = getMappingInfoFromData(
										mapListMappingInfo, mapListFileData);
								Iterator itrFile = mapListFileData.iterator();                    
								while(itrFile.hasNext()) {                   
									Map mapFileData = (Map)itrFile.next();
									mapAttribute = getAttributeMap(mapAllowedTypeInfo, strListHeaderValues,  mapFileData);
									String strSOR     = args[5];
									String ItemCode = (String)mapFileData.get("Item Code");   
									String strUOM = (String)mapFileData.get("UOM");   
									if(UIUtil.isNullOrEmpty(strUOM)&&!alreadyAddedSegment.contains(ItemCode))
									{
										strParentId = strMBId;
										strSegmentId = FrameworkUtil.autoName(context, "type_WMSSegment", "policy_WMSMeasurementItem");
										DomainObject domSegment = DomainObject.newInstance(context,strSegmentId);
										domSegment.connect(context,new RelationshipType(RELATIONSHIP_BILL_OF_QUANTITY),false, DomainObject.newInstance(context,strParentId));
										domSegment.setAttributeValue(context,"Title", ItemCode);
										alreadyAddedSegment.add(ItemCode);
										mapError.put("Message", strDoneMessage);
										errorList.add(mapError);
										strParentId = strSegmentId;
									}
									else
									{
										if(mapAllowedTypeInfo!=null && !mapAllowedTypeInfo.isEmpty())
										{
											//BOQ policy for Measurement Items                      
											mapAllowedTypeInfo.put(DomainConstants.SELECT_POLICY, POLICY_WMS_MEASUREMENT_ITEM);
											clearBasicsInHeader(strListHeaderValues);                                
											mapListImportObjectInfo.add(mapAllowedTypeInfo);
											mapError = createBOQFromFileData(context,
													args,mapAllowedTypeInfo,
													strSOR,ItemCode, mapAttribute,
													strParentId, mapFileData);
											if(!mapError.isEmpty())
											{
												errorList.add(mapError);
											}
										}
									}
								}
							}
						}
					}
					if(errorList.size()>0)
					{
						
						slErrorList = updateFileWithErrorMsg(context,strWorkspacePath,strFileName,errorList);
					}
					if(slErrorList.size() > 0)
					{
						ContextUtil.abortTransaction(context);
						String  strDocumentId = FrameworkUtil.autoName(context, CommonDocument.SYMBOLIC_type_Document, CommonDocument.SYMBOLIC_policy_Document);
						domWO.connect(context,new RelationshipType(DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT),true, DomainObject.newInstance(context,strDocumentId));
						checkInFile(context, strDocumentId, strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
						strListLog.add("Message"+":"+strNoBOQItemMessage);
					}
					else
					{
						ContextUtil.commitTransaction(context);
						String  strDocumentId = FrameworkUtil.autoName(context, CommonDocument.SYMBOLIC_type_Document, CommonDocument.SYMBOLIC_policy_Document);
						domWO.connect(context,new RelationshipType(DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT),true, DomainObject.newInstance(context,strDocumentId));
						checkInFile(context, strDocumentId, strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
					}
				}
			}
		}
		catch(Exception exception)
		{
			ContextUtil.abortTransaction(context);
			exception.printStackTrace();
		}

		return strListLog;
	}

	/**
	*
	* Method to create BOQ items from xls file
	* @param context the eMatrix <code>Context</code> object
	* @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
	* @param strSOR - SOR Library Name
	* @param ItemCode - Item code of SOR Items
	* @param mapAttribute - Attribute information from file
	* @param strParentId - Parent id
	* @param strFormType - Form Type of Work order
	* @return mapFileData - Map of file data
	* @throws Exception 
	* @since R417
	*/
	private Map createBOQFromFileData(Context context,
		   String[] args, Map<String, String> mapAllowedTypeInfo,
		   String strSOR,String ItemCode, Map mapAttribute,
		   String strParentId, Map<String, String> mapFileData)
				   throws Exception {
		DomainObject domObjNewItem= DomainObject.newInstance(context);
		Map errorMap = new HashMap();
		try
		{
			String strAction = args[4];
			String strType             = mapAllowedTypeInfo.get(DomainConstants.SELECT_TYPE);
			String strTypePolicy     = mapAllowedTypeInfo.get(DomainConstants.SELECT_POLICY);
			String strRevision1     = mapAllowedTypeInfo.get(DomainConstants.SELECT_REVISION);
			String strTempPolicy     = mapFileData.get(DomainConstants.SELECT_POLICY);       
			String strDescription = mapFileData.get("description");
			String strRate = mapFileData.get("Rate");
			String strSORRate = DomainConstants.EMPTY_STRING;
			String strQuantity = mapFileData.get("Quantity");
			String strUOM = mapFileData.get("UOM");
			String strSerialNumber = mapFileData.get("SERIAL No.");
			//TODO use constants
			mapAttribute.put("WMSTotalQuantity", strQuantity);
			mapAttribute.put("Title", ItemCode);
			mapAttribute.put("WMSUnitOfMeasure", strUOM);
			mapAttribute.put("WMSBOQSequenceNumber", strSerialNumber);
			
			String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.BOQImport.Import.DoneMessage");
			String strErrorMessage1 = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.BOQImport.Import.ErrorMessage1");
			String strErrorMessage2 = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.BOQImport.Import.ErrorMessage2");
			

			Map mapSORInfo = getSORInfoFromLibrary(context,strSOR,ItemCode);
			if(UIUtil.isNotNullAndNotEmpty(strTempPolicy))
			{
				strTypePolicy = strTempPolicy;
			}
			String strObjName = DomainConstants.EMPTY_STRING;
			if(UIUtil.isNotNullAndNotEmpty(strParentId)) {  
				strObjName = FrameworkUtil.autoName(context,"type_WMSMeasurementTask", DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, true,
						false);                    

				domObjNewItem.createAndConnect(context, strType, strObjName, strRevision1,
						strTypePolicy, args[2], RELATIONSHIP_BILL_OF_QUANTITY, DomainObject.newInstance(context,strParentId), true);
				if(strAction.equals("ReviseBOQ")){		
					domObjNewItem.setAttributeValue(context, ATTRIBUTE_WMS_IS_REVISED_BOQ, "TRUE");  
					domObjNewItem.setState(context, STATE_WMS_MEASUREMENT_ITEM_ACTIVE);
				}
				errorMap.put("Message", strDoneMessage);
				if(mapSORInfo!=null&&!mapSORInfo.isEmpty()) {
					String strSORId = (String)mapSORInfo.get(DomainConstants.SELECT_ID);
					if(UIUtil.isNotNullAndNotEmpty(strSORId))
					{
						domObjNewItem.connect(context,new RelationshipType(RELATIONSHIP_WMS_TASK_SOR),true, DomainObject.newInstance(context,strSORId));
					}
					strSORRate = (String)mapSORInfo.get("Rate");
					errorMap.put("Message", strDoneMessage);
				}
				else
				{
					if(UIUtil.isNotNullAndNotEmpty(ItemCode)&&!"NS".equals(strSOR))
					{
						errorMap.put("Message", strErrorMessage1);
					}
					else
					{
						errorMap.put("Message", strDoneMessage);
					}

				}

				if(mapSORInfo.isEmpty())
				{
					mapAttribute.put("WMSReducedSORRate", strRate);
				}
				else
				{
					mapAttribute.put("WMSReducedSORRate", strSORRate);
				}           

				try {
					domObjNewItem.setDescription(context, strDescription);
					domObjNewItem.setOwner(context, context.getUser());
					domObjNewItem.setAttributeValues(context, mapAttribute);  
				}
				catch(Exception e)
				{
					errorMap.put("Message", strErrorMessage2);
				}

			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return errorMap;
	}

	/**
	*
	* Method to get SOR info from Cage code and library
	* @param context the eMatrix <code>Context</code> object
	* @param ItemCode SOR Cage Code
	* @param strSOR - SOR Library name
	* @return SOR info map
	* @throws Exception if the operation fails
	*
	* @since R417
	*/
	private Map getSORInfoFromLibrary(Context context,
		   String strLibId,String ItemCode)
				   throws  Exception {
		Map mapSOR = new HashMap();
		try {    
			/*StringList slSelect = new StringList(DomainConstants.SELECT_ID);
			MapList mapListObjects = DomainObject.findObjects(
					context,
					TYPE_SOR_LIBRARY,
					strSOR,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.QUERY_WILDCARD,
					DomainConstants.EMPTY_STRING,               // where expression
					DomainConstants.EMPTY_STRING,
					true,
					slSelect, // object selects
					(short) 0);       // limit

			String strLibId = DomainConstants.EMPTY_STRING;
			if(mapListObjects!= null && !mapListObjects.isEmpty())
			{
				Map map = (Map)mapListObjects.get(0);
				if(map != null && !map.isEmpty())
				{
					strLibId = (String)map.get(DomainConstants.SELECT_ID);
				}
			}*/	
			String strItemCode = DomainConstants.EMPTY_STRING;
			StringList slSplitLibIds = FrameworkUtil.split(strLibId, "|");
			for(int i=0;i<slSplitLibIds.size();i++){
				strLibId = (String)slSplitLibIds.get(i);
			if(UIUtil.isNotNullAndNotEmpty(strLibId))
			{
				DomainObject domLib = DomainObject.newInstance(context,strLibId);
				StringList strListBusSelects     = new StringList(2);
				strListBusSelects.add(DomainConstants.SELECT_ID);
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"].value");
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_SOR_RATE+"]");

				StringList strListRelSelects     = new StringList(1);
				strListRelSelects.add(DomainRelationship.SELECT_ID);
				Pattern patternType = new Pattern(TYPE_WMS_SOR_CHAPTER);
				patternType.addPattern(TYPE_WMS_SOR);
				Pattern patternRel = new Pattern(LibraryCentralConstants.RELATIONSHIP_SUBCLASS);
				patternRel.addPattern(LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM);
				MapList mapListSOR = domLib.getRelatedObjects(context,
																patternRel.getPattern(),                         // relationship pattern
																patternType.getPattern(),                                    // object pattern
																false,                                                        // to direction
																true,                                                       // from direction
																(short)0,                                                      // recursion level
																strListBusSelects,                                                 // object selects
																null,                                                         // relationship selects
																DomainConstants.EMPTY_STRING,                                // object where clause
																DomainConstants.EMPTY_STRING,                                // relationship where clause
																(short)0,                                                      // No expand limit
																DomainConstants.EMPTY_STRING,                                // postRelPattern
																TYPE_WMS_SOR, // postTypePattern
																null); 

				if(mapListSOR!=null && !mapListSOR.isEmpty()) {
					for(int j=0;j<mapListSOR.size();j++)
					{
						Map map = (Map)mapListSOR.get(j);
						if(map != null && !map.isEmpty())
						{
							strItemCode = (String)map.get("attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"].value");
							if(strItemCode.equals(ItemCode))
							{
								mapSOR.put(DomainConstants.SELECT_ID,(String)map.get(DomainConstants.SELECT_ID));
								mapSOR.put("Rate",(String)map.get("attribute["+ATTRIBUTE_WMS_SOR_RATE+"]"));
								break;
							}
						}
					}
				}

			}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return mapSOR;
	}

	/**
	 * updateFileWithErrorMsg - update import File with error message
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param  strWorkspacePath - File path
	 * @param  strFileName - File Name 
	 * @param  errorList - maplist for all the item status 
	 * @throws Exception if the operation fails
	 * @since R417
	 */
	private StringList updateFileWithErrorMsg(Context context,String strWorkspacePath,String strFileName,MapList errorList) throws Exception
	{
		StringList slErrorList = new StringList();

		String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
		FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
		//Create Workbook instance holding reference to .xls file
		XSSFWorkbook workbook  = new XSSFWorkbook(fileIS);
		//Get first/desired sheet from the workbook
		XSSFSheet sheet = workbook.getSheetAt(0);
		int columnIndexStatus = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		Row rowData = (Row)rowIterator.next();
		Iterator<Cell> cellIterator = rowData.cellIterator();
		while (cellIterator.hasNext())
		{
			Cell cell = cellIterator.next();
			String strCellvalue = getCellValueOfExcel(cell);

			if("Status".equals(strCellvalue))
			{					        			
				columnIndexStatus = cell.getColumnIndex();
			}		                
		}
		
		Iterator<Row> rowIteratorToWrite = sheet.iterator();
		Iterator errorListItr = errorList.iterator();
		String strDate = DomainConstants.EMPTY_STRING;
		StringList strListDateSplit = DomainConstants.EMPTY_STRINGLIST;
		String strMonth = DomainConstants.EMPTY_STRING;
		Row  next = rowIteratorToWrite.next();

		while(rowIteratorToWrite.hasNext()&&errorListItr.hasNext()) {
			Row rowHeader1 = (Row)rowIteratorToWrite.next();
			Map map = (Map)errorListItr.next();
			String strMessage = (String)map.get("Message");
			if(!"Done".equals(strMessage))
			{
				slErrorList.add(strMessage);
			}						
			Iterator<Cell> cellIterator1 = rowHeader1.cellIterator();
			while(cellIterator1.hasNext())
			{
				Cell cell1 = cellIterator1.next();
				int currentColumn1 = cell1.getColumnIndex();
				//check the column to get date from file
				if(columnIndexStatus==currentColumn1){
					cell1.setCellValue(strMessage);
					break;
				}

			}

		}
		FileOutputStream outputStream = new FileOutputStream(strFilePath);
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();
		return slErrorList;
	}
	
	/**
	*
	* Method to get symbolic , schema and i18n names of the types mentioned in property emxLibraryCentral.ImportItem.type
	* @param context the eMatrix <code>Context</code> object
	* @param strLanguage context language 
	* @return mapListMappingInfo a list of map containing symbolic , schema and i18n names of the types mentioned in property emxLibraryCentral.ImportClassifiedItem.type
	* @throws FrameworkException if the operation fails
	* @throws Exception if the operation fails
	* @throws MatrixException if the operation fails
	*
	* @since R417 WMS
	*/
	private MapList getAllowedItemTypeInfo(Context context, String strLanguage,String strPropertyKey)
		   throws FrameworkException, Exception, MatrixException {
	   StringList strListAllowedTypes = getAllowedItemsTypes(context,strLanguage,strPropertyKey);
	   MapList mapListMappingInfo = new MapList(strListAllowedTypes.size());
	   
	   Iterator iterator  = strListAllowedTypes.iterator();
	   while(iterator.hasNext())
	   {
		   String strAllowedType = (String) iterator.next();
		   strAllowedType = strAllowedType.trim();
		   HashMap<String, String> mapInfo = getAllowedTypeInfo(
				   context, strAllowedType, strLanguage);            
		   HashMap<String,String> mapMapping = getAllowedTypeAttributeMappingFromProperties(context,
				   strAllowedType, strLanguage,strPropertyKey);

		   mapInfo.putAll(mapMapping);

		   mapListMappingInfo.add(mapInfo);
	   }
	   return mapListMappingInfo;
	}
	
	/**
	*
	* Method to get list of types mentioned in property emxLibraryCentral.ImportItem.type 
	* @param context the eMatrix <code>Context</code> object
	* @param strLanguage context language 
	* @return strListAllowedTypes a list of types mentioned in property emxLibraryCentral.ImportItem.type 
	* @throws FrameworkException if the operation fails
	* @throws Exception if the operation fails
	* @throws MatrixException if the operation fails
	*
	* @since R417
	*/
	private StringList getAllowedItemsTypes(Context context, String strLanguage,String strPropertyKey)
		   throws FrameworkException, Exception {

	   String strAllowedTypes = EnoviaResourceBundle.getProperty(context,strPropertyKey);//"emxLibraryCentral.ImportItem.type");
	   if(UIUtil.isNotNullAndNotEmpty(strAllowedTypes))
	   {
		   StringList strListAllowedTypes = FrameworkUtil.splitString(strAllowedTypes, ",");            
		   return strListAllowedTypes;
	   }
	   else
	   {
		   return new StringList();
	   }
	}
	/** 
	 * Method will get cell value of xls file
	 * 
	 * @param Cell
	 * @throws Exception if the operation fails
	 * @author CHiPS
	 * @since 418
	 */
	public String getCellValueOfExcel(Cell cell){
		String strCellvalue = DomainConstants.EMPTY_STRING;

		switch (cell.getCellType())
		{
		case Cell.CELL_TYPE_STRING:
			strCellvalue =cell.getStringCellValue().trim();                    
			break;
		case Cell.CELL_TYPE_NUMERIC:
			double doubleValue =cell.getNumericCellValue();
			strCellvalue = String.valueOf(doubleValue);
			if(UIUtil.isNullOrEmpty(strCellvalue))
			{
				strCellvalue = "0";
			}
			else
			{
				strCellvalue = String.valueOf(doubleValue);
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			boolean booleanValue =cell.getBooleanCellValue();
			strCellvalue = String.valueOf(booleanValue);
			if(UIUtil.isNullOrEmpty(strCellvalue))
			{
				strCellvalue = "FALSE";
			}
			else
			{                        
				strCellvalue = String.valueOf(booleanValue);
			}
			break;
		}
		return strCellvalue;
	}	
	
	
	
	/**
	 * importMeasurementItems - import Measurements 
	 * Used in the process page chipsItemsImportProcess.jsp from command chipsImportItems
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - Selected Class Id
	 *        2 - ownerfieldId - Selected Owner Name
	 *        3 - txtImmportClassifiedItemsVault - Selected vault Name
	 *        4 - file - imported file name
	 * @throws Exception if the operation fails
	 * @since R417
	 */
	public StringList importMeasurementItems(Context context, String[] args) throws Exception
	{
		StringList strListLog = new StringList();
		MapList mapListImportObjectInfo = new MapList();
		String strWorkspacePath    = context.createWorkspace();
		String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeasurementImport.Import.DoneMessage");
		StringList slErrorList  = new StringList();
		MapList errorList = new MapList();
		String strError = DomainConstants.EMPTY_STRING;
		if (args.length == 0 )
		{
			throw new IllegalArgumentException();
		}
		try {

			ContextUtil.startTransaction(context, true);
			String strFileName        = args[3];
			String strLanguage        = context.getSession().getLanguage();
			boolean isValidFile=true;
			boolean hasError = false;
			Map<String,String> map = new HashMap();
			if(UIUtil.isNotNullAndNotEmpty(strFileName))
			{

				StringList strListHeaderValues     = new StringList();

				String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
				FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
				//Create Workbook instance holding reference to .xls file
					XSSFWorkbook workbook = null;
					try {
						workbook  = new XSSFWorkbook(fileIS);
					} catch(Exception e) {
						isValidFile = false;
						map.put("Message","Invalid File Format");
						errorList.add(map);
						strListLog.add("Message:InvalidFileFormatMeasurementItems");
					}
					//Get first/desired sheet from the workbook
				if(isValidFile){
					XSSFSheet sheet = workbook.getSheetAt(0);
					//Iterate through each rows one by one
					Iterator<Row> rowIterator = sheet.iterator();
					MapList mapListMappingInfo         = getAllowedItemTypeInfo(context, strLanguage,"WMS.ImportItem.type");
					
					if(mapListMappingInfo!=null && mapListMappingInfo.size()>0)
					{
						strListHeaderValues = getHeaderValues(rowIterator);
						if(strListHeaderValues.size()>0)
						{
							MapList mapListFileData = getDataFromFile(strListHeaderValues, rowIterator,workbook);
							Map<String, String> mapAllowedTypeInfo = getMappingInfoFromData(mapListMappingInfo, mapListFileData);
							if(mapAllowedTypeInfo!=null && !mapAllowedTypeInfo.isEmpty())
							{
								//BOQ policy for Measurement Items
								mapAllowedTypeInfo.put(DomainConstants.SELECT_POLICY, POLICY_WMS_MEASUREMENT_ITEM);
								clearBasicsInHeader(strListHeaderValues);                                
								mapListImportObjectInfo.add(mapAllowedTypeInfo);

								strListLog = createMeasurementItemsFromFileData(context, args, mapListFileData, mapAllowedTypeInfo,strListHeaderValues, mapListImportObjectInfo);
								hasError = false;
								for(int i=0;i<strListLog.size();i++)
								{
									//Map<String,String> map = new HashMap();
									String strErrorMessage = (String)strListLog.get(i);
									if(UIUtil.isNotNullAndNotEmpty(strErrorMessage)){
										if(!"Done".equals(strErrorMessage)){
											hasError = true;
										}
										map.put("Message",strErrorMessage);
									}
									errorList.add(map);
								}
							}
						}
					}
				}
				slErrorList = updateFileWithErrorMsg(context,strWorkspacePath,strFileName,errorList);
				if(hasError)
				{
					ContextUtil.abortTransaction(context);
					checkInFile(context, args[0], strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
					strError = "Message"+":"+EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.WO.MeasurementsImport.ErrorMessage");
	            }
				else
				{
					ContextUtil.commitTransaction(context);
					checkInFile(context, args[0], strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
				}
			}
			//ContextUtil.commitTransaction(context);
		}
		catch(Exception exception)
		{
			ContextUtil.abortTransaction(context);
			exception.printStackTrace();
		}
		//strListLog = new StringList();
		if(UIUtil.isNotNullAndNotEmpty(strError))
		{
			strListLog.add(strError);
		}
		return strListLog;
	}
	
	
	/**
	*
	* Method to create Measurement item and connect to context object
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - objectId - Selected Class Id
	*        2 - ownerfieldId - Selected Owner Name
	*        3 - txtImmportClassifiedItemsVault - Selected vault Name
	*        4 - file - imported file name
	* @param mapListFileData contains import file data.Where each single row from file is map where header's cell value becomes key and corresponding column value becomes value
	* @param mapAllowedTypeInfo a map containing symbolic , schema and i18n names of the type mentioned in the import file
	* @param strListHeaderValues header values mentioned in import file
	* @return strListLog log containing the list of status of each row imported from the file 
	* @throws Exception if the operation fails
	* @since R417 CHiPS
	*/
	private StringList createMeasurementItemsFromFileData(Context context,
	       String[] args, MapList mapListFileData,
	       Map<String, String> mapAllowedTypeInfo,StringList strListHeaderValues,MapList mapListImportObjectInfo) throws FrameworkException,Exception {
		StringList strListLog                     = new StringList(mapListFileData.size());
		String strMoreThanNITQuantity         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.MoreThanNITQuantity.Message");
		String strErrorWithMeasurements         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.ErrorWithMeasurements.Message");
		String strErrorSORItems         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.SORItems.Message");
		String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeasurementImport.Import.DoneMessage");

		try
		{
			DomainObject domObjMBE = DomainObject.newInstance(context);
			DomainObject domObjItem = DomainObject.newInstance(context);
			String strMBEId = args[0]; 
			Map<String,String> mapAbsMBERelData = new HashMap<String, String>();
			if(UIUtil.isNotNullAndNotEmpty(strMBEId))
			{
				domObjMBE.setId(strMBEId);
				String strWOOID = domObjMBE.getInfo(context, "relationship["+RELATIONSHIP_WMS_WORK_ORDER_MBE+"].from.id");
				String strSegOID = domObjMBE.getInfo(context, "relationship["+RELATIONSHIP_WMS_SEGMENT_MBE+"].from.id");
				if(UIUtil.isNotNullAndNotEmpty(strSegOID))
				{
					strWOOID = strSegOID;
				}
				if(UIUtil.isNotNullAndNotEmpty(strWOOID))
				{
					DomainObject domObjWO = DomainObject.newInstance(context,strWOOID);
					StringList strListBusSelects     = new StringList(3);
					strListBusSelects.add(DomainConstants.SELECT_ID);
					strListBusSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
				//	strListBusSelects.add("attribute["+ATTRIBUTE_SERIAL_NUMBER+"]");

					StringList strListRelSelects     = new StringList(1);
					strListRelSelects.add(DomainRelationship.SELECT_ID);
					Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
					patternType.addPattern(TYPE_WMS_SEGMENT);
					patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK);
					MapList mapListObjects = domObjWO.getRelatedObjects(context,
							RELATIONSHIP_BILL_OF_QUANTITY,                         // relationship pattern
							patternType.getPattern(),                                    // object pattern
							false,                                                        // to direction
							true,                                                       // from direction
							(short)0,                                                      // recursion level
							strListBusSelects,                                                 // object selects
							null,                                                         // relationship selects
							DomainConstants.EMPTY_STRING,                                // object where clause
							DomainConstants.EMPTY_STRING,                                // relationship where clause
							(short)0,                                                      // No expand limit
							DomainConstants.EMPTY_STRING,                                // postRelPattern
							TYPE_WMS_MEASUREMENT_TASK, // postTypePattern
							null); 

					Map<String,String> mapData;
					Iterator<Map<String,String>> iterator = mapListObjects.iterator();
					while(iterator.hasNext())
					{
						mapData = iterator.next();
						mapAbsMBERelData.put(mapData.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]"), mapData.get(DomainObject.SELECT_ID));
						String strSerialNumber ="";// (String)mapData.get("attribute["+ATTRIBUTE_SERIAL_NUMBER+"]");
						if(UIUtil.isNotNullAndNotEmpty(strSerialNumber))
						{
							mapAbsMBERelData.put(strSerialNumber, mapData.get(DomainObject.SELECT_ID));
						}
					}
				}
				String strType                             = mapAllowedTypeInfo.get(DomainConstants.SELECT_TYPE);
				String strTypePolicy                     = mapAllowedTypeInfo.get(DomainConstants.SELECT_POLICY);
				String strRevision                         = getDefaultRevison(context, strTypePolicy);
				MapList mlItemExist = new MapList();

				mapAllowedTypeInfo.put(DomainConstants.SELECT_REVISION, strRevision);            
				Iterator<Map<String,String>> iterator = mapListFileData.iterator();
				Map<String,String> mapFileData ;
				String strMBEQty = DomainConstants.EMPTY_STRING;
				String strITEMQuantity = DomainConstants.EMPTY_STRING;
				double totalMBEQty = 0.0;
				StringList strListItemExist = new StringList();
				while(iterator.hasNext())
				{
					mapFileData = (Map<String,String>)iterator.next();
					String strSOR = mapFileData.get("SOR");
					String strSerialNumber = mapFileData.get("SERIAL No.");
					if(UIUtil.isNullOrEmpty(strSOR))
					{
						strSOR = strSerialNumber;
					}
					String  strRelId = getItemRelId(context,strSOR,args,domObjMBE);
					totalMBEQty = 0.0;
					if(!strListItemExist.contains(strSOR) && UIUtil.isNullOrEmpty(strRelId))
					{
						Map<String,String> mapItem = new HashMap<String,String>();
						String strItemOID =  mapAbsMBERelData.get(strSOR);
						if(UIUtil.isNotNullAndNotEmpty(strItemOID))
						{
							domObjItem.setId(strItemOID);
							DomainRelationship domMBEItemRel = DomainRelationship.connect(context,domObjMBE ,RELATIONSHIP_WMS_MBE_ACTIVITIES, domObjItem);
							String strRelID = DomainConstants.EMPTY_STRING;
							StringList strListRelSelects=new StringList(1);
							strListRelSelects.add(DomainRelationship.SELECT_ID);
							Map relMap = domMBEItemRel.getRelationshipData(context, strListRelSelects);
							if(relMap!=null && !relMap.isEmpty())
							{
								StringList slRelID = (StringList)relMap.get(DomainRelationship.SELECT_ID);
								if(slRelID!=null && slRelID.size() > 0)
								{
									strRelID = (String)slRelID.get(0);
								}
							}
							mapItem.put("ItemCode",strSOR);
							mapItem.put("RelId",strRelID);
							strListItemExist.add(strSOR);
							//mlItemExist.add(mapItem);                
							if(UIUtil.isNotNullAndNotEmpty(strRelID))
							{
								try{
									strMBEQty =  copyMeasurments(context, strRelID,mapFileData,mapAllowedTypeInfo,strListHeaderValues);
								}
								catch(Exception e){
									e.printStackTrace();
									// Invalid Units or Measurements
									strListLog.add(strErrorWithMeasurements);
								}
								strITEMQuantity = DomainRelationship.getAttributeValue(context, strRelID, ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY);
								totalMBEQty = Double.parseDouble(strITEMQuantity) + Double.parseDouble(strMBEQty);
								String strNITQty = domObjItem.getInfo(context, "attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]");
								if(totalMBEQty > Double.parseDouble(strNITQty)){
									// MBE Qty should not be more than NIT Qty and should not less than 0
									strListLog.add(strMoreThanNITQuantity);
								}
								else{								
									DomainRelationship.setAttributeValue(context, strRelID, ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY, String.valueOf(totalMBEQty));
									strListLog.add(strDoneMessage);
								}
							}
						}
						// Invalid SOR Item
						else
						{	
							//Non SOR Items
							if(UIUtil.isNotNullAndNotEmpty(strSOR))
							{
								String strErrorInNonSOR = importNonSORItem(context, domObjMBE, mapItem, strListItemExist, strSOR, strMBEQty, mapFileData, mapAllowedTypeInfo, strListHeaderValues);
								if(UIUtil.isNotNullAndNotEmpty(strErrorInNonSOR))
								{
									strListLog.add(strErrorInNonSOR);
								}
							}
							else
							{
								strListLog.add(strErrorSORItems);
							}
						}

					}
					else
					{
						if(UIUtil.isNotNullAndNotEmpty(strRelId)){
							try{
								strMBEQty = copyMeasurments(context, strRelId,mapFileData,mapAllowedTypeInfo,strListHeaderValues); 
							}
							catch(Exception e){
								e.printStackTrace();
								strListLog.add(strErrorWithMeasurements);
							}
							DomainRelationship domRel = DomainRelationship.newInstance(context, strRelId);
							StringList slSelect = new StringList(3);
							slSelect.add("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]");
							slSelect.add("to.attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]");
							Map mapRelData = domRel.getRelationshipData(context, slSelect);
							StringList strListITEMQuantity = WMSUtil_mxJPO.convertToStringList(mapRelData.get("attribute["+ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY+"]"));
							StringList strListNITQty = WMSUtil_mxJPO.convertToStringList(mapRelData.get("to.attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]"));
							totalMBEQty = Double.parseDouble((String)strListITEMQuantity.get(0)) + Double.parseDouble(strMBEQty);
							if(totalMBEQty > Double.parseDouble((String)strListNITQty.get(0))){
								strListLog.add(strMoreThanNITQuantity);
							}
							else{							
								DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY, String.valueOf(totalMBEQty));
								strListLog.add(strDoneMessage);
							} 
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
		return strListLog;
	}

	/**
	*
	* Method to get Item and MBE Rel id
	* @param context the eMatrix <code>Context</code> object
	* @param strItemCode SOR Cage Code
	* @param args- program arguments
	* @param domMBE - MBE Object
	* @return strRelID - Relationship id between Item and MBE
	* @throws Exception if the operation fails
	*
	* @since R417
	*/
	private String getItemRelId(Context context,
	        String strItemCode,String[] args,DomainObject domMBE)
	                throws  Exception {

	    Map methodItemArgs = new HashMap();
	    String strMBEID = (String)domMBE.getObjectId();
	    methodItemArgs.put("mbeOID",strMBEID );
	    WMSMeasurementBookEntry_mxJPO mbeJPO = new WMSMeasurementBookEntry_mxJPO(context,args);
	    MapList mlMBEItem = mbeJPO.getMBEActivities(context, JPO.packArgs(methodItemArgs));
	   
	    Iterator itrMBE = mlMBEItem.iterator();
	    DomainRelationship domMBEItemRel = new DomainRelationship();
	    String strRelID = DomainConstants.EMPTY_STRING;
	  //if Item is connected with MBE
	    while(itrMBE.hasNext())
	    {
	        Map map = (Map)itrMBE.next();
	        if(map != null)
	        {
	            String strCageCode =(String) map.get("attribute["+ATTRIBUTE_WMS_WORK_ORDER_TITLE+"]");
	            String strSerialNumber ="";// (String) map.get("attribute["+ATTRIBUTE_SERIAL_NUMBER+"]");
	            String strMBEItemId =(String) map.get(DomainConstants.SELECT_ID);
	            if(UIUtil.isNullOrEmpty(strSerialNumber))
	            {
	            	strSerialNumber = DomainConstants.EMPTY_STRING;
	            }
	            if(UIUtil.isNullOrEmpty(strCageCode))
	            {
	            	strCageCode = DomainConstants.EMPTY_STRING;
	            }
	            if((UIUtil.isNotNullAndNotEmpty(strCageCode)|| UIUtil.isNotNullAndNotEmpty(strSerialNumber))&&UIUtil.isNotNullAndNotEmpty(strItemCode))
	            {
	                if(strItemCode.equals(strCageCode) || strItemCode.equals(strSerialNumber))
	                {
	                    strRelID = (String) map.get("id[connection]");
	                    break;
	                }
	            }
	        }
	    }
	    
	    return strRelID;
	}

	/**
	*
	* Method to copy measurements to Item
	* @param context the eMatrix <code>Context</code> object
	* @param strItemCode SOR Cage Code
	* @param args- program arguments
	* @param mapAllowedTypeInfo - Item related info
	* @param strListHeaderValues - File info
	* @param mapFileData - File data
	* @param strRelOID - Relationship id between Item and MBE
	* @return strMBEQty - MBE Quantity
	* @throws Exception if the operation fails
	*
	* @since R417
	*/
		private String copyMeasurments(Context context, String strRelOID,Map mapFileData,Map mapAllowedTypeInfo,StringList strListHeaderValues) throws Exception{  
	    String strMBEQty = DomainConstants.EMPTY_STRING;
	    try{
	        if(UIUtil.isNotNullAndNotEmpty(strRelOID))
	        {
	            String strMeasuremntOID  = FrameworkUtil.autoName(context,
	                   "type_WMSMeasurements",
	                    "policy_WMSMeasurements");
	            DomainObject domMeasuremnt = DomainObject.newInstance(context,strMeasuremntOID);
					Map mapMeasurementAttr = getAttributeMap(mapAllowedTypeInfo, strListHeaderValues,  mapFileData);
	           
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("Assessment Comments"))){
	                mapMeasurementAttr.put("Assessment Comments", DomainConstants.EMPTY_STRING);
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("Title"))){
	                mapMeasurementAttr.put("Title", DomainConstants.EMPTY_STRING);
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSSMBEFrequency"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_FREQUENCY, "0.0");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSMBELength"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_LENGTH, "0.0");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSMBEBreadth"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_BREADTH, "0.0");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSMBEDepth"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_DEPTH,"0.0");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSMBERadius"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_RADIUS,"0.0");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSIsDeduction"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_IS_DEDUCTION,"no");
	            }
	            if("No".equalsIgnoreCase((String)mapMeasurementAttr.get("WMSIsDeduction"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_IS_DEDUCTION,"no");
	            }
	            if("Yes".equalsIgnoreCase((String)mapMeasurementAttr.get("WMSIsDeduction"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_IS_DEDUCTION,"yes");
	            }
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSItemCoEfficientFactor"))){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_ITEM_CO_EFFICIENT_FACTOR, "0.0");
	            }
	          /*  if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSIsManualQuanity"))){
	                mapMeasurementAttr.put("CHiPSIsManualQuanity","no");
	            }
	            if("No".equalsIgnoreCase((String)mapMeasurementAttr.get("WMSIsManualQuanity"))){
	                mapMeasurementAttr.put("CHiPSIsManualQuanity","no");
	            }
	            if("Yes".equalsIgnoreCase((String)mapMeasurementAttr.get("WMSIsManualQuanity"))){
	                mapMeasurementAttr.put("WMSIsManualQuanity","yes");
	            }*/
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("WMSMBEActivityQuantity"))){
	                    mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY, "0.0");
	            }
	            String strUWD = (String)mapMeasurementAttr.get("WMSSUWD");
	            if(UIUtil.isNullOrEmpty(strUWD)){
	                mapMeasurementAttr.put(ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY,DomainConstants.EMPTY_STRING);
	            }
	            else
	            {
	            	strUWD = getUWDValue(context,strUWD);
	            	mapMeasurementAttr.put("WMSUWD", strUWD);
	            }           
	            strMBEQty = (String)mapFileData.get("MBE");
	            if(UIUtil.isNullOrEmpty((String)mapMeasurementAttr.get("CHiPSShapeName"))){  
					
	                strMBEQty = calculateMBEQty(context, strMBEQty, mapMeasurementAttr);
					
	            }
	            else
	            {
	            	String strShapeName =  (String)mapMeasurementAttr.get("CHiPSShapeName");
	            	mapMeasurementAttr.put("CHiPSShapeName",strShapeName);
	            	mapMeasurementAttr.putAll(calculateMBEQtyWithShape(context, strMBEQty, mapMeasurementAttr));
	            	mapMeasurementAttr.remove("WMSMBERadius");
	            	mapMeasurementAttr.remove("WMSMBELength");
	            	mapMeasurementAttr.remove("WMSMBEBreadth");
	            	mapMeasurementAttr.remove("WMSMBEDepth");
	            	strMBEQty = (String)mapMeasurementAttr.get("MBEQty");
	            	mapMeasurementAttr.remove("MBEQty");
	            	if("No Shape".equals(strShapeName)||"Number".equals(strShapeName))
	            	{
	            		mapMeasurementAttr.put("CHiPSShapeImage",strShapeName);
	            		if("No Shape".equals(strShapeName)){
	            			mapMeasurementAttr.put("WMSIsManualQuanity","yes");
	            		}
	            	}
	            	else
	            	{
	            		mapMeasurementAttr.put("CHiPSShapeImage",(String)mapMeasurementAttr.get("CHiPSShapeName")+"Shape.svg");
	            	            	
	            	String strCommand = "modify $1 $2 add interface $3";
	            	String interfaceName = strShapeName;            	
	            	MqlUtil.mqlCommand(context,strCommand, "bus", strMeasuremntOID, "CHiPS"+interfaceName);
	            	}
	            	
	            }
	            Map mapItemRel = new HashMap();
	            mapItemRel.put(ATTRIBUTE_WMS_MBE_ACTIVITY_QUANTITY, strMBEQty);
	            DomainObject domMeasurment = DomainObject.newInstance(context,strMeasuremntOID);
	            mapMeasurementAttr.remove("WMSIsManualQuanity");
	            domMeasuremnt.setAttributeValues(context,mapMeasurementAttr);
	            String mqlCommand= "add connection $1 torel $2 from $3";
	            String Res= MqlUtil.mqlCommand(context, mqlCommand, RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS, strRelOID, strMeasuremntOID);
	            String strMeasurementRelID = domMeasurment.getInfo(context, "relationship["+RELATIONSHIP_WMS_ACTIVITY_MEASUREMENTS+"].id");
	            DomainRelationship.setAttributeValues(context, strMeasurementRelID, mapItemRel);
	        }
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	        //throw e;
	    }
	    
	    return strMBEQty;
	}
	
		/**
		*
		* Method to get UWD value for import Measurement 
		* @param context the eMatrix <code>Context</code> object
		* @param strUWDPrefix - Prefix of UWD value
		* @return UWD value
		* @throws Exception if the operation fails
		*
		* @since R417
		*/
		public String getUWDValue(Context context, String strUWDPrefix) throws Exception{
			String strUWDReturn = DomainConstants.EMPTY_STRING;
		    try {
		            String strUWDValue = EnoviaResourceBundle.getProperty(context, "WMS","WMS.Table.Label.WMSUWDValues", context.getLocale());
		            StringList slUWDList = FrameworkUtil.split(strUWDValue, ",");
		            for(int i=0;i<slUWDList.size();i++)
		            {
		            	StringList slSplitUWD = FrameworkUtil.split((String)slUWDList.get(i), ":");
		            	if(strUWDPrefix.equals((String)slSplitUWD.get(0)))
		            	{
		            		strUWDReturn = slSplitUWD.get(0).toString() + " - " + slSplitUWD.get(1).toString();
		            		break;
		            	}
		            }
		    }
		    catch(Exception e)
		    {
		    	e.printStackTrace();
		    	throw e;
		    }
		    return strUWDReturn;
		}
		
		/**
		*
		* Method to calculate Quantity as per formulas
		* @param context the eMatrix <code>Context</code> object
		* @param  mapMeasurementAttr - attribute information
		* @param strMBEQty - MBE Quantity
		* @return strMBEQty - MBE Quantity
		* @throws Exception if the operation fails
		*
		* @since R417
		*/
		private String calculateMBEQty(Context context,
		        String strMBEQty , Map mapMeasurementAttr)
		                throws  Exception {
		try{
		    String strIsManualQty = (String)mapMeasurementAttr.get("WMSIsManualQuanity");
		            if("Yes".equalsIgnoreCase(strIsManualQty))
		         {
		                String strIsDeduction = (String)mapMeasurementAttr.get("WMSIsDeduction");
		                double dMBEQty = 0.0; 
		                dMBEQty = WMSUtil_mxJPO.convertToDouble(strMBEQty);
		                if("Yes".equalsIgnoreCase(strIsDeduction))
		                {
		                    dMBEQty = dMBEQty*-1;
		                }
		                return String.valueOf(dMBEQty);
		      }
		      else
		      {
		        double dRadius = Double.parseDouble((String)mapMeasurementAttr.get("WMSMBERadius"));
		        double dNum = Double.parseDouble((String)mapMeasurementAttr.get("WMSMBEFrequency"));
		        double dLen = Double.parseDouble((String)mapMeasurementAttr.get("WMSMBELength"));
		        double dBre = Double.parseDouble((String)mapMeasurementAttr.get("WMSMBEBreadth"));
		        double dDep = Double.parseDouble((String)mapMeasurementAttr.get("WMSMBEDepth"));
		        String strIsDeduction = (String)mapMeasurementAttr.get("WMSIsDeduction");
		        double dCF = Double.parseDouble((String)mapMeasurementAttr.get("WMSItemCoEfficientFactor"));
		        double dMBEQty = 0.0; 
		        String strUWD = (String)mapMeasurementAttr.get("WMSUWD");
		        float fUWD = 1;
		        if(UIUtil.isNotNullAndNotEmpty(strUWD))
		        {
		        	StringList slSplitUWD = FrameworkUtil.split(strUWD, "-");
		        	if(slSplitUWD.size() > 0)
		        	{
		        		fUWD = Float.parseFloat(slSplitUWD.get(1).toString());
		        	}        	
		        }
		        
		        if(dNum ==0.0)
		        {
		            dNum = 1;
		        }
		        if(dCF ==0.0)
		        {
		            dCF = 1;
		        }
		        if(dRadius > 0)
		        {
		            if(dDep > 0){
		                dMBEQty = (3.14*dRadius*dRadius*dDep);
		                dMBEQty = dNum*dMBEQty*dCF*fUWD;
		            }
		            else
		            {
		                dMBEQty = (3.14*dRadius*dRadius);
		                dMBEQty = dCF*dNum*dMBEQty*fUWD;
		            }
		        }         
		        else
		        {            
		            if(dBre ==0.0)
		            {
		                dBre = 1;
		            }
		            if(dDep ==0.0)
		            {
		                dDep = 1;
		            }        
		            if(dLen ==0.0)
		            {
		                dLen = 1;
		            }
		            dMBEQty = dNum*dBre*dDep*dLen*dCF*fUWD;
		        }       
		                if("Yes".equalsIgnoreCase(strIsDeduction))
		       {
		                    dMBEQty = dMBEQty*-1;
		                }
		                strMBEQty = String.valueOf(dMBEQty);
		           return strMBEQty;
		       }
		    }  
		catch(Exception e)
		{
		    e.printStackTrace();
		    throw e;
		    
		}
		    
		}	
		
		
		/**
		*
		* Method to calculate Quantity as per Shape formulas
		* @param context the eMatrix <code>Context</code> object
		* @param  mapMeasurementAttr - attribute information
		* @param strMBEQty - MBE Quantity
		* @return strMBEQty - MBE Quantity
		* @throws Exception if the operation fails
		*
		* @since R417
		*/
		private Map calculateMBEQtyWithShape(Context context,
		        String strMBEQty , Map mapMeasurementAttr)
		        		throws  Exception {
			Map mapAttribute = new HashMap();	
			try{
				String strShapeName = (String)mapMeasurementAttr.get("CHiPSShapeName");
				if("No Shape".equalsIgnoreCase(strShapeName)||"Number".equalsIgnoreCase(strShapeName))
				{
					String strIsDeduction = (String)mapMeasurementAttr.get("CHiPSIsDeduction");
					String strNumber = (String)mapMeasurementAttr.get("CHiPSMBEFrequency");
					double dMBEQty = 0.0; 
					if("No Shape".equalsIgnoreCase(strShapeName)){
						if(UIUtil.isNotNullAndNotEmpty(strMBEQty)){
							dMBEQty = WMSUtil_mxJPO.convertToDouble(strMBEQty);
						}
					}
					else
					{
						if(UIUtil.isNotNullAndNotEmpty(strNumber)){
							dMBEQty = WMSUtil_mxJPO.convertToDouble(strNumber);
						}
					}
					if("Yes".equalsIgnoreCase(strIsDeduction))
					{
						dMBEQty = dMBEQty*-1;
					}
					mapAttribute.put("MBEQty", String.valueOf(dMBEQty));
					return mapAttribute;
				}
				else
				{    	
					double dRadius = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSMBERadius"));
					double dNum = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSMBEFrequency"));
					double dLen = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSMBELength"));
					double dBre = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSMBEBreadth"));
					double dDep = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSMBEDepth"));
					mapAttribute = calculateArea(context, strShapeName, String.valueOf(dRadius),String.valueOf(dLen),String.valueOf(dBre),String.valueOf(dDep));
					String strIsDeduction = (String)mapMeasurementAttr.get("CHiPSIsDeduction");
					double dCF = Double.parseDouble((String)mapMeasurementAttr.get("CHiPSItemCoEfficientFactor"));
					double dMBEQty = 0.0; 
					String strCHiPSUWD = (String)mapMeasurementAttr.get("CHiPSUWD");
					float fUWD = 1;
					if(UIUtil.isNotNullAndNotEmpty(strCHiPSUWD))
					{
						StringList slSplitUWD = FrameworkUtil.split(strCHiPSUWD, "-");
						if(slSplitUWD.size() > 0)
						{
							fUWD = Float.parseFloat(slSplitUWD.get(1).toString());
						}        	
					}

					if(dNum ==0.0)
					{
						dNum = 1;
					}
					if(dCF ==0.0)
					{
						dCF = 1;
					}
					double area = 1;
					if(mapAttribute.get("Area")!=null){
						area = (double)mapAttribute.get("Area");
						if(area ==0.0)
						{
							area = 1;
						}
					}
					mapAttribute.remove("Area");
					dMBEQty = dNum*area*dCF*fUWD;

					if("Yes".equalsIgnoreCase(strIsDeduction))
					{
						dMBEQty = dMBEQty*-1;
					}
					strMBEQty = String.valueOf(dMBEQty);
					mapAttribute.put("MBEQty", String.valueOf(dMBEQty));
				}
			}  
			catch(Exception e)
			{
				e.printStackTrace();
				throw e;

			}
			return mapAttribute;

		}
		
		
		/**
		*
		* Method to calculate area as per dimensions pass
		* @param context the eMatrix <code>Context</code> object
		* @param Shape Name,Radius,Length/Edge/Side/Base,Bredth/Width/Base,Depth
		* @return Map with calculate area
		* @throws Exception if the operation fails
		*
		* @since R417
		*/
		public Map  calculateArea(Context context, String strShapeName, String strRadius,String strLen,String strBredth,String strDep) throws Exception
		{
			Map attributeMap = new HashMap();
			String args[] = new String[1];
			WMSMeasurementBookEntry_mxJPO mbeJPO = new WMSMeasurementBookEntry_mxJPO(context, args);
			switch(strShapeName)
			{
				case "Line":			
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Circle":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, strRadius,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Square":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Rectangle":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,strBredth,DomainConstants.EMPTY_STRING);
					break;
				case "Trapezoid":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,strDep,strBredth);
					break;
				case "Hexagon":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "HexagonalPrism":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,strDep,DomainConstants.EMPTY_STRING);
					break;
				case "Cube":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Cuboid":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strDep,strLen,strBredth);
					break;
				case "Trapezoidal":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strBredth,strDep,strLen);					
					break;
				case "Cylinder":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Cone":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strDep,strRadius,DomainConstants.EMPTY_STRING);
					break;
				case "Pyramid":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, DomainConstants.EMPTY_STRING,strLen,strBredth,strDep);
					break;
				case "Sphere":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, strRadius,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
				case "Hemisphere":
					attributeMap = mbeJPO.calculateArea(context, strShapeName, strRadius,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
					break;
			}	
			return attributeMap;
		}

		
		/**
		*
		* Method to import non SOR Items
		* @param context the eMatrix <code>Context</code> object
		* @param domObjMBE, mapItem,strListItemExist,strSOR,strMBEQty,mapFileData,mapAllowedTypeInfo,strListHeaderValues
		* @return String error message
		* @throws Exception if the operation fails
		*
		* @since R417
		*/
		public String importNonSORItem(Context context,DomainObject domObjMBE,Map mapItem,StringList strListItemExist,String strSOR,String strMBEQty,Map mapFileData,Map mapAllowedTypeInfo,StringList strListHeaderValues)
		{
			
			String strLog = DomainConstants.EMPTY_STRING;
			try{
				String strMoreThanNITQuantity         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.MoreThanNITQuantity.Message");
				String strErrorWithMeasurements         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.ErrorWithMeasurements.Message");
				String strErrorSORItems         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeausrementImport.SORItems.Message");
				String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.MeasurementImport.Import.DoneMessage");
				String strItemOID = FrameworkUtil.autoName(context,
						"type_WMSMeasurementTask",
						"policy_WMSMeasurementTask");
				DomainObject domObjItem = DomainObject.newInstance(context,strItemOID);
				domObjItem.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, strSOR);
				DomainRelationship domMBEItemRel = DomainRelationship.connect(context,domObjMBE ,RELATIONSHIP_WMS_MBE_ACTIVITIES, domObjItem);
				String strRelID = DomainConstants.EMPTY_STRING;
				StringList strListRelSelects=new StringList(1);
				strListRelSelects.add(DomainRelationship.SELECT_ID);
				Map relMap = domMBEItemRel.getRelationshipData(context, strListRelSelects);
				String strITEMQuantity = DomainConstants.EMPTY_STRING;
				Double totalMBEQty = 0.0;
				if(relMap!=null && !relMap.isEmpty())
				{
					StringList slRelID = (StringList)relMap.get(DomainRelationship.SELECT_ID);
					if(slRelID!=null && slRelID.size() > 0)
					{
						strRelID = (String)slRelID.get(0);
					}
				}
				mapItem.put("ItemCode",strSOR);
				mapItem.put("RelId",strRelID);
				strListItemExist.add(strSOR);
				//mlItemExist.add(mapItem);     
				if(UIUtil.isNotNullAndNotEmpty(strRelID))
				{
					try{
						strMBEQty =  copyMeasurments(context, strRelID,mapFileData,mapAllowedTypeInfo,strListHeaderValues);
					}
					catch(Exception e){
						e.printStackTrace();
						// Invalid Units or Measurements
						strLog = strErrorWithMeasurements;
					}			
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			return strLog;
		}

		/**
		 * reviseBOQ - import BOQ Items [Segment,SOR Items and Non SOR Items] 
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @param args holds the following input arguments:
		 *        0 -  strWOId - Selected Work Order Id
		 *        3 - file - imported file name
		 * @throws Exception if the operation fails
		 * @since R417
		 */
		public StringList reviseBOQ(Context context, String[] args) throws Exception
		{
			StringList strListLog = new StringList();
			StringList slErrorList  = new StringList();
			MapList mapListImportObjectInfo = new MapList();
			String strWorkspacePath    = context.createWorkspace();
			String strDoneMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.BOQImport.Import.DoneMessage");
			String strNoBOQItemMessage         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.WO.NoBOQItem.Alert");
			String strRingiStatusApproved         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.RevisedBOQStatus.Approved");
			String strRingiStatusNotApproved         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.RevisedBOQStatus.NotApproved");
			String strNotApprovedRingiNumberMessage         = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.NotApprovedRingiNumber.Import.FailedSubject");
			String strLesserQtyMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.LesserQuantity.Import.FailedSubject");
			String strTotalBOQValueExceed = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.TotalBOQValueExceed.Import.FailedSubject");
			String strRevisedBOQStatusSuccess = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.RevisedBOQStatus.Success");
			String strRevisedBOQStatusFailed = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.RevisedBOQStatus.Fail");
			DomainObject domRevisedBOQ = DomainObject.newInstance(context);
			Map mapAttributeRevisedBOQ = new HashMap();
			String strRevisedBOQStatus = "";
			String strRevisedBOQId = "";
			Map mapWOInfo = null;
			Map mapEmailInfo = new HashMap();
			if (args.length == 0 )
			{
				throw new IllegalArgumentException();
			}
			try 
			{
				MapList errorList = new MapList();
				Map mapError = new HashMap();
				String strWOId = args[0];
				String strRingiNumber = args[6];
				String strRingiAmount = args[7];
				String strDescription = args[8];
				String strFileName        = args[3];
				String strRingiStatus = "";
				boolean isValidFile=true;							
				if(UIUtil.isNotNullAndNotEmpty(strWOId) && UIUtil.isNotNullAndNotEmpty(strFileName))
				{
					StringList strListHeaderValues     = new StringList();							
					String strFilePath = strWorkspacePath + java.io.File.separator + strFileName;
					
					StringList slObjSelect = new StringList();
					slObjSelect.add(DomainObject.SELECT_ID);   
					slObjSelect.add(DomainObject.SELECT_NAME);   
					slObjSelect.add("to["+RELATIONSHIP_WMS_PORJECT_WORK_ORDER+"].from.owner");   
					slObjSelect.add("from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.id");   
					slObjSelect.add("from["+RELATIONSHIP_WMS_REVISED_BOQ+"].to.last.id");   
					slObjSelect.add("from["+RELATIONSHIP_WMS_REVISED_BOQ+"].to.previous.id");           
					slObjSelect.add("attribute["+ATTRIBUTE_WMS_VALUE_OF_CONTRACT+"]");   
					DomainObject domWO = DomainObject.newInstance(context, strWOId);
					mapWOInfo =  domWO.getInfo(context,slObjSelect);
					
					//Create Revised BOQ object
					double dTotalValue = 0.0;
					String strTotalValue = "";					
					strRevisedBOQId = (String)mapWOInfo.get("from["+RELATIONSHIP_WMS_REVISED_BOQ+"].to.last.id");
					String strContractValue = (String)mapWOInfo.get("attribute["+ATTRIBUTE_WMS_VALUE_OF_CONTRACT+"]");
					mapAttributeRevisedBOQ.put(ATTRIBUTE_MSIL_RINGI_NUMBER, strRingiNumber);
					mapAttributeRevisedBOQ.put(ATTRIBUTE_WMS_RINGI_AMOUNT, strRingiAmount);
					if(UIUtil.isNotNullAndNotEmpty(strRevisedBOQId)){
						domRevisedBOQ.setId(strRevisedBOQId);
						BusinessObject busReviseObject = domRevisedBOQ.reviseObject(context, true);
						strRevisedBOQId = busReviseObject.getObjectId();
						busReviseObject.close(context);
						domRevisedBOQ.setId(strRevisedBOQId);
						Map mapRevBOQInfo = (Map)getBOQRevisionDetails(context, domRevisedBOQ, strRevisedBOQStatusSuccess);
						if(mapRevBOQInfo != null && !mapRevBOQInfo.isEmpty()){	
							strTotalValue = (String)mapRevBOQInfo.get("attribute["+ATTRIBUTE_WMS_TOTAL_VALUE+"]");
							dTotalValue = Double.parseDouble(strTotalValue) + Double.parseDouble(strRingiAmount);
						}else{
							dTotalValue = Double.parseDouble(strContractValue) + Double.parseDouble(strRingiAmount);							
						}
					}else{
						strRevisedBOQId = FrameworkUtil.autoName(context, "type_WMSRevisedBOQ", "policy_WMSRevisedBOQ");
						domRevisedBOQ.setId(strRevisedBOQId);
						domRevisedBOQ.connect(context,new RelationshipType(RELATIONSHIP_WMS_REVISED_BOQ),false, domWO);
						dTotalValue = Double.parseDouble(strContractValue) + Double.parseDouble(strRingiAmount);				
					}
					domRevisedBOQ.setDescription(context, strDescription);
					mapAttributeRevisedBOQ.put(ATTRIBUTE_WMS_TOTAL_VALUE, String.valueOf(dTotalValue));	
					
					//Code to check wheter valid Ringi Number or not
					//boolean isApprovedRingiNumber = isRingiApproved(context,args,strRingiNumber);
					boolean isApprovedRingiNumber = true;
					if(isApprovedRingiNumber)
					{
						ContextUtil.startTransaction(context, true);
						strRingiStatus = strRingiStatusApproved;						
						String strLanguage        = context.getSession().getLanguage();
						//StringList slObjSelect = new StringList("attribute[CHIPSFormType]");
						String strParentId = DomainConstants.EMPTY_STRING;
						String strMBId = DomainConstants.EMPTY_STRING;
							
						String strSegmentId = DomainConstants.EMPTY_STRING;
						String strFormType = DomainConstants.EMPTY_STRING;
						if(mapWOInfo!=null&&!mapWOInfo.isEmpty())
						{
							strMBId = (String)mapWOInfo.get("from["+RELATIONSHIP_BILL_OF_QUANTITY+"].to.id");
						}
						strParentId = strMBId;						
						FileInputStream fileIS = new FileInputStream(new java.io.File(strFilePath));
						//Create Workbook instance holding reference to .xls file
						XSSFWorkbook workbook=null;
						try{
							workbook  = new XSSFWorkbook(fileIS);
						}catch(Exception e) {
							isValidFile=false;
							mapError.put("Message","Invalid File Format");
							errorList.add(mapError);
							strListLog.add("InvalidFileFormatRevisedBOQ");
						}
						//Get first/desired sheet from the workbook
						if(isValidFile){
							XSSFSheet sheet = workbook.getSheetAt(0);
							//Iterate through each rows one by one
							Iterator<Row> rowIterator = sheet.iterator();
							MapList mapListMappingInfo         = getAllowedItemTypeInfo(context,strLanguage,"WMS.BOQ.type");								
							StringList alreadyAddedSegment = new StringList();
							if(mapListMappingInfo!=null && mapListMappingInfo.size()>0)
							{
								strListHeaderValues = getHeaderValues(rowIterator);									
								Map mapAttribute = new HashMap(strListHeaderValues.size());
								if(strListHeaderValues.size()>0)
								{
									MapList mapListFileData = getDataFromFile(strListHeaderValues,
											rowIterator,workbook);
									Map<String, String> mapAllowedTypeInfo = getMappingInfoFromData(
											mapListMappingInfo, mapListFileData);
									Iterator itrFile = mapListFileData.iterator();   
									double dTotalBOQValue = 0.0f;
									StringList slAlreadyExistingBOQs = new StringList();
									while(itrFile.hasNext()) 
									{                   
										Map mapFileData = (Map)itrFile.next();
										mapAttribute = getAttributeMap(mapAllowedTypeInfo, strListHeaderValues,  mapFileData);
										String strSOR     = args[5];
										String ItemCode = (String)mapFileData.get("Item Code");   
										String strUOM = (String)mapFileData.get("UOM");   
										String strQuantity = (String)mapFileData.get("Quantity");   
										String strRate = (String)mapFileData.get("Rate");   								
										if(UIUtil.isNullOrEmpty(strUOM)&&!alreadyAddedSegment.contains(ItemCode))
										{
											strParentId = strMBId;
											alreadyAddedSegment.add(ItemCode);
										}
										else
										{
											if(mapAllowedTypeInfo!=null && !mapAllowedTypeInfo.isEmpty())
											{
												//To check item code is already exists
												MapList mlExistBOQ = getExistingBOQs(context,args,ItemCode);
												if(mlExistBOQ != null && !mlExistBOQ.isEmpty())
												{
													// Already exist 
													StringList slQtySubmittedTillDate = new StringList();
													int iSize = mlExistBOQ.size();
													for(int iCount=0; iCount<iSize; iCount++)
													{
														Map mpInfo = (Map)mlExistBOQ.get(iCount);
														String strExistingBOQId = (String)mpInfo.get(DomainConstants.SELECT_ID);
														String strMBEQty = (String)mpInfo.get("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
														double dTotalApprovedQuantity = 0.0;
														if(UIUtil.isNotNullAndNotEmpty(strMBEQty)){
															dTotalApprovedQuantity = Double.parseDouble(strMBEQty);
														}
														//If new quantity is lesser than Total Approved Quantity then no data will be changed
														if(Double.parseDouble(strQuantity) < dTotalApprovedQuantity){														
															mapError.put("Message",strLesserQtyMessage);
															errorList.add(mapError);
															strListLog.add("LesserQuantity");
															mapEmailInfo.put("TotalApprovedQuantity", strMBEQty);
															mapEmailInfo.put("NewQuantity", strQuantity);
															break;
														}else{
															if(UIUtil.isNotNullAndNotEmpty(strExistingBOQId)){																	
																DomainObject doBOQ = DomainObject.newInstance(context,strExistingBOQId);
																doBOQ.setAttributeValue(context, ATTRIBUTE_WMS_TOTAL_QUANTITY, strQuantity);
																dTotalBOQValue = dTotalBOQValue + (Double.parseDouble(strQuantity) * Double.parseDouble(strRate));																
																slAlreadyExistingBOQs.add(strExistingBOQId);															
															}
														}
													}
												}
												else
												{
													// New Item    
													if(alreadyAddedSegment != null && alreadyAddedSegment.size()>0)
													{
														strSegmentId = FrameworkUtil.autoName(context, "type_WMSSegment", "policy_WMSMeasurementItem");
														DomainObject domSegment = DomainObject.newInstance(context,strSegmentId);
														domSegment.connect(context,new RelationshipType(RELATIONSHIP_BILL_OF_QUANTITY),false, DomainObject.newInstance(context,strParentId));
														domSegment.setState(context, STATE_WMS_MEASUREMENT_ITEM_ACTIVE);
														domSegment.setAttributeValue(context,"Title", (String)alreadyAddedSegment.get(alreadyAddedSegment.size()-1));
														mapError.put("Message", strDoneMessage);
														errorList.add(mapError);
														strParentId = strSegmentId;
													}
													mapAllowedTypeInfo.put(DomainConstants.SELECT_POLICY, POLICY_WMS_MEASUREMENT_ITEM);
													clearBasicsInHeader(strListHeaderValues);                                
													mapListImportObjectInfo.add(mapAllowedTypeInfo);
													if(UIUtil.isNotNullAndNotEmpty(strQuantity) && UIUtil.isNotNullAndNotEmpty(strRate))
													{
														//dTotalBOQValue = dTotalBOQValue + (Double.parseDouble(strQuantity) * Double.parseDouble(strRate));
													}
													mapError = createBOQFromFileData(context,args,mapAllowedTypeInfo,strSOR,ItemCode, mapAttribute,strParentId, mapFileData);
													if(!mapError.isEmpty())
													{
														errorList.add(mapError);
													}
												}
											}
										}
									}
									if(strListLog.size() == 0)
									{										
										//Check Total BOQ Value 
										dTotalBOQValue = getTotalBOQValue(context,domWO,slAlreadyExistingBOQs,dTotalBOQValue);
										if(dTotalBOQValue > Double.parseDouble(strContractValue))
										{
											if(dTotalBOQValue > dTotalValue)
											{
												mapError.put("Message",strTotalBOQValueExceed);
												errorList.add(mapError);
												strListLog.add("TotalBOQValueExceed");
												mapEmailInfo.put("TotalBOQValue", String.valueOf(dTotalBOQValue));
												mapEmailInfo.put("ContractRingiAmount", String.valueOf(dTotalValue));
											}
										}
									}
								}
							}
						}else{
							strRevisedBOQStatus = strRevisedBOQStatusFailed;
						}
						if(errorList.size()>0)
						{								
							slErrorList = updateFileWithErrorMsg(context,strWorkspacePath,strFileName,errorList);
						}
						if(slErrorList.size() > 0)
						{
							ContextUtil.abortTransaction(context);
							strRevisedBOQStatus = strRevisedBOQStatusFailed;
						}
						else
						{
							ContextUtil.commitTransaction(context);
							strRevisedBOQStatus = strRevisedBOQStatusSuccess;								
						}
					}
					else
					{
						strRingiStatus = strRingiStatusNotApproved;
						strRevisedBOQStatus = strRevisedBOQStatusFailed;
						mapError.put("Message",strNotApprovedRingiNumberMessage);
						errorList.add(mapError);
						strListLog.add("NotApprovedRingiNumber");
					}
					String  strDocumentId = FrameworkUtil.autoName(context, CommonDocument.SYMBOLIC_type_Document, CommonDocument.SYMBOLIC_policy_Document);								
					if(UIUtil.isNotNullAndNotEmpty(strDocumentId)){
						checkInFile(context, strDocumentId, strFileName, DomainConstants.EMPTY_STRING, context.getSession().getVault(), strWorkspacePath);
						String strRelId = domRevisedBOQ.getInfo(context, "from["+DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"].id");
						if(UIUtil.isNotNullAndNotEmpty(strRelId)){
							DomainRelationship.disconnect(context, strRelId);
						}
						domRevisedBOQ.connect(context,new RelationshipType(DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT),true, DomainObject.newInstance(context,strDocumentId));								
					}
					mapAttributeRevisedBOQ.put(ATTRIBUTE_WMS_RINGI_STATUS, strRingiStatus);	
					mapAttributeRevisedBOQ.put(ATTRIBUTE_WMS_REVISED_BOQ_STATUS, strRevisedBOQStatus);	
					domRevisedBOQ.setAttributeValues(context, mapAttributeRevisedBOQ);
				}
			}
			catch(Exception exception)
			{
				ContextUtil.abortTransaction(context);
				strRevisedBOQStatus = strRevisedBOQStatusFailed;
				if(UIUtil.isNotNullAndNotEmpty(strRevisedBOQId)){
					mapAttributeRevisedBOQ.put(ATTRIBUTE_WMS_REVISED_BOQ_STATUS, strRevisedBOQStatus);
					domRevisedBOQ.setAttributeValues(context, mapAttributeRevisedBOQ);					
				}
				if(strListLog.size()==0){
					strListLog.add("Error");
				}
				exception.printStackTrace();
			}
			finally
			{
				if(UIUtil.isNotNullAndNotEmpty(strRevisedBOQId))
				{
					mapEmailInfo.putAll(mapWOInfo);
					mapEmailInfo.putAll(mapAttributeRevisedBOQ);
					mapEmailInfo.put("logList",strListLog);
					sendMailForRevisedBOQ(context,mapEmailInfo);
				}
			}
			return strListLog;
		}
		/**
		*
		* Method to get existing BOQ Items with same ItemCode
		* @param context the eMatrix <code>Context</code> object
		* @param args
		* @param ItemCode - Item code of SOR Items
		* @return mapListObjects - List of Existing BOQ Item maps
		* @throws Exception 
		*/
		private double getTotalBOQValue(Context context,DomainObject domObjWO,StringList slAlreadyExistingBOQs,double dTotalBOQValue)
					   throws Exception {
			double dBOQValue = 0.0;
			try
			{
				
				String strAlreadyExistingBOQs = FrameworkUtil.join(slAlreadyExistingBOQs, ",");
				String strBusWhere = "!(id matchlist \""+strAlreadyExistingBOQs+"\" \",\")";

				StringList strListBusSelects     = new StringList(3);
				strListBusSelects.add(DomainConstants.SELECT_ID);
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]");
				strListBusSelects.add("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]");

				Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
				patternType.addPattern(TYPE_WMS_SEGMENT);
				patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK);

				MapList mlMeasurmentTasks = (MapList)domObjWO.getRelatedObjects(context,
						RELATIONSHIP_BILL_OF_QUANTITY,                         		 // relationship pattern
						patternType.getPattern(),                                    // object pattern
						false,                                                        // to direction
						true,                                                        // from direction
						(short)0,                                                    // recursion level
						strListBusSelects,                                           // object selects
						null,                                           			 // relationship selects
						strBusWhere,                                // object where clause
						DomainConstants.EMPTY_STRING,                                // relationship where clause
						(short)0,                                                    // No expand limit
						DomainConstants.EMPTY_STRING,                                // postRelPattern
						TYPE_WMS_MEASUREMENT_TASK, 									 // postTypePattern
						null);
				
				if(mlMeasurmentTasks!=null && !mlMeasurmentTasks.isEmpty())
				{
					Map mapTemp = null;
					String strQty = "";
					String strRate = "";
					int iSize = mlMeasurmentTasks.size();
					for(int iCount=0;iCount<iSize;iCount++)
					{
						mapTemp= (Map)mlMeasurmentTasks.get(iCount);
						strQty =(String)mapTemp.get("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]");
						strRate =(String)mapTemp.get("attribute["+ATTRIBUTE_WMS_REDUCED_SOR_RATE+"]");
						dBOQValue = dBOQValue + (Double.parseDouble(strQty) * Double.parseDouble(strRate));
					}					
				}
				dTotalBOQValue = dTotalBOQValue + dBOQValue;
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw exception;
			}
			return dTotalBOQValue;
		}
		/**
		*
		* Method to get existing BOQ Items with same ItemCode
		* @param context the eMatrix <code>Context</code> object
		* @param args
		* @param ItemCode - Item code of SOR Items
		* @return mapListObjects - List of Existing BOQ Item maps
		* @throws Exception 
		*/
		private MapList getExistingBOQs(Context context,String[] args,String ItemCode)
					   throws Exception {
			MapList mapListObjects = new MapList();
			try
			{
				String strWorkOrderId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strWorkOrderId))
				{	
					DomainObject domObjWO = DomainObject.newInstance(context,strWorkOrderId);
					StringList strListBusSelects     = new StringList(3);
					strListBusSelects.add(DomainConstants.SELECT_ID);
					strListBusSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
					strListBusSelects.add("attribute["+ATTRIBUTE_WMS_MBE_QUANTITY+"]");
					strListBusSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_QUANTITY+"]");
					strListBusSelects.add("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]");
					strListBusSelects.add("to["+RELATIONSHIP_WMS_MBE_ACTIVITIES+"].attribute["+ATTRIBUTE_WMS_QTY_SUBMITTED_TILL_DATE+"]");
					
					StringList strListRelSelects     = new StringList(1);
					
					Pattern patternType = new Pattern(TYPE_WMS_MEASUREMENT_TASK);
					patternType.addPattern(TYPE_WMS_SEGMENT);
					patternType.addPattern(TYPE_WMS_MEASUREMENT_BOOK);
					String strObjectWhere = "from["+RELATIONSHIP_WMS_TASK_SOR+"].to.attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]=='"+ItemCode+"'";
					Map mPostWhere =new HashMap();
					mPostWhere.put("from["+RELATIONSHIP_WMS_TASK_SOR+"].to.attribute["+ATTRIBUTE_WMS_MSIL_SOR_ITEM_NUMBER+"]",ItemCode);
					mapListObjects = domObjWO.getRelatedObjects(context,
							RELATIONSHIP_BILL_OF_QUANTITY,                         		 // relationship pattern
							patternType.getPattern(),                                    // object pattern
							false,                                                        // to direction
							true,                                                        // from direction
							(short)0,                                                    // recursion level
							strListBusSelects,                                           // object selects
							null,                                           			 // relationship selects
							DomainConstants.EMPTY_STRING,                                // object where clause
							DomainConstants.EMPTY_STRING,                                // relationship where clause
							(short)0,                                                    // No expand limit
							DomainConstants.EMPTY_STRING,                                // postRelPattern
							TYPE_WMS_MEASUREMENT_TASK, 									 // postTypePattern
							mPostWhere); 
				}
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw exception;
			}
			return mapListObjects;
		}
		
		/**
		*
		* Method to get all revision details of RevisedBOQ
		* @param context the eMatrix <code>Context</code> object
		* @param domRevisedBOQ - DomainObject of RevisedBOQ
		* @param strStatus - RevisedBOQ Status
		* @return mapInfo - map details of previous revision of RevisedBOQ
		* @throws Exception 
		*/		
		private Map getBOQRevisionDetails(Context context,DomainObject domRevisedBOQ,String strStatus)
				   throws Exception {
		Map mapInfo = new HashMap();
		try
		{
			if(domRevisedBOQ != null)
			{	
	            StringList slReviseBOQSelects = new StringList();
	            slReviseBOQSelects.add(DomainConstants.SELECT_ID);
	            slReviseBOQSelects.add(DomainConstants.SELECT_REVISION);
	            slReviseBOQSelects.add("attribute["+ATTRIBUTE_WMS_REVISED_BOQ_STATUS+"]");
	            slReviseBOQSelects.add("attribute["+ATTRIBUTE_WMS_TOTAL_VALUE+"]");
	            MapList mlRevInfo = domRevisedBOQ.getRevisionsInfo(context, slReviseBOQSelects, new StringList());	
	            mlRevInfo.sortStructure(DomainConstants.SELECT_REVISION, "descending", "Integer");
	            mlRevInfo.remove(0);	            
	            int iSize = mlRevInfo.size();
				for(int iCount=0; iCount<iSize; iCount++){
					Map mpInfo = (Map)mlRevInfo.get(iCount);
					String strRevisedBOQStatus = (String)mpInfo.get("attribute["+ATTRIBUTE_WMS_REVISED_BOQ_STATUS+"]");
					if(strRevisedBOQStatus.equals(strStatus)){
						mapInfo = mpInfo;
						break;
					}
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw exception;
		}
		return mapInfo;
	}

		/**
		*
		* Method to check Ringi Number is approved or not
		* @param context the eMatrix <code>Context</code> object
		* @param args
		* @param strRingiNumber - Ringi Number
		* @return bRingiStatus - true or false. If ringi number is approved, return true or false
		* @throws Exception 
		*/		
		public boolean isRingiApproved(Context context,String[] args, String strRingiNumber) throws Exception {
			boolean bRingiStatus = false;
			try
			{
				TDRUtil_mxJPO tdrUtil = new TDRUtil_mxJPO(context, args);
				MapList mlRingiDetails = tdrUtil.getRingiDetailsByRingiNo(context, strRingiNumber);
				if(mlRingiDetails != null && mlRingiDetails.isEmpty() == false) {
					Map ringiDetailMap = (HashMap)mlRingiDetails.get(0);					
					String strStatus = (String)ringiDetailMap.get("RNGI_STATUS");
					if("A".equals(strStatus)) {
						bRingiStatus = true;
					}else {
						bRingiStatus = false;
					}	
				}
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw exception;
			}
			return bRingiStatus;
		}
		
		public int sendMailForRevisedBOQ(Context context, Map mapEmailInfo) throws Exception {
		int iReturn = 0;
		try {
			String strSignature = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(), "WMS.Mail.Signature");
			String strMessage = "";
			String strSubject = "";
			String strErrorMessage = "";
			String sWorkOrderName = "";
			String sRingiNumber = "";
			String sRingiStatus = "";
			String sWorkOrderId = "";
			String strProjectOwner = "";
			if (mapEmailInfo != null && mapEmailInfo.size() > 0) {
				StringList slCCList = new StringList();
				sWorkOrderId = (String)mapEmailInfo.get(DomainObject.SELECT_ID);
				sWorkOrderName = (String)mapEmailInfo.get(DomainObject.SELECT_NAME);
				sRingiNumber = (String)mapEmailInfo.get(ATTRIBUTE_MSIL_RINGI_NUMBER);
				sRingiStatus = (String)mapEmailInfo.get(ATTRIBUTE_WMS_RINGI_STATUS);
				strProjectOwner = (String)mapEmailInfo.get("to["+RELATIONSHIP_WMS_PORJECT_WORK_ORDER+"].from.owner");
				StringList slLogList = (StringList)mapEmailInfo.get("logList");
				StringBuffer sbMessage = new StringBuffer();
				if (slLogList.size() > 0) {
					strErrorMessage = slLogList.get(0).toString();
					strSubject = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strErrorMessage+".Import.FailedSubject");  
					strMessage =  EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS."+strErrorMessage+".Import.FailedBody");
					if(strErrorMessage.equalsIgnoreCase("NotApprovedRingiNumber")){					
						sbMessage.append("\n");
						sbMessage.append("Ringi number ");
						sbMessage.append(" '"+sRingiNumber+"'");
						sbMessage.append(" is not approved");
						sbMessage.append("\n");
					}else if(strErrorMessage.equalsIgnoreCase("InvalidFileFormatRevisedBOQ")){
						sbMessage.append("\n");
						sbMessage.append(strMessage);
						sbMessage.append("\n");
					}else if(strErrorMessage.equalsIgnoreCase("LesserQuantity")){
						String sNewQuantity = (String)mapEmailInfo.get("NewQuantity");
						String sTotalApprovedQuantity = (String)mapEmailInfo.get("TotalApprovedQuantity");
						sbMessage.append("\n");
						sbMessage.append(strMessage);
						sbMessage.append("\n");
						sbMessage.append("New quantity is "+ sNewQuantity);
						sbMessage.append("\n");
						sbMessage.append("Total Approved Quantity is "+ sTotalApprovedQuantity);
						sbMessage.append("\n");
					}else if(strErrorMessage.equalsIgnoreCase("TotalBOQValueExceed")){
						String sTotalBOQValue = (String)mapEmailInfo.get("TotalBOQValue");
						String sContractRingiAmount = (String)mapEmailInfo.get("ContractRingiAmount");
						sbMessage.append("\n");
						sbMessage.append(strMessage);
						sbMessage.append("\n");
						sbMessage.append("Total BOQ Value is "+ sTotalBOQValue);
						sbMessage.append("\n");
						sbMessage.append("Contract Value + Ringi Amount  is "+ sContractRingiAmount);
						sbMessage.append("\n");
					}else{
						strSubject = "";
					}
				}else {
					strSubject = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.ReviseBOQ.Import.SuccessSubject");
					strMessage = EnoviaResourceBundle.getProperty(context,"wmsStringResource", context.getLocale(), "WMS.ReviseBOQ.Import.SuccessBody");
					sbMessage.append("\n");
					sbMessage.append(strMessage);
					sbMessage.append("\n");
					sbMessage.append("WorkOrder :  "+ sWorkOrderName);
					sbMessage.append("\n");
					sbMessage.append("Ringi Number : "+ sRingiNumber);
					sbMessage.append("\n");
				}
				StringList objectIdLists = new StringList();
				objectIdLists.add(sWorkOrderId);
				//For email----Start
				StringBuffer sbHTMLBody = new StringBuffer();
				sbHTMLBody.append("<html>");
				sbHTMLBody.append(
						"<head><style>.datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden;}.datagrid table td, .datagrid table th { padding: 3px 10px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; color:#FFFFFF; font-size: 13px; font-weight: bold; border-left: 1px solid #0070A8; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #00557F; border-left: 1px solid #E1EEF4;font-size: 12px;font-weight: normal; }.datagrid table tbody .alt td { background: #E1EEf4; color: #00557F; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }</style></head>");
				sbHTMLBody.append("<body>");
				sbHTMLBody.append("<div class='datagrid'>");
				sbHTMLBody.append("<b>Dear User , <b> <br><br>");
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append(sbMessage.toString());
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append("<BR>");
				sbHTMLBody.append("<center>");
				sbHTMLBody.append("<table width='100%' border='1'>");
				sbHTMLBody.append("<thead>");
				sbHTMLBody.append(
						"<tr align='center'><th width='10%'><b>Work Order</b></th><th width='15%'><b>Ringi Number</b></th><th width='15%'><b>Ringi Status</b></th></tr>");
				sbHTMLBody.append("</thead>");
				sbHTMLBody.append("<tbody>");				
				sbHTMLBody.append("<tr><th width='10%'>" + sWorkOrderName + "</th><th width='15%'>" + sRingiNumber + "</th><th width='15%'>" + sRingiStatus + "</th></tr>");
				sbHTMLBody.append("</tbody>");
				sbHTMLBody.append("</table>");
				sbHTMLBody.append("<br>Thanks,<br>"+strSignature);
				sbHTMLBody.append("</div>");
				sbHTMLBody.append("</body>");
				sbHTMLBody.append("</html>");
				//For email----End
				
				String fromAgent = context.getUser();
				String notifyType = "both";//iconMail,email,both
				if(UIUtil.isNotNullAndNotEmpty(strSubject)){	
					TDRUtil_mxJPO.sendMail(context, new StringList("adarsh.ak@intelizign.com"), strSubject, sbHTMLBody.toString());
					emxNotificationUtil_mxJPO.sendJavaMail(context, new StringList(strProjectOwner), null, null, strSubject, sbMessage.toString(), sbHTMLBody.toString(), fromAgent, null, objectIdLists, notifyType);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return iReturn;
	}	
}