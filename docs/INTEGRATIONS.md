# Integration register

No product accounts are connected, no plugins installed for this project, and no paid resources provisioned.

| Integration | State | Minimum next access | Removal |
|---|---|---|---|
| GitHub | Owner confirmed saikumar040060; approved private restaurant-voice-platform created; connector access unresolved | Access to restaurant-voice-platform only for code/PR/CI; approval question pending | Remove remote and revoke scoped app grant |
| Square | Planned Milestone 8 | Approved sandbox application | Revoke OAuth and remove secrets |
| Telephony/SMS | Planned Milestone 7 | Approved test account and number | Revoke account tokens and disable webhooks |
| Voice/model | Provider-neutral design only | Approved runtime developer credentials later | Revoke token and remove adapter config |
| Hosting | Unselected | None now | Provider-specific later |

Approval records: none required or granted for external accounts so far. Existing connector availability is not evidence of repository ownership or approval to connect a product account.

Local tooling permission on 2026-09-05: owner granted network/Maven-cache write access and Docker socket access for build/test execution. A public PostgreSQL 17.6 image was used with isolated fictional fixtures; no product account was connected. Test containers are stopped/removed after validation.

Repository creation approval: user selected “Create the private repository” and said “go”. Created via the signed-in GitHub UI with Private visibility on 2026-09-05. URL: https://github.com/saikumar040060/restaurant-voice-platform. No collaborators added, no permissions expanded, and no paid settings changed. Git SSH authentication failed; HTTPS has no configured credentials; connector metadata lookup returned 404 and installed-account enumeration was empty. Do not claim source has been uploaded. A pending owner question covers granting the connector access to only this repository; configuration must be inspected before any change.
