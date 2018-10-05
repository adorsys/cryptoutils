package org.adorsys.cryptoutils.basetypes;

import java.io.Serializable;

/**
 * Created by peter on 21.02.17.
 */
public class BaseTypeLong implements Serializable {
    private Long value;

    protected BaseTypeLong() {
    }

    protected BaseTypeLong(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\'" + value + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTypeLong that = (BaseTypeLong) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}

