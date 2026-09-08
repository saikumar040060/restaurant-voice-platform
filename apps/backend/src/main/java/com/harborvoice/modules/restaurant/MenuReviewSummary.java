package com.harborvoice.modules.restaurant;

/** Read-only counts for the current non-executable menu-review revision. */
public record MenuReviewSummary(int approved, int corrected, int rejected, int pending, String publicationState) {
    public MenuReviewSummary {
        if (approved < 0 || corrected < 0 || rejected < 0 || pending < 0 || !"UNPUBLISHED".equals(publicationState)) {
            throw new IllegalArgumentException("invalid unpublished review summary");
        }
    }
}
