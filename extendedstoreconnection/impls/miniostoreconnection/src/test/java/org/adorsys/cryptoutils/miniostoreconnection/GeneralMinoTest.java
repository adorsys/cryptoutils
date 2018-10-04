package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.adorsys.encobject.types.properties.MinioConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * Created by peter on 18.03.18 at 20:02.
 */
public class GeneralMinoTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralMinoTest.class);
    private MinioAccessKey accessKey = new MinioAccessKey("simpleAccessKey");
    private MinioSecretKey secretKey = new MinioSecretKey("simpleSecretKey");
    private URL url = getUrl("http://ers818:9001");

    private static URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    // @Test
    public void a() {
        try {
            MinioClient minioClient = new MinioClient(url, accessKey.getValue(), secretKey.getValue());


            BucketDirectory rootBucket = new BucketDirectory("org.adorsys.cryptoutils");
            String CONTAINER_FILE = ".container.marker.file";

            if (!minioClient.bucketExists(rootBucket.getObjectHandle().getContainer())) {
                minioClient.makeBucket(rootBucket.getObjectHandle().getContainer());
            }
            String content = "Du Affe";
            String contentType = "application/txt";
            String base = "";
            for (int i = 0; i<30; i++) {
                base = base + "/1234567890" + "." + i;
                String filename = base + "/file.txt";
                LOGGER.info("bucketl ength   = " + rootBucket.getObjectHandle().getContainer().length());
                LOGGER.info("filename length = " + filename.length());
                LOGGER.info("totoal length   = " + (rootBucket.getObjectHandle().getContainer().length() + filename.length()));
                minioClient.putObject(rootBucket.getObjectHandle().getContainer(), filename, new ByteArrayInputStream(content.getBytes()), contentType);
            }
            LOGGER.debug("list now " + rootBucket);
            minioClient.listObjects(rootBucket.getObjectHandle().getContainer(), ".*/" + CONTAINER_FILE, true).forEach(
                    el -> {
                        try {
                            LOGGER.debug("found " + el.get().objectName());

                        } catch (Exception e) {
                            throw BaseExceptionHandler.handle(e);
                        }
                    });
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    // @Test
    public void b() {
        new MinioExtendedStoreConnection(url, accessKey, secretKey, MinioConnectionProperties.defaultBucketname, null).showDatabase();
    }

    // @Test
    public void c() {
        new MinioExtendedStoreConnection(url, accessKey, secretKey, MinioConnectionProperties.defaultBucketname, null).cleanDatabase();
    }

}
