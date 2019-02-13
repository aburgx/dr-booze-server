package services;

import repositories.Repository;
import transferObjects.DrinkVO;
import transferObjects.PersonVO;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Alexander Burghuber
 */
@Path("manage")
public class ManageService {

    /**
     * @param authHeader the HTTP-Header that includes an Authorization with the jwt
     * @return the person of an user or an error
     */
    @Path("getPerson")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getPerson(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
        String[] auth = authHeader.split("\\s");
        return Repository.getInstance().getPerson(auth[1]);
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
        return Repository.getInstance().insertDetails(
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
     * @param person the Transfer Object of the Person entity
     * @return a json that includes either the user and person object or an error
     */
    @Path("updateDetails")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateDetails(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, PersonVO person) {
        String[] auth = authHeader.split("\\s");
        return Repository.getInstance().updateDetails(
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

    @Path("addDrink")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDrink(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, DrinkVO drink) {
        String[] auth = authHeader.split("\\s");
        return Repository.getInstance().addDrink(
                auth[1],
                drink.getId(),
                drink.getType(),
                drink.getUnixTime()
        );
    }

}

