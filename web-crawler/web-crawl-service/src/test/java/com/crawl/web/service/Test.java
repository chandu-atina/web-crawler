package com.crawl.web.service;

import com.crawl.web.service.impl.WebCrawlerServiceImpl;

public class Test {

	public static void main(String args[]) {
		CrawlerService crawler = new WebCrawlerServiceImpl();
		crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
	}
}
