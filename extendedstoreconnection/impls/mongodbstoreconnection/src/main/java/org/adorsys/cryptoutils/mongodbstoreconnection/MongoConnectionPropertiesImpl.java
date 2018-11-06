package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;

/**
 * Created by peter on 04.10.18.
 */
public class MongoConnectionPropertiesImpl extends ConnectionPropertiesImpl implements MongoConnectionProperties {
    private MongoURI mongoURI = null;

    @Override
    public MongoURI getMongoURI() {
        return mongoURI;
    }

    public void setMongoURI(MongoURI mongoURI) {
        this.mongoURI = mongoURI;
    }
}
