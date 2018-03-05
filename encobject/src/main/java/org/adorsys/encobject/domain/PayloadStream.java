package org.adorsys.encobject.domain;

import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 08:33.
 */
public interface PayloadStream {
    /**
     * returns the inputstream of the data. The receiver is responsible for closing the stream
     */
    InputStream openStream();

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
