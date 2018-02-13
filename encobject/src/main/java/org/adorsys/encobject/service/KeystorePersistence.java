package org.adorsys.encobject.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.exceptions.KeystoreNotFoundException;
import org.adorsys.encobject.exceptions.MissingKeyAlgorithmException;
import org.adorsys.encobject.exceptions.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.exceptions.MissingKeystoreProviderException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.exceptions.WrongKeystoreCredentialException;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public interface KeystorePersistence {

	void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException;
	void saveKeyStoreWithAttributes(KeyStore keystore, Map<String, String> attributes, CallbackHandler storePassHandler, ObjectHandle handle) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException;

	KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) throws KeystoreNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException;
	Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler) throws KeystoreNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException;

	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 *
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	boolean hasKeystore(ObjectHandle handle);
	
	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, KeyStoreLocation keyStoreLocation);
	public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler handler);
}
