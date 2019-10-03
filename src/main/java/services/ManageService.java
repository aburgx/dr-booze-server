package services;

import data.dto.DrinkDTO;
import data.dto.InsertDetailsDTO;
import data.dto.UpdateDetailsDTO;
import repositories.AlcoholRepository;
import repositories.ChallengeRepository;
import repositories.UserRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("manage")
public class ManageService {
    private UserRepository userRepo = new UserRepository();
    private AlcoholRepository alcoholRepo = new AlcoholRepository();
    private ChallengeRepository challengeRepo = new ChallengeRepository();

    @Path("getUser")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return userRepo.getUser(getJwt(auth));
    }

    @Path("insertDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, InsertDetailsDTO person) {
        return userRepo.insertDetails(
                getJwt(auth),
                person.getFirstName(),
                person.getLastName(),
                person.getGender(),
                person.getBirthday(),
                person.getHeight(),
                person.getWeight()
        );
    }

    @Path("updateDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, UpdateDetailsDTO person) {
        return userRepo.updateDetails(
                getJwt(auth),
                person.getUsername(),
                person.getPassword(),
                person.getFirstName(),
                person.getLastName(),
                person.getGender(),
                person.getBirthday(),
                person.getHeight(),
                person.getWeight()
        );
    }

    @Path("getAlcohols/{type}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlcohols(@PathParam("type") String type) {
        return alcoholRepo.getAlcohols(type.toUpperCase());
    }

    @Path("addDrink")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDrink(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, DrinkDTO drinkDTO) {
        return alcoholRepo.addDrink(
                getJwt(auth),
                drinkDTO.getAlcoholId(),
                drinkDTO.getDrankDate(),
                drinkDTO.getLongitude(),
                drinkDTO.getLatitude()
        );
    }

    @Path("getDrinks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrinks(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return alcoholRepo.getDrinks(getJwt(auth));
    }

    @Path("manageChallenges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String manageChallenges(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return challengeRepo.challengeManager(getJwt(auth));
    }

    private String getJwt(String auth) {
        return auth.split("\\s")[1];
    }
}
