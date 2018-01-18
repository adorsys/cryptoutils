package org.adorsys.jkeygen.keystore;

import org.adorsys.jkeygen.keypair.CertificationResult;
import org.adorsys.jkeygen.keypair.SelfSignedKeyPairData;

public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
