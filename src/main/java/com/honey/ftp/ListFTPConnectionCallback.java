package com.honey.ftp;

import java.util.List;

import com.honey.ftpcp.FTPConnection;
import com.honey.ftpcp.FTPException;

public interface ListFTPConnectionCallback<T> {

	List<T> doInFTPConnections(List<FTPConnection> conns) throws FTPException, FTPAccessException;;
	
	int getCount();
	
}
