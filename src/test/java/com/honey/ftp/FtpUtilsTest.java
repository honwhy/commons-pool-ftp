package com.honey.ftp;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.honey.test.BaseTest;


public class FtpUtilsTest extends BaseTest{

	@Autowired
	private FtpUtils ftpUtils;
	
	@Test
	public void testUploadFile() throws Exception {
		String fileName = UUID.randomUUID().toString()+ ".xml";
		String content = UUID.randomUUID().toString();
		boolean ret = ftpUtils.uploadFile(ConfigProvider.getUploadConfig(), fileName, content);
		assertTrue(ret);
	}
	
	@Test
	public void testDownloadDirectory() {
		ftpUtils.downloadDirectory(ConfigProvider.getDownloadConfig(), new FtpCallback<FtpFile,Boolean> (){

			@Override
			public Boolean doCall(FtpFile k) throws Exception {
				//backupFile(k);
				System.out.println(k.getFileName());
				return true; 
			}
			
		});
	}
	
	@Test
	public void testUploadFileLoop() throws Exception {
		long tstart = System.currentTimeMillis();
		for(int i = 0; i < 500; i++) {
			testUploadFile();
		}
		System.out.println("[testUpAndDownload]cost time: " + (System.currentTimeMillis() - tstart));
	}
	
	@Test
	public void testUploadFileThread() {
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(100);
		ThreadPoolExecutor workPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, workQueue);
		for(int i = 0; i < 500; i++) {
			try {
				workPool.execute(new PutFileThread());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class PutFileThread implements Runnable {

		@Override
		public void run() {
			try {
				testUploadFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	private String backupFile(FtpFile ftpFile){
		return "";
		
	}
}
