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
