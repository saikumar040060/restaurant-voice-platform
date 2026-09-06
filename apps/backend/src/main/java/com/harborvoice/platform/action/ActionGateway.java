package com.harborvoice.platform.action;

public interface ActionGateway {
    ActionResult execute(PolicyGateway.ActionPermit permit, ActionRequest request);

    record ActionResult(String status, String externalReference) {
        public ActionResult {
            if (status == null || status.isBlank()) throw new IllegalArgumentException("status required");
        }
    }
}
