package org.adorsys.cryptoutils.basetypes;

import java.io.Serializable;

/**
 * Created by peter on 20.02.17.
 */
class BaseTypeInteger  implements Serializable {
    private Integer value;

    protected BaseTypeInteger() {
    }

    protected BaseTypeInteger(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void increase() {
        this.value = this.value + 1;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTypeInteger that = (BaseTypeInteger) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
