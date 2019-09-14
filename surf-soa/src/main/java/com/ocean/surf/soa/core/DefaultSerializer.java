package com.ocean.surf.soa.core;

import java.io.*;

/**
 * Created by david on 17/7/16.
 */
public class DefaultSerializer<T> implements ISerialize<T> {

    private ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
//    private ObjectOutputStream oos;

    @Override
    public byte[] serialize(T t) throws IOException {

        bos.reset();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.flush();
        bos.flush();
        oos.close();
        return bos.toByteArray();
    }

    @Override
    public T deSerialize(byte[] data) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);

        T ret = (T)ois.readObject();
        ois.close();
        bis.close();

        return ret;
    }

    @Override
    public T deSerialize(byte[] data, int offset, int length) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length);
        ObjectInputStream ois = new ObjectInputStream(bis);

        T ret = (T)ois.readObject();
        ois.close();
        bis.close();

        return ret;
    }
}
