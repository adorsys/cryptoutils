package org.adorsys.encobject.service;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.ObjectNotFoundException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.exceptions.WrongKeyCredentialException;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.utils.TestFileSystemExtendedStorageConnection;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.jjwk.exceptions.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.exceptions.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.security.auth.callback.CallbackHandler;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.UUID;

public class JWEPersistenceTest {

    private static String container = JWEPersistenceTest.class.getSimpleName();
    private static TestFileSystemExtendedStorageConnection extendedStoreConnection;
    private static JWEPersistence jwePersistence;
    private static ContainerPersistence containerPersistence;
    private static KeyStore keyStore;
    private static CallbackHandler secretKeyPassHandler;
    private static String secretKeyAlias = "mainKey";
    private static EncryptionParams encParams;

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();
        extendedStoreConnection = new TestFileSystemExtendedStorageConnection();
        jwePersistence = new JWEPersistence(extendedStoreConnection);
        containerPersistence = new ContainerPersistence(extendedStoreConnection);

        try {
            containerPersistence.createContainer(container);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

        char[] secretKeyPass = "aSimpleSecretPass".toCharArray();
        String storeAlias = "mainKeyStore";
        char[] storePass = "aSimpleStorePass".toCharArray();
        keyStore = TestKeyUtils.testSecretKeystore(storeAlias, storePass, secretKeyAlias, secretKeyPass);
        Assume.assumeNotNull(keyStore);
        secretKeyPassHandler = new PasswordCallbackHandler(secretKeyPass);
        encParams = new EncryptionParams.Builder()
                .setEncAlgo(JWEAlgorithm.A256GCMKW)
                .setEncMethod(EncryptionMethod.A256GCM).build();

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
    public void testStoreObject() throws UnsupportedEncAlgorithmException, UnsupportedEncodingException, WrongKeyCredentialException, UnsupportedKeyLengthException, UnknownContainerException {
        byte[] data = "This is a sample String to be stored.".getBytes("UTF-8");
        ContentMetaInfo metaIno = null;
        String name = UUID.randomUUID().toString();
        ObjectHandle handle = new ObjectHandle(container, name);
        jwePersistence.storeObject(data, metaIno, handle, keyStore, secretKeyAlias, secretKeyPassHandler, encParams);
        Assert.assertTrue(extendedStoreConnection.existsOnFs(container, name));
    }

    @Test
    public void testLoadObjectInfo() throws UnknownContainerException {
        String dataStr = "This is a sample String to be stored.";
        ContentMetaInfo metaIno = null;
        String name = UUID.randomUUID().toString();
        ObjectHandle handle = new ObjectHandle(container, name);
        try {
            jwePersistence.storeObject(dataStr.getBytes("UTF-8"), metaIno, handle, keyStore, secretKeyAlias, secretKeyPassHandler, encParams);
        } catch (UnsupportedEncodingException | UnsupportedEncAlgorithmException | WrongKeyCredentialException | UnsupportedKeyLengthException e) {
            Assume.assumeNoException(e);
        }
        Assume.assumeTrue(extendedStoreConnection.existsOnFs(container, name));

        try {
            byte[] bs = jwePersistence.loadObject(new ObjectHandle(container, name), keyStore, secretKeyPassHandler);
            String byteStr = new String(bs, "UTF-8");
            Assert.assertEquals(dataStr, byteStr);
        } catch (ObjectNotFoundException | WrongKeyCredentialException | UnsupportedEncodingException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    @Test(expected = WrongKeyCredentialException.class)
    public void testLoadObjectInfoWrongKey() {
        String dataStr = "This is a sample String to be stored.";
        ContentMetaInfo metaIno = null;
        String name = UUID.randomUUID().toString();
        ObjectHandle handle = new ObjectHandle(container, name);
        try {
            jwePersistence.storeObject(dataStr.getBytes("UTF-8"), metaIno, handle, keyStore, secretKeyAlias, secretKeyPassHandler, encParams);
        } catch (UnsupportedEncodingException | UnsupportedEncAlgorithmException | WrongKeyCredentialException | UnsupportedKeyLengthException | UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
        Assume.assumeTrue(extendedStoreConnection.existsOnFs(container, name));

        try {
            byte[] bs = jwePersistence.loadObject(new ObjectHandle(container, name), keyStore, secretKeyPassHandler);
            String byteStr = new String(bs, "UTF-8");
            Assert.assertEquals(dataStr, byteStr);
        } catch (ObjectNotFoundException | WrongKeyCredentialException | UnsupportedEncodingException | UnknownContainerException e) {
            org.junit.Assert.fail(e.getMessage());
        }


        char[] wrongKeyPass = "wrongSecretPass".toCharArray();
        CallbackHandler secondSecretKeyPassHandler = new PasswordCallbackHandler(wrongKeyPass);
        KeyStore ks = TestKeyUtils.testSecretKeystore("wrongStore", "wrongStorePass".toCharArray(), secretKeyAlias, wrongKeyPass);

        jwePersistence.loadObject(new ObjectHandle(container, name), ks, secondSecretKeyPassHandler);
        org.junit.Assert.fail("Expecting a security exception");
    }
}
