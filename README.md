# commons-pools-ftp

使用ftpcp管理FTP连接
使用这个项目前必须
```bash
git clone https://github.com/honwhy/ftpcp.git
cd ftcp
mvn install
```
使用这个项目还需要参考master分支代码；

multithread downloading
```
FTPCPManager ftpCPManager = new FTPCPManager();
ftpCPManager.setUrl("ftp://127.0.0.1");
ftpCPManager.setUsername("sa");
ftpCPManager.setPassword("sa");
ftpCPManager.setKeepAliveTimeout(1 * 60);

ftpCPManager.setConnectTimeout(1 * 1000);
ftpCPManager.setMaxWait(1 * 1000);

CommonFAOSupport support = new CommonFAOSupport(ftpCPManager);

support.downloadDirectory("/apps/data/ftp/download", 4000, 10, processService); //10 thread
```

改进这个项目的建议，使用内存式ftp server做单元测试，可以参考开源项目org.apache.ftpserver，
也可以参考另外一个项目的做法 github.com/honwhy/ftpcp2