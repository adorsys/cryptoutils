package org.adorsys.encobject.service.impl.generator;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.generator.KeyStoreCreationConfig;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 15.05.18 at 09:16.
 * This class must not be used in production.
 * The creation of a key store is pretty expensive. For that, this class
 * keeps a map to REUSE Keystores!
 */
public enum TESTKeyStoreCache {

    INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(TESTKeyStoreCache.class);
    TESTKeyStoreCache() {
        Logger LOGGER = LoggerFactory.getLogger(TESTKeyStoreCache.class);
        LOGGER.info("UGLY_KEYSTORE_CACHE CREATION");
    }

    Map<String, KeyStore> map = new HashMap<>();

    public KeyStore getCachedKeyStoreFor(KeyStoreType keyStoreType,
                                         String serverKeyPairAliasPrefix,
                                         ReadKeyPassword readKeyPassword,
                                         KeyStoreCreationConfig config) {
        String key = getMapKeyFor(keyStoreType, serverKeyPairAliasPrefix, readKeyPassword, config);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        LOGGER.debug("unknown key:" + key);
        map.keySet().forEach(knownkey -> LOGGER.debug("known key:" + knownkey));
        return null;
    }

    public KeyStore cacheKeyStoreFor(KeyStore keyStore,
                                     KeyStoreType keyStoreType,
                                     String serverKeyPairAliasPrefix,
                                     ReadKeyPassword readKeyPassword,
                                     KeyStoreCreationConfig config) {
        String key = getMapKeyFor(keyStoreType, serverKeyPairAliasPrefix, readKeyPassword, config);
        if (map.containsKey(key)) {
            throw new BaseException("this key is already known. " + key);
        }
        return map.put(key, keyStore);
    }

    private String getMapKeyFor(KeyStoreType keyStoreType,
                                String serverKeyPairAliasPrefix,
                                ReadKeyPassword readKeyPassword,
                                KeyStoreCreationConfig config) {
        String key = keyStoreType.getValue() + "+"
                + serverKeyPairAliasPrefix + "+"
                + readKeyPassword.getValue() + "+"
                + config.getEncKeyNumber() + "."
                + config.getSecretKeyNumber() + "."
                + config.getSignKeyNumber();
        return key;
    }

}