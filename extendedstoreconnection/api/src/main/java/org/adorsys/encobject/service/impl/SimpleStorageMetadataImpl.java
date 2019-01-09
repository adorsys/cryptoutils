package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.ResourceMetadata;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.domain.UserMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peter on 19.02.18 at 10:22.
 */
public class SimpleStorageMetadataImpl implements StorageMetadata {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleStorageMetadataImpl.class);

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
        return compareStorageMetadata(this, that);
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

    private boolean compareStorageMetadata(StorageMetadata m1, StorageMetadata m2) {
        try {
            if (!compareStrings(m1.getName(), m2.getName(), "Name")) {
                return false;
            }
            {
                StorageType storageType1 = m1.getType();
                StorageType storageType2 = m2.getType();
                if (storageType1 != null) {
                    if (!storageType1.equals(storageType2)) {
                        LOGGER.debug("Storagetype is not equal: expected(" + storageType1 + ") compared(" + storageType2 + ")");
                        return false;
                    }
                } else {
                    if (storageType2 != null) {
                        LOGGER.debug("Storagetype is not equal: expected(null) compared(" + storageType2 + ")");
                        return false;
                    }
                }
            }
            if (!compareStrings(m1.getProviderID(), m2.getProviderID(), "ProviderID")) {
                return false;
            }
            if (!compareLocation(m1.getLocation(), m2.getLocation())) {
                return false;
            }
            if (m1.getLocation() != null) {
                if (!compareLocation(m1.getLocation().getParent(), m2.getLocation().getParent())) {
                    return false;
                }
            }
            {
                URI r1 = m1.getUri();
                URI r2 = m2.getUri();
                if (r1 != null) {
                    if (r2 == null) {
                        LOGGER.debug("URI is not equal: expected(" + r1.toString() + ") compared(null)");
                        return false;
                    } else {
                        if (!compareStrings(r1.toString(), r2.toString(), "URI")) {
                            return false;
                        }
                    }
                } else {
                    if (r2 != null) {
                        LOGGER.debug("URI is not equal: expected(null) compared(" + r2.toString() + ")");
                        return false;
                    }
                }
            }
            {
                if (m1.getUserMetadata().keySet().size() != m2.getUserMetadata().keySet().size()) {
                    LOGGER.debug("User MetaData number elements not equal: expected(" + m1.getUserMetadata().keySet().size() + ") compared("
                            + m2.getUserMetadata().keySet().size() + ")");
                    return false;
                } else {
                    String key = m1.getUserMetadata().keySet().iterator().next();
                    if (!compareStrings(m1.getUserMetadata().get(key), m2.getUserMetadata().get(key), "User MetaData value for key " + key)) {
                        return false;
                    }
                }

            }

            if (!compareStrings(m1.getETag(), m2.getETag(), "ETag")) {
                return false;
            }
            if (!compareDate(m1.getCreationDate(), m2.getCreationDate(), "Creation Date")) {
                return false;
            }
            if (!compareDate(m1.getLastModified(), m2.getLastModified(), "Last modified Date")) {
                return false;
            }
            if (!compareStrings(m1.getSize().toString(), m2.getSize().toString(), "Size")) {
                return false;
            }
            if (!compareStrings(m1.getShouldBeCompressed().toString(), m2.getShouldBeCompressed().toString(), "should be compressed")) {
                return false;
            }
            if (!compareStrings(m1.getContentType(), m2.getContentType(), "content type")) {
                return false;
            }
            return true;
        } catch (
                Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private boolean compareStrings(String expectedValue, String comparedValue, String description) {
        if (expectedValue == null) {
            if (comparedValue != null) {
                LOGGER.debug(description + " is not equal: expected(null) compared(" + comparedValue + ")");
                return false;
            }
        } else {
            if (!expectedValue.equals(comparedValue)) {
                LOGGER.debug(description + " is not equal: expected(" + expectedValue + ") compared(" + comparedValue + ")");
                return false;
            }
        }
        return true;
    }

    private boolean compareLocation(Location l1, Location l2) {
        LocationScope lsc1 = l1.getScope();
        LocationScope lsc2 = l2.getScope();
        if (lsc1 == null) {
            if (lsc2 != null) {
                LOGGER.debug("Location scope is not equal: expected(null) compared(" + lsc2.name() + ")");
                return false;

            }
        } else {
            if (lsc2 == null) {
                LOGGER.debug("Location scope is not equal: expected(" + lsc1.name() + ") compared(null)");
                return false;
            } else {
                if (!compareStrings(lsc1.name(), lsc2.name(), "Location scope")) {
                    return false;
                }
            }
        }
        if (!compareStrings(l1.getID(), l2.getID(), "Location ID")) {
            return false;
        }
        if (!compareStrings(l1.getDescription(), l2.getDescription(), "Location Description")) {
            return false;
        }
        {
            if (!l1.getIso3166Codes().equals(l2.getIso3166Codes())) {
                LOGGER.debug("number Iso4166 Codecs differ");
                return false;
            }
        }
        return true;
    }

    private boolean compareDate(Date expected, Date compared, String description) {
        if (expected != null) {
            if (!expected.equals(compared)) {
                LOGGER.debug(description + " is not equal: expected(" + expected + ") compared(" + compared + ")");
                return false;
            }
        } else {
            if (compared != null) {
                LOGGER.debug(description + " is not equal: expected(null) compared(" + compared + ")");
                return false;
            }
        }
        return true;
    }
}