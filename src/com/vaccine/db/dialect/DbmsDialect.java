package com.vaccine.db.dialect;

import com.vaccine.db.DbmsType;

// Interface defining SQL dialects for different DBMS types.
// dialect is a set of SQL query templates tailored for a specific DBMS.
public interface DbmsDialect {
    // The type of DBMS this dialect is for.
    DbmsType getDbmsType();
    // Query to list database/schema names.
    String listDatabasesQuery();
    // Query to list table names for a given database/schema.
    String listTablesQuery(String databaseName);
    // Query to list column names for a given table in a given database/schema.
    String listColumnsQuery(String databaseName, String tableName);
}
