package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

/**
 * Created by peter on 27.09.18.
 */
public class FileSystemExtendedStorageConnection extends BucketPathEncryptingExtendedStoreConnection {
    public FileSystemExtendedStorageConnection(BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this(new FileSystemParamParser("").getFilesystembase(), bucketPathEncryptionPassword);
    }


    public FileSystemExtendedStorageConnection(String basedir, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealFileSystemExtendedStorageConnection(basedir), bucketPathEncryptionPassword);
    }
}