package com.ocean.surf.soa.test;

import java.util.Map;

/**
 * Created by david on 17/7/16.
 */
public class SoaService implements ISoaService {

    public int call(int p1, Map<String, Map<Integer, Object>> p2) {
//        System.out.println(p1);
        int tmp = (Integer) p2.get("a").get(1);
        if(tmp == 1)
           return ++p1;
        else
            return -1;
    }
}
