package com.platform.common.model;

public class ProcessingResult {

    private EventStatus status;
    private String message;

    public ProcessingResult() {}

    public ProcessingResult(EventStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
