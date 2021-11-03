// -------------------------------------------------------------
//
// This custom Runnable executes a search on a target URL.
// Runnable can be paused, resumed and stopped.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.runnables;

import com.tba.parsing.Property;
import com.tba.parsing.SiteParser;
import com.tba.enums.TargetURL;

import java.util.*;
import java.util.logging.Logger;

public class SearchRunnable implements Runnable {

    private static final Logger logger = Logger.getLogger(SearchRunnable.class.getName());
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private final TargetURL targetURL;
    private final SiteParser siteParser;
    private final Map<String, Property> totalProperties = new HashMap<>(); // Total properties extracted from target URL.

    public SearchRunnable(TargetURL targetURL, String destination, Date date) {
        this.targetURL = targetURL;
        this.siteParser = new SiteParser(destination, date);
    }

    public Boolean getRunning() {
        return running;
    }

    public TargetURL getTargetURL() {
        return targetURL;
    }

    public Map<String, Property> getTotalProperties() {
        return totalProperties;
    }

    // Code Thread executes.
    // On each loop a page is parsed.
    // A retry mechanism has been implemented, in case a page doesn't return any properties, due to error or no more results,
    // or the properties already exists, as most targets return the same properties after last page.
    // If retries exceed 5, search is terminated.
    @Override
    public void run() {
        Integer nextPageNumber = 0;
        List<Property> pageProperties = new ArrayList<>();
        Integer maxPageRetries = 0;
        while (running) {
            if (!pageProperties.isEmpty()) {
                printPageProperties(pageProperties); // Previous page extracted properties are printed.
            }
            if (pauseCheck()) { // Thread checks if paused.
                break;
            }
            try {                
                // Reflection is used to call SiteParser method.
                pageProperties = (List<Property>) siteParser.getClass().getDeclaredMethod(targetURL.getSiteParserMethod(), Integer.class).invoke(siteParser, nextPageNumber);

                // Retry mechanism
                if (pageProperties.size() > 0) {
                    Integer previousCount = totalProperties.size();
                    pageProperties.forEach(p -> totalProperties.put(p.getName(), p));
                    if (totalProperties.size() == previousCount) {
                        stop();
                    }
                    nextPageNumber++;
                    maxPageRetries = 0;
                } else {
                    maxPageRetries++;
                    if (maxPageRetries > 4) {
                        stop();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("There was an exception (" + e.getMessage() +") while parsing " + targetURL.getURL()  + " page " + nextPageNumber +".");
                maxPageRetries++;
                if (maxPageRetries > 4) {
                    stop();
                }
            }
        }
    }

    // Thread is terminated.
    public void stop() {
        running = false;
        resume();
    }

    // Thread pauses execution.
    // Thread will pause execution only after finishing current loop execution.
    public void pause() {
        paused = true;
    }

    // Thread resumes execution.
    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    // Pause check.
    // First Thread checks if it's in running state.
    // If paused, each Thread will try to acquire pauseLock and wait till notified.
    // Once notified, each Thread will check if it's in running state again.
    private Boolean pauseCheck() {
        synchronized (pauseLock) {
            if (!running) {
                return true;
            }
            if (paused) {
                try {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                } catch (InterruptedException ex) {
                    return true;
                }
                if (!running) {
                    return true;
                }
            }
        }
        return false;
    }

    // Prints extracted properties.
    private void printPageProperties(List<Property> pageProperties) {
        pageProperties.forEach(property -> {
            StringBuilder propertySB = new StringBuilder();
            propertySB.append("New property found in ").append(targetURL.getURL()).append(":\n")
                      .append("Name -> ").append(property.getName()).append("\n")
                      .append("Score -> ").append(property.getScore()).append("\n")
                      .append("Price -> ").append(property.getPrice()).append("\n");
            logger.info(propertySB.toString());
        });
    }

}
