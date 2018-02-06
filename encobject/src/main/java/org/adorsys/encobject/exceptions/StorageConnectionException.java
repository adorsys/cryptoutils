package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 06.02.18 at 14:49.
 */
public class StorageConnectionException extends BaseException {
    public StorageConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageConnectionException(String message) {
        super(message);
    }
}
