package com.proj2022pkg.twichplus.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseBody { // 返回LoginResponseBody的object
    // 定義要返回什麼
    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("name")
    private final String name;

    //ResponseBody將目前Item object的field轉成JSON string傳回給前端
    public LoginResponseBody(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
