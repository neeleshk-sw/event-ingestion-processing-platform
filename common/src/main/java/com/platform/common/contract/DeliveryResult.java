package com.platform.common.contract;

public class DeliveryResult {

    private boolean delivered;
    private String destination;

    public DeliveryResult() {}

    public DeliveryResult(boolean delivered, String destination) {
        this.delivered = delivered;
        this.destination = destination;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public String getDestination() {
        return destination;
    }
}
