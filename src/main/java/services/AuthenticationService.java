package services;

import data.dto.UpdatePasswordDTO;
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
    public String verify(@PathParam("token") String token) {
        if (userRepo.verify(token)) {
            return "Successfully verified.";
        }
        return "Failure. The user might already be verified.";
    }

    @Path("requestPasswordChange")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestPasswordChange(UserDTO user) {
        return userRepo.requestPasswordChange(user.getEmail());
    }

    @Path("updatePassword")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(UpdatePasswordDTO updatePassword) {
        return userRepo.updatePassword(updatePassword.getPin(), updatePassword.getPassword());
    }
}
