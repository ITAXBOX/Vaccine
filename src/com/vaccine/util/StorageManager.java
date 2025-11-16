package com.vaccine.util;

import com.vaccine.model.ScanResult;
import com.vaccine.model.Vulnerability;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StorageManager {

    private static final String LINE_SEP = "------------------------------------------------------------";
    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StorageManager() {
    }

    public static void save(String filePath, ScanResult result) {
        if (filePath == null || filePath.trim().isEmpty() || result == null) {
            return;
        }

        try (FileWriter writer = new FileWriter(filePath, true)) {
            String timestamp = LocalDateTime.now().format(TS_FORMATTER);

            writer.write(LINE_SEP + System.lineSeparator());
            writer.write("VACCINE SQL Injection SCAN REPORT" + System.lineSeparator());
            writer.write(LINE_SEP + System.lineSeparator());
            writer.write("Date    : " + timestamp + System.lineSeparator());
            writer.write("Target  : " + safe(result.getTarget().getBaseUrl()) + System.lineSeparator());
            writer.write("Method  : " + safe(result.getTarget().getMethod()) + System.lineSeparator());
            writer.write("DBMS    : " + safe(String.valueOf(result.getDbmsType())) + System.lineSeparator());
            writer.write(System.lineSeparator());

            // === Summary ===
            writer.write("[SUMMARY]" + System.lineSeparator());
            int vulnCount = result.getVulnerabilities() != null ? result.getVulnerabilities().size() : 0;
            int dbCount = result.getDatabaseNames() != null ? result.getDatabaseNames().size() : 0;
            writer.write("  Vulnerabilities found : " + vulnCount + System.lineSeparator());
            writer.write("  Databases discovered   : " + dbCount + System.lineSeparator());
            writer.write(System.lineSeparator());

            // === Vulnerabilities ===
            writer.write("[VULNERABILITIES]" + System.lineSeparator());
            List<Vulnerability> vulns = result.getVulnerabilities();
            if (vulns == null || vulns.isEmpty()) {
                writer.write("  (none)" + System.lineSeparator());
            } else {
                int idx = 1;
                for (Vulnerability v : vulns) {
                    writer.write("  #" + idx++ + System.lineSeparator());
                    writer.write("    Parameter : " + safe(v.getParameter().getName()) + System.lineSeparator());
                    writer.write("    Strategy  : " + safe(v.getStrategyName()) + System.lineSeparator());
                    writer.write("    Payload   : " + safe(v.getPayload()) + System.lineSeparator());
                    writer.write("    Evidence  :" + System.lineSeparator());
                    indentMultiline(writer, v.getEvidence());
                    writer.write(System.lineSeparator());
                }
            }
            writer.write(System.lineSeparator());

            // === Enumeration (DBs / tables / columns) ===
            writer.write("[ENUMERATION]" + System.lineSeparator());
            List<String> dbs = result.getDatabaseNames();
            Map<String, List<String>> tablesByDb = result.getTablesByDatabase();
            Map<String, List<String>> colsByTable = result.getColumnsByTable();

            if (dbs == null || dbs.isEmpty()) {
                writer.write("  (enumeration not available or no databases discovered)" + System.lineSeparator());
            } else {
                for (String db : dbs) {
                    writer.write("  Database: " + db + System.lineSeparator());
                    List<String> tables = tablesByDb.getOrDefault(db, List.of());
                    if (tables.isEmpty()) {
                        writer.write("    (no tables discovered)" + System.lineSeparator());
                        continue;
                    }

                    for (String table : tables) {
                        writer.write("    Table: " + table + System.lineSeparator());
                        String key = db + "." + table;
                        List<String> cols = colsByTable.getOrDefault(key, List.of());
                        if (cols.isEmpty()) {
                            writer.write("      (no columns discovered)" + System.lineSeparator());
                        } else {
                            writer.write("      Columns:" + System.lineSeparator());
                            for (String c : cols) {
                                writer.write("        - " + c + System.lineSeparator());
                            }
                        }
                    }
                }
            }

            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to save scan result: " + e.getMessage());
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }


    private static void indentMultiline(FileWriter writer, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            writer.write("      " + "(no details)" + System.lineSeparator());
            return;
        }
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            writer.write("      " + line + System.lineSeparator());
        }
    }
}
