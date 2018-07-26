 
  <%@page import="com.matrixone.servlet.Framework"%>
<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
  
  
  <%  
  
     matrix.db.Context context = Framework.getFrameContext(session);
     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
     response.setHeader("Pragma", "no-cache");
     response.setDateHeader("Expires", (new java.util.Date()).getTime());
  
    String strRealError          = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.ValueMustBeReal");
    String strLBHRTogetherError  = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.RadiuswithLandB");
    String strOnlyCFactorEror    = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.CoefficentFactorOnly");
    String strRTogetherLnBError  = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.RadiusBeEmpty");
    String strEmptyMeasuError    = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.MeasurementsBeEmpty");
    String strQtyOutOfLimitError = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.QuantityExceeding");
    String strEmptyImage         = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.EmptyImage");
    String strEmptyRemarks       = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.alert.EmptyRemarks");
   String strAdvanceAmountRealWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.Common.ValueMustBeReal");
   String strAmountDecimalPlaceWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.AmountDecimalPlace");
   String strDecimalPlaceWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.DecimalPlace.Warning");
   String strQuantityDecimalPlaceWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.QuantityDecimalPlace");
   String strRateDecimalPlaceWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.RateDecimalPlace");
   String strRateExceedingWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.RateExceedingBOQRate");
   String strTechnicalDeductionQuantityWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.ExecedingBillQuantity");
   String strNegativeValue = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.NegativeValue.Warning");
   String strExceedingPercentageWarning = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.invalidPercentage");
   String strTwoDecimalError = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.FieldValueDecimalPlace");
   String strQtyNegativeError = EnoviaResourceBundle.getProperty(context, "wmsStringResource", context.getLocale(),"WMS.WarningMessage.OverallQtyNegative");
   
   
     %>
   
     
 function isNumber(n){
    return (isNaN(n));
 }
 
 
function calculateTotalQuantity(){
  
    var varValue	= arguments[0];
	var currCell 	= emxEditableTable.getCurrentCell();
	var uid 		= currCell.rowID;
	var varBoolean = false;
	if(varValue == "Yes" || varValue == "No" || varValue == "yes" || varValue == "no")
	{
			varBoolean = true;
	}

	var columnName	= currCell.columnName;
	
	if(!varBoolean)
	{
		if(isNumber(varValue) && columnName!="UWD")
		{
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			//revertCellValue(uid,colName,oldValue);
			alert("<%=strRealError%>");
			return false;
		}
	}
	
	
	
	var varItemRowId 			= emxEditableTable.getParentRowId(uid);
	var varFrequencyCell 		= emxEditableTable.getCellValueByRowId(uid,"MBEFrequency");
	var varBreadthCell 			= emxEditableTable.getCellValueByRowId(uid,"MBEBreadth");
	var varLengthCell 			= emxEditableTable.getCellValueByRowId(uid,"MBELength");
	var varDeductionsCell 		= emxEditableTable.getCellValueByRowId(uid,"MBEDeduction");
	var varDepthCell 			= emxEditableTable.getCellValueByRowId(uid,"MBEDepth");
	var varCHiPSMBERadiusCell 	= emxEditableTable.getCellValueByRowId(uid,"MBERadius");
	var varCOEfficientCell 		= emxEditableTable.getCellValueByRowId(uid,"ItemCoEfficientFactor");
	var varUWD					= emxEditableTable.getCellValueByRowId(uid,"UWD");
	var varItemTotalCell 		= emxEditableTable.getCellValueByRowId(varItemRowId,"MBECost");




	var varCOEfficientValue 	= varCOEfficientCell.value.current.display;
	var varFrequencyValue 		= varFrequencyCell.value.current.display;
	var varBreadthValue 		= varBreadthCell.value.current.display;
	var varLengthValue 			= varLengthCell.value.current.display;
	var varDeductionsValue 		= varDeductionsCell.value.current.display;
	var varDepthValue	 		= varDepthCell.value.current.display;
	var varCHiPSMBERadiusValue	= varCHiPSMBERadiusCell.value.current.display;
	var varItemTotalCellValue	= varItemTotalCell.value.current.display;

	var varUWDDisplay	= varUWD.value.current.display;
	var varUWDArray		= varUWDDisplay.split("-");
	var UWDintegerValue	= varUWDArray[1];
	
 
	var varMeasurement 			= emxEditableTable.getChildrenColumnValues(varItemRowId,"MBECost","1");
	var varMeasurementsLength 	= varMeasurement.length;
	
	if(varItemTotalCellValue==null || varItemTotalCellValue=="")
	{
		varItemTotalCellValue = 0.0;
	}
	if(varCOEfficientValue==null || varCOEfficientValue=="")
	{
		varCOEfficientValue = 0.0;
	}
	if(varFrequencyValue==null || varFrequencyValue=="")
	{
		varFrequencyValue = 0;
	}
	if(varBreadthValue==null|| varBreadthValue=="")
	{
		varBreadthValue = 0.0;
	}
	if(varLengthValue==null|| varLengthValue=="")
	{
		varLengthValue = 0.0;
	}
	if(varCHiPSMBERadiusValue==null|| varCHiPSMBERadiusValue=="")
	{
		varCHiPSMBERadiusValue = 0.0;
	}
	if(varDepthValue==null|| varDepthValue=="")
	{
		varDepthValue = 0.0;
	}
	if(UWDintegerValue==null|| UWDintegerValue=="")
	{
		UWDintegerValue = 1;
	}
	
	//save point #1
	
	varItemTotalCellValue = parseFloat(varItemTotalCellValue);
	varCOEfficientValue = parseFloat(varCOEfficientValue);

	if(isInt(varCOEfficientValue))
	{
		varCOEfficientValue	=	varCOEfficientValue + ".0";
	}
	//UAT change Ravi 19June 2018
	/*if(!isInt(varFrequencyValue))
	{
		alert("Please enter Integer Value");
		var oldValue = currCell.value.old.display;
		var columnName = getColumn();
		var colName = columnName.name;
		revertCellValue(uid,colName,oldValue);	
		return false;
	}
	*/

	varFrequencyValue = parseFloat(varFrequencyValue);

	varBreadthValue = parseFloat(varBreadthValue);
	varLengthValue = parseFloat(varLengthValue);
	varDepthValue = parseFloat(varDepthValue);
	UWDintegerValue = parseFloat(UWDintegerValue);

	
	varCHiPSMBERadiusValue = parseFloat(varCHiPSMBERadiusValue);	
	//On reset of all values
	if(varLengthValue==0.0 && varBreadthValue==0.0 && varDepthValue==0.0 && varCHiPSMBERadiusValue==0.0 && varFrequencyValue==0.0 && varCOEfficientValue ==0.0)
	{
		emxEditableTable.setCellValueByRowId(uid,"MBECost",0.0,0.0);
		return true;
	}
	if(varLengthValue<0.0 || varBreadthValue<0.0 || varDepthValue<0.0 || varCHiPSMBERadiusValue<0.0 || varFrequencyValue<0.0 )
	{
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);			
			alert("<%=strRealError%>");
			return false;	
	}
	
	if(varLengthValue>0.0 || varBreadthValue>0.0 || varDepthValue>0.0 || varCHiPSMBERadiusValue>0.0 || varFrequencyValue>0.0 )
	{
		var CurrentCell = trim(arguments[0]);
		var currCell 	= emxEditableTable.getCurrentCell();
		var oldValue = currCell.value.old.display;
		var uid 		= currCell.rowID;
		var columnName = getColumn();
		var colName = columnName.name;
		if (CurrentCell.indexOf(".") > -1) 
		{
			var decimalValue=(CurrentCell).substr(CurrentCell.indexOf(".")+1,CurrentCell.length - 1);
			if (decimalValue.length > 3 && colName!="UWD") 
			{
				alert("Value Must Be Upto Three Decimal Only");
				revertCellValue(uid,colName,oldValue);
				return false;
			}
		}
	}
	
	if(varLengthValue>0.0 && varBreadthValue>0.0&& varDepthValue>0.0 && varCHiPSMBERadiusValue>0.0 )
	{
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);
			alert("<%=strLBHRTogetherError%>");
			return false;	
	}
	if( varCHiPSMBERadiusValue>0.0 )
	{
		if(varLengthValue>0.0 || varBreadthValue>0.0)
		{
		    var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);			
			alert("<%=strLBHRTogetherError%>");
			return false;
		}
	}
	if(varDeductionsValue == "no" || varDeductionsValue == "FALSE")
	{
		varDeductionsValue = 1;
	}
	else
	{
		varDeductionsValue = -1;
	}
	if(varDeductionsValue==null)
	{
		varDeductionsValue = 1;
	}	
	if((varLengthValue==0) && (varBreadthValue==0) && (varDepthValue==0) && (varCHiPSMBERadiusValue==0) && (varFrequencyValue==0))
	{
	
		if(varCOEfficientValue >0)
		{
			alert("<%=strOnlyCFactorEror%>");
		}
		else
		{
			alert("<%=strRTogetherLnBError %>");
		}		
		var oldValue = currCell.value.old.display;
		var columnName = getColumn();
		var colName = columnName.name;
		revertCellValue(uid,colName,oldValue);
		return false;
	}
 	else
	{
		var varQuantity = 0;
         
                    if(varLengthValue==0)
                    {
                        varLengthValue = 1;
                    }
                    if(varBreadthValue==0)
                    {
                        varBreadthValue = 1;
                    }
                    if(varDepthValue==0)
                    {
                        varDepthValue = 1;
                    }
    	if(varLengthValue>0 || varBreadthValue>0 || varDepthValue>0)
    	{		 	           		
            varQuantity = ((varDepthValue*varBreadthValue*varLengthValue));
            

    	}
		if(varCHiPSMBERadiusValue>0 )
		{	
			if( varDepthValue>0)
			{						
				varQuantity = 3.14*varCHiPSMBERadiusValue*varCHiPSMBERadiusValue*varDepthValue;		
			}
			else
			{
				varQuantity = 3.14*varCHiPSMBERadiusValue*varCHiPSMBERadiusValue;
			}
		}
		if(varQuantity==0)
		{
			varQuantity = 1;
		}
		if(varFrequencyValue==0)
		{
			varFrequencyValue = 1;
		}
		if(varCOEfficientValue==0 || isNaN(varCOEfficientValue) )
		{
			varCOEfficientValue = 1;
		}
		varQuantity = varQuantity*varCOEfficientValue*varFrequencyValue*varDeductionsValue*UWDintegerValue;
		var varMeasurementQuantityCell = emxEditableTable.getCellValueByRowId(uid,"MBECost");
		var varMeasurementQuantityCellValue   	= varMeasurementQuantityCell.value.current.display;
		if(varMeasurementQuantityCellValue==null)
		{
			varMeasurementQuantityCellValue = 0;
		}
		varMeasurementQuantityCellValue = parseFloat(varMeasurementQuantityCellValue);
		
			var varCalculatedTotal 		= 0.0;
		for(var m=0; m< varMeasurementsLength; m++)
		{
			var varMeasurementTotalcell = varMeasurement[m];
			var varNewMeasurementTotal = varMeasurementTotalcell.getAttribute("newA");
			var varOldMeasurementTotal = varMeasurementTotalcell.getAttribute("a")
			if(varNewMeasurementTotal == null)
			{
				varCalculatedTotal = varCalculatedTotal + parseFloat(varOldMeasurementTotal);
			}
			else
			{
				varCalculatedTotal = varCalculatedTotal + parseFloat(varNewMeasurementTotal);
			}
		}
		varCalculatedTotal = varCalculatedTotal+(varQuantity-varMeasurementQuantityCellValue);
		varCalculatedTotal = varCalculatedTotal.toFixed(3);
		var varItemTotalQuantityCell 	= emxEditableTable.getCellValueByRowId(varItemRowId,"TotalQuantity");
		var varItemMBEQuantityCell 	 	= emxEditableTable.getCellValueByRowId(varItemRowId,"MBEQuantity");
		var varItemTotalQuantityCellValue 	= varItemTotalQuantityCell.value.current.display;
		var varItemMBEQuantityCellValue   	= varItemMBEQuantityCell.value.current.display;
		if(varItemTotalQuantityCellValue==null)
		{
			varItemTotalQuantityCellValue = 0;
		}
		if(varItemMBEQuantityCellValue==null)
		{
			varItemMBEQuantityCellValue = 0;
		}
		varItemTotalQuantityCellValue = parseFloat(varItemTotalQuantityCellValue);
		varItemMBEQuantityCellValue = parseFloat(varItemMBEQuantityCellValue);
		 var  vBalancedQty =       1.2 * (varItemTotalQuantityCellValue-varItemMBEQuantityCellValue);
		if(varDeductionsValue == "no")
		{
		     // get 120% of TotalMBE(Submitted so far ) + MBECost(Current)
		     
		    
			//if( varCalculatedTotal > (varItemTotalQuantityCellValue - varItemMBEQuantityCellValue))
			
			// old if( Math.abs(varCalculatedTotal) > Math.abs(varItemTotalQuantityCellValue - varItemMBEQuantityCellValue).toFixed(3))
			if(Math.abs(varCalculatedTotal) > Math.abs(vBalancedQty).toFixed(3))
			{
				var oldValue = currCell.value.old.display;
				var columnName = getColumn();
				var colName = columnName.name;			
				revertCellValue(uid,colName,oldValue);			
				alert("<%=strQtyOutOfLimitError %>");
				return false;
			}
			else
			{
				var num = varQuantity;
				var valueToSetInMBECost = num.toFixed(2);
				emxEditableTable.setCellValueByRowId(uid,"MBECost",valueToSetInMBECost ,valueToSetInMBECost);
				emxEditableTable.setCellValueByRowId(varItemRowId,"MBECost",varCalculatedTotal,varCalculatedTotal);
			}
		}
		else
		{
			
			
			//if( Math.abs(varCalculatedTotal) > Math.abs(varItemTotalQuantityCellValue - varItemMBEQuantityCellValue))
			// old ravi if( Math.abs(varCalculatedTotal) > Math.abs(varItemTotalQuantityCellValue - varItemMBEQuantityCellValue).toFixed(3))
			
			//UAT Change Ravi: 19 June18
	   	  var vDIff =  parseFloat(varCalculatedTotal)+parseFloat(varItemMBEQuantityCellValue);
		  if(vDIff<0){
		  	  alert("<%=strQtyNegativeError %>");
		  	   revertCellValue(uid,colName,oldValue);	
		  	   return false;
		     }
			else if(Math.abs(varCalculatedTotal) > Math.abs(vBalancedQty).toFixed(3))
			{
				var oldValue = currCell.value.old.display;
				var columnName = getColumn();
				var colName = columnName.name;			
				revertCellValue(uid,colName,oldValue);			
				alert("<%=strQtyOutOfLimitError %>");
				return false;
			}
			else
			{
				var num = varQuantity;
				var valueToSetInMBECost = num.toFixed(3);
				emxEditableTable.setCellValueByRowId(uid,"MBECost",valueToSetInMBECost ,valueToSetInMBECost);
				emxEditableTable.setCellValueByRowId(varItemRowId,"MBECost",varCalculatedTotal,varCalculatedTotal);
			}
		}
	}
	return true;
}
function revertCellValue(rowId,colName,OldValue)
    {
        var row       = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + rowId + "']");
        var objColumn = colMap.getColumnByName(colName);
        var colIndex  = objColumn.index;
        var oldColumn = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + rowId + "']/c["+colIndex+"]");
        oldColumn.setAttribute("newA",OldValue);
        oldColumn.setAttribute("edited","false");
        emxUICore.setText(oldColumn,OldValue);
        currentCell.target.innerHTML= OldValue;
        updatePostXML(row, OldValue , colIndex);
    }	
function setQuantityAsEditable(varObj)
	{
		var varValue	= arguments[0];
		var currCell 	= emxEditableTable.getCurrentCell();
		var uid 		= currCell.rowID;
		var rowNode 	= emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + uid + "']");
		var objectId 	= rowNode.getAttribute("o");
		var relId 		= rowNode.getAttribute("r");
		var varImageCell  = emxEditableTable.getCellValueByRowId(uid,"Image");	
		var varRemarkCell  = emxEditableTable.getCellValueByRowId(uid,"Remarks");	
		var varEmptyImage =false;
		var varEmptyRemark =false;
		
		if(!(varRemarkCell == null )) 
		{	
			var varRemarkCellValue 	= varRemarkCell.value.current.display;
			if(varRemarkCellValue==null||varRemarkCellValue=="")
			{
				varEmptyRemark = true;
			}
		}			
		if(!(varImageCell == null )) 
		{	
			var varImageCellValue 	= varImageCell.value.current.actual;
			if(varImageCellValue==null||varImageCellValue=="")
			{
				varEmptyImage = true;
			}
		}
		if(!(varEmptyImage||varEmptyRemark))
		{
			if(varValue  =="Yes" || varValue  =="yes")
			{
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBECost",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEFrequency",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBELength",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEBreadth",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEDepth",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBERadius",false);				
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"ItemCoEfficientFactor",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"UWD",false);
				emxEditableTable.setCellValueByRowId(uid,"MBEFrequency","0","0");
				emxEditableTable.setCellValueByRowId(uid,"MBELength","0","0");
				emxEditableTable.setCellValueByRowId(uid,"MBEBreadth","0","0");
				emxEditableTable.setCellValueByRowId(uid,"MBEDepth","0","0");
				emxEditableTable.setCellValueByRowId(uid,"MBERadius","0","0");
				emxEditableTable.setCellValueByRowId(uid,"ItemCoEfficientFactor","0","0");
				emxEditableTable.setCellValueByRowId(uid,"UWD"," "," ");
			}
			else
			{
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBECost",false);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEFrequency",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBELength",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEBreadth",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBEDepth",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"MBERadius",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"ItemCoEfficientFactor",true);
				emxEditableTable.setCellEditableByObjectRelId(relId,objectId,"UWD",true);
				emxEditableTable.setCellValueByRowId(uid,"MBECost","0","0");
			}
		}
		else
		{
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);
			
			if(varEmptyImage)
			{
			alert("<%=strEmptyImage%>");
			}
			else if(varEmptyRemark)
			{
			alert("<%=strEmptyRemarks%>");	
			}			
			return false;
			
		}
	return true;
	
	
}
 

function updateTotalQuantity(varObj)
{
	
    var varValue	= arguments[0];
	var currCell 	= emxEditableTable.getCurrentCell();
	var uid 		= currCell.rowID;
	var varItemRowId = emxEditableTable.getParentRowId(uid);
	
	if(isNumber(varValue))
	{
		var oldValue = currCell.value.old.display;
		var columnName = getColumn();
		var colName = columnName.name;
		revertCellValue(uid,colName,oldValue);
		alert("<%=strRealError%>");
		return false;
	}
	var varItemTotalCell 		 	= emxEditableTable.getCellValueByRowId(varItemRowId,"MBECost");	
	var varItemTotalQuantityCell 	= emxEditableTable.getCellValueByRowId(varItemRowId,"TotalQuantity");
	var varItemMBEQuantityCell 	 	= emxEditableTable.getCellValueByRowId(varItemRowId,"MBEQuantity");
	var varDeductionsCell 			= emxEditableTable.getCellValueByRowId(uid,"MBEDeduction");
	
	if(!(varItemTotalCell == null || varItemTotalQuantityCell ==null || varItemMBEQuantityCell==null|| varDeductionsCell == null)) 
	{

		var varItemTotalQuantityCellValue 	= varItemTotalQuantityCell.value.current.display;
		var varItemMBEQuantityCellValue   	= varItemMBEQuantityCell.value.current.display;
		var varItemDeductionsCellValue   	= varDeductionsCell.value.current.display;
		var varItemTotalCellValue   		= varItemTotalCell.value.current.display;
		if(varItemTotalCellValue==null)
		{
			varItemTotalCellValue = 0;
		}
		if(varItemTotalQuantityCellValue==null)
		{
			varItemTotalQuantityCellValue = 0;
		}
		if(varItemMBEQuantityCellValue==null)
		{
			varItemMBEQuantityCellValue = 0;
		}
		if(varItemDeductionsCellValue==null)
		{
			varItemDeductionsCellValue = 1;
		}
		else if(varItemDeductionsCellValue == "No" || varItemDeductionsCellValue == "FALSE")
		{
			varItemDeductionsCellValue = 1;
		}
		else 
		{
			varItemDeductionsCellValue = -1;
		}

		varItemTotalQuantityCellValue 	= parseFloat(varItemTotalQuantityCellValue);
		varItemMBEQuantityCellValue 	= parseFloat(varItemMBEQuantityCellValue);
		var varMeasurement 				= emxEditableTable.getChildrenColumnValues(varItemRowId,"MBECost","1");
		var varMeasurementsLength 		= varMeasurement.length;
		var varCalculatedTotal 			= 0.0;
			
		for(var m=0; m< varMeasurementsLength; m++)
		{
			var varMeasurementTotalcell 	= varMeasurement[m];
			var varNewMeasurementTotal 		= varMeasurementTotalcell.getAttribute("newA");
			var varOldMeasurementTotal 		= varMeasurementTotalcell.getAttribute("a")
			if(varNewMeasurementTotal == null)
			{
				varCalculatedTotal = varCalculatedTotal + parseFloat(varOldMeasurementTotal);
			}
			else
			{
				varCalculatedTotal = varCalculatedTotal + parseFloat(varNewMeasurementTotal);
			}
		}
		varItemTotalQuantityCellValue = parseFloat(varItemTotalQuantityCellValue);
		varItemMBEQuantityCellValue = parseFloat(varItemMBEQuantityCellValue);
		
		//if( varCalculatedTotal > (varItemTotalQuantityCellValue - varItemMBEQuantityCellValue))
		
		var  vBalancedQty =       1.2 * (varItemTotalQuantityCellValue-varItemMBEQuantityCellValue);
		//old one ravi if( Math.abs(varCalculatedTotal) > Math.abs(varItemTotalQuantityCellValue - varItemMBEQuantityCellValue).toFixed(3))
		if( Math.abs(varCalculatedTotal) > Math.abs(vBalancedQty).toFixed(3))
		{
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);
			revertCellValue(varItemRowId,"MBECost",varItemTotalCellValue);
			alert("<%=strQtyOutOfLimitError %>"+vBalancedQty);
			return false;
		}
		else
		{
			emxEditableTable.setCellValueByRowId(varItemRowId,"MBECost",varCalculatedTotal,varCalculatedTotal);
			return true;
		}
	}
}

  
function isFloat(n){
    return Number(n) === n && n % 1 !== 0;
 }


function isInt(n) {
   return n % 1 === 0;
}
function validateTechnicalDeductionQuantity()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;
    //getting cell
    var varDeductionQuantityCell        = emxEditableTable.getCellValueByRowId(uid,"TechnicalDeductionQuantity");
    var varDeductionRateCell            = emxEditableTable.getCellValueByRowId(uid,"TechnicalDeductionRate");
    var varDeductionAmountCell          = emxEditableTable.getCellValueByRowId(uid,"TechnicalDeductionAmount");
    var varBillQuantityCell             = emxEditableTable.getCellValueByRowId(uid,"ItemBillQuantity");
    var varItemBOQRateCell              = emxEditableTable.getCellValueByRowId(uid,"ItemBOQRate");

    //getting cell value
    var varDeductionQuantityCellValue   = varDeductionQuantityCell.value.current.display;    
    var varDeductionRateCellValue       = varDeductionRateCell.value.current.display;
    var varDeductionAmountCellValue     = varDeductionAmountCell.value.current.display;
    var varBillQuantityCellValue        = varBillQuantityCell.value.current.display;
    var varItemBOQRateCellValue         = varItemBOQRateCell.value.current.display;

    //Parsing values
    varDeductionQuantityCellValue       = parseFloat(varDeductionQuantityCellValue);
    varDeductionRateCellValue           = parseFloat(varDeductionRateCellValue);
    varDeductionAmountCellValue         = parseFloat(varDeductionAmountCellValue);
    varBillQuantityCellValue            = parseFloat(varBillQuantityCellValue);
    varItemBOQRateCellValue             = parseFloat(varItemBOQRateCellValue);

        if (CurrentCell.indexOf(".") > -1) 
        {
            var columnName      = getColumn();
            var colName         = columnName.name;
            var decimalValue=(CurrentCell).substr(CurrentCell.indexOf(".")+1,CurrentCell.length - 1);
            if (decimalValue.length > 3 && "TechnicalDeductionQuantity" == colName) 
            {
                alert("you can enter upto three decimal only");
                revertCellValue(uid,colName,oldValue);
                return false;
            }
            else
            {
                if (decimalValue.length > 2 && "TechnicalDeductionRate" == colName) 
                {
                    alert("<%=strDecimalPlaceWarning%>");
                    revertCellValue(uid,colName,oldValue);
                    return false;
                }
                if(isNaN(CurrentCell))
                {
                    alert("<%=strAdvanceAmountRealWarning%>");
                    
                    revertCellValue(uid,colName,oldValue);
                    return false;
                }
                if(varItemBOQRateCellValue == null)
                {
                    varItemBOQRateCellValue = parseFloat(0.0);
                }
                if(varBillQuantityCellValue == null)
                {
                    varBillQuantityCellValue = parseFloat(0.0);
                }
                if(varDeductionQuantityCellValue == null)
                {
                    varDeductionQuantityCellValue = parseFloat(0.0);
                }
                if(varDeductionAmountCellValue == null)
                {
                    varDeductionAmountCellValue = parseFloat(0.0);
                }
                if(varDeductionRateCellValue == null){
                    varDeductionRateCellValue = parseFloat(0.0);
                }
                if(varDeductionRateCellValue < 0)
                {
                    var oldValue = varDeductionRateCell.value.old.display;
                    var columnName = getColumn();
                    var colName = columnName.name;
                    emxEditableTable.setCellValueByRowId(uid,colName,oldValue,oldValue);
                    alert("please enter positive value");
                }
                if(varDeductionQuantityCellValue < 0)
                {
                    alert("please enter positive value");
                    var oldValue        = varDeductionQuantityCell.value.old.display;
                    var columnName      = getColumn();
                    var colName         = columnName.name;
                    emxEditableTable.setCellValueByRowId(uid,colName,oldValue,oldValue);
                }
                var varDecimalCount = 0;
                var varTempValue    = varDeductionQuantityCell.value.current.display
                var varTemp         = varTempValue.split(".");
                if(varTemp.length>=2)
                {
                    varDecimalCount = varTemp[1].length
                }
                if(varDecimalCount>3&&varDecimalCount!=0)
                {
                    alert("<%=strQuantityDecimalPlaceWarning%>");
                    var objColumn       = colMap.getColumnByName("TechnicalDeductionQuantity");
                    var varOldValue     = objColumn.value.old.display;
                    revertCellValue(uid,"TechnicalDeductionQuantity",varOldValue);
                    return false;
                }
                varDecimalCount = 0;
                varTempValue    = varDeductionRateCell.value.current.display
                var varTemp1    = varTempValue.split(".");
                if(varTemp1.length>=2)
                {
                    varDecimalCount = varTemp1[1].length
                }
                if(varDecimalCount>2&&varDecimalCount!=0)
                {
                    alert("<%=strRateDecimalPlaceWarning%>");
                    var objColumn   = colMap.getColumnByName("TechnicalDeductionRate");
                    var varOldValue = objColumn.value.old.display;
                    revertCellValue(uid,"TechnicalDeductionRate",varOldValue);
                    return false;
                }
                if(varDeductionRateCellValue>varItemBOQRateCellValue)
                {
                    alert("<%=strRateExceedingWarning%>");
                    revertCellValue(uid,"TechnicalDeductionRate",varItemBOQRateCellValue);
                    return false;
                }
                var aAllRows = emxUICore.selectNodes(oXML, "/mxRoot/rows/r");
                var varNoOFRows = aAllRows.length;
                varNoOFRows = varNoOFRows-1;
                var varCumDeductedQuantity = 0;
               for(var i = 0; i <varNoOFRows; i++) 
               {
                  var varRowID =  aAllRows[i].getAttribute("id");
                  
                var varDeductedQuantity = emxEditableTable.getCellValueByRowId(varRowID,"TechnicalDeductionQuantity");
                var varDeductedQuantityValue     = varDeductedQuantity.value.current.display;    
                if(varDeductedQuantityValue == null)
                {
                    varDeductedQuantityValue = parseFloat(0.0);
                }
                    varDeductedQuantityValue         = parseFloat(varDeductedQuantityValue);
                  varCumDeductedQuantity = varCumDeductedQuantity+varDeductedQuantityValue;
               }
                if( (varCumDeductedQuantity>varBillQuantityCellValue)||(varDeductionQuantityCellValue>varBillQuantityCellValue))
                {
                    alert("<%=strTechnicalDeductionQuantityWarning%>");
                    revertCellValue(uid,colName,oldValue);
                    return false;
                }
                
                if(varDeductionRateCellValue>0 && varDeductionQuantityCellValue>0)
                {
                    var varFinalAmount = (varItemBOQRateCellValue-varDeductionRateCellValue) * varDeductionQuantityCellValue;
                    varFinalAmount = varFinalAmount.toFixed(2);
                    emxEditableTable.setCellValueByRowId(uid,"TechnicalDeductionAmount",varFinalAmount,varFinalAmount);
                }
            }
        }
        return false;
}
function validateTechnicalDeductionAmount()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;
    if(isNaN(CurrentCell))
    {
        alert("<%=strAdvanceAmountRealWarning%>");
        revertCellValue(uid,colName,oldValue);
        return false;
    }
    var varDeductionAmountCell      = emxEditableTable.getCellValueByRowId(uid,"TechnicalDeductionAmount");
    var varDeductionAmountCellValue = varDeductionAmountCell.value.current.display;
    var varDecimalCount = 0;
    var varTemp         = varDeductionAmountCellValue.split(".");
    if(varTemp.length>=2)
    {
        varDecimalCount = varTemp[1].length
    }
    if(varDecimalCount>2)
    {
        alert("<%=strAmountDecimalPlaceWarning%>");
        var objColumn       = colMap.getColumnByName("TechnicalDeductionRate");
        var varOldValue     = objColumn.value.old.display;
        revertCellValue(uid,"TechnicalDeductionRate",varOldValue);
        return false;
    }
    return true;
}
//function called On the Recovery Table
function calculateRecoveryAmount()
{
	var varValue	= arguments[0];
	var currCell 	= emxEditableTable.getCurrentCell();
	var rowId 		= currCell.rowID;
	var columnName = getColumn();
	var colName = columnName.name;
	var varRecoveryEntryCell             = emxEditableTable.getCellValueByRowId(rowId,"RecoveryEntry");
	var varRecoveryInterestCell             = emxEditableTable.getCellValueByRowId(rowId,"RecoveryInterest");
	var varRecoveryInCurrentCell             = emxEditableTable.getCellValueByRowId(rowId,"RecoveryInCurrent");
	var varAdvanceAmountCell                 = emxEditableTable.getCellValueByRowId(rowId,"AdvanceAmount");
	var varRecoveryTillPrevious             = emxEditableTable.getCellValueByRowId(rowId,"RecoveryTillPrevious");
	var varRecoveryEntryCellValue       = varRecoveryEntryCell.value.current.display;
	var varvarRecoveryInterestCellValue       = varRecoveryInterestCell.value.current.display;
	var varRecoveryInCurrentCellValue = parseFloat(varRecoveryEntryCellValue)+((parseFloat(varRecoveryEntryCellValue)*parseFloat(varvarRecoveryInterestCellValue))/100);
	var varAdvanceAmountCellValue           = varAdvanceAmountCell.value.current.display;
	var varRecoveryTillPreviousValue           = varRecoveryTillPrevious.value.current.display;    
	emxEditableTable.setCellValueByRowId(rowId,"RecoveryInCurrent",varRecoveryInCurrentCellValue,varRecoveryInCurrentCellValue);
	return true;
}
function validateInterest(obj)
{		
	var varValue	= arguments[0];
	var currCell 	= emxEditableTable.getCurrentCell();
	var uid 		= currCell.rowID;
	var vAmount = emxEditableTable.getCellValueByRowId(uid,"RecoveryEntry");

	if(isNaN(varValue)){
		alert('<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.ValueMustBeReal</emxUtil:i18nScript>');
		obj.value = "";
		var oldValue = currCell.value.old.display;
		var columnName = getColumn();
		var colName = columnName.name;
		revertCellValue(uid,colName,oldValue);
		return false;
	}
	else
	{
		if(varValue==null)
		{
			varValue = 0;
		}
		varValue = parseFloat(varValue);
		vAmount = parseFloat(vAmount.value.current.display);
		if(varValue<=0)
		{
			alert("Entered interest should be more than zero");			
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);
			return false;
		}
		else
		{
			var vPerAmount = Math.round(varValue*vAmount)/100;	
			var vCurrentBillAmount=vAmount+vPerAmount;	
			emxEditableTable.setCellValueByRowId(uid,"RecoveryInCurrent",vCurrentBillAmount,vCurrentBillAmount);
		}
	}
	return true;
}
function validateFieldChange()
    {
        var CurrentCell = trim(arguments[0]);
		var currCell 	= emxEditableTable.getCurrentCell();
		var oldValue = currCell.value.old.display;
		var uid 		= currCell.rowID;
		var columnName = getColumn();
		var colName = columnName.name;
		

        if (CurrentCell!="")
        {
            if(isNaN(CurrentCell) || parseFloat(CurrentCell)<0)
            {
                alert("<%=strAdvanceAmountRealWarning%>");
				revertCellValue(uid,colName,oldValue);
                return false;
            }
			else 
			{
                if (CurrentCell.indexOf(".") > -1) 
                {
                    var decimalValue=(CurrentCell).substr(CurrentCell.indexOf(".")+1,CurrentCell.length - 1);
                    if (decimalValue.length > 2) 
                    {
                        alert("<%=strDecimalPlaceWarning%>");
						revertCellValue(uid,colName,oldValue);
                        return false;
                    }
                }
				var vTableName = emxUICore.selectSingleNode(oXML,"//requestMap/setting[@name='table']/text()");
           	 }
		}
        return true;
    }
function CurrentAmountCheck(obj)
{		
	var varValue	= arguments[0];
	var currCell 	= emxEditableTable.getCurrentCell();
	var uid 		= currCell.rowID;
	var vAmount = emxEditableTable.getCellValueByRowId(uid,"RecoveryEntry");
	var vAdvanceAmount = emxEditableTable.getCellValueByRowId(uid,"AdvanceAmount"); 

	if(isNaN(varValue)){
		alert('<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.ValueMustBeReal</emxUtil:i18nScript>');
		obj.value = "";
		var oldValue = currCell.value.old.display;
		var columnName = getColumn();
		var colName = columnName.name;
		revertCellValue(uid,colName,oldValue);
		return false;
	}
	else
	{
		if(varValue==null)
		{
			varValue = 0;
		}
		varValue = parseFloat(varValue);
		vAmount = parseFloat(vAmount.value.current.display);
		if(varValue<=0)
		{
			alert("Entered interest should be more than zero");			
			var oldValue = currCell.value.old.display;
			var columnName = getColumn();
			var colName = columnName.name;
			revertCellValue(uid,colName,oldValue);
			return false;
		}
		else
		{
			vAdvanceAmount = parseFloat(vAdvanceAmount.value.current.display);
			if(vAmount>vAdvanceAmount)
			{
				alert('Recovery Amount is more than Advance Amount');
				var oldValue = currCell.value.old.display;
				var columnName = getColumn();
				var colName = columnName.name;
				revertCellValue(uid,colName,oldValue);
				return false;
			}
		}
	}
	return true;
}

<!-- Material Management -->

function validationAdvanceRate()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;

    if(CurrentCell != "")
    {
        var value       = parseFloat(CurrentCell);
        if(isNaN(CurrentCell) || parseFloat(CurrentCell)<0)
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            return false;
        }
        else if(value<0 || value>100)
        {
            alert("<%=strExceedingPercentageWarning%>");
            revertCellValue(uid,colName,oldValue);
            return false;
        }
    }
    else
    {
        alert("Enter valid value");
        revertCellValue(uid,colName,oldValue);
    }
}
//Material and Secured Advance Stock Functions
function validationMaterialBillStock()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;

    if(CurrentCell !="")
    {
        if(isNaN(CurrentCell))
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            validationMaterialBillStock(oldValue , uid , colName );
            return false;
        }
        else
        {
            var decimalValue=(CurrentCell).substr(CurrentCell.indexOf(".")+1,CurrentCell.length - 1);
            if (decimalValue.length > 3) 
            {
                alert("<%=strDecimalPlaceWarning%>");
                revertCellValue(uid,colName,oldValue);
                validationMaterialBillStock(oldValue , uid , colName );
                return false;
            }
            else
            {
                var varQuantityCell         = emxEditableTable.getCellValueByRowId(uid,"Quantity");
                var varRatePerUnitCell      = emxEditableTable.getCellValueByRowId(uid,"RatePerUnit");
                var varAmountCell           = emxEditableTable.getCellValueByRowId(uid,"Amount");
                var varSGST                 = emxEditableTable.getCellValueByRowId(uid,"WMSStockSGST");
                var varCGST                 = emxEditableTable.getCellValueByRowId(uid,"WMSStockCGST");

                if(!varSGST)
                {
                    varSGST = "0.0";
                }

                if(!varCGST)
                {
                    varCGST = "0.0";
                }
                var varQuantityCellValue    = varQuantityCell.value.current.display;    
                var varRatePerUnitCellValue = varRatePerUnitCell.value.current.display;
                var varAmountCellValue      = varAmountCell.value.current.display;
              
                   if(varSGST.value){
                     var varSGST                 = varSGST.value.current.display;
                   }else{
                     var varSGST ="0.0";
                   }
               
                if(varCGST.value){
                  var  varCGST                 = varCGST.value.current.display;
                }else{
                  var varCGST   ="0.0";
                }

                //Parsing values
                varQuantityCellValue        = parseFloat(varQuantityCellValue);
                varRatePerUnitCellValue     = parseFloat(varRatePerUnitCellValue);
                varAmountCellValue          = parseFloat(varAmountCellValue);

                 varSGST                    = parseFloat(varSGST);
                 varCGST                    = parseFloat(varCGST);





                var FinalTex                = varSGST+varCGST;

                if(FinalTex==0.0)
                {
                   // FinalTex    = 1; old.bring it if columns are made visible again : Ravi
                   FinalTex=0;
                }

                if((varQuantityCellValue == null) || (varQuantityCellValue== 0) || isNaN(varQuantityCellValue))
                {
                        varQuantityCellValue     = parseFloat(1.0);
                }
                if((varRatePerUnitCellValue == null) || (varRatePerUnitCellValue == 0) || isNaN(varRatePerUnitCellValue ))
                {
                        varRatePerUnitCellValue  = parseFloat(1.0);
                }

                if((varAmountCellValue == null) || (varAmountCellValue == 0) || isNaN(varAmountCellValue ))
                {
                    varAmountCellValue      = parseFloat(0.0);
                }

                if(varQuantityCellValue < 0 || varRatePerUnitCellValue <0 || varAmountCellValue <0)
                {
                    revertCellValue(uid,colName,oldValue);
                    validationMaterialBillStock(oldValue , uid , colName );
                    alert("<%=strNegativeValue%>");
                    return false;
                }

                var varFinalAmount  = varQuantityCellValue * varRatePerUnitCellValue;
                varFinalAmount      = Math.round(varFinalAmount * 100) / 100;
                var totalTexValue   = varFinalAmount*FinalTex;
                if(!totalTexValue)
                {
                    totalTexValue=0;
                }
                totalTexValue       = Math.round(totalTexValue) / 100;
                varFinalAmount      = varFinalAmount+totalTexValue;
                if(varFinalAmount)
                emxEditableTable.setCellValueByRowId(uid,"Amount",varFinalAmount,varFinalAmount);
            }
        }
    }
    else
    {
        revertCellValue(uid,colName,oldValue);
        validationMaterialBillStock(oldValue , uid , colName);
    }
}

function addCGSTValue()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;

    if(CurrentCell != "")
    {
        var value       = parseFloat(CurrentCell);
        if(isNaN(CurrentCell) || parseFloat(CurrentCell)<0)
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            addCGSTValue(oldValue , uid , colName );
            return false;
        }
        else if(value<0 || value>100)
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            addCGSTValue(oldValue , uid , colName );
            return false;
        }
        else
        {
            var varQuantityCell         = emxEditableTable.getCellValueByRowId(uid,"Quantity");
            var varRatePerUnitCell      = emxEditableTable.getCellValueByRowId(uid,"RatePerUnit");
            var varSGST                 = emxEditableTable.getCellValueByRowId(uid,"WMSStockSGST");


            var varQuantityCellValue    = varQuantityCell.value.current.display;
            var varRatePerUnitCellValue = varRatePerUnitCell.value.current.display;
           var varSGST  ="0.0";
           if(varSGST.value){
               varSGST                 = varSGST.value.current.display;
            }

            if(varSGST=="")
            {
                varSGST =  "0.0";
            }

            //Parsing values
            varQuantityCellValue        = parseFloat(varQuantityCellValue);
            varRatePerUnitCellValue     = parseFloat(varRatePerUnitCellValue);
            varSGST                     = parseFloat(varSGST);
            var currentValue            = parseFloat(CurrentCell);


            if((varQuantityCellValue == null) || (varQuantityCellValue== 0) || isNaN(varQuantityCellValue))
            {
                varQuantityCellValue     = parseFloat(1.0);
            }
            if((varRatePerUnitCellValue == null) || (varRatePerUnitCellValue == 0) || isNaN(varRatePerUnitCellValue ))
            {
                varRatePerUnitCellValue  = parseFloat(1.0);
            }
            if(varQuantityCellValue < 0 || varRatePerUnitCellValue <0 )
            {
                revertCellValue(uid,colName,oldValue);
                addCGSTValue(oldValue , uid , colName );
                alert("<%=strNegativeValue%>");
                return false;
            }
            var varFinalAmount      = varQuantityCellValue * varRatePerUnitCellValue;
            varFinalAmount          = Math.round(varFinalAmount * 100) / 100;
            var TotalTex            = varSGST+currentValue;
            var totalFinalValue     = (TotalTex*varFinalAmount);
            var texValueAfterAmount  = Math.round(totalFinalValue ) / 100;
            varFinalAmount           = varFinalAmount+texValueAfterAmount;
            emxEditableTable.setCellValueByRowId(uid,"Amount",varFinalAmount,varFinalAmount);
        }
    }
    else
    {
        alert("Enter valid value");
        revertCellValue(uid,colName,oldValue);
        addCGSTValue(oldValue , uid , colName );
    }
}

function addSGSTValue()
{
    var CurrentCell     = trim(arguments[0]);
    var currCell        = emxEditableTable.getCurrentCell();
    var oldValue        = currCell.value.old.display;
    var uid             = currCell.rowID;
    var columnName      = getColumn();
    var colName         = columnName.name;

    if(CurrentCell != "")
    {
        var value       = parseFloat(CurrentCell);
        if(isNaN(CurrentCell) || parseFloat(CurrentCell)<0)
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            addSGSTValue(oldValue , uid , colName );
            return false;
        }
        else if(value<0 || value>100)
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            addSGSTValue(oldValue , uid , colName );
            return false;
        }
        else
        {
            var varQuantityCell         = emxEditableTable.getCellValueByRowId(uid,"Quantity");
            var varRatePerUnitCell      = emxEditableTable.getCellValueByRowId(uid,"RatePerUnit");
            var varCGST                 = emxEditableTable.getCellValueByRowId(uid,"WMSStockCGST");


            var varQuantityCellValue    = varQuantityCell.value.current.display;
            var varRatePerUnitCellValue = varRatePerUnitCell.value.current.display;
             var varCGST ="0.0"
             if(varCGST.value){
                 var   varCGST                 = varCGST.value.current.display;
              }else{
                 var varCGST ="0.0";
              }

            if(varCGST=="")
            {
                varCGST =  "0.0";
            }

            //Parsing values
            varQuantityCellValue        = parseFloat(varQuantityCellValue);
            varRatePerUnitCellValue     = parseFloat(varRatePerUnitCellValue);
            varCGST                     = parseFloat(varCGST);
            var currentValue            = parseFloat(CurrentCell);


            if((varQuantityCellValue == null) || (varQuantityCellValue== 0) || isNaN(varQuantityCellValue))
            {
                varQuantityCellValue     = parseFloat(1.0);
            }
            if((varRatePerUnitCellValue == null) || (varRatePerUnitCellValue == 0) || isNaN(varRatePerUnitCellValue ))
            {
                varRatePerUnitCellValue  = parseFloat(1.0);
            }
            if(varQuantityCellValue < 0 || varRatePerUnitCellValue <0 )
            {
                revertCellValue(uid,colName,oldValue);
                addSGSTValue(oldValue , uid , colName );
                alert("<%=strNegativeValue%>");
                return false;
            }
            var varFinalAmount      = varQuantityCellValue * varRatePerUnitCellValue;
            varFinalAmount          = Math.round(varFinalAmount * 100) / 100;

            var TotalTex            = varCGST+currentValue;

            var totalFinalValue     = (TotalTex*varFinalAmount);
            var texValueAfterAmount          = Math.round(totalFinalValue ) / 100;
            varFinalAmount  =   varFinalAmount+texValueAfterAmount;
            emxEditableTable.setCellValueByRowId(uid,"Amount",varFinalAmount,varFinalAmount);
        }
    }
    else
    {
        alert("Enter valid value");
        revertCellValue(uid,colName,oldValue);
        addSGSTValue(oldValue , uid , colName );
    }
}
//Added for Conversion Rate WMSSORViewTable table
function validateValuewithDecimalCheck()
{
    var cellValue     = trim(arguments[0]);
  
    if(isNaN(cellValue))
    {
       alert("<%=strRealError%>");
        return false;
    }
    var rx = /^\d+(?:\.\d{1,2})?$/ 
    if(rx.test(cellValue)) { 
        return true;
    }
    else { 
        alert("<%=strTwoDecimalError %>");
        return false; 
    } 
    
    }
  function reloadMakersList(){
    
      var strSeleMat=arguments[0];
       var vURL = "../wms/wmsAjaxUtil.jsp?objectId="+strSeleMat+"&action=CacheMaterialId";
	  var setData       =  emxUICore.getDataPost(vURL, "");
      emxEditableTable.reloadCell("MBMaker");
         
    }
    
    function reloadInitialMakersList(){
       var currCell        = emxEditableTable.getCurrentCell();
       var rowId             = currCell.rowID;
       var materialCell         = emxEditableTable.getCellValueByRowId(rowId,"MaterialType");
       strSeleMat = materialCell.value.current.actual;
       if(strSeleMat==''){
         strSeleMat=rowId.objectId;
       }
        
      var vURL = "../wms/wmsAjaxUtil.jsp?objectId="+strSeleMat+"&action=CacheMaterialId";
	  var setData       =  emxUICore.getDataPost(vURL, "");
      emxEditableTable.reloadCell("MBMaker");
     
    }
    
    
function openMaterialCo(id , WOId , ABSid)
{
    showModalDialog("../common/emxIndentedTable.jsp?SuiteDirectory=wms&suiteKey=WMS&program=WMSMaterial:getMaterialConsumption&editLink=false&table=WMSMaterialConsumption&header=WMS.Table.Header.MaterialConsumption&selection=multiple&toolbar=WMSMaterialTolbarConsumption&calculations=true&objectId="+id+"&parentOID="+WOId+"&ABSid="+ABSid+"&relId="+ABSid , 200, 300, false);
}
   
   
 
function calcQut()
{
    var CurrentCell = trim(arguments[0]);
    var currCell    = emxEditableTable.getCurrentCell();
    var oldValue    = currCell.value.old.display;
    var uid         = currCell.rowID;
    var columnName  = getColumn();
    var colName     = columnName.name;
    if(CurrentCell !="")
    {
        if(isNaN(CurrentCell))
        {
            alert("<%=strAdvanceAmountRealWarning%>");
            revertCellValue(uid,colName,oldValue);
            return false;
        }
        else
        {
            var decimalValue=(CurrentCell).substr(CurrentCell.indexOf(".")+1,CurrentCell.length - 1);
            if (decimalValue.length > 3)
            {
                alert("<%=strDecimalPlaceWarning%>");
                revertCellValue(uid,colName,oldValue);
                return false;
            }
            else
            {
                var varCF               = emxEditableTable.getCellValueByRowId(uid,"CF");
                var varItemQut          = emxEditableTable.getCellValueByRowId(uid,"QuantitySincePreviousBill");
                var varMaterial         = emxEditableTable.getCellValueByRowId(uid,"MaterialConsumptionQuantity");
                var varCFDisplay        = varCF.value.current.display;
                var varItemQutDisplay   = varItemQut.value.current.display;
                var varMaterialDisplay  = varMaterial.value.current.display;

                //Parsing values
                varCFDisplay            = parseFloat(varCFDisplay);
                varItemQutDisplay       = parseFloat(varItemQutDisplay);
                varMaterialDisplay      = parseFloat(varMaterialDisplay);

                if(colName=="CF")
                {
                    if(varCFDisplay<0 || varCFDisplay>100)
                    {
                        alert("Conversion Factor should be in % percentage");
                        revertCellValue(uid,colName,oldValue);
                    }
                    else
                    {
                        varFinalAmount = Math.round(varCFDisplay * varItemQutDisplay) / 100;
                        updateCellValue(uid , "MaterialConsumptionQuantity",varFinalAmount )
                        updateCellValue(uid , "RequiredQuntity",varFinalAmount )
                       
                    }
                }
                else
                {
                    updateCellValue(uid , "CF","0.0" );
                    updateCellValue(uid , "RequiredQuntity",CurrentCell );
                  
                }
            }//end of else
        }
    }
}


function openMaterialConsumptionStockFormF(id , ABSid , rowId)
{
  
   var  targetWindow = getTopWindow().findFrame(parent, "CHiPSAMBMaterialConsumptionFormFStock");
   targetWindow.location.href   =    "../common/emxIndentedTable.jsp?SuiteDirectory=wms&suiteKey=WMS&program=WMSMaterial:getMCStockForFomF&editLink=false&table=WMSWOMaterialBillStockForMCFormF&header=WMS.Material.BillStock&selection=multiple&toolbar=WMSMaterialConsumptionToStockFormF&calculations=true&objectId="+id+"&ABSid="+ABSid+"&relId="+ABSid+"&parentRowId="+rowId;
   setTimeout(
        function () {
                var checkedRows = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@checked='checked']");
                unRegisterSelectedRows(checkedRows);
                refreshRows();
                refreshStructureWithOutSort();
                $("#bodyTable tr#" + rowId.replace(/,/g , "\\,"))[0].classList.add("mx_rowHighlight");
                $("#treeBodyTable tr#" + rowId.replace(/,/g , "\\,"))[0].classList.add("mx_rowHighlight");
                var nSelectRow      = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + rowId + "']");
                nSelectRow.setAttribute("checked","checked");
                var checkboxList    = editableTable.divTreeBody.getElementsByTagName("input");
                var chkLen          = checkboxList.length;
                for(var i = 0; i < chkLen; i++){
                    if(checkboxList[i].type == "checkbox" && !checkboxList[i].disabled){
                        if(checkboxList[i].id=="rmbrow-"+rowId)
                        {
                            checkboxList[i].checked = "checked";
                        }
                    }
                }
                registerCheckedRowIds(rowId);
        },
            500
    );
}
  
 function updateCellValue(rowId,colName,OldValue)
{
    var row         = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + rowId + "']");
    var objColumn   = colMap.getColumnByName(colName);
    var colIndex    = objColumn.index;
    var oldColumn   = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + rowId + "']/c["+colIndex+"]");
    oldColumn.setAttribute("newA",OldValue);
    oldColumn.setAttribute("edited","true");
    emxUICore.setText(oldColumn,OldValue);
    currentCell.target.innerHTML= OldValue;
    updatePostXML(row, OldValue , colIndex);
}


function openMaterialConsumptionStock(id , ABSid)
{
    showModalDialog("../common/emxIndentedTable.jsp?SuiteDirectory=wms&suiteKey=WMS&program=WMSMaterial:getMCStock&editLink=false&table=WMSWOMaterialBillStockForMC&header=WMS.Material.BillStock&selection=multiple&toolbar=WMSMaterialConsumptionToStock&calculations=true&objectId="+id+"&ABSid="+ABSid+"&relId="+ABSid , 200, 300, false);
}

/** Added for WMS Work Order Vendor Document Management  */

function validateForPastDate(){
    var today = new Date();
    var vColum=getColumn();
	today.setHours(0,0,0,0);
  	var varDate = parseFloat(arguments[0]);
	var vColDate = new Date(varDate); 
	vColDate.setHours(0,0,0,0);
	if(vColDate < today){
	 	 alert(vColum.label+" cannot be past date");
	      return false;
	}
	return true;
 }

 
 