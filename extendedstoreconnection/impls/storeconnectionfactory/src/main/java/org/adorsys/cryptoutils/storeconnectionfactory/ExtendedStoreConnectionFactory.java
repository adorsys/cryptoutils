package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ExtendedStoreConnection;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoDBExtendedStoreConnection;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.properties.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 15.03.18 at 11:38.
 */
public class ExtendedStoreConnectionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtendedStoreConnectionFactory.class);
    private static ConnectionProperties properties = null;

    public static ExtendedStoreConnection get(ConnectionProperties properties) {
        if (properties instanceof MongoConnectionProperties) {
            return new MongoDBExtendedStoreConnection((MongoConnectionProperties) properties);
        }
        if (properties instanceof MinioConnectionProperties) {
            return new MinioExtendedStoreConnection((MinioConnectionProperties) properties);
        }
        if (properties instanceof AmazonS3ConnectionProperties) {
            return new AmazonS3ExtendedStoreConnection((AmazonS3ConnectionProperties) properties);
        }
        if (properties instanceof FilesystemConnectionProperties) {
            return new FileSystemExtendedStorageConnection((FilesystemConnectionProperties) properties);
        }
        throw new BaseException("Properties of unknown type: " + properties.getClass().getName());
    }

    public static ExtendedStoreConnection get() {
        if (properties == null) {
            properties = new ReadArguments().readEnvironment();
        }
        return get(properties);
    }

    public static void reset() {
        properties = null;
    }

    /**
     * @param args
     * @return die Argumente, die nicht verwertet werden konnten
     */
    public static String[] readArguments(String[] args) {
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        properties = argsAndProperties.properties;
        return argsAndProperties.remainingArgs;
    }
}
