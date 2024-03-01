package edu.eci.arep;



import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Application to consult information about movies.
 *
 * @author Juan Felipe Vivas Manrique
 * 
 * 
 */

public class MovieServer {
    private static final MovieServer _instance = new MovieServer();
    protected static HashMap<String, Method> components = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(MovieServer.class.getName());
    private static String directory = "target/classes/public";
    private static String contentType = "text/html";

    private MovieServer() {
        // Constructor privado para prevenir la instanciación directa de la clase.
        // Enforces the use of the singleton pattern.
    }

    /**
     * Retorna la instancia única de la clase MovieServer (implementa el patrón singleton).
     *
     * @return La instancia de MovieServer.
     */
    public static MovieServer getInstance() {
        return _instance;
    }

    /**
     * Inicia el servidor y comienza a escuchar las solicitudes de los clientes.
     *
     * @param args Argumentos de línea de comandos (no utilizado en este contexto).
     * @throws URISyntaxException            Si ocurre un error en la sintaxis de la URI.
     * @throws InvocationTargetException    Si ocurre un error al invocar un método.
     * @throws IllegalAccessException       Si ocurre un error al acceder a un miembro de la clase de manera ilegal.
     */
    public static void main(String[] args) throws URISyntaxException, InvocationTargetException, IllegalAccessException {
        
        Reflections reflections = new Reflections("edu.eci.arep");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);

        
        for (Class<?> c : classes) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    components.put(method.getAnnotation(RequestMapping.class).value(), method);
                }
            }
        }

        
        try (ServerSocket serverSocket = new ServerSocket(35000)) {
            while (true) {
                LOGGER.info("Listo para recibir ...");
                handleClientRequest(serverSocket.accept());
            }
        } catch (IOException e) {
            LOGGER.info("No se pudo escuchar en el puerto: 35000.");
            System.exit(1);
        }
    }

    /**
     * Maneja una solicitud de cliente individual.
     *
     * @param clientSocket El objeto Socket que representa la conexión del cliente.
     * @throws URISyntaxException            Si ocurre un error en la sintaxis de la URI.
     * @throws InvocationTargetException    Si ocurre un error al invocar un método.
     * @throws IllegalAccessException       Si ocurre un error al acceder a un miembro de la clase de manera ilegal.
     */
    public static void handleClientRequest(Socket clientSocket) throws URISyntaxException, InvocationTargetException, IllegalAccessException {
        try (OutputStream outputStream = clientSocket.getOutputStream();
             PrintWriter out = new PrintWriter(outputStream, true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String requestLine = in.readLine();
            LOGGER.log(Level.INFO, "Recibido: {0}", requestLine);

            if (requestLine != null) {
                URI fileUrl = new URI(requestLine.split(" ")[1]);
                String params = fileUrl.getRawQuery();
                String path = fileUrl.getPath();
                LOGGER.log(Level.INFO, "Ruta: {0}", path);
                String outputLine;

                
                if (path.startsWith("/element")) {
                    String webUri = path.replace("/element", "");
                    if (components.containsKey(webUri)) {
                        Map<String, String> parameters = parseParams(params);
                        if (components.get(webUri).getParameterCount() == 0) {
                            outputLine = (String) components.get(webUri).invoke(null);
                        } else {
                            outputLine = (String) components.get(webUri).invoke(null, parameters);
                        }
                        out.println(httpHeader(contentType).append(outputLine));
                    }
                } else if (path.contains(".")) {
                   
                    String contentType = contentType(path);
                    if (contentType.contains("image")) {
                        outputStream.write(httpClientImage(path));
                    } else {
                        out.println(httpClientFiles(path));
                    }
                } else {
                  
                    out.println(httpClientError());
                }
            }

           
            clientSocket.close();
        } catch (IOException e) {
            LOGGER.info("Error al aceptar la conexión del cliente.");
            System.exit(1);
        }
    }

    /**
     * Recupera un archivo especificado del directorio "target/classes/public" y construye una respuesta HTTP.
     *
     * @return Una cadena que contiene la respuesta HTTP, incluidas las cabeceras y el contenido del archivo.
     */
    public static String httpClientError() {
        StringBuilder outputLine = new StringBuilder();
        outputLine.append("HTTP/1.1 404 Not Found\r\n");
        outputLine.append("Content-Type:text/html\r\n\r\n");
        Path file = Paths.get(directory + "/error.html");
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLine.append(line);
            }
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return outputLine.toString();
    }

    /**
     * Analiza los parámetros de la consulta desde la cadena de consulta dada.
     *
     * @param queryString La cadena de consulta que contiene pares clave-valor.
     * @return Un Map que contiene los parámetros analizados, donde las claves son los nombres de los parámetros y los valores son los valores de los parámetros.
     */
    public static Map<String, String> parseParams(String queryString) {
        if (queryString != null) {
            Map<String, String> params = new HashMap<>();
            for (String param : queryString.split("&")) {
                String[] nameValue = param.split("=");
                params.put(nameValue[0], nameValue[1]);
            }
            return params;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Recupera un archivo especificado del directorio "target/classes/public" y construye una respuesta HTTP.
     *
     * @param path La ruta al archivo, incluida su extensión.
     * @return Una cadena que contiene la respuesta HTTP, incluidas las cabeceras y el contenido del archivo.
     */
    public static String httpClientFiles(String path) {
        StringBuilder outputLine = httpHeader(contentType(path));
        Path file = Paths.get(directory + path);
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLine.append(line);
            }
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return outputLine.toString();
    }

    /**
     * Recupera una imagen especificada del directorio "target/classes/public" y construye una respuesta HTTP.
     *
     * @param path La ruta al archivo, incluida su extensión.
     * @return Un array de bytes que contiene la respuesta HTTP, incluidas las cabeceras y el contenido de la imagen.
     */
    public static byte[] httpClientImage(String path) {
        Path file = Paths.get(directory + path);
        byte[] imageData = null;
        try {
            imageData = Files.readAllBytes(file);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        byte[] headerBytes = httpHeader(contentType(path)).toString().getBytes();
        assert imageData != null;
        int totalLength = headerBytes.length + imageData.length;
        byte[] combinedBytes = new byte[totalLength];
        System.arraycopy(headerBytes, 0, combinedBytes, 0, headerBytes.length);
        System.arraycopy(imageData, 0, combinedBytes, headerBytes.length, imageData.length);
        return combinedBytes;
    }

    /**
     * Construye la cabecera de respuesta HTTP en función de la extensión de archivo proporcionada.
     *
     * @param contentType El tipo de contenido del archivo.
     * @return Un StringBuilder que contiene la cabecera de respuesta HTTP.
     */
    public static StringBuilder httpHeader(String contentType) {
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 200 OK\r\n");
        header.append("Content-Type:").append(contentType).append("\r\n");
        header.append("\r\n");
        return header;
    }

    /**
     * Determina el tipo de contenido del archivo en función de su ruta.
     *
     * @param path La ruta al archivo.
     * @return El tipo de contenido del archivo, o una cadena vacía si no se pudo determinar el tipo de contenido.
     */
    public static String contentType(String path) {
        File file = new File(path);
        String contentType = "";
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return contentType;
    }

    /**
     * Establece el directorio para servir archivos estáticos.
     *
     * @param directoryPath La ruta al directorio que contiene archivos estáticos.
     */
    public static void staticDirectory(String directoryPath) {
        directory = "target/classes/" + directoryPath;
    }

    /**
     * Establece el tipo de respuesta para las respuestas del servicio web subsiguientes.
     *
     * @param responseType El tipo de contenido a utilizar para la respuesta, como "application/json" o "text/html".
     */
    public static void responseType(String responseType) {
        contentType = responseType;
    }

    /**
     * Obtiene el directorio actualmente configurado para servir archivos estáticos.
     *
     * @return La ruta del directorio actual.
     */
    public static String getDirectory() {
        return directory;
    }

    /**
     * Obtiene el tipo de contenido actualmente configurado para las respuestas del servicio web.
     *
     * @return El tipo de contenido actual.
     */
    public static String getContentType() {
        return contentType;
    }
}