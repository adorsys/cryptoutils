package org.adorsys.encobject.service.api;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.List;

public interface ExtendedStoreConnection {

    void putBlob(BucketPath bucketPath, Payload payload);
    Payload getBlob(BucketPath bucketPath);

    void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream);
    PayloadStream getBlobStream(BucketPath bucketPath);

    @Deprecated
    void putBlob(BucketPath bucketPath, byte[] bytes);


    StorageMetadata getStorageMetadata(BucketPath bucketPath);

    boolean blobExists(BucketPath bucketPath);

    void removeBlob(BucketPath bucketPath);

    void removeBlobs(Iterable<BucketPath> bucketPaths);

    long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag recursive);

    void createContainer(String container);

    boolean containerExists(String container);

    void deleteContainer(String container);

    List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag);
}
