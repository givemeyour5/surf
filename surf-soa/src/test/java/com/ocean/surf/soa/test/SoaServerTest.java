package com.ocean.surf.soa.test;

import com.ocean.surf.core.server.IServer;
import com.ocean.surf.soa.SoaServer;
import com.ocean.surf.soa.core.DefaultSerializer;
import com.ocean.surf.soa.core.ISerialize;
import com.ocean.surf.soa.core.MethodCallContext;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by david on 17/7/16.
 */
public class SoaServerTest {

    @Test
    public void test() throws Exception {
        SoaServer server = new SoaServer<>(30000, 1024 * 1024, 8, 20000);
        server.bind(SoaService.class);
        server.start();
        System.in.read();
    }

//    @Test
//    public void testLocalInvoke() throws Exception {
//        MethodCallContext context = new MethodCallContext();
//        context.setMethodName("public abstract int com.ocean.surf.soa.test.ISoaService.call(int,java.util.Map<java.lang.String, java.util.Map<java.lang.Integer, java.lang.Object>>)");
//        context.setArguments(new Object[]{1, null});
//
//
//        SoaServer server = new SoaServer<ISoaService>();
//        server.bind(SoaService.class);
//
//        ISerialize<MethodCallContext> serialize = new DefaultSerializer<>();
//
//        ByteBuffer ret = server.testProcess(ByteBuffer.wrap(serialize.serialize(context)));
//        System.out.println(serialize.deSerialize(ret.array(), ret.position(), ret.remaining()));
//    }
}
