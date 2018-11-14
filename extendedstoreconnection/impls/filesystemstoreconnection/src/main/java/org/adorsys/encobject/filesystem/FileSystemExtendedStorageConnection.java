package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.connection.FilesystemRootBucketName;
import org.adorsys.encobject.types.properties.BucketPathEncryptionFilenameOnly;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;

/**
 * Created by peter on 27.09.18.
 */
public class FileSystemExtendedStorageConnection extends BucketPathEncryptingExtendedStoreConnection {
    public FileSystemExtendedStorageConnection(FilesystemConnectionProperties properties) {
        this(
                properties.getFilesystemRootBucketName(),
                properties.getBucketPathEncryptionPassword(),
                properties.getBucketPathEncryptionFilenameOnly());
    }

    public FileSystemExtendedStorageConnection(
            FilesystemRootBucketName basedir,
            BucketPathEncryptionPassword bucketPathEncryptionPassword,
            BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        super(new RealFileSystemExtendedStorageConnection(basedir), bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly);
    }
}