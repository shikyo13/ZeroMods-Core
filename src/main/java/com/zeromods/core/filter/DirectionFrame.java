package com.zeromods.core.filter;
/** How a direction rule is written: in world directions, or relative to an enclosed area.
 * Hosts that cannot determine an inside fall back to WORLD, so a rule is never silently wrong.
 */
public enum DirectionFrame {
    WORLD, RELATIVE;
    public static DirectionFrame byId(int id) {
        var values = values();
        return id >= 0 && id < values.length ? values[id] : WORLD;
    }
    public int id() { return ordinal(); }
}
