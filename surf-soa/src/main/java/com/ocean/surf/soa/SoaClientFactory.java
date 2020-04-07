package com.ocean.surf.soa;

import com.ocean.surf.soa.core.ClientConfig;
import com.ocean.surf.soa.proxy.DefaultSoaProxy;
import com.ocean.surf.soa.proxy.MultiplexedSoaProxy;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by david on 17/7/16.
 */
public class SoaClientFactory {

    public static <T> T Create(Class<T> clazz, ClientConfig config) throws InterruptedException, ExecutionException, IOException {
        return new DefaultSoaProxy<T>(config.getPoolSize(), config.getAddress(),
                config.getServerPort(), config.getBufferSize(), config.getTimeoutMilliseconds()).bind(clazz);
    }

    public static <T> T CreateMultiplexed(Class<T> clazz, ClientConfig config) throws InterruptedException, ExecutionException, IOException {
        return new MultiplexedSoaProxy<T>(config.getPoolSize(), config.getAddress(),
                config.getServerPort(), config.getBufferSize(), config.getTimeoutMilliseconds()).bind(clazz);
    }
}
