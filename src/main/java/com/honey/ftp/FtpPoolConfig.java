package com.honey.ftp;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

public class FtpPoolConfig extends GenericKeyedObjectPoolConfig{
	public FtpPoolConfig() {
		setTestWhileIdle(true);
		setTimeBetweenEvictionRunsMillis(60000);
		setMinEvictableIdleTimeMillis(1800000L);
		setTestOnBorrow(true);
	}
}
