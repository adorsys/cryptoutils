package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.domain.document.LocationData;
import org.adorsys.encobject.domain.document.StorageMetadataData;
import org.adorsys.encobject.domain.document.UserMetaDataData;
import org.adorsys.encobject.service.SimpleLocationImpl;
import org.adorsys.encobject.service.SimpleStorageMetadataImpl;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by peter on 21.02.18 at 09:12.
 */
public class StorageMetadataFlattenerProtoBuf {
    public static StorageMetadataData fromJavaToProtoBuf(StorageMetadata javaMeta) {
        StorageMetadataData.Builder protoBufBuilder = StorageMetadataData.newBuilder();
        {
            String name = javaMeta.getName();
            if (name != null) {
                protoBufBuilder.setName(name);
            }
        }
        {
            StorageType storageType = javaMeta.getType();
            if (storageType != null) {
                protoBufBuilder.setType(storageType.toString());
            }
        }
        {
            String providerID = javaMeta.getProviderID();
            if (providerID != null) {
                protoBufBuilder.setProviderid(providerID);
            }
        }
        {
            Location location = javaMeta.getLocation();
            if (location != null) {
                protoBufBuilder.setLocationData(fromJavaToProtoBuf(location));
            }
        }
        {
            URI uri = javaMeta.getUri();
            if (uri != null) {
                protoBufBuilder.setUri(uri.toString());
            }
        }
        {
            Map<String, String> map = new HashMap<>();
            for (String key : javaMeta.getUserMetadata().keySet()) {
                map.put(key, javaMeta.getUserMetadata().get(key));
            }
            UserMetaDataData userMetadataData = UserMetaDataData.newBuilder().putAllMap(map).build();
            protoBufBuilder.setUserMetaDataData(userMetadataData);
        }
        {
            String etag = javaMeta.getETag();
            if (etag != null) {
                protoBufBuilder.setEtag(etag);
            }
        }
        {
            Date creationDate = javaMeta.getCreationDate();
            if (creationDate != null) {
                protoBufBuilder.setCreationDate(creationDate.getTime());
            }
        }
        {
            Date modifiedDate = javaMeta.getLastModified();
            if (modifiedDate != null) {
                protoBufBuilder.setModifiedDate(modifiedDate.getTime());
            }
        }
        protoBufBuilder.setSize(javaMeta.getSize());
        return protoBufBuilder.build();
    }

    private static LocationData fromJavaToProtoBuf(Location javaLocation) {
        LocationData.Builder protoBufBuilder = LocationData.newBuilder();

        if (javaLocation.getScope() != null) {
            protoBufBuilder.setScope(javaLocation.getScope().toString());
        }
        if (javaLocation.getID() != null) {
            protoBufBuilder.setId(javaLocation.getID());
        }
        if (javaLocation.getDescription() != null) {
            protoBufBuilder.setDescription(javaLocation.getDescription());
        }
        {
            Iterator<String> iterator = javaLocation.getIso3166Codes().iterator();
            while (iterator.hasNext()) {
                protoBufBuilder.addIs3166Codes(iterator.next());
            }
        }
        Location parent = javaLocation.getParent();
        if (parent != null) {
            protoBufBuilder.setParent(fromJavaToProtoBuf(parent));
        }
        return protoBufBuilder.build();
    }

    public static StorageMetadata fromProtoBufToJava(StorageMetadataData protoBufMeta) {
        SimpleStorageMetadataImpl javaMeta = new SimpleStorageMetadataImpl();
        javaMeta.setName(protoBufMeta.getName());
        {
            String typestring = protoBufMeta.getType();
            if (!StringUtils.isBlank(typestring)) {
                javaMeta.setType(StorageType.valueOf(typestring));
            }
        }
        javaMeta.setProviderID(protoBufMeta.getProviderid());
        {
            LocationData protoBufLocation = protoBufMeta.getLocationData();
            if (protoBufLocation != null) {
                Location javaLocation = fromProtoBufToJava(protoBufLocation);
                javaMeta.setLocation(javaLocation);
            }
        }
        {
            String uristring = protoBufMeta.getUri();
            if (!StringUtils.isBlank(uristring)) {
                javaMeta.setUri(URI.create(uristring));
            }
        }
        {
            for (String key : protoBufMeta.getUserMetaDataData().getMapMap().keySet()) {
                javaMeta.getUserMetadata().put(key, protoBufMeta.getUserMetaDataData().getMapMap().get(key));
            }
        }
        javaMeta.setETag(protoBufMeta.getEtag());
        {
            Long creationDateLong = protoBufMeta.getCreationDate();
            if (creationDateLong != null) {
                javaMeta.setCreationDate(new Date(creationDateLong));
            }
        }
        {
            Long modifiedDate = protoBufMeta.getModifiedDate();
            if (modifiedDate != null) {
                javaMeta.setLastModified(new Date(modifiedDate));
            }
        }
        javaMeta.setSize(protoBufMeta.getSize());
        return javaMeta;
    }

    private static Location fromProtoBufToJava(LocationData protoBufLocation) {
        SimpleLocationImpl javaLocation = new SimpleLocationImpl();

        if (!StringUtils.isBlank(protoBufLocation.getScope())) {
            javaLocation.setScope(LocationScope.valueOf(protoBufLocation.getScope()));
        }
        if (!StringUtils.isBlank(protoBufLocation.getId())) {
            javaLocation.setId(protoBufLocation.getId());
        }
        if (!StringUtils.isBlank(protoBufLocation.getDescription())) {
            javaLocation.setDescription(protoBufLocation.getDescription());
        }
        {
            Iterator<String> protoBufIsoIterator = protoBufLocation.getIs3166CodesList().iterator();
            while (protoBufIsoIterator.hasNext()) {
                javaLocation.getIso3166Codes().add(protoBufIsoIterator.next());
            }
        }
        {
            if (protoBufLocation.hasParent()) {
                javaLocation.setParent(fromProtoBufToJava(protoBufLocation.getParent()));
            }
        }
        return javaLocation;
    }

}
