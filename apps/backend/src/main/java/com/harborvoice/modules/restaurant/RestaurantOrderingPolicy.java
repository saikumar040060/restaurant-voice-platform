package com.harborvoice.modules.restaurant;

/** Tenant-approved restaurant limits and money rules; all money is integer minor units. */
public record RestaurantOrderingPolicy(String currency, int taxBasisPoints, int maximumLineQuantity,
                                      int largeOrderTransferThresholdMinor, int specialInstructionMaximumCharacters) {
    public RestaurantOrderingPolicy {
        if (currency == null || !currency.matches("[A-Z]{3}") || taxBasisPoints < 0 || taxBasisPoints > 10_000
                || maximumLineQuantity < 1 || maximumLineQuantity > 99 || largeOrderTransferThresholdMinor < 0
                || specialInstructionMaximumCharacters < 0 || specialInstructionMaximumCharacters > 2_000) {
            throw new IllegalArgumentException("invalid restaurant ordering policy");
        }
    }

    public boolean requiresLargeOrderTransfer(int subtotalMinor) {
        return subtotalMinor >= largeOrderTransferThresholdMinor;
    }
}
