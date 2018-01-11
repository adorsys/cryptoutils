package org.adorsys.jjwk.serverkey;

import com.nimbusds.jose.jwk.JWKSet;

import java.security.Key;

/**
 * Holds the signature keys in different formats.
 *
 * @author fpo
 *
 */
public class ServerKeyManager implements ServerKeyMapProvider {

    private final ServerKeysHolder serverKeysHolder;
    private final ServerKeyMap keyMap;

    public ServerKeyManager(ServerKeysHolder serverKeysHolder){
        this.serverKeysHolder = serverKeysHolder;
        this.keyMap = new ServerKeyMap(serverKeysHolder.getPrivateKeySet());
    }

    @Override
    public ServerKeysHolder getServerKeysHolder() {
        return serverKeysHolder;
    }

    @Override
    public JWKSet getPublicKeys() {
        return serverKeysHolder.getPublicKeySet();
    }

    @Override
    public KeyAndJwk randomSecretKey() {
        return keyMap.randomSecretKey();
    }

    @Override
    public KeyAndJwk randomSignKey() {
        return keyMap.randomSignKey();
    }

    @Override
    public Key getKey(String keyId) {
        return keyMap.getKey(keyId);
    }

    @Override
    public ServerKeyMap getKeyMap() {
        return keyMap;
    }
}
