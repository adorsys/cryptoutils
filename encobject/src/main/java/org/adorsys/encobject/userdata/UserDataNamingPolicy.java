package org.adorsys.encobject.userdata;

import de.adorsys.dfs.connection.api.domain.ObjectHandle;
import org.adorsys.encobject.domain.KeyCredentials;

/**
 * Defines the semantic for the retrieval of key credential.
 * 
 * In general, the container is the applName + "_" + userId;
 * 
 * This way, we can discover that the user id is unique.
 * 
 * @author fpo
 *
 */
public class UserDataNamingPolicy {
	private static String keystoreFileName = "keystore";
	private static String mainRecordFileName = "mainRecord";
	
	private String appName;
	
	public UserDataNamingPolicy(String appName) {
		super();
		this.appName = appName;
	}

	public KeyCredentials newKeyCredntials (String subject, String userSecret){
		KeyCredentials keyCredentials = new KeyCredentials();
		
		String container = nameUserContainer(subject);
		
		/* The keystore reference and the keyid have the same name. The user name.*/
		ObjectHandle handle = new ObjectHandle();
		handle.setContainer(container);
		handle.setName(keystoreFileName);
		
		keyCredentials.setHandle(handle);
		keyCredentials.setKeyid("mainUserKey");
		
		/*For this version we assume the keypass and the storepass are identicals.*/
		keyCredentials.setKeypass(userSecret);
		keyCredentials.setStorepass(userSecret);
		
		return keyCredentials ;
		
	}
	
	public String nameUserContainer(String subject){
		return appName + "_" + subject;
	}

	public ObjectHandle handleForUserMainRecord(KeyCredentials keyCredentials) {
		ObjectHandle keyHandle = keyCredentials.getHandle();
		ObjectHandle handle = new ObjectHandle();
		handle.setContainer(keyHandle.getContainer());
		handle.setName(mainRecordFileName);
		return handle;
	}
}
