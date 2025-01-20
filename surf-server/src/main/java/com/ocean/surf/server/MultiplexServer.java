package com.ocean.surf.server;

import com.ocean.surf.core.util.ChannelHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexServer extends AbstractServer {
    private AsynchronousServerSocketChannel serverChannel;
    private int bufferSize = 1024 * 1024;
    private int port = 30000;
    private int threadCount = 8;
    private int queueSize = 20000;
    private final Map<Integer, BufContext> sessionPool = new ConcurrentHashMap<>();
    private final Executor executor = new ThreadPoolExecutor(threadCount, threadCount, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize), new ThreadPoolExecutor.CallerRunsPolicy());
    private final AtomicInteger sessionSeed = new AtomicInteger(1);
    private final Queue<Integer> sessionSeedRecycle = new ConcurrentLinkedQueue<>();
    private final Map<SocketAddress, Integer> connSessionSeeds = new ConcurrentHashMap<>();

    private class BufContext {
        private volatile ByteBuffer dataBuffer = ByteBuffer.allocate(bufferSize);
        //len(sessionId) + len(headSize) = 8 bytes
        private volatile ByteBuffer writeBuf = ByteBuffer.allocate(8 + ChannelHelper.BATCH_SIZE);
    }

    protected MultiplexServer() throws IOException {
    }

    protected MultiplexServer(int port, int bufferSize, int threadCount, int queueSize) throws IOException {
        this.port = port;
        this.bufferSize = bufferSize;
        this.threadCount = threadCount;
        this.queueSize = queueSize;
    }

    private void init() throws IOException {
        super.address = new InetSocketAddress(port);
        this.bufferSize += ChannelHelper.HEAD_SIZE;
        serverChannel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(
                new ThreadPoolExecutor(1, 8, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize), new ThreadPoolExecutor.CallerRunsPolicy())
        ));
        //        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
        //        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
        serverChannel.bind(address);
    }

    public void setPort(int port) { this.port = port; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

    public static Server open(int port, int bufferSize, int threadCount, int queueSize) throws IOException {
        return new Server(port, bufferSize, threadCount, queueSize);
    }

    @Override
    public void start() throws IOException {
        init();
        System.out.println("server start...");
        System.out.println("bind address: " + address);
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                serverChannel.accept(null, this);
                ByteBuffer wb = ByteBuffer.wrap("server connected!".getBytes());
                try {
                    ChannelHelper.writeSession(channel, generateSessionSeed(channel.getRemoteAddress()));
                    ChannelHelper.write(channel, wb);
                    handle(channel);
                } catch (Exception e) {
                    e.printStackTrace();
//                    ChannelHelper.close(serverChannel);
                    return;
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

                System.out.println("accept failed");
            }
        });
    }

    private int generateSessionSeed(SocketAddress clientAddress) {
        int seed = 0;
        if(sessionSeedRecycle.size() > 0) {
            seed = sessionSeedRecycle.poll();
        }
        else {
            seed = sessionSeed.getAndAdd(ChannelHelper.SESSION_AMOUNT_PER_CONN);
        }
        connSessionSeeds.put(clientAddress, seed);
        return seed;
    }

    private void clearSession(SocketAddress clientAddress) {
        //recycle sessions of this closed channel depends ip:port
        Integer sessionSeed = connSessionSeeds.remove(clientAddress);
        if(sessionSeed != null) {
            int maxSessionId = sessionSeed + ChannelHelper.SESSION_AMOUNT_PER_CONN;
            for(int session = sessionSeed; session < maxSessionId; ++session) {
                sessionPool.remove(session);
            }
            sessionSeedRecycle.add(sessionSeed);
        }
    }

    private void handle(final AsynchronousSocketChannel channel) {

        final ByteBuffer sessionBuffer = ByteBuffer.allocate(ChannelHelper.SESSION_SIZE);
        final ByteBuffer headBuffer = ByteBuffer.allocate(ChannelHelper.HEAD_SIZE);

        channel.read(sessionBuffer, null, new CompletionHandler<Integer, Object>() {

            @Override
            public void completed(Integer result, Object attachment) {
                if(result == -1) {
                    ChannelHelper.close(channel);
                    try {
                        clearSession(channel.getRemoteAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    int sessionId = ChannelHelper.readSessionId(sessionBuffer);
                    boolean end = false;
                    if(sessionId < 0) {
                        end = true;
                        sessionId = Math.abs(sessionId);
                    }
                    BufContext bufContext = sessionPool.get(sessionId);
                    if(bufContext == null) {
                        bufContext = new BufContext();
                        sessionPool.put(sessionId, bufContext);
                    }
                    final ByteBuffer shadowDataBuf = bufContext.dataBuffer;
                    ChannelHelper.multiplexRead(channel, headBuffer, shadowDataBuf);
                    if(end) {
                        shadowDataBuf.flip();
                        final int sid = sessionId;
//                        System.out.println("session " + sid);
//                        final ByteBuffer shadowWriteBuf = bufContext.writeBuf;
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                final ByteBuffer writeData = process(shadowDataBuf);
                                shadowDataBuf.clear();
                                try {
                                    ChannelHelper.multiplexWrite(sid, channel, writeData.array());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    ChannelHelper.close(channel);
                                }
                            }
                        });
                    }
                    else {
                        shadowDataBuf.limit(shadowDataBuf.capacity());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                }

                channel.read(sessionBuffer, null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("handle failed");
                try {
                    clearSession(channel.getRemoteAddress());
                    System.out.println(channel.getRemoteAddress() + " lost");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void shutdown() throws IOException {
        ChannelHelper.close(serverChannel);
    }

    @Override
    protected ByteBuffer process(ByteBuffer data) {

//        System.out.println("received size : " + data.limit());
        return data;
    }

}
