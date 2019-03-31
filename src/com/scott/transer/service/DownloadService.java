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
import com.sun.javafx.collections.MappingChange.Map;
import com.sun.org.apache.regexp.internal.recompile;

@WebServlet("/download")
public class DownloadService extends BaseServletService{
	static final int BUFF_SIZE = 1 * 1024 * 1024;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getParameter("path");
		log("request file path = " + Config.getTranserPath(path));
		path = Config.getTranserPath(path);
		if(!FileUtils.checkFileExsits(path) || !FileUtils.isFile(path)) {
			resp.sendError(404,"path:" + path + " not find!");
			log("-----" + FileUtils.checkFileExsits(path) + "--" + FileUtils.isFile(path));
			return;
		}
		
		log("----------------------------");
		
		
		String content_range = req.getHeader("Range");
		long startOffSet = getStartOffset(content_range, resp);
		// TODO valid end offset 
		long endOffset = getEnfOffset(content_range, resp);
		
		RandomAccessFile rFile = new RandomAccessFile(new File(path), "r");
		rFile.seek(startOffSet);
		
		byte[] buff = new byte[BUFF_SIZE];
		int len = 0;
		long allLength = 0;
		
		resp.addHeader("Content-Length", allLength + "");
		resp.addHeader("Content-Type", FileUtils.getMimeTypeByPath(path));
		resp.addHeader("Accept", FileUtils.getMimeTypeByPath(path));

		if (FileUtils.getFileName(path) != null) {
			resp.addHeader("Content-Disposition", "attachment;filename=" + FileUtils.getFileName(path));
		}
		
		OutputStream oStream = resp.getOutputStream();
		while((len = rFile.read(buff)) != -1) {
			oStream.write(buff,0,len);
			oStream.flush();
			allLength += len;
		}
		
		log("---- mimeType = " + FileUtils.getMimeTypeByPath(path));
		log("---- mimeType = " + FileUtils.getFileName(path));

		oStream.close();
		rFile.close();
		return;
	}
	
	private long getEnfOffset(String content_range, HttpServletResponse resp) throws IOException {
		long endOffset = 0;
		if (content_range == null || content_range.isEmpty()) {
			return endOffset;
		}
		
		content_range = content_range.replace("bytes=", "");
		try {
			String[] sArr = content_range.split("-");
			endOffset = Long.parseLong(sArr[1]);
		} catch (Exception e) {
			resp.sendError(406,"Range header is not a valid value!");
		}
		
		return endOffset;
	}

	private long getStartOffset(String content_range, HttpServletResponse resp) throws IOException {
		long startOffSet = 0;
		
		if (content_range == null || content_range.isEmpty()) {
			return startOffSet;
		}
		
		content_range = content_range.replace("bytes=", "");
		try {
			String[] sArr = content_range.split("-");
			startOffSet = Long.parseLong(sArr[0]);
		} catch (Exception e) {
			resp.sendError(406,"Range header is not a valid value!");
		}
		
		return startOffSet;
	}
}
