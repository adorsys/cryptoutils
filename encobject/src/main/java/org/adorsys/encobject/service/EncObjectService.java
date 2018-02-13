package org.adorsys.encobject.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.MissingKeyAlgorithmException;
import org.adorsys.encobject.exceptions.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.exceptions.MissingKeystoreProviderException;
import org.adorsys.encobject.exceptions.ObjectNotFoundException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.exceptions.WrongKeyCredentialException;
import org.adorsys.encobject.exceptions.WrongKeystoreCredentialException;
import org.adorsys.encobject.params.KeyParams;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.keystore.SecretKeyEntry;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.jkeygen.secretkey.SecretKeyBuilder;

public class EncObjectService {

	private final KeystorePersistence keystorePersistence;
	private final ObjectPersistence objectPersistence;
	private final ContainerPersistence containerPersistence;

	public EncObjectService(
			KeystorePersistence keystorePersistence,
			ObjectPersistence objectPersistence,
			ContainerPersistence containerPersistence
	) {
		this.keystorePersistence = keystorePersistence;
		this.objectPersistence = objectPersistence;
		this.containerPersistence = containerPersistence;
	}

	/**
	 * Checks if the container with the given name exists.
	 * 
	 * @param container container to check
	 * @return if container exists
	 */
	public boolean containerExists(String container) {
		return containerPersistence.containerExists(container);
	}

	public void newContainer(String container) throws ContainerExistsException {
		containerPersistence.creteContainer(container);
	}
	
	public void newSecretKey(KeyCredentials keyCredentials, KeyParams keyParams) throws CertificateException, WrongKeystoreCredentialException,
			MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException,
		NoSuchAlgorithmException{
		CallbackHandler storePassHandler = new PasswordCallbackHandler(keyCredentials.getStorepass().toCharArray());
		CallbackHandler secretKeyPassHandler = new PasswordCallbackHandler(keyCredentials.getKeypass().toCharArray());
		
		SecretKey secretKey = new SecretKeyBuilder().withKeyAlg(keyParams.getKeyAlogirithm()).withKeyLength(keyParams.getKeyLength()).build();	
		SecretKeyEntry secretKeyData = SecretKeyData.builder().secretKey(secretKey).alias(keyCredentials.getKeyid()).passwordSource(secretKeyPassHandler).build();

		KeyStore keyStore;
		try {
			keyStore = keystorePersistence.loadKeystore(keyCredentials.getHandle(), storePassHandler);
		} catch (ObjectNotFoundException e) {
			keyStore = KeyStoreService.newKeyStore(null);
		}

		KeyStoreService.addToKeyStore(keyStore, secretKeyData);
		
		keystorePersistence.saveKeyStore(keyStore, storePassHandler, keyCredentials.getHandle());
	}

	public byte[] readObject(KeyCredentials keyCredentials, ObjectHandle objectHandle)
			throws ObjectNotFoundException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException,
			MissingKeystoreProviderException, MissingKeyAlgorithmException, CertificateException, IOException,
			WrongKeyCredentialException, UnknownContainerException {
		
		KeyStore keyStore = keystorePersistence.loadKeystore(keyCredentials.getHandle(), new PasswordCallbackHandler(keyCredentials.getStorepass().toCharArray()));
		return objectPersistence.loadObject(objectHandle, keyStore, new PasswordCallbackHandler(keyCredentials.getKeypass().toCharArray()));
	}
	
	public void writeObject(byte[] data, ContentMetaInfo metaIno, ObjectHandle handle, KeyCredentials keyCredentials)
			throws CertificateException, ObjectNotFoundException, WrongKeystoreCredentialException, 
			MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, 
			UnsupportedEncAlgorithmException, WrongKeyCredentialException, UnsupportedKeyLengthException, UnknownContainerException{
		KeyStore keyStore = keystorePersistence.loadKeystore(keyCredentials.getHandle(), new PasswordCallbackHandler(keyCredentials.getStorepass().toCharArray()));
		objectPersistence.storeObject(data, metaIno, handle, keyStore, keyCredentials.getKeyid(), new PasswordCallbackHandler(keyCredentials.getKeypass().toCharArray()), null);
	}
	
	public boolean hasKeystore(KeyCredentials keyCredentials){
		return keystorePersistence.hasKeystore(keyCredentials.getHandle());
	}

}
