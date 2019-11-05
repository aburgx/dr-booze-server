package services;

import data.dto.ChangePasswordDTO;
import data.dto.UserDTO;
import repositories.UserRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("auth")
public class AuthenticationService {

    private UserRepository userRepo = new UserRepository();

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UserDTO user) {
        return userRepo.register(user.getUsername(), user.getEmail(), user.getPassword());
    }

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserDTO user) {
        return userRepo.login(user.getUsername(), user.getPassword());
    }

    @Path("verify/{token}")
    @GET
    public Response verify(@PathParam("token") String token) {
        if (userRepo.verify(token)) {
            return Response.ok("Successfully verified.").build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("Failure. The user might already be verified.").build();
    }

    @Path("request-password-change")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestPasswordChange(UserDTO user) {
        return userRepo.requestPasswordChange(user.getEmail());
    }

    @Path("change-password")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangePasswordDTO changePassword) {
        return userRepo.changePassword(changePassword.getPin(), changePassword.getPassword());
    }
}
