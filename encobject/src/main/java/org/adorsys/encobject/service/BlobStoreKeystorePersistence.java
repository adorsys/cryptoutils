package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public class BlobStoreKeystorePersistence implements KeystorePersistence {
	private final static Logger LOGGER = LoggerFactory.getLogger(BlobStoreKeystorePersistence.class);
	private final static String KEYSTORE_TYPE_KEY="keystore.type";

	private ExtendedStoreConnection extendedStoreConnection;

	public BlobStoreKeystorePersistence(ExtendedStoreConnection extendedStoreConnection) {
		this.extendedStoreConnection = extendedStoreConnection;
	}

	@Override
	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle) {
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			BucketPath bucketPath = new BucketPath(handle.getContainer(), handle.getName());
			Payload payload = ByteArrayPayload.builder(bs).putMetaInfo(KEYSTORE_TYPE_KEY, storeType).build();
			extendedStoreConnection.putBlob(bucketPath, payload );
	}

	@Override
	public void saveKeyStoreWithAttributes(KeyStore keystore, Map<String, String> attributes, CallbackHandler storePassHandler, ObjectHandle handle) {
			String storeType = keystore.getType();
			byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
			BucketPath bucketPath = new BucketPath(handle.getContainer(), handle.getName());
			if(attributes!=null && attributes.containsKey(KEYSTORE_TYPE_KEY))
				throw new IllegalStateException("Can not set attribut type. This is infered from the stored keystore");
			Payload payload = ByteArrayPayload.builder(bs)
					.putMetaInfo(KEYSTORE_TYPE_KEY, storeType)
					.putAllMetaInfo(attributes).build();
			extendedStoreConnection.putBlob(bucketPath, payload );
	}

	@Override
	public KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) {
		BucketPath bucketPath = new BucketPath(handle.getContainer(), handle.getName());
		Payload payload = extendedStoreConnection.getBlob(bucketPath);
		return initKeystore(payload, handle.getName(), handler);
	}

	@Override
	public Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler) {
		BucketPath bucketPath = new BucketPath(handle.getContainer(), handle.getName());
		Payload payload = extendedStoreConnection.getBlob(bucketPath);
		KeyStore keyStore = initKeystore(payload, handle.getName(), handler);
		BlobMetaInfo metaInfo = payload.getBlobMetaInfo();

		Map<String, String> attributeMap = new HashMap<>();
		Set<String> keySet = metaInfo.keySet();
		for (String key : keySet) {
			attributeMap.put(key, metaInfo.get(key));
		}
		attributeMap.remove(KEYSTORE_TYPE_KEY);
		return new Tuple<>(keyStore, attributeMap);
	}

	/**
	 * Checks if a keystore available for the given handle. This is generally true if
	 * the container exists and the file with name "name" is in that container.
	 *
	 * @param handle handle to check
	 * @return if a keystore available for the given handle
	 */
	public boolean hasKeystore(ObjectHandle handle) {
		return extendedStoreConnection.blobExists(new BucketPath(handle.getContainer(), handle.getName()));
	}

	private KeyStore initKeystore(Payload payload, String storeid, CallbackHandler handler) {
			String keyStoreType = payload.getBlobMetaInfo().get(KEYSTORE_TYPE_KEY);
			return KeyStoreService.loadKeyStore(payload.getData(), storeid, keyStoreType, handler);
	}
}
