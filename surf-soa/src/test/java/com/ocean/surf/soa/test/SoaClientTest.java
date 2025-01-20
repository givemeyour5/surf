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
        final ISoaService client = SoaClientFactory.CreateMultiplex(ISoaService.class, config);


        final AtomicInteger totalCount = new AtomicInteger(0);
        final Map<String, AtomicInteger> threadCount = new HashMap<>();

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        System.out.println("tps : " + totalCount);
                    totalCount.set(0);

                    for(Map.Entry<String, AtomicInteger> entry : threadCount.entrySet()) {
                        System.out.println(String.format("thread %s tps : %s", entry.getKey(), entry.getValue()));
                        entry.getValue().set(0);
                    }
                }
            }
        });
        thread.start();


        final Map<String, Map<Integer, Object>> p2 = new HashMap<>();
        Map<Integer, Object> tmp = new HashMap<>();
        tmp.put(1, 1);
        p2.put("a", tmp);
        p2.put("b", tmp);
        Map subParm = new HashMap<String, Object>();
        subParm.put("key1","value1");
        subParm.put("key2","value2");
        subParm.put("key3","value3");
        p2.put("c", subParm);


        final Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                while (true) {
                    int ret = client.call(index, p2);
                    Assert.assertEquals(index+1, ret);
                    ++index;
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread1");
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
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread2");
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
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread3");
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
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread4");
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
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread5");
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
                    totalCount.incrementAndGet();
                    countThread(threadCount, "thread6");
                }
            }
        });
        thread6.start();


        thread.join();

    }

    private void countThread (Map<String, AtomicInteger> threadCount, String name) {
        AtomicInteger tCount = threadCount.get(name);
        if(tCount == null) {
            tCount = new AtomicInteger(0);
        }
        tCount.incrementAndGet();
        threadCount.put(name, tCount);
    }

}
