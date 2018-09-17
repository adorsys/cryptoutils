package org.adorsys.cryptoutils.extendendstoreconnection.impl.ceph;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 17.09.18.
 */
public class CephExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(CephExtendedStoreConnection.class);
    private AmazonS3 connection = null;

    public CephExtendedStoreConnection(URL url, AmazonS3AccessKey accessKey, AmazonS3SecretKey secretKey) {


        AWSCredentialsProvider credentialsProvider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(accessKey.getValue(), secretKey.getValue());
            }

            @Override
            public void refresh() {

            }
        };

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(url.toString(), "US");

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSocketTimeout(500);
        clientConfig.setProtocol(Protocol.HTTP);
        clientConfig.disableSocketProxy();
        connection = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withEndpointConfiguration(endpoint)
                .withClientConfiguration(clientConfig)
                .withPayloadSigningEnabled(false)
                .enablePathStyleAccess()
                .build();
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        InputStream inputStream = new ByteArrayInputStream(payload.getData());
        PayloadStream payloadStream = new SimplePayloadStreamImpl(payload.getStorageMetadata(), inputStream);
        putBlobStream(bucketPath, payloadStream);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return getBlob(bucketPath, null);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        // die hier bereits mitgegebenen StorageMetadata werden dennoch erneut gelesen. Ist im CephInterface so vorgesehen.
        try {
            PayloadStream payloadStream = getBlobStream(bucketPath);
            byte[] content = IOUtils.toByteArray(payloadStream.openStream());
            Payload payload = new SimplePayloadImpl(payloadStream.getStorageMetadata(), content);
            return payload;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        LOGGER.debug("write stream for " + bucketPath);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        Map<String, String> userMetaData = new HashMap<>();
        payloadStream.getStorageMetadata().getUserMetadata().keySet().forEach(key -> userMetaData.put(key, payloadStream.getStorageMetadata().getUserMetadata().get(key)));
        objectMetadata.setUserMetadata(userMetaData);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName(), payloadStream.openStream(), objectMetadata);
        PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
        LOGGER.debug("write of stream for :" + bucketPath + " -> " + putObjectResult.toString());

    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        // die hier bereits mitgegebenen StorageMetadata werden dennoch erneut gelesen. Ist im CephInterface so vorgesehen.
        return getBlobStream(bucketPath, null);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        LOGGER.debug("read for " + bucketPath);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
        S3Object object = connection.getObject(getObjectRequest);
        S3ObjectInputStream objectContent = object.getObjectContent();
        StorageMetadata storageMetadata2 = new SimpleStorageMetadataImpl();
        object.getObjectMetadata().getUserMetadata().keySet().forEach(key -> storageMetadata2.getUserMetadata().put(key, object.getObjectMetadata().getUserMetadata().get(key)));
        PayloadStream payloadStream = new SimplePayloadStreamImpl(storageMetadata2, objectContent);
        LOGGER.debug("read ok for " + bucketPath);
        return payloadStream;
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest();
        getObjectMetadataRequest.setBucketName(bucketPath.getObjectHandle().getContainer());
        getObjectMetadataRequest.setKey(bucketPath.getObjectHandle().getName());
        ObjectMetadata objectMetadata = connection.getObjectMetadata(getObjectMetadataRequest);
        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        objectMetadata.getUserMetadata().keySet().forEach(key -> storageMetadata.getUserMetadata().put(key, objectMetadata.getUserMetadata().get(key)));
        return storageMetadata;
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        return false;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {

    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {

    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {

    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        return false;
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {

    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        return null;
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        return null;
    }
}
