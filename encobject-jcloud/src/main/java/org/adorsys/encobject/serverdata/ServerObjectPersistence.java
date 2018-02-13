package org.adorsys.encobject.serverdata;

import com.nimbusds.jose.CompressionAlgorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncParamSelector;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.exceptions.ObjectNotFoundException;
import org.adorsys.encobject.exceptions.UnknownContainerException;
import org.adorsys.encobject.exceptions.WrongKeyCredentialException;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jjwk.serverkey.ServerKeyMapProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * A server might decide to store objects encrypted.
 * 
 * This class uses server secret keys to encrypt or decrypt those server objects.
 * 
 * @author fpo
 *
 */
public class ServerObjectPersistence {

	private DefaultJWEDecrypterFactory decrypterFactory = new DefaultJWEDecrypterFactory();

	private ExtendedStoreConnection blobStoreConnection;

	public ServerObjectPersistence(ExtendedStoreConnection storeConnection) {
		this.blobStoreConnection = storeConnection;
	}

	/**
	 * Uses the given keyId to encrypt and store data[].
	 *
	 * @param data data
	 * @param metaInfo metaIno
	 * @param handle handle
	 * @param keyMapProvider keyMapProvider
	 * @param keyID keyID
	 * @param encParams encParams
	 * @throws UnsupportedEncAlgorithmException UnsupportedEncAlgorithmException
	 * @throws UnsupportedKeyLengthException UnsupportedKeyLengthException
	 * @throws UnknownContainerException UnknownContainerException
	 */
	public void storeObject(byte[] data, ContentMetaInfo metaInfo, ObjectHandle handle, ServerKeyMapProvider keyMapProvider, String keyID, EncryptionParams encParams) throws UnsupportedEncAlgorithmException, UnsupportedKeyLengthException, UnknownContainerException {
		// We accept empty meta info
		if (metaInfo == null)
			metaInfo = new ContentMetaInfo();

		// Retrieve the key.
		Key key = keyMapProvider.getKey(keyID);

		// Encryption params is optional. If not provided, we select an
		// encryption param based on the key selected.
		if (encParams == null) {
			encParams = EncParamSelector.selectEncryptionParams(key);
		}

		Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID);

		// content type
		String contentTrype = metaInfo.getContentTrype();
		if (StringUtils.isNotBlank(contentTrype)) {
			headerBuilder = headerBuilder.contentType(contentTrype);
		}

		String zip = metaInfo.getZip();
		if (StringUtils.isNotBlank(zip)) {
			headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
		} else {
			if (StringUtils.isNotBlank(contentTrype) && StringUtils.containsIgnoreCase(contentTrype, "text")) {
				headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
			}
		}

		Map<String, Object> addInfos = metaInfo.getAddInfos();
		// exp
		if (metaInfo.getExp() != null) {
			if (addInfos == null)
				addInfos = new HashMap<>();
			addInfos.put("exp", metaInfo.getExp().getTime());
		}

		if (addInfos != null) {
			headerBuilder = headerBuilder.customParams(addInfos);
		}

		JWEHeader header = headerBuilder.build();

		JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(),
				encParams.getEncMethod());

		JWEObject jweObject = new JWEObject(header, new Payload(data));

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

	/**
	 * Loads the object stored under the given handle. Uses a server key to decrypt the object.
	 *
	 * @param handle handle
	 * @param keyMapProvider keyMapProvider
	 * @return byte[]
	 * @throws ObjectNotFoundException ObjectNotFoundException
	 * @throws WrongKeyCredentialException WrongKeyCredentialException
	 * @throws UnknownContainerException UnknownContainerException
	 */
	public byte[] loadObject(ObjectHandle handle, ServerKeyMapProvider keyMapProvider) throws UnknownContainerException, ObjectNotFoundException, WrongKeyCredentialException {
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
		Key key = keyMapProvider.getKey(keyID);

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
}
