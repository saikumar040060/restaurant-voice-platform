-- Backfill is deterministic and idempotent for databases created from V1/V2.
INSERT INTO businesses (id, business_type)
SELECT id, 'restaurant' FROM tenants
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_modules (module_id, version, display_name)
VALUES ('reference', '1.0.0', 'Reference business')
ON CONFLICT (module_id, version) DO NOTHING;

INSERT INTO business_module_bindings (business_id, module_id, module_version, approval_state)
SELECT id, 'reference', '1.0.0', 'APPROVED' FROM businesses
ON CONFLICT (business_id, module_id) DO NOTHING;
