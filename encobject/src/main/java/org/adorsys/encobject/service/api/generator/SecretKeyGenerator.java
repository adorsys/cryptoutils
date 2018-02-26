package org.adorsys.encobject.service.api.generator;

import org.adorsys.jkeygen.keystore.SecretKeyData;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 26.02.18 at 17:03.
 */
public interface SecretKeyGenerator {
    SecretKeyData generate(String alias, CallbackHandler secretKeyPassHandler);
}
