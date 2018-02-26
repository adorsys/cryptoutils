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

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.UUID;

public class KeyStoreGenerator {
    private final KeyStoreType keyStoreType;
    private final String serverKeyPairAliasPrefix;
    private final CallbackHandler readKeyHandler;
    private final KeyStoreCreationConfig config;

    public KeyStoreGenerator(
            KeyStoreCreationConfig config,
            KeyStoreType keyStoreType,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword
    ) {
        this.config = config;
        this.keyStoreType = keyStoreType;
        this.serverKeyPairAliasPrefix = serverKeyPairAliasPrefix;
        this.readKeyHandler = new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
    }

    public KeyStore generate() {
        String keyStoreID = serverKeyPairAliasPrefix;
        try {
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreType);
            PasswordCallbackHandler dummyKeyHandler = new PasswordCallbackHandler("".toCharArray());

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
            return keystoreBuilder.build();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
