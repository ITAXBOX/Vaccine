package com.vaccine.core;

import com.vaccine.db.DbmsType;
import com.vaccine.db.dialect.DbmsDialectFactory;
import com.vaccine.enumeration.EnumerationService;
import com.vaccine.http.HttpRequestSpec;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.ScanResult;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.ArrayList;
import java.util.List;

public class ScanEngine {

    private final ScanContext context;

    public ScanEngine(ScanContext context) {
        this.context = context;
    }

    public ScanResult scan() {
        // 1. Build target
        Target target = Target.fromUrl(context.getConfig().getUrl()
                , context.getConfig().getMethod(), context.getConfig().getBody(), context.getConfig().getHeaders());
        context.setTarget(target);

        System.out.println("[*] Parsed URL: " + target.getBaseUrl());
        System.out.println("[*] Found " + target.getParameters().size() + " parameter(s) to test");

        if (target.getParameters().isEmpty()) {
            System.out.println("[!] Warning: No parameters found in URL. Add query parameters to test for vulnerabilities.");
            System.out.println("[!] Example: " + target.getBaseUrl() + "?id=1");
        } else {
            for (Parameter p : target.getParameters()) {
                System.out.println("    - " + p.getName() + " = " + p.getValue());
            }
        }

        // 2. Baseline request (no injection)
        System.out.println("[*] Sending baseline request...");
        HttpRequestSpec baselineSpec = target.toHttpRequestSpec();
        HttpResponseSnapshot baselineResp = context.getHttpClient().send(baselineSpec);
        context.setBaselineResponse(baselineResp);
        System.out.println("[*] Baseline response: " + baselineResp.getStatusCode() + " (" + baselineResp.getBodyLength() + " bytes)");

        // 3. DBMS fingerprinting (still stub)
        System.out.println("[*] Fingerprinting DBMS...");
        DbmsType dbms = context.getFingerprintingService()
                .detectDbms(context);
        context.setDbmsType(dbms);
        context.setDialect(DbmsDialectFactory.create(dbms));
        System.out.println("[*] Detected DBMS: " + dbms);

        // 4. Test parameters
        List<Vulnerability> vulns = new ArrayList<>();

        if (!target.getParameters().isEmpty()) {
            System.out.println("[*] Testing parameters for SQL injection...");
        }

        for (Parameter param : target.getParameters()) {
            System.out.println("[*] Testing parameter: " + param.getName());
            for (var strategy : context.getStrategies()) {
                System.out.println("    [*] Trying " + strategy.getName() + " strategy...");
                strategy.isVulnerable(context, param).ifPresent(v -> {
                    System.out.println("    [+] VULNERABLE! Found with " + strategy.getName());
                    vulns.add(v);
                });
            }
        }

        // 5. Build result
        ScanResult scanResult = new ScanResult(target, vulns, dbms);

        // 6. Enumeration
        EnumerationService enumerationService = new EnumerationService();
        enumerationService.enumerate(context, scanResult);

        // 7. Return result
        return scanResult;
    }
}
