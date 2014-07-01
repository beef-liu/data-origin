var trClone;
$(document).ready(function() {
	initUI();
	
	searchData();
});

<!------------------ Functions for UI ----------------------------->
function initUI() {
	//change table height when window size changed
	$(window).resize(function() {
		var mainContentHeight = $('#main-content').height();
		var contentHeaderHeight = $('#content-header').height();
		var footerHeight = $('.my-footbar').height();
		var newContentTableHeight = mainContentHeight - contentHeaderHeight - footerHeight - 20;
		$('#content-table').height(newContentTableHeight); 
		//alert("mainContentHeight:" + mainContentHeight + " contentHeaderHeight" + contentHeaderHeight);
	});

	//clone tr of data row
	var tr = $('#tr-data');
	trClone = $(tr[0]).clone();
	
	//set page size
	resetPageSize(50);
	
	//check box 
	$('#table-check-all').change(function() {
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
	
	//th checkbox
	$('[id="th-checkbox"]').click(function() {
		reverseCheckBoxStatus($(this).find('[id="table-check-all"]'));
	});
	//td checkbox
	$('[id="td-checkbox"]').click(function() {
		reverseCheckBoxStatus($(this).find('[id="table-check-row"]'));
	});
	
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
			$(tbody).append($(trClone).clone());
		}
	}
}

<!------------------ Functions for data ----------------------------->
function searchData() {
	
} 
