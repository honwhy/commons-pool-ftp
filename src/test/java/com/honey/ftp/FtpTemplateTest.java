package com.honey.ftp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.honey.entity.InterfaceConfig;
import com.honey.test.BaseTest;

public class FtpTemplateTest extends BaseTest{

	@Autowired
	private FtpTemplate ftpTemplate;
	
	@Test
	public void testListFiles() throws Exception {
		List<String> fileNames = ftpTemplate.listFiles(ConfigProvider.getDownloadConfig(),1000);
		System.out.println(fileNames);
	}
	@Test
	public void testPutFile() throws Exception {
		String content = "It works!";
		String fileName = UUID.randomUUID().toString() + ".txt";
		InterfaceConfig k = ConfigProvider.getUploadConfig();
		boolean ret = false;
		try {
			ret = ftpTemplate.putFile(k, content, fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(ret);
	}
	@Test
	public void testUsePutFile() throws Exception {
		long tstart = System.currentTimeMillis();
		for(int i = 0; i < 100; i++) {
			try {
				testPutFile();
				Thread.sleep(100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[testUsePutFile]cost time: " + (System.currentTimeMillis() - tstart));
	}
	@Test
	public void testMultiPutFile() {
		long tstart = System.currentTimeMillis();
		int size = 1000;
		ArrayList<FileInfo> fileList = Lists.newArrayList();
		int i = 0;
		while(i < size) {
			FileInfo fi = randomGetFile();
			fileList.add(fi);
			i++;
		}
		BlockingQueue<FileInfo> fileQueue = new ArrayBlockingQueue<FileInfo>(size, false, fileList);
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(100);
		ThreadPoolExecutor workPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, workQueue);
		for(int j = 0; j < size; j++) {
			try {
				workPool.submit(new PutFileConsumer(fileQueue));
				//Thread.sleep(50);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[testMultiPutFile]cost time: " + (System.currentTimeMillis() - tstart));
		//sleep 10 second
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testGetAllFile() throws Exception {
		long tstart = System.currentTimeMillis();
		List<String> fileNames = ftpTemplate.listFiles(ConfigProvider.getDownloadConfig(), 1000);
		
		BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>(fileNames);
		
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(100);
		ThreadPoolExecutor workPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, workQueue);
		for(int j = 0; j < 10; j++) {
			try {
				workPool.submit(new GetFileConsumer(fileQueue));
				//Thread.sleep(50);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(String fileName : fileNames) {			
			String content = ftpTemplate.getFile(ConfigProvider.getDownloadConfig(), fileName);
			System.out.println(content);
		}
		System.out.println("[testGetAllFile]cost time: " + (System.currentTimeMillis() - tstart));
	}
	class GetFileConsumer implements Runnable {
		private final BlockingQueue<String> fileQueue;
		public GetFileConsumer(BlockingQueue<String> fileQueue) {
			this.fileQueue = fileQueue;
		}
		@Override
		public void run() {
			while(true) {
				try {
					System.out.println("GetFileConsumer running...");
					String fileName = fileQueue.poll();
					if(fileName == null) {
						System.out.println("GetFileConsumer stopping...");
						break;
					}
					String content = ftpTemplate.getFile(ConfigProvider.getDownloadConfig(), fileName);
					System.out.println(content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	@Test
	public void testDeleteAllFile() throws Exception {
		long tstart = System.currentTimeMillis();
		List<String> fileNames = ftpTemplate.listFiles(ConfigProvider.getDownloadConfig(),1000);
		for(String fileName : fileNames) {			
			boolean content = ftpTemplate.deleteFile(ConfigProvider.getDownloadConfig(), fileName);
			System.out.println(content);
		}
		System.out.println("[testDeleteAllFile]cost time: " + (System.currentTimeMillis() - tstart));
	}
	class PutFileConsumer implements Runnable {
		private final BlockingQueue<FileInfo> fileQueue;
		public PutFileConsumer(final BlockingQueue<FileInfo> fileQueue) {
			this.fileQueue = fileQueue;
		}
		@Override
		public void run() {
			System.out.println("Consumer running...");
			try {
				FileInfo fi = fileQueue.take();
				if(fi == null) {
					return;
				}
				boolean ret = ftpTemplate.putFile(ConfigProvider.getDownloadConfig(), fi.getContent(), fi.getFileName());
				System.out.println(ret);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	class FileInfo {
		private String content;
		private String fileName;
		public FileInfo(String fileName, String content) {
			this.fileName = fileName;
			this.content = content;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
	}
	
	private FileInfo randomGetFile() {
		String content = UUID.randomUUID().toString();
		String fileName = UUID.randomUUID().toString() + ".txt";
		return new FileInfo(fileName, content);
	}
}
