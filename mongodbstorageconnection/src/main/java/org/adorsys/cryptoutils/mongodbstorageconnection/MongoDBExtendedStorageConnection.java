package org.adorsys.cryptoutils.mongodbstorageconnection;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.List;

/**
 * Created by peter on 12.03.18 at 18:53.
 */
public class MongoDBExtendedStorageConnection implements ExtendedStoreConnection {
    private MongoDatabase database;

    public MongoDBExtendedStorageConnection(String databasename) {
        MongoClient mongoClient = new MongoClient();
        database = mongoClient.getDatabase(databasename);
    }

    public MongoDBExtendedStorageConnection() {
        this("default-database");
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {

    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return null;
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {

    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        return null;
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {

    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        return null;
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        return false;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {

    }

    @Override
    public void removeBlobs(Iterable<BucketPath> bucketPaths) {

    }

    @Override
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag recursive) {
        return 0;
    }

    @Override
    public void createContainer(String container) {
        GridFSBuckets.create(database, container);
    }

    @Override
    public boolean containerExists(String container) {
        return false;
    }

    @Override
    public void deleteContainer(String container) {
        GridFSBuckets.create(database, container).drop();

    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        return null;
    }




}
