package org.adorsys.encobject.domain;

import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 08.01.18.
 */
public class KeyStoreAccess {
    private final BucketPath keyStorePath;
    private final KeyStoreAuth keyStoreAuth;

    public KeyStoreAccess(BucketPath keyStorePath, KeyStoreAuth keyStoreAuth) {
        this.keyStorePath = keyStorePath;
        this.keyStoreAuth = keyStoreAuth;
    }

    public BucketPath getKeyStorePath() {
        return keyStorePath;
    }

    public KeyStoreAuth getKeyStoreAuth() {
        return keyStoreAuth;
    }

    @Override
    public String toString() {
        return "KeyStoreAccess{" +
                "keyStorePath=" + keyStorePath +
                ", keyStoreAuth=" + keyStoreAuth +
                '}';
    }
}
