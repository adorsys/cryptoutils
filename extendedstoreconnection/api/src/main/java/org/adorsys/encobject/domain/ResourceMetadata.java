package org.adorsys.encobject.domain;

import java.net.URI;

public abstract interface ResourceMetadata<T extends Enum<T>> extends Comparable<ResourceMetadata<T>> {
	public abstract String getName();

	public abstract T getType();

	public abstract String getProviderID();

	public abstract Location getLocation();

	public abstract URI getUri();

	public abstract UserMetaData getUserMetadata();
}
