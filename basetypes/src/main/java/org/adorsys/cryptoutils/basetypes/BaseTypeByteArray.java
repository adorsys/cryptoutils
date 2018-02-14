package org.adorsys.cryptoutils.basetypes;

import org.adorsys.cryptoutils.utils.HexUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by peter on 23.12.17 at 17:25.
 */
public class BaseTypeByteArray implements Serializable {
    private byte[] value;

    protected BaseTypeByteArray() {}

    protected BaseTypeByteArray(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value='" + HexUtil.convertBytesToHexString(value) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTypeByteArray that = (BaseTypeByteArray) o;

        return Arrays.equals(value, that.value);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
