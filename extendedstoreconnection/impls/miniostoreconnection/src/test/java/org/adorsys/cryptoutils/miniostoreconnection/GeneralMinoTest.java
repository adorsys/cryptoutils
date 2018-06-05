package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * Created by peter on 18.03.18 at 20:02.
 */
public class GeneralMinoTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralMinoTest.class);

    /*
    private MinioAccessKey accessKey = new MinioAccessKey("simpleAccessKey");
    private MinioSecretKey secretKey = new MinioSecretKey("simpleSecretKey");
    private URL url = getUrl("http://electronicrs818:9001");
    private String bucketName = "org.adorsys.cryptoutils";
    */

    private MinioAccessKey accessKey = new MinioAccessKey("adorsys");
    private MinioSecretKey secretKey = new MinioSecretKey("faizooSe0eiDodahx7ath7athah4leeS");
    private URL url = getUrl("https://ceph-demo.cloud.adorsys.de");
    private String bucketName = "org.adorsys.cryptoutils";


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


            BucketDirectory rootBucket = new BucketDirectory(bucketName);
            if (!minioClient.bucketExists(bucketName)) {
                LOGGER.debug("erzeuge Bucket " + bucketName);
                minioClient.makeBucket(bucketName);
            }



            String FIlE_NAME = "first.txt";
            LOGGER.info("create file " + FIlE_NAME);
            byte[] bytes = "Du Affe".getBytes();
            minioClient.putObject(
                    rootBucket.appendName(FIlE_NAME).getObjectHandle().getContainer(),
                    rootBucket.appendName(FIlE_NAME).getObjectHandle().getName(),
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    "");

            LOGGER.debug("list now " + bucketName);
            minioClient.listObjects(bucketName, "", true).forEach(
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
