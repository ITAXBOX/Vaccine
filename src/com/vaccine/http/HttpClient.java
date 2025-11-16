package com.vaccine.http;

public interface HttpClient {
    HttpResponseSnapshot send(HttpRequestSpec spec);
}
