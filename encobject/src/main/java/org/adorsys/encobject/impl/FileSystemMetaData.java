package org.adorsys.encobject.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.ResourceMetadata;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Created by peter on 06.02.18 at 16:01.
 */
public class FileSystemMetaData implements StorageMetadata {
    private String name;

    public FileSystemMetaData(String name) {
        this.name = name;
    }

    @Override
    public StorageType getType() {
        throw new BaseException("NYI");
    }

    @Override
    public String getProviderId() {
        throw new BaseException("NYI");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        throw new BaseException("NYI");
    }

    @Override
    public URI getUri() {
        throw new BaseException("NYI");
    }

    @Override
    public Map<String, String> getUserMetadata() {
        throw new BaseException("NYI");
    }

    @Override
    public String getETag() {
        throw new BaseException("NYI");
    }

    @Override
    public Date getCreationDate() {
        throw new BaseException("NYI");
    }

    @Override
    public Date getLastModified() {
        throw new BaseException("NYI");
    }

    @Override
    public Long getSize() {
        throw new BaseException("NYI");
    }

    @Override
    // TODO
    public int compareTo(ResourceMetadata<StorageType> o) {
        return o.getName().compareTo(getName());
    }

    @Override
    public String toString() {
        return "FileSystemMetaData{" +
                "name='" + name + '\'' +
                '}';
    }
}
