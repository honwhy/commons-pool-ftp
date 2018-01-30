package com.honey.ftp;

/**
 * 
 * Description: for test 
 *  
 * @date: 2018年1月30日 下午4:11:00
 * @author honwhy.wang  
 * @change log  
 * --------------------------------------------------------
 *   修改者                  时间           修改内容 
 *   honwhy.wang     2018年1月30日        新建 
 * --------------------------------------------------------
 */
public class DefaultDownloadDirectoryCallback extends AbstractDownloadDirectoryCallback {

	public DefaultDownloadDirectoryCallback(String directory, int total, int threadNum) {
		super(directory, total, threadNum);
	}

	@Override
	public boolean afterDownload(String fileName, String content) {
		return false;
	}


}
