package org.adorsys.encobject.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 16.01.18.
 */
public class BucketException extends BaseException {
    public BucketException(String message) {
        super(message);
    }
}
