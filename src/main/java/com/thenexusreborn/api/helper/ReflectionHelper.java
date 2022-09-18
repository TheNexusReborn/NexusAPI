package com.thenexusreborn.api.helper;

import java.lang.reflect.*;
import java.util.*;

public class ReflectionHelper {
    
    public static Method getClassMethod(Class<?> clazz, String name, Class<?>... parameters) {
        try {
            return clazz.getDeclaredMethod(name, parameters);
        } catch (NoSuchMethodException e) {}
        
        return getClassMethod(clazz.getSuperclass(), name, parameters);
    }
    
    public static Set<Method> getClassMethods(Class<?> clazz) {
        Set<Method> methods = new LinkedHashSet<>(Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.getSuperclass() != null) {
            getClassMethods(clazz.getSuperclass(), methods);
        }
        return methods;
    }
    
    public static void getClassMethods(Class<?> clazz, Set<Method> methods) {
        if (methods == null) {
            methods = new LinkedHashSet<>();
        }
        
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.getSuperclass() != null) {
            getClassMethods(clazz.getSuperclass(), methods);
        }
    }
    
    public static Field getClassField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {}
        
        return getClassField(clazz.getSuperclass(), name);
    }
    
    public static Set<Field> getClassFields(Class<?> clazz) {
        Set<Field> fields = new LinkedHashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            getClassFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }
    
    public static void getClassFields(Class<?> clazz, Set<Field> fields) {
        if (fields == null) {
            fields = new LinkedHashSet<>();
        }
        
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            getClassFields(clazz.getSuperclass(), fields);
        }
    }
}
