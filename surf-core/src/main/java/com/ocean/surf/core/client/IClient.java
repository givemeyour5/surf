package com.ocean.surf.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created by david on 17/5/8.
 */
public interface IClient {
    int connect() throws ExecutionException, InterruptedException, IOException;
    void close() throws  IOException;
    ByteBuffer read() throws ExecutionException, InterruptedException, IOException;
    ByteBuffer read(Callable callable) throws ExecutionException, InterruptedException, IOException;
    void write(byte[] data) throws ExecutionException, InterruptedException;
    void write(byte[] data, Callable callable) throws ExecutionException, InterruptedException;
    void multiplexedWrite(int sessionId, byte[] data) throws ExecutionException, InterruptedException;
    int readSessionId(final ByteBuffer sessionBuffer) throws ExecutionException, InterruptedException;
    void multiplexedRead(final ByteBuffer headBuffer, final ByteBuffer dataBuffer) throws ExecutionException, InterruptedException, IOException;
}
