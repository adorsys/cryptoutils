package org.adorsys.encobject.domain;

import java.net.URI;
import java.util.Date;
import java.util.Map;

public interface StorageMetadata extends ResourceMetadata<StorageType> {
	public abstract StorageType getType();

	public abstract String getProviderId();

	public abstract String getName();

	public abstract URI getUri();

	public abstract Map<String, String> getUserMetadata();

	public abstract String getETag();

	public abstract Date getCreationDate();

	public abstract Date getLastModified();

	public abstract Long getSize();
}