package com.crawl.web.service.impl;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  13/02/2005		16/02/2015		chandu-atina	Added executor service
 *  13/02/2015		13/02/2015		chandu-atina 	initial basic thread version
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.Node;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.crawl.web.service.CrawlerService;
import com.crawl.web.util.ApplicationProperties;
import com.crawl.web.util.GenericApplicationCache;
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
@Service("MultiThreadedWebCrawlerServiceImpl")
public class MultiThreadedWebCrawlerServiceImpl implements CrawlerService {

	@Autowired
	URLFormatter formatURL;

	@Autowired
	ApplicationProperties appProp;

	@Autowired
	GenericApplicationCache appCache;

	/**
	 * logger class
	 */
	final static Logger log = Logger.getLogger(WebCrawlerServiceImpl.class);

	/**
	 * HashTable used to load the save point parameters
	 */
	Hashtable<String, String> savePoint;

	/**
	 * Indicates whether savePoint is resumed or not.
	 */
	public boolean savePointResumed = false;

	Pattern subjectPattern = Pattern.compile(".*: (.+)$");

	Pattern orgPattern = Pattern.compile("@(.*?)>");

	/**
	 * Process the URL and save all mails linked to the URL. It overrides the
	 * default year and mailLocation with the arguments passed to the method
	 */
	public void processRequest(String year, String mailLocation)
			throws WebCrawlerServiceException {
		appProp.setYear(year);
		appProp.setMailLocation(mailLocation);
		processRequest();
	}

	/**
	 * Process the URL and save all mails linked to the URL. This method crawls
	 * the default year and save the mails to default location. Default Year :
	 * 2014 Default Location : /var/tmp/mails
	 */
	public void processRequest() throws WebCrawlerServiceException {
		final String METHOD_NAME = "processRequest - ";
		try {

			/* comment out to turn off annoying htmlunit warnings */
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF);
			String url = appProp.getWebURL();
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
			log.info("Method:" + METHOD_NAME + " Loading page now: " + url);

			/* Creating and executor instance */
			ExecutorService es = Executors.newCachedThreadPool();

			HtmlPage page = webClient.getPage(url);

			/* will wait JavaScript to execute up to 30s */
			webClient.waitForBackgroundJavaScript(30 * 1000);

			/* selects current node whose id like 'year' and has an anchor tag' */
			String primaryXPath = ".//*[@id[contains(string(),'"
					+ appProp.getYear() + "')]]/a";

			String primaryRegex = appProp.getYear() + ".*mbox/browser";

			/*
			 * Fetch the save point from file
			 */
			savePoint = retrieveSavePoint(false);

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

				/*
				 * Process each and every URL
				 */
				es.execute(new AppThread(mprimaryURL, directoryName, savePoint));
			}
			es.shutdown();
			boolean finshed = es.awaitTermination(1, TimeUnit.HOURS);
			log.info("Executor Flag :" + finshed);
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
			savePoint(false);
		}
	}

	public void processURLs(String mprimaryURL, String directoryName,
			String threadName, Hashtable<String, String> savePoint)
			throws IOException {

		final String METHOD_NAME = "processURLs -";
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
		WebClient ajaxWebClient = new WebClient(BrowserVersion.FIREFOX_24);
		log.info("Method:" + METHOD_NAME + " Loading page now: " + mprimaryURL);
		HtmlPage page = webClient.getPage(mprimaryURL);
		int i = 0;
		XmlPage xmlPage = ajaxWebClient.getPage(mprimaryURL.substring(0,
				mprimaryURL.length() - 7) + "ajax/thread?" + i);
		retriveTopics(xmlPage);
		/* will wait JavaScript to execute up to 30s */
		webClient.waitForBackgroundJavaScript(30 * 1000);

		String secondaryXPath = ".//*[@id[contains(string(),'msg-')]]/td[2]/a";
		String secondaryRegex = "ajax.*%3E";

		List<String> secondaryURLList = new ArrayList<String>();

		/* Check for pagination and fetch all the other URLs */
		while (true) {
			try {
				i++;

				/* Fetch all the email links in the second page */

				List<String> subSecondaryURLList = fetchURLs(page,
						secondaryXPath, mprimaryURL.substring(0,
								mprimaryURL.lastIndexOf("/") + 1),
						secondaryRegex);

				secondaryURLList.addAll(subSecondaryURLList);

				/*
				 * Fetch all the email links by nagivating through pagination in
				 * the second page
				 */
				final HtmlAnchor anchor = page.getAnchorByText("Next »");
				page = anchor.click();
				webClient
						.setAjaxController(new NicelyResynchronizingAjaxController());
				webClient.waitForBackgroundJavaScript(30 * 1000);
				xmlPage = ajaxWebClient.getPage(mprimaryURL.substring(0,
						mprimaryURL.length() - 7) + "ajax/thread?" + i);
				retriveTopics(xmlPage);

			} catch (ElementNotFoundException e) {

				/* Indicates end of pagination */
				log.info("Method:" + METHOD_NAME + " End of Pagination");
				break;
			}

		}

		/* Check for save point */
		int index = 0;
		if (savePoint.get(WebCrawlerConstants.SAVE_POINT).equals(
				WebCrawlerConstants.SAVE_POINT_YES)) {
			index = secondaryURLList.indexOf(savePoint.get(threadName
					+ WebCrawlerConstants.THREAD_KEY_SEPERATOR
					+ WebCrawlerConstants.KEY_URL));
			if (index < 0) {
				log.info("Method:" + METHOD_NAME
						+ "Processing URLs in the current directory :"
						+ directoryName);
				saveMails(secondaryURLList, directoryName, threadName);
			} else if (index >= 0) {
				log.info("Method:"
						+ METHOD_NAME
						+ "Processing remaining URLs in the current directory :"
						+ directoryName);
				saveMails(secondaryURLList.subList(index,
						secondaryURLList.size()), directoryName, threadName);
				savePointResumed = true;
			}
		} else {
			log.info("Method:" + METHOD_NAME
					+ "Processing URLs in the current directory :"
					+ directoryName);
			saveMails(secondaryURLList, directoryName, threadName);
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
		log.info("Method:" + METHOD_NAME + " No.of nodes found based on XPath "
				+ xPath + " :" + mailArchivesList.size());

		urlList = formatURL.getURLList(baseURL, mailArchivesList, regex);
		for (String nurl : urlList) {
			log.debug("Method:" + METHOD_NAME + nurl);
		}
		return urlList;
	}

	/**
	 * Retrieve the content based on URLs and save the content to file system
	 * 
	 * @throws IOException
	 */
	public boolean saveMails(List<String> mailURLList, String directoryName,
			String threadName) throws IOException {
		boolean processedFlag = false;
		final String METHOD_NAME = "saveMails - ";
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
		String mailId = "";

		for (String url : mailURLList) {
			String fileName = "";
			XmlPage page = webClient.getPage(url);

			fileName = url.substring(url.indexOf("ajax/") + 5, url.length());
			String path = appProp.getMailLocation() + directoryName + "/"
					+ fileName;
			File file = new File(path);
			file.getParentFile().mkdirs();
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			DomElement e = page.getDocumentElement();
			mailId = java.net.URLDecoder.decode(e.getAttribute("id"), "UTF-8");
			String subject = StringEscapeUtils.unescapeHtml4(e
					.getElementsByTagName("subject").item(0).getTextContent());
			String organisation = "";

			Matcher orgMatcher = orgPattern.matcher(mailId);
			Matcher topicMatcher;
			if (subject.contains(": ")) {
				topicMatcher = subjectPattern.matcher(subject);
				if (topicMatcher.find()) {
					subject = topicMatcher.group(1);
				}
			}

			if (orgMatcher.find()) {
				organisation = orgMatcher.group(1);
			}
			if (appCache.getOrganisationList().containsKey(organisation)) {
				if (appCache.getOrganisationList().get(organisation)
						.contains(subject)) {

				} else {
					appCache.getOrganisationList().get(organisation)
							.add(subject);
				}
			} else {
				ArrayList<String> topicList = new ArrayList<String>();
				topicList.add(subject);
				appCache.getOrganisationList().put(organisation, topicList);
			}

			bw.write(StringEscapeUtils.unescapeHtml4(page.asText()));
			bw.close();
			log.info("Method:" + METHOD_NAME + "Thread Name :" + threadName
					+ " Saved content from URL :" + url);
			appCache.setAppCacheValue(threadName
					+ WebCrawlerConstants.THREAD_KEY_SEPERATOR
					+ WebCrawlerConstants.KEY_DIRECTORY
					+ WebCrawlerConstants.KEY_VALUE_SEPERATOR, directoryName);
			appCache.setAppCacheValue(threadName
					+ WebCrawlerConstants.THREAD_KEY_SEPERATOR
					+ WebCrawlerConstants.KEY_URL
					+ WebCrawlerConstants.KEY_VALUE_SEPERATOR, url);
			log.debug("Method:" + METHOD_NAME
					+ "Save point stored in Application cache !!");
		}
		log.info("Method:" + METHOD_NAME
				+ "Saved all mails in the curret Page. No.of mails saved :"
				+ mailURLList.size());
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
		// WebCrawlerServiceImpl crawl = new WebCrawlerServiceImpl();
		// crawl.processRequest();
		System.out
				.println(StringEscapeUtils
						.unescapeHtml4("%3c00b201cf0b07$a919a320$fb4ce960$@eclipse.co.uk%3e"));
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
	 * Stores a save point in a flat file to resume for the next execution
	 */
	public void savePoint(boolean testFlag) {
		Hashtable<String, String> cache = appCache.getAppCacheData();
		HashMap<String, ArrayList<String>> organisationList = appCache
				.getOrganisationList();
		// Set<String> topicSet=appCache.getTopicSet();
		final String METHOD_NAME = "savePoint - ";
		if ((cache.size() == 0)) {
			log.debug("Method:"
					+ METHOD_NAME
					+ "No data available for Save Point in Application Cache !!!");
			return;
		}
		String path = "";
		String orgFilePath = "";
		if (testFlag) {
			path = appProp.getSavePointLocation() + "test"
					+ appProp.getFileNameExtension();
			orgFilePath = appProp.getSavePointLocation() + "test"
					+ appProp.getOrgFileNameExtension();
		} else {
			path = appProp.getSavePointLocation() + appProp.getYear()
					+ appProp.getFileNameExtension();
			orgFilePath = appProp.getSavePointLocation() + appProp.getYear()
					+ appProp.getOrgFileNameExtension();
		}
		File file = new File(path);
		File orgListFile = new File(orgFilePath);
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
			bw.flush();

			FileWriter orgFileWriter = new FileWriter(
					orgListFile.getAbsoluteFile());
			bw = new BufferedWriter(orgFileWriter);
			keySet = organisationList.keySet();
			it = keySet.iterator();
			while (it.hasNext()) {
				String key = it.next();
				bw.write("Organisation : " + key + "\nNo.of Topics : "
						+ organisationList.get(key).size() + "\nTopics : "
						+ organisationList.get(key).toString() + "\n\n");
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
	 * 
	 * @throws IOException
	 */
	public Hashtable<String, String> retrieveSavePoint(boolean testFlag)
			throws IOException {
		Hashtable<String, String> savePoint = new Hashtable<String, String>();
		final String METHOD_NAME = "retrieveSavePoint - ";
		String path;
		if (testFlag) {
			path = appProp.getSavePointLocation() + "test"
					+ appProp.getFileNameExtension();
		} else {
			path = appProp.getSavePointLocation() + appProp.getYear()
					+ appProp.getFileNameExtension();
		}
		File file = new File(path);
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
		log.info("Method:" + METHOD_NAME + "Save point retrieved successfully");
		return savePoint;
	}

	/**
	 * @return the appCache
	 */
	public GenericApplicationCache getAppCache() {
		return appCache;
	}

	/**
	 * @param appCache
	 *            the appCache to set
	 */
	public void setAppCache(GenericApplicationCache appCache) {
		this.appCache = appCache;
	}

	public void retriveTopics(XmlPage xmlpage) {
		// log.info(xmlpage.asXml());
		Document doc = xmlpage.getXmlDocument();

		NodeList nList = doc.getElementsByTagName("message");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			org.w3c.dom.Node nNode = nList.item(temp);
			log.info("\nCurrent Element :" + nNode.getNodeName());
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				if (Integer.parseInt(eElement.getAttribute("depth")) == 0) {
					if (Integer.parseInt(eElement.getAttribute("linked")) == 1) {
						appCache.getTopicSet().add(
								eElement.getElementsByTagName("subject")
										.item(0).getTextContent());
					} else {

						Matcher matcher = subjectPattern
								.matcher(StringEscapeUtils
										.unescapeHtml4(eElement
												.getElementsByTagName("subject")
												.item(0).getTextContent()));
						if (matcher.find()) {
							String topic = matcher.group(1);
							appCache.getTopicSet().add(topic);
						}
					}
				}
				log.debug("Depth id : " + eElement.getAttribute("depth"));
				log.debug("Depth id : " + eElement.getAttribute("linked"));

				log.debug("From Name : "
						+ eElement.getElementsByTagName("from").item(0)
								.getTextContent());
				log.debug("Date Name : "
						+ eElement.getElementsByTagName("date").item(0)
								.getTextContent());
				log.debug("Subject Name : "
						+ eElement.getElementsByTagName("subject").item(0)
								.getTextContent());

			}
		}
	}

	/**
	 * Inner class for threa creation
	 */
	public class AppThread implements Runnable {

		private String mprimaryURL;
		private String directoryName;
		private Hashtable<String, String> savePoint;

		public AppThread(String mprimaryURL, String directoryName,
				Hashtable<String, String> savePoint) {
			super();
			this.mprimaryURL = mprimaryURL;
			this.directoryName = directoryName;
			this.savePoint = savePoint;
		}

		@Override
		public void run() {
			try {
				processURLs(mprimaryURL, directoryName, Thread.currentThread()
						.getName(), savePoint);
			} catch (IOException e) {
				throw new WebCrawlerServiceException(new ErrorMessage(
						e.getMessage(), e.getCause()));
			} catch (Exception e) {
				throw new WebCrawlerServiceException(new ErrorMessage(
						e.getMessage(), e.getCause()));
			}
		}

	}
	public void processRequest(String URL, Integer levels, List<String> keywords) throws WebCrawlerServiceException{
		return;
	}
}