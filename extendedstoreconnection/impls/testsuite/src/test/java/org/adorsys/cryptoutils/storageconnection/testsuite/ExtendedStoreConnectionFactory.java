package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.miniostoreconnection.MinioAccessKey;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
import org.adorsys.cryptoutils.miniostoreconnection.MinioSecretKey;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoDBExtendedStoreConnection;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 15.03.18 at 11:38.
 */
public class ExtendedStoreConnectionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtendedStoreConnectionFactory.class);
    public static ExtendedStoreConnection get() {
        try {
            if (System.getProperty("SC-MONGO") != null) {
                LOGGER.info("USE MongoDBExtendedStoreConnection");
                return new MongoDBExtendedStoreConnection();
            }
            if (System.getProperty("SC-MINIO") != null) {
                String value = System.getProperty("SC-MINIO");
                StringTokenizer st = new StringTokenizer(value, ",");
                String urlString = st.nextToken();
                String accessKey = st.nextToken();
                String secretKey = st.nextToken();
                URL url = new URL(urlString);
                MinioAccessKey minioAccessKey = new MinioAccessKey(accessKey);
                MinioSecretKey minioSecretKey = new MinioSecretKey(secretKey);

                LOGGER.info("USE MinioExtendedStoreConnection");
                return new MinioExtendedStoreConnection(url, minioAccessKey, minioSecretKey);
            }
            LOGGER.info("USE FileSystemExtendedStorageConnection");
            return new FileSystemExtendedStorageConnection();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
