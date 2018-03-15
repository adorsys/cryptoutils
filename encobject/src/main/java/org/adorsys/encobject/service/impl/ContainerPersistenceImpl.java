package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;

/**
 * Service in charge of creating and deleting container
 * 
 * @author fpo
 *
 */
public class ContainerPersistenceImpl implements ContainerPersistence {
	
	private ExtendedStoreConnection blobStoreConnection;
	
	public ContainerPersistenceImpl(ExtendedStoreConnection storeConnection) {
		this.blobStoreConnection = storeConnection;
	}

	/**
	 * Creates a container in the blob store.
     *
	 * @param container container name
	 * @throws ContainerExistsException if a container with the same name already exists in the blob store.
	 */
	@Override
	public void createContainer(BucketDirectory container) throws ContainerExistsException {
		blobStoreConnection.createContainer(container);
	}
	
	/**
	 * Checks if a container with this name exists.
     *
     * @param container container name
     * @return true if a container with this name exists
	 */
	@Override
	public boolean containerExists(BucketDirectory container){
		return blobStoreConnection.containerExists(container);
	}
	
	/**
	 * Delete the container with the given name.
     *
     * @param container container name
	 * @throws UnknownContainerException when no container with given name exists
	 */
	@Override
	public void deleteContainer(BucketDirectory container) throws UnknownContainerException {
		blobStoreConnection.deleteContainer(container);
	}
}
