package com.vaccine.cli;

import java.util.HashMap;
import java.util.Map;

import static com.vaccine.util.Util.validateMethod;
import static com.vaccine.util.Util.validateUrl;

public class CommandLineOptions {

    private final String url;
    private final String method;
    private final String outputFile;
    private final String body;
    private final Map<String, String> headers;

    private CommandLineOptions(String url, String method, String outputFile, String body, Map<String, String> headers) {
        this.url = url;
        this.method = method;
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

    public static CommandLineOptions parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided.");
        }

        String method = "GET";
        String outputFile = "vaccine.txt";
        String url = null;
        String body = null;
        Map<String, String> headers = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-o":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for -o option.");
                    }
                    outputFile = args[++i];
                    break;

                case "-X":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for -X option.");
                    }
                    method = args[++i].toUpperCase();
                    validateMethod(method);
                    break;

                case "-d":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for -d option.");
                    }
                    body = args[++i];
                    break;

                case "-H":
                case "--header":
                    if (i + 1 >= args.length)
                        throw new IllegalArgumentException("Missing value for -H");
                    String headerLine = args[++i];
                    String[] parts = headerLine.split(":", 2);
                    if (parts.length != 2)
                        throw new IllegalArgumentException("Invalid header format. Use -H \"Name: Value\"");
                    headers.put(parts[0].trim(), parts[1].trim());
                    break;

                default:
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    if (url == null)
                        url = arg;
                    else
                        throw new IllegalArgumentException("Only one URL is allowed");
                    break;
            }
        }

        validateUrl(url);

        return new CommandLineOptions(url, method, outputFile, body, headers);
    }
}
