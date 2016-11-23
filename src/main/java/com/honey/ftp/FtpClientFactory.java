package com.honey.ftp;

import java.io.IOException;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FtpClientFactory implements KeyedPooledObjectFactory<FtpClientConfig, FTPClient>{


	private static final String LOCAL_ENCODING="UTF-8";
	
	private static final Logger LOGGER =
			LoggerFactory.getLogger(FtpClientFactory.class);
	
	@Override
	public PooledObject<FTPClient> makeObject(FtpClientConfig key)
			throws Exception {
		FTPClient cli = new FTPClient();
		cli.setBufferSize(1024 * 2);
		FTPClientConfig ftpClientConfig = new FTPClientConfig();
        ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
        cli.setControlEncoding(LOCAL_ENCODING);
        cli.configure(ftpClientConfig);
        long connectStart = System.currentTimeMillis();
        cli.setConnectTimeout(3000); //timeout 3 seconds
        cli.setDataTimeout(30 * 1000);
		cli.connect(key.getHost(), key.getPort());
		long connectUsed = System.currentTimeMillis() - connectStart;
		LOGGER.info("connecting " + key.toString() + " used time :" + connectUsed + " ms.");
		int reply = cli.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply)){
			cli.disconnect();
            LOGGER.error("ftp connect fail," + key.toString());
            return null;
		}
		// 设置传输协议  
        cli.enterLocalPassiveMode();
		long loginStart = System.currentTimeMillis();
		boolean loginSuccess = cli.login(key.getUsername(), key.getPassword());
		long loginUsed = System.currentTimeMillis() - loginStart;
		LOGGER.info("login " + key.toString() + " used time :" + loginUsed + " ms.");
		if(loginSuccess){
			LOGGER.debug("FTP login success : " + key.toString());
		}else{
			LOGGER.error("FTP login fail : " + key.toString());
			return null;
		}
        reply = cli.getReplyCode();
        cli.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = cli.getReplyCode();
        
		return new DefaultPooledObject<FTPClient>(cli);
	}

	@Override
	public void destroyObject(FtpClientConfig key, PooledObject<FTPClient> p)
			throws Exception {
		LOGGER.info("Ready to destoy client object.");
		FTPClient cli = p.getObject();
		
		if(cli == null) return;
		
		if(cli.isConnected()){
			cli.logout();
			cli.disconnect();
		}
	}

	private String SYS_COMM = "system";
	
	@Override
	public boolean validateObject(FtpClientConfig key, PooledObject<FTPClient> p) {
		FTPClient cli = p.getObject();
		
		boolean isConnected = cli.isConnected();
		boolean isAvailable = cli.isAvailable();
		
		if(!isConnected || !isAvailable){
			LOGGER.info("连接失效 : " + key + ", 连接状态: " + isConnected + ", 可用状态 : " + isAvailable);
			return false;
		}
		
		try {
			// 心跳
			cli.sendCommand(SYS_COMM);
			return true;
		} catch (IOException e) {
			LOGGER.error("error happen when communicating with ftp server:" + key , e);
			return false;
		}
	}

	@Override
	public void activateObject(FtpClientConfig key, PooledObject<FTPClient> p)
			throws Exception {
		// TODO Auto-generated method stub ftp login
		
	}

	@Override
	public void passivateObject(FtpClientConfig key, PooledObject<FTPClient> p)
			throws Exception {
		// TODO Auto-generated method stub ftp logout
		// TODO change to root directory
		
	}

}
