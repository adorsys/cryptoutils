package org.adorsys.encobject.exceptions;

/**
 * Created by peter on 17.01.18 at 18:24.
 */
public class FileExistsException extends PersistenceException {
    public FileExistsException(String message) {
        super(message);
    }
}
