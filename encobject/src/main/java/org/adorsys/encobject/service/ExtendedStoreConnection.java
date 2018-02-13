package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ContentInfoEntry;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.Map;

public interface ExtendedStoreConnection {

	void putBlob(BucketPath bucketPath, Payload payload);

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

	void createContainer(String container);

	boolean containerExists(String container);

	void deleteContainer(String container);

	void putBlob(ObjectHandle handle, byte[] bytes);

	byte[] getBlob(ObjectHandle handle);

	boolean blobExists(ObjectHandle location);

	PageSet<? extends StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag);


}
