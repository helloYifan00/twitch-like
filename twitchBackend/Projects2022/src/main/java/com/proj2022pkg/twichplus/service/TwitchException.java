package com.proj2022pkg.twichplus.service;
// Make sure it’s under the ....service package.

// 當代碼有問題可以區分出什麼問題
public class TwitchException extends RuntimeException{
    public TwitchException(String errorMessage){
        super(errorMessage);
    }
}
