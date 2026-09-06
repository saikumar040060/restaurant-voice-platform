package com.harborvoice.platform.knowledge;

public final class KnowledgePublication {
    private KnowledgePublication() { }

    public static KnowledgeRecord approve(KnowledgeRecord candidate, int latestVersion) {
        if (candidate == null || candidate.approvalState() == KnowledgeRecord.ApprovalState.REVOKED
                || candidate.version() <= latestVersion) {
            throw new IllegalArgumentException("knowledge version cannot be approved");
        }
        return new KnowledgeRecord(candidate.id(), candidate.businessId(), candidate.locationId(), candidate.key(),
                candidate.content(), candidate.provenance(), candidate.version(), KnowledgeRecord.ApprovalState.APPROVED,
                candidate.freshUntil());
    }
}
