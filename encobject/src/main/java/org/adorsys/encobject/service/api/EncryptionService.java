package org.adorsys.encobject.service.api;

import org.adorsys.encobject.types.KeyID;

/**
 * Created by peter on 22.02.18 at 17:40.
 */
public interface EncryptionService {
    /**
     * @param data die zu verschlüsselnden Daten als Binärfeld
     * @param keySource liefert zu einer übergebenen KeyID einen Key zurück
     * @param keyID die ID, des Schlüssels, mit dem verschlüsselt werden soll
     * @return daten, die aus einem unverschlüsseltem Header bestehen,
     */
    byte[] encrypt(byte[] data, KeySource keySource, KeyID keyID, Boolean compress);
    byte[] decrypt(byte[] data, KeySource keySource, KeyID keyID);
}


