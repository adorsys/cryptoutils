package org.adorsys.cryptoutils.basetypes;

/**
 * Created by peter on 22.01.18 at 17:24.
 */
public class BaseTypePasswordString extends BaseTypeString {
    public BaseTypePasswordString(String value) {
        super(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\'" + hide(getValue()) + "\'}";
    }

    private final static String hide(String value) {
        if (value.length() > 4) {
            return value.substring(0,2) + "***" + value.substring(value.length()-2);
        }
        return "***";
    }
}
