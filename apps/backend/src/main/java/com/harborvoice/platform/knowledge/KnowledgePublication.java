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

    public static KnowledgeRecord revoke(KnowledgeRecord published) {
        if (published == null || published.approvalState() != KnowledgeRecord.ApprovalState.APPROVED) {
            throw new IllegalArgumentException("only approved knowledge can be revoked");
        }
        return new KnowledgeRecord(published.id(), published.businessId(), published.locationId(), published.key(),
                published.content(), published.provenance(), published.version(), KnowledgeRecord.ApprovalState.REVOKED,
                published.freshUntil());
    }

    public static KnowledgeRecord rollback(KnowledgeRecord replacement, KnowledgeRecord current) {
        if (replacement == null || current == null || replacement.businessId() == null
                || !replacement.businessId().equals(current.businessId())
                || !replacement.key().equals(current.key())
                || replacement.version() >= current.version()
                || replacement.approvalState() == KnowledgeRecord.ApprovalState.REVOKED) {
            throw new IllegalArgumentException("rollback must restore an older non-revoked tenant record");
        }
        return new KnowledgeRecord(replacement.id(), replacement.businessId(), replacement.locationId(), replacement.key(),
                replacement.content(), replacement.provenance(), replacement.version(), KnowledgeRecord.ApprovalState.APPROVED,
                replacement.freshUntil());
    }
}
