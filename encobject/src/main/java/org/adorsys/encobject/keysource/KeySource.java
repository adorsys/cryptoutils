package org.adorsys.encobject.keysource;

import java.security.Key;

import org.adorsys.encobject.types.KeyID;

/**
 * Retrieves and returns the key with the corresponding keyId.
 * 
 * @author fpo
 *
 */
public interface KeySource {
	public Key readKey(KeyID keyID);
}
