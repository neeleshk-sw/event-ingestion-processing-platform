package com.platform.routing.config;

import java.util.Map;

public class RoutingRule {

    private String name;
    private Map<String, String> when;
    private String destination;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getWhen() {
        return when;
    }

    public void setWhen(Map<String, String> when) {
        this.when = when;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
