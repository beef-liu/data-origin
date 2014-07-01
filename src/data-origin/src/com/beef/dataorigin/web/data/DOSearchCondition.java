package com.beef.dataorigin.web.data;

import java.util.ArrayList;
import java.util.List;

public class DOSearchCondition {
	private List<DOSearchConditionItem> _searchConditionItemList = new ArrayList<DOSearchConditionItem>();

	public List<DOSearchConditionItem> getSearchConditionItemList() {
		return _searchConditionItemList;
	}

	public void setSearchConditionItemList(
			List<DOSearchConditionItem> searchConditionItemList) {
		_searchConditionItemList = searchConditionItemList;
	}
	
}
