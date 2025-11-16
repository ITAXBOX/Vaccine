package com.vaccine.enumeration;

import com.vaccine.core.ScanContext;
import com.vaccine.db.dialect.DbmsDialect;
import com.vaccine.model.Parameter;
import com.vaccine.model.ScanResult;
import com.vaccine.model.Target;
import com.vaccine.model.Vulnerability;

import java.util.Optional;

public class EnumerationService {

    public void enumerate(ScanContext context, ScanResult result) {
        DbmsDialect dialect = context.getDialect();
        if (dialect == null) {
            System.out.println("[*] Skipping enumeration: No DBMS dialect available");
            return;
        }

        // Must have a UNION vulnerability
        Optional<Vulnerability> unionVulnOpt = result.getVulnerabilities().stream()
                .filter(v -> "Union-based".equalsIgnoreCase(v.getStrategyName()))
                .findFirst();

        if (unionVulnOpt.isEmpty()) {
            System.out.println("[*] Skipping enumeration: No UNION-based vulnerability found");
            return;
        }

        Vulnerability vuln = unionVulnOpt.get();
        Parameter vulnParam = vuln.getParameter();
        Target target = result.getTarget();

        System.out.println("[*] Starting database enumeration using " + dialect.getDbmsType() + " dialect...");

        // Use the dialect-based enumerator (works for any DBMS with a dialect)
        UnionBasedEnumerator enumerator = new DialectBasedEnumerator();
        enumerator.enumerate(context, result, target, vulnParam);

        System.out.println("[*] Enumeration complete");
    }
}
