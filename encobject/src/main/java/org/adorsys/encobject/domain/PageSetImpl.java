package org.adorsys.encobject.domain;

import java.util.LinkedHashSet;
import java.util.Set;

public class PageSetImpl<T> extends LinkedHashSet<T> implements PageSet<T> {
	protected final String marker;

	public PageSetImpl(Set<? extends T> contents, String nextMarker) {
		addAll(contents);
		this.marker = nextMarker;
	}

	public String getNextMarker() {
		return this.marker;
	}

	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = 31 * result + ((this.marker == null) ? 0 : this.marker.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(super.equals(obj)))
			return false;
		if (super.getClass() != obj.getClass())
			return false;
		PageSetImpl other = (PageSetImpl) obj;
		if (this.marker == null)
			if (other.marker != null)
				return false;
			else if (!(this.marker.equals(other.marker)))
				return false;
		return true;
	}

}
