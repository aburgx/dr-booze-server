import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import repositories.ChallengeRepository;
import repositories.Repository;
import utils.Constants;

import java.io.IOException;
import java.net.URI;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        // activate error logging in the console
        Logger l = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
        l.setLevel(Level.FINE);
        l.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        l.addHandler(ch);

        // load alcohol and the challenge templates into the database
        new Repository().loadAlcohol();
        new ChallengeRepository().loadTemplates();

        System.out.println(String.format("Server starting at %s\nHit enter to stop ...", Constants.BASE_URI));
        System.in.read();
        server.shutdownNow();
    }

    private static HttpServer startServer() {
        // search all classes in the package "services" to find REST services
        final ResourceConfig rc = new ResourceConfig().packages("services", "filters");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(Constants.BASE_URI), rc);
    }
}