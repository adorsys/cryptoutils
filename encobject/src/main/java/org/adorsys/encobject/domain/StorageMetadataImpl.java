package org.adorsys.encobject.domain;

import java.net.URI;
import java.util.Date;
import java.util.Map;

public class StorageMetadataImpl extends ResourceMetadataImpl<StorageType> implements StorageMetadata {

	
	private final String eTag;

	
	private final Date creationDate;

	
	private final Date lastModified;
	private final StorageType type;

	
	private final Long size;

	public StorageMetadataImpl(StorageType type,  String id,  String name,
			 Location location,  URI uri,  String eTag,  Date creationDate,
			 Date lastModified, Map<String, String> userMetadata,  Long size) {
		super(id, name, location, uri, userMetadata);
		this.eTag = eTag;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
		if(type==null) throw new NullPointerException("type");
		this.type = type;
		this.size = size;
	}

	public StorageType getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((eTag == null) ? 0 : eTag.hashCode());
		result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageMetadataImpl other = (StorageMetadataImpl) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (eTag == null) {
			if (other.eTag != null)
				return false;
		} else if (!eTag.equals(other.eTag))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		
		return "StorageMetadataImpl [eTag=" + eTag + ", creationDate=" + creationDate + ", lastModified=" + lastModified
				+ ", type=" + type + ", size=" + size + "]" + " - " + super.toString();
	}

	public String getETag() {
		return this.eTag;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public Long getSize() {
		return this.size;
	}

}
