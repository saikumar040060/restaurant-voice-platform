# Backup and restore skeleton

Local Docker volumes are not backups. Before staging, define approved RPO/RTO, encrypted managed PostgreSQL backups, retention, access policy, and isolated restore environment. Restore a backup, verify tenant boundaries and consistency, prevent restored outbox records replaying live side effects, reconcile provider submissions, and save a timed recovery report. Never restore into production without approval. No backup or restoration is claimed tested yet.
