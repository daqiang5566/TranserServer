package com.scott.transer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.WebdavServlet;

import com.scott.transer.utils.FileUtils;

public abstract class BaseServletService extends WebdavServlet{
	protected String mPath;
	private HttpServletRequest mRequest;
	private HttpServletResponse mReponse;
	
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
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
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(!enablePost()) {
			response.sendError(405, "POST Not Support!");
			return;
		}
		mReponse = response;
		mRequest = request;
		if(!readRequest()) {
			return;
		}
		writeReponse();
		
		mReponse = null;
		mRequest = null;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(!enableGet()) {
			response.sendError(405,"GET Not Support!");
			return;
		}
		
		mReponse = response;
		mRequest = request;
		if(!readRequest()) {
			return;
		}
		writeReponse();
		
		mReponse = null;
		mRequest = null;
	}
	
	
	protected HttpServletRequest getRequest() {
		return mRequest;
	}
	
	protected HttpServletResponse getResponse() {
		return mReponse;
	}
	
	protected boolean enablePost() {
		return false;
	}
	
	protected boolean enableGet() {
		return false;
	}
	
	protected abstract void writeReponse() throws IOException, ServletException;
	
	protected abstract boolean readRequest() throws IOException, ServletException;
}
