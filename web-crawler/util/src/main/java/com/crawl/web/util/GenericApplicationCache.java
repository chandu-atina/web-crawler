package com.crawl.web.util;

import java.util.Hashtable;

import org.springframework.stereotype.Component;
/**
 * ApplicationCache is used to store the cache values.
 * The cache gets updated for each processed mail
 * @author chandrasekhara
 *
 */
@Component
public class GenericApplicationCache {

	private Hashtable<String, String> appCacheData = new Hashtable<String, String>();

	/**
	 * @return the appCacheData
	 */
	public Hashtable<String, String> getAppCacheData() {
		return appCacheData;
	}

	/**
	 * @param appCacheData the appCacheData to set
	 */
	public void setAppCacheData(Hashtable<String, String> appCacheData) {
		this.appCacheData = appCacheData;
	}

	public String getAppCacheValue(String key) {
		if (appCacheData.get(key) != null) {
			return appCacheData.get(key);
		} else {
			return null;
		}
	}
	
	public void setAppCacheValue(String key, String value) {
		appCacheData.put(key, value);
	}
}