package org.adorsys.jkeygen.keystore;

import javax.crypto.SecretKey;

public interface SecretKeyEntry extends KeyEntry {
    SecretKey getSecretKey();

    String getKeyAlgo();
}
