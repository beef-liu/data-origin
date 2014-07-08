(function( window, undefined ) {
	var urlparams = new UrlParams();

	function UrlParams() {
		this.getValue = function (paramName){
			var rs = new RegExp("(\&\?)" + paramName + "=([^\&]*)(\&|$)", "g").exec(window.document.location.href);   
		    if(rs){
		    	return decodeURIComponent(rs[2]);
		    } else {
		    	return "";
		    }  
		}
	}
	
	//Expose
	window.urlparams = urlparams;
	
})(window);
