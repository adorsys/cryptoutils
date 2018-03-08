package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.plooh.core.encrypt.SecretKeyAlgoFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.InputStream;
import java.security.Key;

/**
 * Created by peter on 08.03.18 at 19:16.
 */
public class AESEncryptionStreamServiceImpl implements EncryptionStreamService {
    @Override
    public InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        Key key = keySource.readKey(keyID);
        CipherInputStream encryptionStream = SecretKeyAlgoFactory.AES_SECRET_KEY_ALGO.createCipherInputStream(key.getEncoded(), inputStream, Cipher.ENCRYPT_MODE);
        return encryptionStream;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID) {
        Key key = keySource.readKey(keyID);
        CipherInputStream decryptionStream = SecretKeyAlgoFactory.AES_SECRET_KEY_ALGO.createCipherInputStream(key.getEncoded(), inputStream, Cipher.DECRYPT_MODE);
        return decryptionStream;
    }
}
