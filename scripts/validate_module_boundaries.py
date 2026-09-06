#!/usr/bin/env python3
"""Reject prohibited compile-time dependencies between platform and business modules."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1] / "apps/backend/src/main/java/com/harborvoice"
CORE = ROOT / "platform"
RESTAURANT = ROOT / "modules/restaurant"
violations = []
for path in CORE.rglob("*.java"):
    text = path.read_text()
    if "com.harborvoice.modules.restaurant" in text:
        violations.append(f"core imports restaurant: {path}")
for path in RESTAURANT.rglob("*.java"):
    for line in path.read_text().splitlines():
        if line.startswith("import com.harborvoice.modules.restaurant."):
            continue
        if line.startswith("import com.harborvoice.modules."):
            violations.append(f"restaurant imports sibling module: {path}: {line}")
if violations:
    print("FAIL: module boundary violations")
    print("\n".join(violations))
    sys.exit(1)
print("PASS: core and restaurant module dependency boundaries are clean")
