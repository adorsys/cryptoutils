package org.adorsys.encobject.service.api;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.types.KeyID;

/**
 * Created by peter on 26.02.18 at 16:29.
 */
public interface EncryptedPersistenceService {
    void encryptAndPersist(BucketPath bucketPath, Payload payload, KeySource keySource, KeyID keyID);
    void encryptAndPersist(BucketPath bucketPath, PayloadStream payloadStream, KeySource keySource, KeyID keyID);
    Payload loadAndDecrypt(BucketPath bucketPath, KeySource keySource);
    PayloadStream loadAndDecryptStream(BucketPath bucketPath, KeySource keySource);
}
