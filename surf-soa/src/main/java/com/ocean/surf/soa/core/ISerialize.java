package com.ocean.surf.soa.core;

import java.io.IOException;

/**
 * Created by david on 17/7/16.
 */
public interface ISerialize<T> {

    byte[] serialize(T t) throws IOException;
    T deSerialize(byte[] data) throws IOException, ClassNotFoundException;
    T deSerialize(byte[] data, int offset, int length) throws IOException, ClassNotFoundException;
}
