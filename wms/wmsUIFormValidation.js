//=================================================================
// JavaScript WMSUIFormValidation.js
//
// Added this file for WMS project
// This file is used to add any validation routines to be used by the UIForm component
//-----------------------------------------------------------------

function allowTwoDecimals()
{
    var decimalValue = document.forms[0].Rate.value;
    if (decimalValue == "")
    {
        return true;
    }
    if(isNaN(decimalValue))
    {
        alert("Rate must be a Real Number.");
        return false;
    }
    var rx = /^\d+(?:\.\d{1,2})?$/ 
    if(rx.test(decimalValue)) { 
        return true;
    }
    else { 
        alert("Should enter a maximun of 2 decimals for field Rate.");
        return false; 
    } 
}

function valueOfContract(){
    var varValue = this.value;
    var rgx = /^[0-9]*\.?[0-9]*$/;
    if(!varValue.match(rgx)){
        alert("Please enter numeric values only:allowed format[0-9][0.0]");
        this.value = "";
        return false;
    }
	var len = varValue.length;
	var index = varValue.indexOf('.');
	 
	if (index > 0) {
		var CharAfterdot = len - (index+1);
		if (CharAfterdot > 2) {
			alert("Please enter numeric values only upto 2 decimal places.");
			this.value = "";
			return false;
		}
	 }
	
	return true;
}

function reloadContractorIDOnSelectSupplier()
{
    //emxFormReloadField('ContractorId');
	//alert("Inside reloadContractorIDOnSelectSupplier");
	return true;
}

function dateComparison(){
	 

    var varWorkOrderDateValue = document.forms[0].WorkOrderDate_msvalue.value;
    var varInitalDuration = document.forms[0].TimeAllowed.value ;
    var varWorkOrderDate = parseFloat(varWorkOrderDateValue);

    var varIncludeRainySeason = "Yes";
     if(document.forms[0].WMSIncludeRainySeason){
           varIncludeRainySeason =  document.forms[0].WMSIncludeRainySeason.value;
     }
    
    var varPrepareTime="No";
    if(document.forms[0].WMSPrepartionTime){
         varPrepareTime = document.forms[0].WMSPrepartionTime.value;
    }
    var formName = document.emxCreateForm;    

    var rgx = /^[0-9]*$/;
    if(!varInitalDuration.match(rgx)){
        alert("Please enter numeric values only:allowed format[0-9]");
        this.value = "";
        return false;
    }
    varInitalDuration = parseInt(varInitalDuration);
    if(varInitalDuration<0)
    {
        alert("Value cannot be a negative value");
        this.value = "";
        return false;
    }

    if(varWorkOrderDateValue == null || varWorkOrderDateValue == "" || isNaN(varWorkOrderDateValue))
    {
        varWorkOrderDateValue = "0";
    }
    varWorkOrderDateValue = parseFloat(varWorkOrderDateValue);
    if(varInitalDuration==null || varInitalDuration =="" || varInitalDuration=="null")
    {
        varInitalDuration = 0;
    }
    varInitalDuration = parseFloat(varInitalDuration);

    if(varWorkOrderDate >0 && varInitalDuration>0)
    {
        var varDifference = 0;
        if(varInitalDuration>0)
        {
            varDifference = varInitalDuration;
        }
        
        var varDifferenceMilliseconds = varDifference*86400000;
        varDifferenceMilliseconds = parseFloat(varDifferenceMilliseconds);
        var varNewCompletionDueDate = varWorkOrderDateValue+varDifferenceMilliseconds;
        
        //Calculate New Duration 
        if(varIncludeRainySeason == "Yes")
        {
            if(varPrepareTime == "Yes")
            {
              var varDifferenceMillis = 15*86400000;
              varDifferenceMillis = parseFloat(varDifferenceMillis);
              var varCompletionDueDate = varNewCompletionDueDate+varDifferenceMillis;
              var varNewValue = getFormattedDate(varCompletionDueDate);
              document.forms[0]['CompletionDueDate1'].value = varNewValue;
            }
            else
            {
                var varCompletionDate = new Date(varNewCompletionDueDate);
                var varNewValue = getFormattedDate(varNewCompletionDueDate);
                document.forms[0]['CompletionDueDate1'].value = varNewValue;
            }
        }
        else if(varIncludeRainySeason == "No")        
        {
            var url = "../wms/wmsAjaxUtil.jsp?action=getCompletionDate&varWorkOrderDateValue="+varWorkOrderDateValue+"&varCompletionDueDateValue="+varNewCompletionDueDate+"&vDuration="+varInitalDuration+"&vPreparationTime="+varPrepareTime;

            var xmlhttp1;
            if (window.XMLHttpRequest) {// Mozilla/Safari
                xmlhttp1 = new XMLHttpRequest();
            } else if (window.ActiveXObject) {// IE
                xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
            } else {
                alert('Sorry, your browser does not support XML HTTP Request!');
            }
            xmlhttp1.open("GET", url, false);
            xmlhttp1.setRequestHeader("Content-Type", "text");
            xmlhttp1.send(null);
            if ((xmlhttp1.readyState == 4 || xmlhttp1.readyState == "complete" ))
            {
                var responseText = xmlhttp1.responseText;
                var arrValue = responseText.split(":");        
                var varNewCompletionDueDate = arrValue[1];
            }
    
            if(varNewCompletionDueDate>0)
            {
                var varCompletionDate = new Date(varNewCompletionDueDate);
                var varNewValue = getFormattedDate(varNewCompletionDueDate);
                document.forms[0]['CompletionDueDate1'].value = varNewValue;
            }
        }

    }
    return true;
}

function getFormattedDate(date) {
         
	
   var url = "../wms/wmsAjaxUtil.jsp?action=getMatrixFormattedDate&Date="+date;
    var xmlhttp1;
    if (window.XMLHttpRequest) {// Mozilla/Safari
        xmlhttp1 = new XMLHttpRequest();
    } else if (window.ActiveXObject) {// IE
        xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        alert('Sorry, your browser does not support XML HTTP Request!');
    }
    xmlhttp1.open("GET", url, false);
    xmlhttp1.setRequestHeader("Content-Type", "text");
    xmlhttp1.send(null);
    if ((xmlhttp1.readyState == 4 || xmlhttp1.readyState == "complete" ))
    {
        var responseText = xmlhttp1.responseText;
        var arrValue = responseText.split(":");		
        var vFormattedDate= arrValue[1];
    }
    
    return vFormattedDate;

}

function compareWOandProjectDate()
{

 var varWorkOrderDateValue = document.forms[0].WorkOrderDate_msvalue.value;
    if(varWorkOrderDateValue == null || varWorkOrderDateValue == "" || isNaN(varWorkOrderDateValue))
    {
        varWorkOrderDateValue = "0";
    }
    varWorkOrderDateValue = parseFloat(varWorkOrderDateValue);
   var varWorkOrderDate = parseFloat(varWorkOrderDateValue);

    var objectId = document.getElementsByName("objectId");
    var url = "../wms/wmsAjaxUtil.jsp?action=compareWorkOrderProjectDate&objectId="+objectId[0].value;
    var xmlhttp1;
    if (window.XMLHttpRequest) 
    {
        xmlhttp1 = new XMLHttpRequest();
    } 
    else if (window.ActiveXObject) 
    {
        xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
    } 
    else 
    {
        alert('Sorry, your browser does not support XML HTTP Request!');
    }
    var varProjectDate;
    var varFloatProjectDate;
    xmlhttp1.open("GET", url, false);
    xmlhttp1.setRequestHeader("Content-Type", "text");
    xmlhttp1.send(null);
    if ((xmlhttp1.readyState == 4 || xmlhttp1.readyState == "complete" ))
    {
        var responseText = xmlhttp1.responseText;
        var arrValue = responseText.split(":");   
        varProjectDate = arrValue[1];
        varFloatProjectDate = parseFloat(varProjectDate);
    }
    if((varWorkOrderDate < varFloatProjectDate) && varWorkOrderDate > 0.0)
    {
        alert("Work Order Date must be greater than Project Creation date");
        return false;
    }
    return true;
}

function validateDOMeasurement(){
	var today = new Date();
	today.setHours(0,0,0,0);
	var varMBDOM = document.forms[0].WMSMBEDateOfMeasurementDate_msvalue.value;
  	var varMBDOMMS = parseFloat(varMBDOM);
	var DOM = new Date(varMBDOMMS); 
	DOM.setHours(0,0,0,0);
	
	if(DOM>today){
		
		 alert("Date of Measurement cannot be future date");
	        return false;
	}
	return true;
}

function validateRingiDate(){
	var today = new Date();
	today.setHours(0,0,0,0);
	var varMBDOM = document.forms[0].RingiApprovalDate_msvalue.value;
  	var varMBDOMMS = parseFloat(varMBDOM);
	var DOM = new Date(varMBDOMMS); 
	DOM.setHours(0,0,0,0);
	
	if(DOM>today){
		
		 alert("Ringi Approval Date cannot be future date");
	        return false;
	}
	return true;
}

function reloadAbsMBEType()
{
	emxFormReloadField("WMSAbsMBEType");
}
function getSelectedProjectTemplateDetails(){
	var searchProjectTemplateId = document.getElementsByName("SeachProjectOID")[0].value;

	if(searchProjectTemplateId != null && searchProjectTemplateId !="" && searchProjectTemplateId != "undefined"){
		var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=searchProjectData&searchProjectId="+searchProjectTemplateId;

		var responseText = emxUICore.getData(strURL);
		var responseJSONObject = emxUICore.parseJSON(responseText);

		for (var key in responseJSONObject) {

			if(key=="Question" && responseJSONObject[key]=="true"){
				enableQuestionField();
			}else if(key=="Question" && responseJSONObject[key]=="false"){
				disableQuestionField();
			}else if(key=="RT" && responseJSONObject[key]=="true"){
				enableRTF();
				emxFormReloadField('ResourceTemplate');
			}else if(key=="RT" && responseJSONObject[key]=="false"){
				emxFormReloadField('ResourceTemplate');
				disableRT();
			}else if(key=="ProjectDate"){
				// While creating the Project from "Project Template" default date in ProjectDate field should not be changed after selecting the "Project Template".
			} else if(key == "ScheduleFrom"){
				emxFormReloadField('ScheduleFrom');
			} else if(key == "DefaultConstraintType") {
				emxFormReloadField('DefaultConstraintType');
			} else {
				var value = responseJSONObject[key];
				if(value != null && value !=""){
					document.getElementsByName(key)[0].value = value;
			}
		}
		}
		enableTemplatePredictWBS();

	}else{
		setDefaultValue();
	}

}
function setDefaultConstraint() {
	 var defconstratinttype = "As Soon As Possible";
    if(null !=document.getElementById("Schedule FromId")){
    	var scheduledfrom = document.getElementById("Schedule FromId").value;
    }else if(null != document.emxCreateForm.ScheduleFrom){
    var scheduledfrom = document.emxCreateForm.ScheduleFrom.value;
    }
    var defconstratinttype = document.getElementById("DefaultConstraintTypeId");
    if("Project Start Date" == scheduledfrom){
    	defconstratinttype.value = "As Soon As Possible";
    }else{
    	defconstratinttype.value = "As Late As Possible";
    }
}
function isBadNameChars() {
  var isBadNameChar=checkForNameBadCharsList(this);
       if( isBadNameChar.length > 0 )
       {
         alert(BAD_NAME_CHARS + isBadNameChar);
         return false;
       }
    var fieldValue = this.value;
   	var hastabs = hasTabSpace(fieldValue);
   	if(hastabs){
   		alert(BAD_NAME_CHARS + "\n"+" tab space");
   		return false;
   	}
        return true;
}
// End:R207:PRG:Bug:366902
function hasTabSpace(nameString) {
	  var regExp = /\t/;
	  return regExp.test(nameString);
}

function setFocusOnSearchField(){
       document.getElementsByName("SeachProjectDisplay")[0].focus();
}
function disableRTF(){
       setFocusOnSearchField();
       document.getElementById("ResourceTemplateId").disabled=true;
       //the project date field was appearing as disabled so we have set createInputField class to the project date field to appear as enabled
       if(document.getElementById('calc_TypeActual').childNodes[3]){
       document.getElementById('calc_TypeActual').childNodes[3].setAttribute("class","createInputField");
       }
       disableQuestionField();
}
 
function enableRTF(){
       document.getElementById("ResourceTemplateId").disabled=false;
}
 
function disableQuestionField(){
       var keyName = "questionsDisplay";
       var keyValue="";
       var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=QuestionTxt&subMode=NoQuestion";
       var responseTxt = emxUICore.getData(strURL);
       var responseJSONObj = emxUICore.parseJSON(responseTxt);
 
       keyValue = responseJSONObj[keyName];
 
       document.getElementsByName("btnquestions")[0].disabled = true;
       document.getElementsByName("questionsDisplay")[0].value=keyValue;
       disableTemplatePredictWBS();
}
 
function disableTemplatePredictWBS(){
       document.getElementById("predictWBS").disabled=true;
}


function validateValuewithDecimalCheck()
{  
var colmName="Loading Unloading Charges/UOM";
	if(this.name=='BillAmount'){
	colmName="Transportation Bill Amount";
	}
    var cellValue     = this.value;
    if(cellValue==""){
    	return true;
    }
    if(isNaN(cellValue))
    {
       alert(colmName +": Please enter valid numeric value");
        return false;
    }
    var rx = /^\d+(?:\.\d{1,2})?$/ 
    if(rx.test(cellValue)) { 
        return true;
    }
    else { 
        alert(colmName +": Numeric value upto two decimal is allowed");
        return false; 
    }
  }