package com.vaccine.injection;

import com.vaccine.core.ScanContext;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.Optional;

// Union based SQL Injection detection strategy
// This strategy injects UNION SELECT payloads
// and looks for specific markers in the response to identify vulnerabilities.
public class UnionBasedStrategy implements InjectionStrategy {

    // Limit how far we brute-force the number of columns
    // to avoid excessive requests
    // 8 is a reasonable upper limit for many real-world queries
    private static final int MAX_COLUMNS = 8;
    // Marker string to identify successful injection
    // Use a unique string to avoid false positives
    private static final String MARKER = "VACCINE_UNION_MARK_ITAXBOX";

    @Override
    public String getName() {
        return "Union-based";
    }

    @Override
    public Optional<Vulnerability> isVulnerable(ScanContext context, Parameter parameter) {
        Target target = context.getTarget();
        HttpResponseSnapshot baseline = context.getBaselineResponse();

        // Defensive: if baseline is missing, get it now
        if (baseline == null) {
            HttpRequestSpec baselineSpec = target.toHttpRequestSpec();
            baseline = context.getHttpClient().send(baselineSpec);
            context.setBaselineResponse(baseline);
        }

        // Try different column counts
        for (int colCount = 1; colCount <= MAX_COLUMNS; colCount++) {
            String payload = buildUnionPayload(parameter.getValue(), colCount);

            HttpRequestSpec spec = target.toInjectedRequest(parameter, payload);
            HttpResponseSnapshot resp = context.getHttpClient().send(spec);

            if (resp == null || resp.getBody() == null) {
                continue;
            }

            String body = resp.getBody();

            // If marker appears in body, it's very likely UNION-based injection worked
            if (body.contains(MARKER)) {
                Vulnerability vuln = getVulnerability(parameter, payload, colCount);
                return Optional.of(vuln);
            }
        }

        return Optional.empty();
    }

    private Vulnerability getVulnerability(Parameter parameter, String payload, int colCount) {
        String evidence = "Union-based SQL injection detected.\n" +
                "Parameter      : " + parameter.getName() + "\n" +
                "Payload        : " + payload + "\n" +
                "Columns count  : " + colCount + "\n" +
                "Marker found   : " + MARKER;

        return new Vulnerability(
                parameter,
                getName(),
                payload,
                evidence
        );
    }

    // Builds a UNION-based SQL injection payload
    private String buildUnionPayload(String originalValue, int columnsCount) {
        StringBuilder columns = new StringBuilder();

        // Put the marker in the first column, NULL in others
        for (int i = 0; i < columnsCount; i++) {
            if (i > 0) columns.append(", ");
            if (i == 0) {
                columns.append("'").append(MARKER).append("'");
            } else {
                columns.append("NULL");
            }
        }
        // Construct the full UNION payload
        String union = "' UNION ALL SELECT " + columns + " -- ";

        // If originalValue is empty, just use the union part
        if (originalValue == null || originalValue.isEmpty()) {
            return union;
        }

        return originalValue + union;
    }
}
