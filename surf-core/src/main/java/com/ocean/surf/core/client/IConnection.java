package com.ocean.surf.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xiecheng on 17/8/6.
 */
public interface IConnection {

    void connect() throws ExecutionException, InterruptedException, IOException;
    void close() throws IOException;

    byte[] writeAndRead(byte[] data) throws Exception;
//    byte[] writeAndRead(byte[] data, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, IOException, TimeoutException;
}
