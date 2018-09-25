package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ExtendedStoreConnection;
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
    private static StoreConnectionFactoryConfig config = null;

    public static ExtendedStoreConnection get() {
        if (config == null) {
            config = new ReadArguments().readEnvironment();
        }

        switch (config.connectionType) {
            case MONGO:
                return new MongoDBExtendedStoreConnection(
                        config.mongoParams.getHost(),
                        config.mongoParams.getPort(),
                        config.mongoParams.getDatabasename());

            case MINIO:
                return new MinioExtendedStoreConnection(
                        config.minioParams.getUrl(),
                        config.minioParams.getMinioAccessKey(),
                        config.minioParams.getMinioSecretKey(),
                        config.minioParams.getRootBucketName());

            case AMAZONS3:
                return new AmazonS3ExtendedStoreConnection(
                        config.amazonS3Params.getUrl(),
                        config.amazonS3Params.getAmazonS3AccessKey(),
                        config.amazonS3Params.getAmazonS3SecretKey(),
                        config.amazonS3Params.getAmazonS3Region(),
                        config.amazonS3Params.getAmazonS3RootBucket());

            case FILE_SYSTEM:
                return new FileSystemExtendedStorageConnection(
                        config.fileSystemParamParser.getFilesystembase());

            default:
                throw new BaseException("missing switch");
        }
    }

    public static void reset() {
        config = null;
    }

    /**
     * @param args
     * @return die Argumente, die nicht verwertet werden konnten
     */
    public static String[] readArguments(String[] args) {
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        config = argsAndConfig.config;
        return argsAndConfig.remainingArgs;
    }
}
