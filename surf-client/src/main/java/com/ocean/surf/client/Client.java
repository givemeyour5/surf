package com.ocean.surf.client;

import com.ocean.surf.core.util.ByteConvert;
import com.ocean.surf.core.util.ChannelHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by david on 17/5/8.
 */
public class Client extends AbstractClient {

    private final AsynchronousSocketChannel channel;
    private final static ExecutorService WORKER;

    static {
        WORKER = new ThreadPoolExecutor(12, 12, 1, TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(1000));
    }


    private Client(String serverAddress, int serverPort, ByteBuffer buffer) throws IOException {
        super.remoteAddress = new InetSocketAddress(serverAddress, serverPort);
        super.buffer = buffer;
        channel = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(WORKER));
//        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
//        channel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1024);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, false);
    }

    public static Client open(String serverAddress, int serverPort, int bufferSize) throws IOException {
        return new Client(serverAddress, serverPort, ByteBuffer.allocate(bufferSize + ChannelHelper.HEAD_SIZE));
    }


    @Override
    public int connect() throws ExecutionException, InterruptedException, IOException {
        channel.connect(remoteAddress).get();
        ByteBuffer sessionBuffer = ByteBuffer.allocate(ChannelHelper.SESSION_SIZE);
        int sessionSeed = readSessionId(sessionBuffer);
        ByteBuffer receive = read();
        System.out.println(new String(receive.array(), receive.position(), receive.remaining()));
        return sessionSeed;
    }

    @Override
    public void close() throws IOException {

        if(channel.isOpen()) {
            channel.close();
        }
    }

    @Override
    public ByteBuffer read() throws ExecutionException, InterruptedException, IOException {
        buffer.clear();
        ChannelHelper.read(channel, buffer);
        return buffer;
    }

    @Override
    public ByteBuffer read(Callable callable) throws ExecutionException, InterruptedException, IOException {
        buffer.clear();
        ChannelHelper.read(channel, buffer, callable);
        return buffer;
    }


    @Override
    public void write(byte[] data) throws ExecutionException, InterruptedException {

        ByteBuffer wb = ByteBuffer.wrap(data);
        ChannelHelper.write(channel, wb);
    }

    @Override
    public void write(byte[] data, Callable callable) throws ExecutionException, InterruptedException {

        ByteBuffer wb = ByteBuffer.wrap(data);
        ChannelHelper.write(channel, wb, callable);
    }

    @Override
    public void multiplexedWrite(int sessionId, byte[] data) throws ExecutionException, InterruptedException {
        ChannelHelper.multiplexedWrite(sessionId, channel, data);
    }

    @Override
    public int readSessionId(ByteBuffer sessionBuffer) throws ExecutionException, InterruptedException {
        return ChannelHelper.readSessionId(channel, sessionBuffer);
    }

    @Override
    public void multiplexedRead(ByteBuffer headBuffer, ByteBuffer dataBuffer) throws ExecutionException, InterruptedException, IOException {
        ChannelHelper.multiplexedRead(channel, headBuffer, dataBuffer);
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

}
