package com.trackingpath.exceptions;

import org.springframework.http.HttpStatus;

public class GenericException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	  private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	  private String message;
	  
	  
	  public HttpStatus getHttpStatus() {
	    return this.httpStatus;
	  }
	  
	  public GenericException(HttpStatus httpStatus, String message) {
	    super(message);
	    this.message=message;
	    this.httpStatus = httpStatus;
	  }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
