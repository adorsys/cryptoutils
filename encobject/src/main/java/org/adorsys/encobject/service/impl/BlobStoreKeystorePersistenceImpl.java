package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.jkeygen.keystore.KeyStoreType;
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
 */
public class BlobStoreKeystorePersistenceImpl implements KeystorePersistence {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlobStoreKeystorePersistenceImpl.class);
    private final static String KEYSTORE_TYPE_KEY = "INTERNAL_BLOB_STORE_KEYSTORE_PERSISTENCE_TYPE_KEY";

    private ExtendedStoreConnection extendedStoreConnection;

    public BlobStoreKeystorePersistenceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
    }

    @Override
    public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, ObjectHandle handle) {
        String storeType = keystore.getType();
        byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
        BucketPath bucketPath = BucketPath.fromHandle(handle);
        Payload payload = new SimplePayloadImpl(bs);
        payload.getStorageMetadata().getUserMetadata().put(KEYSTORE_TYPE_KEY, storeType);
        extendedStoreConnection.putBlob(bucketPath, payload);
    }

    @Override
    public void saveKeyStoreWithAttributes(KeyStore keystore, UserMetaData userMetaData, CallbackHandler storePassHandler, ObjectHandle handle) {
        String storeType = keystore.getType();
        byte[] bs = KeyStoreService.toByteArray(keystore, handle.getName(), storePassHandler);
        BucketPath bucketPath = BucketPath.fromHandle(handle);

        SimpleStorageMetadataImpl simpleStorageMetadataImpl = new SimpleStorageMetadataImpl();
        simpleStorageMetadataImpl.getUserMetadata().put(KEYSTORE_TYPE_KEY, storeType);
        simpleStorageMetadataImpl.addUserMetadata(userMetaData);

        SimplePayloadImpl payload = new SimplePayloadImpl(simpleStorageMetadataImpl, bs);
        extendedStoreConnection.putBlob(bucketPath, payload);
    }

    @Override
    public KeyStore loadKeystore(ObjectHandle handle, CallbackHandler handler) {
        BucketPath bucketPath = BucketPath.fromHandle(handle);
        Payload payload = extendedStoreConnection.getBlob(bucketPath);
        return initKeystore(payload, handle.getName(), handler);
    }

    @Override
    public Tuple<KeyStore, Map<String, String>> loadKeystoreAndAttributes(ObjectHandle handle, CallbackHandler handler) {
        BucketPath bucketPath = BucketPath.fromHandle(handle);
        Payload payload = extendedStoreConnection.getBlob(bucketPath);
        KeyStore keyStore = initKeystore(payload, handle.getName(), handler);
        UserMetaData metaInfo = payload.getStorageMetadata().getUserMetadata();

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
        return extendedStoreConnection.blobExists(BucketPath.fromHandle(handle));
    }

    private KeyStore initKeystore(Payload payload, String storeid, CallbackHandler handler) {
        KeyStoreType keyStoreType = new KeyStoreType(payload.getStorageMetadata().getUserMetadata().get(KEYSTORE_TYPE_KEY));
        return KeyStoreService.loadKeyStore(payload.getData(), storeid, keyStoreType, handler);
    }
}
