package com.zeromods.core.tutorial;
import java.util.*;
/** Per-profile opt-in progress, with explicit persistence. No automatic opening or narration hooks. */
public final class TutorialProgress {
    private final Map<String,Integer> completed = new HashMap<>();
    public boolean completed(String lesson, int revision) { return completed.getOrDefault(lesson, 0) >= revision; }
    public void complete(TutorialLesson<?,?> lesson) { completed.merge(lesson.id(), lesson.revision(), Math::max); }
    public Map<String,Integer> snapshot() { return Map.copyOf(completed); }
    public void restore(Map<String,Integer> saved) {
        var replacement = new HashMap<String,Integer>();
        saved.forEach((id, revision) -> {
            if (id == null || revision == null || revision < 1) throw new IllegalArgumentException("Invalid progress");
            replacement.put(id, revision);
        });
        completed.clear(); completed.putAll(replacement);
    }
}
