package com.ocean.surf.client;

import com.ocean.surf.core.client.IClient;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by david on 17/5/8.
 */
public abstract class AbstractClient implements IClient {
    protected SocketAddress remoteAddress;
    protected int timeout;
    protected ByteBuffer buffer;
}
