package services;

import objects.DataTransferObject;
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
    public String register(DataTransferObject dto) {
        return AuthenticationRepo.getInstance().register(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword()
        );
    }

    @Path("insertDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String insertDetails(DataTransferObject dto) {
        return AuthenticationRepo.getInstance().insertDetails(
                dto.getEmail(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getGender(),
                dto.getBirthday(),
                dto.getHeight(),
                dto.getWeight()
        );
    }

    @Path("updateDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateDetails(DataTransferObject dto) {
        return AuthenticationRepo.getInstance().updateDetails(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getGender(),
                dto.getBirthday(),
                dto.getHeight(),
                dto.getWeight()
        );
    }

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

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(DataTransferObject dto) {
        return AuthenticationRepo.getInstance().login(dto.getUsername(), dto.getPassword());
    }

}

