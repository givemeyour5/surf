package com.ocean.surf.server;

import com.ocean.surf.core.util.ByteConvert;
import com.ocean.surf.core.util.ChannelHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by david on 17/5/8.
 */
public class Server extends AbstractServer {


    private AsynchronousServerSocketChannel channel;
    private int bufferSize = 1024 * 1024;
    private int port = 30000;
    private int threadCount = 8;
    private int queueSize = 20000;

    protected Server() throws IOException {
    }

    protected Server(int port, int bufferSize, int threadCount, int queueSize) throws IOException {
        this.port = port;
        this.bufferSize = bufferSize;
        this.threadCount = threadCount;
        this.queueSize = queueSize;
    }

    private void init() throws IOException {
        super.address = new InetSocketAddress(port);
        this.bufferSize += ChannelHelper.HEAD_SIZE;
        channel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(
                new ThreadPoolExecutor(threadCount, threadCount, 1, TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(queueSize))
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
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                handle(result);


            }

            @Override
            public void failed(Throwable exc, Object attachment) {

                System.out.println("accept failed");
            }
        });
    }



    private void handle(final AsynchronousSocketChannel channel) {

        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        channel.read(buffer, null, new CompletionHandler<Integer, Object>() {

            @Override
            public void completed(Integer result, Object attachment) {
                if(result == -1) {
                    ChannelHelper.close(channel);
                    return;
                }
                try {
                    ChannelHelper.read(channel, buffer);
//                    buffer.flip();
//                    buffer.position(ChannelHelper.HEAD_SIZE);
                    ByteBuffer writeData = process(buffer);
                    ChannelHelper.write(channel, writeData);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    ChannelHelper.close(channel);
                    return;
                }

                buffer.clear();
                channel.read(buffer, null, this);
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
