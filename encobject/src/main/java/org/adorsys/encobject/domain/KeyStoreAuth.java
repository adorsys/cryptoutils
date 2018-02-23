package org.adorsys.encobject.domain;

import org.adorsys.encobject.exceptions.KeyStoreAuthException;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 05.01.18.
 *
 * BTW, so liest man das Kennwort aus dem Handler
 * char[] password = PasswordCallbackUtils.getPassword(keyStoreAuth.getReadKeyHandler(), keyStorePassword);
 */
public class KeyStoreAuth {
    private ReadStorePassword readStorePassword;
    private ReadKeyPassword readKeyPassword;

    public KeyStoreAuth(ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword) {
        this.readStorePassword = readStorePassword;
        this.readKeyPassword = readKeyPassword;
    }

    public CallbackHandler getReadStoreHandler() {
        if (readStorePassword == null) {
            throw new KeyStoreAuthException("Access to READ STORE HANDLER not allowed.");
        }
        return new PasswordCallbackHandler(readStorePassword.getValue().toCharArray());
    }

    public CallbackHandler getReadKeyHandler() {
        if (readKeyPassword == null) {
            throw new KeyStoreAuthException("Access to READ KEY HANDLER not allowed.");
        }
        return new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
    }

    public ReadStorePassword getReadStorePassword() {
        if (readStorePassword == null) {
            throw new KeyStoreAuthException("Access to READ STORE PASSWORD not allowed.");
        }
        return readStorePassword;
    }

    public ReadKeyPassword getReadKeyPassword() {
        if (readKeyPassword == null) {
            throw new KeyStoreAuthException("Access to READ KEY PASSWORD not allowed");
        }
        return readKeyPassword;
    }

    public void setReadKeyPassword(ReadKeyPassword readKeyPassword) {
        this.readKeyPassword = readKeyPassword;
    }

    @Override
    public String toString() {
        return "KeyStoreAuth{" +
                readStorePassword +
                ", " + readKeyPassword +
                '}';
    }
}
