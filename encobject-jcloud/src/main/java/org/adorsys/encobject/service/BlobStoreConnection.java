package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationImpl;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.PageSetImpl;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageMetadataImpl;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.types.BucketName;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides connection to the blob store. Particularly implements routine for opening
 * anc closing connection to the blob store.
 *
 * @author fpo
 */
public class BlobStoreConnection implements StoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlobStoreConnection.class);

    private BlobStoreContextFactory blobStoreContextFactory;

    public BlobStoreConnection(BlobStoreContextFactory blobStoreContextFactory) {
        this.blobStoreContextFactory = blobStoreContextFactory;
    }

    /**
     * Wenn der Container bereits exisitiert, wird das ignoriert.
     */
    @Override
    public void createContainer(String container) throws ContainerExistsException {
        BlobStoreContext blobStoreContext = this.blobStoreContextFactory.alocate();

        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            if (!blobStore.containerExists(container)) {
                blobStoreContext.getBlobStore().createContainerInLocation(null, objectHandle.getContainer());
            }
        } finally {
            this.blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    @Override
    public boolean containerExists(String container) {
        BlobStoreContext blobStoreContext = this.blobStoreContextFactory.alocate();
        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();

        boolean bucketExists = false;
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            bucketExists = blobStore.containerExists(objectHandle.getContainer());
        } finally {
            this.blobStoreContextFactory.dispose(blobStoreContext);
        }

        return bucketExists;
    }
    
    @Override
    public void deleteContainer(String container) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = this.blobStoreContextFactory.alocate();
        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();
        try {
            blobStoreContext.getBlobStore().deleteContainer(objectHandle.getContainer());
        } finally {
            this.blobStoreContextFactory.dispose(blobStoreContext);
        }
    }
    
    @Override
    public void putBlob(ObjectHandle handle, byte[] bytes) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();

            // add blob
            Blob blob = blobStore.blobBuilder(handle.getName())
                    .payload(bytes)
                    .build();
            blobStore.putBlob(handle.getContainer(), blob);
        } catch (ContainerNotFoundException ex) {
            throw new UnknownContainerException(handle.getContainer());
        } finally {
            blobStoreContextFactory.dispose(blobStoreContext);
        }
    }


    @Override
    public byte[] getBlob(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException {
        BlobStoreContext blobStoreContext = blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            Blob blob;
            try {
                blob = blobStore.getBlob(handle.getContainer(), handle.getName());
                if (blob == null) throw new ObjectNotFoundException(handle.getContainer() + "/" + handle.getName());
            } catch (ContainerNotFoundException e) {
                throw new UnknownContainerException(handle.getContainer());
            }

            try {
                return IOUtils.toByteArray(blob.getPayload().openStream());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        } finally {
            blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    @Override
    public PageSet<? extends StorageMetadata> list(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag) {
        BlobStoreContext blobStoreContext = this.blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            ListContainerOptions listContainerOptions = new ListContainerOptions();
            if (listRecursiveFlag == ListRecursiveFlag.TRUE) {
                listContainerOptions.recursive();
            }
            ObjectHandle objectHandle = bucketPath.getObjectHandle();
            if (objectHandle.getName() != null) {
                String prefix = objectHandle.getName() + BucketPath.BUCKET_SEPARATOR;
                listContainerOptions.prefix(prefix);
                if (listRecursiveFlag == ListRecursiveFlag.FALSE) {
                    listContainerOptions.delimiter(BucketName.BUCKET_SEPARATOR);
                }
            }

            LOGGER.debug("list container:" + objectHandle.getContainer() + " prefix:" + listContainerOptions.getPrefix() + " del:" + listContainerOptions.getDelimiter());
            org.jclouds.blobstore.domain.PageSet<? extends org.jclouds.blobstore.domain.StorageMetadata> ps = blobStore.list(objectHandle.getContainer(), listContainerOptions);
            LinkedHashSet<StorageMetadata> set = new LinkedHashSet<StorageMetadata>();
            for (org.jclouds.blobstore.domain.StorageMetadata s : ps) {
                StorageType type =  s.getType()==null?null:StorageType.valueOf(s.getType().name());
                Location location = copyLocation(new HashSet<String>(), s.getLocation());
                StorageMetadata e = new StorageMetadataImpl(type, s.getProviderId(),
                        s.getName(), location, s.getUri(), s.getETag(), s.getCreationDate(), s.getLastModified(),
                        s.getUserMetadata(), s.getSize());
                set.add(e);
            }
            return new PageSetImpl<>(set, ps.getNextMarker());

        } finally

        {
            this.blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    @Override
    public boolean blobExists(ObjectHandle location) {
        BlobStoreContext blobStoreContext = this.blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            LOGGER.debug("container:" + location.getContainer());
            LOGGER.debug("name     :" + location.getName());
            if (location.getContainer() == null) {
                LOGGER.warn("dont know how to check if container is null");
                return false;
            }
            return blobStore.blobExists(location.getContainer() != null ? location.getContainer() : "",
                    location.getName() != null ? location.getName() : "");
        } finally {
            this.blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    /*
    // other methods not override
    public Tuple<byte[], Map<String, String>> getBlobAndMetadata(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException {
        BlobStoreContext blobStoreContext = blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            Blob blob;
            try {
                blob = blobStore.getBlob(handle.getContainer(), handle.getName());
                if (blob == null) throw new ObjectNotFoundException(handle.getContainer() + "/" + handle.getName());
            } catch (ContainerNotFoundException e) {
                throw new UnknownContainerException(handle.getContainer());
            }

            try {
                byte[] bytes = IOUtils.toByteArray(blob.getPayload().openStream());
                Map<String, String> userMetadata = blob.getMetadata().getUserMetadata();

                return new Tuple<>(bytes, userMetadata);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        } finally {
            blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    public void deleteBlob(ObjectHandle handle) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();

            blobStore.removeBlob(handle.getContainer(), handle.getName());
        } catch (ContainerNotFoundException ex) {
            throw new UnknownContainerException(handle.getContainer());
        } finally {
            blobStoreContextFactory.dispose(blobStoreContext);
        }
    }

    public void putBlobWithMetadata(ObjectHandle handle, byte[] bytes, Map<String, String> userMetadata) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = blobStoreContextFactory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();

            Blob blob = blobStore.blobBuilder(handle.getName())
                    .payload(bytes)
                    .userMetadata(userMetadata)
                    .build();

            blobStore.putBlob(handle.getContainer(), blob);
        } catch (ContainerNotFoundException ex) {
            throw new UnknownContainerException(handle.getContainer());
        } finally {
            blobStoreContextFactory.dispose(blobStoreContext);
        }
    }
*/
    private Location copyLocation(Set<String> ids, org.jclouds.domain.Location l){
    	if(l==null) return null;
    	if(ids.contains(l.getId())) return null;
    	ids.add(l.getId());
		LocationScope scope = l.getScope()==null?null:LocationScope.valueOf(l.getScope().name());
		Location parent = copyLocation(ids, l.getParent());
		return new LocationImpl(scope, l.getId(), l.getDescription(), parent, l.getIso3166Codes(), l.getMetadata());    	
    }
}
