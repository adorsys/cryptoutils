package org.adorsys.encobject.service.api;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.exceptions.ContainerExistsException;
import org.adorsys.encobject.exceptions.UnknownContainerException;

/**
 * Created by peter on 26.02.18 at 16:28.
 */
public interface ContainerPersistence {
    void createContainer(BucketDirectory container) throws ContainerExistsException;

    boolean containerExists(BucketDirectory container);

    void deleteContainer(BucketDirectory container) throws UnknownContainerException;
}
