package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.ceph.CephParamParser;
import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoParamParser;
import org.adorsys.encobject.filesystem.FileSystemParamParser;
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
    public static final String CEPH = "SC-CEPH";
    public static final String FILESYSTEM = "SC-FILESYSTEM";

    public static final String MONGO_ARG = SYSTEM_PROPERTY_PREFIX + MONGO + "=";
    public static final String MINIO_ARG = SYSTEM_PROPERTY_PREFIX + MINIO + "=";
    public static final String CEPH_ARG = SYSTEM_PROPERTY_PREFIX + CEPH + "=";
    public static final String FILESYSTEM_ARG = SYSTEM_PROPERTY_PREFIX + FILESYSTEM + "=";

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
                    } else if (arg.startsWith(CEPH_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.CEPH;
                        config.cephParams = new CephParamParser(arg.substring(CEPH_ARG.length()));
                    } else if (arg.startsWith(FILESYSTEM_ARG)) {
                        config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
                        config.fileSystemParamParser = new FileSystemParamParser(arg.substring(FILESYSTEM_ARG.length()));
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
            if (System.getProperty(CEPH) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.CEPH;
                config.cephParams = new CephParamParser(System.getProperty(CEPH));
                return config;
            }
            if (System.getProperty(FILESYSTEM) != null) {
                config.connectionType = StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM;
                config.fileSystemParamParser = new FileSystemParamParser(System.getProperty(FILESYSTEM));
                return config;
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
