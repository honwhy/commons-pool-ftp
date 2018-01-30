package com.honey.ftp;

import com.honey.ftpcp.FTPConnection;
import com.honey.ftpcp.FTPException;

public interface FTPConnectionCallback<T> {

	T doInFTPConnection(FTPConnection conn) throws FTPException, FTPAccessException;
}
