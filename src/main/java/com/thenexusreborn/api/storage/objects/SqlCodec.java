package com.thenexusreborn.api.storage.objects;

public interface SqlCodec<T> {
    String encode(Object object);
    T decode(String encoded);
}
