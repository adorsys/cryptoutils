package org.adorsys.encobject.service;

import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.Payload;
import org.apache.commons.lang3.BooleanUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ByteArrayPayload implements Payload {
    public static final String SENSITIVE = "Sensitive";
    private byte[] data;
    private BlobMetaInfo metaInfo;

    private ByteArrayPayload(byte[] data) {
        super();
        this.data = data;
    }

    @Override
    public void close() throws IOException {
        // noop
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void release() {
        // Noop
    }

    @Override
    public BlobMetaInfo getBlobMetaInfo() {
        return metaInfo;
    }

    @Override
    public void setBlobMetaInfo(BlobMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    @Override
    public void setSensitive(boolean isSensitive) {
        if (isSensitive() == isSensitive)
            return;
        String sensitive = BooleanUtils.toString(isSensitive, "true", "false");
        metaInfo().put(SENSITIVE, sensitive);
    }

    @Override
    public boolean isSensitive() {
        if (metaInfo == null)
            return false;
        String value = metaInfo.find(SENSITIVE);
        if (value == null)
            return false;
        return BooleanUtils.toBoolean(value);
    }

    BlobMetaInfo metaInfo() {
        if (metaInfo == null)
            metaInfo = new BlobMetaInfo();
        return metaInfo;
    }

    public static class Builder {
        ByteArrayPayload pl;
        private boolean built;

        public Builder(byte[] data) {
            this.pl = new ByteArrayPayload(data);
        }

        public Builder putMetaInfo(String key, String value) {
            checkbuilt();
            pl.metaInfo().put(key, value);
            return this;
        }

        public Builder putAllMetaInfo(Map<String, String> infos) {
            checkbuilt();
            if (infos == null || infos.isEmpty()) return this;
            Set<Entry<String, String>> infoSet = infos.entrySet();
            for (Entry<String, String> info : infoSet) {
                pl.metaInfo().put(info.getKey(), info.getValue());
            }
            return this;
        }

        public ByteArrayPayload build() {
            checkbuilt();
            built = true;
            return pl;
        }

        private void checkbuilt() {
            if (built)
                throw new IllegalStateException("builder invalid");
        }
    }

    public static Builder builder(byte[] data) {
        return new Builder(data);
    }

}
