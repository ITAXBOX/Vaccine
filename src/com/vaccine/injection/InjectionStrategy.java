package com.vaccine.injection;

import com.vaccine.core.ScanContext;
import com.vaccine.model.Parameter;
import com.vaccine.model.Vulnerability;

import java.util.Optional;

public interface InjectionStrategy {

    String getName();

    // Returns an Optional containing a Vulnerability if the parameter is vulnerable, or an empty Optional otherwise
    Optional<Vulnerability> isVulnerable(ScanContext context, Parameter parameter);
}
