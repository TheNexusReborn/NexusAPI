package com.thenexusreborn.api.data.annotations;

import com.thenexusreborn.api.data.objects.SqlCodec;

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
