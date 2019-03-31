package org.adorsys.encobject.service.api;

import com.nimbusds.jose.jwk.*;
import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.exceptions.AsymmetricEncryptionException;
import org.adorsys.encobject.exceptions.SymmetricEncryptionException;
import org.adorsys.encobject.service.impl.KeyStoreBasedPrivateKeySourceImpl;
import org.adorsys.encobject.service.impl.KeyStoreBasedPublicKeySourceImpl;
import org.adorsys.encobject.service.impl.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.PublicKeyJWK;
import org.adorsys.encobject.types.SecretKeyIDWithKey;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.utils.V3CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by peter on 23.02.18 at 09:23.
 */
public class KeyStore2KeySourceHelper {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStore2KeySourceHelper.class);

    /**
     * @param keystorePersistence
     * @param keyStoreAccess      Muss nur das ReadStorePassword enthalten. ReadKeyPassword darf null sein
     * @return
     */
    public static KeySourceAndKeyID getForPublicKey(KeystorePersistence keystorePersistence, KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("getForPublicKey " + keyStoreAccess.getKeyStorePath());
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

        JWKSet exportKeys = load(userKeystore, null, keyStoreAccess.getKeyStorePath());
        List<JWK> encKeys = selectEncKeys(exportKeys);
        JWK randomKey = JwkExport.randomKey(encKeys);
        KeyID keyID = new KeyID(randomKey.getKeyID());
        KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);
        return new KeySourceAndKeyID(keySource, keyID);
    }

    public static PublicKeyJWK getPublicKeyJWK(KeystorePersistence keystorePersistence, KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("getPublicKeyJWK " + keyStoreAccess.getKeyStorePath());
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

        JWKSet exportKeys = load(userKeystore, null, keyStoreAccess.getKeyStorePath());
        List<JWK> encKeys = selectEncKeys(exportKeys);
        return new PublicKeyJWK(JwkExport.randomKey(encKeys));
    }

    /**
     * @param keystorePersistence
     * @param keyStoreAccess      bei Passworte muessen gesetzt sein
     * @return
     */
    public static KeySource getForPrivateKey(KeystorePersistence keystorePersistence, KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("get keysource for private key of " + keyStoreAccess.getKeyStorePath());
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
        KeySource keySource = new KeyStoreBasedPrivateKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyPassword());
        return keySource;
    }

    /**
     * @param keystorePersistence
     * @param keyStoreAccess      bei Passworte muessen gesetzt sein
     * @return
     */
    public static KeySourceAndKeyID getForSecretKey(KeystorePersistence keystorePersistence, KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("get keysource for secret key of " + keyStoreAccess.getKeyStorePath());
        // KeyStore laden
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
        KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
        return new KeySourceAndKeyID(keySource, getRandomSecretKeyIDWithKey(keyStoreAccess, userKeystore).getKeyID());

    }

    public static SecretKeyIDWithKey getRandomSecretKeyIDWithKey(KeyStoreAccess keyStoreAccess, KeyStore userKeystore) {
        // Choose a random secret key with its id
        JWKSet jwkSet = JwkExport.exportKeys(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
        if (jwkSet.getKeys().isEmpty()) {
            throw new SymmetricEncryptionException("did not find any keys in keystore with id: " + keyStoreAccess.getKeyStorePath());
        }
        ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
        KeyAndJwk randomSecretKey;
        try {
            randomSecretKey = serverKeyMap.randomSecretKey();
        } catch (IndexOutOfBoundsException b) {
            throw new SymmetricEncryptionException("did not find any secret keys in keystore with id: " + keyStoreAccess.getKeyStorePath());
        }
        KeyID keyID = new KeyID(randomSecretKey.jwk.getKeyID());
        return new SecretKeyIDWithKey(new KeyID(keyID.getValue()), (SecretKey) randomSecretKey.key);
    }


    public static class KeySourceAndKeyID {
        private final KeySource keySource;
        private final KeyID keyID;

        public KeySourceAndKeyID(KeySource keySource, KeyID keyID) {
            this.keySource = keySource;
            this.keyID = keyID;
        }

        public KeySource getKeySource() {
            return keySource;
        }

        public KeyID getKeyID() {
            return keyID;
        }
    }

    private static List<JWK> selectEncKeys(JWKSet exportKeys) {
        JWKMatcher signKeys = (new JWKMatcher.Builder()).keyUse(KeyUse.ENCRYPTION).build();
        return (new JWKSelector(signKeys)).select(exportKeys);
    }


    private static JWKSet load(final KeyStore keyStore, final PasswordLookup pwLookup, BucketPath keystorePath) {
        try {

            List<JWK> jwks = new LinkedList<>();

            // Load RSA and EC keys
            for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {

                final String keyAlias = keyAliases.nextElement();
                final char[] keyPassword = pwLookup == null ? "".toCharArray() : pwLookup.lookupPassword(keyAlias);

                Certificate cert = keyStore.getCertificate(keyAlias);
                if (cert == null) {
                    continue; // skip
                }

                Certificate[] certs = new Certificate[]{cert};
                if (cert.getPublicKey() instanceof RSAPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    RSAKey rsaJWK = RSAKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    rsaJWK = new RSAKey.Builder(rsaJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(rsaJWK);

                } else if (cert.getPublicKey() instanceof ECPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    ECKey ecJWK = ECKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    ecJWK = new ECKey.Builder(ecJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(ecJWK);
                } else {
                    continue;
                }
            }
            JWKSet jwkSet = new JWKSet(jwks);
            if (jwkSet.getKeys().isEmpty()) {
                throw new AsymmetricEncryptionException("did not find any public keys in keystore " + keystorePath);
            }
            return jwkSet;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
