package com.thenexusreborn.api.http;

public class HttpResponse {
    private final String url;
    private final String response;
    private final int responseCode;
    
    public HttpResponse(String url, String response, int responseCode) {
        this.url = url;
        this.response = response;
        this.responseCode = responseCode;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getResponse() {
        return response;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
}
