package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by peter on 08.03.18 at 20:03.
 */
public class JWEncryptionStreamServiceImpl implements EncryptionStreamService {
    private JWEncryptionServiceImpl jwEncryptionService = new JWEncryptionServiceImpl();

    @Override
    public InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        try {
            byte[] decrypted = IOUtils.toByteArray(inputStream);
            byte[] encrypted = jwEncryptionService.encrypt(decrypted, keySource, keyID, compress);
            return new ByteArrayInputStream(encrypted);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID) {
        try {
            byte[] encrypted = IOUtils.toByteArray(inputStream);
            byte[] decrypted = jwEncryptionService.decrypt(encrypted, keySource, keyID);
            return new ByteArrayInputStream(decrypted);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
