package com.trackingpath.exceptions;

public class NoDataFoundException extends RuntimeException  { 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoDataFoundException(String errorMessage) {
        super(errorMessage);
    }
}
