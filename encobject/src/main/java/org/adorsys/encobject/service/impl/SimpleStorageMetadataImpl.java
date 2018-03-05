package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.ResourceMetadata;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.domain.UserMetaData;

import java.net.URI;
import java.util.Date;

/**
 * Created by peter on 19.02.18 at 10:22.
 */
public class SimpleStorageMetadataImpl implements StorageMetadata {

    private StorageType storageType = null;
    private String providerID = null;
    private String name = null;
    private SimpleLocationImpl location = null;
    private URI uri = null;
    private UserMetaData userMetaData = new UserMetaData();
    private String eTag = null;
    private Date creationDate = null;
    private Date lastModified = null;
    private Long size = null;
    private Boolean shouldBeCompressed = null;
    private String contentType = null;

    public void addUserMetadata(UserMetaData userMetaData) {
        for (String key : userMetaData.keySet()) {
            this.userMetaData.put(key, userMetaData.get(key));
        }
    }

    public SimpleStorageMetadataImpl() {

    }

    public SimpleStorageMetadataImpl(StorageMetadata storageMetadata) {
        setType(storageMetadata.getType());
        setProviderID(storageMetadata.getProviderID());
        setName(storageMetadata.getName());
        if (storageMetadata.getLocation() != null) {
            setLocation(storageMetadata.getLocation()); // deepcopy wird beim setLocation gemacht
        }
        if (storageMetadata.getUri() != null) {
            setUri(URI.create(storageMetadata.getUri().toString()));
        }
        mergeUserMetadata(storageMetadata.getUserMetadata());
        setETag(storageMetadata.getETag());
        setCreationDate(storageMetadata.getCreationDate());
        setLastModified(storageMetadata.getLastModified());
        setSize(storageMetadata.getSize());
        setShouldBeCompressed(storageMetadata.getShouldBeCompressed());
        setContentType(storageMetadata.getContentType());
    }

    public void mergeUserMetadata(UserMetaData otherUserMetadata) {
        if (otherUserMetadata != null) {
            for (String key : otherUserMetadata.keySet()) {
                getUserMetadata().put(key, otherUserMetadata.get(key));
            }
        }
    }

    @Override
    public UserMetaData getUserMetadata() {
        return this.userMetaData;
    }

    @Override
    public StorageType getType() {
        return storageType;
    }

    public void setType(StorageType storageType) {
        this.storageType = storageType;
    }

    @Override
    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SimpleLocationImpl getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = new SimpleLocationImpl(location);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Long getSize() {
        return size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Boolean getShouldBeCompressed() {
        return shouldBeCompressed;
    }

    public void setShouldBeCompressed(Boolean shouldBeCompressed) {
        this.shouldBeCompressed = shouldBeCompressed;
    }


    @Override
    public String getContentType() {
        return contentType;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public int compareTo(ResourceMetadata<StorageType> o) {
        int myHashCode = hashCode();
        int otherHashCode = o.hashCode();
        if (myHashCode < otherHashCode)
            return -1;
        if (myHashCode > otherHashCode)
            return 1;
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleStorageMetadataImpl)) return false;

        SimpleStorageMetadataImpl that = (SimpleStorageMetadataImpl) o;

        if (storageType != that.storageType) return false;
        if (providerID != null ? !providerID.equals(that.providerID) : that.providerID != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getLocation() != null ? !getLocation().equals(that.getLocation()) : that.getLocation() != null)
            return false;
        if (getUri() != null ? !getUri().equals(that.getUri()) : that.getUri() != null) return false;
        if (userMetaData != null ? !userMetaData.equals(that.userMetaData) : that.userMetaData != null) return false;
        if (eTag != null ? !eTag.equals(that.eTag) : that.eTag != null) return false;
        if (getCreationDate() != null ? !getCreationDate().equals(that.getCreationDate()) : that.getCreationDate() != null)
            return false;
        if (getLastModified() != null ? !getLastModified().equals(that.getLastModified()) : that.getLastModified() != null)
            return false;
        return getSize() != null ? getSize().equals(that.getSize()) : that.getSize() == null;

    }

    @Override
    public int hashCode() {
        int result = storageType != null ? storageType.hashCode() : 0;
        result = 31 * result + (providerID != null ? providerID.hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getLocation() != null ? getLocation().hashCode() : 0);
        result = 31 * result + (getUri() != null ? getUri().hashCode() : 0);
        result = 31 * result + (userMetaData != null ? userMetaData.hashCode() : 0);
        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        result = 31 * result + (getCreationDate() != null ? getCreationDate().hashCode() : 0);
        result = 31 * result + (getLastModified() != null ? getLastModified().hashCode() : 0);
        result = 31 * result + (getSize() != null ? getSize().hashCode() : 0);
        return result;
    }
}
