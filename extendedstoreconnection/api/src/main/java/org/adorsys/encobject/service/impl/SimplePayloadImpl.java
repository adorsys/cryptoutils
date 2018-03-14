package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;

public class SimplePayloadImpl implements Payload {
    private SimpleStorageMetadataImpl storageMetadata = null;
    private Boolean sensitive = null;
    private byte[] data = null;

    public SimplePayloadImpl(byte[] data) {
        this(new SimpleStorageMetadataImpl(), data);
    }

    public SimplePayloadImpl(Payload payload) {
        this(payload.getStorageMetadata(), payload.isSensitive(), payload.getData());
    }

    public SimplePayloadImpl(StorageMetadata storageMetadata, byte[] data) {
        this(storageMetadata, true, data);
    }

    public SimplePayloadImpl(StorageMetadata storageMetadata, Boolean sensitive, byte[] data) {
        if (storageMetadata == null) {
            throw new BaseException("Programming error, storageMetaData must not be null");
        }
        if (sensitive == null) {
            throw new BaseException("Programming error, sensitve must not be null");
        }
        if (data == null || data.length == 0) {
            throw new BaseException("Programming error, data must not be null");
        }
        if (data == null || data.length < 1) {
            throw new BaseException("Programming error, size must not be null or < 1");
        }
        this.storageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
        this.sensitive = sensitive;
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public SimpleStorageMetadataImpl getStorageMetadata() {
        return storageMetadata;
    }

    public void setSensitive(boolean isSensitive) {
        sensitive = isSensitive;
    }

    @Override
    public boolean isSensitive() {
        return sensitive;
    }

}
