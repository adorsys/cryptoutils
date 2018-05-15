package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.exceptions.KeyStoreExistsException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.service.api.generator.KeyStoreCreationConfig;
import org.adorsys.encobject.service.impl.generator.KeyStoreCreationConfigImpl;
import org.adorsys.encobject.service.impl.generator.KeyStoreGenerator;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreServiceImpl implements KeyStoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceImpl.class);

    private KeystorePersistence keystorePersistence;
    private ExtendedStoreConnection extendedStoreConnection;

    public KeyStoreServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
        this.extendedStoreConnection = extendedStoreConnection;
    }

    /**
     *
     */
    @Override
    public void createKeyStore(KeyStoreAuth keyStoreAuth,
                               KeyStoreType keyStoreType,
                               BucketPath keyStorePath,
                               KeyStoreCreationConfig config) {
        try {
            LOGGER.debug("start create keystore " + keyStorePath);
            if (extendedStoreConnection.blobExists(keyStorePath)) {
                throw new KeyStoreExistsException("creation of keytore aborted. a keystore already exists in " + keyStorePath);
            }


            if (config == null) {
                config = new KeyStoreCreationConfigImpl(5, 5, 5);
            }
            // TODO, hier also statt der StoreID nun das
            String serverKeyPairAliasPrefix = HexUtil.convertBytesToHexString(keyStorePath.getObjectHandle().getName().getBytes());
            LOGGER.debug("keystoreid = " + serverKeyPairAliasPrefix);
            {
                String realKeyStoreId = new String(HexUtil.convertHexStringToBytes(serverKeyPairAliasPrefix));
                LOGGER.debug("meaning of keystoreid = " + realKeyStoreId);
            }
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config,
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    keyStoreAuth.getReadKeyPassword());
            KeyStore userKeyStore = keyStoreGenerator.generate();

            keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getReadStoreHandler(), keyStorePath.getObjectHandle());
            LOGGER.debug("finished create keystore " + keyStorePath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public KeyStore loadKeystore(BucketPath keyStorePath, CallbackHandler userKeystoreHandler) {
        LOGGER.debug("start load keystore " + keyStorePath);
        KeyStore keyStore = keystorePersistence.loadKeystore(keyStorePath.getObjectHandle(), userKeystoreHandler);
        LOGGER.debug("finished load keystore " + keyStorePath);
        return keyStore;
    }
}
