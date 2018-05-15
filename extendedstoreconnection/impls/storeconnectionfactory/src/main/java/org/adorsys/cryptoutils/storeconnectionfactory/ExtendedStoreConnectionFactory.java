package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoDBExtendedStoreConnection;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

        Frame frame = new Frame();
        switch (config.connectionType) {
            case MONGO:
                frame.add("USE MONGO DB");
                frame.add("mongo db has be up and running )");
                frame.add("host: " + config.mongoParams.getHost());
                frame.add("port: " + config.mongoParams.getPort());
                frame.add("database: " + config.mongoParams.getDatabasename());
                LOGGER.debug(frame.toString());
                return new MongoDBExtendedStoreConnection(
                        config.mongoParams.getHost(),
                        config.mongoParams.getPort(),
                        config.mongoParams.getDatabasename());

            case MINIO:
                frame.add("USE MINIO SYSTEM");
                frame.add("(minio has be up and running )");
                frame.add("url: " + config.minioParams.getUrl().toString());
                frame.add("accessKey: " + config.minioParams.getMinioAccessKey().getValue());
                frame.add("secretKey: " + config.minioParams.getMinioSecretKey().getValue());
                LOGGER.debug(frame.toString());
                return new MinioExtendedStoreConnection(
                        config.minioParams.getUrl(),
                        config.minioParams.getMinioAccessKey(),
                        config.minioParams.getMinioSecretKey());

            case FILE_SYSTEM:
                frame.add("USE FILE SYSTEM");
                frame.add("basedir: " + config.fileSystemParamParser.getFilesystembase());
                LOGGER.debug(frame.toString());
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


    public static class Frame {
        private List<String> list = new ArrayList<>();

        public void add(String line) {
            list.add(line);
        }

        public String toString() {
            int max = 0;
            for (String line : list) {
                if (line.length() > max) max = line.length();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("***" + fill("", max, "*") + "***\n");
            sb.append("*  " + fill("", max, " ") + "  *\n");

            for (String line : list) {
                sb.append("*  " + fill(line, max, " ") + "  *\n");
            }

            sb.append("*  " + fill("", max, " ") + "  *\n");
            sb.append("***" + fill("", max, "*") + "***\n");
            return sb.toString();

        }

        private String fill(String start, int length, String el) {
            while(start.length() < length) {
                start = start + el;
            }
            return start;
        }
    }
}
