# TourismBookingAnalysis
This console application performs a tourism booking analysis, by searching and extracting accommodations information for tourist destinations from target URLs, based on specific search terms.
<br>
Users can interact by selecting an action with their input.
<br>
Note: project requires *java* to be installed.
<br>
External jars sqlite-jdbc-3.30.1.jar and jsoup-1.15.3.jar are provided.

## Usage
First we compile the project:
```
% javac -cp libs/sqlite-jdbc-3.30.1.jar:libs/jsoup-1.15.3.jar src/**/*.java -d bin
```
Then we can execute:
```
% java -cp libs/sqlite-jdbc-3.30.1.jar:.:libs/jsoup-1.15.3.jar:.:bin com.tba.Main
```

## Execution example
```
❯ java -cp libs/sqlite-jdbc-3.30.1.jar:.:libs/jsoup-1.15.3.jar:.:bin com.tba.Main
Jan 22, 2023 5:21:35 PM com.tba.database.DatabaseAdapter <init>
INFO: Database connection initialized successfully.
Jan 22, 2023 5:21:35 PM com.tba.Main main
INFO: Tourism Booking Analysis application started.
Jan 22, 2023 5:21:35 PM com.tba.Main main
INFO: Welcome to Tourism Booking Analysis application!
 Please select one of the following actions:
1 -> Insert Search Term(Destination and Date)
2 -> Start search functionality.
3 -> Show statistics of a Search Term.
4 -> Export last search results.
5 -> Export statistics of a Search Term.
6 -> Terminates the application.

Jan 22, 2023 5:21:35 PM com.tba.Main retrieveInputAction
INFO: Enter action number:
4
Jan 22, 2023 5:21:44 PM com.tba.Main retrieveInputAction
INFO: User input: 4
Jan 22, 2023 5:21:44 PM com.tba.Main exportLastSearchProperties
INFO: You must execute a search before proceeding!
Jan 22, 2023 5:21:44 PM com.tba.Main retrieveInputAction
INFO: Enter action number:
6
Jan 22, 2023 5:21:46 PM com.tba.Main retrieveInputAction
INFO: User input: 6
Jan 22, 2023 5:21:46 PM com.tba.database.DatabaseAdapter closeDatabase
INFO: Database connection terminated successfully.
Jan 22, 2023 5:21:46 PM com.tba.Main main
INFO: Tourism Booking Analysis application terminated.
```
