package com.platform.routing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "routing")
public class RoutingProperties {

    private List<RoutingRule> rules;

    public List<RoutingRule> getRules() {
        return rules;
    }

    public void setRules(List<RoutingRule> rules) {
        this.rules = rules;
    }
}
