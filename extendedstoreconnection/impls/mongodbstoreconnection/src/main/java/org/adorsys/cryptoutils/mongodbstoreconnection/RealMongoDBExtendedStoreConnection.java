package org.adorsys.cryptoutils.mongodbstoreconnection;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
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
import org.adorsys.encobject.exceptions.ResourceNotFoundException;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.service.impl.StoreConnectionListHelper;
import org.adorsys.encobject.types.ExtendedStoreConnectionType;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.connection.MongoURI;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.regex;

/**
 * Created by peter on 12.03.18 at 18:53.
 */
class RealMongoDBExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(RealMongoDBExtendedStoreConnection.class);
    private static final Logger SPECIAL_LOGGER = LoggerFactory.getLogger("SPECIAL_LOGGER");

    private static final String STORAGE_METADATA_KEY = "StorageMetadata";
    private static final String FILENAME_TAG = "filename";
    private static final String BUCKET_ID_FILENAME = ".bcd";

    private MongoDatabase database;
    private DB databaseDeprecated;
    protected StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();

    public RealMongoDBExtendedStoreConnection(MongoURI mongoURI) {
        MongoClientURI mongoClientUri = null;

        try {
            mongoClientUri = new MongoClientURI(mongoURI.getValue());
        }
        catch (Exception e) {
            throw new BaseException("can not parse:\"" + mongoURI.getValue() + "\" " + MongoParamParser.EXPECTED_PARAMS, e);
        }

        Frame frame = new Frame();
        frame.add("USE MONGO DB");
        frame.add("mongo db has be up and running");
        frame.add("uri: " + mongoURI.getValue());
        frame.add(" -> database:" + mongoClientUri.getDatabase());
        frame.add(" -> user:" + mongoClientUri.getUsername());
        mongoClientUri.getHosts().forEach(host -> {
                    frame.add(" -> host:" + host);
                }
        );
        frame.add(" -> ssl:" + mongoClientUri.getOptions().isSslEnabled());
        LOGGER.info(frame.toString());

        MongoClient mongoClient = new MongoClient(mongoClientUri);
        database = mongoClient.getDatabase(mongoClientUri.getDatabase());
        databaseDeprecated = mongoClient.getDB(mongoClientUri.getDatabase());
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        putBlobStream(bucketPath, new SimplePayloadStreamImpl(payload.getStorageMetadata(), new ByteArrayInputStream(payload.getData())));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return getBlob(bucketPath, null);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            PayloadStream blobStream = getBlobStream(bucketPath, storageMetadata);
            return new SimplePayloadImpl(blobStream.getStorageMetadata(), IOUtils.toByteArray(blobStream.openStream()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        LOGGER.debug("start putBlobStream for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        checkBucketExists(bucket);
        String filename = bucketPath.getObjectHandle().getName();

        GridFSUploadOptions uploadOptions = new GridFSUploadOptions();
        uploadOptions.metadata(new Document());
        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl(payloadStream.getStorageMetadata());
        storageMetadata.setType(StorageType.BLOB);
        storageMetadata.setName(BucketPathUtil.getAsString(bucketPath));
        uploadOptions.getMetadata().put(STORAGE_METADATA_KEY, gsonHelper.toJson(storageMetadata));
        InputStream is = payloadStream.openStream();
        ObjectId objectId = bucket.uploadFromStream(filename, is, uploadOptions);
        IOUtils.closeQuietly(is);
        deleteAllExcept(bucket, filename, objectId);

        LOGGER.debug("finished putBlobStream for " + bucketPath);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        return getBlobStream(bucketPath, null);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        LOGGER.debug("start getBlobStream for " + bucketPath);
        if (storageMetadata == null) {
            storageMetadata = getStorageMetadata(bucketPath);
        }
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        checkBucketExists(bucket);
        String filename = bucketPath.getObjectHandle().getName();

        GridFSDownloadOptions options = new GridFSDownloadOptions();
        GridFSDownloadStream fileStream = bucket.openDownloadStream(filename, options);
        PayloadStream payloadStream = new SimplePayloadStreamImpl(storageMetadata, fileStream);
        LOGGER.debug("finished getBlobStream for " + bucketPath);
        return payloadStream;
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        SPECIAL_LOGGER.debug("readmetadata " + bucketPath); // Dies LogZeile ist fuer den JUNIT-Tests StorageMetaDataTest
        LOGGER.debug("readmetadata " + bucketPath);

        GridFSBucket bucket = getGridFSBucket(bucketPath);
        checkBucketExists(bucket);
        GridFS gridFS = new GridFS(databaseDeprecated, bucketPath.getObjectHandle().getContainer());
        GridFSDBFile one = gridFS.findOne(bucketPath.getObjectHandle().getName());
        String jsonString = (String) one.getMetaData().get(STORAGE_METADATA_KEY);
        return gsonHelper.fromJson(jsonString);
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        LOGGER.debug("start blob Exists for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        if (!containerExists(bucket)) {
            return false;
        }
        String filename = bucketPath.getObjectHandle().getName();
        List<ObjectId> ids = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME_TAG, filename)).forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
        LOGGER.debug("finished blob Exists for " + bucketPath);
        return !ids.isEmpty();
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        LOGGER.debug("start removeBlob for " + bucketPath);
        GridFSBucket bucket = getGridFSBucket(bucketPath);
        checkBucketExists(bucket);
        String filename = bucketPath.getObjectHandle().getName();
        List<ObjectId> ids = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME_TAG, filename)).forEach((Consumer<GridFSFile>) file -> ids.add(file.getObjectId()));
        ids.forEach(id -> bucket.delete(id));
        LOGGER.debug("finished removeBlob for " + bucketPath);
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        LOGGER.debug("start removeBlobFolder for " + bucketDirectory);
        if (bucketDirectory.getObjectHandle().getName() == null) {
            throw new StorageConnectionException("not a valid bucket directory " + bucketDirectory);
        }
        GridFSBucket bucket = getGridFSBucket(bucketDirectory);
        String directoryname = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR;
        String pattern = "^" + directoryname + ".*";
        GridFSFindIterable list = bucket.find(regex(FILENAME_TAG, pattern, "i"));
        list.forEach((Consumer<GridFSFile>) file -> bucket.delete(file.getObjectId()));
        LOGGER.debug("finished removeBlobFolder for " + bucketDirectory);
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        LOGGER.debug("createContainer:" + bucketDirectory);
        GridFSBucket bucket = GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer());
        InputStream is = new ByteArrayInputStream(new Date().toString().getBytes());
        try {
            ObjectId objectId = bucket.uploadFromStream(BUCKET_ID_FILENAME, is);
            LOGGER.debug(" container file has been created " + BUCKET_ID_FILENAME + " with mongo id " + objectId.toString());
        } catch (MongoCommandException e) {
            if (e.getErrorMessage().contains("Too many open files")) {
                LOGGER.error("****************************************************");
                LOGGER.error("Due to the following \"Too many open files exception\"");
                LOGGER.error("PLEASE READ https://jira.adorsys.de/browse/DOC-22");
                LOGGER.error("****************************************************");
            }
            throw new BaseException("exception creating container for " + bucketDirectory, e);
        }
        IOUtils.closeQuietly(is);
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        GridFSBucket bucket = GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer());
        return containerExists(bucket);
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        BucketPathUtil.checkContainerName(bucketDirectory.getObjectHandle().getContainer());
        GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer()).drop();

    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.debug("start list for " + bucketDirectory);
        GridFSBucket bucket = getGridFSBucket(bucketDirectory);
        List<StorageMetadata> list = new ArrayList<>();
        if (!containerExists(bucket)) {
            LOGGER.debug("container " + bucket.getBucketName() + " existiert nicht, daher leere Liste");
            return list;
        }

        if (bucketDirectory.getObjectHandle().getName() != null) {
            if (bucket.find(Filters.eq(FILENAME_TAG, bucketDirectory.getObjectHandle().getName())).iterator().hasNext()) {
                // Spezialfall, das übergebene Directory ist eine Datei. In diesem Fall geben wir
                // eine leere Liste zurück
                return list;
            }
        }


        String directoryname = (bucketDirectory.getObjectHandle().getName() != null)
                ? bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR
                : "";

        List<BucketPath> bucketPaths = new ArrayList<>();
        Set<BucketDirectory> dirs = new HashSet<>();
        if (listRecursiveFlag.equals(ListRecursiveFlag.TRUE)) {
            String pattern = "^" + directoryname + ".*";
            GridFSFindIterable gridFSFiles = bucket.find(regex(FILENAME_TAG, pattern, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> bucketPaths.add(
                    new BucketPath(bucketDirectory.getObjectHandle().getContainer(), file.getFilename())));
            // bucketPaths.forEach(el -> LOGGER.info("found recursive:" + el));
            dirs.addAll(StoreConnectionListHelper.findAllSubDirs(bucketDirectory, bucketPaths));
        } else {
            // files only
            String pattern = "^" + directoryname + "[^/]*$";
            GridFSFindIterable gridFSFiles = bucket.find(regex(FILENAME_TAG, pattern, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> bucketPaths.add(
                    new BucketPath(bucketDirectory.getObjectHandle().getContainer(), file.getFilename())));
            // bucketPaths.forEach(el -> LOGGER.info("found non-recursive:" + el));

            dirs.addAll(findSubdirs(bucket, bucketDirectory));
        }

        bucketPaths.forEach(bucketPath -> {
            if (!bucketPath.getObjectHandle().getName().equals(BUCKET_ID_FILENAME)) {
                list.add(getStorageMetadata(bucketPath));
            }
        });

        dirs.add(bucketDirectory);
        dirs.forEach(dir -> {
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.setType(StorageType.FOLDER);
            storageMetadata.setName(BucketPathUtil.getAsString(dir));
            list.add(storageMetadata);
        });

        LOGGER.debug("list(" + bucketDirectory + ")");
        list.forEach(c -> LOGGER.debug(" > " + c.getName() + " " + c.getType()));
        return list;
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        List<BucketDirectory> list = new ArrayList<>();
        databaseDeprecated.getCollectionNames().forEach(el -> {
            if (el.endsWith(".files")) {
                String collectionName = el.substring(0, el.length() - ".files".length());
                list.add(new BucketDirectory(collectionName));
            }
        });
        return list;
    }

    @Override
    public ExtendedStoreConnectionType getType() {
        return ExtendedStoreConnectionType.MONGO;
    }


    // =========================================================================================


    private GridFSBucket getGridFSBucket(BucketPath bucketPath) {
        return GridFSBuckets.create(database, bucketPath.getObjectHandle().getContainer());
    }

    private GridFSBucket getGridFSBucket(BucketDirectory bucketDirectory) {
        return GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer());
    }


    private void deleteAllExcept(GridFSBucket bucket, String filename, ObjectId objectID) {
        List<ObjectId> idsToDelete = new ArrayList<>();
        bucket.find(Filters.eq(FILENAME_TAG, filename)).forEach((Consumer<GridFSFile>) file -> idsToDelete.add(file.getObjectId()));
        LOGGER.debug("****  number of files to delete:" + idsToDelete.size());
        idsToDelete.forEach(id -> {
            if (!id.equals(objectID)) {
                LOGGER.debug("****  delete:" + id);
                bucket.delete(id);
            }
        });
    }

    private Set<BucketDirectory> findSubdirs(GridFSBucket bucket, BucketDirectory bucketDirectory) {
        String prefix = (bucketDirectory.getObjectHandle().getName() != null)
                ? bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR
                : "";
        List<String> allFiles = new ArrayList<>();
        {
            // all files
            String pattern = "^" + prefix + ".*";
            GridFSFindIterable gridFSFiles = bucket.find(regex(FILENAME_TAG, pattern, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> allFiles.add(file.getFilename()));
        }
        Set<BucketDirectory> dirsOnly = new HashSet<>();
        allFiles.forEach(filename -> {
            if (filename.length() < prefix.length()) {
                // Absoluter Sonderfall. Z.B. es exisitiert a/b.txt und gesucht wurde mit a/b.txt
            } else {
                String remainder = filename.substring(prefix.length());
                int pos = remainder.indexOf(BucketPath.BUCKET_SEPARATOR);
                if (pos != -1) {
                    String dirname = remainder.substring(0, pos);
                    dirsOnly.add(bucketDirectory.appendDirectory(dirname));
                }
            }
        });
        return dirsOnly;
    }

    /*
    private Set<BucketDirectory> findAllSubDirs(List<String> filenames, BucketDirectory bucketDirectory) {
        Set<String> allDirs = new HashSet<>();
        filenames.forEach(filename -> {
            int last = filename.lastIndexOf(BucketPath.BUCKET_SEPARATOR);
            if (last != -1) {
                allDirs.add(filename.substring(0, last));
            }
        });
        Set<BucketDirectory> list = new HashSet<>();
        allDirs.forEach(dir -> {
            list.add(new BucketDirectory(bucketDirectory.getObjectHandle().getContainer() + BucketPath.BUCKET_SEPARATOR + dir));
        });
        return list;
    }
    */

    private boolean containerExists(GridFSBucket bucket) {
        return (bucket.find(Filters.eq(FILENAME_TAG, BUCKET_ID_FILENAME)).iterator().hasNext());
    }

    private void checkBucketExists(GridFSBucket bucket) {
        if (!containerExists(bucket)) {
            throw new ResourceNotFoundException("Container " + bucket.getBucketName() + " does not exist yet");
        }
    }

}
