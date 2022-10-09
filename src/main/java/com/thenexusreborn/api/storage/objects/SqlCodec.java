package com.thenexusreborn.api.storage.objects;

public abstract class SqlCodec<T> {
    public abstract String encode(Object object);
    public abstract T decode(String encoded);
}
