package org.adorsys.encobject.exceptions;

public class KeystoreNotFoundException extends ObjectNotFoundException {

	private static final long serialVersionUID = 8261780879874630165L;

	public KeystoreNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	public KeystoreNotFoundException(String message) {
		super(message);
	}
}
