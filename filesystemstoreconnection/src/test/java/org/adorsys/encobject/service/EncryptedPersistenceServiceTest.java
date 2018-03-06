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

    @Test
    public void testVerySimpleEncryption() {
        EncryptionService encryptionService = new VerySimpleEncryptionService();
        String content = "Der Affe ist ein Affe und das bleibt auch so";
        byte[] encrypted = encryptionService.encrypt(content.getBytes(), null, null, null);
        byte[] decrypted = encryptionService.decrypt(encrypted, null);
        Assert.assertFalse(Arrays.equals(content.getBytes(), encrypted));
        Assert.assertTrue(Arrays.equals(content.getBytes(), decrypted));

    }

    @Test
    public void testWithLargeChunkSize() {
        try {
            ExtendedStoreConnection storageConnection = new FileSystemExtendedStorageConnection();
            EncryptionService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);
            BucketPath bucketPath = new BucketPath("folder1/file1");
            String content = "Der Affe ist ein Affe und das bleibt auch so";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersist(bucketPath, payLoadStream, null, null);
            Payload returnedPayload = service.loadAndDecrypt(bucketPath, null);
            LOGGER.debug("vorher:" + content);
            String nachher = new String(returnedPayload.getData());
            LOGGER.debug("nachher:" + nachher);
            Assert.assertEquals(content, nachher);
            PayloadStream returnedPayloadStream = service.loadAndDecryptStream(bucketPath, null);
            String nachherStream = new String(IOUtils.toByteArray(returnedPayloadStream.openStream()));
            LOGGER.debug("nachherStream:" + nachherStream);
            Assert.assertEquals(content, nachherStream);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    @Test
    public void testWithVerySmalChunkSize() {
        try {
            ExtendedStoreConnection storageConnection = new FileSystemExtendedStorageConnection();
            EncryptionService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);
            service.chunkSize = 4;
            BucketPath bucketPath = new BucketPath("folder1/file1");
            String content = "Der Affe ist ein Affe und das bleibt auch so";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersist(bucketPath, payLoadStream, null, null);
            Payload returnedPayload = service.loadAndDecrypt(bucketPath, null);
            LOGGER.debug("vorher:" + content);
            String nachher = new String(returnedPayload.getData());
            LOGGER.debug("nachher:" + nachher);
            Assert.assertEquals(content, nachher);
            PayloadStream returnedPayloadStream = service.loadAndDecryptStream(bucketPath, null);
            String nachherStream = new String(IOUtils.toByteArray(returnedPayloadStream.openStream()));
            LOGGER.debug("nachherStream:" + nachherStream);
            Assert.assertEquals(content, nachherStream);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test
    public void testWithASmalChunkSize() {
        try {
            ExtendedStoreConnection storageConnection = new FileSystemExtendedStorageConnection();
            EncryptionService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);
            service.chunkSize = 30;
            BucketPath bucketPath = new BucketPath("folder1/file1");
            String content = "Der Affe ist ein Affe und das bleibt auch so";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersist(bucketPath, payLoadStream, null, null);
            Payload returnedPayload = service.loadAndDecrypt(bucketPath, null);
            LOGGER.debug("vorher:" + content);
            String nachher = new String(returnedPayload.getData());
            LOGGER.debug("nachher:" + nachher);
            Assert.assertEquals(content, nachher);
            PayloadStream returnedPayloadStream = service.loadAndDecryptStream(bucketPath, null);
            String nachherStream = new String(IOUtils.toByteArray(returnedPayloadStream.openStream()));
            LOGGER.debug("nachherStream:" + nachherStream);
            Assert.assertEquals(content, nachherStream);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
