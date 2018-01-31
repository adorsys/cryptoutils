package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.types.ListRecursiveFlag;

public interface StoreConnection {

    void createContainer(String container) throws ContainerExistsException;

    boolean containerExists(String container);

    void deleteContainer(String container) throws UnknownContainerException;

    void putBlob(ObjectHandle handle, byte[] bytes) throws UnknownContainerException;

    byte[] getBlob(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException;
    
    boolean blobExists(ObjectHandle location);
    
    public PageSet<? extends StorageMetadata> list(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag);
    
}