package com.caraoke.media;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Deletes a stored file only after the surrounding DB transaction commits, so a rollback
 * never leaves a row pointing at a file we already removed.
 */
public final class MediaCleanup {

    private MediaCleanup() { }

    public static void deleteAfterCommit(MediaStorage media, String key) {
        if (key == null) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            media.delete(key);                 // no transaction (e.g. unit tests): delete now
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                media.delete(key);
            }
        });
    }
}
