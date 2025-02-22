package com.ocean.surf.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created by david on 17/5/8.
 */
public interface IClient {
    void connect() throws ExecutionException, InterruptedException, IOException;
    int multiplexConnect() throws ExecutionException, InterruptedException, IOException;
    void close() throws  IOException;
    ByteBuffer read() throws ExecutionException, InterruptedException, IOException;
    ByteBuffer read(Callable callable) throws ExecutionException, InterruptedException, IOException;
    void write(byte[] data) throws ExecutionException, InterruptedException;
    void write(byte[] data, Callable callable) throws ExecutionException, InterruptedException;
    void multiplexWrite(int sessionId, byte[] data, ByteBuffer writeBuf) throws ExecutionException, InterruptedException;
    int readSessionId(final ByteBuffer sessionBuffer) throws ExecutionException, InterruptedException;
    void multiplexRead(final ByteBuffer headBuffer, final ByteBuffer dataBuffer) throws ExecutionException, InterruptedException, IOException;
}
