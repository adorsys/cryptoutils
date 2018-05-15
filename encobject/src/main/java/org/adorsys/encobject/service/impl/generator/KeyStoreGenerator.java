package org.adorsys.encobject.service.impl.generator;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.generator.KeyPairGenerator;
import org.adorsys.encobject.service.api.generator.KeyStoreCreationConfig;
import org.adorsys.encobject.service.api.generator.SecretKeyGenerator;
import org.adorsys.jkeygen.keystore.KeyPairData;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.Date;
import java.util.UUID;

public class KeyStoreGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreGenerator.class);
    public static final String UGLY_KEY_STORE_CACHE = "UGLY_KEY_STORE_CACHE";
    private final KeyStoreType keyStoreType;
    private final String serverKeyPairAliasPrefix;
    private final KeyStoreCreationConfig config;
    private final ReadKeyPassword readKeyPassword;

    public KeyStoreGenerator(
            KeyStoreCreationConfig config,
            KeyStoreType keyStoreType,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword
    ) {
        this.config = config;
        this.keyStoreType = keyStoreType;
        // this.serverKeyPairAliasPrefix = serverKeyPairAliasPrefix;
        this.serverKeyPairAliasPrefix = "KEYSTORE-ID-0";
        this.readKeyPassword = readKeyPassword;
        LOGGER.debug("Keystore ID ignored " + serverKeyPairAliasPrefix);
    }

    public KeyStore generate() {
        if (UglyKeyStoreCache.INSTANCE.isActive()) {
            KeyStore keyStore = UglyKeyStoreCache.INSTANCE.getCachedKeyStoreFor(keyStoreType, serverKeyPairAliasPrefix, readKeyPassword, config);
            if (keyStore != null) {
                LOGGER.debug("KeyStoreGeneration (milliseconds) DURATION WAS 0");
                return keyStore;
            }
        }
        KeyStore keyStore = null;
        Date startTime = new Date();
        try {
            String keyStoreID = serverKeyPairAliasPrefix;
            CallbackHandler readKeyHandler = new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreType);

            {
                KeyPairGenerator encKeyPairGenerator = config.getEncKeyPairGenerator(keyStoreID);
                int numberOfEncKeyPairs = config.getEncKeyNumber();
                for (int i = 0; i < numberOfEncKeyPairs; i++) {
                    KeyPairData signatureKeyPair = encKeyPairGenerator.generateEncryptionKey(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                KeyPairGenerator signKeyPairGenerator = config.getSignKeyPairGenerator(keyStoreID);
                int numberOfSignKeyPairs = config.getSignKeyNumber();
                for (int i = 0; i < numberOfSignKeyPairs; i++) {
                    KeyPairData signatureKeyPair = signKeyPairGenerator.generateSignatureKey(
                            serverKeyPairAliasPrefix + UUID.randomUUID().toString(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                SecretKeyGenerator secretKeyGenerator = config.getSecretKeyGenerator(keyStoreID);
                int numberOfSecretKeys = config.getSecretKeyNumber();
                for (int i = 0; i < numberOfSecretKeys; i++) {
                    SecretKeyData secretKeyData = secretKeyGenerator.generate(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(secretKeyData);
                }
            }
            keyStore = keystoreBuilder.build();
            return keyStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        } finally {
            Date stopTime = new Date();
            long duration = stopTime.getTime() - startTime.getTime();
            LOGGER.debug("KeyStoreGeneration (milliseconds) DURATION WAS " + duration);
            if (UglyKeyStoreCache.INSTANCE.isActive()) {
                UglyKeyStoreCache.INSTANCE.cacheKeyStoreFor(keyStore, keyStoreType, serverKeyPairAliasPrefix, readKeyPassword, config);
            }
        }
    }
}
