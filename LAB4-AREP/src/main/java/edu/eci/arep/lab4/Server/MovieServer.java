package edu.eci.arep.lab4.Server;

import com.google.gson.JsonElement;
import java.io.*;
import java.net.*;

import com.google.gson.JsonObject;
import edu.eci.arep.lab1.Service.MovieService;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase del servidor web para la aplicación web.
 *
 * @author Juan Felipe Vivas
 */
public class MovieServer {

    private static final int PORT = 35000;
    private static final APIRestMovies apf = new APIRestMovies();
    private static MovieServer instance;
    public static String URIStaticFileBase;
    private static Map<String, MovieService> services = new HashMap<String, MovieService>();
    private static MovieServer _instance = getInstace();

    public static MovieServer getInstance() {
        if (instance == null) {
            instance = new MovieServer();
        }
        return instance;
    }

    /**
     * Constructor por defecto.
     */
    public MovieServer() {
    }

    /**
     * Método para iniciar el servidor web.
     *
     * @throws IOException si ocurre un error de entrada/salida
     */
    public static void startServer() throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine = "";
            boolean readingFirst = true;
            String petition = "";
            String method = "";
            Boolean searchMovie = false;

            while ((inputLine = in.readLine()) != null) {

                if (readingFirst) {
                    if (inputLine.contains("GET")) {
                        method = "GET";
                        petition = inputLine.split(" ")[1];
                        break;
                    } else if (inputLine.contains("POST")) {
                        method = "POST";
                        petition = inputLine.split(" ")[1];
                        break;
                    }
                }
                if (!in.ready()) {
                    break;
                }
            }
            System.out.println("Metodo de petición: " + method);
            // System.out.println("Asi llego la petición: " + petition);
            searchMovie = (petition.contains("/film?name=")) ? true : false;

            try {
                URI requestUri = new URI(petition);
                String path = requestUri.getPath();
                String query = requestUri.getQuery();

                query = (query != null) ? query.split("=")[1] : "";

                if (path.startsWith("/service")) {
                    String webUri = path.replace("/service", "");
                    // System.out.println("webUri obtenida despues del replace de /serivce: " + webUri);

                    outputLine = (services.containsKey(webUri))
                            ? petitionPage(services.get(webUri).handle(query), clientSocket.getOutputStream()).replace("{query}", query)
                            : ((webUri.contains("css") || webUri.contains("jpg") || webUri.contains("js"))
                                    ? petitionPage(webUri, clientSocket.getOutputStream())
                                    : errorPage("/NotFound.html", clientSocket.getOutputStream()));
                } else {
                    outputLine = (searchMovie)
                            ? movieInformation(query, clientSocket.getOutputStream())
                            : petitionPage(petition, clientSocket.getOutputStream());
                }

            } catch (Exception e) {
                System.out.println("An error happened: " + e.getMessage());
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    /**
     * Lee la primera línea de un BufferedReader.
     *
     * @param in el BufferedReader de entrada
     * @return la primera línea
     * @throws IOException si ocurre un error de entrada/salida
     */
    private static String readFirstLine(BufferedReader in) throws IOException {
        String inputLine = null;
        boolean readingFirst = true;

        while ((inputLine = in.readLine()) != null) {
            if (readingFirst) {
                return inputLine.split(" ")[1];
            }
            if (!in.ready()) {
                break;
            }
            readingFirst = false;
        }

        return null;
    }
    
     public static MovieServer getInstace() {
        return _instance;
    }

    /**
     * Retorna una estructura HTML con información de la película.
     *
     * @param name el nombre de la película
     * @return una estructura HTML con información de la película y encabezados
     */
    private static String movieInformation(String name, OutputStream op) {
        try {
            JsonObject resp = apf.searchMovie(name);
            JsonElement title = resp.get("Title"), poster = resp.get("Poster"), director = resp.get("Director"), language = resp.get("Language"),
                    plot = resp.get("Plot"), genre = resp.get("Genre"),
                    released = resp.get("Released");

            String outputLine = getOKHeader() + "text/html\r\n"
                    + "Content-Type:text/html\r\n"
                    + "\r\n"
                    + getStaticFile("/movieInfo.html", op).replace("{Title}", title.toString())
                            .replace("\"{Poster}\"", poster.toString()).replace("{Directors}", director.toString()).replace("{Language}", language.toString()).replace("{Released}", released.toString())
                            .replace("{Plot}", plot.toString()).replace("{Genre}", genre.toString());

            return outputLine;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
     private static String errorPage(String filePetition, OutputStream op) {
        return getNotFoundHeader() + getMimeType(filePetition) + "\r\n"
                + "\r\n"
                + getStaticFile(filePetition, op);
    }
    
    private static String petitionPage(String filePetition, OutputStream op) {
        
        return getOKHeader() + getMimeType(filePetition) + "\r\n"
                + "\r\n"
                + getStaticFile(filePetition, op);
    }


    /**
     * Retorna la página HTML principal.
     *
     * @return la página principal de la aplicación
     */
    private static String homePage(String filePetition, OutputStream op) {

        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:" + getMimeType(filePetition) + "\r\n"
                + "\r\n"
                + getStaticFile(filePetition, op);

        return outputLine;

    }

    /**
     * Método para obtener el tipo MIME de un archivo basado en su extensión o
     * en la petición del archivo.
     *
     * @param filePetition Una cadena que representa la petición de un archivo,
     * generalmente el nombre del archivo o una ruta.
     * @return Un valor de cadena que representa el tipo MIME del archivo.
     */
    private static String getMimeType(String filePetition) {
        return (filePetition.endsWith(".html") || filePetition.endsWith("/")) ? "text/html"
                : ((filePetition.endsWith(".css")) ? "text/css"
                : (filePetition.endsWith(".js")) ? "application/javascript" : (filePetition.endsWith(".jpg")) ? "image/jp2" : "text/plain");
    }

    /**
     * Método para obtener el contenido de un archivo estático basado en la
     * petición del archivo.
     *
     * @param filePetition Una cadena que representa la petición de un archivo
     * estático, generalmente el nombre del archivo o una ruta.
     * @return Un valor de cadena que representa el contenido del archivo
     * estático.
     */
    private static String getStaticFile(String filePetition, OutputStream op) {
        Path file = (filePetition.equals("/")) ? Paths.get("src/resource/public/static/client.html")
                : Paths.get("src/resource/public/static" + filePetition);

        System.out.println(filePetition);
        Charset charset = Charset.forName("ISO_8859_1");
        StringBuilder outputLine = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (filePetition.contains(".jpg")) {
                    byte[] imageBytes = getAnImage(filePetition);
                    String response = getOKHeader() + getMimeType(filePetition) + "\r\n"
                            + "Content-Length: " + imageBytes.length + "\r\n"
                            + "\r\n";
                    op.write(response.getBytes());
                    op.write(imageBytes);
                }
                outputLine.append(line).append("\n");

            }
        } catch (Exception e) {
            System.err.format(e.getMessage(), e);
            // e.printStackTrace();
        }

        return outputLine.toString();
    }

    /**
     * Obtiene los bytes de una imagen ubicada en el sistema de archivos.
     *
     * @param filePetition Ruta del archivo de imagen, relativa al directorio
     * "target/classes/public/static".
     * @return Un array de bytes que representa los datos de la imagen. Retorna
     * null si hay algún problema al leer el archivo.
     */
    private static byte[] getAnImage(String filePetition) {

        Path image = Paths.get("src/resource/public/static" + filePetition);

        try {
            return Files.readAllBytes(image);
        } catch (Exception e) {
            System.out.println("can't send the image: " + e.getMessage());;
        }
        return null;
        
    }
    
      public static void get(String r, MovieService ms) {
        services.put(r, ms);
    }

    private static String getOKHeader() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: ";
    }

    /**
     * Method that returns the Not Found header
     *
     * @return a String with not found header
     */
    private static String getNotFoundHeader() {
        return "HTTP/1.1 404 NOT FOUND\r\n"
                + "Content-Type: ";
    }

    public static void setStaticFile(String path) {
        URIStaticFileBase = path;
    }

}
