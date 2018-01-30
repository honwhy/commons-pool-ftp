package com.honey.ftp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.InitializingBean;

import com.honey.ftp.api.CommonProcessFileContentService;
import com.honey.ftpcp.FTPConnection;
import com.honey.ftpcp.FTPException;
import com.honey.ftpcp.FTPManager;

public class CommonFAOSupport implements InitializingBean {

	private FTPClientTemplate ftpClientTemplate;
	//private ExecutorService workPool = Executors.newFixedThreadPool(10);
	public CommonFAOSupport(FTPManager ftpManager) {
		this.ftpClientTemplate = new FTPClientTemplate(ftpManager);
	}

	public boolean uploadFile(final String directory, final String fileName, final String content) {
		FTPConnectionCallback<Boolean> action = new FTPConnectionCallback<Boolean>() {

			public Boolean doInFTPConnection(FTPConnection conn) throws FTPException, FTPAccessException {
				FTPClient client = conn.unwrap(FTPClient.class);
				boolean flag = false;
				BufferedInputStream bis = null;
				try {
					flag = client.changeWorkingDirectory(directory);
					if(flag) {
						bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name())));
						flag = client.storeFile(fileName, bis);
					}
					return flag;
				} catch (IOException e) {
					throw new FTPAccessException(e);
				}
			}
			
		};
		return ftpClientTemplate.execute(action);
	}
	
	public void downloadDirectory(String directory, int total, int threadNum, final CommonProcessFileContentService processService) {
		ListFTPConnectionCallback<Void> action = new AbstractDownloadDirectoryCallback(directory, total, threadNum) {
			
			@Override
			public boolean afterDownload(String fileName, String content) {
				return processService.process(fileName, content);
			}
		};
		ftpClientTemplate.execute(action);
	}
	
	public void afterPropertiesSet() throws Exception {
		if(ftpClientTemplate == null) {
			throw new IllegalArgumentException("FTPClientTemplate can not be null");
		}
		
	}
	
}
