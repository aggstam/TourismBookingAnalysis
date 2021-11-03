// -------------------------------------------------------------
//
// This is the Database Adapter used by the application, to
// communicate with the SQLite Database.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseAdapter {

    private static final Logger logger = Logger.getLogger(DatabaseAdapter.class.getName());
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String url = "jdbc:sqlite:tba.db";
    private Connection connection;

    // Database initialization method.
    // 'SEARCHES' table is created, if it doesn't exist.
    public DatabaseAdapter() {
        try {
            connection = DriverManager.getConnection(url);
            if (connection != null) {
                StringBuilder queryBuilder = new StringBuilder().append("CREATE TABLE IF NOT EXISTS 'SEARCHES'(")
                                                                .append("'SEARCH_ID' INTEGER PRIMARY KEY AUTOINCREMENT,")
                                                                .append("'SEARCH_DESTINATION' TEXT,")
                                                                .append("'SEARCH_DATE' TEXT,")
                                                                .append("'SEARCH_PROPERTIES_FOUND' INTEGER,")
                                                                .append("'SEARCH_UNAVAILABLE_PROPERTIES' INTEGER,")
                                                                .append("'SEARCH_SCORE_MEDIAN' REAL,")
                                                                .append("'SEARCH_PRICE_MEDIAN' REAL,")
                                                                .append("'SEARCH_TIMESTAMP' TEXT)");
                connection.createStatement().execute(queryBuilder.toString());
                logger.info("Database connection initialized successfully.");
            } else {
                logger.info("Database connection could not be initialized.");
            }
        } catch (SQLException e) {
            logger.info("Database connection could not be initialized. Exception thrown: " + e.getMessage());
            closeDatabase();
        }
    }

    // Database connection termination method.
    public void closeDatabase() {
        try {
            if (connection != null) {
                connection.close();
                logger.info("Database connection terminated successfully.");
            } else {
                logger.info("Database connection could not be terminated. Database is not initialized properly.");
            }
        } catch (SQLException e) {
            logger.info("Database connection could not be terminated. Exception thrown: " + e.getMessage());
        }
    }

    // Inserts a Search record to the Database.
    public void insertSearch(Search search) {
        try {
            StringBuilder queryBuilder = new StringBuilder().append("INSERT OR IGNORE INTO 'SEARCHES' ('SEARCH_DESTINATION', 'SEARCH_DATE', 'SEARCH_PROPERTIES_FOUND', 'SEARCH_UNAVAILABLE_PROPERTIES', 'SEARCH_SCORE_MEDIAN', 'SEARCH_PRICE_MEDIAN', 'SEARCH_TIMESTAMP') VALUES ('")
                                                            .append(search.getDestination()).append("', '")
                                                            .append(formatter.format(search.getDate())).append("', '")
                                                            .append(search.getPropertiesFound()).append("', '")
                                                            .append(search.getUnavailableProperties()).append("', '")
                                                            .append(search.getScoreMedian()).append("', '")
                                                            .append(search.getPriceMedian()).append("', '")
                                                            .append(formatter.format(search.getTimestamp())) .append("')");
            connection.createStatement().execute(queryBuilder.toString());
            logger.info("Insert statement successfully executed!");
        } catch (SQLException e) {
            logger.info("Insert statement could be executed. Exception thrown: " + e.getMessage());
        }
    }

    // Retrieves all Search records from Database matching the criteria.
    public List<Search> retrieveSearchTermList(String destination, Date date) {
        List<Search> searchList = new ArrayList<>();
        try {
            StringBuilder queryBuilder = new StringBuilder().append("SELECT * FROM SEARCHES WHERE ")
                                                            .append("SEARCH_DESTINATION").append(" = '").append(destination).append("' AND ")
                                                            .append("SEARCH_DATE").append(" = '").append(formatter.format(date)).append("'");
            ResultSet resultSet = connection.createStatement().executeQuery(queryBuilder.toString());
            // Generates a Search records list from Database records.
            while (resultSet.next()) {
                Search search = new Search.Builder()
                                    .withId(resultSet.getInt("SEARCH_ID"))
                                    .withDestination(resultSet.getString("SEARCH_DESTINATION"))
                                    .withDate(resultSet.getDate("SEARCH_DATE"))
                                    .withPropertiesFound(resultSet.getInt("SEARCH_PROPERTIES_FOUND"))
                                    .withUnavailableProperties(resultSet.getInt("SEARCH_UNAVAILABLE_PROPERTIES"))
                                    .withScoreMedian(resultSet.getDouble("SEARCH_SCORE_MEDIAN"))
                                    .withPriceMedian(resultSet.getDouble("SEARCH_PRICE_MEDIAN"))
                                    .withTimestamp(resultSet.getDate("SEARCH_TIMESTAMP"))
                                    .build();
                searchList.add(search);
            }
        } catch (SQLException e) {
            logger.info("Select statement could be executed. Exception thrown: " + e.getMessage());
        }
        return searchList;
    }

}
