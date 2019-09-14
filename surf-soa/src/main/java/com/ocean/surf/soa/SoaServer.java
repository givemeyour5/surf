package com.ocean.surf.soa;

import com.ocean.surf.server.Server;
import com.ocean.surf.soa.core.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 17/7/16.
 */
public class SoaServer<T> extends Server {

    private T instance;

    private final Map<String, Method> methodMap = new HashMap<>();
    private final static ThreadLocal<ISerialize<Object>> Serializer = new ThreadLocal<>();


    public SoaServer(int port, int bufferSize, int threadCount, int queueSize) throws Exception {
        super(port, bufferSize, threadCount, queueSize);
    }


    public void bind(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        instance = clazz.newInstance();

        Method[] methods = clazz.getInterfaces()[0].getMethods();
        for (Method method : methods) {
            methodMap.put(method.toGenericString(), method);
        }
    }

    @Override
    protected ByteBuffer process(ByteBuffer data) {

        MethodResult result = new MethodResult();
        try {

            MethodCallContext methodCallInfo = resolveMethodCallInfo(data);
            Method method = methodMap.get(methodCallInfo.getMethodName());
            if(method == null) {
                result.setType(ReturnType.Throwable);
                result.setData(new NoSuchMethodException("soa.NoSuchMethodException"));
            }
            else {
                try {
                    Object ret = method.invoke(instance, methodCallInfo.getArguments());
                    result.setData(ret);
                    result.setType(ReturnType.Success);

                } catch (Throwable eMethod) {
                    result.setType(ReturnType.Throwable);
                    result.setData(eMethod);
                }
            }

            return encodeMethodResult(result);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    private ByteBuffer encodeMethodResult(MethodResult result) throws IOException {
        ISerialize<Object> serialize = getSerializer();
        byte[] retBytes = serialize.serialize(result);
        return ByteBuffer.wrap(retBytes);
    }

    private MethodCallContext resolveMethodCallInfo(ByteBuffer data) throws IOException, ClassNotFoundException {

//        ISerialize<MethodCallContext> serialize = new DefaultSerializer<>();
        ISerialize<Object> serialize = getSerializer();
        return (MethodCallContext)serialize.deSerialize(data.array(), data.position(), data.remaining());
    }

    private ISerialize<Object> getSerializer() {
        ISerialize<Object> s = Serializer.get();
        if(s == null) {
            s = new DefaultSerializer<>();
            Serializer.set(s);
        }
        return s;
    }

    public ByteBuffer testProcess(ByteBuffer data) {
        return process(data);
    }
}
