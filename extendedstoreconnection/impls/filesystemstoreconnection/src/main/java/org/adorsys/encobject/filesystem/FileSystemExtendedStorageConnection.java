package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;

/**
 * Created by peter on 27.09.18.
 */
public class FileSystemExtendedStorageConnection extends BucketPathEncryptingExtendedStoreConnection {
    public FileSystemExtendedStorageConnection(FilesystemConnectionProperties properties) {
        this(
                properties.getFilesystemBasedirectoryName(),
                properties.getBucketPathEncryptionPassword());
    }

    public FileSystemExtendedStorageConnection(
            FilesystemBasedirectoryName basedir,
            BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealFileSystemExtendedStorageConnection(basedir), bucketPathEncryptionPassword);
    }
}