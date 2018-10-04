package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;

import java.net.URL;

/**
 * Created by peter on 04.10.18.
 */
public interface MinioConnectionProperties extends ConnectionProperties {
    MinioRootBucketName defaultBucketname = new MinioRootBucketName("org.adorsys.cryptoutils");

    URL getUrl();
    MinioRootBucketName getMinioRootBucketName();
    MinioAccessKey getMinioAccessKey();
    MinioSecretKey getMinioSecretKey();
}
