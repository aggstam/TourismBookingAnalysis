// -------------------------------------------------------------
//
// This is the Property Structure used by the application.
// Property data: Name, Score and Price.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.parsing;

public class Property {

    private String name;
    private Double score;
    private Double price;

    public static class Builder {

        private String name;
        private Double score;
        private Double price;

        public Builder() {}

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withScore(Double score) {
            this.score = score;
            return this;
        }

        public Builder withPrice(Double price) {
            this.price = price;
            return this;
        }

        public Property build() {
            Property property = new Property();
            property.name = this.name;
            property.score = this.score;
            property.price = this.price;
            return property;
        }
    }

    public Property() {}

    public String getName() {
        return name;
    }

    public Double getScore() {
        return score;
    }

    public Double getPrice() {
        return price;
    }

}
