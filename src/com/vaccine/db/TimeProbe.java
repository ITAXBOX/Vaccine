package com.vaccine.db;

// A simple data class representing a time-based probe for DBMS fingerprinting.
// time-based probe means sending queries that cause a delay in response time
// to infer the type of database based on how it handles such queries.
// Each probe is associated with a specific DbmsType and a payload suffix
// that is used to construct the time-delay inducing query.
// For example, different databases have different syntax for causing delays,
// such as "SLEEP(5)" for MySQL or "pg_sleep(5)" for PostgreSQL.
public class TimeProbe {
    final DbmsType type;
    final String payloadSuffix;

    TimeProbe(DbmsType type, String payloadSuffix) {
        this.type = type;
        this.payloadSuffix = payloadSuffix;
    }
}
