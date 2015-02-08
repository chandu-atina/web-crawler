package com.crawl.web.util;

import org.springframework.stereotype.Component;

/**
 * This class we load all the properties required for the crawler.
 * The properites are loaded  into application using spring DI. 
 */
@Component
public class ApplicationProperties {
	/**
	 * This is used in serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Web site URL to be crawled
	 */
	private String webURL ;
	
	/**
	 * Location where the downloaded mails to be saved
	 */
	private String mailLocation ;
	
	/**
	 * Location where the application store the save point 
	 */
	private String savePointLocation ;

	/**
	 * @return the webURL
	 */
	public String getWebURL() {
		return webURL;
	}

	/**
	 * @param webURL the webURL to set
	 */
	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}

	/**
	 * @return the mailLocation
	 */
	public String getMailLocation() {
		return mailLocation;
	}

	/**
	 * @param mailLocation the mailLocation to set
	 */
	public void setMailLocation(String mailLocation) {
		this.mailLocation = mailLocation;
	}

	/**
	 * @return the savePointLocation
	 */
	public String getSavePointLocation() {
		return savePointLocation;
	}

	/**
	 * @param savePointLocation the savePointLocation to set
	 */
	public void setSavePointLocation(String savePointLocation) {
		this.savePointLocation = savePointLocation;
	}

}
