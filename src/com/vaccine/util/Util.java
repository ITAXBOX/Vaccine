package com.vaccine.util;

import com.vaccine.http.HttpMethod;

public class Util {
    public static void validateMethod(String method) {
        HttpMethod.fromString(method);
    }

    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        // Check protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }

        // Use java.net.URI for comprehensive validation
        try {
            java.net.URI uri = new java.net.URI(url);

            // Validate that the URI has a scheme and host
            if (uri.getScheme() == null || uri.getHost() == null || uri.getHost().isEmpty()) {
                throw new IllegalArgumentException("URL must contain a valid scheme and host");
            }
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }

    public static void printUsage() {
        String usage = """
                Usage:
                  ./vaccine [options] <URL>
                
                Options:
                  -o <file>           Output file (default: vaccine.json)
                  -X <GET|POST>       HTTP method to use (default: GET)
                  -d <body>           HTTP request body for POST requests
                
                Examples:
                  ./vaccine https://example.com/page.php?id=1
                  ./vaccine -o results.json -X POST https://example.com/login.php
                """;
        System.out.println(usage);
    }
}
