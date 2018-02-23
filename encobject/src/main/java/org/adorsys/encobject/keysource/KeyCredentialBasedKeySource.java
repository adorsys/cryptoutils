package org.adorsys.encobject.keysource;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.exceptions.WrongKeyCredentialException;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

public class KeyCredentialBasedKeySource implements KeySource {

	private KeyCredentials keyCredentials;
	private KeyStore keyStore;
	private KeystorePersistence keystorePersistence;

	public KeyCredentialBasedKeySource(KeyCredentials keyCredentials,
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
