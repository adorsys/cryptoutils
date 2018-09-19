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
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by peter on 17.09.18.
 */
public class CephExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(CephExtendedStoreConnection.class);
    private AmazonS3 connection = null;
    private final static String CEPH_TMP_FILE_PREFIX = "CEPH_TMP_FILE_";
    private final static String CEPH_TMP_FILE_SUFFIX = "";


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
        putBlobStreamWithMemory(bucketPath, payloadStream, payload.getData().length);
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
        putBlobStreamWithTempFile(bucketPath, payloadStream);
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
        GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(
                bucketPath.getObjectHandle().getContainer(),
                bucketPath.getObjectHandle().getName());
        ObjectMetadata objectMetadata = connection.getObjectMetadata(getObjectMetadataRequest);
        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setType(StorageType.BLOB);
        storageMetadata.setName(BucketPathUtil.getAsString(bucketPath));
        objectMetadata.getUserMetadata().keySet().forEach(key -> storageMetadata.getUserMetadata().put(key, objectMetadata.getUserMetadata().get(key)));
        return storageMetadata;
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        // actually using exceptions is not nice, but it seems to be much faster than any list command
        try {
            connection.getObjectMetadata(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            LOGGER.debug("blob exists " + bucketPath + " TRUE");
            return true;
        } catch (Exception e) {
            LOGGER.debug("blob exists " + bucketPath + " FALSE");
            return false;
        }
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
            LOGGER.debug("removeBlob " + bucketPath);
            connection.deleteObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        LOGGER.debug("remove blob folder " + bucketDirectory);
        if (bucketDirectory.getObjectHandle().getName() == null) {
            throw new StorageConnectionException("not a valid bucket directory " + bucketDirectory);
        }

        List<StorageMetadata> storageMetadatas = list(bucketDirectory, ListRecursiveFlag.TRUE);
        storageMetadatas.forEach(metadata -> {
            if (metadata.getType().equals(StorageType.BLOB)) {
                BucketPath fullName = new BucketPath(metadata.getName());
                connection.deleteObject(fullName.getObjectHandle().getContainer(),
                        fullName.getObjectHandle().getName());
            }
        });
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("create bucket " + bucketDirectory);
        connection.createBucket(bucketDirectory.getObjectHandle().getContainer());
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        LOGGER.debug("container exsits " + bucketDirectory);
        return connection.doesBucketExistV2(bucketDirectory.getObjectHandle().getContainer());
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("delete bucket " + bucketDirectory);
        List<StorageMetadata> list = list(bucketDirectory, ListRecursiveFlag.TRUE);
        for (StorageMetadata storageMetadata : list) {
            if (storageMetadata.getType().equals(StorageType.BLOB)) {
                removeBlob(new BucketPath(storageMetadata.getName()));
            }
        }
        connection.deleteBucket(bucketDirectory.getObjectHandle().getContainer());
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.debug("list " + bucketDirectory);
        List<StorageMetadata> returnList = new ArrayList<>();
        if (!containerExists(bucketDirectory)) {
            LOGGER.debug("return empty list for " + bucketDirectory);
            return returnList;
        }

        String container = bucketDirectory.getObjectHandle().getContainer();
        String prefix = bucketDirectory.getObjectHandle().getName();
        if (prefix == null) {
            prefix = BucketPath.BUCKET_SEPARATOR;
        } else {
            prefix = BucketPath.BUCKET_SEPARATOR + prefix;
        }

        LOGGER.debug("search in " + container + " with prefix " + prefix + " " + listRecursiveFlag);
        String searchKey = prefix.substring(1); // remove first slash
        ObjectListing ol = connection.listObjects(container, searchKey);
        final List<String> keys = new ArrayList<>();
        ol.getObjectSummaries().forEach(el -> keys.add(BucketPath.BUCKET_SEPARATOR + el.getKey()));
        returnList = filter(container, prefix, keys, listRecursiveFlag);
        returnList.forEach(el -> LOGGER.debug("return for " + bucketDirectory + " :" + el.getName() + " type " + el.getType()));
        return returnList;
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        LOGGER.debug("list all buckets");
        List<BucketDirectory> buckets = new ArrayList<>();
        connection.listBuckets().forEach(bucket -> buckets.add(new BucketDirectory(bucket.getName())));
        return buckets;
    }

    // ==========================================================================

    List<StorageMetadata> filter(String container, String prefix, final List<String> keys, ListRecursiveFlag recursive) {
        List<StorageMetadata> result = new ArrayList<>();
        Set<String> dirs = new HashSet<>();

        int numberOfDelimitersOfPrefix = StringUtils.countMatches(prefix, BucketPath.BUCKET_SEPARATOR);
        if (prefix.length() > BucketPath.BUCKET_SEPARATOR.length()) {
            numberOfDelimitersOfPrefix++;
        }
        int numberOfDelimitersExpected = numberOfDelimitersOfPrefix;

        keys.forEach(key -> {
            if (recursive.equals(ListRecursiveFlag.TRUE)) {
                result.add(getStorageMetadata(new BucketPath(container, key)));
            } else {
                int numberOfDelimitersOfKey = StringUtils.countMatches(key, BucketPath.BUCKET_SEPARATOR);
                if (numberOfDelimitersOfKey == numberOfDelimitersExpected) {
                    result.add(getStorageMetadata(new BucketPath(container, key)));
                }
            }

            if (recursive.equals(ListRecursiveFlag.TRUE)) {
                int lastDelimiter = key.lastIndexOf(BucketPath.BUCKET_SEPARATOR);
                String dir = key.substring(0, lastDelimiter);
                if (dir.length() == 0) {
                    dir = BucketPath.BUCKET_SEPARATOR;
                }
                dirs.add(dir);
            } else {
                int numberOfDelimitersOfKey = StringUtils.countMatches(key, BucketPath.BUCKET_SEPARATOR);
                if (numberOfDelimitersOfKey == numberOfDelimitersExpected + 1) {
                    int lastDelimiter = key.lastIndexOf(BucketPath.BUCKET_SEPARATOR);
                    String dir = key.substring(0, lastDelimiter);
                    dirs.add(dir);
                }
            }

        });
        {
            if (dirs.isEmpty() && result.isEmpty()) {
                if (blobExists(new BucketPath(container, prefix))) {
                    // die If-Abfrage dient dem Spezialfall, dass jemand einen BucketPath als BucketDirectory uebergeben hat.
                    // Dann gibt es diesen bereits als file, dann muss eine leere Liste zurücgeben werden
                    return new ArrayList<>();
                }
            }
            // Auch wenn kein file gefunden wurde, das Verzeichnis exisitiert und ist daher zurückzugeben
            dirs.add(prefix);
        }

        for (String dir : dirs) {
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.setType(StorageType.FOLDER);
            storageMetadata.setName(BucketPathUtil.getAsString(new BucketDirectory(new BucketPath(container, dir))));
            result.add(storageMetadata);
        }
        return result;
    }
    private void putBlobStreamWithMemory(BucketPath bucketPath, PayloadStream payloadStream, int size) {
        try {
            LOGGER.debug("write stream for " + bucketPath + " with known length " + size);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(size);
            Map<String, String> userMetaData = new HashMap<>();
            payloadStream.getStorageMetadata().getUserMetadata().keySet().forEach(key -> userMetaData.put(key, payloadStream.getStorageMetadata().getUserMetadata().get(key)));
            objectMetadata.setUserMetadata(userMetaData);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName(), payloadStream.openStream(), objectMetadata);
            PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
            // LOGGER.debug("write of stream for :" + bucketPath + " -> " + putObjectResult.toString());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void putBlobStreamWithTempFile(BucketPath bucketPath, PayloadStream payloadStream) {
        try {
            LOGGER.debug("store " + bucketPath + " to tmpfile with unknown size");
            InputStream is = payloadStream.openStream();
            File targetFile = File.createTempFile(CEPH_TMP_FILE_PREFIX, CEPH_TMP_FILE_SUFFIX);
            java.nio.file.Files.copy(
                    is,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(is);
            LOGGER.debug(bucketPath + " with tmpfile " + targetFile.getAbsolutePath() + " written with " + targetFile.length() + " bytes -> will now be copied to minio");
            FileInputStream fis = new FileInputStream(targetFile);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(targetFile.length());
            Map<String, String> userMetaData = new HashMap<>();
            payloadStream.getStorageMetadata().getUserMetadata().keySet().forEach(key -> userMetaData.put(key, payloadStream.getStorageMetadata().getUserMetadata().get(key)));
            objectMetadata.setUserMetadata(userMetaData);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName(), payloadStream.openStream(), objectMetadata);
            PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
            IOUtils.closeQuietly(fis);
            LOGGER.debug("stored " + bucketPath + " to minio with size " + targetFile.length());
            targetFile.delete();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
