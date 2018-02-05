package org.adorsys.encobject.domain;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Payload extends Closeable {

	/**
	 * Creates a new InputStream object of the payload.
	 */
	InputStream openStream() throws IOException;

	/**
	 * Payload in its original form.
	 */
	byte[] getData();

	/**
	 * Tells if the payload is capable of producing its data more than once.
	 */
	boolean isRepeatable();

	/**
	 * release resources used by this entity. This should be called when data is
	 * discarded.
	 */
	void release();

	/**
	 * Get The content meta info
	 * 
	 * @return
	 */
	Map<String, ContentInfoEntry> getMetaInfo();

	/**
	 * Set the content meta info.
	 * 
	 * @param metaInfo
	 */
	void setMetaInfo(Map<String, ContentInfoEntry> metaInfo);

	/**
	 * Sets whether the payload contains sensitive information. This is used
	 * when trying to decide whether to print out the payload information or not
	 * in logs
	 */
	void setSensitive(boolean isSensitive);

	/**
	 * Returns whether the payload contains sensitive information. This is used
	 * when trying to decide whether to print out the payload information or not
	 * in logs
	 */
	boolean isSensitive();

}
