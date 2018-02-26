package org.adorsys.encobject.service.api.generator;

import org.adorsys.jkeygen.keystore.KeyPairData;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 26.02.18 at 17:09.
 */
public interface KeyPairGenerator {
    KeyPairData generateSignatureKey(String alias, CallbackHandler keyPassHandler);
    KeyPairData generateEncryptionKey(String alias, CallbackHandler keyPassHandler);
}
