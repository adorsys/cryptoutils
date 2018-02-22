package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;

import java.io.File;

/**
 * Created by peter on 21.02.18 at 19:35.
 */
public class BucketPathFileHelper {
    static public File getAsFile(BucketPath bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    static public File getAsFile(BucketDirectory bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    static public File getAsFile(ObjectHandle objectHandle) {
        String container = objectHandle.getContainer();
        String name = objectHandle.getName();
        String fullpath = container + BucketPath.BUCKET_SEPARATOR + name;
        return new File(fullpath);
    }
}
