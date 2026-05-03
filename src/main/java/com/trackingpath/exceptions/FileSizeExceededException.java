package com.trackingpath.exceptions;

@SuppressWarnings("serial")
public class FileSizeExceededException extends RuntimeException {

    private final String fieldName;

    public FileSizeExceededException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
