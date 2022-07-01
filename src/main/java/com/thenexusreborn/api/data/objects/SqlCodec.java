package com.thenexusreborn.api.data.objects;

public abstract class SqlCodec<T> {
    public abstract String encode(Object object);
    public abstract T decode(String encoded);
}
