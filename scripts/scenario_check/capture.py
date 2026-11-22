"""Capture the Minecraft client window on macOS without touching the mod.

The window is found by the PID of the client JVM through CoreGraphics, then
captured with `screencapture -l <window id>`. That works while other windows
cover the game window, but the terminal (or IDE) that runs Gradle needs the
Screen Recording permission. Without it, macOS returns an image of the
desktop background instead of the window, which `looks_blank` detects.
"""

import ctypes
import ctypes.util
import subprocess
import sys
from pathlib import Path


class CaptureError(Exception):
    pass


_kCFNumberSInt64Type = 4
_kCFStringEncodingUTF8 = 0x08000100
_kCGWindowListOptionAll = 0
_kCGNullWindowID = 0


def _frameworks():
    cg = ctypes.cdll.LoadLibrary(ctypes.util.find_library("CoreGraphics"))
    cf = ctypes.cdll.LoadLibrary(ctypes.util.find_library("CoreFoundation"))
    cg.CGWindowListCopyWindowInfo.restype = ctypes.c_void_p
    cg.CGWindowListCopyWindowInfo.argtypes = [ctypes.c_uint32, ctypes.c_uint32]
    cf.CFArrayGetCount.restype = ctypes.c_long
    cf.CFArrayGetCount.argtypes = [ctypes.c_void_p]
    cf.CFArrayGetValueAtIndex.restype = ctypes.c_void_p
    cf.CFArrayGetValueAtIndex.argtypes = [ctypes.c_void_p, ctypes.c_long]
    cf.CFDictionaryGetValue.restype = ctypes.c_void_p
    cf.CFDictionaryGetValue.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
    cf.CFStringCreateWithCString.restype = ctypes.c_void_p
    cf.CFStringCreateWithCString.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_uint32]
    cf.CFNumberGetValue.restype = ctypes.c_bool
    cf.CFNumberGetValue.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_void_p]
    cf.CFRelease.argtypes = [ctypes.c_void_p]
    return cg, cf


def _number(cf, dictionary, key):
    ref = cf.CFDictionaryGetValue(dictionary, key)
    if not ref:
        return None
    value = ctypes.c_int64()
    cf.CFNumberGetValue(ref, _kCFNumberSInt64Type, ctypes.byref(value))
    return value.value


def _bounds_area(cf, dictionary, keys):
    ref = cf.CFDictionaryGetValue(dictionary, keys["bounds"])
    if not ref:
        return 0
    width = _number(cf, ref, keys["width"]) or 0
    height = _number(cf, ref, keys["height"]) or 0
    return width * height


def list_windows(pid: int) -> list[tuple[int, int]]:
    """(window id, area) of the on-screen-layer windows owned by pid, largest first."""
    if sys.platform != "darwin":
        raise CaptureError("window capture is implemented for macOS only")
    cg, cf = _frameworks()
    keys = {name: cf.CFStringCreateWithCString(None, name.encode(), _kCFStringEncodingUTF8)
            for name in ("kCGWindowOwnerPID", "kCGWindowNumber", "kCGWindowLayer", "kCGWindowBounds",
                         "Width", "Height")}
    keys["bounds"] = keys["kCGWindowBounds"]
    keys["width"] = keys["Width"]
    keys["height"] = keys["Height"]
    windows = []
    array = cg.CGWindowListCopyWindowInfo(_kCGWindowListOptionAll, _kCGNullWindowID)
    try:
        for i in range(cf.CFArrayGetCount(array) if array else 0):
            info = cf.CFArrayGetValueAtIndex(array, i)
            if _number(cf, info, keys["kCGWindowOwnerPID"]) != pid:
                continue
            if _number(cf, info, keys["kCGWindowLayer"]) != 0:
                continue
            windows.append((_number(cf, info, keys["kCGWindowNumber"]), _bounds_area(cf, info, keys)))
    finally:
        if array:
            cf.CFRelease(array)
        for name in ("kCGWindowOwnerPID", "kCGWindowNumber", "kCGWindowLayer", "kCGWindowBounds",
                     "Width", "Height"):
            cf.CFRelease(keys[name])
    return sorted(windows, key=lambda w: w[1], reverse=True)


def find_window(pid: int, min_area: int = 200 * 200) -> int | None:
    """The game window of the client JVM, ignoring small helper windows."""
    for window_id, area in list_windows(pid):
        if area >= min_area:
            return window_id
    return None


def capture_window(window_id: int, out_file: Path) -> Path:
    out_file = Path(out_file)
    out_file.parent.mkdir(parents=True, exist_ok=True)
    # -x: no shutter sound, -o: no window shadow.
    result = subprocess.run(["screencapture", "-x", "-o", "-l", str(window_id), str(out_file)],
                            capture_output=True, text=True)
    if result.returncode != 0 or not out_file.exists():
        raise CaptureError(f"screencapture failed ({result.returncode}): {result.stderr.strip()}")
    return out_file


def main(argv=None) -> int:
    """Manual probe: python -m scripts.scenario_check.capture <pid> <out.png>"""
    args = sys.argv[1:] if argv is None else argv
    pid, out = int(args[0]), Path(args[1])
    window_id = find_window(pid)
    if window_id is None:
        print(f"no window for pid {pid}: {list_windows(pid)}")
        return 1
    print(capture_window(window_id, out))
    return 0


if __name__ == "__main__":
    sys.exit(main())
