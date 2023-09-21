package com.thenexusreborn.api.maven;

import com.thenexusreborn.api.NexusAPI;
import me.firestar311.starlib.api.reflection.URLClassLoaderAccess;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Objects;

public final class LibraryLoader {
    
//    @SuppressWarnings("Guava")
//    private static final Supplier<URLClassLoaderAccess> URL_INJECTOR = Suppliers.memoize(() -> URLClassLoaderAccess.create(NexusAPI.getApi().getLoader()));
//    
    
    public static void loadAll(Object object, URLClassLoader classLoader) {
        loadAll(object.getClass(), classLoader);
    }
    
    public static void loadAll(Class<?> clazz, URLClassLoader classLoader) {
        MavenLibrary[] libs = clazz.getDeclaredAnnotationsByType(MavenLibrary.class);
    
        for (MavenLibrary lib : libs) {
            load(lib.groupId(), lib.artifactId(), lib.version(), lib.repo().value(), classLoader);
        }
    }
    
    public static void load(String groupId, String artifactId, String version, URLClassLoader classLoader) {
        load(groupId, artifactId, version, "https://repo1.maven.org/maven2", classLoader);
    }
    
    public static void load(String groupId, String artifactId, String version, String repoUrl, URLClassLoader classLoader) {
        load(new Dependency(groupId, artifactId, version, repoUrl), classLoader);
    }
    
    public static void load(Dependency d, URLClassLoader classLoader) {
        NexusAPI.getApi().getLogger().info(String.format("Loading dependency %s:%s:%s from %s", d.groupId(), d.artifactId(), d.version(), d.repoUrl()));
        String name = d.artifactId() + "-" + d.version();
        
        File saveLocation = new File(getLibFolder(), name + ".jar");
        if (!saveLocation.exists()) {
            try {
                NexusAPI.getApi().getLogger().info("Dependency '" + name + "' is not already in the libraries folder. Attempting to download...");
                URL url = d.getUrl();
                
                try (InputStream is = url.openStream()) {
                    Files.copy(is, saveLocation.toPath());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
    
            NexusAPI.getApi().getLogger().info("Dependency '" + name + "' successfully downloaded.");
        }
        
        if (!saveLocation.exists()) {
            throw new RuntimeException("Unable to download dependency: " + d);
        }
        
        try {
            URLClassLoaderAccess.create(classLoader).addURL(saveLocation.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load dependency: " + saveLocation, e);
        }
        
        NexusAPI.getApi().getLogger().info("Loaded dependency '" + name + "' successfully.");
    }
    
    private static File getLibFolder() {
        File pluginDataFolder = NexusAPI.getApi().getFolder();
        File pluginsDir = pluginDataFolder.getParentFile();
        
        File helperDir = new File(pluginsDir, "helper");
        File libs = new File(helperDir, "libraries");
        libs.mkdirs();
        return libs;
    }
    
    public record Dependency(String groupId, String artifactId, String version, String repoUrl) {
            public Dependency(String groupId, String artifactId, String version, String repoUrl) {
                this.groupId = Objects.requireNonNull(groupId, "groupId");
                this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
                this.version = Objects.requireNonNull(version, "version");
                this.repoUrl = Objects.requireNonNull(repoUrl, "repoUrl");
            }
            
            public URL getUrl() throws MalformedURLException {
                String repo = this.repoUrl;
                if (!repo.endsWith("/")) {
                    repo += "/";
                }
                repo += "%s/%s/%s/%s-%s.jar";
                
                String url = String.format(repo, this.groupId.replace(".", "/"), this.artifactId, this.version, this.artifactId, this.version);
                return new URL(url);
            }
            
            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                }
                if (!(o instanceof final Dependency other)) {
                    return false;
                }
                return this.groupId().equals(other.groupId()) &&
                        this.artifactId().equals(other.artifactId()) &&
                        this.version().equals(other.version()) &&
                        this.repoUrl().equals(other.repoUrl());
            }
            
            @Override
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                result = result * PRIME + this.groupId().hashCode();
                result = result * PRIME + this.artifactId().hashCode();
                result = result * PRIME + this.version().hashCode();
                result = result * PRIME + this.repoUrl().hashCode();
                return result;
            }
            
            @Override
            public String toString() {
                return "LibraryLoader.Dependency(" +
                        "groupId=" + this.groupId() + ", " +
                        "artifactId=" + this.artifactId() + ", " +
                        "version=" + this.version() + ", " +
                        "repoUrl=" + this.repoUrl() + ")";
            }
        }
}