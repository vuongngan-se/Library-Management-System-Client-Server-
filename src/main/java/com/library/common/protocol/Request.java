package com.library.common.protocol;

public class Request {
    private String action;
    private Object data;

    public Request() {}

    public Request(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
