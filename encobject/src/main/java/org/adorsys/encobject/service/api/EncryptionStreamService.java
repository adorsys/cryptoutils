package org.adorsys.encobject.service.api;

import org.adorsys.encobject.types.KeyID;

import java.io.InputStream;

/**
 * Created by peter on 07.03.18 at 09:35.
 */
public interface EncryptionStreamService {
    InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress);
    InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID);
}
