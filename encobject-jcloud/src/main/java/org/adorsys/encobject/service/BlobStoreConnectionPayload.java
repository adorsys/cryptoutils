package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.NYIException;
import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.Payload;

import java.io.IOException;
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
    public InputStream openStream() throws IOException {
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

    @Override
    public void release() {
        throw new NYIException();
    }

    @Override
    public BlobMetaInfo getBlobMetaInfo() {
        throw new NYIException();
    }

    @Override
    public void setBlobMetaInfo(BlobMetaInfo metaInfo) {
        throw new NYIException();
    }

    @Override
    public void setSensitive(boolean isSensitive) {
        throw new NYIException();
    }

    @Override
    public boolean isSensitive() {
        throw new NYIException();
    }

    @Override
    public void close() throws IOException {
        throw new NYIException();
    }
}
