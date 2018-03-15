package org.adorsys.encobject.domain;

import java.util.Date;

public interface StorageMetadata extends ResourceMetadata<StorageType> {

    public abstract String getETag();

    public abstract Date getCreationDate();

    public abstract Date getLastModified();

    // diese Attribute werden vom PersistenceLayer gesetzt bzw überschrieben
    // name und type liegen in ResourceMetadata

    public abstract Long getSize();

    public abstract Boolean getShouldBeCompressed();

    public abstract String getContentType();

}

