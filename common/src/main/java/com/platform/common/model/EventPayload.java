package com.platform.common.model;

import java.util.Map;

public class EventPayload {

    private Map<String, Object> data;

    public EventPayload() {}

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
