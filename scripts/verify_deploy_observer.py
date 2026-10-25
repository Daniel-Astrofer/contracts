#!/usr/bin/env python3
"""Read-only interoperability test against an explicitly selected deploy controller.

Frozen fixture time is test-only. No network, deploy mutation, or live credentials.
"""
import argparse
import datetime as dt
import importlib.machinery
import importlib.util
import json
from pathlib import Path
import sys
import tempfile

sys.dont_write_bytecode = True
parser = argparse.ArgumentParser()
parser.add_argument("--controller", required=True, type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parent.parent
fixture = json.loads((root / "test-vectors/release-observer-v1.json").read_text())
release = json.loads((root / "test-vectors/release-lock-v3.json").read_text())
loader = importlib.machinery.SourceFileLoader("release_controller_test", str(args.controller))
spec = importlib.util.spec_from_loader(loader.name, loader)
controller = importlib.util.module_from_spec(spec)
sys.modules[loader.name] = controller
sys.path.insert(0, str(args.controller.parent))
loader.exec_module(controller)

class FixtureClock(dt.datetime):
    @classmethod
    def now(cls, tz=None):
        return cls(2026, 10, 1, 21, 0, 10, tzinfo=dt.timezone.utc)

controller.dt.datetime = FixtureClock
assert controller.canonical_digest(release) == fixture["releaseLockCanonicalDigest"]
summary = {
    "releaseId": release["releaseId"],
    "networkId": release["network"]["id"],
    "sequence": release["sequence"],
    "bft": release["authorization"]["bft"],
    "policy": release["policy"],
}
with tempfile.TemporaryDirectory(prefix="deploy-observer-test-", dir=root / "target") as directory:
    report_path = Path(directory) / "report.json"
    roster_path = Path(directory) / "roster.json"
    report_path.write_text(json.dumps(fixture["report"]))
    roster_path.write_text(json.dumps(fixture["roster"]))
    result = controller.verify_bank_observer_report(release, summary, str(report_path), str(roster_path))
    assert result["compatibleObservers"] == 3
    assert result["signaturesVerified"] == 3
    # Three individually signed reads are not aggregate signatures.
    wrong = dict(fixture["report"])
    wrong["signatures"] = fixture["reads"]["observations"][0]["signatures"]
    report_path.write_text(json.dumps(wrong))
    try:
        controller.verify_bank_observer_report(release, summary, str(report_path), str(roster_path))
    except controller.ReleaseValidationError:
        pass
    else:
        raise AssertionError("read wrapper signature was accepted as report quorum")
print(json.dumps(result, sort_keys=True))
