package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 17.01.18 at 18:07.
 */
public class PersistenceException extends BaseException {
    public PersistenceException(String message) {
        super(message);
    }
}
