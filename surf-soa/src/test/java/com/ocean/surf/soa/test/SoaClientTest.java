package com.ocean.surf.soa.test;

import com.ocean.surf.soa.SoaClientFactory;
import com.ocean.surf.soa.core.ClientConfig;
import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 17/7/16.
 */
public class SoaClientTest {


    @Test
    public void test() throws Exception {
        ClientConfig config = new ClientConfig();
        config.setPoolSize(1);
        config.setAddress("127.0.0.1");
        config.setServerPort(30000);
        config.setBufferSize(1024*1024);
        config.setTimeoutMilliseconds(60000);
//        final ISoaService client = SoaClientFactory.Create(ISoaService.class, config);
        final ISoaService client = SoaClientFactory.CreateMultiplexed(ISoaService.class, config);


        final AtomicInteger count = new AtomicInteger(0);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        System.out.println("qps : " + count);
                    count.set(0);
                }
            }
        });
        thread.start();


        final Map<String, Map<Integer, Object>> p2 = new HashMap<>();
        Map<Integer, Object> tmp = new HashMap<>();
        tmp.put(1, 1);
        p2.put("a", tmp);
        p2.put("b", tmp);
        Map dd = new HashMap<String, Object>();
        dd.put("aaaa","dfdfdfd");
        dd.put("bbb","dfdfdfd");
        dd.put("ccccc","dfdfdfd");
        p2.put("c", dd);

        final Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
//                  System.out.println(String.format("old: %d,  new: %d", index+1, ret));
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    count.incrementAndGet();

                }
            }
        });
        thread1.start();

        final Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 90;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
//                    ++index;
                    count.incrementAndGet();
//            System.out.println(String.format("old: %d,  new: %d", index++, ret));
                }
            }
        });
        thread2.start();

        final Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    count.incrementAndGet();
//            System.out.println(String.format("old: %d,  new: %d", index++, ret));
                }
            }
        });
        thread3.start();

        final Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    count.incrementAndGet();
//            System.out.println(String.format("old: %d,  new: %d", index++, ret));
                }
            }
        });
        thread4.start();

        final Thread thread5 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    count.incrementAndGet();
//            System.out.println(String.format("old: %d,  new: %d", index++, ret));
                }
            }
        });
        thread5.start();

        final Thread thread6 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    count.incrementAndGet();
//            System.out.println(String.format("old: %d,  new: %d", index++, ret));
                }
            }
        });
        thread6.start();


        thread.join();

    }

}
