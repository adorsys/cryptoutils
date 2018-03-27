package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.service.impl.BlobStoreKeystorePersistenceImpl;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

public class KeystorePersistenceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeystorePersistenceTest.class);

    private static BucketDirectory container = new BucketDirectory("keystorepersistencetest/keystoredirectory");
    private static ExtendedStoreConnection extendedStoreConnection;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();
        extendedStoreConnection = ExtendedStoreConnectionFactory.get();
        keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
        containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);

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
        keystorePersistence.saveKeyStore(keystore, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), container.appendName(storeid).getObjectHandle());
        Assert.assertTrue(extendedStoreConnection.blobExists(container.appendName(storeid)));
    }

    @Test
    public void testLoadKeystore() {
        try {
            String storeid = "sampleKeyStorePersistence";
            char[] storePass = "aSimplePass".toCharArray();
            KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
            Assume.assumeNotNull(keystore);
            keystorePersistence.saveKeyStore(keystore, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), container.appendName(storeid).getObjectHandle());

            KeyStore loadedKeystore = null;
            loadedKeystore = keystorePersistence.loadKeystore(container.appendName(storeid).getObjectHandle(), TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build());
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
            String storeid = "sampleKeyStorePersistence";
            char[] storePass = "aSimplePass".toCharArray();
            KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
            Assume.assumeNotNull(keystore);
            UserMetaData attributes = new UserMetaData();
            attributes.put("a", "1");
            attributes.put("b", "2");
            attributes.put("c", "3");

            keystorePersistence.saveKeyStoreWithAttributes(keystore, attributes, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), container.appendName(storeid).getObjectHandle());

            KeyStore loadedKeystore = null;
            Map<String, String> keyStoreAttributes = null;

            Tuple<KeyStore, Map<String, String>> keyStoreMapTuple = keystorePersistence.loadKeystoreAndAttributes(container.appendName(storeid).getObjectHandle(), TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build());
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
