package org.adorsys.encobject.params;

import java.security.Key;

import org.adorsys.jjwk.exceptions.UnsupportedEncAlgorithmException;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;

/**
 * {@link EncParamSelector} selects an encryption algorithm and an encryption method based on the 
 * key used for encryption.
 * 
 * @author fpo
 *
 */
public class EncParamSelector {
	public static EncryptionParams selectEncryptionParams(Key key) {
		String algorithm = key.getAlgorithm();
		// TODO fix hack
		if(StringUtils.equalsAnyIgnoreCase("NONE", algorithm)){
			algorithm = "AES";
		}
		if(StringUtils.equalsAnyIgnoreCase("AES", algorithm)){
			return new EncryptionParams.Builder().setEncAlgo(JWEAlgorithm.A256GCMKW).setEncMethod(EncryptionMethod.A256GCM).build();
		}
		if(StringUtils.equalsAnyIgnoreCase("RSA", algorithm)){
			return new EncryptionParams.Builder().setEncAlgo(JWEAlgorithm.RSA_OAEP_256).setEncMethod(EncryptionMethod.A128GCM).build();
		}
		// TODO EC
		throw new UnsupportedEncAlgorithmException("UnsupportedEncAlgorithmException from key:" + algorithm);
	}
}
