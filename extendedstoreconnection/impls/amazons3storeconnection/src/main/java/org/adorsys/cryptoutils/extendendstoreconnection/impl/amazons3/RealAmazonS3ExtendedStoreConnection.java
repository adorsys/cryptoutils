package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

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
import org.adorsys.cryptoutils.utils.Frame;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.connection.AmazonS3AccessKey;
import org.adorsys.encobject.types.connection.AmazonS3Region;
import org.adorsys.encobject.types.connection.AmazonS3RootBucketName;
import org.adorsys.encobject.types.connection.AmazonS3SecretKey;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by peter on 17.09.18.
 */
class RealAmazonS3ExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(RealAmazonS3ExtendedStoreConnection.class);
    private static final Logger SPECIAL_LOGGER = LoggerFactory.getLogger("SPECIAL_LOGGER");
    private AmazonS3 connection = null;
    private final static String AMAZONS3_TMP_FILE_PREFIX = "AMAZONS3_TMP_FILE_";
    private final static String AMAZONS3_TMP_FILE_SUFFIX = "";
    private static final String STORAGE_METADATA_KEY = "StorageMetadata";
    private StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();
    private final static int AMAZON_S3_META_LIMIT = 1024 * 2;
    private BucketDirectory amazonS3RootBucket;
    private BucketDirectory amazonS3RootContainersBucket;
    private AmazonS3Region amazonS3Region;

    public RealAmazonS3ExtendedStoreConnection(URL url,
                                               AmazonS3AccessKey accessKey,
                                               AmazonS3SecretKey secretKey,
                                               AmazonS3Region anAmazonS3Region,
                                               AmazonS3RootBucketName anAmazonS3RootBucketName) {
        amazonS3Region = anAmazonS3Region;
        amazonS3RootBucket = new BucketDirectory(anAmazonS3RootBucketName.getValue());
        amazonS3RootContainersBucket = new BucketDirectory(amazonS3RootBucket.getObjectHandle().getContainer() + ".containers");
        Frame frame = new Frame();
        frame.add("USE AMAZON S3 COMPLIANT SYSTEM");
        frame.add("(has be up and running)");
        frame.add("url: " + url.toString());
        frame.add("accessKey:   " + accessKey);
        frame.add("secretKey:   " + secretKey);
        frame.add("region:      " + amazonS3Region);
        frame.add("root bucket: " + amazonS3RootBucket);
        LOGGER.info(frame.toString());
        if (LOGGER.isDebugEnabled()) {
            new BaseException("JUST A STACK, TO SEE WHERE THE CONNECTION IS CREATED");
        }

        AWSCredentialsProvider credentialsProvider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(accessKey.getValue(), secretKey.getValue());
            }

            @Override
            public void refresh() {

            }
        };

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(url.toString(), amazonS3Region.getValue());

        ClientConfiguration clientConfig = new ClientConfiguration();
        // clientConfig.setSocketTimeout(10000);
        clientConfig.setProtocol(Protocol.HTTP);
        clientConfig.disableSocketProxy();
        connection = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withEndpointConfiguration(endpoint)
                .withClientConfiguration(clientConfig)
                .withPayloadSigningEnabled(false)
                .enablePathStyleAccess()
                .build();

        if (!connection.doesBucketExistV2(amazonS3RootBucket.getObjectHandle().getContainer())) {
            connection.createBucket(amazonS3RootBucket.getObjectHandle().getContainer());
        }
        if (!connection.doesBucketExistV2(amazonS3RootContainersBucket.getObjectHandle().getContainer())) {
            connection.createBucket(amazonS3RootContainersBucket.getObjectHandle().getContainer());
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        LOGGER.debug("putBlob " + bucketPath);
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
        // die hier bereits mitgegebenen StorageMetadata werden dennoch erneut gelesen. Ist im Interface so vorgesehen.
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
    public PayloadStream getBlobStream(BucketPath abucketPath, StorageMetadata storageMetadata) {
        LOGGER.debug("getBlobStream " + abucketPath);
        BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
        S3Object object = connection.getObject(getObjectRequest);
        S3ObjectInputStream objectContent = object.getObjectContent();
        StorageMetadata storageMetadata2 = getStorageMetadataFromObjectdata(object.getObjectMetadata(), abucketPath);
        PayloadStream payloadStream = new SimplePayloadStreamImpl(storageMetadata2, objectContent);
        LOGGER.debug("read ok for " + bucketPath);
        return payloadStream;
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        LOGGER.debug("putBlob " + bucketPath);
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath abucketPath) {
        SPECIAL_LOGGER.debug("readmetadata " + abucketPath); // Dies LogZeile ist fuer den JUNIT-Tests StorageMetaDataTest
        LOGGER.debug("getStorageMetadata " + abucketPath);
        BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

        GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(
                bucketPath.getObjectHandle().getContainer(),
                bucketPath.getObjectHandle().getName());
        ObjectMetadata objectMetadata = connection.getObjectMetadata(getObjectMetadataRequest);
        StorageMetadata storageMetadata = getStorageMetadataFromObjectdata(objectMetadata, abucketPath);
        return storageMetadata;
    }

    @Override
    public boolean blobExists(BucketPath abucketPath) {
        LOGGER.debug("blobExists " + abucketPath);
        BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

        // actually using exceptions is not nice, but it seems to be much faster than any list command
        try {
            connection.getObjectMetadata(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            LOGGER.debug("blob exists " + abucketPath + " TRUE");
            return true;
        } catch (Exception e) {
            LOGGER.debug("blob exists " + abucketPath + " FALSE");
            return false;
        }
    }

    @Override
    public void removeBlob(BucketPath abucketPath) {
        LOGGER.debug("removeBlob " + abucketPath);
        BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

        connection.deleteObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        LOGGER.debug("removeBlobFolder " + bucketDirectory);
        if (bucketDirectory.getObjectHandle().getName() == null) {
            throw new StorageConnectionException("not a valid bucket directory " + bucketDirectory);
        }
        internalRemoveMultiple(bucketDirectory);
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        LOGGER.debug("containerExists " + bucketDirectory);
        BucketPath bucketPath = amazonS3RootContainersBucket.appendName(bucketDirectory.getObjectHandle().getContainer());
        try {
            // Nicht schön hier mit Exceptions zu arbeiten, aber schneller als mit list
            connection.getObjectMetadata(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("createContainer " + bucketDirectory);

        if (!containerExists(bucketDirectory)) {
            BucketPath bucketPath = amazonS3RootContainersBucket.appendName(bucketDirectory.getObjectHandle().getContainer());

            byte[] content = "x".getBytes();
            LOGGER.debug("write " + bucketPath);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(content.length);
            ByteArrayInputStream bis = new ByteArrayInputStream(content);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath.getObjectHandle().getContainer(),
                    bucketPath.getObjectHandle().getName(),
                    bis, objectMetadata);
            PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
            // LOGGER.debug("write of stream for :" + bucketPath + " -> " + putObjectResult.toString());
        }
    }


    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("deleteContainer " + bucketDirectory);
        internalRemoveMultiple(new BucketDirectory(bucketDirectory.getObjectHandle().getContainer()));
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory abucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.debug("list " + abucketDirectory);
        List<StorageMetadata> returnList = new ArrayList<>();
        if (!containerExists(abucketDirectory)) {
            LOGGER.debug("return empty list for " + abucketDirectory);
            return returnList;
        }

        if (blobExists(new BucketPath(BucketPathUtil.getAsString(abucketDirectory)))) {
            // diese If-Abfrage dient dem Spezialfall, dass jemand einen BucketPath als BucketDirectory uebergeben hat.
            // Dann gibt es diesen bereits als file, dann muss eine leere Liste zurücgeben werden
            return new ArrayList<>();
        }

        BucketDirectory bucketDirectory = amazonS3RootBucket.append(abucketDirectory);

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
        if (LOGGER.isTraceEnabled()) {
            returnList.forEach(el -> LOGGER.trace("return for " + abucketDirectory + " :" + el.getName() + " type " + el.getType()));
        }
        return returnList;
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        LOGGER.debug("listAllBuckets");
        List<BucketDirectory> buckets = new ArrayList<>();
        ObjectListing ol = connection.listObjects(amazonS3RootContainersBucket.getObjectHandle().getContainer());
        ol.getObjectSummaries().forEach(bucket -> buckets.add(new BucketDirectory(bucket.getKey())));
        return buckets;
    }

    public void cleanDatabase() {
        LOGGER.warn("DELETE DATABASE");
        for (BucketDirectory bucketDirectory : listAllBuckets()) {
            deleteContainer(bucketDirectory);
        }
    }

    public void showDatabase() {
        try {
            ObjectListing ol = connection.listObjects(amazonS3RootBucket.getObjectHandle().getContainer());
            List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
            for (S3ObjectSummary key : ol.getObjectSummaries()) {
                LOGGER.debug(key.getKey());
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
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
                result.add(getStorageMetadata(new BucketPath(key)));
            } else {
                int numberOfDelimitersOfKey = StringUtils.countMatches(key, BucketPath.BUCKET_SEPARATOR);
                if (numberOfDelimitersOfKey == numberOfDelimitersExpected) {
                    result.add(getStorageMetadata(new BucketPath(key)));
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
            // Auch wenn kein file gefunden wurde, das Verzeichnis exisitiert und ist daher zurückzugeben
            dirs.add(prefix);
        }

        for (String dir : dirs) {
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.setType(StorageType.FOLDER);
            storageMetadata.setName(BucketPathUtil.getAsString(new BucketDirectory(new BucketPath(dir))));
            result.add(storageMetadata);
        }
        return result;
    }

    private void putBlobStreamWithMemory(BucketPath abucketPath, PayloadStream payloadStream, int size) {
        try {
            LOGGER.debug("putBlobStreamWithMemory " + abucketPath + " with known length " + size);
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl(payloadStream.getStorageMetadata());
            storageMetadata.setName(BucketPathUtil.getAsString(abucketPath));
            storageMetadata.setType(StorageType.BLOB);
            ObjectMetadata objectMetadata = geteObjectMetadataFromStorageMetadata(storageMetadata);
            objectMetadata.setContentLength(size);

            BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketPath.getObjectHandle().getContainer(),
                    bucketPath.getObjectHandle().getName(),
                    payloadStream.openStream(),
                    objectMetadata);
            PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
            // LOGGER.debug("write of stream for :" + bucketPath + " -> " + putObjectResult.toString());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void putBlobStreamWithTempFile(BucketPath abucketPath, PayloadStream payloadStream) {
        try {
            LOGGER.debug("putBlobStreamWithTempFile " + abucketPath + " to tmpfile with unknown size");
            InputStream is = payloadStream.openStream();
            File targetFile = File.createTempFile(AMAZONS3_TMP_FILE_PREFIX, AMAZONS3_TMP_FILE_SUFFIX);
            java.nio.file.Files.copy(
                    is,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(is);
            LOGGER.debug(abucketPath + " with tmpfile " + targetFile.getAbsolutePath() + " written with " + targetFile.length() + " bytes -> will now be copied to ceph");
            FileInputStream fis = new FileInputStream(targetFile);

            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl(payloadStream.getStorageMetadata());
            storageMetadata.setName(BucketPathUtil.getAsString(abucketPath));
            storageMetadata.setType(StorageType.BLOB);
            ObjectMetadata objectMetadata = geteObjectMetadataFromStorageMetadata(storageMetadata);
            objectMetadata.setContentLength(targetFile.length());

            BucketPath bucketPath = amazonS3RootBucket.append(abucketPath);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName(), fis, objectMetadata);
            PutObjectResult putObjectResult = connection.putObject(putObjectRequest);
            IOUtils.closeQuietly(fis);
            LOGGER.debug("stored " + bucketPath + " to ceph with size " + targetFile.length());
            targetFile.delete();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    // Ceph speichert die UserMetaData im header des Requests. Dadurch sind sie
    // - caseInsensitive
    // - längenbeschränkt
    // Abgesehen davon gibt es auch Probleme den JsonsString direkt zu übernehmen. Die Excpion beim Put verrät allerdings nicht,
    // was für Probleme das sind. Daher werden die Metadaten in einen lesbaren ByteCode umgewandelt.
    private ObjectMetadata geteObjectMetadataFromStorageMetadata(SimpleStorageMetadataImpl storageMetadata) {
        String metadataAsString = gsonHelper.toJson(storageMetadata);
        String metadataAsHexString = HexUtil.convertBytesToHexString(metadataAsString.getBytes());
        Map<String, String> userMetaData = new HashMap<>();
        userMetaData.put(STORAGE_METADATA_KEY, metadataAsHexString);
        int sizeOfMetadataHexString = metadataAsHexString.length();
        if (sizeOfMetadataHexString > AMAZON_S3_META_LIMIT) {
            throw new BaseException("Die Metadaten haben im HexFormat eine Länge von " + sizeOfMetadataHexString + ". Das Limit liegt aber bei " + AMAZON_S3_META_LIMIT + ". Der original String der Daten ist " + metadataAsString.length() + " Zeichen groß. Hier die Daten:" + metadataAsString);
        } else {
            LOGGER.debug("SIZE OF METADATA IS IN HEXFORMAT " + sizeOfMetadataHexString);
        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setUserMetadata(userMetaData);
        return objectMetadata;
    }

    private StorageMetadata getStorageMetadataFromObjectdata(ObjectMetadata objectMetadata, BucketPath bucketPath) {
        String metadataAsHexString = objectMetadata.getUserMetadata().get(STORAGE_METADATA_KEY);
        if (metadataAsHexString == null) {
            throw new BaseException("UserData do not contain mandatory " + STORAGE_METADATA_KEY + " for " + bucketPath);
/*
            LOGGER.error ("UserData do not contain mandatory " + STORAGE_METADATA_KEY + " for " + bucketPath);
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.setType(StorageType.BLOB);
            storageMetadata.setName(BucketPathUtil.getAsString(bucketPath));
            return storageMetadata;
            */
        }
        String metadataAsString = new String(HexUtil.convertHexStringToBytes(metadataAsHexString));
        return gsonHelper.fromJson(metadataAsString);
    }

    /**
     * Löscht alles unterhalb und einschließlich des genannten bucketDirectories
     *
     * @param abucketDirectory
     */
    private void internalRemoveMultiple(BucketDirectory abucketDirectory) {
        LOGGER.debug("internalRemoveMultiple " + abucketDirectory);
        if (!containerExists(abucketDirectory)) {
            return;
        }

        BucketDirectory bucketDirectory = amazonS3RootBucket.append(abucketDirectory);

        String container = bucketDirectory.getObjectHandle().getContainer();
        String prefix = bucketDirectory.getObjectHandle().getName();
        if (prefix == null) {
            prefix = "";
        }
        ObjectListing ol = connection.listObjects(container, prefix);
        if (ol.getObjectSummaries().isEmpty()) {
            LOGGER.debug("no files found in " + container + " with prefix " + prefix);
        }

        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for (S3ObjectSummary key : ol.getObjectSummaries()) {
            keys.add(new DeleteObjectsRequest.KeyVersion(key.getKey()));
            if (keys.size() == 100) {
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(container);
                deleteObjectsRequest.setKeys(keys);
                LOGGER.debug("DELETE CHUNK CONTENTS OF BUCKET " + container + " with " + keys.size() + " elements");
                DeleteObjectsResult deleteObjectsResult = connection.deleteObjects(deleteObjectsRequest);
                LOGGER.debug("SERVER CONFIRMED DELETION OF " + deleteObjectsResult.getDeletedObjects().size() + " elements");
                ObjectListing ol2 = connection.listObjects(container);
                LOGGER.debug("SERVER has remaining " + ol2.getObjectSummaries().size() + " elements");
                if (ol2.getObjectSummaries().size() == ol.getObjectSummaries().size()) {
                    throw new BaseException("Fatal error. Server confirmied deleltion of " + keys.size() + " elements, but still " + ol.getObjectSummaries().size() + " elementents in " + container);
                }
            }
        }
        if (!keys.isEmpty()) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(container);
            deleteObjectsRequest.setKeys(keys);
            LOGGER.debug("DELETE CONTENTS OF BUCKET " + container + " with " + keys.size() + " elements");
            DeleteObjectsResult deleteObjectsResult = connection.deleteObjects(deleteObjectsRequest);
            LOGGER.debug("SERVER CONFIRMED DELETION OF " + deleteObjectsResult.getDeletedObjects().size() + " elements");
        }
        if (abucketDirectory.getObjectHandle().getName() == null) {
            BucketPath containerFile = amazonS3RootContainersBucket.appendName(abucketDirectory.getObjectHandle().getContainer());
            connection.deleteObject(containerFile.getObjectHandle().getContainer(), containerFile.getObjectHandle().getName());
        }
    }
}
