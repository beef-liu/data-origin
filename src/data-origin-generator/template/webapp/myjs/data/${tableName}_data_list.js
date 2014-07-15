<!----------- Variables ----------------->
//DEBUG
var _tableName = "${tableName}";

var _trClone;

var _curPageSize = 50;
var _curPageIndex = 0; //from 0 -> (count-1)
var _curPageCount = 1;

var _orderByFields = "";
var _searchConditionXml = "";


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

function doExportData() {
	prepareSearchCondition();
	
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.${dataClassName}DataImportExportService",
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
						+ "serviceType=" + "${basePackage}.service.${dataClassName}DataImportExportService"
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
			serviceType: "${basePackage}.service.${dataClassName}DataSearchService",
			serviceMethod: "searchDataCount",
			//tableName: "${tableName}",
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
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.${dataClassName}DataSearchService",
			serviceMethod: "searchData",
			//tableName: "${tableName}",
			searchConditionXml: _searchConditionXml,
			orderByFields: _orderByFields,
			beginIndex: _curPageIndex * _curPageSize,
			pageSize: _curPageSize
		},
		success: function(response) {
			reloadData(response);
		},
	});
} 

function reloadData(dataListXml) {
	$('[id="tr-data"]').remove();
    easyJsDomUtil.loadListDataXmlToDomNode({
        dataListXml: dataListXml,
        dataXmlNodeName: "${dataClassName}",
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
