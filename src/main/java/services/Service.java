package services;

import objects.LoginCredentials;
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

    @Path("test")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String testJPA() {
        return AuthenticationRepo.getInstance().test();
    }

    /**
     * @param credentials a Json String that includes an username, an email and a password
     * @return a Json String that includes either the newly registered user or all validation errors
     */
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(final LoginCredentials credentials) {
        return AuthenticationRepo.getInstance().register(credentials.getUsername(), credentials.getEmail(), credentials.getPassword());
    }

    /**
     * @param token the unique string that is a pathparam of the url inside the verification email
     * @return either a success or failure response
     */
    @Path("verify/{token}")
    @GET
    public Response verify(@PathParam("token") String token) {
        URI location = null;
        // when the right token was given, send the user to the login page telling him it was successful else tell him there was something wrong
        try {
            location = new URI("http://localhost:4200/login?token=" + AuthenticationRepo.getInstance().verify(token));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(location);
        return Response.temporaryRedirect(location).build();

    }

    /**
     * @param credentials a Json string that includes the username and the password
     * @return a Json String that includes either the logged in user or login errors
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String login(final LoginCredentials credentials) {
        return AuthenticationRepo.getInstance().login(credentials.getUsername(), credentials.getPassword());
    }

}

