package com.example.flicks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flicks.models.Config;
import com.example.flicks.models.Movie;

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{
    // list of movies
    ArrayList<Movie> movies;
    // config needed for image urls
    Config config;
    // context resolved from parent, for rendering, must be available to adapter
    Context context;

    // initialize with list
    public MovieAdapter(ArrayList<Movie> movies) {
        this.movies = movies;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    // creates/inflates a new view
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // get context and create inflater
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // use inflater to create view object, return wrapped with new viewholder class
        // create view using the item_movie layout
        View movieView = inflater.inflate(R.layout.item_movie, parent, false);
        // return view wrapped by ViewHolder
        return new ViewHolder(movieView);
    }

    // binds an inflated view to a new item
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get movie data at specified position
        Movie movie = movies.get(position);
        // populate view with movie data
        holder.tvTitle.setText(movie.getTitle());
        holder.tvOverview.setText(movie.getOverview());

        // determine current orientation
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // build url for poster image
        String imageUrl = null;

        // if portrait mode, load poster image
        if (isPortrait) {
            imageUrl = config.getImageUrl(config.getPosterSize(), movie.getPosterPath());
        } else {
            // load backdrop image
            imageUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
        }

        // get correct placeholder/imageview for current orientation
        int placeholderId = isPortrait? R.drawable.flicks_movie_placeholder : R.drawable.flicks_backdrop_placeholder;
        ImageView imageView = isPortrait ? holder.ivPosterImage : holder.ivBackdropImage;

        // load image using Glide
        // transformations all require handle to context + value for radius/margin
        // larger value for radius = rounder/larger corner
        // margin = amount cropped from photo edges
        Glide.with(context)
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(context, 25, 0))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(imageView);
    }

    // returns total number of items in the list
    @Override
    public int getItemCount() {
        return movies.size();
    }

    // create viewholder as inner class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // track view objects
        ImageView ivPosterImage;
        ImageView ivBackdropImage;
        TextView tvTitle;
        TextView tvOverview;

        public ViewHolder(View itemView) {
            super(itemView);
            // lookup view items by id
            ivPosterImage = (ImageView) itemView.findViewById(R.id.ivPosterImage);
            ivBackdropImage = (ImageView) itemView.findViewById(R.id.ivBackdropImage);
            tvOverview = (TextView) itemView.findViewById(R.id.tvOverview);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            // add itemView's OnClickListener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // get item position
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                // get movie at position. doesn't work if class is static
                Movie movie = movies.get(position);
                // create intent to display MovieDetailsActivity
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                // pass movie as extra serialized via Parcels.wrap(). use its short name as key
                intent.putExtra(Movie.class.getSimpleName(), Parcels.wrap(movie));
                // pass full movie trailer image url
                String trailerUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
                intent.putExtra("trailer_url", trailerUrl);
                // show activity
                context.startActivity(intent);
            }
        }
    }
}
