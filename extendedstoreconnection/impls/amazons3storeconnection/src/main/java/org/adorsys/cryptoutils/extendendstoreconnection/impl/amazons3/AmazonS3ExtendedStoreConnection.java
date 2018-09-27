package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

import java.net.URL;

/**
 * Created by peter on 27.09.18.
 */
public class AmazonS3ExtendedStoreConnection extends BucketPathEncryptingExtendedStoreConnection {
    public final static AmazonS3RootBucket DEFAULT_BUCKET = new AmazonS3RootBucket("amazons3rootbucketforadorsys");
    public final static AmazonS3Region DEFAULT_REGION = new AmazonS3Region("us-east-1");

    public AmazonS3ExtendedStoreConnection(URL url, AmazonS3AccessKey accessKey, AmazonS3SecretKey secretKey, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this(url, accessKey, secretKey, DEFAULT_REGION, DEFAULT_BUCKET, bucketPathEncryptionPassword);
    }

    public AmazonS3ExtendedStoreConnection(URL url, AmazonS3AccessKey accessKey, AmazonS3SecretKey secretKey, AmazonS3Region amazonS3Region, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this(url, accessKey, secretKey, amazonS3Region, DEFAULT_BUCKET, bucketPathEncryptionPassword);
    }

    public AmazonS3ExtendedStoreConnection(URL url, AmazonS3AccessKey accessKey, AmazonS3SecretKey secretKey, AmazonS3Region anAmazonS3Region, AmazonS3RootBucket anAmazonS3RootBucket, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealAmazonS3ExtendedStoreConnection(url, accessKey, secretKey, anAmazonS3Region, anAmazonS3RootBucket), bucketPathEncryptionPassword);
    }


    public void cleanDatabase() {
        ((RealAmazonS3ExtendedStoreConnection) super.extendedStoreConnection).cleanDatabase();
    }


    public void showDatabase() {
        ((RealAmazonS3ExtendedStoreConnection) super.extendedStoreConnection).showDatabase();
    }
}