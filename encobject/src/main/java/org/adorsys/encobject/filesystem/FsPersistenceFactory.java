package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.serverdata.ServerObjectPersistence;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.EncObjectService;
import org.adorsys.encobject.service.KeystorePersistence;


public class FsPersistenceFactory {

    private KeystorePersistence keystorePersistence;
    private BlobStoreContextFactory blobStoreFactory; 
    private ContainerPersistence containerPersistence;
    private ServerObjectPersistence serverObjectPersistence;
    private EncObjectService encObjectService;

    public FsPersistenceFactory(String baseDir) {
    	blobStoreFactory = new FsBlobStoreFactory(baseDir);
    	keystorePersistence = new KeystorePersistence(blobStoreFactory);
    	containerPersistence = new ContainerPersistence(blobStoreFactory);
    	serverObjectPersistence = new ServerObjectPersistence(blobStoreFactory);
    	encObjectService = new EncObjectService(blobStoreFactory);
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
