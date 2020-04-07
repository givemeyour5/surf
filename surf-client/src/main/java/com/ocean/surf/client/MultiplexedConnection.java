package com.ocean.surf.client;

import com.ocean.surf.core.client.IClient;
import com.ocean.surf.core.client.IConnection;
import com.ocean.surf.core.util.ChannelHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexedConnection implements IConnection {
    private final Map<Long, ConnectionContext> pool = new ConcurrentHashMap<>();
    private final Map<Integer, ConnectionContext> sessionPool = new ConcurrentHashMap<>();
    private final String serverAddress;
    private final int serverPort;
    private final int bufferSize;
    private final long timeout;
    private final Thread readThread;
    private IClient client;

    public MultiplexedConnection(String serverAddress, int serverPort, int bufferSize, long timeoutMilliseconds) throws IOException, ExecutionException, InterruptedException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.bufferSize = bufferSize;
        this.timeout = timeoutMilliseconds;
        this.readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void connect() throws ExecutionException, InterruptedException, IOException {
        client = Client.open(serverAddress, serverPort, bufferSize);
        client.connect();
        readThread.start();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public byte[] writeAndRead(byte[] data) throws Exception {
        return null;
    }

    @Override
    public ByteBuffer writeAndRead2(byte[] data) throws Exception {
        long tid = Thread.currentThread().getId();
        ConnectionContext context = pool.get(tid);
        if(context == null) {
            context = new ConnectionContext(Math.abs(new Random(System.currentTimeMillis()).nextInt()), bufferSize);
            pool.put(tid, context);
            sessionPool.put(context.sessionId, context);
        }
        client.multiplexedWrite(context.sessionId, data);
        context.available.acquire();
        return context.buffer;
    }

    private void read() throws ExecutionException, InterruptedException, IOException {
        ByteBuffer sessionBuffer = ByteBuffer.allocate(ChannelHelper.SESSION_SIZE);
        ByteBuffer headBuffer = ByteBuffer.allocate(ChannelHelper.HEAD_SIZE);
        while (true) {
            int sessionId = client.readSessionId(sessionBuffer);
            boolean end = false;
            if(sessionId < 0) {
                end = true;
                sessionId = Math.abs(sessionId);
            }
            ConnectionContext context = sessionPool.get(sessionId);
            ByteBuffer dataBuffer = context.buffer;
            client.multiplexedRead(headBuffer, dataBuffer);
            if(end) {
                dataBuffer.flip();
                context.available.release();
            }
        }
    }

    private class ConnectionContext {
        private Semaphore available = new Semaphore(0, false);
        private ByteBuffer buffer;
        private int sessionId;
        public ConnectionContext(int sessionId, int bufferSize) {
            this.sessionId = sessionId;
            buffer = ByteBuffer.allocate(bufferSize);
        }
    }
}
