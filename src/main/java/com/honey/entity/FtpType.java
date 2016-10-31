
package com.honey.entity;


public enum FtpType {

	UPLOAD("0", "上传"),
	DOWNLOAD("1", "下载");
	
	private String code;
	private String name;
	
	private FtpType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
}
