package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import io.minio.messages.DeleteError;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.exceptions.NYIException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.BucketException;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.service.impl.StoreConnectionListHelper;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 18.03.18 at 19:59.
 */
public class MinioExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(MinioExtendedStoreConnection.class);
    private final static String CONTENT_TYPE = "";
    private final static String METADATA_EXT = ".metadata.extension.";

    private final MinioClient minioClient;
    private final StorageMetadataFlattenerGSON storageMetadataFlattenerGSON = new StorageMetadataFlattenerGSON();

    public MinioExtendedStoreConnection(URL url, MinioAccessKey minioAccessKey, MinioSecretKey minioSecretKey) {
        try {
            this.minioClient = new MinioClient(url, minioAccessKey.getValue(), minioSecretKey.getValue());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        putBlobStream(bucketPath, new SimplePayloadStreamImpl(payload.getStorageMetadata(), new ByteArrayInputStream(payload.getData())));
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
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        try {
            StorageMetadata storageMetadata = getStorageMetadata(bucketPath);
            InputStream stream = minioClient.getObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
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
        try {
            LOGGER.debug("load metadata for " + bucketPath);
            InputStream is = minioClient.getObject(bucketPath.getObjectHandle().getContainer(), bucketPath.add(METADATA_EXT).getObjectHandle().getName());
            byte[] bytes = IOUtils.toByteArray(is);
            String jsonString = new String(bytes);
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
        String container = bucketPath.getObjectHandle().getContainer();
        String prefix = bucketPath.getObjectHandle().getName();
        ArrayList<String> list = new ArrayList<>();
        minioClient.listObjects(container, prefix, false).forEach(item -> {
                    try {
                        list.add(item.get().objectName());
                    } catch (Exception e) {
                        throw BaseExceptionHandler.handle(e);
                    }
                }
        );
        list.forEach(el -> LOGGER.debug("FOUND :" + el));
        boolean value = !list.isEmpty();
        LOGGER.info("blobExists:" + bucketPath + " -> " + value);
        return value;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        try {
            LOGGER.debug("removeBlob " + bucketPath);
            minioClient.removeObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            minioClient.removeObject(bucketPath.getObjectHandle().getContainer(), bucketPath.add(METADATA_EXT).getObjectHandle().getName());
            LOGGER.info("removeBlob done " + bucketPath);
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
    public void removeBlobs(Iterable<BucketPath> iterable) {
        iterable.forEach(bucketPath -> removeBlob(bucketPath));
    }

    @Override
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        throw new NYIException();
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        try {
            LOGGER.info("create container " + bucketDirectory);
            String container = bucketDirectory.getObjectHandle().getContainer();
            if (!minioClient.bucketExists(container)) {
                minioClient.makeBucket(container);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        try {
            return minioClient.bucketExists(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        LOGGER.info("deleteContainer " + bucketDirectory);
        try {
            List<String> objectNames = new ArrayList<>();
            minioClient.listObjects(bucketDirectory.getObjectHandle().getContainer()).forEach(el -> {
                try {
                    // LOGGER.info("container " + bucketDirectory + " contains: " + el.get().objectName());
                    objectNames.add(el.get().objectName());
                } catch (Exception e) {
                    throw BaseExceptionHandler.handle(e);
                }
            });

            LOGGER.info("delete " + objectNames.size() + " Elements of Container " + bucketDirectory);
            minioClient.removeObject(bucketDirectory.getObjectHandle().getContainer(), objectNames).forEach(error -> {
                try {
                    DeleteError de = error.get();
                    throw new BucketException("can not delete File " + de.objectName() + " of bucket " + bucketDirectory.getObjectHandle().getContainer() + " :" + de.message());
                } catch (Exception e) {
                    throw BaseExceptionHandler.handle(e);
                }
            });
            LOGGER.info("eventually delete empty Container " + bucketDirectory);
            minioClient.removeBucket(bucketDirectory.getObjectHandle().getContainer());
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
        String prefix = bucketDirectory.getObjectHandle().getName();
        if (prefix == null) {
            prefix = "";
        }
        List<BucketPath> bucketPaths = new ArrayList<>();
        LOGGER.debug("search in " + bucketDirectory + " with prefix " + prefix + " " + listRecursiveFlag);
        minioClient.listObjects(container, prefix, true).forEach(el -> {
            try {
                BucketPath bucketPath = new BucketPath(container, el.get().objectName());
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

    public void deleteAllBuckets() {
        try {
            LOGGER.info("******************************************************");
            LOGGER.info("DELETE ALL BUCKETS OF DATABASE - FOR TEST PURPOSE ONLY");
            minioClient.listBuckets().forEach(bucket -> deleteContainer(new BucketDirectory(bucket.name())));
            LOGGER.info("******************************************************");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    // ===============================================================================================================

    private void putBlobStreamWithMemory(BucketPath bucketPath, PayloadStream payloadStream) {
        try {
            storeMetadata(bucketPath, payloadStream.getStorageMetadata());
            byte[] bytes = IOUtils.toByteArray(payloadStream.openStream());
            minioClient.putObject(bucketPath.getObjectHandle().getContainer(),
                    bucketPath.getObjectHandle().getName(),
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    CONTENT_TYPE);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void putBlobStreamWithTempFile(BucketPath bucketPath, PayloadStream payloadStream) {
        throw new NYIException();
    }

    private void storeMetadata(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            LOGGER.debug("store metadata for " + bucketPath);
            SimpleStorageMetadataImpl metaData = new SimpleStorageMetadataImpl(storageMetadata);
            metaData.setType(StorageType.BLOB);
            metaData.setName(BucketPathUtil.getAsString(bucketPath));
            String jsonString = storageMetadataFlattenerGSON.toJson(metaData);
            byte[] bytes = jsonString.getBytes();
            InputStream is = new ByteArrayInputStream(bytes);
            minioClient.putObject(bucketPath.getObjectHandle().getContainer(),
                    bucketPath.add(METADATA_EXT).getObjectHandle().getName(),
                    is,
                    bytes.length,
                    CONTENT_TYPE);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }


}
