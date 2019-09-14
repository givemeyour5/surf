package com.ocean.surf.soa.test;

import java.util.Map;

/**
 * Created by david on 17/7/16.
 */
public interface ISoaService {

    int call(int p1, Map<String, Map<Integer, Object>> p2);
}
