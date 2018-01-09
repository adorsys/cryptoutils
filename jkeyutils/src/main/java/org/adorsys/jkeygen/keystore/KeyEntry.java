package org.adorsys.jkeygen.keystore;

import javax.security.auth.callback.CallbackHandler;

public interface KeyEntry {
    CallbackHandler getPasswordSource();

    String getAlias();
}
