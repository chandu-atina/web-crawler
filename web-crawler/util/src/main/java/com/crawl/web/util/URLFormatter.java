package com.crawl.web.util;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  03/02/2005		04/02/2015		chandu-atina	Added getURLList service
 *  03/02/2015		03/02/2015		chandu-atina 	initial skeleton creation
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class URLFormatter {

	public List<String> getURLList(String baseURL, List<?> realtiveXpathURL,
			String regex) {
		List<String> urlList = new ArrayList<String>();
		// String regex="2014.*browser";
		Pattern p = Pattern.compile(regex);
		for (Object obj : realtiveXpathURL) {
			Matcher m = p.matcher(obj.toString());
			if (m.find()) {
				urlList.add(baseURL + m.group(0));
			}
		}
		return urlList;
	}
	
	public List<String> getURLList(String baseURL, List<?> realtiveXpathURL) {
		List<String> urlList = new ArrayList<String>();
		String regex=".*href=\"(.+?)\"";
		Pattern p = Pattern.compile(regex);
		for (Object obj : realtiveXpathURL) {
			Matcher m = p.matcher(obj.toString());
			if (m.find()) {
				urlList.add(baseURL + m.group(1));
			}
		}
		return urlList;
	}

	public static void main(String args[]) {
		URLFormatter urlFormat = new URLFormatter();
		List<String> s = new ArrayList<String>();
		s.add("apple201401.mbox/browser");
		urlFormat.getURLList("http://hhh.com", s, "2014.*browser");

	}
}
