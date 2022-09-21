package com.thenexusreborn.api.http;

import java.io.*;
import java.net.*;

public abstract class HttpRequest {
    protected final String url;
    protected final HttpURLConnection connection;
    
    public HttpRequest(String url) throws IOException {
        this.url = url;
        this.connection = (HttpURLConnection) new URL(this.url).openConnection();
    }
    
    public HttpResponse connect() throws IOException {
        this.connection.connect();
    
        try (Reader stream = new InputStreamReader(this.connection.getInputStream()); BufferedReader in = new BufferedReader(stream)) {
            StringBuilder response = new StringBuilder();
            String line;
        
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        
            return new HttpResponse(this.url, response.toString(), this.connection.getResponseCode());
        }
    }
}
