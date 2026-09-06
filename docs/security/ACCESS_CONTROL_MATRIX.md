# Proposed access control matrix

All access is constrained to the authenticated tenant and assigned locations. Deny capabilities not explicitly granted.

| Capability | Owner | Manager | Employee | Support | System |
|---|---|---|---|---|---|
| Active calls/orders | yes | assigned locations | assigned locations | no default | narrow scoped service |
| Menu availability | yes | assigned locations | availability only | no | approved sync |
| Knowledge approval | yes | assigned locations | no | no | no |
| Employee configuration | yes | assigned locations | no | no | no |
| Integration credentials | configure securely | no default | no | no | adapter secret access |
| Audit viewer | yes | authorized locations | no | explicit temporary grant | append only |
| Customer export/deletion | verified process | no default | no | no | approved job |

Support access requires explicit scoped authorization and audit. Detailed policy and positive/negative tests are Milestone 1 work. No role grants override prohibited voice actions.

## Current implementation (first Milestone 1 slice)

Only OWNER can create a restaurant. OWNER reads all restaurants/locations in their tenant; MANAGER and EMPLOYEE read only assigned locations and restaurants containing those locations. SUPPORT and SYSTEM cannot log in interactively. This table above remains the target for later features, not a claim that its dashboard/integration/export actions are implemented. Unknown or unimplemented capabilities have no API.
