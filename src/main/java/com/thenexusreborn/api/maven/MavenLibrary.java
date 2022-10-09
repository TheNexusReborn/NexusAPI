package com.thenexusreborn.api.maven;

import java.lang.annotation.*;

@Repeatable(MavenLibraries.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenLibrary {
    
    String groupId();
    
    String artifactId();
    
    String version();
    
    Repository repo() default @Repository(url = "https://repo1.maven.org/maven2");
}
