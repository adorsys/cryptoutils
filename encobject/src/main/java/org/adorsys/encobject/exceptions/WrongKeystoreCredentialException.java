package org.adorsys.encobject.exceptions;

import de.adorsys.common.exceptions.BaseException;

public class WrongKeystoreCredentialException extends BaseException {
	private static final long serialVersionUID = 5247639037951303061L;

	public WrongKeystoreCredentialException(Throwable cause) {
		super(cause);
	}
}
