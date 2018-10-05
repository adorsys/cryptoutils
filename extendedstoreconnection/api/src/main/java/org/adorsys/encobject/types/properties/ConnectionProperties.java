package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.BucketPathEncryptionPassword;

/**
 * Created by peter on 04.10.18.
 */
public interface ConnectionProperties {
    BucketPathEncryptionPassword defaultEncryptionPassword = new BucketPathEncryptionPassword("2837/(&dfja34j39,yiEsdkfhasDfkljh");

    BucketPathEncryptionPassword getBucketPathEncryptionPassword();
}
