package com.aidims.aidimsbackend.dto;

import java.util.Map;

public class LoginResponse {
    private String status;  // Changed from boolean success to String status
    private String message;
    private Map<String, Object> data;

    public static LoginResponse success(Map<String, Object> data) {
        LoginResponse response = new LoginResponse();
        response.status = "success";  // Changed from success = true
        response.message = "Đăng nhập thành công";
        response.data = data;
        return response;
    }

    public static LoginResponse error(String message) {
        LoginResponse response = new LoginResponse();
        response.status = "error";  // Changed from success = false
        response.message = message;
        response.data = null;
        return response;
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Object> getData() { return data; }
}