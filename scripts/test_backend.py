#!/usr/bin/env python3
"""Run integration tests in a new disposable PostgreSQL container, then remove it."""
import os
from pathlib import Path
import subprocess
import time
import uuid

root = Path(__file__).resolve().parents[1]
name = "harbor-test-" + uuid.uuid4().hex[:12]
container_created = False
try:
    subprocess.run([
        "docker", "run", "-d", "--rm", "--name", name,
        "-e", "POSTGRES_DB=voice_test", "-e", "POSTGRES_USER=voice_test",
        "-e", "POSTGRES_PASSWORD=isolated-test-only", "-p", "127.0.0.1::5432",
        "postgres:17.6",
    ], check=True)
    container_created = True
    for attempt in range(60):
        ready = subprocess.run(["docker", "exec", name, "pg_isready", "-U", "voice_test", "-d", "voice_test"],
                               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if ready.returncode == 0:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError("PostgreSQL did not become ready")
    address = subprocess.check_output(["docker", "port", name, "5432/tcp"], text=True).strip()
    port = int(address.rsplit(":", 1)[1])
    env = os.environ.copy()
    env.update(VOICE_DB_URL=f"jdbc:postgresql://127.0.0.1:{port}/voice_test",
               VOICE_DB_USER="voice_test", VOICE_DB_PASSWORD="isolated-test-only")
    subprocess.run(["mvn", "-B", "-f", "apps/backend/pom.xml", "verify"], cwd=root, env=env, check=True)
finally:
    if container_created:
        subprocess.run(["docker", "stop", name], check=True, stdout=subprocess.DEVNULL)
