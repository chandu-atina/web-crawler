/**
 * 
 */
package com.crawl.web.util;


import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import junit.framework.TestCase;

/**
 * Test Class to test URLFormatter
 *
 */
public class URLFormatterTest extends TestCase{
	

	URLFormatter urlFormat=new URLFormatter();

	/**
	 * Test method to verify the URL formatting
	 *
	 */
	@Test
	public void testGetURLList() throws Exception {
		// fail();
		List<String> expectedList=new ArrayList<String>();
		expectedList.add("http://www.google.com/201312.mbox/browser");
		expectedList.add("http://www.google.com/201311.mbox/browser");
		expectedList.add("http://www.google.com/201310.mbox/browser");
		
		List<String> urlList=new ArrayList<String>();
		urlList.add("[HtmlAnchor[<a href=\"201312.mbox/browser\" title=\"Dynamic browser\">]");
		urlList.add("[HtmlAnchor[<a href=\"201311.mbox/browser\" title=\"Dynamic browser\">]");
		urlList.add("[HtmlAnchor[<a href=\"201310.mbox/browser\" title=\"Dynamic browser\">]");
		
		String regex="2013.*mbox/browser";
		//List<String> actualList=urlFormat.getURLList("http://www.google.com/", urlList, regex);
		List<String> actualList=urlFormat.getURLList("http://www.google.com/", urlList, regex);
		
		assertEquals("Mismatch ",expectedList, actualList);
	}

	/**
	 * @return the urlFormat
	 */
	public URLFormatter getUrlFormat() {
		return urlFormat;
	}

	/**
	 * @param urlFormat the urlFormat to set
	 */
	public void setUrlFormat(URLFormatter urlFormat) {
		this.urlFormat = urlFormat;
	}
}
