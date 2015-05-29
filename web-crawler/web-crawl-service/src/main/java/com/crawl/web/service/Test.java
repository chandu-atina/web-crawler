package com.crawl.web.service;

import org.apache.log4j.Logger;

import com.crawl.web.service.impl.WebCrawlerServiceImpl;
import com.crawl.web.util.exception.WebCrawlerServiceException;

public class Test {
	
	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);
	public static void main(String args[]) {
		try{
			CrawlerService crawler = new WebCrawlerServiceImpl();
			//crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
			crawler.test();
		}catch(WebCrawlerServiceException e){
			log.error(e.toString());
		}
	}
}
