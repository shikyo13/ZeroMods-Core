package com.zeromods.core.energy;
/** Loader-neutral long-valued storage. Adapters translate FE/Fabric energy calls and mark dirty.
 * Server-thread only; simulation must never change storage or invoke the changed callback.
 */
public final class EnergyBuffer {
    private final long capacity, maxReceive, maxExtract;
    private final Runnable changed;
    private long stored;
    public EnergyBuffer(long capacity, long maxReceive, long maxExtract, Runnable changed) {
        if (capacity < 0 || maxReceive < 0 || maxExtract < 0) throw new IllegalArgumentException("Negative energy limit");
        this.capacity = capacity; this.maxReceive = maxReceive; this.maxExtract = maxExtract;
        this.changed = java.util.Objects.requireNonNull(changed);
    }
    public long stored() { return stored; }
    public long capacity() { return capacity; }
    public long receive(long amount, boolean simulate) {
        long accepted = Math.min(Math.max(0, amount), Math.min(maxReceive, capacity - stored));
        if (!simulate && accepted != 0) { stored += accepted; changed.run(); } return accepted;
    }
    public long extract(long amount, boolean simulate) {
        long extracted = Math.min(Math.max(0, amount), Math.min(maxExtract, stored));
        if (!simulate && extracted != 0) { stored -= extracted; changed.run(); } return extracted;
    }
    /** Restore saved state without a dirty callback during deserialization. */
    public void restore(long amount) { stored = Math.max(0, Math.min(capacity, amount)); }
}
