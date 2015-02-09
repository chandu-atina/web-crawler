package com.crawl.web.util.messages;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  06/02/2015		06/02/2015		chandu-atina 	initial creation
 */

public class ErrorMessage {
	
	private String code;
	
	private String message;
	
	private Throwable rootCause;
	
	public ErrorMessage(String code, String message, Throwable rootCause) {
		this.code=code;
		this.message=message;
		this.rootCause=rootCause;
	}
	
	public ErrorMessage(String message, Throwable rootCause) {
		this.message=message;
		this.rootCause=rootCause;
	}
	
	/**
	 * getter method for errors list 
	 * @return
	 */
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * getter method for message 
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * getter method for eroot cause 
	 * @return
	 */

	
	public String toString(){
		return "Error Code :"+code+" || Errror Message :"+message+" || Root Cause :"+rootCause;
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	public void setRootCause(Throwable rootCause) {
		this.rootCause = rootCause;
	}
}
