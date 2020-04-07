package com.ocean.surf.soa.proxy;

import com.ocean.surf.client.Connection;
import com.ocean.surf.client.MultiplexedConnection;
import com.ocean.surf.core.client.IConnection;
import com.ocean.surf.soa.core.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * Created by David on 2020/4/5.
 */
public class MultiplexedSoaProxy<T> implements InvocationHandler {

    private final IConnection connection;
    private final static ThreadLocal<ISerialize<Object>> Serializer = new ThreadLocal<>();


    public MultiplexedSoaProxy(int poolSize, String address, int port, int bufferSize, long timeoutMilliseconds) throws IOException, ExecutionException, InterruptedException {
        connection = new MultiplexedConnection(address, port, bufferSize, timeoutMilliseconds);
        connection.connect();
    }

    public T bind(Class<T> clazz) {
        T ret = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
//        T ret = (T)Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
        return ret;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodCallContext context = new MethodCallContext();
        context.setMethodName(method.toGenericString());
        context.setArguments(args);
        ISerialize<Object> serializer = getSerializer();
        byte[] data = serializer.serialize(context);
        ByteBuffer ret = connection.writeAndRead2(data);
        MethodResult result = (MethodResult)serializer.deSerialize(ret.array(), 0, ret.limit());
        ret.clear();
        if(result.getType() == ReturnType.Throwable) {
            throw (Throwable)result.getData();
        }
        return result.getData();
    }

    private ISerialize<Object> getSerializer() {
        ISerialize<Object> s = Serializer.get();
        if(s == null) {
            s = new DefaultSerializer<>();
            Serializer.set(s);
        }
        return s;
    }
}
