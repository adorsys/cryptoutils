package org.adorsys.jjwk.serverkey;

public class ServerKeyPropertiesConstants {

	public static final String RESET_KEYSTORE = "RESET_KEYSTORE";
	public static final String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
		
	public static final String SERVER_KEYSTORE_SECRET_KEY_SIZE = "SERVER_KEYSTORE_SECRET_KEY_SIZE";// 256
	public static final String SERVER_KEYSTORE_SECRET_KEY_ALGO = "SERVER_KEYSTORE_SECRET_KEY_ALGO"; // AES
	public static final String SERVER_KEYSTORE_RSA_SIGN_ALGO = "SERVER_KEYSTORE_RSA_SIGN_ALGO";// SHA1withRSA

	public static final String SERVER_KEYSTORE_KEYPAIR_SIZE = "SERVER_KEYSTORE_KEYPAIR_SIZE";// 2048
	public static final String SERVER_KEYSTORE_KEYPAIR_ALGO = "SERVER_KEYSTORE_KEYPAIR_ALGO";// RSA

	// Please use KeyStoreType.DEFAULT
	// public static final String SERVER_KEYSTORE_TYPE = "SERVER_KEYSTORE_TYPE";

	public static final String SERVER_SECRET_KEY_COUNT = "SERVER_SECRET_KEY_COUNT";// 5
	public static final String SERVER_ENCRYPT_KEY_COUNT = "SERVER_ENCRYPT_KEY_COUNT";// 5
	public static final String SERVER_SIGN_KEY_COUNT = "SERVER_SIGN_KEY_COUNT";// 5
	
	public static final String SERVER_KEYALIAS_PREFIX = "SERVER_KEYALIAS_PREFIX"; // "adsts-"
	public static final String SERVER_KEYPAIR_NAME = "SERVER_KEYPAIR_NAME"; // "Adorsys Security Token Service"
	public static final String SERVER_KEYSTORE_NAME = "SERVER_KEYSTORE_NAME"; //"adsts-keystore"
	public static final String SERVER_KEYSTORE_CONTAINER = "SERVER_KEYSTORE_CONTAINER"; // "adsts-container"
}
