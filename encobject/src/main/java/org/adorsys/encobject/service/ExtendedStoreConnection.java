package org.adorsys.encobject.service;

import java.util.Map;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ContentInfoEntry;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.types.ListRecursiveFlag;

public interface ExtendedStoreConnection extends StoreConnection {

	/**
	 * Add the payload at the location specified in bucketPath
	 * 
	 * @param bucketPath
	 * @param payload
	 * 
	 * @return etag of the blob you uploaded, possibly null where etags are
	 *         unsupported
	 */
	String putBlob(BucketPath bucketPath, Payload payload);

	/**
	 * Retrieve meta information of the blob at the location {@link BucketPath}
	 * 
	 * @param bucketPath
	 * @return a Map with blob meta information
	 */
	Map<String, ContentInfoEntry> blobMetadata(BucketPath bucketPath);

	/**
	 * Retrieve the blob at the location {@link BucketPath}
	 * 
	 * @param bucketPath
	 * @return
	 */
	Payload getBlob(BucketPath bucketPath);

	/**
	 * Remove the blob at the location {@link BucketPath}
	 * 
	 * @param bucketPath
	 */
	void removeBlob(BucketPath bucketPath);

	void removeBlobs(Iterable<BucketPath> bucketPaths);

	long countBlobs(BucketPath bucketPath, ListRecursiveFlag recursive);

}
