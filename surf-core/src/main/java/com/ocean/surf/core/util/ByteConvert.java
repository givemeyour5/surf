package com.ocean.surf.core.util;

/**
 * Created by david on 17/5/11.
 */
public class ByteConvert {

    public static byte[] toBytes(int data) {
        byte[] ret = new byte[4];
        ret[0] = (byte)((data >> 24) & 0XFF);
        ret[1] = (byte)(data >> 16);
        ret[2] = (byte)(data >> 8);
        ret[3] = (byte)(data);
        return ret;
    }

    public static  int toInt32(byte[] data) {
        int ret = 0;
        for(int i = 0; i < 4; ++i) {
            ret = ret | ((data[i] & 0X000000FF) << ((3 -i) * 8));
        }
        return ret;
    }

    public static  int toInt32(byte[] data, int offset) {
        int ret = 0;
        for(int i = 0; i < 4; ++i) {
            ret = ret | ((data[i + offset] & 0X000000FF) << ((3 - i) * 8));
        }
        return ret;
    }
}
