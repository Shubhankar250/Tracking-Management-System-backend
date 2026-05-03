package com.trackingpath.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@SuppressWarnings("serial")
public class IncorrectArgumentException extends RuntimeException  {
	
	public IncorrectArgumentException(String message) {
        super(message);
    }

}
