package org.adorsys.jjwk.kid;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.Base64URL;

public class FixKeyId {

	/**
	 * Replaces each alias of a store with the base 64 encoded sha-1 thumbprint of the corresponding key.
	 * @param ks ks
	 * @param callbackHandler callbackHandler
	 * @return value
	 */
	public static boolean fixKeyId(final KeyStore ks, CallbackHandler callbackHandler) {
		try {
			JWKSet exportPrivateKeys = JwkExport.exportKeys(ks, callbackHandler);
			List<JWK> keys = exportPrivateKeys.getKeys();
			boolean change = false;
			for (JWK jwk : keys) {
				String keyID = jwk.getKeyID();
				Base64URL thumbprint = jwk.computeThumbprint();
				String expectedKeyId = thumbprint.toString().toLowerCase();
				if(!StringUtils.equals(keyID, expectedKeyId)){
					char[] keypass = PasswordCallbackUtils.getPassword(callbackHandler, keyID);
					Key key = ks.getKey(keyID, keypass);
					Certificate certificate = ks.getCertificate(keyID);
					Certificate[] chain = {certificate};
					ks.setKeyEntry(expectedKeyId, key, keypass, chain);
					ks.deleteEntry(keyID);
					change = true;
				}
			}
			return change;
		} catch (JOSEException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e){
			throw new IllegalStateException(e);
		}
	}
}
