package org.adorsys.encobject.domain;

import java.net.URI;
import java.util.Map;

public abstract interface ResourceMetadata<T extends Enum<T>> extends Comparable<ResourceMetadata<T>> {
	public abstract T getType();

	public abstract String getProviderId();

	public abstract String getName();

	public abstract Location getLocation();

	public abstract URI getUri();

	public abstract Map<String, String> getUserMetadata();
}