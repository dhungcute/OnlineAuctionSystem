package com.auction.api.protocol;

import java.io.Serializable;

/**
 * Gói tin tiêu chuẩn Server phản hồi ngược lại cho Client
 */
public class NetworkResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success; 
    private String message;  
    private Object data;     

    public NetworkResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static NetworkResponse ok(Object data) {
        return new NetworkResponse(true, "Thành công", data);
    }

    public static NetworkResponse ok(String message, Object data) {
        return new NetworkResponse(true, message, data);
    }

    public static NetworkResponse fail(String message) {
        return new NetworkResponse(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}