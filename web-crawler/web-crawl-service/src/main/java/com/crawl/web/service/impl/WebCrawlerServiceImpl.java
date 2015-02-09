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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.ApplicationProperties;
import com.crawl.web.util.URLFormatter;
import com.crawl.web.constants.WebCrawlerConstants;
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

/**
 * WebCrawlerServiceImpl class has a few service methods that are helpful to
 * crawl a given URL and save all the mail contents to a flat file.
 */
@Service
public class WebCrawlerServiceImpl implements CrawlerService {

	@Autowired
	URLFormatter formatURL;

	@Autowired
	ApplicationProperties appProp;

	/**
	 * logger class
	 */
	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);
	
	/**
	 * HashTable used to load the save point parameters
	 */
	Hashtable<String, String> savePoint;

	/**
	 * Process the URL and save all mails linked to the URL.
	 */
	public void processRequest(String url1) throws WebCrawlerServiceException {
		final String METHOD_NAME = "processRequest - ";
		try {

			/* comment out to turn off annoying htmlunit warnings */
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF);
			String url = appProp.getWebURL();
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
			log.info("Method:" + METHOD_NAME + " Loading page now: " + url);
			HtmlPage page = webClient.getPage(url);

			/* will wait JavaScript to execute up to 30s */
			webClient.waitForBackgroundJavaScript(30 * 1000);

			/* selects current node whose id like 'year' and has an anchor tag' */
			String primaryXPath = ".//*[@id[contains(string(),'"+appProp.getYear()+"')]]/a";
			
			String primaryRegex = appProp.getYear()+".*mbox/browser";

			/*
			 * Fetch the save point from file
			 */
			savePoint = retrieveSavePoint();
			boolean savePointResumed = false;

			/*
			 * Fetch the primary list of urls in the first page Later traverse
			 * through each url to fetch all the mails
			 */
			List<String> primaryURLList = fetchURLs(page, primaryXPath,
					url.substring(0, url.lastIndexOf("/") + 1), primaryRegex);
			for (String mprimaryURL : primaryURLList) {

				/*
				 * All mails goes to their respective directories based on
				 * directory name
				 */
				String directoryName = mprimaryURL.substring(
						mprimaryURL.indexOf(appProp.getYear()),
						mprimaryURL.indexOf(appProp.getYear()) + 6);

				/* Check for save point */
				if (savePoint.get(WebCrawlerConstants.SAVE_POINT).equals(
						WebCrawlerConstants.SAVE_POINT_YES)) {
					if (directoryName.compareTo(savePoint
							.get(WebCrawlerConstants.KEY_DIRECTORY)) > 0) {
						log.debug("Method:" + METHOD_NAME
								+ "Skipping current directory :"+directoryName);
						continue;
					}
				}
				log.info("Method:" + METHOD_NAME + " Loading page now: "
						+ mprimaryURL);

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

				/* Check for save point */
				int index = 0;
				if (savePoint.get(WebCrawlerConstants.SAVE_POINT).equals(
						WebCrawlerConstants.SAVE_POINT_YES)
						&& !savePointResumed) {
					index = secondaryURLList.indexOf(savePoint
							.get(WebCrawlerConstants.KEY_URL));
					if (index < 0) {
						log.info("Method:" + METHOD_NAME
								+ "Current List of URLs are already processed");
					} else if (index >= 0) {
						log.info("Method:" + METHOD_NAME
								+ "Processing remaining URLs in the current directory :"+directoryName);
						saveMails(secondaryURLList.subList(index,
								secondaryURLList.size()), directoryName);
						savePointResumed = true;
					}
				} else {
					log.info("Method:" + METHOD_NAME
							+ "Processing URLs in the current directory :"+directoryName);
					saveMails(secondaryURLList, directoryName);
				}

				/* Check for pagination and fetch all the other URLs */
				while (true) {
					try {
						final HtmlAnchor anchor = page
								.getAnchorByText("Next »");
						HtmlPage subPage = anchor.click();
						webClient
								.setAjaxController(new NicelyResynchronizingAjaxController());
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
						if (savePoint.get(WebCrawlerConstants.SAVE_POINT)
								.equals(WebCrawlerConstants.SAVE_POINT_YES)
								&& !savePointResumed) {
							index = subSecondaryURLList.indexOf(savePoint
									.get(WebCrawlerConstants.KEY_URL));

							if (index < 0) {
								log.info("Method:"
										+ METHOD_NAME
										+ "Current List of URLs are already processed");
							} else if (index >= 0) {
								log.info("Method:" + METHOD_NAME
										+ "Processing remaining URLs in the current directory :"+directoryName);
								saveMails(subSecondaryURLList.subList(index,
										subSecondaryURLList.size()),
										directoryName);
								savePointResumed = true;
							}
						} else {
							log.info("Method:" + METHOD_NAME
									+ "Processing URLs in the current directory :"+directoryName);
							saveMails(subSecondaryURLList, directoryName);
						}
						page = subPage;

					} catch (ElementNotFoundException e) {

						/* Indicates end of pagination */
						log.info("Method:" + METHOD_NAME
								+ " End of Pagination");
						break;
					}
				}
			}
		} catch (MalformedURLException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(
					e.getMessage(), e.getCause()));
		} catch (IOException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(
					e.getMessage(), e.getCause()));
		} catch (Exception e) {
			throw new WebCrawlerServiceException(new ErrorMessage(
					e.getMessage(), e.getCause()));
		} finally {
			savePoint();
		}
	}

	/**
	 * formats the raw URLs in appropriate format
	 */
	public List<String> fetchURLs(HtmlPage page, String xPath, String baseURL,
			String regex) {
		List<String> urlList;
		final String METHOD_NAME = "fetchURLs - ";

		// select the nodes based on xpath
		List<?> mailArchivesList = page.getByXPath(xPath);
		log.info("Method:" + METHOD_NAME
				+ " No.of nodes found based on XPath " + xPath + " :"
				+ mailArchivesList.size());

		urlList = formatURL.getURLList(baseURL, mailArchivesList, regex);
		for (String nurl : urlList) {
			log.debug("Method:" + METHOD_NAME + nurl);
		}
		return urlList;
	}

	/**
	 * Retrieve the content based on URLs and save the content to file system
	 * @throws IOException 
	 */
	public boolean saveMails(List<String> mailURLList, String directoryName)
			throws IOException {
		boolean processedFlag = false;
		final String METHOD_NAME = "saveMails - ";
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);

		for (String url : mailURLList) {
			String fileName = "";
			XmlPage page = webClient.getPage(url);
			/*
			 * //log.info(page.asXml()); List<?> list=page.getByXPath("/mail");
			 * if(list.get(0) instanceof DomElement){ log.info(CLASS_NAME +
			 * METHOD_NAME+i+((DomElement) list.get(0)).getAttribute("id"));
			 * fileName=((DomElement) list.get(0)).getAttribute("id"); i++; }
			 */

			fileName = url.substring(url.indexOf("ajax/") + 5, url.length());
			String path = appProp.getMailLocation() + directoryName + "/"
					+ fileName;
			File file = new File(path);
			file.getParentFile().mkdirs();
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(page.asXml());
			bw.close();
			log.debug("Method:" + METHOD_NAME + "Saved content from URL :"
					+ url);
			ApplicationCache.getInstance().setAppCacheValue(
					WebCrawlerConstants.KEY_DIRECTORY
							+ WebCrawlerConstants.KEY_VALUE_SEPERATOR,
					directoryName);
			ApplicationCache.getInstance().setAppCacheValue(
					WebCrawlerConstants.KEY_URL
							+ WebCrawlerConstants.KEY_VALUE_SEPERATOR, url);
			log.debug("Method:" + METHOD_NAME
					+ "Save point stored in Application cache !!");
		}
		log.info("Method:" + METHOD_NAME
				+ "Saved all mails in the curret Page. No.of mails saved :"+mailURLList.size());
		processedFlag = true;
		return processedFlag;
	}

	/**
	 * Test Service
	 */
	public void test() {
		throw new WebCrawlerServiceException("hu");
	}

	public static void main(String args[]) {
		WebCrawlerServiceImpl crawl = new WebCrawlerServiceImpl();
		crawl.processRequest("sample");
	}

	/**
	 * @return the formatURL
	 */
	public URLFormatter getFormatURL() {
		return formatURL;
	}

	/**
	 * @param formatURL
	 *            the formatURL to set
	 */
	public void setFormatURL(URLFormatter formatURL) {
		this.formatURL = formatURL;
	}

	/**
	 * static inner class for maintaining cache
	 */
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

	/**
	 * Stores a save point in a flat file to resume for the next execution
	 */
	public void savePoint() {
		Hashtable<String, String> cache = ApplicationCache.getInstance()
				.getAppCache();
		final String METHOD_NAME = "savePoint - ";
		if((cache.size()==0)){
			log.debug("Method:" + METHOD_NAME
					+ "No data available for Save Point in Application Cache !!!");
			return;
		}
		String path = appProp.getSavePointLocation() + appProp.getYear()
				+ appProp.getFileNameExtension();
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
				bw.write(key + cache.get(key)
						+ WebCrawlerConstants.FIELD_SEPERATOR);
			}
			bw.close();
			log.info("Method:" + METHOD_NAME
					+ "Save Point saved successfully to File System!!!");
		} catch (IOException e) {
			throw new WebCrawlerServiceException(new ErrorMessage(
					e.getMessage(), e.getCause()));
		} catch (Exception e) {
			throw new WebCrawlerServiceException(new ErrorMessage(
					e.getMessage(), e.getCause()));
		}
	}

	/**
	 * @return the appProp
	 */
	public ApplicationProperties getAppProp() {
		return appProp;
	}

	/**
	 * @param appProp
	 *            the appProp to set
	 */
	public void setAppProp(ApplicationProperties appProp) {
		this.appProp = appProp;
	}

	/**
	 * Retrieves the save point from file saved in previous execution
	 * @throws IOException 
	 */
	public Hashtable<String, String> retrieveSavePoint() throws IOException {
		Hashtable<String, String> savePoint = new Hashtable<String, String>();
		final String METHOD_NAME="retrieveSavePoint - ";
		File file = new File(appProp.getSavePointLocation() + appProp.getYear()
				+ appProp.getFileNameExtension());
		if (!file.exists()) {
			savePoint.put(WebCrawlerConstants.SAVE_POINT,
					WebCrawlerConstants.SAVE_POINT_NO);
			log.debug("Method:" + METHOD_NAME
					+ "No save point available to resume");
			return savePoint;
		}

		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String s;
		if ((s = br.readLine()) == null) {
			savePoint.put(WebCrawlerConstants.SAVE_POINT,
					WebCrawlerConstants.SAVE_POINT_NO);
			br.close();
			log.debug("Method:" + METHOD_NAME
					+ "No data available in save point file !!!");
			return savePoint;
		}
		StringTokenizer st = new StringTokenizer(s,
				WebCrawlerConstants.FIELD_SEPERATOR);
		while (st.hasMoreTokens()) {
			String keyValuePair = st.nextToken();
			int index = keyValuePair.indexOf(
					WebCrawlerConstants.KEY_VALUE_SEPERATOR, 0);
			String key = keyValuePair.substring(0, index);
			String value = keyValuePair.substring(index + 1);
			savePoint.put(key, value);
		}
		br.close();

		savePoint.put(WebCrawlerConstants.SAVE_POINT,
				WebCrawlerConstants.SAVE_POINT_YES);
		log.info("Method:" + METHOD_NAME
				+ "Save point retrieved successfully");
		return savePoint;
	}
}