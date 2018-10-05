package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;
import org.adorsys.encobject.types.properties.MinioConnectionProperties;

import java.net.URL;

/**
 * Created by peter on 04.10.18.
 */
public class MinioConnectionPropertiesImpl extends ConnectionPropertiesImpl implements MinioConnectionProperties {
    private URL url;
    private MinioRootBucketName minioRootBucketName = defaultBucketname;
    private MinioAccessKey minioAccessKey;
    private MinioSecretKey minioSecretKey;

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public MinioRootBucketName getMinioRootBucketName() {
        return minioRootBucketName;
    }

    @Override
    public MinioAccessKey getMinioAccessKey() {
        return minioAccessKey;
    }

    @Override
    public MinioSecretKey getMinioSecretKey() {
        return minioSecretKey;
    }

    public void setMinioRootBucketName(MinioRootBucketName minioRootBucketName) {
        this.minioRootBucketName = minioRootBucketName;
    }

    public void setMinioAccessKey(MinioAccessKey minioAccessKey) {
        this.minioAccessKey = minioAccessKey;
    }

    public void setMinioSecretKey(MinioSecretKey minioSecretKey) {
        this.minioSecretKey = minioSecretKey;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
