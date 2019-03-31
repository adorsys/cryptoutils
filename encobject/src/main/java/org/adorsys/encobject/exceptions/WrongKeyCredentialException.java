package org.adorsys.encobject.exceptions;

import de.adorsys.common.exceptions.BaseException;

public class WrongKeyCredentialException extends BaseException {
	private static final long serialVersionUID = 9152279184615563007L;
	public WrongKeyCredentialException(Throwable cause) {
		super(cause);
	}
	public WrongKeyCredentialException(String message) {
		super(message);
	}
}
