package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 14:49.
 */
public class CreateFolderException extends StorageConnectionException {
    public CreateFolderException(String message) {
        super(message);
    }
}
