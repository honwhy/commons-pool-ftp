package com.honey.ftp;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.honey.entity.InterfaceConfig;

public class FtpUtils {
	private static final Logger logger = LoggerFactory.getLogger(FtpUtils.class);
	
	private static final int MAX_CORE_NUM = 5;
	private static final int MAX_THREAD_NUM = 10;
	private static final int DEFAULT_PAGE_SIZE = 10;
	@Autowired
	private FtpTemplate ftpTemplate;
	private ConcurrentHashMap<String, ThreadPoolExecutor> poolMap = new ConcurrentHashMap<>(); //存储线程池 key=id+interfaceCode
	
	public void downloadDirectory(InterfaceConfig config,
			 final FtpCallback<FtpFile,Boolean> callback) {
		logger.info("正在下载FTP目录" + toFtpInfo(config));
		ThreadPoolExecutor workPool = poolMap.get(config.getId()+config.getInterfaceCode());
		if(workPool == null) {
			BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(100);
			workPool = new ThreadPoolExecutor(MAX_CORE_NUM, MAX_THREAD_NUM, 1, TimeUnit.MINUTES, workQueue);
			poolMap.put(config.getId() + config.getInterfaceCode(), workPool);
		}
		try {
			List<String> fileNames = ftpTemplate.listFiles(config,config.getOrdersCount());
			BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>(fileNames); //生产者资料
			for(int i = 0; i < config.getThreadNum(); i++) {
				try {
					workPool.execute(new GetFileConsumer(config, fileQueue, callback));
				} catch (Exception e) {
					logger.error("提交线程出现异常",e);
				}
			}
		} catch (Exception e) {
			logger.error("FTP操作出现异常"+toFtpInfo(config),e);
		}
		logger.info("下载FTP目录完成" + toFtpInfo(config));
	}
	public boolean uploadFile(InterfaceConfig config, String fileName, String content) throws Exception{
		logger.info("正在上传FTP文件{}到{}",fileName, toFtpInfo(config));
		boolean ret = false;
		try {
			ret = ftpTemplate.putFile(config, content, fileName);
		} catch (Exception e) {
			logger.error("FTP操作出现异常"+toFtpInfo(config),e);
			throw e;
		}
		logger.info("上传FTP文件{}到{}完成", fileName, toFtpInfo(config));
		return ret;
	}
	class GetFileConsumer implements Runnable {
		private final Logger cslogger = LoggerFactory.getLogger(GetFileConsumer.class);
		private final InterfaceConfig config;
		private final BlockingQueue<String> fileQueue;
		private final FtpCallback<FtpFile,Boolean> callback;
		public GetFileConsumer(InterfaceConfig config, BlockingQueue<String> fileQueue,
				FtpCallback<FtpFile,Boolean> callback) {
			this.config = config;
			this.fileQueue = fileQueue;
			this.callback = callback;
		}
		@Override
		public void run() {
			while(true) {
				String fileName = null;
				try {
					if (cslogger.isDebugEnabled()) {
						cslogger.debug("准备下载并处理文件");
					}
					fileName = fileQueue.poll();
					if(fileName == null) {
						if (cslogger.isDebugEnabled()) {
							cslogger.debug("文件队列为空下载处理文件线程结束");
						}
						break;
					}
					ftpTemplate.getFileCallback(config, fileName, callback);
				} catch (Exception e) {
					cslogger.error("下载或处理文件异常" + toFtpInfo(config) + "/" + fileName,e);
				}
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
	public FtpTemplate getFtpTemplate() {
		return ftpTemplate;
	}
	public void setFtpTemplate(FtpTemplate ftpTemplate) {
		this.ftpTemplate = ftpTemplate;
	}
	
}
