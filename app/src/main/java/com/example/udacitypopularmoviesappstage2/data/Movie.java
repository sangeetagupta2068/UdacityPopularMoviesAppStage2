package com.example.udacitypopularmoviesappstage2.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "favourite_movies")
public class Movie {

    @PrimaryKey
    @NonNull
    private String movieId;

    private String movieName;
    private String movieReleaseDate;
    private String movieSummary;
    private String movieImageUrl;
    private String movieRating;


    public Movie(String movieName, String movieRating, String movieSummary, String movieImageUrl, String movieReleaseDate,String movieId) {
        this.movieImageUrl = movieImageUrl;
        this.movieName = movieName;
        this.movieRating = movieRating;
        this.movieSummary = movieSummary;
        this.movieId = movieId;
        this.movieReleaseDate = movieReleaseDate;
    }

    public String getMovieId(){ return movieId;}

    public String getMovieName() {
        return movieName;
    }

    public String getMovieSummary() {
        return movieSummary;
    }

    public String getMovieImageUrl() {
        return movieImageUrl;
    }

    public String getMovieRating() {
        return movieRating;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }
}
