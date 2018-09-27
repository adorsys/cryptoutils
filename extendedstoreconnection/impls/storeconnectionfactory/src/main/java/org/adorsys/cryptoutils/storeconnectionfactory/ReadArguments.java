package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ParamParser;
import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoParamParser;
import org.adorsys.encobject.filesystem.FileSystemParamParser;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by peter on 27.03.18 at 20:20.
 */
public class ReadArguments {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReadArguments.class);
    private static final String SYSTEM_PROPERTY_PREFIX = "-D";
    public static final String MONGO = "SC-MONGO";
    public static final String MINIO = "SC-MINIO";
    public static final String AMAZONS3 = "SC-AMAZONS3";
    public static final String FILESYSTEM = "SC-FILESYSTEM";
    public static final String ENCRYPTION_PASSWORD = "SC-ENCRYPTION-PASSWORD";
    public static final String NO_ENCRYPTION_PASSWORD = "SC-NO-ENCRYPTION-PASSWORD";

    public static final String MONGO_ARG = SYSTEM_PROPERTY_PREFIX + MONGO + "=";
    public static final String MINIO_ARG = SYSTEM_PROPERTY_PREFIX + MINIO + "=";
    public static final String AMAZONS3_ARG = SYSTEM_PROPERTY_PREFIX + AMAZONS3 + "=";
    public static final String FILESYSTEM_ARG = SYSTEM_PROPERTY_PREFIX + FILESYSTEM + "=";
    public static final String ENCRYPTION_PASSWORD_ARG = SYSTEM_PROPERTY_PREFIX + ENCRYPTION_PASSWORD + "=";
    public static final String NO_ENCRYPTION_PASSWORD_ARG = SYSTEM_PROPERTY_PREFIX + NO_ENCRYPTION_PASSWORD + "=";

    public ArgsAndConfig readArguments(String[] args) {
        Arrays.stream(args).forEach(arg -> LOGGER.debug("readArguments arg:" + arg));

        List<String> remainingArgs = new ArrayList<>();
        StoreConnectionFactoryConfig config = new StoreConnectionFactoryConfig();

        Arrays.stream(args).forEach(arg -> {
                    if (arg.startsWith(MONGO_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MONGO;
                        config.mongoParams = new MongoParamParser(arg.substring(MONGO_ARG.length()));
                    } else if (arg.startsWith(MINIO_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MINIO;
                        config.minioParams = new MinioParamParser(arg.substring(MINIO_ARG.length()));
                    } else if (arg.startsWith(AMAZONS3_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.AMAZONS3;
                        config.amazonS3Params = new AmazonS3ParamParser(arg.substring(AMAZONS3_ARG.length()));
                    } else if (arg.startsWith(FILESYSTEM_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
                        config.fileSystemParamParser = new FileSystemParamParser(arg.substring(FILESYSTEM_ARG.length()));
                    } else if (arg.startsWith(ENCRYPTION_PASSWORD_ARG)) {
                        config.bucketPathEncryptionPassword = new BucketPathEncryptionPassword(arg.substring(ENCRYPTION_PASSWORD_ARG.length()));
                    } else if (arg.startsWith(NO_ENCRYPTION_PASSWORD_ARG)) {
                        config.bucketPathEncryptionPassword = null;
                    } else {
                        remainingArgs.add(arg);
                    }
                }
        );
        String[] remainingArgArray = new String[remainingArgs.size()];
        remainingArgArray = remainingArgs.toArray(remainingArgArray);
        return new ArgsAndConfig(config, remainingArgArray);
    }

    public StoreConnectionFactoryConfig readEnvironment() {
        LOGGER.debug("readEnvironment");

        try {
            StoreConnectionFactoryConfig config = new StoreConnectionFactoryConfig();
            if (System.getProperty(MONGO) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MONGO;
                config.mongoParams = new MongoParamParser(System.getProperty(MONGO));
                return config;
            }
            if (System.getProperty(MINIO) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MINIO;
                config.minioParams = new MinioParamParser(System.getProperty(MINIO));
                return config;
            }
            if (System.getProperty(AMAZONS3) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.AMAZONS3;
                config.amazonS3Params = new AmazonS3ParamParser(System.getProperty(AMAZONS3));
                return config;
            }
            if (System.getProperty(FILESYSTEM) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
                config.fileSystemParamParser = new FileSystemParamParser(System.getProperty(FILESYSTEM));
                return config;
            }
            if (System.getProperty(ENCRYPTION_PASSWORD) != null) {
                config.bucketPathEncryptionPassword = new BucketPathEncryptionPassword(System.getProperty(ENCRYPTION_PASSWORD));
            }
            if (System.getProperty(NO_ENCRYPTION_PASSWORD) != null) {
                config.bucketPathEncryptionPassword = null;
            }
            config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
            config.fileSystemParamParser = new FileSystemParamParser("");
            return config;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    public static class ArgsAndConfig {
        public StoreConnectionFactoryConfig config;
        public String[] remainingArgs;

        public ArgsAndConfig(StoreConnectionFactoryConfig config, String[] remainingArgs) {
            this.config = config;
            this.remainingArgs = remainingArgs;
        }
    }
}
