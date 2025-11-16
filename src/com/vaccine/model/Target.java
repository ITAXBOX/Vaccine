package com.vaccine.model;

import com.vaccine.http.HttpRequestSpec;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Represents an HTTP target with its parameters and methods to create requests
// with injected payloads for testing.
// It can parse URLs to extract parameters and build HTTP request specifications.
// It supports both GET and POST methods.
// It can generate requests with specific parameters injected with payloads.
public class Target {
    private final String baseUrl;
    private final String method;
    private final String rawBody;
    private final List<Parameter> parameters;
    private final Map<String, String> headers;

    public Target(String baseUrl, String method, List<Parameter> parameters, String rawBody, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.method = method;
        this.rawBody = rawBody;
        this.parameters = parameters;
        this.headers = headers;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getMethod() {
        return method;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static Target fromUrl(String url, String method, String body, Map<String, String> headers) {
        List<Parameter> params = new ArrayList<>();

        String base = url;
        String queryString;

        // Extract GET parameters
        int idx = url.indexOf('?');
        if (idx != -1) {
            base = url.substring(0, idx);
            queryString = url.substring(idx + 1);

            for (String kv : queryString.split("&")) {
                String[] parts = kv.split("=", 2);
                String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                params.add(new Parameter(name, value, Parameter.Location.QUERY));
            }
        }

        // Extract POST parameters
        if ("POST".equalsIgnoreCase(method) && body != null) {
            for (String kv : body.split("&")) {
                String[] parts = kv.split("=", 2);
                String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                params.add(new Parameter(name, value, Parameter.Location.BODY));
            }
        }

        return new Target(base, method, params, body, headers);
    }


    public HttpRequestSpec toHttpRequestSpec() {
        HttpRequestSpec spec = new HttpRequestSpec()
                .method(this.method)
                .url(buildUrlWithParams(null, null));

        if (headers != null) {
            spec.getHeaders().putAll(headers);
        }

        if ("POST".equalsIgnoreCase(method) && rawBody != null) {
            spec.rawBody(rawBody);
        }

        return spec;
    }


    public HttpRequestSpec toInjectedRequest(Parameter targetParam, String injectedValue) {
        HttpRequestSpec spec = new HttpRequestSpec()
                .method(this.method);

        if ("GET".equalsIgnoreCase(method)) {
            spec.url(buildUrlWithParams(targetParam, injectedValue));
        } else if ("POST".equalsIgnoreCase(method)) {
            spec.url(this.baseUrl);
            spec.rawBody(buildPostBody(targetParam, injectedValue));
        }

        if (headers != null) {
            spec.getHeaders().putAll(headers);
        }

        return spec;
    }


    private String buildPostBody(Parameter targetParam, String injectedValue) {
        if (rawBody == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            if (p.getLocation() != Parameter.Location.BODY) continue;

            if (!sb.isEmpty()) sb.append("&");
            sb.append(urlEncode(p.getName())).append("=");

            if (p == targetParam) sb.append(urlEncode(injectedValue));
            else sb.append(urlEncode(p.getValue()));
        }
        return sb.toString();
    }


    private String buildUrlWithParams(Parameter injectedParam, String injectedValue) {
        StringBuilder sb = new StringBuilder(this.baseUrl);
        if (!parameters.isEmpty()) {
            sb.append("?");
            for (int i = 0; i < parameters.size(); i++) {
                Parameter p = parameters.get(i);
                if (i > 0) sb.append("&");
                sb.append(urlEncode(p.getName())).append("=");

                String value = p.getValue();
                if (injectedParam != null && p == injectedParam) {
                    value = injectedValue;
                }
                sb.append(urlEncode(value));
            }
        }
        return sb.toString();
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

}
