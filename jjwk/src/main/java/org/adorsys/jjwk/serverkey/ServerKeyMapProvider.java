package org.adorsys.jjwk.serverkey;

import com.nimbusds.jose.jwk.JWKSet;

import java.security.Key;

public interface ServerKeyMapProvider {

    @Deprecated
    ServerKeyMap getKeyMap();

    @Deprecated
    ServerKeysHolder getServerKeysHolder();

    JWKSet getPublicKeys();

    KeyAndJwk randomSecretKey();

    KeyAndJwk randomSignKey();

    Key getKey(String keyId);
}
