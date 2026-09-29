package com.apkglobal.test.flow;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory answers of the current scan setup (process-scoped). Read by the Ready screen and by the
 * scanner screen that plugs in after it.
 */
public final class ScanSession {

    private static final ScanSession INSTANCE = new ScanSession();

    private final Map<String, String> answers = new LinkedHashMap<>();
    private boolean detailedUnlocked;
    private boolean hdUnlocked;

    private ScanSession() {
    }

    public static ScanSession get() {
        return INSTANCE;
    }

    public synchronized void put(String stepId, String optionKey) {
        answers.put(stepId, optionKey);
    }

    @Nullable
    public synchronized String get(String stepId) {
        return answers.get(stepId);
    }

    public synchronized void setDetailedUnlocked(boolean unlocked) {
        detailedUnlocked = unlocked;
    }

    public synchronized boolean isDetailedUnlocked() {
        return detailedUnlocked;
    }

    public synchronized void setHdUnlocked(boolean unlocked) {
        hdUnlocked = unlocked;
    }

    public synchronized boolean isHdUnlocked() {
        return hdUnlocked;
    }

    public synchronized void clear() {
        answers.clear();
        detailedUnlocked = false;
        hdUnlocked = false;
    }
}
