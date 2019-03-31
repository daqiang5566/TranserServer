package com.scott.transer.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.scott.transer.BaseServletService;
import com.scott.transer.Config;
import com.scott.transer.utils.FileUtils;
import com.sun.org.apache.regexp.internal.recompile;


/**
 * formpart 测试
 * 规定:
 * 
 * part file 为文件字段
 * filename 为文件名
 * 
 * part path 为参数字段，标识保存目录(保存到根目录下 path文件夹下)
 *  
 * @author shijiale
 *
 */
@WebServlet("/upload_formpart")
public class FormpartUploadService extends BaseServletService{
	
	static final String PART_FILE = "file";
	static final String PART_PATH = "path";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(req)) {
			resp.sendError(500, "only support multipart/form-data");
			return;
		}
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		Result result = new Result();
		
		try {
			Map<String, List<FileItem>> map = upload.parseParameterMap(req);
			onParsedRequest(map, result);
			
			log("---- result = " + result);
			String path = result.get(PART_FILE);
			boolean success = FileUtils.isFile(path);
			if (success) {
				resp.setStatus(200);
				resp.getOutputStream().write("{\"code\":200}".getBytes("utf-8"));
			} else {
				resp.getOutputStream().write("{\"code\":-1}".getBytes("utf-8"));
			}
		} catch (FileUploadException e) {
			resp.sendError(500, e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void onParsedRequest(Map<String, List<FileItem>> map, Result result) throws IOException {
		
		Set<String> keySet = map.keySet();
		for (String k : keySet) {
			List<FileItem> list = map.get(k);
			for (FileItem fileItem : list) {
				if (fileItem.isFormField()) {
					onParsedParam(fileItem, result);
				} else {
					String path = result.get(PART_PATH);
					onParsedFile(fileItem, path, result);
				}
			}
		}
	}
	
	private boolean onParsedParam(FileItem fileItem, Result result) {
		if (fileItem == null || !fileItem.isFormField()) {
			return false;
		}
		
		result.put(fileItem.getName(), fileItem.getString());
		return true;
	}
	
	private boolean onParsedFile(FileItem fileItem, String path, Result result) throws IOException {
		if (fileItem == null || fileItem.isFormField()) {
			return false;
		}
		
		log("--- fileItem - file = " + fileItem);
		
		String fileName = fileItem.getName();
		if (fileName == null || fileName.isEmpty()) {
			return false;
		}
		
		InputStream iStream = fileItem.getInputStream();
		String destPath = Config.getTranserPath(path);
		if (destPath == null || destPath.isEmpty()) {
			return false;
		}
		
		File file = new File(destPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		file = new File(file.getAbsolutePath() + File.separator + fileName);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		byte[] buff = new byte[8 * 1024];
		int len = 0;
		
		while((len = iStream.read(buff)) != -1) {
			fileOutputStream.write(buff, 0, len);
			fileOutputStream.flush();
		}
		
		fileOutputStream.close();
		iStream.close();
		result.put(PART_FILE, file.getAbsolutePath());
		return true;
	}
	
	private static class Result {
		private Map<String, String> param;
		Result() {
			param = new HashMap<>();
		}
		
		void put(String k, String v) {
			param.put(k, v);
		}
		
		String get(String k) {
			return param.get(k);
		}
		
		@Override
		public String toString() {
			return param.toString();
		}
	}
}
