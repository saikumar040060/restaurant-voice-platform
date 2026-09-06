#!/usr/bin/env python3
"""Run integration tests in a new disposable PostgreSQL container, then remove it."""
import os
from pathlib import Path
import subprocess
import time
import tempfile
import secrets
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
    # The project targets Java 21; prefer an installed Java 25 runtime when the
    # caller's shell still points at Java 17 (which cannot run class version 65).
    preferred_java = Path.home() / "Library/Java/JavaVirtualMachines/openjdk-25.0.1/Contents/Home"
    if preferred_java.is_dir():
        env["JAVA_HOME"] = str(preferred_java)
    env.update(VOICE_DB_URL=f"jdbc:postgresql://127.0.0.1:{port}/voice_test",
               VOICE_DB_USER="voice_test", VOICE_DB_PASSWORD="isolated-test-only")
    subprocess.run(["mvn", "-B", "-f", "apps/backend/pom.xml", "verify"], cwd=root, env=env, check=True)
    subprocess.run(["docker", "exec", name, "psql", "-U", "voice_test", "-d", "voice_test",
                    "-v", "ON_ERROR_STOP=1", "-c", "TRUNCATE tenants CASCADE;"],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    with tempfile.TemporaryDirectory(prefix="voice-bootstrap-") as secret_dir:
        secret_file = Path(secret_dir) / "password"
        secret_file.write_text(secrets.token_urlsafe(24))
        secret_file.chmod(0o600)
        bootstrap_env = env.copy()
        bootstrap_env.update(VOICE_BOOTSTRAP_PASSWORD_FILE=str(secret_file),
                             VOICE_BOOTSTRAP_TENANT="Fictional Bootstrap Test",
                             VOICE_BOOTSTRAP_USERNAME="bootstrap-test-owner")
        java = str(Path(env["JAVA_HOME"]) / "bin/java") if "JAVA_HOME" in env else "java"
        command = [java, "-jar", "apps/backend/target/restaurant-voice-platform-0.0.1-SNAPSHOT.jar",
                   "--spring.profiles.active=bootstrap", "--spring.main.web-application-type=none"]
        first = subprocess.run(command, cwd=root, env=bootstrap_env, capture_output=True, text=True, timeout=45)
        if first.returncode != 0:
            raise RuntimeError("Packaged bootstrap failed: " + first.stdout + first.stderr)
        second = subprocess.run(command, cwd=root, env=bootstrap_env, capture_output=True, text=True, timeout=45)
        if second.returncode == 0 or "Bootstrap is allowed only on an empty installation" not in second.stdout + second.stderr:
            raise RuntimeError("Packaged bootstrap did not reject repeated initialization")
        secret_file.chmod(0o644)
        insecure = subprocess.run(command, cwd=root, env=bootstrap_env, capture_output=True, text=True, timeout=45)
        if insecure.returncode == 0 or "Bootstrap password file must be readable only by its owner" not in insecure.stdout + insecure.stderr:
            raise RuntimeError("Packaged bootstrap did not reject broadly readable password file")
        count = subprocess.check_output(["docker", "exec", name, "psql", "-U", "voice_test", "-d", "voice_test",
                                         "-At", "-c", "SELECT count(*) FROM tenants"], text=True).strip()
        if count != "1":
            raise RuntimeError("Bootstrap changed tenant count unexpectedly")
        print("PASS: packaged bootstrap initializes once, rejects repeat and insecure secret file, and exits without a web server.")
finally:
    if container_created:
        subprocess.run(["docker", "stop", name], check=True, stdout=subprocess.DEVNULL)
