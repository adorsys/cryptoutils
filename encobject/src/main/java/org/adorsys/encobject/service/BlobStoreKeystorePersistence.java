package org.adorsys.encobject.service;

import com.google.protobuf.ByteString;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.domain.keystore.KeystoreData;
import org.adorsys.encobject.exceptions.KeystoreNotFoundException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Map;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public class BlobStoreKeystorePersistence implements KeystorePersistence {
	private final static Logger LOGGER = LoggerFactory.getLogger(BlobStoreKeystorePersistence.class);

	private ExtendedStoreConnection extendedStoreConnection;

	public BlobStoreKeystorePersistence(ExtendedStoreConnection extendedStoreConnection) {
		this.extendedStoreConnection = extendedStoreConnection;
	}

	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle){
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			KeystoreData keystoreData = KeystoreData.newBuilder().setType(storeType).setKeystore(ByteString.copyFrom(bs)).build();
			extendedStoreConnection.putBlob(handle, keystoreData.toByteArray());
	}

	public void saveKeyStoreWithAttributes(KeyStore keystore, Map<String, String> attributes, CallbackHandler storePassHandler, ObjectHandle handle){
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			KeystoreData keystoreData = KeystoreData.newBuilder()
					.setType(storeType)
					.setKeystore(ByteString.copyFrom(bs))
					.putAllAttributes(attributes)
					.build();
			extendedStoreConnection.putBlob(handle, keystoreData.toByteArray());
	}
	
	public KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) {
		KeystoreData keystoreData = loadKeystoreData(handle);
		return initKeystore(keystoreData, handle.getName(), handler);
	}

	public Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler){
		KeystoreData keystoreData = loadKeystoreData(handle);
		KeyStore keyStore = initKeystore(keystoreData, handle.getName(), handler);

		return new Tuple<>(keyStore, keystoreData.getAttributesMap());
	}

	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 * 
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	public boolean hasKeystore(ObjectHandle handle) {
			return extendedStoreConnection.getBlob(handle)!=null;
	}

	
	private KeystoreData loadKeystoreData(ObjectHandle handle) {
		byte[] keyStoreBytes;
			keyStoreBytes = extendedStoreConnection.getBlob(handle);

		try {
			return KeystoreData.parseFrom(keyStoreBytes);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid protocol buffer", e);
		}
	}

	private KeyStore initKeystore(KeystoreData keystoreData, String storeid, CallbackHandler handler){
			return KeyStoreService.loadKeyStore(keystoreData.getKeystore().toByteArray(), storeid, keystoreData.getType(), handler);
	}

	/*
	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, KeyStoreLocation keyStoreLocation) {
		try {
			// Match store type aggainst file extension
			if(!keyStoreLocation.getKeyStoreType().equals(new KeyStoreType(keystore.getType())))
					throw new ExtendedPersistenceException("Invalid store type - expected : " + keystore.getType() + " but is: " + keyStoreLocation.getKeyStoreType().getValue());
			
			// write keystore to byte array.
			LOGGER.debug("write keystore at " + keyStoreLocation + " and with type " + keystore.getType());
			byte[] bs = KeyStoreService.toByteArray(keystore, keyStoreLocation.getLocationHandle().getName(), storePassHandler);
			
			// write byte array to blob store.
			extendedStoreConnection.putBlob(keyStoreLocation.getLocationHandle() , bs);
		} catch (Exception e) {
			BaseExceptionHandler.handle(e);
		}
	}
	
	public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler handler) {
		try {
			// Read bytes
			byte[] ksBytes = extendedStoreConnection.getBlob(keyStoreLocation.getLocationHandle());
			LOGGER.debug("loaded keystore has size:" + ksBytes.length);
			// Initialize key store
			return KeyStoreService.loadKeyStore(ksBytes, keyStoreLocation.getLocationHandle().getName(), keyStoreLocation.getKeyStoreType().getValue(), handler);
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}
		*/
	
}
