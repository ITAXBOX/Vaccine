package com.vaccine.core;

import com.vaccine.config.ScanConfig;
import com.vaccine.db.dialect.DbmsDialect;
import com.vaccine.db.DbmsFingerprintingService;
import com.vaccine.db.DbmsType;
import com.vaccine.http.HttpClient;
import com.vaccine.http.HttpResponseSnapshot;
import com.vaccine.injection.InjectionStrategy;
import com.vaccine.model.Target;

import java.util.List;

public class ScanContext {
    private final ScanConfig config;
    private final HttpClient httpClient;
    private HttpResponseSnapshot baselineResponse;
    private final DbmsFingerprintingService fingerprintingService;
    private final List<InjectionStrategy> strategies;
    private DbmsDialect dialect;
    private Target target;
    private DbmsType dbmsType = DbmsType.UNKNOWN;

    public ScanContext(ScanConfig config,
                       HttpClient httpClient,
                       DbmsFingerprintingService fingerprintingService,
                       List<InjectionStrategy> strategies) {
        this.config = config;
        this.httpClient = httpClient;
        this.fingerprintingService = fingerprintingService;
        this.strategies = strategies;
    }

    public ScanConfig getConfig() {
        return config;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public DbmsFingerprintingService getFingerprintingService() {
        return fingerprintingService;
    }

    public List<InjectionStrategy> getStrategies() {
        return strategies;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public DbmsType getDbmsType() {
        return dbmsType;
    }

    public void setDbmsType(DbmsType dbmsType) {
        this.dbmsType = dbmsType;
    }

    public HttpResponseSnapshot getBaselineResponse() {
        return baselineResponse;
    }

    public void setBaselineResponse(HttpResponseSnapshot baselineResponse) {
        this.baselineResponse = baselineResponse;
    }

    public DbmsDialect getDialect() {
        return dialect;
    }

    public void setDialect(DbmsDialect dialect) {
        this.dialect = dialect;
    }
}
