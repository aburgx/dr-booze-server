package service;

import oracle.jdbc.proxy.annotation.Post;
import repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;

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
    public void register(ServiceUser serviceUser) {
        repository.register(serviceUser.getUsername(), serviceUser.getPassword(), serviceUser.getEmail());
    }

    @Path("login")
    @POST
    public String login(ServiceUser serviceUser) {
        // repository.login(serviceUser.getUsername(), serviceUser.getPassword());
        return "Not supported yet.";
    }

    @Path("testJPA")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        repository.testJPA();
        return "Testing completed.";
    }

}

class ServiceUser {

    private String username;
    private String password;
    private String email;

    public ServiceUser() {}

    public ServiceUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
