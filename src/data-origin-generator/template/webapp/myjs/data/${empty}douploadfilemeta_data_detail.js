//0:update 1:insert
var _dataDetailMode = 0;

function uploadFileUpload() {
	var fileId = "";
	if(_dataDetailMode == 0) {
		fileId = $('input[dataDetail="file_id"]').val();
	}
	
	easyUpload.upload({
		form: $('#form_upload_file'),
		url: WEB_APP + "/cloudDataService.do",
		data: {
			serviceType: "${basePackage}.service.MyDOUploadFileService",
			serviceMethod: "uploadFile",
			fileId: fileId,
		},
		success: function(response) {
			$('#file_upload_file').val('');
			
			if(response.trim().indexOf("<DOServiceMsg>") == 0) {
				//Service Msg
				myShowErrorMsg($(response).find('msg').text());
			} else {
				loadDataDetail(response);
				
				searchData(true);
			}
		},
		error: function() {
			$('#file_upload_file').val('');
			
			myShowErrorMsg(DEFAULT_MSG_ERROR_AJAX);
		}
	});
}

function deleteCheckedData() {
	var dataRowCheckBoxNodeList = $('#data-tbody').find('input[type="checkbox"][id="table-check-row"]');
	var i;
	
	var dataListXml = "<List>" + "\n";
	//only primary key
	var dataXml, checkBoxNode, trNode;
	var checkedCount = 0;
	for(i = 0; i < dataRowCheckBoxNodeList.length; i++) {
		checkBoxNode = dataRowCheckBoxNodeList[i];
		if(checkBoxNode.checked) {
			trNode = $(checkBoxNode).parents('[listData="DOUploadFileMeta"]')[0];
			dataXml = easyJsDomUtil.mappingDomNodeToDataXml(trNode, "primaryKeytrue");
			
			dataListXml += dataXml + "\n";
			
			checkedCount++;
		}
	}
	
	dataListXml += "</List>";
	if(checkedCount == 0) {
		return;
	}
	
	myShowConfirmMsg(DEFAULT_MSG_CONFIRM_DELETE_DATA, function() {
		setTimeout(function() {
			myAjax({
				url: WEB_APP + "/cloudDataService.do",
				type: "post",
				dataType: "text",
				data: {
					serviceType: "${basePackage}.service.DOUploadFileMetaDataDetailService",
					serviceMethod: "deleteDataByPKList",
					dataListXml: dataListXml,
				},
				success: function(response) {
					var deletedCount = response;
					myShowInfoMsg(DEFAULT_MSG_INFO_DID_DELETE_DATA + ". Checked " + checkedCount + ", deleted " + deletedCount);
					
					setTimeout(function() {
						myHideConfirmMsgDlg();
						
						setTimeout(function() {
							searchData();
						}, 500);
					}, 3000);
					
				},
			});
		}, 500);
	});
}

function gotoDataDetailForNew() {
	_dataDetailMode = 1;
	
	//clear all input field
	$('#img_upload_file_upload')[0].src = "";
	$('#link_upload_file_download_url')[0].href = "";
	$('#link_upload_file_download_url>span').text("");
	
	$('input[dataDetail],textarea[dataDetail]').val('');
	
	$('div[hideWhenNewFile]').hide();
	
	$('#modal-data-detail').modal();
}

function gotoDataDetail(thisNode) {
	_dataDetailMode = 0;

	var trNode;
	if($(thisNode).attr('listData') == 'DOUploadFileMeta') {
		trNode = thisNode;
	} else {
		trNode = $(thisNode).parents('[listData="DOUploadFileMeta"]')[0];
	}
	var dataXml = easyJsDomUtil.mappingDomNodeToDataXml(trNode, "primaryKeytrue");
	
	//query data
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.DOUploadFileMetaDataDetailService",
			serviceMethod: "findDataByPK",
			dataXml: dataXml,
		},
		success: function(response) {
			//load data, and show it
			loadDataDetail(response);
			
			$('#modal-data-detail').modal();
		},
	});
}

function loadDataDetail(dataDetailXml) {
	$('div[hideWhenNewFile]').show();

	var dataDetailNode = $('[dataDetail="DataDetail"]')[0]; 
	var dataXmlDoc = easyJsDomUtil.parseXML(dataDetailXml);
	
	easyJsDomUtil.mappingDataXmlNodeToDomNode(
		dataDetailNode, "dataDetail", dataXmlDoc.firstChild
	);
	
	formatColValInput();
	
	var download_url = $(dataDetailXml).find("download_url").text();
	$('#link_upload_file_download_url')[0].href = download_url;
	$('#link_upload_file_download_url>span').text(download_url);
	
	var thumbnail_download_url = $(dataDetailXml).find("thumbnail_download_url").text();
	thumbnail_download_url = myUrlAddParam(thumbnail_download_url, "timestamp=" + (new Date()).getTime());
	$('#img_upload_file_upload')[0].src = thumbnail_download_url;
	
}

function saveDetailData(thisNode) {
	var serviceMethod;
	var msgPrefix;

	//update mode
	serviceMethod = "updateDataByPK";
	msgPrefix = "Updating data";
	
	var dataDetailNode = $('[dataDetail="DataDetail"]')[0];
	var dataXml = easyJsDomUtil.mappingDomNodeToDataXml(dataDetailNode, "dataDetail");

	var dataXmlDoc = easyJsDomUtil.parseXML(dataXml); 
	reverseFormatColValXml(dataXmlDoc);
	//dataXml = dataXmlDoc.firstChild.outerHTML;
	dataXml = myXmlDocToStr(dataXmlDoc); 

	//save data
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.DOUploadFileMetaDataDetailService",
			serviceMethod: serviceMethod,
			dataXml: dataXml,
		},
		success: function(response) {
			if(response == "success") {
				myShowInfoMsg(msgPrefix + " succeeded.");
				
				setTimeout(function() {
					myHideConfirmMsgDlg();
					
					setTimeout(function() {
						$('#modal-data-detail').modal('hide');
						searchData();
					}, 500);
				}, 3000);
			} else {
				myShowInfoMsg(msgPrefix + " failed.");
			}
		},
	});
}


function formatColValInput() {
	var dataDetailNode = $('[dataDetail="DataDetail"]')[0];
	var dataColNodes = $(dataDetailNode).find('div[dataColBlock]');
	var i, dataColNode, dispFormat, dataColValNode;
	for(i = 0; i < dataColNodes.length; i++) {
		dataColNode = dataColNodes[i];
		
		dispFormat = $(dataColNode).find('[name="fieldDispFormat"]').val();
		if(dispFormat.length > 0) {
			//format
			dataColValNode = $(dataColNode).find('[dataDetail]');
			$(dataColValNode).val(
				myFormatDataColValue(dispFormat, dataColValNode.val())
			);
			
		}
		
	}
}

function reverseFormatColValXml(dataXmlDoc) {
	var dataDetailNode = $('[dataDetail="DataDetail"]')[0];
	var dataColNodes = $(dataDetailNode).find('div[dataColBlock]');
	var i, dataColNode, dispFormat, dataColValNode;
	var colName, colXmlNodes;
	for(i = 0; i < dataColNodes.length; i++) {
		dataColNode = dataColNodes[i];
		
		dispFormat = $(dataColNode).find('[name="fieldDispFormat"]').val();
		
		if(dispFormat.length > 0) {
			//reverse format
			dataColValNode = $(dataColNode).find('[dataDetail]');
			colName = $(dataColValNode).attr('dataDetail');
			
			colXmlNodes = $(dataXmlDoc).find(colName);
			if(colXmlNodes.length > 0) {
				$(colXmlNodes).text(
					myReverseFormatDataColValue(dispFormat, dataColValNode.val())
				);
			}
		}
	}
}
