package org.adorsys.cryptoutils.basetypes;

import org.adorsys.cryptoutils.utils.HexUtil;

/**
 * Created by peter on 22.01.18 at 17:45.
 */
public class BaseTypePasswordByteArray extends BaseTypeByteArray {
    public BaseTypePasswordByteArray(byte[] value) {
        super(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\'" + BaseTypePasswordByteArray.hide(getValue()) + "\'}";
    }

    public final static String hide(byte[] bytes) {
        String value = HexUtil.convertBytesToHexString(bytes);
        if (value.length() > 8) {
            return value.substring(0, 4) + "...." + value.substring(value.length() - 4);
        }
        return "....";
    }

}
