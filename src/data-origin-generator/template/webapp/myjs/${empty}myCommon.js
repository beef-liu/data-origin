var WEB_APP = "/${webContextName}";

var ERROR_MSG_NODE = "<DOServiceMsg>";
var DEFAULT_MSG_TITLE_ERROR = "Error";
var DEFAULT_MSG_TITLE_INFO = "Info";
var DEFAULT_MSG_TITLE_CONFIRM = "Confirm";

var DEFAULT_MSG_ERROR_AJAX = "Network Error";
var DEFAULT_MSG_ERROR_INPUT_REQUIRE_NUMBER = "Pease input Number";

var DEFAULT_MSG_CONFIRM_DELETE_DATA = "Are you sure to delete checked data row?\n (Can not be undo)";
var DEFAULT_MSG_INFO_DID_DELETE_DATA = "Data deleted";

function myAjax(args) {
	var dataType = "text";
	if(args.dataType != undefined) {
		dataType = args.dataType;
	}
	
	myShowLoading();
	
	$.ajax({
		url: args.url,
		type: args.type,
		dataType: dataType,
		data: args.data,
		timeout: 30000,
		success: function(response) {
			myHideLoading();
			
			if(response.trim().indexOf(ERROR_MSG_NODE) == 0) {
				//Service Msg
				myShowErrorMsg($(response).find('msg').text());
			} else {
				args.success(response);
			}
		},
		error: function() {
			myHideLoading();
			
			if(args.error != undefined) {
				args.error();
			} else {
				myShowErrorMsg(DEFAULT_MSG_ERROR_AJAX);
			}
		} 
	});
}

function myShowErrorMsg(msg) {
	//alert(msg);
	myShowConfirmMsgDlg(DEFAULT_MSG_TITLE_ERROR, msg);
}

function myShowInfoMsg(msg) {
	//alert(msg);
	myShowConfirmMsgDlg(DEFAULT_MSG_TITLE_INFO, msg);
}

function myShowConfirmMsg(msg, okClickCallback) {
	myShowConfirmMsgDlg(DEFAULT_MSG_TITLE_CONFIRM, msg, okClickCallback);
}

function myLoadJS(scriptPath) {
	var scriptNode = document.createElement("script");
	scriptNode.setAttribute("src", scriptPath);
	
	document.head.appendChild(scriptNode);
}

function myShowLoading() {
	var loadingNode = $('#anim-loading')[0];
	var containerNode = $('#anim-loading-container')[0];
	
	$('#anim-loading-container').show();
		
	var left = (containerNode.clientWidth - loadingNode.clientWidth) / 2;
	var top = (containerNode.clientHeight - loadingNode.clientHeight) / 2;
	$('#anim-loading').css('left', String(left) + "px");
	$('#anim-loading').css('top', String(top) + "px");
	
	$(containerNode).unbind("click").bind("click", function() {
		$(this).hide();
	});
}

function myHideLoading() {
	$('#anim-loading-container').hide();	
}

function myShowConfirmMsgDlg(title, msg, okClickCallback) {
	$('#modal-alert').find('#myModalLabel').text(title);
	if(okClickCallback != undefined) {
		$('#modal-alert').find('.btn-default').text('Cancel');
		$('#modal-alert').find('.btn-primary').css('display', 'block');
		$('#modal-alert').find('.btn-primary').unbind('click').bind('click', function(event) {
			$('#modal-alert').modal('hide');
			okClickCallback();
		});
	} else {
		$('#modal-alert').find('.btn-default').text('Close');
		$('#modal-alert').find('.btn-primary').css('display', 'none');
	}
	
	$('#modal-alert').find('[id="modal-msg"]').text(msg);
	
	$('#modal-alert').modal();	
}

function myHideConfirmMsgDlg() {
	$('#modal-alert').modal('hide');	
}

function myFormatDataColValue(dispFormat, dataColVal) {
	var iYY, iMM, iDD;
	iYY = dispFormat.indexOf("yy");
	iMM = dispFormat.indexOf("MM");
	iDD = dispFormat.indexOf("dd");
	if(iYY >= 0 && iMM > 0 && iDD > 0
		&& iDD > iMM  && iMM > iYY) {
		//format to Date
		var utcMS = Number(dataColVal);
		if(utcMS == 0) {
			return "";
		} else {
			var dt = myParseDateFromLong(utcMS);
			return dt.formatToStr(dispFormat);
		}
	} else {
		//do nothing
		return dataColVal;
	}
}

function myReverseFormatDataColValue(dispFormat, dataColVal) {
	var iYY, iMM, iDD;
	iYY = dispFormat.indexOf("yy");
	iMM = dispFormat.indexOf("MM");
	iDD = dispFormat.indexOf("dd");
	if(iYY >= 0 && iMM > 0 && iDD > 0
		&& iDD > iMM  && iMM > iYY) {
		//go to reverse format
		return myParseDateFromFormattedStr(dataColVal, dispFormat).getTime();
	} else {
		//do nothing
		return dataColVal;
	}
}

/**
 * 
 * @param {Object} dateStr
 * @param {Object} dateFormat e.g., 'yyyy/MM/dd', 'yyyy/MM/dd HH:mm:ss', 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm:ss'
 */
function myParseDateFromFormattedStr(dateStr, dateFormat) {
	var ymdStr = "";
	var hmsStr = "";
	var indexOfSpace = dateStr.indexOf(' ');
	if(indexOfSpace >= 0) {
		ymdStr = dateStr.substring(0, indexOfSpace);
		hmsStr = dateStr.substring(indexOfSpace + 1);
	} else {
		if(dateStr.indexOf(':') >= 0) {
			hmsStr = dateStr;
		} else {
			ymdStr = dateStr;
		}
	}
	
	var curTime = new Date();
	var ymdhmsArray = [curTime.getFullYear(), curTime.getMonth(), curTime.getDate(), curTime.getHours(), curTime.getMinutes(), curTime.getSeconds()];
	var i, indexTmp;
	
	if(ymdStr.length > 0) {
		var tokenArray = ymdStr.split(/[/-]/g);
		
		indexTmp = 2;
		for(i = tokenArray.length - 1; i >= 0; i--) {
			ymdhmsArray[indexTmp] = Number(tokenArray[i]);
			if(indexTmp == 1) {
				//month is 0-11
				ymdhmsArray[indexTmp] --;
			}
			
			indexTmp --;
		}
	}
	
	if(hmsStr.length > 0) {
		var tokenArray = hmsStr.split(/[:]/g);
		
		indexTmp = 5;
		for(i = tokenArray.length - 1; i >= 0; i--) {
			ymdhmsArray[indexTmp] = Number(tokenArray[i]);
			indexTmp --;
		}
	}
	
	indexTmp = 0;
	curTime.setFullYear(ymdhmsArray[indexTmp++]);
	curTime.setMonth(ymdhmsArray[indexTmp++]);
	curTime.setDate(ymdhmsArray[indexTmp++]);
	curTime.setHours(ymdhmsArray[indexTmp++]);
	curTime.setMinutes(ymdhmsArray[indexTmp++]);
	curTime.setSeconds(ymdhmsArray[indexTmp++]);
	
	return curTime;
}

function myParseDateFromLong(utcMS) {
	var dt = new Date();
    dt.setTime(utcMS);
    
    return dt;
}

Date.prototype.formatToStr = function(dateFormat) {
	if(this.getFullYear() == 1899) {
		return "";
	}
	
	var returnText = dateFormat; 

	if (returnText.indexOf("yyyy") >= 0) { 
		returnText = returnText.replace("yyyy", this.getFullYear()); 
	} 

	if (returnText.indexOf("MM") >= 0) { 
		returnText = returnText.replace("MM", myStrPadLeft(String(this.getMonth() + 1), '0', 2)); 
	} 

	if (returnText.indexOf("dd") >= 0) { 
		returnText = returnText.replace("dd", myStrPadLeft(String(this.getDate()), '0', 2)); 
	} 

	if (returnText.indexOf("HH") >= 0) { 
		returnText = returnText.replace("HH", myStrPadLeft(String(this.getHours()), '0', 2)); 
	} 

	if (returnText.indexOf("mm") >= 0) { 
		returnText = returnText.replace("mm", myStrPadLeft(String(this.getMinutes()), '0', 2)); 
	} 

	if (returnText.indexOf("ss") >= 0) { 
		returnText = returnText.replace("ss", myStrPadLeft(String(this.getSeconds()), '0', 2)); 
	}

	return returnText; 
}

function myStrPadLeft(strIn, padChar, expectingLen) {
	var strResult = strIn;  
    var len = strIn.length;  
    while(len < expectingLen) {  
        strResult = padChar + strResult;  
        len++;  
    }  
    return strResult;  
}  
