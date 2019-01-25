package services;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import repositories.AuthenticationRepo;

import java.io.IOException;
import java.net.URI;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexander Burghuber
 */
public class Main {

    public static final String BASE_URI = "http://localhost:8080/rest";

    public static void main(String[] args) throws IOException {
        // start the server
        final HttpServer server = startServer();

        /*
        unneeded right now
        static content - in project directory "public" are the html-files : localhost:8080/index.html
        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("public"), "/");
        */

        // activate error logging in the console
        Logger l = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
        l.setLevel(Level.FINE);
        l.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        l.addHandler(ch);
        
        // call AuthenticationRepo to start the entitymanager & the validator
        AuthenticationRepo.getInstance();

        System.out.println(String.format("Server starting at %s\nHit enter to stop ...", BASE_URI));
        System.in.read();
        server.shutdownNow();
    }

    private static HttpServer startServer() {
        // search all classes in the package "services" to find REST services
        final ResourceConfig rc = new ResourceConfig().packages("services", "filters");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
}
