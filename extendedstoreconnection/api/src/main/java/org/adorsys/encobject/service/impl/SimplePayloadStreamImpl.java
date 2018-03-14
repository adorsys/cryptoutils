package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;

import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 08:30.
 */
public class SimplePayloadStreamImpl implements PayloadStream {
    private InputStream inputStream;
    private Boolean repeatable;
    private Boolean sensitive;
    private SimpleStorageMetadataImpl storageMetadata;

    public SimplePayloadStreamImpl(StorageMetadata storageMetadata, InputStream inputStream) {
        this(storageMetadata, false, true, inputStream);
    }

    public SimplePayloadStreamImpl(StorageMetadata storageMetadata, Boolean repeatable, Boolean sensitive, InputStream inputStream) {
        this.inputStream = inputStream;
        this.repeatable = repeatable;
        this.sensitive = sensitive;
        this.storageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
    }

    public SimplePayloadStreamImpl(PayloadStream payloadStream) {
        this(payloadStream.getStorageMetadata(), payloadStream.isRepeatable(), payloadStream.isSensitive(), payloadStream.openStream());
    }

    @Override
    public InputStream openStream() {
        return inputStream;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public boolean isSensitive() {
        return sensitive;
    }

    @Override
    public SimpleStorageMetadataImpl getStorageMetadata() {
        return storageMetadata;
    }


}
