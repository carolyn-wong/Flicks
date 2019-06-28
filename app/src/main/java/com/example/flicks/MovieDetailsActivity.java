package com.example.flicks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    // retrieve and unwrap Movie from Intent
    // movie to display
    Movie movie;

    // view objects;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivTrailerImage;
    String trailerUrl;
    TextView tvRecTitle;
    ImageView ivRecImage;

    // constants
    // base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // parameter name for API key
    public final static String API_KEY_PARAM = "api_key";
    // tag for logging from this activity
    public final static String TAG = "MovieDetailsActivity";

    // instance values - associated with specific instance of movie list activity
    AsyncHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // resolve view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        tvRecTitle = (TextView) findViewById(R.id.tvRecTitle);
        ivTrailerImage = findViewById(R.id.ivTrailerImage);
        ivRecImage = findViewById(R.id.ivRecImage);

        // unwrap movie pass in via intent, using its simple name as key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        trailerUrl = getIntent().getStringExtra("trailer_url");

        // get correct placeholder/imageview for current orientation
        int placeholderId = R.drawable.flicks_backdrop_placeholder;

        // load image using Glide
        Glide.with(getBaseContext())
                .load(trailerUrl)
                .bitmapTransform(new RoundedCornersTransformation(getBaseContext(), 25, 0))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(ivTrailerImage);

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        // assign click listener to display trailer when backdrop image tapped
        ivTrailerImage = (ImageView) findViewById(R.id.ivTrailerImage);
        ivTrailerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(movie.getId() == null) {
                    Toast.makeText(getApplicationContext(), "Movie id does not exist", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    intent.putExtra("movie_id", movie.getId());
                    Log.i("movie id here", Integer.toString(movie.getId()));
                    startActivity(intent);
                }
            }
        });

        getRec(Integer.toString(movie.getId()));
    }


    private void getRec(String vidId) {
        // create url
        String url = API_BASE_URL + "/movie/" + vidId + "/recommendations?api_key=";
        // set request parameters (appended to URL)
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        client = new AsyncHttpClient();
        // GET request expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // parse JSON response results value as JSON array
                try {
                    JSONArray results = response.getJSONArray("results");
                    // assign YouTube key from JSON object
                    String recTitle = results.getJSONObject(0).getString("title");
                    Log.i(TAG, "Returned recommendation title" + recTitle);
                    tvRecTitle.setText(recTitle);
                } catch (JSONException e) {
                    logError("Failed to parse recommendations", e, true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from recommendations endpoint", throwable, true);
            }
        });
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
