// -------------------------------------------------------------
//
// This custom Runnable is used to periodically inform the user
// that application has been paused.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.runnables;

import java.util.logging.Logger;

public class PauseThread implements Runnable {

    private static final Logger logger = Logger.getLogger(PauseThread.class.getName());
    private Thread worker;
    private Boolean running = false;

    public PauseThread() {}

    // A new Thread starts executing the Runnable code.
    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    // Thread is terminated.
    public void stop() {
        running = false;
        if (worker != null) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                logger.info("Pause Thread was interrupted.");
            }
        }
    }

    // Code Thread executes.
    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                logger.info("Operation has been paused...");
                Thread.sleep(5000);
            } catch (InterruptedException e){
                logger.info("Pause Thread was interrupted.");
            }
        }
    }

}
