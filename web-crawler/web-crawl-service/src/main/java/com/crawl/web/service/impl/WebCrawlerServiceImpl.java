package com.crawl.web.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.URLFormatter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebCrawlerServiceImpl implements CrawlerService {
	
	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);

	public void processRequest(String url) {
		try {
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF); /*
															 * comment out to
															 * turn off annoying
															 * htmlunit warnings
															 */
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
			log.info("Loading page now: " + url);
			HtmlPage page = webClient.getPage(url);
			webClient.waitForBackgroundJavaScript(30 * 1000); /*
															 * will wait
															 * JavaScript to
															 * execute up to 30s
															 */
			/*String pageAsXml = page.asXml();
			  System.out.println(pageAsXml); */
			
			// selects current node whose id like '2014' and has an anchor tag'
			List<?> mailArchivesList = page
					.getByXPath(".//*[@id[contains(string(),'2014')]]/a");
			log.debug("Found " + mailArchivesList.size()
					+ " 'bucket' divs.");
			for (Object s : mailArchivesList) {
				log.debug(s.toString());
			}
			URLFormatter formatURL= new URLFormatter();
			List<String> urlList= formatURL.getURLList(url, mailArchivesList);
			for (String nurl : urlList) {
				log.debug(nurl);
			}
		} catch (Exception e) {
			System.out.println("Exception Caught" + e.getStackTrace());
		}
	}

	public static void main(String args[]) {
		WebCrawlerServiceImpl crawl = new WebCrawlerServiceImpl();
		crawl.processRequest("sample");
	}
}
