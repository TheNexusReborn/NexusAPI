package com.thenexusreborn.api.maven;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenLibraries {
    MavenLibrary[] value() default {};
}
