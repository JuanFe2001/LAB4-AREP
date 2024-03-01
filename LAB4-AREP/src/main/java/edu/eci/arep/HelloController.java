package edu.eci.arep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HelloController {

    @RequestMapping("/hola")
    public static String hola(Map<String, String> p) {
        MovieServer.responseType("text/html");
        return "<h1>Hola " + p.get("nombre") + "</h1>";
    }


}
