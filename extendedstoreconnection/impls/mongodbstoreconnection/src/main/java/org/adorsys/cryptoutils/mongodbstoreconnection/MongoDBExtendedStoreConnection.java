package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.properties.BucketPathEncryptionFilenameOnly;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;

/**
 * Created by peter on 27.09.18.
 */
public class MongoDBExtendedStoreConnection extends BucketPathEncryptingExtendedStoreConnection {
    public MongoDBExtendedStoreConnection(MongoConnectionProperties properties) {
        this(properties.getMongoURI(),
                properties.getBucketPathEncryptionPassword(),
                properties.getBucketPathEncryptionFilenameOnly());
    }

    public MongoDBExtendedStoreConnection(
            MongoURI mongoURI,
            BucketPathEncryptionPassword bucketPathEncryptionPassword,
            BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        super(new RealMongoDBExtendedStoreConnection(mongoURI), bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly);
    }
}
