package com.scott.transer.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;
import com.scott.transer.moudle.FileInfo;
import com.scott.transer.utils.FileUtils;

@WebServlet("/file_list_get")
public class FileListGetService extends BaseServletService{
	
	private String mPath;
	
	@Override
	protected void writeReponse() throws IOException, ServletException {
		
		List<FileInfo> fileList = FileUtils.getFileList(Config.getTranserPath(mPath));
		JSONArray jsonArray = new JSONArray();
		for(FileInfo info : fileList) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", info.name);
			log("path = " + info.path + ",newPath = " + Config.getTanserRootPath());
			String p = info.path.substring(info.path.indexOf(Config.transerRootPath) + Config.transerRootPath.length(),info.path.length());
			jsonObject.put("path", p);
			jsonObject.put("length", info.length);
			jsonObject.put("type", info.type);
			jsonObject.put("date", info.date);
			jsonArray.add(jsonObject);
		}
		
		getResponse().getOutputStream().write(jsonArray.toJSONString().getBytes());
	}

	@Override
	protected boolean readRequest() throws IOException, ServletException {
		mPath = getRequest().getParameter("path");
		if(mPath == null) {
			getResponse().sendError(404,"File Not Find!");
			return false;
		}
		
		if(FileUtils.checkFileExsits(mPath)) {
			getResponse().sendError(404,"File Not Find,path = " + mPath);
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean enableGet() {
		return true;
	}
}
