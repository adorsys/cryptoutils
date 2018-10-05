package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.*;

/**
 * Created by peter on 04.10.18.
 */
public interface MongoConnectionProperties extends ConnectionProperties {
    static final MongoHost defaultHost=new MongoHost("127.0.0.1");
    static final MongoPort defaultPort=new MongoPort(27017L);
    static final MongoDatabaseName defaultDatabasename = new MongoDatabaseName("mongodb");

    MongoDatabaseName getMongoDatabaseName();
    MongoHost getMongoHost();
    MongoPort getMongoPort();
    MongoUser getMongoUser();
    MongoPassword getMongoPassword();
}
