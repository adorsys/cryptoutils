package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 16:22.
 */
public class DeleteFileException extends StorageConnectionException {
    public DeleteFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
