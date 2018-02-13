package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class MissingKeyAlgorithmException extends BaseException {
	private static final long serialVersionUID = 5346384032530818425L;
	public MissingKeyAlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}
}
