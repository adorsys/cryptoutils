package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.mongodbstoreconnection.MongoDBExtendedStoreConnection;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 15.03.18 at 11:38.
 */
public class ExtendedStoreConnectionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtendedStoreConnectionFactory.class);
    public static ExtendedStoreConnection get() {
        if (System.getProperty("SC-MONGO") != null) {
            LOGGER.info("USE MongoDBExtendedStoreConnection");
            return new MongoDBExtendedStoreConnection();
        }
        LOGGER.info("USE FileSystemExtendedStorageConnection");
        return new FileSystemExtendedStorageConnection();
    }
}
