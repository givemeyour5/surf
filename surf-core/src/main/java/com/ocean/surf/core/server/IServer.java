package com.ocean.surf.core.server;

import java.io.IOException;

/**
 * Created by david on 17/5/8.
 */
public interface IServer {
    void start() throws IOException;
    void shutdown() throws IOException;
}
