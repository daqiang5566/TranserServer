package com.scott.transer;


import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.scott.transer.utils.FileUtils;

public class BaseServletService extends HttpServlet{

	public BaseServletService() {
		
	}
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("path");
		if(path == null || path == "") {
			response.addHeader("Content-Length", "0");
			response.sendError(404);
			return;
		}
		
		if(!FileUtils.checkFileExsits(Config.getTranserPath(path))) {
			response.addHeader("Content-Length", "0");
			response.sendError(404);
			return;
		}
		
		File file = new File(Config.getTranserPath(path));
		response.addHeader("Content-Length", file.length() + "");
	}
}
