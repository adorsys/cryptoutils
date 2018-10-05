package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.Frame;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.BucketException;
import org.adorsys.encobject.exceptions.ResourceNotFoundException;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.service.impl.StoreConnectionListHelper;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 18.03.18 at 19:59.
 */
class RealMinioExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(RealMinioExtendedStoreConnection.class);
    private static final Logger SPECIAL_LOGGER = LoggerFactory.getLogger("SPECIAL_LOGGER");

    private final static String CONTENT_TYPE = "";
    private final static String METADATA_EXT = ".metadata.extension.";
    private final static String MINIO_TMP_FILE_PREFIX = "MINIO_TMP_FILE_";
    private final static String MINIO_TMP_FILE_SUFFIX = "";
    private final BucketDirectory rootBucket;
    private final BucketDirectory containerBucket;
    public static final String CHARSET_NAME = "UTF-8";


    private final MinioClient minioClient;
    private final StorageMetadataFlattenerGSON storageMetadataFlattenerGSON = new StorageMetadataFlattenerGSON();

    public RealMinioExtendedStoreConnection(URL url, MinioAccessKey minioAccessKey, MinioSecretKey minioSecretKey, MinioRootBucketName rootBucketName) {
        Frame frame = new Frame();
        frame.add("USE MINIO SYSTEM");
        frame.add("(minio has be up and running )");
        frame.add("url: " + url.toString());
        frame.add("accessKey: " + minioAccessKey);
        frame.add("secretKey: " + minioSecretKey);
        frame.add("root: " + rootBucketName);
        LOGGER.info(frame.toString());

        try {
            rootBucket = new BucketDirectory(rootBucketName.getValue());
            containerBucket = new BucketDirectory(rootBucketName.getValue() + ".containers");
            this.minioClient = new MinioClient(url, minioAccessKey.getValue(), minioSecretKey.getValue());
            if (!minioClient.bucketExists(rootBucket.getObjectHandle().getContainer())) {
                LOGGER.debug("real bucket " + rootBucket + " wird angelegt ");
                minioClient.makeBucket(rootBucket.getObjectHandle().getContainer());
            } else {
                LOGGER.debug("real bucket " + rootBucket + " existiert bereits ");
            }
            if (!minioClient.bucketExists(containerBucket.getObjectHandle().getContainer())) {
                LOGGER.debug("container bucket " + containerBucket + " wird angelegt ");
                minioClient.makeBucket(containerBucket.getObjectHandle().getContainer());
            } else {
                LOGGER.debug("container bucket " + containerBucket + " existiert bereits ");
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        putBlobStreamWithMemory(bucketPath, new SimplePayloadStreamImpl(payload.getStorageMetadata(), new ByteArrayInputStream(payload.getData())), payload.getData().length);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        try {
            PayloadStream payloadStream = getBlobStream(bucketPath);
            return new SimplePayloadImpl(payloadStream.getStorageMetadata(), IOUtils.toByteArray(payloadStream.openStream()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            PayloadStream payloadStream = getBlobStream(bucketPath, storageMetadata);
            return new SimplePayloadImpl(payloadStream.getStorageMetadata(), IOUtils.toByteArray(payloadStream.openStream()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        putBlobStreamWithTempFile(bucketPath, payloadStream);
        storeMetadata(bucketPath, payloadStream.getStorageMetadata());
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        return getBlobStream(bucketPath, null);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            if (storageMetadata == null) {
                storageMetadata = getStorageMetadata(bucketPath);
            }
            InputStream stream = minioClient.getObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).getObjectHandle().getName());
            PayloadStream payloadStream = new SimplePayloadStreamImpl(storageMetadata, stream);
            return payloadStream;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        SPECIAL_LOGGER.debug("readmetadata " + bucketPath); // Dies LogZeile ist fuer den JUNIT-Tests StorageMetaDataTest
        LOGGER.debug("readmetadata " + bucketPath);
        try {
            if (!blobExists(bucketPath)) {
                throw new ResourceNotFoundException(bucketPath.toString());
            }
            LOGGER.debug("load metadata for " + bucketPath);
            InputStream is = minioClient.getObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).add(METADATA_EXT).getObjectHandle().getName());
            byte[] bytes = IOUtils.toByteArray(is);
            String jsonString = new String(bytes, CHARSET_NAME);
            StorageMetadata storageMetadata = storageMetadataFlattenerGSON.fromJson(jsonString);
            // LOGGER.debug("meta data for " + bucketPath + " is " + jsonString);
            return storageMetadata;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        LOGGER.debug("blobExists:" + bucketPath);
        String container = rootBucket.append(bucketPath).getObjectHandle().getContainer();
        String name = rootBucket.append(bucketPath).getObjectHandle().getName();
        try {
            minioClient.statObject(container, name);
            return true;
        } catch (Exception e) {
            // Exceptions for controll flow is not allowed. actually.
            // but the list method is too slow
        }
        return false;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        try {
            LOGGER.debug("removeBlob " + bucketPath);
            minioClient.removeObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).getObjectHandle().getName());
            minioClient.removeObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).add(METADATA_EXT).getObjectHandle().getName());
            LOGGER.debug("removeBlob done " + bucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        if (bucketDirectory.getObjectHandle().getName() == null) {
            throw new StorageConnectionException("not a valid bucket directory " + bucketDirectory);
        }
        List<StorageMetadata> list = list(bucketDirectory, ListRecursiveFlag.TRUE);
        list.forEach(metadata -> {
            if (metadata.getType().equals(StorageType.BLOB)) {
                removeBlob(new BucketPath(metadata.getName()));
            }
        });
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        try {
            LOGGER.debug("create container " + bucketDirectory);
            if (!containerExists(bucketDirectory)) {
                byte[] bytes = "X".getBytes();
                minioClient.putObject(
                        containerBucket.getObjectHandle().getContainer(),
                        containerBucket.appendName(bucketDirectory.getObjectHandle().getContainer()).getObjectHandle().getName(),
                        new ByteArrayInputStream(bytes),
                        bytes.length,
                        CONTENT_TYPE);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        try {
            LOGGER.debug("containerExists:" + bucketDirectory);
            ArrayList<String> list = new ArrayList<>();
            minioClient.listObjects(
                    containerBucket.getObjectHandle().getContainer(),
                    bucketDirectory.getObjectHandle().getContainer()).forEach(item -> {
                        try {
                            list.add(item.get().objectName());
                        } catch (Exception e) {
                            throw BaseExceptionHandler.handle(e);
                        }
                    }
            );
            boolean value = !list.isEmpty();
            LOGGER.debug("containerExists:" + bucketDirectory + " -> " + value);
            return value;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("deleteContainer " + bucketDirectory);
        try {
            List<String> objectNames = new ArrayList<>();
            minioClient.listObjects(
                    rootBucket.getObjectHandle().getContainer(),
                    rootBucket.append(bucketDirectory).getObjectHandle().getName()).forEach(el -> {
                try {
                    // LOGGER.debug("container " + bucketDirectory + " contains: " + el.get().objectName());
                    objectNames.add(el.get().objectName());
                } catch (Exception e) {
                    throw BaseExceptionHandler.handle(e);
                }
            });

            LOGGER.debug("delete " + objectNames.size() + " Elements of Container " + bucketDirectory);
            minioClient.removeObject(
                    rootBucket.getObjectHandle().getContainer(),
                    objectNames).forEach(error -> {
                try {
                    DeleteError de = error.get();
                    throw new BucketException("can not delete File " + de.objectName() + " of bucket "
                            + rootBucket.append(bucketDirectory).getObjectHandle().getContainer() + " :" + de.message());
                } catch (Exception e) {
                    throw BaseExceptionHandler.handle(e);
                }
            });
            minioClient.removeObject(
                    containerBucket.getObjectHandle().getContainer(),
                    bucketDirectory.getObjectHandle().getContainer());

            LOGGER.debug("eventually delete empty Container " + bucketDirectory);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        List<StorageMetadata> returnList = new ArrayList<>();
        if (!containerExists(bucketDirectory)) {
            return returnList;
        }
        String container = bucketDirectory.getObjectHandle().getContainer();
        LOGGER.debug("container ist hier:" + container);
        String directoryname = null;
        String prefix = bucketDirectory.getObjectHandle().getName();
        if (prefix == null) {
            prefix = "";
            directoryname = rootBucket.appendDirectory(container).getObjectHandle().getName();
        } else {
            directoryname = rootBucket.appendDirectory(container).appendDirectory(prefix).getObjectHandle().getName();
        }
        List<BucketPath> bucketPaths = new ArrayList<>();
        LOGGER.debug("search in " + bucketDirectory + " with prefix " + prefix + " " + listRecursiveFlag);
        LOGGER.debug("real search in " + rootBucket.appendDirectory(container).getObjectHandle().getContainer()
                + " with prefix " + directoryname + " true");
        minioClient.listObjects(
                rootBucket.appendDirectory(container).getObjectHandle().getContainer(),
                directoryname, true).forEach(el -> {
            try {
                String name = el.get().objectName();
                BucketPath bucketPath = new BucketPath(name);
                if (!bucketPath.getObjectHandle().getName().endsWith(METADATA_EXT)) {
                    LOGGER.debug("found: " + bucketPath);
                    bucketPaths.add(bucketPath);
                }
            } catch (Exception e) {
                throw BaseExceptionHandler.handle(e);
            }
        });
        if (bucketPaths.contains(new BucketPath(bucketDirectory.getObjectHandle().getContainer(), bucketDirectory.getObjectHandle().getName()))) {
            // die If-Abfrage dient dem Spezialfall, dass jemand einen BucketPath als BucketDirectory uebergeben hat.
            // Dann gibt es diesen bereits als file, dann muss eine leere Liste zurücgeben werden
            return returnList;
        }

        final String pref = prefix;
        LOGGER.debug("filter prefix ist " + pref);
        Set<BucketDirectory> bucketDirectories = StoreConnectionListHelper.findAllSubDirs(bucketPaths);
        bucketDirectories.add(bucketDirectory);
        if (listRecursiveFlag.equals(ListRecursiveFlag.FALSE)) {
            bucketDirectories.removeIf(bucketSubdir -> {
                String name = bucketSubdir.getObjectHandle().getName();
                if (name == null) {
                    LOGGER.debug("filter bucketDirectory " + bucketSubdir + " -> false");
                    return false;
                }
                if (!name.startsWith(pref)) {
                    throw new BaseException("expected " + name + " to start with " + pref);
                }
                String remainder = name.substring(pref.length());
                int firstSlash = remainder.indexOf(BucketPath.BUCKET_SEPARATOR);
                boolean filterMeOut = false;
                if (pref.length() == 0) {
                    filterMeOut = firstSlash != -1;
                }
                if (!filterMeOut && firstSlash != -1) {
                    int secondSlash = remainder.indexOf(BucketPath.BUCKET_SEPARATOR, firstSlash + 1);
                    filterMeOut = (secondSlash != -1);
                }
                LOGGER.debug("filter bucketDirectory " + bucketSubdir + " " + remainder + " -> " + filterMeOut);
                return filterMeOut;
            });
        }
        bucketDirectories.forEach(bucketSubdir -> {
            SimpleStorageMetadataImpl metadata = new SimpleStorageMetadataImpl();
            metadata.setType(StorageType.FOLDER);
            metadata.setName(BucketPathUtil.getAsString(bucketSubdir));
            returnList.add(metadata);
        });

        if (listRecursiveFlag.equals(ListRecursiveFlag.FALSE)) {
            bucketPaths.removeIf(bucketPath -> {
                        String name = bucketPath.getObjectHandle().getName();
                        if (!name.startsWith(pref)) {
                            throw new BaseException("expected " + name + " to start with " + pref);
                        }
                        String remainder = name.substring(pref.length() + 1);

                        int index = remainder.indexOf(BucketPath.BUCKET_SEPARATOR);
                        boolean filterMeOut = index != -1;
                        LOGGER.debug("filter bucketPath " + bucketPath + " " + remainder + " -> " + filterMeOut);
                        return filterMeOut;
                    }
            );
        }
        bucketPaths.forEach(bucketPath -> returnList.add(getStorageMetadata(bucketPath)));

        LOGGER.debug("List returns");
        returnList.forEach(metaData -> {
            LOGGER.debug(metaData.getName() + " " + metaData.getType());
        });
        return returnList;
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        try {
            List<BucketDirectory> list = new ArrayList<>();
            Iterator<Result<Item>> iterator = minioClient.listObjects(containerBucket.getObjectHandle().getContainer()).iterator();
            while (iterator.hasNext()) {
                list.add(new BucketDirectory(iterator.next().get().objectName()));
            }
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public void cleanDatabase() {
        try {
            Iterator<Bucket> iterator = minioClient.listBuckets().iterator();
            while (iterator.hasNext()) {
                String realBucketName = iterator.next().name();
                Iterator<Result<Item>> iterator1 = minioClient.listObjects(realBucketName).iterator();
                while (iterator1.hasNext()) {
                    Result<Item> el = iterator1.next();
                    LOGGER.debug("remove " + realBucketName + "->" + el.get().objectName());
                    minioClient.removeObject(realBucketName, el.get().objectName());
                }
                if (!realBucketName.equals(rootBucket.getObjectHandle().getContainer()) &&
                        !realBucketName.equals(containerBucket.getObjectHandle().getContainer())) {
                    LOGGER.debug("remove bucket " + realBucketName);
                    minioClient.removeBucket(realBucketName);
                }
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    public void showDatabase() {
        try {
            Iterator<Bucket> iterator = minioClient.listBuckets().iterator();
            while (iterator.hasNext()) {
                String realBucketName = iterator.next().name();
                Iterator<Result<Item>> iterator1 = minioClient.listObjects(realBucketName).iterator();
                while (iterator1.hasNext()) {
                    Result<Item> el = iterator1.next();
                    LOGGER.debug("found " + realBucketName + "->" + el.get().objectName());
                }
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    // ===============================================================================================================

    private void putBlobStreamWithMemory(BucketPath bucketPath, PayloadStream payloadStream, int size) {
        LOGGER.debug("store to minio with known size of " + size);
        try {
            storeMetadata(bucketPath, payloadStream.getStorageMetadata());
            byte[] bytes = IOUtils.toByteArray(payloadStream.openStream());
            minioClient.putObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).getObjectHandle().getName(),
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    CONTENT_TYPE);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void putBlobStreamWithTempFile(BucketPath bucketPath, PayloadStream payloadStream) {
        try {
            LOGGER.debug("store " + bucketPath + " to tmpfile with unknown size");
            InputStream is = payloadStream.openStream();
            File targetFile = File.createTempFile(MINIO_TMP_FILE_PREFIX, MINIO_TMP_FILE_SUFFIX);
            java.nio.file.Files.copy(
                    is,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(is);
            LOGGER.debug(bucketPath + " with tmpfile " + targetFile.getAbsolutePath() + " written with " + targetFile.length() + " bytes -> will now be copied to minio");
            FileInputStream fis = new FileInputStream(targetFile);
            minioClient.putObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).getObjectHandle().getName(),
                    fis,
                    targetFile.length(),
                    CONTENT_TYPE);
            IOUtils.closeQuietly(fis);
            LOGGER.debug("stored " + bucketPath + " to minio with size " + targetFile.length());
            targetFile.delete();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void storeMetadata(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            LOGGER.debug("store metadata for " + bucketPath);
            SimpleStorageMetadataImpl metaData = new SimpleStorageMetadataImpl(storageMetadata);
            metaData.setType(StorageType.BLOB);
            metaData.setName(BucketPathUtil.getAsString(bucketPath));
            String jsonString = storageMetadataFlattenerGSON.toJson(metaData);
            byte[] bytes = jsonString.getBytes(CHARSET_NAME);
            InputStream is = new ByteArrayInputStream(bytes);
            minioClient.putObject(
                    rootBucket.append(bucketPath).getObjectHandle().getContainer(),
                    rootBucket.append(bucketPath).add(METADATA_EXT).getObjectHandle().getName(),
                    is,
                    bytes.length,
                    CONTENT_TYPE);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }


}
