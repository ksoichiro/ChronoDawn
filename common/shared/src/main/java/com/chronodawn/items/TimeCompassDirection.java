package com.chronodawn.items;

/**
 * Eight-way direction from the player to the Time Compass target, used in its chat messages.
 * Minecraft's -Z is north and +X is east.
 */
public final class TimeCompassDirection {
    private static final String[] DIRECTIONS = {
        "north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"
    };

    private TimeCompassDirection() {
    }

    /**
     * @param dx target X minus player X
     * @param dz target Z minus player Z
     * @return direction key such as "north" or "southwest"
     */
    public static String of(int dx, int dz) {
        // atan2(dz, dx) is 0 for east and 90 for south (+Z). Adding 90 makes north 0,
        // east 90, south 180 and west 270, a clockwise bearing.
        double bearing = (Math.toDegrees(Math.atan2(dz, dx)) + 90.0 + 360.0) % 360.0;
        int sector = (int) Math.floor((bearing + 22.5) / 45.0) % DIRECTIONS.length;
        return DIRECTIONS[sector];
    }
}
