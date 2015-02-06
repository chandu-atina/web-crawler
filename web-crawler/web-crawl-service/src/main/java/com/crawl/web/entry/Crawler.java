package com.crawl.web.entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.exception.WebCrawlerServiceException;

@Component
public class Crawler {

	final static Logger log = Logger.getLogger(Crawler.class);

	private static final String CONFIG_PATH = "classpath*:com/sw_engineering_candies/application-config.xml";

	@Autowired
	CrawlerService webCrawler;

	public static void main(String args[]) {
		try {
			final ApplicationContext context = new ClassPathXmlApplicationContext(
					"application-config-crawler.xml");
			final Crawler crawler = context.getBean(Crawler.class);
			boolean processedFlag=crawler.crawlWebPage("http://mail-archives.apache.org/mod_mbox/maven-users/");
			if(processedFlag){
				log.info("Crawling completed successfully");
			}
			// crawler.processRequest("http://mail-archives.apache.org/mod_mbox/maven-users/");
		} catch (WebCrawlerServiceException e) {
			log.error(e.toString());
		}finally{
			log.info("write to file");
		}
	}

	public boolean crawlWebPage(String url) {
		webCrawler.processRequest(url);
		//webCrawler.test();
		return true;
	}

	public CrawlerService getWebCrawler() {
		return webCrawler;
	}

	public void setWebCrawler(CrawlerService webCrawler) {
		this.webCrawler = webCrawler;
	}
	
	
}
