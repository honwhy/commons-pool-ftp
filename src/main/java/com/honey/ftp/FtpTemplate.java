package com.honey.ftp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.honey.entity.FtpType;
import com.honey.entity.InterfaceConfig;
import com.google.common.base.Charsets;

@Component("ftpTemplate")
public class FtpTemplate implements FtpOperations<InterfaceConfig> {

	private Logger logger = LoggerFactory.getLogger(FtpTemplate.class);
	
	@Autowired
	private FtpClientPool ftpClientPool;


//	@Override
//	public <K,T> T execute(FtpCallback<K,T> ftpCallback) throws Exception {
//		return ftpCallback.doCall(); //template here deal with exception
//	}

	@Override
	public List<String> listFiles(final InterfaceConfig k, int limit) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在获取" + toFtpInfo(k) + "文件列表");
		}
		final FTPClient client = getFtpClient(getFtpClientPool(), k);
		boolean ret = changeDirectory(client,k);
		List<String> fileList = null;
		try {
			if(ret) {
				String[] fileNames = client.listNames();
				if(fileNames == null) {
					logger.error("获取不到" + toFtpInfo(k) + "文件列表");
				} else {
					int capactiy = Math.min(limit, fileNames.length);
					String[] fileNamesCopy = Arrays.copyOfRange(fileNames, 0, capactiy);
					fileList = Arrays.asList(fileNamesCopy);
				}
			}
			return fileList;
		} catch(Exception e) {
			logger.error("获取" + toFtpInfo(k) + "文件列表异常", e);
			throw e;
		} finally {
			//return to object pool
			if(client != null) {
				returnFtpClient(getFtpClientPool(), k, client);
			}
		}
	}

	@Override
	public String getFile(InterfaceConfig k, String fileName) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在下载" + toFtpInfo(k) + "/" + fileName + "文件");
		}
		final FTPClient client = getFtpClient(getFtpClientPool(), k);
		boolean ret = changeDirectory(client,k);
		
		try {
			if(ret) {
				return performPerFile(client, fileName);
			}
			return null;
		} catch(Exception e) {
			logger.error("下载" + toFtpInfo(k) + "/" + fileName + "文件异常",e);
			throw e;
		} finally {
			//return to object pool
			if(client != null) {
				returnFtpClient(getFtpClientPool(), k, client);
			}
		}
	}
	@Override
	public void getFileCallback(InterfaceConfig k, String fileName, FtpCallback<FtpFile,Boolean> callback) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在下载" + toFtpInfo(k) + "/" + fileName + "文件");
		}
		FTPClient client = getFtpClient(getFtpClientPool(), k);
		if (logger.isDebugEnabled()) {
			logger.debug(client.toString());
		}
		final int PER_MAX_ERROR = 3; //重试三次
		try {
			String content = null;
			boolean getFileErr = true;
			for(int j = 1; j <= PER_MAX_ERROR; j++) {
				try {
					content = getFile(k, fileName, client);
					getFileErr = false;
					logger.error("下载文件成功，重试次数"+ j);
					break;
				} catch (Exception e) {
					logger.error("下载文件出现异常，重试次数"+j, e);
					getFileErr = true;
					getFtpClientPool().invalidateObject(buildFtpClientConfig(k), client);
					client = getFtpClient(getFtpClientPool(), k); //重新获取client
				}
			}
			if(!getFileErr) { //如果下载文件多次重试后没有发生异常
				boolean ret = callback.doCall(new FtpFile(fileName, content));
				if(ret) {
					client.deleteFile(fileName);
				}
			}
			
		} catch(Exception e) {
			logger.error("下载" + toFtpInfo(k) + "/" + fileName + "文件异常",e);
			throw e;
		} finally {
			//return to object pool
			if(client != null) {
				returnFtpClient(getFtpClientPool(), k, client);
			}
		}
	}
	
	//内部方法
	private String getFile(InterfaceConfig k, String fileName, FTPClient client) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在下载" + toFtpInfo(k) + "/" + fileName + "文件");
		}
		boolean ret = changeDirectory(client,k);
		
		try {
			if(ret) {
				return performPerFile(client, fileName);
			}
			return null;
		} catch(Exception e) {
			logger.error("下载" + toFtpInfo(k) + "/" + fileName + "文件异常",e);
			throw e;
		} finally {
			//the work of returning client to pool is delegated outside			
		}
	}
	
	@Override
	public boolean putFile(InterfaceConfig k, String content, String fileName) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在上传文件到" + toFtpInfo(k) + "/" + fileName);
		}
		final FTPClient client = getFtpClient(getFtpClientPool(), k);
		boolean ret = changeDirectory(client,k); //切换目录失败无异常
		BufferedInputStream inStream = null;
		boolean success = false;
		try {
			if (ret) {
				inStream = new BufferedInputStream(new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)));
				success = client.storeFile(fileName, inStream);
			}
			return success;
		} catch (Exception e) {
			logger.error("正在上传文件到" + toFtpInfo(k) + "/" + fileName + "异常",e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error("关闭文件流时发生异常",e);
				}
			}
			//return to object pool
			if(client != null) {
				returnFtpClient(getFtpClientPool(), k, client);
			}
		}
		return success;
	}

	@Override
	public boolean deleteFile(InterfaceConfig k, String fileName) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("正在删除" + toFtpInfo(k) + "/" + fileName + "文件");
		}
		final FTPClient client = getFtpClient(getFtpClientPool(), k);
		boolean ret = changeDirectory(client,k);
		
		try {
			if(ret) {
				return client.deleteFile(fileName);
			}
			return false;
		} catch(Exception e) {
			logger.error("删除" + toFtpInfo(k) + "/" + fileName + "文件异常", e);
			throw e;
		} finally {
			//return to object pool
			if(client != null) {
				returnFtpClient(getFtpClientPool(), k, client);
			}
		}
		
	}
	
	private String toFtpInfo(InterfaceConfig k) {
		StringBuffer sb = new StringBuffer();
		sb.append("[")
			.append(k.getFtpUrl())
			.append(":")
			.append(k.getFtpPort())
			.append("]")
			.append("/")
			.append(k.getFtpDirectory());
		return sb.toString();
	}
	private FTPClient getFtpClient(FtpClientPool ftpClientPool, InterfaceConfig k) throws Exception {
		FtpClientConfig config = buildFtpClientConfig(k);
		FTPClient client = null;
		try {
			client = ftpClientPool.borrowObject(config);
		} catch (Exception e) {
			logger.error("获取FTPClient对象异常 " + toFtpInfo(k),e);
			throw e;
		}
		return client;
	}
	private void returnFtpClient(FtpClientPool ftpClientPool, InterfaceConfig k, FTPClient client) {
		FtpClientConfig config = buildFtpClientConfig(k);
		ftpClientPool.returnObject(config, client); //TODO be careful
	}
	private FtpClientConfig buildFtpClientConfig(InterfaceConfig k) {
		FtpClientConfig config = new FtpClientConfig();
		config.setHost(k.getFtpUrl());
		config.setPort(Integer.valueOf(k.getFtpPort()));
		config.setUsername(k.getUserName());
		config.setPassword(k.getPwd());
		if(FtpType.UPLOAD.getCode().equals(k.getFtpType())) {
			config.setTransType(1);
		} else {
			config.setTransType(2);
		}
		return config;
	}
	private boolean changeDirectory(FTPClient client, InterfaceConfig k) throws Exception{
		boolean ret = false;
		try {
			ret = client.changeWorkingDirectory(k.getFtpDirectory());
			if(!ret) {
				logger.error("切换目录失败{}", toFtpInfo(k));
				throw new Exception(String.format("切换目录失败 %s",toFtpInfo(k)));
			}
		} catch (Exception e) {
			logger.error("切换目录失败：" + toFtpInfo(k), e);
			throw e; //抛出去
		}
		return ret;
	}
	private String performPerFile(FTPClient client, String fileName) throws Exception{
		String content = "";
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			client.enterLocalPassiveMode();
			boolean downSuc = client.retrieveFile(fileName, os);
			if(downSuc){
				InputStream inputstream = new ByteArrayInputStream(os.toByteArray());
				content = IOUtils.toString(inputstream, "UTF-8");
				if (inputstream != null) { 
					inputstream.close(); 
	            }
				if(os!=null){
					os.close();
				}
			}else{
				logger.info("下载报文失败:" + fileName);
				throw new Exception("下载报文失败:" + fileName);
			}
		} catch (Exception e1) {
			logger.error("ftp 通信错误 或 文件IO出错, 下载文件成功状态:"+client.getRemoteAddress(), e1);
			throw e1;
		}
		
		return content;
	}
	public FtpClientPool getFtpClientPool() {
		return ftpClientPool;
	}
	
	public void setFtpClientPool(FtpClientPool ftpClientPool) {
		this.ftpClientPool = ftpClientPool;
	}

}
