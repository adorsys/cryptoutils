package org.adorsys.encobject.complextypes;

import org.adorsys.encobject.domain.LocationInterface;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.types.KeyStoreID;
import org.adorsys.encobject.types.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
// TODO müsste auch komplett auf BucketPath umgestellt werden, so wie DocumentBucketPath
public class KeyStoreLocation implements LocationInterface {
	private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreLocation.class);
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private final KeyStoreDirectory keyStoreDirectory;
	private final KeyStoreID keyStoreID;
	private final KeyStoreType keyStoreType;

	public KeyStoreLocation(KeyStoreDirectory keyStoreDirectory, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		this.keyStoreDirectory = keyStoreDirectory;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}
	
	public KeyStoreDirectory getKeyStoreDirectory() {
		return keyStoreDirectory;
	}

	public KeyStoreID getKeyStoreID() {
		return keyStoreID;
	}

	public KeyStoreType getKeyStoreType() {
		return keyStoreType;
	}

	public ObjectHandle getLocationHandle(){
		return keyStoreDirectory.append(keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue()).getObjectHandle();
	}

	@Override
	public String toString() {
		return "KeyStoreLocation{" +
				keyStoreDirectory +
				", " + keyStoreID +
				", " + keyStoreType +
				'}';
	}
}
