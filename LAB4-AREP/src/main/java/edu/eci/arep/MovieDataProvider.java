package edu.eci.arep;

/**
 * Interface for movie information providers
 *
 * @author Juan Felipe Vivas Manrique
 */
public interface MovieDataProvider {

    /**
     * Fetches movie data from the OMDb API using the given title value.
     *
     * @param titleValue The title of the movie to fetch data for.
     * @return The fetched movie data in JSON format, or null if an error occurs.
     */
    String fetchMovieData(String titleValue);

}
