package org.adorsys.encobject.service;

/**
 * Service in charge of creating and deleting container
 * 
 * @author fpo
 *
 */
public class ContainerPersistence {
	
	private BlobStoreConnection blobStoreConnection;
	
	public ContainerPersistence(BlobStoreContextFactory blobStoreContextFactory) {
		this.blobStoreConnection = new BlobStoreConnection(blobStoreContextFactory);
	}

	/**
	 * Creates a container in the blob store. 
	 * 
	 * @throws ContainerExistsException if a container with the same name already exists in the blob store.
	 */
	public void creteContainer(String container) throws ContainerExistsException {
		blobStoreConnection.createContainer(container);
	}
	
	/**
	 * Checks if a container with this name exists.
	 */
	public boolean containerExists(String container){
		return blobStoreConnection.containerExists(container);
	}
	
	/**
	 * Delete the container with the given name.
	 */
	public void deleteContainer(String container) throws UnknownContainerException {
		blobStoreConnection.deleteContainer(container);
	}
}
