package com.ocean.surf.core.test;

import com.ocean.surf.core.util.ByteConvert;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by xiecheng on 17/5/11.
 */
public class ByteConvertTest {

    @Test
    public void test() {
        int value = 173325;
        byte[] bvalue = ByteConvert.toBytes(value);
//        int ivalue = ByteConvert.toInt32(bvalue);
        int ivalue = ByteBuffer.wrap(bvalue).getInt();
        Assert.assertEquals(ivalue, value);
    }
}
