package com.honey.ftp;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.honey.ftpcp.FTPConnection;
import com.honey.ftpcp.FTPConnectionUtil;
import com.honey.ftpcp.FTPException;
import com.honey.ftpcp.FTPManager;

public class FTPClientTemplate implements FTPOperations, InitializingBean{

	private FTPManager ftpManager;
	
	public FTPClientTemplate() {
	}
	
	public FTPClientTemplate(FTPManager ftpManager) {
		this.ftpManager = ftpManager;
		afterPropertiesSet();
	}

	public <T> T execute(FTPClientCallback<T> action) throws FTPAccessException {
		Assert.notNull(action, "Callback object must not be null");

		FTPConnection conn = null;
		try {
			conn = FTPConnectionUtil.getConnection(ftpManager);
			FTPClient client = conn.unwrap(FTPClient.class);
			return action.doInFTPClient(client);
		}
		catch (FTPException ex) {
			throw new FTPAccessException(ex);
		}
		finally {
			FTPConnectionUtil.releaseConnection(conn);
		}
	}
	


	public <T> T execute(FTPConnectionCallback<T> action) throws FTPAccessException {
		Assert.notNull(action, "Callback object must not be null");

		FTPConnection conn = null;
		try {
			conn = FTPConnectionUtil.getConnection(ftpManager);
			return action.doInFTPConnection(conn);
		}
		catch (FTPException ex) {
			throw new FTPAccessException(ex);
		}
		finally {
			FTPConnectionUtil.releaseConnection(conn);
		}
	}

	public <T> List<T> execute(ListFTPConnectionCallback<T> action) throws FTPAccessException {
		Assert.notNull(action, "Callback object must not be null");
		Assert.isTrue(action.getCount()>=1, "Callback object count must larger than 1");
		int initalCount = action.getCount();
		List<FTPConnection> conns = new ArrayList<FTPConnection>(initalCount);
		
		try {
			while(initalCount > 0) {
				FTPConnection conn = null;
				try {
					conn = FTPConnectionUtil.getConnection(ftpManager);
					if(conn != null) {
						conns.add(conn);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				initalCount--;
			}
			return action.doInFTPConnections(conns);
		}
		catch (FTPException ex) {
			throw new FTPAccessException(ex);
		}
		finally {
			for(FTPConnection conn: conns) {
				FTPConnectionUtil.releaseConnection(conn);
			}
		}
	}
	
	public void afterPropertiesSet() {
		if(ftpManager == null) {
			throw new IllegalArgumentException("FTPManager can not be null");
		}
		
	}


}
