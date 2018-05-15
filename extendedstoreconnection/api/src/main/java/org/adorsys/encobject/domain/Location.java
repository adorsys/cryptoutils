package org.adorsys.encobject.domain;

import java.util.Set;

public abstract interface Location {
	public abstract LocationScope getScope();

	public abstract String getID();

	public abstract String getDescription();

	public abstract Location getParent();

	public abstract Set<String> getIso3166Codes();
}
