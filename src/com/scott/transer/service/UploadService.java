package com.scott.transer.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;

@WebServlet("/upload")
public class UploadService extends BaseServletService{
	
	static final int BUFF_SIZE = 1 * 1024 * 1024;
	
	private void recvFile(String mPath, String mFileName, String mSessionId, long mStartOffset, 
			long mEndOffset, long mContentLength, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		File file = new File(Config.getTranserPath(mPath));
		if(!file.exists()) {
			file.mkdirs();
		}
		
		try {
			mPath = URLDecoder.decode(mPath, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		log("---- mPath =" + mPath);
		
		file = new File(Config.getTranserPath(mPath) + File.separator + mSessionId);
		log("--- need create file = " + file.getAbsolutePath());
		
		if (!file.exists()) {
			file.createNewFile();
		}
		RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		rFile.seek(mStartOffset);
		
		int len = 0;
		byte[] buff = new byte[BUFF_SIZE];
		InputStream iStream = req.getInputStream();
		log("start send ====");
		while((len = iStream.read(buff)) != -1) {
			rFile.write(buff,0,len);
		}
		log("send finish ==== end = " + mEndOffset + ",length = " + rFile.length());
		rFile.close();
		iStream.close();

		if((mEndOffset + 1) == mContentLength) {
			String newPath = renameFile(file.getAbsolutePath(), mSessionId, mFileName);
			log("newFile = " + newPath + ",file = " + file.getAbsolutePath());
			file.renameTo(new File(newPath));
			onSuccessful(resp.getOutputStream(), mPath, mFileName);
		} else {
			onPiceSuccessful(resp.getOutputStream(), mStartOffset, mEndOffset);
		}
	}
	
	private void onPiceSuccessful(OutputStream oStream, long mStartOffset, long mEndOffset) {
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
	
	private void onSuccessful(OutputStream oStream, String mPath, String mFileName) {
		
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
	
	private String renameFile(String oldPath, String mSessionId, String mFileName) {
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String mPath = req.getHeader("path");
		if(mPath == null || mPath == "") {
			resp.sendError(404,"path = " + mPath + " Not Find!");
			return;
		}
		
		String content_disposition = req.getHeader("Content-Disposition");
		if(content_disposition == null || content_disposition == "") {
			resp.sendError(406,"Content-Disposition must be to set!");
			return;
		}
		String mFileName = content_disposition.split("=")[1];
		mFileName = mFileName.substring(mFileName.indexOf("filename=") + 1,mFileName.length());
		
		String mSessionId = req.getHeader("Session-ID");
		
		String content_range = req.getHeader("Content-Range");
		if(content_range == null || content_range == "") {
			resp.sendError(406,"Content-Range must be to set!");
			return;
		}
		content_range = content_range.replace("bytes", "");
		content_range = content_range.trim();
		long mStartOffset = Long.parseLong(content_range.split("-")[0]);
		long mEndOffset = Long.parseLong(content_range.split("-")[1].split("/")[0]);
		long mContentLength = Long.parseLong(content_range.split("-")[1].split("/")[1]);
		log("path = " + mPath);
		
		recvFile(mPath, mFileName, mSessionId, mStartOffset, mEndOffset, mContentLength, req, resp);
	}
}
