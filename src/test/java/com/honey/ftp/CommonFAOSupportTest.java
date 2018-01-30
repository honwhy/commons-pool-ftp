package com.honey.ftp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.honey.ftp.api.CommonProcessFileContentService;
import com.honey.ftpcp.FTPCPManager;

public class CommonFAOSupportTest {

	private static FTPCPManager ftpCPManager;
	private static CommonFAOSupport support;
	
	@BeforeClass
	public static void doBefore() {
		ftpCPManager = new FTPCPManager();
		ftpCPManager.setUrl("ftp://127.0.0.1");
		ftpCPManager.setUsername("sa");
		ftpCPManager.setPassword("sa");
		ftpCPManager.setKeepAliveTimeout(1 * 60);
		
		ftpCPManager.setConnectTimeout(1 * 1000);
		ftpCPManager.setMaxWait(1 * 1000);
		
		support = new CommonFAOSupport(ftpCPManager);
	}
	
	@Test
	public void testDownloadDirectory() {
		CommonProcessFileContentService processService = new CommonProcessFileContentService(){

			public boolean process(String fileName, String content) {
				System.out.println(fileName + ": " + content);
				return false;
			}};
		support.downloadDirectory("/apps/data/ftp/download", 4000, 10, processService);
	}
	
	@AfterClass
	public static void doAfter() {
		try {
			ftpCPManager.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
