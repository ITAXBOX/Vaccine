package com.vaccine.injection;

import com.vaccine.core.ScanContext;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Error based SQL Injection detection strategy
// This strategy injects payloads that cause SQL errors
// and looks for error messages or significant response changes.
public class ErrorBasedStrategy implements InjectionStrategy {

    // Generic SQL error signatures (you can expand this list anytime)
    private static final List<String> ERROR_SIGNATURES = Arrays.asList(
            "you have an error in your sql syntax",          // MySQL / MariaDB
            "mysql server version for the right syntax",
            "warning: mysql",
            "unclosed quotation mark after the character string", // MSSQL
            "quoted string not properly terminated",          // Oracle
            "sql syntax error",
            "sqlstate",
            "sqlite error",
            "psqlexception",
            "syntax error at or near"
    );

    @Override
    public String getName() {
        return "Error-based";
    }

    @Override
    public Optional<Vulnerability> isVulnerable(ScanContext context, Parameter parameter) {
        Target target = context.getTarget();
        HttpResponseSnapshot baseline = context.getBaselineResponse();

        // In case baseline somehow isn't set (defensive)
        if (baseline == null) {
            HttpRequestSpec baselineSpec = target.toHttpRequestSpec();
            baseline = context.getHttpClient().send(baselineSpec);
            context.setBaselineResponse(baseline);
        }

        // Some classic bad quote payloads
        String[] suffixes = {
                "'", "\"", "')", "\")", "'-- ", "\"-- ", "'))", "\"))"
        };

        for (String suffix : suffixes) {
            String payload = parameter.getValue() + suffix;

            HttpRequestSpec spec = target.toInjectedRequest(parameter, payload);
            HttpResponseSnapshot resp = context.getHttpClient().send(spec);

            if (hasSqlError(baseline, resp)) {
                String evidence = "Error-based SQL injection detected.\n" +
                        "Parameter: " + parameter.getName() + "\n" +
                        "Payload : " + payload + "\n" +
                        "Reason  : SQL error pattern found in response.";

                Vulnerability vuln = new Vulnerability(
                        parameter,
                        getName(),
                        payload,
                        evidence
                );
                return Optional.of(vuln);
            }
        }

        return Optional.empty();
    }

    private boolean hasSqlError(HttpResponseSnapshot baseline, HttpResponseSnapshot test) {
        if (test == null)
            return false;

        String body = test.getBody();
        if (body == null) return false;

        String lower = body.toLowerCase();

        // Check for explicit SQL error messages
        for (String sig : ERROR_SIGNATURES) {
            if (lower.contains(sig)) {
                return true;
            }
        }

        // Check for significant changes in response size (both increases AND decreases)
        // A significant change might indicate an error page or broken query
        int baseLen = baseline != null ? baseline.getBodyLength() : 0;
        int testLen = test.getBodyLength();
        int diff = Math.abs(testLen - baseLen);

        // Consider changes greater than 5% as potentially significant
        // (lowered from 50% to catch smaller changes)
        if (baseLen > 0 && diff > (baseLen * 0.05)) {
            // If the response became significantly smaller,
            // it's more likely to be an error (content removed due to broken query)
            if (testLen < baseLen) {
                return true;
            }

            // If it became larger, require a bigger threshold (30%)
            return diff > (baseLen * 0.30);
        }

        return false;
    }
}
