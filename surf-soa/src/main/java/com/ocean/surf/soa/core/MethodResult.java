package com.ocean.surf.soa.core;

import java.io.Serializable;

/**
 * Created by david on 17/8/4.
 */
public class MethodResult implements Serializable {

    private ReturnType type;
    private Object data;



    public ReturnType getType() {return type;}
    public void setType(ReturnType type) {this.type = type;}

    public Object getData() {return data;}
    public void setData(Object data) {this.data = data;}
}
