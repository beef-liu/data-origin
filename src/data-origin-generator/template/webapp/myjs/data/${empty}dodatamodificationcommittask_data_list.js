<!----------- Variables ----------------->
//DEBUG
var _tableName = "dodatamodificationcommittask";

var _trClone;

var _curPageSize = 50;
var _curPageIndex = 0; //from 0 -> (count-1)
var _curPageCount = 1;

var _orderByFields = "";
var _searchConditionXml = "";

var _dataimpColNameNodeCopy;
var _dataimpInputColNodeCopy;
var _dataimpImportFileName = "";
var _dataimpImportSheetIndex = 0;

$(document).ready(function() {
	initUI();
	
	doSearch();
});

<!------------------ Functions for UI ----------------------------->
function resizeTableHeight() {
	var mainContentHeight = $('#main-content').height();
	var contentHeaderHeight = $('#content-header').height();
	var footerHeight = $('.my-footbar').height();
	var newContentTableHeight = mainContentHeight - contentHeaderHeight - footerHeight;
	$('#content-table').height(newContentTableHeight); 
	//alert("mainContentHeight:" + mainContentHeight + " contentHeaderHeight" + contentHeaderHeight);
}
function initUI() {
	//change table height when window size changed
	$(window).resize(function() {
		resizeTableHeight();
	});
	resizeTableHeight();

	//clone tr of data row
	var tr = $('#tr-data');
	_trClone = $(tr[0]).clone();
	
	//set page size
	//resetPageSize(_curPageSize);
	
	//check box 
	$('#table-check-all').change(function(event) {
		if($(this).attr('checked') != undefined && $(this).attr('checked') == 'checked') {
			$('[id="table-check-row"]').attr('checked', 'checked');
		} else {
			$('[id="table-check-row"]').removeAttr('checked');
		}
	});
	
	//th sort
	$('.my-th-sort').click(function() {
		if($(this).hasClass('asc')) {
			$(this).removeClass('asc').addClass('desc');
		} else if($(this).hasClass('desc')) {
			$(this).removeClass('desc').addClass('nosort');
		} else if($(this).hasClass('nosort')) {
			$(this).removeClass('nosort').addClass('asc');
		}
	});
	
	//td checkbox
	$('[id="td-checkbox"]').click(function() {
		reverseCheckBoxStatus($(this).find('[id="table-check-row"]'));
	});
	
	//pageSize dropdown
	$('#pagesize_dropdown').find('li').click(function() {
		var clickedPageSize = Number($(this).find('span').text());
		if(_curPageSize == clickedPageSize) {
			return;
		}

		_curPageSize = Number(clickedPageSize);
		$('#page_size_selected').text(_curPageSize);
		
		
		var selectedIndex = Number($(this).attr('rel'));
		var liNodes = $('#pagesize_dropdown').find('li');
		var optionNodes = $('[name="pagesize_list"]').find('option');
		var i;
		for(i = 0; i < liNodes.length; i++) {
			if(selectedIndex == i) {
				$(liNodes[i]).addClass('selected');
				$(optionNodes[i]).attr('selected', 'selected');
			} else {
				$(liNodes[i]).removeClass('selected');
				$(optionNodes[i]).removeAttr('selected');
			}
		}
		
		//search again
		doSearch();
	});
	
	$('#btn-prev-page').click(function() {
		if(_curPageIndex == 0) {
			return;
		}
		
		_curPageIndex--;
		pageIndexChanged();
	});
	
	$('#btn-next-page').click(function() {
		if(_curPageIndex == (_curPageCount - 1)) {
			return;
		}
		
		_curPageIndex++;
		pageIndexChanged();
	});
	
	$('#input-page-go').keydown(function(event) {
		if(event.keyCode == 0x0d) {
			gotoPage();
		}
	});
	$('#btn-page-go').click(function() {
		gotoPage();
	});
	
	//data import button
	$('#btn-data-import').bind('click', function(event) {
		$('#file-data-import').click();
	});
	
	$('#file-data-import').bind('change', function(event) {
		importFileChanged();
	});
	
	//dataimp dialog --------------------
	_dataimpColNameNodeCopy = $($('#dataimpColDispName')[0]).clone();
	_dataimpInputColNodeCopy = $($('[dataimpColValue="DOColValue"]')[0]).clone();
}

function pageIndexChanged() {
	$('#page-num').text(_curPageIndex + 1);
	searchData();
}

function gotoPage() {
	var pageNumStr = $('#input-page-go').val().trim();
	if(pageNumStr.length == 0) {
		return;
	}
	
	var pageNum = Number(pageNumStr); 
	if(pageNum == NaN) {
		alert(DEFAULT_MSG_ERROR_INPUT_REQUIRE_NUMBER);
		return;
	}
	
	if(pageNum > _curPageCount) {
		pageNum = _curPageCount;
	} else if(pageNum < 1) {
		pageNum = 1;
	}
	
	_curPageIndex = pageNum - 1; 
	pageIndexChanged();
}

function reverseCheckBoxStatus(checkbox) {
	if($(checkbox).attr('checked') != undefined && $(checkbox).attr('checked') == 'checked') {
		$(checkbox).removeAttr('checked');
	} else {
		$(checkbox).attr('checked', 'checked');
	}
}

function resetPageSize(pageSize) {
	var tbody = $('#data-tbody');
	var tr = $('[id="tr-data"]');
	
	var curLen = tr.length;
	var pageSizeIncrement = pageSize - curLen;
	var i;
	if(pageSizeIncrement == 0) {
		return;
	} else if (pageSizeIncrement < 0) {
		for(i = curLen - 1; i >= pageSize; i--) {
			$(tr[i]).remove();
		}
	} else {
		for(i = 0; i < pageSizeIncrement; i++) {
			$(tbody).append($(_trClone).clone());
		}
	}
}

<!------------------ Functions for data ----------------------------->
function prepareSearchCondition() {
	//set _searchConditionXml
	_searchConditionXml = easyJsDomUtil.mappingDomNodeToDataXml(
		$('[searchCondition="DOSearchCondition"]')[0], "searchCondition");
}

function setSortCol() {
	
}

function importFileChanged() {
	easyUpload.upload({
		form: $('#form-import-data'),
		url: WEB_APP + "/cloudDataService.do",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService",
			serviceMethod: "checkDataExcelSheetCount",
		},
		success: function(response) {
			resetDataImportInputFile();
			if(response.trim().indexOf("<DOServiceMsg>") == 0) {
				//Service Msg
				myShowErrorMsg($(response).find('msg').text());
			} else {
				var sheetCount = Number($(response).find('sheetCount').text());
				if(sheetCount <= 1) {
					importFileCheckTitleRow(response, 0);
				} else {
					showSheetChooser(response, sheetCount);
				}
			}
		},
		error: function() {
			resetDataImportInputFile();
			myShowErrorMsg(DEFAULT_MSG_ERROR_AJAX);
		}
	});
}

function resetDataImportInputFile() {
	$('#file-data-import').val('');
}

function showSheetChooser(checkSheetResultXml) {
	var dropdownSheetNode = $('#modal-choose-sheet').find('[id="dropdown-sheet-name"]')[0];
	var sheetNameList = $(checkSheetResultXml).find('sheetNameList').find('String');
	
	//make dropdown
	$(dropdownSheetNode).find('option').remove();
	var optionNode;
	var i;
	for(i = 0; i < sheetNameList.length; i++) {
		optionNode = document.createElement('option');
		optionNode.value = String(i);
		optionNode.text = $(sheetNameList[i]).text();
		
		$(dropdownSheetNode).append(optionNode);
	}
	
	//show dialog
	$('#modal-choose-sheet').find('.btn-primary').unbind('click').bind('click', function(event) {
		$('#modal-choose-sheet').modal('hide');
		//ok button clicked -> checkTitleRow
		var sheetIndex = $('#modal-choose-sheet').find('[id="dropdown-sheet-name"]')[0].selectedIndex;
		if(sheetIndex < 0) {
			sheetIndex = 0;
		}

		importFileCheckTitleRow(checkSheetResultXml, sheetIndex);
	});
	$('#modal-choose-sheet').modal();
}

function importFileCheckTitleRow(checkSheetResultXml, sheetIndex) {
	var fileName = $(checkSheetResultXml).find('importFile').text();

	_dataimpImportFileName = fileName;
	_dataimpImportSheetIndex = sheetIndex;
	
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService",
			serviceMethod: "checkDataExcelTitleRow",
			fileName: fileName,
			sheetIndex: sheetIndex
		},
		success: function(response) {
			//DODataImportCheckTitleRowResult
			showDataImportColInput(response);
		},
	});
	
}

function showDataImportColInput(checkTitleRowResultXml) {
	var i;
	var colMeta, metaDataField, dbCol;
	
	//set col name list -------------------
	var colTitleList = $(checkTitleRowResultXml).find('colTitleList').find('String');
	var uploadedColMetaList = $(checkTitleRowResultXml).find('colMetaList').find('DODataImportColMetaInfo');
	var colMeta, metaDataField;
	var colDispName, colName;
	
	$('[id="dataimpColDispName"]').remove();
	
	var dataimpColListNode = $('#dataimpColList');
	var dataimpColNameNodeTmp;
	for(i = 0; i < uploadedColMetaList.length; i++) {
		colMeta = uploadedColMetaList[i];
		colDispName = $(colTitleList[i]).text();
		
		dataimpColNameNodeTmp = $(_dataimpColNameNodeCopy).clone();
		$(dataimpColNameNodeTmp).text(colDispName);
		
		if($(colMeta).find('dbCol').length == 0) {
			//not matched
			$(dataimpColNameNodeTmp).removeClass('matched-db-col');
		} else {
			//matched DB col
			$(dataimpColNameNodeTmp).removeClass('matched-db-col').addClass('matched-db-col');
		}
		
		$(dataimpColListNode).append(dataimpColNameNodeTmp);
	}
	
	
	//set input col ------------------- 
	var inputColMetaList = $(checkTitleRowResultXml).find('lackingColMetaList').find('DODataImportColMetaInfo');
	var colDispName, colName;
	var isPrimaryKey;
	var inputColNode;
	$('[dataimpColValue="DOColValue"]').remove();
	var dataimpInputColListNode = $('[dataimpColValue="List"]');
	
	if(inputColMetaList.length == 0) {
		$('#modal-dataimp-input-required-msg').hide();
	} else {
		$('#modal-dataimp-input-required-msg').show();
	}
	
	for(i = 0; i < inputColMetaList.length; i++) {
		colMeta = inputColMetaList[i];
		
		dbCol = $(colMeta).find('dbCol')[0];
		metaDataField = $(colMeta).find('metaDataField')[0];
		colDispName = $(metaDataField).find('fieldDispName').text();
		colName = $(metaDataField).find('fieldName').text();
		isPrimaryKey = $(dbCol).find('primaryKey').text();
		
		inputColNode = $(_dataimpInputColNodeCopy).clone();
		$(inputColNode).find('#dataimpInputColDispName').text(colDispName);
		if(isPrimaryKey == "true") {
			$(inputColNode).find('#dataimpInputColDispName').removeClass('primarykey').addClass('primarykey');
		} else {
			$(inputColNode).find('#dataimpInputColDispName').removeClass('primarykey');
		}
		
		$(inputColNode).find('[dataimpColValue="colName"]').val(colName);
		
		$(dataimpInputColListNode).append(inputColNode);
	}
	
	
	//show dialog ------------------------
	$('#modal-dataimp-input-required').find('.btn-primary').unbind('click').bind('click', function(event) {
		$('#modal-dataimp-input-required').modal('hide');
		importDataExcel();
	});
	$('#modal-dataimp-input-required').modal();
}

function importDataExcel() {
	var colValueListXml = easyJsDomUtil.mappingDomNodeToDataXml(
		$('[dataimpColValue="List"]')[0], "dataimpColValue"
	);
	
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService",
			serviceMethod: "importDataExcel",
			fileName: _dataimpImportFileName,
			sheetIndex: _dataimpImportSheetIndex,
			colValueListXml: colValueListXml
		},
		success: function(response) {
			//DODataImportResult
			showDataImportResult(response);
		},
	});
}

function showDataImportResult(dataImportResultXml) {
	var tableName = $(dataImportResultXml).find('tableName').text();
	var tableComment = $(dataImportResultXml).find('tableComment').text();
	var totalCount = $(dataImportResultXml).find('totalCount').text();
	var insertedCount = $(dataImportResultXml).find('insertedCount').text();
	var updatedCount = $(dataImportResultXml).find('updatedCount').text();
	var errorCount = $(dataImportResultXml).find('errorCount').text();
	var importResultFile = $(dataImportResultXml).find('importResultFile').text();
	
	var msg = "Data of " + tableName + "(" + tableComment + ")" + " imported:\n" 
		+ "  " + "Total   :" + totalCount + "\n"
		+ "  " + "Inserted:" + insertedCount + "\n"
		+ "  " + "Updated :" + updatedCount + "\n"
		+ "  " + "Error   :" + errorCount + "\n"
		+ "\n"
		+ "Click 'OK' to download result xlsx"
		;
		
	myShowConfirmMsg(msg, function(){
		window.location = WEB_APP + "/cloudDataService.do?" 
			+ "serviceType=" + "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService"
			+ "&serviceMethod=downloadTempExcel" 
			+ "&fileName=" + importResultFile; 
	});
}

function doExportData() {
	prepareSearchCondition();
	
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService",
			serviceMethod: "exportDataExcel",
			searchConditionXml: _searchConditionXml
		},
		success: function(response) {
			var totalCount = Number($(response).find('totalCount').text());
			var exportResultFile = $(response).find('exportResultFile').text();
			
			myShowConfirmMsg(
				"Total count of data:" + totalCount + "\nPlease click 'OK' button to download excel file",
				function(){
					window.location = WEB_APP + "/cloudDataService.do?" 
						+ "serviceType=" + "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataImportExportService"
						+ "&serviceMethod=downloadTempExcel" 
						+ "&fileName=" + exportResultFile; 
				});			
		},
	});
}

function doSearch() {
	prepareSearchCondition();
	
	searchDataCount();
}

function searchDataCount() {
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataSearchService",
			serviceMethod: "searchDataCount",
			//tableName: "dodatamodificationcommittask",
			searchConditionXml: _searchConditionXml
		},
		success: function(response) {
			var dataCount = Number(response);
			_curPageCount = Math.round(dataCount / _curPageSize);
			_curPageIndex = 0;
			
			$('#page-num').text(_curPageIndex + 1);
			$('#page-count').text(_curPageCount);
			
			searchData();
		},
	});
}

function resetPageController() {
	
}

function searchData() {
	var timeOfSearchBegin = new Date();
	
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "com.beef.dataorigin.test.ws.service.DODataModificationCommitTaskDataSearchService",
			serviceMethod: "searchData",
			//tableName: "dodatamodificationcommittask",
			searchConditionXml: _searchConditionXml,
			orderByFields: _orderByFields,
			beginIndex: _curPageIndex * _curPageSize,
			pageSize: _curPageSize
		},
		success: function(response) {
			reloadData(response);
			
			var timeCostOfSearch = ((new Date()).getTime() - timeOfSearchBegin.getTime()) / 1000.0;
			$('#last_query_time_begin').text(timeOfSearchBegin.formatToStr('HH:mm:ss'));
			$('#last_query_time_cost').text(timeCostOfSearch);
		},
	});
} 

function reloadData(dataListXml) {
	$('[id="tr-data"]').remove();
    easyJsDomUtil.loadListDataXmlToDomNode({
        dataListXml: dataListXml,
        dataXmlNodeName: "DODataModificationCommitTask",
        dataListDomNode: $('[listData="List"]'),
        dataDomNodeCopy: _trClone,
        domNodeAttrName: "listData",
        dataNodeDidLoadFunc: function (dataDomNode, index, length) {
        	//row num
        	var rowNum = _curPageIndex * _curPageSize + index + 1;
        	$(dataDomNode).find('[id="td-row-num"]').text(rowNum);
        	
        	//format
        	var dataColNodes = $(dataDomNode).find('[id="td-data-col"]');
        	var i, dataColNode, dispFormat, dataColVal;
        	for(i = 0; i < dataColNodes.length; i++) {
        		dataColNode = dataColNodes[i];
        		dispFormat = $(dataColNode).attr('fieldDispFormat');
        		dataColVal = $(dataColNode).text();
        		if(dispFormat != undefined && dispFormat.length > 0
        			&& dataColVal.length > 0) {
        			$(dataColNode).text(
        				myFormatDataColValue(dispFormat, dataColVal)
        				);
        		}
        	}
        }
    });
}
