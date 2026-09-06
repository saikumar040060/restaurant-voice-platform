# Integration register

Current verification, 2026-09-06: connector can read restaurant-voice-platform and the published baseline tree matches local 01129e9. No repository-access approval remains pending. Prior empty-repository/404 statements below are historical. No product provider, credential, paid service or permissions change was introduced in this architecture review. See PROJECT_STATUS.md for history reconciliation and unverified hosted-CI status.

No product accounts are connected, no plugins installed for this project, and no paid resources provisioned.

| Integration | State | Minimum next access | Removal |
|---|---|---|---|
| GitHub | Access works; baseline snapshot published; local and remote histories differ | Existing project access; no new permissions requested | Remove remote and revoke scoped app grant |
| Square | Planned Milestone 8 | Approved sandbox application | Revoke OAuth and remove secrets |
| Telephony/SMS | Mocks M3/M5; real evaluation M5b after approval | None now; later approved test account and number | Revoke account tokens and disable webhooks |
| Voice/model | Provider-neutral design only | Approved runtime developer credentials later | Revoke token and remove adapter config |
| Hosting | Unselected | None now | Provider-specific later |

Approval records: owner authorized the private GitHub repository and subsequently connected it. No runtime product account is authorized by this architecture review. Historical setup notes below are superseded by the current verification note.

Local tooling permission on 2026-09-05: owner granted network/Maven-cache write access and Docker socket access for build/test execution. A public PostgreSQL 17.6 image was used with isolated fictional fixtures; no product account was connected. Test containers are stopped/removed after validation.

Repository creation approval: user selected “Create the private repository” and said “go”. Created via the signed-in GitHub UI with Private visibility on 2026-09-05. URL: https://github.com/saikumar040060/restaurant-voice-platform. No collaborators added, no permissions expanded, and no paid settings changed. Git SSH authentication failed; HTTPS has no configured credentials; connector metadata lookup returned 404 and installed-account enumeration was empty. Do not claim source has been uploaded. A pending owner question covers granting the connector access to only this repository; configuration must be inspected before any change.
