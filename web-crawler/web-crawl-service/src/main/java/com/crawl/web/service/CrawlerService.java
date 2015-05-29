package com.crawl.web.service;


import java.util.List;

import com.crawl.web.util.exception.WebCrawlerServiceException;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  03/02/2015		03/02/2015		chandu-atina 	initial skeleton creation
 */
/**
 * hi sample java document 
 */
public interface CrawlerService {

	public void processRequest() throws WebCrawlerServiceException;
	
	public void processRequest(String year, String mailLocation) throws WebCrawlerServiceException;
	
	public void processRequest(String URL, Integer levels, List<String> keywords) throws WebCrawlerServiceException;
	
	public void processRequest(Boolean doTag) throws WebCrawlerServiceException;
	
	public void processRequest(String year, String mailLocation,Boolean doTag) throws WebCrawlerServiceException;
	
	public void processRequest(String URL, Integer levels, List<String> keywords,Boolean doTag) throws WebCrawlerServiceException;

	
	public void test();

}
