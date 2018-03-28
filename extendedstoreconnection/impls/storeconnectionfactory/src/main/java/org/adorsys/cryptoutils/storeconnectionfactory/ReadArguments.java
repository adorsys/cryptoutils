package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
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
    public static final String FILESYSTEM = "SC-FILESYSTEM";

    public static final String MONGO_ARG = SYSTEM_PROPERTY_PREFIX + MONGO;
    public static final String MINIO_ARG = SYSTEM_PROPERTY_PREFIX + MINIO + "=";
    public static final String FILESYSTEM_ARG = SYSTEM_PROPERTY_PREFIX + FILESYSTEM;

    public ArgsAndConfig readArguments(String[] args) {
        List<String> remainingArgs = new ArrayList<>();
        StoreConnectionFactoryConfig config = new StoreConnectionFactoryConfig();

        Arrays.stream(args).forEach(arg -> {
                    if (arg.startsWith(MONGO_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MONGO;
                    } else if (arg.startsWith(MINIO_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MINIO;
                        String minioParams = arg.substring(MINIO_ARG.length());
                        config.minioParams = new MinioParamParser(minioParams);
                    } else if (arg.startsWith(FILESYSTEM_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
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
        try {
            StoreConnectionFactoryConfig config = new StoreConnectionFactoryConfig();
            if (System.getProperty(MONGO) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MONGO;
                return config;
            }
            if (System.getProperty(MINIO) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.MINIO;
                config.minioParams = new MinioParamParser(System.getProperty(MINIO));
                return config;
            }
            if (System.getProperty(FILESYSTEM) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
                return config;
            }
            config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
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
