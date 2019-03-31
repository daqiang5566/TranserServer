package com.scott.transer.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.tagplugins.jstl.core.If;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;
import com.scott.transer.moudle.FileInfo;
import com.scott.transer.utils.FileUtils;

@WebServlet("/file_list_get")
public class FileListGetService extends BaseServletService{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String mPath = req.getParameter("path");
		
		if(mPath != null && FileUtils.checkFileExsits(mPath)) {
			mPath = "";
		}
		
		List<FileInfo> fileList = FileUtils.getFileList(Config.getTranserPath(mPath));
		if (fileList == null || fileList.isEmpty()) {
			resp.getOutputStream().write(new JSONArray().toString().getBytes());
			return;
		}
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
		
		resp.getOutputStream().write(jsonArray.toJSONString().getBytes());
	}
}
