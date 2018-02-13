package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class MissingKeystoreProviderException extends BaseException {
	private static final long serialVersionUID = 4941429144446435839L;
	public MissingKeystoreProviderException(String message, Throwable cause) {
		super(message, cause);
	}
}
