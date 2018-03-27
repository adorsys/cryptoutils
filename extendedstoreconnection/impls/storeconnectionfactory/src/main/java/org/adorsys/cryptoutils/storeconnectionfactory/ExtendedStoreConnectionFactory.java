package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
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
    private static StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();

    public static ExtendedStoreConnection get() {

        switch(config.connectionType) {
            case MONGO:
                LOGGER.info("***************************************");
                LOGGER.info("*                                     *");
                LOGGER.info("*  USE MONGO DB                       *");
                LOGGER.info("*  (mongo db has be up and running )  *");
                LOGGER.info("*                                     *");
                LOGGER.info("***************************************");
                return new MongoDBExtendedStoreConnection();

            case MINIO:
                LOGGER.info("************************************");
                LOGGER.info("*                                  *");
                LOGGER.info("*  USE MINIO SYSTEM                *");
                LOGGER.info("*  (minio has be up and running )  *");
                LOGGER.info("*                                  *");
                LOGGER.info("************************************");
                return new MinioExtendedStoreConnection(
                        config.minioParams.getUrl(),
                        config.minioParams.getMinioAccessKey(),
                        config.minioParams.getMinioSecretKey());

            case FILE_SYSTEM:
                LOGGER.info("**********************");
                LOGGER.info("*                    *");
                LOGGER.info("*  USE FILE SYSTEM   *");
                LOGGER.info("*                    *");
                LOGGER.info("**********************");
                return new FileSystemExtendedStorageConnection();

            default:
                throw new BaseException("missing switch");
        }
    }

    /**
     *
     * @param args
     * @return die Argumente, die nicht verwertet werden konnten
     */
    public static String[] readArguments(String[] args) {
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        config = argsAndConfig.config;
        return argsAndConfig.remainingArgs;
    }
}
