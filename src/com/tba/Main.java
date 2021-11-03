// -------------------------------------------------------------
//
// This console application performs a tourism booking analysis,
// by searching and extracting accommodations information for tourist destinations
// from target URLs, based on specific search terms.
// Users can interact by selecting an action with their input.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba;

import com.tba.database.DatabaseAdapter;
import com.tba.database.Search;
import com.tba.enums.Action;
import com.tba.enums.ControlKey;
import com.tba.enums.TargetURL;
import com.tba.parsing.Property;
import com.tba.runnables.PauseThread;
import com.tba.runnables.SearchRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final Scanner inputScanner = new Scanner(System.in); // System.in is used for interacting with the user.
    private static final SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat simpleDateFormatterWithTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final DatabaseAdapter databaseAdapter = new DatabaseAdapter(); // SQLite database is initialized.
    private static String destinationSearchTerm = null;
    private static Date dateSearchTerm = null;
    private static Map<TargetURL, Map<String, Property>> propertiesPerTargetURL = new HashMap<>(); // Map of last search extracted properties of each target URL.
    private static Search lastSearch = null; // Last performed search results.

    // This is the UI thread interacting with the user.
    public static void main(String[] args) {
        logger.info("Tourism Booking Analysis application started.");
        try {
            StringBuilder legend = new StringBuilder();
            legend.append("Welcome to Tourism Booking Analysis application!\n Please select one of the following actions:\n");
            appendAvailableActions(legend);
            logger.info(legend.toString());
            // Retrieving user input to define the action. Application terminates if user enters "6".
            Action action = retrieveInputAction();
            while (!action.equals(Action.QUIT)) {
                if (!action.equals(Action.UNRECOGNISED)) {
                    Main.class.getDeclaredMethod(action.getMethod()).invoke(null); // Reflection is used to call Action method.
                }
                action = retrieveInputAction();
            }
        } catch (Exception e) {
            // When an exception occurs, its stacktrace is printed and the application terminates.
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +"). Application terminating.");
        } finally {
            // After application termination, database connection is closed.
            databaseAdapter.closeDatabase();
        }
        logger.info("Tourism Booking Analysis application terminated.");
    }

    // Retrieving users input in order to define the action.
    private static Action retrieveInputAction() {
        logger.info("Enter action number: ");
        Action enumAction = Action.UNRECOGNISED;
        try {
            String input = inputScanner.nextLine();
            logger.info("User input: " + input);
            Integer inputAction = Integer.parseInt(input);
            enumAction = EnumSet.allOf(Action.class)
                                       .stream()
                                       .filter(e -> e.getValue().equals(inputAction))
                                       .findAny()
                                       .orElse(Action.UNRECOGNISED);
        } catch (NumberFormatException e) {
            logger.info("Please enter an Integer.");
        }
        if (enumAction.equals(Action.UNRECOGNISED)) {
            // When the input is not recognised, a legend with available actions is printed.
            StringBuilder legend = new StringBuilder();
            legend.append("Unrecognised action. Accepted actions:\n");
            appendAvailableActions(legend);
            logger.info(legend.toString());
        }
        return enumAction;
    }

    // Creating destinationSearchTerm and dateSearchTerm global variables, used by rest functionalities.
    private static void insertSearchTerm() {
        try {
            // Retrieve Destination search term from User.
            logger.info("Provide Destination: ");
            destinationSearchTerm = inputScanner.nextLine();
            while (destinationSearchTerm == null || destinationSearchTerm.isBlank()) {
                logger.info("Input is empty. Please retry: ");
                destinationSearchTerm = inputScanner.nextLine();
            }

            // Retrieve Date search term from User.
            logger.info("Provide Date (dd/MM/yyyy): ");
            simpleDateFormatter.setLenient(false);
            dateSearchTerm = null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE) - 1, 23, 59, 59);
            Date yesterday = calendar.getTime();
            while (dateSearchTerm == null) {
                String dateSearchTermString = inputScanner.nextLine();
                while (dateSearchTermString == null || dateSearchTermString.isBlank()) {
                    logger.info("Input is empty. Please retry: ");
                    dateSearchTermString = inputScanner.nextLine();
                }
                try {
                    dateSearchTerm = simpleDateFormatter.parse(dateSearchTermString);
                    if (dateSearchTerm.before(yesterday)) {
                        logger.info("You can't provide a past date. Please retry: ");
                        dateSearchTerm = null;
                    }
                } catch (ParseException e) {
                    dateSearchTerm = null;
                    logger.info("You must provide a date in 'dd/MM/yyyy' format. Please retry: ");
                }
            }

            StringBuilder searchTermSB = new StringBuilder();
            searchTermSB.append("New search Term Created!\n")
                        .append("Destination -> ").append(destinationSearchTerm).append("\n")
                        .append("Date -> ").append(simpleDateFormatter.format(dateSearchTerm)).append("\n");
            logger.info(searchTermSB.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +") in insertSearchTerm method.");
        }
    }

    // Search on target URLs functionality.
    // User must have created destinationSearchTerm and dateSearchTerm first.
    // Each target URL is assigned to a different Thread.
    // Threads can be controlled (pause/resume/stop) by the user.
    // When user pauses the process, a PauseThread is activated, printing a message periodically.
    // On process finish, found properties of each Thread are retrieved and search statistics are calculated.
    private static void startSearch() {
        try {
            if (destinationSearchTerm == null || destinationSearchTerm.isBlank() || dateSearchTerm == null) {
                logger.info("You must provide search terms before proceeding!");
                insertSearchTerm();
            } else {
                StringBuilder searchSB = new StringBuilder();
                searchSB.append("New search is starting for terms:\n")
                        .append("Destination -> ").append(destinationSearchTerm).append("\n")
                        .append("Date -> ").append(simpleDateFormatter.format(dateSearchTerm)).append("\n")
                        .append("Search started...\n");
                logger.info(searchSB.toString());

                List<SearchRunnable> searchRunnableList = new ArrayList<>();
                List<Thread> searchThreadList = new ArrayList<>();
                EnumSet.allOf(TargetURL.class).forEach(targetURL -> {
                    SearchRunnable searchRunnable = new SearchRunnable(targetURL, destinationSearchTerm, dateSearchTerm);
                    Thread searchThread = new Thread(searchRunnable);
                    searchRunnableList.add(searchRunnable);
                    searchThreadList.add(searchThread);
                });
                PauseThread pauseThread = new PauseThread();
                searchThreadList.forEach(Thread::start);
                String endSignal;
                while(checkSearchRunnableListStatus(searchRunnableList)) { // While Threads are not finished.
                    if (System.in.available() > 0) { // Main Thread checks console for available input.
                        endSignal = inputScanner.nextLine();
                        if (endSignal.equalsIgnoreCase(ControlKey.PAUSE.toString())) {
                            pauseThread.start();
                            searchRunnableList.forEach(SearchRunnable::pause);
                        } else if (endSignal.equalsIgnoreCase(ControlKey.RESUME.toString())) {
                            pauseThread.stop();
                            searchRunnableList.forEach(SearchRunnable::resume);
                        } else if (endSignal.equalsIgnoreCase(ControlKey.STOP.toString())) {
                            pauseThread.stop();
                            searchRunnableList.forEach(SearchRunnable::stop);
                        }
                    }
                }
                propertiesPerTargetURL.clear();
                searchRunnableList.forEach(searchRunnable -> propertiesPerTargetURL.put(searchRunnable.getTargetURL(), searchRunnable.getTotalProperties()));
                searchThreadList.forEach(searchThread -> {
                    try {
                        searchThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                pauseThread.stop();
                finalizeSearch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +") in startSearch method.");
        }
    }

    // Calculates and stores search statistics.
    private static void finalizeSearch() {
        List<Property> properties = new ArrayList<>();
        propertiesPerTargetURL.values().forEach(element -> properties.addAll(element.values()));
        Search.Builder searchBuilder = new Search.Builder();
        searchBuilder.withDestination(destinationSearchTerm)
                     .withDate(dateSearchTerm)
                     .withPropertiesFound(properties.size())
                     .withTimestamp(new Date());
        StringBuilder statisticsSB = new StringBuilder();
        statisticsSB.append("Search finished!\n")
                    .append("Statistics:\n")
                    .append("Properties found -> ").append(properties.size()).append("\n");
        if (properties.size() > 0) {
            Long unavailableProperties = properties.stream().filter(p -> p.getPrice() == null).count();
            Double scoreSum = properties.stream().filter(p -> p.getScore() != null).mapToDouble(Property::getScore).sum();
            Double priceSum = properties.stream().filter(p -> p.getPrice() != null).mapToDouble(Property::getPrice).sum();
            Double scoreMedian = scoreSum / properties.size();
            Double priceMedian = priceSum / properties.size();
            searchBuilder.withUnavailableProperties(unavailableProperties.intValue())
                         .withScoreMedian(scoreMedian)
                         .withPriceMedian(priceMedian);
            statisticsSB.append("Unavailable Properties -> ").append(unavailableProperties).append("\n")
                        .append("Score Median -> ").append(String.format("%.2f", scoreMedian)).append("\n")
                        .append("Price Median -> ").append(String.format("%.2f", priceMedian)).append("\n");
        }
        logger.info(statisticsSB.toString());
        lastSearch = searchBuilder.build();
        databaseAdapter.insertSearch(lastSearch);
    }

    // Check if SearchRunnable Threads are active.
    private static boolean checkSearchRunnableListStatus(List<SearchRunnable> searchRunnableList) {
        for (SearchRunnable searchRunnable : searchRunnableList) {
            if (searchRunnable.getRunning()) {
                return true;
            }
        }
        return false;
    }

    // Retrieves stored searches(history) of a search term.
    // User must have created destinationSearchTerm and dateSearchTerm first.
    private static void retrieveSearchTermStatistics() {
        try {
            if (destinationSearchTerm == null || destinationSearchTerm.isBlank() || dateSearchTerm == null) {
                logger.info("You must provide search terms before proceeding!");
                insertSearchTerm();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Retrieving previous searches statistics for terms:\n")
                        .append("Destination -> ").append(destinationSearchTerm).append("\n")
                        .append("Date -> ").append(simpleDateFormatter.format(dateSearchTerm));
                logger.info(stringBuilder.toString());
                StringBuilder searchSB = retrieveSearchTermListStringBuilder();
                logger.info(searchSB.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +") in retrieveSearchTermStatistics method.");
        }
    }

    // Exports last search results to a .txt file.
    private static void exportLastSearchProperties() {
        try {
            if (propertiesPerTargetURL.isEmpty() || lastSearch == null) {
                logger.info("You must execute a search before proceeding!");
            } else {
                exportFolderInitialization();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Exporting search results for terms:")
                        .append("Destination -> ").append(lastSearch.getDestination()).append(", ")
                        .append("Date -> ").append(simpleDateFormatter.format(lastSearch.getDate()));
                logger.info(stringBuilder.toString());
                StringBuilder exportSB = new StringBuilder();
                exportSB.append("Search export for terms:\n")
                        .append("Destination -> ").append(lastSearch.getDestination()).append(", ")
                        .append("Date -> ").append(simpleDateFormatter.format(lastSearch.getDate())).append("\n")
                        .append("Statistics:\n")
                        .append("Properties found -> ").append(lastSearch.getPropertiesFound()).append(", ")
                        .append("Unavailable Properties -> ").append(lastSearch.getUnavailableProperties()).append(", ")
                        .append("Score Median -> ").append(String.format("%.2f", lastSearch.getScoreMedian())).append(", ")
                        .append("Price Median -> ").append(String.format("%.2f", lastSearch.getPriceMedian())).append("\n");
                propertiesPerTargetURL.keySet().forEach(key -> {
                    exportSB.append("Properties found in ").append(key.getURL()).append(":\n");
                    propertiesPerTargetURL.get(key).values().forEach(property -> {
                        exportSB.append("Name -> ").append(property.getName()).append(", ")
                                .append("Score -> ").append(property.getScore()).append(", ")
                                .append("Price -> ").append(property.getPrice()).append("\n");
                    });
                });
                String fileName = "search_"+ lastSearch.getTimestamp().getTime() + "_export.txt";
                Writer writer = new FileWriter("exports/" + fileName);
                writer.append(exportSB.toString());
                writer.flush();
                writer.close();
                logger.info("File " + fileName +" has been successfully created in exports folder!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +") in exportLastSearchResults method.");
        }
    }

    // Exports stored searches(history) of a search term to a .txt file.
    // User must have created destinationSearchTerm and dateSearchTerm first.
    private static void exportSearchTermStatistics() {
        try {
            if (destinationSearchTerm == null || destinationSearchTerm.isBlank() || dateSearchTerm == null) {
                logger.info("You must provide search terms before proceeding!");
                insertSearchTerm();
            } else {
                exportFolderInitialization();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Exporting previous searches statistics for terms: ")
                        .append("Destination -> ").append(destinationSearchTerm).append(", ")
                        .append("Date -> ").append(simpleDateFormatter.format(dateSearchTerm));
                logger.info(stringBuilder.toString());
                StringBuilder searchSB = retrieveSearchTermListStringBuilder();
                stringBuilder.append(searchSB);
                String fileName = "search_term_history_" + new Date().getTime() + "_export.txt";
                Writer writer = new FileWriter("exports/" + fileName);
                writer.append(searchSB.toString());
                writer.flush();
                writer.close();
                logger.info("File " + fileName +" has been successfully created in exports folder!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("There was an exception (" + e.getMessage() +") in exportSearchTermStatistics method.");
        }
    }

    // Initializes export folder.
    private static void exportFolderInitialization() throws IOException {
        File exportFolder = new File("exports");
        if (!exportFolder.exists() && !exportFolder.mkdir()) {
            throw new IOException("Export folder could not be accessed.");
        }
    }

    // Retrieves stored searches of a search term from database and returns them as a StringBuilder.
    private static StringBuilder retrieveSearchTermListStringBuilder() {
        StringBuilder searchSB = new StringBuilder();
        List<Search> searchList = databaseAdapter.retrieveSearchTermList(destinationSearchTerm, dateSearchTerm);
        if (searchList.size() <= 0) {
            searchSB.append("No previous search statistics records found!");
        } else {
            searchSB.append("Statistics history for search term: ")
                    .append("Destination -> ").append(destinationSearchTerm).append(", ")
                    .append("Date -> ").append(simpleDateFormatter.format(dateSearchTerm)).append("\n");
            searchList.forEach(search -> {
                searchSB.append(simpleDateFormatterWithTime.format(search.getTimestamp())).append(": ")
                        .append("Properties found -> ").append(search.getPropertiesFound()).append(", ")
                        .append("Unavailable Properties -> ").append(search.getUnavailableProperties()).append(", ")
                        .append("Score Median -> ").append(String.format("%.2f", search.getScoreMedian())).append(", ")
                        .append("Price Median -> ").append(String.format("%.2f", search.getPriceMedian())).append("\n");
            });
        }
        return searchSB;
    }

    // Appends all Action class enums values and descriptions to provided StringBuilder.
    private static void appendAvailableActions(StringBuilder legend) {
        EnumSet.allOf(Action.class)
                .stream()
                .filter(e -> e.getValue() > 0)
                .forEach(e -> legend.append(e.getDescription()));
    }

}
