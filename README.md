# commons-pools-ftp demo
a demo project using commons-pools and commons-net to interact with ftp server

change the `getUploadConfig` method to provide ftp uploading configurations.
```
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
```
# Thanks
* [PortableFtpServer](https://sourceforge.net/projects/portable-ftp-server/)

# TODO
* Multithread unit test
* 封装成组件形式

# Note
* 对象池维护是第一次FTP登录成功的连接，实际传输文件时还有data connection的连接，但并没有（办法）维护起来，所以看起来整个对象池只做了一半的工作
× 还有一些发现