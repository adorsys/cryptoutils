package org.adorsys.encobject.service;

import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.KeyID;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by peter on 05.03.18 at 12:19.
 */
public class EncryptedPersistenceServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptedPersistenceServiceTest.class);

    @Test
    public void testVerySimpleEncryption() {
        try {
            EncryptionStreamService encryptionService = new VerySimpleEncryptionService();
            byte[] content = getTrickyContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            byte[] encrypted = IOUtils.toByteArray(encryptionService.getEncryptedInputStream(inputStream, null, null, null));
            byte[] decrypted = IOUtils.toByteArray(encryptionService.getDecryptedInputStream(new ByteArrayInputStream(encrypted), null, null));
            Assert.assertFalse(Arrays.equals(content, encrypted));
            Assert.assertTrue(Arrays.equals(content, decrypted));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    byte[] getTrickyContent() {
        int size = 100;
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) i;
            if (i % 10 == 0) {
                result[i] = -1;
            }
        }
        return result;
    }

    @Test
    public void writeWithStreamAndLoadBytesAndStream() {
        try {
            String file = "folder1/file";
            KeyID keyID = new KeyID("a not null keyid");
            ExtendedStoreConnection storageConnection = new FileSystemExtendedStorageConnection();
            EncryptionStreamService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);
            BucketPath bucketPath = new BucketPath(file);
            byte[] content = getTrickyContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersistStream(bucketPath, payLoadStream, null, keyID);
            Payload returnedPayload = service.loadAndDecrypt(bucketPath, null);
            byte[] readPayoad = returnedPayload.getData();
            Assert.assertTrue(Arrays.equals(content, readPayoad));
            PayloadStream returnedPayloadStream = service.loadAndDecryptStream(bucketPath, null);
            byte[] readPayloadStream = IOUtils.toByteArray(returnedPayloadStream.openStream());
            Assert.assertTrue(Arrays.equals(content, readPayloadStream));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
