package org.adorsys.encobject.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class LocationImpl implements Location {
	private final LocationScope scope;
	private final String id;
	private final String description;
	private final Location parent;
	private final Set<String> iso3166Codes;
	private final Map<String, Object> metadata;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "LocationImpl [scope=" + scope + ", id=" + id + ", description=" + description + ", parent=" + parent
				+ ", iso3166Codes=" + iso3166Codes + ", metadata=" + metadata + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationImpl other = (LocationImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (scope != other.scope)
			return false;
		return true;
	}

	public LocationImpl(LocationScope scope, String id, String description, Location parent,
			Set<String> iso3166Codes, Map<String, Object> metadata) {
		checkNotNull(scope, "scope");
		this.scope = scope;
		checkNotNull(id, "id");
		this.id = id;
		checkNotNull(description, "description");
		this.description = description;
		checkNotNull(metadata, "metadata");
		this.metadata = Collections.unmodifiableMap(metadata);
		checkNotNull(iso3166Codes, "iso3166Codes");
		this.iso3166Codes = Collections.unmodifiableSet(iso3166Codes);
		this.parent = parent;
	}

	private void checkNotNull(Object o, String string) {
		if(o==null) throw new NullPointerException(string);
	}

	public LocationScope getScope() {
		return this.scope;
	}

	public String getId() {
		return this.id;
	}

	public String getDescription() {
		return this.description;
	}

	public Location getParent() {
		return this.parent;
	}

	public Set<String> getIso3166Codes() {
		return this.iso3166Codes;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}
}