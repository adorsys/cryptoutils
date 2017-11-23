package org.adorsys.encobject.service;

import org.adorsys.encobject.domain.ObjectHandle;

public interface StoreConnection {

    void createContainer(String container) throws ContainerExistsException;

    boolean containerExists(String container);

    void deleteContainer(String container) throws UnknownContainerException;

    void putBlob(ObjectHandle handle, byte[] bytes) throws UnknownContainerException;

    byte[] getBlob(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException;
}