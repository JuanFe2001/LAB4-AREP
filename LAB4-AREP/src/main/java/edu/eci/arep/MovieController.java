package edu.eci.arep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MovieController {

    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();
    private static final MovieDataProvider movieDataProvider = new APIRestMovies();

    @RequestMapping("/movies")
    public static String movies(Map<String, String> p) {
        MovieServer.responseType("application/json");
        return CACHE.computeIfAbsent(p.get("title"), movieDataProvider::fetchMovieData);
    }
}
