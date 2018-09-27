package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption.BucketPathEncryptingExtendedStoreConnection;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

/**
 * Created by peter on 27.09.18.
 */
public class MongoDBExtendedStoreConnection extends BucketPathEncryptingExtendedStoreConnection {
    public MongoDBExtendedStoreConnection(String host, Integer port, String databasename, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        super(new RealMongoDBExtendedStoreConnection(host, port, databasename), bucketPathEncryptionPassword);
    }
}
