package com.example.udacitypopularmoviesappstage2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private final MovieListItemHandler movieListItemHandler;

    MovieAdapter(Context context, MovieListItemHandler movieListItemHandler) {
        this.context = context;
        this.movieListItemHandler = movieListItemHandler;
    }

    public interface MovieListItemHandler {
        void onMovieItemClick(int index);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.generateMoviePoster(position);
    }

    @Override
    public int getItemCount() {
        if (MainActivity.movies == null) {
            return 0;
        }
        return MainActivity.movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView moviePosterView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            moviePosterView = itemView.findViewById(R.id.movie_poster);
            itemView.setOnClickListener(this);
        }

        public void generateMoviePoster(int position) {
            Picasso.with(context)
                    .load(MainActivity.movies.get(position).getMovieImageUrl())
                    .into(moviePosterView);
            Log.d("Image test1", moviePosterView.toString());
            Log.d("size of movie:", String.valueOf(MainActivity.movies.size()));
        }

        @Override
        public void onClick(View view) {
            int index = getAdapterPosition();
            movieListItemHandler.onMovieItemClick(index);
        }
    }

}

