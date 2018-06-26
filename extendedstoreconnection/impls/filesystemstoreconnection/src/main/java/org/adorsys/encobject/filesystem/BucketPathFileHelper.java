package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;

import java.io.File;

/**
 * Created by peter on 21.02.18 at 19:35.
 */
public class BucketPathFileHelper {
    static public File getAsFile(BucketPath bucketPath, boolean absolute) {
        return getAsFile(bucketPath.getObjectHandle(), absolute);
    }

    static public File getAsFile(BucketDirectory bucketPath, boolean absolute) {
        return getAsFile(bucketPath.getObjectHandle(), absolute);
    }

    static public File getAsFile(ObjectHandle objectHandle, boolean absolute) {
        String container = objectHandle.getContainer();
        String name = objectHandle.getName();
        String fullpath = container + BucketPath.BUCKET_SEPARATOR + name;
        if (absolute) {
            fullpath = BucketPath.BUCKET_SEPARATOR + fullpath;
        }
        return new File(fullpath);
    }
}
