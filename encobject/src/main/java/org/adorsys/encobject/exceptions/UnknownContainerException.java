package org.adorsys.encobject.exceptions;

import de.adorsys.common.exceptions.BaseException;

/**
 * Requested container not found
 * 
 * @author fpo
 *
 */
public class UnknownContainerException extends BaseException {
	private static final long serialVersionUID = 2434653140109032234L;

	public UnknownContainerException(String container) {
		super(container);
	}
}
