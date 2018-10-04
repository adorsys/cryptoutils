package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.AmazonS3AccessKey;
import org.adorsys.encobject.types.connection.AmazonS3Region;
import org.adorsys.encobject.types.connection.AmazonS3RootBucketName;
import org.adorsys.encobject.types.connection.AmazonS3SecretKey;

import java.net.URL;

/**
 * Created by peter on 04.10.18.
 */
public interface AmazonS3ConnectionProperties extends ConnectionProperties {
    AmazonS3Region defaultRegion = new AmazonS3Region("us-east-1");
    AmazonS3RootBucketName defaultRootBucketName = new AmazonS3RootBucketName("amazons3rootbucketforadorsys");

    URL getUrl();
    AmazonS3AccessKey getAmazonS3AccessKey();
    AmazonS3SecretKey getAmazonS3SecretKey();
    AmazonS3Region getAmazonS3Region();
    AmazonS3RootBucketName getAmazonS3RootBucketName();
}
