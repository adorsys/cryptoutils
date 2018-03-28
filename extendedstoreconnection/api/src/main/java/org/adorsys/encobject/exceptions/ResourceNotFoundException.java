package org.adorsys.encobject.exceptions;

/**
 * Created by peter on 28.03.18 at 10:50.
 */
public class ResourceNotFoundException extends StorageConnectionException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
