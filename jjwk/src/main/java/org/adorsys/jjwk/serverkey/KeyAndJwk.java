package org.adorsys.jjwk.serverkey;

import java.security.Key;

import com.nimbusds.jose.jwk.JWK;

public class KeyAndJwk {
    public final Key key;
    public final JWK jwk;
    public KeyAndJwk(Key key, JWK jwk) {
        this.key = key;
        this.jwk = jwk;
    }
}
