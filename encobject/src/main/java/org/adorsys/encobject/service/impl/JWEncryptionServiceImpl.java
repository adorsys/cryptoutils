package org.adorsys.encobject.service.impl;

import com.nimbusds.jose.CompressionAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.exceptions.ExtendedPersistenceException;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.params.EncParamSelector;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.types.EncryptionType;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.PersistenceLayerContentMetaInfoUtil;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.apache.commons.io.IOUtils;

import java.security.Key;

/**
 * Created by peter on 22.02.18 at 17:50.
 */
public class JWEncryptionServiceImpl implements EncryptionService {
    private DefaultJWEDecrypterFactory decrypterFactory = new DefaultJWEDecrypterFactory();

    @Override
    public byte[] encrypt(byte[] data, KeySource keySource, KeyID keyID, Boolean compress) {
        try {
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            Key key = keySource.readKey(keyID);

            PersistenceLayerContentMetaInfoUtil.setKeyID(metaInfo, keyID);
            PersistenceLayerContentMetaInfoUtil.setEncryptionType(metaInfo, EncryptionType.JWE);
            EncryptionParams encParams = EncParamSelector.selectEncryptionParams(key);

            JWEHeader.Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID.getValue());
            ContentMetaInfoUtils.metaInfo2Header(metaInfo, headerBuilder);

            if (compress != null && compress) {
                headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
            }

            JWEHeader header = headerBuilder.build();
            JWEEncrypter jweEncrypter = JWEEncryptedSelector.getEncrypter(key, encParams.getEncAlgo(), encParams.getEncMethod());

            JWEObject jweObject = new JWEObject(header, new Payload(data));
            jweObject.encrypt(jweEncrypter);

            String jweEncryptedObject = jweObject.serialize();

            byte[] bytesToStore = jweEncryptedObject.getBytes("UTF-8");
            return bytesToStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] jweEncryptedBytes, KeySource keySource, KeyID keyID) {
        try {
            String jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");

            JWEObject jweObject = JWEObject.parse(jweEncryptedObject);
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            ContentMetaInfoUtils.header2MetaInfo(jweObject.getHeader(), metaInfo);
            EncryptionType encryptionType = PersistenceLayerContentMetaInfoUtil.getEncryptionnType(metaInfo);
            if (!encryptionType.equals(EncryptionType.JWE)) {
                throw new BaseException("Expected EncryptionType is " + EncryptionType.JWE + " but was " + encryptionType);
            }
            KeyID keyID2 = PersistenceLayerContentMetaInfoUtil.getKeyID(metaInfo);
            KeyID keyID3 = new KeyID(jweObject.getHeader().getKeyID());
            if (!keyID.equals(keyID2)) {
                throw new BaseException("die in der MetaInfo hinterlegte keyID " + keyID + " passt nicht zu der im header hinterlegten KeyID " + keyID2);
            }
            if (!keyID2.equals(keyID3)) {
                throw new BaseException("die in der MetaInfo hinterlegte keyID " + keyID2 + " passt nicht zu der im header hinterlegten KeyID " + keyID3);
            }
            Key key = keySource.readKey(keyID);

            if (key == null) {
                throw new ExtendedPersistenceException("can not read key with keyID " + keyID + " from keySource of class " + keySource.getClass().getName());
            }

            JWEDecrypter decrypter = decrypterFactory.createJWEDecrypter(jweObject.getHeader(), key);
            jweObject.decrypt(decrypter);
            return jweObject.getPayload().toBytes();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
