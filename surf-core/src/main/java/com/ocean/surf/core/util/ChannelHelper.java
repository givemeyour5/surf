package com.ocean.surf.core.util;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


/**
 * Created by david on 17/5/20.
 */
public class ChannelHelper {

    public final static int HEAD_SIZE = 4;

    public static void read(final AsynchronousSocketChannel channel, final ByteBuffer buffer) throws ExecutionException, InterruptedException {

        if(buffer == null){
            throw new NullPointerException("buffer can't be null");
        }

        while (buffer.position() < ChannelHelper.HEAD_SIZE) {
            channel.read(buffer).get();
        }
        int size = ByteConvert.toInt32(buffer.array()) + ChannelHelper.HEAD_SIZE;

        if(size > buffer.capacity()) {
//            size -= buffer.position();
//            while(size > 0) {
//                buffer.clear();
//                channel.read(buffer).get();
//                size -= buffer.position();
//            }
            throw new BufferOverflowException();
        }

        while(size > buffer.position()) {
            channel.read(buffer).get();
        }
        buffer.flip();
        buffer.position(ChannelHelper.HEAD_SIZE);
    }

    public static void read(final AsynchronousSocketChannel channel, final ByteBuffer buffer, final Callable callback) throws ExecutionException, InterruptedException {

        if(buffer == null){
            throw new NullPointerException("buffer can't be null");
        }

        channel.read(buffer, callback, new CompletionHandler<Integer, Callable>() {
            @Override
            public void completed(Integer result, Callable attachment) {
                if(result == -1) {
                    close(channel);
                    return;
                }
                if(buffer.position() < HEAD_SIZE) {
                    channel.read(buffer, attachment, this);
                }
                else {
                    final int size = ByteConvert.toInt32(buffer.array()) + HEAD_SIZE;
                    if(size > buffer.capacity()) {
                        throw new BufferOverflowException();
                    }
                    else {
                        if(size > buffer.position()) {
                            channel.read(buffer, attachment, new CompletionHandler<Integer, Callable>() {
                                @Override
                                public void completed(Integer result, Callable attachment) {
                                    if(result == -1) {
                                        close(channel);
                                        return;
                                    }
                                    if(size > buffer.position()) {
                                        channel.read(buffer, attachment, this);
                                    }
                                    else {
                                        buffer.flip();
                                        buffer.position(HEAD_SIZE);
                                        if(attachment != null) {
                                            try {
                                                attachment.call();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, Callable attachment) {

                                }
                            });
                        }
                        else {
                            buffer.flip();
                            buffer.position(HEAD_SIZE);
                            if(attachment != null) {
                                try {
                                    attachment.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Callable attachment) {

            }
        });

    }


    public static void write(final AsynchronousSocketChannel channel, final ByteBuffer data) throws ExecutionException, InterruptedException {

        if(data == null) {
            throw new NullPointerException("data can't be null");
        }
        int size = data.remaining();
        ByteBuffer byteSize = ByteBuffer.wrap(ByteConvert.toBytes(size));
        while (byteSize.hasRemaining()) {
            channel.write(byteSize).get();
        }
        while (data.hasRemaining()) {
            channel.write(data).get();
        }
    }

    public static void write(final AsynchronousSocketChannel channel, final ByteBuffer data, final Callable callback) throws ExecutionException, InterruptedException {

        if(data == null) {
            throw new NullPointerException("data can't be null");
        }
        int size = data.remaining();
        final ByteBuffer byteSize = ByteBuffer.wrap(ByteConvert.toBytes(size));

        channel.write(byteSize, callback, new CompletionHandler<Integer, Callable>() {
            @Override
            public void completed(Integer result, Callable attachment) {
                if(result == -1) {
                    close(channel);
                    return;
                }
                if(byteSize.hasRemaining()) {
                    channel.write(byteSize, attachment, this);
                }
                else {
                    if(data.hasRemaining()) {
                        channel.write(data, attachment, new CompletionHandler<Integer, Callable>() {
                            @Override
                            public void completed(Integer result, Callable attachment) {
                                if(result == -1) {
                                    close(channel);
                                    return;
                                }
                                if(data.hasRemaining()) {
                                    channel.write(data, attachment, this);
                                }
                                else {
                                    if(attachment != null) {
                                        try {
                                            attachment.call();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void failed(Throwable exc, Callable attachment) {

                            }
                        });
                    }
                    else {
                        if(attachment != null) {
                            try {
                                attachment.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Callable attachment) {

            }
        });



    }


    public static void close(Channel channel) {
        try {
            if(channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
