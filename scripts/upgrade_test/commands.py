"""Rewrite commands written for Minecraft 1.21.1 so they run on newer versions.

Layers and scenarios are written once in 1.21.1 syntax. Minecraft 1.21.11
renamed the game rules to snake_case resource IDs, and merged some of them,
so those commands are translated before they are sent.
"""

import re

from scripts.upgrade_test.layers import version_key

# 1.21.1 name -> (1.21.11+ name, value mapping or None to keep the value).
_GAMERULES_1_21_11 = {
    "announceAdvancements": ("show_advancement_messages", None),
    "doDaylightCycle": ("advance_time", None),
    "doWeatherCycle": ("advance_weather", None),
    # doFireTick false stopped fire spreading. Its successor is the radius
    # around players in which fire spreads, so 0 means nowhere.
    "doFireTick": ("fire_spread_radius_around_player", {"false": "0", "true": "128"}),
    "mobGriefing": ("mob_griefing", None),
    "randomTickSpeed": ("random_tick_speed", None),
    "sendCommandFeedback": ("send_command_feedback", None),
}

_GAMERULE = re.compile(r"(?P<head>(?:^|\brun )gamerule )(?P<name>\w+)(?P<rest>(?: (?P<value>\S+))?.*)$")


def adapt(command: str, mc_version: str | None) -> str:
    """Return command in the syntax of mc_version (unchanged for 1.21.1 or when unknown)."""
    if not mc_version or version_key(mc_version) < (1, 21, 11):
        return command
    match = _GAMERULE.search(command)
    if not match or match.group("name") not in _GAMERULES_1_21_11:
        return command
    name, values = _GAMERULES_1_21_11[match.group("name")]
    rest = match.group("rest")
    value = match.group("value")
    if values and value in values:
        rest = rest.replace(value, values[value], 1)
    return command[:match.start()] + match.group("head") + name + rest
