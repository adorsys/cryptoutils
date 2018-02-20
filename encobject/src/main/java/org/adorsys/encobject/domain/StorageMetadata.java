package org.adorsys.encobject.domain;

import java.util.Date;

public interface StorageMetadata extends ResourceMetadata<StorageType> {

    public abstract String getETag();

    public abstract Date getCreationDate();

    public abstract Date getLastModified();

    // diese Attribute werden vom PersistenceLayer gesetzt bzw überschrieben
    // name und type liegen in ResourceMetadata

    public abstract Long getSize();

    // diese setter werden vom PersistenzLayer benötigt

    public abstract void setSize(Long size);

    public abstract void setName(String name);

    public abstract void setType(StorageType type);

}

