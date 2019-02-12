package services;

import repositories.Repository;
import transferObjects.UserVO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Alexander Burghuber
 */
@Path("auth")
public class AuthenticationService {

    /**
     * Registers a new user
     *
     * @param user the Transfer Object of the User entity
     * @return a json that includes either the newly registered user or all validation errors
     */
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(UserVO user) {
        return Repository.getInstance().register(
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
        );
    }

    /**
     * Logs an user in
     *
     * @param user the Transfer Object of the User entity
     * @return a json that includes either the jwt token or an error
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public String login(UserVO user) {
        return Repository.getInstance().login(user.getUsername(), user.getPassword());
    }

    /**
     * Verifies the email of an user with an unique token that was sent with the email confirmation
     *
     * @param token the unique token
     * @return a Response that redirects the user
     */
    @Path("verify/{token}")
    @GET
    public Response verify(@PathParam("token") String token) {
        URI location = null;
        try {
            // when the right token was given, send the user to the login page telling him it was successful else tell him there was something wrong
            location = new URI("http://localhost:4200/login?token="
                    + Repository.getInstance()
                    .verify(token));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(location);
        return Response.temporaryRedirect(location).build();
    }
}