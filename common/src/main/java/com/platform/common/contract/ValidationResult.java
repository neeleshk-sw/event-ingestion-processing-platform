package com.platform.common.contract;

public class ValidationResult {

    private boolean valid;
    private String reason;

    public ValidationResult() {}

    public ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }
}
