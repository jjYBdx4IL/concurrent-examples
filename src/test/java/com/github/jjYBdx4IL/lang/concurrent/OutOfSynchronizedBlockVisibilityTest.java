package com.github.jjYBdx4IL.lang.concurrent;

import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html
 *
 * "An unlock (synchronized block or method exit) of a monitor happens-before
 * every subsequent lock (synchronized block or method entry) of that same
 * monitor. And because the happens-before relation is transitive, all actions
 * of a thread prior to unlocking happen-before all actions subsequent to any
 * thread locking that monitor."
 *
 * @author jjYBdx4IL
 */
public class OutOfSynchronizedBlockVisibilityTest implements Runnable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OutOfSynchronizedBlockVisibilityTest.class);

    volatile int iterations = 1000000;
    int a = 0; // gets updated outside/before synchronized block
    int b = 0; // tells receiver thread to run, updated insize synchronized block

    @Test(timeout = 300000L)
    public void test() throws InterruptedException {

        Thread rcv = new Thread(this, "receiver-thread");
        rcv.start();

        while (iterations > 0) {
            a = 1;
            synchronized (this) {
                b = 1;
                LOG.debug("notify");
                notify();
                LOG.debug("wait");
                while (b != 0) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        LOG.error("", ex);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        while (iterations > 0) {
            iterations--;

            LOG.debug("wait");
            synchronized (this) {
                while (b != 1) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        LOG.error("", ex);
                        throw new RuntimeException(ex);
                    }
                }
            }
            LOG.debug("rcvd");
            if (a != 1) {
                LOG.debug("error");
                throw new IllegalStateException();
            }
            a = 0;
            synchronized (this) {
                b = 0;
                LOG.debug("notify");
                notify();
            }
        }
        LOG.debug("done");
    }
}
