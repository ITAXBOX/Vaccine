package com.vaccine.db.dialect;

import com.vaccine.db.DbmsType;

public class MySqlDialect implements DbmsDialect {

    @Override
    public DbmsType getDbmsType() {
        return DbmsType.MYSQL;
    }

    @Override
    public String listDatabasesQuery() {
        // One row per schema, column: schema_name
        return "SELECT schema_name FROM information_schema.schemata";
    }

    @Override
    public String listTablesQuery(String databaseName) {
        // One row per table, column: table_name
        return "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = '" + escape(databaseName) + "'";
    }

    @Override
    public String listColumnsQuery(String databaseName, String tableName) {
        // One row per column, column: column_name
        return "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = '" + escape(databaseName) + "' " +
                "AND table_name = '" + escape(tableName) + "'";
    }

    private String escape(String s) {
        // Basic single-quote escape to avoid breaking the pure SQL fragment
        return s.replace("'", "''");
    }
}
