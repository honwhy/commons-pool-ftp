package com.honey.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.springframework.util.Assert;

import com.honey.ftpcp.FTPConnection;
import com.honey.ftpcp.FTPConnectionUtil;
import com.honey.ftpcp.FTPException;

public abstract class AbstractDownloadDirectoryCallback implements ListFTPConnectionCallback<Void> {
	
	private String directory;
	private int total = -1;
	private int initalCount;
	
	public AbstractDownloadDirectoryCallback(String directory, int total, int threadNum) {
		this.directory = directory;
		this.total = total;
		this.initalCount = threadNum;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Void> doInFTPConnections(List<FTPConnection> conns)  throws FTPException, FTPAccessException {
		Assert.notEmpty(conns, "FTPConnection list can not be empty");
		List<?> results = Collections.emptyList();
		FTPConnection conn = conns.get(0);
		FTPClient client = conn.unwrap(FTPClient.class);
		try {
			boolean flag = client.changeWorkingDirectory(directory);
			if(!flag) {
				//logger here
				return (List<Void>) results;
			}
			ExecutorService workPool = Executors.newFixedThreadPool(10);
			FTPListParseEngine engine = client.initiateListParsing(directory);
			int pageSize = total/conns.size();
			int count = 0;
			int threadCount = conns.size();
			CountDownLatch cdl = new CountDownLatch(threadCount);
			while(engine.hasNext() && threadCount>0) {
				FTPFile[] files = engine.getNext(pageSize);
				count += files.length;
				
				if(files.length > 0) {
					FTPConnection _conn = conns.get(threadCount-1);
					workPool.submit(new PagedDownloadProcess(_conn, Arrays.asList(files), cdl));
				}
				threadCount--;
			}
			workPool.shutdown();
			while(threadCount>0) {
				cdl.countDown();
				FTPConnection toCloseConn = conns.get(threadCount-1);
				FTPConnectionUtil.releaseConnection(toCloseConn);
				conns.remove(toCloseConn);
				threadCount -- ;
			}
			
			cdl.await();
			//logger count
		} catch (IOException e) {
			throw new FTPAccessException(e);
		} catch (InterruptedException e) {
			throw new FTPAccessException(e);
		}
		return (List<Void>) results;
	}

	public int getCount() {
		return initalCount;
	}

	class PagedDownloadProcess implements Runnable {
		private final FTPConnection conn;
		private final List<FTPFile> files;
		private final CountDownLatch cdl;
		
		PagedDownloadProcess(FTPConnection conn, List<FTPFile> files, CountDownLatch cdl) {
			this.conn = conn;
			this.files = files;
			this.cdl = cdl;
		}
		public void run() {
			try {
				String base = directory;
				FTPClient client = conn.unwrap(FTPClient.class);
				for(FTPFile file : files) {
					try {
						String fileName = base + "/" + file.getName();
						String content = performPerFile(client, fileName);
						boolean flag = afterDownload(fileName, content);
						if(flag) {
							client.deleteFile(fileName);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				cdl.countDown();
			}
		}
		
	}
	private String performPerFile(FTPClient client, String fileName) throws IOException{
		String content = "";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
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
		}
		
		return content;
	}	
	public abstract boolean afterDownload(String fileName, String content);
}
