package org.adorsys.encobject.service;

import org.jclouds.blobstore.BlobStoreContext;

/**
 * Interface for looking up and disposing context.
 * @author fpo
 *
 */
public interface BlobStoreContextFactory {
	
	/**
	 * Provides a blob store context for use by the application. Using allocate and dispose can allow for pooling 
	 * of {@link BlobStoreContext}s
	 */
	public BlobStoreContext alocate();
	
	/**
	 * Disposes the {@link BlobStoreContext}
	 */
	public void dispose(BlobStoreContext context);
}
