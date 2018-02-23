package org.adorsys.encobject.keysource;

import java.security.Key;

import org.adorsys.encobject.types.KeyID;
import org.adorsys.jjwk.serverkey.ServerKeyMapProvider;

public class KeyMapProviderBasedKeySource implements KeySource {
	private final ServerKeyMapProvider keyMapProvider;
	
	public KeyMapProviderBasedKeySource(ServerKeyMapProvider keyMapProvider) {
		this.keyMapProvider = keyMapProvider;
	}

	@Override
	public Key readKey(KeyID keyID) {
		return keyMapProvider.getKey(keyID.getValue());
	}

}
