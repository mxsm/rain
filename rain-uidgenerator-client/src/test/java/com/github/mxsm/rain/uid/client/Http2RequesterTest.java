package com.github.mxsm.rain.uid.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class Http2RequesterTest {

    private final List<HttpServer> servers = new ArrayList<>();

    @AfterEach
    void stopServers() {
        for (HttpServer server : servers) {
            server.stop(0);
        }
    }

    @Test
    void roundRobinsSuccessfulRequests() throws Exception {
        TestServer first = startServer(200, "first");
        TestServer second = startServer(200, "second");
        Config config = config(first.uri(), second.uri());

        assertEquals("first", Http2Requester.executeGET(config, "/uid"));
        assertEquals("second", Http2Requester.executeGET(config, "/uid"));
        assertEquals("first", Http2Requester.executeGET(config, "/uid"));
    }

    @Test
    void failsOverToNextEndpointWhenFirstRequestFails() throws Exception {
        TestServer first = startServer(500, "failed");
        TestServer second = startServer(200, "ok");
        Config config = config(first.uri(), second.uri());
        config.setMaxRetries(1);

        assertEquals("ok", Http2Requester.executeGET(config, "/uid"));
    }

    @Test
    void sendsBearerToken() throws Exception {
        TestServer server = startTokenServer("secret");
        Config config = config(server.uri());
        config.setToken("secret");

        assertEquals("authorized", Http2Requester.executeGET(config, "/uid"));
    }

    @Test
    void sendsPostRequests() throws Exception {
        TestServer server = startMethodServer("POST");
        Config config = config(server.uri());

        assertEquals("POST", Http2Requester.executePOST(config, "/uid"));
    }

    private Config config(String... uris) {
        Config config = new Config();
        config.setUidGeneratorServerUris(List.of(uris));
        config.setConnectTimeout(Duration.ofSeconds(1));
        config.setReadTimeout(Duration.ofSeconds(2));
        config.setMaxRetries(0);
        return config;
    }

    private TestServer startServer(int status, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/uid", exchange -> write(exchange, status, body));
        server.setExecutor(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "test-http-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        servers.add(server);
        return new TestServer(server);
    }

    private TestServer startTokenServer(String expectedToken) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/uid", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (("Bearer " + expectedToken).equals(authorization)) {
                write(exchange, 200, "authorized");
                return;
            }
            write(exchange, 401, "unauthorized");
        });
        server.setExecutor(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "test-http-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        servers.add(server);
        return new TestServer(server);
    }

    private TestServer startMethodServer(String expectedMethod) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/uid", exchange -> {
            if (expectedMethod.equals(exchange.getRequestMethod())) {
                write(exchange, 200, exchange.getRequestMethod());
                return;
            }
            write(exchange, 405, exchange.getRequestMethod());
        });
        server.setExecutor(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "test-http-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        servers.add(server);
        return new TestServer(server);
    }

    private void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private record TestServer(HttpServer server) {

        String uri() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }
    }
}
