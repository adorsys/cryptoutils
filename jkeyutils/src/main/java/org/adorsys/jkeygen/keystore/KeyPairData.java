package org.adorsys.jkeygen.keystore;

import lombok.Builder;
import lombok.Getter;
import org.adorsys.jkeygen.keypair.CertificationResult;
import org.adorsys.jkeygen.keypair.SelfSignedKeyPairData;

import javax.security.auth.callback.CallbackHandler;

@Getter
public class KeyPairData extends KeyEntryData implements KeyPairEntry {

    private final SelfSignedKeyPairData keyPair;

    private final CertificationResult certification;

    @Builder
    private KeyPairData(CallbackHandler passwordSource, String alias, SelfSignedKeyPairData keyPair, CertificationResult certification) {
        super(passwordSource, alias);
        this.keyPair = keyPair;
        this.certification = certification;
    }
}
