var WEB_APP = "/${webContextName}";

var DEFAULT_MSG_ERROR_AJAX = "Network Error";
var DEFAULT_MSG_ERROR_INPUT_REQUIRE_NUMBER = "Pease input Number";

function myShowErrorMsg(msg) {
	alert(msg);
}

function myAjax(args) {
	var dataType = "text";
	if(args.dataType != undefined) {
		dataType = args.dataType;
	}
	
	$.ajax({
		url: args.url,
		type: args.type,
		dataType: dataType,
		data: args.data,
		success: function(response) {
			if(response.trim().indexOf("<DOServiceMsg>") == 0) {
				//Service Msg
				myShowErrorMsg($(response).find('msg').text());
			} else {
				args.success(response);
			}
		},
		error: args.error != undefined? args.error: myAjaxError(),
	});
}

function myAjaxError() {
	alert(DEFAULT_MSG_ERROR_AJAX);
}

function myLoadJS(scriptPath) {
	var scriptNode = document.createElement("script");
	scriptNode.setAttribute("src", scriptPath);
	
	document.head.appendChild(scriptNode);
}
