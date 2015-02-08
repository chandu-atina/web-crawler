package com.crawl.web.service;

import java.util.Hashtable;

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

	public void processRequest(String URL) throws WebCrawlerServiceException;
	
	public void test();

}
