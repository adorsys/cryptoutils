package org.adorsys.encobject.service;

import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;

/**
 * Created by peter on 05.03.18 at 16:37.
 */
public class VerySimpleEncryptionService implements EncryptionService{
    @Override
    public byte[] encrypt(byte[] data, KeySource keySource, KeyID keyID, Boolean compress) {
        byte[] output = new byte[data.length];
        for (int i = 0; i<data.length; i++) {
            output[i] = (byte) (255 - (int) data[i]);
        }
        return HexUtil.convertBytesToHexString(output).getBytes();
    }

    @Override
    public byte[] decrypt(byte[] data, KeySource keySource) {
        byte[] realData = HexUtil.convertHexStringToBytes(new String(data));
        byte[] output = new byte[realData.length];
        for (int i = 0; i<realData.length; i++) {
            output[i] = (byte) (255 - (int) realData[i]);
        }
        return output;
    }
}
