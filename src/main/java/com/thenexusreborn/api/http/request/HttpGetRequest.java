package com.thenexusreborn.api.http.request;

import com.thenexusreborn.api.http.*;

import java.io.*;

public class HttpGetRequest extends HttpRequest {
    
    public HttpGetRequest(String url) throws IOException {
        super(url);
    }
}