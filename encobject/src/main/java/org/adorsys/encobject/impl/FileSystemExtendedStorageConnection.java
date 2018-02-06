package org.adorsys.encobject.impl;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ContentInfoEntry;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.Map;

/**
 * Created by peter on 06.02.18 at 12:40.
 */
public class FileSystemExtendedStorageConnection implements ExtendedStoreConnection {
    @Override
    public void createContainer(String container) throws ContainerExistsException {

    }

    @Override
    public boolean containerExists(String container) {
        return false;
    }

    @Override
    public void deleteContainer(String container) throws UnknownContainerException {

    }

    @Override
    public void putBlob(ObjectHandle handle, byte[] bytes) throws UnknownContainerException {

    }

    @Override
    public byte[] getBlob(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException {
        return new byte[0];
    }

    @Override
    public boolean blobExists(ObjectHandle location) {
        return false;
    }

    @Override
    public PageSet<? extends StorageMetadata> list(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag) {
        return null;
    }

    @Override
    public String putBlob(BucketPath bucketPath, Payload payload) {
        return null;
    }

    @Override
    public Map<String, ContentInfoEntry> blobMetadata(BucketPath bucketPath) {
        return null;
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return null;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {

    }

    @Override
    public void removeBlobs(Iterable<BucketPath> bucketPaths) {

    }

    @Override
    public long countBlobs(BucketPath bucketPath, ListRecursiveFlag recursive) {
        return 0;
    }
}
