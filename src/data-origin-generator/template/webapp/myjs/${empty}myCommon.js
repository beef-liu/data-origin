var WEB_APP = "/${webContextName}";

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
			
			if(response.trim().indexOf("<DOServiceMsg>") == 0) {
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
