package com.adisalagic.sportgeek.api;

public class ApiBuilder {
    private String url = "localhost";
    private int port = 1337;
    private String version = ApiVersion.V1;
    private String token = null;
    private boolean usePort = false;

    public ApiBuilder setUrl(String url){
        this.url = url;
        return this;
    }

    public ApiBuilder setPort(int port){
        this.port = port;
        return this;
    }

    public ApiBuilder setVersion(String version){
        this.version = version;
        return this;
    }

    public ApiBuilder setToken(String token){
        this.token = token;
        return this;
    }

    public ApiBuilder usePort(boolean shouldI){
        this.usePort = shouldI;
        return this;
    }

    public Api build(){
        return new Api(url, port, version, token, usePort);
    }
}
