package org.adorsys.encobject.types;

import javax.crypto.SecretKey;

public class SecretKeyIDWithKey {
    private KeyID keyID;
    private SecretKey secretKey;

    public SecretKeyIDWithKey(KeyID keyID, SecretKey secretKey) {
        this.keyID = keyID;
        this.secretKey = secretKey;
    }

    public KeyID getKeyID() {
        return keyID;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
