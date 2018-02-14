package org.adorsys.jjwk.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class UnsupportedEncAlgorithmException extends BaseException {
	private static final long serialVersionUID = -102550810645375099L;

	public UnsupportedEncAlgorithmException(String message) {
		super(message);
	}

	public UnsupportedEncAlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
