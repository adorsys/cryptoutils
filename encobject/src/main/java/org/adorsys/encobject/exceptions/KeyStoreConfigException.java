package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

public class KeyStoreConfigException extends KeyStoreExistsException {
    public KeyStoreConfigException(String message) {
        super(message);
    }
}
