package com.honey.ftp;

import java.util.List;

public interface FTPOperations {
	
	<T> T execute(FTPClientCallback<T> action) throws FTPAccessException;
	
	<T> T execute(FTPConnectionCallback<T> action) throws FTPAccessException;
	
	<T> List<T> execute(ListFTPConnectionCallback<T> action) throws FTPAccessException;
}
