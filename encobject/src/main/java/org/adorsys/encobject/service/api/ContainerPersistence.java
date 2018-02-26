package org.adorsys.encobject.service.api;

import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.UnknownContainerException;

/**
 * Created by peter on 26.02.18 at 16:28.
 */
public interface ContainerPersistence {
    void createContainer(String container) throws ContainerExistsException;

    boolean containerExists(String container);

    void deleteContainer(String container) throws UnknownContainerException;
}
