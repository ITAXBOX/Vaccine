package com.vaccine.app;

import com.vaccine.cli.CommandLineOptions;
import com.vaccine.config.ScanConfig;
import com.vaccine.facade.VaccineFacade;
import com.vaccine.model.ScanResult;

import static com.vaccine.util.Util.printUsage;

public class VaccineApp {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }

        try {
            CommandLineOptions options = CommandLineOptions.parse(args);

            ScanConfig config = new ScanConfig(options.getUrl(), options.getMethod()
                    , options.getOutputFile(), options.getBody(), options.getHeaders());
            VaccineFacade facade = new VaccineFacade();
            ScanResult result = facade.scan(config);

            // Basic summary (you can later improve color, formatting, etc.)
            System.out.println("=== Vaccine Scan Summary ===");
            System.out.println("Target      : " + result.getTarget().getBaseUrl());
            System.out.println("HTTP Method : " + result.getTarget().getMethod());
            System.out.println("DBMS        : " + result.getDbmsType());
            System.out.println("Vulnerabilities found: " + result.getVulnerabilities().size());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        }
    }
}
