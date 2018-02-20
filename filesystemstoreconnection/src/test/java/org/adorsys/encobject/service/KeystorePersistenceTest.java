package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class KeystorePersistenceTest {
    private static String container = KeystorePersistenceTest.class.getSimpleName();
    private static ExtendedStoreConnection extendedStoreConnection;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();
        extendedStoreConnection = new FileSystemExtendedStorageConnection();
        keystorePersistence = new BlobStoreKeystorePersistence(extendedStoreConnection);
        containerPersistence = new ContainerPersistence(extendedStoreConnection);

        try {
            containerPersistence.createContainer(container);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    @AfterClass
    public static void afterClass() {
        try {
            if (containerPersistence != null && containerPersistence.containerExists(container))
                containerPersistence.deleteContainer(container);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void testStoreKeystore() throws NoSuchAlgorithmException, CertificateException, UnknownContainerException {
        String storeid = "sampleKeyStorePersistence";
        char[] storePass = "aSimplePass".toCharArray();
        KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
        Assume.assumeNotNull(keystore);
        keystorePersistence.saveKeyStore(keystore, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), new ObjectHandle(container, storeid));
        Assert.assertTrue(extendedStoreConnection.blobExists(new BucketPath(container, storeid)));
    }

    @Test
    public void testLoadKeystore() {
        try {
            String container = "KeystorePersistenceTest";
            String storeid = "sampleKeyStorePersistence";
            char[] storePass = "aSimplePass".toCharArray();
            KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
            Assume.assumeNotNull(keystore);
            keystorePersistence.saveKeyStore(keystore, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), new ObjectHandle(container, storeid));

            KeyStore loadedKeystore = null;
            loadedKeystore = keystorePersistence.loadKeystore(new ObjectHandle(container, storeid), TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build());
            Assert.assertNotNull(loadedKeystore);
            Key key = null;
            key = loadedKeystore.getKey("mainKey", "aSimpleSecretPass".toCharArray());
            Assert.assertNotNull(key);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test
    public void testLoadKeystoreWithAttributes() {
        try {
            String container = "KeystorePersistenceTest";
            String storeid = "sampleKeyStorePersistence";
            char[] storePass = "aSimplePass".toCharArray();
            KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
            Assume.assumeNotNull(keystore);
            UserMetaData attributes = new UserMetaData();
            attributes.put("a", "1");
            attributes.put("b", "2");
            attributes.put("c", "3");

            keystorePersistence.saveKeyStoreWithAttributes(keystore, attributes, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), new ObjectHandle(container, storeid));

            KeyStore loadedKeystore = null;
            Map<String, String> keyStoreAttributes = null;

            Tuple<KeyStore, Map<String, String>> keyStoreMapTuple = keystorePersistence.loadKeystoreAndAttributes(new ObjectHandle(container, storeid), TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build());
            loadedKeystore = keyStoreMapTuple.getX();
            keyStoreAttributes = keyStoreMapTuple.getY();

            Assert.assertNotNull(loadedKeystore);
            Assert.assertNotNull(keyStoreAttributes);

            Assert.assertEquals(3, keyStoreAttributes.size());

            Key key = null;
            key = loadedKeystore.getKey("mainKey", "aSimpleSecretPass".toCharArray());
            Assert.assertNotNull(key);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

}
