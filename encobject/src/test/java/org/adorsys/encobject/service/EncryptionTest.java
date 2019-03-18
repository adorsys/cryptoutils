package org.adorsys.encobject.service;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.impl.AESEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.generator.SecretKeyGeneratorImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by peter on 08.03.18 at 16:13.
 */
public class EncryptionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptionTest.class);

    @Test
    public void decryptionOK() {
        try {
            KeyIDWithKey t = createDocumentKeyIdWithKey();
            LOGGER.debug(t.toString());

            byte[] decrypted = "Der Affe ist ein Affe und das bleibt auch so".getBytes();
            InputStream decryptedStream = new ByteArrayInputStream(decrypted);
            UserMetaData userMetaData = new UserMetaData();
            AESEncryptionStreamServiceImpl aesEncryptionStreamService = new AESEncryptionStreamServiceImpl();
            KeySource keySource = new SimpleKeySource(t);

            InputStream encryptionStream = aesEncryptionStreamService.getEncryptedInputStream(userMetaData, decryptedStream, keySource, t.keyID, true);
            byte[] encrypted = IOUtils.toByteArray(encryptionStream);

            InputStream encryptedInputStream = new ByteArrayInputStream(encrypted);
            InputStream decryptionStream = aesEncryptionStreamService.getDecryptedInputStream(userMetaData, encryptedInputStream, keySource, t.keyID);
            byte[] redecrypted = IOUtils.toByteArray(decryptionStream);

            LOGGER.debug("  decrypted : " + HexUtil.convertBytesToHexString(decrypted));
            LOGGER.debug("  encrypted : " + HexUtil.convertBytesToHexString(encrypted));
            LOGGER.debug("redecrypted : " + HexUtil.convertBytesToHexString(redecrypted));

            Assert.assertTrue(Arrays.equals(decrypted, redecrypted));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void DOC_50_decryptionFailure() {
        try {
            KeyIDWithKey t = createDocumentKeyIdWithKey();
            LOGGER.debug(t.toString());

            byte[] decrypted = "Der Affe ist ein Affe und das bleibt auch so".getBytes();
            InputStream decryptedStream = new ByteArrayInputStream(decrypted);
            UserMetaData userMetaData = new UserMetaData();
            AESEncryptionStreamServiceImpl aesEncryptionStreamService = new AESEncryptionStreamServiceImpl();
            KeySource keySource = new SimpleKeySource(t);

            InputStream encryptionStream = aesEncryptionStreamService.getEncryptedInputStream(userMetaData, decryptedStream, keySource, t.keyID, true);
            byte[] encrypted = IOUtils.toByteArray(encryptionStream);

            userMetaData.remove("ENC_PROVIDER");
            userMetaData.put("ENC_PROVIDER","DC");

            InputStream encryptedInputStream = new ByteArrayInputStream(encrypted);
            CatchException.catchException(() -> aesEncryptionStreamService.getDecryptedInputStream(userMetaData, encryptedInputStream, keySource, t.keyID));
            Assert.assertNotNull(CatchException.caughtException());
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
            return "SecretKeyIDWithKey{" +
                    "\n               keyID = " + keyID.getValue() +
                    "\n   secretKey.encoded = " + HexUtil.convertBytesToHexString(secretKey.getEncoded()) +
                    "\n secretKey.algorithm = " + secretKey.getAlgorithm() +
                    "\n  sesecretKey.format = " + secretKey.getFormat() +
                    "\n" +
                    '}';
        }
    }

    public static class SimpleKeySource implements KeySource {
        private KeyIDWithKey keyIDWithKey;
        public SimpleKeySource(KeyIDWithKey keyIDWithKey) {
            this.keyIDWithKey = keyIDWithKey;
        }
        @Override
        public Key readKey(KeyID keyID) {
            Assert.assertEquals(keyID, keyIDWithKey.getKeyID());
            return keyIDWithKey.getSecretKey();
        }
    }


}
