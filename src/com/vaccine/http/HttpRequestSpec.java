package com.vaccine.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestSpec {
    private String method;
    private String rawBody;
    private String url;
    private final Map<String, String> headers = new HashMap<>();

    public String getMethod() {
        return method;
    }

    public HttpRequestSpec method(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    public void rawBody(String body) {
        this.rawBody = body;
    }

    public String getRawBody() {
        return rawBody;
    }


    public String getUrl() {
        return url;
    }

    public HttpRequestSpec url(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
