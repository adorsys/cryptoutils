package org.adorsys.encobject.service.impl.generator;

import org.adorsys.encobject.service.api.generator.SecretKeyGenerator;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.secretkey.SecretKeyBuilder;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

public class SecretKeyGeneratorImpl implements SecretKeyGenerator {

    private final String secretKeyAlgo;
    private final Integer keySize;

    public SecretKeyGeneratorImpl(String secretKeyAlgo, Integer keySize) {
        this.secretKeyAlgo = secretKeyAlgo;
        this.keySize = keySize;
    }

    @Override
    public SecretKeyData generate(String alias, CallbackHandler secretKeyPassHandler) {
        SecretKey secretKey = new SecretKeyBuilder()
                .withKeyAlg(secretKeyAlgo)
                .withKeyLength(keySize)
                .build();

        return SecretKeyData.builder().secretKey(secretKey).alias(alias).passwordSource(secretKeyPassHandler).keyAlgo(secretKeyAlgo).build();

    }
}
