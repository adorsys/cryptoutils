package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.NYIException;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.UserMetaData;

import java.io.InputStream;

/**
 * Created by peter on 15.02.18 at 10:57.
 */
public class BlobStoreConnectionPayload implements Payload {

    private byte[] data;
    public BlobStoreConnectionPayload(byte[] data) {
        this.data = data;
    }
    @Override
    public InputStream openStream() {
        throw new NYIException();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public boolean isRepeatable() {
        throw new NYIException();
    }

    public StorageMetadata getStorageMetadata() {
        throw new NYIException();
    }

    public void setUserMetaData(UserMetaData metaInfo) {
        throw new NYIException();
    }

    @Override
    public boolean isSensitive() {
        throw new NYIException();
    }

}
