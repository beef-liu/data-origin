package com.beef.dataorigin.web.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import MetoXML.XmlSerializer;

import com.beef.dataorigin.setting.msg.DOServiceMsg;
import com.beef.dataorigin.web.context.DataOriginWebContext;

public class DOServiceMsgUtil {
	public final static String ErrorSearchPageSizeExceedMax = "Error.Search.PageSizeOutOfRange";

	public final static String ErrorDataImportSheetMoreThanOne = "Error.DataImport.SheetMoreThanOne";
	public final static String ErrorDataImportMustExcel = "Error.DataImport.MustUploadExcel";
	public final static String ErrorDataImportUpdateFailDataNotExist = "Error.DataImport.updateFail.DataNotExist";
	
	public final static String ErrorDataDetailDataNotExist = "Error.DataDetail.DataNotExist";
	
	public static String getDefinedMsgXml(String msgCode) {
		return DataOriginWebContext.getDataOriginContext().getServiceMsgXml(msgCode);
	}

	public static DOServiceMsg getDefinedMsg(String msgCode) {
		return DataOriginWebContext.getDataOriginContext().getServiceMsg(msgCode);
	}
	
	public static String makeMsgXml(Throwable error) {
		try {
			DOServiceMsg msg = new DOServiceMsg();
			msg.setMsgLevel(DOServiceMsg.MSG_LEVEL_ERROR);
			msg.setMsg(getStackTrace(error));
			
			return XmlSerializer.objectToString(msg, DOServiceMsg.class); 
		} catch (Throwable e1) {
			throw new RuntimeException(e1);
		}
	}

	public static String getStackTrace(Throwable error) {
		PrintWriter pw = null;
		try {
			StringWriter sw = new StringWriter();
			pw = new PrintWriter(sw);
			error.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}
	
	public static List<DOServiceMsg> createDefaultMsgList() {
		List<DOServiceMsg> msgList = new ArrayList<DOServiceMsg>();
		
		msgList.add(new DOServiceMsg(DOServiceMsg.MSG_LEVEL_ERROR, ErrorSearchPageSizeExceedMax, "PageSize can not bigger than 500"));
		
		msgList.add(new DOServiceMsg(DOServiceMsg.MSG_LEVEL_ERROR, ErrorDataImportMustExcel, "Must upload excel file for data importing"));
		msgList.add(new DOServiceMsg(DOServiceMsg.MSG_LEVEL_ERROR, ErrorDataImportUpdateFailDataNotExist, "Data updating failed: Not exist"));
		msgList.add(new DOServiceMsg(DOServiceMsg.MSG_LEVEL_ERROR, ErrorDataImportSheetMoreThanOne, "Please input sheet index"));

		msgList.add(new DOServiceMsg(DOServiceMsg.MSG_LEVEL_ERROR, ErrorDataDetailDataNotExist, "Data not exist"));
		
		return msgList;
	}
}
