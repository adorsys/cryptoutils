package org.adorsys.encobject.service.impl.generator;

import org.adorsys.encobject.service.api.generator.KeyStoreCreationConfig;

/**
 * Created by peter on 09.01.18.
 */
public class KeyStoreCreationConfigImpl implements KeyStoreCreationConfig {
    private final Integer encKeyNumber;
    private final Integer signKeyNumber;
    private final Integer secretKeyNumber;

    /**
     * Weitere Konstruktoren ggf. wenn Algorithmen angepasst werden sollen
     * @param encKeyNumber
     * @param signKeyNumber
     * @param secretKeyNumber
     */
    public KeyStoreCreationConfigImpl(Integer encKeyNumber, Integer signKeyNumber, Integer secretKeyNumber) {
        this.encKeyNumber = encKeyNumber;
        this.signKeyNumber = signKeyNumber;
        this.secretKeyNumber = secretKeyNumber;
    }

    @Override
    public KeyPairGeneratorImpl getEncKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc-" + keyPrefix);
    }

    @Override
    public KeyPairGeneratorImpl getSignKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "sign-" + keyPrefix);
    }

    @Override
    public SecretKeyGeneratorImpl getSecretKeyGenerator(String keyPrefix) {
        return new SecretKeyGeneratorImpl("AES", 256);
    }

    @Override
    public Integer getEncKeyNumber() {
        return encKeyNumber;
    }

    @Override
    public Integer getSignKeyNumber() {
        return signKeyNumber;
    }

    @Override
    public Integer getSecretKeyNumber() {
        return secretKeyNumber;
    }
}
