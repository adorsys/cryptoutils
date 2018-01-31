package org.adorsys.encobject.domain;

import java.util.Set;

public interface PageSet<T> extends Set<T> {
	public String getNextMarker();
}