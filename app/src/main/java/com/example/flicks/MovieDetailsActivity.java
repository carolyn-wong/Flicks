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

import org.parceler.Parcels;

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

    // add config instead of passing in image url as an intent?
    // context for rendering
    // Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // resolve view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);

        // unwrap movie pass in via intent, using its simple name as key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        ivTrailerImage = findViewById(R.id.ivTrailerImage);
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
                if(movie.getId()==null) {
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
    }
}
