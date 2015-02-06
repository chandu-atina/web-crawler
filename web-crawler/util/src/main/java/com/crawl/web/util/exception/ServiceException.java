package com.crawl.web.util.exception;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  06/02/2015		06/02/2015		chandu-atina 	initial creation
 */

import com.crawl.web.util.messages.ErrorMessage;
import com.crawl.web.util.messages.ErrorMessages;

public class ServiceException extends RuntimeException{
	/**
	 * This is used in serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * holds the error messages
	 */
	private ErrorMessages errors = new ErrorMessages();
	
	/**
	 * default constructor and calls super class default constructor
	 */
	public ServiceException() {
		super();
	}
	
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServiceException(String message) {
		super(message);
	}
	
	public ServiceException(Throwable cause) {
		super(cause);
	}
	
	public ErrorMessages getErrors() {
		if(errors == null){
			this.errors = new ErrorMessages();
		}
		return errors;
	}
	
	public String toString() {
		return super.toString()+"\n"+this.getErrors().toString();
	}
	
	public ServiceException(ErrorMessages errors) {
		if(errors != null) {
			this.errors = errors;
		}
	}
	
	public ServiceException(ErrorMessage error) {
		this.getErrors().addError(error);
	}
}
