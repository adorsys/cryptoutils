package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

import org.adorsys.encobject.types.connection.AmazonS3AccessKey;
import org.adorsys.encobject.types.connection.AmazonS3Region;
import org.adorsys.encobject.types.connection.AmazonS3RootBucketName;
import org.adorsys.encobject.types.connection.AmazonS3SecretKey;
import org.adorsys.encobject.types.properties.AmazonS3ConnectionProperties;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;

import java.net.URL;

/**
 * Created by peter on 04.10.18.
 */
public class AmazonS3ConnectionProperitesImpl extends ConnectionPropertiesImpl implements AmazonS3ConnectionProperties{
    private AmazonS3AccessKey amazonS3AccessKey;
    private AmazonS3SecretKey amazonS3SecretKey;
    private AmazonS3Region amazonS3Region = defaultRegion;
    private AmazonS3RootBucketName amazonS3RootBucketName = defaultRootBucketName;
    private URL url;

    public AmazonS3ConnectionProperitesImpl() {}

    public AmazonS3ConnectionProperitesImpl(AmazonS3ConnectionProperties source) {
        super(source);
        amazonS3AccessKey = source.getAmazonS3AccessKey();
        amazonS3Region = source.getAmazonS3Region();
        amazonS3SecretKey = source.getAmazonS3SecretKey();
        amazonS3RootBucketName = source.getAmazonS3RootBucketName();
        url = source.getUrl();
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public AmazonS3AccessKey getAmazonS3AccessKey() {
        return amazonS3AccessKey;
    }

    @Override
    public AmazonS3SecretKey getAmazonS3SecretKey() {
        return amazonS3SecretKey;
    }

    @Override
    public AmazonS3Region getAmazonS3Region() {
        return amazonS3Region;
    }

    @Override
    public AmazonS3RootBucketName getAmazonS3RootBucketName() {
        return amazonS3RootBucketName;
    }

    public void setAmazonS3AccessKey(AmazonS3AccessKey amazonS3AccessKey) {
        this.amazonS3AccessKey = amazonS3AccessKey;
    }

    public void setAmazonS3SecretKey(AmazonS3SecretKey amazonS3SecretKey) {
        this.amazonS3SecretKey = amazonS3SecretKey;
    }

    public void setAmazonS3Region(AmazonS3Region amazonS3Region) {
        this.amazonS3Region = amazonS3Region;
    }

    public void setAmazonS3RootBucketName(AmazonS3RootBucketName amazonS3RootBucketName) {
        this.amazonS3RootBucketName = amazonS3RootBucketName;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
