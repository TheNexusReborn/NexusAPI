package com.thenexusreborn.api.storage.annotations;

import com.thenexusreborn.api.storage.objects.ObjectHandler;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableInfo {
    String value();
    Class<? extends ObjectHandler> handler() default ObjectHandler.class;
}
