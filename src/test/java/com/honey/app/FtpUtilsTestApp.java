package com.honey.app;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.honey.ftp.ConfigProvider;
import com.honey.ftp.FtpCallback;
import com.honey.ftp.FtpFile;
import com.honey.ftp.FtpUtils;

public class FtpUtilsTestApp {
	
	public static void main(String[] args) {
		
		String[] location = new String[]{
    			"spring/applicationContext.xml"	
    		};
		
		AbstractApplicationContext context =
				new ClassPathXmlApplicationContext(location);
		FtpUtils ftpUtils = context.getBean("ftpUtils", FtpUtils.class);
		ftpUtils.downloadDirectory(ConfigProvider.getDownloadConfig(), new FtpCallback<FtpFile, Boolean>() {
			
			@Override
			public Boolean doCall(FtpFile k) throws Exception {
				return true; //delete file anyway
			}
		});
	}
}
