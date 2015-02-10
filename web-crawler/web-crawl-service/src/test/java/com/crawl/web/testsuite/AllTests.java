package com.crawl.web.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.crawl.web.service.CrawlerServiceTest;
import com.crawl.web.service.impl.WebCrawlerServiceImplTest;
import com.crawl.web.util.ApplicationPropertiesTest;
/**
 * Test suite to run all the test cases in single go
 * @author chandrasekhara
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ WebCrawlerServiceImplTest.class, CrawlerServiceTest.class,
		ApplicationPropertiesTest.class })
public class AllTests {

}
