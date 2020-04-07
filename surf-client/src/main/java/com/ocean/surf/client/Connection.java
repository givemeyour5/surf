package com.ocean.surf.client;

import com.ocean.surf.core.client.IClient;
import com.ocean.surf.core.client.IConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by david on 17/8/6.
 */
public class Connection implements IConnection {


    private final BlockingQueue<ConnectionContext> pool;
    private final int poolSize;
    private final String serverAddress;
    private final int serverPort;
    private final int bufferSize;
    private final long timeout;

//    private final static ThreadLocal<Semaphore> ss = new ThreadLocal<Semaphore>() {
//        @Override
//        protected Semaphore initialValue() {
//            return new Semaphore(0, false);
//        }
//    };
//    private final static ExecutorService WORKER;

//    static {
//        WORKER = new ThreadPoolExecutor(12, 12, 1, TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(1000));
//    }

    public Connection(int poolSize, String serverAddress, int serverPort, int bufferSize, long timeoutMilliseconds) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
        this.poolSize = poolSize;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.bufferSize = bufferSize;
        this.timeout = timeoutMilliseconds;
    }

    @Override
    public void connect() throws ExecutionException, InterruptedException, IOException {
        for(int i = 0; i < poolSize; ++i) {
            IClient client = Client.open(serverAddress, serverPort, bufferSize);
            client.connect();
            pool.put(new ConnectionContext(client));
        }
    }


    @Override
    public void close() throws IOException {
        for(ConnectionContext context : pool) {
            context.client.close();
        }
    }

    private void createClient() throws IOException, ExecutionException, InterruptedException {
        IClient client = Client.open(serverAddress, serverPort, bufferSize);
        client.connect();
        pool.put(new ConnectionContext(client));
    }

    private byte[] doWriteAndRead(byte[] data) throws Exception {
        IClient client = null;
        try {
            ConnectionContext context = pool.take();
            client = context.client;
            client.write(data);
            ByteBuffer buffer = client.read();
            byte[] ret = new byte[buffer.remaining()];
            System.arraycopy(buffer.array(), buffer.position(), ret, 0, ret.length);
            pool.put(context);
            return ret;
        }
        catch (Exception e) {
            if(client != null) {
                try {
                    client.close();
                }
                finally {
                    createClient();
                }
            }
            throw e;
        }
    }

    private byte[] doWriteAndReadTimeout(byte[] data) throws Exception {
        IClient client = null;
        try {
            final ConnectionContext context = pool.take();
            client = context.client;
            final Semaphore available = context.available;
            context.client.write(data, new Callable() {
                @Override
                public Object call() throws Exception {
                    context.client.read(new Callable() {
                        @Override
                        public Object call() throws Exception {
                            available.release();
                            return null;
                        }
                    });
                    return null;
                }
            });
//            buffer = context.client.read(new Callable() {
//                @Override
//                public Object call() throws Exception {
//                    available.release();
//                    return null;
//                }
//            });

            if(!available.tryAcquire(timeout, TimeUnit.MILLISECONDS)){
                throw new TimeoutException();
            }

            ByteBuffer buffer = ((Client)client).getBuffer();
            byte[] ret = new byte[buffer.remaining()];
            System.arraycopy(buffer.array(), buffer.position(), ret, 0, ret.length);
            pool.put(context);

            return ret;
        }
        catch (Exception e) {
            if(client != null) {
                try {
                    client.close();
                }
                finally {
                    createClient();
                }
            }
            throw e;
        }
    }

    @Override
    public byte[] writeAndRead(byte[] data) throws Exception {
        if(timeout > 0) {
            return doWriteAndReadTimeout(data);
//            return doWriteAndRead(data, timeout, TimeUnit.MILLISECONDS);
        }
        else {
            return doWriteAndRead(data);
        }
    }

    @Override
    public ByteBuffer writeAndRead2(byte[] data) throws Exception {
        return null;
    }

//    private byte[] doWriteAndRead(final byte[] data, long timeout, TimeUnit unit) throws Exception {
//        Future<byte[]> future = WORKER.submit(new Callable<byte[]>() {
//            @Override
//            public byte[] call() throws Exception {
//                return doWriteAndRead(data);
//            }
//        });
//        try {
//            return future.get(timeout, unit);
//        }
//        catch (TimeoutException e){
//            future.cancel(true);
//            throw e;
//        }
//
//    }

    private class ConnectionContext {
        private IClient client;
        private Semaphore available = new Semaphore(0, false);
        public ConnectionContext(IClient client) {
            this.client = client;
        }
    }

}
