package com.rubyboat.pseudolib.resources;


import com.rubyboat.pseudolib.Pseudolib;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackProvider implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String protocol = ResourcePackProvider.class.getResource("").getProtocol();
        String baseResourcePath = "";
        if (Objects.equals(protocol, "jar")) {
            // Running from a jar
            try {
                String jarPath = ResourcePackProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart();
                baseResourcePath = new File(jarPath).getParent();
            } catch (URISyntaxException e) {
                throw new IOException("Failed to locate JAR file", e);
            }
        } else if (Objects.equals(protocol, "file")) {
            // Running from filesystem (e.g., IDE)
            baseResourcePath = "F:/Dev/fabric/pseudolib/build/resources/main/resourcepack";
        }

        String zipFileName = "resources.zip";

        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
        exchange.sendResponseHeaders(200, 0);

        try (ZipOutputStream zos = new ZipOutputStream(exchange.getResponseBody())) {
            zos.setLevel(Deflater.NO_COMPRESSION);
            // If running from JAR, list resources in a way that works within JAR files
            // If running from file system, list files normally
            if (Objects.equals(protocol, "jar")) {
                // Retrieve the path to the JAR file
                String jarPath = ResourcePackProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart();

                // Create a JarFile to read entries
                try (JarFile jarFile = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("resourcepack/") && !entry.isDirectory()) {
                            // Create a new zip entry and copy the resource content
                            ZipEntry zipEntry = new ZipEntry(name.substring("resourcepack/".length()));
                            zos.putNextEntry(zipEntry);
                            try (InputStream is = jarFile.getInputStream(entry)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) != -1) {
                                    zos.write(buffer, 0, length);
                                }
                            }
                            zos.closeEntry();
                        }
                    }
                }
            } else if (Objects.equals(protocol, "file")) {
                // Use baseResourcePath to list and add files to ZipOutputStream
                Path basePath = Paths.get(baseResourcePath);
                Files.walk(basePath)
                        .forEach(path -> {
                            String name = basePath.relativize(path).toString();
                            // For directories, append a "/" to ensure the zip entry is created as a directory
                            if (Files.isDirectory(path)) {
                                name = name.endsWith("/") ? name : name + "/";
                            }
                            ZipEntry zipEntry = new ZipEntry(name);
                            try {
                                zos.putNextEntry(zipEntry);
                                if (!Files.isDirectory(path)) {
                                    Files.copy(path, zos);
                                }
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new UncheckedIOException("Error adding resource to zip", e);
                            }
                        });

            }
            // No need for explicit close of zos, try-with-resources will handle it
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        // No need for explicit close of exchange's output stream either
    }
}
