"""Remove disposable test instances while keeping reports and logs."""

import shutil


def scenario(root):
    for name in ("server", "client", "agent"):
        target = root / name
        if target.exists():
            shutil.rmtree(target)


def upgrade(root, sources):
    for source in sources:
        for name in ("server", "old-client", "new-client", "before-world"):
            target = root / source / name
            if target.exists():
                shutil.rmtree(target)
