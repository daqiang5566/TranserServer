package com.scott.transer.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimetypesFileTypeMap;

import com.scott.transer.moudle.FileInfo;
import com.sun.org.apache.regexp.internal.recompile;


public class FileUtils {
	
	public static List<FileInfo> getFileList(String path) {
		if(path == null) {
			return null;
		}
		File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		
		if(!file.isDirectory()) {
			return null;
		}
		
		List<FileInfo> fileList = new ArrayList<>();
		File[] files = file.listFiles();
		for(File file2 : files) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.name = file2.getName();
			fileInfo.length = file2.length();
			fileInfo.type = file2.isDirectory() ? 0 : 1;
			fileInfo.path = path + "/" + file2.getName();
			//System.out.println(file2.getPath());
			fileInfo.date = file2.lastModified();
			
			fileList.add(fileInfo);
		}
		return fileList;
	}
	
	public static void writeFile(long start,File input,OutputStream os) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(input, "rw");
		randomAccessFile.seek(start);
		
		int len = 0;
		final int BUFF_SIZE = 1 * 1024 * 1024;
		byte[] buf = new byte[BUFF_SIZE];
		while((len = randomAccessFile.read(buf)) != -1) {
			os.write(buf, 0, len);
			os.flush();
		}
		
		randomAccessFile.close();
		os.close();
	}

	public static long getFileSize(String path) {
		return 0;
	}
	
	public static boolean checkFileExsits(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	public static boolean isFile(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}
		
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		
		return file.isFile();
	}
	
	public static String getMimeTypeByPath(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		return new MimetypesFileTypeMap().getContentType(file);
	}
	
	public static String getFileName(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		
		return new File(path).getName();
	}
}
