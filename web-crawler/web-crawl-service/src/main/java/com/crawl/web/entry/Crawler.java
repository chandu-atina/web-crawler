package com.crawl.web.entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.crawl.web.exception.WebCrawlerServiceException;
import com.crawl.web.service.CrawlerService;
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
			log.info(args.length);
			boolean processedFlag=crawler.crawlWebPage(args);
			if(processedFlag){
				log.info("Crawling completed successfully");
			}
			// crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
		} catch (WebCrawlerServiceException e) {
			log.error(e.toString());
			log.info("Crawler Terminated !!!");
		}
	}
	
	/**
	 * non-static method which calls the crawling mechanism
	 */
	public boolean crawlWebPage() {
		webCrawler.processRequest();
		//webCrawler.test();
		return true;
	}
	
	/**
	 * overloaded non-static method which calls the crawling mechanism
	 * based on input arguments
	 */
	public boolean crawlWebPage(String args[]) {
		
		if(args.length==2){
			webCrawler.processRequest(args[0],args[1]);
		}else{
			webCrawler.processRequest();
		}
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
