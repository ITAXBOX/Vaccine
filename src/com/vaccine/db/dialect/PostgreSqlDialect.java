package com.vaccine.db.dialect;

import com.vaccine.db.DbmsType;

public class PostgreSqlDialect implements DbmsDialect {

    @Override
    public DbmsType getDbmsType() {
        return DbmsType.POSTGRESQL;
    }

    @Override
    public String listDatabasesQuery() {
        // In PostgreSQL, databases live in pg_database
        return "SELECT datname FROM pg_database WHERE datistemplate = false";
    }

    @Override
    public String listTablesQuery(String databaseName) {
        // Usually we care about public schema; you can refine later
        return "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = 'public'";
    }

    @Override
    public String listColumnsQuery(String databaseName, String tableName) {
        return "SELECT column_name FROM information_schema.columns " +
                "WHERE table_name = '" + escape(tableName) + "' " +
                "AND table_schema = 'public'";
    }

    private String escape(String s) {
        return s.replace("'", "''");
    }
}
