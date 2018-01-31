package org.adorsys.encobject.types;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.domain.ContentMetaInfo;

import java.util.HashMap;

/**
 * Created by peter on 24.01.18 at 09:15.
 */
public class PersistenceLayerContentMetaInfoUtil {
    private final static String KEYID = "KeyID";
    private final static String ENCRYPTIONN_TYPE = "EncryptionType";

    public static void setKeyID(ContentMetaInfo contentMetaInfo, KeyID keyID) {
        if (contentMetaInfo == null) {
            throw new BaseException("Programming error contentMetaInfo must not be null");
        }
        if (contentMetaInfo.getAddInfos() == null) {
            contentMetaInfo.setAddInfos(new HashMap<String, Object>());
        }
        contentMetaInfo.getAddInfos().put(KEYID, keyID.getValue());
    }
    public static void setEncryptionType(ContentMetaInfo contentMetaInfo, EncryptionType encryptionType) {
        if (contentMetaInfo == null) {
            throw new BaseException("Programming error contentMetaInfo must not be null");
        }
        if (contentMetaInfo.getAddInfos() == null) {
            contentMetaInfo.setAddInfos(new HashMap<String, Object>());
        }
        contentMetaInfo.getAddInfos().put(ENCRYPTIONN_TYPE, encryptionType);
    }

    public static KeyID getKeyID(ContentMetaInfo contentMetaInfo) {
        String content = (String) contentMetaInfo.getAddInfos().get(KEYID);
        return new KeyID(content);
    }

    public static EncryptionType getEncryptionnType(ContentMetaInfo contentMetaInfo) {
        String encryptionType = (String) contentMetaInfo.getAddInfos().get(ENCRYPTIONN_TYPE);
        return EncryptionType.valueOf(encryptionType);

    }
}
