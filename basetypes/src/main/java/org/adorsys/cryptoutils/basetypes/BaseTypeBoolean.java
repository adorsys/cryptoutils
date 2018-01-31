package org.adorsys.cryptoutils.basetypes;

import java.io.Serializable;

/**
 * Created by peter on 06.03.17.
 */
public class BaseTypeBoolean implements Serializable {
    private Boolean value;

    protected BaseTypeBoolean() {
    }

    protected BaseTypeBoolean(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value=" + value +
                '}';
    }

}
