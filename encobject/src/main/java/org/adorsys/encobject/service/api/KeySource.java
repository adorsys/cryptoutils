package org.adorsys.encobject.service.api;

import org.adorsys.encobject.types.KeyID;

import java.security.Key;

/**
 * Retrieves and returns the key with the corresponding keyId.
 * 
 * @author fpo
 *
 */
public interface KeySource {
	public Key readKey(KeyID keyID);
}
