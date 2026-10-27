"""
OtakuList Local WiFi Sync Server Root Wrapper.
Mendelegasikan eksekusi langsung ke modul `sync/sync_server.py`.
"""
import os
import sys
import subprocess

if __name__ == "__main__":
    target_script = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sync", "sync_server.py")
    cmd = [sys.executable, target_script] + sys.argv[1:]
    try:
        sys.exit(subprocess.call(cmd))
    except KeyboardInterrupt:
        sys.exit(0)
