package com.crawl.web.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.crawl.web.constants.WebCrawlerConstants;
import com.crawl.web.service.CrawlerService;
import com.crawl.web.service.impl.WebCrawlerServiceImpl.ApplicationCache;

import junit.framework.Assert;
import junit.framework.TestCase;

public class WebCrawlerServiceImplTest{
	

	private static final String CONFIG_PATH = "classpath:Application-config-crawler.xml";
	
	private static ApplicationContext context;
	/**
	 * 
	 * @throws Exception
	 * Check if all properties are loaded by spring IOC.
	 * Asserts if any of the property is null
	 */
	
	@BeforeClass
	public static void method(){
		context = new ClassPathXmlApplicationContext(
				CONFIG_PATH);
	}
	
	@Test
	public void testsaveMails(){
		final WebCrawlerServiceImpl crawler = context.getBean(WebCrawlerServiceImpl.class);
		
		List<String> mailURLList=new ArrayList<String>();
		mailURLList.add("http://mail-archives.apache.org/mod_mbox/maven-users/201301.mbox/ajax/%3CCAKyLS_xPDgpQpCK45m_nQAybd-_v0TnpORAFBMDZSPZCkeEEQQ%40mail.gmail.com%3E");
		mailURLList.add("http://mail-archives.apache.org/mod_mbox/maven-users/201412.mbox/ajax/%3C547C1A5F.7070709%40uni-jena.de%3E");
		mailURLList.add("http://mail-archives.apache.org/mod_mbox/maven-users/201409.mbox/ajax/%3C20140902102202.F09D011C00B4%40dd12814.kasserver.com%3E");
		
		String directoryName="testDirectory";
		
		boolean flag=false;
		try {
			flag = crawler.saveMails(mailURLList, directoryName);
		} catch (IOException e) {
			Assert.fail("Exception occurred while saving mails !!!"+e.getLocalizedMessage());
		}
		
		Assert.assertTrue("Failed to save email Content !!!",flag);
	}

	@Test
	public void testSavePoint(){
		final WebCrawlerServiceImpl crawler = context.getBean(WebCrawlerServiceImpl.class);
		String directoryName="testDirectory";
		String url="http://mail-archives.apache.org/mod_mbox/maven-users/201409.mbox/ajax/%3C20140902102202.F09D011C00B4%40dd12814.kasserver.com%3E";
		
		ApplicationCache.getInstance().setAppCacheValue(
				WebCrawlerConstants.KEY_DIRECTORY
						+ WebCrawlerConstants.KEY_VALUE_SEPERATOR,
				directoryName);
		ApplicationCache.getInstance().setAppCacheValue(
				WebCrawlerConstants.KEY_URL
						+ WebCrawlerConstants.KEY_VALUE_SEPERATOR, url);
		crawler.savePoint(true);
		Hashtable<String, String> savePoint = new Hashtable<String, String>();
		try {
			savePoint=crawler.retrieveSavePoint(true);
		} catch (IOException e) {
			Assert.fail("Failed to retrieve save point !!!"+e.getLocalizedMessage());
		}
		Assert.assertEquals("Invalid save point URL !!!", url,savePoint.get("URL"));
		Assert.assertEquals("Invalid save point URL !!!", directoryName,savePoint.get("DIRECTORY"));
	}
}
