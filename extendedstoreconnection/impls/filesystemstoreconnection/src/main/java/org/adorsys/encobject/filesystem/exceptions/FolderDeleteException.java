package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 14:59.
 */
public class FolderDeleteException extends StorageConnectionException {
    public FolderDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
