package com.zeromods.core.filter;

/** Player-only selection, independent of non-player category filters. */
public enum PlayerListMode {
  GENERAL_FILTER, LISTED, UNLISTED;

  public boolean matches(boolean listed) {
    return switch (this) {
      case LISTED -> listed;
      case UNLISTED -> !listed;
      case GENERAL_FILTER -> throw new IllegalStateException("Use the general entity filter");
    };
  }
}
