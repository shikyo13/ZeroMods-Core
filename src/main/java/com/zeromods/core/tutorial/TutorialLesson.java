package com.zeromods.core.tutorial;
import java.util.*;
/** Immutable lesson definition. Stable namespaced IDs keep future scenes and progress compatible. */
public record TutorialLesson<C,T>(String id, int revision, List<TutorialScene<C,T>> scenes) {
    public TutorialLesson {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || revision < 1) throw new IllegalArgumentException("Lesson ID/revision");
        scenes = List.copyOf(scenes); if (scenes.isEmpty()) throw new IllegalArgumentException("Empty lesson");
        var ids = new HashSet<String>();
        for (var scene : scenes) {
            if (scene.id() == null || !ids.add(scene.id()) || !Double.isFinite(scene.durationSeconds()) || scene.durationSeconds() <= 0)
                throw new IllegalArgumentException("Invalid or duplicate scene");
        }
    }
    public TutorialPlaybackController playback(int initialScene) {
        return new TutorialPlaybackController(scenes.stream().mapToDouble(TutorialScene::durationSeconds).toArray(), initialScene);
    }
}
