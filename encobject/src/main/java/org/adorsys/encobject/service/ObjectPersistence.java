package org.adorsys.encobject.service;

import com.nimbusds.jose.CompressionAlgorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.exceptions.ExtendedPersistenceException;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.params.EncParamSelector;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.types.EncryptionType;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.encobject.types.PersistenceLayerContentMetaInfoUtil;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ObjectPersistence {

	private DefaultJWEDecrypterFactory decrypterFactory = new DefaultJWEDecrypterFactory();
	
	private final StoreConnection blobStoreConnection;

	public ObjectPersistence(StoreConnection blobStoreConnection) {
		this.blobStoreConnection = blobStoreConnection;
	}

	public void storeObject(byte[] data, ContentMetaInfo metaIno, ObjectHandle handle, KeyStore keyStore, String keyID, CallbackHandler keyPassHandler, EncryptionParams encParams) throws UnsupportedEncAlgorithmException, WrongKeyCredentialException, UnsupportedKeyLengthException, UnknownContainerException {
		// We accept empty meta info
		if(metaIno==null) metaIno=new ContentMetaInfo();
		
		// Retrieve the key.
		Key key = readKey(keyStore, keyID, keyPassHandler);
		
		// Encryption params is optional. If not provided, we select an encryption param based on the key selected.
		if(encParams==null){
			encParams=EncParamSelector.selectEncryptionParams(key);
		}
		
		Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID);

		// content type
		String contentTrype = metaIno.getContentTrype();
		if(StringUtils.isNotBlank(contentTrype)){
			headerBuilder = headerBuilder.contentType(contentTrype);
		}
		
		String zip = metaIno.getZip();
		if(StringUtils.isNotBlank(zip)){
			headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
		} else {
			if(StringUtils.isNotBlank(contentTrype) && StringUtils.containsIgnoreCase(contentTrype, "text")){
				headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
			}
		}
		
		Map<String, Object> addInfos = metaIno.getAddInfos();
		// exp
		if(metaIno.getExp()!=null){
			if(addInfos==null) addInfos = new HashMap<>();
			addInfos.put("exp", metaIno.getExp().getTime());
		}
		
		if(addInfos!=null){
			headerBuilder = headerBuilder.customParams(addInfos);
		}
		
		JWEHeader header = headerBuilder.build();
		
		JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(), encParams.getEncMethod());
		
		JWEObject jweObject = new JWEObject(header,new Payload(data));
		
		try {
			jweObject.encrypt(jweEncrypter);
		} catch (JOSEException e) {
			throw new IllegalStateException("Encryption error", e);
		}
		
		String jweEncryptedObject = jweObject.serialize();
		
		byte[] bytesToStore;
		try {
			bytesToStore = jweEncryptedObject.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported content type", e);
		}
		blobStoreConnection.putBlob(handle, bytesToStore);

	}

	public byte[] loadObject(ObjectHandle handle, KeyStore keyStore, CallbackHandler keyPassHandler) throws ObjectNotFoundException, WrongKeyCredentialException, UnknownContainerException{

		byte[] jweEncryptedBytes = blobStoreConnection.getBlob(handle);
		String jweEncryptedObject;
		try {
			jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException("Unsupported content type", e);
		}
		JWEObject jweObject;
		try {
			jweObject = JWEObject.parse(jweEncryptedObject);
		} catch (ParseException e) {
			throw new IllegalStateException("Can not parse jwe object", e);
		}
		String keyID = jweObject.getHeader().getKeyID();
		Key key = readKey(keyStore, keyID, keyPassHandler);

		JWEDecrypter decrypter;
		try {
			decrypter = decrypterFactory.createJWEDecrypter(jweObject.getHeader(), key);
		} catch (JOSEException e) {
			throw new IllegalStateException("No suitable key found", e);
		}
		try {
			jweObject.decrypt(decrypter);
		} catch (JOSEException e) {
			throw new WrongKeyCredentialException(e);
		}
		return jweObject.getPayload().toBytes();
	}
	
    /**
     * Encrypt and stores an byte array given additional meta information and encryption details.
     *
     * @param data      : unencrypted version of bytes to store
     * @param metaInfo  : document meta information. e.g. content type, compression, expiration
     * @param location  : location of the document. Includes container name (bucket) and file name.
     * @param keySource : key producer. Return a key given the keyId
     * @param keyID     : id of the key to be used from source to encrypt the docuement.
     * @param encParams
     */
    public void storeObject(byte[] data, ContentMetaInfo metaInfo, ObjectHandle location, KeySource keySource, KeyID keyID,
                            EncryptionParams encParams, OverwriteFlag overwrite) {

        try {

            // We accept empty meta info
            if (metaInfo == null) metaInfo = new ContentMetaInfo();

            // Retrieve the key.
            Key key = keySource.readKey(keyID);

            PersistenceLayerContentMetaInfoUtil.setKeyID(metaInfo, keyID);
            PersistenceLayerContentMetaInfoUtil.setEncryptionType(metaInfo, EncryptionType.JWE);

            // Encryption params is optional. If not provided, we select an
            // encryption param based on the key selected.
            if (encParams == null) encParams = EncParamSelector.selectEncryptionParams(key);

            Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID.getValue());
            ContentMetaInfoUtils.metaInfo2Header(metaInfo, headerBuilder);

            JWEHeader header = headerBuilder.build();

            JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(),
                    encParams.getEncMethod());

            JWEObject jweObject = new JWEObject(header, new Payload(data));
            jweObject.encrypt(jweEncrypter);

            String jweEncryptedObject = jweObject.serialize();

            byte[] bytesToStore = jweEncryptedObject.getBytes("UTF-8");

            if (overwrite == OverwriteFlag.FALSE) {
            	boolean blobExists = blobStoreConnection.blobExists(location);
            	if (blobExists) {
            		throw new FileExistsException("File " + location.getContainer() + " " + location.getName() + " already exists");
            	}
            }
            blobStoreConnection.putBlob(location, bytesToStore);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    public PersistentObjectWrapper loadObject(ObjectHandle location, KeySource keySource) {

        try {
            if (location == null)
                throw new ExtendedPersistenceException("Location for Object must not be null.");

            byte[] jweEncryptedBytes = blobStoreConnection.getBlob(location);
            String jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");

            JWEObject jweObject = JWEObject.parse(jweEncryptedObject);
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            ContentMetaInfoUtils.header2MetaInfo(jweObject.getHeader(), metaInfo);
            EncryptionType encryptionType = PersistenceLayerContentMetaInfoUtil.getEncryptionnType(metaInfo);
            if (! encryptionType.equals(EncryptionType.JWE)) {
                throw new BaseException("Expected EncryptionType is " + EncryptionType.JWE + " but was " + encryptionType);
            }
            KeyID keyID = PersistenceLayerContentMetaInfoUtil.getKeyID(metaInfo);
            KeyID keyID2 = new KeyID(jweObject.getHeader().getKeyID());
            if (!keyID.equals(keyID2)) {
                throw new BaseException("die in der MetaInfo hinterlegte keyID " + keyID + " passt nicht zu der im header hinterlegten KeyID " + keyID2);
            }
            Key key = keySource.readKey(keyID);

            if (key == null) {
                throw new ExtendedPersistenceException("can not read key with keyID " + keyID + " from keySource of class " + keySource.getClass().getName());
            }

            JWEDecrypter decrypter = decrypterFactory.createJWEDecrypter(jweObject.getHeader(), key);
            jweObject.decrypt(decrypter);
            return new PersistentObjectWrapper(jweObject.getPayload().toBytes(), metaInfo, keyID, location);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
	
	/*
	 * Retrieves the key with the given keyID from the keystore. The key password will be retrieved by
	 * calling the keyPassHandler.
	 */
	private Key readKey(KeyStore keyStore, String keyID, CallbackHandler keyPassHandler) throws WrongKeyCredentialException {
		try {
			return keyStore.getKey(keyID, PasswordCallbackUtils.getPassword(keyPassHandler, keyID));
		} catch (UnrecoverableKeyException e) {
			throw new WrongKeyCredentialException(e);
		} catch (KeyStoreException e) {
			throw new WrongKeyCredentialException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}		
	}
}
