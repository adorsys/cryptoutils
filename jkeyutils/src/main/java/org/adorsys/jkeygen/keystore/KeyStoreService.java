package org.adorsys.jkeygen.keystore;

import org.adorsys.jkeygen.keypair.CertificationResult;
import org.adorsys.jkeygen.keypair.SelfSignedKeyPairData;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.jkeygen.utils.V3CertificateUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Key store manipulation routines.
 *
 * @author fpo
 */
public class KeyStoreService {

    /**
     * Create an initializes a new key store. The key store is not yet password protected.
     *
     * @param storeType storeType
     * @return KeyStore keyStore
     * @throws IOException IOException
     */
    public static KeyStore newKeyStore(String storeType) throws IOException {

        // Use default type if blank.
        if (StringUtils.isBlank(storeType)) storeType = "UBER";
        try {
            KeyStore ks = KeyStore.getInstance(storeType);
            ks.load(null, null);
            return ks;
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Write this key store into a byte array
     *
     * @param keystore     keystore
     * @param storeId      storeId
     * @param storePassSrc storePassSrc
     * @return key store byte array
     * @throws IOException              if there was an I/O problem with data
     * @throws CertificateException     if any of the certificates included in the keystore data could not be stored
     * @throws NoSuchAlgorithmException if the appropriate data integrity algorithm could not be found
     */
    public static byte[] toByteArray(KeyStore keystore, String storeId, CallbackHandler storePassSrc) throws NoSuchAlgorithmException, CertificateException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            keystore.store(stream, PasswordCallbackUtils.getPassword(storePassSrc, storeId));
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Keystore not initialized.", e);
        }
        return stream.toByteArray();
    }

    /**
     * Loads a key store. Given the store bytes, the store type
     *
     * @param in           : the inputStream from which to read the keystore
     * @param storeId      : The store id. This is passed to the callback handler to identify the requested password record.
     * @param storeType    : the type of this key store. f null, the defaut java keystore type is used.
     * @param storePassSrc : the callback handler that retrieves the store password.
     * @return KeyStore
     * @throws KeyStoreException         either NoSuchAlgorithmException or NoSuchProviderException
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded
     * @throws UnrecoverableKeyException if a password is required but not given, or if the given password was incorrect
     * @throws IOException               if there is an I/O or format problem with the keystore data
     */
    public static KeyStore loadKeyStore(InputStream in, String storeId, String storeType, CallbackHandler storePassSrc) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, IOException {

        // Use default type if blank.
        if (StringUtils.isBlank(storeType)) storeType = "UBER";

        KeyStore ks = KeyStore.getInstance(storeType);

        try {
            ks.load(in, PasswordCallbackUtils.getPassword(storePassSrc, storeId));
        } catch (IOException e) {
            // catch missing or wrong key.
            if (e.getCause() != null && (e.getCause() instanceof UnrecoverableKeyException)) {
                throw (UnrecoverableKeyException) e.getCause();
            } else if (e.getCause() != null && (e.getCause() instanceof BadPaddingException)) {
                throw new UnrecoverableKeyException(e.getMessage());
            }
            throw e;
        }
        return ks;
    }

    public static KeyStore loadKeyStore(String storeType, KeyStore.LoadStoreParameter loadStoreParameter) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, IOException {

        // Use default type if blank.
        if (StringUtils.isBlank(storeType)) storeType = "UBER";

        KeyStore ks = KeyStore.getInstance(storeType);

        try {
            ks.load(loadStoreParameter);
        } catch (IOException e) {
            // catch missing or wrong key.
            if (e.getCause() != null && (e.getCause() instanceof UnrecoverableKeyException)) {
                throw (UnrecoverableKeyException) e.getCause();
            } else if (e.getCause() != null && (e.getCause() instanceof BadPaddingException)) {
                throw new UnrecoverableKeyException(e.getMessage());
            }
            throw e;
        }
        return ks;
    }

    /**
     * @param data         : the byte array containing key store data.
     * @param storeId      : The store id. This is passed to the callback handler to identify the requested password record.
     * @param storeType    : the type of this key store. f null, the defaut java keystore type is used.
     * @param storePassSrc : the callback handler that retrieves the store password.
     * @return KeyStore
     * @throws KeyStoreException         either NoSuchAlgorithmException or NoSuchProviderException
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded
     * @throws UnrecoverableKeyException if a password is required but not given, or if the given password was incorrect
     * @throws IOException               if there is an I/O or format problem with the keystore data
     */
    public static KeyStore loadKeyStore(byte[] data, String storeId, String storeType, CallbackHandler storePassSrc) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return loadKeyStore(new ByteArrayInputStream(data), storeId, storeType, storePassSrc);
    }

    /**
     * Put the given entries into a key store. The key store must have been initialized before.
     *
     * @param ks         ks
     * @param keyEntries keyEntries
     */
    public static void fillKeyStore(final KeyStore ks, Collection<KeyEntry> keyEntries) {
        try {
            for (KeyEntry keyEntry : keyEntries) {
                addToKeyStore(ks, keyEntry);
            }
        } catch (KeyStoreException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Put the given entry into a key store. The key store must have been initialized before.
     *
     * @param ks         ks
     * @param keyEntry keyEntry to be added
     */
    public static void addToKeyStore(final KeyStore ks, KeyEntry keyEntry) throws KeyStoreException {
        if (keyEntry instanceof KeyPairEntry) {
            addToKeyStore(ks, (KeyPairEntry) keyEntry);
        } else if (keyEntry instanceof SecretKeyEntry) {
            addToKeyStore(ks, (SecretKeyEntry) keyEntry);
        } else if (keyEntry instanceof TrustedCertEntry) {
            addToKeyStore(ks, (TrustedCertEntry) keyEntry);
        }
    }

    private static void addToKeyStore(final KeyStore ks, KeyPairEntry keyPairHolder) throws KeyStoreException {

        List<Certificate> chainList = new ArrayList<>();
        CertificationResult certification = keyPairHolder.getCertification();
        X509CertificateHolder subjectCert = certification != null ? certification.getSubjectCert() : keyPairHolder.getKeyPair().getSubjectCert();
        chainList.add(V3CertificateUtils.getX509JavaCertificate(subjectCert));
        if (certification != null) {
            List<X509CertificateHolder> issuerChain = certification.getIssuerChain();
            for (X509CertificateHolder x509CertificateHolder : issuerChain) {
                chainList.add(V3CertificateUtils.getX509JavaCertificate(x509CertificateHolder));
            }
        }
        Certificate[] chain = chainList.toArray(new Certificate[chainList.size()]);
        ks.setKeyEntry(keyPairHolder.getAlias(), keyPairHolder.getKeyPair().getKeyPair().getPrivate(),
                PasswordCallbackUtils.getPassword(keyPairHolder.getPasswordSource(), keyPairHolder.getAlias()), chain);
    }

    public static void addToKeyStore(final KeyStore ks, SecretKeyEntry secretKeyData) {
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKeyData.getSecretKey());
        ProtectionParameter protParam = getPasswordProtectionParameter(secretKeyData.getPasswordSource(), secretKeyData.getAlias());
        try {
            ks.setEntry(secretKeyData.getAlias(), entry, protParam);
        } catch (KeyStoreException e) {
            // Key store not initialized
            throw new IllegalStateException(e);
        }
    }

    private static ProtectionParameter getPasswordProtectionParameter(CallbackHandler passwordSource, String alias) {
        return new KeyStore.PasswordProtection(PasswordCallbackUtils.getPassword(passwordSource, alias));
    }

    private static void addToKeyStore(final KeyStore ks, TrustedCertEntry trustedCertHolder) throws KeyStoreException {
        ks.setCertificateEntry(trustedCertHolder.getAlias(), V3CertificateUtils.getX509JavaCertificate(trustedCertHolder.getCertificate()));
    }

    public static List<KeyEntry> loadEntries(KeyStore keyStore, PasswordProvider passwordProvider) {
        List<KeyEntry> keyEntries = new ArrayList<>();
        Enumeration<String> aliases;

        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        for(String alias : Collections.list(aliases)) {
            KeyStore.Entry entry;
            try {
                CallbackHandler passwordSource = passwordProvider.providePasswordCallbackHandler(alias);
                entry = keyStore.getEntry(alias, getPasswordProtectionParameter(passwordSource, alias));
                KeyEntry keyEntry = createFromKeyStoreEntry(alias, entry, passwordSource);

                keyEntries.add(keyEntry);
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        return keyEntries;
    }

    private static KeyEntry createFromKeyStoreEntry(String alias, KeyStore.Entry entry, CallbackHandler passwordSource) {
        if (entry instanceof KeyStore.PrivateKeyEntry) {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
            return fromPrivateKeyEntry(alias, passwordSource, privateKeyEntry);
        } else if (entry instanceof KeyStore.SecretKeyEntry) {
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) entry;
            SecretKey secretKey = secretKeyEntry.getSecretKey();

            return SecretKeyData.builder()
                    .alias(alias)
                    .passwordSource(passwordSource)
                    .secretKey(secretKey)
                    .keyAlgo(secretKey.getAlgorithm())
                    .build();
        } else if(entry instanceof KeyStore.TrustedCertificateEntry) {
            KeyStore.TrustedCertificateEntry trustedCertificateEntry = (KeyStore.TrustedCertificateEntry) entry;

            return TrustedCertData.builder()
                    .alias(alias)
                    .passwordSource(passwordSource)
                    .certificate(toX509CertificateHolder(trustedCertificateEntry.getTrustedCertificate()))
                    .build();
        } else {
            throw new RuntimeException("Unknown type: " + entry.getClass());
        }
    }

    private static KeyPairEntry fromPrivateKeyEntry(String alias, CallbackHandler passwordSource, KeyStore.PrivateKeyEntry privateKeyEntry) {
        KeyPair keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());

        X509CertificateHolder subjectCert = toX509CertificateHolder(privateKeyEntry.getCertificate());
        SelfSignedKeyPairData keyPairData = new SelfSignedKeyPairData(keyPair, subjectCert);

        CertificationResult certification = new CertificationResult(subjectCert, toX509CertificateHolders(privateKeyEntry.getCertificateChain()));

        return KeyPairData.builder()
                .alias(alias)
                .keyPair(keyPairData)
                .certification(certification)
                .passwordSource(passwordSource)
                .build();
    }

    private static List<X509CertificateHolder> toX509CertificateHolders(Certificate[] certificates) {
        return Arrays.stream(certificates)
                .map(KeyStoreService::toX509CertificateHolder)
                .collect(Collectors.toList());
    }

    private static X509CertificateHolder toX509CertificateHolder(Certificate certificate) {
        org.bouncycastle.asn1.x509.Certificate bouncyCastleAsn1Certificate = null;

        try {
            bouncyCastleAsn1Certificate = org.bouncycastle.asn1.x509.Certificate.getInstance(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        return new X509CertificateHolder(bouncyCastleAsn1Certificate);
    }

    public interface PasswordProvider {
        CallbackHandler providePasswordCallbackHandler(String keyAlias);
    }

    public static class PasswordProviderMap implements PasswordProvider {
        private final Map<String, char[]> passwordsForAlias;

        public PasswordProviderMap(Map<String, char[]> passwordsForAlias) {
            this.passwordsForAlias = passwordsForAlias;
        }

        @Override
        public CallbackHandler providePasswordCallbackHandler(String keyAlias) {
            char[] password = passwordsForAlias.get(keyAlias);

            if(password == null) {
                throw new RuntimeException("Password for alias '" + keyAlias + "' not found");
            }

            return new PasswordCallbackHandler(password);
        }
    }

    public static class SimplePasswordProvider implements PasswordProvider {

        private final CallbackHandler callbackHandler;

        public SimplePasswordProvider(char[] password) {
            this.callbackHandler = new PasswordCallbackHandler(password);
        }

        public SimplePasswordProvider(CallbackHandler callbackHandler) {
            this.callbackHandler = callbackHandler;
        }

        @Override
        public CallbackHandler providePasswordCallbackHandler(String keyAlias) {
            return callbackHandler;
        }
    }
}

