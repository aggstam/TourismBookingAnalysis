// -------------------------------------------------------------
//
// Each TargetURL enum represents a web site, the application
// communicates with, to extract properties data for each search.
// Method attribute refers to SiteParser.class methods (code) that
// each TargetURL triggers, using reflection.
// Action data: Url,  SiteParser Method.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.enums;

public enum TargetURL {
    AIRBNB("airbnb.gr", "parseAirbnbPage"),
    BOOKING("booking.com", "parseBookingPage"),
    HOTELS("hotels.com", "parseHotelsPage"),
    HOTELS_SCANNER("hotels-scanner.com", "parseHotelsScannerPage");

    private final String url;
    private final String siteParserMethod;

    TargetURL(final String url, final String siteParserMethod) {
        this.url = url;
        this.siteParserMethod = siteParserMethod;
    }

    public String getURL() {
        return url;
    }

    public String getSiteParserMethod() {
        return siteParserMethod;
    }
}
