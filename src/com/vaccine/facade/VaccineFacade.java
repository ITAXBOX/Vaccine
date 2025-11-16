package com.vaccine.facade;

import com.vaccine.config.ScanConfig;
import com.vaccine.core.ScanContext;
import com.vaccine.core.ScanEngine;
import com.vaccine.db.DbmsFingerprintingService;
import com.vaccine.http.HttpClient;
import com.vaccine.http.SimpleHttpClient;
import com.vaccine.injection.InjectionStrategyProvider;
import com.vaccine.model.ScanResult;
import com.vaccine.util.StorageManager;

public class VaccineFacade {

    public ScanResult scan(ScanConfig config) {
        // Assemble dependencies
        HttpClient httpClient = new SimpleHttpClient();
        DbmsFingerprintingService fingerprintingService= new DbmsFingerprintingService(httpClient);

        // Create context and execute scan
        ScanContext context = new ScanContext(
                config,
                httpClient,
                fingerprintingService,
                InjectionStrategyProvider.getStrategies()
        );
        ScanEngine engine = new ScanEngine(context);
        ScanResult result = engine.scan();

        StorageManager.save(config.getOutputFile(), result);

        return result;
    }
}
