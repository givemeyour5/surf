package com.ocean.surf.soa.core;

import java.io.Serializable;

/**
 * Created by david on 17/7/16.
 */
public class MethodCallContext implements Serializable {

    private String methodName;
    private Object[] arguments;

    public MethodCallContext() {}

    public String getMethodName() { return this.methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Object[] getArguments() { return this.arguments; }
    public void setArguments(Object[] arguments) { this.arguments = arguments; }

}
