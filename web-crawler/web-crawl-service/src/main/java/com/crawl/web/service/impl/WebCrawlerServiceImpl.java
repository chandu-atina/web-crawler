package com.crawl.web.service.impl;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  03/02/2005		06/02/2015		chandu-atina	Added services
 *  03/02/2015		03/02/2015		chandu-atina 	initial skeleton creation
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Savepoint;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.URLFormatter;
import com.crawl.web.exception.WebCrawlerServiceException;
import com.crawl.web.util.messages.ErrorMessage;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

@Service
public class WebCrawlerServiceImpl implements CrawlerService {
	
	@Autowired URLFormatter formatURL;

	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);
	
	final static String CLASS_NAME="WebCrawlerServiceImpl "; 

	public void processRequest(String url) throws WebCrawlerServiceException{
		final String METHOD_NAME="processRequest";
		try {
			/* comment out to turn off annoying htmlunit warnings */
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF);
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
			log.info(CLASS_NAME + METHOD_NAME+" Loading page now: " + url);
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
				log.info(CLASS_NAME + METHOD_NAME+" Loading page now: " + mprimaryURL);
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
						log.info(CLASS_NAME + METHOD_NAME+" End of Pagination");
						break;
					}
				}
			}
		} catch (MalformedURLException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}catch (IOException e){
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}catch (Exception e){
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}finally{
			savePoint();
		}
	}

	public List<String> fetchURLs(HtmlPage page, String xPath, String baseURL,
			String regex) throws WebCrawlerServiceException{
		List<String> urlList;
		final String METHOD_NAME="fetchURLs ";
		// select the nodes based on xpath
		List<?> mailArchivesList = page.getByXPath(xPath);
		log.info(CLASS_NAME + METHOD_NAME+" No.of nodes found based on XPath " + xPath + " :"
				+ mailArchivesList.size());
		for (Object s : mailArchivesList) {
			// log.info(s.toString());
		}
		//URLFormatter formatURL = new URLFormatter();
		urlList = formatURL.getURLList(baseURL, mailArchivesList, regex);
		for (String nurl : urlList) {
			log.debug(CLASS_NAME + METHOD_NAME+nurl);
		}
		return urlList;
	}
	
	public boolean saveMails(List<String> mailURLList,
			String directoryName) throws WebCrawlerServiceException{
		boolean processedFlag = false;
		int i=1;
		final String METHOD_NAME="saveMails ";
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
		try {
			for (String url : mailURLList) {
				XmlPage page = webClient.getPage(url);
				//log.info(page.asXml());
				List<?> list=page.getByXPath("/mail");
				String fileName="";
				if(list.get(0) instanceof DomElement){
						log.info(CLASS_NAME + METHOD_NAME+i+((DomElement) list.get(0)).getAttribute("id"));
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
				ApplicationCache.getInstance().setAppCacheValue("directory:", directoryName);
				ApplicationCache.getInstance().setAppCacheValue("fileName:", fileName);
				ApplicationCache.getInstance().setAppCacheValue("fileIndex:", String.valueOf(i-1));
			}
			log.info(CLASS_NAME + METHOD_NAME+"Saved all mails in the curret Page");
			processedFlag=true;
		} catch (MalformedURLException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}catch (IOException e){
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}catch (Exception e){
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}
		return processedFlag;
	}
	public void test(){
		throw new WebCrawlerServiceException("hu");
	}
	public static void main(String args[]) {
		WebCrawlerServiceImpl crawl = new WebCrawlerServiceImpl();
		crawl.processRequest("sample");
	}

	public URLFormatter getFormatURL() {
		return formatURL;
	}

	public void setFormatURL(URLFormatter formatURL) {
		this.formatURL = formatURL;
	}

	static class ApplicationCache {
		private static ApplicationCache appCache = new ApplicationCache();
		private Hashtable<String, String> appCacheData = new Hashtable<String, String>();
		
		private ApplicationCache() {

		}

		static ApplicationCache getInstance() {
			return appCache;
		}

		String getAppCacheValue(String key) {
			if (appCacheData.get(key) != null) {
				return appCacheData.get(key);
			} else {
				return null;
			}
		}
		Hashtable<String, String> getAppCache() {
			if (appCacheData != null) {
				return appCacheData;
			} else {
				return null;
			}
		}
		void setAppCacheValue(String key, String value) {
			appCacheData.put(key, value);
		}
	}
	
	public void savePoint() {
		Hashtable<String, String> cache = ApplicationCache.getInstance()
				.getAppCache();
		String path = "/var/tmp/save_point/AppSavePoint";
		File file = new File(path);
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			Set<String> keySet = cache.keySet();
			Iterator<String> it = keySet.iterator();
			while (it.hasNext()) {
				String key = it.next();
				bw.write(key + cache.get(key) + "||");
			}
			bw.close();
		} catch (IOException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		} catch (Exception e) {
			throw new WebCrawlerServiceException(new ErrorMessage(e.getMessage(),e.getCause()));
		}
	}
}