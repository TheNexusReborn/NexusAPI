package com.thenexusreborn.api.util;

public class Response<T> {
    
    public enum Type {
        SUCCESS, FAILURE
    }
    
    private final T response;
    private final Type type;
    private final Throwable error;
    
    public Response(T response, Type type, Throwable error) {
        this.response = response;
        this.type = type;
        this.error = error;
    }
    
    public Response(T response, Type type) {
        this(response, type, null);
    }
    
    public Response(Type type, Throwable error) {
        this(null, type, error);
    }
    
    public T get() {
        return response;
    }
    
    public Type getType() {
        return type;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public boolean success() {
        return this.type == Type.SUCCESS;
    }
}
