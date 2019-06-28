package com.example.flicks.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel // annotation indicates class is Parcelable
// tracks information associated with individual movie to display
public class Movie {
    // values from API, can only access from within Movie class
    // fields should be public for parceler
    String title;
    String overview;
    String posterPath; // only the path, not full URL
    String backdropPath; // field for backdrop path

    // track vote_average value returned from API
    Double voteAverage;

    // movie id to be parsed from JSONObject passed to constructpr
    Integer id;

    // no arguments. empty constructor required for Parceler
    public Movie() {}

    // add constructor so can initialize directly from JSON data
    // let constructor throw error since want to handle error back in activity
    public Movie(JSONObject object) throws JSONException {
        title = object.getString("title");
        overview = object.getString("overview");
        posterPath = object.getString("poster_path");
        backdropPath = object.getString("backdrop_path");
        // initialize voteAverage from JSON
        voteAverage = object.getDouble("vote_average");
        // initialize movie id from JSON
        id = object.getInt("id");
    }

    // generate getters only since don't need to assign values from outside class
    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getId() {
        return id;
    }
}
