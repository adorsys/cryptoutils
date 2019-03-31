package org.adorsys.jtstamp.service;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyStore;

import javax.security.auth.callback.CallbackHandler;

import de.adorsys.common.exceptions.BaseException;
import org.adorsys.jkeygen.keypair.KeyPairBuilder;
import org.adorsys.jkeygen.keypair.SelfSignedCertBuilder;
import org.adorsys.jkeygen.keypair.SelfSignedKeyPairData;
import org.adorsys.jkeygen.keystore.KeyPairData;
import org.adorsys.jkeygen.keystore.KeyPairEntry;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.jkeygen.pwd.PasswordMapCallbackHandler;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KstUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(KstUtils.class);

	public static final String KEY_STORE_NAME = "FrancisKeyStore";
	public static final char[] storePass = "FrancisKeystorePass".toCharArray();
	public static final char[] keyPairPass = "FrancisKeyPairPass".toCharArray();
	public static final String keyPairAlias = "FrancisKeyPairAlias";
	
	public static void turnOffEncPolicy(){

		// Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
		// see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
		try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, java.lang.Boolean.FALSE);
	    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			LOGGER.debug("ignore Exception: " + ex.getClass().getName() + " during turnOffEncPolicy");
	    }		
	}

	
	public static KeyStore testKeystore(){
		try {
			CallbackHandler keyPassHandler = new PasswordCallbackHandler(keyPairPass);
			CallbackHandler storePassHandler = new PasswordCallbackHandler(storePass);
			
			byte[] bs = new KeystoreBuilder()
					.withKeyEntry(newKeyPair("Francis Pouatcha", keyPairAlias, keyPassHandler))
					.withStoreId(KEY_STORE_NAME)
					.build(storePassHandler);

			ByteArrayInputStream bis = new ByteArrayInputStream(bs);
			return KeyStoreService.loadKeyStore(bis, KEY_STORE_NAME, null, storePassHandler);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static KeyPairEntry newKeyPair(String userName, String alias, CallbackHandler keyPassHandler){
		KeyPair keyPair = new KeyPairBuilder().withKeyAlg("RSA").withKeyLength(2048).build();
		X500Name cn = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, userName).build();
		SelfSignedKeyPairData keyPairData = new SelfSignedCertBuilder().withSubjectDN(cn)
				.withSignatureAlgo("SHA256withRSA").withNotAfterInDays(300).withCa(false).build(keyPair);
		return KeyPairData.builder().keyPair(keyPairData).alias(alias).passwordSource(keyPassHandler).build();
	}

	public static PasswordMapCallbackHandler.Builder callbackHandlerBuilder(){
		return new PasswordMapCallbackHandler.Builder()
				.withEntry(keyPairAlias, keyPairPass);
	}
}
