package com.vaccine.http;

public class HttpResponseSnapshot {
    private final int statusCode;
    private final String body;
    private final long responseTimeMillis;

    public HttpResponseSnapshot(int statusCode, String body, long responseTimeMillis) {
        this.statusCode = statusCode;
        this.body = body;
        this.responseTimeMillis = responseTimeMillis;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public int getBodyLength() {
        return body != null ? body.length() : 0;
    }
}
