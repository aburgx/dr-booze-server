package service;

import repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Alexander Burghuber
 */
@Path("booze")
public class Service {

    private Repository repository = new Repository();

    @Path("message")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String message() {
        return "Hello REST Service powered by Java SE.";
    }

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String register(LoginCredentials credentials) {
        return repository.register(credentials.getUsername(), credentials.getEmail(), credentials.getPassword());
    }

    @Path("login")
    @POST
    public String login(LoginCredentials serviceUser) {
        return "Not supported yet.";
    }

}

class LoginCredentials {

    private String username;
    private String email;
    private String password;

    LoginCredentials() {}

    LoginCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    LoginCredentials(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

}
