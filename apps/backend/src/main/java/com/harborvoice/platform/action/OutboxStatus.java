package com.harborvoice.platform.action;

public enum OutboxStatus {
    PENDING, DISPATCHED, UNKNOWN, RECONCILED, DEAD_LETTER;

    public boolean terminal() {
        return this == RECONCILED || this == DEAD_LETTER;
    }
}
