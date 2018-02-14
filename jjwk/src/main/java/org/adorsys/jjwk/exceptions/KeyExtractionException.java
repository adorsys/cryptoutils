package org.adorsys.jjwk.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class KeyExtractionException extends BaseException {
	private static final long serialVersionUID = -102550810645375099L;

	public KeyExtractionException(String message) {
		super(message);
	}

	public KeyExtractionException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
