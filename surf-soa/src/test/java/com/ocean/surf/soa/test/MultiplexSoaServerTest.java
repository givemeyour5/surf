package com.ocean.surf.soa.test;

import com.ocean.surf.soa.MultiplexSoaServer;
import org.junit.Test;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexSoaServerTest {
    @Test
    public void test() throws Exception {
        MultiplexSoaServer server = new MultiplexSoaServer<>(30000, 1024 * 1024, 8, 20000);
        server.bind(SoaService.class);
        server.start();
        System.in.read();
    }
}
