package org.adorsys.encobject.service;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Tuple;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.Map;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public interface KeystorePersistence {

	void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle);
	void saveKeyStoreWithAttributes(KeyStore keystore, Map<String, String> attributes, CallbackHandler storePassHandler, ObjectHandle handle);

	KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler);
	Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler);

	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 *
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	boolean hasKeystore(ObjectHandle handle);
	
//	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, KeyStoreLocation keyStoreLocation);
//	public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler handler);
}
