package org.adorsys.encobject.userdata;

import java.io.IOException;

public interface ObjectMapperSPI {

	public <T> T readValue(byte[] src, Class<T> klass) throws IOException;

	public <T> byte[] writeValueAsBytes(T t) throws IOException;

}
