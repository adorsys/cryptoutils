package org.adorsys.encobject.types.properties;

import org.adorsys.encobject.types.connection.*;

/**
 * Created by peter on 04.10.18.
 */
public interface MongoConnectionProperties extends ConnectionProperties {
    MongoURI getMongoURI();
}
