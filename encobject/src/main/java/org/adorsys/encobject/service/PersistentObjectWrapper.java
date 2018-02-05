package org.adorsys.encobject.service;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.types.KeyID;

/**
 * Describes the structure of a persistent object.
 * 
 * @author fpo
 *
 */
public class PersistentObjectWrapper {
	/*
	 * This is the decrypted payload. This will be :
	 */
	private byte[] data;
	
	/*
	 * Meta Info, will be stored unencrypted but tamper resistent in the header of the encrypted object. 
	 */
	private ContentMetaInfo metaIno;
	
	/*
	 * Representation of a symmetric DocumentKeyId.
	 */
	private KeyID keyID;
	
	private ObjectHandle handle;

	public PersistentObjectWrapper(byte[] data, ContentMetaInfo metaIno, KeyID keyID, ObjectHandle handle) {
		super();
		this.data = data;
		this.metaIno = metaIno;
		this.keyID = keyID;
		this.handle = handle;
	}
	
	public ObjectHandle getHandle() {
		return handle;
	}
	public byte[] getData() {
		return data;
	}
	public ContentMetaInfo getMetaIno() {
		return metaIno;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setMetaIno(ContentMetaInfo metaIno) {
		this.metaIno = metaIno;
	}

	public void setHandle(ObjectHandle handle) {
		this.handle = handle;
	}

	public KeyID getKeyID() {
		return keyID;
	}

	public void setKeyID(KeyID keyID) {
		this.keyID = keyID;
	}
	
}
