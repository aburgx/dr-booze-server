package services;

import data.dto.DetailsDTO;
import data.dto.DrinkDTO;
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
    public Response setDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, DetailsDTO details) {
        return userRepo.setDetails(
                getJwt(auth),
                details.getFirstName(),
                details.getLastName(),
                details.getGender(),
                details.getBirthday(),
                details.getHeight(),
                details.getWeight()
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

    @Path("drinks/{id}")
    @DELETE
    @Consumes
    public Response removeDrink(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("id") long drinkId) {
        return alcoholRepo.removeDrink(getJwt(auth), drinkId);
    }

    @Path("favourites/{type}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFavouritesOfType(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("type") String type) {
        return alcoholRepo.getFavouritesOfType(getJwt(auth), type.toUpperCase());
    }

    @Path("favourites/{id}")
    @POST
    public Response addFavourite(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("id") long alcoholId) {
        return alcoholRepo.addFavourite(getJwt(auth), alcoholId);
    }

    @Path("favourites/{id}")
    @DELETE
    public Response removeFavourite(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("id") long alcoholId) {
        return alcoholRepo.removeFavourite(getJwt(auth), alcoholId);
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
