package com.zeromods.core.settings;
import java.util.*;
/** Typed extension registry with duplicate and unknown-key rejection. */
public final class SettingsSchema {
    private final Map<String,Setting<?>> settings = new LinkedHashMap<>();
    public <T> Setting<T> register(Setting<T> setting) {
        if (settings.putIfAbsent(setting.id(), setting) != null) throw new IllegalArgumentException("Duplicate setting: " + setting.id());
        return setting;
    }
    public Setting<?> require(String id) {
        var setting = settings.get(id); if (setting == null) throw new IllegalArgumentException("Unknown setting: " + id); return setting;
    }
    public List<Setting<?>> settings() { return List.copyOf(settings.values()); }
}
