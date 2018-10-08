package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.UserMetaData;
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

    private static final String ENCRYPTION_PROVIDER_KEY = "ENC_PROVIDER";
    private static final String ENCRYPTION_PROVIDER_VALUE = "BC";

    private static final String ENCRYPTION_CIPHER_ALGORITHM_KEY = "ENC_CIPHER_ALGORITHM";
    private static final String ENCRYPTION_CIPHER_ALGORITHM_VALUE = "AES/ECB/PKCS7Padding";

    private static final String ENCRYPTION_KEYGEN_ALGORITHM_KEY = "ENC_KEYGEN_ALGORITHM";
    private static final String ENCRYPTION_KEYGEN_ALGORITHM_VALUE = "AES";

    @Override
    public InputStream getEncryptedInputStream(UserMetaData userMetaData, InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        userMetaData.put(ENCRYPTION_PROVIDER_KEY, ENCRYPTION_PROVIDER_VALUE);
        userMetaData.put(ENCRYPTION_CIPHER_ALGORITHM_KEY, ENCRYPTION_CIPHER_ALGORITHM_VALUE);
        userMetaData.put(ENCRYPTION_KEYGEN_ALGORITHM_KEY, ENCRYPTION_KEYGEN_ALGORITHM_VALUE);

        Key key = keySource.readKey(keyID);
        CipherInputStream encryptionStream = createCipherInputStream(userMetaData, key.getEncoded(), inputStream, Cipher.ENCRYPT_MODE);
        return encryptionStream;
    }

    @Override
    public InputStream getDecryptedInputStream(UserMetaData userMetaData, InputStream inputStream, KeySource keySource, KeyID keyID) {
        Key key = keySource.readKey(keyID);
        CipherInputStream decryptionStream = createCipherInputStream(userMetaData, key.getEncoded(), inputStream, Cipher.DECRYPT_MODE);
        return decryptionStream;
    }


    private static CipherInputStream createCipherInputStream(UserMetaData userMetaData, byte[] secretKey,
                                                     InputStream original, int cipherMode) {

        Cipher cipher = initCipher(userMetaData, secretKey, cipherMode);
        CipherInputStream cis = new CipherInputStream(original, cipher);
        return cis;
    }

    private static CipherOutputStream createCipherOutputStream(UserMetaData userMetaData, byte[] secretKey,
                                                       OutputStream original, int cipherMode) {
        Cipher cipher = initCipher(userMetaData, secretKey, cipherMode);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(original, cipher);
        return cipherOutputStream;
    }

    private static Cipher initCipher(UserMetaData userMetaData, byte[] secretKey, int cipherMode) {
        String provider = userMetaData.get(ENCRYPTION_PROVIDER_KEY);
        String cipherAlgorithm = userMetaData.get(ENCRYPTION_CIPHER_ALGORITHM_KEY);
        String keygenAlgorithm = userMetaData.get(ENCRYPTION_KEYGEN_ALGORITHM_KEY);

        Cipher cipher;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, keygenAlgorithm);
            cipher = Cipher.getInstance(cipherAlgorithm, provider);
            cipher.init(cipherMode, keySpec);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
        return cipher;
    }

}
