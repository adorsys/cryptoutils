package org.adorsys.encobject.domain;

import java.net.URI;
import java.util.Date;

public interface StorageMetadata extends ResourceMetadata<StorageType> {
	public abstract StorageType getType();

	public abstract String getProviderID();

	public abstract String getName();

	public abstract URI getUri();

	public abstract UserMetaData getUserMetadata();

	public abstract String getETag();

	public abstract Date getCreationDate();

	public abstract Date getLastModified();

	public abstract Long getSize();

	// diese setter werden von jeder Implementierung einer ExtendedStoreConnection benötigt
	public abstract void setSize(Long size);

	public abstract void setType(StorageType type);

	public abstract void setName(String name);
}

