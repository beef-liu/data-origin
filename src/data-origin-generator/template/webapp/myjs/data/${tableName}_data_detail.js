/* #set(MetaDataUISetting dataUISetting, MMetaDataUISetting mDataUISetting, DBTable dbTable, MDBTable mDBTable) */

function gotoDetail(thisNode) {
	var trNode;
	if($(thisNode).attr('listData') == '${dataClassName}') {
		trNode = thisNode;
	} else {
		trNode = $(thisNode).parents('[listData="${dataClassName}"]')[0];
	}
	var dataXml = easyJsDomUtil.mappingDomNodeToDataXml(trNode, "primaryKeytrue");
	
	//query data
	myAjax({
		url: WEB_APP + "/cloudDataService.do",
		type: "post",
		dataType: "text",
		data: {
			serviceType: "${basePackage}.service.${dataClassName}DataDetailService",
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
			
			$('#modal-data-detail').modal();
		},
	});
	
		
}
