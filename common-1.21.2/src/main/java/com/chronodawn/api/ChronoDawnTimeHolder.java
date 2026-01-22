package com.chronodawn.api;

/**
 * Interface to access independent time field added by LevelGetTimeMixin.
 * This allows ServerLevelSetTimeMixin to share the same time storage.
 *
 * Note: This interface must be outside the mixin package to avoid
 * IllegalClassLoadError when referenced by mixin classes.
 */
public interface ChronoDawnTimeHolder {
    Long chronodawn$getIndependentTime();
    void chronodawn$setIndependentTime(Long time);
}
