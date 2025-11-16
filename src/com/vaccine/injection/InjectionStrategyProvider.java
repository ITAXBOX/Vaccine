package com.vaccine.injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Provides all available injection strategies.
public class InjectionStrategyProvider {

    private static final List<InjectionStrategy> STRATEGIES = Arrays.asList(
            new ErrorBasedStrategy(),
            new UnionBasedStrategy(),
            new BooleanBasedStrategy(),
            new TimeBasedStrategy()
    );

    private InjectionStrategyProvider() {
    }

    public static List<InjectionStrategy> getStrategies() {
        return new ArrayList<>(STRATEGIES);
    }
}

