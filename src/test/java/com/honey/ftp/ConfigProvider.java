package com.honey.ftp;

import com.honey.entity.FtpType;
import com.honey.entity.InterfaceConfig;

public class ConfigProvider {

	protected static InterfaceConfig getUploadConfig() {
		InterfaceConfig config = new InterfaceConfig();
		config.setFtpUrl("127.0.0.1");
		config.setFtpPort("21");
		config.setUserName("root");
		config.setPwd("123456");
		config.setFtpDirectory("/apps/data/ftp/upload");
		config.setFtpType(FtpType.UPLOAD.getCode());
		config.setOrdersCount(50);
		config.setThreadNum(5);
		return config;
	}
	
	protected static InterfaceConfig getDownloadConfig() {
		InterfaceConfig config = new InterfaceConfig();
		config.setFtpUrl("127.0.0.1");
		config.setFtpPort("21");
		config.setUserName("root");
		config.setPwd("123456");
		config.setFtpDirectory("/apps/data/ftp/download");
		config.setFtpType(FtpType.DOWNLOAD.getCode());
		config.setOrdersCount(500);
		config.setThreadNum(10);
		return config;
	}
}
