package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;

/**
 * Created by peter on 04.10.18.
 */
public class MongoConnectionPropertiesImpl extends ConnectionPropertiesImpl implements MongoConnectionProperties {
    private MongoDatabaseName mongoDatabaseName = defaultDatabasename;
    private MongoPort mongoPort = defaultPort;
    private MongoHost mongoHost = defaultHost;
    private MongoUser mongoUser = null;
    private MongoPassword mongoPassword = null;

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

    @Override
    public MongoUser getMongoUser() {
        return mongoUser;
    }

    @Override
    public MongoPassword getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPort(MongoPort mongoPort) {
        this.mongoPort = mongoPort;
    }

    public void setMongoHost(MongoHost mongoHost) {
        this.mongoHost = mongoHost;
    }

    public void setMongoUser(MongoUser mongoUser) {
        this.mongoUser = mongoUser;
    }

    public void setMongoPassword(MongoPassword mongoPassword) {
        this.mongoPassword = mongoPassword;
    }
}
