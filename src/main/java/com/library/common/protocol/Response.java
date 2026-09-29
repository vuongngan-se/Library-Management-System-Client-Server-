package com.library.common.protocol;

public class Response {
    private String status; // SUCCESS, ERROR
    private String message;
    private Object data;

    public Response() {}

    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response success(String message, Object data) {
        return new Response("SUCCESS", message, data);
    }

    public static Response error(String message) {
        return new Response("ERROR", message, null);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
