package com.vaccine.injection;

import com.vaccine.core.ScanContext;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.Optional;

// Boolean based SQL Injection detection strategy
// This strategy injects payloads that evaluate to TRUE and FALSE
// and compares the responses to identify discrepancies.
public class BooleanBasedStrategy implements InjectionStrategy {

    // Some DBs are picky about comments, this usually works on MySQL/Postgres/MSSQL
    private static final String TRUE_SUFFIX  = "' OR 1=1-- ";
    private static final String FALSE_SUFFIX = "' OR 1=2-- ";

    @Override
    public String getName() {
        return "Boolean-based";
    }

    @Override
    public Optional<Vulnerability> isVulnerable(ScanContext context, Parameter parameter) {
        Target target = context.getTarget();

        // 1. Ensure baseline exists
        HttpResponseSnapshot baseline = context.getBaselineResponse();
        if (baseline == null) {
            HttpRequestSpec baseReq = target.toHttpRequestSpec();
            baseline = context.getHttpClient().send(baseReq);
            context.setBaselineResponse(baseline);
        }

        // 2. Build TRUE and FALSE payloads based on original value
        String originalValue = parameter.getValue() != null ? parameter.getValue() : "";

        String truePayload  = originalValue + TRUE_SUFFIX;
        String falsePayload = originalValue + FALSE_SUFFIX;

        // 3. Send TRUE request
        HttpRequestSpec trueSpec = target.toInjectedRequest(parameter, truePayload);
        HttpResponseSnapshot trueResp = context.getHttpClient().send(trueSpec);

        // 4. Send FALSE request
        HttpRequestSpec falseSpec = target.toInjectedRequest(parameter, falsePayload);
        HttpResponseSnapshot falseResp = context.getHttpClient().send(falseSpec);

        if (trueResp == null || falseResp == null) {
            return Optional.empty();
        }

        // 5. Compare responses
        // We mainly care that:
        //   - TRUE response is different from baseline
        //   - TRUE response is different from FALSE response
        // This usually means the injected condition actually affects the query.
        boolean trueVsBaseDifferent = isSignificantlyDifferent(baseline, trueResp);
        boolean trueVsFalseDifferent = isSignificantlyDifferent(trueResp, falseResp);

        if (trueVsBaseDifferent && trueVsFalseDifferent) {
            String evidence = "Boolean-based SQL injection detected.\n" +
                    "Parameter        : " + parameter.getName() + "\n" +
                    "True payload     : " + truePayload + "\n" +
                    "False payload    : " + falsePayload + "\n" +
                    "Reason           : Response for TRUE condition differs from both baseline and FALSE condition.\n" +
                    "Baseline length  : " + baseline.getBodyLength() + "\n" +
                    "TRUE length      : " + trueResp.getBodyLength() + "\n" +
                    "FALSE length     : " + falseResp.getBodyLength() + "\n";

            Vulnerability vuln = new Vulnerability(
                    parameter,
                    getName(),
                    truePayload,
                    evidence
            );
            return Optional.of(vuln);
        }

        return Optional.empty();
    }

    private boolean isSignificantlyDifferent(HttpResponseSnapshot a, HttpResponseSnapshot b) {
        if (a == null || b == null) return false;

        if (a.getStatusCode() != b.getStatusCode()) {
            return true;
        }

        int lenA = a.getBodyLength();
        int lenB = b.getBodyLength();
        if (lenA == 0 && lenB == 0) return false;

        int diff = Math.abs(lenA - lenB);
        double ratio = (lenA > 0 ? (double) diff / lenA : 1.0);

        // More than 20% change = significant
        return ratio > 0.2;
    }
}
