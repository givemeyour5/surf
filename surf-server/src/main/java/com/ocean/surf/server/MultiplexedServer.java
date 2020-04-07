package com.ocean.surf.server;

import com.ocean.surf.core.util.ChannelHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexedServer extends AbstractServer {
    private AsynchronousServerSocketChannel channel;
    private int bufferSize = 1024 * 1024;
    private int port = 30000;
    private int threadCount = 8;
    private int queueSize = 20000;
    private final Map<Integer, ByteBuffer> sessionPool = new ConcurrentHashMap<>();
    private final Executor executor = new ThreadPoolExecutor(threadCount, threadCount, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize));

    protected MultiplexedServer() throws IOException {
    }

    protected MultiplexedServer(int port, int bufferSize, int threadCount, int queueSize) throws IOException {
        this.port = port;
        this.bufferSize = bufferSize;
        this.threadCount = threadCount;
        this.queueSize = queueSize;
    }

    private void init() throws IOException {
        super.address = new InetSocketAddress(port);
        this.bufferSize += ChannelHelper.HEAD_SIZE;
        channel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(
                new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize))
        ));
        //        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
        //        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
        channel.bind(address);
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
        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                channel.accept(null, this);
                ByteBuffer wb = ByteBuffer.wrap("server connected!".getBytes());
                try {
                    ChannelHelper.write(result, wb);
                    handle(result);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                }

            }

            @Override
            public void failed(Throwable exc, Object attachment) {

                System.out.println("accept failed");
            }
        });
    }



    private void handle(final AsynchronousSocketChannel channel) throws ExecutionException, InterruptedException {

        final ByteBuffer sessionBuffer = ByteBuffer.allocate(ChannelHelper.SESSION_SIZE);
        final ByteBuffer headBuffer = ByteBuffer.allocate(ChannelHelper.HEAD_SIZE);



        channel.read(sessionBuffer, null, new CompletionHandler<Integer, Object>() {

            @Override
            public void completed(Integer result, Object attachment) {
                if(result == -1) {
                    ChannelHelper.close(channel);
                    return;
                }
                try {
                    int sessionId = ChannelHelper.readSessionId(channel, sessionBuffer);
                    boolean end = false;
                    if(sessionId < 0) {
                        end = true;
                        sessionId = Math.abs(sessionId);
                    }
                    ByteBuffer dataBuffer = sessionPool.get(sessionId);
                    if(dataBuffer == null) {
                        dataBuffer = ByteBuffer.allocate(bufferSize);
                        sessionPool.put(sessionId, dataBuffer);
                    }
                    final ByteBuffer processBuffer = dataBuffer;
                    ChannelHelper.multiplexedRead(channel, headBuffer, dataBuffer);
                    if(end) {
                        processBuffer.flip();
                        final int sid = sessionId;
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                final ByteBuffer writeData = process(processBuffer);
                                processBuffer.clear();
                                try {
                                    ChannelHelper.multiplexedWrite(sid, channel, writeData.array());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    ChannelHelper.close(channel);
                                }
                            }
                        });
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
                    System.out.println(channel.getRemoteAddress() + " lost");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void shutdown() throws IOException {
        ChannelHelper.close(channel);
    }

    @Override
    protected ByteBuffer process(ByteBuffer data) {

//        System.out.println("received size : " + data.limit());
        return data;
    }
}
