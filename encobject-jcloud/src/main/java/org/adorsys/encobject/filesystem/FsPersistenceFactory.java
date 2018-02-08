package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.serverdata.ServerObjectPersistence;
import org.adorsys.encobject.service.*;


public class FsPersistenceFactory {

    private KeystorePersistence keystorePersistence;
    private ExtendedStoreConnection extendedStoreConnection;
    private ContainerPersistence containerPersistence;
    private ServerObjectPersistence serverObjectPersistence;
    private EncObjectService encObjectService;

    public FsPersistenceFactory(String baseDir) {
    	extendedStoreConnection = new FileSystemExtendedStorageConnection();
    	keystorePersistence = new BlobStoreKeystorePersistence(extendedStoreConnection);

        containerPersistence = new ContainerPersistence(extendedStoreConnection);
    	serverObjectPersistence = new ServerObjectPersistence(extendedStoreConnection);
    	encObjectService = new EncObjectService(
    	        keystorePersistence,
                new ObjectPersistence(extendedStoreConnection),
                containerPersistence
        );
    }

    /*
    public BlobStoreContextFactory getBlobStoreContextFactory() {
        return blobStoreFactory;
    }
*/
    public KeystorePersistence getKeystorePersistence() {
        return keystorePersistence;
    }
    
    public ContainerPersistence getContainerPersistence() {
        return containerPersistence;
    }

    /*
	public BlobStoreContextFactory getBlobStoreFactory() {
		return blobStoreFactory;
	}
*/
	public ServerObjectPersistence getServerObjectPersistence() {
		return serverObjectPersistence;
	}

	public EncObjectService getEncObjectService() {
		return encObjectService;
	}

}
