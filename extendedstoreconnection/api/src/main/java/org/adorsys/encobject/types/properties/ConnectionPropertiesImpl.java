package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.BucketPathEncryptionPassword;

/**
 * Created by peter on 04.10.18.
 */
public class ConnectionPropertiesImpl implements ConnectionProperties {
    private BucketPathEncryptionPassword bucketPathEncryptionPassword = defaultEncryptionPassword;
    private BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly = defaultBucketPathEncryptionFilenameOnly;

    @Override
    public BucketPathEncryptionPassword getBucketPathEncryptionPassword() {
        return bucketPathEncryptionPassword;
    }

    public void setBucketPathEncryptionPassword(BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this.bucketPathEncryptionPassword = bucketPathEncryptionPassword;
    }

    public BucketPathEncryptionFilenameOnly getBucketPathEncryptionFilenameOnly() {
        return bucketPathEncryptionFilenameOnly;
    }

    public void setBucketPathEncryptionFilenameOnly(BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        this.bucketPathEncryptionFilenameOnly = bucketPathEncryptionFilenameOnly;
    }
}
