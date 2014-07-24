var WEB_APP = "/${webContextName}";

var ERROR_MSG_NODE = "<DOServiceMsg>";
var DEFAULT_MSG_TITLE_ERROR = "Error";
var DEFAULT_MSG_TITLE_CONFIRM = "Confirm";

var DEFAULT_MSG_ERROR_AJAX = "Network Error";
var DEFAULT_MSG_ERROR_INPUT_REQUIRE_NUMBER = "Pease input Number";

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
