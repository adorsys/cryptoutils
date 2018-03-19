package org.adorsys.encobject.complextypes;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 20.02.18 at 08:37.
 */
public class BucketPathUtil {
    public static String getAsString(BucketDirectory bucketDirectory) {
        String container = bucketDirectory.getObjectHandle().getContainer();
        String name = bucketDirectory.getObjectHandle().getName();
        if (name == null) {
            name = "";
        }

        return container + BucketPath.BUCKET_SEPARATOR + name;
    }

    public static String getAsString(BucketPath bucketPath) {
        String container = bucketPath.getObjectHandle().getContainer();
        String name = bucketPath.getObjectHandle().getName();
        if (name == null) {
            name = "";
        }

        return container + BucketPath.BUCKET_SEPARATOR + name;
    }

    public static void checkContainerName(String name) {
        if (name.indexOf(BucketPath.BUCKET_SEPARATOR) != -1) {
            throw new BaseException(name + " is not a valid container name. Must not contain " + BucketPath.BUCKET_SEPARATOR);
        }
    }
}
