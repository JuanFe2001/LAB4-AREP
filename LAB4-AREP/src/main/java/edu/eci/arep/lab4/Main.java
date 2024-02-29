/*
 * Clase principal para iniciar la aplicación del servidor de películas.
 */
package edu.eci.arep.lab4;

import java.io.IOException;

import edu.eci.arep.lab4.Server.MovieServer;

import com.google.gson.*;

/**
 * Clase principal para iniciar la aplicación del servidor de películas.
 *
 * @author Juan Felipe Vivas
 */

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal para iniciar el servidor de películas.
 *
 * @author Juan Felipe Vivas
 */
public class Main {

    /**
     * Constructor por defecto de la clase Main.
     */
    public Main() {

    }

    /**
     * Método principal para iniciar la aplicación.
     *
     * @param args argumentos para iniciar la aplicación
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        try {

            // adding arsw service to the server
            MovieServer.get("/prueba2", (m) -> {
                return "/prueba2.html";
            });

            // adding arep service to the server
            MovieServer.get("/prueba1", (m) -> {
                return "/prueba1.html";
            });


            MovieServer.getInstace().startServer();
        } catch (IOException e) {
            System.out.println("Error al iniciar el server: "+ e.getMessage());
        }
        
    }
}
