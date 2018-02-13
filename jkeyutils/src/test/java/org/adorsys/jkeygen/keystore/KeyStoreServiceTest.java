package org.adorsys.jkeygen.keystore;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.jkeygen.keypair.KeyPairBuilder;
import org.adorsys.jkeygen.keypair.SelfSignedCertBuilder;
import org.adorsys.jkeygen.keypair.SelfSignedKeyPairData;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.jkeygen.secretkey.SecretKeyBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyStoreServiceTest {
    private static final String KEY_STORE_NAME = "FrancisKeyStore";
    private static final char[] storePass = "FrancisKeystorePass".toCharArray();

    private static final char[] keyPairPass = "FrancisKeyPairPass".toCharArray();
    private static final String keyPairAlias = "FrancisKeyPairAlias";

    private static final char[] secretKeyPass = "FrancisSecretKeyPass".toCharArray();
    private static final String secretKeyAlias = "FrancisSecretKeyAlias";

    private SelfSignedKeyPairData keyPairData;

    private SecretKey secretKey;

    @BeforeClass
    public static void beforeClass() {
        // Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
        // see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Before
    public void before() {
        KeyPair keyPair = new KeyPairBuilder().withKeyAlg("RSA").withKeyLength(2048).build();
        Assume.assumeNotNull(keyPair);

        X500Name cn = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, "Francis Pouatcha").build();
        keyPairData = new SelfSignedCertBuilder().withSubjectDN(cn)
                .withSignatureAlgo("SHA256withRSA").withNotAfterInDays(300).withCa(false).build(keyPair);
        Assume.assumeNotNull(keyPairData);
        Assume.assumeNotNull(keyPairData.getKeyPair());
        Assume.assumeNotNull(keyPairData.getSubjectCert());

        secretKey = new SecretKeyBuilder().withKeyAlg("AES").withKeyLength(256).build();
        Assume.assumeNotNull(secretKey);

    }

    @Test
    public void testCeateKeystore() {
        CallbackHandler keyPassHandler = new PasswordCallbackHandler(keyPairPass);
        CallbackHandler storePassHandler = new PasswordCallbackHandler(storePass);

        KeyPairEntry keyPairStoreData = KeyPairData.builder().keyPair(keyPairData).alias(keyPairAlias).passwordSource(keyPassHandler).build();

        CallbackHandler secretKeyPassHandler = new PasswordCallbackHandler(secretKeyPass);
        SecretKeyEntry secretKeyData = SecretKeyData.builder().secretKey(secretKey).alias(secretKeyAlias).passwordSource(secretKeyPassHandler).build();

        try {
            new KeystoreBuilder()
                    .withKeyEntry(keyPairStoreData)
                    .withKeyEntry(secretKeyData)
                    .withStoreId("sampleKeystore")
                    .build(storePassHandler);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testLoadKeystore() {
        byte[] bs = createKeyStore();
        Assume.assumeNotNull(bs);

        ByteArrayInputStream bis = new ByteArrayInputStream(bs);
        KeyStore keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
        Assert.assertNotNull(keyStore);
    }

    @Test(expected = BaseException.class)
    public void testBadStorePass() {
        boolean soFar = false;
        try {
            byte[] bs = createKeyStore();
            Assume.assumeNotNull(bs);

            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            char[] badStorePass = "WrongFrancisKeystorePass".toCharArray();
            soFar = true;
            KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(badStorePass));
        } finally {
            Assert.assertTrue("test did not run untill expected exception", soFar);
        }
    }

    @Test
    public void testLoadKeyPair() {
        try {
            byte[] bs = createKeyStore();
            Assume.assumeNotNull(bs);

            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            KeyStore keyStore = null;
            keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
            Assume.assumeNotNull(keyStore);

            Key key = keyStore.getKey(keyPairAlias, keyPairPass);
            Assert.assertNotNull(key);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testLoadSecretKey() {
        try {
            byte[] bs = createKeyStore();
            Assume.assumeNotNull(bs);

            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            KeyStore keyStore = null;
            keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
            Assume.assumeNotNull(keyStore);

            Key key1 = keyStore.getKey(secretKeyAlias, secretKeyPass);
            Assert.assertNotNull(key1);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test(expected = UnrecoverableKeyException.class)
    public void testBadKeyPairPass() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        boolean soFar = false;
        try {
            byte[] bs = createKeyStore();
            Assume.assumeNotNull(bs);

            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            KeyStore keyStore = null;
            keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
            Assume.assumeNotNull(keyStore);

            char[] badKeyPass = "WrongFrancisKeyPass".toCharArray();
            soFar = true;
            keyStore.getKey(keyPairAlias, badKeyPass);
        } finally {
            Assert.assertTrue("test did not run untill expected exception", soFar);
        }
    }


    @Test(expected = UnrecoverableKeyException.class)
    public void testBadSecretKeyPass() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        boolean soFar = false;
        try {
            byte[] bs = createKeyStore();
            Assume.assumeNotNull(bs);

            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            KeyStore keyStore = null;
            keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
            Assume.assumeNotNull(keyStore);

            char[] badKeyPass = "WrongFrancisSecretKeyPass".toCharArray();
            soFar = true;
            keyStore.getKey(secretKeyAlias, badKeyPass);
        } finally {
            Assert.assertTrue("test did not run untill expected exception", soFar);
        }
    }

    @Test
    public void testLoadKeyEntries() {
        byte[] bs = createKeyStore();
        Assume.assumeNotNull(bs);

        ByteArrayInputStream bis = new ByteArrayInputStream(bs);
        KeyStore keyStore = null;
        keyStore = KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, new PasswordCallbackHandler(storePass));
        Assume.assumeNotNull(keyStore);


        List<KeyEntry> keyEntries = KeyStoreService.loadEntries(keyStore, createPasswordProviderMap());
        Assert.assertNotNull(keyEntries);
        Assert.assertEquals(2, keyEntries.size());
    }

    private KeyStoreService.PasswordProvider createPasswordProviderMap() {
        Map<String, char[]> passwordMap = new HashMap<>();
        passwordMap.put(keyPairAlias, keyPairPass);
        passwordMap.put(secretKeyAlias, secretKeyPass);

        return new KeyStoreService.PasswordProviderMap(passwordMap);
    }

    private byte[] createKeyStore() {
        CallbackHandler keyPassHandler = new PasswordCallbackHandler(keyPairPass);
        CallbackHandler storePassHandler = new PasswordCallbackHandler(storePass);

        KeyPairEntry keyPairStoreData = KeyPairData.builder().keyPair(keyPairData).alias(keyPairAlias).passwordSource(keyPassHandler).build();

        CallbackHandler secretKeyPassHandler = new PasswordCallbackHandler(secretKeyPass);
        SecretKeyEntry secretKeyData = SecretKeyData.builder().secretKey(secretKey).alias(secretKeyAlias).passwordSource(secretKeyPassHandler).build();

        try {
            return new KeystoreBuilder()
                    .withKeyEntry(keyPairStoreData)
                    .withKeyEntry(secretKeyData)
                    .withStoreId("sampleKeystore")
                    .build(storePassHandler);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            return null;
        }
    }
}
