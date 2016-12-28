package com.luceolab.parentaladvisorobjectmodule;

public interface Constants {

    String REST_API_URL = "http://shadowing.luceolab.com:8080";
    String PREFS_NAME = "ParentalAdvisorObjectModule";

    // Milliseconds per second
    int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    int UPDATE_INTERVAL_IN_SECONDS = 120;
    // Update frequency in milliseconds
    long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    int FASTEST_INTERVAL_IN_SECONDS = 120;
    // A fast frequency ceiling in milliseconds
    long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

}
