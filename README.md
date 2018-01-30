# commons-pools-ftp

使用ftpcp管理FTP连接
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