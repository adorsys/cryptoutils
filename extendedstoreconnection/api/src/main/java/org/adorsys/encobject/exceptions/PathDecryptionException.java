package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 26.09.18.
 */
public class PathDecryptionException extends BaseException {
    public PathDecryptionException(String path, Exception origException) {
        super("Can not decrypt " + path, origException);
    }
}
