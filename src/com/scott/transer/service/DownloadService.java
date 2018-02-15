package com.scott.transer.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;
import com.scott.transer.utils.FileUtils;

@WebServlet("/download")
public class DownloadService extends BaseServletService{
	
	private long mStartOffset;
	private long mEndOffset;
	private String mPath;
	final int BUFF_SIZE = 1 * 1024 * 1024;

	@Override
	protected boolean enableGet() {
		return true;
	}


	@Override
	protected boolean readRequest() throws IOException, ServletException{
		mPath = getRequest().getParameter("path");
		log("request file path = " + Config.getTranserPath(mPath));
		if(!FileUtils.checkFileExsits(Config.getTranserPath(mPath))) {
			getResponse().sendError(404,"path:" + mPath + " not find!");
			return false;
		}
		
		String content_range = getRequest().getHeader("Range");
		if(content_range == null || content_range == "") {
			mStartOffset = 0;
			mEndOffset = 0;
			return true;
		}
		
		content_range = content_range.replace("bytes=", "");
		try {
			String[] sArr = content_range.split("-");
			mStartOffset = Long.parseLong(sArr[0]);
			mEndOffset = Long.parseLong(sArr[1]);
		} catch (Exception e) {
			getResponse().sendError(406,"Range header is not a valid value!");
			return false;
		}
		return true;
	}


	@Override
	protected void writeReponse() throws IOException, ServletException {
		RandomAccessFile rFile = new RandomAccessFile(new File(Config.getTranserPath(mPath)), "r");
		rFile.seek(mStartOffset);
		
		byte[] buff = new byte[BUFF_SIZE];
		int len = 0;
		long allLength = 0;
		
		OutputStream oStream = getResponse().getOutputStream();
		while((len = rFile.read(buff)) != -1) {
			oStream.write(buff,0,len);
			oStream.flush();
			allLength += len;
		}
		
		getResponse().addHeader("Content-Length", allLength + "");
		oStream.close();
		rFile.close();
	}

}
