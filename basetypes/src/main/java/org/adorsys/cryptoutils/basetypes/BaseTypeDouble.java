package org.adorsys.cryptoutils.basetypes;

import java.io.Serializable;

/**
 * Created by peter on 20.02.17.
 */
class BaseTypeDouble implements Serializable {
    private Double value;

    protected BaseTypeDouble() {
    }

    protected BaseTypeDouble(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value=" + value +
                '}';
    }

}
