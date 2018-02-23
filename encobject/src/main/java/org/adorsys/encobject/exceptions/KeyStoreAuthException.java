package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 08:43.
 */
public class KeyStoreAuthException extends BaseException {
    public KeyStoreAuthException(String message) {
        super(message);
    }
}
