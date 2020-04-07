package com.ocean.surf.soa.test;

import com.ocean.surf.soa.MultiplexedSoaServer;
import org.junit.Test;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexedSoaServerTest {
    @Test
    public void test() throws Exception {
        MultiplexedSoaServer server = new MultiplexedSoaServer<>(30000, 1024 * 1024, 8, 20000);
        server.bind(SoaService.class);
        server.start();
        System.in.read();
    }
}
