package org.adorsys.jkeygen.keystore;

import org.bouncycastle.cert.X509CertificateHolder;

public interface TrustedCertEntry extends KeyEntry {
    X509CertificateHolder getCertificate();
}
