package com.ocean.surf.client.test;

import com.ocean.surf.client.Client;
import com.ocean.surf.core.client.IClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by david on 16/12/4.
 */

public class clientPerformanceTest {

    @Test
    public void test() throws IOException, ExecutionException, InterruptedException {


        final AtomicLong count = new AtomicLong(0);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("tps : " + count);
                    count.set(0);
                }
            }
        });
        thread.start();

        final Thread threadSend1 = new Thread(new Runnable() {
            long index = 0;

            @Override
            public void run() {
                final IClient client;
                try {
                    client = Client.open("127.0.0.1", 30000, 1024);
                    client.connect();

                    while(true) {
                        String sendStr = "my test -- " + index++;
                        client.write(sendStr.getBytes());
                        ByteBuffer receive = client.read();
                        String recvStr = new String(receive.array(), receive.position(), receive.remaining());
                        Assert.assertEquals(sendStr, recvStr);
                        count.incrementAndGet();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });
        threadSend1.start();


//       final Thread threadSend2 = new Thread(new Runnable() {
//            long index = 0;
//
//            @Override
//            public void run() {
//                final IClient client;
//                try {
//                    client = Client.open("127.0.0.1", 30000, 1024);
//                    client.connect();
//
//                    while(true) {
//                        String sendStr = "my test -- " + index++;
//                        client.write(sendStr.getBytes());
//                        ByteBuffer receive = client.read();
//                        String recvStr = new String(receive.array(), receive.position(), receive.remaining());
//                        Assert.assertEquals(sendStr, recvStr);
//                        count.incrementAndGet();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        threadSend2.start();
//
//         final Thread threadSend3 = new Thread(new Runnable() {
//             long index = 0;
//
//             @Override
//             public void run() {
//                 final IClient client;
//                 try {
//                     client = Client.open("127.0.0.1", 30000, 1024);
//                     client.connect();
//
//                     while(true) {
//                         String sendStr = "my test -- " + index++;
//                         client.write(sendStr.getBytes());
//                         ByteBuffer receive = client.read();
//                         String recvStr = new String(receive.array(), receive.position(), receive.remaining());
//                         Assert.assertEquals(sendStr, recvStr);
//                         count.incrementAndGet();
//                     }
//                 } catch (IOException e) {
//                     e.printStackTrace();
//                 } catch (InterruptedException e) {
//                     e.printStackTrace();
//                 } catch (ExecutionException e) {
//                     e.printStackTrace();
//                 }
//
//             }
//         });
//        threadSend3.start();

//        final Thread threadSend4 = new Thread(new Runnable() {
//            long index = 0;
//
//            @Override
//            public void run() {
//                final IClient client;
//                try {
//                    client = Client.open("127.0.0.1", 30000, 1024);
//                    client.connect();
//
//                    while(true) {
//                        String sendStr = "my test -- " + index++;
//                        client.write(sendStr.getBytes());
//                        ByteBuffer receive = client.read();
//                        String recvStr = new String(receive.array(), receive.position(), receive.remaining());
//                        Assert.assertEquals(sendStr, recvStr);
//                        count.incrementAndGet();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        threadSend4.start();


        thread.join();

//        client.close();

    }
}
