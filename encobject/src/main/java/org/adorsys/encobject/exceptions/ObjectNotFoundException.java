package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Object with corresponding handle not found in the object storage.
 * 
 * @author fpo
 *
 */
public class ObjectNotFoundException extends BaseException {

	private static final long serialVersionUID = -5566918389843503687L;

	/*
	Sorry für die Beseitigung der beiden Parameter enableSuppression und writableStackTrace
	 */

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}
}
