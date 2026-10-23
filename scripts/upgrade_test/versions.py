"""Which old releases to upgrade from, and where their JARs come from."""

import hashlib
import http.client
import json
import subprocess
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

MODRINTH_PROJECT_ID = "yEsoyNev"  # the slug endpoint (/project/chronodawn) returns 404
MC_VERSION = "1.21.1"


class VersionSelectionError(Exception):
    pass


def _describe(git_dir: Path, *args: str) -> str | None:
    result = subprocess.run(
        ["git", "-C", str(git_dir), "describe", "--tags", "--match", "v[0-9]*", *args],
        capture_output=True, text=True,
    )
    return result.stdout.strip() if result.returncode == 0 else None


def resolve_previous_release(git_dir: Path) -> str:
    """Latest release tag reachable from HEAD, or the one before it when HEAD is that tag.

    Release verification runs before the commit that bumps mod_version, so
    comparing against mod_version would skip the release players are on.
    """
    rev = "HEAD^" if _describe(git_dir, "--exact-match", "HEAD") else "HEAD"
    tag = _describe(git_dir, "--abbrev=0", rev)
    if tag is None:
        raise VersionSelectionError(f"No v* release tag reachable from {rev} in {git_dir}")
    return tag[1:]


def resolve_sources(baseline: str, previous: str) -> list[str]:
    return [baseline] if baseline == previous else [baseline, previous]


def select_modrinth_file(versions_json: list[dict], source: str, loader: str) -> dict:
    for_loader = [v for v in versions_json if loader in v.get("loaders", [])]
    matches = [v for v in for_loader if v["version_number"] == source]
    if not matches:
        # Some uploads carry a suffix such as "0.9.0+1.21.1-fabric".
        matches = [v for v in for_loader if v["version_number"].startswith(source + "+")]
    if len(matches) != 1:
        found = ", ".join(sorted(v["version_number"] for v in for_loader)) or "none"
        reason = "No" if not matches else f"{len(matches)}"
        raise VersionSelectionError(
            f"{reason} Modrinth versions match {source} for {loader} on {MC_VERSION}; available: {found}"
        )
    files = matches[0]["files"]
    primary = [f for f in files if f.get("primary")] or files
    return primary[0]


def _fetch_versions(loader: str) -> list[dict]:
    query = urllib.parse.urlencode({
        "game_versions": json.dumps([MC_VERSION]),
        "loaders": json.dumps([loader]),
    })
    url = f"https://api.modrinth.com/v2/project/{MODRINTH_PROJECT_ID}/version?{query}"
    request = urllib.request.Request(url, headers={"User-Agent": "ksoichiro/ChronoDawn upgrade-test"})
    # Chunked responses through a filtering proxy are occasionally cut short.
    for attempt in range(3):
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                return json.load(response)
        except (http.client.IncompleteRead, urllib.error.URLError):
            if attempt == 2:
                raise


def _sha512(path: Path) -> str:
    digest = hashlib.sha512()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def fetch_old_jar(source: str, loader: str, cache_dir: Path) -> Path:
    target_dir = cache_dir / "jars" / loader / source
    cached = list(target_dir.glob("*.jar")) if target_dir.exists() else []
    if len(cached) == 1:
        return cached[0]
    entry = select_modrinth_file(_fetch_versions(loader), source, loader)
    target_dir.mkdir(parents=True, exist_ok=True)
    target = target_dir / entry["filename"]
    partial = target.with_suffix(".part")
    request = urllib.request.Request(entry["url"], headers={"User-Agent": "ksoichiro/ChronoDawn upgrade-test"})
    with urllib.request.urlopen(request, timeout=300) as response, partial.open("wb") as out:
        out.write(response.read())
    if _sha512(partial) != entry["hashes"]["sha512"]:
        partial.unlink()
        raise VersionSelectionError(f"sha512 mismatch for {entry['url']}")
    partial.rename(target)
    return target
