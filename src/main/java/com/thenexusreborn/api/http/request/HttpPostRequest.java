package com.thenexusreborn.api.http.request;

import com.thenexusreborn.api.http.*;

import java.io.*;

public class HttpPostRequest extends HttpRequest {
    
    public HttpPostRequest(String url) throws IOException {
        super(url);
        this.connection.setRequestMethod("POST");
    }
}
