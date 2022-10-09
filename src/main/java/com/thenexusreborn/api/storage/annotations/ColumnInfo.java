package com.thenexusreborn.api.storage.annotations;

import com.thenexusreborn.api.storage.objects.SqlCodec;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnInfo {
    String name() default "";
    String type() default "";
    boolean primaryKey() default false;
    boolean autoIncrement() default false;
    boolean notNull() default false;
    Class<? extends SqlCodec> codec() default SqlCodec.class;
}
