package org.adorsys.encobject.domain;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public abstract class ResourceMetadataImpl<T extends Enum<T>> implements ResourceMetadata<T> {

	private final String providerId;

	private final String name;

	private final Location location;

	private final URI uri;
	private final Map<String, String> userMetadata = new LinkedHashMap<>();

	public ResourceMetadataImpl(String providerId, String name, Location location,
			URI uri, Map<String, String> userMetadata) {
		this.providerId = providerId;
		this.name = name;
		this.location = location;
		this.uri = uri;
		Set<Entry<String,String>> entrySet = userMetadata.entrySet();
		for (Entry<String, String> entry : entrySet) {
			if(entry.getKey()!=null && entry.getValue()!=null){
				this.userMetadata.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public int compareTo(ResourceMetadata<T> that) {
		return StringUtils.compare(name, that.getName());
	}

	public String getProviderId() {
		return this.providerId;
	}

	public String getName() {
		return this.name;
	}

	public Location getLocation() {
		return this.location;
	}

	public URI getUri() {
		return this.uri;
	}

	public Map<String, String> getUserMetadata() {
		return this.userMetadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((providerId == null) ? 0 : providerId.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceMetadataImpl other = (ResourceMetadataImpl) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (providerId == null) {
			if (other.providerId != null)
				return false;
		} else if (!providerId.equals(other.providerId))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResourceMetadataImpl [providerId=" + providerId + ", name=" + name + ", location=" + location + ", uri="
				+ uri + ", userMetadata=" + userMetadata + "]";
	}

}