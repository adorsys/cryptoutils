package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by peter on 18.03.18 at 20:02.
 */
public class GeneralMinoTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralMinoTest.class);
    private MinioAccessKey accessKey = new MinioAccessKey("9J8I2EAWUNRVRLXIVV5B");
    private MinioSecretKey secretKey = new MinioSecretKey("wJKOJNWiQVBkzIipinJlG8k6iCFlSlES1c9mo2jI");
    private URL url = getUrl("http://localhost:9000");

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
        new MinioExtendedStoreConnection(url, accessKey, secretKey).showDatabase();
    }

    // @Test
    public void c() {
        new MinioExtendedStoreConnection(url, accessKey, secretKey).cleanDatabase();
    }

}
