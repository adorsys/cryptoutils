package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.FilesystemRootBucketName;

/**
 * Created by peter on 04.10.18.
 */
public interface FilesystemConnectionProperties  extends ConnectionProperties {
    FilesystemRootBucketName defaultBasedirectory = new FilesystemRootBucketName("target/filesystemstorage");

    FilesystemRootBucketName getFilesystemRootBucketName();
}
