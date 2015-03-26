package com.crawl.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

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
	
	private HashMap<String,ArrayList<String>> organisationList= new HashMap<String,ArrayList<String>>();
	
	private Set<String> topicSet= new HashSet<String>();

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
	

	public HashMap<String, ArrayList<String>> getOrganisationList() {
		return organisationList;
	}

	public void setOrganisationList(
			HashMap<String, ArrayList<String>> organisationList) {
		this.organisationList = organisationList;
	}

	public Set<String> getTopicSet() {
		return topicSet;
	}

	public void setTopicSet(Set<String> topicSet) {
		this.topicSet = topicSet;
	}
	
}