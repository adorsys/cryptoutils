package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.encobject.types.connection.MongoDatabaseName;
import org.adorsys.encobject.types.connection.MongoHost;
import org.adorsys.encobject.types.connection.MongoPort;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;

/**
 * Created by peter on 04.10.18.
 */
public class MongoConnectionPropertiesImpl extends ConnectionPropertiesImpl implements MongoConnectionProperties {
    private MongoDatabaseName mongoDatabaseName = defaultDatabasename;
    private MongoPort mongoPort = defaultPort;
    private MongoHost mongoHost = defaultHost;

    @Override
    public MongoDatabaseName getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    @Override
    public MongoHost getMongoHost() {
        return mongoHost;
    }

    @Override
    public MongoPort getMongoPort() {
        return mongoPort;
    }

    public void setMongoDatabaseName(MongoDatabaseName mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    public void setMongoPort(MongoPort mongoPort) {
        this.mongoPort = mongoPort;
    }

    public void setMongoHost(MongoHost mongoHost) {
        this.mongoHost = mongoHost;
    }
}
