package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Test;

import java.net.URL;

/**
 * Created by peter on 18.03.18 at 20:02.
 */
public class GeneralMinoTest {
    @Test
    public void a() {
        try {
            String accessKey = "7IVCW3EES6A5L9YV5VO8";
            String secretKey= "6aTPgIRhyfH5sHdmff69Mizjiefudf0mFtuwNXN7";
            URL url = new URL("http://localhost:9000");
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);

            String bucket = "affe";
            Assert.assertFalse(minioClient.bucketExists(bucket));
            minioClient.makeBucket(bucket);
            Assert.assertTrue(minioClient.bucketExists(bucket));
            minioClient.removeBucket(bucket);
            Assert.assertFalse(minioClient.bucketExists(bucket));

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
