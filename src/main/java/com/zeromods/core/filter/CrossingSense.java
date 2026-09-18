package com.zeromods.core.filter;
/** Which way a boundary that encloses an area was crossed. Selections are held as a bit mask so a
 * rule keeps both senses in one value, the way world directions do.
 */
public enum CrossingSense {
    INWARD, OUTWARD;
    public static final int ALL = (1 << INWARD.ordinal()) | (1 << OUTWARD.ordinal());
    public int bit() { return 1 << ordinal(); }
    public boolean selected(int mask) { return (mask & bit()) != 0; }
    public int toggle(int mask) { return mask ^ bit(); }
    public static int sanitize(int mask) { return mask & ALL; }
}
