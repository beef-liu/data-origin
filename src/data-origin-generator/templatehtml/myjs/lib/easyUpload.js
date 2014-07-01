/**
 * @author XingGu Liu
 */
(function( window, undefined ) {
	var easyUpload = new EasyUpload();
	
	function EasyUpload() {
		this.upload = function(args) {
			uploadImp(args.form, args.url, args.data, args.success, args.error);
		};
		
		function uploadImp (
				form,
				url,
				data,
				success,
				error
				) {
			var nowDate = new Date();
			var iframeId = "uploadResultIframe" + nowDate.getTime(); 
			var iframeHtml = '<iframe uploadHiddenFrame="true"  id="' + iframeId + '" name="' + iframeId + '" style="display:none" width=1 height=1></iframe>';

			//set properties of form ----------
			$(form).prop("target", iframeId);
			$(form).prop("action", url);
			$(form).prop("method", "post");
			$(form).prop("enctype", "multipart/form-data");

			//add hidden input for params ----------
			var paramHiddenInput;
			for(var paramName in data) {
				var paramVal = data[paramName];
				
				paramHiddenInput = $('<input type="hidden" name="' + paramName + '" value="">');
				$(paramHiddenInput).prop("value", (paramVal == undefined || paramVal == null)? "": paramVal);
				
				$(form).find('input[name="' + paramName + '"]').remove();
				$(form).append(paramHiddenInput);
			}

			//create temp frame and load it -----------------			
			var domIFrame = $(iframeHtml);
			$('iframe[uploadHiddenFrame="true"]').remove();
			$(document.body).append(domIFrame);

			$(domIFrame).unbind('load');
			$(domIFrame).bind('load', function () {
				var response = null;
				try {
					if($.browser != undefined && $.browser.msie) {
						response = $(this)[0].contentDocument.documentElement.innerText;
					} else {
						response = $(this).contents()[0].body.innerText;
					}

					//The event of loading first time is caused by $(body).append
					if( success != null) {
						success(response);
					}
				} catch (e) {
					response = undefined;
				}

				//remove objs
				$('iframe[uploadHiddenFrame="true"]').remove();
				for(var paramName in data) {
					$(form).find('input[name="' + paramName + '"]').remove();
				}

				if(response == undefined) {
					if(error != null) {
						error();
					}
				}
				
			});

			form.submit();
		}
	}
	
	//Expose
	window.easyUpload = easyUpload;

})(window);

