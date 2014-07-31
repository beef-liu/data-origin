package ${basePackage}.service;

import com.beef.dataorigin.web.upload.DOUploadFileService;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;

public class MyDOUploadFileService extends DOUploadFileService {

	@Override
	public String uploadFile(RequestWrapper request) {
		return super.uploadFile(request);
	}
	
	@Override
	public void downloadFile(RequestWrapper request,
			ResponseWrapper response, String fileId) {
		super.downloadFile(request, response, fileId);
	}
	
}
