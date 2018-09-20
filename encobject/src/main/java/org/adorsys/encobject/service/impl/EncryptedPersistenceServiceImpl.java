package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.EncryptedPersistenceService;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by peter on 22.02.18 at 18:51.
 */
public class EncryptedPersistenceServiceImpl implements EncryptedPersistenceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptedPersistenceServiceImpl.class);
    private static final String ENCRYPTION_SERVICE = "EncryptedPersistenceServiceImpl.ENCRYPTION_SERVICE";
    private static final String ENCRYPTION_KEY_ID = "EncryptedPersistenceServiceImpl.ENCRYPTION_KEY_ID";
    private static final int MIN_SIZE_FOR_STREAMING = 100 * 1024 * 1024; // 100 MB

    ExtendedStoreConnection extendedStoreConnection;
    EncryptionStreamService encryptionStreamService;

    public EncryptedPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection, EncryptionStreamService encryptionStreamService) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.encryptionStreamService = encryptionStreamService;
    }

    @Override
    public void encryptAndPersist(BucketPath bucketPath, Payload payload, KeySource keySource, KeyID keyID) {
        try {
            InputStream inputStream = new ByteArrayInputStream(payload.getData());
            InputStream encryptedInputStream = encryptionStreamService.getEncryptedInputStream(inputStream, keySource, keyID, payload.getStorageMetadata().getShouldBeCompressed());
            payload.getStorageMetadata().getUserMetadata().put(ENCRYPTION_SERVICE, encryptionStreamService.getClass().toString());
            payload.getStorageMetadata().getUserMetadata().put(ENCRYPTION_KEY_ID, keyID.getValue());
            LOGGER.debug("ENCRYPT BYTES WITH " + keyID);


            // Hier kann nun anhand der Größe entschieden werden, ob weiter mit dem Bytestream gearbeitet werden soll, oder
            // die feste Größe benutzt wird. Letzteres hat im Falle von Minio den Vorteil, dass die Größe bekannt ist und daher keine
            // temporäre Datei benutzt werden muss.
            int length = payload.getData().length;
            if (length >= MIN_SIZE_FOR_STREAMING) {
                LOGGER.info("bytes " + length + "to write is larger or equal " + MIN_SIZE_FOR_STREAMING + " bytes, so payload will be encrypted with new payloadSTREAM");
                SimplePayloadStreamImpl newPayload = new SimplePayloadStreamImpl(payload.getStorageMetadata(), encryptedInputStream);
                extendedStoreConnection.putBlobStream(bucketPath, newPayload);
            } else {
                LOGGER.debug("bytes " + length + "to write is smaller " + MIN_SIZE_FOR_STREAMING + " bytes, so payload will encrypted in memory");
                byte[] encryptedBytes = IOUtils.toByteArray(encryptedInputStream);
                Payload encryptedPayload = new SimplePayloadImpl(payload.getStorageMetadata(), encryptedBytes);
                extendedStoreConnection.putBlob(bucketPath, encryptedPayload);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public Payload loadAndDecrypt(BucketPath bucketPath, KeySource keySource) {
        return loadAndDecrypt(bucketPath, keySource, null);
    }

    @Override
    public Payload loadAndDecrypt(BucketPath bucketPath, KeySource keySource, StorageMetadata storageMetadata) {
        try {
            Payload payload = extendedStoreConnection.getBlob(bucketPath, storageMetadata);
            String encryptionType = payload.getStorageMetadata().getUserMetadata().get(ENCRYPTION_SERVICE);
            payload.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_SERVICE);
            if (!encryptionType.equals(encryptionStreamService.getClass().toString())) {
                throw new BaseException("expected encryptionService of class " + encryptionType + " but was " + encryptionStreamService.getClass().toString());
            }
            String keyIDString = payload.getStorageMetadata().getUserMetadata().get(ENCRYPTION_KEY_ID);
            payload.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_KEY_ID);
            KeyID keyID = new KeyID(keyIDString);
            LOGGER.debug("DECRYPT BYTES WITH " + keyID);
            InputStream encryptedInputStream = new ByteArrayInputStream(payload.getData());
            InputStream decryptedInputStream = encryptionStreamService.getDecryptedInputStream(encryptedInputStream, keySource, keyID);
            byte[] decryptData = IOUtils.toByteArray(decryptedInputStream);

            return new SimplePayloadImpl(payload.getStorageMetadata(), decryptData);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void encryptAndPersistStream(BucketPath bucketPath, PayloadStream payloadStream, KeySource
            keySource, KeyID keyID) {
        payloadStream.getStorageMetadata().getUserMetadata().put(ENCRYPTION_SERVICE, encryptionStreamService.getClass().toString());
        payloadStream.getStorageMetadata().getUserMetadata().put(ENCRYPTION_KEY_ID, keyID.getValue());
        LOGGER.debug("ENCRYPT STREAM WITH " + keyID);
        InputStream encryptedStream = encryptionStreamService.getEncryptedInputStream(payloadStream.openStream(), keySource, keyID, payloadStream.getStorageMetadata().getShouldBeCompressed());
        extendedStoreConnection.putBlobStream(bucketPath, new SimplePayloadStreamImpl(payloadStream.getStorageMetadata(), encryptedStream));
    }

    @Override
    public PayloadStream loadAndDecryptStream(BucketPath bucketPath, KeySource keySource) {
        return loadAndDecryptStream(bucketPath, keySource, null);
    }

    @Override
    public PayloadStream loadAndDecryptStream(BucketPath bucketPath, KeySource keySource, StorageMetadata
            storageMetadata) {
        PayloadStream payloadStream = extendedStoreConnection.getBlobStream(bucketPath, storageMetadata);
        String encryptionType = payloadStream.getStorageMetadata().getUserMetadata().get(ENCRYPTION_SERVICE);
        payloadStream.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_SERVICE);
        if (!encryptionType.equals(encryptionStreamService.getClass().toString())) {
            throw new BaseException("expected encryptionService of class " + encryptionType + " but was " + encryptionStreamService.getClass().toString());
        }
        String keyIDString = payloadStream.getStorageMetadata().getUserMetadata().get(ENCRYPTION_KEY_ID);
        payloadStream.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_KEY_ID);
        KeyID keyID = new KeyID(keyIDString);
        LOGGER.debug("DECRYPT STREAM WITH " + keyID);

        InputStream encryptedInputStream = payloadStream.openStream();
        InputStream decryptedInputStream = encryptionStreamService.getDecryptedInputStream(encryptedInputStream, keySource, keyID);
        return new SimplePayloadStreamImpl(payloadStream.getStorageMetadata(), decryptedInputStream);
    }


}
