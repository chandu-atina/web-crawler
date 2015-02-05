package com.crawl.web.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.apache.log4j.Logger;
import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.URLFormatter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class WebCrawlerServiceImpl implements CrawlerService {

	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);

	public void processRequest(String url) {
		try {
			/* comment out to turn off annoying htmlunit warnings */
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF);
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
			log.info("Loading page now: " + url);
			HtmlPage page = webClient.getPage(url);
			/* will wait JavaScript to execute up to 30s */
			webClient.waitForBackgroundJavaScript(30 * 1000);
			/* selects current node whose id like '2014' and has an anchor tag' */
			String primaryXPath = ".//*[@id[contains(string(),'2014')]]/a";
			String primaryRegex = "2014.*mbox/browser";
			/*
			 * Fetch the primary list of urls in the first page Later traverse
			 * through each url to fetch all the mails
			 */
			List<String> primaryURLList = fetchURLs(page, primaryXPath,
					url.substring(0, url.lastIndexOf("/") + 1), primaryRegex);
			for (String mprimaryURL : primaryURLList) {
				log.info("Loading page now: " + mprimaryURL);
				/* All mails goes to their respective directories based on directory name */
				String directoryName= mprimaryURL.substring(mprimaryURL.indexOf("2014"),mprimaryURL.indexOf("2014")+6);
				page = webClient.getPage(mprimaryURL);
				/* will wait JavaScript to execute up to 30s */
				webClient.waitForBackgroundJavaScript(30 * 1000);
				String secondaryXPath = ".//*[@id[contains(string(),'msg-')]]/td[2]/a";
				String secondaryRegex = "ajax.*%3E";
				/* Fetch all the email links in the second page */
				List<String> secondaryURLList = fetchURLs(page, secondaryXPath,
						mprimaryURL.substring(0,
								mprimaryURL.lastIndexOf("/") + 1),
						secondaryRegex);
				saveMails(secondaryURLList,directoryName);
				/* Check for pagination and fetch all the other URLs */
				while (true) {
					try {
						final HtmlAnchor anchor = page
								.getAnchorByText("Next »");
						HtmlPage subPage = anchor.click();
						webClient.setAjaxController(new
						 NicelyResynchronizingAjaxController());
						webClient.waitForBackgroundJavaScript(30 * 1000);
						/*
						 * Fetch all the email links by nagivating through
						 * pagination in the second page
						 */
						List<String> subSecondaryURLList = fetchURLs(
								subPage,
								secondaryXPath,
								mprimaryURL.substring(0,
										mprimaryURL.lastIndexOf("/") + 1),
								secondaryRegex);
						// TODO fetch content
						saveMails(subSecondaryURLList,directoryName);
						page=subPage;

					} catch (ElementNotFoundException e) {
						/* Indicates end of pagination */
						log.info("not found anchor next page tag");
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception Caught" + e.getStackTrace());
		}
	}

	public List<String> fetchURLs(HtmlPage page, String xPath, String baseURL,
			String regex) {
		List<String> urlList;
		// select the nodes based on xpath
		List<?> mailArchivesList = page.getByXPath(xPath);
		log.info("No.of nodes found based on XPath " + xPath + " :"
				+ mailArchivesList.size());
		for (Object s : mailArchivesList) {
			// log.info(s.toString());
		}
		URLFormatter formatURL = new URLFormatter();
		urlList = formatURL.getURLList(baseURL, mailArchivesList, regex);
		for (String nurl : urlList) {
			log.debug(nurl);
		}
		return urlList;
	}
	
	public boolean saveMails(List<String> mailURLList,
			String directoryName) {
		boolean processedFlag = false;
		int i=1;
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
		try {
			for (String url : mailURLList) {
				XmlPage page = webClient.getPage(url);
				//log.info(page.asXml());
				List<?> list=page.getByXPath("/mail");
				String fileName="";
				if(list.get(0) instanceof DomElement){
						log.info(i+((DomElement) list.get(0)).getAttribute("id"));
						fileName=((DomElement) list.get(0)).getAttribute("id");
						i++;
				}
				String path = "/home/chandrasekhara/mails/" + directoryName + "/"+fileName;
				File file = new File(path);
				file.getParentFile().mkdirs();
				boolean f= file.createNewFile();

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(page.asXml());
				bw.close();
			}
			log.info("over");
		} catch (MalformedURLException e) {

		} catch (IOException e) {

		}
		return processedFlag;
	}

	public static void main(String args[]) {
		WebCrawlerServiceImpl crawl = new WebCrawlerServiceImpl();
		crawl.processRequest("sample");
	}
}
