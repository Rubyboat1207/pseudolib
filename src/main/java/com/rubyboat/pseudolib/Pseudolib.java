package com.rubyboat.pseudolib;

import com.rubyboat.pseudolib.resources.ResourcePackProvider;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.SecureRandom;

public class Pseudolib implements ModInitializer {
    // check SendResourcePackTask

    public static String RESOURCE_PACK_PATH;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @Override
    public void onInitialize() {
        RESOURCE_PACK_PATH = generateRandomString(300);

        int port = 8000;
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(server == null) {
            return;
        }
        server.createContext("/" + RESOURCE_PACK_PATH, new ResourcePackProvider());
        server.createContext("/staticrp_path", new ResourcePackProvider());
        server.createContext("/rp_path", (HttpExchange exchange) -> {
            var bytes = RESOURCE_PACK_PATH.getBytes(Charset.defaultCharset());

            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        });
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }
}
