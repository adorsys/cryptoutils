package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.serverdata.ServerObjectPersistence;
import org.adorsys.encobject.service.*;


public class FsPersistenceFactory {

    private KeystorePersistence keystorePersistence;
    private BlobStoreContextFactory blobStoreFactory; 
    private ContainerPersistence containerPersistence;
    private ServerObjectPersistence serverObjectPersistence;
    private EncObjectService encObjectService;

    public FsPersistenceFactory(String baseDir) {
    	blobStoreFactory = new FsBlobStoreFactory(baseDir);
    	keystorePersistence = new BlobStoreKeystorePersistence(blobStoreFactory);
        BlobStoreConnection storeConnection = new BlobStoreConnection(blobStoreFactory);

        containerPersistence = new ContainerPersistence(storeConnection);
    	serverObjectPersistence = new ServerObjectPersistence(storeConnection);
    	encObjectService = new EncObjectService(
    	        keystorePersistence,
                new ObjectPersistence(storeConnection),
                containerPersistence
        );
    }

    public BlobStoreContextFactory getBlobStoreContextFactory() {
        return blobStoreFactory;
    }

    public KeystorePersistence getKeystorePersistence() {
        return keystorePersistence;
    }
    
    public ContainerPersistence getContainerPersistence() {
        return containerPersistence;
    }

	public BlobStoreContextFactory getBlobStoreFactory() {
		return blobStoreFactory;
	}

	public ServerObjectPersistence getServerObjectPersistence() {
		return serverObjectPersistence;
	}

	public EncObjectService getEncObjectService() {
		return encObjectService;
	}

}
