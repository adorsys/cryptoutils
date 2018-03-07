package org.adorsys.encobject.service;

import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
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
    public static final int LARGE_CHUNK_SIZE = 1024*1024;
    public static final int VERY_SMALL_CHUNK_SIZE = 4;
    public static final int SMALL_CHUNK_SIZE = 30;

    @Test
    public void testVerySimpleEncryption() {
        EncryptionService encryptionService = new VerySimpleEncryptionService();
        byte[] content = getTrickyContent();
        byte[] encrypted = encryptionService.encrypt(content, null, null, null);
        byte[] decrypted = encryptionService.decrypt(encrypted, null);
        Assert.assertFalse(Arrays.equals(content, encrypted));
        Assert.assertTrue(Arrays.equals(content, decrypted));

    }

    @Test
    public void testWithLargeChunkSize() {
        writeWithStreamAndLoadBytesAndStream(LARGE_CHUNK_SIZE, "folder1/large");
    }

    @Test
    public void testWithASmalLChunkSize() {
        writeWithStreamAndLoadBytesAndStream(SMALL_CHUNK_SIZE, "folder1/small");

    }

    @Test
    public void testWithVerySmallChunkSize() {
        writeWithStreamAndLoadBytesAndStream(VERY_SMALL_CHUNK_SIZE, "folder1/verysmall");
    }


    byte[] getTrickyContent() {
        int size = 100;
        byte[] result = new byte[size];
        for (int i = 0; i < size ; i++) {
            result[i] = (byte) i;
            if (i % 10 == 0) {
                result[i] = -1;
            }
        }
        return result;
    }

    private void writeWithStreamAndLoadBytesAndStream(int chunkSize, String file) {
        try {
            ExtendedStoreConnection storageConnection = new FileSystemExtendedStorageConnection();
            EncryptionService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);
            service.chunkSize = chunkSize;
            BucketPath bucketPath = new BucketPath(file);
            byte[] content = getTrickyContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersistStream(bucketPath, payLoadStream, null, null);
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
