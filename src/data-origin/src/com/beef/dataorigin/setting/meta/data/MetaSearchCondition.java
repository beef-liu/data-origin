package com.beef.dataorigin.setting.meta.data;

import java.util.ArrayList;
import java.util.List;

public class MetaSearchCondition {
 	private List<MetaSearchConditionItem> _searchConditionItemList = new ArrayList<MetaSearchConditionItem>();

	public List<MetaSearchConditionItem> getSearchConditionItemList() {
		return _searchConditionItemList;
	}

	public void setSearchConditionItemList(
			List<MetaSearchConditionItem> searchConditionItemList) {
		_searchConditionItemList = searchConditionItemList;
	}
 	
}
