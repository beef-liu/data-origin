package ${basePackage}.service;

import com.beef.dataorigin.web.upload.DOUploadFileService;
import com.salama.service.core.net.RequestWrapper;
import com.salama.service.core.net.ResponseWrapper;

public class MyDOUploadFileService extends DOUploadFileService {

	@Override
	public String uploadFile(RequestWrapper request, String fileId,
			String fileTag) {
		return super.uploadFile(request, fileId, fileTag);
	}
	
	@Override
	public void downloadFile(RequestWrapper request,
			ResponseWrapper response, String fileId) {
		super.downloadFile(request, response, fileId);
	}
	
}
