package services;

import transferObjects.PersonVO;
import transferObjects.UserVO;
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
public class RestService {

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
        return AuthenticationRepo.getInstance().register(
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
        );
    }

    /**
     * Logs an user in
     *
     * @param user the Transfer Object of the User entity
     * @return a json that includes either the user and/or the person or an error
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(UserVO user) {
        return AuthenticationRepo.getInstance().login(user.getUsername(), user.getPassword());
    }

    /**
     * Inserts the details(firstName, lastName, gender, etc.) of an already existing user
     *
     * @param person the Transfer Object of the Person entity
     * @return a json that includes either the user and person object or an error
     */
    @Path("insertDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String insertDetails(PersonVO person) {
        return AuthenticationRepo.getInstance().insertDetails(
                person.getEmail(),
                person.getFirstName(),
                person.getLastName(),
                person.getGender(),
                person.getBirthday(),
                person.getHeight(),
                person.getWeight()
        );
    }

    /**
     * Updates the details of a person
     *
     * @param person the Transfer Object of the Person entity
     * @return a json that includes either the user and person object or an error
     */
    @Path("updateDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateDetails(PersonVO person) {
        return AuthenticationRepo.getInstance().updateDetails(
                person.getUsername(),
                person.getEmail(),
                person.getPassword(),
                person.getFirstName(),
                person.getLastName(),
                person.getGender(),
                person.getBirthday(),
                person.getHeight(),
                person.getWeight()
        );
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
                    + AuthenticationRepo.getInstance()
                    .verify(token)
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(location);
        return Response.temporaryRedirect(location).build();
    }

}

