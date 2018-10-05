package org.adorsys.encobject.types.connection;

import org.adorsys.cryptoutils.basetypes.BaseTypePasswordString;

/**
 * Created by peter on 18.03.18 at 20:30.
 */
public class MinioSecretKey extends BaseTypePasswordString {
    public MinioSecretKey(String value) {
        super(value);
    }
}
