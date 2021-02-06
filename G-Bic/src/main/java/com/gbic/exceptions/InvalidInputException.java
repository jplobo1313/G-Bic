package com.gbic.exceptions;

public class InvalidInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidInputException(String errorMessage) {
        super(errorMessage);
    }
}
