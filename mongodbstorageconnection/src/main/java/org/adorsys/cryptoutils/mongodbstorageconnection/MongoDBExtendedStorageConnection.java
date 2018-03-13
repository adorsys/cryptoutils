package org.adorsys.cryptoutils.mongodbstorageconnection;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
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
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.regex;

/**
 * Created by peter on 12.03.18 at 18:53.
 */
public class MongoDBExtendedStorageConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(MongoDBExtendedStorageConnection.class);
    public static final String STORAGE_METADATA = "StorageMetadata";
    public static final String FILENAME = "filename";
    private MongoDatabase database;
    protected StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();


    public MongoDBExtendedStorageConnection(String databasename) {
        MongoClient mongoClient = new MongoClient();
        database = mongoClient.getDatabase(databasename);
    }

    public MongoDBExtendedStorageConnection() {
        this("default-database");
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        putBlobStream(bucketPath, new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), new ByteArrayInputStream(payload.getData())));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        try {
            PayloadStream blobStream = getBlobStream(bucketPath);
            return new SimplePayloadImpl(blobStream.getStorageMetadata(), IOUtils.toByteArray(blobStream.openStream()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        LOGGER.info("start putBlobStream for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        String filename = bucketPath.getObjectHandle().getName();

        GridFSUploadOptions uploadOptions = new GridFSUploadOptions();
        uploadOptions.metadata(new Document());
        SimpleStorageMetadataImpl storareMetaData = new SimpleStorageMetadataImpl(payloadStream.getStorageMetadata());
        storareMetaData.setType(StorageType.BLOB);
        storareMetaData.setName(BucketPathUtil.getAsString(bucketPath));
        uploadOptions.getMetadata().put(STORAGE_METADATA, gsonHelper.toJson(storareMetaData));
        InputStream is = payloadStream.openStream();
        ObjectId objectId = bucket.uploadFromStream(filename, is, uploadOptions);
        IOUtils.closeQuietly(is);
        deleteAllExcept(bucket, filename, objectId);

        LOGGER.info("finished putBlobStream for " + bucketPath);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        LOGGER.info("start getBlobStream for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        String filename = bucketPath.getObjectHandle().getName();

        GridFSDownloadStream fileStream = bucket.openDownloadStream(filename);
        PayloadStream payloadStream = new SimplePayloadStreamImpl(getStorageMetadata(bucketPath), fileStream);
        LOGGER.info("finished getBlobStream for " + bucketPath);
        return payloadStream;
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        String filename = bucketPath.getObjectHandle().getName();
        return getStorageMetadata(bucket, filename);
    }

    private StorageMetadata getStorageMetadata(GridFSBucket bucket, String filename) {
        List<ObjectId> list = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME, filename)).forEach((Consumer<GridFSFile>) file -> list.add(file.getObjectId()));
        if (list.isEmpty()) {
            throw new BaseException("file not found " + filename);
        }
        if (list.size() > 1) {
            throw new BaseException("more than on instance found of " + filename);
        }
        return getStorageMetadata(bucket, list.get(0));
    }

    private StorageMetadata getStorageMetadata(GridFSBucket bucket, ObjectId objectId) {
        throw new NYIException();
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        LOGGER.info("start blob Exists for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        String filename = bucketPath.getObjectHandle().getName();
        List<ObjectId> ids = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME, filename)).forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
        LOGGER.info("finished blob Exists for " + bucketPath);
        return !ids.isEmpty();
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        LOGGER.info("start removeBlob for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        String filename = bucketPath.getObjectHandle().getName();
        List<ObjectId> ids = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME, filename)).forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
        ids.forEach(id -> bucket.delete(id));
        LOGGER.info("finished removeBlob for " + bucketPath);
    }

    @Override
    public void removeBlobs(Iterable<BucketPath> bucketPaths) {
        bucketPaths.forEach(bucketPath -> removeBlob(bucketPath));
    }

    @Override
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag recursive) {
        return list(bucketDirectory, recursive).size();
    }

    @Override
    public void createContainer(String container) {
        GridFSBuckets.create(database, container);
    }

    @Override
    public boolean containerExists(String container) {
        return true;
    }

    @Override
    public void deleteContainer(String container) {
        GridFSBuckets.create(database, container).drop();

    }

    // =========================================================================================


    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.info("start list for " + bucketDirectory);
        GridFSBucket bucket = getGridFSBucket(bucketDirectory);
        String filename = bucketDirectory.getObjectHandle().getName();

        List<StorageMetadata> list = new ArrayList<>();
        List<ObjectId> ids = new ArrayList<>();
        if (listRecursiveFlag.equals(ListRecursiveFlag.TRUE)) {
            String pattern1 = filename + BucketPath.BUCKET_SEPARATOR + "*";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
        } else {
            // files only
            {
                String pattern1 = filename + BucketPath.BUCKET_SEPARATOR + "[^/]*$";
                GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
                gridFSFiles.forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
            }
            findSubdirs(bucket, bucketDirectory).forEach(dir -> {
                SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
                storageMetadata.setType(StorageType.FOLDER);
                storageMetadata.setName(dir);
                list.add(storageMetadata);
            });
        }
        ids.forEach((Consumer<ObjectId>) objectID -> {
            list.add(getStorageMetadata(bucket, objectID));
        });

        return list;
    }


    private GridFSBucket getGridFSBucket(BucketPath bucketPath) {
        return getGridFSBucket(bucketPath.getBucketDirectory());
    }

    private GridFSBucket getGridFSBucket(BucketDirectory bucketDirectory) {
        return GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer());
    }


    private void deleteAllExcept(GridFSBucket bucket, String filename, ObjectId objectID) {
        List<ObjectId> idsToDelete = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME, filename)).forEach((Consumer<GridFSFile>) file -> idsToDelete.add(file.getObjectId()));
        idsToDelete.forEach(id -> {
            if (objectID.equals(objectID)) {
                bucket.delete(id);
            }
        });
    }

    private Set<String> findSubdirs(GridFSBucket bucket, BucketDirectory bucketDirectory) {
        String prefix = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR;
        Set<String> dirsOnly = new HashSet<>();
        List<String> allFiles = new ArrayList<>();
        {
            // all files
            String pattern = prefix + "*";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> allFiles.add(file.getFilename()));
        }
        allFiles.forEach(filename -> {
            String remainder = filename.substring(prefix.length());
            int pos = remainder.indexOf(BucketPath.BUCKET_SEPARATOR);
            if (pos != -1) {
                String dirname = remainder.substring(0, pos);
                dirsOnly.add(BucketPathUtil.getAsString(bucketDirectory.appendDirectory(dirname)));

            }
        });
        return dirsOnly;
    }
}
