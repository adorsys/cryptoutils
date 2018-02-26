package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.jjwk.serverkey.ServerKeyMapProvider;

import java.security.Key;

public class KeyMapProviderBasedKeySourceImpl implements KeySource {
	private final ServerKeyMapProvider keyMapProvider;
	
	public KeyMapProviderBasedKeySourceImpl(ServerKeyMapProvider keyMapProvider) {
		this.keyMapProvider = keyMapProvider;
	}

	@Override
	public Key readKey(KeyID keyID) {
		return keyMapProvider.getKey(keyID.getValue());
	}

}
