package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.types.KeyID;

/**
 * Created by peter on 22.02.18 at 18:51.
 */
public class EncryptedPersistenceService {
    public static final String ENCRYPTION_SERVICE = "ENCRYPTION_SERVICE";

    ExtendedStoreConnection extendedStoreConnection;
    EncryptionService encryptionService;

    public EncryptedPersistenceService(ExtendedStoreConnection extendedStoreConnection, EncryptionService encryptionService) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.encryptionService = encryptionService;
    }

    public void encryptAndPersist(BucketPath bucketPath, Payload payload, KeySource keySource, KeyID keyID) {
        byte[] encryptedData = encryptionService.encrypt(payload.getData(), keySource, keyID, payload.getStorageMetadata().getShouldBeCompressed());
        payload.getStorageMetadata().getUserMetadata().put(ENCRYPTION_SERVICE, encryptionService.getClass().toString());
        SimplePayloadImpl newPayload = new SimplePayloadImpl(payload.getStorageMetadata(), encryptedData);
        extendedStoreConnection.putBlob(bucketPath, newPayload);
    }

    public Payload loadAndDecrypt(BucketPath  bucketPath, KeySource keySource) {
        Payload payload = extendedStoreConnection.getBlob(bucketPath);
        String encryptionType = payload.getStorageMetadata().getUserMetadata().get(ENCRYPTION_SERVICE);
        if (!encryptionType.equals(encryptionService.getClass().toString())) {
            throw new BaseException("Expected encryptionType " + encryptionService.getClass().toString() + " but was " + encryptionType);
        }
        payload.getStorageMetadata().getUserMetadata().remove(ENCRYPTION_SERVICE);
        JWEncryptionService service = new JWEncryptionService();
        byte[] decryptData = service.decrypt(payload.getData(), keySource);
        return new SimplePayloadImpl(payload.getStorageMetadata(), decryptData);
    }
}