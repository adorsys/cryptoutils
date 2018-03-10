package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

/**
 * Created by peter on 08.03.18 at 19:16.
 */
public class AESEncryptionStreamServiceImpl implements EncryptionStreamService {

    private static final String aesKeyGenAlgoString = "AES";
    private static final String aesCypherAlgoString = "AES/ECB/PKCS7Padding";

    @Override
    public InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        Key key = keySource.readKey(keyID);
        CipherInputStream encryptionStream = createCipherInputStream(key.getEncoded(), inputStream, Cipher.ENCRYPT_MODE);
        return encryptionStream;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID) {
        Key key = keySource.readKey(keyID);
        CipherInputStream decryptionStream = createCipherInputStream(key.getEncoded(), inputStream, Cipher.DECRYPT_MODE);
        return decryptionStream;
    }


    public static CipherInputStream createCipherInputStream(byte[] secretKey,
                                                     InputStream original, int cipherMode) {

        Cipher cipher = initCipher(secretKey, cipherMode);
        CipherInputStream cis = new CipherInputStream(original, cipher);
        return cis;
    }

    public static CipherOutputStream createCipherOutputStream(byte[] secretKey,
                                                       OutputStream original, int cipherMode) {
        Cipher cipher = initCipher(secretKey, cipherMode);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(original, cipher);
        return cipherOutputStream;
    }

    private static Cipher initCipher(byte[] secretKey, int cipherMode) {
        Cipher cipher;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, aesKeyGenAlgoString);
            cipher = Cipher.getInstance(aesCypherAlgoString, "BC");
            cipher.init(cipherMode, keySpec);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
        return cipher;
    }

}
