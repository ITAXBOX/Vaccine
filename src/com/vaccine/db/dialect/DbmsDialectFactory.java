package com.vaccine.db.dialect;

import com.vaccine.db.DbmsType;

public class DbmsDialectFactory {

    public static DbmsDialect create(DbmsType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case MYSQL -> new MySqlDialect();
            case POSTGRESQL -> new PostgreSqlDialect();
            default -> null;
        };
    }
}
