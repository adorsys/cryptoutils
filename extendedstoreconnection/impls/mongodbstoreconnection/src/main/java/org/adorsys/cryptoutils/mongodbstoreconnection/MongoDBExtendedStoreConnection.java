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
        this(
                properties.getMongoHost(),
                properties.getMongoPort(),
                properties.getMongoDatabaseName(),
                properties.getMongoUser(),
                properties.getMongoPassword(),
                properties.getBucketPathEncryptionPassword(),
                properties.getBucketPathEncryptionFilenameOnly());
    }

    public MongoDBExtendedStoreConnection(
            MongoHost host,
            MongoPort port,
            MongoDatabaseName databasename,
            MongoUser user,
            MongoPassword password,
            BucketPathEncryptionPassword bucketPathEncryptionPassword,
            BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        super(new RealMongoDBExtendedStoreConnection(host, port, databasename, user, password), bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly);
    }
}
