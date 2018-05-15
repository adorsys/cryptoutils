package org.adorsys.encobject.service;

import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.service.impl.AESEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.generator.SecretKeyGeneratorImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by peter on 08.03.18 at 16:13.
 */
public class EncryptionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptionTest.class);

    @Test
    public void a() {
        try {
            KeyIDWithKey t = createDocumentKeyIdWithKey();
            LOGGER.debug(t.toString());

            byte[] decrypted = "Der Affe ist ein Affe und das bleibt auch so".getBytes();
            InputStream decryptedStream = new ByteArrayInputStream(decrypted);
            CipherInputStream encryptionStream = AESEncryptionStreamServiceImpl.createCipherInputStream(t.getSecretKey().getEncoded(), decryptedStream, Cipher.ENCRYPT_MODE);
            byte[] encrypted = IOUtils.toByteArray(encryptionStream);

            InputStream encryptedInputStream = new ByteArrayInputStream(encrypted);
            CipherInputStream decryptionStream = AESEncryptionStreamServiceImpl.createCipherInputStream(t.getSecretKey().getEncoded(), encryptedInputStream, Cipher.DECRYPT_MODE);
            byte[] redecrypted = IOUtils.toByteArray(decryptionStream);

            LOGGER.debug("  decrypted : " + HexUtil.convertBytesToHexString(decrypted));
            LOGGER.debug("  encrypted : " + HexUtil.convertBytesToHexString(encrypted));
            LOGGER.debug("redecrypted : " + HexUtil.convertBytesToHexString(redecrypted));

            Assert.assertTrue(Arrays.equals(decrypted, redecrypted));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public KeyIDWithKey createDocumentKeyIdWithKey() {
        // Eine zufällige DocumentKeyID erzeugen
        KeyID documentKeyID = new KeyID("DK" + UUID.randomUUID().toString());

        // Für die DocumentKeyID einen DocumentKey erzeugen
        SecretKeyGeneratorImpl secretKeyGenerator = new SecretKeyGeneratorImpl("AES", 256);
        SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), null);
        SecretKey documentKey = secretKeyData.getSecretKey();
        return new KeyIDWithKey(documentKeyID, documentKey);
    }

    private static class KeyIDWithKey {
        private final KeyID keyID;
        private final SecretKey secretKey;

        public KeyIDWithKey(KeyID keyID, SecretKey secretKey) {
            this.keyID = keyID;
            this.secretKey = secretKey;
        }

        public KeyID getKeyID() {
            return keyID;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }

        @Override
        public String toString() {
            return "KeyIDWithKey{" +
                    "\n               keyID = " + keyID.getValue() +
                    "\n   secretKey.encoded = " + HexUtil.convertBytesToHexString(secretKey.getEncoded()) +
                    "\n secretKey.algorithm = " + secretKey.getAlgorithm() +
                    "\n  sesecretKey.format = " + secretKey.getFormat() +
                    "\n" +
                    '}';
        }
    }


}
