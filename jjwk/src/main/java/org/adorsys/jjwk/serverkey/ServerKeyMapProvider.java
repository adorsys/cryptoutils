package org.adorsys.jjwk.serverkey;

public interface ServerKeyMapProvider {
    ServerKeyMap getKeyMap();
    ServerKeysHolder getServerKeysHolder();
}
