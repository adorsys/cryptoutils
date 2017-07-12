package org.adorsys.encobject.userdata;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.KeyParams;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.EncObjectService;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;

public class ObjectPersistenceAdapter {

	private ObjectMapperSPI objectMapper;
	private KeyCredentials keyCredentials;
	private EncObjectService encObjectService;

	public ObjectPersistenceAdapter(EncObjectService encObjectService, KeyCredentials keyCredentials, ObjectMapperSPI objectMapper) {
		super();
		this.encObjectService = encObjectService;
		this.keyCredentials = keyCredentials;
		this.objectMapper = objectMapper;
	}
	
	/**
	 * Checks if the user with the given key credential has a store.
	 * 
	 * @return if the given key credential has a store
	 */
	public boolean hasStore(){
		return encObjectService.hasKeystore(keyCredentials);
	}
	
	/**
	 * Initializes the store of the user with the given keyCredentials
	 */
	public void initStore(){
		try {
			String container = keyCredentials.getHandle().getContainer();
			if(!encObjectService.containerExists(container)){
				try {
					encObjectService.newContainer(container);
				} catch (ContainerExistsException e) {
					throw new IllegalStateException(e);
				}
			}
			encObjectService.newSecretKey(keyCredentials, keyParams());
		} catch (CertificateException | NoSuchAlgorithmException | WrongKeystoreCredentialException
				| MissingKeystoreAlgorithmException | MissingKeystoreProviderException | MissingKeyAlgorithmException
				| IOException | UnknownContainerException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public <T> T load(ObjectHandle handle, Class<T> klass) {
		byte[] src = null;
		try {
			src = encObjectService.readObject(keyCredentials, handle);
		} catch (ObjectNotFoundException e) {
			return null;
		} catch (CertificateException | WrongKeystoreCredentialException | MissingKeystoreAlgorithmException
				| MissingKeystoreProviderException | MissingKeyAlgorithmException | IOException
				| WrongKeyCredentialException | UnknownContainerException e) {
			throw new IllegalStateException(e);
		}

		try {
			return objectMapper.readValue(src, klass);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public <T> void store(ObjectHandle userMainRecordhandle, T t) {
		storeInternal(userMainRecordhandle, t);
	}

	private <T> void storeInternal(ObjectHandle handle, T t) {
		String container = keyCredentials.getHandle().getContainer();
		if(!encObjectService.containerExists(container)){
			try {
				encObjectService.newContainer(container);
			} catch (ContainerExistsException e) {
				throw new IllegalStateException("Can not create container with name: " + container, e);
			}
		}

		try {
			byte[] data = objectMapper.writeValueAsBytes(t);
			encObjectService.writeObject(data, null, handle, keyCredentials);
		} catch (CertificateException | ObjectNotFoundException | WrongKeystoreCredentialException
				| MissingKeystoreAlgorithmException | MissingKeystoreProviderException | MissingKeyAlgorithmException
				| IOException | UnsupportedEncAlgorithmException | WrongKeyCredentialException
				| UnsupportedKeyLengthException | UnknownContainerException e) {
			throw new IllegalStateException(e);
		}

	}

	public KeyCredentials getKeyCredentials() {
		return keyCredentials;
	}

	private static KeyParams keyParams() {
		KeyParams keyParams = new KeyParams();
		keyParams.setKeyAlogirithm("AES");
		keyParams.setKeyLength(256);
		return keyParams;
	}
}
