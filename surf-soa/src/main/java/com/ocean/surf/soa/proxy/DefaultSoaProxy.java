package com.ocean.surf.soa.proxy;

import com.ocean.surf.client.Connection;
import com.ocean.surf.core.client.IConnection;
import com.ocean.surf.soa.core.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by david on 17/7/16.
 */
public class DefaultSoaProxy<T> implements InvocationHandler {

    private final IConnection connection;
    private final static ThreadLocal<ISerialize<Object>> Serializer = new ThreadLocal<>();


    public DefaultSoaProxy(int poolSize, String address, int port, int bufferSize, long timeoutMilliseconds) throws IOException, ExecutionException, InterruptedException {
        connection = new Connection(poolSize, address, port, bufferSize, timeoutMilliseconds);
        connection.connect();
    }

    public T bind(Class<T> clazz) {
        T ret = (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
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
        byte[] ret = connection.writeAndRead(data);
        MethodResult result = (MethodResult)serializer.deSerialize(ret, 0, ret.length);

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
