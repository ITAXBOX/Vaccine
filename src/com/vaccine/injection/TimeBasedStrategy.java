package com.vaccine.injection;

import com.vaccine.core.ScanContext;
import com.vaccine.db.DbmsType;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.Optional;

// Time based SQL Injection detection strategy
// This strategy injects payloads that cause delays in the database response
// and measures the response time to identify vulnerabilities.
public class TimeBasedStrategy implements InjectionStrategy {

    // Seconds to sleep
    private static final int DELAY_SECONDS = 3;

    // We consider it delayed if response time is >= baseline + this margin (ms)
    private static final long DELAY_MARGIN_MS = 1500L; // 1.5s

    @Override
    public String getName() {
        return "Time-based";
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

        DbmsType dbms = context.getDbmsType();
        String originalValue = parameter.getValue() != null ? parameter.getValue() : "";

        String timePayload = buildTimePayload(dbms, originalValue);

        HttpRequestSpec timeSpec = target.toInjectedRequest(parameter, timePayload);
        HttpResponseSnapshot timeResp = context.getHttpClient().send(timeSpec);

        if (timeResp == null) {
            return Optional.empty();
        }

        long baseTime = baseline.getResponseTimeMillis();
        long delayedTime = timeResp.getResponseTimeMillis();

        boolean isDelayed = isSignificantlyDelayed(baseTime, delayedTime);

        if (isDelayed) {
            String evidence = "Time-based SQL injection detected.\n" +
                    "Parameter       : " + parameter.getName() + "\n" +
                    "Payload         : " + timePayload + "\n" +
                    "DBMS            : " + dbms + "\n" +
                    "Baseline time   : " + baseTime + " ms\n" +
                    "Delayed time    : " + delayedTime + " ms\n" +
                    "Delay margin    : " + DELAY_MARGIN_MS + " ms\n" +
                    "Reason          : Response time after injection is significantly higher than baseline.";

            Vulnerability vuln = new Vulnerability(
                    parameter,
                    getName(),
                    timePayload,
                    evidence
            );
            return Optional.of(vuln);
        }

        return Optional.empty();
    }

    private String buildTimePayload(DbmsType dbms, String originalValue) {
        // Fallback to MySQL-style if unknown
        if (dbms == null) {
            dbms = DbmsType.UNKNOWN;
        }

        String suffix = switch (dbms) {
            case MYSQL ->
                // MySQL / MariaDB
                // ' OR SLEEP(3)--
                    "' OR SLEEP(" + DELAY_SECONDS + ")-- ";
            case POSTGRESQL ->
                // PostgreSQL
                // '; SELECT pg_sleep(3);--
                // (works when injected in string context; closes the string and the query, then sleeps)
                    "'; SELECT pg_sleep(" + DELAY_SECONDS + ");--";
            case MSSQL ->
                // Microsoft SQL Server
                // '; WAITFOR DELAY '0:0:3';--
                    "'; WAITFOR DELAY '0:0:" + DELAY_SECONDS + "';--";
            case ORACLE ->
                // Oracle
                // ' OR DBMS_LOCK.SLEEP(3) IS NULL --
                    "' OR DBMS_LOCK.SLEEP(" + DELAY_SECONDS + ") IS NULL --";
            case SQLITE ->
                // SQLite has no native sleep(). A common trick is to cause heavy computation,
                // but it's unreliable and environment-dependent.
                // For the purposes of this project, we can attempt a big randomblob().
                // ' OR randomblob(1000000000) IS NULL --  (may cause delay or error)
                    "' OR randomblob(1000000000) IS NULL --";
            default ->
                // Generic "most-likely-works" payload (MySQL style)
                    "' OR SLEEP(" + DELAY_SECONDS + ")-- ";
        };

        return originalValue + suffix;
    }

    private boolean isSignificantlyDelayed(long baselineMs, long testMs) {
        if (baselineMs <= 0) {
            // If baseline is weird, just ensure test >= DELAY_SECONDS * 1000
            return testMs >= (DELAY_SECONDS * 1000L - 500L);
        }
        long diff = testMs - baselineMs;
        return diff >= DELAY_MARGIN_MS;
    }
}
