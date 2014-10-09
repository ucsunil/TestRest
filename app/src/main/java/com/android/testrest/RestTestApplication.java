package com.android.testrest;

import android.app.Application;

/**
 * Created by umonssu on 10/6/14.
 */
public class RestTestApplication extends Application {

    public static final String[] requests = {"GET", "POST", "PUT", "DELETE"};
    public static final String MALFORMED_URL = "The query URL is malformed. Please check and try again.";
    public static final String EMPTY_URL = "Please enter the URL to query";
    public static final String APP_FAILURE = "Application level failure!!";
    public static final String QUERY_SUCCESS = "Success; Code = ";
    public static final String QUERY_REDIRECT = "Redirect; Code = ";
    public static final String QUERY_FAILURE = "Client error; Code = ";
    public static final String SERVER_ERROR = "Server error; Code = ";
    public static final String ADD_HEADER_MISSING_KEY = "The header key is missing. Please enter a " +
            "value and then try adding a new header.";
    public static final String ADD_HEADER_MISSING_VALUE = "The value key is missing. Please enter a " +
            "value and then try adding a new header.";
    public static final String ADD_HEADER_MISSING = "Please enter values for the current header before " +
            "adding a new one.";
    public static final String HEADER_MISSING_KEY = "The header key is missing. Please fix the " +
                        "error and try again";
    public static final String HEADER_MISSING_VALUE = "The header value is missing. Please fix " +
                        "the error and try again";
}
