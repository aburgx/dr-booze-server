package services;

import objects.LoginCredentials;
import oracle.jdbc.proxy.annotation.Post;
import repositories.AuthenticationRepo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Alexander Burghuber
 */
@Path("booze")
public class Service {

    @Path("message")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String message() {
        return "Hello REST Service powered by Java SE.";
    }

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(final LoginCredentials credentials) {
        return AuthenticationRepo.getInstance().register(credentials.getUsername(), credentials.getEmail(), credentials.getPassword());
    }

    @Path("verify/{token}")
    @GET
    public Response verify(@PathParam("token") String token) {
        URI location = null;
        try {
            // when the right token was given, send the user to the login page telling him it was successful else tell him there was something wrong
            location = new URI("http://localhost:4200/login?token=" + AuthenticationRepo.getInstance().verify(token));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(location);
        return Response.temporaryRedirect(location).build();
    }

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String login(final LoginCredentials credentials) {
        return AuthenticationRepo.getInstance().login(credentials.getUsername(), credentials.getPassword());
    }

}

