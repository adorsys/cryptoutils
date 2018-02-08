package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 15:19.
 */
public class FileIsFolderException extends StorageConnectionException {
    public FileIsFolderException(String message) {
        super(message);
    }
}
