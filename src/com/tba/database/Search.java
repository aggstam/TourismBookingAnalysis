// -------------------------------------------------------------
//
// This is the Search Structure used by the application.
// Search data: Id, Destination, Date, Properties found, Unavailable properties, Score Median, Price Median and Timestamp.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.database;

import java.util.Date;

public class Search {

    private Integer id;
    private String destination;
    private Date date;
    private Integer propertiesFound;
    private Integer unavailableProperties;
    private Double scoreMedian;
    private Double priceMedian;
    private Date timestamp;

    public static class Builder {

        private Integer id;
        private String destination;
        private Date date;
        private Integer propertiesFound;
        private Integer unavailableProperties;
        private Double scoreMedian;
        private Double priceMedian;
        private Date timestamp;

        public Builder() {}

        public Search.Builder withId(Integer id) {
            this.id = id;
            return this;
        }

        public Search.Builder withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public Search.Builder withDate(Date date) {
            this.date = date;
            return this;
        }

        public Search.Builder withPropertiesFound(Integer propertiesFound) {
            this.propertiesFound = propertiesFound;
            return this;
        }

        public Search.Builder withUnavailableProperties(Integer unavailableProperties) {
            this.unavailableProperties = unavailableProperties;
            return this;
        }

        public Search.Builder withScoreMedian(Double scoreMedian) {
            this.scoreMedian = scoreMedian;
            return this;
        }

        public Search.Builder withPriceMedian(Double priceMedian) {
            this.priceMedian = priceMedian;
            return this;
        }

        public Search.Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Search build() {
            Search search = new Search();
            search.id = this.id;
            search.destination = this.destination;
            search.date = this.date;
            search.propertiesFound = this.propertiesFound;
            search.unavailableProperties = this.unavailableProperties;
            search.scoreMedian = this.scoreMedian;
            search.priceMedian = this.priceMedian;
            search.timestamp = this.timestamp;
            return search;
        }
    }

    public Search() {}

    public Integer getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public Date getDate() {
        return date;
    }

    public Integer getPropertiesFound() {
        return propertiesFound;
    }

    public Integer getUnavailableProperties() {
        return unavailableProperties;
    }

    public Double getScoreMedian() {
        return scoreMedian;
    }

    public Double getPriceMedian() {
        return priceMedian;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
