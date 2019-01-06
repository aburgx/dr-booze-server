package services;

import repositories.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @Path("test")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String testJPA() {
        return repository.test();
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
        return repository.register(credentials.getUsername(), credentials.getEmail(), credentials.getPassword());
    }

    @Path("verify/{token}")
    @GET
    public Response verify(@PathParam("token") String token) {
        return repository.verify(token);
    }

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String login(final LoginCredentials credentials) {
        return repository.login(credentials.getUsername(), credentials.getPassword());
    }

}

class LoginCredentials {

    private String username;
    private String email;
    private String password;

    LoginCredentials() {
    }

    LoginCredentials(String username, String password) {
        this.username = username.toLowerCase();
        this.password = password;
    }

    LoginCredentials(String username, String email, String password) {
        this.username = username.toLowerCase();
        this.email = email;
        this.password = password;
    }

    String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
