package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 15:17.
 */
public class FileNotFoundException extends StorageConnectionException {
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
