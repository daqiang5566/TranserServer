package com.scott.transer.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.json.simple.JSONObject;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;

@WebServlet("/upload")
public class UploadService extends BaseServletService{
	
	private String mPath;
	private String mFileName;
	private String mSessionId;
	private long mContentLength;
	private long mStartOffset;
	private long mEndOffset;
	
	final int BUFF_SIZE = 1 * 1024 * 1024;
	
	@Override
	protected void writeReponse() throws IOException, ServletException {
		File file = new File(Config.getTranserPath(mPath));
		if(!file.exists()) {
			file.mkdirs();
		}
		log("path === " + mPath);
		
		file = new File(Config.getTranserPath(mPath) + File.separator + mSessionId);
		RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		rFile.seek(mStartOffset);
		
		int len = 0;
		byte[] buff = new byte[BUFF_SIZE];
		InputStream iStream = getRequest().getInputStream();
		log("start send ====");
		while((len = iStream.read(buff)) != -1) {
			rFile.write(buff,0,len);
		}
		log("send finish ==== end = " + mEndOffset + ",length = " + rFile.length());
		rFile.close();
		iStream.close();

		if((mEndOffset + 1) == mContentLength) {
			String newPath = renameFile(file.getAbsolutePath());
			log("newFile = " + newPath + ",file = " + file.getAbsolutePath());
			file.renameTo(new File(newPath));
			onSuccessful(getResponse().getOutputStream());
		} else {
			onPiceSuccessful(getResponse().getOutputStream());
		}
	}
	
	private void onPiceSuccessful(OutputStream oStream) {
		JSONObject jObj = new JSONObject();
		jObj.put("code", 200);
		jObj.put("start", mStartOffset);
		jObj.put("end", mEndOffset);
		try {
			oStream.write(jObj.toJSONString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		log(jObj.toJSONString());
	}
	
	private void onSuccessful(OutputStream oStream) {
		
		JSONObject jObj = new JSONObject();
		jObj.put("code", 0);
		jObj.put("path", mPath);
		jObj.put("name", mFileName);
		try {
			oStream.write(jObj.toJSONString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		log(jObj.toJSONString());
	}
	
	private String renameFile(String oldPath) {
		String path = oldPath.replace(mSessionId, "");
		path += mFileName;
		log("newPath = " + path + ",oldPath = " + oldPath);

		int count = 0;
		String newPath = path;
		while(true) {
			File file = new File(newPath);
			if(!file.exists()) {
				break;
			}
			if(path.contains(".")) {
				String ext = path.substring(path.lastIndexOf("."),path.length());
				newPath = path.substring(0,path.lastIndexOf("."));
				newPath = newPath + "(" + (++count) + ")" + ext;
			} else {
				newPath = path + "(" + (++count) + ")";
			}
			log("newPath = " + newPath + ",oldPath = " + oldPath);
		}
		return newPath;
	}

	@Override
	protected boolean readRequest() throws IOException, ServletException {
		mPath = getRequest().getHeader("path");
		if(mPath == null || mPath == "") {
			getResponse().sendError(404,"path = " + mPath + " Not Find!");
			return false;
		}
		
		String content_disposition = getRequest().getHeader("Content-Disposition");
		if(content_disposition == null || content_disposition == "") {
			getResponse().sendError(406,"Content-Disposition must be to set!");
			return false;
		}
		mFileName = content_disposition.split("=")[1];
		mFileName = mFileName.substring(mFileName.indexOf("filename=") + 1,mFileName.length());
		
		mSessionId = getRequest().getHeader("Session-ID");
		
		String content_range = getRequest().getHeader("Content-Range");
		if(content_range == null || content_range == "") {
			getResponse().sendError(406,"Content-Range must be to set!");
			return false;
		}
		content_range = content_range.replace("bytes", "");
		content_range = content_range.trim();
		mStartOffset = Long.parseLong(content_range.split("-")[0]);
		mEndOffset = Long.parseLong(content_range.split("-")[1].split("/")[0]);
		mContentLength = Long.parseLong(content_range.split("-")[1].split("/")[1]);
		log("path = " + mPath);
		return true;
	}
	
	@Override
	protected boolean enablePost() {
		return true;
	}
}
