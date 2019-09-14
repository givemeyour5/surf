package com.ocean.surf.server.test;

import com.ocean.surf.core.server.IServer;
import com.ocean.surf.server.Server;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by david on 17/5/8.
 */
public class serverTest {

    @Test
    public void test() throws IOException {
        IServer server = Server.open(30000, 1024 * 1024, 4, 20000);
        server.start();
        System.in.read();
    }
}
