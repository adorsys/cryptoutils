package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class MissingKeystoreAlgorithmException extends BaseException {
	private static final long serialVersionUID = -8244399588062333573L;
	public MissingKeystoreAlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}
}
