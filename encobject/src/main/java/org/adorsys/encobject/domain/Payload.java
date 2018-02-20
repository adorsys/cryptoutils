package org.adorsys.encobject.domain;

import java.io.InputStream;

public interface Payload {

	/**
	 * returns the inputstream of the data. The receiver is responsible for closing the stream
	 */
	InputStream openStream();

	/**
	 * conveniance Method. Delivers the whole input stream, as long as its size
	 * is below the THRESH_HOLD
	 */
	byte[] getData();

	/**
	 * Tells if the stream is capable of producing its data more than once.
	 */
	boolean isRepeatable();

	/**
	 * Returns whether the payload contains sensitive information. This is used
	 * when trying to decide whether to print out the payload information or not
	 * in logs
	 */
	boolean isSensitive();

	StorageMetadata getStorageMetadata();
}
