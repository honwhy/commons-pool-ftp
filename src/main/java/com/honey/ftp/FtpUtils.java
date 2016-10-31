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
			if(CollectionUtils.isEmpty(fileNames)) {
				logger.info("从FTP目录{}未获取到文件列表", toFtpInfo(config));
				return ;
			}
			int pageSize = fileNames.size() / config.getThreadNum(); //每个线程的分页大小
			if(pageSize == 0) {
				pageSize = DEFAULT_PAGE_SIZE;
			}
			int page = 1;
			int offset = 0;
			for(int i = 0; i < config.getThreadNum(); i++) {
				int end = page * pageSize + offset;
				end = Math.min(end, fileNames.size()); 
				String array[] = Arrays.copyOfRange(fileNames.toArray(new String[fileNames.size()]), offset, end);
				logger.debug("{}",array.length);
				try {
					//BlockingQueue<String> fileQueue = new ArrayBlockingQueue<String>(array.length, false, Arrays.asList(array)); //生产者资料
					workPool.execute(new GetFileConsumer(config, array, callback));
				} catch (Exception e) {
					logger.error("提交线程出现异常",e);
				}
				offset = end; //下一个分页
				if(end == fileNames.size()) {
					break;
				}
				page++;
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
		private final String array[];
		private final FtpCallback<FtpFile,Boolean> callback;
		public GetFileConsumer(InterfaceConfig config, String[] array,
				FtpCallback<FtpFile,Boolean> callback) {
			this.config = config;
			this.array = array;
			this.callback = callback;
		}
		@Override
		public void run() {
					for(String fileName : array) {						
						try {
							ftpTemplate.getFileCallback(config, fileName, callback);
						} catch (Exception e) {
							cslogger.error("下载文件{}{}出现异常{}", toFtpInfo(config), fileName,e);
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
}
