package com.honey.ftp;

import org.apache.commons.net.ftp.FTPClient;

import com.honey.ftpcp.FTPException;

public interface FTPClientCallback<T> {

	T doInFTPClient(FTPClient client) throws FTPException, FTPAccessException;
}
