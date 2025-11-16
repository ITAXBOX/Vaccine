package com.vaccine.model;

import com.vaccine.db.DbmsType;

import java.util.*;

public class ScanResult {

    private final Target target;
    private final List<Vulnerability> vulnerabilities;
    private final DbmsType dbmsType;
    private final List<String> databaseNames = new ArrayList<>();
    private final Map<String, List<String>> tablesByDatabase = new HashMap<>();
    private final Map<String, List<String>> columnsByTable = new HashMap<>();

    public ScanResult(Target target, List<Vulnerability> vulnerabilities, DbmsType dbmsType) {
        this.target = target;
        this.vulnerabilities = vulnerabilities;
        this.dbmsType = dbmsType;
    }

    public Target getTarget() {
        return target;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public DbmsType getDbmsType() {
        return dbmsType;
    }

    public List<String> getDatabaseNames() {
        return Collections.unmodifiableList(databaseNames);
    }

    public Map<String, List<String>> getTablesByDatabase() {
        // Return an unmodifiable view to prevent external modification
        return Collections.unmodifiableMap(tablesByDatabase);
    }

    public Map<String, List<String>> getColumnsByTable() {
        return Collections.unmodifiableMap(columnsByTable);
    }

    public void addDatabaseName(String dbName) {
        if (!databaseNames.contains(dbName)) {
            databaseNames.add(dbName);
        }
    }

    public void addTableName(String dbName, String tableName) {
        tablesByDatabase
                .computeIfAbsent(dbName, _ -> new ArrayList<>())
                .add(tableName);
    }

    public void addColumnName(String dbName, String tableName, String columnName) {
        String key = dbName + "." + tableName;
        columnsByTable
                .computeIfAbsent(key, _ -> new ArrayList<>())
                .add(columnName);
    }
}
