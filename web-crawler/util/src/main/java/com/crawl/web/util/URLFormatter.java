package com.crawl.web.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLFormatter {

	public List<String> getURLList(String baseURL, List<?> realtiveXpathURL,String regex){
		List<String> urlList= new ArrayList<String>();
		//String regex="2014.*browser";
		Pattern p = Pattern.compile(regex);
		for(Object obj: realtiveXpathURL){
			Matcher m = p.matcher(obj.toString());
			if(m.find()){
				urlList.add(baseURL+m.group(0));
			}
		}
		return urlList;
	}
	public static void main(String args[]){
		URLFormatter urlFormat= new URLFormatter();
		List<String> s= new ArrayList<String>();
		s.add("apple201401.mbox/browser");
		urlFormat.getURLList("http://hhh.com", s,"2014.*browser");
		
	}
}
