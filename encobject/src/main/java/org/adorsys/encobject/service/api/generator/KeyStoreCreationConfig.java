package org.adorsys.encobject.service.api.generator;

/**
 * Created by peter on 26.02.18 at 17:04.
 */
public interface KeyStoreCreationConfig {
    KeyPairGenerator getEncKeyPairGenerator(String keyPrefix);

    KeyPairGenerator getSignKeyPairGenerator(String keyPrefix);

    SecretKeyGenerator getSecretKeyGenerator(String keyPrefix);

    Integer getEncKeyNumber();

    Integer getSignKeyNumber();

    Integer getSecretKeyNumber();
}
