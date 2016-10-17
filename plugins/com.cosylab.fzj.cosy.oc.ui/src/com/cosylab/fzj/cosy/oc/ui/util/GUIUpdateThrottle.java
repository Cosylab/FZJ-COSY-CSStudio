package com.cosylab.fzj.cosy.oc.ui.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.cosylab.fzj.cosy.oc.OrbitCorrectionPlugin;

/**
 * GUI Update throttle
 * <p>
 * Assume that the GUI sometimes receives an update that should be processed right away. At other times it receives a
 * burst of updates, where it would be best to wait a little and then redraw to show "everything" instead of reacting to
 * each single update right away which only results in flicker and may even be much slower overall.
 * <p>
 * This class delays the first update a little bit, so in case it's a burst, those updates accumulate. Then it updates,
 * and suppresses further updates for a while to limit flicker. Finally, it starts over.
 *
 * @author Kay Kasemir
 */
public class GUIUpdateThrottle extends Thread {
    /** Delay in milliseconds for the initial update after trigger */
    private final long initialMillis;

    /** Delay in milliseconds for the suppression of a burst of events */
    private final long suppressionMillis;

    /** Counter for trigger events that arrived */
    private int triggers;

    /** Flag that tells thread to run or exit */
    private volatile boolean run = true;

    private final Object mutex = new Object();
    
    private final Optional<Consumer<Void>> consumer;

    /**
     * Initialise
     *
     * @param initialMillis Delay [ms] for the initial update after trigger
     * @param suppressionMillis Delay [ms] for the suppression of a burst of events
     */
    public GUIUpdateThrottle(final long initialMillis, final long suppressionMillis, final Consumer<Void> consumer) {
        super("Orbit Correction GUI Update"); //$NON-NLS-1$
        this.initialMillis = initialMillis;
        this.suppressionMillis = suppressionMillis;
        this.consumer = Optional.ofNullable(consumer);
        setDaemon(true);
    }

    /**
     * Register an event trigger. Will result in throttled call to <code>fire</code>
     */
    public void trigger() {
        synchronized (mutex) { // Count suppressed events
            ++triggers;
            mutex.notifyAll();
        }
    }

    /** Thread Runnable that handles received triggers */
    @SuppressWarnings("nls")
    @Override
    public void run() {
        try {
            while (run) {
                // Wait for a trigger
                synchronized (mutex) {
                    while (triggers <= 0) {
                        mutex.wait();
                    }
                }
                // Wait a little longer, so in case of a burst, we update
                // after already receiving more than just the start of the
                // burst
                Thread.sleep(initialMillis);
                synchronized (mutex) {
                    triggers = 0;
                }
                if (run) {
                    fire();
                }
                // Suppress further updates a little to prevent flicker
                Thread.sleep(suppressionMillis);
            }
        } catch (InterruptedException ex) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE, "GUI Update failed", ex);
        }
    }

    /**
     * Calls accept on the {@link Consumer} provided via the constructor. If the consumer was not given, nothing
     * happens.
     */
    protected void fire() {
        consumer.ifPresent(c -> c.accept(null));
    }

    /** Tell thread to quit, but don't wait for that to happen */
    public void dispose() {
        run = false;
        trigger();
    }
    
    /**
     * Returns true if the thread is alive and running.
     * 
     * @return true if alive and running or false if no longer alive or requested to stop.
     */
    public boolean isRunning() {
        return run && isAlive();
    }
}
