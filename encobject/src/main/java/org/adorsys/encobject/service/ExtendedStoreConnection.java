package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.types.ListRecursiveFlag;

public interface ExtendedStoreConnection {

    void putBlob(BucketPath bucketPath, Payload payload);

    void putBlob(BucketPath bucketPath, byte[] bytes);

    Payload getBlob(BucketPath bucketPath);

    BlobMetaInfo getBlobMetaInfo(BucketPath bucketPath);

    boolean blobExists(BucketPath bucketPath);

    void removeBlob(BucketPath bucketPath);

    void removeBlobs(Iterable<BucketPath> bucketPaths);

    long countBlobs(BucketPath bucketPath, ListRecursiveFlag recursive);

    void createContainer(String container);

    boolean containerExists(String container);

    void deleteContainer(String container);

    PageSet<? extends StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag);
}
