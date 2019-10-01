package services;

import data.transferobjects.DrinkVO;
import data.transferobjects.PersonVO;
import repositories.ChallengeRepository;
import repositories.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Alexander Burghuber
 */
@Path("manage")
public class ManageService {
    private Repository repo = new Repository();
    private ChallengeRepository challengeRepo = new ChallengeRepository();

    /**
     * @param authHeader the HTTP-Header that includes an Authorization with the jwt
     * @return the person of an user or an error
     */
    @Path("getPerson")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getPerson(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
        String[] auth = authHeader.split("\\s");
        return repo.getPerson(auth[1]);
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
    public String insertDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, PersonVO person) {
        String[] auth = authHeader.split("\\s");
        return repo.insertDetails(
                auth[1],
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
     * @param person     the Transfer Object of the Person entity
     * @param authHeader the Http-Header
     * @return a json that includes either the user and person object or an error
     */
    @Path("updateDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, PersonVO person) {
        String[] auth = authHeader.split("\\s");
        return repo.updateDetails(
                auth[1],
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
     * Adds a drink to an user
     *
     * @param authHeader the Http-Header
     * @param drinkVO      the drink the user drank
     * @return a Http-Response
     */
    @Path("addDrink")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDrink(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, DrinkVO drinkVO) {
        String[] auth = authHeader.split("\\s");
        return repo.addDrink(auth[1], drinkVO);
    }

    /**
     * Returns all drinks from a user
     *
     * @param authHeader the Http-Header
     * @return a json that includes all drinks from a user
     */
    @Path("getDrinks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrinks(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
        String[] auth = authHeader.split("\\s");
        return repo.getDrinks(auth[1]);
    }

    /**
     * Return the 3 challenges a user has
     *
     * @param authHeader the Http-Header
     * @return a json that includes the challenges from a user
     */
    @Path("manageChallenges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String manageChallenges(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
        String[] auth = authHeader.split("\\s");
        return challengeRepo.challengeManager(auth[1]);
    }

}