package com.rakesh.qapragyan17;

import com.google.gson.annotations.SerializedName;

class Response {

    @SerializedName("status_code")
    private int statusCode;

    private String message;

    @SerializedName("user_id")
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}