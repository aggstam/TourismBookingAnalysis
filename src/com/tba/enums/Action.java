// -------------------------------------------------------------
//
// Each Action enum represents an application process, that can be
// triggered by the user. Method attribute refers to Main.class
// methods (code) that each Action triggers, using reflection.
// Action data: Value, Description and Method.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.enums;

public enum Action {
    UNRECOGNISED(0, "0 -> This code is used if users input is not recognised.\n", null),
    INSERT_SEARCH_TERM(1, "1 -> Insert Search Term(Destination and Date)\n", "insertSearchTerm"),
    START_SEARCH(2, "2 -> Start search functionality.\n", "startSearch"),
    STATISTICS(3, "3 -> Show statistics of a Search Term.\n", "retrieveSearchTermStatistics"),
    EXPORT_LAST_SEARCH(4, "4 -> Export last search results.\n", "exportLastSearchProperties"),
    EXPORT_SEARCH_TERM_STATISTICS(5, "5 -> Export statistics of a Search Term.\n", "exportSearchTermStatistics"),
    QUIT(6, "6 -> Terminates the application.\n", null);

    private final Integer value;
    private final String description;
    private final String method;

    Action(Integer value, String description, String method) {
        this.value = value;
        this.description = description;
        this.method = method;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }

    public String getMethod() {
        return this.method;
    }
}
