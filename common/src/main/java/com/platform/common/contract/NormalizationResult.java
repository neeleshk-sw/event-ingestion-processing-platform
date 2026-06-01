package com.platform.common.contract;

import com.platform.common.model.EventEnvelope;

public class NormalizationResult {

    private EventEnvelope normalizedEvent;

    public NormalizationResult() {}

    public NormalizationResult(EventEnvelope normalizedEvent) {
        this.normalizedEvent = normalizedEvent;
    }

    public EventEnvelope getNormalizedEvent() {
        return normalizedEvent;
    }
}
