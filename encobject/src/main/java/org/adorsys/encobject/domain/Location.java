package org.adorsys.encobject.domain;

import java.util.Map;
import java.util.Set;

public abstract interface Location {
	public abstract LocationScope getScope();

	public abstract String getId();

	public abstract String getDescription();

	public abstract Location getParent();

	public abstract Map<String, Object> getMetadata();

	public abstract Set<String> getIso3166Codes();
}