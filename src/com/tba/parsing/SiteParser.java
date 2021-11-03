// -------------------------------------------------------------
//
// This auxiliary class is used by the application to extract
// each TargetURL page properties.
// JSoup is used for communicating and HTML parsing.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.parsing;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class SiteParser {

    private static final Logger logger = Logger.getLogger(SiteParser.class.getName());
    private final String destination;
    private final LocalDate checkinDate;
    private final LocalDate checkoutDate;
    private static final DateTimeFormatter  formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SiteParser(String destination, Date date) {
        this.destination = destination;
        this.checkinDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.checkoutDate = this.checkinDate.plusDays(1);
    }

    // airbnb.gr page retrieval and parsing.
    public List<Property> parseAirbnbPage(Integer pageNumber) throws IOException {
        // Search url is created and executed.
        StringBuilder searchSB = new StringBuilder();
        searchSB.append("https://www.airbnb.gr/s/")
                .append(destination).append("/homes?")
                .append("checkin=").append(checkinDate.format(formatter))
                .append("&checkout=").append(checkoutDate.format(formatter))
                .append("&items_offset=").append(pageNumber * 20);
        Document doc = Jsoup.connect(searchSB.toString()).get();

        // Properties extraction.
        List<Property> pageProperties = new ArrayList<>();
        for (Element div : doc.select("._8ssblpx")) {
            Double score = null;
            try {
                score = Double.parseDouble(div.selectFirst("._10fy1f8").text()) * 2;
            } catch (NullPointerException | NumberFormatException e) {
                /* Score value remains null. */
            }
            Double price = null;
            try {
                price = Double.parseDouble(div.selectFirst("._1p7iugi").text().replaceAll(".*:€", ""));
            } catch (NullPointerException | NumberFormatException e) {
                /* Price value remains null. Property is considered as unavailable. */
            }
            Property property = new Property.Builder()
                    .withName(div.selectFirst("._bzh5lkq").text())
                    .withScore(score)
                    .withPrice(price)
                    .build();
            pageProperties.add(property);
        }
        return pageProperties;
    }

    // booking.com page retrieval and parsing.
    public List<Property> parseBookingPage(Integer pageNumber) throws IOException {
        // Search url is created and executed.
        StringBuilder searchSB = new StringBuilder();
        searchSB.append("https://www.booking.com/searchresults.en.html?")
                .append("ss=").append(destination)
                .append("&checkin_year=").append(checkinDate.getYear())
                .append("&checkin_month=").append(checkinDate.getMonthValue())
                .append("&checkin_monthday=").append(checkinDate.getDayOfMonth())
                .append("&checkout_year=").append(checkoutDate.getYear())
                .append("&checkout_month=").append(checkoutDate.getMonthValue())
                .append("&checkout_monthday=").append(checkoutDate.getDayOfMonth())
                .append("&offset=").append(pageNumber * 25);
        Document doc = Jsoup.connect(searchSB.toString()).get();

        // Properties extraction.
        List<Property> pageProperties = new ArrayList<>();
        for (Element div : doc.select(".sr_property_block")) {
            Double score = null;
            try {
                score = Double.parseDouble(div.selectFirst(".bui-review-score__badge").text());
            } catch (NullPointerException | NumberFormatException e) {
                /* Score value remains null. */
            }
            Double price = null;
            try {
                price = Double.parseDouble(div.selectFirst(".bui-price-display__value").text().replace("€ ", ""));
            } catch (NullPointerException | NumberFormatException e) {
                /* Price value remains null. Property is considered as unavailable. */
            }
            Property property = new Property.Builder()
                    .withName(div.selectFirst(".sr-hotel__name").text())
                    .withScore(score)
                    .withPrice(price)
                    .build();
            pageProperties.add(property);
        }
        return pageProperties;
    }

    // hotels.com page retrieval and parsing.
    public List<Property> parseHotelsPage(Integer pageNumber) throws IOException {
        // Search url is created and executed.
        StringBuilder searchSB = new StringBuilder();
        searchSB.append("https://el.hotels.com/search.do?")
                .append("q-destination=").append(destination)
                .append("&q-check-in=").append(checkinDate.format(formatter))
                .append("&q-check-out=").append(checkoutDate.format(formatter))
                .append("&pn=").append(pageNumber + 1);
        Document doc = Jsoup.connect(searchSB.toString()).get();

        // Properties extraction.
        List<Property> pageProperties = new ArrayList<>();
        for (Element div : doc.select(".hotel-wrap")) {
            Double score = null;
            try {
                score = Double.parseDouble(div.selectFirst(".guest-reviews-badge").text().replaceAll("\\D+","")) / 10;
            } catch (NullPointerException | NumberFormatException e) {
                /* Score value remains null. */
            }
            Double price = null;
            try {
                price = Double.parseDouble(div.selectFirst(".price").text().replace("€", ""));
            } catch (NullPointerException | NumberFormatException e) {
                /* Price value remains null. Property is considered as unavailable. */
            }
            Property property = new Property.Builder()
                    .withName(div.selectFirst(".p-name").text())
                    .withScore(score)
                    .withPrice(price)
                    .build();
            pageProperties.add(property);
        }
        return pageProperties;
    }

    // hotels-scanner.com page retrieval and parsing.
    // This target retrieves properties using JavaScript, therefore two requests are executed.
    // First requests obtains target's cookies, so the second one can be successful.
    // If a lot of requests are executed in a small period of time, target will not accept
    // applications requests, due to security concerns.
    public List<Property> parseHotelsScannerPage(Integer pageNumber) throws IOException {
        // Search url is created and executed to obtain target's cookies.
        StringBuilder searchSB = new StringBuilder();
        searchSB.append("https://www.hotels-scanner.com/Hotels/Search?")
                .append("destination=place:").append(destination)
                .append("&checkin=").append(checkinDate.format(formatter))
                .append("&checkout=").append(checkoutDate.format(formatter))
                .append("&pageIndex=").append(pageNumber)
                .append("&radius=0km&Rooms=1&adults_1=2&showSoldOut=true");
        Connection.Response res = Jsoup.connect(searchSB.toString()).method(Connection.Method.GET).execute();
        // Second (actual) url is created and executed.
        searchSB = new StringBuilder();
        searchSB.append("https://www.hotels-scanner.com/Hotels/SearchResults?")
                .append("destination=place:").append(destination)
                .append("&checkin=").append(checkinDate.format(formatter))
                .append("&checkout=").append(checkoutDate.format(formatter))
                .append("&pageIndex=").append(pageNumber)
                .append("&radius=0km&Rooms=1&adults_1=2&showSoldOut=true");

        List<Property> pageProperties = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(searchSB.toString()).cookies(res.cookies()).get();

            // Properties extraction.
            for (Element div : doc.select(".hc-searchresultitem")) {
                Double score = null;
                try {
                    score = Double.parseDouble(div.selectFirst(".hc-guestratingsummary").attr("content"));
                } catch (NullPointerException | NumberFormatException e) {
                    /* Score value remains null. */
                }
                Double price = null;
                try {
                    price = Double.parseDouble(div.selectFirst(".hc-searchresultitemdeal__currentrate").text().replace("€", ""));
                } catch (NullPointerException | NumberFormatException e) {
                    /* Price value remains null. Property is considered as unavailable. */
                }
                Property property = new Property.Builder()
                        .withName(div.selectFirst(".hc-searchresultitem__hotelname").text())
                        .withScore(score)
                        .withPrice(price)
                        .build();
                pageProperties.add(property);
            }
        } catch (HttpStatusException e) {
            logger.info("hotels-scanner.com thinks we attack them......again. HttpStatus " + e.getStatusCode() + " was returned.");
        }
        return pageProperties;
    }

}
