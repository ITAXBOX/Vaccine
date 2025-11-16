package com.vaccine.model;

public class Parameter {

    public enum Location {
        QUERY, BODY
    }

    private final String name;
    private final String value;
    private final Location location;

    public Parameter(String name, String value, Location location) {
        this.name = name;
        this.value = value;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Location getLocation() {
        return location;
    }
}
