package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by peter on 19.02.18 at 10:28.
 */
public class SimpleLocationImpl implements Location {
    private Location parent = null;
    private LocationScope locationScope = null;
    private String id = null;
    private String description = null;
    private Set<String> iso3166Codes = new HashSet<>();

    public SimpleLocationImpl() {

    }

    public SimpleLocationImpl(Location location) {
        if (location.getParent() != null) {
            parent = new SimpleLocationImpl(location.getParent());
        }
        locationScope = location.getScope();
        id = location.getID();
        description = location.getDescription();
        Iterator<String> iterator = location.getIso3166Codes().iterator();
        while (iterator.hasNext()) {
            getIso3166Codes().add(iterator.next());
        }
    }

    @Override
    public LocationScope getScope() {
        return locationScope;
    }

    public void setScope(LocationScope locationScope) {
        this.locationScope = locationScope;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Location getParent() {
        return parent;
    }

    @Override
    public Set<String> getIso3166Codes() {
        return iso3166Codes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIso3166Codes(Set<String> iso3166Codes) {
        this.iso3166Codes = iso3166Codes;
    }

    public void setParent(Location parent) {
        this.parent = parent;
    }
}
