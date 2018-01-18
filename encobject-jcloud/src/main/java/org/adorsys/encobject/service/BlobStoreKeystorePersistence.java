package org.adorsys.encobject.service;

import com.google.protobuf.ByteString;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.keystore.KeystoreData;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.jkeygen.keystore.KeyStoreService;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public class BlobStoreKeystorePersistence implements KeystorePersistence {

	private BlobStoreConnection blobStoreConnection;

	public BlobStoreKeystorePersistence(BlobStoreContextFactory blobStoreContextFactory) {
		this.blobStoreConnection = new BlobStoreConnection(blobStoreContextFactory);
	}

	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException{
		try {
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			KeystoreData keystoreData = KeystoreData.newBuilder().setType(storeType).setKeystore(ByteString.copyFrom(bs)).build();
			blobStoreConnection.putBlob(handle, keystoreData.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void saveKeyStoreWithAttributes(KeyStore keystore, Map<String, String> attributes, CallbackHandler storePassHandler, ObjectHandle handle) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException{
		try {
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			KeystoreData keystoreData = KeystoreData.newBuilder().setType(storeType).setKeystore(ByteString.copyFrom(bs)).build();
			blobStoreConnection.putBlobWithMetadata(handle, keystoreData.toByteArray(), attributes);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) throws KeystoreNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException{
		KeystoreData keystoreData = loadKeystoreData(handle);
		return initKeystore(keystoreData, handle.getName(), handler);
	}

	public Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler) throws KeystoreNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException{
		Tuple<KeystoreData, Map<String, String>> keystoreDataWithAttributes = loadKeystoreDataWithAttributes(handle);
		KeyStore keyStore = initKeystore(keystoreDataWithAttributes.getX(), handle.getName(), handler);

		return new Tuple<>(keyStore, keystoreDataWithAttributes.getY());
	}
	
	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 * 
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	public boolean hasKeystore(ObjectHandle handle) {
		try {
			return blobStoreConnection.getBlob(handle)!=null;
		} catch (UnknownContainerException | ObjectNotFoundException e) {
			return false;
		}
	}

	
	private KeystoreData loadKeystoreData(ObjectHandle handle) throws KeystoreNotFoundException, UnknownContainerException{
		byte[] keyStoreBytes;
		try {
			keyStoreBytes = blobStoreConnection.getBlob(handle);
		} catch (ObjectNotFoundException e) {
			throw new KeystoreNotFoundException(e.getMessage(), e);
		}
		
		try {
			return KeystoreData.parseFrom(keyStoreBytes);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid protocol buffer", e);
		}
	}

	private Tuple<KeystoreData, Map<String, String>> loadKeystoreDataWithAttributes(ObjectHandle handle) throws KeystoreNotFoundException, UnknownContainerException{
		Tuple<byte[], Map<String, String>> loadedTuple;

		try {
			loadedTuple = blobStoreConnection.getBlobAndMetadata(handle);
		} catch (ObjectNotFoundException e) {
			throw new KeystoreNotFoundException(e.getMessage(), e);
		}

		try {
			KeystoreData keystoreData = KeystoreData.parseFrom(loadedTuple.getX());
			return new Tuple<>(keystoreData, loadedTuple.getY());
		} catch (IOException e) {
			throw new IllegalStateException("Invalid protocol buffer", e);
		}
	}

	private KeyStore initKeystore(KeystoreData keystoreData, String storeid, CallbackHandler handler) throws WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, CertificateException, IOException {
		try {
			return KeyStoreService.loadKeyStore(keystoreData.getKeystore().toByteArray(), storeid, keystoreData.getType(), handler);
		} catch (UnrecoverableKeyException e) {
			throw new WrongKeystoreCredentialException(e);
		} catch (KeyStoreException e) {
			if(e.getCause()!=null){
				Throwable cause = e.getCause();
				if(cause instanceof NoSuchAlgorithmException){
					throw new MissingKeystoreAlgorithmException(cause.getMessage(), cause);
				}
				if(cause instanceof NoSuchProviderException){
					throw new MissingKeystoreProviderException(cause.getMessage(), cause);
				}
			}
			throw new IllegalStateException("Unidentified keystore exception", e);
		} catch (NoSuchAlgorithmException e) {
			throw new MissingKeyAlgorithmException(e.getMessage(), e);
		}
	}
}
