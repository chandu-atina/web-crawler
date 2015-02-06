package com.crawl.web.util.messages;

/*
 * ##############################$History Card$###################################
 * ### Latest changes description should be on the top of the history card list###
 * ###############################################################################
 *  Created Date	Updated Date	Author			Change Description
 *  ============	============	============	===================
 *  06/02/2015		06/02/2015		chandu-atina 	initial creation
 */

import java.util.ArrayList;
import java.util.List;

public class ErrorMessages {

		// raw collection holds the error messages
		private List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
		
		/**
		 * getter method for errors list 
		 * @return
		 */
		public List<ErrorMessage> getErrors() {
			return errors;
		}
		
		/**
		 * this method is used to set the error messages
		 * to List
		 * @param errors
		 */
		public void addErrors(List<ErrorMessage> errors) {
			this.errors.addAll(errors);
		}
		
		/**
		 * This method is used to add the error message 
		 * to error list
		 * 
		 * @param error
		 */
		public void addError(ErrorMessage error) {
			this.errors.add(error);
		}
		
		/**
		 * This method is used to clear the list of error messages
		 */
		public void clear() {
			this.errors.clear();
		}
		
		/**
		 * This method is used to check is there any error message or not
		 * in the list
		 * @return
		 */
		public boolean hasErrors() {
			return ! this.errors.isEmpty();
		}
		
		/**
		 * This method is used to get the string format of the object
		 */
		@Override
		public String toString() {
			return errors.toString();

		}
}
