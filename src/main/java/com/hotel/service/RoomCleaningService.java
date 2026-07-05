package com.hotel.service;

import java.util.function.Consumer;

/**
 * MULTITHREADING CONCEPT: Extends Thread directly (as opposed to Runnable)
 * to simulate a room cleaning operation running on a background thread.
 *
 * Demonstrates: Thread subclass, Thread.sleep(), Thread.join()
 *
 * The cleaning progresses through stages, and a progress callback is invoked
 * at each stage so the UI can show a progress indicator.
 */
public class RoomCleaningService extends Thread {

    private final int roomNumber;
    private final Consumer<String> progressCallback;
    private final Runnable onComplete;

    /**
     * @param roomNumber       the room being cleaned
     * @param progressCallback called at each cleaning stage with a status message
     * @param onComplete       called when cleaning is finished
     */
    public RoomCleaningService(int roomNumber, Consumer<String> progressCallback,
                                Runnable onComplete) {
        super("CleaningThread-Room-" + roomNumber);
        this.roomNumber = roomNumber;
        this.progressCallback = progressCallback;
        this.onComplete = onComplete;
    }

    /**
     * THREAD SUBCLASS: Overrides run() to define the thread's task.
     * Simulates cleaning by sleeping at each stage.
     */
    @Override
    public void run() {
        try {
            String[] stages = {
                "Removing linens",
                "Vacuuming floor",
                "Sanitizing bathroom",
                "Replacing toiletries",
                "Making the bed",
                "Final inspection"
            };

            for (int i = 0; i < stages.length; i++) {
                String stage = stages[i];
                if (progressCallback != null) {
                    progressCallback.accept("Room " + roomNumber + " — " + stage + " doing...");
                }

                // Thread.sleep(): Simulates work being done at each stage
                Thread.sleep(600);

                if (progressCallback != null) {
                    progressCallback.accept("Room " + roomNumber + " — " + stage + " complete");
                }

                // Thread.yield(): Give other threads a chance to run
                Thread.yield();
            }

            if (progressCallback != null) {
                progressCallback.accept("Room " + roomNumber + " — Cleaning complete!");
            }

            // Pause so the user can read the complete lines before it vanishes
            Thread.sleep(2000);

            if (onComplete != null) {
                onComplete.run();
            }

        } catch (InterruptedException e) {
            System.err.println("[RoomCleaningService] Cleaning interrupted for Room "
                    + roomNumber + ": " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Convenience method demonstrating Thread.join().
     * Blocks the calling thread until this cleaning thread finishes.
     *
     * @param timeoutMs maximum time to wait in milliseconds
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public void waitForCompletion(long timeoutMs) throws InterruptedException {
        // Thread.join(): The calling thread blocks until this thread terminates
        this.join(timeoutMs);
    }
}
