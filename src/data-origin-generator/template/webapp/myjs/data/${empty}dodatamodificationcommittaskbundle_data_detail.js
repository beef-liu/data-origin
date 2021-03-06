//0:update 1:insert
var _dataDetailMode = 0;

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
			trNode = $(checkBoxNode).parents('[listData="DODataModificationCommitTaskBundle"]')[0];
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
					serviceType: "${basePackage}.service.DODataModificationCommitTaskBundleDataDetailService",
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
		}, 100);
	});
}

function gotoDataDetailForNew() {
	_dataDetailMode = 1;
	
	//clear all input field
	$('input[dataDetail],textarea[dataDetail]').val('');
	
	$('#modal-data-detail').modal();
}

function gotoDataDetail(thisNode) {
	_dataDetailMode = 0;

	var trNode;
	if($(thisNode).attr('listData') == 'DODataModificationCommitTaskBundle') {
		trNode = thisNode;
	} else {
		trNode = $(thisNode).parents('[listData="DODataModificationCommitTaskBundle"]')[0];
	}
	var dataXml = easyJsDomUtil.mappingDomNodeToDataXml(trNode, "primaryKeytrue");
	
	var dataXmlDoc = easyJsDomUtil.parseXML(dataXml); 
	reverseFormatColValXmlForListRow(dataXmlDoc, trNode);
	//dataXml = dataXmlDoc.firstChild.outerHTML;
	dataXml = myXmlDocToStr(dataXmlDoc); 
	
	//query data
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.DODataModificationCommitTaskBundleDataDetailService",
			serviceMethod: "findDataByPK",
			dataXml: dataXml,
		},
		success: function(response) {
			//load data, and show it
			var dataDetailNode = $('[dataDetail="DataDetail"]')[0]; 
			var dataXmlDoc = easyJsDomUtil.parseXML(response);
			
			easyJsDomUtil.mappingDataXmlNodeToDomNode(
				dataDetailNode, "dataDetail", dataXmlDoc.firstChild
			);
			
			formatColValInput();

			var primarykeyNode = $('[primarykey="DODataModificationCommitTaskBundle"]')[0]; 
			easyJsDomUtil.mappingDataXmlNodeToDomNode(
				dataDetailNode, "primarykey", dataXmlDoc.firstChild
			);
			
			$('#modal-data-detail').modal();
		},
	});
}

function saveDetailData(thisNode) {
	var serviceMethod;
	var msgPrefix;
	/*
	if(_dataDetailMode == 1) {
		//insert mode
		serviceMethod = "insertData";
		msgPrefix = "Inserting data";
	} else {
		//update mode
		serviceMethod = "updateDataByPK";
		msgPrefix = "Updating data";
	}
	*/
	serviceMethod = "modifyDataCommitTaskBundleScheduleTime";
	
	/*
	var dataDetailNode = $('[primarykey="DODataModificationCommitTaskBundle"]')[0];
	var dataXml = easyJsDomUtil.mappingDomNodeToDataXml(dataDetailNode, "dataDetail");

	var dataXmlDoc = easyJsDomUtil.parseXML(dataXml); 
	reverseFormatColValXml(dataXmlDoc);
	dataXml = dataXmlDoc.firstChild.outerHTML;
	*/ 

	//save data
	var displayFormat = $('[dataColBlock="schedule_commit_time"]').find('input[name="fieldDispFormat"]').val();
	var newSchedule_commit_time = $('[dataDetail="schedule_commit_time"]').val();
	newSchedule_commit_time = myReverseFormatDataColValue(displayFormat, newSchedule_commit_time);
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.DODataModificationCommitTaskBundleDataDetailService",
			serviceMethod: serviceMethod,
			table_name: $('[primarykey="table_name"]').val(),
			schedule_commit_time: $('[primarykey="schedule_commit_time"]').val(),
			newSchedule_commit_time: newSchedule_commit_time,
		},
		success: function(response) {
			if(response == "success") {
				myShowInfoMsg("Update succeeded.");
				
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

function reverseFormatColValXmlForListRow(dataXmlDoc, trNode) {
	var dataColNodes = $(trNode).find('td[listData]');
	var i, dataColNode, dispFormat, dataColVal;
	var colName, colXmlNodes;
	for(i = 0; i < dataColNodes.length; i++) {
		dataColNode = dataColNodes[i];
		
		dispFormat = $(dataColNode).attr('fieldDispFormat');
		
		if(dispFormat.length > 0) {
			//reverse format
			dataColVal = $(dataColNode).text();
			colName = $(dataColNode).attr('listData');
			
			colXmlNodes = $(dataXmlDoc).find(colName);
			if(colXmlNodes.length > 0) {
				$(colXmlNodes).text(
					myReverseFormatDataColValue(dispFormat, dataColVal)
				);
			}
		}
	}
}
