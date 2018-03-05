package org.adorsys.encobject.domain;

public interface Payload {

	/**
	 * conveniance Method. Delivers the whole input stream, as long as its size
	 * is below the THRESH_HOLD
	 */
	byte[] getData();

	/**
	 * Returns whether the payload contains sensitive information. This is used
	 * when trying to decide whether to print out the payload information or not
	 * in logs
	 */
	boolean isSensitive();

	StorageMetadata getStorageMetadata();
}
