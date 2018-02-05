package org.adorsys.encobject.domain;

/**
 * This is the description of a content meta info entry.
 * 
 * @author fpo
 *
 */
public class ContentInfoEntry {
	// The Version of the entry. Type might change.
	private String version;
	// The type of this entry. This is used to discover the factory 
	// used to load the value in the hosp process.
	private String type;
	// The String representation of the entry. Complex entries might
	// by stored as base64 encoded byte representation of the object.
	// Type information shall allow detailed description of the
	// Structure of this string.
	private String value;
	
	public ContentInfoEntry(String type, String version, String value) {
		super();
		this.type = type;
		this.version = version;
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
