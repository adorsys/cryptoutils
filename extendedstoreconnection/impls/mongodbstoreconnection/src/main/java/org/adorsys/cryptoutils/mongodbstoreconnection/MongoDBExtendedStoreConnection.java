package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.connection.MongoDatabaseName;
import org.adorsys.encobject.types.connection.MongoHost;
import org.adorsys.encobject.types.connection.MongoPort;
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
                properties.getBucketPathEncryptionPassword());
    }

    public MongoDBExtendedStoreConnection(
            MongoHost host,
            MongoPort port,
            MongoDatabaseName databasename,
            BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealMongoDBExtendedStoreConnection(host, port, databasename), bucketPathEncryptionPassword);
    }
}
