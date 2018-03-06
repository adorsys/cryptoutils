package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.service.api.EncryptedPersistenceService;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Created by peter on 22.02.18 at 18:51.
 */
public class EncryptedPersistenceServiceImpl implements EncryptedPersistenceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptedPersistenceServiceImpl.class);
    private static final String ENCRYPTION_SERVICE = "EncryptedPersistenceServiceImpl.ENCRYPTION_SERVICE";
    private static final String CHUNKED_ENCRYPTION = "EncryptedPersistenceServiceImpl.CHUNKED_ENCRYPTION";
    private static final int GLOBAL_CHUNK_SIZE = 4096;
    public int chunkSize = GLOBAL_CHUNK_SIZE;

    ExtendedStoreConnection extendedStoreConnection;
    EncryptionService encryptionService;

    public EncryptedPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection, EncryptionService encryptionService) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.encryptionService = encryptionService;
    }

    @Override
    public void encryptAndPersist(BucketPath bucketPath, Payload payload, KeySource keySource, KeyID keyID) {
        byte[] encryptedData = encryptionService.encrypt(payload.getData(), keySource, keyID, payload.getStorageMetadata().getShouldBeCompressed());
        payload.getStorageMetadata().getUserMetadata().put(ENCRYPTION_SERVICE, encryptionService.getClass().toString());
        SimplePayloadImpl newPayload = new SimplePayloadImpl(payload.getStorageMetadata(), encryptedData);
        extendedStoreConnection.putBlob(bucketPath, newPayload);
    }

    @Override
    public void encryptAndPersist(BucketPath bucketPath, PayloadStream payloadStream, KeySource keySource, KeyID keyID) {
        payloadStream.getStorageMetadata().getUserMetadata().put(ENCRYPTION_SERVICE, encryptionService.getClass().toString());
        payloadStream.getStorageMetadata().getUserMetadata().put(CHUNKED_ENCRYPTION, "true");

        InputStream encryptedStream = new SimpleChunkedEncryptionInputStream(
                payloadStream.openStream(),
                encryptionService,
                chunkSize,
                keySource,
                keyID,
                payloadStream.getStorageMetadata().getShouldBeCompressed()
        );
        extendedStoreConnection.putBlobStream(bucketPath,new SimplePayloadStreamImpl(payloadStream.getStorageMetadata(), encryptedStream));
    }

    @Override
    public Payload loadAndDecrypt(BucketPath bucketPath, KeySource keySource) {
        Payload payload = extendedStoreConnection.getBlob(bucketPath);
        String encryptionType = payload.getStorageMetadata().getUserMetadata().get(ENCRYPTION_SERVICE);
        payload.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_SERVICE);
        if (encryptionType.equals(encryptionService.getClass().toString())) {
            String chunkedString = payload.getStorageMetadata().getUserMetadata().find(CHUNKED_ENCRYPTION);
            Boolean chunked = false;
            if (chunkedString != null) {
                payload.getStorageMetadata().getUserMetadata().remove(CHUNKED_ENCRYPTION);
                chunked = "TRUE".equals(chunkedString.toUpperCase());
            }
            LOGGER.info("chunked:" + chunked);

            byte[] decryptData = null;
            if (chunked) {
                String fullString = new String(payload.getData());
                StringTokenizer st = new StringTokenizer(fullString, SimpleChunkedEncryptionInputStream.DELIMITER_STRING);
                while (st.hasMoreElements()) {
                    byte[] bytes = st.nextToken().getBytes();
                    byte[] decryptDataChunk = encryptionService.decrypt(bytes, keySource);
                    decryptData = SimpleChunkedDecryptionInputStream.add(decryptData, decryptDataChunk);
                }
            } else {
                decryptData = encryptionService.decrypt(payload.getData(), keySource);
            }
            return new SimplePayloadImpl(payload.getStorageMetadata(), decryptData);
        } else {
            throw new BaseException("expected encryptionService of class " + encryptionType + " but was " + encryptionService.getClass().toString());
        }
    }

    @Override
    public PayloadStream loadAndDecryptStream(BucketPath bucketPath, KeySource keySource) {
        PayloadStream payloadStream = extendedStoreConnection.getBlobStream(bucketPath);
        String encryptionType = payloadStream.getStorageMetadata().getUserMetadata().get(ENCRYPTION_SERVICE);
        payloadStream.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_SERVICE);
        if (encryptionType.equals(encryptionService.getClass().toString())) {
            String chunkedString = payloadStream.getStorageMetadata().getUserMetadata().find(CHUNKED_ENCRYPTION);
            if (chunkedString != null) {
                payloadStream.getStorageMetadata().getUserMetadata().remove(CHUNKED_ENCRYPTION);
            }

            return new SimplePayloadStreamImpl(payloadStream.getStorageMetadata(), new SimpleChunkedDecryptionInputStream(payloadStream.openStream(), encryptionService, keySource));
        }
        throw new BaseException("expected encryptionService of class " + encryptionType + " but was " + encryptionService.getClass().toString());
    }


}