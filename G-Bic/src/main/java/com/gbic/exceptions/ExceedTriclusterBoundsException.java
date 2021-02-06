package com.gbic.exceptions;

public class ExceedTriclusterBoundsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExceedTriclusterBoundsException(String errorMessage) {
        super(errorMessage);
    }
}
