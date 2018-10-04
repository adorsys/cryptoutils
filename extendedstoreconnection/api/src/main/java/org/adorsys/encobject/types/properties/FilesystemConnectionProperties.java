package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;

/**
 * Created by peter on 04.10.18.
 */
public interface FilesystemConnectionProperties  extends ConnectionProperties {
    FilesystemBasedirectoryName defaultBasedirectory = new FilesystemBasedirectoryName("target/filesystemstorage");

    FilesystemBasedirectoryName getFilesystemBasedirectoryName();
}
