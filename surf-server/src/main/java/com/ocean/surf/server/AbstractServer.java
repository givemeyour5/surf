package com.ocean.surf.server;

import com.ocean.surf.core.server.IServer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by xiecheng on 17/5/8.
 */
public abstract class AbstractServer implements IServer {

    protected SocketAddress address;

    protected abstract ByteBuffer process(ByteBuffer data);
}
