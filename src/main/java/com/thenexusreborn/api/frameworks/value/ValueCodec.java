package com.thenexusreborn.api.frameworks.value;

import com.thenexusreborn.api.frameworks.value.Value.Type;
import com.thenexusreborn.api.storage.objects.SqlCodec;

import java.lang.reflect.Method;

public class ValueCodec implements SqlCodec<Value> {
    @Override
    public String encode(Object object) {
        Value value = (Value) object;
        if (value == null) {
            return "null";
        }
        
        String encoded = value.getType().name() + ":";
        if (value.get() == null) {
            return encoded + "null";
        }
        
        if (value.getType() == Type.ENUM) {
            Enum<?> enumObject = (Enum<?>) value.get();
            return encoded + enumObject.getClass().getName() + ":" + enumObject.name();
        }
        
        return encoded + value.get().toString();
    }
    
    @Override
    public Value decode(String encoded) {
        if (encoded == null || encoded.equals("") || encoded.equalsIgnoreCase("null")) {
            return null;
        }
        String[] split = encoded.split(":");
        if (split.length == 1) {
            return new Value(Value.Type.valueOf(split[0]), null);
        }
    
        Value.Type type = Value.Type.valueOf(split[0].toUpperCase());
        
        String rawValue = split[1];
        if (rawValue.equals("") || rawValue.equalsIgnoreCase("null")) {
            return new Value(type, null);
        }
        
        Object object = switch (type) {
            case INTEGER -> Integer.parseInt(rawValue);
            case STRING -> rawValue;
            case LONG -> Long.parseLong(rawValue);
            case BOOLEAN -> Boolean.parseBoolean(rawValue);
            case DOUBLE -> Double.parseDouble(rawValue);
            case ENUM -> {
                   try {
                       Class<?> enumClazz = Class.forName(split[1]);
                       Method method = enumClazz.getDeclaredMethod("valueOf", String.class);
                       yield  method.invoke(null, split[2]);
                   } catch (Exception e) {
                       e.printStackTrace(); //TODO For Debug Purposes, will remove after testing this is done, need to keep catch because of the checked Exception on Class.forName()
                       yield null;
                   }
            }
        };
        
        return new Value(type, object);
    }
}
