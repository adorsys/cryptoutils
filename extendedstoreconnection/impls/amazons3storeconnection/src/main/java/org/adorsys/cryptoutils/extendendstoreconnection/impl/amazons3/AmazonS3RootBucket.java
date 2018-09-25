package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 25.09.18.
 * Entspricht einem pyhsischen Bucket, dieser ist für die Benutzer der Schnittstelle transaprent. Alle in
 * der Schnittstelle angelegten Buckets liegen unterhalb dieses Buckets
 */
public class AmazonS3RootBucket extends BaseTypeString {
    public AmazonS3RootBucket(String s) {
        super (s);
    }
}
