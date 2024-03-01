package edu.eci.arep;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proveedor de información de películas (OMDb)
 *
 * @author Juan Felipe Vivas Manrique
 */
public class APIRestMovies implements MovieDataProvider {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://www.omdbapi.com/?apikey=f70773ff&t=";
    private static final Logger LOGGER = Logger.getLogger(APIRestMovies.class.getName());

    /**
     * Recupera datos de la película utilizando el servicio web OMDb.
     *
     * @param titleValue El título de la película para buscar información.
     * @return Una cadena que contiene la información de la película en formato JSON.
     */
    @Override
    public String fetchMovieData(String titleValue) {
        String outputLine = null;
        try {
            LOGGER.info(titleValue);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GET_URL + titleValue)).header("User-Agent", USER_AGENT).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            LOGGER.log(Level.INFO, "Código de respuesta GET :: {0}", responseCode);

            if (responseCode == 200) { 
                String movieInformation = response.body();
                LOGGER.log(Level.INFO, "Cuerpo de la respuesta: {0}", movieInformation);
                outputLine = movieInformation;
            } else {
                LOGGER.info("La solicitud GET no funcionó");
            }
            LOGGER.info("GET COMPLETADO");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al recuperar datos de la película: {0}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return outputLine;
    }
}
