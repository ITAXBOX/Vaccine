package com.vaccine.config;

import java.util.Map;

public class ScanConfig {
    private final String url;
    private final String method;
    private final String outputFile;
    private final String body;
    private final Map<String, String> headers;

    public ScanConfig(String url, String method, String outputFile, String body, Map<String, String> headers) {
        this.url = url;
        this.method = method.toUpperCase();
        this.outputFile = outputFile;
        this.body = body;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
