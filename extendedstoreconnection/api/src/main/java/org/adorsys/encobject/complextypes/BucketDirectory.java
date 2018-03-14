package org.adorsys.encobject.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 29.01.18 at 14:40.
 */
// Ist extra kein BucketPath, wenngleich es technisch so ist.
public class BucketDirectory {
    private BucketPath path;
    public BucketDirectory(String path) {
        this.path = new BucketPath(path);
    }

    public BucketDirectory(BucketPath bucketPath) {
        this.path = bucketPath;
    }

    public BucketDirectory append(BucketDirectory directory) {
        return new BucketDirectory(path.append(directory.path));
    }

    public BucketPath append(BucketPath bucketPath) {
        return this.path.append(bucketPath);
    }

    public BucketDirectory appendDirectory(String directory) {
        return append(new BucketDirectory(directory));
    }

    public BucketPath appendName(String name) {
        return append(new BucketPath(name));
    }

    public BucketPath addSuffix(String suffix) {
        return path.add(suffix);
    }

    public ObjectHandle getObjectHandle() {
        return path.getObjectHandle();
    }

    @Override
    public String toString() {
        return "BucketDirectory{" +
                "path=" + path +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BucketDirectory)) return false;

        BucketDirectory that = (BucketDirectory) o;

        return path.equals(that.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
