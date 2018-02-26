package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;

import java.security.Key;
import java.security.KeyStore;

/**
 * Created by peter on 26.02.18 at 14:00.
 */
public class KeyStoreBasedPrivateKeySourceImpl implements KeySource {

    private KeyStore keyStore;
    private ReadKeyPassword readKeyPassword;


    public KeyStoreBasedPrivateKeySourceImpl(KeyStore keyStore, ReadKeyPassword readKeyPassword) {
        this.keyStore = keyStore;
        this.readKeyPassword = readKeyPassword;
    }


    @Override
    public Key readKey(KeyID keyID) {
        try {
            return keyStore.getKey(keyID.getValue(), readKeyPassword.getValue().toCharArray());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
