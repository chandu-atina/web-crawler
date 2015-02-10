package com.crawl.web.util;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.crawl.web.entry.Crawler;

import junit.framework.Assert;
import junit.framework.TestCase;
/**
 * Test Class to test whether application properties
 * are loaded successfully or not
 *
 */
public class ApplicationPropertiesTest{
	
	private static final String CONFIG_PATH = "classpath:Application-config-crawler.xml";

	/**
	 * 
	 * @throws Exception
	 * Check if all properties are loaded by spring IOC.
	 * Asserts if any of the property is null
	 */
	@Test
	public void testGetURLList() throws Exception {
		final ApplicationContext context = new ClassPathXmlApplicationContext(
				CONFIG_PATH);
		final ApplicationProperties appProp = context.getBean(ApplicationProperties.class);
		boolean flag=appProp.toString().contains("null");
		Assert.assertFalse("Application Properties are not loaded successfully",flag);
	}
}
