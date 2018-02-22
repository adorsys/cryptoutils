package org.adorsys.encobject.filesystem;

import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.SimpleLocationImpl;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.adorsys.encobject.service.SimpleStorageMetadataImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 20.02.18 at 16:53.
 */
public class StorageMetaDataTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(StorageMetaDataTest.class);
    private List<String> containers = new ArrayList<>();

    @Before
    public void before() {
        containers.clear();
    }

    @After
    public void after() {
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        for (String c : containers) {
            try {
                LOGGER.debug("AFTER TEST DELETE CONTAINER " + c);
                s.deleteContainer(c);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Laden der StorageMetaData direkt
     */
    @Test
    public void testStorageMetaData() {
        String container = "storageMetaData/1";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        StorageMetadata storageMetadata = createStorageMetadata();
        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "Inhalt".getBytes());
        s.putBlob(filea, origPayload);

        StorageMetadata loadedStorageMetadata = s.getStorageMetadata(filea);
        int fehler = compareStorageMetadata(storageMetadata, loadedStorageMetadata);

        LOGGER.info("Es werden drei Fehler erwartet für Name, StorageType und Size");
        Assert.assertEquals("number of fehlers", 3, fehler);
    }


    @Test
    public void jsonTest() {
        StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();
        StorageMetadata storageMetadata = createStorageMetadata();
        String jsonString = gsonHelper.toJson(storageMetadata);
        LOGGER.debug(jsonString);
        StorageMetadata reloadedStorageMetadata = gsonHelper.fromJson(jsonString);
        int fehler = compareStorageMetadata(storageMetadata, reloadedStorageMetadata);
        Assert.assertEquals("number of fehler", 0, fehler);
    }


    public static final String PATTERN = "yyyy-MM-dd-HH:mm:ss";
    public static final String NAME_VALUE = "Name is Peter";
    public static final String STORAGE_TYPE = "FOLDER";
    public static final String PROVIDER_ID_VALUE = "Die ProviderID 1";
    public static final String LOCATION_ID_0 = "LocationID0";
    public static final String LOCATION_0_DESCRIPTION = "any description of parent location";
    public static final String LOCATION_ID_1 = "LocationID1";
    public static final String LOCATION_1_DESCRIPTION = "any description of this location";
    public static final String LOCATION_0_SCOPE = "HOST";
    public static final String LOCATION_1_SCOPE = "SYSTEM";
    public static final String URI_VALUE = "www.electronicpeter.de";
    public static final String JSON_STRING_ELEMENT = "{\"quote\":\"VALUE\"} ";
    public static final String ETAG_VALUE = "DAs legendäre ETAG";
    public static final String CREATION_DATE = "2018-02-20-14:54:18";
    public static final String MODIFIED_DATE = "2018-02-20-14:54:41";
    public static final String ISO3166_CODE = "Iso3166Code";
    public static final int NUMBER_OF_ISOCODECS = 5;
    public static final String SHOULD_BE_COMPRESSED = "true";
    public static final String CONTENT_TYPE = "application/xml";


    private StorageMetadata createStorageMetadata() {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            // ResourceMetadata
            {
                storageMetadata.setName(NAME_VALUE);
                storageMetadata.setType(StorageType.valueOf(STORAGE_TYPE));
                storageMetadata.setProviderID(PROVIDER_ID_VALUE);
                {
                    SimpleLocationImpl parentLocation = new SimpleLocationImpl();
                    parentLocation.setId(LOCATION_ID_0);
                    parentLocation.setDescription(LOCATION_0_DESCRIPTION);
                    parentLocation.setScope(LocationScope.valueOf(LOCATION_0_SCOPE));

                    SimpleLocationImpl location = new SimpleLocationImpl();
                    location.setId(LOCATION_ID_1);
                    location.setDescription(LOCATION_1_DESCRIPTION);
                    location.setScope(LocationScope.valueOf(LOCATION_1_SCOPE));
                    location.setParent(parentLocation);

                    for (int i = 0; i < NUMBER_OF_ISOCODECS; i++) {
                        location.getIso3166Codes().add(ISO3166_CODE + i);
                    }
                    storageMetadata.setLocation(location);


                }
                storageMetadata.setUri(java.net.URI.create(URI_VALUE));
                for (int i = 0; i < 10; i++) {
                    storageMetadata.getUserMetadata().put("key_" + i + " mit json elementen: " + JSON_STRING_ELEMENT, JSON_STRING_ELEMENT);
                }
            }
            // StorageMetaData
            storageMetadata.setETag(ETAG_VALUE);
            storageMetadata.setCreationDate(sdf.parse(CREATION_DATE));
            storageMetadata.setLastModified(sdf.parse(MODIFIED_DATE));
            storageMetadata.setSize(new Long(1111));
            storageMetadata.setContentType(CONTENT_TYPE);
            storageMetadata.setShouldBeCompressed(new Boolean(SHOULD_BE_COMPRESSED));
            return storageMetadata;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private int compareStorageMetadata(StorageMetadata m1, StorageMetadata m2) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
            // ResourceMetadata
            int fehler = 0;
            fehler += compareStrings(m1.getName(), m2.getName(), NAME_VALUE, "Name");
            {
                StorageType storageType1 = m1.getType();
                StorageType storageType2 = m2.getType();
                if (!storageType1.equals(storageType2) || !storageType1.equals(StorageType.valueOf(STORAGE_TYPE))) {
                    LOGGER.debug("StorageType nicht korrekt");
                    fehler += 1;
                }
            }
            fehler += compareStrings(m1.getProviderID(), m2.getProviderID(), PROVIDER_ID_VALUE, "ProviderID");
            {
                {
                    Location l1 = m1.getLocation();
                    Location l2 = m2.getLocation();
                    {
                        {
                            LocationScope lsc1 = l1.getScope();
                            LocationScope lsc2 = l2.getScope();
                            if (!lsc1.equals(lsc2) || !lsc1.equals(LocationScope.valueOf(LOCATION_1_SCOPE))) {
                                LOGGER.debug("Location Scope nicht korrekt");
                                fehler += 1;
                            }
                        }
                        fehler += compareStrings(l1.getID(), l2.getID(), LOCATION_ID_1, "Location ID");
                        fehler += compareStrings(l1.getDescription(), l2.getDescription(), LOCATION_1_DESCRIPTION, "Location Description");
                        {
                            if (l1.getIso3166Codes().size() != l2.getIso3166Codes().size() || l1.getIso3166Codes().size() != NUMBER_OF_ISOCODECS) {
                                LOGGER.debug("Anzahl der Iso4166 Codecs in Location falsch: " + l1.getIso3166Codes().size() + " "
                                        + l2.getIso3166Codes().size() + " "
                                        + NUMBER_OF_ISOCODECS);
                                fehler += 1;
                            }
                        }
                    }
                    {
                        l1 = l1.getParent();
                        l2 = l2.getParent();
                        if (l2 == null) {
                            LOGGER.debug("Parent Location ist nicht gesetzt.");
                            fehler += 1;
                        } else {
                            {
                                LocationScope lsc1 = l1.getScope();
                                LocationScope lsc2 = l2.getScope();
                                if (!lsc1.equals(lsc2) || !lsc1.equals(LocationScope.valueOf(LOCATION_0_SCOPE))) {
                                    LOGGER.debug("Parent Location Scope nicht korrekt");
                                    fehler += 1;
                                }
                            }
                            fehler += compareStrings(l1.getID(), l2.getID(), LOCATION_ID_0, "Parent Location ID");
                            fehler += compareStrings(l1.getDescription(), l2.getDescription(), LOCATION_0_DESCRIPTION, "Parent Location Description");
                            {
                                if (l1.getIso3166Codes().size() != l2.getIso3166Codes().size() || l1.getIso3166Codes().size() != 0) {
                                    LOGGER.debug("Anzahl der Iso4166 Codecs in Parent Location falsch: " + l1.getIso3166Codes().size() + " "
                                            + l2.getIso3166Codes().size() + " "
                                            + 0);
                                    fehler += 1;
                                }
                            }
                        }
                    }
                }
            }
            {
                URI r1 = m1.getUri();
                URI r2 = m2.getUri();
                URI expected = URI.create(URI_VALUE);
                if (!r1.equals(r2) || !r1.equals(expected)) {
                    LOGGER.debug("URI nicht korrekt " + r1 + " " + r2 + " " + expected);
                    fehler += 1;
                }
            }
            {
                if (m1.getUserMetadata().keySet().size() != m2.getUserMetadata().keySet().size() || m1.getUserMetadata().keySet().size() != 10) {
                    LOGGER.debug("User MetaData Anzahl Element stimmt nicht " + m1.getUserMetadata().keySet().size() + " "
                            + m2.getUserMetadata().keySet().size() + " " + 10);
                    fehler += 1;
                } else {
                    String key = m1.getUserMetadata().keySet().iterator().next();
                    fehler += compareStrings(m1.getUserMetadata().get(key), m2.getUserMetadata().get(key), JSON_STRING_ELEMENT, "UserMetadata Value for key " + key);
                }

            }
            fehler += compareStrings(m1.getETag(), m2.getETag(), ETAG_VALUE, "ETag");
            fehler += compareStrings(sdf.format(m1.getCreationDate()), sdf.format(m2.getCreationDate()), CREATION_DATE, "Creation Date");
            fehler += compareStrings(sdf.format(m1.getLastModified()), sdf.format(m2.getLastModified()), MODIFIED_DATE, "Creation Date");
            fehler += compareStrings(m1.getSize().toString(), m2.getSize().toString(), "1111", "Size");
            fehler += compareStrings(m1.getShouldBeCompressed().toString(), m2.getShouldBeCompressed().toString(), SHOULD_BE_COMPRESSED, "should be compressed");
            fehler += compareStrings(m1.getContentType(), m2.getContentType(), CONTENT_TYPE, "content type");
            return fehler;
        } catch (
                Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private int compareStrings(String v1, String v2, String expected, String description) {
        if (!v1.equals(v2) || !v1.equals(expected)) {
            LOGGER.debug(description + " nicht korrekt: v1(" + v1 + ") v2(" + v2 + ") expected(" + expected + ")");
            return 1;
        }
        return 0;
    }


}
