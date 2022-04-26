package com.proj2022pkg.twichplus.entity.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/* For store the login information sent from frontend */
public class LoginRequestBody {
    private final String userId;
    private final String password;

    @JsonCreator // 用來deserialization，JSON String 轉成 具體的class
    public LoginRequestBody(@JsonProperty("user_id") String userId, @JsonProperty("password") String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
