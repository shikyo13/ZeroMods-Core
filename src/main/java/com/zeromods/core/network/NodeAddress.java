package com.zeromods.core.network;
import java.util.Objects;
/** Dimension is part of identity; equal coordinates in different worlds are distinct. */
public record NodeAddress(String dimension, int x, int y, int z) {
    public NodeAddress { Objects.requireNonNull(dimension); if (dimension.isBlank()) throw new IllegalArgumentException("dimension"); }
}
