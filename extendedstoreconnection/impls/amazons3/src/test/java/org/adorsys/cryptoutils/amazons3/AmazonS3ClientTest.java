package org.adorsys.cryptoutils.amazons3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 18.03.18 at 20:02.
 */
public class AmazonS3ClientTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmazonS3ClientTest.class);
    private AmazonS3AccessKey accessKey = new AmazonS3AccessKey("simpleAccessKey");
    private AmazonS3SecretKey secretKey = new AmazonS3SecretKey("simpleSecretKey");
    private String urlString = "http://electronicrs818:15080";

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
            String AFFE = "affe";
            AWSCredentialsProvider credentialsProvider = new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {
                    return new BasicAWSCredentials(accessKey.getValue(), secretKey.getValue());
                }

                @Override
                public void refresh() {

                }
            };
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setProtocol(Protocol.HTTP);

            AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(urlString, "DE");

            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setSocketTimeout(1500);
            clientConfig.setProtocol(Protocol.HTTP);
            clientConfig.disableSocketProxy();
            AmazonS3 conn = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withEndpointConfiguration(endpoint)
                    .withClientConfiguration(clientConfig)
                    .withPayloadSigningEnabled(false)
                    .enablePathStyleAccess()
                    .build();
            List<Bucket> buckets = conn.listBuckets();
            buckets.forEach(bucket -> {
                LOGGER.info("found bucket:" + bucket);
                ObjectListing objectListing = conn.listObjects(new ListObjectsRequest().withBucketName(bucket.getName()));
                objectListing.getObjectSummaries().forEach(sum -> {
                    LOGGER.info("found " + sum.getKey() + " in " + sum.getBucketName());
                });
            });

            // Erzeuge Datei affe.txt im bucket affe
            Bucket bucket = null;
            if (!conn.doesBucketExistV2(AFFE)) {
                LOGGER.info("bucket " + AFFE + " does not exist yet. create it");
                bucket = conn.createBucket(AFFE);
            } else {
                LOGGER.info("bucket " + AFFE + " wird wiederverwendet");
                bucket = conn.listBuckets().stream().filter(b -> b.getName().equals(AFFE)).findFirst().get();
            }

            String key = "firstFile.txt";
            LOGGER.info("start create file " + key + " in bucket " + AFFE);
            String content = "affe";
            InputStream is = new ByteArrayInputStream(content.getBytes());

            boolean withTmpfile = false;
            if (!withTmpfile) {
                LOGGER.info("safe directly");
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(content.getBytes().length);
                Map<String, String> userMetadata = new HashMap<>();
                userMetadata.put("mykey", "myvalue");
                objectMetadata.setUserMetadata(userMetadata);
                PutObjectRequest putObjectRequest = new PutObjectRequest(AFFE, key, is, objectMetadata);
                PutObjectResult putObjectResult = conn.putObject(putObjectRequest);
                LOGGER.info("creation of object :" + putObjectResult.toString());
            } else {
                LOGGER.info("safe to tempFile first");
                File tmpFile = safeFile(is);
                LOGGER.info("now safe tempFile to ceph");

                PutObjectResult putObjectResult = conn.putObject(AFFE, key, tmpFile);
                LOGGER.info("creation of object :" + putObjectResult.toString());
            }

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public File safeFile(InputStream is) {
        try {
            File targetFile = File.createTempFile("ceph", "tmp");
            FileUtils.copyInputStreamToFile(is, targetFile);
            return targetFile;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
