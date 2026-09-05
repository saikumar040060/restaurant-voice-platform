#!/usr/bin/env python3
"""Offline foundation integrity check; not a product/security test suite."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
required = [
    "PRODUCT_SPEC.md", "AGENTS.md", "README.md", "SECURITY.md",
    "docs/PROJECT_STATUS.md", "docs/PRODUCT_ADDENDUM.md",
    "docs/architecture/0001-foundation.md", "docs/architecture/DATA_FLOW.md",
    "docs/security/THREAT_MODEL.md", "docs/security/ACCESS_CONTROL_MATRIX.md",
    "docs/runbooks/INCIDENT_RESPONSE.md", "docs/runbooks/BACKUP_RESTORE.md",
    "docs/TEST_STRATEGY.md", "docs/INTEGRATIONS.md", ".github/workflows/ci.yml",
    "apps/backend/pom.xml", "infra/local/compose.yaml", "packages/contracts/openapi.yaml",
]
errors = [f"Missing: {name}" for name in required if not (root / name).is_file()]
count = 0
for path in root.rglob("*"):
    if not path.is_file() or any(part in {".git", "target", "node_modules", "__pycache__"} for part in path.relative_to(root).parts):
        continue
    if path.name == ".env":
        continue
    content = path.read_text(encoding="utf-8")
    count += 1
    if not content.endswith("\n"):
        errors.append(f"Missing final newline: {path.relative_to(root)}")
    if path.name != "PRODUCT_SPEC.md":
        for number, line in enumerate(content.splitlines(), 1):
            if line.rstrip() != line:
                errors.append(f"Trailing whitespace: {path.relative_to(root)}:{number}")
    if re.search(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----", content):
        errors.append(f"Private key marker: {path.relative_to(root)}")
try:
    tree = ET.parse(root / "apps/backend/pom.xml")
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    if tree.findtext("m:properties/m:java.version", namespaces=ns) != "21":
        errors.append("Backend must target Java 21")
except (ET.ParseError, OSError) as error:
    errors.append(str(error))
if errors:
    print("FOUNDATION CHECK FAILED\n" + "\n".join(errors))
    sys.exit(1)
print(f"PASS: {len(required)} required files, {count} text files checked; POM parses and targets Java 21.")
print("Scope: foundation integrity/formatting and private-key marker check only; not a full secret scan.")
