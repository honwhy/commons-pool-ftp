package com.honey.ftp;

import java.util.List;

public interface FtpOperations<K> {

	//<T> T execute(FtpCallback<K,T> ftpCallback) throws Exception;
	List<String> listFiles(K k, int limit) throws Exception;
	String getFile(K k, String fileName) throws Exception;
	void getFileCallback(K k, String fileName, FtpCallback<FtpFile,Boolean> callback) throws Exception;
	boolean putFile(K k, String content, String fileName) throws Exception;
	boolean deleteFile(K k, String fileName) throws Exception;
	
}
