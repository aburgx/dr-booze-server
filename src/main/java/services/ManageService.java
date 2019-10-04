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

    @Path("user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return userRepo.getUser(getJwt(auth));
    }

    @Path("details")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, InsertDetailsDTO insertDetails) {
        return userRepo.insertDetails(
                getJwt(auth),
                insertDetails.getFirstName(),
                insertDetails.getLastName(),
                insertDetails.getGender(),
                insertDetails.getBirthday(),
                insertDetails.getHeight(),
                insertDetails.getWeight()
        );
    }

    @Path("details")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, UpdateDetailsDTO updateDetails) {
        return userRepo.updateDetails(
                getJwt(auth),
                updateDetails.getUsername(),
                updateDetails.getPassword(),
                updateDetails.getFirstName(),
                updateDetails.getLastName(),
                updateDetails.getGender(),
                updateDetails.getBirthday(),
                updateDetails.getHeight(),
                updateDetails.getWeight()
        );
    }

    @Path("alcohols/{type}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlcohols(@PathParam("type") String type) {
        return alcoholRepo.getAlcohols(type.toUpperCase());
    }

    @Path("drinks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrinks(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return alcoholRepo.getDrinks(getJwt(auth));
    }

    @Path("drinks")
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

    @Path("challenges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String manageChallenges(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return challengeRepo.challengeManager(getJwt(auth));
    }

    private String getJwt(String auth) {
        return auth.split("\\s")[1];
    }
}
