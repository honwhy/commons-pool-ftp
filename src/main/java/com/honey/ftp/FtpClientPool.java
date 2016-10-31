package com.honey.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class FtpClientPool extends GenericKeyedObjectPool<FtpClientConfig, FTPClient>  {

	public FtpClientPool(FtpClientFactory factory, FtpPoolConfig config) {
		super(factory, config);
	}

}
