package com.vaccine.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

// Simple HTTP client implementation using HttpURLConnection
// Supports basic GET and POST requests with headers and body
public class SimpleHttpClient implements HttpClient {

    @Override
    public HttpResponseSnapshot send(HttpRequestSpec spec) {
        long start = System.currentTimeMillis();
        int status = -1;
        String body = "";

        try {
            URI uri = new URI(spec.getUrl());
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod(spec.getMethod());

            if (spec.getHeaders() != null) {
                for (var entry : spec.getHeaders().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // --- POST body ---
            if ("POST".equalsIgnoreCase(spec.getMethod()) && spec.getRawBody() != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(spec.getRawBody().getBytes(StandardCharsets.UTF_8));
                }
            }

            status = conn.getResponseCode();

            InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    body = br.lines().collect(Collectors.joining("\n"));
                }
            }

        } catch (Exception e) {
            body = e.toString();
        }

        long time = System.currentTimeMillis() - start;
        return new HttpResponseSnapshot(status, body, time);
    }

}
