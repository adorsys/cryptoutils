package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.exceptions.WrongKeyCredentialException;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class KeyCredentialBasedKeySourceImpl implements KeySource {

	private KeyCredentials keyCredentials;
	private KeyStore keyStore;
	private KeystorePersistence keystorePersistence;

	public KeyCredentialBasedKeySourceImpl(KeyCredentials keyCredentials,
                                           KeystorePersistence keystorePersistence) {
		this.keyCredentials = keyCredentials;
		this.keystorePersistence = keystorePersistence;
	}

	@Override
	public Key readKey(KeyID keyID) {
		if (keyStore == null)
			keyStore = keystorePersistence.loadKeystore(keyCredentials.getHandle(),
					new PasswordCallbackHandler(keyCredentials.getStorepass().toCharArray()));
		try {
			return keyStore.getKey(keyID.getValue(), PasswordCallbackUtils.getPassword(
					new PasswordCallbackHandler(keyCredentials.getKeypass().toCharArray()), keyID.getValue()));
		} catch (UnrecoverableKeyException e) {
			throw new WrongKeyCredentialException(e);
		} catch (KeyStoreException e) {
			throw new WrongKeyCredentialException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
