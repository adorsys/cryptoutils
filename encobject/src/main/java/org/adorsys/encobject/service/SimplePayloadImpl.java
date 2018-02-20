package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class SimplePayloadImpl implements Payload {
    public static final String SENSITIVE = "Sensitive";
    private long THREASH_HOLD = 2000;
    private StorageMetadata storageMetadata = null;
    private Boolean sensitive = null;
    private Boolean repeatable = null;

    // Entweder ein Stream oder Data
    private byte[] data = null;
    private InputStream inputStream = null;

    public SimplePayloadImpl(byte[] data) {
        this(new SimpleStorageMetadataImpl(), data);
    }

    public SimplePayloadImpl(StorageMetadata storageMetadata, byte[] data) {
        this(storageMetadata, true, false, data);
    }

    public SimplePayloadImpl(StorageMetadata storageMetadata, Boolean sensitive, Boolean repeatable, byte[] data) {
        if (storageMetadata == null) {
            throw new BaseException("Programming error, storageMetaData must not be null");
        }
        if (sensitive == null) {
            throw new BaseException("Programming error, sensitve must not be null");
        }
        if (repeatable == null) {
            throw new BaseException("Programming error, repeatable must not be null");
        }
        if (data == null || data.length == 0) {
            throw new BaseException("Programming error, data must not be null");
        }
        if (data == null || data.length < 1) {
            throw new BaseException("Programming error, size must not be null or < 1");
        }
        this.storageMetadata = storageMetadata;
        this.storageMetadata.setSize(new Long(data.length));
        this.sensitive = sensitive;
        this.repeatable = repeatable;
        this.inputStream = null;
        this.data = data;
    }

    @Override
    public InputStream openStream() {
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        return inputStream;
    }

    @Override
    public byte[] getData() {
        try {
            if (data != null) {
                return data;
            }
            Long size = storageMetadata.getSize();

            if (size.longValue() < THREASH_HOLD) {
                byte[] data = new byte[size.intValue()];

                int read = inputStream.read(data);
                if (read != size.intValue()) {
                    throw new BaseException("expected to read " + size + " bytes but read:" + read);
                }
                inputStream = null;
                return data;
            }
            throw new BaseException("Data size " + size + " is larger than threashhold " + THREASH_HOLD);

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public StorageMetadata getStorageMetadata() {
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
