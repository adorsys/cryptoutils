package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.adorsys.encobject.types.properties.BucketPathEncryptionFilenameOnly;
import org.adorsys.encobject.types.properties.MinioConnectionProperties;

import java.net.URL;

/**
 * Created by peter on 27.09.18.
 */
public class MinioExtendedStoreConnection extends BucketPathEncryptingExtendedStoreConnection {
    public MinioExtendedStoreConnection(MinioConnectionProperties properties) {
        this(
                properties.getUrl(),
                properties.getMinioAccessKey(),
                properties.getMinioSecretKey(),
                properties.getMinioRootBucketName(),
                properties.getBucketPathEncryptionPassword(),
                properties.getBucketPathEncryptionFilenameOnly());
    }

    public MinioExtendedStoreConnection(
            URL url,
            MinioAccessKey minioAccessKey,
            MinioSecretKey minioSecretKey,
            MinioRootBucketName rootBucketName,
            BucketPathEncryptionPassword bucketPathEncryptionPassword,
            BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        super(new RealMinioExtendedStoreConnection(url, minioAccessKey, minioSecretKey, rootBucketName), bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly);

    }

    public void cleanDatabase() {
        ((RealMinioExtendedStoreConnection) super.extendedStoreConnection).cleanDatabase();
    }


    public void showDatabase() {
        ((RealMinioExtendedStoreConnection) super.extendedStoreConnection).showDatabase();
    }
}
