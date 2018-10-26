package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.KeyID;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 05.03.18 at 12:19.
 */
public class EncryptedPersistenceServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptedPersistenceServiceTest.class);
    public static Set<BucketDirectory> buckets = new HashSet<>();

    @Before
    public void before() {
        buckets.clear();
    }

    @After
    public void after() {
        try {
            ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
            ContainerPersistence containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
            for (BucketDirectory container : buckets) {
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + container);
                containerPersistence.deleteContainer(container);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }


    @Test
    public void testVerySimpleEncryption() {
        try {
            EncryptionStreamService encryptionService = new VerySimpleEncryptionService();
            byte[] content = getTrickyContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            UserMetaData userMetaData = new UserMetaData();
            byte[] encrypted = IOUtils.toByteArray(encryptionService.getEncryptedInputStream(userMetaData, inputStream, null, null, null));
            byte[] decrypted = IOUtils.toByteArray(encryptionService.getDecryptedInputStream(userMetaData, new ByteArrayInputStream(encrypted), null, null));
            Assert.assertFalse(Arrays.equals(content, encrypted));
            Assert.assertArrayEquals(content, decrypted);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    /**
     * every byte apperars AND a few times the -1 byte appears, this has the meaning of stream end.
     */
    byte[] getTrickyContent() {
        int RESULT_SIZE=2000;
        int BYTE_MIN=-128;
        int BYTE_MAX=127;

        byte[] result = new byte[RESULT_SIZE];
        byte byteValue = (byte) BYTE_MIN;
        for (int i = 0; i < RESULT_SIZE; i++) {
            result[i] = byteValue;
            if (i % 10 == 0) {
                result[i] = -1;
            } else {
                if (byteValue == BYTE_MAX) {
                    byteValue = (byte) BYTE_MIN;
                } else {
                    byteValue = (byte) (byteValue + 1);
                }
            }
            // LOGGER.info("i " + i + " byteValue " + result[i]);
        }
        return result;
    }

    @Test
    public void writeWithStreamAndLoadBytesAndStream() {
        try {
            ExtendedStoreConnection storageConnection = ExtendedStoreConnectionFactory.get();
            BucketPath bucketPath = new BucketPath("folder1/file1");
            storageConnection.createContainer(bucketPath.getBucketDirectory());
            buckets.add(bucketPath.getBucketDirectory());

            KeyID keyID = new KeyID("a not null keyid");
            EncryptionStreamService encryptionService = new VerySimpleEncryptionService();
            EncryptedPersistenceServiceImpl service = new EncryptedPersistenceServiceImpl(storageConnection, encryptionService);

            byte[] content = getTrickyContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            PayloadStream payLoadStream = new SimplePayloadStreamImpl(new SimpleStorageMetadataImpl(), inputStream);
            service.encryptAndPersistStream(bucketPath, payLoadStream, null, keyID);
            {
                Payload returnedPayload = service.loadAndDecrypt(bucketPath, null);
                byte[] readPayoad = returnedPayload.getData();
                Assert.assertTrue(Arrays.equals(content, readPayoad));
            }
            {
                PayloadStream returnedPayloadStream = service.loadAndDecryptStream(bucketPath, null);
                try (InputStream is = returnedPayloadStream.openStream()) {
                    byte[] readPayloadStream = IOUtils.toByteArray(is);
                    Assert.assertTrue(Arrays.equals(content, readPayloadStream));
                }
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
