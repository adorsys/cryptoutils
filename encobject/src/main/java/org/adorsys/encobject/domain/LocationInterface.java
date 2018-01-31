package org.adorsys.encobject.domain;

/**
 * Created by peter on 06.01.18.
 */
public interface LocationInterface {
    ObjectHandle getLocationHandle();

    /*
    *
    * Es felht noch eine to and fromString mit ca. folgender Funktionalität. Wird aber noch nicht benötigt.
    * keyStoreBucketName = new KeyStoreBucketPath(StringUtils.substringAfterLast(storeFQN, KeyStoreBucketPath.BUCKET_SEPARATOR));
	* String storeName = StringUtils.substringBeforeLast(storeFQN, KeyStoreBucketPath.BUCKET_SEPARATOR);
	* keyStoreType = new KeyStoreType(StringUtils.substringAfterLast(storeName, FILE_EXTENSION_SEPARATOR));
	* keyStoreID = new KeyStoreID(StringUtils.substringBeforeLast(storeName, FILE_EXTENSION_SEPARATOR));
    *
    */
}
