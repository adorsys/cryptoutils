package org.adorsys.encobject.service;

import com.nitorcreations.junit.runners.NestedRunner;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Tuple;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(NestedRunner.class)
public class BlobStoreConnectionTest {

    public static final String CONTAINER = "test container";
    public static final String NAME = "test name";
    private BlobStoreConnection connection;
    private BlobStoreContextFactory blobStoreConnectionFactory;
    private ObjectHandle handle;
    private HashMap<String, String> userMetadata;

    @Before
    public void setup() throws Exception {
        blobStoreConnectionFactory = new TestFsBlobStoreFactory();
        connection = new BlobStoreConnection(blobStoreConnectionFactory);
        handle = new ObjectHandle(CONTAINER, NAME);

        connection.createContainer(CONTAINER);
    }

    @After
    public void tearDown() throws Exception {
        connection.deleteContainer(CONTAINER);
    }

    public class StoreBlobWithMetadata {

        private Tuple<byte[], Map<String, String>> blobAndMetadata;
        private byte[] bytes;

        @Before
        public void setup() throws Exception {
            userMetadata = new HashMap<>();
            userMetadata.put("a", "1");
            userMetadata.put("b", "2");
            userMetadata.put("c", "3");

            bytes = new byte[] {1, 2, 3};
            connection.putBlobWithMetadata(handle, bytes, userMetadata);
            blobAndMetadata = connection.getBlobAndMetadata(handle);
        }

        @Test
        public void shouldReadBlobBytes() throws Exception {
            byte[] bytes = blobAndMetadata.getX();

            assertThat(bytes, is(not(nullValue())));
            assertThat(bytes, is(equalTo(this.bytes)));
        }


        @Test
        public void shouldReadBlobWithMetadata() throws Exception {
            Map<String, String> metadata = blobAndMetadata.getY();

            assertThat(metadata, is(not(nullValue())));
            assertThat(metadata.entrySet(), hasSize(3));
        }
    }
}
