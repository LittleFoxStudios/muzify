package com.littlefoxstudios.muzify.internet;

public class AccessToken {
    private String accessToken;
    private Long expiryTime;

    public AccessToken(String accessToken, Long expiryTime){
        this.accessToken = accessToken;
        this.expiryTime = expiryTime;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public Long getExpiryTime(){
        return expiryTime;
    }
}
