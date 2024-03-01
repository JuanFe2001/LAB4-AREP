package edu.eci.arep;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Map;

/**
 * Unit test for simple App.
 */
public class MovieServerTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MovieServerTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MovieServerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }


    public void testParseParams() {
        String queryString = "title=Guardians%20Of%20The%20Galaxy&year=2010";
        Map<String, String> params = MovieServer.parseParams(queryString);
        assertEquals(2, params.size());
        assertTrue(params.containsKey("title"));
        assertTrue(params.containsKey("year"));
        assertEquals("Guardians%20Of%20The%20Galaxy", params.get("title"));
        assertEquals("2010", params.get("year"));
    }

    public void testContentType() {
        assertEquals("image/png", MovieServer.contentType("cinema.png"));
        assertEquals("text/html", MovieServer.contentType("client.html"));
        assertEquals("text/css", MovieServer.contentType("client.css"));
        assertEquals("image/jpeg", MovieServer.contentType("cinema.jpg"));
    }

    public void testHttpHeader() {
        String header = "HTTP/1.1 200 OK\r\nContent-Type:image/png\r\n\r\n";
        assertEquals(header, MovieServer.httpHeader("image/png").toString());
    }

    public void testFetchMovieData() {
        MovieDataProvider movieDataProvider = new APIRestMovies();
        String movieData = movieDataProvider.fetchMovieData("Guardians%20Of%20The%20Galaxy");
        assertTrue(movieData.contains("{\"Title\":\"Guardians of the Galaxy\",\"Year\":\"2014\""));
    }

    public void testStaticDirectory() {
        MovieServer.staticDirectory("public/test");
        assertEquals("target/classes/public/test", MovieServer.getDirectory());
    }

    public void testResponseType() {
        MovieServer.responseType("application/json");
        assertEquals("application/json", MovieServer.getContentType());
    }
}
