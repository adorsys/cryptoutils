package org.adorsys.encobject.filesystem.exceptions;

import org.adorsys.encobject.exceptions.StorageConnectionException;

/**
 * Created by peter on 06.02.18 at 14:53.
 */
public class FolderIsAFileException extends StorageConnectionException {
    public FolderIsAFileException(String message) {
        super(message);
    }
}
