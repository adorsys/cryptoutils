package org.adorsys.encobject.userdata;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.exceptions.ObjectNotFoundException;
import org.adorsys.encobject.params.KeyParams;
import org.adorsys.encobject.service.EncObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectPersistenceAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectPersistenceAdapter.class);

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
    public boolean hasStore() {
        return encObjectService.hasKeystore(keyCredentials);
    }

    /**
     * Initializes the store of the user with the given keyCredentials
     */
    public void initStore() {
        try {
            String container = keyCredentials.getHandle().getContainer();
            if (!encObjectService.containerExists(container)) {
                encObjectService.newContainer(container);
            }
            encObjectService.newSecretKey(keyCredentials, keyParams());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public <T> T load(ObjectHandle handle, Class<T> klass) {
        try {
            byte[] src = null;
            try {
                src = encObjectService.readObject(keyCredentials, handle);
            } catch (ObjectNotFoundException e) {
                LOGGER.warn("ExceptionHandling used for control flow. This is not allowed");
                return null;
            }
            return objectMapper.readValue(src, klass);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public <T> void store(ObjectHandle userMainRecordhandle, T t) {
        storeInternal(userMainRecordhandle, t);
    }

    private <T> void storeInternal(ObjectHandle handle, T t) {
        try {
            String container = keyCredentials.getHandle().getContainer();
            if (!encObjectService.containerExists(container)) {
                encObjectService.newContainer(container);
            }

            byte[] data = objectMapper.writeValueAsBytes(t);
            encObjectService.writeObject(data, null, handle, keyCredentials);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
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
