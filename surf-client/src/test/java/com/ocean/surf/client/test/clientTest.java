package com.ocean.surf.client.test;

import com.ocean.surf.client.Client;
import com.ocean.surf.core.client.IClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xiecheng on 17/5/9.
 */
public class clientTest {

    @Test
    public void test() throws IOException, ExecutionException, InterruptedException {

        final IClient client;
        try {
            client = Client.open("127.0.0.1", 30000, 1024 * 1024);
            client.connect();

            final AtomicBoolean flag = new AtomicBoolean(false);
            String sendStr = buildSendData();
            client.write(sendStr.getBytes(), null);

//            while (!flag.get());
            ByteBuffer receive = client.read(new Callable() {
                @Override
                public Object call() throws Exception {
                    flag.set(true);
                    return null;
                }
            });

            while (!flag.get());
            String recvStr = new String(receive.array(), receive.position(), receive.remaining());
            Assert.assertEquals(sendStr, recvStr);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private String buildSendData() {
        String str = "my test -- ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30000; ++i) {
            sb.append(str);
        }
        return sb.toString();
    }
}
