package com.example.flicks;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MovieTrailerActivity extends YouTubeBaseActivity {

    // constants
    // base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // parameter name for API key
    public final static String API_KEY_PARAM = "api_key";
    // tag for logging from this activity
    public final static String TAG = "MovieTrailerActivity";

    // instance values - associated with specific instance of movie list activity
    AsyncHttpClient client;
    // id of YouTube video
    String ytId;

    private void getYouTube(String vidId) {
        // create url
        String url = API_BASE_URL + "/movie/" + vidId + "/videos?api_key=";
        // set request parameters (appended to URL)
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        client = new AsyncHttpClient();
        // GET request expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // parse JSON response results value as JSON array
                // get YouTube link
                try {
                    JSONArray results = response.getJSONArray("results");
                    // assign YouTube key from JSON object
                    ytId = results.getJSONObject(0).getString("key");
                    Log.i(TAG, "Returned movie YouTube id" + ytId);

                    // resolve player view from layout
                    YouTubePlayerView playerView = (YouTubePlayerView) findViewById(R.id.player);


                    // initialize with API key stored in secrets.xml
                    playerView.initialize(getString(R.string.youtube_key), new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                            // do work here to cue or play video, etc.
                            youTubePlayer.cueVideo(ytId);
                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                            // log error
                            Log.e("MovieTrailerActivity", "Error initializing YouTube player");
                        }
                    });


                } catch (JSONException e) {
                    logError("Failed to parse movie videos link", e, true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from movie videos endpoint", throwable, true);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_trailer);

        int movieId = (Integer) getIntent().getExtras().getInt("movie_id");
        Log.i("MOVIE ID", Integer.toString(movieId));

        // TODO - add try catch statement?? code may already account for this
        getYouTube(Integer.toString(movieId));

    }

    // handle errors, log and alert user of silent failures (if indicated)
    // Throwable - base class of all errors/exceptions in Java
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log error
        Log.e(TAG, message, error);
        // optionally fail non-silently - alert user
        if (alertUser) {
            // show long toast with error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
