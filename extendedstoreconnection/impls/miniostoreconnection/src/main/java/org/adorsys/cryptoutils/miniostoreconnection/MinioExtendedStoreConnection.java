package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

import java.net.URL;

/**
 * Created by peter on 27.09.18.
 */
public class MinioExtendedStoreConnection extends BucketPathEncryptingExtendedStoreConnection {
    public static final String DEFAULT_ROOT_BUCKET_NAME = "org.adorsys.cryptoutils";

    public MinioExtendedStoreConnection(URL url, MinioAccessKey minioAccessKey, MinioSecretKey minioSecretKey, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this(url, minioAccessKey, minioSecretKey, DEFAULT_ROOT_BUCKET_NAME, bucketPathEncryptionPassword);
    }

    public MinioExtendedStoreConnection(URL url, MinioAccessKey minioAccessKey, MinioSecretKey minioSecretKey, String rootBucketName, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealMinioExtendedStoreConnection(url, minioAccessKey, minioSecretKey, rootBucketName), bucketPathEncryptionPassword);

    }

    public void cleanDatabase() {
        ((RealMinioExtendedStoreConnection) super.extendedStoreConnection).cleanDatabase();
    }


    public void showDatabase() {
        ((RealMinioExtendedStoreConnection) super.extendedStoreConnection).showDatabase();
    }
}
