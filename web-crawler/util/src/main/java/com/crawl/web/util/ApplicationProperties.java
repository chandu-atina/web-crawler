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
	 * year indicates the year to crawl 
	 */
	private String year;
	
	/**
	 * indicates the save point file extension 
	 */
	private String fileNameExtension;
	
	/**
	 * indicates the list of organizations and no.of
	 * users from each organization participating in
	 * the forums 
	 */
	private String orgFileNameExtension;

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

	/**
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(String year) {
		this.year = year;
	}

	/**
	 * @return the fileNameExtension
	 */
	public String getFileNameExtension() {
		return fileNameExtension;
	}

	/**
	 * @param fileNameExtension the fileNameExtension to set
	 */
	public void setFileNameExtension(String fileNameExtension) {
		this.fileNameExtension = fileNameExtension;
	}
	
	/**
	 * @return the orgFileNameExtension
	 */
	public String getOrgFileNameExtension() {
		return orgFileNameExtension;
	}
	
	/**
	 * @param orgFileNameExtension the orgFileNameExtension to set
	 */
	public void setOrgFileNameExtension(String orgFileNameExtension) {
		this.orgFileNameExtension = orgFileNameExtension;
	}

	/**
	 * @return all properties appended as a string
	 */
	public String toString() {
		return "WEB_URL :" + this.webURL + " FILE_NAME_EXTENSION :"
				+ this.fileNameExtension + " MAIL_LOCATION :"
				+ this.mailLocation + " SAVE_POINT_LOCATION :"
				+ this.savePointLocation + "YEAR :"
				+ this.year + "ORGANIZATION_FILE_NAME_EXTENSION :"
				+ this.orgFileNameExtension;
	}
}
