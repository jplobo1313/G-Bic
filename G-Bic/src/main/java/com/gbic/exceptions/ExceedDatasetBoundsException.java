package com.gbic.exceptions;

public class ExceedDatasetBoundsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExceedDatasetBoundsException(String errorMessage) {
        super(errorMessage);
    }
}
