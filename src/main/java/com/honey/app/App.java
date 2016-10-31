package com.honey.app;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.honey.ftp.FtpClientPool;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	String[] location = new String[]{
    			"spring/applicationContext.xml"	
    		};
		
		AbstractApplicationContext context =
				new ClassPathXmlApplicationContext(location);
    		
		FtpClientPool ftpClientPool = context.getBean("ftpClientPool", FtpClientPool.class);
		System.out.println(ftpClientPool.toString());
    }
}
