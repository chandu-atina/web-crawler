package com.crawl.web.entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.exception.WebCrawlerServiceException;
/**
 * Crawler is a entry point for crawling a web URL.
 * This is a main class which is automatically picked from
 * jar when executed from command prompt
 *
 */
@Component
public class Crawler {

	final static Logger log = Logger.getLogger(Crawler.class);

	private static final String CONFIG_PATH = "classpath:Application-config-crawler.xml";

	@Autowired
	CrawlerService webCrawler;
	
	/**
	 * main method is the starting point of web URL crawling
	 */
	public static void main(String args[]) {
		try {
			final ApplicationContext context = new ClassPathXmlApplicationContext(
					CONFIG_PATH);
			final Crawler crawler = context.getBean(Crawler.class);
			log.info("Staring Crawler !!!");
			boolean processedFlag=crawler.crawlWebPage("http://mail-archives.apache.org/mod_mbox/maven-users/");
			if(processedFlag){
				log.info("Crawling completed successfully");
			}
			// crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
		} catch (WebCrawlerServiceException e) {
			log.error(e.toString());
		}
	}
	
	/**
	 * non-static class which actually calls the crawling mechanism
	 */
	public boolean crawlWebPage(String url) {
		webCrawler.processRequest(url);
		//webCrawler.test();
		return true;
	}
	/**
	 * @return the webCrawler
	 */
	public CrawlerService getWebCrawler() {
		return webCrawler;
	}
	/**
	 * @param webCrawler the webCrawler to set
	 */
	public void setWebCrawler(CrawlerService webCrawler) {
		this.webCrawler = webCrawler;
	}
	
}
