package com.ocean.surf.soa.core;


/**
 * Created by david on 17/7/16.
 */
public class ClientConfig {

    private int poolSize;
    private String address;
    private int serverPort;
    private int bufferSize;
    private long timeoutMilliseconds;

    public int getPoolSize() {return this.poolSize;}
    public void setPoolSize(int poolSize) {this.poolSize = poolSize;}

    public String getAddress() { return this.address;}
    public void setAddress(String address) { this.address = address;}

    public int getServerPort() {return this.serverPort;}
    public void setServerPort(int serverPort) {this.serverPort = serverPort;}

    public int getBufferSize() {return this.bufferSize;}
    public void setBufferSize(int bufferSize) {this.bufferSize = bufferSize;}

    public long getTimeoutMilliseconds() {return this.timeoutMilliseconds;}
    public void setTimeoutMilliseconds(long timeoutMilliseconds) {this.timeoutMilliseconds = timeoutMilliseconds;}

}
