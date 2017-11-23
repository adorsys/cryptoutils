package org.adorsys.encobject.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.keystore.KeystoreData;
import org.adorsys.jkeygen.keystore.KeyStoreService;

import com.google.protobuf.ByteString;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public interface KeystorePersistence {

	void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException;
	KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) throws KeystoreNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException;
	
	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 * 
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	boolean hasKeystore(ObjectHandle handle);
}
