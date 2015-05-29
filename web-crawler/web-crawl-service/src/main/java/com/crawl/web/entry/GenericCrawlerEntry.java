/*package com.crawl.web.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.crawl.web.exception.WebCrawlerServiceException;
import com.crawl.web.service.CrawlerService;
*//**
 * Crawler is a entry point for crawling a web URL.
 * This is a main class which is automatically picked from
 * jar when executed from command prompt
 *
 *//*
@Component
public class GenericCrawlerEntry {

	final static Logger log = Logger.getLogger(Crawler.class);

	private static final String CONFIG_PATH = "classpath:Application-config-crawler.xml";

	@Autowired
	@Qualifier("GenericMultiThreadedWebCrawlerServiceImpl")
	CrawlerService webCrawler;
	
	*//**
	 * main method is the starting point of web URL crawling
	 *//*
	public static void main(String args[]) {
		try {
			final ApplicationContext context = new ClassPathXmlApplicationContext(
					CONFIG_PATH);
			final Crawler crawler = context.getBean(Crawler.class);
			log.info("Staring Crawler !!!");
			long startTime =Calendar.getInstance().getTimeInMillis();
			log.info("No. of arguments supplied :"+args.length);
			boolean processedFlag=crawler.crawlWebPage(args);
			if(processedFlag){
				log.info("Crawling completed successfully");
				long endTime =Calendar.getInstance().getTimeInMillis();
				log.info("Total Time Taken :"+(endTime-startTime)+" milli seconds");
			}
			// crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
		} catch (WebCrawlerServiceException e) {
			log.error(e.toString());
			log.info("Crawler Terminated !!!");
		}
	}
	
	*//**
	 * non-static method which calls the crawling mechanism
	 *//*
	public boolean crawlWebPage() {
		webCrawler.processRequest();
		//webCrawler.test();
		return true;
	}
	
	*//**
	 * overloaded non-static method which calls the crawling mechanism
	 * based on input arguments
	 *//*
	public boolean crawlWebPage(String args[]) {
		
		if(args.length<3){
			log.info("Invalid no.of arguments supplied. Minimum 3 arguments are expected.");
			return true;
		}else{
			for(int i=0;i<Integer.parseInt(args[1]);i++){
				List<String> s= new ArrayList<String>();
				s.add(args[i+2]);
				webCrawler.processRequest(args[0],Integer.parseInt(args[1]),s);
			}
			webCrawler.processRequest();
		}
		return true;
	}
	*//**
	 * @return the webCrawler
	 *//*
	public CrawlerService getWebCrawler() {
		return webCrawler;
	}
	*//**
	 * @param webCrawler the webCrawler to set
	 *//*
	public void setWebCrawler(CrawlerService webCrawler) {
		this.webCrawler = webCrawler;
	}
	
}
*/